---
name: find-ui-check
description: See what OpenText Find actually shows a given user. Log in as that user in a browser (a browser tool you have, Playwright/Puppeteer, or headless Chrome over the DevTools protocol), open any Find page (search, dashboard, settings), and check a screenshot, console errors, failed API calls and page content. Use after any Find configuration, template, dashboard, logo or deployment change, when a user reports something odd in the Find UI, or to compare what different roles see.
---

# Checking the Find UI

API checks alone miss real problems: a dashboard list that rendered nothing, unwanted filters, a stale database selection, a cached stylesheet. After a change, look at the page as the affected kind of user.

## Rules (read first)

1. **Look at a screenshot**, not just text or logs. Layout problems only show in the picture.
2. **Check as each kind of user the change affects.** A FindBI user and a FindUser-only user get different UIs (result tabs, dashboards, splash page, big logo).
3. **Use a fresh browser profile** (or a private window) so cached CSS and saved state from earlier runs don't hide or fake a result. Delete a throwaway profile afterwards.
4. **Keep passwords out of what you show.** Read them from the credentials file; don't echo them or put them in screenshots of the login form.
5. **Don't use the user's own browser session or profile** unless they asked you to.

## Choosing a browser

Use whichever is available, in this order:
1. **A browser tool or skill this agent already has** (for example a Chrome extension or built-in browser tool). Log in through the Find login page.
2. **Playwright or Puppeteer**, if installed. Log in through the form, or set the cookie (below).
3. **Headless Chrome/Chromium over the DevTools protocol**, with no packages (Node 22+ has `fetch` and `WebSocket` built in):
   1. Log in over HTTP (`POST /authenticate` with form fields `username` and `password`; expect 302) and take the `FINDSESSIONID` value from `Set-Cookie`.
   2. Start Chrome: `--headless=new --remote-debugging-port=0 --user-data-dir=<fresh temp dir> --window-size=1400,900 about:blank`. Port 0 avoids clashes. Chrome prints `DevTools listening on ws://127.0.0.1:<port>/…` on **stderr**, and also writes the port as the first line of `<profile>/DevToolsActivePort`; poll for either for a few seconds.
   3. `GET http://127.0.0.1:<port>/json/list`, connect a WebSocket to the page target's `webSocketDebuggerUrl`.
   4. Send `Runtime.enable`, `Network.enable`, `Page.enable`, then `Network.setCookie {name: "FINDSESSIONID", value, domain: "127.0.0.1", path: "/", httpOnly: true}` and `Page.navigate {url}`.
   5. Collect events: `Runtime.consoleAPICalled` (console errors), `Runtime.exceptionThrown`, `Network.responseReceived` (status of `/api/` and `/customization/` calls), `Network.loadingFailed`.
   6. Wait (12 s for search pages, 15 s+ for dashboards), then `Runtime.evaluate` for checks and `document.body.innerText`, and `Page.captureScreenshot` (base64 PNG).
   7. Close the socket, terminate Chrome, wait for it to exit, then delete the profile folder.

## Pages

| Page | Path |
|---|---|
| Search results | `/public/search/query/<text>` (the page rewrites its URL afterwards) |
| Splash / start page | `/public/search/splash` |
| A dashboard | `/public/dashboards/<Dashboard Name>` (FindBI only) |
| Settings | `/config` (FindAdmin) |

Window size matters for dashboards: they scale to the window. Test a realistic size such as 1400×900.

## What to check

| After | Look for |
|---|---|
| Any change | No console errors or exceptions; no `/api/` or `/customization/` response ≥ 400 |
| `config.json` (fields, filters, sorting, tabs) | The sidebar filter list and their display names; the sort menu; the result tabs. Repeat as a FindUser-only user |
| Templates | Result cards show the template's fields (e.g. "Directed by"); `/customization/result-templates` lists the files |
| Dashboards | Every widget shows data. Results-list widgets hide items that don't fit, so count the rendered titles: `JSON.stringify([...document.querySelectorAll('.results-list')].map(e=>[...e.querySelectorAll('h4')].map(h=>h.innerText).filter(Boolean)))` |
| Highlight colours | Highlighted terms exist (`document.querySelectorAll('.search-text').length`) and `getComputedStyle(document.querySelector('.search-text')).backgroundColor` is the new colour |
| Logos | Visible in the screenshot; `/api/public/customization/logo/*/current` returned 200 |
| A deployment | The page loads assets from `static-<new build number>/` |
| "Something odd" reports | Reproduce the user's exact path and role. Check the API calls: e.g. a search request without `indexes=` means the user's database selection is empty |

**Menus and dropdowns** (e.g. the sort menu) open on a click event: `document.querySelector('<selector>').click()` through `Runtime.evaluate`, then wait a second before the screenshot. If one won't open headless, check the same thing through the API and say so. Aborted requests (`net::ERR_ABORTED`) when switching tabs are normal.

**Previews** (`/api/public/view/viewDocument`) depend on View fetching the document's source: for web references (e.g. Wikipedia URLs) a 500 can mean the site refused or rate-limited View's fetch. Check View's `logs/application.log`, and fetch the URL yourself, before calling it a Find problem.

Tell users who report a style change "not working" to hard-refresh: `app.css` is cached for an hour.
