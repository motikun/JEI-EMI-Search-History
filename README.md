# JEI & EMI Search History

A client-side Minecraft mod by **motikun** that saves and displays search history for JEI and EMI.

## Features

- Focus the JEI or EMI search bar to see recent searches.
- Click an entry, or select it with the arrow keys and press Enter, to search again.
- Right-click an entry to delete it without changing the current search.
- Scroll through older entries and hover to read the full search text.
- Automatically save the final input when the search bar loses focus or the screen closes.
- Keep up to 100 searches across worlds, servers, and game restarts.
- Show the history above other tooltips, including with Modern UI installed.

Deleted searches stay removed until you edit or explicitly reuse them. JEI and EMI share the same history; either viewer can be used on its own.

## Compatibility

| Minecraft | Loader | Java | Supported recipe viewers |
| --- | --- | --- | --- |
| 1.20.1 | Forge 47.4.10 or newer, below 48 | 17 | JEI 15.56.0.205 / 15.62.0.219; EMI 1.1.24+1.20.1+forge |
| 1.21.1 | NeoForge 21.1.240 or newer, below 21.2 | 21 | JEI 19.21.2.313–19.64.0.453; EMI 1.1.24+1.21.1+neoforge |

Modern UI is optional. Compatibility was checked with Modern UI 3.12.0.1 on Forge and 3.13.0.1 on NeoForge.

## Installation

Place the JAR for your Minecraft version and loader in your client's `mods` folder, alongside JEI, EMI, or both. **No server installation is needed.** Without either viewer, no search history panel is shown.

History is stored in `config/jeihistory/history.json`. The internal mod ID remains `jeihistory`.

## Building

Run the Gradle wrapper with Java 21. The build uses a Java 17 toolchain for Forge and Java 21 for NeoForge; missing toolchains can be downloaded automatically.

```sh
./gradlew build
```

Version 1.0.0 produces:

- `build/libs/jeihistory-1.0.0-mc1.20.1-forge.jar`
- `build/libs/jeihistory-1.0.0-mc1.21.1-neoforge.jar`

To launch a development client:

```sh
./gradlew runClient                  # NeoForge
./gradlew :forge:runForgeClient      # Forge
```

Use `-PwithEmi` to add EMI, or `-PemiOnly` to run with EMI alone.

## License

Copyright (c) 2026 motikun. Licensed under the [MIT License](LICENSE).
