package REALDrummer;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;

public class UltraSwitch {

    // sign post=63, wall sign=68, lever=69, stone pressure plate=70, wooden pressure plate=72, stone button=77, wooden button = 143
    private static final Object[][] SWITCH_TYPES = { { 63, "sign" }, { 68, "sign" }, { 69, "lever" }, { 70, "pressure plate" }, { 72, "pressure plate" }, { 77, "button" },
            { 143, "button" } };

    public Block block;
    public String warp_name, warp_owner, switch_type;
    public double cost;
    public Location location;
    public int cooldown_time, max_uses, x, y, z;
    public boolean global_cooldown;
    public World world;
    public String[] exempted_players;

    public UltraSwitch(String warp_name, String warp_owner, Block block, int cooldown_time, int max_uses, boolean global_cooldown, double cost, String[] exempted_players) {
        this.warp_name = warp_name;
        this.warp_owner = warp_owner;
        this.block = block;
        switch_type = getSwitchType(block);
        if (switch_type == null) {
            myUltraWarps.console.sendMessage(ChatColor.DARK_RED + "I couldn't find the switch type for this block!");
            myUltraWarps.console.sendMessage(ChatColor.WHITE + block.toString());
        }
        this.cooldown_time = cooldown_time;
        this.max_uses = max_uses;
        this.global_cooldown = global_cooldown;
        this.cost = cost;
        this.exempted_players = exempted_players;
        location = block.getLocation();
        x = location.getBlockX();
        y = location.getBlockY();
        z = location.getBlockZ();
        world = location.getWorld();
    }

    public UltraSwitch(String save_line) {
        // The [switch type] at ([x], [y], [z]) in "[world]" is linked to [owner]'s warp "[warp name]". (It can be used [max uses] times before [that
        // player/everyone] has to wait [cooldown time] before using it again.)
        // TODO: add: (It [costs/gives] (everyone except [player1, player2, and players3]) [money] [economy currency] (and [minor] [economy minor currency]) to
        // use it.)
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
            // we have to check for Doubles, not Integers, and (int) them to make the switches.txt backwards-compatible
            x = (int) Double.parseDouble(temp[0]);
            y = (int) Double.parseDouble(temp[1]);
            z = (int) Double.parseDouble(temp[2]);
        } catch (NumberFormatException exception) {
            myUltraWarps.console.sendMessage(ChatColor.DARK_RED + "I got an error while trying to read the coordinates of this switch!");
            myUltraWarps.console.sendMessage(ChatColor.DARK_RED + "save line: \"" + ChatColor.WHITE + save_line + ChatColor.DARK_RED + "\"");
            myUltraWarps.console.sendMessage(ChatColor.DARK_RED + "I read the coordiantes as " + ChatColor.WHITE + "(" + temp[0] + ", " + temp[1] + ", " + temp[2] + ").");
            exception.printStackTrace();
            return;
        }
        world = myUltraWarps.server.getWorld(save_line.split("\"")[1]);
        if (world == null) {
            myUltraWarps.console.sendMessage(ChatColor.DARK_RED + "I couldn't find a world called \"" + save_line.split("\"")[1] + "\"!");
            myUltraWarps.console.sendMessage(ChatColor.DARK_RED + "save line: \"" + ChatColor.WHITE + save_line + ChatColor.DARK_RED + "\"");
            return;
        }
        location = new Location(world, x, y, z);
        block = location.getBlock();
        temp = save_line.split("'s warp \"");
        warp_owner = temp[0].substring(save_line.indexOf(" is linked to ") + 14);
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
                myUltraWarps.console.sendMessage(ChatColor.DARK_RED + "I got an error while trying to read whether or not the cooldown on this switch was global!");
                myUltraWarps.console.sendMessage(ChatColor.DARK_RED + "save line: \"" + ChatColor.WHITE + save_line + ChatColor.DARK_RED + "\"");
                myUltraWarps.console.sendMessage(ChatColor.DARK_RED + "I read the temp[3] as " + ChatColor.WHITE + temp[3] + ChatColor.DARK_RED + ".");
                return;
            }
            cooldown_time = myUltraWarps.readTime(save_line.substring(save_line.indexOf(" has to wait ") + 13, save_line.indexOf("before using it again.")));
        } else {
            warp_name = temp[1].substring(0, temp[1].length() - 2);
            max_uses = -1;
            global_cooldown = false;
            cooldown_time = 0;
        }
    }

    @SuppressWarnings("deprecation")
    public static String getSwitchType(Block block) {
        for (Object[] type : SWITCH_TYPES)
            if ((Integer) type[0] == block.getTypeId())
                return (String) type[1];
        return null;
    }

    public static UltraSwitch getSwitch(Block block) {
        for (UltraSwitch _switch : myUltraWarps.switches)
            if (_switch.location.equals(block.getLocation()))
                return _switch;
        return null;
    }

    @Override
    public String toString() {
        // The [switch type] at ([x], [y], [z]) in "[world]" is linked to [owner]'s warp "[warp name]". (It can be used [max uses] times before [that
        // player/everyone] has to wait [cooldown time] before using it again.)
        // TODO: add: (It [costs/gives] (everyone except [player1, player2, and players3]) [money] [economy currency] (and [minor] [economy minor currency]) to
        // use it.)
        String save_line =
                "The " + switch_type + " at (" + x + ", " + y + ", " + z + ") in \"" + world.getWorldFolder().getName() + "\" is linked to " + warp_owner + "'s warp \""
                        + warp_name + "\".";
        if (cooldown_time > 0) {
            if (max_uses != 1)
                save_line += " It can be used " + max_uses + " times before ";
            else
                save_line += " It can be used once before ";
            if (global_cooldown)
                save_line += "everyone has to wait ";
            else
                save_line += "that player has to wait ";
            save_line += myUltraWarps.writeTime(cooldown_time, false) + " before using it again.";
        }
        return save_line;
    }
}