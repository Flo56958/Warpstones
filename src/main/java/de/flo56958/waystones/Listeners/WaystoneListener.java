package de.flo56958.waystones.Listeners;

import de.flo56958.waystones.Main;
import de.flo56958.waystones.Utilities.NBT.NBTUtilitiesReflections;
import de.flo56958.waystones.Utilities.PlayerInfo;
import de.flo56958.waystones.Waystone;
import de.flo56958.waystones.WaystoneManager;
import de.flo56958.waystones.gui.ButtonAction;
import de.flo56958.waystones.gui.GUI;
import org.bukkit.*;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Shulker;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;

public class WaystoneListener implements Listener {

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

	@EventHandler
	public void onPlace(PlayerInteractEvent e) {
		if (e.getClickedBlock() == null) return;
		if (e.getAction() != Action.RIGHT_CLICK_BLOCK) return;

		ItemStack stack = e.getItem();
		if (stack == null) return;
		if (!stack.hasItemMeta()) return;

		NBTUtilitiesReflections nbts = new NBTUtilitiesReflections(stack);
		if (!nbts.hasNBT()) return;

		if (nbts.getInt("Waystone") != 56958) return;

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

			WaystoneManager.getInstance().addWaystone(waystone);
			WaystoneManager.getInstance().activateWaystone(e.getPlayer(), waystone);
			if (e.getPlayer().getGameMode() != GameMode.CREATIVE) stack.setAmount(stack.getAmount() - 1);
		}
	}

	@EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
	public void onInteract(PlayerInteractEntityEvent e) {
		//TODO: Add sound and particle effects
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

		//As the event always triggers two times
		Long time = playerInteractTimer.get(e.getPlayer().getUniqueId().toString());
		if (time == null) {
			playerInteractTimer.put(e.getPlayer().getUniqueId().toString(), System.currentTimeMillis());
		} else {
			long t = System.currentTimeMillis();
			if (t - time < 1000) return;
			else playerInteractTimer.put(e.getPlayer().getUniqueId().toString(), t);
		}

		if (e.getPlayer().isSneaking()) { //only activation
			WaystoneManager.getInstance().toggleWaystone(e.getPlayer(), waystone);
		} else { //GUI and Teleport options and activation
			WaystoneManager.getInstance().activateWaystone(e.getPlayer(), waystone);

			//Gather all possible waypoints
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
			boolean isOwner = waystone.owner.equals(e.getPlayer().getUniqueId().toString());
			GUI customizeGUI = null;
			if(isOwner) {
				customizeGUI = new GUI();
				GUI.Window window = customizeGUI.addWindow(3, "Customizer - " + waystone.Name);

				//Rename Waypoint
				ItemStack nameritem = new ItemStack(Material.PAPER);
				ItemMeta meta = nameritem.getItemMeta();
				meta.setDisplayName("Rename Waypoint");
				ArrayList<String> lore = new ArrayList<>();
				lore.add(ChatColor.WHITE + "Send the Name in Chat!");
				meta.setLore(lore);
				nameritem.setItemMeta(meta);
				GUI.Window.Button namer = window.addButton(2, 1, nameritem);
				Waystone finalWaystone = waystone;
				namer.addAction(ClickType.LEFT, new ButtonAction.REQUEST_INPUT(namer, (p, input) -> {
					input = input.replaceAll("&", "§");
					//TODO: Naming Blacklist
					finalWaystone.Name = input;
					if (shulker.isCustomNameVisible()) shulker.setCustomName(input);
				}, ""));

				//Set Waypoint Global
				ItemStack globalitem = (waystone.isGlobal) ? new ItemStack(Material.GREEN_WOOL) : new ItemStack(Material.RED_WOOL);
				meta = globalitem.getItemMeta();
				meta.setDisplayName("Toggle Global Setting");
				lore = new ArrayList<>();
				lore.add(ChatColor.WHITE + "Should the waypoint be seen by everyone?");
				meta.setLore(lore);
				globalitem.setItemMeta(meta);
				GUI.Window.Button global = window.addButton(4, 1, globalitem);
				ItemStack finalGlobalitem = global.getItemStack();
				global.addAction(ClickType.LEFT, new ButtonAction.RUN_RUNNABLE(global, () -> {
					if (finalGlobalitem.getType() == Material.RED_WOOL) { //Toggle on
						finalWaystone.isGlobal = true;
						WaystoneManager.getInstance().globalWaystones.add(finalWaystone);
						finalGlobalitem.setType(Material.GREEN_WOOL);
					} else if (finalGlobalitem.getType() == Material.GREEN_WOOL) { //Toggle off
						finalWaystone.isGlobal = false;
						WaystoneManager.getInstance().globalWaystones.remove(finalWaystone);
						finalGlobalitem.setType(Material.RED_WOOL);
					}
				}));

				//Change Waypoint Color
				ItemStack coloritem = new ItemStack(Material.WHITE_WOOL);
				meta = coloritem.getItemMeta();
				meta.setDisplayName("Change Waypoint Color");
				coloritem.setItemMeta(meta);
				GUI.Window.Button color = window.addButton(6, 1, coloritem);
				color.addAction(ClickType.LEFT, new ButtonAction.PAGE_UP(color));

				GUI.Window colorWindow = customizeGUI.addWindow(2, "Choose Color for " + waystone.Name);
				int index = 0;
				ArrayList<DyeColor> colors = new ArrayList(Arrays.asList(DyeColor.values()));
				colors.add(null);
				for (DyeColor c : colors) {
					ItemStack col;
					if (c == DyeColor.WHITE) {
						col = new ItemStack(Material.WHITE_SHULKER_BOX);
					} else if (c == DyeColor.ORANGE) {
						col = new ItemStack(Material.ORANGE_SHULKER_BOX);
					} else if (c == DyeColor.MAGENTA) {
						col = new ItemStack(Material.MAGENTA_SHULKER_BOX);
					} else if (c == DyeColor.LIGHT_BLUE) {
						col = new ItemStack(Material.LIGHT_BLUE_SHULKER_BOX);
					} else if (c == DyeColor.YELLOW) {
						col = new ItemStack(Material.YELLOW_SHULKER_BOX);
					} else if (c == DyeColor.LIME) {
						col = new ItemStack(Material.LIME_SHULKER_BOX);
					} else if (c == DyeColor.PINK) {
						col = new ItemStack(Material.PINK_SHULKER_BOX);
					} else if (c == DyeColor.GRAY) {
						col = new ItemStack(Material.GRAY_SHULKER_BOX);
					} else if (c == DyeColor.LIGHT_GRAY) {
						col = new ItemStack(Material.LIGHT_GRAY_SHULKER_BOX);
					} else if (c == DyeColor.CYAN) {
						col = new ItemStack(Material.CYAN_SHULKER_BOX);
					} else if (c == DyeColor.PURPLE) {
						col = new ItemStack(Material.PURPLE_SHULKER_BOX);
					} else if (c == DyeColor.BLUE) {
						col = new ItemStack(Material.BLUE_SHULKER_BOX);
					} else if (c == DyeColor.BROWN) {
						col = new ItemStack(Material.BROWN_SHULKER_BOX);
					} else if (c == DyeColor.GREEN) {
						col = new ItemStack(Material.GREEN_SHULKER_BOX);
					} else if (c == DyeColor.RED) {
						col = new ItemStack(Material.RED_SHULKER_BOX);
					} else if (c == DyeColor.BLACK) {
						col = new ItemStack(Material.BLACK_SHULKER_BOX);
					} else {
						col = new ItemStack(Material.SHULKER_BOX);
					}
					meta = col.getItemMeta();
					if (c != null) meta.setDisplayName(c.name());
					else meta.setDisplayName("NORMAL");
					col.setItemMeta(meta);
					GUI.Window.Button but = colorWindow.addButton(index++, col);
					but.addAction(ClickType.LEFT, new ButtonAction.RUN_RUNNABLE(but, () -> {
						shulker.setColor(c);
						finalWaystone.color = c;
					}));
				}

				//Remove Waystone
				ItemStack removeItem = new ItemStack(Material.BEDROCK);
				meta = removeItem.getItemMeta();
				meta.setDisplayName("Remove Waystone");
				removeItem.setItemMeta(meta);
				GUI.Window.Button remove = window.addButton(5, 2, removeItem);
				GUI finalCustomizeGUI = customizeGUI;
				remove.addAction(ClickType.LEFT, new ButtonAction.RUN_RUNNABLE(remove, () -> {
					WaystoneManager.getInstance().removeWaystone(finalWaystone);
					shulker.getWorld().dropItemNaturally(shulker.getLocation(), Main.waystoneItem);
					shulker.remove();
					finalCustomizeGUI.close();
				}));

				//Transfer Ownership
				ItemStack owneritem = new ItemStack(Material.BLACK_WOOL);
				meta = owneritem.getItemMeta();
				meta.setDisplayName("Transfer Ownership");
				lore = new ArrayList<>();
				lore.add(ChatColor.WHITE + "Send the Name of the new Owner in Chat!");
				meta.setLore(lore);
				owneritem.setItemMeta(meta);
				GUI.Window.Button owner = window.addButton(3, 2, owneritem);
				owner.addAction(ClickType.LEFT, new ButtonAction.REQUEST_INPUT(owner, (p, input) -> {
					Player player = Bukkit.getServer().getPlayer(input);
					if (player == null) {
						p.sendMessage(ChatColor.RED + "Player was not found or not online!");
					} else {
						finalWaystone.owner = player.getUniqueId().toString();
						p.sendMessage(ChatColor.WHITE + "Transferred successfully to " + input + "!");
						player.sendMessage(ChatColor.WHITE + "You have been given ownership of the waypoint "
								+ finalWaystone.Name + " from " + p.getName() + "!");
						WaystoneManager.getInstance().activateWaystone(player, finalWaystone);
					}
				}, ""));

				//Show Nametag
				ItemStack nametagitem = (shulker.isCustomNameVisible()) ? new ItemStack(Material.GREEN_WOOL) : new ItemStack(Material.RED_WOOL);
				meta = nametagitem.getItemMeta();
				meta.setDisplayName("Toggle Nametag Visibility");
				lore = new ArrayList<>();
				lore.add(ChatColor.WHITE + "Should the waypoint have a Nametag?");
				meta.setLore(lore);
				nametagitem.setItemMeta(meta);
				GUI.Window.Button nametag = window.addButton(4, 0, nametagitem);
				ItemStack finalNametagItem = nametag.getItemStack();
				nametag.addAction(ClickType.LEFT, new ButtonAction.RUN_RUNNABLE(nametag, () -> {
					if (finalNametagItem.getType() == Material.RED_WOOL) { //Toggle on
						shulker.setCustomNameVisible(true);
						shulker.setCustomName(finalWaystone.Name);
						finalNametagItem.setType(Material.GREEN_WOOL);
					} else if (finalNametagItem.getType() == Material.GREEN_WOOL) { //Toggle off
						shulker.setCustomNameVisible(false);
						shulker.setCustomName(null);
						finalNametagItem.setType(Material.RED_WOOL);
					}
				}));
			}

			while (!done) {
				GUI.Window currentPage = gui.addWindow(6, "Selector #" + windowindex++ + " - " + waystone.Name);

				if (isOwner) {
					ItemStack customItem = new ItemStack(Material.PAPER);
					ItemMeta meta = customItem.getItemMeta();
					meta.setDisplayName("Customize Waypoint");
					customItem.setItemMeta(meta);
					GUI.Window.Button custom = currentPage.addButton(4, 5, customItem);
					custom.addAction(ClickType.LEFT, new ButtonAction.PAGE_GOTO(custom, customizeGUI.getWindow(0)));
				}

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
					if (item.equals(waystone)) continue;

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
					int dx = item.x - waystone.x;
					int dy = item.y - waystone.y;
					int dz = item.z - waystone.z;
					long distance = Math.abs(Math.round(Math.sqrt(dx * dx + dy * dy + dz * dz)));
					long dist = distance - Main.plugin.getConfig().getInt("FreeTeleportRange", 150);
					int cost;
					if (dist <= 0) cost = 0;
					else cost = (int) Math.round(dist * Main.plugin.getConfig().getDouble("ExperienceCostPerBlock"));
					if (!waystone.worldname.equals(item.worldname)) cost += Main.plugin.getConfig().getInt("InterdimensionalTravelCost", 200);
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
						}));
					}
					index++;
				}

				if (wslist.isEmpty()) done = true;

			}
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
