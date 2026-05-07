# Changelog

## 0.1.0 — 2026-05-07
- Initial release: floating-nametag rank prefix for MCDuels players, with ModMenu config and toggle keybind.
- Supports 9 kits: Axe, Crystal PvP, Mace, NethOp, Pot, SMP, Spear Mace, Sword, UHC.
- Two-tier cache (in-memory + on-disk) keyed by UUID — survives restarts, uses ≤1 API call per player per hour.
- HT/LT scheme (HT1 = `high_dueler` … LT5 = `iron`) with the rank-color palette from the design spec.
- Async, non-blocking lookup against `https://mcduels.com/public/player/{id}` — render path never blocks on I/O.
