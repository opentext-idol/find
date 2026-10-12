---
name: find-setup
description: How to deploy OpenText Find (the Knowledge Discovery search UI, a Java web app) on top of IDOL components. Covers prerequisites (Content, Community + Agentstore, View, Java 17), running find.war as a process or a Linux/Windows service, the Find home directory, wiring config.json or the Settings page to the backend servers, Community login, the FindUser/FindBI/FindAdmin roles and users, and end-to-end checks. Use when asked to install, deploy or connect Find, or when users can't log in or search in Find.
---

# Setting up Find

Find is `find.war`, a self-contained Spring Boot web application. It doesn't index anything itself. It sends queries to Content, logs users in through Community, and renders documents through View. Set those up first (idol-component-setup skill), then Find.

## 1. Prerequisites

| Needed | Why | Without it |
|---|---|---|
| **Content** (data index) | search, filters | nothing to search |
| **Community** + its **Agentstore** | user login, roles, saved profiles | **only the admin Settings page works; no user can search** |
| **View** | document previews | previews fail |
| **Java 17** | runs `find.war` | `java -version` to check |
| Optional: QMS, Statistics Server, Answer Server, … | promotions, statistics, answers | those features stay off |

The Find zip contains only `find.war` and service templates. Community, View and the others come in separate product zips. If they're missing, say so and ask for them rather than half-installing.

**Before starting Find, check all of these answer `GetStatus` with SUCCESS and `GetLicenseInfo` with `validlicense` true:** Content, the Agentstore, Community and View. Community must be connected to both Content (`[DataDre]`) and the Agentstore (`[AgentDre]`). Its `application.log` must not keep repeating "could not establish connection with Agent DRE". Commenting out Community's Agentstore link is not a valid configuration: install and start an Agentstore instead (idol-component-setup checklist).

## 2. Run it

```
java -Didol.find.home=<HOME> -Dserver.port=8080 -jar find.war     # -D options go BEFORE -jar
```
- **`<HOME>`** is the Find home directory. The Find user needs read/write access to it. It holds:
  - `config.json`;
  - `customization/` (templates, dashboards, logos, styles);
  - `logs/`;
  - `data/` (H2 database of saved searches).
- **Other useful `-D` properties:**
  - `-Xmx1024m` (heap);
  - `-Dfind.defaultRoles=FindUser` (gives new users a role, useful with LDAP-backed Community);
  - logging location;
  - SSL;
  - reverse proxy settings.
- **As a service:**
  - Linux: `install/linux/systemd/find.service`, or `sysv/find.sh`. Edit the paths, user and port.
  - Windows: `install/windows/winsw/find.xml`, used with the WinSW wrapper renamed to `find.exe`, then `find.exe install`.
- **Started** when `APPLICATION STARTED` appears in the console or `logs/`, and `http://HOST:8080/loginPage` answers.

Only one Find can use a given home directory: its H2 database is locked by the running instance. Check the port is free before starting another.

**Stopping:** Find has no stop action. Stop the service, or stop the Java process found by its listening port (`ss -ltnp 'sport = :8080'`, or `Get-NetTCPConnection -LocalPort 8080` on Windows). Never pattern-match on `find.war`. A restart takes about 20–30 s.

## 3. First-run configuration

The first start writes a default `config.json` with a generated admin login (`login.defaultLogin.username` and `password`). That login only reaches the **Settings** page.

**Order matters:**
1. Configure the servers.
2. Create the roles and **your own users** in Community (§4).
3. Only then switch `login.method` to Community and restart.

After the switch, the generated default admin login stops working: every login, admins included, goes through Community. Don't re-create the default admin in Community with its generated password. Create a named admin user with a fresh password and all three Find roles.

**The usual sequence when editing the file:**
1. Start Find once; `config.json` appears in `<HOME>` within about 20 s.
2. Stop Find by its port.
3. Edit `config.json` (keys below; it's plain JSON, so keep it valid).
4. Start Find again and wait for `APPLICATION STARTED` (about 20–30 s).

Point Find at the backend servers in either of two ways:
- **Settings page:** log in with the default login, fill in each server, click **Test Connection**, then **Save Changes**.
- **`config.json` directly:** stop Find, edit, start it again. Most `config.json` changes need a restart.

| `config.json` | Value |
|---|---|
| `content.host` / `port` | Content ACI port (9100) |
| `login.community.host` / `port` | Community ACI port (9030) |
| `login.method` | **The Community security repository name in lower case.** The default `"default"` means "default admin login only". With Community's built-in repository (`[Security] 0=Autonomy` in `community.cfg`) use `"autonomy"`. |
| `view.host` / `view.port` | **View's ACI port** (9080 by default) |
| `view.viewingMode` | `FIELD` (fetch by a reference field, usually `DREREFERENCE`), `UNIVERSAL` or `CONNECTOR`. `view.connector` (port 10000) is only used in `CONNECTOR` mode; leave it alone otherwise |
| `queryManipulation`, `statsServer`, `answerServer`, `communityAgentStore`, … | `enabled: true` + server, only if installed. `communityAgentStore` is only for the related-users tab; login and search don't need it |
| `themeTracker` | **Set `enabled: false`** unless you have that service. The generated default enables it with an address (10.2.21.236:19120) that is not part of your environment |

Use `127.0.0.1` or the real host name rather than `localhost` (see idol-component-setup: IDOL's `Clients=localhost` doesn't cover IPv6 `::1`). The generated file uses `localhost` in every server block: `content`, `login.community`, `view` (and `view.connector`), `communityAgentStore`, `queryManipulation`, `statsServer`, `answerServer`, `entitySearch`, `controlPoint`. Replace it with `127.0.0.1` in each block you use; the enabled ones must be correct.

## 4. Roles and users (in Community)

**Find never creates its roles.** Create them in Community: the admin UI at `http://COMMUNITY:9030/action=admin` under Control > Roles and Users, or ACI:
```
/action=RoleAdd&RoleName=FindUser            (also FindBI, FindAdmin)
/action=UserAdd&UserName=NAME&Password=PW
/action=RoleAddUserToRole&RoleName=FindUser&UserName=NAME
/action=UserRead&UserName=NAME&RoleList=true      (check)
```

| Role | Gives |
|---|---|
| `FindUser` | Logging in and searching. **Required:** users with only FindBI or FindAdmin can't log in |
| `FindBI` | Dashboards, saved searches, the extra result tabs (topic map, sunburst, trending, table) |
| `FindAdmin` | The Settings page and admin APIs (customization reload, logos) |

Create at least one user with all three, and one with only `FindUser` to see the end-user experience.
- Give each user its own **randomly generated** password (16+ letters and digits). Use the same value in `UserAdd` and in the credentials file, so they can't drift apart.
- Keep passwords out of chat, reports and command output you show, and in a `chmod 600` file, e.g. `user=NAME password=PW` per line.
- When reading the file back, extract the fields you need without printing the file.

## 5. Verify end to end

Check through the browser, or through the HTTP API with a session:

| Step | Request | Expect |
|---|---|---|
| Log in | `POST /authenticate` (form fields `username`, `password`) | 302 to `/public`; cookie `FINDSESSIONID` |
| Databases | `GET /api/public/search/list-indexes` | Your Content databases, with document counts. Content's stock `News` and `Archive` show here too: remove them from `content.cfg` if unused (idol-index), or list them in `uiCustomization.defaultDeselectedDatabases` |
| Search | `GET /api/public/search/query-text-index/results?text=TERM&indexes=DB&start=1&max_results=5&summary=context&auto_correct=false&total_results=true` | Results and `totalResults`. The parameter is `text`, not `queryText` |
| Filters | `GET /api/public/parametric/values?fieldNames=FIELD&databases=DB&queryText=*` | Values. `databases` is required |
| Preview | `GET /api/public/view/viewDocument?reference=REF&index=DB&part=DOCUMENT` | 200 HTML. `part` is required |

Then look at the UI as each kind of user. The find-ui-check skill does this headlessly. Previews of web references depend on View being able to fetch them.

## 6. Next

- **Load data:** idol-index.
- **Tailor the UI** to the data: find-customize says which skill does what (find-fields-and-filters, find-result-templates, find-dashboard, find-branding).
- **Build a patched Find:** not covered by a skill yet.
- **Look up a setting:** the Find and IDOL product documentation (HTML help shipped in the `*_Documentation.zip` files).

## Common issues

- **Can't search, only Settings:** `login.method` is still `"default"`, or Community isn't reachable.
- **Users can log in but see nothing to search:** no databases selected (a blank "filters applied" chip), or Content has no data. Keep the Databases filter enabled so users can fix the selection.
- **New users can't log in:** missing the `FindUser` role. Find doesn't create roles.
- **Changes don't show:**
  - `config.json` edits need a restart;
  - `customization/*.json` edits need `/api/admin/customization/config/reload` as a FindAdmin user;
  - stylesheets are cached by browsers for an hour.
