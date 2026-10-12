---
name: find-customize
description: Start here for broad requests to tailor, customise or "make OpenText Find good for this data" - it says which Find customisation skill does what and in which order (fields and filters, result templates, dashboards, branding, UI check), plus sidebar application links, what each role sees, and features that need other IDOL components. For a specific change, load the specific skill directly.
---

# Customizing Find: which skill for what

| To change | Skill | Where the change lives | Applied by |
|---|---|---|---|
| Field names, filters and their order, hidden filters, sort options, result tabs, message of the day | **find-fields-and-filters** | `config.json` | Find restart |
| How each result or the preview panel looks; images in results | **find-result-templates** | `customization/templates.json` + `templates/*.handlebars` | reload |
| Dashboards and the saved searches behind them | **find-dashboard** | `customization/dashboards.json` + saved searches via API | reload |
| Logos, highlight colours | **find-branding** | admin API; `customization/style.json` | immediate; reload |
| Check what a user actually sees | **find-ui-check** | | |

All files are in the Find home directory (`-Didol.find.home`). "Reload" = `GET /api/admin/customization/config/reload` as a FindAdmin user.

## Tailoring Find to a new dataset: order of work

1. **Data first** (idol-index skill): the fields users filter on must be Parametric (or Numeric) in Content, dates must be clean, and the values tidy. Find does not change the data it displays.
2. **find-fields-and-filters**: names, filters, sorting, tabs.
3. **find-result-templates** (optional): richer result cards.
4. **find-dashboard** (optional, FindBI users only).
5. **find-branding** (optional).
6. **find-ui-check** as a FindUser-only user and as a FindBI user. They see different things: FindUser-only users get the big logo on their main page, no dashboards, and only the `user` result tabs.

Do them one at a time, and collect each skill's "Done means" evidence before moving on.

## Small things not covered elsewhere

- **Sidebar application links:** `customization/applications.json`, then reload:
  ```json
  {"applications": [{"applicationName": "Content admin", "url": "http://127.0.0.1:9100/a=admin", "openInNewTab": true, "enabled": true}]}
  ```
- **Features that need other components** (leave them off otherwise):

  | Feature | Needs |
  |---|---|
  | Promotions, blacklists, dictionary type-ahead | QMS |
  | Statistics | Statistics Server |
  | Answers and conversations | Answer Server |
  | Entity search panel | Content with entity databases |
  | Related users, recommendations | Agentstore profiles |
  | Map tab and widget | latitude/longitude fields and a tile server |
