package me.THEREALWWEFAN231.tunnelmc.javaconnection.packettranslators.movement;

import com.nukkitx.math.vector.Vector3f;
import com.nukkitx.protocol.bedrock.packet.MovePlayerPacket;

import me.THEREALWWEFAN231.tunnelmc.TunnelMC;
import me.THEREALWWEFAN231.tunnelmc.bedrockconnection.Client;
import me.THEREALWWEFAN231.tunnelmc.translator.PacketTranslator;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket.LookOnly;

public class LookOnlyTranslator extends PacketTranslator<PlayerMoveC2SPacket.LookOnly> {

	@Override
	public void translate(LookOnly packet) {
		LookOnly lookOnly = (LookOnly) packet;

		int runtimeId = TunnelMC.mc.player.getEntityId();

		MovePlayerPacket movePlayerPacket = new MovePlayerPacket();
		movePlayerPacket.setRuntimeEntityId(runtimeId);
		movePlayerPacket.setPosition(Vector3f.from(lookOnly.getX(TunnelMC.mc.player.getPos().x), lookOnly.getY(TunnelMC.mc.player.getPos().y) + TunnelMC.mc.player.getStandingEyeHeight(), lookOnly.getZ(TunnelMC.mc.player.getPos().z)));
		movePlayerPacket.setRotation(Vector3f.from(lookOnly.getPitch(TunnelMC.mc.player.pitch), lookOnly.getYaw(TunnelMC.mc.player.yaw), 0));
		movePlayerPacket.setMode(MovePlayerPacket.Mode.HEAD_ROTATION);
		movePlayerPacket.setOnGround(lookOnly.isOnGround());
		Client.instance.sendPacket(movePlayerPacket);

	}

	@Override
	public Class<?> getPacketClass() {
		return PlayerMoveC2SPacket.LookOnly.class;
	}

}
