Find expects the reference field in both hosted and on-prem mode to be unique although IDOL and HOD do not require this. If multiple documents are returned by a single reference we use the first one with a matching case.

### Suggested Configuration
To ensure that this does not happen when indexing documents into IDOL or HOD do not set the flag which allows duplicates.