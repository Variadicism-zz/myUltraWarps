package REALDrummer;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;

public class UltraSwitch {

	private String warp_name, warp_owner, switch_type, save_line;
	private double x, y, z, cost;
	private int cooldown_time, max_uses;
	private boolean global_cooldown;
	private World world;
	private String[] exempted_players;

	public UltraSwitch(String my_warp_name, String my_warp_owner, String my_switch_type, int my_cooldown_time, int my_max_uses, boolean my_global_cooldown,
			double my_cost, String[] my_exempted_players, double my_x, double my_y, double my_z, World my_world) {
		warp_name = my_warp_name;
		warp_owner = my_warp_owner;
		switch_type = my_switch_type;
		cooldown_time = my_cooldown_time;
		max_uses = my_max_uses;
		global_cooldown = my_global_cooldown;
		cost = my_cost;
		exempted_players = my_exempted_players;
		x = my_x;
		y = my_y;
		z = my_z;
		world = my_world;
		save_line =
				"The " + switch_type + " at (" + x + ", " + y + ", " + z + ") in \"" + world.getWorldFolder().getName() + "\" is linked to " + warp_owner
						+ "'s warp \"" + warp_name + "\".";
		if (cooldown_time > 0) {
			if (max_uses != 1)
				save_line += " It can be used " + max_uses + " times before ";
			else
				save_line += " It can be used once before ";
			if (global_cooldown)
				save_line += "everyone has to wait ";
			else
				save_line += "that player has to wait ";
			save_line += myUltraWarps.translateTimeInmsToString(cooldown_time, false) + " before using it again.";
		}
	}

	public UltraSwitch(String my_save_line) {
		// The [switch type] at ([x], [y], [z]) in "[world]" is linked to [owner]'s warp "[warp name]". (It can be used [max uses] times before [that
		// player/everyone] has to wait [cooldown time] before using it again.)
		// TODO: add: ([player1, player2, and player3] are exempted from all charges and restrictions.) (It [costs/gives] players [money] [economy currency]
		// (and [minor] [economy minor currency]) to use it.)
		save_line = my_save_line;
		// read the save line
		if (save_line.substring(4, 5).equals("b"))
			switch_type = "button";
		else if (save_line.substring(4, 5).equals("p"))
			switch_type = "pressure plate";
		else if (save_line.substring(4, 5).equals("l"))
			switch_type = "lever";
		else if (save_line.substring(4, 5).equals("s"))
			switch_type = "sign";
		String[] temp = save_line.substring(save_line.indexOf('(') + 1, save_line.indexOf(')')).split(", ");
		try {
			x = Double.parseDouble(temp[0]);
			y = Double.parseDouble(temp[1]);
			z = Double.parseDouble(temp[2]);
		} catch (NumberFormatException exception) {
			myUltraWarps.console.sendMessage(ChatColor.DARK_RED + "I got an error while trying to read the coordinates of this switch!");
			myUltraWarps.console.sendMessage(ChatColor.DARK_RED + "save line: \"" + ChatColor.WHITE + save_line + ChatColor.DARK_RED + "\"");
			myUltraWarps.console.sendMessage(ChatColor.DARK_RED + "I read the coordiantes as " + ChatColor.WHITE + "(" + temp[0] + ", " + temp[1] + ", "
					+ temp[2] + ").");
			exception.printStackTrace();
			return;
		}
		world = myUltraWarps.server.getWorld(save_line.split("\"")[1]);
		if (world == null) {
			myUltraWarps.console.sendMessage(ChatColor.DARK_RED + "I couldn't find a world called \"" + save_line.split("\"")[1] + "\"!");
			myUltraWarps.console.sendMessage(ChatColor.DARK_RED + "save line: \"" + ChatColor.WHITE + save_line + ChatColor.DARK_RED + "\"");
			return;
		}
		temp = save_line.split("'s warp \"");
		warp_owner = temp[0].substring(save_line.indexOf(" is linked to ") + 15);
		if (save_line.contains("\". It can be used ")) {
			temp = temp[1].split("\". It can be used ");
			warp_name = temp[0];
			temp = temp[1].split(" ");
			try {
				max_uses = Integer.parseInt(temp[0]);
			} catch (NumberFormatException exception) {
				myUltraWarps.console.sendMessage(ChatColor.DARK_RED + "I got an error while trying to read the maximum number of uses for this switch!");
				myUltraWarps.console.sendMessage(ChatColor.DARK_RED + "save line: \"" + ChatColor.WHITE + save_line + ChatColor.DARK_RED + "\"");
				myUltraWarps.console.sendMessage(ChatColor.DARK_RED + "I read the max uses as " + ChatColor.WHITE + temp[0] + ChatColor.DARK_RED + ".");
				exception.printStackTrace();
				return;
			}
			if (temp[3].equals("that"))
				global_cooldown = false;
			else if (temp[3].equals("everyone"))
				global_cooldown = true;
			else {
				myUltraWarps.console.sendMessage(ChatColor.DARK_RED
						+ "I got an error while trying to read whether or not the cooldown on this switch was global!");
				myUltraWarps.console.sendMessage(ChatColor.DARK_RED + "save line: \"" + ChatColor.WHITE + save_line + ChatColor.DARK_RED + "\"");
				myUltraWarps.console.sendMessage(ChatColor.DARK_RED + "I read the temp[3] as " + ChatColor.WHITE + temp[3] + ChatColor.DARK_RED + ".");
				return;
			}
			cooldown_time =
					myUltraWarps.translateStringtoTimeInms(save_line.substring(save_line.indexOf(" has to wait ") + 13, save_line
							.indexOf("before using it again.")));
		} else {
			warp_name = temp[1].substring(0, temp[1].length() - 2);
			max_uses = -1;
			global_cooldown = false;
			cooldown_time = 0;
		}
	}

	public String getSaveLine() {
		return save_line;
	}

	public String getWarpName() {
		return warp_name;
	}

	public String getWarpOwner() {
		return warp_owner;
	}

	public String getSwitchType() {
		return switch_type;
	}

	public int getCooldownTime() {
		return cooldown_time;
	}

	public int getMaxUses() {
		return max_uses;
	}

	public boolean hasAGlobalCooldown() {
		return global_cooldown;
	}

	public double getCost() {
		return cost;
	}

	public String[] getExemptedPlayers() {
		return exempted_players;
	}

	public double getX() {
		return x;
	}

	public double getY() {
		return y;
	}

	public double getZ() {
		return z;
	}

	public World getWorld() {
		return world;
	}

	public Location getLocation() {
		return new Location(world, x, y, z);
	}
}