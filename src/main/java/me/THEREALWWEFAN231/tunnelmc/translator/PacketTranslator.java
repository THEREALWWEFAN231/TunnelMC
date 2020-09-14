package me.THEREALWWEFAN231.tunnelmc.translator;

public abstract class PacketTranslator<T> {

	public abstract void translate(T packet);

	public abstract Class<?> getPacketClass();//i could use reflection but it generally wouldn't be ideal

}
