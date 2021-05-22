package me.THEREALWWEFAN231.tunnelmc.translator.container.type;

import com.nukkitx.protocol.bedrock.data.inventory.ContainerType;

import net.minecraft.screen.ScreenHandlerType;

public class ContainerTypeTranslator {
	
	public static ScreenHandlerType<?> bedrockToJava(ContainerType containerType) {
		switch (containerType) {
		case NONE:
			break;
		case INVENTORY:
			break;
		case CONTAINER:
			return ScreenHandlerType.GENERIC_9X3;
		case WORKBENCH:
			return ScreenHandlerType.CRAFTING;
		case FURNACE:
			return ScreenHandlerType.FURNACE;
		case ENCHANTMENT:
			return ScreenHandlerType.ENCHANTMENT;
		case BREWING_STAND:
			return ScreenHandlerType.BREWING_STAND;
		case ANVIL:
			return ScreenHandlerType.ANVIL;
		case DISPENSER:
			return ScreenHandlerType.GENERIC_3X3;
		case DROPPER:
			return ScreenHandlerType.GENERIC_3X3;
		case HOPPER:
			return ScreenHandlerType.HOPPER;
		case CAULDRON:
			break;
		case MINECART_CHEST:
			return ScreenHandlerType.GENERIC_9X3;
		case MINECART_HOPPER:
			return ScreenHandlerType.HOPPER;
		case HORSE:
			break;//in the java edition the horse inventory is opened by OpenHorseScreenS2CPacket
		case BEACON:
			return ScreenHandlerType.BEACON;
		case STRUCTURE_EDITOR:
			break;//no idea
		case TRADE:
			return ScreenHandlerType.MERCHANT;
		case COMMAND_BLOCK:
			break;//command blocks aren't containers in the java edition, they are opened via CommandBlockBlockEntity
		case JUKEBOX:
			break;
		case ARMOR:
			break;
		case HAND:
			break;
		case COMPOUND_CREATOR:
			break;
		case MATERIAL_REDUCER:
			break;
		case LAB_TABLE:
			break;
		case LOOM:
			return ScreenHandlerType.LOOM;
		case LECTERN:
			return ScreenHandlerType.LECTERN;
		case GRINDSTONE:
			return ScreenHandlerType.GRINDSTONE;
		case BLAST_FURNACE:
			return ScreenHandlerType.BLAST_FURNACE;
		case SMOKER:
			return ScreenHandlerType.SMOKER;
		case STONECUTTER:
			return ScreenHandlerType.STONECUTTER;
		case CARTOGRAPHY:
			return ScreenHandlerType.CARTOGRAPHY_TABLE;
		case HUD:
			break;
		case JIGSAW_EDITOR:
			break;//I have no idea? Structure block? :shrug:
		case SMITHING_TABLE:
			return ScreenHandlerType.SMITHING;
		default:
			break;
		}
		return null;
	}
	
}
