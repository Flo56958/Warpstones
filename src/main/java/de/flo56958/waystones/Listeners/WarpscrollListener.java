package de.flo56958.waystones.Listeners;

import de.flo56958.waystones.Main;
import de.flo56958.waystones.Utilities.NBT.NBTUtilitiesReflections;
import de.flo56958.waystones.WaystoneManager;
import de.flo56958.waystones.gui.GUI;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class WarpscrollListener implements Listener {

	private final static ItemStack forwardStack;
	private final static ItemStack backStack;
	static {
		forwardStack = new ItemStack(Material.GREEN_STAINED_GLASS_PANE, 1);
		ItemMeta forwardMeta = forwardStack.getItemMeta();

		if (forwardMeta != null) {
			forwardMeta.setDisplayName(ChatColor.GREEN + "Forward");
			forwardStack.setItemMeta(forwardMeta);
		}

		backStack = new ItemStack(Material.RED_STAINED_GLASS_PANE, 1);
		ItemMeta backMeta = backStack.getItemMeta();

		if (backMeta != null) {
			backMeta.setDisplayName(ChatColor.RED + "Back");
			backStack.setItemMeta(backMeta);
		}
	}

	@EventHandler //Cannot set ignorecancelled
	public void onInteract(PlayerInteractEvent e) {
		if (e.getAction() != Action.RIGHT_CLICK_AIR && e.getAction() != Action.RIGHT_CLICK_BLOCK) return;

		ItemStack scroll = e.getItem();
		if (scroll == null) return;
		if (!scroll.hasItemMeta()) return;

		NBTUtilitiesReflections nbts = new NBTUtilitiesReflections(scroll);
		if (!nbts.hasNBT()) return;

		if (nbts.getInt("Warpscroll") != 56958) return;

		if (!e.getPlayer().hasPermission("waystones.warpscroll.use")) return;

		//As the event always triggers two times
		if (WaystoneManager.checkInteractTimer(e.getPlayer())) return;

		e.setCancelled(true);

		//Check for cooldown
		Long cooldown = WaystoneManager.cooldowns.get(e.getPlayer().getUniqueId().toString());
		if (cooldown != null) {
			long diff = (System.currentTimeMillis() - cooldown) / 1000 - Main.plugin.getConfig().getInt("Warpscroll.Cooldown");
			if (diff < 0) { //Still on cooldown
				Main.sendActionBar(e.getPlayer(), "Warpscroll is still on cooldown! " + -diff + " Seconds remain!");
				return;
			} else {
				WaystoneManager.cooldowns.remove(e.getPlayer().getUniqueId().toString());
			}
		}

		GUI gui = WaystoneManager.getInstance().createGUI(null, e.getItem(), e.getPlayer(), null);
		gui.show(e.getPlayer());
	}
}
