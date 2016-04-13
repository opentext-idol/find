# Overview

To present results, Find receives document models (document-model.js) from the server. To present the document, the data has to be rendered into a template.

# Result Renderer

Result Renderer (result-renderer.js) is a configurable factory for generating html strings from document models. By editing the existing config object (result-renderer-config.js), the result renderer may be configured to template a given result differently if it presents with qualifying data.

The test for the result renderer demonstrates an example of how this may be achieved and it is instructive to refer to it. The test's configuration contains configuration objects with three functions: the predicate, data, and template functions.

## The Predicate Function

The predicate tests the model to see if it qualifies for the template. If the predicate function returns true, the corresponding template will be used. The predicate function receives the document model and the isPromotion flag, which indicates if the document is a search result or a promotion.

## The Data Function

The data function is used to transform data from the document model into specific data required by the template. This is demonstrated by the second configuration object. The defaultData function in result-renderer-config demonstrates more clearly why you might want to do this transformation up front.

## The Template Function

The template function is called internally by the result renderer with the model and the return value of the data function to generate the result as html.

# Recommended Usage

To add your own templates for given predicates, add your configuration objects to result-renderer-config.js. Predicates will be matched in order, and the first matching template will be used. Predicates must be defined such that all models are matched by at least one predicate; an easy way to ensure this is to make sure the final configuration object has a predicate which always returns true.

In the example, the first configuration object checks the document model's fields array to see if it contains a field with id "trees". Any document that contains a field with this id will be matched. If the document does not match, the second predicate will be tried, which matches fields with the ids "plants" or "fungi". If the document does not match, the final predicate will be tried, which always returns true.