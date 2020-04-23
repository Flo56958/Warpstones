package de.flo56958.warpstones.Listeners;

import de.flo56958.warpstones.WarpstoneManager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.WorldSaveEvent;

public class WorldSaveListener implements Listener {

	@EventHandler
	public void onSave(WorldSaveEvent e) {
		WarpstoneManager.getInstance().save();
	}
}
