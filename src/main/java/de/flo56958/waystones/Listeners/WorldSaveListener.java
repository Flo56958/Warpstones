package de.flo56958.waystones.Listeners;

import de.flo56958.waystones.WaystoneManager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.WorldSaveEvent;

public class WorldSaveListener implements Listener {

	@EventHandler
	public void onSave(WorldSaveEvent e) {
		WaystoneManager.getInstance().save();
	}
}
