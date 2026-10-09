---
name: find-dashboard
description: Add or change a dashboard in OpenText Find - the saved searches behind it (created through Find's API as a FindBI user), customization/dashboards.json with its exact shape, the widget types (results list, sunburst, trending, topic map, static text and images, map, video, clocks) and their settings, grid layout, and how to prove Find accepted it and every widget shows data. Use when asked to create, fix or change a Find dashboard, add widgets, or create saved searches.
---

# Find: dashboards and saved searches

## Rules (read first)

1. **`dashboards.json` must have exactly the shape below.** Find does not load anything else: wrong key names give an empty dashboard list, and no error is reported.
2. **Data widgets need a saved search** owned by a FindBI user, referenced by its **id**. Ids are assigned by Find and differ per install: create the saved searches first and use the ids it returns.
3. **A 200 from `/public/dashboards/<Name>` does not show the dashboard loaded**: the page always answers 200. Proof is the dashboard in the page's `config-json`, and a screenshot with data in every widget.
4. **`TrendingWidget` `dateField` must be `AUTN_DATE`** (the document date) or a NumericDate field. A field that Content's `[SetDateFields]` maps into the document date (e.g. `LAUNCH_DATE`) is **not** a date field itself: the widget shows "Error: failed to retrieve data".
5. **Back up** `customization/dashboards.json` before editing. Changes need a **reload** (as a FindAdmin user), not a restart.
6. **Passwords:** read them from the credentials file into a variable; never print them.

## 1. Log in (FindBI user with FindAdmin for the reload)

```
F=http://127.0.0.1:<find port>
PW=$(sed -n 's/^user=curator password=\([^ ]*\).*/\1/p' credentials.txt)     # adapt to your credentials file
curl -s -c jar.txt -o /dev/null -w '%{http_code} %{redirect_url}\n' --data-urlencode username=curator --data-urlencode "password=$PW" $F/authenticate
#   302 to /public = logged in
```

## 2. Create the saved searches

One `POST` per saved search; the reply is JSON with the new `id`:
```
curl -s -b jar.txt -H 'Content-Type: application/json' -d @under-50.json $F/api/bi/saved-query
```
`under-50.json`:
```json
{"title": "Under £50", "indexes": [{"name": "Products", "domain": null}],
 "conceptClusterPhrases": [{"phrase": "*", "primary": true, "clusterId": 0}],
 "parametricValues": [],
 "numericRangeRestrictions": [{"field": "PRICE_GBP", "min": 0, "max": 50}],
 "dateRangeRestrictions": [], "minScore": 0, "dateRange": null, "minDate": null, "maxDate": null}
```
- **Query text** goes in `conceptClusterPhrases[0].phrase` (`*` = everything, or words like `waterproof jacket`).
- **Filter by value:** `"parametricValues": [{"field": "CATEGORY", "value": "Footwear"}]` (raw field name, exact value).
- **Numeric range:** `numericRangeRestrictions` as above (Numeric fields, or Parametric fields with numeric values).
- **Date limits:** leave `minDate`, `maxDate` and `dateRange` as `null`. Setting `maxDate` on its own makes the saved search's results call fail. Updating a saved search does not clear a date limit, so delete the saved search and create a new one. To limit by date, restrict a numeric year field instead (e.g. `{"field": "FILM_YEAR", "min": 1900, "max": 2026}`).
- **Databases:** `indexes`, one entry per database.
- **List** (the caller's own only): `GET $F/api/bi/saved-query` → `{"savedQueries": [...]}`. **Change one:** take it from that list, remove `canEdit`, `user` and the `date*` fields, edit, and `PUT` it to `/api/bi/saved-query/{id}`. **Delete:** `DELETE /api/bi/saved-query/{id}`.

## 3. Write `customization/dashboards.json`

Example (saved search ids 1 = All products, 3 = Under £50; use your own ids):
```json
{"dashboards": [{
  "dashboardName": "Catalogue Overview", "enabled": true,
  "width": 4, "height": 4, "updateInterval": 600, "displayWidgetNames": "always",
  "widgets": [
    {"name": "About", "type": "StaticContentWidget", "x": 0, "y": 0, "width": 1, "height": 2,
     "widgetSettings": {"html": "<div style='padding:1em'><p>Every product by category and brand, the best-rated bargains, and when each category's products launched.</p></div>"}},
    {"name": "Categories and brands", "type": "SunburstWidget", "x": 1, "y": 0, "width": 2, "height": 2,
     "datasource": {"source": "SavedSearch", "config": {"type": "QUERY", "id": "1"}},
     "widgetSettings": {"firstField": "CATEGORY", "secondField": "BRAND", "maxLegendEntries": 8}},
    {"name": "Best-rated under £50", "type": "ResultsListWidget", "x": 3, "y": 0, "width": 1, "height": 4,
     "datasource": {"source": "SavedSearch", "config": {"type": "QUERY", "id": "3"}},
     "widgetSettings": {"maxResults": 8, "sort": "RATING:numberdecreasing+REVIEW_COUNT:numberdecreasing"}},
    {"name": "Launches by category", "type": "TrendingWidget", "x": 0, "y": 2, "width": 3, "height": 2,
     "datasource": {"source": "SavedSearch", "config": {"type": "QUERY", "id": "1"}},
     "widgetSettings": {"parametricField": "CATEGORY", "dateField": "AUTN_DATE", "numberOfBuckets": 12, "maxValues": 8}}
  ]}]}
```
To add a dashboard to an existing file, append another object to the `dashboards` array.

**Layout:** `width` × `height` on the dashboard is a grid; each widget's `x`, `y`, `width`, `height` are **grid cells, not pixels**. The grid stretches to fill the browser window, so more rows make every cell shorter. Widgets must fit inside the grid and not overlap. Results lists **hide items that don't fully fit** their cell: give them at least 2 rows (a 1-row list can show nothing).

**Results-list items can't be styled:** the dashboard list uses its own built-in item layout (title, thumbnail, summary), not the `templates.json` result template, and nothing in `customization/` changes it. Changing it means a code change and a rebuilt Find (no build-and-deploy skill is available yet); ask before going down that route.

**Widget types** (case-sensitive) and their `widgetSettings`:

| `type` | Needs `datasource` | `widgetSettings` |
|---|---|---|
| `StaticContentWidget` | no | `html` |
| `StaticImageWidget` | no | `url` |
| `CurrentTimeWidget`, `TimeLastRefreshedWidget` | no | none needed |
| `ResultsListWidget` | yes | `maxResults`, `sort` (raw Content sort string), `columnLayout` |
| `SunburstWidget` | yes | `firstField`, `secondField` (Parametric fields), `maxLegendEntries` |
| `TrendingWidget` | yes | `parametricField` (Parametric), `dateField` (`AUTN_DATE` or a NumericDate field), `numberOfBuckets`, `maxValues`, optional `minDate`, `maxDate`, `values` |
| `TopicMapWidget` | yes | `maxResults` |
| `MapWidget` | yes | needs latitude/longitude fields |
| `VideoWidget` | yes | needs video references |

Field settings take **raw Content field names** (Find turns them into `{id, displayName}` itself). `datasource` is always `{"source": "SavedSearch", "config": {"type": "QUERY", "id": "<id as a string>"}}`.

## 4. Check, then reload

Before reloading: the file parses as JSON; every type is in the table; every data widget's id is one of your saved searches; every widget fits in the grid without overlapping. Then reload: `GET $F/api/admin/customization/config/reload` (FindAdmin; answers with a redirect).

Dashboards are shown to FindBI users only (`enableDashboards` option), at `/public/dashboards/<Dashboard Name>` and in the side menu.

## Done means (collect this evidence)

1. **Find accepted it:** `GET $F/public/` (with `-b jar.txt`) contains JSON in `<script … id="config-json">`; its `dashboards` array lists your dashboard with **all** its widgets. An empty array, or missing widgets, means Find dropped the file or those widgets: compare your keys with the example.
2. **The saved searches exist:** `GET $F/api/bi/saved-query` lists every id the widgets use.
3. **Every widget shows data:** a screenshot (find-ui-check skill) of `/public/dashboards/<Name>` as a FindBI user at a realistic window size (e.g. 1400×900), **opened and checked widget by widget**. "Error: failed to retrieve data" or an empty widget means a wrong field or field type (see rule 4) or an empty saved search. Report what each widget shows.
