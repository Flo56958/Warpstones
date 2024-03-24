package de.flo56958.warpstones.Listeners;

import de.flo56958.warpstones.Main;
import de.flo56958.warpstones.Utilities.ChatWriter;
import de.flo56958.warpstones.Warpstone;
import de.flo56958.warpstones.WarpstoneManager;
import de.flo56958.warpstones.gui.GUI;
import org.bukkit.*;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Shulker;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;

import java.util.UUID;

public class WarpstoneListener implements Listener {

	@EventHandler (priority = EventPriority.HIGH, ignoreCancelled = true)
	public void onPlace(PlayerInteractEvent e) {
		if (e.getClickedBlock() == null) return;
		if (e.getAction() != Action.RIGHT_CLICK_BLOCK) return;

		ItemStack stack = e.getItem();
		if (stack == null) return;
		if (!stack.hasItemMeta()) return;

		if (!stack.getItemMeta().getPersistentDataContainer().has(new NamespacedKey(Main.plugin, "Warpstone"), PersistentDataType.INTEGER)) {
			return;
		}

		if (!e.getPlayer().hasPermission("warpstone.place")) return;

		//checking if maximum warpstones are reached
		if (WarpstoneManager.getInstance().warpstones.size() >= Main.plugin.getConfig().getInt("MaximumWarpstones")) {
			ChatWriter.sendActionBar(e.getPlayer(), "Warpstone can't be placed as the maximum amount of Waypoint is reached!");
			return;
		}

		//ITEM is Warpstonemarker
		//creating Warpstone

		Location loc = e.getClickedBlock().getLocation().clone();
		switch (e.getBlockFace()) {
			case NORTH:
				loc.add(0, 0, -1);
				break;
			case EAST:
				loc.add(1, 0, 0);
				break;
			case SOUTH:
				loc.add(0, 0, 1);
				break;
			case WEST:
				loc.add(-1, 0, 0);
				break;
			case UP:
				loc.add(0, 1, 0);
				break;
			case DOWN:
				loc.add(0, -1, 0);
				break;
			default:
				return;
		}

		//Checking for Warpstones already in this location
		for (Warpstone warpstone : WarpstoneManager.getInstance().warpstones) {
			if (loc.getWorld().getName().equals(warpstone.Name) && loc.getBlockX() == warpstone.x
					&& loc.getBlockY() == warpstone.y && loc.getBlockZ() == warpstone.z) {
				ChatWriter.sendActionBar(e.getPlayer(), "Warpstone can't be placed as the space is already occupied!");
				return;
			}
		}

		Entity ent = loc.getWorld().spawnEntity(loc, EntityType.SHULKER);
		if (ent instanceof Shulker) {
			Warpstone warpstone = new Warpstone();
			Shulker shulker = (Shulker) ent;
			warpstone.uuid = shulker.getUniqueId().toString();
			warpstone.Name = stack.getItemMeta().getDisplayName();
			warpstone.worlduuid = loc.getWorld().getUID().toString();
			warpstone.owner = e.getPlayer().getUniqueId().toString();
			warpstone.color = shulker.getColor();
			warpstone.x = loc.getBlockX();
			warpstone.y = loc.getBlockY();
			warpstone.z = loc.getBlockZ();
			shulker.setAI(false);
			shulker.setInvulnerable(true);
			shulker.setCustomName(stack.getItemMeta().getDisplayName());
			shulker.setCustomNameVisible(true);
			shulker.setSilent(true);

			WarpstoneManager.getInstance().addWarpstone(warpstone);
			WarpstoneManager.getInstance().activateWarpstone(e.getPlayer(), warpstone);
			if (e.getPlayer().getGameMode() != GameMode.CREATIVE) stack.setAmount(stack.getAmount() - 1);
		}
	}

	@EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
	public void onInteract(PlayerInteractEntityEvent e) {
		ItemStack itemInMainHand = e.getPlayer().getInventory().getItemInMainHand();
		ItemStack itemInOffHand = e.getPlayer().getInventory().getItemInOffHand();

		if (!(e.getRightClicked() instanceof Shulker)) return;
		Shulker shulker = (Shulker) e.getRightClicked();
		if (shulker.hasAI()) return;

		Warpstone warpstone = null;
		for (Warpstone warp : WarpstoneManager.getInstance().warpstones) {
			if (warp.uuid.equals(shulker.getUniqueId().toString())) {
				warpstone = warp;
				break;
			}
		}
		if (warpstone == null) return;

		//Naming the Shulker is not possible with Nametag
		if (itemInMainHand != null && itemInMainHand.getType() == Material.NAME_TAG) {
			e.setCancelled(true);
			return;
		}
		if (itemInOffHand != null && itemInOffHand.getType() == Material.NAME_TAG) {
			e.setCancelled(true);
			return;
		}
		e.setCancelled(true);

		//As the event always triggers two times
		if (WarpstoneManager.checkInteractTimer(e.getPlayer())) return;

		if (e.getPlayer().isSneaking()) { //only activation
			if (!e.getPlayer().hasPermission("warpstones.discover")) return;
			WarpstoneManager.getInstance().toggleWarpstone(e.getPlayer(), warpstone);
		} else { //GUI and Teleport options and activation
			if (!e.getPlayer().hasPermission("warpstones.use")) return;
			WarpstoneManager.getInstance().activateWarpstone(e.getPlayer(), warpstone);

			if (warpstone.locked && !(warpstone.owner.equals(e.getPlayer().getUniqueId().toString())
					|| e.getPlayer().hasPermission("warpstones.admin"))) {
				ChatWriter.sendActionBar(e.getPlayer(), ChatColor.RED + "Warpstone is locked by Owner!");
				return;
			}

			GUI gui = WarpstoneManager.getInstance().createGUI(warpstone, null, e.getPlayer(), shulker, false);
			if (gui != null) gui.show(e.getPlayer());
		}

	}

	@EventHandler
	public void onShulkerDeath(EntityDeathEvent e) {
		if (!(e.getEntity() instanceof Shulker)) return;
		if (e.getEntity().hasAI()) return;

		UUID id = e.getEntity().getUniqueId();

		if (WarpstoneManager.getInstance().warpstones.removeIf(warpstone -> warpstone.uuid.equals(id.toString()))) {
			e.getDrops().clear();
		}
	}
}
