# Component notes

## Default ports

| Component | ACI | Index | Service | Notes |
|---|---|---|---|---|
| Content | 9100 | 9101 | 9102 | data index |
| Agentstore | 9050 | 9051 | 9052 | a Content with a different config |
| Category | 9020 | | 9022 | |
| Community | 9030 | | 9032 | users, roles, agents |
| View | 9080 | | 9082 | document rendering and highlighting |
| DAH / DIH | 9060 / 9070 | – / 9071 | 9062 / 9072 | distribution. View's default `IDOLPort=9060` assumes a DAH in front of Content |
| QMS | 16000 | | 16002 | query manipulation (promotions, type-ahead) |
| Answer Server | 12000 | | 12002 | |
| Statistics Server | 19870 | | 19872 | |
| CFS / connectors | 7000 / 7002+ | | 17000 / 17002+ | ingestion |
| Find | HTTP 8080 | | | Java web app, not an ACI server |
| License Server | usually 20000 | | | whatever its key was issued for |

## Content (data index)

- **Databases:** `[Databases] NumDBs` plus `[DatabaseN] Name=`. The stock config ships `News` and `Archive`. Documents choose a database through `#DREDBNAME`, or `DREDbName=` when indexing.
- **Indexing:** goes to the index port (`DREADD?path&DREDbName=DB`, `DREADDDATA`). See the idol-index skill.
- **Sync:** the stock config has `DelayedSync=TRUE`, `MaxSyncDelay=120` and `SearchUncommittedDocuments=FALSE`. New documents stay invisible for up to 2 minutes unless you send `DRESYNC` to the index port.
- **Linux:** raise open-file limits (≥ 65536) for the user running it.

## Agentstore

- **What it is:** a Content component with an agent-store config. The Content zip ships two:
  - `alt_configs/agentstore.cfg`, for Community and Category;
  - `alt_configs/agentstore_qms.cfg`, for QMS promotions.
- **Ways to run one:**
  - Give it its own folder (a fresh extract of the Content zip) and copy `alt_configs/agentstore.cfg` over `content.cfg`. `alt_configs/idol.common.cfg` is the same as the top-level one.
  - Or run `content.exe -configfile …/agentstore.cfg` from a separate working directory. Its relative `[Paths]` (index) and `license/`/`uid/` must not collide with the data Content's.
- **Ports:** 9050/9051/9052 by default.

## Community

- **Needs:** a data index (`[DataDre] host`, `aciport`) and an Agentstore (`[AgentDre] host`, `aciport`). Command-line equivalents: `-datahost`, `-dataport`, `-agentstoreport`.
- **`[Security] SecurityInfoKeys` must be set to a real AES key file.** The shipped value is a placeholder path, and Community exits with "Failed to initialise security". Generate a key with the bundled `autpassword` tool:
  ```
  autpassword -x -tAES -oKeyFile=/abs/path/aes.keyfile      (Linux: ./autpassword.exe, LD_LIBRARY_PATH=.)
  ```
  Then set `SecurityInfoKeys` to that absolute path. Protect the file (`chmod 600`), and use the same key in any component that must decrypt security info.
  - **The key is printed to stdout** (`PATH=<hex key>`). Don't paste that output into logs, reports or chat; discard it.
- **Security repositories:** `[Security] 0=Autonomy` names the built-in user repository (others: LDAP, NT, OTDS modules). Front ends refer to it by this name; Find's `login.method` is `autonomy`.
- **Users and roles:** use the Community admin UI (`/action=admin`) or ACI: `RoleAdd`, `UserAdd`, `RoleAddUserToRole`, `UserRead&RoleList=true`.

## View

- **Needs:** a Content (or DAH) for highlighting: `[Viewing] IDOLHost` / `IDOLPort`. The **default `IDOLPort` is 9060 (DAH)**. Point it at Content's ACI port (9100) when there's no DAH, or start with `-contentport`.
- **Linux:** needs `./filters` on `LD_LIBRARY_PATH` as well as `.`.
- **Viewing modes:**
  - Reference-based: it fetches the document from the reference URL or path. Public sites can rate-limit it (HTTP 429).
  - Universal viewing (`[UniversalViewing]`): it looks documents up in the document store first.
  - Connector-based.

## Category, QMS, Answer Server, Statistics Server

These follow the same install, licence, start and verify steps. Configure their dependencies:
- Category → Content + Agentstore (+ Community);
- QMS → Content + a QMS Agentstore (`agentstore_qms.cfg`);
- Answer Server and Statistics Server: see their own docs.
