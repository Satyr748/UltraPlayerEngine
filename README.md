# UltraPlayerEngine

A small, server-side Fabric 1.21.11 mod that measures rolling server tick
performance and reports player count, estimated TPS, average MSPT, P95 MSPT,
and JVM memory use.

## Important scope note

This first version is a **measurement/baseline build**, not a VexBot AI
optimizer. It does not patch VexBot, skip bot ticks, change combat behavior,
or promise a 100-bot capacity. Those changes require profiling a real server
and a supported integration point from the VexBot developers. This standalone
mod is designed to collect useful measurements first.

## Requirements

- Minecraft Java Edition 1.21.11
- Fabric server
- Fabric Loader 0.19.3 or newer
- Fabric API 0.141.6+1.21.11 or newer
- Java 21

## Build locally

Install a Java 21 JDK and Gradle 9.2.1, then run:

### Windows
```bat
gradle --no-daemon clean build
```

### Linux
```bash
gradle --no-daemon clean build
```

The remapped mod JAR will be in `build/libs/`.

## Commands

Requires operator permission level 2:

- `/upe status` — shows online player count, rolling average MSPT, estimated TPS,
  P95 MSPT, JVM memory usage, and sample count.
- `/upe reset` — clears the rolling measurement window.

The player count is the server's player-list count. Whether a particular
fake-player implementation appears in that count depends on how that mod
registers its bots.

## Build the JAR on GitHub

1. Create a new empty GitHub repository.
2. Upload the contents of this project (the `.github` folder too).
3. Open the repository's **Actions** tab.
4. The workflow named **Build UltraPlayerEngine** runs on push, pull request,
   or manually via **Run workflow**.
5. Open the successful workflow run and download the artifact
   `UltraPlayerEngine-Fabric-1.21.11`.
6. Put the built `.jar` into the Fabric server's `mods` folder.

## Next development step

Run a controlled test with VexBot and record `/upe status` at several bot
counts. Use the results to identify whether the bottleneck is tick time,
entity/pathfinding work, chunk loading, or networking before adding targeted
optimizations.
