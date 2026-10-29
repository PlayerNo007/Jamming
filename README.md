# Jamming

**Take control of Minecraft's music.** Skip, replay, pause and loop tracks, pick any song from a searchable list, and let jukeboxes pause the background music.

> Made by **PlayerNo007** · Watch the making-of on YouTube: [youtube.com/@PlayerNo007](https://www.youtube.com/@PlayerNo007)

---

## Features

### Music controls, anywhere
- **Next / previous track**, **pause / resume** and **loop** with a key press, while you play.
- A small **player in the main menu** (top-left corner): control the menu music and open Jamming without loading a world.
- Pausing only pauses the **music**. Everything else keeps playing.

### The Jamming menu (press **N**)
- Every music track the game knows, in one list: **vanilla music, music discs, and music from resource packs and other mods**.
- Real track names (*"Lena Raine – Ancestry"*), with the artist under the title.
- **Click a track to play it.** A track you picked plays to the end, even in the main menu or underwater.
- **Music volume** slider (drag it or use the mouse wheel).

### Filters and search
- **All / Here / ★ Favorites**: *Here* shows the tracks that fit where you are right now (biome, dimension, menu).
- **Dimension filter**: Overworld, Nether, End, Menu, Discs (and mods).
- Hover a track's dimension to see **every biome it plays in**.
- **Smart search**:
  - `calm` → title or artist
  - `@C418` → artist
  - `#nether`, `#cherry` → dimension or biome
  - combine them: `calm @C418 #overworld`
- Click an **artist** or **dimension** in the list to filter by it.
- **Sort** by title, artist or dimension.
- **Favorites** with a star. Saved between game starts.

### Jukeboxes and note blocks
When a **jukebox or note blocks** play near you (32 blocks), the background music **pauses**, and it **resumes** where it stopped once they are quiet again.

### Gap between songs
Vanilla waits 10–20 minutes between songs. Choose your own gap: **0 s, 15 s, 30 s, 1 min, 2 min, 5 min** or **Vanilla**.

---

## Keys

| Action | Default key |
|---|---|
| Open Jamming | **N** |
| Next track | **→** (Right Arrow) |
| Previous track | **←** (Left Arrow) |
| Pause / Resume | **Right Shift** |
| Loop track | *not set* |

Change them in **Options → Controls → Key Binds → Jamming**.

---

## Compatibility

- **Minecraft 26.2 – 26.3**, **Fabric Loader**
- Requires **[Fabric API](https://modrinth.com/mod/fabric-api)**
- **Client-side only**: works in singleplayer and on any server. The server does not need it.
- Settings are saved in `config/jamming.json` (favorites, sort, gap). You can also turn off the jukebox pause there (`"pauseForJukebox": false`).

---

## Credits

Jamming is inspired by **[Music Control](https://modrinth.com/mod/music-control)** by **Pardys** ([source on GitHub](https://github.com/sf-inc/music_control)), which no longer works on Minecraft 26.2 and newer.
Music Control is released under the **MIT License**. Jamming is MIT too, and keeps the original copyright notice in its license file.
Thank you, Pardys, for the original idea!

---

## Changelog

### 1.0.1
- Fixed: paused music no longer starts playing again after changing dimension, respawning or leaving the world. It stays paused until you resume it.

### 1.0.0
- First release for Minecraft 26.2 – 26.3.
