# Resource Migration Status

Generated on 2026-06-17 after Tasks 8-11.

## Completed Renames

### Cognitive layouts

- `fragment_record` -> `cognitive_fragment_record`
- `fragment_setting` -> `cognitive_fragment_setting`
- `item_record` -> `cognitive_item_record`
- `item_setting` -> `cognitive_item_setting`

### Bridge layouts

- `fragment_record` -> `bridge_fragment_record`
- `fragment_setting` -> `bridge_fragment_setting`
- `item_record` -> `bridge_item_record`
- `item_setting` -> `bridge_item_setting`

### Cognitive drawables

- `background_rounded` -> `cognitive_background_rounded`
- `background_top_rounded` -> `cognitive_background_top_rounded`
- `brain` -> `cognitive_brain`
- `game` -> `cognitive_game`
- `home` -> `cognitive_home`
- `history` -> `cognitive_history`
- `settings` -> `cognitive_settings`

### Bridge drawables

- `background_rounded` -> `bridge_background_rounded`
- `background_top_rounded` -> `bridge_background_top_rounded`
- `brain` -> `bridge_brain`
- `game` -> `bridge_game`
- `home` -> `bridge_home`
- `history` -> `bridge_history`
- `settings` -> `bridge_settings`

## Verification

- Exact old-reference scan for the renamed `cognitive` and `bridge` layout/drawable resources passed.
- New `cognitive_*` and `bridge_*` resource files are present.
- Old `cognitive` and `bridge` target resource files are absent.
- `assembleDebug` was not run because local build verification is being done manually.

## Remaining Duplicate Resource Groups

These duplicates remain after the initial resource-prefixing pass:

- `animator/button_press`: `bridge`, `cognitive`, `common`
- `drawable/background_border`: `bridge`, `cognitive`
- `drawable/background_border_ripple`: `bridge`, `cognitive`
- `drawable/background_circle`: `bridge`, `cognitive`
- `drawable/background_top_rounded`: `app`, `common`
- `drawable/call`: `bridge`, `cognitive`
- `drawable/expand`: `bridge`, `cognitive`
- `drawable/ic_launcher_background`: `app`, `bridge`, `cognitive`
- `drawable/ic_launcher_foreground`: `app`, `bridge`, `cognitive`
- `drawable/logout`: `bridge`, `cognitive`
- `drawable/mic`: `bridge`, `cognitive`, `common`
- `drawable/pulse`: `bridge`, `cognitive`
- `drawable/video`: `bridge`, `cognitive`
- `drawable/voiceprint`: `bridge`, `cognitive`
- `font/harmonyos_sans_bold`: `app`, `bridge`, `cognitive`, `common`
- `font/harmonyos_sans_medium`: `app`, `bridge`, `cognitive`, `common`
- `font/harmonyos_sans_regular`: `app`, `bridge`, `cognitive`, `common`
- `layout/dialog_record_detail`: `bridge`, `cognitive`, `common`
- launcher icon resources under `mipmap-*`: `app`, `bridge`
- `values/colors.xml`: `app`, `bridge`, `cognitive`, `common`
- `values/strings.xml`: `app`, `bridge`, `cognitive`
- `values/themes.xml`: `app`, `bridge`, `cognitive`, `common`

## Follow-Up

- Review whether `dialog_record_detail` should live only in `common`; otherwise prefix the bridge/cognitive copies.
- Decide whether shared icon drawables such as `call`, `mic`, `video`, and `voiceprint` should move to `common` instead of being duplicated.
- Keep launcher icons and shared fonts deferred unless the build or packaging process reports a concrete collision.
- Do not rename values XML filenames; only rename entries inside them if a real resource-name collision appears.
