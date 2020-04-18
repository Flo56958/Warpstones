package de.flo56958.waystones.Listeners;

import de.flo56958.waystones.Main;
import de.flo56958.waystones.Utilities.NBT.NBTUtilitiesReflections;
import de.flo56958.waystones.Waystone;
import de.flo56958.waystones.WaystoneManager;
import de.flo56958.waystones.gui.GUI;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
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

import java.util.HashMap;
import java.util.UUID;

public class WaystoneListener implements Listener {

	private final HashMap<String, Long> playerInteractTimer = new HashMap<>();

	@EventHandler (priority = EventPriority.HIGH, ignoreCancelled = true)
	public void onPlace(PlayerInteractEvent e) {
		if (e.getClickedBlock() == null) return;
		if (e.getAction() != Action.RIGHT_CLICK_BLOCK) return;

		ItemStack stack = e.getItem();
		if (stack == null) return;
		if (!stack.hasItemMeta()) return;

		NBTUtilitiesReflections nbts = new NBTUtilitiesReflections(stack);
		if (!nbts.hasNBT()) return;

		if (nbts.getInt("Waystone") != 56958) return;

		if (!e.getPlayer().hasPermission("waystones.place")) return;

		//checking if maximum waypoints are reached
		if (WaystoneManager.getInstance().waystones.size() >= Main.plugin.getConfig().getInt("MaximumWaypoints")) {
			Main.sendActionBar(e.getPlayer(), "Waystone can't be placed as the maximum amount of Waypoint is reached!");
			return;
		}

		//ITEM is Waypointmarker
		//creating Waypoint

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

		//Checking for Waystones already in this location
		for (Waystone waystone : WaystoneManager.getInstance().waystones) {
			if (loc.getWorld().getName().equals(waystone.Name) && loc.getBlockX() == waystone.x
					&& loc.getBlockY() == waystone.y && loc.getBlockZ() == waystone.z) {
				Main.sendActionBar(e.getPlayer(), "Waystone can't be placed as the space is already occupied!");
				return;
			}
		}

		Entity ent = loc.getWorld().spawnEntity(loc, EntityType.SHULKER);
		if (ent instanceof Shulker) {
			Waystone waystone = new Waystone();
			Shulker shulker = (Shulker) ent;
			waystone.uuid = shulker.getUniqueId().toString();
			waystone.Name = stack.getItemMeta().getDisplayName();
			waystone.worldname = loc.getWorld().getName();
			waystone.owner = e.getPlayer().getUniqueId().toString();
			waystone.color = shulker.getColor();
			waystone.x = loc.getBlockX();
			waystone.y = loc.getBlockY();
			waystone.z = loc.getBlockZ();
			shulker.setAI(false);
			shulker.setInvulnerable(true);
			shulker.setCustomName(stack.getItemMeta().getDisplayName());
			shulker.setCustomNameVisible(true);
			shulker.setSilent(true);

			WaystoneManager.getInstance().addWaystone(waystone);
			WaystoneManager.getInstance().activateWaystone(e.getPlayer(), waystone);
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

		Waystone waystone = null;
		for (Waystone way : WaystoneManager.getInstance().waystones) {
			if (way.uuid.equals(shulker.getUniqueId().toString())) {
				waystone = way;
				break;
			}
		}
		if (waystone == null) return;

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
		if (WaystoneManager.checkInteractTimer(e.getPlayer())) return;

		if (e.getPlayer().isSneaking()) { //only activation
			if (!e.getPlayer().hasPermission("waystones.discover")) return;
			WaystoneManager.getInstance().toggleWaystone(e.getPlayer(), waystone);
		} else { //GUI and Teleport options and activation
			if (!e.getPlayer().hasPermission("waystones.use")) return;
			WaystoneManager.getInstance().activateWaystone(e.getPlayer(), waystone);

			GUI gui = WaystoneManager.getInstance().createGUI(waystone, null, e.getPlayer(), shulker);

			gui.show(e.getPlayer());
		}

	}

	@EventHandler
	public void onShulkerDeath(EntityDeathEvent e) {
		if (!(e.getEntity() instanceof Shulker)) return;
		if (e.getEntity().hasAI()) return;

		UUID id = e.getEntity().getUniqueId();

		Waystone toremove = null;
		for (Waystone waystone : WaystoneManager.getInstance().waystones) {
			if (waystone.uuid.equals(id.toString())) {
				toremove = waystone;
				break;
			}
		}

		if (toremove != null) {
			WaystoneManager.getInstance().removeWaystone(toremove);
			e.getDrops().clear();
		}
	}
}
