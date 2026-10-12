---
name: find-branding
description: Brand OpenText Find - replace the big and small logos (sizes, backgrounds, upload and apply through Find's admin API), change the search-term highlight colours (customization/style.json), and set the message of the day. Covers how to prove Find serves the new logos and colours. Use when asked to rebrand, re-skin or theme Find, change its logo, name or colours, or set a banner message.
---

# Find: logos, highlight colours and message of the day

## Rules (read first)

1. **Logos are drawn at their native pixel size** in fixed boxes. Make them exactly **240×45** (big) and **100×20** (small), as PNG.
2. **Logo changes go through the admin API** as a FindAdmin user, not by copying files. They apply immediately.
3. **The highlight file is `customization/style.json`** (not `styles.json`). It needs a **reload**; browsers cache the stylesheet for an hour, so users must hard-refresh.
4. **Check what Find serves**, not just what you wrote: the served logo bytes and the served CSS.
5. **Passwords:** read them from the credentials file into a variable; never print them.

## Log in (FindAdmin user)

```
F=http://127.0.0.1:<find port>
PW=$(sed -n 's/^user=curator password=\([^ ]*\).*/\1/p' credentials.txt)     # adapt to your credentials file
curl -s -c jar.txt -o /dev/null -w '%{http_code} %{redirect_url}\n' --data-urlencode username=curator --data-urlencode "password=$PW" $F/authenticate
#   302 to /public = logged in
```

## Logos

| Logo | Size | Where it shows | Background it sits on |
|---|---|---|---|
| `BIG_LOGO` | 240×45 | splash/start page, and the main page for users without FindBI | light |
| `SMALL_LOGO` | 100×20 | the navigation bar, top left | dark (blue gradient) |

Make them with any image tool (e.g. Pillow, ImageMagick, Inkscape): a wordmark in a readable weight, **transparent background** (a solid box shows as a patch on the navigation bar's gradient), dark text for the big logo and light text for the small one.

Upload each, then make Find use them:
```
curl -s -b jar.txt -w '%{http_code}\n' -F file=@big.png   $F/api/admin/customization/assets/BIG_LOGO     # 201; 409 = a file of that name exists: repeat with -X PUT
curl -s -b jar.txt -w '%{http_code}\n' -F file=@small.png $F/api/admin/customization/assets/SMALL_LOGO
curl -s -b jar.txt -w '%{http_code}\n' -H 'Content-Type: application/json' \
     -d '{"assets": {"BIG_LOGO": "big.png", "SMALL_LOGO": "small.png"}}' $F/api/admin/customization/config     # 204
```
The names in the last call are the uploaded file names.

## Highlight colours

`customization/style.json` in the Find home directory:
```json
{
  "simpleVariables": { },
  "termHighlightColor": "#1B2A4A",
  "termHighlightBackground": "#FDE7D0"
}
```
- Plain CSS colour values. **No extra quotes inside the strings**: Find copies the value into the CSS as-is, so `"'#FDE7D0'"` produces an invalid rule that browsers drop.
- `simpleVariables` is not used by the stock stylesheet, so leave it empty unless the stylesheet is changed.
- Reload: `GET $F/api/admin/customization/config/reload` (answers with a redirect).

## Not covered by these settings

The start page's banner photo, the OpenText wordmark at the bottom of the start page, and the browser tab title and icon are part of Find's build, not its customization settings. Say so if asked to change them; don't edit files inside `find.war`.

## Message of the day

`"messageOfTheDay": {"message": "Spring sale: 20% off tents"}` in `config.json` (shown in the top bar). Needs a Find restart (find-fields-and-filters skill, "Apply").

## Done means (collect this evidence)

1. **Find serves your logos:** `curl -s -b jar.txt $F/api/public/customization/logo/BIG_LOGO/current | sha256sum` equals `sha256sum big.png` (same for `SMALL_LOGO`). Without the session cookie the reply is empty.
2. **Find serves your colours:** `curl -s -b jar.txt $F/customization/style/app.css` contains your colours in valid rules: grep for the colour values (e.g. `.search-text{background:#FDE7D0;}`); there are other `.search-text` rules too.
3. **It looks right:** screenshots (find-ui-check skill), opened and checked, of the start page and a search results page as a FindUser-only user (big logo, highlighted terms) and the search page as a FindBI user (small logo in the navigation bar). Note anything that looks off, e.g. a logo with a visible box behind it.
