package de.flo56958.warpstones.commands.subs;

import de.flo56958.warpstones.Utilities.LanguageManager;
import de.flo56958.warpstones.WarpstoneManager;
import de.flo56958.warpstones.commands.ArgumentType;
import de.flo56958.warpstones.commands.CommandManager;
import de.flo56958.warpstones.commands.SubCommand;
import de.flo56958.warpstones.gui.GUI;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Syntax of /ws listallwaypoints:
 * 		/ws listallwaypoints
 */
public class ListAllWarpstonesCommand implements SubCommand {

	@Override
	public boolean onCommand(@NotNull CommandSender sender, @NotNull String[] args) {
		if (sender instanceof Player) {
			GUI gui = WarpstoneManager.getInstance().createGUI(null, null, (Player) sender, null, true);
			gui.show((Player) sender);
			return true;
		}

		CommandManager.sendError(sender, LanguageManager.getString("Commands.Failure.Cause.PlayerOnlyCommand"));
		return true;
	}

	@Override
	public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull String[] args) {
		return null;
	}


	@Override
	@NotNull
	public String getName() {
		return "listallwarpstones";
	}

	@Override
	@NotNull
	public List<String> getAliases(boolean withName) {
		ArrayList<String> aliases = new ArrayList<>();
		if (withName) aliases.add(getName());
		aliases.add("law");
		aliases.add("la");
		return aliases;
	}

	@Override
	@NotNull
	public String getPermission() {
		return "warpstones.commands.listallwarpstones";
	}

	@Override
	@NotNull
	public Map<Integer, List<ArgumentType>> getArgumentsToParse() {
		return new HashMap<>();
	}

	@Override
	@NotNull
	public String syntax() {
		return "/ws listallwarpstones";
	}
}
