package me.THEREALWWEFAN231.tunnelmc.javaconnection.packettranslators;

import me.THEREALWWEFAN231.tunnelmc.translator.PacketTranslator;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;

public class PlayerMoveC2SPacketTranslator extends PacketTranslator<PlayerMoveC2SPacket> {

	@Override
	public void translate(PlayerMoveC2SPacket packet) {
		/*PlayerMoveC2SPacket playerMoveC2SPacket = (PlayerMoveC2SPacket) packet;
		
		MovePlayerPacket movePlayerPacket = new MovePlayerPacket();
		long entityId = ((ClientPacketHandler)Client.instance.bedrockClient.getSession().getPacketHandler()).runtimeId;
		movePlayerPacket.setRuntimeEntityId(entityId);
		movePlayerPacket.setPosition(Vector3f.from(playerMoveC2SPacket.getX(Client.mc.player.getPos().x), playerMoveC2SPacket.getY(Client.mc.player.getPos().y), playerMoveC2SPacket.getZ(Client.mc.player.getPos().z)));
		movePlayerPacket.setRotation(Vector3f.from(playerMoveC2SPacket.getYaw(Client.mc.player.yaw), playerMoveC2SPacket.getYaw(Client.mc.player.pitch), 0));
		movePlayerPacket.setMode(MovePlayerPacket.Mode.HEAD_ROTATION);
		movePlayerPacket.setOnGround(playerMoveC2SPacket.isOnGround());
		Client.instance.sendPacket(Client.instance.bedrockClient.getSession(), movePlayerPacket);*/
	}

	@Override
	public Class<?> getPacketClass() {
		return PlayerMoveC2SPacket.class;
	}

}
