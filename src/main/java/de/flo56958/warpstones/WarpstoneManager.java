package de.flo56958.warpstones;

import com.google.gson.Gson;
import de.flo56958.warpstones.Utilities.ChatWriter;
import de.flo56958.warpstones.Utilities.PlayerInfo;
import de.flo56958.warpstones.Utilities.SortingType;
import de.flo56958.warpstones.gui.ButtonAction;
import de.flo56958.warpstones.gui.GUI;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.entity.Shulker;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.io.*;
import java.util.*;

public class WarpstoneManager {

	public static final HashMap<String, Long> cooldowns = new HashMap<>();
	private static final HashMap<String, Long> playerInteractTimer = new HashMap<>();
	private final static ItemStack forwardStack;
	private final static ItemStack backStack;
	private static WarpstoneManager instance;

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
	//contains all Warpstones
	public List<Warpstone> warpstones;
	//contains all public Warpstones
	public List<Warpstone> globalWarpstones;
	//Maps a Player to his activated Warpstones
	public HashMap<String, PlayerSave> playerWarpstones;

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

	public synchronized static WarpstoneManager getInstance() {
		if (instance == null) {
			instance = new WarpstoneManager();
			instance.init();
		}
		return instance;
	}

	private void init() {
		warpstones = new ArrayList<>();
		globalWarpstones = new ArrayList<>();

		playerWarpstones = new HashMap<>();

		//load all Warpstones
		File dir = new File(Main.plugin.getDataFolder(), "saves/Warpstones");
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

						Warpstone way = gson.fromJson(json, Warpstone.class);
						warpstones.add(way);
						if (way.isGlobal) {
							globalWarpstones.add(way);
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
						playerWarpstones.put(child.getName().split("\\.")[0], save);
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
		}
	}

	public void save() {
		for (Warpstone warpstone : warpstones) {
			saveWarpstone(warpstone);
		}
		for (String uuid : playerWarpstones.keySet()) {
			savePlayer(uuid);
		}
	}

	public void activateWarpstone(Player p, Warpstone warpstone) {
		playerWarpstones.putIfAbsent(p.getUniqueId().toString(), new PlayerSave(new HashSet<>(), SortingType.ALPHABETICAL));
		playerWarpstones.get(p.getUniqueId().toString()).warpstones.add(warpstone.uuid);
	}

	public void toggleWarpstone(Player p, Warpstone warpstone) {
		playerWarpstones.putIfAbsent(p.getUniqueId().toString(), new PlayerSave(new HashSet<>(), SortingType.ALPHABETICAL));
		PlayerSave save = playerWarpstones.get(p.getUniqueId().toString());
		if (save.warpstones.contains(warpstone.uuid)) {
			if (warpstone.owner.equals(p.getUniqueId().toString()))
				return; //owner should always have the warpstone discovered
			save.warpstones.remove(warpstone.uuid);
			ChatWriter.sendActionBar(p, "Warpstone " + warpstone.Name + " has been undiscovered!");
		} else {
			save.warpstones.add(warpstone.uuid);
			ChatWriter.sendActionBar(p, "Warpstone " + warpstone.Name + " has been discovered!");
		}
	}

	public void addWarpstone(Warpstone warpstone) {
		saveWarpstone(warpstone);
		warpstones.add(warpstone);
		if (warpstone.isGlobal) {
			globalWarpstones.add(warpstone);
		}
	}

	public void removeWarpstone(Warpstone warpstone) {
		warpstones.remove(warpstone);
		globalWarpstones.remove(warpstone);
		File file = new File(Main.plugin.getDataFolder(), "/saves/Warpstones/" + warpstone.uuid + ".json");
		if (file.exists()) file.delete();
	}

	public void savePlayer(String uuid) {
		String str = gson.toJson(playerWarpstones.get(uuid), PlayerSave.class);
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

	public void saveWarpstone(Warpstone warpstone) {
		String str = gson.toJson(warpstone, Warpstone.class);
		try {
			File file = new File(Main.plugin.getDataFolder(), "/saves/Warpstones/" + warpstone.uuid + ".json");
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
        return PlayerInfo.getPlayerExp(p) >= Math.round(amount);
    }

	private void withdraw(Player p, double amount) {
		if (p.getGameMode() == GameMode.CREATIVE) return;
		p.giveExp(-(int) Math.round(amount));
	}

	public GUI createGUI(Warpstone warpstone, ItemStack warpscroll, Player p, Shulker shulker, boolean listall) {
		PlayerSave save = playerWarpstones.get(p.getUniqueId().toString());
		if (save == null) return null;

		Location from = p.getLocation();
		if (warpstone != null)
			from = new Location(Bukkit.getServer().getWorld(UUID.fromString(warpstone.worlduuid)), warpstone.x, warpstone.y, warpstone.z);
		//Gather all possible waypoints
		HashSet<Warpstone> ws;
		if (!listall) {
			ws = new HashSet<>(globalWarpstones);
			HashSet<String> toremove = new HashSet<>();

			loop:
			for (String s : save.warpstones) {
				for (Warpstone w : warpstones) {
					if (w.uuid.equals(s)) {
						ws.add(w);
						continue loop;
					}
				}
				toremove.add(s);
			}
			save.warpstones.removeAll(toremove);
		} else {
			ws = new HashSet<>(warpstones);
		}

		HashMap<Warpstone, Long> distances = new HashMap<>();
		HashMap<Warpstone, Double> costs = new HashMap<>();

		for (Warpstone w : ws) {
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

		ArrayList<Warpstone> wslist = new ArrayList<>(ws);
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

		GUI gui = new GUI(Main.plugin);
		int windowindex = 1;
		boolean done = false;
		boolean isOwner = warpstone != null && (warpstone.owner.equals(p.getUniqueId().toString())
				|| p.hasPermission("warpstones.admin"));
		GUI customizeGUI = null;
		if (isOwner) {
			customizeGUI = new GUI(Main.plugin);
			GUI.Window window = customizeGUI.addWindow(3, "Customizer - " + warpstone.Name);
			Warpstone finalWarpstone = warpstone;
			{
				//Rename Waypoint
				ItemStack nameritem = new ItemStack(Material.PAPER);
				ItemMeta meta = nameritem.getItemMeta();
				meta.setDisplayName("Rename Warpstone");
				ArrayList<String> lore = new ArrayList<>();
				lore.add(ChatColor.WHITE + "Send the Name in Chat!");
				meta.setLore(lore);
				nameritem.setItemMeta(meta);
				GUI.Window.Button namer = window.addButton(2, 1, nameritem);
				namer.addAction(ClickType.LEFT, new ButtonAction.REQUEST_INPUT(namer, (pl, input) -> {
					input = input.replaceAll("&", "§");
					//TODO: Naming Blacklist
					finalWarpstone.Name = input;
					if (shulker.isCustomNameVisible()) shulker.setCustomName(input);
				}, ""));
			}
			{
				//Set Waypoint Global
				ItemStack globalitem = (warpstone.isGlobal) ? new ItemStack(Material.GREEN_WOOL) : new ItemStack(Material.RED_WOOL);
				ItemMeta meta = globalitem.getItemMeta();
				meta.setDisplayName("Toggle Global Setting");
				List<String> lore = new ArrayList<>();
				lore.add(ChatColor.WHITE + "Should the warpstone be seen by everyone?");
				double cost = 0;
				if (!p.hasPermission("warpstones.gobal")) {
					cost = Main.plugin.getConfig().getDouble("MakeGlobalCost", 100);
					String scost = "Cost: ";
					scost += cost + " XP";
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
						finalWarpstone.isGlobal = true;
						WarpstoneManager.getInstance().globalWarpstones.add(finalWarpstone);
						finalGlobalitem.setType(Material.GREEN_WOOL);
						withdraw(p, finalCost);
					} else if (finalGlobalitem.getType() == Material.GREEN_WOOL) { //Toggle off
						finalWarpstone.isGlobal = false;
						WarpstoneManager.getInstance().globalWarpstones.remove(finalWarpstone);
						finalGlobalitem.setType(Material.RED_WOOL);
					}
				}));
			}
			{
				//Change Warpstone Color
				ItemStack coloritem = new ItemStack(Material.WHITE_WOOL);
				ItemMeta meta = coloritem.getItemMeta();
				meta.setDisplayName("Change Warpstone Color");
				coloritem.setItemMeta(meta);
				GUI.Window.Button color = window.addButton(6, 1, coloritem);
				color.addAction(ClickType.LEFT, new ButtonAction.PAGE_UP(color));

				GUI.Window colorWindow = customizeGUI.addWindow(2, "Choose Color for " + warpstone.Name);
				int index = 0;
				ArrayList<DyeColor> colors = new ArrayList(Arrays.asList(DyeColor.values()));
				colors.add(null);
				for (DyeColor c : colors) {
					ItemStack col = WarpstoneManager.getInstance().getShulkerboxFromColor(c);
					meta = col.getItemMeta();
					if (c != null) meta.setDisplayName(c.name());
					else meta.setDisplayName("NORMAL");
					col.setItemMeta(meta);
					GUI.Window.Button but = colorWindow.addButton(index++, col);
					but.addAction(ClickType.LEFT, new ButtonAction.RUN_RUNNABLE(but, () -> {
						shulker.setColor(c);
						finalWarpstone.color = c;
					}));
				}
			}
			{
				//Remove Warpstone
				ItemStack removeItem = new ItemStack(Material.BEDROCK);
				ItemMeta meta = removeItem.getItemMeta();
				meta.setDisplayName("Remove Warpstone");
				removeItem.setItemMeta(meta);
				GUI.Window.Button remove = window.addButton(5, 2, removeItem);
				GUI finalCustomizeGUI = customizeGUI;
				remove.addAction(ClickType.LEFT, new ButtonAction.RUN_RUNNABLE(remove, () -> {
					WarpstoneManager.getInstance().removeWarpstone(finalWarpstone);
					shulker.getWorld().dropItemNaturally(shulker.getLocation(), Main.warpstoneItem);
					shulker.remove();
					finalCustomizeGUI.close();
				}));
			}
			{
				//Transfer Ownership
				ItemStack owneritem = new ItemStack(Material.BLACK_WOOL);
				ItemMeta meta = owneritem.getItemMeta();
				meta.setDisplayName("Transfer Ownership");
				List<String> lore = new ArrayList<>();
				lore.add(ChatColor.WHITE + "Send the Name of the new Owner in Chat!");
				meta.setLore(lore);
				owneritem.setItemMeta(meta);
				GUI.Window.Button owner = window.addButton(3, 2, owneritem);
				owner.addAction(ClickType.LEFT, new ButtonAction.REQUEST_INPUT(owner, (pl, input) -> {
					Player player = Bukkit.getServer().getPlayer(input);
					if (player == null) {
						pl.sendMessage(ChatColor.RED + "Player was not found or not online!");
					} else {
						if (!player.hasPermission("warpstones.place")) {
							pl.sendMessage(ChatColor.RED + player.getDisplayName() + "does not have the necessary permissions!");
							return;
						}
						finalWarpstone.owner = player.getUniqueId().toString();
						pl.sendMessage(ChatColor.WHITE + "Transferred successfully to " + input + "!");
						player.sendMessage(ChatColor.WHITE + "You have been given ownership of the warpstone "
								+ finalWarpstone.Name + " from " + pl.getName() + "!");
						WarpstoneManager.getInstance().activateWarpstone(player, finalWarpstone);
					}
				}, ""));
			}
			{
				//Show Nametag
				ItemStack nametagitem = (shulker.isCustomNameVisible()) ? new ItemStack(Material.GREEN_WOOL) : new ItemStack(Material.RED_WOOL);
				ItemMeta meta = nametagitem.getItemMeta();
				meta.setDisplayName("Toggle Nametag Visibility");
				List<String> lore = new ArrayList<>();
				lore.add(ChatColor.WHITE + "Should the warpstone have a Nametag?");
				meta.setLore(lore);
				nametagitem.setItemMeta(meta);
				GUI.Window.Button nametag = window.addButton(4, 0, nametagitem);
				ItemStack finalNametagItem = nametag.getItemStack();
				nametag.addAction(ClickType.LEFT, new ButtonAction.RUN_RUNNABLE(nametag, () -> {
					if (finalNametagItem.getType() == Material.RED_WOOL) { //Toggle on
						shulker.setCustomNameVisible(true);
						shulker.setCustomName(finalWarpstone.Name);
						finalNametagItem.setType(Material.GREEN_WOOL);
					} else if (finalNametagItem.getType() == Material.GREEN_WOOL) { //Toggle off
						shulker.setCustomNameVisible(false);
						shulker.setCustomName(null);
						finalNametagItem.setType(Material.RED_WOOL);
					}
				}));
			}
			{
				//Lock Warpstone
				ItemStack lockitem = (warpstone.locked) ? new ItemStack(Material.GREEN_WOOL) : new ItemStack(Material.RED_WOOL);
				ItemMeta meta = lockitem.getItemMeta();
				meta.setDisplayName("Toggle Locking of Warpstone");
				List<String> lore = new ArrayList<>();
				lore.add(ChatColor.WHITE + "Should the Warpstone be inaccessible for everyone?");
				meta.setLore(lore);
				lockitem.setItemMeta(meta);
				GUI.Window.Button lock = window.addButton(0, 2, lockitem);
				ItemStack finalNametagItem = lock.getItemStack();
				lock.addAction(ClickType.LEFT, new ButtonAction.RUN_RUNNABLE(lock, () -> {
					if (finalNametagItem.getType() == Material.RED_WOOL) { //Toggle on
						finalNametagItem.setType(Material.GREEN_WOOL);
						finalWarpstone.locked = true;
					} else if (finalNametagItem.getType() == Material.GREEN_WOOL) { //Toggle off
						finalNametagItem.setType(Material.RED_WOOL);
						finalWarpstone.locked = false;
					}
				}));
			}
		}

		//TODO: add option for custom ItemType in Menu

		while (!done) {
			String title = "Selector #" + windowindex++ + " - ";
			title += (warpstone == null) ? "Warpscroll" : warpstone.Name;
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
				Warpstone item = wslist.get(0);
				wslist.remove(0);
				if (item == null) continue;
				if (item.equals(warpstone)) continue;

				ItemStack stack = WarpstoneManager.getInstance().getShulkerboxFromColor(item.color);
				ItemMeta meta = stack.getItemMeta();
				meta.setDisplayName(item.Name);
				List<String> lore = new ArrayList<>();
				String playername = Bukkit.getOfflinePlayer(UUID.fromString(item.owner)).getName();
				lore.add(ChatColor.WHITE + "Owner: " + playername);
				if (item.isGlobal) lore.add(ChatColor.WHITE + "Global Warpstone");
				lore.add(ChatColor.WHITE + "World: " + Bukkit.getServer().getWorld(UUID.fromString(item.worlduuid)).getName());
				lore.add(ChatColor.WHITE + "Location: " + item.x + " " + item.y + " " + item.z);

				double cost = costs.get(item);
				long distance = distances.get(item);
				boolean canTeleport = canAfford(p, cost);
				lore.add(ChatColor.WHITE + "Distance: " + distance + " Blocks");
				String ec = " XP";
				String c = String.valueOf(Math.round(cost));
				lore.add(((canTeleport) ? ChatColor.WHITE : ChatColor.RED) + "Cost to teleport: " + c + ec);

				//check for locked Warpstone
				if(item.locked && !(item.owner.equals(p.getUniqueId().toString())
						|| p.hasPermission("warpstones.admin"))) {
					canTeleport = false;
					lore.add(((item.isGlobal) ? 2 : 1), ChatColor.RED + "" + ChatColor.BOLD + "Locked Warpstone");
				}

				meta.setLore(lore);
				stack.setItemMeta(meta);

				GUI.Window.Button but = currentPage.addButton(index, stack);
				if (canTeleport) {
					if (warpstone != null || listall) {
						but.addAction(ClickType.LEFT, new ButtonAction.RUN_RUNNABLE(but, () -> {
							withdraw(p, cost);
							Location loc = new Location(Bukkit.getWorld(UUID.fromString(item.worlduuid)), item.x, item.y + 1, item.z);
							TeleportManager.initTeleportation(p, loc, Main.plugin.getConfig().getInt("WaypointTeleportTime", 0));
						}));
					} else {
						but.addAction(ClickType.LEFT, new ButtonAction.RUN_RUNNABLE(but, () -> {
							Location location = new Location(Bukkit.getWorld(UUID.fromString(item.worlduuid)), item.x, item.y + 1, item.z);
							TeleportManager.initTeleportation(p, location, Main.plugin.getConfig().getInt("WarpscrollTeleportTime", 5));
							if (p.getGameMode() != GameMode.CREATIVE) {
								withdraw(p, cost);
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

		Bukkit.getScheduler().runTaskLater(Main.plugin, gui::close, 5 * 60 * 20);
		return gui;
	}
}
