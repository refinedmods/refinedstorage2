---
name: community-addons-audit
description: Audit the community addons page against the live CurseForge and Modrinth listings, then apply what it finds.
disable-model-invocation: true
---

# Community addons audit

`docs/pages/addons/community-addons.adoc` is the only page listing community addons. It
goes stale every time an author publishes a new addon or ports an existing one to a new
Minecraft version, and nothing in the repo detects that — the truth lives on CurseForge
and Modrinth. This skill rebuilds the page from those two listings.

Steps 1–4 produce a change list. **The user approves that list before step 6 edits
anything.**

## 1. Read the page and the search results

Read the current page first: it defines the entry format and the existing order.

Then sweep the CurseForge category with WebFetch (`curl` is sandboxed and CurseForge sits
behind Cloudflare), one call per page. The response reports a total count — keep paging
until you have covered it.

```
https://www.curseforge.com/minecraft/search?class=mc-mods&page=<N>&pageSize=50&sortBy=relevancy&categories=refined-storage&version=26.1.2%2C1.21.1
```

Ask each fetch for name, slug and blurb of every result.

## 2. Triage every result into exactly one bucket

The `refined-storage` category is heavily polluted — expect a quarter of the first page
and nearly all of the last to have no connection to Refined Storage at all. Triage is the
bulk of the work, and the user wants to see your judgement, so name the mods in every
bucket.

- **Community addon** — a real RS addon, or a mod with genuine RS integration. In scope.
- **Official** — anything matching `refined-storage-*-integration`, plus Quartz Arsenal,
  Quartz Accessories and Mekanism. These have their own pages via `addons/index.adoc`;
  they never belong on the community page.
- **Miscategorized** — no RS relationship. Out of scope; list the names.
- **Borderline** — RS is optional, or reached only through a third mod. List these
  separately and let the user decide rather than deciding for them.

## 3. Pull versions and descriptions per candidate

Fetch each candidate's CurseForge page. Its file list is the source of truth for the
`Supports` cell — a mod page's prose often lags its actual builds.

Only 1.21.1 and 26.1.2 are in scope, and each maps to an RS major version:

| Minecraft | Refined Storage |
|---|---|
| 1.21.1 | v2 |
| 26.1.2 | v3 |

So the `Supports` cell reads
`Refined Storage v2 on Minecraft 1.21.1, Refined Storage v3 on Minecraft 26.1.2` when both
are built, or just the matching half when one is. Drop a candidate that builds for
neither.

While you are on the page, check the blurb against the existing entry's description. Stale
descriptions are as common as stale versions — Cable Tiers was described as adding cables
long after it had switched to adding tiered devices.

## 4. Cross-check Modrinth

Use the API, not the web UI:

- search — `https://api.modrinth.com/v2/search?query=<terms>&limit=100`
- project — `https://api.modrinth.com/v2/project/<slug>`

**Search ANDs every term**, so a specific multi-word query returns zero hits and reads
exactly like "not published there". Sweep broad first: `refined storage` and `refined` at
`limit=100` cover most of the ecosystem in two calls. Fall back to a single distinctive
term (`qio`, `schematicannon`, `replication`) for whatever is still unaccounted for.

Confirm every hit through the project endpoint before linking it. A matching name is not a
match — the description must describe the same mod **and** `game_versions` must overlap the
CurseForge versions. Traps that have actually bitten:

- Modrinth `create-refined-storage-recipes` is a different, 1.20.1-only mod from CurseForge
  `create-refined-recipes`.
- Slugs diverge across sites: `refined-schematics` on CurseForge is `refinedschematics` on
  Modrinth.
- Authors typo their own slug (`rs-storage-wanings`). Link it as published.
- A Modrinth page can lag its CurseForge twin — RS: Requestify stops at 1.20.1 there while
  CurseForge ships 1.21.1. Keep that entry CurseForge-only.

Run this over the existing entries too, not just the new ones: entries already on the page
sometimes gained a Modrinth release since they were written.

## 5. Report, then wait

Present additions, description changes, version bumps and deletions, each with the
evidence (which versions, which slugs). Add the ignored-official and ignored-miscategorized
lists, and flag the borderline calls as questions. Then stop and wait.

## 6. Apply

Copy an existing entry's shape rather than inventing one. Beyond that:

- Keep the existing entry order and slot each new entry beside a related one, so pairs such
  as the two Replication bridges read together.
- A section title containing a colon breaks AsciiDoc natural cross-references. Give it an
  explicit `[#id]` anchor and link it as `<<id,Title>>`.
- `xref:` paths resolve relative to `docs/pages/addons/`.

Verify before reporting done — every xref must resolve, and the table delimiters must
balance:

```bash
cd docs/pages/addons
grep -o 'xref:[^[]*' community-addons.adoc | sed 's/xref://' | sort -u |
  while read -r f; do [ -f "$f" ] || echo "MISSING $f"; done
echo "sections=$(grep -c '^== ' community-addons.adoc)" \
     "supports=$(grep -c '\*Supports\*' community-addons.adoc)" \
     "downloads=$(grep -c '\*Download\*' community-addons.adoc)" \
     "delims=$(grep -c '^|===' community-addons.adoc)"
```

`sections`, `supports` and `downloads` must be equal, and `delims` exactly twice that.
