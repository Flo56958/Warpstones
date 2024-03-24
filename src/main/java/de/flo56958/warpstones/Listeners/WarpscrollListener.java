package de.flo56958.warpstones.Listeners;

import de.flo56958.warpstones.Main;
import de.flo56958.warpstones.Utilities.ChatWriter;
import de.flo56958.warpstones.WarpstoneManager;
import de.flo56958.warpstones.gui.GUI;
import org.bukkit.NamespacedKey;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;

public class WarpscrollListener implements Listener {

	@EventHandler //Cannot set ignorecancelled
	public void onInteract(PlayerInteractEvent e) {
		if (e.getAction() != Action.RIGHT_CLICK_AIR && e.getAction() != Action.RIGHT_CLICK_BLOCK) return;

		ItemStack scroll = e.getItem();
		if (scroll == null) return;
		if (!scroll.hasItemMeta()) return;

		if (!scroll.getItemMeta().getPersistentDataContainer().has(new NamespacedKey(Main.plugin, "Warpscroll"), PersistentDataType.INTEGER)) {
			return;
		}

		if (!e.getPlayer().hasPermission("warpstones.warpscroll.use")) return;

		//As the event always triggers two times
		if (WarpstoneManager.checkInteractTimer(e.getPlayer())) return;

		e.setCancelled(true);

		//Check for cooldown
		Long cooldown = WarpstoneManager.cooldowns.get(e.getPlayer().getUniqueId().toString());
		if (cooldown != null) {
			long diff = (System.currentTimeMillis() - cooldown) / 1000 - Main.plugin.getConfig().getInt("Warpscroll.Cooldown");
			if (diff < 0) { //Still on cooldown
				ChatWriter.sendActionBar(e.getPlayer(), "Warpscroll is still on cooldown! " + -diff + " Seconds remain!");
				return;
			} else {
				WarpstoneManager.cooldowns.remove(e.getPlayer().getUniqueId().toString());
			}
		}

		GUI gui = WarpstoneManager.getInstance().createGUI(null, e.getItem(), e.getPlayer(), null, false);
		if (gui != null) gui.show(e.getPlayer());
	}
}
