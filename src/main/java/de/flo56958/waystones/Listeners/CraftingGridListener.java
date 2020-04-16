package de.flo56958.waystones.Listeners;

import de.flo56958.waystones.Utilities.NBT.NBTUtilitiesReflections;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.PrepareItemCraftEvent;
import org.bukkit.inventory.CraftingInventory;
import org.bukkit.inventory.ItemStack;

public class CraftingGridListener implements Listener {
	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public void onPrepare(PrepareItemCraftEvent event) {
		CraftingInventory inv = event.getInventory();
		for (ItemStack is : inv.getMatrix()) {
			if (is == null) continue;
			NBTUtilitiesReflections nbts = new NBTUtilitiesReflections(is);
			if (!nbts.hasNBT()) continue;

			if (nbts.getInt("Warpscroll") == 56958 || nbts.getInt("Waystone") == 56958) {
				inv.setResult(null);
				break;
			}
		}
	}
}
