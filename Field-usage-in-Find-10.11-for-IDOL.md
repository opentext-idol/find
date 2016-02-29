This documentation only applies to Find 10.11 for IDOL.

# IDOL Field Usage in Find #
In Find, the titles and summaries of search results (and the near-native view of those results) are taken from fields in your IDOL documents. Find also uses IDOL fields in the video and audio player, and when you open a document in a new tab.

This document describes the IDOL fields that Find uses, and how Find uses those fields, so that you can index your content into IDOL in such a way that it works with Find.

## IDOL Fields ##
Find looks for the following fields in IDOL documents:

- `content_type`. If the value of the `content_type` field is **audio** or **video**, Find treats the document as an audio or video file, and uses the information in the `url` and `offset` fields to process the document.
- `url`. The URL of an audio or video file (for example, http://example.com/example_video.mp4). Find needs to be able to access this URL from the browser by using an HTTP GET request.
- `offset`. The time offset from the start (in seconds) at which to begin playing an audio or video file.
- `author`. The author of the document. Where available, Find displays the content of the IDOL `author` field in the document metadata when you view the document.
- `category`. You can configure a field in your IDOL document that describes the category that the document belongs to. Where available, Find displays the content of the IDOL `category` field in the document metadata when you view the document.
- `date_created` or `created_date`. You can configure a field in your IDOL document that contains the date when the document was created. Where available, Find displays the content of the IDOL `date_created` or `created_date` field in the document metadata when you view the document.
- `date_modified` or `modified_date`. You can configure a field in your IDOL document that contains the date when the document was modified. Where available, Find displays the content of the IDOL `date_modified` or `modified_date` field in the document metadata when you view the document.

IDOL Server uses `DateType` fields to populate the `<autn:date>` and `<autn:datestring>` metadata fields for your documents. Find uses the `<autn:date>` tag in the IDOL response as the date of the document on the list of results. Use the `[FieldProcessing]`, `[DateFields]`, and `[SetDateFields]` sections of the IDOL configuration file to specify the fields in your documents that should be treated as `DateType` fields. For more information, see the *IDOL Server Administration Guide*.

## Title and Summary Information ##
Find displays the `<autn:title>` tag in the IDOL response as the title of the document in the list of results. Use the `[FieldProcessing]`, `[TitleFields]`, and `[SetTitleFields]` sections of the IDOL configuration file to specify the fields in your documents that should be treated as title fields. For more information, see the *IDOL Server Administration Guide*.
        
Find also displays a context summary for IDOL documents in the list of search results. This is a conceptual summary of each result document, and contains sentences that are particularly relevant to the terms in the query. These sentences can be from different parts of the result document. For more information on how to configure summarization in IDOL Server, see the *IDOL Server Administration Guide*.

## Database Information ##

The `GetStatus` action in IDOL is used to display the list of databases. Find displays all the databases in the `GetStatus` response except for internal databases, which are not shown.

## Configure Parametric Fields ##
A parametric search allows you to search for items by their characteristics (the values in certain fields).  For example, you can search an IDOL Server wine database for specific wine varieties from a specific region by specifying which fields must match these characteristics, so that only wines that are of the specified variety and from the specified region are returned.
        
Use the `[Server]` section of the IDOL configuration file to set up the fields that you want to use as parametric fields.

**To configure IDOL Server to recognize parametric fields**

1. Set the `ParametricRefinement` parameter to **True**.
1. List a parametric field process in the `[FieldProcessing]` section.
1. Create a property for the process.
1. Identify the fields that you want to associate with the process.
1. Create a section for the parametric property in which you set the `ParametricType` parameter to **True**.

For more information on how to configure parametric fields, see the *IDOL Server Administration Guide*.

## Configure View Server ##
        
To view IDOL documents, you must configure a reference field in the Find configuration file.

When you choose a document to view, the `GetContent` action reads the document.

The field that you specified in the Find configuration file is then read from the document content , and is used as the reference in the subsequent `View` action.

For example:

    Action=View&Reference=<reference>&NoAci=True&EmbedImages=True&StripScript=True&OriginalBaseUrl=True

**Note:** If you do not configure the reference field, or if the field cannot be found, an error occurs.

**Note:** The `View` action accepts the following parameter values, which cannot be overriden:

    EmbeddedImages=True
    StripScript=True
    OriginalBaseUrl=True
    NoAci=True
