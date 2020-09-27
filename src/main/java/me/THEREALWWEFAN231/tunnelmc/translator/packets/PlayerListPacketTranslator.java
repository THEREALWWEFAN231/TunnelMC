package me.THEREALWWEFAN231.tunnelmc.translator.packets;

import java.util.ArrayList;

import com.mojang.authlib.GameProfile;
import com.nukkitx.protocol.bedrock.packet.PlayerListPacket;

import me.THEREALWWEFAN231.tunnelmc.bedrockconnection.Client;
import me.THEREALWWEFAN231.tunnelmc.mixins.interfaces.IMixinPlayerListS2CPacket;
import me.THEREALWWEFAN231.tunnelmc.translator.PacketTranslator;
import net.minecraft.network.packet.s2c.play.PlayerListS2CPacket;
import net.minecraft.network.packet.s2c.play.PlayerListS2CPacket.Entry;
import net.minecraft.text.LiteralText;
import net.minecraft.util.Identifier;

public class PlayerListPacketTranslator extends PacketTranslator<PlayerListPacket> {

	@Override
	public void translate(PlayerListPacket packet) {
		boolean add = packet.getAction() == PlayerListPacket.Action.ADD;
		ArrayList<PlayerListS2CPacket.Entry> entries = new ArrayList<Entry>();

		PlayerListS2CPacket playerListS2CPacket = new PlayerListS2CPacket();
		((IMixinPlayerListS2CPacket) playerListS2CPacket).setAction(add ? PlayerListS2CPacket.Action.ADD_PLAYER : PlayerListS2CPacket.Action.REMOVE_PLAYER);
		((IMixinPlayerListS2CPacket) playerListS2CPacket).setEntries(entries);

		for (PlayerListPacket.Entry entry : packet.getEntries()) {
			if (add) {
				
			}

			entries.add(playerListS2CPacket.new Entry(new GameProfile(entry.getUuid(), entry.getName()), 0, null, new LiteralText(entry.getName())));
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
