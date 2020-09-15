# TunnelMC

TunnelMC allows Minecraft Java Edition Players to join and play Minecraft Bedrock Edition servers

# How does it work
Firstly TunnelMC is a [Fabric Mod](https://fabricmc.net/). What we do is we open a connection to a Minecraft Bedrock server and translate any incoming and outgoing packets, so they can be read by both Editions.

# Why a Fabric Mod and not a Proxy
Well we love fabric ❤️, also making it a mod instead of a proxy allows us to do some stuff we normally could not do. Such as skins, we read the skins from the bedrock server instead of [Minecraft.net](https://minecraft.net/) this would not be possible without some sort of mod. Also *technically* we could add emotes and other stuff Minecraft Java Edition does not have. Granted we probably wont add emotes but we *could*.

# What is left to add
Yeah well, thats not the correct question to ask, we just recently started development and the real question is what have we added.
- Offline server authentication(does not work with bedrock dedicated server, but does with nukkit)
- Basic chunk translation
- Block translation(thanks to [Geysers' mappings](https://github.com/GeyserMC/mappings), still needs a little work, but its generally there
- Spawning of players
- Skins(generally working, layers seem to not work)
- Chat
- Swinging animation

# Contributing
I'd like to help or try to help, where do I start? Setting up the project is just like any other [Fabric Mod](https://fabricmc.net/) for eclipse you need to run the gradlew genSources command, then gradlew eclipse, then import it as an existing project into eclipse, if your using another IDE please look at the [Fabric Wiki](https://fabricmc.net/wiki/tutorial:setup).

Also it would be appreciated if you coded in this style
```java
if(x) {
  doSomething();
}
x.forEach(new Consumer<X>() {

  @Override
  public void accept(X x) {
    doSomething();
  }
});
```
rather then
```java
if(x)
  doSomething();
x.forEach((x) -> {
  doSomething();
});
```
Also if you have any knowledge on xbox live/the api it would be cool if you added xbox live authentication and or joining worlds from invites.😎

# Credits
This generally would not be possible without some open source projects, wheather its just looking how thing works inorder to reverse translate them, looking at their code to see how thing work, and or copying a little bit of their code. We apperiate all these projects.
- [Protocol](https://github.com/CloudburstMC/Protocol)
- [Nukkit](https://github.com/CloudburstMC/Nukkit)
- [Geyser](https://github.com/GeyserMC/Geyser)
- [gophertunnel](https://github.com/Sandertv/gophertunnel)

# How can I try it
You currently can not, we are still in development and a lot has not been added yet.

# [Discord](https://discord.gg/qH6GqxW)
We might post some screen shots or information about TunnelMC in there, or if you'd like to help out, you can join and we can see whats crackin.
