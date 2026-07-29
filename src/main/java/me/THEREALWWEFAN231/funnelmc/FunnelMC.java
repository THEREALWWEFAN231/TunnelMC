package me.THEREALWWEFAN231.funnelmc;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import me.THEREALWWEFAN231.funnelmc.debug.LoadTrackerDiagnostics;
import me.THEREALWWEFAN231.funnelmc.translator.EntityTranslator;
import me.THEREALWWEFAN231.funnelmc.translator.PacketTranslatorManager;
import me.THEREALWWEFAN231.funnelmc.translator.blockentity.BlockEntityRegistry;
import me.THEREALWWEFAN231.funnelmc.translator.blockentity.BlockEntityTranslator;
import me.THEREALWWEFAN231.funnelmc.translator.blockstate.BlockStateTranslator;
import me.THEREALWWEFAN231.funnelmc.translator.container.screenhandler.ScreenHandlerTranslatorManager;
import me.THEREALWWEFAN231.funnelmc.translator.enchantment.EnchantmentTranslator;
import me.THEREALWWEFAN231.funnelmc.translator.item.ItemTranslator;
import me.THEREALWWEFAN231.funnelmc.javaconnection.packet.movement.PlayerAuthInputSender;
import me.THEREALWWEFAN231.funnelmc.utils.FileManagement;
import me.THEREALWWEFAN231.funnelmc.utils.FunnelLogSetup;
import net.minecraft.client.Minecraft;

public class FunnelMC {

	public static FunnelMC instance = new FunnelMC();
	public static Minecraft mc = Minecraft.getInstance();

	// Bumped by hand on every build sent to the tester - logged first thing on startup so a fresh
	// funnelmc.log can always be checked against this string to confirm which jar actually produced
	// it, instead of guessing from error line numbers whether an old build is still in use.
	public static final String BUILD_ID = "fix-unsigned-connectionid-encode-2026-07-29";

	public FileManagement fileManagement;
	public PacketTranslatorManager packetTranslatorManager;

	public void initialize() {
		FunnelLogSetup.install();

		Logger startupLogger = LogManager.getLogger(FunnelMC.class);
		startupLogger.warn("FunnelMC starting, BUILD_ID={}", BUILD_ID);

		this.fileManagement = new FileManagement();
		this.packetTranslatorManager = new PacketTranslatorManager();

		BlockEntityRegistry.load();
		BlockStateTranslator.load();
		EntityTranslator.load();
		ItemTranslator.load();
		EnchantmentTranslator.load();
		ScreenHandlerTranslatorManager.load();
		new PlayerAuthInputSender();
		new LoadTrackerDiagnostics();
	}

}
