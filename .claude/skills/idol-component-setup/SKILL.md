---
name: idol-component-setup
description: How to install, license, configure, start, verify and stop an OpenText IDOL / Knowledge Discovery ACI component (Content, Agentstore, Community, View, Category, QMS, DAH/DIH and others) from its product zip, on Linux or Windows, as a process or a service. Covers the License Server connection, config file anatomy, ports, component dependencies and common failures. Use whenever an IDOL component needs setting up, licensing, starting/stopping or troubleshooting, whatever the install layout or tooling available.
---

# Setting up an IDOL component

Every Knowledge Discovery server component (Content, Community, View, Category, QMS, …) is an **ACI server**:
- a zip with an executable (`content.exe`, also on Linux), a `.cfg` file of the same name, and supporting folders;
- it talks HTTP on an **ACI port** (queries and admin), a **service port** (stop, status) and sometimes an **index port** (Content);
- it gets its **licence from a License Server**.

The same procedure works for all of them. The checklist below has what each common component needs; `reference/components.md` has the detail.

## Rules (read first)

1. **Install every component the goal needs; dependencies cannot be switched off.** If something won't start, the fix is almost always a missing step from the checklist below (a key file, a dependency to start first, a port), not disabling its config. If a dependency really can't be installed, stop and tell the user.
2. **Secrets stay secret.**
   - Generate random passwords (16+ letters and digits) and keys.
   - Store them only in a `chmod 600` file.
   - Never echo or print them, or put them in commands you show, reports or messages.
   - When reading a credentials file back, extract the fields you need without printing it.
   - Never reuse a generated secret (such as Find's default admin password) for something else.
3. **Use `127.0.0.1`, not `localhost`**, for every local request and every component-to-component host setting (see step 6).
4. **Stop on the service port, and find processes by their port.** Never pattern-match on process names or command lines.
5. **Back up a config file before editing it** (`cp file file.orig`), and verify each component (step 6) before starting the next.

## Component checklist: what each needs before its first start

| Component | Must do before first start | Start after |
|---|---|---|
| **Content** | Licence (`[License]`), ports, a database for your data (`[Databases]`) | License Server |
| **Agentstore** (needed by Community) | **Its own fresh extract of the Content zip**, with `alt_configs/agentstore.cfg` copied over `content.cfg`. Licence, ports (default 9050/9051/9052) | License Server |
| **Community** (needed by Find for login) | 1. Licence and ports. 2. **Generate an AES key file** (stock config points at a placeholder, and Community exits with "Failed to initialise security"): `autpassword.exe -x -tAES -oKeyFile=<abs path>` from the Community folder (Linux: `LD_LIBRARY_PATH=. ./autpassword.exe …`). It prints the key, so don't copy the output anywhere. Set `[Security] SecurityInfoKeys=<abs path>` and `chmod 600` the file. 3. `[DataDre]` → Content and **`[AgentDre]` → the Agentstore**: host `127.0.0.1` and their ACI ports. Community requires a running Agentstore and logs "could not establish connection with Agent DRE" until one is available; the section cannot be commented out | Content **and** Agentstore |
| **View** | Licence and ports. `[Viewing] IDOLHost=127.0.0.1`, `IDOLPort=<Content ACI port>`. The default 9060 is a DAH port. Linux: `LD_LIBRARY_PATH=.:./filters` | Content |
| Others (Category, QMS, …) | See `reference/components.md` | their dependencies |

## 1. Establish the inputs before touching anything

Ask the user, or find in the workspace, before installing:

| Input | Where it usually comes from | Why it matters |
|---|---|---|
| Component zips, OS and architecture | `<Component>_<ver>_<OS>_<ARCH>.zip` | Use the build that matches the host |
| License Server `host:port` | A note or file from the user, e.g. `license.txt` containing `host:20000` | No licence, no service. See licensing below |
| Which components, and how they connect | The goal: Find needs Content, Community, Agentstore and View | Dependencies are configured in each component's `.cfg` (`reference/components.md`) |
| Ports | Defaults in `reference/components.md` | Clashes with other instances on the host, including containers. Check what's already listening |
| Install location, OS user | User's choice; dev installs often go in a work folder, production in `/opt/...` or `C:\...` | Run as a non-admin user; keep data off temp folders |
| Docs | `<Component>_<ver>_Documentation.zip` (HTML help) | Parameter details. Search them with `grep -ril` after extracting the zip |

Check the License Server is reachable before going further. Any ACI reply shows the server is reachable, even an error such as "The action you attempted is not recognized" for `action=getstatus`. `http://HOST:PORT/action=LicenseInfo` is the proper check, because it lists the products and seats. On Windows, `Test-NetConnection HOST -Port PORT` checks the network path. If nothing answers, stop and tell the user: nothing can be licensed.

## 2. Install = extract

Extract each zip into its own folder, one folder per **instance**. Two instances of the same product, such as Content and an Agentstore, need two folders (about 220 MB each for Content), or at least separate data paths and ports. Then:
- **Keep the executables executable.** The Linux zips store execute permissions and `unzip` keeps them. If you extract with a tool that drops Unix permissions, such as Python's `zipfile`, run `chmod +x *.exe` in each component folder.
- **Never copy the folder of an instance that has already run.** Its `license/` and `uid/` folders hold a licence bound to that instance's IP and service port. Start each instance from a fresh extract, or copy without `license/`, `uid/`, `index/` and `logs/`.
- **Don't delete `license/` or `uid/` from a running install.** The component stops working; see `IMPORTANT.txt` in the zip.

## 3. Configure

The `.cfg` is INI-like:
- `[Section]` headers and `Key=Value` lines;
- comments start with `//`;
- a section can be pulled from another file: `[License] < "idol.common.cfg" [License]`. Shared settings (licence, roles, logging) often live in `idol.common.cfg`, so **edit the file the section really comes from.**

Check the effective configuration without starting the server: `content.exe -getconfig` prints it with includes expanded.

Settings to look at for every component:

| Section / key | What to set |
|---|---|
| `[License] LicenseServerHost`, `LicenseServerACIPort` | The License Server. The default `localhost:20000` only works if one runs locally |
| `[Server] Port` (= the **ACI port**), `[Service] ServicePort` (= the **service port**), `[Server] IndexPort` (= Content's **index port**) | Unique per instance on the host |
| `[AuthorizationRoles]` and the role sections' `Clients=` | Who may send admin, query or index actions. The default is `localhost`, which is IPv4 127.0.0.1 only (see step 6) |
| `[Paths]` | Data folders, relative to the working directory by default |
| Component dependencies | e.g. View → Content, Community → Content + Agentstore. See `reference/components.md` |

Keep a copy of every file before changing it, and make the smallest edits that work. The shipped configs are commented, so read the surrounding comments. If a parameter is unclear, look it up in the component's documentation zip (`grep -ril 'ParamName' <docs folder>`).

**Changing ports** (e.g. a second stack on one host):
- The component's own settings: `[Server] Port`, `[Server] IndexPort` (Content), `[Service] ServicePort`.
- `[Service] Access-Control-Allow-Origin=http://localhost:PORT` contains the ACI port too.
- Every component that points at it: View `[Viewing] IDOLPort`, Community `[DataDre]`/`[AgentDre] aciport`, Find's `config.json`.

After changing ports, check with `-getconfig` and `grep` for the old numbers.

Command-line overrides are an alternative to editing copies: `-configfile PATH`, `-aciport N`, `-serviceport N`, `-indexport N`, and for some components `-contentport`, `-datahost`/`-dataport`, `-agentstoreport`. Run `<exe> -version` to print the version and build.

## 4. Licensing

Read `reference/licensing.md` for the details. In short:
- **Prerequisite:** a running License Server holding `licensekey.dat` and `versionkey.dat`. Components only need its host and ACI port.
- **On start-up** the component requests a licence and caches it in `license/` and `uid/`. The licence is bound to the component host's IP or MAC address and to the component's **service port**.
- **Changing the IP or service port:** stop the component, run `<exe> -revokelicense -configfile <cfg>`, change it, then start again.
- **Check:**
  - `action=GetLicenseInfo` on the component should show `<autn:validlicense>true`;
  - `license.log` and `service.log` in the component folder;
  - `logs/application.log` contains a `Licensed for …` line.
- **Capacity:** `action=LicenseInfo` on the License Server shows seats in use and Content's document limits.

## 5. Start

Start the License Server first (if it's local), then dependencies before the components that use them:
1. Content and the Agentstore;
2. then Community, View, Category and QMS;
3. then front ends such as Find.

| Platform | Run in the foreground or background | Run as a service |
|---|---|---|
| Linux | From the component folder: `LD_LIBRARY_PATH=. ./content.exe` (add `./filters` for View). Background it with output redirected. **Don't use `start-*.sh` unattended:** they `clear` the screen and wait for Enter | Fill in a template from `init/` (systemd, SystemV or upstart; see `init/readme.md`), install it, then `systemctl enable --now`. Set `LimitNOFILE` (Content needs ≥ 65536 open files) |
| Windows | Run `content.exe` from its folder | Admin prompt: `content.exe -install -servicename … -depend LicenseServer`; start it in Services. `-uninstall` removes it |
| Containers | | OpenText publishes images and compose files (see the Getting Started guide, "Containers"). `versionkey.dat` still comes from the License Server package |

The working directory matters, because relative `[Paths]` and the `license/` and `uid/` folders resolve against it. A service file should set it.

**Waiting for a start.** Start each process **once**, in the background, detached from your shell (`nohup … > console.log 2>&1 &`, or `setsid`), so it survives the command that started it. Then poll `GetStatus` on its ACI port every 2 s for **up to 60 s**: licensing and opening an index take a few seconds, Community longer. While waiting, read `logs/application.log`, not your own guesses:
- `Started.` or a SUCCESS reply: done.
- An error: fix it, stop that process (by its port, or its PID if it has no port yet), then start again.
- Still quiet after 60 s: check the process is alive and what the log says last (e.g. Community waiting for its Agentstore).

**Never start a second copy of an instance that may still be starting**, and don't run it in the foreground under `timeout`: that stops the process abruptly and can leave a lock file behind that blocks the next start. The log holds the errors.

## 6. Verify (all over HTTP: curl, PowerShell `Invoke-WebRequest`, or a browser)

| Request | Expect |
|---|---|
| `http://127.0.0.1:ACI/action=GetStatus` | `<response>SUCCESS</response>`, version, databases (Content) |
| `…/action=GetLicenseInfo` | `<autn:validlicense>true</autn:validlicense>` |
| `…/action=GetVersion` | Version and build |
| `…/action=GRL` | The request log (who called, when) |
| `logs/application.log` | `Licensed for …`, then `Started.`; errors otherwise |

**Use `127.0.0.1`, not `localhost`, for local requests.** With the default `Clients=localhost`, a request that arrives over IPv6 `::1` is refused with "Client is not allowed to execute this action". Alternatively, add `::1`, or the real client addresses, to the role's `Clients`.

## 7. Stop

| Platform | How |
|---|---|
| Any | `http://HOST:SERVICEPORT/action=stop`. It must be the **service** port: the same action on the ACI port answers SUCCESS and does nothing. Content can take a few seconds to exit |
| Services | `systemctl stop NAME`, or Windows Services |

Before deleting or replacing an instance's files (index, config), wait until **that** process has exited. Find it by the port it listens on (`ss -ltnp 'sport = :PORT'`, or `Get-NetTCPConnection -LocalPort PORT` on Windows), not by process name: two instances share `content.exe`, and pattern-matching tools like `pkill -f` can match your own shell.

## Troubleshooting

`reference/troubleshooting.md` covers licence errors, the stock defaults that block start-up (Community's placeholder key file, View's port), access refusals and data visibility (delayed sync).

## Related skills

- **find-setup:** Find on top of these components.
- **idol-index:** loading data into Content.
