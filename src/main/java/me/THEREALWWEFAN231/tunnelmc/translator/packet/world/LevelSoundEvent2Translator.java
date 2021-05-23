package me.THEREALWWEFAN231.tunnelmc.translator.packet.world;

import com.nukkitx.protocol.bedrock.packet.LevelSoundEvent2Packet;
import me.THEREALWWEFAN231.tunnelmc.translator.PacketTranslator;

public class LevelSoundEvent2Translator extends PacketTranslator<LevelSoundEvent2Packet> {

    @Override
    public void translate(LevelSoundEvent2Packet packet) {
        System.out.println(packet);
        // Make this JSON mappings for any non-extra-data
    }

    @Override
    public Class<?> getPacketClass() {
        return LevelSoundEvent2Packet.class;
    }

}