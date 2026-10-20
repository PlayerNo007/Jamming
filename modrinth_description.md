<p align="center"><b>Take control of Minecraft's music.</b><br>Skip, replay, pause and loop tracks, pick any song from a searchable list,<br>and let jukeboxes pause the background music.</p>

<p align="center">Made by <b>PlayerNo007</b> · <a href="https://www.youtube.com/@PlayerNo007">YouTube</a></p>

## Features

### 🎵 Music controls, anywhere
- **Next / previous track**, **pause / resume** and **loop** with one key press, while you play
- A small **player in the main menu** (top-left corner): control the menu music without loading a world
- Pausing only pauses the **music**, everything else keeps playing

### 📜 The Jamming menu
Press <kbd>N</kbd> to open it.
- Every music track the game knows in one list: **vanilla music, music discs, and music from resource packs and other mods**
- Real track names (*Lena Raine – Ancestry*), with the artist under the title
- **Click a track to play it.** A track you picked plays to the end, even in the main menu or underwater
- **Music volume** slider (drag it or use the mouse wheel)

### 🔎 Filters and search
- **All / Here / ★ Favorites**: *Here* shows the tracks that fit where you are right now
- **Dimension filter**: Overworld, Nether, End, Menu, Discs (and mods)
- Hover a track's dimension to see **every biome it plays in**
- **Sort** by title, artist or dimension
- **Favorites** with a star, saved between game starts
- Click an **artist** or **dimension** in the list to filter by it

<details>
<summary><b>Smart search: how it works</b></summary>

| You type | It finds |
|:---|:---|
| `calm` | tracks with "calm" in the title or artist |
| `@C418` | tracks by C418 |
| `#nether` | tracks that play in the Nether |
| `#cherry` | tracks that play in the Cherry Grove biome |
| `calm @C418 #overworld` | all of it combined (plain text first) |

</details>

### 📻 Jukeboxes and note blocks
When a **jukebox or note blocks** play near you (32 blocks), the background music **pauses**. It **resumes** where it stopped once they are quiet again.

### ⏱ Gap between songs
Vanilla waits 10–20 minutes between songs. Pick your own gap: **0 s, 15 s, 30 s, 1 min, 2 min, 5 min** or **Vanilla**.

## Keys

| Action | Default key |
|:---|:---:|
| Open Jamming | <kbd>N</kbd> |
| Next track | <kbd>→</kbd> |
| Previous track | <kbd>←</kbd> |
| Pause / Resume | <kbd>Right Shift</kbd> |
| Loop track | *not set* |

Change them in **Options → Controls → Key Binds → Jamming**.

## Compatibility
- **Minecraft 26.2 – 26.3**, Fabric
- Requires **[Fabric API](https://modrinth.com/mod/fabric-api)**
- **Client-side only**: works in singleplayer and on any server, the server does not need it
- Settings are saved in `config/jamming.json` (favorites, sort, gap). You can turn off the jukebox pause there: `"pauseForJukebox": false`

## Credits
Jamming is inspired by **[Music Control](https://modrinth.com/mod/music-control)** by **Pardys** ([source](https://github.com/sf-inc/music_control)), which no longer works on Minecraft 26.2 and newer.
Music Control is released under the **MIT License**. Jamming is MIT too and keeps the original copyright notice.
Thank you, Pardys, for the original idea!

<details>
<summary><b>Changelog</b></summary>

**1.0.0**
- First release for Minecraft 26.2 – 26.3

</details>
