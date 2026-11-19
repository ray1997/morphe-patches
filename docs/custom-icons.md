# Custom icons mod

The patch replaces LINE's UI drawables with Microsoft Fluent 24dp vectors.

## Replaced families

| Family | Count | Notes |
|---|---:|---|
| `navi_bottom_*`, `timeline_navi_bottom_*` | 70 | Tab bar and Timeline tabs; `_on` is Filled, `_off` is Regular, `_new` keeps its red dot |
| `navi_top_*`, `timeline_grand_navi_top_*`, `main_tab_search_*` | 74 | Main tab top bars |
| `chat_tab_ui_header_*`, `chat_tab_ui_friends_sub_tab_*` | 17 | Chats and Friends headers, plus menu, See more chevrons |
| `setting_ic_*` | 26 | Settings rows; the `@color/...` reference is kept |
| `home_*`, `home26_*` | 15 | Home services, profile chevrons, feed controls, AI summary |

Total: 203 drawables.

## Placement

| Original size | Glyph box |
|---|---|
| 64x85 | 20px at (22, 40) |
| 38x38 | 20px at (9, 9) |
| 36x36 | 28px at (4, 4) |
| 28x28 | 26px at (1, 1) |
| 22x22 | 21px at (0.5, 0.5) |
| 67x67 | 40px, centred |
| 69x69 | 39px, centred |
| other | fitted to the smaller viewport dimension |

## Deliberately unchanged

- Brand marks: Apple, Google, Google Assistant, PayPay, LINE Premium, LYP.
- Premium and birthday badges, ranking backgrounds, YouTube badge.
- Composed buttons: `setting_ic_profile_photo_button`, `home26_ai_matome_agent_sound`,
  `home26_feed_image_sound_*`, `home_tab_ic_safety_check`.
- WebP reaction images.
- The Chats archive glyph: drawn in Compose, not a drawable.
