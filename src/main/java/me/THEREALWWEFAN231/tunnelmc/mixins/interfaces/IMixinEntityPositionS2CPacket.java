package me.THEREALWWEFAN231.tunnelmc.mixins.interfaces;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import net.minecraft.network.packet.s2c.play.EntityPositionS2CPacket;

@Mixin(EntityPositionS2CPacket.class)
public interface IMixinEntityPositionS2CPacket {

	@Accessor("id")
	public void setId(int newValue);

	@Accessor("x")
	public void setX(double newValue);

	@Accessor("y")
	public void setY(double newValue);

	@Accessor("z")
	public void setZ(double newValue);

	@Accessor("yaw")
	public void setYaw(byte newValue);

	@Accessor("pitch")
	public void setPitch(byte newValue);

	@Accessor("onGround")
	public void setOnGround(boolean newValue);

}
