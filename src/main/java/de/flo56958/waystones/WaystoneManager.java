package de.flo56958.waystones;

import com.google.gson.Gson;
import org.bukkit.entity.Player;

import java.io.*;
import java.util.*;

public class WaystoneManager {

	private static WaystoneManager instance;

	private final Gson gson = new Gson();

	//contains all Waystones
	public List<Waystone> waystones;
	//contains all public Waystones
	public List<Waystone> globalWaystones;

	//Maps a Player to his activated Waystones
	public HashMap<String, HashSet<String>> playerWaystones;

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

						while(line != null){
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

						while(line != null){
							sb.append(line).append("\n");
							line = buf.readLine();
						}

						buf.close();
						is.close();

						String json = sb.toString();
						HashSet<String> list = (HashSet<String>) gson.fromJson(json, HashSet.class);
						playerWaystones.put(child.getName().split("\\.")[0], list);
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
		playerWaystones.putIfAbsent(p.getUniqueId().toString(), new HashSet<>());
		playerWaystones.get(p.getUniqueId().toString()).add(waystone.uuid);
	}

	public void toggleWaystone(Player p, Waystone waystone) {
		playerWaystones.putIfAbsent(p.getUniqueId().toString(), new HashSet<>());
		HashSet<String> set = playerWaystones.get(p.getUniqueId().toString());
		if (set.contains(waystone.uuid)) {
			if (waystone.owner.equals(p.getUniqueId().toString())) return; //owner should always have the waystone discovered
			set.remove(waystone.uuid);
			Main.sendActionBar(p, "Waystone " + waystone.Name + " has been undiscovered!");
		} else {
			set.add(waystone.uuid);
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
		String str = gson.toJson(playerWaystones.get(uuid), HashSet.class);
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
}
