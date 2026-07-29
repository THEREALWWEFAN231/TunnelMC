package me.THEREALWWEFAN231.funnelmc.mixins;

import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import io.netty.channel.Channel;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import me.THEREALWWEFAN231.funnelmc.bedrockconnection.Client;
import net.minecraft.network.Connection;
import net.minecraft.network.DisconnectionDetails;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundLevelParticlesPacket;
import net.minecraft.network.protocol.common.ClientboundPingPacket;
import net.minecraft.network.protocol.status.ClientboundStatusResponsePacket;

@Mixin(Connection.class)
public class MixinClientConnection {

	@Shadow
	private Channel channel;

	@Shadow private DisconnectionDetails disconnectionDetails;

	@Inject(method = "isConnected", at = @At("HEAD"), cancellable = true)
	public void isConnected(CallbackInfoReturnable<Boolean> callbackInfoReturnable) {
		if (Client.instance.isConnectionOpen()) {
			callbackInfoReturnable.setReturnValue(true);
		}
	}

	// TODO: Connection no longer has an isEncrypted() method to hook (it was removed/reworked in
	// modern MC), so the "fake encryption" trick that used to make skins show up in the player
	// list HUD no longer applies here. Skin visibility needs to be re-checked against whatever
	// mechanism replaced it (likely resolved via PlayerInfo/GameProfile properties now, not a
	// connection-level encrypted flag).

	@Inject(method = "send(Lnet/minecraft/network/protocol/Packet;Lio/netty/channel/ChannelFutureListener;Z)V", at = @At("HEAD"), cancellable = true)
	private void send(Packet<?> packet, ChannelFutureListener callback, boolean flush, CallbackInfo callbackInfo) {
		if (Client.instance.isConnectionOpen()) {
			Client.instance.javaConnection.packetTranslatorManager.translatePacket(packet);
			callbackInfo.cancel();
		}
	}

	@Inject(method = "channelRead0", at = @At("HEAD"), cancellable = true)
	public void channelRead0(ChannelHandlerContext channelHandlerContext, Packet<?> packet, CallbackInfo callback) {
		if (this.channel.isOpen()) {

			if (packet instanceof ClientboundLevelParticlesPacket || packet instanceof ClientboundStatusResponsePacket || packet instanceof ClientboundPingPacket) {
				return;
			}

			System.out.println("got packet " + packet.getClass());
		}
	}

	@Inject(method = "disconnect(Lnet/minecraft/network/chat/Component;)V", at = @At("HEAD"), cancellable = true)
	public void disconnect(Component disconnectReason, CallbackInfo ci) {
		if (Client.instance.isConnectionOpen()) {
			// this.channel is null here
			Client.instance.bedrockSession.disconnect();
			this.disconnectionDetails = new DisconnectionDetails(disconnectReason);
			ci.cancel();
		}
	}

	// Our isConnected() override above always reports "connected" while the bedrock session is
	// open, even though this.channel is really null (there's no backing Netty channel for this
	// fake connection). That's fine for the disconnect(Component) overload above since we cancel
	// it outright, but disconnect(Component) itself just delegates to this DisconnectionDetails
	// overload internally, and other vanilla code (e.g. ClientCommonPacketListenerImpl.onPacketError)
	// calls this overload directly - so it needs the same guard, otherwise vanilla's own
	// "if (this.isConnected()) this.channel.close()" sees our forced-true isConnected() and NPEs
	// closing a channel that was never there.
	@Inject(method = "disconnect(Lnet/minecraft/network/DisconnectionDetails;)V", at = @At("HEAD"), cancellable = true)
	public void disconnect(DisconnectionDetails details, CallbackInfo ci) {
		if (Client.instance.isConnectionOpen()) {
			Client.instance.bedrockSession.disconnect();
			this.disconnectionDetails = details;
			ci.cancel();
		}
	}

}
