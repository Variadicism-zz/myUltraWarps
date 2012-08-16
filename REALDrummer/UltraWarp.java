package REALDrummer;

import org.bukkit.ChatColor;
import org.bukkit.World;

public class UltraWarp {

	private String owner, name, warp_message, no_warp_message, save_line;
	private String[] listed_users;
	private boolean listed, restricted;
	private double x, y, z, pitch, yaw;
	private World world;

	public UltraWarp(String my_owner, String my_name, boolean my_listed,
			boolean my_restricted, String my_warp_message,
			String my_no_warp_message, String[] my_listed_users, double my_x,
			double my_y, double my_z, double my_pitch, double my_yaw,
			World my_world) {
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
		if (listed_users == null)
			listed_users = new String[0];
		String listed_string;
		if (listed)
			listed_string = "a listed";
		else
			listed_string = "an unlisted";
		if (restricted) {
			save_line = new String(owner + "'s warp \"" + name + "\" is "
					+ listed_string + ", restricted warp at (" + x + ", " + y
					+ ", " + z + ") in \"" + world.getWorldFolder().getName()
					+ "\" aiming at (" + pitch + ", " + yaw
					+ "). Prohibited users see \"" + no_warp_message
					+ "\" while ");
		} else {
			save_line = new String(owner + "'s warp \"" + name + "\" is "
					+ listed_string + ", unrestricted warp at (" + x + ", " + y
					+ ", " + z + ") in \"" + world.getWorldFolder().getName()
					+ "\" aiming at (" + pitch + ", " + yaw
					+ "). Permitted users see \"" + warp_message + "\" while ");
		}
		if (listed_users.length == 0)
			save_line = save_line + "other users may see \"";
		else if (listed_users.length == 1) {
			save_line = save_line + listed_users[0] + " sees \"";
		} else if (listed_users.length == 2) {
			save_line = save_line + listed_users[0] + " and " + listed_users[1]
					+ " both see \"";
		} else
			for (int i = 0; i < listed_users.length; i++) {
				if (listed_users.length - 2 > i)
					save_line = save_line + listed_users[i] + ", ";
				else if (listed_users.length - 1 == i)
					save_line = save_line + listed_users[i] + " all see \"";
				else
					save_line = save_line + listed_users[i] + ", and ";
			}
		if (restricted)
			save_line = save_line + warp_message + "\".";
		else
			save_line = save_line + no_warp_message + "\".";
	}

	public UltraWarp(String my_save_line) {
		save_line = my_save_line;
		// [owner]'s warp "[name]" is [a listed/an unlisted], [(un)restricted]
		// warp at ([x], [y], [z]) in "[world]" aiming at ([pitch], [yaw]).
		// [Prohibited/Permitted] users
		// see "[no_warp_message or warp_message]" while [listed_user[0],
		// listed_user[1], and listed_user[2]] all see "[warp_message]".
		int progress = 0;
		// figure out the warp owner
		for (int i = 0; i < 19; i++) {
			if (save_line.substring(i, i + 2).equals("'s")) {
				owner = save_line.substring(0, i);
				int temp_i = i;
				i = 20;
				progress = temp_i + 9;
			}
		}
		int temp_progress = progress;
		// figure out the warp name
		for (int i = temp_progress; i < temp_progress + 75; i++) {
			if (save_line.substring(i, i + 5).equals("\" is ")) {
				name = save_line.substring(progress, i);
				int temp_i = i;
				i = temp_progress + 76;
				progress = temp_i + 5;
			}
		}
		// figure out whether or not it's listed
		if (save_line.substring(progress, progress + 8).equals("a listed")) {
			listed = true;
			progress = progress + 10;
		} else {
			listed = false;
			progress = progress + 13;
		}
		// figure out whether or not it's restricted
		if (save_line.substring(progress, progress + 2).equals("un")) {
			restricted = false;
			progress = progress + 22;
		} else {
			restricted = true;
			progress = progress + 20;
		}
		temp_progress = progress;
		// figure out the x-coordinate
		for (int i = temp_progress; i < temp_progress + 50; i++) {
			if (save_line.substring(i, i + 1).equals(",")) {
				x = Double.parseDouble(save_line.substring(progress, i));
				int temp_i = i;
				i = progress + 51;
				progress = temp_i + 2;
			}
		}
		temp_progress = progress;
		// figure out the y-coordinate
		for (int i = temp_progress; i < temp_progress + 50; i++) {
			if (save_line.substring(i, i + 1).equals(",")) {
				y = Double.parseDouble(save_line.substring(progress, i));
				int temp_i = i;
				i = progress + 51;
				progress = temp_i + 2;
			}
		}
		temp_progress = progress;
		// figure out the z-coordinate
		for (int i = temp_progress; i < temp_progress + 50; i++) {
			if (save_line.substring(i, i + 1).equals(")")) {
				z = Double.parseDouble(save_line.substring(progress, i));
				int temp_i = i;
				i = progress + 51;
				progress = temp_i + 6;
			}
		}
		temp_progress = progress;
		// get the world name
		for (int i = temp_progress; i < temp_progress + 150; i++)
			if (save_line.substring(i, i + 1).equals("\"")) {
				String world_name = save_line.substring(progress, i);
				for (World my_world : myUltraWarps.server.getWorlds()) {
					if (my_world.getWorldFolder().getName().equals(world_name))
						world = my_world;
				}
				int temp_i = i;
				i = temp_progress + 151;
				progress = temp_i + 13;
			}
		temp_progress = progress;
		// figure out the pitch
		for (int i = temp_progress; i < temp_progress + 50; i++) {
			if (save_line.substring(i, i + 1).equals(",")) {
				pitch = Double.parseDouble(save_line.substring(progress, i));
				int temp_i = i;
				i = progress + 51;
				progress = temp_i + 2;
			}
		}
		temp_progress = progress;
		// figure out the yaw
		for (int i = temp_progress; i < temp_progress + 50; i++) {
			if (save_line.substring(i, i + 1).equals(")")) {
				yaw = Double.parseDouble(save_line.substring(progress, i));
				int temp_i = i;
				i = progress + 51;
				if (restricted)
					progress = temp_i + 25;
				else
					progress = temp_i + 24;
			}
		}
		// figure out the first message
		temp_progress = progress;
		for (int i = temp_progress; i < save_line.length() - 8; i++) {
			if (save_line.substring(i, i + 8).equals("\" while ")) {
				if (restricted)
					no_warp_message = save_line.substring(progress, i);
				else
					warp_message = save_line.substring(progress, i);
				int temp_i = i;
				i = save_line.length();
				progress = temp_i + 8;
			}
		}
		// figure out the listed users
		temp_progress = progress;
		if (save_line.substring(temp_progress, temp_progress + 6).equals(
				"other ")) {
			listed_users = new String[0];
			progress = temp_progress + 21;
		} else {
			int comma_counter = 0;
			boolean more_than_2 = false;
			for (int i = temp_progress; i < save_line.length(); i++) {
				if (i + 2 < save_line.length())
					if (save_line.substring(i, i + 2).equals(", ")) {
						comma_counter++;
						more_than_2 = true;
					}
				if (i + 5 < save_line.length())
					if (save_line.substring(i, i + 5).equals(" and "))
						i = save_line.length();
			}
			if (more_than_2) {
				listed_users = new String[comma_counter + 1];
				for (int j = 0; j < listed_users.length; j++) {
					for (int i = progress; i < save_line.length(); i++) {
						if (i + 5 < save_line.length())
							if (save_line.substring(i, i + 2).equals(", ")
									|| save_line.substring(i, i + 5).equals(
											" all ")) {
								listed_users[j] = save_line.substring(progress,
										i);
								progress = i + 2;
								if (i + 6 < save_line.length())
									if (save_line.substring(i, i + 6).equals(
											", and "))
										progress = progress + 4;
									else if (save_line.substring(i, i + 5)
											.equals(" all "))
										progress = progress + 8;
								i = save_line.length();
							}
					}
				}
			} else {
				for (int i = temp_progress; i < save_line.length(); i++) {
					if (i + 6 < save_line.length())
						if (save_line.substring(i, i + 6).equals(" sees ")) {
							listed_users = new String[1];
							listed_users[0] = save_line.substring(progress, i);
							progress = i + 7;
							i = save_line.length();
						} else if (save_line.substring(i, i + 5)
								.equals(" and ")) {
							listed_users = new String[2];
							listed_users[0] = save_line.substring(progress, i);
							progress = i + 5;
							i = progress;
						} else if (save_line.substring(i, i + 6).equals(
								" both ")) {
							listed_users[1] = save_line.substring(progress, i);
							int temp_i = i;
							progress = temp_i + 11;
							i = save_line.length();
						}
				}
			}
		}
		// figure out the last message
		if (restricted)
			warp_message = save_line
					.substring(progress, save_line.length() - 2);
		else
			no_warp_message = save_line.substring(progress,
					save_line.length() - 2);
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

	public double getPitch() {
		return pitch;
	}

	public double getYaw() {
		return yaw;
	}

	public World getWorld() {
		return world;
	}
}
