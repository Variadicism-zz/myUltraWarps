package REALDrummer;

public class SettingsSet {
    // for target, "[server]" means it's global/server-wide, the use of "[]" means it's a group, and no "[]" means it's a player
    public String default_warp, default_no_warp;
    public boolean home_on_death;
    public int max_warps, warp_history_length, death_history_length;
    public long cooldown;

    public SettingsSet(boolean my_home_on_death, String my_default_warp, String my_default_no_warp, int my_max_warps, long my_cooldown, int my_warp_history_length,
            int my_death_history_length) {
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
        home_on_death = true;
        default_warp = "&aWelcome to the [warp].";
        default_no_warp = "&cYou're not allowed to warp to [owner]'s [warp].";
        max_warps = -1;
        cooldown = 0;
        warp_history_length = 20;
        death_history_length = 5;
    }

    // to change a variable, we need to make a whole new SettingsSet, so all of these setters must return SettingsSets
    public SettingsSet setHomeOnDeath(boolean new_home_on_death) {
        return new SettingsSet(new_home_on_death, default_warp, default_no_warp, max_warps, cooldown, warp_history_length, death_history_length);
    }

    public SettingsSet setDefaultWarpMessage(String new_default_warp) {
        return new SettingsSet(home_on_death, new_default_warp, default_no_warp, max_warps, cooldown, warp_history_length, death_history_length);
    }

    public SettingsSet setDefaultNoWarpMessage(String new_default_no_warp) {
        return new SettingsSet(home_on_death, default_warp, new_default_no_warp, max_warps, cooldown, warp_history_length, death_history_length);
    }

    public SettingsSet setMaxWarps(int new_max_warps) {
        return new SettingsSet(home_on_death, default_warp, default_no_warp, new_max_warps, cooldown, warp_history_length, death_history_length);
    }

    public SettingsSet setCooldownTime(long new_cooldown) {
        return new SettingsSet(home_on_death, default_warp, default_no_warp, max_warps, new_cooldown, warp_history_length, death_history_length);
    }

    public SettingsSet setWarpHistoryLength(int new_warp_history_length) {
        return new SettingsSet(home_on_death, default_warp, default_no_warp, max_warps, cooldown, new_warp_history_length, death_history_length);
    }

    public SettingsSet setDeathHistoryLength(int new_death_history_length) {
        return new SettingsSet(home_on_death, default_warp, default_no_warp, max_warps, cooldown, warp_history_length, new_death_history_length);
    }
}
