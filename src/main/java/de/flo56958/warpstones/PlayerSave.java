package de.flo56958.warpstones;

import de.flo56958.warpstones.Utilities.SortingType;

import java.util.HashSet;

public class PlayerSave {
	public HashSet<String> warpstones;
	public SortingType sortingType;

	public PlayerSave(HashSet<String> warpstones, SortingType sortingType) {
		this.warpstones = warpstones;
		this.sortingType = sortingType;
	}
}
