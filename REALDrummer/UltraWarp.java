package REALDrummer;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;

public class UltraWarp {

	private String owner, name, warp_message, no_warp_message, save_line;
	private String[] listed_users;
	private boolean listed, restricted;
	private double x, y, z;
	private float pitch, yaw;
	private World world;

	public UltraWarp(String my_owner, String my_name, boolean my_listed, boolean my_restricted, String my_warp_message, String my_no_warp_message,
			String[] my_listed_users, double my_x, double my_y, double my_z, float my_pitch, float my_yaw, World my_world) {
		owner = my_owner;
		name = my_name;
		listed = my_listed;
		restricted = my_restricted;
		warp_message = my_warp_message;
		no_warp_message = my_no_warp_message;
		listed_users = my_listed_users;
		x = my_x;
		y = my_y;
		z = my_z;
		pitch = my_pitch;
		yaw = my_yaw;
		world = my_world;
		String listed_string;
		if (listed)
			listed_string = "a listed";
		else
			listed_string = "an unlisted";
		if (restricted)
			save_line =
					owner + "'s warp \"" + name + "\" is " + listed_string + ", restricted warp at (" + x + ", " + y + ", " + z + ") in \""
							+ world.getWorldFolder().getName() + "\" aiming at (" + pitch + ", " + yaw + "). Prohibited users see \"" + no_warp_message
							+ "\" while ";
		else
			save_line =
					owner + "'s warp \"" + name + "\" is " + listed_string + ", unrestricted warp at (" + x + ", " + y + ", " + z + ") in \""
							+ world.getWorldFolder().getName() + "\" aiming at (" + pitch + ", " + yaw + "). Permitted users see \"" + warp_message
							+ "\" while ";
		if (listed_users == null)
			listed_users = new String[0];
		if (listed_users.length == 0)
			save_line += "other users may see \"";
		else if (listed_users.length == 1) {
			save_line += listed_users[0] + " sees \"";
		} else if (listed_users.length == 2) {
			save_line += listed_users[0] + " and " + listed_users[1] + " both see \"";
		} else
			for (int i = 0; i < listed_users.length; i++) {
				if (listed_users.length - 2 > i)
					save_line += listed_users[i] + ", ";
				else if (listed_users.length - 1 == i)
					save_line += listed_users[i] + " all see \"";
				else
					save_line += listed_users[i] + ", and ";
			}
		if (restricted)
			save_line += warp_message + "\".";
		else
			save_line += no_warp_message + "\".";
	}

	public UltraWarp(String my_save_line) {
		save_line = my_save_line;
		// [owner]'s warp "[name]" is [a listed/an unlisted], [(un)restricted] warp at ([x], [y], [z]) in "[world]" aiming at ([pitch], [yaw]).
		// [Prohibited/Permitted] users see "[no_warp_message or warp_message]" while [listed_users[0]], [listed_users[1]], [...] and [listed_users[2]] all see
		// "[warp_message or no_warp_message]".
		// alternate ending (for 2 listed users): [...]" while [listed_users[0]] and [listed_users[1]] both see "[warp_message or no_warp_message]".
		// alternate of the alternate ending (for 1 listed user): [...]" while [listed_users[0]] sees "[warp_message or no_warp_message]".
		// alternative to the alternate of the alternate ending (for no listed users): [...]" while other users may see "[warp_message or no_warp_message]".
		owner = save_line.split("'s warp \"")[0];
		name = save_line.substring(owner.length() + 9).split("\"")[0];
		String[] temp = save_line.split("listed, ");
		if (temp[0].endsWith("un"))
			listed = false;
		else
			listed = true;
		if (temp[1].startsWith("un"))
			restricted = false;
		else
			restricted = true;
		temp = temp[1].split(", ");
		try {
			x = Double.parseDouble(temp[0].split(" warp at \\(")[1]);
			y = Double.parseDouble(temp[1]);
			z = Double.parseDouble(temp[2].split("\\)")[0]);
		} catch (NumberFormatException exception) {
			myUltraWarps.console.sendMessage(ChatColor.DARK_RED + "There was an error in reading the x, y, and z coordinates in an UltraWarp save line!");
			myUltraWarps.console.sendMessage(ChatColor.WHITE + myUltraWarps.colorCode(save_line));
		}
		String world_name = temp[2].split("\"")[1];
		for (World my_world : myUltraWarps.server.getWorlds())
			if (my_world.getName().equals(world_name)) {
				world = my_world;
				break;
			}
		if (world == null)
			myUltraWarps.console.sendMessage(ChatColor.DARK_RED + "I couldn't find the world that this warp is located in!");
		temp = save_line.split(" aiming at \\(")[1].split(", ");
		try {
			pitch = Float.parseFloat(temp[0]);
			yaw = Float.parseFloat(temp[1].split("\\).")[0]);
		} catch (NumberFormatException exception) {
			myUltraWarps.console.sendMessage(ChatColor.DARK_RED + "There was an error in reading the pitch and yaw coordinates!");
			myUltraWarps.console.sendMessage(ChatColor.WHITE + myUltraWarps.colorCode(save_line));
		}
		boolean warp_message_first = true;
		if (temp[1].substring(1).split("\\). ")[1].startsWith("Pro"))
			warp_message_first = false;
		else if (!temp[1].substring(1).split("\\). ")[1].startsWith("Per")) {
			myUltraWarps.console.sendMessage(ChatColor.DARK_RED + "I had trouble reading the beginning of the second sentence for this warp.");
			myUltraWarps.console.sendMessage(ChatColor.WHITE + myUltraWarps.colorCode(save_line));
			myUltraWarps.console.sendMessage(ChatColor.DARK_RED + "This is what I read:");
			myUltraWarps.console.sendMessage(ChatColor.DARK_RED + "\"" + temp[1].substring(1).split("\\). ")[1] + "\"");
		}
		temp = save_line.split("\" while ");
		String temp2;
		if (warp_message_first) {
			try {
				warp_message = temp[0].split(" users see \"")[1];
			} catch (ArrayIndexOutOfBoundsException exception) {
				warp_message = "";
			}
			no_warp_message = temp[1].substring(temp[1].indexOf('\"') + 1, temp[1].length() - 2);
			temp2 = temp[1].substring(0, temp[1].length() - no_warp_message.length() - 3);
		} else {
			warp_message = temp[1].substring(temp[1].indexOf('\"') + 1, temp[1].length() - 2);
			try {
				no_warp_message = temp[0].split(" users see \"")[1];
			} catch (ArrayIndexOutOfBoundsException exception) {
				no_warp_message = "";
			}
			temp2 = temp[1].substring(0, temp[1].length() - warp_message.length() - 3);
		}
		// no listed users
		if (temp2.startsWith("other users may see"))
			listed_users = new String[0];
		// one listed user
		else if (temp2.endsWith(" sees ")) {
			listed_users = new String[1];
			listed_users[0] = temp2.substring(0, temp2.length() - 6);
		}
		// two listed users
		else if (temp2.endsWith(" both see "))
			listed_users = temp2.substring(0, temp2.length() - 10).split(" and ");
		// three or more listed users
		else if (temp2.endsWith(" all see ")) {
			temp2 = temp2.substring(0, temp2.length() - 9);
			listed_users = temp2.split(", ");
			// eliminate the "and " at the beginning of the last user's name
			listed_users[listed_users.length - 1] = listed_users[listed_users.length - 1].substring(4);
		} else {
			myUltraWarps.console.sendMessage(ChatColor.DARK_RED + "I got a problem reading the listed users on this warp!");
			myUltraWarps.console.sendMessage(ChatColor.WHITE + myUltraWarps.colorCode(save_line));
		}
	}

	public UltraWarp(String my_owner, String my_name, boolean my_listed, boolean my_restricted, String my_warp_message, String my_no_warp_message,
			String[] my_listed_users, Location location) {
		this(my_owner, my_name, my_listed, my_restricted, my_warp_message, my_no_warp_message, my_listed_users, location.getX(), location.getY(), location
				.getZ(), location.getPitch(), location.getYaw(), location.getWorld());
	}

	public UltraWarp(String warp_message, Location location) {
		this("Sum1", "warp", false, false, warp_message, "No", null, location.getX(), location.getY(), location.getZ(), location.getPitch(), location.getYaw(),
				location.getWorld());
	}

	public String getSaveLine() {
		return save_line;
	}

	public String getOwner() {
		return owner;
	}

	public String getName() {
		return name;
	}

	public String getColoredName() {
		if (listed && !restricted)
			return ChatColor.WHITE + name;
		else if (listed)
			return ChatColor.RED + name;
		else if (!listed && restricted)
			return ChatColor.DARK_RED + name;
		else
			return ChatColor.GRAY + name;
	}

	public String getColoredOwner() {
		if (listed && !restricted)
			return ChatColor.WHITE + owner;
		else if (listed)
			return ChatColor.RED + owner;
		else if (!listed && restricted)
			return ChatColor.DARK_RED + owner;
		else
			return ChatColor.GRAY + owner;
	}

	public boolean isListed() {
		return listed;
	}

	public boolean isRestricted() {
		return restricted;
	}

	public String getWarpMessage() {
		return warp_message;
	}

	public String getNoWarpMessage() {
		return no_warp_message;
	}

	public String[] getListedUsers() {
		return listed_users;
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

	public float getPitch() {
		return pitch;
	}

	public float getYaw() {
		return yaw;
	}

	public World getWorld() {
		return world;
	}

	public Location getLocation() {
		return new Location(world, x, y, z, (float) yaw, (float) pitch);
	}
}
