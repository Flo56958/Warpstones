package de.flo56958.waystones;

import de.flo56958.waystones.Listeners.WarpscrollListener;
import de.flo56958.waystones.Listeners.WaystoneListener;
import de.flo56958.waystones.Listeners.WorldSaveListener;
import de.flo56958.waystones.Utilities.NBT.NBTUtilitiesReflections;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;

public final class Main extends JavaPlugin {

	public static JavaPlugin plugin;
	public static ItemStack waystoneItem;
	public static ItemStack warpscrollItem;

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
		Bukkit.getPluginManager().registerEvents(new WorldSaveListener(), this);
		Bukkit.getPluginManager().registerEvents(new WarpscrollListener(), this);

		//Setting up Crafting recipe
		//TODO: Change recipe
		try {
			NamespacedKey nkey = new NamespacedKey(this, "Waystone");
			ShapedRecipe newRecipe = new ShapedRecipe(nkey, waystoneItem); //init recipe

			newRecipe.shape("   ", " S ", "   "); //makes recipe
			newRecipe.setIngredient('S', Material.SHULKER_SHELL);
			this.getServer().addRecipe(newRecipe); //adds recipe
		} catch (Exception e) {
			e.printStackTrace();
		}

		try {
			NamespacedKey nkey = new NamespacedKey(this, "Warpscroll");
			ShapedRecipe newRecipe = new ShapedRecipe(nkey, warpscrollItem); //init recipe

			newRecipe.shape("   ", " P ", "   "); //makes recipe
			newRecipe.setIngredient('P', Material.PAPER);
			this.getServer().addRecipe(newRecipe); //adds recipe
		} catch (Exception e) {
			e.printStackTrace();
		}
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
