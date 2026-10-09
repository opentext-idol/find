---
name: find-result-templates
description: Change how OpenText Find shows each search result or the document preview panel, using Handlebars templates - e.g. a product card with price, brand, rating and colour badges, or an image thumbnail. Covers templates.json, the template files, triggers (which documents use which template), the fields and helpers available, and the thumbnail fallback. Use when asked to customise result cards, result layout, the preview panel, or to show images or extra fields in search results.
---

# Find: result and preview templates

## Rules (read first)

1. **Templates use `fieldsInfo` ids, not raw field names.** Every field a template reads must be defined in `config.json` `fieldsInfo` (find-fields-and-filters skill); adding one there needs a Find restart.
2. **Back up** `customization/templates.json` before editing; keep it valid JSON.
3. **Template changes need a reload** (as a FindAdmin user), not a restart. Field changes in `config.json` need a restart.
4. **Passwords:** read them from the credentials file into a variable; never print them.

## Files

In the Find home directory:
- `customization/templates.json`: which template file is used for which documents;
- `customization/templates/*.handlebars`: the templates.

```json
{
  "searchResult": [ {"file": "product-result.handlebars", "triggers": [ {"indexes": ["Products"]} ]} ],
  "previewPanel": [ ],
  "promotion": [ ], "documentFacts": [ ], "entitySearch": [ ], "entityFacts": [ ], "entityFactsDetail": [ ]
}
```
- **`searchResult`**: the card in the search results list. **`previewPanel`**: the metadata block of the panel shown when a result is opened (the document view below it is View's). Dashboard results-list widgets don't use either; their item layout is built in.
- **Triggers:** `{"indexes": ["DB", …]}` (documents from these databases) or `{"field": "<fieldsInfo id>", "values": ["…"]}`. Several triggers in one entry must all match. The first entry that matches wins; documents matching none use Find's default template.
- Keep all seven keys, even when empty.

## Example: product card

`customization/templates/product-result.handlebars` (fields `price`, `brand`, `subcategory`, `rating`, `reviews`, `colour`, `inStock` are `fieldsInfo` ids):
```handlebars
<div class="default-result-container clearfix">
    <span class="pull-right m-t-xs m-l-sm"><i class="document-selected-indicator"></i></span>
    <div class="results-contents">
        <h4 class="m-b-nil m-t-xs result-header">
            {{title}}
            {{#getFieldValue 'price'}}<span class="pull-right">£{{this}}</span>{{/getFieldValue}}
        </h4>
        <div class="m-t-xs">
            {{#getFieldValue 'brand'}}<span class="font-bold">{{this}}</span>{{/getFieldValue}}
            {{#getFieldValue 'subcategory'}}<span class="text-muted"> · {{this}}</span>{{/getFieldValue}}
            {{#hasField 'rating'}}<span class="m-l-sm">★ {{getFieldValue 'rating'}}</span>{{#getFieldValue 'reviews'}}<span class="text-muted"> ({{this}} reviews)</span>{{/getFieldValue}}{{/hasField}}
        </div>
        <div class="m-t-xs">
            {{#getFieldValues 'colour' max=4 ellipsis=''}}<span class="badge m-r-xs">{{this}}</span>{{/getFieldValues}}
            {{#hasFieldValue 'inStock' 'no'}}<span class="text-danger m-l-sm">Out of stock</span>{{/hasFieldValue}}
        </div>
        <p class="result-summary m-t-xs">{{{summary}}}</p>
        <span class="text-right results-small-links">
            <a href="{{similarDocumentsUrl}}" class="similar-documents-trigger result-container-control" role="button" tabindex="-1">{{i18n 'search.similarDocuments'}}</a>
        </span>
    </div>
</div>
```
Keep the outer `default-result-container`, the `document-selected-indicator` and the "similar documents" link: Find's result list relies on them.

**What a template can use:**
- Document values: `title`, `reference`, `date`, `database`, `summary` (HTML: use triple braces `{{{summary}}}`), `url`, `icon`, `thumbnailSrc`, `similarDocumentsUrl`, `age`, `weight`, `fields[]` (each with `id`, `values`, `displayName`).
- Field helpers (ids in quotes):
  - `{{getFieldValue 'id'}}` (first value) and `{{getFieldValues 'id' max=3 delimiter=', ' ellipsis='…'}}`; both also work as blocks, with `{{this}}` inside;
  - `{{#hasField 'id'}}…{{/hasField}}`, `{{#hasFieldValue 'id' 'value'}}…{{/hasFieldValue}}`;
  - `equal`, `regexIf`, `prettyPrintNumber`, `toRelativeTime`, `toExternalUrl`, `wikiThumbnailHelper url 120`, `i18n`.
- Find's own templates are good starting points: `unzip -p find.war WEB-INF/classes/static/js/find/templates/app/page/search/default-custom-templates/search-result.handlebars` (also `preview-mode-metadata`, `promotion`, `document-facts`, `entity-*`).

## Steps

1. Make sure every id the template uses is in `fieldsInfo` (restart Find if you added any).
2. Write the template file and the `templates.json` entry; validate the JSON.
3. Reload: `GET $F/api/admin/customization/config/reload` as a FindAdmin user (answers with a redirect; Find's `logs/server.log` shows "Reloading customization component").

**Images only, no template:** map the image URL field to the built-in `thumbnailUrl` id in `fieldsInfo` (`"thumbnailUrl": {"names": ["IMAGE_URL"], …}`). Find's default template shows it next to each result. Restart Find.

Log in and keep the session cookie:
```
F=http://127.0.0.1:<find port>
PW=$(sed -n 's/^user=curator password=\([^ ]*\).*/\1/p' credentials.txt)     # adapt to your credentials file
curl -s -c jar.txt -o /dev/null -w '%{http_code} %{redirect_url}\n' --data-urlencode username=curator --data-urlencode "password=$PW" $F/authenticate
#   302 to /public = logged in
```

## Done means (collect this evidence)

1. `GET $F/customization/result-templates` lists your template file(s).
2. A search that hits the triggering database returns documents whose `fieldMap` has every id the template uses: `GET $F/api/public/search/query-text-index/results?text=<term>&indexes=<DB>&start=1&max_results=3&summary=context&auto_correct=false` (`fieldMap` keys are lower-cased ids).
3. **A screenshot of a search page** (find-ui-check skill), opened and checked: the cards show the template's values (e.g. the price and rating), not Find's default card. Pick a search term that only matches the triggering database, so its results are on the first page.
