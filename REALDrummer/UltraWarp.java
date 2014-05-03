package REALDrummer;

import java.util.ArrayList;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class UltraWarp {

    public String owner, name, warp_message, no_warp_message;
    public String[] listed_users;
    public boolean listed, restricted;
    public Location location;

    public UltraWarp(String owner, String name, boolean listed, boolean restricted, String warp_message, String no_warp_message, String[] listed_users, Location location) {
        this.owner = owner;
        this.name = name;
        this.listed = listed;
        this.restricted = restricted;
        this.warp_message = warp_message;
        this.no_warp_message = no_warp_message;
        this.listed_users = listed_users;
        this.location = location;
        if (listed_users == null)
            listed_users = new String[0];
    }

    public UltraWarp(String save_line) {
        try {
            // [owner]'s warp "[name]" is [a listed/an unlisted], [(un)restricted] warp at ([x], [y], [z]) in "[world]" aiming at ([pitch], [yaw]).
            // [Prohibited/Permitted] users see "[no_warp_message or warp_message]" while [listed_users[0]], [listed_users[1]], [...] and [listed_users[2]] all see
            // "[warp_message or no_warp_message]".
            // alternate ending (for 2 listed users): [...]" while [listed_users[0]] and [listed_users[1]] both see "[warp_message or no_warp_message]".
            // alternate of the alternate ending (for 1 listed user): [...]" while [listed_users[0]] sees "[warp_message or no_warp_message]".
            // alternative to the alternate of the alternate ending (for no listed users): [...]" while other users may see "[warp_message or no_warp_message]".
            owner = save_line.split("'s warp \"")[0];
            name = save_line.substring(owner.length() + 9).split("\" is ")[0];
            String[] temp = save_line.split("listed, ");
            if (temp[0].endsWith("un"))
                listed = false;
            else
                listed = true;
            if (temp[1].startsWith("un"))
                restricted = false;
            else
                restricted = true;
            location = myUltraWarps.readLocation(temp[1].substring(temp[1].indexOf('('), temp[1].indexOf(temp[1].contains("\". P") ? "\". P" : "). P") + 1));
            if (location == null)
                myUltraWarps
                        .tellOps(ChatColor.DARK_RED + "I had a problem reading the location of this warp; I read \"" + ChatColor.WHITE
                                + temp[1].substring(temp[1].indexOf('('), temp[1].indexOf(temp[1].contains("\". P") ? "\". P" : "). P") + 1)
                                + "\" as the location of the warp.", true);
            temp = temp[1].split(", ");
            boolean warp_message_first = true;
            if (temp[3].split(temp[3].contains("\". ") ? "\". " : "\\). ")[1].startsWith("Pro"))
                warp_message_first = false;
            else if (!temp[3].substring(1).split(temp[3].contains("\". ") ? "\". " : "\\). ")[1].startsWith("Per")) {
                myUltraWarps.tellOps(ChatColor.DARK_RED + "I had trouble reading the beginning of the second sentence for this warp (\"" + ChatColor.WHITE
                        + myUltraWarps.colorCode(save_line) + ChatColor.DARK_RED + "\"; I read \"" + ChatColor.WHITE
                        + temp[1].substring(1).split(temp[3].contains("\". ") ? "\". " : "\\). ")[1] + ChatColor.DARK_RED + "\").", true);
            }
            temp = save_line.split("\" while ");
            String temp2;
            if (warp_message_first) {
                try {
                    warp_message = temp[0].split(" users see \"")[1];
                } catch (ArrayIndexOutOfBoundsException e) {
                    warp_message = "";
                }
                no_warp_message = temp[1].substring(temp[1].indexOf('\"') + 1, temp[1].length() - 2);
                temp2 = temp[1].substring(0, temp[1].length() - no_warp_message.length() - 3);
            } else {
                warp_message = temp[1].substring(temp[1].indexOf('\"') + 1, temp[1].length() - 2);
                try {
                    no_warp_message = temp[0].split(" users see \"")[1];
                } catch (ArrayIndexOutOfBoundsException e) {
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
            } else
                myUltraWarps.tellOps(ChatColor.DARK_RED + "I got a problem reading the listed users on this warp\"" + ChatColor.WHITE + myUltraWarps.colorCode(save_line)
                        + "\"!", true);
        } catch (Exception e) {
            myUltraWarps.processException(ChatColor.DARK_RED + "There was an issue reading this warp save line: \"" + ChatColor.WHITE + save_line + ChatColor.DARK_RED + "\"",
                    e);
        }
    }

    public UltraWarp(String warp_message, Location location) {
        this("Sum1", "warp", false, false, warp_message, "No", null, location);
    }

    public String getColoredName() {
        if (listed && !restricted)
            return ChatColor.WHITE + name;
        else if (listed)
            return ChatColor.AQUA + name;
        else if (!listed && restricted)
            return ChatColor.DARK_GRAY + name;
        else
            return ChatColor.GRAY + name;
    }

    public String getColoredOwner() {
        if (listed && !restricted)
            return ChatColor.WHITE + owner;
        else if (listed)
            return ChatColor.AQUA + owner;
        else if (!listed && restricted)
            return ChatColor.DARK_GRAY + owner;
        else
            return ChatColor.GRAY + owner;
    }

    public String getType() {
        if (listed)
            if (!restricted)
                return "open";
            else
                return "advertised";
        else if (!restricted)
            return "secret";
        return "private";
    }

    public String getColoredType() {
        if (listed)
            if (!restricted)
                return ChatColor.WHITE + "open";
            else
                return ChatColor.AQUA + "advertised";
        else if (!restricted)
            return ChatColor.GRAY + "secret";
        return ChatColor.DARK_GRAY + "private";
    }

    public String getQualifiedName() {
        return owner + "'s " + name;
    }

    public static UltraWarp getWarp(int extra_param, CommandSender sender, String... parameters) {
        int index = getWarpIndex(extra_param, sender, parameters);
        if (index != -1)
            try {
                return myUltraWarps.warps.get(index);
            } catch (ArrayIndexOutOfBoundsException e) {
                //
            }
        return null;
    }

    public static int getWarpIndex(int extra_param, CommandSender sender, String... parameters) {
        try {
            // establish some objects
            myUltraWarps.UWindex = -1;
            String qname = readQualifiedName(extra_param, sender, parameters);
            myUltraWarps.UWowner = qname.contains("'s") ? qname.substring(0, qname.indexOf(' ') - 2) : null;
            myUltraWarps.UWname = qname.contains(" ") ? qname.substring(qname.indexOf(' ') + 1) : qname;
            Player player = null;
            if (sender instanceof Player)
                player = (Player) sender;
            // weed out the definitely wrong warps
            ArrayList<UltraWarp> possible_warps = new ArrayList<UltraWarp>();
            ArrayList<Integer> possible_indexes = new ArrayList<Integer>();
            for (int i = 0; i < myUltraWarps.warps.size(); i++)
                if (myUltraWarps.UWowner == null || (player != null && myUltraWarps.UWowner.equals(player.getName()))
                        || myUltraWarps.warps.get(i).owner.equals(myUltraWarps.UWowner))
                    if (myUltraWarps.warps.get(i).name.toLowerCase().startsWith(myUltraWarps.UWname.toLowerCase())) {
                        possible_warps.add(myUltraWarps.warps.get(i));
                        possible_indexes.add(i);
                    }
            // prioritize the possible warps
            int[] priorities = new int[possible_warps.size()];
            for (int i = 0; i < possible_warps.size(); i++) {
                int priority;
                boolean user_is_listed = false;
                if (player == null)
                    user_is_listed = true;
                else
                    for (String listed_user : possible_warps.get(i).listed_users)
                        if (listed_user.equals(player.getName()))
                            user_is_listed = true;
                if (player != null && myUltraWarps.UWowner != null && myUltraWarps.UWowner.equals(possible_warps.get(i).owner))
                    priority = 3;
                else if (possible_warps.get(i).listed)
                    if ((!possible_warps.get(i).restricted || !user_is_listed) || (possible_warps.get(i).restricted && user_is_listed))
                        priority = 6;
                    else
                        priority = 12;
                else if ((!possible_warps.get(i).restricted && !user_is_listed) || (possible_warps.get(i).restricted && user_is_listed))
                    priority = 9;
                else
                    priority = 15;
                if (possible_warps.get(i).name.equalsIgnoreCase(myUltraWarps.UWname))
                    priority--;
                if (possible_warps.get(i).name.equals(myUltraWarps.UWname))
                    priority--;
                priorities[i] = priority;
            }
            // find the highest priority warp
            myUltraWarps.UWindex = -1;
            if (possible_warps.size() > 0) {
                myUltraWarps.UWindex = possible_indexes.get(0);
                if (possible_warps.size() > 1) {
                    int first_priority_index = 0;
                    for (int i = 1; i < priorities.length; i++)
                        if (priorities[i] < priorities[first_priority_index]) {
                            myUltraWarps.UWindex = possible_indexes.get(i);
                            first_priority_index = i;
                        }
                }
            }
            return myUltraWarps.UWindex;
        } catch (Exception e) {
            myUltraWarps.processException("There was a problem finding the index of a warp!", e);
            return -1;
        }
    }

    public static String readQualifiedName(int extra_param, CommandSender sender, String... parameters) {
        Player player = null;
        if (sender instanceof Player)
            player = (Player) sender;
        String name, owner;
        // extract the name of the player and the name of the warp
        if (parameters[extra_param].toLowerCase().endsWith("'s")) {
            name = parameters[extra_param + 1];
            owner = parameters[extra_param].substring(0, parameters[extra_param].length() - 2);
            extra_param++;
        } else {
            if (player != null)
                owner = player.getName();
            else
                owner = null;
            name = parameters[extra_param];
        }
        // locate the owner's full true name
        if (owner != null && (player == null || !owner.equals(player.getName())))
            owner = myUltraWarps.autoCompleteName(owner);
        return owner != null ? owner + "'s " + name : name;
    }

    @Override
    public boolean equals(Object object) {
        // only check the warps' names and owners for equivalency
        if (object instanceof UltraWarp && ((UltraWarp) object).name.equals(name) && ((UltraWarp) object).owner.equals(owner))
            return true;
        return false;
    }

    @Override
    public String toString() {
        return owner
                + "'s warp \""
                + name
                + "\" is "
                + (listed ? "a " : "an un")
                + "listed, "
                + (restricted ? "" : "un")
                + "restricted warp at "
                + myUltraWarps.writeLocation(location, false, true)
                + ". "
                + (restricted ? "Prohibited" : "Permitted")
                + " users see \""
                + (restricted ? no_warp_message : warp_message)
                + "\" while "
                + (listed_users == null || listed_users.length == 0 ? "other users may see \"" : listed_users.length == 1 ? listed_users[0] + " sees \""
                        : listed_users.length == 2 ? listed_users[0] + " and " + listed_users[1] + " both see \"" : myUltraWarps.writeArray(listed_users) + " all see \"")
                + (restricted ? warp_message : no_warp_message) + "\".";
    }
}