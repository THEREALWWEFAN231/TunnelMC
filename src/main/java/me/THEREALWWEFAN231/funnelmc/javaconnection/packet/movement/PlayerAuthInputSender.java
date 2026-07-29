package me.THEREALWWEFAN231.funnelmc.javaconnection.packet.movement;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.cloudburstmc.math.vector.Vector2f;
import org.cloudburstmc.math.vector.Vector3f;
import org.cloudburstmc.protocol.bedrock.data.AuthoritativeMovementMode;
import org.cloudburstmc.protocol.bedrock.data.ClientPlayMode;
import org.cloudburstmc.protocol.bedrock.data.InputInteractionModel;
import org.cloudburstmc.protocol.bedrock.data.InputMode;
import org.cloudburstmc.protocol.bedrock.data.PlayerAuthInputData;
import org.cloudburstmc.protocol.bedrock.packet.PlayerAuthInputPacket;

import com.darkmagician6.eventapi.EventManager;
import com.darkmagician6.eventapi.EventTarget;

import me.THEREALWWEFAN231.funnelmc.FunnelMC;
import me.THEREALWWEFAN231.funnelmc.bedrockconnection.Client;
import me.THEREALWWEFAN231.funnelmc.events.EventPlayerTick;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.player.Input;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

import java.util.Set;

// Servers running server-authoritative movement (most current ones, including the vanilla Bedrock
// Dedicated Server by default) expect PlayerAuthInputPacket every tick instead of the legacy
// client-authoritative MovePlayerPacket (which they reject outright - see the movementMode guard in
// PlayerMoveTranslator). Real server-authoritative movement simulates the player from the analog
// move vector and input flags below, not from the reported position/delta - those are only used for
// reconciliation - so without them the server thinks we never pressed a key and keeps correcting us
// back to a stationary position every tick.
public class PlayerAuthInputSender {

	private static final Logger logger = LogManager.getLogger(PlayerAuthInputSender.class);

	private long tick;
	private Vector3f lastSentPosition;

	public PlayerAuthInputSender() {
		EventManager.register(this);
	}

	@EventTarget
	public void onEvent(EventPlayerTick event) {
		if (Client.instance.movementMode == AuthoritativeMovementMode.CLIENT) {
			return;
		}

		if (!Client.instance.isConnectionOpen() || FunnelMC.mc.player == null) {
			return;
		}

		Vec3 lookAngle = FunnelMC.mc.player.getLookAngle();

		Vector3f position = Vector3f.from(FunnelMC.mc.player.getX(), FunnelMC.mc.player.getY() + FunnelMC.mc.player.getEyeHeight(Pose.STANDING), FunnelMC.mc.player.getZ());

		// Server-authoritative movement won't actually move the player server-side off of position
		// alone - it needs a non-zero delta each tick to accept the move, or it just keeps echoing
		// back the last confirmed position (which MovePlayerPacketTranslator then snaps us back to).
		Vector3f delta = this.lastSentPosition == null ? Vector3f.ZERO : position.sub(this.lastSentPosition);
		this.lastSentPosition = position;

		// getLastSentInput() only updates inside LocalPlayer.sendPosition(), which is gated behind
		// its own network-send throttling/camera-control checks and lagged behind reality. The live
		// per-tick key state used to actually drive local movement (aiStep/travel) lives on
		// mc.player.input.keyPresses instead - read that so held keys show up immediately.
		Input input = FunnelMC.mc.player.input.keyPresses;
		float forwardValue = (input.forward() ? 1f : 0f) - (input.backward() ? 1f : 0f);
		float strafeValue = (input.right() ? 1f : 0f) - (input.left() ? 1f : 0f);
		Vector2f moveVector = Vector2f.from(strafeValue, forwardValue);

		PlayerAuthInputPacket packet = new PlayerAuthInputPacket();
		packet.setPosition(position);
		packet.setRotation(Vector3f.from(FunnelMC.mc.player.getXRot(), FunnelMC.mc.player.getYRot(), FunnelMC.mc.player.getYRot()));
		packet.setMotion(Vector2f.from(delta.getX(), delta.getZ()));
		packet.setInputMode(InputMode.MOUSE);
		packet.setPlayMode(ClientPlayMode.NORMAL);
		packet.setInputInteractionModel(InputInteractionModel.CLASSIC);
		packet.setInteractRotation(Vector2f.ZERO);
		packet.setTick(this.tick++);
		packet.setDelta(delta);
		packet.setAnalogMoveVector(moveVector);
		packet.setCameraOrientation(Vector3f.from((float) lookAngle.x, (float) lookAngle.y, (float) lookAngle.z));
		packet.setRawMoveVector(moveVector);

		Set<PlayerAuthInputData> inputData = packet.getInputData();
		if (input.forward()) inputData.add(PlayerAuthInputData.UP);
		if (input.backward()) inputData.add(PlayerAuthInputData.DOWN);
		if (input.left()) inputData.add(PlayerAuthInputData.LEFT);
		if (input.right()) inputData.add(PlayerAuthInputData.RIGHT);
		if (input.jump()) inputData.add(PlayerAuthInputData.JUMPING);
		if (input.shift()) {
			inputData.add(PlayerAuthInputData.SNEAKING);
			inputData.add(PlayerAuthInputData.SNEAK_DOWN);
		}
		if (input.sprint()) inputData.add(PlayerAuthInputData.SPRINTING);

		Client.instance.sendPacket(packet);

		if (this.tick % 20 == 0) {
			BlockPos below = FunnelMC.mc.player.blockPosition().below();
			BlockState belowState = FunnelMC.mc.level != null ? FunnelMC.mc.level.getBlockState(below) : null;
			logger.warn("[MovementDiag] sent tick={} pos={} moveVector={} inputData={} onGround={} blockBelow={}={}",
					this.tick, position, moveVector, inputData, FunnelMC.mc.player.onGround(), below, belowState);
		}
	}

}
