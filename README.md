# FunnelMC

FunnelMC is a [Fabric](https://fabricmc.net/) mod that lets Minecraft **Java Edition** players
connect to Minecraft **Bedrock Edition** servers and worlds. It opens a Bedrock protocol
connection and translates packets in both directions so the two editions can talk to each other,
without needing a separate proxy process.

## Status

This project is currently being modernized from its original 1.16.5-era codebase up to
**Minecraft 26.2** (Fabric Loom 1.17, Java 25) against the latest
[CloudburstMC Protocol](https://github.com/CloudburstMC/Protocol) library.

- ✅ Builds against MC 26.2 / Loom 1.17 / Java 25 — `./gradlew compileJava` is clean.
- ✅ Bedrock protocol dependency updated to `org.cloudburstmc.protocol` (the library GeyserMC
  itself builds against), replacing the old `com.nukkitx.protocol:bedrock-v431`.
- 🚧 Runtime correctness against the new protocol/API surface is still being verified — several
  translators carry `TODO`s where modern Minecraft's registry-driven systems (dimensions,
  enchantments, block entities, world height) need real backing data that hasn't been wired up
  yet. Nothing is silently faked; gaps are flagged in place in the code.
- 🚧 End-to-end connection testing (actually joining a Bedrock server/world) is in progress.

Previously working features from before the rewrite (chunk translation, block translation via
[Geyser's mappings](https://github.com/GeyserMC/mappings), player spawning, skins, chat, swing
animation, offline-server authentication) are being re-verified as part of the modernization —
see the status above for where things currently stand.

## How does it work

FunnelMC is a Fabric mod, not a standalone proxy. When you connect, it opens a Bedrock client
connection to the target server and translates incoming/outgoing packets so both the Bedrock
server and your (unmodified, vanilla-protocol) Java client can understand each other.

## Why a mod and not a proxy

Being a mod instead of an external proxy lets us do things a proxy can't easily do — for example,
reading skins directly from the Bedrock server instead of from [minecraft.net](https://minecraft.net/).
It also leaves room to support Bedrock-only features (emotes, etc.) that don't otherwise exist on
Java Edition, though we're not promising those anytime soon.

## Building

Standard Fabric mod setup:

```
./gradlew build
```

Requires JDK 25. For IDE setup, see the [Fabric Wiki](https://fabricmc.net/wiki/tutorial:setup).

## Contributing

Contributions are welcome, especially around:
- Verifying/fixing packet translators against the current CloudburstMC Protocol version
- Wiring up the registry-backed data (dimensions, enchantments, biomes, block entities) that the
  MC 26.2 rewrite still stubs out with `TODO`s
- Xbox Live authentication / joining worlds from invites 😎

Please try to match the existing code style (explicit braces, no single-line `if`s).

## Credits

This project wouldn't be possible without these open source projects — whether we referenced
their code to understand the protocol, or borrowed pieces outright:
- [Protocol](https://github.com/CloudburstMC/Protocol)
- [Geyser](https://github.com/GeyserMC/Geyser)
- [Nukkit](https://github.com/CloudburstMC/Nukkit)
- [gophertunnel](https://github.com/Sandertv/gophertunnel)

## Can I try it

Not yet — the modernization to MC 26.2 is still in progress and the connection pipeline hasn't
been verified end-to-end. Check the Status section above.

## [Discord](https://discord.gg/qH6GqxW)
We might post screenshots or updates about FunnelMC there, or if you'd like to help out, feel
free to join.

## Pictures
This is a picture of the Java Edition on a Bedrock Edition server
![](/pictures/JavaEdition.png)
This is a picture of what it looks like on the Bedrock Edition
![](/pictures/Windows10Edition.png)
