When we release a new version of Find, we `tag` it in Git.

## Version 1

The first version of Find only worked against Haven OnDemand (which was called IDOL OnDemand back then), using the `v1` APIs.

If you want to use the `v1.x.x` version of Find, only use the latest release (i.e. currently `v1.0.3`)

### Tags

- [v1.0.0](../../releases/tag/v1.0.0) - The first release of Find
- [v1.0.1](../../releases/tag/v1.0.1) - Hotfix to the config page
- [v1.0.2](../../releases/tag/v1.0.2) - Minor refactoring
- [v1.0.3](../../releases/tag/v1.0.3) - Hotfix to update the version of the IOD client library in use.  This tag branches away from `master`, as the fix was applied after `master` had moved onto `v2`

## Version 2

The second version of Find moved to the Haven OnDemand v2 APIs and added a lot more functionality.

### Tags

- [v2.0.0](../../releases/tag/v2.0.0) - Move to the Java HOD Client for v2 APIs
- [v2.0.1](../../releases/tag/v2.0.1) - Versioning cleanup
- [v2.0.2](../../releases/tag/v2.0.2) - Library patch
- [v2.0.3](../../releases/tag/v2.0.3) - Documentation patch

## Version 10.11

The 10.11 release is when Find became a dual-stack application for both Haven OnDemand and IDOL.  As such, the version number was brought into sync with the IDOL version number.

### Tags

- [v10.11.0](../../releases/tag/v10.11.0) - Release of Find for IDOL on-prem for IDOL customers