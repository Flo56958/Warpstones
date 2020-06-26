package de.flo56958.warpstones;

import de.flo56958.warpstones.Listeners.CraftingGridListener;
import de.flo56958.warpstones.Listeners.WarpscrollListener;
import de.flo56958.warpstones.Listeners.WarpstoneListener;
import de.flo56958.warpstones.Listeners.WorldSaveListener;
import de.flo56958.warpstones.Utilities.LanguageManager;
import de.flo56958.warpstones.commands.CommandManager;
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
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

public final class Main extends JavaPlugin {

	public static JavaPlugin plugin;
	public static ItemStack warpstoneItem;
	public static ItemStack warpscrollItem;

	//TODO: Player should decide if XP or Vault

	public static boolean useVault = false;
	public static Economy econ = null;

	@Override
	public void onEnable() {
		plugin = this;
		//Setting up folder structure
		File file = new File(this.getDataFolder(), "saves/Warpstones");
		file.mkdirs();
		file = new File(this.getDataFolder(), "saves/Players");
		file.mkdirs();

		//load config
		loadConfig();
		LanguageManager.reload();

		warpstoneItem = new ItemStack(Material.SHULKER_SHELL);
		ItemMeta meta = warpstoneItem.getItemMeta();
		meta.setDisplayName("Warpstone");
		PersistentDataContainer persistentDataContainer = meta.getPersistentDataContainer();
		persistentDataContainer.set(new NamespacedKey(Main.plugin, "Warpstone"), PersistentDataType.INTEGER, 56958);
		warpstoneItem.setItemMeta(meta);


		warpscrollItem = new ItemStack(Material.PAPER);
		meta = warpscrollItem.getItemMeta();
		meta.setDisplayName("Warpscroll");
		persistentDataContainer = meta.getPersistentDataContainer();
		persistentDataContainer.set(new NamespacedKey(Main.plugin, "Warpscroll"), PersistentDataType.INTEGER, 56958);
		warpscrollItem.setItemMeta(meta);

		useVault = setupEconomy();
		if (useVault) Bukkit.getLogger().log(Level.INFO, "Found and enabled Vault!");

		//Setting up WarpstoneManager
		WarpstoneManager.getInstance();

		//Setting up Commands
		CommandManager cmd = new CommandManager();
		this.getCommand("warpstones").setExecutor(cmd);
		this.getCommand("warpstones").setTabCompleter(cmd);

		//Setting up Listeners
		Bukkit.getPluginManager().registerEvents(new WarpstoneListener(), this);
		if (getConfig().getBoolean("AutoSave", true)) Bukkit.getPluginManager().registerEvents(new WorldSaveListener(), this);
		Bukkit.getPluginManager().registerEvents(new CraftingGridListener(), this);

		//Setting up Crafting recipe
		FileConfiguration config = this.getConfig();
		{
			Map<String, String> recipeMaterials = new HashMap<>();
			recipeMaterials.put("S", Material.SHULKER_SHELL.name());
			recipeMaterials.put("N", Material.NETHER_STAR.name());

			config.addDefault("WarpstoneRecipe.Materials", recipeMaterials);
		}
		{
			Map<String, String> recipeMaterials = new HashMap<>();
			recipeMaterials.put("S", Material.SHULKER_SHELL.name());
			recipeMaterials.put("P", Material.PAPER.name());

			config.addDefault("Warpscroll.Recipe.Materials", recipeMaterials);
		}
		saveConfig();
		loadConfig();
		try {
			NamespacedKey nkey = new NamespacedKey(Main.plugin, "Warpstone");
			ShapedRecipe newRecipe = new ShapedRecipe(nkey, warpstoneItem);
			String top = config.getString("WarpstoneRecipe.Top");
			String middle = config.getString("WarpstoneRecipe.Middle");
			String bottom = config.getString("WarpstoneRecipe.Bottom");
			ConfigurationSection materials = config.getConfigurationSection("WarpstoneRecipe.Materials");

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
		WarpstoneManager.getInstance().save();
	}
}
