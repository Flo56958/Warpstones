package de.flo56958.warpstones.Utilities;

import de.flo56958.warpstones.Main;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;
import java.util.logging.Level;

public class ChatWriter {

	private final static TreeMap<Integer, String> map = new TreeMap<>();
	public static String CHAT_PREFIX;

	static {
		CHAT_PREFIX = Main.plugin.getConfig().getString("chat-prefix");
		map.put(1000000, "%BOLD%%UNDERLINE%M%RESET%");
		map.put(500000, "%BOLD%%UNDERLINE%D%RESET%");
		map.put(100000, "%BOLD%%UNDERLINE%C%RESET%");
		map.put(50000, "%BOLD%%UNDERLINE%L%RESET%");
		map.put(10000, "%BOLD%%UNDERLINE%X%RESET%");
		map.put(5000, "%BOLD%%UNDERLINE%V%RESET%");
		map.put(1000, "M");
		map.put(900, "CM");
		map.put(500, "D");
		map.put(400, "CD");
		map.put(100, "C");
		map.put(90, "XC");
		map.put(50, "L");
		map.put(40, "XL");
		map.put(10, "X");
		map.put(9, "IX");
		map.put(5, "V");
		map.put(4, "IV");
		map.put(1, "I");
	}

	public static void reload() {
		CHAT_PREFIX = Main.plugin.getConfig().getString("chat-prefix");
	}

	/**
	 * Sends a chat message
	 *
	 * @param receiver
	 * @param color    The ChatColor after the CHAT_PREFIX
	 * @param message
	 */
	public static void sendMessage(CommandSender receiver, ChatColor color, String message) {
		if (Main.plugin.getConfig().getBoolean("chat-messages")) {
			receiver.sendMessage(CHAT_PREFIX + " " + color + message);
		}
	}

	/**
	 * Logs severe errors. (not toggleable)
	 *
	 * @param message
	 */
	public static void logError(String message) {
		Bukkit.getLogger().log(Level.SEVERE, CHAT_PREFIX + " " + message);
	}

	/**
	 * Logs information. (not toggleable)
	 *
	 * @param message
	 */
	public static void logInfo(String message) {
		Bukkit.getLogger().log(Level.INFO, CHAT_PREFIX + " " + message);
	}

	/**
	 * Logs information with the ability to have text color (not toggleable)
	 *
	 * @param message
	 */
	public static void logColor(String message) {
		Bukkit.getConsoleSender().sendMessage(CHAT_PREFIX + " " + message);
	}

	public static String addColors(@NotNull String input) {
		input = input.replaceAll("%BLACK%", ChatColor.BLACK.toString());
		input = input.replaceAll("%DARK_BLUE%", ChatColor.DARK_BLUE.toString());
		input = input.replaceAll("%DARK_GREEN%", ChatColor.DARK_GREEN.toString());
		input = input.replaceAll("%DARK_AQUA%", ChatColor.DARK_AQUA.toString());
		input = input.replaceAll("%DARK_RED%", ChatColor.DARK_RED.toString());
		input = input.replaceAll("%DARK_PURPLE%", ChatColor.DARK_PURPLE.toString());
		input = input.replaceAll("%GOLD%", ChatColor.GOLD.toString());
		input = input.replaceAll("%GRAY%", ChatColor.GRAY.toString());
		input = input.replaceAll("%DARK_GRAY%", ChatColor.DARK_GRAY.toString());
		input = input.replaceAll("%BLUE%", ChatColor.BLUE.toString());
		input = input.replaceAll("%GREEN%", ChatColor.GREEN.toString());
		input = input.replaceAll("%AQUA%", ChatColor.AQUA.toString());
		input = input.replaceAll("%RED%", ChatColor.RED.toString());
		input = input.replaceAll("%LIGHT_PURPLE%", ChatColor.LIGHT_PURPLE.toString());
		input = input.replaceAll("%YELLOW%", ChatColor.YELLOW.toString());
		input = input.replaceAll("%WHITE%", ChatColor.WHITE.toString());
		input = input.replaceAll("%BOLD%", ChatColor.BOLD.toString());
		input = input.replaceAll("%UNDERLINE%", ChatColor.UNDERLINE.toString());
		input = input.replaceAll("%ITALIC%", ChatColor.ITALIC.toString());
		input = input.replaceAll("%STRIKE%", ChatColor.STRIKETHROUGH.toString());
		input = input.replaceAll("%MAGIC%", ChatColor.MAGIC.toString());
		input = input.replaceAll("%RESET%", ChatColor.RESET.toString());

		return input;
	}

	public static ChatColor getColor(String input) {
		return ChatColor.valueOf(input.split("%")[1]);
	}

	public static List<String> splitString(String msg, int lineSize) {
		if (msg == null) return new ArrayList<>();
		List<String> res = new ArrayList<>();

		String[] str = msg.split(" ");
		int index = 0;
		while (index < str.length) {
			StringBuilder line = new StringBuilder();
			do {
				index++;
				line.append(str[index - 1]);
				line.append(" ");
			} while (index < str.length && line.length() + str[index].length() < lineSize);
			res.add(ChatColor.WHITE + line.toString().substring(0, line.length() - 1));
		}

		return res;
	}

	public static String getDisplayName(ItemStack tool) {
		String name;

		if (tool.getItemMeta() == null || !tool.getItemMeta().hasDisplayName()) {
			name = tool.getType().toString();
		} else {
			name = tool.getItemMeta().getDisplayName();
		}

		return name;
	}
}
