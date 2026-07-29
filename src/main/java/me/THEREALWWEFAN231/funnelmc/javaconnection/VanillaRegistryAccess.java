package me.THEREALWWEFAN231.funnelmc.javaconnection;

import java.util.List;

import net.minecraft.core.HolderLookup;
import net.minecraft.core.LayeredRegistryAccess;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.component.DataComponentInitializers;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.RegistryDataLoader;
import net.minecraft.server.RegistryLayer;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.repository.ServerPacksSource;
import net.minecraft.server.packs.resources.CloseableResourceManager;
import net.minecraft.server.packs.resources.MultiPackResourceManager;
import net.minecraft.tags.TagLoader;

// There's no real Java server here to send the registry-sync packets (ClientboundRegistryDataPacket)
// a vanilla client would normally get before login, and ClientPacketListener's constructor and
// ClientLevel both hard-require dynamic registries like worldgen/biome and worldgen/dimension_type
// to actually be present (not just the static/built-in item/block/etc layer ClientRegistryLayer.STATIC
// provides) - with their tags genuinely bound, not just their elements.
//
// An earlier version of this built worldgen registries from VanillaRegistries.createLookup(), the
// same in-memory bootstrap the game's own datagen tooling uses to dump default registry JSON at build
// time. That bootstrap is deliberately incapable of real tag resolution (see
// RegistrySetBuilder.EmptyTagLookup#get(TagKey) - always returns HolderSet.emptyNamed(...), a
// placeholder whose contents() unconditionally throws "can't be dereferenced during construction").
// Any bootstrapped value that captures a tag reference internally (e.g. DimensionType.timelines(),
// used by ClientLevel's EnvironmentAttributeSystem on every login) carries that poisoned reference
// forever, so it crashes the instant real gameplay code dereferences it - regardless of anything done
// to the registry afterward, since the reference is baked into the value at bootstrap time.
//
// So this instead runs the same real JSON-registry-loading pipeline WorldLoader uses to build a
// server's (or a singleplayer world's) registries at startup - RegistryDataLoader for elements plus
// TagLoader for real tag bindings. It can't reuse Minecraft.getInstance().getResourceManager() for
// this - that manager is a MultiPackResourceManager permanently constructed with PackType.CLIENT_RESOURCES,
// so it only ever indexes each pack's assets/ paths and structurally can never see a data/minecraft/...
// entry, no matter what's on the classpath. Registries without RegistryValidator.nonEmpty() (biome,
// dimension_type, ...) silently come back empty through it instead of erroring, which is what let this
// go unnoticed - the variant registries (cat_variant, wolf_variant, ...) are the only ones with that
// validator, so they're the only ones that fail loudly with "Registry must be non-empty". The fix is
// the same one WorldLoader.PackConfig.createResourceManager() uses server-side: build a resource manager
// scoped to PackType.SERVER_DATA over the built-in vanilla pack instead, so data/minecraft/... is
// actually visible. The client jar already bundles that data since it needs it for singleplayer/LAN
// worlds anyway - ServerPacksSource.createVanillaPackSource() builds the same pack singleplayer uses.
public class VanillaRegistryAccess {

	private static RegistryAccess.Frozen instance;

	public static synchronized RegistryAccess.Frozen get() {
		if (instance == null) {
			instance = build();
		}
		return instance;
	}

	private static RegistryAccess.Frozen build() {
		try (CloseableResourceManager resourceManager = new MultiPackResourceManager(PackType.SERVER_DATA,
				List.of(ServerPacksSource.createVanillaPackSource()))) {
			LayeredRegistryAccess<RegistryLayer> layers = RegistryLayer.createRegistryAccess();
			List<Registry.PendingTags<?>> staticTags = TagLoader.loadTagsForExistingRegistries(resourceManager, layers.getLayer(RegistryLayer.STATIC));

			RegistryAccess.Frozen worldgenLoadContext = layers.getAccessForLoading(RegistryLayer.WORLDGEN);
			List<HolderLookup.RegistryLookup<?>> worldgenContextRegistries = TagLoader.buildUpdatedLookups(worldgenLoadContext, staticTags);

			RegistryAccess.Frozen worldgenRegistries = RegistryDataLoader
					.load(resourceManager, worldgenContextRegistries, RegistryDataLoader.WORLDGEN_REGISTRIES, Runnable::run)
					.join();

			// Same ordering WorldLoader.load uses: only commit the real static-layer tags (item/block/
			// entity_type/... tags, shared with the rest of the running client) once the worldgen load that
			// referenced their patched-but-not-yet-applied view has actually succeeded.
			staticTags.forEach(Registry.PendingTags::apply);

			RegistryAccess.Frozen compositeAccess = layers.replaceFrom(RegistryLayer.WORLDGEN, worldgenRegistries).compositeAccess();

			// Every registry element's default DataComponentMap (item max stack size, food, durability,
			// ...) starts unbound - Holder.Reference#components() throws "Components not bound yet"
			// until something calls bindComponents() on it. On a real connection that's the last step of
			// WorldLoader.load() (ReloadableServerResources.loadResources() ->
			// updateComponentsAndStaticRegistryTags()), which only runs when actually joining a world;
			// there's no world here for it to run against, so items were unusable the instant real
			// gameplay code (e.g. ItemStack's constructor) touched one.
			List<DataComponentInitializers.PendingComponents<?>> pendingComponents = BuiltInRegistries.DATA_COMPONENT_INITIALIZERS.build(compositeAccess);
			pendingComponents.forEach(DataComponentInitializers.PendingComponents::apply);

			return compositeAccess;
		}
	}

}
