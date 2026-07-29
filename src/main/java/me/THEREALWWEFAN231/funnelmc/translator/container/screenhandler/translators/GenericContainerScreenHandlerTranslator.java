package me.THEREALWWEFAN231.funnelmc.translator.container.screenhandler.translators;

import me.THEREALWWEFAN231.funnelmc.bedrockconnection.Client;
import me.THEREALWWEFAN231.funnelmc.bedrockconnection.caches.container.BedrockContainer;
import me.THEREALWWEFAN231.funnelmc.translator.container.screenhandler.ScreenHandlerTranslator;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.inventory.AbstractContainerMenu;

public class GenericContainerScreenHandlerTranslator extends ScreenHandlerTranslator<ChestMenu> {

    @Override
    public BedrockContainer getBedrockContainerFromJava(ChestMenu javaContainer, int javaSlotId) {
        int slotsInContainer = javaContainer.getRowCount() * 9;

        if (javaSlotId < slotsInContainer) {
            return Client.instance.containers.getCurrentlyOpenContainer();
        }

        return Client.instance.containers.getPlayerInventory();
    }

    @Override
    public int getBedrockSlotFromJavaContainer(ChestMenu javaContainer, int javaSlotId, BedrockContainer bedrockContainer) {
        int slotsInContainer = javaContainer.getRowCount() * 9;
        if (javaSlotId < slotsInContainer) {//the ids are the same in java and bedrock for chest containers
            return javaSlotId;
        }

        javaSlotId -= slotsInContainer;

        if (javaSlotId > 26) {//hotbar
            return javaSlotId - 27;
        }

        return javaSlotId + 9;
    }

    @Override
    public Class<? extends AbstractContainerMenu> getScreenHandlerClass() {
        return ChestMenu.class;
    }

}