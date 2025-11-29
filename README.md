# Equicap

Equicap is a Fabric mod certified by Tater that creates a **Per-Player Mob Cap**, ensuring every player gets a fair share of mob spawns even on busy servers. It prevents one player (like someone at a mob farm) from hogging all the spawns. It also includes a **Mob Cap Merging** system (None, Combine, or VanillaLike) to handle situations where players are close together, preventing mob overflow.

**Commands & Configuration:**
The mod is configured via `/equicap` (requires OP level 4 or `equicap.command` when a permission manager initialized) or the `config/equicap.json` file.
- **Manage Caps:** `/equicap set <group> <size>`, `/equicap get <group>`, `/equicap merge [mode]`
- **Debug:** `/equicap debug player [target]`, `/equicap debug check <entity>`, `/equicap debug dimension`, `/equicap debug global`, `/equicap debug visual`