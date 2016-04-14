As of b1ab79fabda58e007ea14f915b385a7a8def26f0, Find has support for easy customisation of results templates.

Let's start with some documentation:

# Overview

To present results, Find receives document models (`document-model.js`) from the server. To present the document, the data has to be rendered into a template.

# Result Renderer

Result Renderer (`result-renderer.js`) is a configurable factory for generating html strings from document models. By editing the existing config object (`result-renderer-config.js`), the result renderer may be configured to template a given result differently if it presents with qualifying data.

The test for the result renderer demonstrates an example of how this may be achieved and it is instructive to refer to it. The test's configuration contains configuration objects with three functions: the predicate, data, and template functions.

## The Predicate Function

The predicate tests the model to see if it qualifies for the template. If the predicate function returns true, the corresponding template will be used. The predicate function receives the document model and the `isPromotion` flag, which indicates if the document is a search result or a promotion.

## The Data Function

The data function is used to transform data from the document model into specific data required by the template. This is demonstrated by the second configuration object. The `defaultData` function in `result-renderer-config` demonstrates more clearly why you might want to do this transformation up front.

## The Template Function

The template function is called internally by the result renderer with the model and the return value of the data function to generate the result as html.

# Recommended Usage

To add your own templates for given predicates, add your configuration objects to `result-renderer-config.js`. Predicates will be matched in order, and the first matching template will be used. Predicates must be defined such that all models are matched by at least one predicate; an easy way to ensure this is to make sure the final configuration object has a predicate which always returns true.

In the example, the first configuration object checks the document model's fields array to see if it contains a field with id "trees". Any document that contains a field with this id will be matched. If the document does not match, the second predicate will be tried, which matches fields with the ids "plants" or "fungi". If the document does not match, the final predicate will be tried, which always returns true.

# Test

There's a very useful Jasmine test in `find-core/src/test/js/spec/app/page/search/results/result-rendering/result-renderer.js` that goes over the expected behaviour.

# Example

Let's start by looking at `result-renderer-config.js`, in the `find-core` module.  Find the return statement at the bottom of the file:

```
return [
    {
        template: _.template(resultsTemplate),
        data: defaultData,
        predicate: _.constant(true)
    }
];
```

This array is where we add our new templates.  Each object in the array has three things in it - a `template` function (Underscore.js' `_.template()` function is your friend here), a `data` processing function to generate an object to pass into the template, and a `predicate` function that is run to select a template to render.

The single object in the array is the default template - it uses `resultsTemplate` (which you can find imported at the top of the file in the line `'text!find/templates/app/page/search/results/results-container.html'`), takes `defaultData` (look for the function definition in the middle of the file), and has a predicate that always returns true.

## Let's create a new template

Before we can add a template to `result-renderer-config.js`, we need to create the actual template HTML file.  Start by looking at `find-core/src/main/public/static/js/find/templates/app/page/search/results/results-container.html` - the default results HTML template.  Notice that it isn't pure HTML - there's embedded JavaScript in the template, wrapped in `<% %>` tags - have a look at the [Underscore.js templating documentation](http://underscorejs.org/#template) to learn the syntax.

Create a copy of `results-container.html` in the same directory, but with a different file name.  This is your results template.  For the time being, we'll assume that you've called it "my-results-template.html".

Make changes to your results template to change how results should be rendered.

## Adding your template to the Results Renderer Config

Back to `result-renderer-config.js`.  First of all, we need to import your new template.  Find the call to `define()` at the top of the file - this uses [Require.js](http://requirejs.org/) to define a module and import an array of requirements.  Add your template to the array, taking care to append a comma to the end of the previous line.  Then, give your template a name in the function that is also passed to `define()`.  It should now look like this:

```
define([
    'underscore',
    'find/app/page/search/results/add-links-to-summary',
    'find/app/util/document-mime-types',
    'text!find/templates/app/page/search/results/results-container.html',
    'text!find/templates/app/page/search/results/my-results-template.html'
], function(_, addLinksToSummary, documentMimeTypes, resultsTemplate, customResultsTemplate) {
```

Inside the main function body, your template is now available as `customResultsTemplate` (or whatever name you gave it).  It's a string, not a function or an object - the `text!` prefix asked Require to literally just read the file in as a string.  We'll need to wrap it with the Underscore `template` function to create a templating function out of it that can execute the embedded JavaScript in the template.

Simple mode: let's assume that your template doesn't need any different data passed into it.  Just add it to the return statement above the default template:

```
return [
    {
        template: _.template(customResultsTemplate),
        data: defaultData,
        predicate: function(model, isPromotion) {
            return Boolean(_.find(model.get('fields'), function(field) {
                return field.id === 'SOME_FIELD_NAME' && _.contains(field.values, 'magic')
            }))
        }
    },
    {
        template: _.template(resultsTemplate),
        data: defaultData,
        predicate: _.constant(true)
    }
];
```

When the field called `SOME_FIELD_NAME` contains the value `magic`, the `customResultsTemplate` will be used instead of the default template.

The templates in the array are processed in order, so it's important to always put your new templates above the default one, otherwise they will never be used.

## Run Find to see your new template in action

All that's left is to run Find and see your changes in action!  See [[Running-a-Development-Copy-of-Find]]