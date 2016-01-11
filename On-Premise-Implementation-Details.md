Document titles are retrieved from the title tag in the Idol response (retrieved via Idol "SetTitleFields" configuration).  
The dates on results on the main page are retrieved from the date tag of the Idol response (configured via "SetDateFields" in the Idol configuration file).  
The document summary is a context summary (see Idol documentation for further details).  
The list of databases is retrieved by querying idol using GetStatus (internal databases are not displayed).  
Parametric fields are retrieved using GetQueryTagValues. They are configured in the Idol configuration file.  