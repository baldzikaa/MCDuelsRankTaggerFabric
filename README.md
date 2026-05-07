# MCDuels Rank Tagger

A Fabric client-side mod that displays players' [MCDuels](https://mcduels.com) ranks above their nametags in-game.

The prefix shows each player's kit icon and their highest tier (e.g. `⚔ HT3 | playername`), so you can tell who you're up against at a glance.

## Features

- Floating nametag prefix with kit icon + highest tier across all 9 kits (Axe, Crystal, Mace, NethOp, Pot, SMP, Spear Mace, Sword, UHC)
- Picks the kit with the highest rank automatically
- Two-tier cache (in-memory + on-disk) — survives client restarts
- Async API lookups — rendering is never blocked on I/O
- Configurable via [ModMenu](https://modrinth.com/mod/modmenu) + toggle keybind
- Works on any Minecraft 1.21.4 server

## Requirements

- Minecraft **1.21.4**
- [Fabric Loader](https://fabricmc.net/) 0.16.0 or newer
- [Fabric API](https://modrinth.com/mod/fabric-api)
- [Cloth Config API](https://modrinth.com/mod/cloth-config)
- [ModMenu](https://modrinth.com/mod/modmenu) (optional — for the in-game settings screen)

## Installation

1. Install Fabric Loader for Minecraft 1.21.4.
2. Drop Fabric API, Cloth Config, and `mcduels-tagger-<version>.jar` into your `mods/` folder.
3. Launch Minecraft.

## Usage

- Join any server. Player nametags will show their MCDuels rank prefix automatically.
- Press the toggle keybind (configurable in **Options → Controls**) to show/hide tags.
- Open **Mods → MCDuels Rank Tagger → Config** (via ModMenu) to:
  - Choose which kits count toward the "highest rank" calculation
  - Toggle showing tags on yourself / on sneaking players

### Debug command

`/mcduelstagger lookup <name>` — fetch a player's rank manually (cache-first, falls back to a live API call).

## Building from source

```sh
./gradlew build
```

The output jar will be in `build/libs/`.

## Compatibility

| Minecraft       | Status                                  |
|-----------------|-----------------------------------------|
| 1.21.4          | ✅ Primary tested version               |
| 1.21.5 – 1.21.x | ✅ Supported (please report issues)     |
| ≤ 1.21.3        | ❌ Not supported (no `EntityRenderState`) |
| 1.20.x          | Planned                                 |

**Why 1.21.4 is the minimum:** in 1.21.4 Mojang refactored `PlayerEntityRenderer` to a render-state-driven system. Older versions accessed the entity directly during render and don't have the `updateRenderState(...)` hook this mod injects into.

## License

[MIT](LICENSE)
