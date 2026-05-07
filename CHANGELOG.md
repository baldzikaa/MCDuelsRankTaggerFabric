# Changelog

## 0.2.0 — 2026-05-07
- **Minecraft 1.21.11 support.** Ported the mixin to the new generic `PlayerEntityRenderer` (target signature `updateRenderState(PlayerLikeEntity, PlayerEntityRenderState, F)V`); added an `instanceof PlayerEntity` guard so mannequins / display avatars are skipped cleanly.
- Updated for 1.21.11 yarn API renames: `GameProfile.name()` / `id()` (now a record), `KeyBinding.Category.MISC` (categories became records), `Style.withFont(StyleSpriteSource.Font)` (fonts wrapped in a sprite source).
- Build toolchain bumped: Gradle wrapper 8.10 → 8.14, Fabric Loom 1.8 → 1.13, fabric-api → 0.141.3+1.21.11, cloth-config → 21.11.153.
- Adopted MCDuels rank labels (`HD`, `D`, `HDi`, `Di`, `HG`, `G`, `HS`, `S`, `HI`, `I`) with refreshed palette: dueler reds, diamond cyan, gold, silver greys, warm-iron browns. Player nametag color is preserved — only the rank label is tinted.
- Code-review fixes: `McDuelsClient` is now `AutoCloseable`, distinguishes 4xx (permanent) from 5xx (transient); `PlayerRankCache.putFailed` uses atomic `compute`; cache snapshot is immutable; shutdown save runs off the render thread; keybind toggle no longer blocks on disk I/O; logging uses SLF4J consistently.
- Renamed `Rank` enum identifiers (`HT1`–`LT5` → `HD`/`D`/`HDI`/`DI`/`HG`/`G`/`HS`/`S`/`HI`/`I`) for clarity.

## 0.1.0 — 2026-05-07
- Initial release: floating-nametag rank prefix for MCDuels players, with ModMenu config and toggle keybind.
- Supports 9 kits: Axe, Crystal PvP, Mace, NethOp, Pot, SMP, Spear Mace, Sword, UHC.
- Two-tier cache (in-memory + on-disk) keyed by UUID — survives restarts, uses ≤1 API call per player per hour.
- HT/LT scheme (HT1 = `high_dueler` … LT5 = `iron`) with the rank-color palette from the design spec.
- Async, non-blocking lookup against `https://mcduels.com/public/player/{id}` — render path never blocks on I/O.
