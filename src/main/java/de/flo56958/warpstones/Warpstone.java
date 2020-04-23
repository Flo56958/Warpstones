package de.flo56958.warpstones;

import org.bukkit.DyeColor;

public class Warpstone {

	public String worlduuid;
	public int x;
	public int y;
	public int z;
	public String Name;
	public boolean isGlobal = false;
	public boolean locked = false;

	//Is the same as for the shulker entity
	public String uuid;
	public String owner;
	public DyeColor color = null;

}
