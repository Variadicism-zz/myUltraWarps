package REALDrummer;

import java.util.ArrayList;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

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
        else if (method.equals("perform teleportation") || method.equals("teleport between worlds")) {
            if (method.equals("teleport between worlds"))
                myUltraWarps.debug("trying again to teleport between worlds...");
            performTeleportation((Player) sender, (Location) objects[0]);
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

    public static boolean performTeleportation(Player player, Location to) {
        // if the player is teleporting between worlds, make sure that if someone else just teleported between worlds, there is a 2-tick delay after the last
        // person in line teleports
        if (!player.getWorld().equals(to.getWorld()))
            if (myUltraWarps.delay_teleportation_between_worlds) {
                myUltraWarps.server.getScheduler().scheduleSyncDelayedTask(myUltraWarps.mUW, new myUltraWarps$1(player, "teleport between worlds", to), 2);
                myUltraWarps.debug("recent interworldly teleportation; delaying teleportation...");
                return true;
            } else {
                myUltraWarps.delay_teleportation_between_worlds = true;
                myUltraWarps.server.getScheduler().scheduleSyncDelayedTask(myUltraWarps.mUW, new myUltraWarps$1(player, "remove interworldly teleportation delay"), 2);
                myUltraWarps.debug("ensuring safe interworldly teleportation...");
            }
        // perform teleportation
        Entity vehicle = player.getVehicle();
        myUltraWarps.debug("Teleporting " + player.getName() + "...");
        // teleport the player with their vehicle if their vehicle is a horse or a pig or if they're in a minecart and the destination is a rail
        boolean with_vehicle = false;
        if (player.isInsideVehicle() && (vehicle.getType() == EntityType.HORSE || vehicle.getType() == EntityType.PIG))
            with_vehicle = true;
        else if (player.isInsideVehicle() && vehicle.getType() == EntityType.MINECART) {
            myUltraWarps.debug("Checking if the minecart should teleport with " + player.getName() + "...");
            if (to.getBlock().getType() == Material.ACTIVATOR_RAIL || to.getBlock().getType() == Material.POWERED_RAIL || to.getBlock().getType() == Material.DETECTOR_RAIL
                    || to.getBlock().getType() == Material.RAILS) {
                with_vehicle = true;
                myUltraWarps.debug(player.getName() + " is teleporting in a minecart to a rail, so I'm teleporting the cart with them.");
            } else if (to.getBlock().getType() == Material.PORTAL) {
                myUltraWarps.debug(player.getName() + "'s minecart is going through a portal. Let me see if there's a rail in front of the minecart at the other end....");
                Vector velocity = player.getVehicle().getVelocity();
                Location rail_check =
                        new Location(to.getWorld(), to.getX() + velocity.getX() / Math.abs(velocity.getX()), to.getY(), to.getZ() + velocity.getZ()
                                / Math.abs(velocity.getZ()), to.getYaw(), to.getPitch());
                if (rail_check.getBlock().getType() == Material.ACTIVATOR_RAIL || rail_check.getBlock().getType() == Material.POWERED_RAIL
                        || rail_check.getBlock().getType() == Material.DETECTOR_RAIL || rail_check.getBlock().getType() == Material.RAILS) {
                    with_vehicle = true;
                    myUltraWarps.debug("Yep. There's a rail at the other end of the portal. I'll teleport the minecart with " + player.getName() + ".");
                } else
                    myUltraWarps.debug(player.getName() + "'s destination is not a rail, so I will not teleport their minecart with them.");
            } else
                myUltraWarps.debug(player.getName() + "'s destination is not a rail, so I will not teleport their minecart with them.");
        } else if (player.isInsideVehicle())
            myUltraWarps.debug(player.getName() + " is not in a proper vehicle for teleportation. Ejecting player from their vehicle...");
        if (with_vehicle && !player.getWorld().equals(to.getWorld())) {
            myUltraWarps.debug(player.getName() + " tried to teleport between worlds with a vehicle, but I told them they couldn't.");
            player.sendMessage(ChatColor.RED
                    + "Sorry, but you can't teleport between worlds with vehicles right now. Don't blame REALDrummer; it's a bug with CraftBukkit and he's working with them to get it fixed as soon as possible.");
            return false;
        }
        if (player.isInsideVehicle() && !player.leaveVehicle()) {
            myUltraWarps.debug(ChatColor.RED + "I couldn't get " + player.getName() + " to leave their vehicle.");
            return false;
        }
        if (!with_vehicle && !player.teleport(to)) {
            myUltraWarps.debug(ChatColor.RED + "I couldn't teleport " + player.getName() + " properly.");
            return false;
        }
        if (with_vehicle && !vehicle.teleport(to)) {
            myUltraWarps.debug(ChatColor.RED + "I couldn't teleport " + player.getName() + "'s vehicle with them properly.");
            return false;
        }
        if (with_vehicle && !vehicle.setPassenger(player)) {
            myUltraWarps.debug(ChatColor.RED + "I couldn't get " + player.getName() + " back in their vehicle after teleporting them both.");
            return false;
        }
        return true;
    }

}