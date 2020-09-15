package me.THEREALWWEFAN231.tunnelmc;

import me.THEREALWWEFAN231.tunnelmc.translator.PacketTranslatorManager;
import me.THEREALWWEFAN231.tunnelmc.translator.blockstate.BlockStateTranslator;
import me.THEREALWWEFAN231.tunnelmc.utils.FileManagement;
import net.minecraft.client.MinecraftClient;

public class TunnelMC {

	public static TunnelMC instance = new TunnelMC();
	public static MinecraftClient mc = MinecraftClient.getInstance();

	public FileManagement fileManagement;
	public PacketTranslatorManager packetTranslatorManager;

	public void initialize() {
		this.fileManagement = new FileManagement();
		this.packetTranslatorManager = new PacketTranslatorManager();

		BlockStateTranslator.load();
	}

}
