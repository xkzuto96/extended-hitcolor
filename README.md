# Extended HitColor

A client-side Fabric mod for Minecraft that fixes armor damage flashing and gives you an in-game menu to customize your hit color.

In vanilla Minecraft, when you hit a player or mob, only their body flashes red while their armor and trims stay completely unchanged. Extended HitColor fixes that so armor and trims flash in sync with the entity, and adds an in-game color picker GUI to tweak the damage flash color, opacity, or toggle features.

### Supported Versions
* **Fabric 26.1, 26.1.1, 26.1.2, 26.2, 26.3**
* Client-side only — works on any multiplayer server without needing the mod on the server.
* Works alongside performance and rendering mods (Sodium, Iris, ImmediatelyFast, etc.).

### Features
* **Armor & Trim Flashing**: Armor, trims, and enchanted equipment flash with the exact same damage tint and timing as the player/mob.
* **In-Game Color Picker GUI**: Full visual spectrum picker with hue and opacity sliders, hex code input (`#RRGGBB` / `#AARRGGBB`), preset colors, and a live preview box.
* **Custom Color & Opacity**: Change the flash to any color you like (white, purple, cyan, yellow, etc.) and adjust the transparency to your preference.
* **Independent Toggles**: Toggle armor flashing and custom hit colors separately.
* **Keybind & Chat Command**: Press **H** (configurable under Controls -> Key Binds) or type `/hitcolor` (or `/extendedhitcolor`) in chat.
* **Mod Menu Support**: Directly accessible from your mod list if Mod Menu is installed.

### How to Use
1. Press **H** or run `/hitcolor` to open the configuration screen.
2. Select a color using the visual picker, use the RGB/Alpha sliders, click one of the preset color swatches, or type/paste a hex code.
3. Click **Done** to save. Settings are automatically saved to `.minecraft/config/extended_hitcolor.json`.

### Requirements
* Fabric Loader (>= 0.19.5)
* Fabric API
* Java 25
* Mod Menu (optional)

### Building from Source
Clone the repo and build with Gradle:
```bash
./gradlew build
```
Built jars for all versions will be output to `build/libs/`.

### License
This project is licensed under the MIT License.
