---
title: Creating Widgets
layout: default
---
# Creating a Widget

## Introduction

### What are Widgets

Simply put, widgets are configurable components that make up a Dashboard. Widgets come in a variety of types; each type is designed to display different type of content to user, and keep it up-to-date if the content changes over time, provided that the dashboard has a configured update interval. A widget is a configurable JavaScript module that is rendered on the dashboard grid as a [Backbone][backbone] View. Widgets can be configured using the `dashboards.json` configuration file and can take a custom settings object.
> **Note:** For more information on the configuration file, please see the Find Admin Guide.

### Assumptions and Constraints

- A widget must be responsive. The grid system we have implemented means that a widget can be configured to occupy a rectangle of any size or proportion; it must accommodate reasonable configurations without breaking the UI.
- The widget should not take a long time to load. Given this is a dashboard system it needs to be be responsive and load in a timely manner.
- The widgets should all run as separate instances and not interfere with other widgets.
- A widget is non interactive. Widgets should only display information and if the `onClick` function is used it should navigate to the source of the data used to render the display.
- If a dashboard has a configured update interval, all updateable widgets will be periodically refreshed, e.g. fetch fresh data and update their displays accordingly. Such widgets must detect changes to their data and have ways to re-render the requisite parts of their display that do not rely on the page being refreshed.

### Widget Examples

#### Topic Map Widget

![Topic Map Widget][topic-map]

Here we have the Topic Map Widget. This is one of the standard built in widgets which is backed by a saved search (explained [here][SavedSearchWidget]). In a running instance of Find, this widget will automatically resize when the window size changes and it will update the saved search and Topic Map after a specified interval. There can be multiple of these on a single dashboard without them conflicting with each other. 

#### Current Time and Date Widget

![Current Time and Date][time-and-date]

The Current Time and Date Widget is an example of a standard widget (explained [here][StandardWidget]). This is not backed by any data from the server other than the widget specific settings and will not update periodically. 

#### Results List Widget

![Results List Widget][results-list]

Like the [Topic Map Widget][TopicMapWidget] the Results List Widget is backed by a saved search and will display the top `n` results as specified in the configuration file. The Results List Widget will alter its layout to accommodate it's shape and size. If the configured widget size is too small to display all the requested results, excess entries will be hidden.

## Widget Types

There are three types of widget currently supported in Find: 
- [Standard Widget][StandardWidget]
- [Updating Widget][UpdatingWidget] 
- [SavedSearch Widget][SavedSearchWidget]

These three widget types should cover most use cases and can all be implemented quickly by extending their abstract views found in the widgets folder: 
`find/webapp/idol/src/main/public/static/js/find/idol/app/page/dashboard/widgets/`

### Standard Widget

A standard widget is designed to be largely static. This is not to say that the widget itself can not change (see the [Current Time and Date Widget][CurrentDateTimeWidget]), but that it will not be updated by the Dashboard once it's update interval passes. It will be rendered once, and is in sole control of any changes that happen to it thereafter. For example: a widget to display a company-wide promotional video.
> **Note:** If the widget is just a static piece of HTML or an image, then the built in Static Content Widget or Static Image Widget should be used, respectively.Refer to the Admin Guide for configuration details.

### Updating Widget
An Updating Widget is the same as a [Standard Widget][StandardWidget] but will update itself every `n` seconds as defined in the `dashboards.json` configuration file. The update calls the widget's `doUpdate()` function which must be implemented. This type of widget should be used when there is a need to fetch data or re-render on a regular interval. For example a widget to display the weather may poll the api of a third party weather service. 

> **Note:** The [Saved Search Widgets][SavedSearchWidget] extends the Updating Widget abstract view and implements its own flavour of `doUpdate()` designed to fetch data from saved searches.

### Saved Search Widget

The Saved Search Widget is an extension of the [Updating Widget][UpdatingWidget] that is backed by a saved search. It will re-fetch the saved search data, which can then be used to retrieve any additional information needed by the widget to update its display. For example, the [Results List Widget][ResultsListWidget] relies on fetching the Document Collection, and the Sunburst Widget requires Dependent Parametric Values.

> **Note:** The saved search can be either a Query or a Snapshot. As snapshots do not change after creation, a widget backed by a Snapshot will never update.

## Development

### Shared Development

#### Widget Registry

All widgets are located and instantiated via the widget registry (`widget-registry.js`). This is where the widget source files are loaded via [Require.js][requirejs]. When a widget is loaded by the dashboard it uses the name specified in the configuration file to perform a lookup in the widget registry to retrieve the constructor.

A widget registry entry looks like this:
```javascript
SunburstWidget: {
    Constructor: SunburstWidget
}
```
The key for the object property (`SunburstWidget`) is the name used in the configuration file to refer to this widget. The constructor property should be the Backbone View constructor for the widget view. Widgets should be written in a separate file and loaded via [Require.js][requirejs] into the registry.

#### HTML and Layout

The layout of the widgets is very simple. Each widget has a title (optional) and a content `div`, these use `display: flex` so if there is no title then the content will expand to take up 100% of the height. For the purposes of widget development the only element of consequence is the widget content div, this is passed to view from the abstract widget view as `$content` and is available after calling the render method on the widget abstract view. For example:
```javascript
<AbstractView>.prototype.render.apply(this); // <AbstractView> can be Widget, UpdatingWidget, or SavedSearchWidget. It must be imported via Require.js.
this.$content.html(someHtml);
```

All widget types come with a loading spinner which is displayed until the `initialised()` function is called. This will hide the loading spinner and show the content. The `initialised()` function should be called at the end of the render method just after the `$content` element is populated.

The widgets are sized and laid out in a grid pattern (as explained in the Admin Guide) the size of which is specified on a per dashboard basis. This is handled by the infrastructure of the dashboard page. Because of this, the widget must be capable of handling multiple alternate layouts: for example the [Results List Widget][ResultsListWidget] will switch to a column-based layout when it would allow for more data to be displayed then a row-based layout.

#### Functions and Properties

`clickable` is the only property used by all widget types. It takes a `boolean` value and determines whether the widget click handler is called when the user clicks the widget. As mentioned above the widget should be non interactive and the click handler should only be used to navigate to the data source of the widget

We provide an `onResize` function to handle window resize events which should be used rather than implementing separate listeners as this also handles the sidebar opening and collapsing as well.

The `onClick` method is provided to handle click functionality and will be called if the widget is clicked anywhere. This will only be called if the `clickable` property is set to `true`.
> **Note:** This should not be overridden by a Saved Search Widget as that already has a click handler function.

#### Widget Settings

In the configuration file each widget can have a `widgetSettings` object that will look something like this: 

```json
"widgetSettings": {
    "key": "some value",
    "key2": {
        "subkey": "another value",
        "subkey2" : [1,2,3,4,5,6]
    }
}
```
These values are passed in to the widget when it is initialised, and stored as a variable on the view. For example the above would be accessed via:

```javascript
initialize: function(options) {
    Widget.prototype.initialize.apply(this, arguments);
    this.key = this.widgetSettings.key;
    this.key2 = this.widgetSettings.key2;
    this.subkey = this.widgetSettings.key2.subkey
    this.subkey2 = this.widgetSettings.key2.subkey2
}
```

### Standard Widget Development

The standard widgets are very simple and utilise nothing additional to the above shared settings when implemented. Most uses of this type of widget could be replaced with a `StaticContentWidget` or `StaticImageWidget`.

#### Example

```javascript
define([
    './widget' // load the abstract widget view.
], function(Widget) {
    'use strict';

    return Widget.extend({
        initialize: function(options) {
            Widget.prototype.initialize.apply(this, arguments);

            this.subject = this.widgetSettings.subject || 'world'; // setting to determine who to greet.
        },

        render: function() {
            Widget.prototype.render.apply(this);
            this.$content.html('Hello, ' + this.subject + '!'); // render html greeting.
            this.initialised(); // remove the loading spinner.
        }
    });
});
```

### Updating Widget Development

The updating widget utilises a set of functions to handle the update. These functions need to be implemented carefully to ensure that the widget works with the `TimeLastRefreshedWidget` (this is a widget which tracks the dashboard update cycle and displays this information to the user).

#### Functions

`doUpdate(done)` is the main update function that is called when the dashboard refreshes all of the widgets. The `done` parameter is a callback that must be called when the widget has finished updating (if this is not called the `TimeLastRefreshed` widget will not know the update has finished and the loading spinner will not be hidden). This function should re-fetch any data needed to render the widget and then update the UI accordingly. The loading spinner is handled by the abstract view and does not need to be shown and hidden manually.

`onCancelled` is called when the update has been cancelled for any reason. This function should cancel any pending requests made by the widget and resolve or remove any outstanding promises.

#### Example

```javascript
define([
    './updating-widget', // load the abstract updating-widget view.
    './some-weather-service' // load some weather service.
], function(UpdatingWidget, SomeWeatherService) {
    'use strict';

    return UpdatingWidget.extend({
        initialize: function(options) {
            UpdatingWidget.prototype.initialize.apply(this, arguments);
            this.weatherService = new SomeWeatherService({location: this.widgetSettings.location}); // create some weather service with a location from the settings.
            this.subject = this.widgetSettings.subject || 'world'; // setting to determine whom to greet.
        },

        render: function() {
            Widget.prototype.render.apply(this);
            this.$content.html('Hello, ' + this.subject + '! The weather near you is: <span class="weather"></span>'); // render some html greeting with a weather option.
            this.initialised(); // remove the loading spinner.
        }

        doUpdate(done) {
            this.weatherService.getWeather({ // perform some fetch on the weather service.
                success: function(weather) {
                    this.$('.weather').html(weather); // render the weather.
                    done(); // call update callback to signify completion.
                }
            })
        }

        onCancelled: function() {
            if (this.weatherService.getPromise()) { // if the weather service is fetching.
                this.weatherService.cancelPromise(); // perform some request cancellation.
            }
        }
    });
});
```

### Saved Search Widget Development

The saved search widgets are an extension of the [Updating Widget][UpdatingWidget] they have their own version of the `doUpdate` and `onCancelled` methods, which must not be overridden unless the prototype function is called as well. The abstract view handles the retrieval of the saved search during initialisation and updates.

#### Functions

`postInitialize` is a function that is run after the saved search has been fetched successfully on initialisation. This can optionally return a promise in which case `getData` will not be called until is has been resolved. This is useful for loading any extra objects or views that are contingent on the information in the saved search. Barring connection errors, this function will only be called once during the life of the widget, therefore it should not be used to fetch information that need to be refreshed periodically.

`getData` is the main method for retrieving the data needed to render the view. It must return a promise, which will be used to handle the `doUpdate` callback. This function will be called on every update cycle. For example, in the [Results List Widget][ResultsListWidget] this function is used to fetch the Document Collection. The widget creates a listener on the collection which renders the new results and calculates what can be displayed.

#### Properties

`savedSearchModel` is the model that controls the saved search information. This is controlled by the abstract view and should be considered read only.

`queryModel` is available if this is required. This contains the same information as the saved search model in a different format and is mainly used for internal purposes.

`viewType` is the results view that should be loaded on click. This property is optional and will default to the first configured results view if nothing is specified.

### Example
```javascript
define([
    'underscore', // import underscore for templating.
    './saved-search-widget', // import the abstract saved search widget.
    'find/app/model/documents-collection', // import the documents collection.
    'moment' // import moment for time parsing.
], function(_, SavedSearchWidget, DocumentsCollection, moment) {
    'use strict';

    return SavedSearchWidget.extend({
        viewType: 'list', // when clicked take user to the saved search with the list view displayed.

        template: _.template('<span>Latest result is: <%-title%> <br> it was indexed on: <%-date%></span>'), // template for the document.

        initialize: function(options) {
            SavedSearchWidget.prototype.initialize.apply(this, arguments);

            this.documentsCollection = new DocumentsCollection(); // create the collection.

            this.listenTo(this.documentsCollection, 'add', function(attributes) { // add a listener to alter the html when a new model is added
                this.$content.html(this.template({
                    title: attributes.title,
                    date: moment(attributes.date).format()
                }))
            });

        },

        getData: function() {
            return this.documentsCollection.fetch({ // fetch the document collection based on the saved search
                data: {
                    text: this.queryModel.get('queryText'),
                    max_results: 1,
                    indexes: this.queryModel.get('indexes'),
                    field_text: this.queryModel.get('fieldText'),
                    min_date: this.queryModel.getIsoDate('minDate'),
                    max_date: this.queryModel.getIsoDate('maxDate'),
                    sort: 'date',
                    summary: 'context',
                    queryType: 'MODIFIED',
                    highlight: false
                },
                reset: false
            });
        }
    });
});
```

[requirejs]: http://requirejs.org/
[backbone]: http://backbonejs.org/
[topic-map]: ./topic-map.png
[time-and-date]: ./time-and-date.png
[results-list]: ./results-list.png
[CurrentDateTimeWidget]:#current-time-and-date
[ResultsListWidget]:#results-list-widget
[StandardWidget]:#standard-widget
[UpdatingWidget]:#updating-widget
[SavedSearchWidget]:#saved-search-widget
[TopicMapWidget]:#topic-map-widget