package me.THEREALWWEFAN231.tunnelmc.translator.enchantment;

import java.util.HashMap;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.Enchantments;

//https://www.digminecraft.com/lists/enchantment_list_pe.php
public class EnchantmentTranslator {

	//bedrock enchantment ids and the java enchantment
	public static final HashMap<Integer, Enchantment> BEDROCK_TO_JAVA_ENCHANTMENTS = new HashMap<Integer, Enchantment>();

	public static void load() {

		BEDROCK_TO_JAVA_ENCHANTMENTS.put(0, Enchantments.PROTECTION);
		BEDROCK_TO_JAVA_ENCHANTMENTS.put(1, Enchantments.FIRE_PROTECTION);
		BEDROCK_TO_JAVA_ENCHANTMENTS.put(2, Enchantments.FEATHER_FALLING);
		BEDROCK_TO_JAVA_ENCHANTMENTS.put(3, Enchantments.BLAST_PROTECTION);
		BEDROCK_TO_JAVA_ENCHANTMENTS.put(4, Enchantments.PROJECTILE_PROTECTION);
		BEDROCK_TO_JAVA_ENCHANTMENTS.put(5, Enchantments.THORNS);
		BEDROCK_TO_JAVA_ENCHANTMENTS.put(6, Enchantments.RESPIRATION);
		BEDROCK_TO_JAVA_ENCHANTMENTS.put(7, Enchantments.DEPTH_STRIDER);
		BEDROCK_TO_JAVA_ENCHANTMENTS.put(8, Enchantments.AQUA_AFFINITY);
		BEDROCK_TO_JAVA_ENCHANTMENTS.put(9, Enchantments.SHARPNESS);
		BEDROCK_TO_JAVA_ENCHANTMENTS.put(10, Enchantments.SMITE);
		BEDROCK_TO_JAVA_ENCHANTMENTS.put(11, Enchantments.BANE_OF_ARTHROPODS);
		BEDROCK_TO_JAVA_ENCHANTMENTS.put(12, Enchantments.KNOCKBACK);
		BEDROCK_TO_JAVA_ENCHANTMENTS.put(13, Enchantments.FIRE_ASPECT);
		BEDROCK_TO_JAVA_ENCHANTMENTS.put(14, Enchantments.LOOTING);
		BEDROCK_TO_JAVA_ENCHANTMENTS.put(15, Enchantments.EFFICIENCY);
		BEDROCK_TO_JAVA_ENCHANTMENTS.put(16, Enchantments.SILK_TOUCH);
		BEDROCK_TO_JAVA_ENCHANTMENTS.put(17, Enchantments.UNBREAKING);
		BEDROCK_TO_JAVA_ENCHANTMENTS.put(18, Enchantments.FORTUNE);
		BEDROCK_TO_JAVA_ENCHANTMENTS.put(19, Enchantments.POWER);
		BEDROCK_TO_JAVA_ENCHANTMENTS.put(20, Enchantments.PUNCH);
		BEDROCK_TO_JAVA_ENCHANTMENTS.put(21, Enchantments.FLAME);
		BEDROCK_TO_JAVA_ENCHANTMENTS.put(22, Enchantments.INFINITY);
		BEDROCK_TO_JAVA_ENCHANTMENTS.put(23, Enchantments.LUCK_OF_THE_SEA);
		BEDROCK_TO_JAVA_ENCHANTMENTS.put(24, Enchantments.LURE);
		BEDROCK_TO_JAVA_ENCHANTMENTS.put(25, Enchantments.FROST_WALKER);
		BEDROCK_TO_JAVA_ENCHANTMENTS.put(26, Enchantments.MENDING);
		BEDROCK_TO_JAVA_ENCHANTMENTS.put(27, Enchantments.BINDING_CURSE);
		BEDROCK_TO_JAVA_ENCHANTMENTS.put(28, Enchantments.VANISHING_CURSE);
		BEDROCK_TO_JAVA_ENCHANTMENTS.put(29, Enchantments.IMPALING);
		BEDROCK_TO_JAVA_ENCHANTMENTS.put(30, Enchantments.RIPTIDE);
		BEDROCK_TO_JAVA_ENCHANTMENTS.put(31, Enchantments.LOYALTY);
		BEDROCK_TO_JAVA_ENCHANTMENTS.put(32, Enchantments.CHANNELING);
		BEDROCK_TO_JAVA_ENCHANTMENTS.put(33, Enchantments.MULTISHOT);
		BEDROCK_TO_JAVA_ENCHANTMENTS.put(34, Enchantments.PIERCING);
		BEDROCK_TO_JAVA_ENCHANTMENTS.put(35, Enchantments.QUICK_CHARGE);
		BEDROCK_TO_JAVA_ENCHANTMENTS.put(36, Enchantments.SOUL_SPEED);
	}
	
}
