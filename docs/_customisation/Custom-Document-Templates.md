---
title: Custom Document Templates
layout: default
---

As of version 11.4, Find supports customisation of how search results are displayed via configuration and templates files 
in the home directory. This replaces the previously documented method that required changing the code and re-compiling the
application.

# Configuration

To use this feature, admins must create or modify the templates.json config file in the Find customization directory. 
This contains a JSON object of the following form:

```json
{
  "searchResult": [
    {
      "file": "person.handlebars",
      "triggers": [
        {
          "field": "categories",
          "values": ["Living people"]
        }
      ]
    }
  ],
  "promotion": [],
  "previewPanel": []
}
```

The object's top-level keys represent the different types of templates that can be customised, currently:

- *searchResult:* A document in the results list view
- *promotion:* A promoted document in the results list view
- *previewPanel:* The document metadata displayed in the preview side panel

The structure is the same for each of these keys: a list of template file names and trigger conditions. The template file
must exist in the $FIND_HOME/customization/templates directory.

# Triggers

A trigger condition consists of a field ID (the key in the fieldsInfo section of the config file) and a list of values. 
If the list of values is empty, a document matches the trigger if it has any value for the given field. 
Otherwise, a document matches if the given field has at least one
of the values in the list.

Triggers for a template are considered to be AND conditions. A document only matches a template if it matches all of its 
triggers.

To choose which template to use to display a given document, the templates in the configured list for the required 
template type are processed in order. The first template whose triggers match the document is chosen. If no configured 
template matches, the application default template is used.

# Templates

Template files are processed using [Handlebars](https://handlebarsjs.com/) and must produce HTML output representing one
document. Templates are executed with the following Handlebars context: 

```typescript
interface ResultTemplateData {
  reference: string;
  title: string;
  date: string;
  database: string;
  promotionName: string|undefined;
  summary: string; // The highlighted summary, should not be HTML escaped
  url: string|undefined; // URL of the original document or media file
  icon: string; // Icon class based on content type
  similarDocumentsUrl: string|undefined; // URL for linking to the similar documents view, only in result and promotion
  thumbnailSrc: string|undefined, // Source attribute to load the thumbnail in an <img> tag
  age: string; // Internationalised age of the document (e.g. "3 years ago") 
  fields: {id: string, values: string[], displayName: string, advanced: boolean}[];
}
```

The following custom helpers are exposed to templates:

- *equal*: Block helper taking two arguments. The block is only printed if the two arguments are referentially equal.
- *hasField*: Block helper taking one string argument (the field). The block is only printed if the document has a value 
for  the field.
- *hasFieldValue*: Block helper taking two string arguments, the field and the value. The block is only printed if the
document has the given value in the given field.
- *getFieldValue*: Prints the first value for the given field.
- *withField*: Block helper which executes the block in the context of the given field.
- *i18n*: Prints a string from the application internationalisation file.

Fields in custom helpers are referenced via id. That is, the corresponding key in the fieldsInfo section of the config
file. Document fields are only available if they are explicitly referenced in that section.

# Example Template

The following template could be used as a search result template.

```html
<div>
    <h1><i class="{{icon}}"></i>{{title}}</h1>
    {{#hasField 'thumbnail'}}
        <img src="{{thumbnail}}"/>
    {{/hasField}}
    <p>{{{summary}}}</p>
    <p>Author: {{getFieldValue "Author"}}</p>
    <a href="{{similarDocumentsUrl}}">See similar documents</a>
</div>
```
