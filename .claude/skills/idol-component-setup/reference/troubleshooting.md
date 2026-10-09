# Troubleshooting IDOL components

## Won't start / exits straight away

| Symptom (`logs/application.log`, console, `service.log`) | Cause | Fix |
|---|---|---|
| Licence errors | See `licensing.md` | |
| Community: `"SecurityInfoKeys" should specify a valid AES key…`, `Failed to initialise security` | Placeholder key path in the shipped config | Generate a key with `autpassword -x -tAES -oKeyFile=…` and point `SecurityInfoKeys` at it |
| Community logs "Indexer could not establish connection with Agent DRE. Will retry soon." every 15 s, stops answering, and ignores `action=stop` | No Agentstore running (or wrong `[AgentDre]` host/port). Community requires `[AgentDre]`; it cannot be commented out | Install and start an Agentstore first (a separate Content extract with `agentstore.cfg`), then start Community. To stop the broken one, use a normal terminate signal on the PID found by its port |
| Community waits at start-up for the Data DRE | Content not running, or wrong `[DataDre]` | Start Content first; check host and port |
| Linux: `error while loading shared libraries` | Library path | Run from the component folder with `LD_LIBRARY_PATH=.` (View: `.:./filters`) |
| Hangs at start-up in a script | `start-*.sh` runs `clear` and waits for Enter | Run the executable directly or as a service |
| `Address already in use`, or a second instance exits | Port clash | Pick unique ACI, service and index ports; check what's listening |
| Content fails under load / too many open files | File descriptor limit | Raise `nofile` (≥ 65536); for systemd services, `LimitNOFILE=` |

## Running, but requests fail

| Symptom | Cause | Fix |
|---|---|---|
| `Client is not allowed to execute this action` | The caller's address isn't in the role's `Clients`. Often `localhost` resolved to `::1` while `Clients=localhost` means 127.0.0.1 | Call `127.0.0.1`, or add the address to `Clients` |
| `action=stop` returns SUCCESS but the process keeps running | Sent to the ACI port | Send it to the **service** port |
| An action is "not recognized" | Wrong action name for this version (e.g. `getfieldinfo` is not a 26.x Content action) | Check the component Help in its documentation zip (`grep -ril ACTION <docs folder>`) |
| `getquerytagvalues` → "No query text supplied" | Needs `text=` | Add `text=*` |

## Data and results

| Symptom | Cause | Fix |
|---|---|---|
| Indexed documents don't show up for ~2 min | `DelayedSync=TRUE`, `MaxSyncDelay=120` | `DRESYNC` on the index port, or change the settings |
| `totalhits` doesn't match the document count | `totalresults=true` is an estimate | Use `GetStatus` database counts |
| View previews fail | `IDOLPort` still 9060, or the source URL is unreachable or rate-limited | Point View at Content; check the reference is fetchable |

## Operational habits that avoid damage

- Find a process by its listening port before stopping or killing it. Never pattern-match on names or command lines, which can hit other instances or your own shell.
- Wait for that exact process to exit before deleting its index or swapping its files.
- Back up config files before editing, and diff afterwards.
- Don't copy a used instance's `license/` and `uid/` folders to another instance.
