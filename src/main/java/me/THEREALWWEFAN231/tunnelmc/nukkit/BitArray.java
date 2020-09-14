package me.THEREALWWEFAN231.tunnelmc.nukkit;

public interface BitArray {

	void set(int index, int value);

	int get(int index);

	int size();

	int[] getWords();

	BitArrayVersion getVersion();

	BitArray copy();
}
