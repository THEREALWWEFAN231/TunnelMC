package me.THEREALWWEFAN231.tunnelmc.translator.packets;

import java.awt.Color;
import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.HashMap;

import com.mojang.authlib.GameProfile;
import com.nukkitx.protocol.bedrock.data.skin.ImageData;
import com.nukkitx.protocol.bedrock.packet.PlayerListPacket;

import me.THEREALWWEFAN231.tunnelmc.TunnelMC;
import me.THEREALWWEFAN231.tunnelmc.bedrockconnection.Client;
import me.THEREALWWEFAN231.tunnelmc.mixins.interfaces.IMixinPlayerListS2CPacket;
import me.THEREALWWEFAN231.tunnelmc.translator.PacketTranslator;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.NativeImageBackedTexture;
import net.minecraft.network.packet.s2c.play.PlayerListS2CPacket;
import net.minecraft.network.packet.s2c.play.PlayerListS2CPacket.Entry;
import net.minecraft.text.LiteralText;
import net.minecraft.util.Identifier;

public class PlayerListPacketTranslator extends PacketTranslator<PlayerListPacket> {

	public static HashMap<String, SkinType> skins = new HashMap<String, SkinType>();//this is only temporary

	@Override
	public void translate(PlayerListPacket packet) {
		boolean add = packet.getAction() == PlayerListPacket.Action.ADD;
		ArrayList<PlayerListS2CPacket.Entry> entries = new ArrayList<Entry>();

		PlayerListS2CPacket playerListS2CPacket = new PlayerListS2CPacket();
		((IMixinPlayerListS2CPacket) playerListS2CPacket).setAction(add ? PlayerListS2CPacket.Action.ADD_PLAYER : PlayerListS2CPacket.Action.REMOVE_PLAYER);
		((IMixinPlayerListS2CPacket) playerListS2CPacket).setEntries(entries);

		for (PlayerListPacket.Entry entry : packet.getEntries()) {
			if (add) {
				//im sure this could be improved and this generally shouldn't be the way we do this
				try {
					ImageData skinData = entry.getSkin().getSkinData();
					NativeImage nativeImage = new NativeImage(skinData.getWidth(), skinData.getHeight(), false/*not really sure*/);
					ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(skinData.getImage());
					for (int y = 0; y < nativeImage.getHeight(); y++) {
						for (int x = 0; x < nativeImage.getWidth(); x++) {
							int red = byteArrayInputStream.read();
							int green = byteArrayInputStream.read();
							int blue = byteArrayInputStream.read();
							int alpha = byteArrayInputStream.read();
							nativeImage.setPixelColor(x, y, new Color(blue, green, red, alpha).getRGB());//okkkkk.... i guess native image is ABGR :shrug:
						}
					}

					Identifier skinTexture = TunnelMC.mc.getTextureManager().registerDynamicTexture((entry.getName().toLowerCase() + "skin").replaceAll(" ", ""), new NativeImageBackedTexture(nativeImage));
					boolean slim = entry.getSkin().getSkinId().contains(".CustomSlim");//so there is not really a relevent way to detect if a store skin is slim or not, without somehow getting all the store skins, it would be cool, if your reading this mojang, if a slim boolean was sent somewhere maybe ArmSize :shrug:?!?!?
					
					PlayerListPacketTranslator.skins.put(entry.getName(), new SkinType(skinTexture, slim));

				} catch (Exception e) {
					e.printStackTrace();
				}
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
