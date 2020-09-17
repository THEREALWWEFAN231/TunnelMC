package me.THEREALWWEFAN231.tunnelmc.javaconnection.packettranslators.movement;

import com.nukkitx.math.vector.Vector3f;
import com.nukkitx.protocol.bedrock.packet.MovePlayerPacket;

import me.THEREALWWEFAN231.tunnelmc.TunnelMC;
import me.THEREALWWEFAN231.tunnelmc.bedrockconnection.Client;
import me.THEREALWWEFAN231.tunnelmc.translator.PacketTranslator;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket.Both;

public class BothTranslator extends PacketTranslator<PlayerMoveC2SPacket.Both> {

	@Override
	public void translate(Both packet) {
		Both both = (Both) packet;

		int runtimeId = TunnelMC.mc.player.getEntityId();

		MovePlayerPacket movePlayerPacket = new MovePlayerPacket();
		movePlayerPacket.setRuntimeEntityId(runtimeId);
		movePlayerPacket.setPosition(Vector3f.from(both.getX(TunnelMC.mc.player.getPos().x), both.getY(TunnelMC.mc.player.getPos().y) + TunnelMC.mc.player.getStandingEyeHeight(), both.getZ(TunnelMC.mc.player.getPos().z)));
		movePlayerPacket.setRotation(Vector3f.from(both.getPitch(TunnelMC.mc.player.pitch), both.getYaw(TunnelMC.mc.player.yaw), 0));
		movePlayerPacket.setMode(MovePlayerPacket.Mode.NORMAL);
		movePlayerPacket.setOnGround(both.isOnGround());
		Client.instance.sendPacket(movePlayerPacket);
		
	}

	@Override
	public Class<?> getPacketClass() {
		return PlayerMoveC2SPacket.Both.class;
	}

}
