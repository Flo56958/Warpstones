package de.flo56958.waystones;

import com.google.gson.Gson;
import de.flo56958.waystones.Utilities.PlayerInfo;
import de.flo56958.waystones.Utilities.SortingType;
import de.flo56958.waystones.gui.ButtonAction;
import de.flo56958.waystones.gui.GUI;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.entity.Shulker;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.io.*;
import java.util.*;

public class WaystoneManager {

	public static final HashMap<String, Long> cooldowns = new HashMap<>();
	private static final HashMap<String, Long> playerInteractTimer = new HashMap<>();
	private final static ItemStack forwardStack;
	private final static ItemStack backStack;
	private static WaystoneManager instance;

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

	private final Gson gson = new Gson();
	//contains all Waystones
	public List<Waystone> waystones;
	//contains all public Waystones
	public List<Waystone> globalWaystones;
	//Maps a Player to his activated Waystones
	public HashMap<String, PlayerSave> playerWaystones;

	public static boolean checkInteractTimer(Player player) {
		Long time = playerInteractTimer.get(player.getUniqueId().toString());
		if (time == null) {
			playerInteractTimer.put(player.getUniqueId().toString(), System.currentTimeMillis());
		} else {
			long t = System.currentTimeMillis();
			if (t - time < 1000) return true;
			else playerInteractTimer.put(player.getUniqueId().toString(), t);
		}
		return false;
	}

	public synchronized static WaystoneManager getInstance() {
		if (instance == null) {
			instance = new WaystoneManager();
			instance.init();
		}
		return instance;
	}

	private void init() {
		waystones = new ArrayList<>();
		globalWaystones = new ArrayList<>();

		playerWaystones = new HashMap<>();

		//load all Waystones
		File dir = new File(Main.plugin.getDataFolder(), "saves/Waystones");
		File[] directoryListing = dir.listFiles();
		if (directoryListing != null) {
			for (File child : directoryListing) {
				if (child.isFile()) {
					try {
						InputStream is = new FileInputStream(child);
						BufferedReader buf = new BufferedReader(new InputStreamReader(is));

						String line = buf.readLine();
						StringBuilder sb = new StringBuilder();

						while (line != null) {
							sb.append(line).append("\n");
							line = buf.readLine();
						}

						buf.close();
						is.close();

						String json = sb.toString();

						Waystone way = gson.fromJson(json, Waystone.class);
						waystones.add(way);
						if (way.isGlobal) {
							globalWaystones.add(way);
						}
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
		}

		//load all Playerdata
		dir = new File(Main.plugin.getDataFolder(), "saves/Players");
		directoryListing = dir.listFiles();
		if (directoryListing != null) {
			for (File child : directoryListing) {
				if (child.isFile()) {
					try {
						InputStream is = new FileInputStream(child);
						BufferedReader buf = new BufferedReader(new InputStreamReader(is));

						String line = buf.readLine();
						StringBuilder sb = new StringBuilder();

						while (line != null) {
							sb.append(line).append("\n");
							line = buf.readLine();
						}

						buf.close();
						is.close();

						String json = sb.toString();
						PlayerSave save = gson.fromJson(json, PlayerSave.class);
						playerWaystones.put(child.getName().split("\\.")[0], save);
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
		}
	}

	public void save() {
		for (Waystone waystone : waystones) {
			saveWaystone(waystone);
		}
		for (String uuid : playerWaystones.keySet()) {
			savePlayer(uuid);
		}
	}

	public void activateWaystone(Player p, Waystone waystone) {
		playerWaystones.putIfAbsent(p.getUniqueId().toString(), new PlayerSave(new HashSet<>(), SortingType.ALPHABETICAL));
		playerWaystones.get(p.getUniqueId().toString()).waystones.add(waystone.uuid);
	}

	public void toggleWaystone(Player p, Waystone waystone) {
		playerWaystones.putIfAbsent(p.getUniqueId().toString(), new PlayerSave(new HashSet<>(), SortingType.ALPHABETICAL));
		PlayerSave save = playerWaystones.get(p.getUniqueId().toString());
		if (save.waystones.contains(waystone.uuid)) {
			if (waystone.owner.equals(p.getUniqueId().toString()))
				return; //owner should always have the waystone discovered
			save.waystones.remove(waystone.uuid);
			Main.sendActionBar(p, "Waystone " + waystone.Name + " has been undiscovered!");
		} else {
			save.waystones.add(waystone.uuid);
			Main.sendActionBar(p, "Waystone " + waystone.Name + " has been discovered!");
		}
	}

	public void addWaystone(Waystone waystone) {
		saveWaystone(waystone);
		waystones.add(waystone);
		if (waystone.isGlobal) {
			globalWaystones.add(waystone);
		}
	}

	public void removeWaystone(Waystone waystone) {
		waystones.remove(waystone);
		globalWaystones.remove(waystone);
		File file = new File(Main.plugin.getDataFolder(), "/saves/Waystones/" + waystone.uuid + ".json");
		if (file.exists()) file.delete();
	}

	public void savePlayer(String uuid) {
		String str = gson.toJson(playerWaystones.get(uuid), PlayerSave.class);
		try {
			File file = new File(Main.plugin.getDataFolder(), "/saves/Players/" + uuid + ".json");
			if (!file.exists()) {
				file.createNewFile();
			}
			BufferedWriter writer = new BufferedWriter(new FileWriter(file));
			writer.write(str);
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void saveWaystone(Waystone waystone) {
		String str = gson.toJson(waystone, Waystone.class);
		try {
			File file = new File(Main.plugin.getDataFolder(), "/saves/Waystones/" + waystone.uuid + ".json");
			if (!file.exists()) {
				file.createNewFile();
			}
			BufferedWriter writer = new BufferedWriter(new FileWriter(file));
			writer.write(str);
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public ItemStack getShulkerboxFromColor(DyeColor color) {
		ItemStack stack;
		if (color == DyeColor.WHITE) {
			stack = new ItemStack(Material.WHITE_SHULKER_BOX);
		} else if (color == DyeColor.ORANGE) {
			stack = new ItemStack(Material.ORANGE_SHULKER_BOX);
		} else if (color == DyeColor.MAGENTA) {
			stack = new ItemStack(Material.MAGENTA_SHULKER_BOX);
		} else if (color == DyeColor.LIGHT_BLUE) {
			stack = new ItemStack(Material.LIGHT_BLUE_SHULKER_BOX);
		} else if (color == DyeColor.YELLOW) {
			stack = new ItemStack(Material.YELLOW_SHULKER_BOX);
		} else if (color == DyeColor.LIME) {
			stack = new ItemStack(Material.LIME_SHULKER_BOX);
		} else if (color == DyeColor.PINK) {
			stack = new ItemStack(Material.PINK_SHULKER_BOX);
		} else if (color == DyeColor.GRAY) {
			stack = new ItemStack(Material.GRAY_SHULKER_BOX);
		} else if (color == DyeColor.LIGHT_GRAY) {
			stack = new ItemStack(Material.LIGHT_GRAY_SHULKER_BOX);
		} else if (color == DyeColor.CYAN) {
			stack = new ItemStack(Material.CYAN_SHULKER_BOX);
		} else if (color == DyeColor.PURPLE) {
			stack = new ItemStack(Material.PURPLE_SHULKER_BOX);
		} else if (color == DyeColor.BLUE) {
			stack = new ItemStack(Material.BLUE_SHULKER_BOX);
		} else if (color == DyeColor.BROWN) {
			stack = new ItemStack(Material.BROWN_SHULKER_BOX);
		} else if (color == DyeColor.GREEN) {
			stack = new ItemStack(Material.GREEN_SHULKER_BOX);
		} else if (color == DyeColor.RED) {
			stack = new ItemStack(Material.RED_SHULKER_BOX);
		} else if (color == DyeColor.BLACK) {
			stack = new ItemStack(Material.BLACK_SHULKER_BOX);
		} else {
			stack = new ItemStack(Material.SHULKER_BOX);
		}

		return stack;
	}

	private boolean canAfford(Player p, double amount) {
		if (p.getGameMode() == GameMode.CREATIVE) return true;
		if (Main.useVault) {
			if (!Main.econ.has(p, amount)) {
				return false;
			}
		} else {
			if (PlayerInfo.getPlayerExp(p) < Math.round(amount)) {
				return false;
			}
		}
		return true;
	}

	private void withdraw(Player p, double amount) {
		if (p.getGameMode() == GameMode.CREATIVE) return;
		if (Main.useVault) {
			Main.econ.withdrawPlayer(p, amount);
		} else {
			p.giveExp(-(int) Math.round(amount));
		}
	}

	public GUI createGUI(Waystone waystone, ItemStack warpscroll, Player p, Shulker shulker, boolean listall) {
		PlayerSave save = playerWaystones.get(p.getUniqueId().toString());
		if (save == null) return null;

		Location from = p.getLocation();
		if (waystone != null)
			from = new Location(Bukkit.getServer().getWorld(UUID.fromString(waystone.worlduuid)), waystone.x, waystone.y, waystone.z);
		//Gather all possible waypoints
		HashSet<Waystone> ws;
		if (!listall) {
			ws = new HashSet<>(globalWaystones);
			HashSet<String> toremove = new HashSet<>();

			loop:
			for (String s : save.waystones) {
				for (Waystone w : waystones) {
					if (w.uuid.equals(s)) {
						ws.add(w);
						continue loop;
					}
				}
				toremove.add(s);
			}
			save.waystones.removeAll(toremove);
		} else {
			ws = new HashSet<>(waystones);
		}

		HashMap<Waystone, Long> distances = new HashMap<>();
		HashMap<Waystone, Double> costs = new HashMap<>();

		for (Waystone w : ws) {
			//calculate distance
			int dx = w.x - from.getBlockX();
			int dy = w.y - from.getBlockY();
			int dz = w.z - from.getBlockZ();
			long distance = Math.abs(Math.round(Math.sqrt(dx * dx + dy * dy + dz * dz)));
			distances.put(w, distance);

			//calculate cost
			long dist = distance - Main.plugin.getConfig().getInt("FreeTeleportRange", 150);
			double cost;
			if (dist <= 0) cost = 0;
			else cost = dist * Main.plugin.getConfig().getDouble("CostPerBlock");
			if (!from.getWorld().getUID().toString().equals(w.worlduuid))
				cost += Main.plugin.getConfig().getDouble("InterdimensionalTravelCost", 200);
			costs.put(w, cost);
		}

		ArrayList<Waystone> wslist = new ArrayList<>(ws);
		switch (save.sortingType) {
			case ALPHABETICAL:
				wslist.sort(Comparator.comparing(x -> x.Name));
				break;
			case DISTANCE:
				wslist.sort(Comparator.comparing(distances::get));
				break;
			case COST:
				wslist.sort(Comparator.comparing(costs::get));
				break;
			case RANDOM:
				Collections.shuffle(wslist);
				break;
		}

		int size = wslist.size();

		GUI gui = new GUI();
		int windowindex = 1;
		boolean done = false;
		boolean isOwner = waystone != null && (waystone.owner.equals(p.getUniqueId().toString())
				|| p.hasPermission("waystones.admin"));
		GUI customizeGUI = null;
		if (isOwner) {
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
			namer.addAction(ClickType.LEFT, new ButtonAction.REQUEST_INPUT(namer, (pl, input) -> {
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
			double cost = 0;
			if (!p.hasPermission("waystones.gobal")) {
				cost = Main.plugin.getConfig().getDouble("MakeGlobalCost", 100);
				String scost = "Cost: ";
				if (Main.useVault) {
					scost += Main.econ.format(cost);
				} else {
					scost += cost + " XP";
				}
				boolean canPurchase = canAfford(p, cost);
				ChatColor color = (canPurchase) ? ChatColor.WHITE : ChatColor.RED;

				lore.add(color + scost);
				lore.add("The cost is only necessary on activation.");
			}
			meta.setLore(lore);
			globalitem.setItemMeta(meta);
			GUI.Window.Button global = window.addButton(4, 1, globalitem);
			ItemStack finalGlobalitem = global.getItemStack();
			double finalCost = cost;
			global.addAction(ClickType.LEFT, new ButtonAction.RUN_RUNNABLE(global, () -> {
				if (finalGlobalitem.getType() == Material.RED_WOOL) { //Toggle on
					finalWaystone.isGlobal = true;
					WaystoneManager.getInstance().globalWaystones.add(finalWaystone);
					finalGlobalitem.setType(Material.GREEN_WOOL);
					withdraw(p, finalCost);
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
				ItemStack col = WaystoneManager.getInstance().getShulkerboxFromColor(c);
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
			owner.addAction(ClickType.LEFT, new ButtonAction.REQUEST_INPUT(owner, (pl, input) -> {
				Player player = Bukkit.getServer().getPlayer(input);
				if (player == null) {
					pl.sendMessage(ChatColor.RED + "Player was not found or not online!");
				} else {
					if (!player.hasPermission("waystones.place")) {
						pl.sendMessage(ChatColor.RED + player.getDisplayName() + "does not have the necessary permissions!");
						return;
					}
					finalWaystone.owner = player.getUniqueId().toString();
					pl.sendMessage(ChatColor.WHITE + "Transferred successfully to " + input + "!");
					player.sendMessage(ChatColor.WHITE + "You have been given ownership of the waypoint "
							+ finalWaystone.Name + " from " + pl.getName() + "!");
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

		//TODO: add option for custom ItemType in Menu

		while (!done) {
			String title = "Selector #" + windowindex++ + " - ";
			title += (waystone == null) ? "Warpscroll" : waystone.Name;
			GUI.Window currentPage = gui.addWindow(6, title);

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

			{
				ItemStack sortStack = new ItemStack(Material.PAPER);
				ItemMeta meta = sortStack.getItemMeta();
				meta.setDisplayName(save.sortingType.getDisplayName());
				meta.setLore(Collections.singletonList(ChatColor.WHITE + "Effect only after Interface reopening"));
				sortStack.setItemMeta(meta);

				GUI.Window.Button sort = currentPage.addButton(7, 5, sortStack);
				ItemStack finalsortStack = sort.getItemStack();
				sort.addAction(ClickType.LEFT, new ButtonAction.RUN_RUNNABLE(sort, () -> {
					save.sortingType = save.sortingType.getNext();
					ItemMeta m = finalsortStack.getItemMeta();
					m.setDisplayName(save.sortingType.getDisplayName());
					finalsortStack.setItemMeta(m);
				}));
			}

			int index = 0;
			while (!wslist.isEmpty() && index < 45) {
				Waystone item = wslist.get(0);
				wslist.remove(0);
				if (item == null) continue;
				if (item.equals(waystone)) continue;

				ItemStack stack = WaystoneManager.getInstance().getShulkerboxFromColor(item.color);
				ItemMeta meta = stack.getItemMeta();
				meta.setDisplayName(item.Name);
				List<String> lore = new ArrayList<>();
				String playername = Bukkit.getOfflinePlayer(UUID.fromString(item.owner)).getName();
				lore.add(ChatColor.WHITE + "Owner: " + playername);
				if (item.isGlobal) lore.add(ChatColor.WHITE + "Global Waystone");
				lore.add(ChatColor.WHITE + "World: " + Bukkit.getServer().getWorld(UUID.fromString(item.worlduuid)).getName());
				lore.add(ChatColor.WHITE + "Location: " + item.x + " " + item.y + " " + item.z);

				double cost = costs.get(item);
				long distance = distances.get(item);
				boolean canTeleport = canAfford(p, cost);
				ChatColor color = (canTeleport) ? ChatColor.WHITE : ChatColor.RED;
				lore.add(ChatColor.WHITE + "Distance: " + distance + " Blocks");
				String ec = "XP";
				String c = String.valueOf(Math.round(cost));
				if (Main.useVault) {
					ec = "";
					c = Main.econ.format(cost);
				}
				lore.add(color + "Cost to teleport: " + c + " " + ec);
				meta.setLore(lore);
				stack.setItemMeta(meta);

				GUI.Window.Button but = currentPage.addButton(index, stack);
				if (canTeleport) {
					double finalCost = cost;
					if (waystone != null || listall) {
						but.addAction(ClickType.LEFT, new ButtonAction.RUN_RUNNABLE(but, () -> {
							withdraw(p, finalCost);
							Location loc = new Location(Bukkit.getWorld(UUID.fromString(item.worlduuid)), item.x, item.y + 1, item.z);
							TeleportManager.initTeleportation(p, loc, Main.plugin.getConfig().getInt("WaypointTeleportTime", 0));
						}));
					} else {
						but.addAction(ClickType.LEFT, new ButtonAction.RUN_RUNNABLE(but, () -> {
							Location location = new Location(Bukkit.getWorld(UUID.fromString(item.worlduuid)), item.x, item.y + 1, item.z);
							TeleportManager.initTeleportation(p, location, Main.plugin.getConfig().getInt("WarpscrollTeleportTime", 5));
							if (p.getGameMode() != GameMode.CREATIVE) {
								withdraw(p, finalCost);
								cooldowns.put(p.getUniqueId().toString(), System.currentTimeMillis());
								if (Main.plugin.getConfig().getBoolean("Warpscroll.RemoveAfterUse", false)) {
									warpscroll.setAmount(warpscroll.getAmount() - 1);
								}
							}
						}));
					}
				}
				index++;
			}

			if (wslist.isEmpty()) done = true;
		}

		return gui;
	}
}
