Document titles are retrieved from the title tag in the Idol response (retrieved via Idol "SetTitleFields" configuration).  
The dates on results on the main page are retrieved from the date tag of the Idol response (configured via "SetDateFields" in the Idol configuration file).  
The document summary is a context summary (see Idol documentation for further details).  
The list of databases is retrieved by querying idol using GetStatus (internal databases are not displayed).  
Parametric fields are retrieved using GetQueryTagValues. They are configured in the Idol configuration file.  

For viewing documents, Find requires that a reference field be configured in the Find config file.  
When a document is selected to be viewed, the content for the document in question is read (using the GetContent action).  
The configured field is then read from the document content (first occurrence if the field appears multiple times) and is used as the reference in the subsequent view action.  
An error is thrown if the reference field is not configured or could not be found.  
Hard-coded parameters on the view action include:  
EmbeddedImages=true  
StripScript=true  
OriginalBaseUrl=true