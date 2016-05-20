This documentation only applies to Find 10.11 for IDOL.

# IDOL Field Usage in Find #
In Find, the titles and summaries of search results (and the near-native view of those results) are taken from fields in your IDOL documents. Find also uses IDOL fields in the video and audio player, and when you open a document in a new tab.

This document describes the IDOL fields that Find uses, and how Find uses those fields, so that you can index your content into IDOL in such a way that it works with Find.

## IDOL Fields ##
The fields which Find looks for in an IDOL document can be configured in the "fieldsInfo" section of the config.json file. This is a map of an id to a list of IDOL field names and an optional type. The type can be STRING (the default), NUMBER, DATE or BOOLEAN. For example:

````
{
    "dateCreated": {
        "names": [
            "date_created"
        ],
        "type": "DATE"
    },
    "authors": {
        "names": [
            "author",
            "collaborator"
        ]
    }
}
````

When Find reads an IDOL document, it looks for all values for all of the fields listed in the names section for all ids listed in the config file. The values found for a given id affect application behaviour in an id-specifc way. Some ids only support one field value per document, in which case the first value is used.

Available ids are detailed below.

| ID            | Type | Notes                                                 | Default Names            |
| ------------- | ---- | ----------------------------------------------------- | ------------------------ |
| contentType | STRING | If the value of the `content_type` field is **audio** or **video**, Find treats the document as an audio or video file, and uses the information in the `url` and `offset` fields to process the document. | AU_REPOSITORY_METADATA_TYPE_STRING |
| url | STRING | The URL of an audio or video file (for example, http://example.com/example_video.mp4). Find needs to be able to access this URL from the browser by using an HTTP GET request. | AU_REPOSITORY_METADATA_URL_FILE_STRING |
| offset | STRING | The time offset from the start (in seconds) at which to begin playing an audio or video file. | OFFSET |
| authors | STRING | The author of the document. Where available, Find displays the content of the IDOL `author` field in the document metadata when you view the document. | AU_DOCUMENT_METADATA_AUTHOR_STRING |
| categories | STRING | You can configure a field in your IDOL document that describes the category that the document belongs to. Where available, Find displays the content of the IDOL `category` field in the document metadata when you view the document. | AU_REPOSITORY_METADATA_CATEGORY_STRING |
| dateCreated | DATE | You can configure a field in your IDOL document that contains the date when the document was created. Where available, Find displays the content of the IDOL `date_created` or `created_date` field in the document metadata when you view the document. | AU_DOCUMENT_METADATA_DATE_CREATED_EPOCHSECONDS |
| dateModified | DATE | You can configure a field in your IDOL document that contains the date when the document was modified. Where available, Find displays the content of the IDOL `date_modified` or `modified_date` field in the document metadata when you view the document. | AU_DOCUMENT_METADATA_DATE_MODIFIED_EPOCHSECONDS |
| latitude | NUMBER | The latitude in degrees. Used to locate a document in the document detail view. | LAT |
| longitude | NUMBER | The longitude in degrees. Used to locate a document in the document detail view. | LON |
| mmapUrl | STRING | If MMAP is enabled, this is appended to the configured MMAP base URL for linking to an instance of MMAP. | MMAP_PROXY_URL |
| sourceType | STRING | The source of the media. | SOURCETYPE |
| thumbnail | STRING | A base 64 encoded image to use as a thumbnail for the search result. | PREVIEW_THUMBNAIL_BASE64 |
| thumbnailUrl | STRING | The URL of a thumbnail image file (for example, http://example.com/thumbnail.png). The thumbnail images will be displayed in aspect ratio of 16:9. Find needs to be able to access this URL from the browser by using an HTTP GET request. | THUMBNAIL_URL |
| transcript | STRING | A transcript for a document extracted from an audio source. If present, this is printed out on the transcript tab of the document detail view. | TRANSCRIPTION_CONTENT |

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