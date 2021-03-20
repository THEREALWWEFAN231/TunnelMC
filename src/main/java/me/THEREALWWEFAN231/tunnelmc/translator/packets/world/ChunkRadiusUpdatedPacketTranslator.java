package me.THEREALWWEFAN231.tunnelmc.translator.packets.world;

import com.nukkitx.protocol.bedrock.packet.ChunkRadiusUpdatedPacket;
import com.nukkitx.protocol.bedrock.packet.SetLocalPlayerAsInitializedPacket;
import com.nukkitx.protocol.bedrock.packet.TickSyncPacket;

import me.THEREALWWEFAN231.tunnelmc.bedrockconnection.Client;
import me.THEREALWWEFAN231.tunnelmc.translator.PacketTranslator;
import me.THEREALWWEFAN231.tunnelmc.translator.packets.StartGameTranslator;

public class ChunkRadiusUpdatedPacketTranslator extends PacketTranslator<ChunkRadiusUpdatedPacket> {

	@Override
	public void translate(ChunkRadiusUpdatedPacket packet) {
		Client.instance.sendPacketImmediately(new TickSyncPacket());

		SetLocalPlayerAsInitializedPacket setLocalPlayerAsInitializedPacket = new SetLocalPlayerAsInitializedPacket();
		setLocalPlayerAsInitializedPacket.setRuntimeEntityId(StartGameTranslator.lastRunTimeId);
		Client.instance.sendPacketImmediately(setLocalPlayerAsInitializedPacket);
	}

	@Override
	public Class<?> getPacketClass() {
		return ChunkRadiusUpdatedPacket.class;
	}

}
