package de.flo56958.waystones;

import de.flo56958.waystones.Utilities.SortingType;

import java.util.HashSet;

public class PlayerSave {
	public HashSet<String> waystones;
	public SortingType sortingType;

	public PlayerSave(HashSet<String> waystones, SortingType sortingType) {
		this.waystones = waystones;
		this.sortingType = sortingType;
	}
}
