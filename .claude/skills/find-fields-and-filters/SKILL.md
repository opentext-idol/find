---
name: find-fields-and-filters
description: Make OpenText Find's search page fit a dataset - friendly field display names, which fields appear in result previews, the filter sidebar (which filters show, their order, hiding internal or numeric junk filters), sort options (e.g. price low to high, newest first, top rated), result tabs per role, and the message of the day. All in Find's config.json. Use when asked to rename fields, change or reorder filters, add sort options, hide filters or tabs, or "tailor the search page" in Find. Needs a running Find and data in Content.
---

# Find: fields, filters, sorting and result tabs

Everything here is in Find's `config.json` (in the Find home directory, set by `-Didol.find.home`). **Changes need a Find restart.**

## Rules (read first)

1. **Back up `config.json` before editing** (`cp config.json config.json.bak-<what>`), and keep it valid JSON (`python3 -m json.tool config.json > /dev/null`).
2. **Merge, don't replace.** Edit the keys below inside the existing file; leave the server settings (`content`, `login`, `view`, …) alone.
3. **Filters come from Content, not from `fieldsInfo`.** Find offers **every** Parametric, Numeric and NumericDate field in Content as a filter, including internal ones like `AUTN_DATE`. To add a filter, the field must have one of those types in Content (idol-index skill). To remove one, list it in `parametricNeverShow`.
4. **`parametricOrder`, `parametricNeverShow` and sort strings take raw Content field names** (upper-case, e.g. `PRICE_GBP`). **`fieldsInfo` keys are Find ids** (e.g. `price`), used by templates.
5. **Passwords:** read them from the credentials file into a variable; never print them.

## Know the data first

Ask Content which fields exist and with which type (one type per call):
```
curl "http://127.0.0.1:<content ACI port>/a=GetTagNames&fieldtype=Parametric"     # then Numeric, NumericDate
curl "http://127.0.0.1:<port>/a=GetQueryTagValues&text=*&fieldname=CATEGORY&documentcount=true&sort=documentcount&maxvalues=10"
```
Pick: the 4–8 most useful filters (low-cardinality fields people narrow by), which numeric fields are useful (price, rating) and which are internal (stock counts, sizes in bytes, ranks), and which fields belong in result previews.

## Example (product catalogue)

Fields in Content: `CATEGORY`, `SUBCATEGORY`, `BRAND`, `COLOUR`, `SIZE`, `IN_STOCK`, `MATERIAL` (Parametric); `RATING` (Parametric with numeric values); `PRICE_GBP`, `WEIGHT_G`, `STOCK_LEVEL`, `REVIEW_COUNT` (Numeric); `LAUNCH_DATE` feeds `AUTN_DATE`.

```json
"fieldsInfo": {
  "category":    {"names": ["CATEGORY"],     "displayName": "Category",   "advanced": false, "csvExport": true, "values": []},
  "subcategory": {"names": ["SUBCATEGORY"],  "displayName": "Type",       "advanced": false, "csvExport": true, "values": []},
  "brand":       {"names": ["BRAND"],        "displayName": "Brand",      "advanced": false, "csvExport": true, "values": []},
  "price":       {"names": ["PRICE_GBP"],    "displayName": "Price (£)",  "advanced": false, "csvExport": true, "values": []},
  "rating":      {"names": ["RATING"],       "displayName": "Rating",     "advanced": false, "csvExport": true, "values": []},
  "reviews":     {"names": ["REVIEW_COUNT"], "displayName": "Reviews",    "advanced": true,  "csvExport": true, "values": []},
  "colour":      {"names": ["COLOUR"],       "displayName": "Colour",     "advanced": false, "csvExport": true, "values": []},
  "weight":      {"names": ["WEIGHT_G"],     "displayName": "Weight (g)", "advanced": true,  "csvExport": true, "values": []}
},
"uiCustomization": {
  "parametricOrder": ["CATEGORY", "SUBCATEGORY", "BRAND", "PRICE_GBP", "RATING", "COLOUR", "SIZE", "IN_STOCK"],
  "parametricNeverShow": ["AUTN_DATE", "STOCK_LEVEL", "REVIEW_COUNT", "WEIGHT_G"],
  "options": {
    "enableIndexesFilter": {"user": true, "bi": true},
    "resultViewOrder": {"user": ["list"], "bi": ["list", "topic-map", "sunburst", "trending", "table"]}
  }
},
"search": {
  "defaultSortOption": "relevance",
  "sortOptions": {
    "relevance": {"sort": "relevance", "label": "by relevance"},
    "priceLow":  {"sort": "PRICE_GBP:numberincreasing", "label": "price: low to high"},
    "priceHigh": {"sort": "PRICE_GBP:numberdecreasing", "label": "price: high to low"},
    "topRated":  {"sort": "RATING:numberdecreasing+REVIEW_COUNT:numberdecreasing", "label": "top rated"},
    "newest":    {"sort": "date", "label": "newest first"}
  }
},
"messageOfTheDay": {"message": "3,000 products from the Fernhill catalogue"}
```

What each part does:

| Key | Meaning |
|---|---|
| `fieldsInfo.<id>` | `names`: the raw Content field(s). `displayName`: label shown everywhere (filters, previews). `advanced: false`: shown in the result preview; `true`: only under "Show Advanced". Reuse built-in ids where they fit: `thumbnailUrl` (the default result template shows it as an image), `url`, `authors`, `categories`, `contentType`. Don't add `type: "number"` for years or ratings: Find then shows `2021.0` |
| `parametricOrder` | Filters listed here come first, in this order; the rest follow alphabetically |
| `parametricNeverShow` | Filters to hide. Always consider `AUTN_DATE` (the date filter covers it) and internal numeric fields |
| `options.enableIndexesFilter` | **Keep it on**, even with one database: it's the only control that shows and fixes an empty database selection (symptom: a blank "filters applied (1)" chip, or "The list of databases has not yet been retrieved") |
| Other `options.<flag>: {"user": bool, "bi": bool}` | `enableRelatedConcepts` (the right-hand "related concepts" column; turn it off if it shows noise), `enableDatesFilter`, `enableDocumentSelectionFilter`, `enableGeographyFilter`, `enableMetaFilter`, `enableTypeAhead`, `enableSavedSearch`, `enableDashboards`, `enableSideBar` |
| `options.resultViewOrder` | Result tabs per role: `user` = FindUser only, `bi` = FindBI. Choose from `list`, `topic-map`, `sunburst`, `trending`, `table`, `map`, `recommendation`, `related-users`. Leave out tabs with no data behind them (`map` needs latitude/longitude fields; `related-users`/`recommendation` need Agentstore profiles) |
| `sortOptions.<key>.sort` | A **raw Content sort string**: `FIELD:numberincreasing`, `FIELD:numberdecreasing`, `FIELD:alphabetical`, `FIELD:reversealphabetical`, `relevance`, `date` (newest `AUTN_DATE` first), `reversedate`. Combine with `+` (tie-breaks) |
| `defaultSortOption` | Key of the sort used first |
| `messageOfTheDay.message` | Text in the top bar |

Other `uiCustomization` keys: `filterOrder` (sidebar sections: `metaFilter`, `indexesFilter`, `datesFilter`, `geographyFilter`, `documentSelectionFilter`, `parametricFilter`; `"-"` for a spacer), `parametricAlwaysShow`, `defaultDeselectedDatabases` (e.g. empty stock databases), `parametricValuesSort` (`DocumentCount`, `Alphabetical`, …), `listViewPagingSize`.

## Apply

1. Back up, edit, validate the JSON.
2. Restart Find: find the Java process by the port Find listens on (`ss -ltnpH 'sport = :<find port>'`), note its full command line (`/proc/<pid>/cmdline`), stop it, wait for that PID to exit, start it again with the same command line, and wait for `APPLICATION STARTED` in its log (20–60 s).

## Done means (collect this evidence)

Log in as each kind of user and keep the session cookie:
```
F=http://127.0.0.1:<find port>
PW=$(sed -n 's/^user=viewer password=\([^ ]*\).*/\1/p' credentials.txt)     # adapt to your credentials file
curl -s -c jar.txt -o /dev/null -w '%{http_code} %{redirect_url}\n' --data-urlencode username=viewer --data-urlencode "password=$PW" $F/authenticate
#   302 to /public = logged in
```
1. **Find loaded your settings:** `GET $F/public/` (with `-b jar.txt`) contains the client config as JSON in `<script … id="config-json">`; its `uiCustomization`, `fieldsInfo`-derived names and sort options match what you wrote. If they don't, Find didn't restart or the JSON is invalid (check its log).
2. **Sorting works:** `GET $F/api/public/search/query-text-index/results?text=*&indexes=<DB>&start=1&max_results=5&summary=context&auto_correct=false&sort=PRICE_GBP:numberincreasing` returns documents in that order (the `sort` parameter takes the raw sort string, not the option name).
3. **Fields reach the results:** each document's `fieldMap` in that reply has your `fieldsInfo` ids (keys come back **lower-cased**: `instock`, not `inStock`).
4. **Filters have values:** `GET $F/api/public/parametric/values?fieldNames=CATEGORY&databases=<DB>&queryText=*&maxValues=10` (`databases` is required). The counts Find shows are **estimates**: Find asks Content with `Predict=true`, so they can be higher than Content's exact counts (`GetQueryTagValues … &predict=false`). This is expected behaviour. Numeric fields (e.g. a year) show as a range histogram, not a list of values.
5. **It looks right in a browser, as each role** (find-ui-check skill): a screenshot of a search page as a FindUser-only user and as a FindBI user, opened and checked: the filter list and order, display names, the sort menu, the result tabs. Name anything still wrong in your report.
