package REALDrummer;

import org.bukkit.World;

public class UltraSwitch {

	private String warp_name, warp_owner, switch_type, save_line;
	private double x, y, z, cost;
	private int cooldown_time, max_uses;
	private boolean global_cooldown;
	private World world;
	private String[] exempted_players;

	public UltraSwitch(String my_warp_name, String my_warp_owner,
			String my_switch_type, int my_cooldown_time, int my_max_uses,
			boolean my_global_cooldown, double my_cost,
			String[] my_exempted_players, double my_x, double my_y,
			double my_z, World my_world) {
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
		save_line = "The " + switch_type + " at (" + x + ", " + y + ", " + z
				+ ") in \"" + world.getWorldFolder().getName()
				+ "\" is linked to " + warp_owner + "'s warp \"" + warp_name
				+ "\".";
		if (cooldown_time > 0) {
			if (max_uses != 1)
				save_line = save_line + " It can be used " + max_uses
						+ " times before ";
			else
				save_line = save_line + " It can be used once before ";
			if (global_cooldown)
				save_line = save_line + "everyone has to wait ";
			else
				save_line = save_line + "that player has to wait ";
			int days = 0, remainder, hours = 0, minutes = 0;
			double seconds = 0;
			days = (int) (cooldown_time / 86400000);
			remainder = cooldown_time % 86400000;
			if (remainder != 0) {
				hours = (int) (remainder / 3600000);
				remainder = remainder % 3600000;
				if (remainder != 0) {
					minutes = (int) (remainder / 60000);
					remainder = remainder % 60000;
					seconds = remainder / 1000.0;
				}
			}
			if (days > 0) {
				save_line = save_line + days + " days ";
				if ((hours > 0 && minutes == 0 && seconds == 0)
						|| (hours == 0 && minutes > 0 && seconds == 0)
						|| (hours == 0 && minutes == 0 && seconds > 0))
					save_line = save_line + "and ";
			}
			if (hours > 0) {
				save_line = save_line + hours + " hours ";
				if ((minutes > 0 && seconds == 0)
						|| (minutes == 0 && seconds > 0))
					save_line = save_line + "and ";
			}
			if (minutes > 0) {
				save_line = save_line + minutes + " minutes ";
				if (seconds > 0)
					save_line = save_line + "and ";
			}
			if (seconds > 0)
				save_line = save_line + seconds + " seconds ";
			save_line = save_line + "before using it again.";
		}
	}

	public UltraSwitch(String my_save_line) {
		// The [switch type] at ([x], [y], [z]) in "[world]" is linked to
		// [owner]'s warp "[warp name]". [player1, player2, and
		// player3] are exempted from all charges and restrictions. (It can be
		// used [max uses] times before
		// [that player/everyone] has to wait [cooldown time] before using it
		// again.) (It [costs/gives players] [money] [economy currency] (and
		// [minor] [economy minor currency]) to use it.)
		save_line = my_save_line;
		// figure out the switch type
		if (save_line.substring(4, 5).equals("b"))
			switch_type = "button";
		else if (save_line.substring(4, 5).equals("p"))
			switch_type = "pressure plate";
		else if (save_line.substring(4, 5).equals("l"))
			switch_type = "lever";
		else if (save_line.substring(4, 5).equals("s"))
			switch_type = "sign";
		int progress = 4 + switch_type.length() + 5;
		int temp_progress = progress;
		// figure out the x-coordinate
		for (int i = temp_progress; i < temp_progress + 50; i++) {
			if (save_line.substring(i, i + 1).equals(",")) {
				x = Double.parseDouble(save_line.substring(progress, i));
				int temp_i = i;
				i = temp_progress + 26;
				progress = temp_i + 2;
			}
		}
		temp_progress = progress;
		// figure out the y-coordinate
		for (int i = temp_progress; i < temp_progress + 50; i++) {
			if (save_line.substring(i, i + 1).equals(",")) {
				y = Double.parseDouble(save_line.substring(progress, i));
				int temp_i = i;
				i = temp_progress + 26;
				progress = temp_i + 2;
			}
		}
		temp_progress = progress;
		// figure out the z-coordinate
		for (int i = temp_progress; i < temp_progress + 50; i++) {
			if (save_line.substring(i, i + 1).equals(")")) {
				z = Double.parseDouble(save_line.substring(progress, i));
				int temp_i = i;
				i = temp_progress + 26;
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
				progress = temp_i + 15;
			}
		// figure out the warp owner's name
		temp_progress = progress;
		for (int i = temp_progress; i < temp_progress + 19; i++) {
			if (save_line.substring(i, i + 2).equals("'s")) {
				warp_owner = save_line.substring(progress, i);
				int temp_i = i;
				i = temp_progress + 20;
				progress = temp_i + 9;
			}
		}
		temp_progress = progress;
		// figure out the warp name
		for (int i = temp_progress; i < temp_progress + 25; i++) {
			if (save_line.substring(i, i + 2).equals("\".")) {
				warp_name = save_line.substring(progress, i);
				int temp_i = i;
				i = temp_progress + 26;
				progress = temp_i + 18;
			}
		}
		temp_progress = progress;
		if (!(temp_progress > save_line.length())) {
			boolean done = false;
			// figure out the max uses
			for (int i = temp_progress; i < temp_progress + 6; i++) {
				if (save_line.substring(i, i + 1).equals(" ")) {
					try {
						max_uses = Integer.parseInt(save_line.substring(
								progress, i));
						int temp_i = i;
						i = temp_progress + 7;
						progress = temp_i + 14;
					} catch (NumberFormatException exception) {
						max_uses = 1;
						i = temp_progress + 7;
						progress = temp_progress + 12;
					}
				}
			}
			// figure out whether or not the cooldown is global
			if (save_line.substring(progress, progress + 1).equals("e")) {
				global_cooldown = true;
				progress = progress + 21;
			} else {
				global_cooldown = false;
				progress = progress + 25;
			}
			temp_progress = progress;
			// figure out the cooldown time
			double some_value = 0;
			for (int i = temp_progress; i < temp_progress + 6; i++) {
				if (save_line.substring(i, i + 1).equals(" ")) {
					some_value = Double.parseDouble(save_line.substring(
							progress, i));
					int temp_i = i;
					i = temp_progress + 7;
					progress = temp_i + 1;
				}
			}
			if (save_line.substring(progress, progress + 4).equals("days")) {
				cooldown_time = (int) (cooldown_time + some_value * 86400000);
				progress = progress + 5;
			} else if (save_line.substring(progress, progress + 5).equals(
					"hours")) {
				cooldown_time = (int) (cooldown_time + some_value * 3600000);
				progress = progress + 6;
			} else if (save_line.substring(progress, progress + 7).equals(
					"minutes")) {
				cooldown_time = (int) (cooldown_time + some_value * 60000);
				progress = progress + 8;
			} else if (save_line.substring(progress, progress + 7).equals(
					"seconds"))
				cooldown_time = (int) (cooldown_time + some_value * 1000);
			if (cooldown_time >= 60000) {
				temp_progress = progress;
				try {
					for (int i = temp_progress; i < temp_progress + 6; i++) {
						if (save_line.substring(i, i + 1).equals(" ")) {
							some_value = Double.parseDouble(save_line
									.substring(progress, i));
							int temp_i = i;
							i = temp_progress + 7;
							progress = temp_i + 1;
						}
					}
					if (save_line.substring(progress, progress + 5).equals(
							"hours")) {
						cooldown_time = (int) (cooldown_time + some_value * 3600000);
						progress = progress + 6;
					} else if (save_line.substring(progress, progress + 7)
							.equals("minutes")) {
						cooldown_time = (int) (cooldown_time + some_value * 60000);
						progress = progress + 8;
					} else if (save_line.substring(progress, progress + 7)
							.equals("seconds"))
						cooldown_time = (int) (cooldown_time + some_value * 1000);
				} catch (NumberFormatException exception) {
					if (save_line.substring(progress, progress + 4).equals(
							"and ")) {
						progress = progress + 4;
						temp_progress = progress;
						for (int i = temp_progress; i < temp_progress + 6; i++) {
							if (save_line.substring(i, i + 1).equals(" ")) {
								some_value = Double.parseDouble(save_line
										.substring(progress, i));
								int temp_i = i;
								i = temp_progress + 7;
								progress = temp_i + 1;
							}
						}
						if (save_line.substring(progress, progress + 5).equals(
								"hours")) {
							cooldown_time = (int) (cooldown_time + some_value * 3600000);
							progress = progress + 6;
						} else if (save_line.substring(progress, progress + 7)
								.equals("minutes")) {
							cooldown_time = (int) (cooldown_time + some_value * 60000);
							progress = progress + 8;
						} else if (save_line.substring(progress, progress + 7)
								.equals("seconds"))
							cooldown_time = (int) (cooldown_time + some_value * 1000);
						done = true;
					}
				}
				if (cooldown_time >= 3600000 && !done) {
					temp_progress = progress;
					try {
						for (int i = temp_progress; i < temp_progress + 6; i++) {
							if (save_line.substring(i, i + 1).equals(" ")) {
								some_value = Double.parseDouble(save_line
										.substring(progress, i));
								int temp_i = i;
								i = temp_progress + 7;
								progress = temp_i + 1;
							}
						}
						if (save_line.substring(progress, progress + 7).equals(
								"minutes")) {
							cooldown_time = (int) (cooldown_time + some_value * 60000);
							progress = progress + 8;
						} else if (save_line.substring(progress, progress + 7)
								.equals("seconds"))
							cooldown_time = (int) (cooldown_time + some_value * 1000);
					} catch (NumberFormatException exception) {
						if (save_line.substring(progress, progress + 4).equals(
								"and ")) {
							progress = progress + 4;
							temp_progress = progress;
							for (int i = temp_progress; i < temp_progress + 6; i++) {
								if (save_line.substring(i, i + 1).equals(" ")) {
									some_value = Double.parseDouble(save_line
											.substring(progress, i));
									int temp_i = i;
									i = temp_progress + 7;
									progress = temp_i + 1;
								}
							}
							if (save_line.substring(progress, progress + 7)
									.equals("minutes")) {
								cooldown_time = (int) (cooldown_time + some_value * 60000);
								progress = progress + 8;
							} else if (save_line.substring(progress,
									progress + 7).equals("seconds"))
								cooldown_time = (int) (cooldown_time + some_value * 1000);
							done = true;
						}
					}
				}
				if (cooldown_time >= 86400000 && !done) {
					temp_progress = progress;
					try {
						for (int i = temp_progress; i < temp_progress + 6; i++) {
							if (save_line.substring(i, i + 1).equals(" ")) {
								some_value = Double.parseDouble(save_line
										.substring(progress, i));
								int temp_i = i;
								i = temp_progress + 7;
								progress = temp_i + 1;
							}
						}
						if (save_line.substring(progress, progress + 7).equals(
								"seconds"))
							cooldown_time = (int) (cooldown_time + some_value * 1000);
					} catch (NumberFormatException exception) {
						if (save_line.substring(progress, progress + 4).equals(
								"and ")) {
							progress = progress + 4;
							temp_progress = progress;
							for (int i = temp_progress; i < temp_progress + 6; i++) {
								if (save_line.substring(i, i + 1).equals(" ")) {
									some_value = Double.parseDouble(save_line
											.substring(progress, i));
									int temp_i = i;
									i = temp_progress + 7;
									progress = temp_i + 1;
								}
							}
							if (save_line.substring(progress, progress + 7)
									.equals("seconds"))
								cooldown_time = (int) (cooldown_time + some_value * 1000);
							done = true;
						}
					}
				}
			}
		} else {
			max_uses = 0;
			cooldown_time = 0;
			global_cooldown = false;
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
}