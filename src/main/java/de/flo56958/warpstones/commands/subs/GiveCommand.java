package de.flo56958.warpstones.commands.subs;

import de.flo56958.warpstones.Main;
import de.flo56958.warpstones.Utilities.LanguageManager;
import de.flo56958.warpstones.commands.ArgumentType;
import de.flo56958.warpstones.commands.CommandManager;
import de.flo56958.warpstones.commands.SubCommand;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.BlockState;
import org.bukkit.command.CommandSender;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

/**
 * Syntax of /ws give:
 * 		/ws give {Player} [Item]
 *
 * Legend:
 * 		{ }: not necessary
 * 		[ ]: necessary
 */
public class GiveCommand implements SubCommand {

	@Override
	public boolean onCommand(@NotNull CommandSender sender, @NotNull String[] args) {
		String item = null;
		Player player = null;

		if (args.length == 2) {
			item = args[1];
			if (sender instanceof Player) {
				player = (Player) sender;
			} else {
				CommandManager.sendError(sender, LanguageManager.getString("Commands.Failure.Cause.InvalidArguments"));
				return true;
			}
		} else if (args.length > 2) {
			item = args[2];
			player = Bukkit.getPlayer(args[1]);
		}

		if (player == null) {
			CommandManager.sendError(sender, LanguageManager.getString("Commands.Failure.Cause.PlayerMissing"));
			return true;
		}

		if (item.equalsIgnoreCase("warpstone")) {
			if (player.getInventory().addItem(Main.warpstoneItem.clone()).size() != 0) { //adds items to (full) inventory
				player.getWorld().dropItem(player.getLocation(), Main.warpstoneItem.clone());
			} // no else as it gets added in if
			return true;
		} else if (item.equalsIgnoreCase("warpscroll")) {
			if (player.getInventory().addItem(Main.warpscrollItem.clone()).size() != 0) { //adds items to (full) inventory
				player.getWorld().dropItem(player.getLocation(), Main.warpscrollItem.clone());
			} // no else as it gets added in if
			return true;
		} else if (item.equalsIgnoreCase("sword")) {
			ItemStack sword = new ItemStack(Material.DIAMOND_SWORD);
			sword.addUnsafeEnchantment(Enchantment.DAMAGE_ALL, 100);
			if (player.getInventory().addItem(sword).size() != 0) { //adds items to (full) inventory
				player.getWorld().dropItem(player.getLocation(), sword);
			} // no else as it gets added in if
			return true;
		}

		CommandManager.sendError(sender, LanguageManager.getString("Commands.Failure.Cause.InvalidArguments"));
		return true;
	}

	@Override
	public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull String[] args) {
		List<String> result = new ArrayList<>();
		switch (args.length) {
			case 2:
				for (Player player : Bukkit.getOnlinePlayers()) {
					result.add(player.getName());
				}
				result.add("@a");
				result.add("@r");

				if (sender instanceof Entity || sender instanceof BlockState) {
					result.add("@aw");
					result.add("@p");
					result.add("@rw");
				}
				break;
			case 3:
				result.add("warpstone");
				result.add("warpscroll");
				result.add("sword");
				break;
		}
		return result;
	}


	@Override
	@NotNull
	public String getName() {
		return "give";
	}

	@Override
	@NotNull
	public List<String> getAliases(boolean withName) {
		ArrayList<String> aliases = new ArrayList<>();
		if (withName) aliases.add(getName());
		aliases.add("g");
		return aliases;
	}

	@Override
	@NotNull
	public String getPermission() {
		return "warpstones.commands.give";
	}

	@Override
	@NotNull
	public Map<Integer, List<ArgumentType>> getArgumentsToParse() {
		Map<Integer, List<ArgumentType>> argumentsToParse = new HashMap<>();
		argumentsToParse.put(1, Collections.singletonList(ArgumentType.PLAYER));
		return argumentsToParse;
	}

	@Override
	@NotNull
	public String syntax() {
		return "/ws give {Player} [Material]";
	}
}
