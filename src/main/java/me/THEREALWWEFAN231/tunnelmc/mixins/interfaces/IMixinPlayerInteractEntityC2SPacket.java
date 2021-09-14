package me.THEREALWWEFAN231.tunnelmc.mixins.interfaces;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import net.minecraft.network.packet.c2s.play.PlayerInteractEntityC2SPacket;

@Mixin(PlayerInteractEntityC2SPacket.class)
public interface IMixinPlayerInteractEntityC2SPacket {
	
	@Accessor("entityId")
	public int getId();
	
}
