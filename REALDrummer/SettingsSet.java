package REALDrummer;

public class SettingsSet {
	// for target, "[server]" means it's global/server-wide, the use of "[]" means it's a group, and no "[]" means it's a player
	public String default_warp, default_no_warp;
	public boolean must_request_to, must_request_from, home_on_death;
	public int max_warps, warp_history_length, death_history_length;
	public long cooldown;

	public SettingsSet(boolean my_must_request_to, boolean my_must_request_from, boolean my_home_on_death, String my_default_warp, String my_default_no_warp,
			int my_max_warps, long my_cooldown, int my_warp_history_length, int my_death_history_length) {
		must_request_to = my_must_request_to;
		must_request_from = my_must_request_from;
		home_on_death = my_home_on_death;
		default_warp = my_default_warp;
		default_no_warp = my_default_no_warp;
		max_warps = my_max_warps;
		cooldown = my_cooldown;
		warp_history_length = my_warp_history_length;
		death_history_length = my_death_history_length;
	}

	public SettingsSet() {
		// set up with the default settings
		must_request_to = true;
		must_request_from = true;
		home_on_death = true;
		default_warp = "&aWelcome to the [warp].";
		default_no_warp = "&cYou're not allowed to warp to [owner]'s [warp].";
		max_warps = -1;
		cooldown = 0;
		warp_history_length = 20;
		death_history_length = 5;
	}
}
