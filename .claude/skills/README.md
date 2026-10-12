# Skills

Claude Code skills for OpenText IDOL and OpenText Find. Each skill is a folder with a `SKILL.md` file. Claude Code loads them automatically for anyone who opens this project, so there is nothing to install. Claude uses a skill when a request matches its `description`.

## IDOL components

| Skill | Use it to |
|---|---|
| `idol-component-setup` | Install, license, configure, start, verify and stop IDOL / Knowledge Discovery components (Content, Community, View, and others). |
| `idol-index` | Index a dataset (IDX file) into Content: field types, reindexing and verification. |

## OpenText Find

| Skill | Use it to |
|---|---|
| `find-setup` | Deploy Find (`find.war`) on top of the IDOL components, and set up its users and roles. |
| `find-customize` | Start here for broad "tailor Find to this data" requests. It points to the skills below. |
| `find-fields-and-filters` | Rename fields, change filters and sort options, and set result tabs (`config.json`). |
| `find-result-templates` | Change result cards and the preview panel with Handlebars templates. |
| `find-dashboard` | Create or change dashboards and their saved searches. |
| `find-branding` | Change logos, highlight colours and the message of the day. |
| `find-ui-check` | Check what a given user actually sees in the Find UI, using a browser. |

## Prerequisites

Most of the skills need a running IDOL environment. Set up the components with `idol-component-setup`, then Find with `find-setup`. The `find-*` customisation skills need a running Find with data in Content.
