package me.THEREALWWEFAN231.funnelmc.translator.packet.entity;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.UUID;

import com.mojang.authlib.GameProfile;
import org.cloudburstmc.protocol.bedrock.packet.PlayerListPacket;

import me.THEREALWWEFAN231.funnelmc.bedrockconnection.Client;
import me.THEREALWWEFAN231.funnelmc.translator.PacketTranslator;
import net.minecraft.network.protocol.game.ClientboundPlayerInfoRemovePacket;
import net.minecraft.network.protocol.game.ClientboundPlayerInfoUpdatePacket;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.GameType;

public class PlayerListPacketTranslator extends PacketTranslator<PlayerListPacket> {

	// None of ClientboundPlayerInfoUpdatePacket's real constructors take a plain List<Entry> - they
	// all build entries from real ServerPlayer instances, which we don't have. A Mixin @Accessor
	// setter for the (final) entries field used to fill this gap, but Mixin didn't strip the field's
	// finality here, so writing through it threw IllegalAccessError at runtime ("Update to
	// non-static final field ... attempted from a different method (setEntries) than the
	// initializer method <init>"). Plain reflection works fine for a per-instance final field like
	// this one, unlike a static final, so that's what's used instead.
	private static final Field ENTRIES_FIELD;
	static {
		try {
			ENTRIES_FIELD = ClientboundPlayerInfoUpdatePacket.class.getDeclaredField("entries");
			ENTRIES_FIELD.setAccessible(true);
		} catch (NoSuchFieldException e) {
			throw new ExceptionInInitializerError(e);
		}
	}

	@Override
	public void translate(PlayerListPacket packet) {
		boolean add = packet.getAction() == PlayerListPacket.Action.ADD;

		if (!add) {
			List<UUID> removedProfileIds = new ArrayList<>();
			for (PlayerListPacket.Entry entry : packet.getEntries()) {
				removedProfileIds.add(entry.getUuid());
			}

			ClientboundPlayerInfoRemovePacket removePacket = new ClientboundPlayerInfoRemovePacket(removedProfileIds);
			Client.instance.javaConnection.processServerToClientPacket(removePacket);
			return;
		}

		List<ClientboundPlayerInfoUpdatePacket.Entry> entries = new ArrayList<>();
		for (PlayerListPacket.Entry entry : packet.getEntries()) {
			// gamemode says nullable but is used in ClientGameSession/:
			entries.add(new ClientboundPlayerInfoUpdatePacket.Entry(entry.getUuid(), new GameProfile(entry.getUuid(), entry.getName()),
					true, 0, GameType.SURVIVAL, Component.literal(entry.getName()), true, 0, null));
		}

		ClientboundPlayerInfoUpdatePacket playerListS2CPacket = new ClientboundPlayerInfoUpdatePacket(
				EnumSet.of(ClientboundPlayerInfoUpdatePacket.Action.ADD_PLAYER), Collections.emptyList());

		try {
			ENTRIES_FIELD.set(playerListS2CPacket, entries);
		} catch (IllegalAccessException e) {
			throw new RuntimeException(e);
		}

		Client.instance.javaConnection.processServerToClientPacket(playerListS2CPacket);
	}

	@Override
	public Class<?> getPacketClass() {
		return PlayerListPacket.class;
	}

	public static class SkinType {//this is only temporary

		public Identifier texture;
		public boolean slim;

		public SkinType(Identifier texture, boolean slim) {
			this.texture = texture;
			this.slim = slim;
		}

	}

}
