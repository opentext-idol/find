Find looks for the following fields in Idol/HoD documents:
* content_type
* url
* offset
* author
* category
* date_created or created_date
* date_modified or modified_date

If content_type is audio or video, the document is treated as an audio/video file using the url and offset fields.
The url field must point to a video/audio file in a supported format (browser-dependent).
The offset field determines the time (in seconds from the start) after which playback should commence.
All other fields are metadata only, and are displayed, if present, when the document is viewed.