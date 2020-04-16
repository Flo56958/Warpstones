package de.flo56958.waystones.Listeners;

import de.flo56958.waystones.Main;
import de.flo56958.waystones.Utilities.NBT.NBTUtilitiesReflections;
import de.flo56958.waystones.Utilities.PlayerInfo;
import de.flo56958.waystones.Waystone;
import de.flo56958.waystones.WaystoneManager;
import de.flo56958.waystones.gui.ButtonAction;
import de.flo56958.waystones.gui.GUI;
import org.bukkit.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;

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

	private final HashMap<String, Long> playerInteractTimer = new HashMap<>();
	private final HashMap<String, Long> cooldowns = new HashMap<>();

	@EventHandler //Cannot set ignorecancelled
	public void onInteract(PlayerInteractEvent e) {
		//TODO: Add sound and particle effects
		//TODO: Make Teleport Time
		if (e.getAction() != Action.RIGHT_CLICK_AIR) return;

		ItemStack scroll = e.getItem();
		if (scroll == null) return;
		if (!scroll.hasItemMeta()) return;

		NBTUtilitiesReflections nbts = new NBTUtilitiesReflections(scroll);
		if (!nbts.hasNBT()) return;

		if (nbts.getInt("Warpscroll") != 56958) return;

		//As the event always triggers two times
		Long time = playerInteractTimer.get(e.getPlayer().getUniqueId().toString());
		if (time == null) {
			playerInteractTimer.put(e.getPlayer().getUniqueId().toString(), System.currentTimeMillis());
		} else {
			long t = System.currentTimeMillis();
			if (t - time < 1000) return;
			else playerInteractTimer.put(e.getPlayer().getUniqueId().toString(), t);
		}

		e.setCancelled(true);

		//Check for cooldown
		Long cooldown = cooldowns.get(e.getPlayer().getUniqueId().toString());
		if (cooldown != null) {
			long diff = (System.currentTimeMillis() - cooldown) / 1000 - Main.plugin.getConfig().getInt("Warpscroll.Cooldown");
			if (diff < 0) { //Still on cooldown
				Main.sendActionBar(e.getPlayer(), "Warpscroll is still on cooldown! " + -diff + " Seconds remain!");
				return;
			} else {
				cooldowns.remove(e.getPlayer().getUniqueId().toString());
			}
		}

		Location loc = e.getPlayer().getLocation();

		HashSet<Waystone> ws = new HashSet<>(WaystoneManager.getInstance().globalWaystones);
		HashSet<String> toremove = new HashSet<>();

		loop:
		for (String s : WaystoneManager.getInstance().playerWaystones.get(e.getPlayer().getUniqueId().toString())) {
			for (Waystone w : WaystoneManager.getInstance().waystones) {
				if (w.uuid.equals(s)) {
					ws.add(w);
					continue loop;
				}
			}
			toremove.add(s);
		}
		WaystoneManager.getInstance().playerWaystones.get(e.getPlayer().getUniqueId().toString()).removeAll(toremove);

		ArrayList<Waystone> wslist = new ArrayList<>(ws);
		wslist.sort(Comparator.comparing(x -> x.Name));

		int size = wslist.size();

		GUI gui = new GUI();
		int windowindex = 1;
		boolean done = false;
		while (!done) {
			GUI.Window currentPage = gui.addWindow(6, "Selector #" + windowindex++ + " - Warpscroll");

			if (size > 45) {
				GUI.Window.Button back = currentPage.addButton(0, 5, backStack);
				back.addAction(ClickType.LEFT, new ButtonAction.PAGE_DOWN(back));

				GUI.Window.Button forward = currentPage.addButton(8, 5, forwardStack);
				forward.addAction(ClickType.LEFT, new ButtonAction.PAGE_UP(forward));
			}

			int index = 0;
			while (!wslist.isEmpty() && index < 45) {
				Waystone item = wslist.get(0);
				wslist.remove(0);
				if (item == null) continue;

				ItemStack stack;
				if (item.color == DyeColor.WHITE) {
					stack = new ItemStack(Material.WHITE_SHULKER_BOX);
				} else if (item.color == DyeColor.ORANGE) {
					stack = new ItemStack(Material.ORANGE_SHULKER_BOX);
				} else if (item.color == DyeColor.MAGENTA) {
					stack = new ItemStack(Material.MAGENTA_SHULKER_BOX);
				} else if (item.color == DyeColor.LIGHT_BLUE) {
					stack = new ItemStack(Material.LIGHT_BLUE_SHULKER_BOX);
				} else if (item.color == DyeColor.YELLOW) {
					stack = new ItemStack(Material.YELLOW_SHULKER_BOX);
				} else if (item.color == DyeColor.LIME) {
					stack = new ItemStack(Material.LIME_SHULKER_BOX);
				} else if (item.color == DyeColor.PINK) {
					stack = new ItemStack(Material.PINK_SHULKER_BOX);
				} else if (item.color == DyeColor.GRAY) {
					stack = new ItemStack(Material.GRAY_SHULKER_BOX);
				} else if (item.color == DyeColor.LIGHT_GRAY) {
					stack = new ItemStack(Material.LIGHT_GRAY_SHULKER_BOX);
				} else if (item.color == DyeColor.CYAN) {
					stack = new ItemStack(Material.CYAN_SHULKER_BOX);
				} else if (item.color == DyeColor.PURPLE) {
					stack = new ItemStack(Material.PURPLE_SHULKER_BOX);
				} else if (item.color == DyeColor.BLUE) {
					stack = new ItemStack(Material.BLUE_SHULKER_BOX);
				} else if (item.color == DyeColor.BROWN) {
					stack = new ItemStack(Material.BROWN_SHULKER_BOX);
				} else if (item.color == DyeColor.GREEN) {
					stack = new ItemStack(Material.GREEN_SHULKER_BOX);
				} else if (item.color == DyeColor.RED) {
					stack = new ItemStack(Material.RED_SHULKER_BOX);
				} else if (item.color == DyeColor.BLACK) {
					stack = new ItemStack(Material.BLACK_SHULKER_BOX);
				} else {
					stack = new ItemStack(Material.SHULKER_BOX);
				}
				ItemMeta meta = stack.getItemMeta();
				meta.setDisplayName(item.Name);
				List<String> lore = new ArrayList<>();
				String playername = Bukkit.getOfflinePlayer(UUID.fromString(item.owner)).getName();
				lore.add(ChatColor.WHITE + "Owner: " + playername);
				if (item.isGlobal) lore.add(ChatColor.WHITE + "Global Waystone");
				lore.add(ChatColor.WHITE + "World: " + item.worldname);
				lore.add(ChatColor.WHITE + "Location: " + item.x + " " + item.y + " " + item.z);

				//calculate cost
				int dx = item.x - loc.getBlockX();
				int dy = item.y - loc.getBlockY();
				int dz = item.z - loc.getBlockZ();
				long distance = Math.abs(Math.round(Math.sqrt(dx * dx + dy * dy + dz * dz)));
				long dist = distance - Main.plugin.getConfig().getInt("FreeTeleportRange", 150);
				int cost;
				if (dist <= 0) cost = 0;
				else cost = (int) Math.round(dist * Main.plugin.getConfig().getDouble("ExperienceCostPerBlock"));
				if (!loc.getWorld().getName().equals(item.worldname)) cost += Main.plugin.getConfig().getInt("InterdimensionalTravelCost", 200);
				cost = (int) Math.round(cost * Main.plugin.getConfig().getDouble("Warpscroll.XPMultiplier", 1.0));
				ChatColor color = ChatColor.WHITE;
				boolean canTeleport = true;
				if (e.getPlayer().getGameMode() != GameMode.CREATIVE && PlayerInfo.getPlayerExp(e.getPlayer()) < cost) {
					canTeleport = false;
					color = ChatColor.RED;
				}
				lore.add(ChatColor.WHITE + "Distance: " + distance + " Blocks");
				lore.add(color + "XP-Cost to teleport: " + cost + "XP");
				meta.setLore(lore);
				stack.setItemMeta(meta);

				GUI.Window.Button but = currentPage.addButton(index, stack);
				if (canTeleport) {
					int finalCost = cost;
					but.addAction(ClickType.LEFT, new ButtonAction.RUN_RUNNABLE_ON_PLAYER(but, (p, s) -> {
						if (p.getGameMode() != GameMode.CREATIVE) p.giveExp(-finalCost);
						p.teleport(new Location(Bukkit.getWorld(item.worldname), item.x, item.y + 1, item.z));
						cooldowns.put(p.getUniqueId().toString(), System.currentTimeMillis());
					}));
				}
				index++;
			}

			if (wslist.isEmpty()) done = true;
		}
		gui.show(e.getPlayer());
	}
}
