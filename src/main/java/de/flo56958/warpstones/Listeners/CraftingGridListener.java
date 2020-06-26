package de.flo56958.warpstones.Listeners;

import de.flo56958.warpstones.Main;
import org.bukkit.NamespacedKey;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.PrepareItemCraftEvent;
import org.bukkit.inventory.CraftingInventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

public class CraftingGridListener implements Listener {
	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public void onPrepare(PrepareItemCraftEvent event) {
		CraftingInventory inv = event.getInventory();
		for (ItemStack is : inv.getMatrix()) {
			if (is == null) continue;
			ItemMeta meta = is.getItemMeta();
			if (meta != null) {
				PersistentDataContainer persistentDataContainer = meta.getPersistentDataContainer();
				if (persistentDataContainer.has(new NamespacedKey(Main.plugin, "Warpscroll"), PersistentDataType.INTEGER)
						|| persistentDataContainer.has(new NamespacedKey(Main.plugin, "Warpstone"), PersistentDataType.INTEGER)) {
					inv.setResult(null);
					break;
				}
			}
		}
	}
}
