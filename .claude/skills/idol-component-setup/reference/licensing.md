# Licensing IDOL components

## The model

- **License Server** is its own component, usually one per installation. It holds:
  - `licensekey.dat`, issued by OpenText Support. It is **bound to the License Server's IP/MAC address and ACI port**: moving the License Server needs a new key.
  - `versionkey.dat`, from the License Server package on the Software Licenses and Downloads portal. It allows particular component versions, so an upgrade needs a new one. Docker Hub images don't include it.
- **Components** need only `[License] LicenseServerHost` and `LicenseServerACIPort`, plus optional `LicenseServerTimeout` and `LicenseServerRetries`.
  - At start-up they request a seat, then cache the licence in `license/` and `uid/` in their working directory.
  - The seat is bound to the component host's IP and the component's **service port**.
- **OEM** installations are licensed differently, without a License Server; see `KnowledgeDiscovery_<ver>_OEMLicensing.pdf` in the Content docs zip.

## Setting it up for a component

1. Get the License Server host and ACI port (often handed over as `host:port` in a text file).
2. Check it answers: `http://HOST:PORT/action=getstatus`. If you're allowed, `action=LicenseInfo` lists products, seats and Content document limits.
3. Set the two `[License]` keys in the file the section is really read from. In stock configs that's `idol.common.cfg`, through `[License] < "idol.common.cfg" [License]`.
4. Start the component, then check:
   - `action=GetLicenseInfo` shows `<autn:validlicense>true`;
   - `logs/application.log` contains `Licensed for …`;
   - `license.log` and `service.log` in the component folder.

## Rules

| Do | Don't |
|---|---|
| To change a component's IP or service port: stop it, `<exe> -revokelicense -configfile <cfg>`, change it, start it | Change the service port or IP of a licensed instance without revoking |
| Set up each instance from a fresh extract | Copy a folder that has already run; its `license/` and `uid/` belong to the original instance |
| Free seats from dead hosts with `AdminRevokeClient` on the License Server | Delete `license/` or `uid/` from a working install |
| Download a new `versionkey.dat` when upgrading components | Expect a container image to bring its own `versionkey.dat` |

## Errors (from the docs, abridged)

| Message | Meaning |
|---|---|
| Failed to connect to license server … | License Server unreachable, and no usable cache. Check host, port, firewall and that it's running |
| Failed to connect to license server using cached licensed details | Running on the cache for a limited time; fix License Server reachability |
| Your license cache details do not match the current service configuration. Shutting the service down. | The service port or IP changed after licensing. Revoke and restart |
| Invalid license for this build version: no valid MaxBuild limit present | The License Server needs a `versionkey.dat` that allows this build |
| Invalid license for this product version … | The licence doesn't cover this component or version |
| Could not load license: file versionkey.dat was invalid | Replace `versionkey.dat` from the support site |
| `-revokelicense`: "Failed to revoke license from server. Bad Response"; `license.log`: "License server data files are out of sync" | A License Server-side data synchronisation problem. Keep the instance folder, including `license/` and `uid/`, so you can retry later, or ask the License Server admin to free the seat with `AdminRevokeClient` |

## Removing an instance (test installs, decommissioning)

1. Stop it (`action=stop` on the service port; wait for that PID to exit).
2. Run `<exe> -revokelicense -configfile <cfg>` from its folder, so the seat goes back to the License Server.
3. Only after `Successfully revoked license from server`, delete the folder.

   If revoking fails, keep the folder and note why: deleting it leaves a seat that only the License Server admin can free.
