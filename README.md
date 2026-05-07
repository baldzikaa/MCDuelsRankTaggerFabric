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

- Minecraft **1.21.4** or newer (1.21.x)
- **Java 21** (required by Minecraft 1.21.4+)
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

Two builds cover the supported range — Mojang refactored the entity-renderer
internals between 1.21.8 and 1.21.11, so a single jar can't bind on both.

| Minecraft        | Build                | Status                                |
|------------------|----------------------|---------------------------------------|
| 1.21.4 – 1.21.8  | `0.1.x` (1.21.4 yarn) | ✅ Tested                             |
| 1.21.9, 1.21.10  | `0.1.x` or `0.2.x`   | ⚠️ Untested — try the closest build   |
| 1.21.11          | `0.2.x` (1.21.11 yarn) | ✅ Tested                            |
| 1.21.12+         | `0.2.x`              | ✅ Likely works (please report)       |
| ≤ 1.21.3         | —                    | ❌ Not supported (no `EntityRenderState`) |
| 1.20.x           | —                    | Planned                               |

**Why 1.21.4 is the floor:** in 1.21.4 Mojang refactored `PlayerEntityRenderer`
to a render-state-driven system. Older versions accessed the entity directly
during render and don't have the `updateRenderState(...)` hook this mod injects.

**Why two builds:** in 1.21.11 Mojang generified `PlayerEntityRenderer` and
introduced `PlayerLikeEntity`, which changed the mixin target's bytecode
descriptor. The `main` branch tracks the 1.21.11+ build; the `0.1.x` line is
maintained on the `legacy/1.21.4` branch for older patch versions.

## License

[MIT](LICENSE)
