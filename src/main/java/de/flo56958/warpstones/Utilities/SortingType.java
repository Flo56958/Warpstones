package de.flo56958.warpstones.Utilities;

public enum SortingType {
	ALPHABETICAL,
	DISTANCE,
	COST,
	RANDOM;

	public String getDisplayName() {
		String s = "Sorting: ";
		switch (this) {
			case ALPHABETICAL:
				s += "ALPHABETICAL";
				break;
			case DISTANCE:
				s += "DISTANCE";
				break;
			case COST:
				s += "COST";
				break;
			case RANDOM:
				s += "RANDOM";
				break;
		}
		return s;
	}

	public SortingType getNext() {
		switch (this) {
			case ALPHABETICAL:
				return DISTANCE;
			case DISTANCE:
				return COST;
			case COST:
				return RANDOM;
			case RANDOM:
				return ALPHABETICAL;
		}
		return ALPHABETICAL;
	}
}
