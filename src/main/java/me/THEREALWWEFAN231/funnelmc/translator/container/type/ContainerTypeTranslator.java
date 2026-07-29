package me.THEREALWWEFAN231.funnelmc.translator.container.type;

import org.cloudburstmc.protocol.bedrock.data.inventory.ContainerType;

import net.minecraft.world.inventory.MenuType;

public class ContainerTypeTranslator {

	public static MenuType<?> bedrockToJava(ContainerType containerType) {
		switch (containerType) {
		case NONE:
			break;
		case INVENTORY:
			break;
		case CONTAINER:
			return MenuType.GENERIC_9x3;
		case WORKBENCH:
			return MenuType.CRAFTING;
		case FURNACE:
			return MenuType.FURNACE;
		case ENCHANTMENT:
			return MenuType.ENCHANTMENT;
		case BREWING_STAND:
			return MenuType.BREWING_STAND;
		case ANVIL:
			return MenuType.ANVIL;
		case DISPENSER:
			return MenuType.GENERIC_3x3;
		case DROPPER:
			return MenuType.GENERIC_3x3;
		case HOPPER:
			return MenuType.HOPPER;
		case CAULDRON:
			break;
		case MINECART_CHEST:
			return MenuType.GENERIC_9x3;
		case MINECART_HOPPER:
			return MenuType.HOPPER;
		case HORSE:
			break;//in the java edition the horse inventory is opened by ClientboundHorseScreenOpenPacket
		case BEACON:
			return MenuType.BEACON;
		case STRUCTURE_EDITOR:
			break;//no idea
		case TRADE:
			return MenuType.MERCHANT;
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
			return MenuType.LOOM;
		case LECTERN:
			return MenuType.LECTERN;
		case GRINDSTONE:
			return MenuType.GRINDSTONE;
		case BLAST_FURNACE:
			return MenuType.BLAST_FURNACE;
		case SMOKER:
			return MenuType.SMOKER;
		case STONECUTTER:
			return MenuType.STONECUTTER;
		case CARTOGRAPHY:
			return MenuType.CARTOGRAPHY_TABLE;
		case HUD:
			break;
		case JIGSAW_EDITOR:
			break;//I have no idea? Structure block? :shrug:
		case SMITHING_TABLE:
			return MenuType.SMITHING;
		default:
			break;
		}
		return null;
	}

}
