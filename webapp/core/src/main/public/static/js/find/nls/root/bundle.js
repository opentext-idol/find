/*
 * Copyright 2017 Hewlett Packard Enterprise Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

define([
    'js-whatever/js/substitution'
], function(substitution) {
    'use strict';

    return substitution({
        'about.app.version': 'Version',
        'about.copyright': "Find © Copyright 2014-2017 Hewlett Packard Enterprise Development Company, L.P.",
        'about.foss': 'FOSS Acknowledgements',
        'about.lib.name': 'Library Name',
        'about.lib.version': 'Version',
        'about.lib.licence': 'License',
        'about.search': 'Search',
        'about.tagLine': 'Handcrafted in Cambridge.',
        'about.versionString': '{0} commit {1}',
        'applications': 'Applications',
        'app.about': 'About',
        'app.apply': 'Apply',
        'app.backToSearch': 'Back to search',
        'app.cancel': 'Cancel',
        'app.compare': 'Compare',
        'app.customizations': 'Customizations',
        'app.conceptBoxPlaceholder': 'Add a Concept',
        'app.delete': 'Delete\u2026',
        'app.close': 'Close\u2026',
        'app.button.delete': 'Delete',
        'app.exportToCsv': 'Export Results to CSV\u2026',
        'app.exportToCsv.modal.title': 'Select the fields you would like to export',
        'app.button.exportCsv': 'Export CSV',
        'app.from': 'From',
        'app.loading': 'Loading\u2026',
        'app.logout': 'Logout',
        'app.less': "less",
        'app.more': "more",
        'app.name': "Micro Focus Find",
        'app.ok': 'OK',
        'app.rename': 'Rename',
        'app.reset': 'Reset',
        'app.roles': 'Roles',
        'app.seeAll': 'See All',
        'app.seeMore': 'See More',
        'app.search': 'Search',
        'app.searchPlaceholder': 'What do you want to find?',
        'app.settings': 'Settings',
        'app.unfiltered': 'Unfiltered',
        'app.unknown': 'Unknown',
        'app.until': 'Until',
        'app.user': 'User',
        'app.users': 'Users',
        'customizations.apply.error': 'File {0} not found!',
        'customizations.apply.success': 'File {0} applied successfully',
        'customizations.applyDefault.success': 'Default file applied successfully',
        'customizations.bigLogo': 'Splash Screen Logo',
        'customizations.bigLogo.description': 'Upload a logo for the splash screen. This must be 240px \u00D7 45px.',
        'customizations.delete.message': 'Are you sure you want to delete the file {0}?',
        'customizations.delete.success': 'File {0} successfully deleted',
        'customizations.delete.title': 'Delete File',
        'customizations.error.default': 'An unknown error occurred uploading the file',
        'customizations.error.DIRECTORY_ERROR': 'An error occurred creating the customizations directory',
        'customizations.error.FILE_EXISTS': 'A file with the given name already exists',
        'customizations.error.IO_ERROR': 'An error occurred uploading the file',
        'customizations.error.INVALID_FILE': 'The file is of an invalid type',
        'customizations.fileDimensionsInvalid': 'Image has invalid dimensions',
        'customizations.smallLogo': 'Small Logo',
        'customizations.smallLogo.description': 'Upload a logo for the navigation bar. This must be 100px \u00D7 20px.',
        'dashboards': 'Dashboards',
        'dashboards.widget': 'Widget',
        'dashboards.widget.dataError': 'Error: failed to retrieve data',
        'dashboards.widget.dataError.tooSlow': 'Request failed to complete in time',
        'dashboards.widget.noData': 'The query returned no results',
        'dashboards.widget.notFound': 'Widget of type "{0}" could not be found. Please check your configuration and try again',
        'dashboards.widget.lastRefresh.nextRefresh': 'Next refresh at',
        'dashboards.widget.lastRefresh.refreshing': 'Refreshing {0} of {1} widgets',
        'dashboards.widget.lastRefresh.timeLastUpdated': 'Last updated at',
        'dashboards.widget.sunburst.legend.hiddenValues': 'Other',
        'dashboards.widget.sunburst.legend.noValues': 'No values to display',
        'dashboards.fullscreen': 'Full Screen',
        'datepicker.language': 'en',
        'default.title': 'Page Unavailable',
        'default.message': "We can't find the page you requested.",
        'default.button': 'Return to Search',
        // default dropzone messages for all but dictDefaultMessage
        'dropzone.dictDefaultMessage': 'Click here or Drag and Drop to upload files',
        'dropzone.dictFallbackMessage': "Your browser does not support drag'n'drop file uploads.",
        'dropzone.dictFallbackText': 'Please use the fallback form below to upload your files like in the olden days.',
        'dropzone.dictFileTooBig': 'File is too big ({{filesize}}MB). Max filesize: {{maxFilesize}}MB.',
        'dropzone.dictInvalidFileType': 'The file is of an invalid type',
        'dropzone.dictResponseError': 'Server responded with {{statusCode}} code.',
        'dropzone.dictCancelUpload': 'Cancel upload',
        'dropzone.dictCancelUploadConfirmation': 'Are you sure you want to cancel this upload?',
        'dropzone.dictRemoveFile': 'Remove file',
        'dropzone.dictRemoveFileConfirmation': '',
        'error.message.default': 'An error has occurred.',
        'error.default.contactSupport': 'Please contact support.',
        'error.details': 'Error details: {0}',
        'error.UUID': 'Error UUID: {0}',
        'error.unknown': 'Unknown error.',
        'export.powerpoint.single': 'Export to PowerPoint: Single Slide',
        'export.powerpoint.multiple': 'Export to PowerPoint: Multiple Slides',
        'export.powerpoint.videoWidget.exportFailure.CORS': 'Failed to export video, possibly due to invalid CORS settings. Please contact your system administrator.',
        'export.powerpoint.widgetEmpty': 'Widget not exported: The query returned no results',
        'export.powerpoint.widgetError': 'Widget not exported: The query produced an error',
        'login.defaultLogin': 'Details for the default login can be found in your config.json file',
        'login.error.auth': 'Please check your username and password.',
        'login.error.connection': 'There was an error authenticating with your Community server. Please check if your Community server is available.',
        'login.error.nonadmin': 'This user does not have admin permissions.',
        'login.important': 'Important',
        'login.infoDefaultLogin': 'This contains a default username (displayed below) and password.',
        'login.infoPasswordCopyPaste': 'You can copy and paste the password into the field below.',
        'login.infoSearchConfig': 'Using your favorite text editor, search config.json for "defaultLogin", in the "login" section.',
        'login.moreInfo': 'More',
        'login.newCredentials': 'Login with new credentials',
        'login.login': 'Login',
        'login.title': 'Login to {0}',
        'old.browser.chrome': 'Latest version of Chrome',
        'old.browser.edge': 'Latest version of Microsoft Edge',
        'old.browser.firefox': 'Latest version of Firefox',
        'old.browser.ie': 'Internet Explorer 11',
        'old.browser.supportedBrowsers': 'Please use one of the following supported browsers:',
        'old.browser.title': 'Browser not supported',
        'old.browser.unsupported': 'It looks like your browser is not supported by this app.',
        'placeholder.hostname': 'hostname',
        'placeholder.ip': 'IP',
        'placeholder.port': 'port',
        'search.answeredQuestion.question': 'Question: ',
        'search.answeredQuestion.answer': 'Answer: ',
        'search.alsoSearchingFor': 'Also searching for',
        'search.concepts': 'Concepts',
        'search.concepts.empty': 'No concepts selected',
        'search.databases': 'Databases',
        'search.dates': 'Dates',
        'search.dates.timeInterval.CUSTOM': 'Custom',
        'search.dates.timeInterval.WEEK': 'Last Week',
        'search.dates.timeInterval.MONTH': 'Last Month',
        'search.dates.timeInterval.YEAR': 'Last Year',
        'search.dates.timeInterval.NEW': 'Since Last Search',
        'search.dates.timeInterval.new.description': 'Show results since you last used this filter or changed the search',
        'search.document.authors': 'Authors',
        'search.document.contentType': 'Content Type',
        'search.document.sourceType': 'Source Type',
        'search.document.date': 'Date',
        'search.document.dateModified': 'Date Modified',
        'search.document.dateCreated': 'Date Created',
        'search.document.detail.loadingError': 'Failed to load the document',
        'search.document.detail.expand': 'Expand Preview',
        'search.document.detail.openOriginal': 'Open Original',
        'search.document.detail.highlightQueryTerms': 'Highlight Query Terms',
        'search.document.detail.tabs.authors': 'Authors',
        'search.document.detail.tabs.location': 'Location',
        'search.document.detail.tabs.location.latitude': 'Latitude',
        'search.document.detail.tabs.location.longitude': 'Longitude',
        'search.document.detail.tabs.metadata': 'Metadata',
        'search.document.detail.tabs.metadata.noAdvanced': 'This document has no advanced metadata fields',
        'search.document.detail.tabs.metadata.showAdvanced': 'Show advanced',
        'search.document.detail.tabs.metadata.hideAdvanced': 'Hide advanced',
        'search.document.detail.tabs.similarDocuments': 'Similar documents',
        'search.document.detail.tabs.similarDates': 'Similar dates',
        'search.document.detail.tabs.similarDates.pickMessage': 'Set a time window using the sliders',
        'search.document.detail.tabs.similarDates.after': 'After Document',
        'search.document.detail.tabs.similarDates.before': 'Before Document',
        'search.document.detail.tabs.similarDates.temporalSummaryHtml': 'Between <strong>{0}</strong> before and <strong>{1}</strong> after the document',
        'search.document.detail.tabs.similarSources': 'Similar sources',
        'search.document.detail.tabs.transcript': 'Transcript',
        'search.document.domain': 'Domain',
        'search.document.openInNewTab': 'Open in New Tab',
        'search.document.reference': 'Reference',
        'search.document.staticContent': 'Static Content',
        'search.document.summary': 'Summary',
        'search.document.title': 'Title',
        'search.document.thumbnail': 'Thumbnail',
        'search.document.thumbnailUrl': 'Thumbnail URL',
        'search.document.transcript': 'Transcript',
        'search.document.url': 'URL',
        'search.document.mmapUrl': 'MMAP URL',
        'search.document.weight': 'Weight',
        'search.editConcept.save': 'Save',
        'search.editConcept.cancelSave': 'Cancel',
        'search.error.promotions': 'An error occurred while retrieving promotions',
        'search.error.relatedConcepts': 'Error: could not retrieve Related Concepts',
        'search.error.parametric': 'An error occurred while retrieving additional filter values',
        'search.error.parametric.fields': 'An error occurred while retrieving additional filters',
        'search.filters': 'Filters',
        'search.filters.applied': 'Filters applied',
        'search.filters.filter': 'Filter\u2026',
        'search.filters.empty': 'No filters matched',
        'search.filters.removeAll': 'remove all',
        'search.geography': 'Geography',
        'search.geography.modal.title': 'Geographic Filters',
        'search.geography.none': 'No filters defined',
        'search.geography.filterCount': '{0} {1} defined {2}',
        'search.geography.filter': 'filter',
        'search.geography.filters': 'filters',
        'search.geography.disabled': '(but disabled)',
        'search.geography.apply.filters': 'Search',
        'search.geography.deleteAll': 'Delete all layers.',
        'search.geography.draw.toolbar.actions.title': 'Cancel drawing',
        'search.geography.draw.toolbar.actions.text': 'Cancel',
        'search.geography.draw.toolbar.finish.title': 'Finish drawing',
        'search.geography.draw.toolbar.finish.text': 'Finish',
        'search.geography.draw.toolbar.undo.title': 'Delete last point drawn',
        'search.geography.draw.toolbar.undo.text': 'Delete last point',
        'search.geography.draw.toolbar.buttons.polygon': 'Draw a polygon',
        'search.geography.draw.toolbar.buttons.circle': 'Draw a circle',
        'search.geography.draw.handlers.circle.tooltip.start': 'Click and drag to draw circle.',
        'search.geography.draw.handlers.circle.radius': 'Radius',
        'search.geography.draw.handlers.polygon.tooltip.start': 'Click to start drawing shape.',
        'search.geography.draw.handlers.polygon.tooltip.cont': 'Click to continue drawing shape.',
        'search.geography.draw.handlers.polygon.tooltip.end': 'Click first point to close this shape.',
        'search.geography.draw.handlers.polyline.error': '<strong>Error:</strong> shape edges cannot cross!',
        'search.geography.draw.handlers.rectangle.tooltip.start': 'Click and drag to draw rectangle.',
        'search.geography.draw.handlers.simpleshape.tooltip.end': 'Release mouse to finish drawing.',
        'search.geography.edit.toolbar.actions.save.title': 'Save changes.',
        'search.geography.edit.toolbar.actions.save.text': 'Save',
        'search.geography.edit.toolbar.actions.cancel.title': 'Revert editing, discards all changes.',
        'search.geography.edit.toolbar.actions.cancel.text': 'Revert',
        'search.geography.edit.toolbar.buttons.edit': 'Edit layers.',
        'search.geography.edit.toolbar.buttons.editDisabled': 'No layers to edit.',
        'search.geography.edit.toolbar.buttons.remove': 'Delete layers.',
        'search.geography.edit.toolbar.buttons.removeDisabled': 'No layers to delete.',
        'search.geography.edit.toolbar.buttons.negate': 'Toggle inclusive/exclusive filters.',
        'search.geography.edit.toolbar.buttons.negateDisabled': 'No layers to toggle inclusive/exclusive filters.',
        'search.geography.edit.handlers.edit.tooltip.text': 'Drag handles, or marker to edit feature.',
        'search.geography.edit.handlers.edit.tooltip.subtext': 'Click revert to undo changes.',
        'search.geography.edit.handlers.remove.tooltip.text': 'Click on a feature to remove',
        'search.geography.edit.handlers.negate.tooltip.text': 'Click on a feature to toggle between inclusion and exclusion filters',
        'search.indexes': 'Indexes',
        'search.indexes.all': 'All',
        'search.indexes.publicIndexes': 'Public Indexes',
        'search.indexes.privateIndexes': 'Private Indexes',
        'search.indexes.empty': 'Waiting for Indexes\u2026',
        'search.newSearch': 'New Search',
        'search.numericParametricFields': 'Numeric Parametric Fields',
        'search.numericParametricFields.error': 'Failed to load data',
        'search.numericParametricFields.noValues': 'No values',
        'search.numericParametricFields.noMax': 'No Max',
        'search.numericParametricFields.noMin': 'No Min',
        'search.numericParametricFields.reset': 'Reset',
        'search.numericParametricFields.tooltip': 'Range: {0} \u2013 {1}\nCount: {2}',
        'search.noResults': 'No results found',
        'search.noMoreResults': 'No more results found',
        'search.parametricFilters.modal.empty': 'No parametric values',
        'search.parametricFilters.modal.title': 'Select parametric filters',
        'search.parametricFields': 'Parametric Fields',
        'search.parametric.empty': 'No parametric fields found',
        'search.preview': 'Preview',
        'search.preview.previewMode': 'Preview Mode',
        'search.preview.mmap': 'Explore in MMAP',
        'search.preview.selectDocument': 'Select a document from the list to preview',
        'search.relatedConcepts': 'Related Concepts',
        'search.relatedConcepts.topResults.error': 'An error occurred while fetching top results',
        'search.relatedConcepts.topResults.none': 'No top results found',
        'search.relatedConcepts.notLoading': 'The list of indexes has not yet been retrieved',
        'search.relatedConcepts.none': 'There are no related concepts',
        'search.results': 'results',
        'search.results.pagination.of': 'of',
        'search.results.pagination.showing': 'Showing',
        'search.results.pagination.to': 'to',
        'search.resultsSort': 'Sort',
        'search.resultsSort.date': 'by date',
        'search.resultsSort.relevance': 'by relevance',
        'search.resultsView.list': 'List',
        'search.resultsView.topic-map': 'Topic Map',
        'search.resultsView.sunburst': 'Sunburst',
        'search.resultsView.sunburst.noParametricValues': 'Could not display Sunburst View: your search returned no parametric values',
        'search.resultsView.sunburst.error.query': 'Error: could not display Sunburst View',
        'search.resultsView.sunburst.noDependentParametricValues': 'There are too many parametric fields to display in Sunburst View',
        'search.resultsView.sunburst.error.noSecondFieldValues': 'There are no documents with values for both fields. Showing results for only first field.',
        'search.resultsView.map': 'Map',
        'search.resultsView.map.field': 'Field',
        'search.resultsView.map.show.more': 'Show More',
        'search.resultsView.map.title': 'Title',
        'search.resultsView.table': 'Table',
        'search.resultsView.table.count': 'Count',
        'search.resultsView.table.error.query': 'Error: could not display Table View',
        'search.resultsView.table.noParametricValues': 'Could not display Table View: your search returned no parametric values',
        'search.resultsView.table.noDependentParametricValues': 'There are too many parametric fields to display in Table View',
        'search.resultsView.table.info': 'Showing _START_ to _END_ of _TOTAL_ entries', // see https://datatables.net/reference/option/language.info
        'search.resultsView.table.infoFiltered': '(filtered from _MAX_ total entries)', // see https://datatables.net/reference/option/language.infoFiltered
        'search.resultsView.table.lengthMenu': 'Show _MENU_ entries', // see https://datatables.net/reference/option/language.lengthMenu
        'search.resultsView.table.next': 'Next',
        'search.resultsView.table.noneHeader': 'NONE',
        'search.resultsView.table.previous': 'Previous',
        'search.resultsView.table.searchInResults': 'Search in Results',
        'search.resultsView.table.zeroRecords': 'No matching records found',
        'search.resultsView.trending': 'Trending',
        'search.resultsView.trending.bucketSlider.fewerBuckets': 'High Level',
        'search.resultsView.trending.bucketSlider.moreBuckets': 'Detailed',
        'search.resultsView.trending.empty': 'Could not display Trending Chart. No parametric values to display for this query.',
        'search.resultsView.trending.error.query': 'Error: could not display Trending Chart',
        'search.resultsView.trending.error.invalidDate': 'Invalid date format',
        'search.resultsView.trending.error.minBiggerThanMax': 'Invalid date: the date is greater than the maximum date',
        'search.resultsView.trending.error.maxSmallerThanMin': 'Invalid date: the date is smaller than the minimum date',
        'search.resultsView.trending.minDate': 'Minimum date',
        'search.resultsView.trending.maxDate': 'Maximum date',
        'search.resultsView.trending.tooltipText': '{0} documents per {1} with the tag "{2}" between {3} and {4}',
        'search.resultsView.trending.yAxis': 'Document Rate (per {0})',
        'search.resultsView.trending.unit.DAY': 'day',
        'search.resultsView.trending.unit.HOUR': 'hour',
        'search.resultsView.trending.unit.MINUTE': 'minute',
        'search.resultsView.trending.unit.SECOND': 'second',
        'search.resultsView.trending.unit.YEAR': 'year',
        'search.resultsView.trending.snapToNow': 'Snap To Now',
        'search.resultsView.amount.shown': 'Showing <strong>{0}</strong> to <strong>{1}</strong> of <strong>{2}</strong> results',
        'search.resultsView.amount.shown.no.increment': 'Showing the top <strong>{0}</strong> results of <strong>{1}</strong>',
        'search.resultsView.amount.shown.no.results': 'There are no results with the location field selected',
        'search.answeredQuestion': 'Answered question',
        'search.answeredQuestion.systemName': 'Answered by {0}',
        'search.promoted': 'Promoted',
        'search.savedSearchControl.save': 'Save',
        'search.savedSearchControl.openEdit.create': 'Save query',
        'search.savedSearchControl.openEdit.edit': 'Save as query',
        'search.savedSearchControl.update': 'Save',
        'search.savedSearchControl.nameSearch': 'Name your search',
        'search.savedSearchControl.searchType.QUERY': 'Query',
        'search.savedSearchControl.searchType.SNAPSHOT': 'Snapshot',
        'search.savedSearchControl.cancelSave': 'Cancel',
        'search.savedSearchControl.error': 'Error: could not save search',
        'search.savedSearchControl.error.timeout': 'Timeout while trying to save current search. Try refining your query',
        'search.savedSearchControl.rename': 'Rename',
        'search.savedSearchControl.openAsSearch': 'Open as Query',
        'search.savedSearchControl.titleBlank': 'Title must not be blank',
        'search.savedSearchControl.nameAlreadyExists': 'Search with this name already exists',
        'search.savedSearchControl.nameEmptyOrWhitespace': 'Name must contain at least one printable character',
        'search.savedSearchControl.sharedByOthers': 'Shared by others',
        'search.savedSearchControl.sharedByOthers.empty': 'No shared searches found.',
        'search.savedSearchControl.sharingOptions': 'Sharing Options',
        'search.savedSearchControl.sharingOptions.canEdit': 'Can edit',
        'search.savedSearchControl.sharingOptions.error': 'Error: ',
        'search.savedSearchControl.sharingOptions.error.editPermissions': 'an error occurred with search {0} edit permissions for {1}',
        'search.savedSearchControl.sharingOptions.error.shared': 'an error occurred sharing search {0} with {1}',
        'search.savedSearchControl.sharingOptions.error.unshared': 'an error occurred unsharing search {0} with {1}',
        'search.savedSearchControl.sharingOptions.notSharedWith': 'Users this search not shared with...',
        'search.savedSearchControl.sharingOptions.searchForMoreUsers': 'Search for more users',
        'search.savedSearchControl.sharingOptions.sharedWith': 'Users this search shared with...',
        'search.savedSearchControl.sharingOptions.shareWithThisUser': 'Share with this user',
        'search.savedSearchControl.sharingOptions.success': 'Success: ',
        'search.savedSearchControl.sharingOptions.success.canEdit': 'search {0} can be edited by {1}',
        'search.savedSearchControl.sharingOptions.success.cannotEdit': 'search {0} cannot be edited by {1}',
        'search.savedSearchControl.sharingOptions.success.shared': 'search {0} is shared with {1}',
        'search.savedSearchControl.sharingOptions.success.unshared': 'search {0} is not shared with {1}',
        'search.savedSearchControl.sharingOptions.unshareWithThisUser': 'Unshare with this user',
        'search.savedSearchControl.sharingOptions.users.empty': 'No users found',
        'search.suggest.title': 'Similar results to "{0}"',
        'search.topicMap.empty': 'No topics found for this query',
        'search.topicMap.error': 'Error: could not retrieve topics for this search',
        'search.topicMap.fast': 'Fast',
        'search.topicMap.accurate': 'Accurate',
        'search.sunburst.title': 'Parametric Distribution',
        'search.sunburst.fieldPlaceholder.first': 'Select a field',
        'search.sunburst.fieldPlaceholder.second': 'Select a second field',
        'search.sunburst.tooSmall': 'There are an additional {0} values with document counts too small to display. Please refine your search.',
        'search.sunburst.missingValues': 'This area represents {0} search result(s) which contained no values for the parametric field {1}',
        'search.savedSearches': 'Searches',
        'search.savedSearches.confirm.deleteMessage': 'Are you sure you want to remove {0} saved search?',
        'search.savedSearches.confirm.deleteMessage.title': 'Delete saved search',
        'search.savedSearches.confirm.resetMessage': 'Are you sure you want to reset {0} saved search?',
        'search.savedSearches.confirm.resetMessage.title': 'Reset saved search',
        'search.savedSearches.deleteFailed': 'Error: could not delete the saved search',
        'search.selected': 'Selected',
        'search.similarDocuments': 'Similar documents',
        'search.similarDocuments.error': 'Error: could not fetch similar documents',
        'search.similarDocuments.none': 'No similar documents found',
        'search.spellCheck.showingResults': 'Showing results for',
        'search.spellCheck.searchFor': 'Search for',
        'settings.cancel': 'Cancel',
        'settings.cancel.message': 'All unsaved changes will be lost.',
        'settings.cancel.title': 'Revert settings?',
        'settings.close': 'Close',
        'settings.unload.confirm': 'You have unsaved settings!',
        'settings.adminUser': 'Admin User',
        'settings.adminUser.description': 'Configure the admin username and password for Find.',
        'settings.answerServer.description': 'Specify where Answer Server is located.',
        'settings.answerServer.enable': 'Enable Answer Server',
        'settings.answerServer.enabled': 'Answer Server is enabled',
        'settings.answerServer.disable': 'Disable Answer Server',
        'settings.answerServer.disabled': 'Answer Server is disabled',
        'settings.answerServer.loading': 'Loading\u2026',
        'settings.answerServer.title': 'Answer Server',
        'settings.community.description': "Community handles authentication for Find. We recommend using a dedicated Community server for Find and not using it for any other parts of your IDOL installation.  Your Community server will need an Agentstore server for data storage.",
        'settings.community.login.type': 'Login Type',
        'settings.community.login.fetchTypes': 'Test connection to retrieve available login types.',
        'settings.community.login.invalidType': 'You must test connection and choose a valid login type.',
        'settings.community.serverName': 'community',
        'settings.community.title': 'Community',
        'settings.content.description': 'Specify where your content is indexed.',
        'settings.content.title': 'Content',
        'settings.description': "This page is for editing the Find config file.  The config file location is stored in the Java system property {0}.  The current location is {1}.",
        'settings.iod.apiKey': 'API key',
        'settings.iod.application': 'Application',
        'settings.iod.domain': 'Domain',
        'settings.locale.title': 'Locale',
        'settings.locale.default': 'Default locale',
        'settings.logoutFromSettings': 'Logout from Settings',
        'settings.map': 'Mapping',
        'settings.map.attribution': 'Attribution',
        'settings.map.description': 'View location information. The tile server URL should contain {x}, {y}, and {z} variables',
        'settings.map.disable': 'Disable Mapping',
        'settings.map.disabled': 'Mapping is disabled',
        'settings.map.enable': 'Enable Mapping',
        'settings.map.enabled': 'Mapping is enabled',
        'settings.map.loading': 'Loading\u2026',
        'settings.map.results.step': 'Results to load each time',
        'settings.map.url': 'Tile Server URL Template',
        'settings.mmap': 'MMAP',
        'settings.mmap.description': 'View rich media with MMAP',
        'settings.mmap.disable': 'Disable MMAP',
        'settings.mmap.disabled': 'MMAP is disabled',
        'settings.mmap.enable': 'Enable MMAP',
        'settings.mmap.enabled': 'MMAP is enabled',
        'settings.mmap.loading': 'Loading\u2026',
        'settings.mmap.url': 'URL',
        'settings.savedSearches': 'Saved Searches',
        'settings.savedSearches.description': 'Configuration relating to saved searches',
        'settings.savedSearches.loading': 'Loading\u2026',
        'settings.savedSearches.polling.disable': 'Disable polling',
        'settings.savedSearches.polling.disabled': 'Polling for updates to saved searches is disabled',
        'settings.savedSearches.polling.enable': 'Enable polling',
        'settings.savedSearches.polling.enabled': 'Polling for updates to saved searches is enabled',
        'settings.savedSearches.polling.interval': 'Polling Interval (in minutes)',
        'settings.password': 'Password',
        'settings.password.description': 'Password will be stored encrypted',
        'settings.password.redacted': '(redacted)',
        'settings.queryManipulation': 'Query Manipulation',
        'settings.queryManipulation.blacklist': 'Blacklist Name',
        'settings.queryManipulation.description': 'Enable query manipulation with QMS',
        'settings.queryManipulation.disable': 'Disable Query Manipulation',
        'settings.queryManipulation.disabled': 'Query Manipulation is disabled',
        'settings.queryManipulation.dictionary': 'Dictionary',
        'settings.queryManipulation.enable': 'Enable Query Manipulation',
        'settings.queryManipulation.enabled': 'Query Manipulation is enabled',
        'settings.queryManipulation.expandQuery': 'Enable synonyms',
        'settings.queryManipulation.index': 'Index',
        'settings.queryManipulation.loading': 'Loading\u2026',
        'settings.queryManipulation.typeaheadMode': 'Typeahead Mode',
        'settings.requiredFields': 'required fields',
        'settings.restoreChanges': 'Revert Changes',
        'settings.retry': 'Retry Save',
        'settings.save': 'Save Changes',
        'settings.save.confirm': 'Are you sure you want to save this configuration?',
        'settings.save.confirm.title': 'Confirm Save',
        'settings.save.saving': 'Saving configuration. Please wait\u2026',
        'settings.save.retypePassword': '(you may need to re-type your password)',
        'settings.save.success': 'Success!',
        'settings.save.success.message': 'Configuration has been saved.',
        'settings.save.errorThrown': 'Threw exception: ',
        'settings.save.failure': 'Error!',
        'settings.save.failure.validation.message': 'Validation error in',
        'settings.save.failure.and': 'and',
        'settings.save.failure.text': 'Would you like to retry?',
        'settings.save.unknown': 'Server returned error: ',
        'settings.statsserver.description': 'Send statistics to StatsServer for analysis',
        'settings.statsserver.disable': 'Disable Statistics Collection',
        'settings.statsserver.disabled': 'Statistics Collection is disabled',
        'settings.statsserver.enable': 'Enable Statistics Collection',
        'settings.statsserver.enabled': 'Statistics Collection is enabled',
        'settings.statsserver.title': 'StatsServer',
        'settings.statsserver.validation.CONNECTION_ERROR': 'An error occurred while contacting the StatsServer',
        'settings.statsserver.validation.INVALID_CONFIGURATION': 'The StatsServer is not configured correctly for Micro Focus Find',
        'settings.test': 'Test Connection',
        'settings.test.ok': 'Connection OK',
        'settings.test.databaseBlank': 'Database must not be blank!',
        'settings.test.failed': 'Connection failed',
        'settings.test.failed.password': 'Connection failed (you may need to re-type your password)',
        'settings.test.hostBlank': 'Host name must not be blank!',
        'settings.test.passwordBlank': 'Password must not be blank!',
        'settings.test.portInvalid': 'Port must not be blank, and inside the range 1-65535 !',
        'settings.test.usernameBlank': 'Username must not be blank!',
        'settings.username': 'Username',
        'settings.view': 'View',
        'settings.view.connector': 'Connector',
        'settings.view.description': 'View documents by either a custom field, or using a connector',
        'settings.view.referenceFieldBlank': 'Reference Field must not be blank',
        'settings.view.referenceFieldLabel': 'Reference Field',
        'settings.view.referenceFieldPlaceholder': 'Enter Reference Field',
        'settings.view.viewingMode': 'Viewing Mode',
        'settings.CONNECTION_ERROR': 'An error occurred while contacting the ACI server',
        'settings.DEFAULT_LOGIN': 'Default login specified. Test Community connection and select a server type.',
        'settings.FETCH_PORT_ERROR': "An error occurred while fetching the details of the server's index and service ports",
        'settings.FETCH_SERVICE_PORT_ERROR': "An error occurred while fetching the details of the server's service port",
        'settings.INCORRECT_SERVER_TYPE': 'Incorrect server type. Make sure you are using one of {0}',
        'settings.INDEX_PORT_ERROR': "An error occurred while fetching the details of the server's index port",
        'settings.REQUIRED_FIELD_MISSING': 'One or more of the required fields is missing',
        'settings.REGULAR_EXPRESSION_MATCH_ERROR': 'The target server is of an incorrect type',
        'settings.SERVICE_AND_INDEX_PORT_ERROR': "The server's service or index ports could not be determined",
        'settings.SERVICE_PORT_ERROR': "The server's service port could not be determined",
        'users.password': 'Password',
        'users.admin': 'Admin',
        'users.noUsers': 'No users retrieved from Community.',
        'users.refresh': 'Refresh',
        'users.none': 'There are currently no admin users',
        'users.title': 'User Management',
        'users.button.create': 'Create',
        'users.button.createUser': 'Create User',
        'users.button.cancel': 'Close',
        'users.create': 'Create New Users',
        'users.delete': 'Delete',
        'users.delete.text': 'Are you sure?',
        'users.delete.confirm': 'Confirm',
        'users.delete.cancel': 'Cancel',
        'users.info.done': 'Done!',
        'users.info.createdMessage': 'User {0} successfully created.',
        'users.info.deletedMessage': 'User {0} successfully deleted.',
        'users.info.error': 'Error!',
        'users.info.creationFailedMessage': 'New user profile creation failed.',
        'users.password.confirm': 'Confirm Password',
        'users.password.error': 'Password must not be blank',
        'users.select.level': 'Select User Level:',
        'users.serverError': 'Server returned error.',
        'users.admin.role.add': 'Create role',
        'users.admin.role.add.title': 'Admin role required',
        'users.admin.role.add.description': 'This Community server does not have an admin role. Would you like to create one?',
        'wizard.last': 'Logout',
        'wizard.next': 'Next',
        'wizard.prev': 'Prev',
        'wizard.step.settings': 'Settings',
        'wizard.step.users': 'Users',
        'wizard.step.welcome': 'Welcome',
        'wizard.welcome': "Welcome to the Find configuration wizard",
        'wizard.welcome.helper': "This wizard will help you set up Find in two quick steps:",
        'wizard.welcome.step1': 'On the Settings page, configure your connection settings, then click Save',
        'wizard.welcome.step2': "On the Users page, create initial user accounts, then click Logout",
        'wizard.welcome.finish': 'After you complete the configuration wizard, you can start using Find'
    });
});
