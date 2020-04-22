package de.flo56958.waystones;

import de.flo56958.waystones.Listeners.CraftingGridListener;
import de.flo56958.waystones.Listeners.WarpscrollListener;
import de.flo56958.waystones.Listeners.WaystoneListener;
import de.flo56958.waystones.Listeners.WorldSaveListener;
import de.flo56958.waystones.Utilities.NBT.NBTUtilitiesReflections;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

public final class Main extends JavaPlugin {

	public static JavaPlugin plugin;
	//TODO: Make give command
	public static ItemStack waystoneItem;
	public static ItemStack warpscrollItem;

	//TODO: Make command to give sword to instantly kill Waystones
	//TODO: Player should decide if XP or Vault

	public static boolean useVault = false;
	public static Economy econ = null;

	static {
		waystoneItem = new ItemStack(Material.SHULKER_SHELL);
		ItemMeta meta = waystoneItem.getItemMeta();
		meta.setDisplayName("Waystone");
		waystoneItem.setItemMeta(meta);
		NBTUtilitiesReflections nbts = new NBTUtilitiesReflections(waystoneItem);
		nbts.setInt("Waystone", 56958);
		NBTUtilitiesReflections.setNBTTagCompound(waystoneItem, nbts.getNBTTagCompound());

		warpscrollItem = new ItemStack(Material.PAPER);
		meta = warpscrollItem.getItemMeta();
		meta.setDisplayName("Warpscroll");
		warpscrollItem.setItemMeta(meta);
		nbts = new NBTUtilitiesReflections(warpscrollItem);
		nbts.setInt("Warpscroll", 56958);
		NBTUtilitiesReflections.setNBTTagCompound(warpscrollItem, nbts.getNBTTagCompound());
	}

	@Override
	public void onEnable() {
		plugin = this;
		//Setting up folder structure
		File file = new File(this.getDataFolder(), "saves/Waystones");
		file.mkdirs();
		file = new File(this.getDataFolder(), "saves/Players");
		file.mkdirs();

		//load config
		loadConfig();

		//Setting up WaystoneManager
		WaystoneManager.getInstance();

		//Setting up Listeners
		Bukkit.getPluginManager().registerEvents(new WaystoneListener(), this);
		if (getConfig().getBoolean("AutoSave", true)) Bukkit.getPluginManager().registerEvents(new WorldSaveListener(), this);
		Bukkit.getPluginManager().registerEvents(new CraftingGridListener(), this);

		//Setting up Crafting recipe
		FileConfiguration config = this.getConfig();
		{
			Map<String, String> recipeMaterials = new HashMap<>();
			recipeMaterials.put("S", Material.SHULKER_SHELL.name());
			recipeMaterials.put("N", Material.NETHER_STAR.name());

			config.addDefault("WaystoneRecipe.Materials", recipeMaterials);
		}
		{
			Map<String, String> recipeMaterials = new HashMap<>();
			recipeMaterials.put("S", Material.SHULKER_SHELL.name());
			recipeMaterials.put("P", Material.PAPER.name());

			config.addDefault("Warpscroll.Recipe.Materials", recipeMaterials);
		}
		saveConfig();
		try {
			NamespacedKey nkey = new NamespacedKey(Main.plugin, "Waystone");
			ShapedRecipe newRecipe = new ShapedRecipe(nkey, waystoneItem);
			String top = config.getString("WaystoneRecipe.Top");
			String middle = config.getString("WaystoneRecipe.Middle");
			String bottom = config.getString("WaystoneRecipe.Bottom");
			ConfigurationSection materials = config.getConfigurationSection("WaystoneRecipe.Materials");

			newRecipe.shape(top, middle, bottom);

			for (String key : materials.getKeys(false)) {
				newRecipe.setIngredient(key.charAt(0), Material.getMaterial(materials.getString(key)));
			}

			this.getServer().addRecipe(newRecipe);
		} catch (Exception e) {
			e.printStackTrace();
		}
		if (config.getBoolean("Warpscroll.Enabled", true)) {
			Bukkit.getPluginManager().registerEvents(new WarpscrollListener(), this);
			try {
				NamespacedKey nkey = new NamespacedKey(Main.plugin, "Warpscroll");
				ShapedRecipe newRecipe = new ShapedRecipe(nkey, warpscrollItem);
				String top = config.getString("Warpscroll.Recipe.Top");
				String middle = config.getString("Warpscroll.Recipe.Middle");
				String bottom = config.getString("Warpscroll.Recipe.Bottom");
				ConfigurationSection materials = config.getConfigurationSection("Warpscroll.Recipe.Materials");

				newRecipe.shape(top, middle, bottom);

				for (String key : materials.getKeys(false)) {
					newRecipe.setIngredient(key.charAt(0), Material.getMaterial(materials.getString(key)));
				}

				this.getServer().addRecipe(newRecipe);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		useVault = setupEconomy();
		if (useVault) Bukkit.getLogger().log(Level.INFO, "Found and enabled Vault!");
	}

	private boolean setupEconomy() {
		if (!(getConfig().getBoolean("UseVault", false))) return false;
		if (getServer().getPluginManager().getPlugin("Vault") == null) {
			return false;
		}
		RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
		if (rsp == null) {
			return false;
		}
		econ = rsp.getProvider();
		return econ != null;
	}

	private void loadConfig() {
		getConfig().options().copyDefaults(true);
		saveConfig();
	}

	public static void sendActionBar(Player player, String message) { //Extract from the source code of the Actionbar-API (altered)
		if (!player.isOnline()) {
			return; // Player may have logged out, unlikely but possible?
		}

		player.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(message));
	}

	@Override
	public void onDisable() {
		// Save all
		WaystoneManager.getInstance().save();
	}
}
