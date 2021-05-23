package me.THEREALWWEFAN231.tunnelmc.translator;

public abstract class PacketTranslator<T> {

	public abstract void translate(T packet);

	public abstract Class<?> getPacketClass();

	public boolean idleUntil() {
		return false;
	}

}
