# Module Boundary Migration Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Move `cognitive` source packages under `com.example.cognitive.*` and then prefix module-owned resources so `cognitive`, `bridge`, `common`, and `app` stop colliding during resource merge.

**Architecture:** Do this in two independent tracks. First migrate Kotlin/Java packages while leaving resource names untouched. After package migration builds, rename resources by module prefix and update every XML/code reference in the same commit group.

**Tech Stack:** Android Gradle Plugin, Kotlin, Java, Android resource merger, Room/KAPT, ARouter.

## Global Constraints

- Do not change business behavior while moving packages or resources.
- Do not rename database entities, Room table names, JSON fields, or API payload field names.
- Do not edit generated `build/` files, APKs, release artifacts, or baseline profile outputs.
- Do not commit `local.properties`.
- Run `assembleDebug` after each task when the local Android SDK license issue is fixed.
- Until SDK licenses are fixed, run static scans and report that build verification is blocked by SDK license state.
- Keep each commit scoped to one module boundary task.
- If a task touches Android components registered in manifest, update manifest names in the same task.

---

## File Structure

Package migration touches:

- `cognitive/src/main/java/**`: move package declarations and imports from naked roots to `com.example.cognitive.*`.
- `app/src/main/AndroidManifest.xml`: update component class names for `GeofenceReceiver`, `StepForegroundService`, `SchulteGridActivity`, and `ScheduleActivity`.
- `app/src/main/java/com/example/cogwatch/**`: update imports that currently reference `user.*` or `remote.*`.
- `bridge/src/main/java/**`: update imports only if bridge references cognitive naked packages. Do not move bridge files in this package track.

Resource migration touches:

- `cognitive/src/main/res/**`: rename module-owned resources to `cognitive_*`.
- `bridge/src/main/res/**`: rename module-owned resources to `bridge_*`.
- `common/src/main/res/**`: rename module-owned resources to `common_*`.
- `app/src/main/res/**`: rename only app-owned colliding resources to `app_*`.
- Code/XML files under all modules that reference renamed resources.

---

## Task 1: Create Package Migration Map

**Files:**
- Read: `cognitive/src/main/java/**`
- Create: `docs/module-boundary/package-migration-map.md`

**Interfaces:**
- Consumes: current package declarations from `cognitive/src/main/java/**`.
- Produces: exact old-to-new package map used by Tasks 2-7.

- [ ] **Step 1: Create mapping doc**

Create `docs/module-boundary/package-migration-map.md` with this content:

```markdown
# Cognitive Package Migration Map

| Old package | New package |
| --- | --- |
| `collection.*` | `com.example.cognitive.collection.*` |
| `game.*` | `com.example.cognitive.game.*` |
| `geofence.*` | `com.example.cognitive.geofence.*` |
| `mine.*` | `com.example.cognitive.mine.*` |
| `read_assessment.*` | `com.example.cognitive.read_assessment.*` |
| `remote` | `com.example.cognitive.remote` |
| `repository` | `com.example.cognitive.repository` |
| `risk.*` | `com.example.cognitive.risk.*` |
| `schedule.*` | `com.example.cognitive.schedule.*` |
| `schulte.*` | `com.example.cognitive.schulte.*` |
| `setting.*` | `com.example.cognitive.setting.*` |
| `sports.*` | `com.example.cognitive.sports.*` |
| `ui` | `com.example.cognitive.ui` |
| `user` | `com.example.cognitive.user` |
```

- [ ] **Step 2: Confirm current naked packages**

Run:

```powershell
Get-ChildItem -Recurse -File cognitive\src\main\java -Include *.kt,*.java |
  Select-String -Pattern '^package ' |
  ForEach-Object { $_.Line.Trim() } |
  Sort-Object |
  Get-Unique
```

Expected: output includes naked packages listed in the map and existing `com.example.cognitive.*` packages.

- [ ] **Step 3: Commit mapping doc**

Run:

```powershell
git add docs/module-boundary/package-migration-map.md
git commit -m "docs: map cognitive package migration"
```

---

## Task 2: Migrate Pure Model and Risk Packages

**Files:**
- Modify: `cognitive/src/main/java/risk/**`
- Modify: imports in `cognitive/src/main/java/**`
- Modify: imports in `app/src/main/java/**` if references appear

**Interfaces:**
- Consumes: map from Task 1.
- Produces: `com.example.cognitive.risk.*` packages.

- [ ] **Step 1: Move package declarations**

For every file under `cognitive/src/main/java/risk`, replace:

```kotlin
package risk.
```

with:

```kotlin
package com.example.cognitive.risk.
```

For Java files, use the same package prefix form and keep the trailing semicolon.

- [ ] **Step 2: Update imports**

Replace imports in `cognitive/src/main/java/**`, `app/src/main/java/**`, and `bridge/src/main/java/**`:

```kotlin
import risk.
```

with:

```kotlin
import com.example.cognitive.risk.
```

- [ ] **Step 3: Static scan**

Run:

```powershell
rg -n "^(package|import) risk(\.|;|$)|import risk\." cognitive\src\main app\src\main bridge\src\main
```

Expected: no output.

- [ ] **Step 4: Build or record blocker**

Run:

```powershell
.\gradlew.bat assembleDebug --stacktrace
```

Expected after SDK licenses are fixed: build passes or fails on the next unrelated package migration issue. If it is blocked by SDK licenses, record that exact blocker in the task notes.

- [ ] **Step 5: Commit**

```powershell
git add cognitive\src\main app\src\main bridge\src\main
git commit -m "refactor: move cognitive risk packages under namespace"
```

---

## Task 3: Migrate Data, Repository, Remote, User, and Utility Packages

**Files:**
- Modify: `cognitive/src/main/java/remote/**`
- Modify: `cognitive/src/main/java/repository/**`
- Modify: `cognitive/src/main/java/user/**`
- Modify: `cognitive/src/main/java/ui/**`
- Modify: imports in `app/src/main/java/**`
- Modify: imports in `cognitive/src/main/java/**`

**Interfaces:**
- Produces: `com.example.cognitive.remote`, `com.example.cognitive.repository`, `com.example.cognitive.user`, `com.example.cognitive.ui`.

- [ ] **Step 1: Update package declarations**

Apply these replacements:

```text
package remote -> package com.example.cognitive.remote
package repository -> package com.example.cognitive.repository
package user -> package com.example.cognitive.user
package ui -> package com.example.cognitive.ui
```

Keep Java semicolons where present.

- [ ] **Step 2: Update imports**

Apply these replacements in `app/src/main/java`, `cognitive/src/main/java`, `bridge/src/main/java`, and `common/src/main/java`:

```text
import remote. -> import com.example.cognitive.remote.
import repository. -> import com.example.cognitive.repository.
import user. -> import com.example.cognitive.user.
import ui. -> import com.example.cognitive.ui.
```

- [ ] **Step 3: Static scan**

Run:

```powershell
rg -n "^(package|import) (remote|repository|user|ui)(\.|;|$)|import (remote|repository|user|ui)\." app\src\main cognitive\src\main bridge\src\main common\src\main
```

Expected: no output.

- [ ] **Step 4: Build or record blocker**

Run:

```powershell
.\gradlew.bat assembleDebug --stacktrace
```

- [ ] **Step 5: Commit**

```powershell
git add app\src\main cognitive\src\main bridge\src\main common\src\main
git commit -m "refactor: move cognitive data packages under namespace"
```

---

## Task 4: Migrate Feature UI Packages

**Files:**
- Modify: `cognitive/src/main/java/collection/**`
- Modify: `cognitive/src/main/java/game/**`
- Modify: `cognitive/src/main/java/mine/**`
- Modify: `cognitive/src/main/java/read_assessment/**`
- Modify: `cognitive/src/main/java/setting/**`
- Modify: imports in `cognitive/src/main/java/**`

**Interfaces:**
- Produces: namespaced UI feature packages.

- [ ] **Step 1: Update package declarations**

Apply:

```text
package collection. -> package com.example.cognitive.collection.
package game. -> package com.example.cognitive.game.
package mine. -> package com.example.cognitive.mine.
package read_assessment. -> package com.example.cognitive.read_assessment.
package setting. -> package com.example.cognitive.setting.
```

- [ ] **Step 2: Update imports**

Apply:

```text
import collection. -> import com.example.cognitive.collection.
import game. -> import com.example.cognitive.game.
import mine. -> import com.example.cognitive.mine.
import read_assessment. -> import com.example.cognitive.read_assessment.
import setting. -> import com.example.cognitive.setting.
```

- [ ] **Step 3: Static scan**

Run:

```powershell
rg -n "^(package|import) (collection|game|mine|read_assessment|setting)(\.|;|$)|import (collection|game|mine|read_assessment|setting)\." cognitive\src\main app\src\main bridge\src\main
```

Expected: no output.

- [ ] **Step 4: Build or record blocker**

Run:

```powershell
.\gradlew.bat assembleDebug --stacktrace
```

- [ ] **Step 5: Commit**

```powershell
git add cognitive\src\main app\src\main bridge\src\main
git commit -m "refactor: move cognitive UI feature packages under namespace"
```

---

## Task 5: Migrate Android Component Packages and Manifest References

**Files:**
- Modify: `cognitive/src/main/java/geofence/**`
- Modify: `cognitive/src/main/java/schedule/**`
- Modify: `cognitive/src/main/java/schulte/**`
- Modify: `cognitive/src/main/java/sports/**`
- Modify: `app/src/main/AndroidManifest.xml`
- Modify: imports in `app/src/main/java/**`
- Modify: imports in `cognitive/src/main/java/**`

**Interfaces:**
- Produces: manifest-valid class names:
  - `com.example.cognitive.geofence.receiver.GeofenceReceiver`
  - `com.example.cognitive.schedule.ui.ScheduleActivity`
  - `com.example.cognitive.schulte.ui.SchulteGridActivity`
  - `com.example.cognitive.sports.data.StepForegroundService`

- [ ] **Step 1: Update package declarations**

Apply:

```text
package geofence. -> package com.example.cognitive.geofence.
package schedule. -> package com.example.cognitive.schedule.
package schulte. -> package com.example.cognitive.schulte.
package sports. -> package com.example.cognitive.sports.
```

- [ ] **Step 2: Update imports**

Apply:

```text
import geofence. -> import com.example.cognitive.geofence.
import schedule. -> import com.example.cognitive.schedule.
import schulte. -> import com.example.cognitive.schulte.
import sports. -> import com.example.cognitive.sports.
```

- [ ] **Step 3: Update manifest component names**

In `app/src/main/AndroidManifest.xml`, replace:

```xml
android:name="geofence.receiver.GeofenceReceiver"
android:name="schulte.ui.SchulteGridActivity"
android:name="sports.data.StepForegroundService"
android:name="schedule.ui.ScheduleActivity"
```

with:

```xml
android:name="com.example.cognitive.geofence.receiver.GeofenceReceiver"
android:name="com.example.cognitive.schulte.ui.SchulteGridActivity"
android:name="com.example.cognitive.sports.data.StepForegroundService"
android:name="com.example.cognitive.schedule.ui.ScheduleActivity"
```

- [ ] **Step 4: Static scan**

Run:

```powershell
rg -n "^(package|import) (geofence|schedule|schulte|sports)(\.|;|$)|import (geofence|schedule|schulte|sports)\.|android:name=\"(geofence|schedule|schulte|sports)\." app\src\main cognitive\src\main
```

Expected: no output.

- [ ] **Step 5: Build or record blocker**

Run:

```powershell
.\gradlew.bat assembleDebug --stacktrace
```

- [ ] **Step 6: Commit**

```powershell
git add app\src\main cognitive\src\main
git commit -m "refactor: move cognitive component packages under namespace"
```

---

## Task 6: Verify Package Migration Complete

**Files:**
- Read: `cognitive/src/main/java/**`
- Read: `app/src/main/AndroidManifest.xml`

**Interfaces:**
- Produces: clean package namespace baseline before resource rename.

- [ ] **Step 1: Scan for naked packages**

Run:

```powershell
rg -n "^(package|import) (collection|game|geofence|mine|read_assessment|remote|repository|risk|schedule|schulte|setting|sports|ui|user)(\.|;|$)|import (collection|game|geofence|mine|read_assessment|remote|repository|risk|schedule|schulte|setting|sports|ui|user)\." app\src\main cognitive\src\main bridge\src\main common\src\main
```

Expected: no output.

- [ ] **Step 2: Scan manifest components**

Run:

```powershell
rg -n "android:name=\"(geofence|schedule|schulte|sports)\." app\src\main\AndroidManifest.xml
```

Expected: no output.

- [ ] **Step 3: Build or record blocker**

Run:

```powershell
.\gradlew.bat assembleDebug --stacktrace
```

- [ ] **Step 4: Commit if any cleanup was needed**

```powershell
git add app\src\main cognitive\src\main bridge\src\main common\src\main
git commit -m "chore: verify cognitive package namespace migration"
```

---

## Task 7: Create Resource Rename Map

**Files:**
- Create: `docs/module-boundary/resource-rename-map.md`
- Read: `app/src/main/res/**`
- Read: `bridge/src/main/res/**`
- Read: `cognitive/src/main/res/**`
- Read: `common/src/main/res/**`

**Interfaces:**
- Produces: exact resource old-to-new mapping for Tasks 8-11.

- [ ] **Step 1: Generate duplicate resource list**

Run:

```powershell
@'
from pathlib import Path
from collections import defaultdict
modules = ["app", "bridge", "cognitive", "common"]
resources = defaultdict(list)
for module in modules:
    base = Path(module, "src/main/res")
    if not base.exists():
        continue
    for path in base.rglob("*"):
        if path.is_file():
            resources[(path.parent.name, path.stem)].append(str(path))
for (kind, name), paths in sorted(resources.items()):
    if len(paths) > 1:
        print(f"{kind}/{name}: " + " | ".join(paths))
'@ | python -
```

Expected: list of current duplicate resource names. If `python` is unavailable, use the bundled Python path from the workspace dependencies.

- [ ] **Step 2: Create mapping doc**

Create `docs/module-boundary/resource-rename-map.md` with sections:

```markdown
# Resource Rename Map

## Rule

- Rename module-owned resources as `<module>_<old_name>`.
- Do not rename launcher icons in this pass.
- Do not rename font files in this pass unless a build error proves a conflict.
- Do not rename `values/colors.xml`, `values/strings.xml`, or `values/themes.xml` files; rename entries inside them only when needed by a collision.

## Cognitive Initial Map

| Type | Old | New |
| --- | --- | --- |
| layout | `fragment_record` | `cognitive_fragment_record` |
| layout | `fragment_setting` | `cognitive_fragment_setting` |
| layout | `item_record` | `cognitive_item_record` |
| layout | `item_setting` | `cognitive_item_setting` |
| drawable | `background_rounded` | `cognitive_background_rounded` |
| drawable | `background_top_rounded` | `cognitive_background_top_rounded` |
| drawable | `brain` | `cognitive_brain` |
| drawable | `game` | `cognitive_game` |
| drawable | `home` | `cognitive_home` |
| drawable | `history` | `cognitive_history` |
| drawable | `settings` | `cognitive_settings` |

## Bridge Initial Map

| Type | Old | New |
| --- | --- | --- |
| layout | `fragment_record` | `bridge_fragment_record` |
| layout | `fragment_setting` | `bridge_fragment_setting` |
| layout | `item_record` | `bridge_item_record` |
| layout | `item_setting` | `bridge_item_setting` |
| drawable | `background_rounded` | `bridge_background_rounded` |
| drawable | `background_top_rounded` | `bridge_background_top_rounded` |
| drawable | `brain` | `bridge_brain` |
| drawable | `game` | `bridge_game` |
| drawable | `home` | `bridge_home` |
| drawable | `history` | `bridge_history` |
| drawable | `settings` | `bridge_settings` |
```

- [ ] **Step 3: Commit map**

```powershell
git add docs/module-boundary/resource-rename-map.md
git commit -m "docs: map module resource renames"
```

---

## Task 8: Prefix Cognitive Layout Resources

**Files:**
- Move: selected `cognitive/src/main/res/layout/*.xml`
- Modify: `cognitive/src/main/java/**`
- Modify: cognitive XML files that use `<include layout="@layout/...">`

**Interfaces:**
- Produces: `R.layout.cognitive_*` references for renamed cognitive layouts.

- [ ] **Step 1: Rename files**

Move:

```text
cognitive/src/main/res/layout/fragment_record.xml -> cognitive/src/main/res/layout/cognitive_fragment_record.xml
cognitive/src/main/res/layout/fragment_setting.xml -> cognitive/src/main/res/layout/cognitive_fragment_setting.xml
cognitive/src/main/res/layout/item_record.xml -> cognitive/src/main/res/layout/cognitive_item_record.xml
cognitive/src/main/res/layout/item_setting.xml -> cognitive/src/main/res/layout/cognitive_item_setting.xml
```

- [ ] **Step 2: Update Kotlin/Java references**

Replace in `cognitive/src/main/java/**`:

```text
R.layout.fragment_record -> R.layout.cognitive_fragment_record
R.layout.fragment_setting -> R.layout.cognitive_fragment_setting
R.layout.item_record -> R.layout.cognitive_item_record
R.layout.item_setting -> R.layout.cognitive_item_setting
```

- [ ] **Step 3: Update XML references**

Replace in `cognitive/src/main/res/**/*.xml`:

```text
@layout/fragment_record -> @layout/cognitive_fragment_record
@layout/fragment_setting -> @layout/cognitive_fragment_setting
@layout/item_record -> @layout/cognitive_item_record
@layout/item_setting -> @layout/cognitive_item_setting
```

- [ ] **Step 4: Static scan**

Run:

```powershell
rg -n "R\.layout\.(fragment_record|fragment_setting|item_record|item_setting)|@layout/(fragment_record|fragment_setting|item_record|item_setting)" cognitive\src\main
```

Expected: no output.

- [ ] **Step 5: Build or record blocker**

Run:

```powershell
.\gradlew.bat assembleDebug --stacktrace
```

- [ ] **Step 6: Commit**

```powershell
git add cognitive\src\main
git commit -m "refactor: prefix cognitive layout resources"
```

---

## Task 9: Prefix Bridge Layout Resources

**Files:**
- Move: selected `bridge/src/main/res/layout/*.xml`
- Modify: `bridge/src/main/java/**`
- Modify: bridge XML files that reference renamed layouts.

**Interfaces:**
- Produces: `R.layout.bridge_*` references for renamed bridge layouts.

- [ ] **Step 1: Rename files**

Move:

```text
bridge/src/main/res/layout/fragment_record.xml -> bridge/src/main/res/layout/bridge_fragment_record.xml
bridge/src/main/res/layout/fragment_setting.xml -> bridge/src/main/res/layout/bridge_fragment_setting.xml
bridge/src/main/res/layout/item_record.xml -> bridge/src/main/res/layout/bridge_item_record.xml
bridge/src/main/res/layout/item_setting.xml -> bridge/src/main/res/layout/bridge_item_setting.xml
```

- [ ] **Step 2: Update Kotlin/Java references**

Replace in `bridge/src/main/java/**`:

```text
R.layout.fragment_record -> R.layout.bridge_fragment_record
R.layout.fragment_setting -> R.layout.bridge_fragment_setting
R.layout.item_record -> R.layout.bridge_item_record
R.layout.item_setting -> R.layout.bridge_item_setting
```

- [ ] **Step 3: Static scan**

Run:

```powershell
rg -n "R\.layout\.(fragment_record|fragment_setting|item_record|item_setting)|@layout/(fragment_record|fragment_setting|item_record|item_setting)" bridge\src\main
```

Expected: no output.

- [ ] **Step 4: Build or record blocker**

Run:

```powershell
.\gradlew.bat assembleDebug --stacktrace
```

- [ ] **Step 5: Commit**

```powershell
git add bridge\src\main
git commit -m "refactor: prefix bridge layout resources"
```

---

## Task 10: Prefix Cognitive Drawable Resources

**Files:**
- Move: selected `cognitive/src/main/res/drawable/*.xml`
- Modify: `cognitive/src/main/java/**`
- Modify: `cognitive/src/main/res/**/*.xml`

**Interfaces:**
- Produces: `R.drawable.cognitive_*` and `@drawable/cognitive_*`.

- [ ] **Step 1: Rename selected files**

Move:

```text
cognitive/src/main/res/drawable/background_rounded.xml -> cognitive/src/main/res/drawable/cognitive_background_rounded.xml
cognitive/src/main/res/drawable/background_top_rounded.xml -> cognitive/src/main/res/drawable/cognitive_background_top_rounded.xml
cognitive/src/main/res/drawable/brain.xml -> cognitive/src/main/res/drawable/cognitive_brain.xml
cognitive/src/main/res/drawable/game.xml -> cognitive/src/main/res/drawable/cognitive_game.xml
cognitive/src/main/res/drawable/home.xml -> cognitive/src/main/res/drawable/cognitive_home.xml
cognitive/src/main/res/drawable/history.xml -> cognitive/src/main/res/drawable/cognitive_history.xml
cognitive/src/main/res/drawable/settings.xml -> cognitive/src/main/res/drawable/cognitive_settings.xml
```

- [ ] **Step 2: Update code references**

Replace in `cognitive/src/main/java/**`:

```text
R.drawable.background_rounded -> R.drawable.cognitive_background_rounded
R.drawable.background_top_rounded -> R.drawable.cognitive_background_top_rounded
R.drawable.brain -> R.drawable.cognitive_brain
R.drawable.game -> R.drawable.cognitive_game
R.drawable.home -> R.drawable.cognitive_home
R.drawable.history -> R.drawable.cognitive_history
R.drawable.settings -> R.drawable.cognitive_settings
```

- [ ] **Step 3: Update XML references**

Replace in `cognitive/src/main/res/**/*.xml`:

```text
@drawable/background_rounded -> @drawable/cognitive_background_rounded
@drawable/background_top_rounded -> @drawable/cognitive_background_top_rounded
@drawable/brain -> @drawable/cognitive_brain
@drawable/game -> @drawable/cognitive_game
@drawable/home -> @drawable/cognitive_home
@drawable/history -> @drawable/cognitive_history
@drawable/settings -> @drawable/cognitive_settings
```

- [ ] **Step 4: Static scan**

Run:

```powershell
rg -n "R\.drawable\.(background_rounded|background_top_rounded|brain|game|home|history|settings)|@drawable/(background_rounded|background_top_rounded|brain|game|home|history|settings)" cognitive\src\main
```

Expected: no output.

- [ ] **Step 5: Build or record blocker**

Run:

```powershell
.\gradlew.bat assembleDebug --stacktrace
```

- [ ] **Step 6: Commit**

```powershell
git add cognitive\src\main
git commit -m "refactor: prefix cognitive drawable resources"
```

---

## Task 11: Prefix Bridge Drawable Resources

**Files:**
- Move: selected `bridge/src/main/res/drawable/*.xml`
- Modify: `bridge/src/main/java/**`
- Modify: `bridge/src/main/res/**/*.xml`

**Interfaces:**
- Produces: `R.drawable.bridge_*` and `@drawable/bridge_*`.

- [ ] **Step 1: Rename selected files**

Move:

```text
bridge/src/main/res/drawable/background_rounded.xml -> bridge/src/main/res/drawable/bridge_background_rounded.xml
bridge/src/main/res/drawable/background_top_rounded.xml -> bridge/src/main/res/drawable/bridge_background_top_rounded.xml
bridge/src/main/res/drawable/brain.xml -> bridge/src/main/res/drawable/bridge_brain.xml
bridge/src/main/res/drawable/game.xml -> bridge/src/main/res/drawable/bridge_game.xml
bridge/src/main/res/drawable/home.xml -> bridge/src/main/res/drawable/bridge_home.xml
bridge/src/main/res/drawable/history.xml -> bridge/src/main/res/drawable/bridge_history.xml
bridge/src/main/res/drawable/settings.xml -> bridge/src/main/res/drawable/bridge_settings.xml
```

- [ ] **Step 2: Update code references**

Replace in `bridge/src/main/java/**`:

```text
R.drawable.background_rounded -> R.drawable.bridge_background_rounded
R.drawable.background_top_rounded -> R.drawable.bridge_background_top_rounded
R.drawable.brain -> R.drawable.bridge_brain
R.drawable.game -> R.drawable.bridge_game
R.drawable.home -> R.drawable.bridge_home
R.drawable.history -> R.drawable.bridge_history
R.drawable.settings -> R.drawable.bridge_settings
```

- [ ] **Step 3: Update XML references**

Replace in `bridge/src/main/res/**/*.xml`:

```text
@drawable/background_rounded -> @drawable/bridge_background_rounded
@drawable/background_top_rounded -> @drawable/bridge_background_top_rounded
@drawable/brain -> @drawable/bridge_brain
@drawable/game -> @drawable/bridge_game
@drawable/home -> @drawable/bridge_home
@drawable/history -> @drawable/bridge_history
@drawable/settings -> @drawable/bridge_settings
```

- [ ] **Step 4: Static scan**

Run:

```powershell
rg -n "R\.drawable\.(background_rounded|background_top_rounded|brain|game|home|history|settings)|@drawable/(background_rounded|background_top_rounded|brain|game|home|history|settings)" bridge\src\main
```

Expected: no output.

- [ ] **Step 5: Build or record blocker**

Run:

```powershell
.\gradlew.bat assembleDebug --stacktrace
```

- [ ] **Step 6: Commit**

```powershell
git add bridge\src\main
git commit -m "refactor: prefix bridge drawable resources"
```

---

## Task 12: Final Collision Scan

**Files:**
- Read: `app/src/main/res/**`
- Read: `bridge/src/main/res/**`
- Read: `cognitive/src/main/res/**`
- Read: `common/src/main/res/**`

**Interfaces:**
- Produces: list of remaining acceptable collisions and follow-up task list.

- [ ] **Step 1: Run duplicate resource scan**

Run the duplicate resource script from Task 7.

Expected: remaining duplicates are reviewed and intentionally deferred, such as launcher icons, shared fonts, and value file names.

- [ ] **Step 2: Scan code for old resource references**

Run:

```powershell
rg -n "R\.(layout|drawable)\.(fragment_record|fragment_setting|item_record|item_setting|background_rounded|background_top_rounded|brain|game|home|history|settings)" app\src\main bridge\src\main cognitive\src\main common\src\main
```

Expected: no output for renamed resources.

- [ ] **Step 3: Build or record blocker**

Run:

```powershell
.\gradlew.bat assembleDebug --stacktrace
```

- [ ] **Step 4: Commit final verification notes**

```powershell
git add docs/module-boundary
git commit -m "docs: record module boundary migration status"
```

---

## Self-Review

- Spec coverage: package namespace migration is covered by Tasks 1-6; resource prefixing is covered by Tasks 7-12.
- Placeholder scan: no task uses TBD/TODO/fill-in language; each task has exact files, replacements, commands, and expected scan output.
- Type consistency: all new package names use `com.example.cognitive.*`; manifest component names match Task 5 outputs; resource prefixes use `cognitive_` and `bridge_`.

