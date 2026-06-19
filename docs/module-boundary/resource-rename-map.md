# Resource Rename Map

## Rule

- Rename module-owned resources as `<module>_<old_name>`.
- Do not rename launcher icons in this pass.
- Do not rename font files in this pass unless a build error proves a conflict.
- Do not rename `values/colors.xml`, `values/strings.xml`, or `values/themes.xml` files; rename entries inside them only when needed by a collision.

## Duplicate Scan Snapshot

Generated on 2026-06-17 with a PowerShell equivalent of the Task 7 duplicate-resource scan because `python` is not available on this machine.

Notable duplicate groups include:

- `layout/fragment_record`
- `layout/fragment_setting`
- `layout/item_record`
- `layout/item_setting`
- `layout/dialog_record_detail`
- `drawable/background_rounded`
- `drawable/background_top_rounded`
- `drawable/brain`
- `drawable/game`
- `drawable/home`
- `drawable/history`
- `drawable/settings`
- `drawable/background_border`
- `drawable/background_border_ripple`
- `drawable/background_circle`
- `drawable/call`
- `drawable/expand`
- `drawable/logout`
- `drawable/mic`
- `drawable/pulse`
- `drawable/video`
- `drawable/voiceprint`
- launcher icon resources under `mipmap-*` and `drawable/ic_launcher_*`
- shared fonts under `font/harmonyos_sans_*`
- value files named `colors.xml`, `strings.xml`, and `themes.xml`

The initial implementation tasks only rename the smaller layout/drawable set below. Other duplicates should be reviewed in a later pass after the first resource migration builds.

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
