---
name: idol-index
description: Get a dataset (IDOL IDX file) into an IDOL Content index well. Profile its fields, choose and configure field types (parametric, numeric, date, index), clean awkward values such as partial dates, create the database, load/reindex/sync, and verify with queries and parametric values. Use when asked to index data, add or change fields, fix how fields are stored, reindex, or check what's in Content. Needs a running Content (idol-component-setup). Everything is plain config edits and ACI requests, with curl or any HTTP client.
---

# Indexing data into Content

## Rules (read first)

1. **Use `http://127.0.0.1:PORT`, never `localhost`.** IDOL's `Clients=localhost` doesn't match IPv6 `::1`, so `localhost` requests can be refused.
2. **Indexing goes to the index port** (`[Server] IndexPort`, default 9101). Queries and status go to the ACI port (default 9100).
3. **Back up `content.cfg` before editing it**, and restart Content after editing (stop on the service port, wait for that PID to exit, start).
4. **Field-type changes only apply to documents indexed afterwards.** After changing field processing, wipe and reload (step 5).
5. **Keep the original data file.** Write cleaned data to a new file, in your working folder rather than next to the original.

## Quick reference

```
curl "http://127.0.0.1:9101/DREADD?/absolute/path/data.idx&DREDbName=Products" # -> INDEXID=1 (the server reads the path)
curl "http://127.0.0.1:9100/a=indexergetstatus&index=1"                        # job progress
curl "http://127.0.0.1:9101/DRESYNC?"                                          # make indexed documents searchable now
curl "http://127.0.0.1:9100/a=GetStatus"                                       # exact document counts per database
curl "http://127.0.0.1:9100/a=Query&text=tent&databasematch=Products&print=fields&printfields=DRETITLE,PRICE_GBP&maxresults=5"
curl "http://127.0.0.1:9100/a=GetQueryTagValues&text=*&fieldname=CATEGORY&documentcount=true&sort=documentcount&maxvalues=10"
curl "http://127.0.0.1:9100/a=GetTagNames&fieldtype=Parametric"                # which fields got which type (one type per call)
```

## Workflow

### 1. Profile the data

An IDX file is plain text, one document per block:
```
#DREREFERENCE <unique id>
#DRETITLE <title>
#DREFIELD name="value"          (repeatable; a repeated field is multi-valued)
#DREDBNAME <database>           (optional; otherwise give DREDbName= when loading)
#DRECONTENT
<body text>
#DREENDDOC
```

Before configuring anything, find out for each field:

| Question | How (grep, awk, a short script, whatever is at hand) |
|---|---|
| How many documents? | count `#DREENDDOC` lines |
| Which fields, and how many documents have each? | count `^#DREFIELD name=` per name |
| How many distinct values, and how long? | collect the values of one field; count distinct, look at the top 20 |
| Is it a date? Which format? Any **partial dates** (`2021-06`, `2019`, `00/00/2001`)? | turn digits into `9` and count the shapes (e.g. `sed -E 's/[0-9]/9/g' \| sort \| uniq -c`) |
| Numbers? Numbers with units or symbols (`£129.00`, `1.2 kg`)? URLs? Lists packed into one value with `\|`? Inconsistent case (`Navy`, `navy`)? | look at the top values, and at distinct values case-insensitively |

Then pick a type for each field:

| What you see | Field type |
|---|---|
| Few distinct, short values (category, brand, colour, country) | **parametric** (filters / facets) |
| A full date | **date** (becomes the document date, `AUTN_DATE`) |
| A price, a weight, a count, a year | **numeric** (ranges, numeric sorting); a rating or year users pick from a list: parametric with numeric values, or both (see "Field-type rules") |
| Long text (> ~200 characters) | **index** (full-text searchable) |
| Values almost all distinct (titles, ids) | not parametric; index or leave it stored |
| URLs (images, links) | leave it stored, not indexed |
| Many distinct short values (tags, people's names) | parametric still works, but it's a noisy filter; order it last in Find |

### 2. Clean the data, if needed

Write a derived IDX. Common fixes (examples from a product catalogue):
- **Partial dates** must become full dates: `2021-06` → `2021-06-01`, `2019` → `2019-01-01`, `00/00/2001` → `01/01/2001`. Otherwise Content can't parse them and stores the date as 1970 (epoch 0), which breaks date filters, date sorting and trending. A document with **no** usable date gets the time it was indexed, so it sorts as brand new: look for implausible dates (far future, far past) too, and decide what to do with them.
- **Numbers with symbols or units** need a clean numeric copy: `price="£129.00"` → add `price_gbp="129.00"` (keep the original for display). Content can't treat `£129.00` as a number.
- **Inconsistent case** in filter values splits one value into several filter entries (`Navy`, `navy`, `NAVY`): normalise it (e.g. Title Case).
- **Add derived fields** for what users will filter or sort on, e.g. `launch_year` from the launch date.
- **Image URLs** (if the UI should show images, via Find's built-in `thumbnailUrl` field): check a sample of the URLs, one of each file type, return HTTP 200 (`curl -s -o /dev/null -w '%{http_code}' -A 'Mozilla/5.0' URL`) before indexing. If you derive smaller thumbnail URLs, follow the image host's documented URL scheme and test those too.

A short script in whatever language is at hand is fine; check its counts (documents in = documents out; how many values it changed).

### 3. Configure Content (`content.cfg`)

**Databases.** The stock config ships `News` and `Archive`. Replace them with yours unless they're used; empty databases clutter Find:
```
[Databases]
NumDBs=1

[Database0]
Name=Products
```

**Field types.** `[FieldProcessing]` lists numbered property sections. Add your fields (as `*/NAME`, upper-case) to the stock ones' comma-separated `PropertyFieldCSVs`:

| Kind | Stock section |
|---|---|
| parametric | `[SetParametricFields]` |
| numeric | `[SetNumericFields]` |
| numeric date (epoch seconds) | `[SetNumericDateFields]` |
| date (the document date) | `[SetDateFields]` |
| full-text index | `[SetIndexFields]` (`[SetIndexAndWeightHigher]` for heavier weighting, e.g. titles) |
| title / reference / print / highlight | `[SetTitleFields]`, `[SetReferenceFields]`, `[SetPrintFields]`, `[SetHighlightFields]` |

**Server settings:**
- `[Server] CaseSensitiveParametricValues=TRUE`. Without it, parametric values are stored and shown UPPERCASED.
- Check `DateFormatCSVs` covers your date format. `DD/MM/YYYY`, `YYYY-MM-DD` and others are included by default.

Restart Content, then check `GetStatus` lists your database.

### 4. Load

1. `DREADD` on the index port with the **absolute** path (the server reads it, so the file must be on the Content host). It returns `INDEXID=n` at once and indexes in the background (3,000 small documents take a few seconds; 22,000 documents / 200 MB took 25–75 s).
2. Poll `a=indexergetstatus&index=n` on the ACI port until the job's `<description>` is `Finished` (or `<percentage_processed>` is 100). Job items use plain tags (`<description>`, `<documents_processed>`, `<duration_secs>`), with no `autn:` prefix.
3. `DRESYNC` on the index port. With the shipped `DelayedSync=TRUE` / `MaxSyncDelay=120`, documents aren't searchable for up to 2 minutes otherwise. Until then `GetStatus` counts them under `documents` while `committed_documents` stays lower.

Re-loading the same file replaces documents (`KillDuplicates=*/DREREFERENCE`), but the replaced copies linger as deleted sections for a while and can inflate parametric counts. After changing the data or field types, **wipe and reload** (step 5) rather than loading on top.

### 5. Wipe and reload (after field-type changes)

1. Note the PID listening on Content's ACI port (`ss -ltnpH 'sport = :ACIPORT'`) and its exact command line (`/proc/PID/cmdline`). Don't look processes up with `ps | grep`: other Content instances (an Agentstore, containers) run the same `content.exe`, with different options.
2. Stop Content on its **service** port (`curl "http://127.0.0.1:<service port>/action=stop"`), then wait until **that PID** has exited (`kill -0 PID` fails). Never delete the index of a running Content.
3. Delete the `index/` folder in the Content folder (the `[Paths]` entries all point inside it by default). Leave `license/` and `uid/` alone.
4. Start Content again from its folder with the same command line as before, then load again (step 4).

### 6. Verify

- `GetStatus`: the database's document count (exact) equals the number of `#DREENDDOC` lines.
- `GetTagNames&fieldtype=Parametric` (then `Numeric`, `NumericDate`, `Index`): each field got the type you meant.
- `GetQueryTagValues` for each filter field: sensible values, correct case.
- A query with a `fieldtext` and a `sort`, e.g. `&fieldtext=NRANGE{0,50}:PRICE_GBP&sort=PRICE_GBP:numberincreasing` (URL-encode the braces as `%7B` `%7D` in curl).

## Field-type rules

- **One property per field.** Listing a field in two `[FieldProcessing]` sections does *not* combine them. Before adding a field, check it isn't already in another property section. For a field that must be both (e.g. a rating users pick from a list *and* sort by), add a combined property:
  ```
  [FieldProcessing]
  29=SetNumericParametricFields      // next free number
  [SetNumericParametricFields]
  Property=NumericParametricFields
  PropertyFieldCSVs=*/RATING,*/LAUNCH_YEAR
  [NumericParametricFields]
  NumericType=TRUE
  ParametricType=TRUE
  ```
  (and remove the field from the single-type sections). `GetTagNames&fieldtype=Parametric` then lists it, and numeric sorting and `NRANGE` work on it. Parametric fields with numeric values support `NRANGE{a,b}:FIELD` and min/max/average through `ParametricNumericMapping=TRUE`, so often parametric alone is enough.
- **Find shows numeric fields as filters.** Find offers every Parametric/Numeric/NumericDate field (including `AUTN_DATE`) as a sidebar filter. Hide internal ones with Find's `parametricNeverShow` (find-fields-and-filters skill).

## ACI notes

- **`getquerytagvalues` needs a `text=`.** Use `text=*`.
- **`totalresults=true` is an estimate** (it can be off by a fraction of a percent). `GetStatus` database counts are exact.
- **`getfieldinfo` is not available in 26.4.** Use `GetTagNames&fieldtype=…`, one type per call. Names come back as `DOCUMENT/FIELD`. An unknown type name returns *every* field.
- **Sort syntax:** `FIELD:numberdecreasing`, `FIELD:alphabetical`, `relevance`, `date`. Combine several with `+`.
