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

Mojang refactored `PlayerEntityRenderer` between 1.21.8 and 1.21.11
(generified, swapped `AbstractClientPlayerEntity` for `PlayerLikeEntity`,
changed the `Style.withFont` parameter, etc.), so a single jar cannot bind
on both ranges. This repo therefore ships **two parallel branches**:

| Minecraft        | Branch / tag                     | Jar version | Status                              |
|------------------|----------------------------------|-------------|-------------------------------------|
| 1.21.4 – 1.21.8  | [`legacy/1.21.4`](../../tree/legacy/1.21.4) / [`v0.1.1`](../../releases/tag/v0.1.1) | `0.1.1`     | ✅ Tested                           |
| 1.21.9, 1.21.10  | either branch                    | —           | ⚠️ Untested — try the closer build |
| 1.21.11          | [`main`](../../tree/main) / [`v0.2.0`](../../releases/tag/v0.2.0) | `0.2.0`     | ✅ Tested                           |
| 1.21.12+         | `main`                           | `0.2.x`     | ✅ Likely works (please report)     |
| ≤ 1.21.3         | —                                | —           | ❌ Not supported (no `EntityRenderState`) |
| 1.20.x           | —                                | —           | Planned                             |

**Picking the right branch when building from source:**

```sh
# For Minecraft 1.21.11+
git checkout main
./gradlew build

# For Minecraft 1.21.4 – 1.21.8
git checkout legacy/1.21.4
./gradlew build
```

**Why 1.21.4 is the floor:** Mojang introduced the `EntityRenderState`
snapshot system in 1.21.4. Older versions don't have the `updateRenderState`
hook this mod injects, so they cannot be supported without a separate mixin.

## License

[MIT](LICENSE)
