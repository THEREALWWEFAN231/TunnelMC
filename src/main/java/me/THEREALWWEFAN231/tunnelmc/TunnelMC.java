package me.THEREALWWEFAN231.tunnelmc;

import me.THEREALWWEFAN231.tunnelmc.translator.EntityTranslator;
import me.THEREALWWEFAN231.tunnelmc.translator.PacketTranslatorManager;
import me.THEREALWWEFAN231.tunnelmc.translator.blockentity.BlockEntityRegistry;
import me.THEREALWWEFAN231.tunnelmc.translator.blockstate.BlockStateTranslator;
import me.THEREALWWEFAN231.tunnelmc.translator.container.screenhandler.ScreenHandlerTranslatorManager;
import me.THEREALWWEFAN231.tunnelmc.translator.enchantment.EnchantmentTranslator;
import me.THEREALWWEFAN231.tunnelmc.translator.item.ItemTranslator;
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

		BlockEntityRegistry.load();
		BlockStateTranslator.load();
		EntityTranslator.load();
		ItemTranslator.load();
		EnchantmentTranslator.load();
		ScreenHandlerTranslatorManager.load();
	}

}
