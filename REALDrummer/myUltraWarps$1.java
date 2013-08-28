package REALDrummer;

import java.util.ArrayList;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class myUltraWarps$1 implements Runnable {
    private CommandSender sender;
    private String method;
    private Object[] objects;
    // for remindOfToRequest() and remindOfFromRequest()
    private byte seconds_passed = 20;

    public myUltraWarps$1(CommandSender my_sender, String my_method, Object... my_objects) {
        sender = my_sender;
        method = my_method;
        objects = my_objects;
    }

    @SuppressWarnings("unchecked")
    @Override
    public void run() {
        if (method.equals("remove cooldown")) {
            myUltraWarps.cooling_down_players.remove((String) objects[0]);
            myUltraWarps.debug((String) objects[0] + " was removed from the list of cooling down players.");
        } else if (method.equals("follow through on /to request"))
            followThroughOnToRequest((Player) objects[0], (ArrayList<String>) objects[1]);
        else if (method.equals("teleport between worlds")) {
            myUltraWarps.debug("trying again to teleport between worlds...");
            myUltraWarps.performTeleportation((Player) sender, (Location) objects[0]);
        } else if (method.equals("remove interworldly teleportation delay")) {
            myUltraWarps.debug("interworldly teleportation delay satisfied; changing boolean to \"false\"");
            myUltraWarps.delay_teleportation_between_worlds = false;
        } else
            myUltraWarps.tellOps(ChatColor.DARK_RED + "Hey! What the heck does \"" + method + "\" mean? Get REALDrummer over here and tell him to fix this!", true);
    }

    private void followThroughOnToRequest(Player target_player, ArrayList<String> previous_requests) {
        // sender is the player who sent the request.
        // If target_player already answered player's request, do not send reminders.
        if (myUltraWarps.to_teleport_requests.get(target_player.getName()) == null
                || !myUltraWarps.to_teleport_requests.get(target_player.getName()).contains(sender.getName()))
            return;
        // seconds_passed starts at 20 because I set it up to not even call this method for the first time until after 20 seconds.
        if (seconds_passed == 20 && myUltraWarps.to_teleport_requests.get(target_player.getName()).equals(previous_requests)) {
            target_player.sendMessage(ChatColor.GREEN + "Hey, " + target_player.getName() + ", " + sender.getName() + " still wants to teleport to you. Is that okay?");
            // the next time this method is called, 40 seconds will have passed
            seconds_passed = 40;
            myUltraWarps.server.getScheduler().scheduleSyncDelayedTask(myUltraWarps.mUW, this, 400);
        } else if (seconds_passed == 40 && myUltraWarps.to_teleport_requests.get(target_player.getName()).equals(previous_requests)) {
            target_player.sendMessage(ChatColor.GREEN + "Hey, " + target_player.getName() + ", can " + sender.getName() + " teleport to you? Yes or no?");
            // the next time this method is called, 60 seconds will have passed
            seconds_passed = 60;
            myUltraWarps.server.getScheduler().scheduleSyncDelayedTask(myUltraWarps.mUW, this, 400);
        } else if (seconds_passed == 60 && myUltraWarps.to_teleport_requests.get(target_player.getName()) != null
                && myUltraWarps.to_teleport_requests.get(target_player.getName()).contains(sender.getName())) {
            // remove the teleportation request
            target_player.sendMessage(ChatColor.GREEN + "I'll assume your silence means that you don't want " + sender.getName() + " to teleport to you.");
            sender.sendMessage(ChatColor.RED + "Despite my best efforts, " + target_player.getName() + " won't respond. They might be busy. Try again in a bit.");
            ArrayList<String> requests = myUltraWarps.to_teleport_requests.get(target_player.getName());
            requests.remove(target_player.getName());
            myUltraWarps.to_teleport_requests.put(sender.getName(), requests);
        } // We don't want to send reminders if the player has received more requests since then. We'll let the latest requests be handled by the scheduled
          // events created by the latest request so we don't spam the target player's chat if he/she receives mutliple requests in a short span of time.
          // We still want to make sure that the request expires after 60 seconds total, though
        else if (!myUltraWarps.to_teleport_requests.get(target_player.getName()).equals(previous_requests)) {
            // the time here = the time it takes until 60 seconds has passed (*20 because the Bukkit scheduler works on 20 ticks/second)
            myUltraWarps.server.getScheduler().scheduleSyncDelayedTask(myUltraWarps.mUW, this, (60 - seconds_passed) * 20);
            seconds_passed = 60;
        }
    }
}
