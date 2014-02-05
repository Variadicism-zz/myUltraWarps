package REALDrummer;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;

import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.XMLEvent;

import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.permission.Permission;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.Server;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.EntityTargetEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.event.world.ChunkUnloadEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.ChatPaginator;
import org.bukkit.util.ChatPaginator.ChatPage;

/** This is the main class for myUltraWarps. You can find the source code for this .class <a
 * href="https://github.com/REALDrummer/myUltraWarps/blob/master/REALDrummer/myUltraWarps.java">right here</a>.
 * 
 * @author REALDrummer */
public class myUltraWarps extends JavaPlugin implements Listener {
    public static Plugin mUW, Vault = null;
    public static Server server;
    public static ConsoleCommandSender console;
    public static final ChatColor COLOR = ChatColor.GREEN;
    public static final Short[] NON_SOLID_BLOCK_IDS = { 0, 6, 27, 28, 30, 31, 32, 37, 38, 39, 40, 50, 51, 55, 59, 63, 64, 65, 66, 68, 69, 70, 71, 72, 75, 76, 77, 78, 83, 90,
            93, 94, 104, 105, 115, 119, 131, 132, 140, 141, 142, 142, 144, 147, 148, 149, 150, 157, 171, 175 };
    private static final String[] COLOR_COLOR_CODE_CHARS = { "0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "a", "b", "c", "d", "e", "f" }, FORMATTING_COLOR_CODE_CHARS = {
            "k", "l", "m", "n", "o", "r" };
    public static ArrayList<UltraWarp> warps = new ArrayList<UltraWarp>();
    public static ArrayList<UltraSwitch> switches = new ArrayList<UltraSwitch>();
    public static HashMap<String, SettingsSet> settings = new HashMap<String, SettingsSet>();
    public static String UWname, UWowner;
    public static int UWindex;
    private ArrayList<Object[]> help_pages = new ArrayList<Object[]>();
    public static boolean use_group_settings = true, autosave_warps = false, autosave_switches = false, autosave_config = true, auto_update = true,
            delay_teleportation_between_worlds = false;
    private static ArrayList<String> debuggers = new ArrayList<String>();
    private boolean parsing_warp_message = false, parsing_no_warp_message = false;
    private static HashMap<World, String> spawn_messages_by_world = new HashMap<World, String>();
    private static HashMap<String, Boolean> full_list_organization_by_user = new HashMap<String, Boolean>();
    private static HashMap<String, Location> spawning_players = new HashMap<String, Location>();
    private static HashMap<String, Object[]> teleporting_players = new HashMap<String, Object[]>();
    // [...]_teleport_requests = HashMap<player who would be teleported, ArrayList<player(s) who sent the request(s)>>
    public static HashMap<String, ArrayList<String>> info_messages_for_players = new HashMap<String, ArrayList<String>>(),
            to_teleport_requests = new HashMap<String, ArrayList<String>>(), from_teleport_requests = new HashMap<String, ArrayList<String>>(),
            blocked_players = new HashMap<String, ArrayList<String>>(), trusted_players = new HashMap<String, ArrayList<String>>();
    private static HashMap<String, ArrayList<UltraWarp>> warp_histories = new HashMap<String, ArrayList<UltraWarp>>();
    private static HashMap<String, ArrayList<Location>> death_histories = new HashMap<String, ArrayList<Location>>();
    private static HashMap<String, Integer> last_warp_indexes = new HashMap<String, Integer>(), last_warp_to_death_indexes = new HashMap<String, Integer>();
    // cooling_down_players = HashMap<player's name, time in ms that they last warped>
    public static HashMap<String, Long> cooling_down_players = new HashMap<String, Long>();
    private static Permission permissions = null;
    private static Economy economy = null;

    // TODO FOR ALL PLUGINS: on loading stuff, if the file didn't exist, don't say you loaded the stuff. That's a lie.
    // TODO FOR ALL PLUGINS: encompass the complete inside of all methods that can run outside of other methods (onEnable, onDisable, command/run operators, and
    // listeners) in a try/catch block with an Exception parameter to inform ops of ANY errors in ANY
    // methods
    // TODO FOR ALL PLUGINS: make sure all .replaceAll()s are my custom ones, not Java's String.replaceAll()

    // TODO FOR 7.8: finish documenting everything, including adding @params for "parameters" in many of the command methods
    // TODO FOR 7.8: fix unrestricted warp message issues
    // TODO FOR 8: make a series of tutorial videos explaining the different features of myUltraWarps; if the video for a part is not done yet, make a video that
    // starts with "Coming soon to a server near you!", then after a few seconds transitions into Nyan Cat with a Steve face
    // TODO: make the group settings remain as they are if Vault doesn't exist instead of letting them disappear
    // TODO: make /trust
    // TODO: make anti-spam filters for /to and /from
    // TODO: make on-login info messages rollover
    // TODO: make /back and /fwd display "(warp [index]/[warp_history.size()])"
    // TODO: if a player blocks a myUltraWarps admin while he's offline, unblock the admin when he logs on and inform the blocker
    // TODO: /send request system
    // TODO: make messages informing of non-default characteristics in a warp in /create
    // TODO: reorganize the command operator's if statements

    // DONE: fixed warp self-restricting on reload

    // plugin enable/disable and the command operator
    /** This method is called when myUltraWarps is enabled. */
    @Override
    public void onEnable() {
        try {
            mUW = this;
            server = getServer();
            console = server.getConsoleSender();
            // register this class as a listener
            server.getPluginManager().registerEvents(this, this);
            // set up the warp_histories list for /back
            for (Player player : server.getOnlinePlayers())
                warp_histories.put(player.getName(), new ArrayList<UltraWarp>());
            // load the saved data
            loadTheConfig(console);
            loadTheWarps(console);
            loadTheSwitches(console);
            loadTheTemporaryData();
            if (auto_update)
                checkForUpdates(console);
            // load the help pages
            for (int i = 0; i < 43; i++) {
                Object[] help_line = new Object[4];
                if (i == 0) {
                    help_line[0] =
                            "&a&o/create (\"warp\") [warp name] (settings) &fcreates a warp called \"[warp name]\". You can also use &a&o/make warp &for &a&o/set warp.&f\nFor the \"(settings)&f\" parameter, you can put in one or more of these settings to customize the warp:\n&a&otype:[type] &fallows you to decide whether the warp private (restricted and unlisted), secret (unlisted but unrestricted), advertised (listed but restricted), or open (listed and unrestricted).";
                    help_line[1] = "myultrawarps.create";
                    help_line[2] = true;
                    help_line[3] = 10;
                } else if (i == 1) {
                    help_line[0] = "&a&ogiveto:[player] &fallows you to give the warp to another player.";
                    help_line[1] = "myultrawarps.create";
                    help_line[2] = true;
                    help_line[3] = 1;
                } else if (i == 2) {
                    help_line[0] = "&f&oYou &fcan give warps to other players even if they already have a warp with the same name.";
                    help_line[1] = "myultrawarps.create.other";
                    help_line[2] = false;
                    help_line[3] = 2;
                } else if (i == 3) {
                    help_line[0] =
                            "&a&owarp:[message] &fallows you to customize the message that appears when someone warps to your warp. The message can be as long as you like and may have spaces and you can use color codes! I love colors.\n&a&onowarp:[message] &fallows you to customize the message that appears when someone tries to warp to your warp, but is not allowed to.";
                    help_line[1] = "myultrawarps.create";
                    help_line[2] = true;
                    help_line[3] = 7;
                } else if (i == 4) {
                    help_line[0] =
                            "&a&olist:[player1],[player2] &fallows you to add players to the warp's list. The warp's list works both as a blacklist for unrestricted warps and as a whitelist for restricted warps. You may list as many people as you want at once by separating usernames with commas and no spaces.\n&a&ounlist:[player1],[player2] &fallows you to remove people from the warp's list.";
                    help_line[1] = "myultrawarps.create";
                    help_line[2] = true;
                    help_line[3] = 7;
                } else if (i == 5) {
                    help_line[0] = "&a&o/warp (owner\"'s\") [warp name] &fwarps you to the specified warp.";
                    help_line[1] = "myultrawarps.warptowarp";
                    help_line[2] = true;
                    help_line[3] = 2;
                } else if (i == 6) {
                    help_line[0] = "&f&oYou &fcan warp to other people's warps, too--even restricted ones.";
                    help_line[1] = "myultrawarps.warptowarp.other";
                    help_line[2] = false;
                    help_line[3] = 2;
                } else if (i == 7) {
                    help_line[0] =
                            "&a&o/warp (world) [x] [y] [z] (world) &fwarps you to the specified coordinates in (world). You do not need to type the world name both before and after the coordinates; one or the other will do. If (world) is left blank, you will warp to those coordinates in your current world.";
                    help_line[1] = "myultrawarps.warptocoord";
                    help_line[2] = true;
                    help_line[3] = 5;
                } else if (i == 8) {
                    help_line[0] =
                            "&a&o/change (\"warp\") (owner\"'s\") [warp name] [settings] &fchanges the settings of an existing warp. The settings are the same as the ones for &a&o/create&f, but you can also use &a&oname:[new name] &fto change the name of a warp. You can also use &a&o/modify&f.";
                    help_line[1] = "myultrawarps.change";
                    help_line[2] = true;
                    help_line[3] = 4;
                } else if (i == 9) {
                    help_line[0] = "&f&oYou &fcan also change other players' warps.";
                    help_line[1] = "myultrawarps.change.other";
                    help_line[2] = false;
                    help_line[3] = 1;
                } else if (i == 10) {
                    help_line[0] = "&a&o/move (\"warp\") (owner\"'s\") [warp name] &fmoves the warp to your current location. You can also use &a&o/translate warp&f.";
                    help_line[1] = "myultrawarps.move";
                    help_line[2] = true;
                    help_line[3] = 2;
                } else if (i == 11) {
                    help_line[0] = "&f&oYou &fcan also move other players' warps.";
                    help_line[1] = "myultrawarps.move.other";
                    help_line[2] = false;
                    help_line[3] = 1;
                } else if (i == 12) {
                    help_line[0] = "&a&o/delete (\"warp\") (owner\"'s\") [warp name] &fdeletes the specified warp. You can also use &a&o/remove warp&f.";
                    help_line[1] = "myultrawarps.delete";
                    help_line[2] = true;
                    help_line[3] = 2;
                } else if (i == 13) {
                    help_line[0] = "&f&oYou &fcan also delete other players' warps.";
                    help_line[1] = "myultrawarps.move.other";
                    help_line[2] = false;
                    help_line[3] = 1;
                } else if (i == 14) {
                    help_line[0] =
                            "&a&o/warp(\"s\") list &fdisplays all of your warps and all listed warps with color coding. White warps are open, red warps are advertised, gray warps are secret, and dark red warps are private.";
                    help_line[1] = "myultrawarps.list";
                    help_line[2] = true;
                    help_line[3] = 4;
                } else if (i == 15) {
                    help_line[0] =
                            "&a&o/full warp list (\"page\" [#]) (\"by owner\"/\"by name\") (\"owner:\"[owner]) (\"type:\"[type]) &flists all of the warps for the entire server whether they're listed or not. You can use the parameters above to go page by page, organize the list by owner or by name, or create filters on your search. You can also use &a&o/entire warp list &for &a&o/complete warp list &fand you can put an \"s\" at the end of \"warp\" if you like.";
                    help_line[1] = "myultrawarps.list.full";
                    help_line[2] = false;
                    help_line[3] = 7;
                } else if (i == 16) {
                    help_line[0] = "&a&o/warp info (owner\"'s\") [warp name] &fdisplays all the information about the specified warp.";
                    help_line[1] = "myultrawarps.warpinfo";
                    help_line[2] = true;
                    help_line[3] = 2;
                } else if (i == 17) {
                    help_line[0] = "&f&oYou &fcan also see information on other players' warps.";
                    help_line[1] = "myultrawarps.warpinfo.other";
                    help_line[2] = false;
                    help_line[3] = 1;
                } else if (i == 18) {
                    help_line[0] = "&a&o/back &fwarps you back to the last place you warped. You can also use &a&o/return &for &a&o/last&f.";
                    help_line[1] = "myultrawarps.back";
                    help_line[2] = true;
                    help_line[3] = 2;
                } else if (i == 19) {
                    help_line[0] = "&a&o/set home &fcreates a special unlisted, restricted warp called \"home\" with special default messages.";
                    help_line[1] = "myultrawarps.sethome";
                    help_line[2] = true;
                    help_line[3] = 2;
                } else if (i == 20) {
                    help_line[0] = "&f&oYou &fcan also add the parameter (owner\"'s\") to set other players' home warps.";
                    help_line[1] = "myultrawarps.sethome.other";
                    help_line[2] = false;
                    help_line[3] = 2;
                } else if (i == 21) {
                    help_line[0] = "&a&o/home &fwarps you to your home.";
                    help_line[1] = "myultrawarps.home";
                    help_line[2] = true;
                    help_line[3] = 1;
                } else if (i == 22) {
                    help_line[0] = "&f&oYou &fcan also add the parameter (owner\"'s\") to warp to other players' homes.";
                    help_line[1] = "myultrawarps.home.other";
                    help_line[2] = false;
                    help_line[3] = 2;
                } else if (i == 23) {
                    help_line[0] = "&a&o/set spawn &fsets the spawn point for the world you're in.";
                    help_line[1] = "myultrawarps.admin";
                    help_line[2] = false;
                    help_line[3] = 1;
                } else if (i == 24) {
                    help_line[0] = "&a&o/spawn &fteleports you to your world's spawn point.";
                    help_line[1] = "myultrawarps.spawn";
                    help_line[2] = true;
                    help_line[3] = 1;
                } else if (i == 25) {
                    help_line[0] = "&a&o/jump &fteleports you to the spot you're pointing at.";
                    help_line[1] = "myultrawarps.jump";
                    help_line[2] = true;
                    help_line[3] = 1;
                } else if (i == 26) {
                    help_line[0] = "&a&o/top &fteleports you to the highest solid block directly above or below you.";
                    help_line[1] = "myultrawarps.top";
                    help_line[2] = true;
                    help_line[3] = 2;
                } else if (i == 27) {
                    help_line[0] =
                            "&a&o/link (owner\"'s\") [warp name] (settings) &flinks a warp to a button, lever, or pressure plate that you are pointing at. Once a warp is linked to one of these switches, right-clicking that button, lever, or sign or stepping on the pressure plate will warp you to the warp that the switch is linked to.";
                    help_line[1] = "myultrawarps.link";
                    help_line[2] = true;
                    help_line[3] = 5;
                } else if (i == 28) {
                    help_line[0] = "&f&oYou &fcan also link other players' warps to your switches.";
                    help_line[1] = "myultrawarps.link.other";
                    help_line[2] = false;
                    help_line[3] = 1;
                } else if (i == 29) {
                    help_line[0] =
                            "&a&o/unlink (owner\"'s\") (warp name) &funlinks a warp from a button, pressure plate, sign, or lever that you are pointing at or unlinks all switches from the specified warp if a warp is specified.";
                    help_line[1] = "myultrawarps.unlink";
                    help_line[2] = true;
                    help_line[3] = 4;
                } else if (i == 30) {
                    help_line[0] = "&f&oYou &fcan also unlink other players' switches.";
                    help_line[1] = "myultrawarps.unlink.other";
                    help_line[2] = false;
                    help_line[3] = 1;
                } else if (i == 31) {
                    help_line[0] = "&a&o/switch(\"es\") list &flists all your switches by warp that they're linked to and how many switches are linked to each warp.";
                    help_line[1] = "myultrawarps.list";
                    help_line[2] = true;
                    help_line[3] = 2;
                } else if (i == 32) {
                    help_line[0] =
                            "&a&o/switch info (owner\"'s\") (warp name) &fdisplays the information on the switch that you are pointing at or displays the information on all the switches that are linked to the specified warp if a warp is specified.";
                    help_line[1] = "myultrawarps.switchinfo";
                    help_line[2] = true;
                    help_line[3] = 4;
                } else if (i == 33) {
                    help_line[0] = "&f&oYou &fcan also see information on other players' switches.";
                    help_line[1] = "myultrawarps.switchinfo.other";
                    help_line[2] = false;
                    help_line[3] = 1;
                } else if (i == 34) {
                    help_line[0] =
                            "&a&o/to [player] &fteleports you to the designated player. If the admins configure it so that you have to ask the person if you can teleport to them before doing so, &a&o/to &fwill ask them if it's okay. The target player can then just type their answer into the chat box. It's that easy. You can also use &a&o/find&f.";
                    help_line[1] = "myultrawarps.to";
                    help_line[2] = true;
                    help_line[3] = 5;
                } else if (i == 35) {
                    help_line[0] =
                            "&a&o/from [player] &fforcibly teleports the designated player to you. You can also use &a&o/pull&f, &a&o/yank&f, &a&o/bring&f, or &a&o/get&f.";
                    help_line[1] = "myultrawarps.from";
                    help_line[2] = false;
                    help_line[3] = 2;
                } else if (i == 36) {
                    help_line[0] =
                            "&a&o/warp all (\"to\") [\"here\"/\"there\"/\"warp\" [warp]/\"player\" [player]] &fwarps everyone on the server to your current location, the spot you're pointing at, or the designated warp or player.";
                    help_line[1] = "myultrawarps.warpall";
                    help_line[2] = false;
                    help_line[3] = 4;
                } else if (i == 37) {
                    help_line[0] =
                            "&a&o/send [player] (\"to\") [\"there\"/\"warp\" [warp]/\"player\" [player]] &fwarps the designated player to the spot you're pointing at or the designated warp or player.";
                    help_line[1] = "myultrawarps.send";
                    help_line[2] = false;
                    help_line[3] = 3;
                } else if (i == 38) {
                    help_line[0] =
                            "&a&o/warps [\"around\"/\"near\"] [\"here\"/\"there\"/\"warp\" [warp]/\"player\" [player]] (search radius) &flists all the warps within the search radius of the designated warp, player, or other specified location. By default, the search radius is 20 blocks.";
                    help_line[1] = "myultrawarps.warpsaround";
                    help_line[2] = true;
                    help_line[3] = 5;
                } else if (i == 39) {
                    help_line[0] =
                            "&a&o/default [\"warp\"/\"no warp\"] (\"message\") (\"for\" [player]/\"group:\"[group]/\"server\") [message] &fchanges the default warp or no warp message for a player, a group, or the entire server.";
                    help_line[1] = "myultrawarps.config";
                    help_line[2] = true;
                    help_line[3] = 4;
                } else if (i == 40) {
                    help_line[0] =
                            "&a&o/max warps(\"for\" [player]/\"group:\"[group]/\"server\") [max warps] &fallows admins to change the maximum number of warps that a player, a group, or the entire server can make.";
                    help_line[1] = "myultrawarps.admin";
                    help_line[2] = false;
                    help_line[3] = 3;
                } else if (i == 41) {
                    help_line[0] =
                            "&a&o/myUltraWarps load (\"the\") [\"warps\"/\"switches\"/\"config\"] &freloads all the data on the server for the warps, switches, or configurations straight from the warps.txt, switches.txt, or config.txt file and formats the file. You can also use &a&o/mUW&f.";
                    help_line[1] = "myultrawarps.admin";
                    help_line[2] = false;
                    help_line[3] = 4;
                } else if (i == 42) {
                    help_line[0] =
                            "&a&o/myUltraWarps save (\"the\") [\"warps\"/\"switches\"/\"config\"] &fsaves all the data on the server and updates and formats the warps.txt, switches.txt, or config.txt file. You can also use &a&o/mUW&f.";
                    help_line[1] = "myultrawarps.admin";
                    help_line[2] = false;
                    help_line[3] = 4;
                }
                help_pages.add(help_line);
            }
            // done enabling
            String[] enable_messages =
                    { "Scotty can now beam you up.", "The warps have entered the building.", "These ARE the warps you're looking for.", "May the warps be with you.",
                            "Let's rock these warps.", "Warp! Warp! Warp! Warp! Warp! Warp!", "What warp through yonder server breaks?",
                            "Wanna see me warp to that mountain and back?\nWanna see me do it again?", "/jump is much less lethal now!",
                            "/top used to take people above the Nether's ceiling!" };
            tellOps(COLOR + enable_messages[(int) (Math.random() * enable_messages.length)], true);
        } catch (Exception exception) {
            processException("There was a problem enabling myUltraWarps!", exception);
        }
    }

    /** This method is called when myUltraWarps is disabled. */
    @Override
    public void onDisable() {
        try {
            // forcibly enable the permissions plugin
            if (permissions != null) {
                Plugin permissions_plugin = server.getPluginManager().getPlugin(permissions.getName());
                if (permissions_plugin != null && !permissions_plugin.isEnabled())
                    server.getPluginManager().enablePlugin(permissions_plugin);
            }
            saveTheWarps(console, true);
            saveTheSwitches(console, true);
            saveTheConfig(console, true);
            saveTheTemporaryData();
            // done disabling
            String[] disable_messages =
                    { "Ta ta for now!", "See you soon!", "I'll miss you!", "Don't forget me!", "Don't forget to write!", "Don't leave me here all alone!",
                            "Hasta la vista, baby.", "Wait for me!" };
            tellOps(COLOR + disable_messages[(int) (Math.random() * disable_messages.length)], true);
        } catch (Exception exception) {
            processException("There was a problem disabling myUltraWarps!", exception);
        }
    }

    /** This method is called if a command is used that myUltraWarps has regiestered in its <tt>plugin.yml</tt>. */
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String command, String[] parameters) {
        try {
            boolean success = false;
            if (command.equalsIgnoreCase("setspawn") || (command.equalsIgnoreCase("set") && parameters.length > 0 && parameters[0].equalsIgnoreCase("spawn"))) {
                success = true;
                if (sender instanceof Player && sender.hasPermission("myultrawarps.admin"))
                    setSpawn(sender);
                else if (!(sender instanceof Player))
                    console.sendMessage(ChatColor.RED + "You can't decide where the spawn point goes. You can't point it out to me. Sorry.");
                else {
                    if (command.equalsIgnoreCase("set") && parameters.length > 0 && parameters[0].equalsIgnoreCase("spawn"))
                        sender.sendMessage(ChatColor.RED + "Sorry, but you don't have permission to use " + COLOR + "/set spawn" + ChatColor.RED + ".");
                    else
                        sender.sendMessage(ChatColor.RED + "Sorry, but you don't have permission to use " + COLOR + "/setspawn" + ChatColor.RED + ".");
                }
            } else if (command.equalsIgnoreCase("sethome") || (command.equalsIgnoreCase("set") && parameters.length > 0 && parameters[0].equalsIgnoreCase("home"))) {
                success = true;
                if (sender instanceof Player
                        && (sender.hasPermission("myultrawarps.sethome") || sender.hasPermission("myultrawarps.sethome.other") || sender.hasPermission("myultrawarps.user") || sender
                                .hasPermission("myultrawarps.admin")))
                    setHome(sender, parameters);
                else if (!(sender instanceof Player))
                    console.sendMessage(ChatColor.RED + "You can't have a home! YOU...ARE...A...CONSOLE!");
                else if (parameters.length == 0 || !parameters[0].equalsIgnoreCase("home"))
                    sender.sendMessage(ChatColor.RED + "Sorry, but you don't have permission to use " + COLOR + "/sethome" + ChatColor.RED + ".");
                else
                    sender.sendMessage(ChatColor.RED + "Sorry, but you don't have permission to use " + COLOR + "/set home" + ChatColor.RED + ".");
            } else if ((command.equalsIgnoreCase("warplist") || command.equalsIgnoreCase("warpslist"))
                    || ((command.equalsIgnoreCase("warp") || command.equalsIgnoreCase("warps")) && parameters.length > 0 && parameters[0].equalsIgnoreCase("list"))) {
                success = true;
                if (!(sender instanceof Player)
                        || (sender.hasPermission("myultrawarps.list") || sender.hasPermission("myultrawarps.user") || sender.hasPermission("myultrawarps.admin")))
                    warpList(sender, parameters);
                else if (parameters.length == 0 || !parameters[0].equalsIgnoreCase("list"))
                    sender.sendMessage(ChatColor.RED + "Sorry, but you don't have permission to use " + COLOR + "/" + command.toLowerCase() + ChatColor.RED + ".");
                else
                    sender.sendMessage(ChatColor.RED + "Sorry, but you don't have permission to use " + COLOR + "/" + command.toLowerCase() + " list" + ChatColor.RED + ".");
            } else if (((command.equalsIgnoreCase("full") || command.equalsIgnoreCase("entire") || command.equalsIgnoreCase("complete")) && parameters.length > 1
                    && (parameters[0].equalsIgnoreCase("warp") || parameters[0].equalsIgnoreCase("warps")) && parameters[1].equalsIgnoreCase("list"))
                    || command.equalsIgnoreCase("fullwarplist")
                    || command.equalsIgnoreCase("entirewarplist")
                    || command.equalsIgnoreCase("completewarplist")
                    || command.equalsIgnoreCase("fullwarpslist")
                    || command.equalsIgnoreCase("entirewarpslist")
                    || command.equalsIgnoreCase("completewarpslist")
                    || command.equalsIgnoreCase("fwl") || command.equalsIgnoreCase("cwl") || command.equalsIgnoreCase("ewl")) {
                success = true;
                if (!(sender instanceof Player) || sender.hasPermission("myultrawarps.list.full") || sender.hasPermission("myultrawarps.admin"))
                    fullWarpList(sender, parameters);
                else
                    sender.sendMessage(ChatColor.RED + "Sorry, but you don't have permission to use " + COLOR + "/" + command.toLowerCase() + " warps list" + ChatColor.RED
                            + ".");
            } else if ((command.equalsIgnoreCase("switchlist") || command.equalsIgnoreCase("switcheslist"))
                    || ((command.equalsIgnoreCase("switch") || command.equalsIgnoreCase("switches")) && parameters.length > 0 && parameters[0].equalsIgnoreCase("list"))) {
                success = true;
                if (!(sender instanceof Player)
                        || (sender.hasPermission("myultrawarps.list") || sender.hasPermission("myultrawarps.user") || sender.hasPermission("myultrawarps.admin")))
                    switchList(sender, parameters);
                else if (parameters.length == 0 || !parameters[0].equalsIgnoreCase("list"))
                    sender.sendMessage(ChatColor.RED + "Sorry, but you don't have permission to use " + COLOR + "/" + command.toLowerCase() + ChatColor.RED + ".");
                else
                    sender.sendMessage(ChatColor.RED + "Sorry, but you don't have permission to use " + COLOR + "/" + command.toLowerCase() + " list" + ChatColor.RED + ".");
            } else if (((command.equalsIgnoreCase("full") || command.equalsIgnoreCase("entire") || command.equalsIgnoreCase("complete")) && parameters.length > 1
                    && (parameters[0].equalsIgnoreCase("switch") || parameters[0].equalsIgnoreCase("switches")) && parameters[1].equalsIgnoreCase("list"))
                    || command.equalsIgnoreCase("fullswitchlist")
                    || command.equalsIgnoreCase("entireswitchlist")
                    || command.equalsIgnoreCase("completeswitchlist")
                    || command.equalsIgnoreCase("fullswitcheslist")
                    || command.equalsIgnoreCase("entireswitcheslist")
                    || command.equalsIgnoreCase("completeswitcheslist")
                    || command.equalsIgnoreCase("fsl") || command.equalsIgnoreCase("csl") || command.equalsIgnoreCase("esl")) {
                success = true;
                if (!(sender instanceof Player) || sender.hasPermission("myultrawarps.list.full") || sender.hasPermission("myultrawarps.admin"))
                    fullSwitchList(sender, parameters);
                else
                    sender.sendMessage(ChatColor.RED + "Sorry, but you don't have permission to use " + COLOR + "/" + command.toLowerCase() + " switches list" + ChatColor.RED
                            + ".");
            } else if (command.equalsIgnoreCase("spawn")) {
                success = true;
                if (sender instanceof Player
                        && (sender.hasPermission("myultrawarps.spawn") || sender.hasPermission("myultrawarps.user") || sender.hasPermission("myultrawarps.admin")))
                    spawn(sender, parameters);
                else if (!(sender instanceof Player))
                    console.sendMessage(ChatColor.RED + "You cannot warp! Stop trying to warp! You have no body! Stop trying to warp!");
                else
                    sender.sendMessage(ChatColor.RED + "Sorry, but you don't have permission to use " + COLOR + "/spawn" + ChatColor.RED + ".");
            } else if (command.equalsIgnoreCase("createwarp")
                    || command.equalsIgnoreCase("makewarp")
                    || command.equalsIgnoreCase("setwarp")
                    || ((command.equalsIgnoreCase("create") || command.equalsIgnoreCase("make") || command.equalsIgnoreCase("set")) && (parameters.length == 0 || !parameters[0]
                            .equalsIgnoreCase("warp")))) {
                success = true;
                if (sender instanceof Player
                        && (sender.hasPermission("myultrawarps.create") || sender.hasPermission("myultrawarps.create.other") || sender.hasPermission("myultrawarps.user") || sender
                                .hasPermission("myultrawarps.admin")) && parameters.length > 0)
                    createWarp(0, sender, parameters);
                else if (!(sender instanceof Player))
                    console.sendMessage(ChatColor.RED + "Silly console! You can't make a warp to your current location! You have no body! :P");
                else if (parameters.length == 0)
                    sender.sendMessage(ChatColor.RED + "You forgot to tell me what you want to name the warp!");
                else
                    sender.sendMessage(ChatColor.RED + "Sorry, but you don't have permission to use " + COLOR + "/" + command.toLowerCase() + ChatColor.RED + ".");
            } else if ((command.equalsIgnoreCase("create") || command.equalsIgnoreCase("make") || command.equalsIgnoreCase("set")) && parameters.length > 0
                    && parameters[0].equalsIgnoreCase("warp")) {
                success = true;
                if (sender instanceof Player
                        && (sender.hasPermission("myultrawarps.create") || sender.hasPermission("myultrawarps.create.other") || sender.hasPermission("myultrawarps.user") || sender
                                .hasPermission("myultrawarps.admin")) && parameters.length > 1)
                    createWarp(1, sender, parameters);
                else if (!(sender instanceof Player))
                    console.sendMessage(ChatColor.RED + "Silly console! You can't make a warp! You have no body! :P");
                else if (parameters.length <= 1)
                    sender.sendMessage(ChatColor.RED + "You forgot to tell me what you want to name the warp!");
                else
                    sender.sendMessage(ChatColor.RED + "Sorry, but you don't have permission to use " + COLOR + "/" + command.toLowerCase() + " warp" + ChatColor.RED + ".");
            } else if (command.equalsIgnoreCase("warpinfo")) {
                success = true;
                if ((!(sender instanceof Player) || (sender.hasPermission("myultrawarps.warpinfo") || sender.hasPermission("myultrawarps.warpinfo.other")
                        || sender.hasPermission("myultrawarps.user") || sender.hasPermission("myultrawarps.admin")))
                        && parameters.length > 0)
                    warpInfo(0, sender, parameters);
                else if (parameters.length == 0)
                    sender.sendMessage(ChatColor.RED + "You need to tell me the name of the warp you want info on!");
                else
                    sender.sendMessage(ChatColor.RED + "Sorry, but you don't have permission to use " + COLOR + "/warpinfo" + ChatColor.RED + ".");
            } else if (command.equalsIgnoreCase("warp") && parameters.length > 0 && parameters[0].equalsIgnoreCase("info")) {
                success = true;
                if ((!(sender instanceof Player) || (sender.hasPermission("myultrawarps.warpinfo") || sender.hasPermission("myultrawarps.warpinfo.other")
                        || sender.hasPermission("myultrawarps.user") || sender.hasPermission("myultrawarps.admin")))
                        && parameters.length > 1)
                    warpInfo(1, sender, parameters);
                else if (parameters.length == 1)
                    sender.sendMessage(ChatColor.RED + "You need to tell me the name of the warp you want info on!");
                else
                    sender.sendMessage(ChatColor.RED + "Sorry, but you don't have permission to use " + COLOR + "/warp info" + ChatColor.RED + ".");
            } else if (command.equalsIgnoreCase("warpall")) {
                success = true;
                if (parameters.length > 0 && (!(sender instanceof Player) || sender.hasPermission("myultrawarps.warpall") || sender.hasPermission("myultrawarps.admin")))
                    warpAll(0, sender, parameters);
                else if (parameters.length == 0)
                    sender.sendMessage(ChatColor.RED + "You forgot to tell me where you want all the players warped!");
                else
                    sender.sendMessage(ChatColor.RED + "Sorry, but you don't have permission to use " + COLOR + "/warpall" + ChatColor.RED + ".");
            } else if (command.equalsIgnoreCase("warp") && parameters.length > 0 && parameters[0].equalsIgnoreCase("all")) {
                success = true;
                if (parameters.length > 1 && (!(sender instanceof Player) || sender.hasPermission("myultrrawarps.warpall") || sender.hasPermission("myultrawarps.admin")))
                    warpAll(1, sender, parameters);
                else if (parameters.length == 1)
                    sender.sendMessage(ChatColor.RED + "You forgot to tell me where you want all the players warped!");
                else
                    sender.sendMessage(ChatColor.RED + "Sorry, but you don't have permission to use " + COLOR + "/warpall" + ChatColor.RED + ".");
            } else if (command.equalsIgnoreCase("warp")) {
                success = true;
                if (parameters.length == 0) {
                    if (sender instanceof Player)
                        sender.sendMessage(ChatColor.RED + "You forgot to tell me what warp you want to warp to!");
                    else
                        console.sendMessage(ChatColor.RED + "Silly console! You can't warp! You have no body! :P");
                } else if (parameters.length < 3) {
                    if (sender instanceof Player
                            && (sender.hasPermission("myultrawarps.warptowarp") || sender.hasPermission("myultrawarps.warptowarp.other")
                                    || sender.hasPermission("myultrawarps.user") || sender.hasPermission("myultrawarps.admin")))
                        warp(sender, parameters);
                    else if (!(sender instanceof Player))
                        console.sendMessage(ChatColor.RED + "Silly console! You can't warp! You have no body! :P");
                    else
                        sender.sendMessage(ChatColor.RED + "Sorry, but you don't have permission to warp to preset warps.");
                } else {
                    if (sender instanceof Player
                            && (sender.hasPermission("myultrawarps.warptocoord") || sender.hasPermission("myultrawarps.user") || sender.hasPermission("myultrawarps.admin")))
                        warpToCoordinate(sender, parameters);
                    else if (!(sender instanceof Player))
                        console.sendMessage(ChatColor.RED + "Silly console! You can't warp! You have no body! :P");
                    else
                        sender.sendMessage(ChatColor.RED + "Sorry, but you don't have permission to warp to specific coordinates.");
                }
            } else if (command.equalsIgnoreCase("warptocoord")) {
                success = true;
                if (sender instanceof Player
                        && (sender.hasPermission("myultrawarps.warptocoord") || sender.hasPermission("myultrawarps.user") || sender.hasPermission("myultrawarps.admin"))
                        && parameters.length >= 3)
                    warpToCoordinate(sender, parameters);
                else if (!(sender instanceof Player))
                    console.sendMessage(ChatColor.RED + "Silly console! You can't warp! You have no body! :P");
                else if (parameters.length < 3)
                    sender.sendMessage("You forgot to tell me where you want to warp to!");
                else
                    sender.sendMessage(ChatColor.RED + "Sorry, but you don't have permission to warp to specific coordinates.");
            } else if (command.equalsIgnoreCase("default")
                    && parameters.length > 0
                    && (parameters[0].equalsIgnoreCase("warp") || parameters[0].toLowerCase().startsWith("warp:") || parameters[0].equalsIgnoreCase("nowarp")
                            || parameters[0].toLowerCase().startsWith("nowarp:") || (parameters.length > 1 && parameters[0].equalsIgnoreCase("no") && (parameters[1]
                            .equalsIgnoreCase("warp") || parameters[1].toLowerCase().startsWith("warp:"))))) {
                success = true;
                int extra_param = 1;
                if (parameters[0].equalsIgnoreCase("no"))
                    extra_param++;
                if (parameters.length > extra_param && parameters[extra_param].equalsIgnoreCase("message"))
                    extra_param++;
                if (parameters.length >= extra_param
                        && (!(sender instanceof Player) || sender.hasPermission("myultrawarps.config") || sender.hasPermission("myultrawarps.user") || sender
                                .hasPermission("myultrawarps.admin")))
                    changeDefaultMessage(extra_param, sender, parameters);
                else if (sender instanceof Player && !sender.hasPermission("myultrawarps.config") && !sender.hasPermission("myultrawarps.user")
                        && !sender.hasPermission("myultrawarps.admin"))
                    if (parameters[0].equalsIgnoreCase("no"))
                        sender.sendMessage(ChatColor.RED + "Sorry, but you're not allowed to use " + COLOR + "/default no " + parameters[1].toLowerCase() + ChatColor.RED
                                + ".");
                    else
                        sender.sendMessage(ChatColor.RED + "Sorry, but you're not allowed to use " + COLOR + "/default " + parameters[0].toLowerCase() + ChatColor.RED + ".");
                else
                    sender.sendMessage(ChatColor.RED + "You forgot to tell me the new default message!");

            } else if (command.equalsIgnoreCase("changewarp") || command.equalsIgnoreCase("modifywarp")
                    || ((command.equalsIgnoreCase("change") || command.equalsIgnoreCase("modify")) && (parameters.length == 0 || !parameters[0].equalsIgnoreCase("warp")))) {
                success = true;
                if ((!(sender instanceof Player) || sender.hasPermission("myultrawarps.change") || sender.hasPermission("myultrawarps.change.other")
                        || sender.hasPermission("myultrawarps.user") || sender.hasPermission("myultrawarps.admin"))
                        && parameters.length > 1)
                    changeWarp(0, sender, parameters);
                else if (parameters.length == 0)
                    sender.sendMessage(ChatColor.RED + "You didn't tell me what warp to change or how to change it!");
                else if (parameters.length == 1)
                    sender.sendMessage(ChatColor.RED + "You didn't tell me what to change!");
                else
                    sender.sendMessage(ChatColor.RED + "Sorry, but you don't have permission to use " + COLOR + "/" + command.toLowerCase() + ChatColor.RED + ".");
            } else if ((command.equalsIgnoreCase("change") || command.equalsIgnoreCase("modify")) && parameters.length > 0 && parameters[0].equalsIgnoreCase("warp")) {
                success = true;
                if ((!(sender instanceof Player) || (sender.hasPermission("myultrawarps.change") || sender.hasPermission("myultrawarps.change.other")
                        || sender.hasPermission("myultrawarps.user") || sender.hasPermission("myultrawarps.admin")))
                        && parameters.length > 2)
                    changeWarp(1, sender, parameters);
                else if (parameters.length < 2)
                    sender.sendMessage(ChatColor.RED + "You didn't tell me what warp to change or how to change it!");
                else if (parameters.length == 2)
                    sender.sendMessage(ChatColor.RED + "You didn't tell me what to change!");
                else
                    sender.sendMessage(ChatColor.RED + "Sorry, but you don't have permission to use " + COLOR + "/" + command.toLowerCase() + " warp" + ChatColor.RED + ".");
            } else if (command.equalsIgnoreCase("deletewarp") || command.equalsIgnoreCase("removewarp")
                    || ((command.equalsIgnoreCase("delete") || command.equalsIgnoreCase("remove")) && (parameters.length == 0 || !parameters[0].equalsIgnoreCase("warp")))) {
                success = true;
                if ((!(sender instanceof Player) || (sender.hasPermission("myultrawarps.delete") || sender.hasPermission("myultrawarps.delete.other")
                        || sender.hasPermission("myultrawarps.user") || sender.hasPermission("myultrawarps.admin")))
                        && parameters.length > 0)
                    deleteWarp(0, sender, parameters);
                else if (parameters.length == 0)
                    sender.sendMessage(ChatColor.RED + "You forgot to tell me what warp to delete!");
                else
                    sender.sendMessage(ChatColor.RED + "Sorry, but you don't have permission to use " + COLOR + "/" + command.toLowerCase() + ChatColor.RED + ".");
            } else if ((command.equalsIgnoreCase("delete") || command.equalsIgnoreCase("remove")) && parameters.length > 0 && parameters[0].equalsIgnoreCase("warp")) {
                success = true;
                if ((!(sender instanceof Player) || (sender.hasPermission("myultrawarps.delete") || sender.hasPermission("myultrawarps.delete.other")
                        || sender.hasPermission("myultrawarps.user") || sender.hasPermission("myultrawarps.admin")))
                        && parameters.length > 1)
                    deleteWarp(1, sender, parameters);
                else if (parameters.length == 1)
                    sender.sendMessage(ChatColor.RED + "You forgot to tell me what warp to delete!");
                else
                    sender.sendMessage(ChatColor.RED + "Sorry, but you don't have permission to use " + COLOR + "/" + command.toLowerCase() + " warp" + ChatColor.RED + ".");
            } else if ((command.equalsIgnoreCase("back") || command.equalsIgnoreCase("return") || command.equalsIgnoreCase("last"))) {
                success = true;
                if (sender instanceof Player
                        && (sender.hasPermission("myultrawarps.back") || sender.hasPermission("myultrawarps.user") || sender.hasPermission("myultrawarps.admin")))
                    back(sender, parameters);
                else if (!(sender instanceof Player))
                    console.sendMessage(ChatColor.RED + "How exactly can you go back to your last warp if you can't warp in the first place?");
                else
                    sender.sendMessage(ChatColor.RED + "Sorry, but you don't have permission to use " + COLOR + "/" + command.toLowerCase() + ChatColor.RED + ".");
            } else if (command.equalsIgnoreCase("jump") || command.equalsIgnoreCase("j")) {
                success = true;
                if (sender instanceof Player
                        && (sender.hasPermission("myultrawarps.jump") || sender.hasPermission("myultrawarps.user") || sender.hasPermission("myultrawarps.admin")))
                    jump(sender);
                else if (!(sender instanceof Player))
                    console.sendMessage(COLOR + "You jumped! " + ChatColor.RED + "Just kidding. You're a console and you have no body.");
                else
                    sender.sendMessage(ChatColor.RED + "Sorry, but you don't have permission to use " + COLOR + "/jump" + ChatColor.RED + ".");
            } else if (command.equalsIgnoreCase("linkwarp") || (command.equalsIgnoreCase("link") && (parameters.length == 0 || !parameters[0].equalsIgnoreCase("warp")))) {
                success = true;
                if (sender instanceof Player
                        && (sender.hasPermission("myultrawarps.link") || sender.hasPermission("myultrawarps.link.other") || sender.hasPermission("myultrawarps.user") || sender
                                .hasPermission("myultrawarps.admin")) && parameters.length > 0)
                    linkWarp(0, sender, parameters);
                else if (parameters.length == 0)
                    sender.sendMessage(ChatColor.RED + "You forgot to tell me what warp I should use!");
                else if (!(sender instanceof Player))
                    console.sendMessage(ChatColor.RED + "Point out the switch you want to link \"" + parameters[0] + "\" to. Oh, wait. You can't. You're a console.");
                else
                    sender.sendMessage(ChatColor.RED + "Sorry, but you don't have permission to use " + COLOR + "/" + command.toLowerCase() + ChatColor.RED + ".");
            } else if (command.equalsIgnoreCase("link") && parameters.length > 0 && parameters[0].equalsIgnoreCase("warp")) {
                success = true;
                if (sender instanceof Player
                        && (sender.hasPermission("myultrawarps.link") || sender.hasPermission("myultrawarps.link.other") || sender.hasPermission("myultrawarps.user") || sender
                                .hasPermission("myultrawarps.admin")) && parameters.length > 1)
                    linkWarp(1, sender, parameters);
                else if (parameters.length == 1)
                    sender.sendMessage(ChatColor.RED + "You forgot to tell me what warp I should use!");
                else if (!(sender instanceof Player))
                    console.sendMessage(ChatColor.RED + "Point out the switch you want to link \"" + parameters[0] + "\" to. Oh, wait. You can't. You're a console.");
                else
                    sender.sendMessage(ChatColor.RED + "Sorry, but you don't have permission to use " + COLOR + "/link warp" + ChatColor.RED + ".");
            } else if (command.equalsIgnoreCase("unlinkwarp") || (command.equalsIgnoreCase("unlink") && (parameters.length == 0 || !parameters[0].equalsIgnoreCase("warp")))) {
                success = true;
                if (!(sender instanceof Player) || sender.hasPermission("myultrawarps.unlink") || sender.hasPermission("myultrawarps.unlink.other")
                        || sender.hasPermission("myultrawarps.user") || sender.hasPermission("myultrawarps.admin"))
                    unlinkWarp(0, sender, parameters);
                else
                    sender.sendMessage(ChatColor.RED + "Sorry, but you don't have permission to use " + COLOR + "/" + command.toLowerCase() + ChatColor.RED + ".");
            } else if (command.equalsIgnoreCase("unlink") && parameters.length > 0 && parameters[0].equalsIgnoreCase("warp")) {
                success = true;
                if (!(sender instanceof Player) || sender.hasPermission("myultrawarps.unlink") || sender.hasPermission("myultrawarps.unlink.other")
                        || sender.hasPermission("myultrawarps.user") || sender.hasPermission("myultrawarps.admin"))
                    unlinkWarp(1, sender, parameters);
                else
                    sender.sendMessage(ChatColor.RED + "Sorry, but you don't have permission to use " + COLOR + "/unlink warp" + ChatColor.RED + ".");
            } else if (command.equalsIgnoreCase("movewarp") || command.equalsIgnoreCase("translatewarp")
                    || (((command.equalsIgnoreCase("move") || command.equalsIgnoreCase("translate"))) && (parameters.length == 0 || !parameters[0].equalsIgnoreCase("warp")))) {
                success = true;
                if (sender instanceof Player
                        && (sender.hasPermission("myultrawarps.change") || sender.hasPermission("myultrawarps.change.other") || sender.hasPermission("myultrawarps.user") || sender
                                .hasPermission("myultrawarps.admin")) && parameters.length > 0)
                    moveWarp(0, sender, parameters);
                else if (!(sender instanceof Player))
                    console.sendMessage(ChatColor.RED + "You can't move any warps because you can't point out a new location for the warp! You have no body!");
                else if (parameters.length == 0)
                    sender.sendMessage(ChatColor.RED + "You forgot to tell me which warp to move!");
                else
                    sender.sendMessage(ChatColor.RED + "Sorry, but you don't have permission to use " + COLOR + "/" + command.toLowerCase() + ChatColor.RED + ".");
            } else if ((command.equalsIgnoreCase("move") || command.equalsIgnoreCase("translate")) && parameters.length > 0 && parameters[0].equalsIgnoreCase("warp")) {
                success = true;
                if (sender instanceof Player
                        && (sender.hasPermission("myultrawarps.change") || sender.hasPermission("myultrawarps.change.other") || sender.hasPermission("myultrawarps.user") || sender
                                .hasPermission("myultrawarps.admin")) && parameters.length > 1)
                    moveWarp(1, sender, parameters);
                else if (!(sender instanceof Player))
                    console.sendMessage(ChatColor.RED + "You can't move any warps because you can't point out a new location for the warp! You have no body!");
                else if (parameters.length == 1)
                    sender.sendMessage(ChatColor.RED + "You forgot to tell me which warp to move!");
                else
                    sender.sendMessage(ChatColor.RED + "Sorry, but you don't have permission to use " + COLOR + "/" + command.toLowerCase() + " warp" + ChatColor.RED + ".");
            } else if (command.equalsIgnoreCase("home") || command.equalsIgnoreCase("h")) {
                success = true;
                if (sender instanceof Player
                        && (sender.hasPermission("myultrawarps.home") || sender.hasPermission("myultrawarps.home.other") || sender.hasPermission("myultrawarps.user") || sender
                                .hasPermission("myultrawarps.admin")))
                    home(sender, parameters);
                else if (!(sender instanceof Player))
                    console.sendMessage(ChatColor.RED + "You can't have a home! YOU...ARE...A...CONSOLE!");
                else
                    sender.sendMessage(ChatColor.RED + "Sorry, but you don't have permission to use " + COLOR + "/home" + ChatColor.RED + ".");
            } else if ((command.equalsIgnoreCase("mUW") || command.equalsIgnoreCase("myUltraWarps"))
                    && (parameters.length == 0 || (parameters.length > 0 && parameters[0].equalsIgnoreCase("help")))) {
                success = true;
                displayHelp(sender, parameters);
            } else if ((command.equalsIgnoreCase("mUW") || command.equalsIgnoreCase("myUltraWarps"))
                    && parameters.length > 1
                    && parameters[0].equalsIgnoreCase("save")
                    && (parameters[1].equalsIgnoreCase("warps") || (parameters.length > 2 && parameters[1].equalsIgnoreCase("the") && parameters[2].equalsIgnoreCase("warps")))) {
                success = true;
                if (!(sender instanceof Player) || sender.hasPermission("myultrawarps.admin"))
                    saveTheWarps(sender, true);
                else if (command.equalsIgnoreCase("myUltraWarps"))
                    sender.sendMessage(ChatColor.RED + "Sorry, but you don't have permission to use " + COLOR + "/myUltraWarps save" + ChatColor.RED + ".");
                else
                    sender.sendMessage(ChatColor.RED + "Sorry, but you don't have permission to use " + COLOR + "/mUW save" + ChatColor.RED + ".");
            } else if ((command.equalsIgnoreCase("mUW") || command.equalsIgnoreCase("myUltraWarps"))
                    && parameters.length > 1
                    && parameters[0].equalsIgnoreCase("save")
                    && (parameters[1].equalsIgnoreCase("switches") || (parameters.length > 2 && parameters[1].equalsIgnoreCase("the") && parameters[2]
                            .equalsIgnoreCase("switches")))) {
                success = true;
                if (!(sender instanceof Player) || sender.hasPermission("myultrawarps.admin"))
                    saveTheSwitches(sender, true);
                else if (command.equalsIgnoreCase("myUltraWarps"))
                    sender.sendMessage(ChatColor.RED + "Sorry, but you don't have permission to use " + COLOR + "/myUltraWarps save" + ChatColor.RED + ".");
                else
                    sender.sendMessage(ChatColor.RED + "Sorry, but you don't have permission to use " + COLOR + "/mUW save" + ChatColor.RED + ".");
            } else if ((command.equalsIgnoreCase("mUW") || command.equalsIgnoreCase("myUltraWarps"))
                    && parameters.length > 1
                    && parameters[0].equalsIgnoreCase("save")
                    && (parameters[1].equalsIgnoreCase("config") || (parameters.length > 2 && parameters[1].equalsIgnoreCase("the") && parameters[2]
                            .equalsIgnoreCase("config")))) {
                success = true;
                if (!(sender instanceof Player) || sender.hasPermission("myultrawarps.admin"))
                    saveTheConfig(sender, true);
                else if (command.equalsIgnoreCase("myUltraWarps"))
                    sender.sendMessage(ChatColor.RED + "Sorry, but you don't have permission to use " + COLOR + "/myUltraWarps save" + ChatColor.RED + ".");
                else
                    sender.sendMessage(ChatColor.RED + "Sorry, but you don't have permission to use " + COLOR + "/mUW save" + ChatColor.RED + ".");
            } else if ((command.equalsIgnoreCase("mUW") || command.equalsIgnoreCase("myUltraWarps")) && parameters.length == 1 && parameters[0].equalsIgnoreCase("save")) {
                success = true;
                if (!(sender instanceof Player) || sender.hasPermission("myultrawarps.admin")) {
                    saveTheWarps(sender, true);
                    saveTheSwitches(sender, true);
                    saveTheConfig(sender, true);
                } else if (command.equalsIgnoreCase("myUltraWarps"))
                    sender.sendMessage(ChatColor.RED + "Sorry, but you don't have permission to use " + COLOR + "/myUltraWarps save" + ChatColor.RED + ".");
                else
                    sender.sendMessage(ChatColor.RED + "Sorry, but you don't have permission to use " + COLOR + "/mUW save" + ChatColor.RED + ".");
            } else if ((command.equalsIgnoreCase("myUltraWarps") || command.equalsIgnoreCase("mUW"))
                    && parameters.length > 1
                    && parameters[0].equalsIgnoreCase("load")
                    && (parameters[1].equalsIgnoreCase("warps") || (parameters.length > 2 && parameters[1].equalsIgnoreCase("the") && parameters[2].equalsIgnoreCase("warps")))) {
                success = true;
                if (!(sender instanceof Player) || sender.hasPermission("myultrawarps.admin"))
                    loadTheWarps(sender);
                else if (command.equalsIgnoreCase("myUltraWarps"))
                    sender.sendMessage(ChatColor.RED + "Sorry, but you don't have permission to use " + COLOR + "/myUltraWarps load" + ChatColor.RED + ".");
                else
                    sender.sendMessage(ChatColor.RED + "Sorry, but you don't have permission to use " + COLOR + "/mUW load" + ChatColor.RED + ".");
            } else if ((command.equalsIgnoreCase("myUltraWarps") || command.equalsIgnoreCase("mUW"))
                    && parameters.length > 1
                    && parameters[0].equals("load")
                    && (parameters[1].equalsIgnoreCase("switches") || (parameters.length > 2 && parameters[1].equalsIgnoreCase("the") && parameters[2]
                            .equalsIgnoreCase("switches")))) {
                success = true;
                if (!(sender instanceof Player) || sender.hasPermission("myultrawarps.admin"))
                    loadTheSwitches(sender);
                else if (command.equalsIgnoreCase("myUltraWarps"))
                    sender.sendMessage(ChatColor.RED + "Sorry, but you don't have permission to use " + COLOR + "/myUltraWarps load" + ChatColor.RED + ".");
                else
                    sender.sendMessage(ChatColor.RED + "Sorry, but you don't have permission to use " + COLOR + "/mUW load" + ChatColor.RED + ".");
            } else if ((command.equalsIgnoreCase("myUltraWarps") || command.equalsIgnoreCase("mUW"))
                    && parameters.length > 1
                    && parameters[0].equalsIgnoreCase("load")
                    && (parameters[1].equalsIgnoreCase("config") || (parameters.length > 2 && parameters[1].equalsIgnoreCase("the") && parameters[2]
                            .equalsIgnoreCase("config")))) {
                success = true;
                if (!(sender instanceof Player) || sender.hasPermission("myultrawarps.admin"))
                    loadTheConfig(sender);
                else if (command.equalsIgnoreCase("myUltraWarps"))
                    sender.sendMessage(ChatColor.RED + "Sorry, but you don't have permission to use " + COLOR + "/myUltraWarps load" + ChatColor.RED + ".");
                else
                    sender.sendMessage(ChatColor.RED + "Sorry, but you don't have permission to use " + COLOR + "/mUW load" + ChatColor.RED + ".");
            } else if ((command.equalsIgnoreCase("myUltraWarps") || command.equalsIgnoreCase("mUW")) && parameters.length == 1 && parameters[0].equalsIgnoreCase("load")) {
                success = true;
                if (!(sender instanceof Player) || sender.hasPermission("myultrawarps.admin")) {
                    loadTheWarps(sender);
                    loadTheSwitches(sender);
                    loadTheConfig(sender);
                } else if (command.equalsIgnoreCase("myUltraWarps"))
                    sender.sendMessage(ChatColor.RED + "Sorry, but you don't have permission to use " + COLOR + "/myUltraWarps load" + ChatColor.RED + ".");
                else
                    sender.sendMessage(ChatColor.RED + "Sorry, but you don't have permission to use " + COLOR + "/mUW load" + ChatColor.RED + ".");
            } else if ((command.equalsIgnoreCase("myUltraWarps") || command.equalsIgnoreCase("mUW")) && parameters.length >= 1
                    && parameters[0].toLowerCase().startsWith("update")) {
                success = true;
                if (!(sender instanceof Player) || sender.hasPermission("myultrawarps.admin"))
                    checkForUpdates(sender);
                else if (command.equalsIgnoreCase("myUltraWarps"))
                    sender.sendMessage(ChatColor.RED + "Sorry, but you don't have permission to use " + COLOR + "/myUltraWarps update" + ChatColor.RED + ".");
                else
                    sender.sendMessage(ChatColor.RED + "Sorry, but you don't have permission to use " + COLOR + "/mUW update" + ChatColor.RED + ".");
            } else if ((command.equalsIgnoreCase("mUW") || command.equalsIgnoreCase("myUltraWarps")) && parameters.length > 0
                    && parameters[0].toLowerCase().startsWith("debug")) {
                if (sender instanceof Player && !sender.hasPermission("myultrawarps.admin"))
                    if (command.equalsIgnoreCase("myUltraWarps"))
                        sender.sendMessage(ChatColor.RED + "Sorry, but you don't have permission to use " + COLOR + "/myUltraWarps debug" + ChatColor.RED + ".");
                    else
                        sender.sendMessage(ChatColor.RED + "Sorry, but you don't have permission to use " + COLOR + "/mUW debug" + ChatColor.RED + ".");
                else {
                    String sender_name = "console";
                    if (sender instanceof Player)
                        sender_name = ((Player) sender).getName();
                    if (debuggers.contains(sender_name)) {
                        debuggers.remove(sender_name);
                        sender.sendMessage(COLOR + "Bugs swatted!");
                    } else {
                        debuggers.add(sender_name);
                        sender.sendMessage(COLOR + "Let's fix some bugs! I'll get the fly swatter!");
                    }
                }
                return true;
            } else if (command.equalsIgnoreCase("mUW") || command.equalsIgnoreCase("myUltraWarps")) {
                String[] new_parameters = new String[0];
                if (parameters.length > 0) {
                    new_parameters = new String[parameters.length - 1];
                    for (int i = 1; i < parameters.length; i++)
                        new_parameters[i - 1] = parameters[i];
                }
                success = onCommand(sender, cmd, parameters[0], new_parameters);
            } else if (command.equalsIgnoreCase("top") || command.equalsIgnoreCase("t")) {
                success = true;
                if (sender instanceof Player
                        && (sender.hasPermission("myultrawarps.top") || sender.hasPermission("myultrawarps.user") || sender.hasPermission("myultrawarps.admin")))
                    top(sender);
                else if (!(sender instanceof Player))
                    console.sendMessage(ChatColor.RED + "You don't have a body! Stop trying to warp!");
                else
                    sender.sendMessage(ChatColor.RED + "Sorry, but you don't have permission to use " + COLOR + "/top" + ChatColor.RED + ".");
            } else if (command.equalsIgnoreCase("switchinfo")) {
                success = true;
                if (!(sender instanceof Player)
                        || (sender.hasPermission("myultrawarps.switchinfo") || sender.hasPermission("myultrawarps.user") || sender.hasPermission("myultrawarps.admin"))) {
                    switchInfo(0, sender, parameters);
                } else
                    sender.sendMessage(ChatColor.RED + "Sorry, but you don't have permission to use " + COLOR + "/switchinfo" + ChatColor.RED + ".");
            } else if (command.equalsIgnoreCase("switch") && parameters.length > 0 && parameters[0].equalsIgnoreCase("info")) {
                success = true;
                if (!(sender instanceof Player)
                        || (sender.hasPermission("myultrawarps.switchinfo") || sender.hasPermission("myultrawarps.user") || sender.hasPermission("myultrawarps.admin"))) {
                    switchInfo(1, sender, parameters);
                } else
                    sender.sendMessage(ChatColor.RED + "Sorry, but you don't have permission to use " + COLOR + "/switch info" + ChatColor.RED + ".");
            } else if (command.equalsIgnoreCase("to") || command.equalsIgnoreCase("find")) {
                success = true;
                if (sender instanceof Player
                        && (sender.hasPermission("myultrawarps.to") || sender.hasPermission("myultrawarps.to.norequest") || sender.hasPermission("myultrawarps.user") || sender
                                .hasPermission("myultrawarps.admin")) && parameters.length > 0)
                    to(sender, parameters);
                else if (!(sender instanceof Player))
                    console.sendMessage(ChatColor.RED + "For the last time: You cannot warp! YOU HAVE NO BODY!");
                else if (parameters.length == 0)
                    sender.sendMessage(ChatColor.RED + "You forgot to tell me who I should teleport you to!");
                else
                    sender.sendMessage(ChatColor.RED + "Sorry, but you don't have permission to use " + COLOR + "/" + command.toLowerCase() + ChatColor.RED + ".");
            } else if (command.equalsIgnoreCase("from") || command.equalsIgnoreCase("pull") || command.equalsIgnoreCase("yank") || command.equalsIgnoreCase("bring")
                    || command.equalsIgnoreCase("get")) {
                success = true;
                if (sender instanceof Player
                        && (sender.hasPermission("myultrawarps.from") || sender.hasPermission("myultrawarps.from.norequest") || sender.hasPermission("myultrawarps.user") || sender
                                .hasPermission("myultrawarps.admin")) && parameters.length > 0)
                    from(sender, parameters);
                else if (!(sender instanceof Player))
                    console.sendMessage(ChatColor.RED + "No more trying to warp! It's not going to work! You're a CONSOLE!");
                else if (parameters.length == 0)
                    sender.sendMessage(ChatColor.RED + "You forgot to tell me who I should teleport to you!");
                else
                    sender.sendMessage(ChatColor.RED + "Sorry, but you don't have permission to use " + COLOR + "/" + command.toLowerCase() + ChatColor.RED + ".");
            } else if (command.equalsIgnoreCase("send")) {
                success = true;
                if ((!(sender instanceof Player) || sender.hasPermission("myultrawarps.send") || sender.hasPermission("myultrawarps.admin")) && parameters.length >= 2)
                    send(sender, parameters);
                else if (parameters.length == 0)
                    sender.sendMessage(ChatColor.RED + "You forgot to tell me who you want me to send where!");
                else if (parameters.length == 1)
                    sender.sendMessage(ChatColor.RED + "You forgot to tell me where to send " + parameters[0] + "!");
                else
                    sender.sendMessage(ChatColor.RED + "Sorry, but you don't have permission to use " + COLOR + "/send" + ChatColor.RED + ".");
            } else if (command.equalsIgnoreCase("warps") && parameters.length > 0 && (parameters[0].equalsIgnoreCase("around") || parameters[0].equalsIgnoreCase("near"))) {
                success = true;
                if ((!(sender instanceof Player) || sender.hasPermission("myultrawarps.warpsaround") || sender.hasPermission("myultrawarps.user") || sender
                        .hasPermission("myultrawarps.admin"))
                        && parameters.length > 1)
                    warpsAround(1, sender, parameters);
                else if (parameters.length > 1)
                    sender.sendMessage(ChatColor.RED + "Sorry, but you're not allowed to use " + COLOR + "/warps " + parameters[0].toLowerCase() + ChatColor.RED + ".");
                else
                    sender.sendMessage(ChatColor.RED + "You forgot to tell me where you want the search to be centered!");
            } else if (command.equalsIgnoreCase("warpsaround") || command.equalsIgnoreCase("warpsnear")) {
                success = true;
                if ((!(sender instanceof Player) || sender.hasPermission("myultrawarps.warpsaround") || sender.hasPermission("myultrawarps.user") || sender
                        .hasPermission("myultrawarps.admin"))
                        && parameters.length > 0)
                    warpsAround(0, sender, parameters);
                else if (parameters.length > 0)
                    sender.sendMessage(ChatColor.RED + "Sorry, but you're not allowed to use " + COLOR + "/" + command.toLowerCase() + ChatColor.RED + ".");
                else
                    sender.sendMessage(ChatColor.RED + "You forgot to tell me where you want the search to be centered!");
            } else if (command.equalsIgnoreCase("maxwarps") || command.equalsIgnoreCase("maximumwarps")) {
                success = true;
                if (parameters.length > 0 && (!(sender instanceof Player) || sender.hasPermission("myultrawarps.admin")))
                    changeMaxWarps(0, sender, parameters);
                else if (sender instanceof Player && !sender.hasPermission("myultrawarps.admin"))
                    sender.sendMessage(ChatColor.RED + "Sorry, but you don't have permission to use " + COLOR + "/" + command.toLowerCase() + ChatColor.RED + ".");
                else
                    sender.sendMessage(ChatColor.RED + "You forgot to tell me what you want me to change the max warps to!");
            } else if ((command.equalsIgnoreCase("max") || command.equalsIgnoreCase("maximum")) && parameters.length > 0 && parameters[0].equalsIgnoreCase("warps")) {
                success = true;
                if (parameters.length > 1 && (!(sender instanceof Player) || sender.hasPermission("myultrawarps.admin")))
                    changeMaxWarps(1, sender, parameters);
                else if (sender instanceof Player && !sender.hasPermission("myultrawarps.admin"))
                    sender.sendMessage(ChatColor.RED + "Sorry, but you don't have permission to use " + COLOR + "/" + command.toLowerCase() + " warps" + ChatColor.RED + ".");
                else
                    sender.sendMessage(ChatColor.RED + "You forgot to tell me what you want me to change the max warps to!");
            } else if (command.equalsIgnoreCase("forward") || command.equalsIgnoreCase("fwd")) {
                success = true;
                if (sender instanceof Player
                        && (sender.hasPermission("myultrawarps.admin") || sender.hasPermission("myultrawarps.user") || sender.hasPermission("myultrawarps.back")))
                    forward(sender, parameters);
                else if (!(sender instanceof Player))
                    sender.sendMessage(ChatColor.RED + "You're a console!! How can I warp you somewhere you've already warped if you can't warp at all in the first place?!");
                else
                    sender.sendMessage(ChatColor.RED + "Sorry, but you don't have permission to use " + COLOR + "/" + command.toLowerCase() + ChatColor.RED + ".");
            } else if (command.equalsIgnoreCase("deathfwd") || command.equals("fwddeath") || command.equalsIgnoreCase("dfwd") || command.equalsIgnoreCase("fwdd")
                    || (command.equalsIgnoreCase("death") && parameters.length > 0 && (parameters[0].equalsIgnoreCase("fwd") || parameters[0].equalsIgnoreCase("forward")))) {
                success = true;
                if (sender instanceof Player
                        && (sender.hasPermission("myultrawarps.admin") || sender.hasPermission("myultrawarps.user") || sender.hasPermission("myultrawarps.death")))
                    deathForward(sender, parameters);
                else if (!(sender instanceof Player))
                    sender.sendMessage(ChatColor.RED
                            + "You can't die, you can't warp, and you can't warp to your death location. How do you suppose I would move you forward in your death history?");
                else
                    sender.sendMessage(ChatColor.RED + "Sorry, but you don't have permission to use " + COLOR + "/" + command.toLowerCase() + ChatColor.RED + ".");
            } else if (command.equalsIgnoreCase("death")) {
                success = true;
                if (sender instanceof Player
                        && (sender.hasPermission("myultrawarps.death") || sender.hasPermission("myultrawarps.user") || sender.hasPermission("myultrawarps.admin")))
                    death(sender, parameters);
                else if (!(sender instanceof Player))
                    sender.sendMessage(ChatColor.RED + "You can't go back to your last death location! You can't warp! You can't even die!");
                else
                    sender.sendMessage(ChatColor.RED + "Sorry, but you don't have permission to use " + COLOR + "/" + command.toLowerCase() + ChatColor.RED + ".");
            } else if (command.equalsIgnoreCase("blocklist") || (command.equalsIgnoreCase("block") && parameters.length > 0 && parameters[0].equalsIgnoreCase("list"))) {
                success = true;
                if (sender instanceof Player
                        && (sender.hasPermission("myultrawarps.block") || sender.hasPermission("myultrawarps.user") || sender.hasPermission("myultrawarps.admin")))
                    if (command.equalsIgnoreCase("blocklist"))
                        blockList(0, sender, parameters);
                    else
                        blockList(1, sender, parameters);
                else if (!(sender instanceof Player))
                    sender.sendMessage(ChatColor.RED + "You can't even block people!");
                else if (command.equalsIgnoreCase("block"))
                    sender.sendMessage(ChatColor.RED + "Sorry, but you don't have permission to use " + COLOR + "/block list" + ChatColor.RED + ".");
                else
                    sender.sendMessage(ChatColor.RED + "Sorry, but you don't have permission to use " + COLOR + "/blocklist" + ChatColor.RED + ".");
            } else if (command.equalsIgnoreCase("block")) {
                success = true;
                if (sender instanceof Player
                        && (sender.hasPermission("myultrawarps.block") || sender.hasPermission("myultrawarps.user") || sender.hasPermission("myultrawarps.admin"))
                        && parameters.length > 0)
                    block(sender, parameters);
                else if (!(sender instanceof Player))
                    sender.sendMessage(ChatColor.RED + "You can't block anyone! People can't even send you requests in the first place!");
                else if (parameters.length > 0)
                    sender.sendMessage(ChatColor.RED + "Sorry, but you don't have permission to use " + COLOR + "/block" + ChatColor.RED + ".");
                else
                    sender.sendMessage(ChatColor.RED + "You forgot to tell me who you want to block!");
            } else if (command.equalsIgnoreCase("unblock")) {
                success = true;
                if (sender instanceof Player
                        && (sender.hasPermission("myultrawarps.block") || sender.hasPermission("myultrawarps.user") || sender.hasPermission("myultrawarps.admin"))
                        && parameters.length > 0)
                    unblock(sender, parameters);
                else if (!(sender instanceof Player))
                    sender.sendMessage(ChatColor.RED + "Do I even need to explain why you can't use this command?");
                else if (parameters.length > 0)
                    sender.sendMessage(ChatColor.RED + "Sorry, but you don't have permission to use " + COLOR + "/unblock" + ChatColor.RED + ".");
                else
                    sender.sendMessage(ChatColor.RED + "You forgot to tell me who you want to unblock!");
            }
            return success;
        } catch (Exception exception) {
            processException("There was a problem in onCommand()!", exception);
            return false;
        }
    }

    // String utils
    /** This method replaces every instance of each String given in the text given with another String. This method has a few advantages over Java's standard
     * <tt>String.replaceAll(String, String)</tt> method: <b>1)</b> this method can replace multiple Strings with other Strings using a single method while
     * <tt>String.replaceAll(String, String)</tt> only has the ability to replace one String with one other String and <b>2)</b> this method treats brackets ("[]"), hyphens
     * ("-"), braces ("{}"), and other symbols normally whereas many of these symbols have special meanings in <tt>String.replaceAll(String, String)</tt>.
     * 
     * @param text
     *            is the text that must be modified.
     * @param changes
     *            are the changes that must be made to <b><tt>text</b></tt>. Every even-numbered item in this list will be replaced by the next (odd-numbered) String given;
     *            for example, if the four parameters given for <b><tt>changes</b></tt> are <tt>replaceAll(...,"wierd", "weird", "[player]", player.getName())</tt>, this
     *            method will replace all instances of "wierd" with "weird" and all instances of "[player]" with <tt>player.getName()</tt> in <b> <tt>text</b></tt>.
     * @return <b><tt>text</b></tt> will all modifications given in <b><tt>changes</b></tt> made. */
    public static String replaceAll(String text, String... changes) {
        if (changes.length == 0)
            return text;
        for (int j = 0; j < changes.length; j += 2) {
            if (!text.toLowerCase().contains(changes[j].toLowerCase()))
                return text;
            for (int i = 0; text.length() >= i + changes[j].length(); i++) {
                if (text.substring(i, i + changes[j].length()).equalsIgnoreCase(changes[j])) {
                    text = text.substring(0, i) + changes[j + 1] + text.substring(i + changes[j].length());
                    i += changes[j + 1].length() - 1;
                }
                if (!text.toLowerCase().contains(changes[j].toLowerCase()))
                    break;
            }
        }
        return text;
    }

    /** This method is used to interpret the answers to questions <b>1)</b> in the chat and <b>2)</b> in the <tt>config.txt</tt> file for myUltraWarps.
     * 
     * @param sender
     *            is the Player that sent the response message or <tt>console</tt> for <tt>config.txt</tt> questions.
     * @param unformatted_response
     *            is the raw String message that will be formatted in this message to be all lower case with no punctuation and analyzed for a "yes" or "no" answer.
     * @param current_status_line
     *            is for use with the <tt>config.txt</tt> questions only; it allows this method to default to the current status of a configuration if no answer is given to a
     *            <tt>config.txt</tt> question.
     * @param current_status_is_true_message
     *            is for use with the <tt>config.txt</tt> questions only; it allows this method to compare <b>current_status_line</b> to this message to determine whether or
     *            not the current status of the configuration handled by this config question is <b>true</b> or <b>false</b>.
     * @return <b>for chat responses:</b> <b>true</b> if the response matches one of the words or phrases in <tt>yeses</tt>, <b>false</b> if the response matches one of the
     *         words or phrases in <tt>nos</tt>, or <b>null</b> if the message did not seem to answer the question. <b>for <tt>config.txt</tt> question responses:</b>
     *         <b>true</b> if the answer to the question matches one of the words or phrases in <tt>yeses</tt>, <b>false</b> if the answer to the question matches one of the
     *         words or phrases in <tt>nos</tt>. If there is no answer to the question or the answer does not match a "yes" or a "no" response, it will return <b>true</b> if
     *         <b><tt>current_status_line</tt></b> matches <b> <tt>current_status_is_true_message</tt></b> or <b>false</b> if it does not. */
    public static Boolean getResponse(CommandSender sender, String unformatted_response, String current_status_line, String current_status_is_true_message) {
        try {
            String[] yeses =
                    { "yes", "yeah", "yea", "yep", "sure", "why not", "ok", "okay", "do it", "fine", "whatever", "very well", "accept", "tpa", "cool", "hell yeah",
                            "hells yeah", "hells yes", "come", "ya", "ja", "k ", "kk" }, nos =
                    { "no", "nah", "nope", "no thanks", "no don't", "hell no", "shut up", "ignore", "it's not", "its not", "creeper", "unsafe", "wait", "one ", "1 " };
            boolean said_yes = false, said_no = false;
            String formatted_response = unformatted_response;
            // elimiate unnecessary spaces and punctuation
            while (formatted_response.startsWith(" "))
                formatted_response = formatted_response.substring(1);
            while (formatted_response.endsWith(" "))
                formatted_response = formatted_response.substring(0, formatted_response.length() - 1);
            formatted_response = formatted_response.toLowerCase();
            // check their response
            for (String yes : yeses)
                if (formatted_response.startsWith(yes))
                    said_yes = true;
            if (said_yes)
                return true;
            else {
                for (String no : nos)
                    if (formatted_response.startsWith(no))
                        said_no = true;
                if (said_no)
                    return false;
                else if (current_status_line != null) {
                    if (!formatted_response.equals("")) {
                        if (unformatted_response.substring(0, 1).equals(" "))
                            unformatted_response = unformatted_response.substring(1);
                        sender.sendMessage(ChatColor.RED + "I don't know what \"" + unformatted_response + "\" means.");
                    }
                    while (current_status_line.startsWith(" "))
                        current_status_line = current_status_line.substring(1);
                    if (current_status_line.startsWith(current_status_is_true_message))
                        return true;
                    else
                        return false;
                } else
                    return null;
            }
        } catch (Exception exception) {
            processException("There was a problem in onEnable()!", exception);
            return null;
        }
    }

    /** This is a simple auto-complete method that can take the first few letters of a player's name and return the full name of the player. It prioritizes in two ways:
     * <b>1)</b> it gives online players priority over offline players and <b>2)</b> it gives shorter names priority over longer usernames because if a player tries to
     * designate a player and this plugin returns a different name than the user meant that starts with the same letters, the user can add more letters to get the longer
     * username instead. If these priorities were reversed, then there would be no way to specify a user whose username is the first part of another username, e.g. "Jeb" and
     * "Jebs_bro". This matching is <i>not</i> case-sensitive.
     * 
     * @param name
     *            is the String that represents the first few letters of a username that needs to be auto-completed.
     * @return the completed username that begins with <b><tt>name</b></tt> (<i>not</i> case-sensitive) */
    public static String autoCompleteName(String name) {
        String full_name = null;
        for (Player possible_owner : server.getOnlinePlayers())
            // if this player's name also matches and it shorter, return it instead becuase if someone is using an autocompleted command, we need to make sure
            // to get the shortest name because if they meant to use the longer username, they can remedy this by adding more letters to the parameter; however,
            // if they meant to do a shorter username and the auto-complete finds the longer one first, they're screwed
            if (possible_owner.getName().toLowerCase().startsWith(name.toLowerCase()) && (full_name == null || full_name.length() > possible_owner.getName().length()))
                full_name = possible_owner.getName();
        for (OfflinePlayer possible_owner : server.getOfflinePlayers())
            if (possible_owner.getName().toLowerCase().startsWith(name.toLowerCase()) && (full_name == null || full_name.length() > possible_owner.getName().length()))
                full_name = possible_owner.getName();
        return full_name;
    }

    /** This method can translate a String of time terms and values to a single int time in milliseconds (ms). It can interpret a variety of formats from "2d 3s 4m" to
     * "2 days, 4 minutes, and 3 seconds" to "2.375 minutes + 5.369s & 3.29days". Punctuation is irrelevant. Spelling is irrelevant as long as the time terms begin with the
     * correct letter. Order of values is irrelevant. (Days can come before seconds, after seconds, or both.) Repetition of values is irrelevant; all terms are simply
     * converted to ms and summed. Integers and decimal numbers are equally readable. The highest time value it can read is days; it cannot read years or months (to avoid the
     * complications of months' different numbers of days and leap years).
     * 
     * @param written
     *            is the String to be translated into a time in milliseconds (ms).
     * @return the time given by the String <b><tt>written</b></tt> translated into milliseconds (ms). */
    public static int readTime(String written) {
        int time = 0;
        String[] temp = written.split(" ");
        ArrayList<String> words = new ArrayList<String>();
        for (String word : temp)
            if (!word.equalsIgnoreCase("and") && !word.equalsIgnoreCase("&"))
                words.add(word.toLowerCase().replaceAll(",", ""));
        while (words.size() > 0) {
            // for formats like "2 days 3 minutes 5.57 seconds" or "3 d 5 m 12 s"
            try {
                double amount = Double.parseDouble(words.get(0));
                if (words.get(0).contains("d") || words.get(0).contains("h") || words.get(0).contains("m") || words.get(0).contains("s"))
                    throw new NumberFormatException();
                int factor = 0;
                if (words.size() > 1) {
                    if (words.get(1).startsWith("d"))
                        factor = 86400000;
                    else if (words.get(1).startsWith("h"))
                        factor = 3600000;
                    else if (words.get(1).startsWith("m"))
                        factor = 60000;
                    else if (words.get(1).startsWith("s"))
                        factor = 1000;
                    if (factor > 0)
                        // since a double of, say, 1.0 is actually 0.99999..., (int)ing it will reduce exact numbers by one, so I added 0.1 to it to avoid that.
                        time = time + (int) (amount * factor + 0.1);
                    words.remove(0);
                    words.remove(0);
                } else
                    words.remove(0);
            } catch (NumberFormatException exception) {
                // if there's no space between the time and units, e.g. "2h, 5m, 25s" or "4hours, 3min, 2.265secs"
                double amount = 0;
                int factor = 0;
                try {
                    if (words.get(0).contains("d") && (!words.get(0).contains("s") || words.get(0).indexOf("s") > words.get(0).indexOf("d"))) {
                        amount = Double.parseDouble(words.get(0).split("d")[0]);
                        factor = 86400000;
                    } else if (words.get(0).contains("h")) {
                        amount = Double.parseDouble(words.get(0).split("h")[0]);
                        factor = 3600000;
                    } else if (words.get(0).contains("m")) {
                        amount = Double.parseDouble(words.get(0).split("m")[0]);
                        factor = 60000;
                    } else if (words.get(0).contains("s")) {
                        amount = Double.parseDouble(words.get(0).split("s")[0]);
                        factor = 1000;
                    }
                    if (factor > 0)
                        // since a double of, say, 1.0 is actually 0.99999..., (int)ing it will reduce exact numbers by one, so I added 0.1 to it to avoid that.
                        time = time + (int) (amount * factor + 0.1);
                } catch (NumberFormatException exception2) {
                    //
                }
                words.remove(0);
            }
        }
        return time;
    }

    /** This method is the inverse counterpart to the {@link #writeTime(long, boolean) translateStringToTimeInms()} method. It can construct a String to describe an amount of
     * time in ms in an elegant format that is readable by the aforementioned counterpart method as well as human readers.
     * 
     * @param time
     *            is the time in milliseconds (ms) that is to be translated into a readable phrase.
     * @param round_seconds
     *            determines whether or not the number of seconds should be rounded to make the phrase more elegant and readable to humans. This parameter is normally false if
     *            this method is used to save data for the plugin because we want to be as specific as possible; however, for messages sent to players in game, dropping excess
     *            decimal places makes the phrase more friendly and readable.
     * @return a String describing <b><tt>time</b></tt> */
    public static String writeTime(long time, boolean round_seconds) {
        // get the values (e.g. "2 days" or "55.7 seconds")
        ArrayList<String> values = new ArrayList<String>();
        if (time > 86400000) {
            values.add((int) (time / 86400000) + " days");
            time = time % 86400000;
        }
        if (time > 3600000) {
            values.add((int) (time / 3600000) + " hours");
            time = time % 3600000;
        }
        if (time > 60000) {
            values.add((int) (time / 60000) + " minutes");
            time = time % 60000;
        }
        // add a seconds value if there is still time remaining or if there are no other values
        if (time > 0 || values.size() == 0)
            // if you have partial seconds and !round_seconds, it's written as a double so it doesn't truncate the decimals
            if ((time / 1000.0) != (time / 1000) && !round_seconds)
                values.add((time / 1000.0) + " seconds");
            // if seconds are a whole number, just write it as a whole number (integer)
            else
                values.add(Math.round(time / 1000) + " seconds");
        // if there are two or more values, add an "and"
        if (values.size() >= 2)
            values.add(values.size() - 1, "and");
        // assemble the final String
        String written = "";
        for (int i = 0; i < values.size(); i++) {
            // add spaces as needed
            if (i > 0)
                written = written + " ";
            written = written + values.get(i);
            // add commas as needed
            if (values.size() >= 4 && i < values.size() - 1 && !values.get(i).equals("and"))
                written = written + ",";
        }
        if (!written.equals(""))
            return written;
        else
            return null;
    }

    /** This method combines all of the given <tt>String</tt>s array into a single String.
     * 
     * @param strings
     *            is the list of <tt>String</tt>s that will be combined into a signel <tt>String</tt>.
     * @param separator
     *            is the String used to separate the different <tt>String</tt>s, e.g. ", " in the list "apple, orange, lemon, melon"
     * @param indices
     *            is an optional parameter that can be used to select a range of indices in the array <b> <tt>strings</b></tt>. If one index is given, it will be used as the
     *            minimum index (inclusive) for parsing <b><tt>strings</b></tt> for adding pieces to the resultant <tt>String</tt>. If two indices are given, the first index
     *            is used as the minimum index (inclusive) and the second is used as the maximum (non-inclusive).
     * @return the <tt>String</tt> constructed by putting all the <tt>String</tt>s in <b><tt>strings</tt></b> together into one <tt>String</tt>. */
    public static String combine(Object[] strings, String separator, int... indices) {
        if (separator == null)
            separator = "";
        int start_index = 0, end_index = strings.length;
        if (indices.length > 0) {
            start_index = indices[0];
            if (indices.length > 1)
                end_index = indices[1];
        }
        String combination = "";
        for (int i = start_index; i < end_index; i++) {
            try {
                combination += strings[i].toString();
            } catch (ArrayIndexOutOfBoundsException e) {
                processException("Someone gave me bad indices!", e);
            }
            if (i < end_index - 1)
                combination += separator;
        }
        return combination;
    }

    public static String paginate(String message, String command_format, String not_enough_pages, int page_number, boolean not_console) {
        ChatPage chat_page;
        if (not_console) {
            // try to make it all one page up to 10 lines tall
            chat_page = ChatPaginator.paginate(message, page_number, 64, 10);
            // if the message is too long to be one page, even with a 10-line height, reduce the page height to 8
            if (chat_page.getTotalPages() > 1)
                chat_page = ChatPaginator.paginate(message, page_number, 64, 8);
        } else
            chat_page = ChatPaginator.paginate(message, page_number, 64, 20);
        // if the page number given is too high, format and return the not_enough_pages message
        if (page_number > chat_page.getTotalPages()) {
            String total_pages = chat_page.getTotalPages() + " pages";
            if (chat_page.getTotalPages() == 1)
                total_pages = "1 page";
            return replaceAll(ChatColor.RED + not_enough_pages, "[total]", total_pages);
        }
        String page = null;
        for (String line : chat_page.getLines())
            if (page == null)
                page = line;
            else
                page += "\n" + line;
        // if there's more than one page, add a prefix notifying the user
        if (chat_page.getTotalPages() > 1) {
            String prefix = COLOR + "Here's page " + page_number + " of " + chat_page.getTotalPages() + "!\n";
            if (page_number > 1)
                prefix += ChatColor.WHITE + "...";
            page = prefix + page;
        }
        // if there are more pages, add a suffix notifying the user
        if (chat_page.getTotalPages() > page_number)
            page +=
                    ChatColor.WHITE + "...\n" + COLOR + "Use " + replaceAll(ChatColor.ITALIC + command_format, "[#]", String.valueOf(page_number + 1)) + COLOR
                            + " to see more.";
        return page;
    }

    public static String writeLocation(Location location, boolean use_block_coordinates, boolean force_pitch_and_yaw) {
        // location format: ([x], [y], [z]) (facing ([pitch], [yaw])) in "[world]"
        String string = "(";
        if (use_block_coordinates)
            string += location.getBlockX() + ", " + location.getBlockY() + ", " + location.getBlockZ() + ") ";
        else
            string += location.getX() + ", " + location.getY() + ", " + location.getZ() + ") ";
        if (!use_block_coordinates && (location.getPitch() != 0.0 || location.getYaw() != 0.0 || force_pitch_and_yaw))
            string += "facing (" + location.getPitch() + ", " + location.getYaw() + ") ";
        return string + "in \"" + location.getWorld().getWorldFolder().getName() + "\"";
    }

    public static Location readLocation(String string) {
        // location format: ([x], [y], [z]) ([facing/aiming at] ([pitch], [yaw])) in "[world]"
        String[] coordinates = string.substring(1, string.indexOf(')')).split(", ");
        float[] facing_coordinates = new float[] { 0, 0 };
        if (string.contains("facing") || string.contains("aiming at")) {
            String[] facing_coordinates_string;
            if (string.contains("facing"))
                facing_coordinates_string = string.substring(string.indexOf(" facing ") + 9, string.lastIndexOf(')')).split(", ");
            else
                facing_coordinates_string = string.substring(string.indexOf(" aiming at ") + 12, string.lastIndexOf(')')).split(", ");
            try {
                facing_coordinates = new float[] { Float.parseFloat(facing_coordinates_string[0]), Float.parseFloat(facing_coordinates_string[1]) };
            } catch (NumberFormatException e) {
                tellOps(ChatColor.DARK_RED + "I got an error trying to read the direction on this location String!\n" + ChatColor.WHITE + "\"" + string + "\"\n"
                        + "I read these as the coordinates: " + ChatColor.WHITE + "\"" + facing_coordinates[0] + "\", \"" + facing_coordinates[1] + "\"", true);
                return null;
            }
        }
        World world = server.getWorld(string.substring(string.indexOf(" in \"") + 5, string.lastIndexOf('"')));
        if (world == null) {
            tellOps(ChatColor.DARK_RED + "I got an error trying to read the world on this location String!\n" + ChatColor.WHITE + "\"" + string + "\"\n"
                    + "I read this as the world name: " + ChatColor.WHITE + "\"" + string.substring(string.indexOf(" in \"") + 5, string.length() - 1) + "\"", true);
            return null;
        }
        try {
            return new Location(world, Double.parseDouble(coordinates[0]), Double.parseDouble(coordinates[1]), Double.parseDouble(coordinates[2]), facing_coordinates[1],
                    facing_coordinates[0]);
        } catch (NumberFormatException e) {
            tellOps(ChatColor.DARK_RED + "I got an error trying to read this location String!\n" + ChatColor.WHITE + "\"" + string + "\"\n"
                    + "I read these as the coordinates: " + ChatColor.WHITE + "\"" + coordinates[0] + "\", \"" + coordinates[1] + "\", \"" + coordinates[2] + "\"", true);
            return null;
        }
    }

    // ChatColor utils
    /** This method actiavtes any color codes in a given String and returns the message with color codes eliminated from the text and colors added to the text. This method is
     * necessary because it does two (2) things that <a href="ChatColor#translateAlternateColorCodes(char, String)">CraftBukkit's color code translating method</a> cannot.
     * <b>1)</b> It rearranges color codes in the text to ensure that every one is used. With CraftBukkit's standard methods, any formatting color codes (e.g. &k for magic or
     * &l for bold) that <i>precede</i> color color codes (e.g. &a for light green or &4 for dark red) are automatically cancelled, but if the formatting color codes comes
     * <i>after</i> the color color code, the following text will be colored AND formatted. This method can simply switch the places of the formatting and color color codes in
     * these instances to ensure that both are used (e.g. "&k&4", which normally results in dark red text, becomes "&4&k", which results in dark red magic text). <b>2)</b> It
     * allows the use of anti-color codes, an invention of mine. Anti-color codes use percent symbols (%) in place of ampersands (&) and work in the opposite way of normal
     * color codes. They allow the user to cancel one coloring or formatting in text without having to rewrite all of the previous color codes. For example, normally to change
     * from a dark red, magic, bold text ("&4&k&l") to a dark red magic text ("&4&k"), you would have to use "&4&k"; with this feature, however, you can simply use "%l" to
     * cancel the bold formatting. This feature is essential for the AutoCorrect abilities; for example, the profanity filter must have the ability to execute a magic color
     * code, but then cancel it without losing any colors designated by the sender earlier in the message. Without this ability, the white color code ("&f") could perhaps be
     * used to cancel the magic formatting, but in a red message containing a profanity, that would result in the rest of the message after the covered up profanity being
     * white.
     * 
     * @param text
     *            is the string that must be color coded.
     * @return the String colored according to the color codes given */
    public static String colorCode(String text) {
        try {
            text = "&f" + text;
            // put color codes in the right order if they're next to each other
            for (int i = 0; i < text.length() - 3; i++)
                if (isColorCode(text.substring(i, i + 2), false, true) && isColorCode(text.substring(i + 2, i + 4), true, true))
                    text = text.substring(0, i) + text.substring(i + 2, i + 4) + text.substring(i, i + 2) + text.substring(i + 4);
            // replace all anti color codes with non antis
            String current_color_code = "";
            for (int i = 0; i < text.length() - 1; i++) {
                if (isColorCode(text.substring(i, i + 2), null, true))
                    current_color_code = current_color_code + text.substring(i, i + 2);
                else if (isColorCode(text.substring(i, i + 2), null, false)) {
                    while (text.length() > i + 2 && isColorCode(text.substring(i, i + 2), null, false)) {
                        current_color_code = current_color_code.replaceAll("&" + text.substring(i + 1, i + 2), "");
                        if (current_color_code.equals(""))
                            current_color_code = "&f";
                        text = text.substring(0, i) + text.substring(i + 2);
                    }
                    text = text.substring(0, i) + current_color_code + text.substring(i);
                }
            }
            String colored_text = ChatColor.translateAlternateColorCodes('&', text);
            return colored_text;
        } catch (Exception exception) {
            processException("There was a problem in onEnable()!", exception);
            return null;
        }
    }

    /** This method can determine whether or not a String is a color code or not and what type or color code it is (formatting vs. color color codes and/or normal vs.
     * anti-color codes).
     * 
     * @param text
     *            is the two-character String that this method analyzes to see whether or not it is a color code.
     * @param color
     *            is a Boolean that can have three values. A value of <b>true</b> means that the color code must be non-formatting, e.g. "&a" (light green) or "&4" (dark red).
     *            A value of <b>false</b> means that the color code must be formatting, e.g. "&k" for magic or "&l" for bold. A value of <b>null</b> means that it can be
     *            either a formatting or non-formatting color code to return true.
     * @param non_anti
     *            works similarly to <b><tt>color</tt></b>, but for anti-color codes vs. normal color codes. A value of <b>true</b> means that the color code must <i>not</i>
     *            be an anti-color code.
     * @return true if the String is a color code and the other standards set by the Boolean parameters are met; false otherwise */
    public static Boolean isColorCode(String text, Boolean color, Boolean non_anti) {
        try {
            if (!text.startsWith("&") && !text.startsWith("%"))
                return false;
            if (non_anti != null)
                if (non_anti && text.startsWith("%"))
                    return false;
                else if (!non_anti && text.startsWith("&"))
                    return false;
            if (color == null || color)
                for (String color_color_code_char : COLOR_COLOR_CODE_CHARS)
                    if (text.substring(1, 2).equalsIgnoreCase(color_color_code_char))
                        return true;
            if (color == null || !color)
                for (String formatting_color_code_char : FORMATTING_COLOR_CODE_CHARS)
                    if (text.substring(1, 2).equalsIgnoreCase(formatting_color_code_char))
                        return true;
            return false;
        } catch (Exception exception) {
            processException("There was a problem in onEnable()!", exception);
            return null;
        }
    }

    // list utils
    /** This method simply checks to see if the Object <b><tt>object</b></tt> is listed in the array <b><tt>objects</b></tt>.
     * 
     * @param objects
     *            is the array through which this method will search for <b><tt>object</b></tt>.
     * @param object
     *            is the Object which this method will search for in <b><tt>objects</b></tt>.
     * @return <b>true</b> if <b><tt>objects</b></tt> contains <b><tt>object</b></tt> or <b>false</b> if <b><tt>objects</b></tt> does not contain <b> <tt>object</b></tt>. */
    public static boolean contains(Object[] objects, Object object) {
        for (Object listed_object : objects)
            if (listed_object.equals(object))
                return true;
        return false;
    }

    /** This method returns a grammatically correct list that contains all of the items given in a String array.
     * 
     * @param objects
     *            is the String array which will be written into a list.
     * @param options
     *            is an optional parameter that can allow the user to customize the String used to separate items in a 3+-item list (which is ", " by default) and/or the
     *            String used to separate the items in a 2-item list or the last item in a 3+-item list from the rest (which is "and" by default). The first option (
     *            <tt>[0]</tt>) is the separator String; the second option (<tt>[1]</tt>) is the final conjunction String.
     * @return a grammatically correct list of the objects in <b><tt>objects</b></tt>. */
    public static String writeArray(Object[] objects, String... options) {
        String separator = ", ", final_conjunction = "and";
        if (options.length > 0 && options[0] != null)
            separator = options[0];
        if (options.length > 1 && options[1] != null)
            final_conjunction = options[1];
        if (objects.length == 0)
            return "";
        else if (objects.length == 1)
            return String.valueOf(objects[0]);
        else if (objects.length == 2)
            return objects[0] + " " + final_conjunction + " " + objects[1];
        else {
            String list = "";
            for (int i = 0; i < objects.length; i++) {
                list += objects[i];
                if (i <= objects.length - 1) {
                    list += separator;
                    if (i == objects.length - 2)
                        list += final_conjunction + " ";
                }
            }
            return list;
        }
    }

    // mass-message utils
    /** This method sends a given message to everyone who is currently debugging this plugin. Players and the console can enter debugging mode using <i>/mUW debug</i>.
     * 
     * @param message
     *            is the <tt>String</tt> that will be sent as a message to any users currently debugging this plugin. */
    public static void debug(String message) {
        if (debuggers.size() == 0)
            return;
        if (debuggers.contains("console")) {
            console.sendMessage(COLOR + message);
            if (debuggers.size() == 1)
                return;
        }
        for (Player player : server.getOnlinePlayers())
            if (debuggers.contains(player.getName()))
                player.sendMessage(COLOR + message);
    }

    /** This method sends a given message to every operator currently on the server as well as to the console.
     * 
     * @param message
     *            is the message that will be sent to all operators and the console. <b><tt>Message</b></tt> will be color coded using myPluginUtils's
     *            {@link #colorCode(String) colorCode(String)} method.
     * @param also_tell_console
     *            indicates whether or not <b><tt>message</b></tt> will also be sent to the console.
     * @param exempt_ops
     *            is an optional parameter in which you may list any ops by exact username that should not receive <b><tt>message</b></tt>. */
    public static void tellOps(String message, boolean also_tell_console, String... exempt_ops) {
        // capitalize the first letters of sentences
        if (message.length() > 1)
            message = message.substring(0, 1).toUpperCase() + message.substring(1);
        for (Player player : server.getOnlinePlayers())
            if (player.isOp() && !contains(exempt_ops, player.getName()))
                player.sendMessage(COLOR + colorCode(message));
        if (also_tell_console)
            console.sendMessage(COLOR + colorCode(message));
    }

    /** This method filters the stack trace of the given <tt>Exception</tt> that's caught in myUltraWarps and extracts all the pertinent, relevant information. Then it sends a
     * message to all ops on the server that includes a given custom message and a few lines describing the <tt>Exception</tt> and the first few lines of the stack trace.
     * 
     * @param message
     *            is the custom message that precedes the filtered data from the Exception.
     * @param e
     *            is the <tt>Exception</tt> that is being analyzed by this method. */
    public static void processException(String message, Throwable e) {
        tellOps(ChatColor.DARK_RED + message, true);
        /* skip stack trace lines until we get to the part with explicit line numbers and class names that don't come from Java's standard libraries; the stuff we're skipping
         * is anything that comes from the native Java code with no line numbers or class names that will help us pinpoint the issue */
        int lines_to_skip = 0;
        while (lines_to_skip < e.getStackTrace().length
                && (e.getStackTrace()[lines_to_skip].getLineNumber() < 0 || e.getStackTrace()[lines_to_skip].getClassName().startsWith("java")))
            lines_to_skip++;
        while (e != null) {
            // output a maximum of three lines of the stack trace
            tellOps(ChatColor.DARK_RED + e.getClass().getName().substring(e.getClass().getName().lastIndexOf('.') + 1) + " at line "
                    + e.getStackTrace()[lines_to_skip].getLineNumber() + " of " + e.getStackTrace()[lines_to_skip].getClassName() + ".java (myUltraWarps)", true);
            if (lines_to_skip + 1 < e.getStackTrace().length)
                tellOps(ChatColor.DARK_RED + "  ...and at line " + e.getStackTrace()[lines_to_skip + 1].getLineNumber() + " of "
                        + e.getStackTrace()[lines_to_skip + 1].getClassName() + ".java (myUltraWarps)", true);
            if (lines_to_skip + 2 < e.getStackTrace().length)
                tellOps(ChatColor.DARK_RED + "  ...and at line " + e.getStackTrace()[lines_to_skip + 2].getLineNumber() + " of "
                        + e.getStackTrace()[lines_to_skip + 2].getClassName() + ".java (myUltraWarps)", true);
            e = e.getCause();
            if (e != null)
                tellOps(ChatColor.DARK_RED + "...which was caused by:", true);
        }
    }

    // other utils
    /** This method is used when reading through the parameters of <i>/change</i> or <i>/create</i> commands. These commands can include warp or no warp messages, which often
     * have to take up more than one parameter in order to contain spaces. This method is used to stop reading the warp or no warp messages in the parameters when needed, i.e.
     * at the end of the command parameters or when it reaches another parameter such as "giveto:[player]".
     * 
     * @param warp_message
     *            is the warp message that was being read in the command; it will be <b>null</b> if the warp message was not given in the current part of the command.
     * @param no_warp_message
     *            is the no warp message that was being read in the command; it will be <b>null</b> if the no warp message was not given in the current part of the command.
     * @param true_warp_name
     *            is the name of the warp which this message is associated with.
     * @param true_owner_name
     *            is the owner of the warp which this message is associated with.
     * @param player_is_owner
     *            designates wither or not the person executing the command is the owner of the warp or not.
     * @param sender
     *            is the Player or <tt>console</tt> that is executing the command to change the warp or no warp message.
     * @param result_message
     *            is the message that will be displayed at the end of <i>/change</i> to tell the <tt>sender</tt> which warp properties they did and did not successfully
     *            change.
     * @return the result_message so that <i>/change</i> can display it to <tt>sender</tt> at the end of the changeWarp() method. */
    public String stopParsingMessages(String warp_message, String no_warp_message, String true_warp_name, String true_owner_name, boolean player_is_owner,
            CommandSender sender, String result_message) {
        if (parsing_warp_message) {
            parsing_warp_message = false;
            if (!result_message.equals(""))
                result_message = result_message + "\n";
            if (player_is_owner)
                if (warp_message.endsWith(".") || warp_message.endsWith("!") || warp_message.endsWith("?"))
                    result_message =
                            result_message + COLOR + "Now people who successfully warp to \"" + true_warp_name + "\" will see \"" + ChatColor.WHITE + colorCode(warp_message)
                                    + COLOR + "\"";
                else if (warp_message.equals(""))
                    result_message = result_message + COLOR + "Now people who successfully warp to \"" + true_warp_name + "\" won't see a message.";
                else
                    result_message =
                            result_message + COLOR + "Now people who successfully warp to \"" + true_warp_name + "\" will see \"" + ChatColor.WHITE + colorCode(warp_message)
                                    + COLOR + ".\"";
            else if (warp_message.endsWith(".") || warp_message.endsWith("!") || warp_message.endsWith("?"))
                result_message =
                        result_message + COLOR + "Now people who successfully warp to " + true_owner_name + "'s \"" + true_warp_name + "\" will see \"" + ChatColor.WHITE
                                + colorCode(warp_message) + COLOR + "\"";
            else if (warp_message.equals(""))
                result_message = result_message + COLOR + "Now people who successfully warp to " + true_owner_name + "'s \"" + true_warp_name + "\" won't see a message.";
            else
                result_message =
                        result_message + COLOR + "Now people who successfully warp to " + true_owner_name + "'s \"" + true_warp_name + "\" will see \"" + ChatColor.WHITE
                                + colorCode(warp_message) + COLOR + ".\"";
        } else if (parsing_no_warp_message) {
            parsing_no_warp_message = false;
            if (!result_message.equals(""))
                result_message = result_message + "\n";
            if (player_is_owner)
                if (warp_message.endsWith(".") || warp_message.endsWith("!") || warp_message.endsWith("?"))
                    result_message =
                            result_message + COLOR + "Now people who aren't allowed to warp to \"" + true_warp_name + "\" will see \"" + ChatColor.WHITE
                                    + colorCode(no_warp_message) + COLOR + "\"";
                else if (no_warp_message.equals(""))
                    result_message = result_message + COLOR + "Now people who aren't allowed to warp to \"" + true_warp_name + "\" won't see a message.";
                else
                    result_message =
                            result_message + COLOR + "Now people who aren't allowed to warp to \"" + true_warp_name + "\" will see \"" + ChatColor.WHITE
                                    + colorCode(no_warp_message) + COLOR + ".\"";
            else if (no_warp_message.endsWith(".") || no_warp_message.endsWith("!") || no_warp_message.endsWith("?"))
                result_message =
                        result_message + COLOR + "Now people who aren't allowed to warp to " + true_owner_name + "'s \"" + true_warp_name + "\" will see \"" + ChatColor.WHITE
                                + colorCode(no_warp_message) + COLOR + "\"";
            else if (no_warp_message.equals(""))
                result_message = result_message + COLOR + "Now people who aren't allowed to warp to " + true_owner_name + "'s \"" + true_warp_name + "\" won't see a message.";
            else
                result_message =
                        result_message + COLOR + "Now people who aren't allowed to warp to " + true_owner_name + "'s \"" + true_warp_name + "\" will see \"" + ChatColor.WHITE
                                + colorCode(no_warp_message) + COLOR + ".\"";
        }
        return result_message;
    }

    /** This method gets the settings for a player that were configured in the <tt>config.txt</tt>. It gives more specific settings priority over less specific settings. In
     * other words, if <b><tt>player</b></tt> has individual settings, it will return those; if <b><tt>player</b></tt> has no individual settings, it will return the settings
     * for <b><tt>player</b></tt>'s permissions group (assuming <b><tt>player</b></tt> is in a permission group, group settings are enabled, and the server has Vault); if <b>
     * <tt>player</b></tt> has no group or individual settings, it will return the global settings. In the HashMap list of SettingsSets, keys can be either usernames of
     * players for individual settings, group names enclosed in brackets (e.g. "[admin]" for the admin group settings), or "[server]" for global settings
     * 
     * @param player
     *            is the name of the user (or permissions-based group or "[server]") that we need the settings for.
     * @return the most specific SettingsSet that applies to <b><tt>player</b></tt> */
    public SettingsSet getSettings(String player) {
        /* prioritize by searching first for individual settings, then group settings if you can't find individual settings, the server-wide (global) settings if you can't
         * find group settings */
        // first, try retrieving the player's individual settings
        if (settings.get(player) != null) {
            debug("retrieved settings: individual (" + player + ")");
            return settings.get(player);
        }

        // if no individual settings are available, attempt to retrieve the player's permissions group settings
        if (permissions != null && permissions.getPrimaryGroup((World) null, player) != null) {
            debug("acknowledged " + player + "'s " + permissions.getPrimaryGroup((World) null, player) + " group membership");
            if (use_group_settings && settings.get("[" + permissions.getPrimaryGroup((World) null, player) + "]") != null) {
                debug("retrieved settings: group (" + permissions.getPrimaryGroup((World) null, player) + ")");
                return settings.get("[" + permissions.getPrimaryGroup((World) null, player) + "]");
            }
        } else if (permissions == null)
            debug("no permissions available; no group found");
        else
            debug("no permissions group found");

        // finally, if no group or individual settings are available, retrieve the server's global settings
        if (settings.get("[server]") != null) {
            debug("retrieved settings: global");
            return settings.get("[server]");
        }

        // if by some bizzare occurrence the server settings don't exist, return a SettingsSet with the default settings
        tellOps(ChatColor.DARK_RED + "I couldn't retrieve " + player + "'s settings!", true);
        return new SettingsSet();
    }

    /** This method uses the given <tt>Player</tt>'s location to calculate the block that they're pointing at. It works like the <tt>Player.getTargetBlock()</tt> method from
     * CraftBukkit, but it's better because it can skip non-solid blocks (optionally not switches) and it can see much further (max 500 blocks).
     * 
     * @param player
     *            is the <tt>Player</tt> that will be analyzed by this method to find the target block.
     * @param skip_switches
     *            will ignore all non-solid blocks (such as air, buttons, signs, and anything else non-liquid that you can walk through) if <b>true</b> and will ignore all
     *            non-solid blocks except for switch blocks (signs, buttons, pressure plates, etc.) if <b>false</b>.
     * @return the block that <tt><b>player</b></tt> is poitning at. */
    @SuppressWarnings("deprecation")
    public static Block getTargetBlock(Player player, boolean skip_switches) {
        // d is for distance from the player's eye location
        for (int d = 0; d < 5000; d++) {
            double yaw = player.getLocation().getYaw(), pitch = player.getLocation().getPitch();
            Location location =
                    new Location(player.getWorld(), player.getLocation().getX() + d / 10.0 * Math.cos(Math.toRadians(yaw + 90)) * Math.cos(Math.toRadians(-pitch)), player
                            .getEyeLocation().getY()
                            + d / 10.0 * Math.sin(Math.toRadians(-pitch)), player.getLocation().getZ() + d / 10.0 * Math.sin(Math.toRadians(yaw + 90))
                            * Math.cos(Math.toRadians(-pitch)));
            Block block = location.getBlock();
            // make sure the location isn't outside the bounds of the world
            if (block == null || Math.abs(location.getBlockX()) >= 2000000 || Math.abs(location.getBlockZ()) >= 2000000 || location.getY() < 0
                    || location.getY() > location.getWorld().getMaxHeight()) {
                debug("No good target found; search ended at " + writeLocation(location, true, false));
                return null;
            }
            // make sure the location is either not a non-solid block or, if we're not skipping switches, a switch
            if (!contains(NON_SOLID_BLOCK_IDS, (short) block.getTypeId()) || (!skip_switches && UltraSwitch.getSwitchType(block) != null)) {
                debug("found target block at " + writeLocation(block.getLocation(), true, false));
                return block;
            }
        }
        return null;
    }

    /** This method checks that the player has the ability and permission to teleport and teleports them if they do. See the parameters for more information.
     * 
     * @param player
     *            is the Player being teleported.
     * @param from
     *            is the place where <b><tt>player</b></tt> began. If <b><tt>from</b></tt> is not <b>null</b>, it will be used to record this teleportation event in <b>
     *            <tt>player</b></tt>'s warp history.
     * @param to
     *            is the place <b><tt>player</b></tt> is being teleported to. If <b><tt>from</b></tt> is not <b>null</b>, it will be used to record this teleportation event in
     *            <b> <tt>player</b></tt>'s warp history in addition to being the target of <b><tt>player</b></tt>'s teleportation.
     * @param send_warp_message
     *            designates whether or not this method should send <b><tt>player</b></tt> the warp message designated by the "to" The value of this parameter is <b>false</b>
     *            when the message to be sent to <b><tt>player</b></tt> is different from the message to be saved in <b> <tt>player</b></tt>'s warp history.
     * @param non_teleporting_player
     *            is used when there is a second Player or <tt>console</tt> involved in the teleportation either as the executor of the teleportation command or the target of
     *            the teleportation. This is used to allow Players with admin permissions or the console to teleport players without interference from this plugin because <b>
     *            <tt>player</b></tt>'s warping cool down time is not up or for any other reasons.
     * @return <b>true</b> if the teleportation was successful and <b>false</b> if <b><tt>player</b></tt> does not have permission or must wait for their cooldown time to
     *         expire */
    public boolean teleport(Player player, UltraWarp from, UltraWarp to, boolean send_warp_message, CommandSender non_teleporting_player) {
        SettingsSet set = getSettings(player.getName());
        // stop here if the cooldown timer has not finished
        if (cooling_down_players.containsKey(player.getName()) && !player.hasPermission("myultrawarps.admin")
                && (non_teleporting_player == null || non_teleporting_player instanceof Player && !non_teleporting_player.hasPermission("myultrawarps.admin"))) {
            player.sendMessage(ChatColor.RED + "Sorry, but you still have to wait "
                    + writeTime(set.cooldown - (int) (Calendar.getInstance().getTimeInMillis() - cooling_down_players.get(player.getName())), true)
                    + " before you can teleport again.");
            if (non_teleporting_player != null)
                // in some instances like /to or /from, other players are involved in the telportation. These players need to be informed of cool down timer restrictions
                non_teleporting_player.sendMessage(ChatColor.RED + "Sorry, but " + player.getName() + " can't teleport for another "
                        + writeTime(set.cooldown - (int) (Calendar.getInstance().getTimeInMillis() - cooling_down_players.get(player.getName())), true) + ".");
            return false;
        }
        // don't allow teleportation outside of the map
        if (Math.abs(to.location.getX()) > 2000000 || Math.abs(to.location.getZ()) > 2000000) {
            debug(ChatColor.RED + "Teleportation cancelled; |coords| > (2,000,000, 2,000,000)");
            // \u00B1 is a plus of minus symbol
            player.sendMessage(ChatColor.RED + "Sorry, but you can't teleport past x = \u00B12,000,000 or z = \u00B12,000,000. because it crashes most Minecraft servers.");
            if (non_teleporting_player != null)
                non_teleporting_player.sendMessage(ChatColor.RED + "Sorry, but " + player.getName()
                        + " can't teleport past x = \u00B12,000,000 or z = \u00B12,000,000. because it crashes most Minecraft servers.");
            return false;
        }
        // teleport the player
        if (!to.location.getChunk().isLoaded()) {
            debug(player.getName() + "'s target chunk hasn't been loaded yet; I'll load it and teleport them there when it's ready.");
            teleportPartII(player, from, to, send_warp_message, non_teleporting_player);
            if (!to.location.getChunk().load()) {
                debug(ChatColor.RED + "I couldn't load the target chunk!");
                player.sendMessage(ChatColor.DARK_RED + "I'm sorry, but I couldn't load the chunk that you're supposed to teleport to!");
                if (non_teleporting_player != null)
                    non_teleporting_player.sendMessage(ChatColor.DARK_RED + "I couldn't load the chunk that you were teleporting " + player.getName() + " to!");
                return false;
            }
        }
        teleportPartII(player, from, to, send_warp_message, non_teleporting_player);
        return true;
    }

    public void teleportPartII(Player player, UltraWarp from, UltraWarp to, boolean send_warp_message, CommandSender non_teleporting_player) {
        if (player.isInsideVehicle())
            debug("When teleporting, " + player.getName() + " was riding a(n) " + player.getVehicle().getType().name());
        server.getScheduler().scheduleSyncDelayedTask(mUW, new myUltraWarps$1(player, "perform teleportation", to.location), 0);
        SettingsSet set = getSettings(player.getName());
        // send the warp message
        if (send_warp_message && !to.warp_message.equals(""))
            player.sendMessage(colorCode(to.warp_message.replaceAll("\\[player\\]", player.getName())));
        if (from != null) {
            // save the player's location before warping
            ArrayList<UltraWarp> replacement = warp_histories.get(player.getName());
            Integer last_warp_index = last_warp_indexes.get(player.getName());
            if (replacement != null && last_warp_index != null)
                while (replacement.size() > last_warp_index + 1)
                    replacement.remove(replacement.size() - 1);
            else if (replacement == null)
                replacement = new ArrayList<UltraWarp>();
            replacement.add(from);
            // save the player's location after warping
            replacement.add(to);
            while (replacement.size() > set.warp_history_length)
                replacement.remove(0);
            warp_histories.put(player.getName(), replacement);
            last_warp_indexes.put(player.getName(), replacement.size() - 1);
        }
        // start the cool down timer if necessary
        if (set.cooldown > 0 && !player.hasPermission("myultrawarps.admin")) {
            // record the time that the timer starts
            cooling_down_players.put(player.getName(), Calendar.getInstance().getTimeInMillis());
            // the Bukkit scheduler is timed using a tick 20 times/second; therefore, the cooldown time (which is in ms) is divided by 50: /1000 to convert
            // it to seconds and *20 to account for the 20 ticks/second
            server.getScheduler().scheduleSyncDelayedTask(this, new myUltraWarps$1(player, "remove cooldown", player.getName()), set.cooldown / 50);
        }
    }

    // listeners
    @EventHandler
    public void teleportOnTargetChunkLoad(ChunkLoadEvent event) {
        for (String name : teleporting_players.keySet())
            if (server.getPlayerExact(name) == null) {
                debug(name + "'s target loaded for teleportation, but they were nowhere to be found.");
                teleporting_players.remove(name);
            } else if (((UltraWarp) teleporting_players.get(name)[0]).location.getChunk().equals(event.getChunk())) {
                debug(name + "'s target chunk loaded for teleportation!");
                Object[] objects = teleporting_players.get(name);
                teleportPartII(server.getPlayerExact(name), (UltraWarp) objects[0], (UltraWarp) objects[1], (Boolean) objects[2], (CommandSender) objects[3]);
                teleporting_players.remove(name);
            }
        for (String name : spawning_players.keySet())
            if (server.getPlayerExact(name) == null) {
                debug(name + "'s target loaded for spawning, but they were nowhere to be found.");
                spawning_players.remove(name);
            } else if (spawning_players.get(name).getChunk().equals(event.getChunk())) {
                debug(name + "'s target chunk loaded for spawning!");
                spawning_players.remove(name);
                server.getScheduler().scheduleSyncDelayedTask(mUW, new myUltraWarps$1(server.getPlayerExact(name), "perform teleportation", spawning_players.get(name)), 0);
            }
    }

    @EventHandler
    public void preventUnloadingOfTargetChunks(ChunkUnloadEvent event) {
        for (String name : teleporting_players.keySet())
            if (server.getPlayerExact(name) == null) {
                debug(name + "'s target for teleportation unloaded, but they were nowhere to be found.");
                teleporting_players.remove(name);
            } else if (((UltraWarp) teleporting_players.get(name)[0]).location.getChunk().equals(event.getChunk())) {
                debug("I stopped the server from unloading the chunk that " + name + " was trying to teleport to.");
                event.setCancelled(true);
            }
        for (String name : spawning_players.keySet())
            if (server.getPlayerExact(name) == null) {
                debug(name + "'s target for spawning unloaded, but they were nowhere to be found.");
                spawning_players.remove(name);
            } else if (spawning_players.get(name).getChunk().equals(event.getChunk())) {
                debug("I stopped the server from unloading the chunk that " + name + " was trying to spawn in.");
                event.setCancelled(true);
            }
    }

    @EventHandler
    public void fixFallSuffocationOnSpawn(PlayerJoinEvent event) {
        // load the chunk the player is spawning in
        if (!event.getPlayer().getLocation().getChunk().isLoaded()) {
            debug(event.getPlayer().getName() + "'s target chunk for spawning hasn't been loaded yet; I'll load it and teleport them there when it's ready.");
            spawning_players.put(event.getPlayer().getName(), event.getPlayer().getLocation());
            if (!event.getPlayer().getLocation().getChunk().load()) {
                debug(ChatColor.RED + "I couldn't load the target chunk!");
                event.getPlayer().kickPlayer(ChatColor.RED + "I'm sorry, but I couldn't load the chunk that you're supposed to spawn in! Try again in a minute!");
            }
        } else
            debug(event.getPlayer().getName() + "'s login chunk was already loaded.");
    }

    @EventHandler
    public void stopSpawningPlayersFromTakingDamage(EntityDamageEvent event) {
        if (event.getEntity() instanceof Player && spawning_players.containsKey(((Player) event.getEntity()).getName()))
            event.setCancelled(true);
    }

    @EventHandler
    public void protectSpawningPlayers(EntityTargetEvent event) {
        if (event.getEntity() instanceof Player && spawning_players.containsKey(((Player) event.getEntity()).getName()))
            event.setCancelled(true);
    }

    /** This listener method makes myUltraWarps do three things when a new player (<tt>event.getPlayer()</tt>) logs in: <b>1)</b> display the set spawn message for the world if
     * the player has never played before, <b>2)</b> check the player's permissions and remove them from the list of players waiting for the teleportation cool down to expire
     * (<tt>cooling_down_players</tt>), and <b>3)</b> send the player any important info messages concerning their warps or switches, e.g. if one of their switches was broken
     * while they were offline.
     * 
     * @param event
     *            is the PlayerJoinEvent that triggers this listener method. */
    @EventHandler
    public void informPlayersOfStuffAndRemoveTheirCoolingDownStatusIfNecessary(PlayerJoinEvent event) {
        if (!event.getPlayer().hasPlayedBefore())
            event.getPlayer().sendMessage(colorCode(spawn_messages_by_world.get(event.getPlayer().getWorld()).replaceAll("\\[player\\]", event.getPlayer().getName())));
        else {
            // tell admins that myUltraWarps has updated
            if (event.getPlayer().hasPermission("myultrawarps.admin") && new File(this.getDataFolder(), "myUltraWarps.jar").exists())
                event.getPlayer()
                        .sendMessage(
                                COLOR
                                        + "myUltraWarps has been updated! You should put in the new version right now! I already downloaded it into the myUltraWarps plugin data folder (the place where you find the config.txt and stuff). All you have to do is stop the server and replace the myUltraWarps in the plugins folder with the new one.\nDo it now!");
            // remind debugging admins that they're debugging
            if (debuggers.contains(event.getPlayer().getName()))
                event.getPlayer().sendMessage(COLOR + "Your debugging messages are still on for myUltraWarps!");
            // send people their messages
            else if (info_messages_for_players.get(event.getPlayer().getName()) != null)
                for (String message : info_messages_for_players.get(event.getPlayer().getName()))
                    event.getPlayer().sendMessage(message);
            // remove a player's cooling down status if necessary
            // I would have it check when it's loading the temporary data concerning cooling down players, but the player needs to be online for me to check all
            // their permissions, so I have to check it when they log on instead
            if (cooling_down_players.containsKey(event.getPlayer().getName())
                    && cooling_down_players.get(event.getPlayer().getName()) + getSettings(event.getPlayer().getName()).cooldown < Calendar.getInstance().getTimeInMillis())
                cooling_down_players.remove(event.getPlayer().getName());
        }
    }

    /** This listener method teleports players to their set home warps when they die if they set one and if the <tt>config.txt</tt> setting says that they should.
     * 
     * @param event
     *            is the PlayerRespawnEvent that triggers this method. */
    @EventHandler
    public void teleportToHomeOnRespawn(PlayerRespawnEvent event) {
        if (!event.getPlayer().hasPermission("myultrawarps.respawnhome") && !event.getPlayer().hasPermission("myultrawarps.user")
                && !event.getPlayer().hasPermission("myultrawarps.admin"))
            if (!getSettings(event.getPlayer().getName()).home_on_death)
                return;
        UltraWarp home = null;
        for (UltraWarp warp : warps)
            if (warp.owner.equals(event.getPlayer().getName()) && warp.name.equals("home"))
                home = warp;
        if (home != null) {
            event.setRespawnLocation(home.location);
            event.getPlayer().sendMessage(colorCode(home.warp_message));
        } else
            event.getPlayer().sendMessage(ChatColor.RED + "I would teleport you to your home, but you haven't set one yet!");
    }

    /** This listener method warps players to the appropriate warp if they use a switch warp (a pressure plate, lever, button, etc. linked to a warp).
     * 
     * @param event
     *            is the PlayerInteractEvent that triggers this method. The PlayerInteractEvent can also occur if someone hits or right-clicks anything; it does not only apply
     *            to using switches. */
    @EventHandler(priority = EventPriority.HIGH)
    public void warpViaSwitch(PlayerInteractEvent event) {
        if (warps != null && warps.size() > 0 && switches != null && switches.size() > 0) {
            Block target_block = event.getClickedBlock();
            UltraSwitch target = null;
            UltraWarp warp_target = null;
            if (target_block == null || UltraSwitch.getSwitchType(target_block) == null)
                return;
            for (UltraSwitch my_switch : switches)
                if (target_block.getLocation().equals(my_switch.location)
                        && ((my_switch.switch_type.equals("pressure plate") && event.getAction().equals(Action.PHYSICAL)) || (!my_switch.switch_type.equals("pressure plate") && event
                                .getAction().equals(Action.RIGHT_CLICK_BLOCK)))) {
                    target = my_switch;
                    break;
                }
            // if target is null, whatever switch they pressed wasn't linked to a warp
            if (target == null)
                return;
            // cancel the interaction event if you right-clicked a sign to make sure it doesn't make you place the block in your hand
            if (target_block.getType() == Material.WALL_SIGN || target_block.getType() == Material.SIGN_POST)
                event.setCancelled(true);
            for (UltraWarp warp : warps)
                if (warp.owner.equals(target.warp_owner) && warp.name.equals(target.warp_name))
                    warp_target = warp;
            // if warp_target is null, that's a problem
            if (warp_target == null) {
                event.getPlayer().sendMessage(
                        ChatColor.RED + "Uh...the warp this switch was linked to seems to have disappeared without my knowledge. Sorry. Talk to your server admin.");
                return;
            }
            boolean listed = false;
            for (String listed_user : warp_target.listed_users)
                if (listed_user.equals(event.getPlayer().getName()))
                    listed = true;
            if (event.getPlayer().getName().equals(warp_target.owner) || (!warp_target.restricted && !listed) || (warp_target.restricted && listed)
                    || event.getPlayer().hasPermission("myultrawarps.warptowarp.other") || event.getPlayer().hasPermission("myultrawarps.admin")) {
                String warp_name = warp_target.name;
                if (!warp_target.owner.equals(event.getPlayer().getName()))
                    warp_name = warp_target.owner + "'s " + warp_target.name;
                teleport(event.getPlayer(), new UltraWarp("God", "coordinates", false, false, "&aThis is the spot you were at before you warped to " + warp_name + ".", "",
                        null, event.getPlayer().getLocation()), warp_target, true, null);
            } else
                event.getPlayer().sendMessage(colorCode(warp_target.no_warp_message.replaceAll("\\[player\\]", event.getPlayer().getName())));
        }
    }

    /** This listener method tracks switch breaking by Players. If the Player does not have admin-type permissions and does not own the switch, it will not allow that Player to
     * break the switch. If the Player does own the switch or has admin-type permissions to break other people's switches, it will unlink the warp linked to the switch and
     * inform the owner of the switch either immediately or the next time they log on.
     * 
     * @param event
     *            is the BlockBreakEvent that triggers this method. */
    @EventHandler
    public void playerBrokeASwitch(BlockBreakEvent event) {
        if (warps != null && warps.size() > 0 && switches != null && switches.size() > 0 && UltraSwitch.getSwitchType(event.getBlock()) != null) {
            for (int i = 0; i < switches.size(); i++)
                if (switches.get(i).location.equals(event.getBlock().getLocation()) && switches.get(i).switch_type.equals(UltraSwitch.getSwitchType(event.getBlock()))) {
                    // if the user broke their own switch
                    if ((event.getPlayer().hasPermission("myultrawarps.unlink") || event.getPlayer().hasPermission("myultrawarps.user"))
                            && switches.get(i).warp_owner.equals(event.getPlayer().getName())) {
                        event.getPlayer().sendMessage(
                                COLOR + "You unlinked \"" + switches.get(i).warp_name + "\" from this " + UltraSwitch.getSwitchType(event.getBlock()) + ".");
                        switches.remove(i);
                    } // if the switch was broken by an admin
                    else if (event.getPlayer().hasPermission("myultrawarps.unlink.other") || event.getPlayer().hasPermission("myultrawarps.admin")) {
                        event.getPlayer().sendMessage(
                                COLOR + "You unlinked " + switches.get(i).warp_owner + "'s " + switches.get(i).switch_type + " that was linked to \""
                                        + switches.get(i).warp_name + ".\"");
                        boolean owner_found = false;
                        for (Player player : server.getOnlinePlayers())
                            if (player.getName().equals(switches.get(i).warp_owner)) {
                                owner_found = true;
                                player.sendMessage(ChatColor.RED + event.getPlayer().getName() + " broke your " + switches.get(i).switch_type + " at (" + switches.get(i).x
                                        + ", " + switches.get(i).y + ", " + switches.get(i).z + ") in \"" + switches.get(i).world.getName() + ".\"");
                            }
                        if (!owner_found) {
                            ArrayList<String> messages = info_messages_for_players.get(switches.get(i).warp_owner);
                            if (messages == null)
                                messages = new ArrayList<String>();
                            messages.add(ChatColor.RED + event.getPlayer().getName() + " broke your " + switches.get(i).switch_type + " at (" + (int) switches.get(i).x + ", "
                                    + (int) switches.get(i).y + ", " + (int) switches.get(i).z + ") in \"" + switches.get(i).world.getName() + ".\"");
                            info_messages_for_players.put(switches.get(i).warp_owner, messages);
                        }
                        switches.remove(i);
                    } else {
                        event.setCancelled(true);
                        event.getPlayer().sendMessage(ChatColor.RED + "This switch doesn't belong to you. You're not allowed to break it.");
                    }
                }
        }
    }

    /** This listener method tracks explosions that break switches. Since myUltraWarps does not have the ability to track explosion causes to people in most cases, it will just
     * inform the owner immediately or the next time they log on; it will not stop the explosion.
     * 
     * @param event
     *            is the EntityExplodeEvent that triggers this method. */
    @EventHandler
    public void explosionBrokeASwitch(EntityExplodeEvent event) {
        if (warps != null && warps.size() > 0 && switches != null && switches.size() > 0)
            for (int i = 0; i < event.blockList().size(); i++)
                if (UltraSwitch.getSwitchType(event.blockList().get(i)) != null) {
                    for (int j = 0; j < switches.size(); j++)
                        if (switches.get(j).location.equals(event.blockList().get(i).getLocation())
                                && switches.get(j).switch_type.equals(UltraSwitch.getSwitchType(event.blockList().get(i)))) {
                            String cause = "An explosion of some sort";
                            if (event.getEntityType() == null)
                                cause = "Some genius trying to use a bed in the Nether";
                            else if (event.getEntityType() == EntityType.CREEPER)
                                cause = "A creeper";
                            else if (event.getEntityType() == EntityType.FIREBALL)
                                cause = "A Ghast";
                            else if (event.getEntityType() == EntityType.PRIMED_TNT)
                                cause = "A T.N.T. blast";
                            boolean owner_found = false;
                            for (Player player : server.getOnlinePlayers())
                                if (player.getName().equals(switches.get(j).warp_owner)) {
                                    owner_found = true;
                                    player.sendMessage(ChatColor.RED + "Your " + switches.get(j).switch_type + " at (" + switches.get(j).x + ", " + switches.get(j).y + ", "
                                            + switches.get(j).z + ") in \"" + switches.get(j).world.getName() + "\" linked to \"" + switches.get(j).warp_name
                                            + "\" was broken by " + cause + "!");
                                    break;
                                }
                            if (!owner_found) {
                                ArrayList<String> messages = info_messages_for_players.get(switches.get(j).warp_owner);
                                if (messages == null)
                                    messages = new ArrayList<String>();
                                messages.add(ChatColor.RED + cause + " broke your " + switches.get(j).switch_type + " at (" + switches.get(j).x + ", " + switches.get(j).y
                                        + ", " + switches.get(j).z + ") in \"" + switches.get(j).world.getName() + ".\"");
                                info_messages_for_players.put(switches.get(j).warp_owner, messages);
                            }
                            switches.remove(j);
                        }
                }
    }

    /** This listener method reads chat messages and determines whether they are "yes" or "no" answers to teleportation requests using the
     * {@link #getResponse(CommandSender, String, String, String) getResponse()} method. It gives <i>/to</i> teleportation requests priority over <i>/from</i> teleportation
     * requests -- admittedly arbitrarily. If the {@link #getResponse(CommandSender, String, String, String) getResponse()} method returns <b> <tt>null</tt></b>, it will assume
     * that the message is not meant to answer the teleportation request and will allow the chat message to pass to the normal chat channels. The priority of this listener is
     * { @link org.bukkit.EventPriority LOWEST} in order to ensure that this method is read before any chat-related processes. If this message is in fact an answer to a
     * teleportation request, the message must not be sent to the normal chat channels. The chat event will be cancelled.
     * 
     * @param event
     *            is the AsyncPlayerChatEvent that triggers this method. */
    @EventHandler(priority = EventPriority.LOWEST)
    public void readTeleportAcceptancesOrRefusals(AsyncPlayerChatEvent event) {
        if (to_teleport_requests.get(event.getPlayer().getName()) != null && to_teleport_requests.get(event.getPlayer().getName()).size() > 0) {
            Player teleporting_player = null;
            for (Player player : server.getOnlinePlayers())
                if (player.getName().equals(to_teleport_requests.get(event.getPlayer().getName()).get(0))) {
                    teleporting_player = player;
                    break;
                }
            if (teleporting_player == null) {
                event.getPlayer().sendMessage(ChatColor.RED + to_teleport_requests.get(event.getPlayer().getName()).get(0) + " must have gone offline. Sorry.");
                event.setCancelled(true);
                ArrayList<String> requesting_players = to_teleport_requests.get(event.getPlayer().getName());
                if (requesting_players == null)
                    requesting_players = new ArrayList<String>();
                else {
                    String remove = requesting_players.get(0);
                    while (requesting_players.contains(remove))
                        requesting_players.remove(remove);
                }
                to_teleport_requests.put(event.getPlayer().getName(), requesting_players);
                return;
            }
            Boolean accepted = getResponse(event.getPlayer(), event.getMessage(), null, null);
            if (accepted != null && accepted) {
                event.setCancelled(true);
                if (teleport(teleporting_player, new UltraWarp("God", "coordinates", false, false, "&aThis is the spot you were at before you teleported to "
                        + event.getPlayer().getName() + ".", "", null, teleporting_player.getLocation()), new UltraWarp("God", "coordinates", false, false,
                        "&aThis is the spot you were at when you teleported to " + event.getPlayer().getName() + ".", "", null, event.getPlayer().getLocation()), false, event
                        .getPlayer())) {
                    teleporting_player.sendMessage(COLOR + event.getPlayer().getName() + " said \"" + event.getMessage() + "\"!");
                    event.getPlayer().sendMessage(COLOR + "Cool. I'll go get " + teleporting_player.getName() + ".");
                }
                // remove the teleportation request
                ArrayList<String> requesting_players = to_teleport_requests.get(event.getPlayer().getName());
                if (requesting_players == null)
                    requesting_players = new ArrayList<String>();
                else {
                    String remove = requesting_players.get(0);
                    while (requesting_players.contains(remove))
                        requesting_players.remove(remove);
                }
                to_teleport_requests.put(event.getPlayer().getName(), requesting_players);
            } else if (accepted != null) {
                event.setCancelled(true);
                if (!event.getMessage().endsWith(".") && !event.getMessage().endsWith("!") && !event.getMessage().endsWith("?")) {
                    event.getPlayer().sendMessage(COLOR + "Okay. I'll tell " + teleporting_player.getName() + " that you said \"" + event.getMessage() + ".\"");
                    teleporting_player.sendMessage(ChatColor.RED + "Sorry, but " + event.getPlayer().getName() + " said \"" + event.getMessage() + ".\"");
                } else {
                    event.getPlayer().sendMessage(COLOR + "Okay. I'll tell " + teleporting_player.getName() + " that you said \"" + event.getMessage() + "\"");
                    teleporting_player.sendMessage(ChatColor.RED + "Sorry, but " + event.getPlayer().getName() + " said \"" + event.getMessage() + "\"");
                }
                ArrayList<String> requesting_players = to_teleport_requests.get(event.getPlayer().getName());
                if (requesting_players == null)
                    requesting_players = new ArrayList<String>();
                else {
                    String remove = requesting_players.get(0);
                    while (requesting_players.contains(remove))
                        requesting_players.remove(remove);
                }
                to_teleport_requests.put(event.getPlayer().getName(), requesting_players);
            }
        } else if (from_teleport_requests.get(event.getPlayer().getName()) != null && from_teleport_requests.get(event.getPlayer().getName()).size() > 0) {
            // event.getPlayer() is the one teleporting here!
            Player non_teleporting_player = server.getPlayer(from_teleport_requests.get(event.getPlayer().getName()).get(0));
            // if the target player went offline, tell this player
            if (non_teleporting_player == null) {
                event.getPlayer().sendMessage(ChatColor.RED + from_teleport_requests.get(event.getPlayer().getName()).get(0) + " must have gone offline. Sorry.");
                event.setCancelled(true);
                ArrayList<String> requesting_players = from_teleport_requests.get(event.getPlayer().getName());
                if (requesting_players == null)
                    requesting_players = new ArrayList<String>();
                else {
                    String remove = requesting_players.get(0);
                    while (requesting_players.contains(remove))
                        requesting_players.remove(remove);
                }
                from_teleport_requests.put(event.getPlayer().getName(), requesting_players);
                return;
            }
            Boolean accepted = getResponse(event.getPlayer(), event.getMessage(), null, null);
            if (accepted != null && accepted) {
                event.setCancelled(true);
                if (teleport(event.getPlayer(), new UltraWarp("God", "coordinates", false, false, "&aThis is the spot you were at before " + non_teleporting_player.getName()
                        + " teleported you to them.", "", null, event.getPlayer().getLocation()),
                        new UltraWarp("God", "coordinates", false, false, "&aThis is the spot you were at when you were teleported to " + non_teleporting_player.getName()
                                + ".", "", null, non_teleporting_player.getLocation()), false, non_teleporting_player)) {
                    event.getPlayer().sendMessage(COLOR + "Here's your " + non_teleporting_player.getName() + "!");
                    non_teleporting_player.sendMessage(COLOR + "Look! I brought you a " + event.getPlayer().getName() + "!");
                }
                // remove the teleportation request
                ArrayList<String> requesting_players = from_teleport_requests.get(event.getPlayer().getName());
                if (requesting_players == null)
                    requesting_players = new ArrayList<String>();
                else {
                    String remove = requesting_players.get(0);
                    while (requesting_players.contains(remove))
                        requesting_players.remove(remove);
                }
                from_teleport_requests.put(event.getPlayer().getName(), requesting_players);
            } else if (accepted != null) {
                event.setCancelled(true);
                // inform both players
                if (!event.getMessage().endsWith(".") && !event.getMessage().endsWith("!") && !event.getMessage().endsWith("?")) {
                    event.getPlayer().sendMessage(COLOR + "Okay. I'll tell " + non_teleporting_player.getName() + " that you said \"" + event.getMessage() + ".\"");
                    non_teleporting_player.sendMessage(ChatColor.RED + "Sorry, but " + event.getPlayer().getName() + " said \"" + event.getMessage() + ".\"");
                } else {
                    event.getPlayer().sendMessage(COLOR + "Okay. I'll tell " + non_teleporting_player.getName() + " that you said \"" + event.getMessage() + "\"");
                    non_teleporting_player.sendMessage(ChatColor.RED + "Sorry, but " + event.getPlayer().getName() + " said \"" + event.getMessage() + "\"");
                }
                // remove the teleportation request
                ArrayList<String> requesting_players = from_teleport_requests.get(event.getPlayer().getName());
                if (requesting_players == null)
                    requesting_players = new ArrayList<String>();
                else {
                    String remove = requesting_players.get(0);
                    while (requesting_players.contains(remove))
                        requesting_players.remove(remove);
                }
                from_teleport_requests.put(event.getPlayer().getName(), requesting_players);
            }
        }
    }

    /** This listener method tracks players' deaths in order to save them into their death histories. myUltraWarps tracks death histories in order to allow players to use the
     * <i>/death</i> command to teleport back to the last place they died.
     * 
     * @param event */
    @EventHandler
    public void trackDeathHistories(PlayerDeathEvent event) {
        ArrayList<Location> replacement = death_histories.get(event.getEntity().getName());
        if (replacement == null)
            replacement = new ArrayList<Location>();
        replacement.add(event.getEntity().getLocation());
        while (replacement.size() > getSettings(event.getEntity().getName()).death_history_length)
            replacement.remove(0);
        death_histories.put(event.getEntity().getName(), replacement);
        last_warp_to_death_indexes.put(event.getEntity().getName(), replacement.size() - 1);
    }

    @SuppressWarnings("unused")
    private void oldVehiclePortalFixingCode() {
        // @EventHandler
        // public void fixPortalVehicleBug(PlayerPortalEvent event) {
        // if (recent_portal_users.contains(event.getPlayer().getName())) {
        // debug(event.getPlayer().getName() + " entered a portal, but they never left the portal after the last portal use, so I won't teleport them.");
        // return;
        // }
        // event.getPlayer().sendMessage(colorCode(spawn_messages_by_world.get(event.getTo().getWorld()).replaceAll("\\[player\\]",
        // event.getPlayer().getName())));
        // recent_portal_users.add(event.getPlayer().getName());
        // debug(event.getPlayer().getName() + " went through a portal.");
        // if (!vehicular_portal_users.containsKey(event.getPlayer().getName()))
        // return;
        // // everything past here only applies to people using vehicles through portals
        // Object[] vehicle = vehicular_portal_users.get(event.getPlayer().getName());
        // vehicular_portal_users.remove(event.getPlayer().getName());
        // event.setCancelled(true);
        // event.getPortalTravelAgent().setSearchRadius(50);
        // Location to = event.getPortalTravelAgent().findPortal(event.getTo());
        // if (to == null) {
        // event.getPlayer().sendMessage(ChatColor.DARK_RED + "There was an issue finding the portal that this portal leads to.");
        // tellOps(ChatColor.DARK_RED + "Hey, " + event.getPlayer().getName() + " tried to go through a portal at " + writeLocation(event.getFrom(), true)
        // + ", but I couldn't find the portal's destination!", true, event.getPlayer().getName());
        // to = event.getTo();
        // }
        // boolean failed = !event.getPlayer().teleport(to);
        // if (failed)
        // debug(ChatColor.RED + "I couldn't teleport " + event.getPlayer() + " through the portal properly!");
        // debug(event.getPlayer().getName() +
        // " was riding a vehicle when they entered a portal, so I'm taking the vehicle through with them and putting them back in it.");
        // if (((Entity) vehicle[0]).getType() == EntityType.MINECART) {
        // to.setY(to.getY() + 1.0);
        // debug("I added 1 to the y-coordinate of the destination to put the bottom of the minecart above the rail.");
        // }
        // failed = !((Entity) vehicle[0]).teleport(to);
        // if (failed)
        // debug(ChatColor.RED + "I couldn't teleport a vehicle with a player through a portal.");
        // failed = !((Entity) vehicle[0]).setPassenger(event.getPlayer());
        // if (failed)
        // debug(ChatColor.RED + "I couldn't make the player get back in their vehicle after teleporting through a portal.");
        // ((Entity) vehicle[0]).setVelocity((Vector) vehicle[1]);
        // debug("vehicle's new velocity: (" + ((Entity) vehicle[0]).getVelocity().getX() + ", " + ((Entity) vehicle[0]).getVelocity().getY() + ", "
        // + ((Entity) vehicle[0]).getVelocity().getZ() + ")");
        // }
        //
        // @EventHandler
        // public void putRidingAnimalsThroughPortals(PlayerMoveEvent event) {
        // if (recent_portal_users.contains(event.getPlayer().getName())) {
        // if (event.getTo().getBlock().getType() != Material.PORTAL && event.getTo().getBlock().getType() != Material.ENDER_PORTAL) {
        // recent_portal_users.remove(event.getPlayer().getName());
        // debug(event.getPlayer().getName() + " was removed from the recent portal users list upon exiting the portal (PlayerMoveEvent).");
        // }
        // return;
        // }
        // if ((event.getTo().getBlock().getType() == Material.PORTAL || event.getTo().getBlock().getType() == Material.ENDER_PORTAL) &&
        // event.getPlayer().isInsideVehicle()) {
        // debug(event.getPlayer().getName() + " went to a portal (PlayerMoveEvent) in a vehicle.");
        // vehicular_portal_users.put(event.getPlayer().getName(), new Object[] { event.getPlayer().getVehicle(), event.getPlayer().getVehicle().getVelocity()
        // });
        // boolean failed = !event.getPlayer().leaveVehicle();
        // if (failed)
        // debug(ChatColor.RED + "I couldn't get " + event.getPlayer().getName() + " out of their vehicle to put them through the portal.");
        // // the purpose of the ".getBlock().getLocation()" is to center the player on the block
        // failed = !event.getPlayer().teleport(event.getTo().getBlock().getLocation());
        // if (failed)
        // debug(ChatColor.RED + "I couldn't reposition " + event.getPlayer().getName() + " properly in the portal.");
        // }
        // }
        //
        // @EventHandler
        // public void putVehiclesThroughPortals(VehicleMoveEvent event) {
        // if (event.getVehicle().getPassenger() == null || !(event.getVehicle().getPassenger() instanceof Player))
        // return;
        // Player player = (Player) event.getVehicle().getPassenger();
        // if ((event.getTo().getBlock().getType() == Material.PORTAL || event.getTo().getBlock().getType() == Material.ENDER_PORTAL)
        // && !recent_portal_users.contains(player.getName())) {
        // debug(player.getName() + " went to a portal (VehicleMoveEvent) in a vehicle.");
        // vehicular_portal_users.put(player.getName(), new Object[] { event.getVehicle(), event.getVehicle().getVelocity() });
        // boolean failed = !event.getVehicle().eject();
        // if (failed)
        // debug(ChatColor.RED + "I couldn't get " + player.getName() + " out of their vehicle to put them through the portal.");
        // // the purpose of the ".getBlock().getLocation()" is to center the player on the block
        // failed = !player.teleport(event.getTo().getBlock().getLocation());
        // if (failed)
        // debug(ChatColor.RED + "I couldn't reposition " + player.getName() + " properly in the portal.");
        // } else if (recent_portal_users.contains(player.getName()) && event.getTo().getBlock().getType() != Material.PORTAL
        // && event.getTo().getBlock().getType() != Material.ENDER_PORTAL) {
        // recent_portal_users.remove(player.getName());
        // debug(player.getName() + " was removed from the recent portal users list upon exiting the portal (VehicleMoveEvent).");
        // }
        // }
    }

    // loading
    /** This method loads the warp data for the whole server from the <tt>warps.txt</tt> file in the myUltraWarps folder. If this method is called, any changes made to any
     * warps in game that have not been saved will be lost.
     * 
     * @param sender
     *            is the Player or <tt>console</tt> that executed the command to load the warps from file. When myUltraWarps is enabled and this method is called,
     *            <tt>console</tt> is used as <tt><b>sender</b></tt>. */
    private void loadTheWarps(CommandSender sender) {
        warps = new ArrayList<UltraWarp>();
        File warps_file = new File(getDataFolder(), "warps.txt");
        // read the warps.txt file
        try {
            if (!warps_file.exists()) {
                getDataFolder().mkdir();
                console.sendMessage(COLOR + "I couldn't find a warps.txt file. I'll make a new one.");
                warps_file.createNewFile();
                return;
            }
            BufferedReader in = new BufferedReader(new FileReader(warps_file));
            String save_line = in.readLine();
            while (save_line != null) {
                save_line = save_line.trim();
                if (!save_line.equals(""))
                    warps.add(new UltraWarp(save_line));
                save_line = in.readLine();
            }
            in.close();
            if (warps.size() > 1) {
                // alphabetize the warps by name (by owner secondarily)
                ArrayList<UltraWarp> temp_warps = new ArrayList<UltraWarp>();
                for (UltraWarp warp : warps)
                    temp_warps.add(warp);
                warps = new ArrayList<UltraWarp>();
                UltraWarp first_warp;
                int delete_index;
                while (temp_warps.size() > 0) {
                    first_warp = temp_warps.get(0);
                    delete_index = 0;
                    for (int j = 0; j < temp_warps.size(); j++) {
                        if (temp_warps.get(j).name.compareToIgnoreCase(first_warp.name) < 0
                                || (temp_warps.get(j).name.compareToIgnoreCase(first_warp.name) == 0 && temp_warps.get(j).owner.compareToIgnoreCase(first_warp.owner) < 0)) {
                            first_warp = temp_warps.get(j);
                            delete_index = j;
                        }
                    }
                    // rename warps named "info", "all", or "list"
                    if (!first_warp.name.equalsIgnoreCase("info") && !first_warp.name.equalsIgnoreCase("all") && !first_warp.name.equalsIgnoreCase("list"))
                        warps.add(first_warp);
                    else {
                        UltraWarp renamed_first_warp =
                                new UltraWarp(first_warp.owner, "my" + first_warp.name, first_warp.listed, first_warp.restricted, first_warp.warp_message,
                                        first_warp.no_warp_message, first_warp.listed_users, first_warp.location);
                        warps.add(renamed_first_warp);
                        boolean found = false;
                        for (Player renamed_warp_owner : server.getOnlinePlayers())
                            if (renamed_warp_owner.getName().equals(first_warp.owner)) {
                                renamed_warp_owner.sendMessage(ChatColor.RED + "I found a warp of yours that was named \"" + first_warp.name
                                        + ".\" Unfortunately, it interferes with the command " + COLOR + "/warp " + first_warp.name + ChatColor.RED
                                        + ", so I had to rename it \"my" + first_warp.name + ".\" Sorry for the inconvenience.");
                                found = true;
                            }
                        if (!found) {
                            // info the player that his/her warp has been renamed
                            ArrayList<String> messages = info_messages_for_players.get(first_warp.owner);
                            if (messages == null)
                                messages = new ArrayList<String>();
                            messages.add(ChatColor.RED + "I found a warp of yours that was named \"" + first_warp.name + ".\" Unfortunately, it interferes with the command "
                                    + COLOR + "/warp " + first_warp.name + ChatColor.RED + ", so I had to rename it \"my" + first_warp.name
                                    + ".\" Sorry for the inconvenience.");
                            info_messages_for_players.put(first_warp.owner, messages);
                        }
                    }
                    temp_warps.remove(delete_index);
                }
            }
            saveTheWarps(sender, false);
        } catch (IOException exception) {
            processException("I got an IOException while trying to save your warps.", exception);
            return;
        }
        // send the sender a confirmation message
        if (warps.size() > 1)
            sender.sendMessage(COLOR + "Your " + warps.size() + " warps have been loaded.");
        else if (warps.size() == 1)
            sender.sendMessage(COLOR + "Your 1 warp has been loaded.");
        else
            sender.sendMessage(COLOR + "You have no warps to load!");
        if (sender instanceof Player)
            if (warps.size() > 1)
                console.sendMessage(COLOR + ((Player) sender).getName() + " loaded " + warps.size() + " warps from file.");
            else if (warps.size() == 1)
                console.sendMessage(COLOR + ((Player) sender).getName() + " loaded the server's 1 warp from file.");
            else
                console.sendMessage(COLOR + ((Player) sender).getName() + " loaded the server's warps from file, but there were no warps on file.");
    }

    /** This method loads the switch warp data for the whole server from the <tt>switches.txt</tt> file in the myUltraWarps folder. If this method is called, any changes made
     * to any warping switches in game that have not been saved will be lost.
     * 
     * @param sender
     *            is the Player or <tt>console</tt> that executed the command to load the switches from file. When myUltraWarps is enabled and this method is called,
     *            <tt>console</tt> is used as <tt><b>sender</b></tt>. */
    private void loadTheSwitches(CommandSender sender) {
        switches = new ArrayList<UltraSwitch>();
        File switches_file = new File(getDataFolder(), "switches.txt");
        try {
            if (!switches_file.exists()) {
                getDataFolder().mkdir();
                sender.sendMessage(COLOR + "I couldn't find a switches.txt file. I'll make a new one.");
                switches_file.createNewFile();
                return;
            }
            // read the switches.txt file
            BufferedReader in = new BufferedReader(new FileReader(switches_file));
            String save_line = in.readLine();
            while (save_line != null) {
                save_line = save_line.trim();
                if (!save_line.equals(""))
                    switches.add(new UltraSwitch(save_line));
                save_line = in.readLine();
            }
            in.close();
        } catch (IOException exception) {
            processException("I got you a present. It's an IOEcxeption in config.txt.", exception);
            return;
        }
        if (switches.size() > 1) {
            // alphabetize the switches by warp name (by owner secondarily)
            ArrayList<UltraSwitch> temp_switches = new ArrayList<UltraSwitch>();
            for (UltraSwitch my_switch : switches)
                temp_switches.add(my_switch);
            switches = new ArrayList<UltraSwitch>();
            UltraSwitch first_switch;
            int delete_index;
            while (temp_switches.size() > 0) {
                first_switch = temp_switches.get(0);
                delete_index = 0;
                for (int j = 0; j < temp_switches.size(); j++) {
                    if (temp_switches.get(j).warp_name.compareToIgnoreCase(first_switch.warp_name) < 0
                            || (temp_switches.get(j).warp_name.compareToIgnoreCase(first_switch.warp_name) == 0 && temp_switches.get(j).warp_owner
                                    .compareToIgnoreCase(first_switch.warp_owner) < 0)) {
                        first_switch = temp_switches.get(j);
                        delete_index = j;
                    }
                }
                switches.add(first_switch);
                temp_switches.remove(delete_index);
            }
        }
        saveTheSwitches(sender, false);
        if (switches.size() > 1)
            sender.sendMessage(COLOR + "Your " + switches.size() + " switches have been loaded.");
        else if (switches.size() == 1)
            sender.sendMessage(COLOR + "Your 1 switch has been loaded.");
        else
            sender.sendMessage(COLOR + "You have no switches to load!");
        if (sender instanceof Player)
            if (switches.size() > 1)
                console.sendMessage(COLOR + ((Player) sender).getName() + " loaded " + warps.size() + " switches from file.");
            else if (switches.size() == 1)
                console.sendMessage(COLOR + ((Player) sender).getName() + " loaded the server's 1 switch from file.");
            else
                console.sendMessage(COLOR + ((Player) sender).getName() + " loaded the server's switches from file, but there were no switches on file.");
    }

    /** This method loads the warp-related configurations for the whole server from the <tt>config.txt</tt> file in the myUltraWarps folder. If this method is called, any
     * changes made to any configurable settings in game that have not been saved will be lost. Configurations include default warp and no warp messages, whether or not
     * players automatically respawn at home when they die, and more.
     * 
     * @param sender
     *            is the Player or <tt>console</tt> that executed the command to load the configurations from file. When myUltraWarps is enabled and this method is called,
     *            <tt>console</tt> is used as <tt><b>sender</b></tt>. */
    private void loadTheConfig(CommandSender sender) {
        // link up with Vault
        Vault = server.getPluginManager().getPlugin("Vault");
        if (Vault != null) {
            // locate the permissions and economy plugins
            try {
                permissions = server.getServicesManager().getRegistration(Permission.class).getProvider();
            } catch (NullPointerException exception) {
                permissions = null;
            }
            try {
                economy = server.getServicesManager().getRegistration(Economy.class).getProvider();
            } catch (NullPointerException exception) {
                economy = null;
            }
            // forcibly enable the permissions plugin
            if (permissions != null) {
                Plugin permissions_plugin = server.getPluginManager().getPlugin(permissions.getName());
                if (permissions_plugin != null && !permissions_plugin.isEnabled())
                    server.getPluginManager().enablePlugin(permissions_plugin);
            }
            // send confirmation messages
            console.sendMessage(COLOR + "I see your Vault...");
            if (permissions == null && economy == null)
                console.sendMessage(ChatColor.RED + "...but I can't find any economy or permissions plugins.");
            else if (permissions != null) {
                console.sendMessage(COLOR + "...and raise you a " + permissions.getName() + "...");
                if (economy != null)
                    console.sendMessage(COLOR + "...as well as a " + economy.getName() + ".");
                else
                    console.sendMessage(ChatColor.RED + "...but I can't find your economy plugin.");
            } else if (permissions == null && economy != null) {
                console.sendMessage(COLOR + "...and raise you a " + economy.getName() + "...");
                console.sendMessage(ChatColor.RED + "...but I can't find your permissions plugin.");
            }
        }
        // reset settings to the default values
        settings = new HashMap<String, SettingsSet>();
        spawn_messages_by_world = new HashMap<World, String>();
        for (World world : server.getWorlds()) {
            String world_name = world.getName();
            if (world_name.endsWith("_nether"))
                world_name = "The Nether";
            else if (world_name.endsWith("_the_end"))
                world_name = "The End";
            spawn_messages_by_world.put(world, "&aWelcome to " + world_name + ", [player]!");
        }
        // check the config file
        File config_file = new File(getDataFolder(), "config.txt");
        try {
            if (!config_file.exists()) {
                getDataFolder().mkdir();
                sender.sendMessage(COLOR + "I couldn't find a config.txt file. I'll make a new one.");
                config_file.createNewFile();
                saveTheConfig(sender, false);
                return;
            }
            // read the config.txt file
            BufferedReader in = new BufferedReader(new FileReader(config_file));
            String save_line = in.readLine(), parsing = "", parsing_group = null, parsing_player = null;
            while (save_line != null) {

                // skip empty lines
                while (save_line != null && save_line.trim().equals("")) {
                    debug("skipped empty save line: \"" + ChatColor.WHITE + save_line + COLOR + "\"");
                    save_line = in.readLine();
                }
                if (save_line == null)
                    break;
                save_line = save_line.trim();
                debug("\"" + ChatColor.WHITE + save_line + COLOR + "\"");

                // get the configurations
                if (save_line.startsWith("Do you want to be able to change settings for permissions-based groups of users?")) {
                    use_group_settings = getResponse(sender, save_line.substring(80), in.readLine(), "Group settings are enabled");
                    debug("retrieved use group settings setting: " + use_group_settings);
                } else if (save_line.startsWith("Do you want myUltraWarps to check for updates every time it is enabled?")) {
                    auto_update = getResponse(sender, save_line.substring(71), in.readLine(), "Right now, myUltraWarps will auto-update.");
                    debug("retrieved auto update setting: " + auto_update);
                } else if (save_line.startsWith("Do you want myUltraWarps to automatically save the warps file every time a change is made?")) {
                    autosave_warps = getResponse(sender, save_line.substring(90), in.readLine(), "Right now, autosave is on for warps.");
                    debug("retrieved autosave warps setting: " + autosave_warps);
                } else if (save_line.startsWith("Do you want myUltraWarps to automatically save the switches file every time a change is made?")) {
                    autosave_switches = getResponse(sender, save_line.substring(93), in.readLine(), "Right now, autosave is on for switches.");
                    debug("retrieved autosave switches setting: " + autosave_switches);
                } else if (save_line.startsWith("Do you want myUltraWarps to automatically save the config file every time a change is made?")) {
                    autosave_config = getResponse(sender, save_line.substring(91), in.readLine(), "Right now, autosave is on for the config.");
                    debug("retrieved autosave config setting: " + autosave_config);
                } else if (save_line.startsWith("You can set the messages that appear when someone teleports to the spawn point for each world here.")) {
                    parsing = "spawn messages";
                    debug("began parsing spawn messages");
                    save_line = in.readLine();
                    debug("\"" + ChatColor.WHITE + save_line + COLOR + "\"");
                } else if (save_line.startsWith("global settings:")) {
                    parsing = "global";
                    debug("began parsing global settings");
                    save_line = save_line.substring(16);
                    debug("\"" + ChatColor.WHITE + save_line + COLOR + "\"");
                } else if (save_line.startsWith("group settings:")) {
                    parsing = "group";
                    debug("began parsing group settings");
                    save_line = save_line.substring(15);
                    debug("\"" + ChatColor.WHITE + save_line + COLOR + "\"");
                } else if (save_line.startsWith("individual settings:")) {
                    parsing = "individual";
                    debug("began parsing individual settings");
                    save_line = save_line.substring(20);
                    debug("\"" + ChatColor.WHITE + save_line + COLOR + "\"");
                }

                // skip empty lines
                while (save_line != null && save_line.trim().equals("")) {
                    debug("skipped empty save line: \"" + ChatColor.WHITE + save_line + COLOR + "\"");
                    save_line = in.readLine();
                }
                if (save_line == null)
                    break;
                save_line = save_line.trim();
                debug("\"" + ChatColor.WHITE + save_line + COLOR + "\"");

                if (parsing.equals("spawn messages")) {
                    String[] temp = save_line.split(":");
                    String world_name = temp[0], spawn_message = temp[1].trim();
                    if (!world_name.equals("")) {
                        // format the Nether and End world names to cooordinate with the server's world naming system
                        if (world_name.endsWith(" (The Nether)"))
                            world_name = world_name.substring(0, world_name.length() - 13) + "_nether";
                        else if (world_name.endsWith(" (The End)"))
                            world_name = world_name.substring(0, world_name.length() - 10) + "_the_end";
                        World world = server.getWorld(world_name);
                        if (world != null) {
                            spawn_messages_by_world.put(world, spawn_message);
                            debug("read world spawn message for " + world_name + ": \"" + spawn_message + "\"");
                        } else {
                            sender.sendMessage(ChatColor.RED + "I've never heard of a world called \"" + temp[0] + ".\"");
                            debug(ChatColor.DARK_RED + "could not find world \"" + ChatColor.WHITE + temp[0] + ChatColor.DARK_RED + "\"");
                        }
                    }
                } else if (parsing.equals("global")) {
                    SettingsSet global_set = settings.get("[server]");
                    // if the global settings set doesn't exist yet, start a new SettingsSet
                    if (global_set == null)
                        global_set = new SettingsSet();

                    // read!
                    if (save_line.startsWith("Do you want players to automatically teleport to their homes when they respawn?")) {
                        global_set =
                                global_set.setHomeOnDeath(getResponse(sender, save_line.substring(79), in.readLine(),
                                        "Right now, players automatically teleport home after they die."));
                        debug("read home on death global setting: " + global_set.home_on_death);
                    } else if (save_line.toLowerCase().startsWith("default warp message: ")) {
                        global_set = global_set.setDefaultWarpMessage(save_line.substring(22));
                        debug("read default warp message global setting: " + global_set.default_warp);
                    } else if (save_line.toLowerCase().startsWith("default no warp message: ")) {
                        global_set = global_set.setDefaultNoWarpMessage(save_line.substring(25));
                        debug("read default no warp message global setting: " + global_set.default_no_warp);
                    } else if (save_line.toLowerCase().startsWith("max warps: ")) {
                        try {
                            global_set = global_set.setMaxWarps(Integer.parseInt(save_line.substring(11)));
                            debug("read max warps global setting: " + global_set.max_warps);
                        } catch (NumberFormatException exception) {
                            if (save_line.substring(11).equalsIgnoreCase("infinite"))
                                global_set = global_set.setMaxWarps(-1);
                            else {
                                sender.sendMessage(ChatColor.RED + "There was an error in your global settings.");
                                sender.sendMessage(ChatColor.RED + "The maximum number of warps that someone can have has to be an integer or \"infinite.\"");
                            }
                        }
                    } else if (save_line.toLowerCase().startsWith("cool down time: ")) {
                        global_set = global_set.setCooldownTime(readTime(save_line.substring(16)));
                        debug("read cool down time global setting: " + global_set.cooldown);
                    } else if (save_line.toLowerCase().startsWith("warp history length: ")) {
                        try {
                            global_set = global_set.setWarpHistoryLength(Integer.parseInt(save_line.substring(21)));
                            debug("read warp history length global setting: " + global_set.warp_history_length);
                        } catch (NumberFormatException exception) {
                            if (save_line.substring(21).equalsIgnoreCase("infinite"))
                                global_set = global_set.setWarpHistoryLength(-1);
                            else {
                                sender.sendMessage(ChatColor.RED + "There was an error in your global settings.");
                                sender.sendMessage(ChatColor.RED + "The warp history length has to be an integer or \"infinite.\"");
                            }
                        }
                    } else if (save_line.toLowerCase().startsWith("death history length: ")) {
                        try {
                            global_set = global_set.setDeathHistoryLength(Integer.parseInt(save_line.substring(22)));
                            debug("read death history length global setting: " + global_set.death_history_length);
                        } catch (NumberFormatException exception) {
                            if (save_line.substring(22).equalsIgnoreCase("infinite"))
                                global_set = global_set.setDeathHistoryLength(-1);
                            else {
                                sender.sendMessage(ChatColor.RED + "There was an error in your global settings.");
                                sender.sendMessage(ChatColor.RED + "The death history length has to be an integer or \"infinite.\"");
                            }
                        }
                    }
                    // update settings
                    settings.put("[server]", global_set);
                } else if (parsing.equals("group") && use_group_settings && permissions != null) {
                    // if it's the beginning of a set, get the group name; otherwise, we're probably in the middle of a set, so just use the parsing_group
                    // that's already there
                    if (!save_line.split(":")[0].contains(" "))
                        if (save_line.endsWith(":")) {
                            parsing_group = save_line.substring(0, save_line.length() - 1);
                            save_line = in.readLine();
                        }
                        // this part is to make sure reading the config works even if someone made an error when they were typing in the config and put a
                        // settings line on the same line as the group's name line
                        else if (save_line.split(":").length > 1) {
                            parsing_group = save_line.split(":")[0];
                            save_line = save_line.split(":")[1];
                            while (save_line.startsWith(" "))
                                save_line = save_line.substring(1);
                        }
                    if (parsing_group != null && !parsing_group.equals("")) {
                        SettingsSet group_set = settings.get("[" + parsing_group + "]");
                        // if a SettingsSet for this group doesn't exist yet, start a new one
                        if (group_set == null)
                            // default to server settings, but if they don't exist yet, use myUltraWarps defaults
                            if (settings.get("[server]") != null)
                                group_set = settings.get("[server]");
                            else
                                group_set = new SettingsSet();
                        save_line = save_line.trim();
                        if (save_line.startsWith("Do you want players in this group to automatically teleport to their homes when they respawn?"))
                            group_set =
                                    group_set.setHomeOnDeath(getResponse(sender, save_line.substring(93), in.readLine(),
                                            "Right now, players in this group automatically teleport home after they die."));
                        else if (save_line.toLowerCase().startsWith("default warp message: "))
                            group_set = group_set.setDefaultWarpMessage(save_line.substring(22));
                        else if (save_line.toLowerCase().startsWith("default no warp message: "))
                            group_set = group_set.setDefaultNoWarpMessage(save_line.substring(25));
                        else if (save_line.toLowerCase().startsWith("max warps: ")) {
                            try {
                                group_set = group_set.setMaxWarps(Integer.parseInt(save_line.substring(11)));
                            } catch (NumberFormatException exception) {
                                if (save_line.substring(11).equalsIgnoreCase("infinite"))
                                    group_set = group_set.setMaxWarps(-1);
                                else
                                    sender.sendMessage(ChatColor.RED
                                            + "There was an error in your group settings.\nThe maximum number of warps that someone can have has to be an integer or \"infinite.\"");
                            }
                        } else if (save_line.toLowerCase().startsWith("cool down time: "))
                            group_set = group_set.setCooldownTime(readTime(save_line.substring(16)));
                        else if (save_line.toLowerCase().startsWith("warp history length: ")) {
                            try {
                                group_set = group_set.setWarpHistoryLength(Integer.parseInt(save_line.substring(21)));
                            } catch (NumberFormatException exception) {
                                if (save_line.substring(21).equalsIgnoreCase("infinite"))
                                    group_set = group_set.setWarpHistoryLength(-1);
                                else
                                    sender.sendMessage(ChatColor.RED + "There was an error in your group settings.\nThe warp history length to be an integer or \"infinite.\"");
                            }
                        } else if (save_line.toLowerCase().startsWith("death history length: ")) {
                            try {
                                group_set = group_set.setDeathHistoryLength(Integer.parseInt(save_line.substring(22)));
                            } catch (NumberFormatException exception) {
                                if (save_line.substring(22).equalsIgnoreCase("infinite"))
                                    group_set = group_set.setDeathHistoryLength(-1);
                                else
                                    sender.sendMessage(ChatColor.RED
                                            + "There was an error in your group settings.\nThe death history length has to be an integer or \"infinite.\"");
                            }
                        }
                        settings.put("[" + parsing_group + "]", group_set);
                    }
                } else if (parsing.equals("individual")) {
                    // if it's the beginning of a set, get the player's name; otherwise, we're probably in the middle of a set, so just use the parsing_player
                    // that's already there
                    if (!save_line.split(":")[0].contains(" "))
                        if (save_line.endsWith(":")) {
                            parsing_player = save_line.substring(0, save_line.length() - 1);
                            save_line = in.readLine();
                        }
                        // this part is to make sure reading the config works even if someone made an error when they were typing in the config and put a
                        // settings line on the same line as the group's name line
                        else if (save_line.split(":").length > 1) {
                            parsing_player = save_line.split(":")[0];
                            save_line = save_line.split(":")[1];
                            while (save_line.startsWith(" "))
                                save_line = save_line.substring(1);
                        }
                    if (parsing_player != null && !parsing_player.equals("") && !parsing_player.equals("1mAnExampl3")) {
                        SettingsSet player_set = settings.get(parsing_player);
                        // if a SettingsSet for this group doesn't exist yet, start a new one
                        if (player_set == null)
                            // default to group settings
                            if (use_group_settings && permissions != null && permissions.getPrimaryGroup(parsing_player, null) != null
                                    && settings.get(permissions.getPrimaryGroup(parsing_player, null)) != null)
                                player_set = settings.get(permissions.getPrimaryGroup(parsing_player, null));
                            // if the group settings for this player aren't available, default ot server settings
                            else if (settings.get("[server]") != null)
                                player_set = settings.get("[server]");
                            // if server settings don't exist yet either for some strange reason, use myUltraWarps defaults
                            else
                                player_set = new SettingsSet();
                        // read!
                        // eliminate preceding spaces
                        while (save_line.startsWith(" "))
                            save_line = save_line.substring(1);
                        if (save_line.startsWith("Do you want " + parsing_player + " to automatically teleport to their home when they respawn?"))
                            player_set =
                                    player_set.setHomeOnDeath(getResponse(sender, save_line.substring(71 + parsing_player.length()), in.readLine(), "Right now, "
                                            + parsing_player + " automatically teleports home after they die."));
                        else if (save_line.toLowerCase().startsWith("default warp message: "))
                            player_set = player_set.setDefaultWarpMessage(save_line.substring(22));
                        else if (save_line.toLowerCase().startsWith("default no warp message: "))
                            player_set = player_set.setDefaultNoWarpMessage(save_line.substring(25));
                        else if (save_line.toLowerCase().startsWith("max warps: ")) {
                            try {
                                player_set = player_set.setMaxWarps(Integer.parseInt(save_line.substring(11)));
                            } catch (NumberFormatException exception) {
                                if (save_line.substring(11).equalsIgnoreCase("infinite"))
                                    player_set = player_set.setMaxWarps(-1);
                                else {
                                    sender.sendMessage(ChatColor.RED + "There was an error in your individual settings.");
                                    sender.sendMessage(ChatColor.RED + "The maximum number of warps that someone can have has to be an integer or \"infinite.\"");
                                }
                            }
                        } else if (save_line.toLowerCase().startsWith("cool down time: "))
                            player_set = player_set.setCooldownTime(readTime(save_line.substring(16)));
                        else if (save_line.toLowerCase().startsWith("warp history length: ")) {
                            try {
                                player_set = player_set.setWarpHistoryLength(Integer.parseInt(save_line.substring(21)));
                            } catch (NumberFormatException exception) {
                                if (save_line.substring(21).equalsIgnoreCase("infinite"))
                                    player_set = player_set.setWarpHistoryLength(-1);
                                else {
                                    sender.sendMessage(ChatColor.RED + "There was an error in your individual settings.");
                                    sender.sendMessage(ChatColor.RED + "The warp history length has to be an integer or \"infinite.\"");
                                    sender.sendMessage(ChatColor.RED + "I'm setting the warp history length for " + parsing_player + " to \"infinite.\"");
                                }
                            }
                        } else if (save_line.toLowerCase().startsWith("death history length: ")) {
                            try {
                                player_set = player_set.setDeathHistoryLength(Integer.parseInt(save_line.substring(22)));
                            } catch (NumberFormatException exception) {
                                if (save_line.substring(22).equalsIgnoreCase("infinite"))
                                    player_set = player_set.setDeathHistoryLength(-1);
                                else {
                                    sender.sendMessage(ChatColor.RED + "There was an error in your individual settings.");
                                    sender.sendMessage(ChatColor.RED + "The death history length has to be an integer or \"infinite.\"");
                                    sender.sendMessage(ChatColor.RED + "I'm setting the death history length for " + parsing_player + " to \"infinite.\"");
                                }
                            }
                        }
                        settings.put(parsing_player, player_set);
                    }
                }
                save_line = in.readLine();
            }
            in.close();
        } catch (IOException exception) {
            processException("I got you a present. It's an IOEcxeption in config.txt.", exception);
        }
        // if a world's spawn message is not configured, use the default
        for (World world : server.getWorlds()) {
            if (!spawn_messages_by_world.containsKey(world)) {
                String world_name = world.getWorldFolder().getName();
                if (world_name.endsWith("_nether"))
                    world_name = "The Nether";
                else if (world_name.endsWith("_the_end"))
                    world_name = "The End";
                spawn_messages_by_world.put(world, "&aWelcome to " + world_name + ", [player].");
            }
        }
        saveTheConfig(sender, false);
        sender.sendMessage(COLOR + "Your configurations have been loaded.");
        if (sender instanceof Player)
            console.sendMessage(COLOR + sender.getName() + " loaded the myUltraWarps config from file.");
    }

    /** This method loads important data from a read-only temporary file (<tt>temp.txt</tt>) when myUltraWarps is enabled. This data includes blocked and trusted players, info
     * messages concerning events such as broken warping switches, and more. When this method completes, the <tt>temp.txt</tt> file is deleted. Unlike other myUltraWarps
     * loading methods, this method cannot be called using a command. It is executed only when myUltraWarps is enabled and does not display any confirmational messages. */
    private void loadTheTemporaryData() {
        // check the temporary file
        File temp_file = new File(getDataFolder(), "temp.txt");
        if (temp_file.exists())
            // read the temp.txt file
            try {
                BufferedReader in = new BufferedReader(new FileReader(temp_file));
                String save_line = in.readLine(), data_type = "", player = "";
                while (save_line != null) {
                    save_line = save_line.trim();
                    // skip empty lines
                    while (save_line != null && save_line.equals(""))
                        save_line = in.readLine();
                    if (save_line == null)
                        break;
                    if (save_line.startsWith("==== "))
                        data_type = save_line.substring(5, save_line.length() - 5);
                    else if (save_line.startsWith("== "))
                        player = save_line.split(" ")[1];
                    else if (data_type.equals("warp histories")) {
                        // if it's a last warp index, save it as that
                        try {
                            last_warp_indexes.put(player, Integer.parseInt(save_line));
                        } catch (NumberFormatException exception) {
                            // otherwise, read it as a warp save line
                            ArrayList<UltraWarp> warp_history = warp_histories.get(player);
                            if (warp_history == null)
                                warp_history = new ArrayList<UltraWarp>();
                            warp_history.add(new UltraWarp(save_line));
                            warp_histories.put(player, warp_history);
                        }
                    } else if (data_type.equals("death histories")) {
                        // if it's a last warp to death index, save it as that
                        try {
                            last_warp_to_death_indexes.put(player, Integer.parseInt(save_line));
                        } catch (NumberFormatException exception) {
                            // otherwise, read it as a death location
                            ArrayList<Location> death_history = death_histories.get(player);
                            if (death_history == null)
                                death_history = new ArrayList<Location>();
                            try {
                                String[] temp = save_line.split(",");
                                World world = null;
                                for (World my_world : server.getWorlds())
                                    if (my_world.getWorldFolder().getName().equals(temp[0])) {
                                        world = my_world;
                                        break;
                                    }
                                death_history.add(new Location(world, Double.parseDouble(temp[1]), Double.parseDouble(temp[2]), Double.parseDouble(temp[3]), Float
                                        .parseFloat(temp[4]), Float.parseFloat(temp[5])));
                                death_histories.put(player, death_history);
                            } catch (NumberFormatException exception2) {
                                processException("There was a problem reading the death history from the temporary data file!", exception2);
                            }
                        }
                    } else if (data_type.equals("blocked players")) {
                        ArrayList<String> replacement = blocked_players.get(player);
                        if (replacement == null)
                            replacement = new ArrayList<String>();
                        replacement.add(save_line);
                        blocked_players.put(player, replacement);
                    } else if (data_type.equals("cool down times")) {
                        try {
                            cooling_down_players.put(player, Long.parseLong(save_line));
                        } catch (NumberFormatException exception) {
                            processException("There was an error in loading the cool down time data for " + player + " from the temporary file!", exception);
                        }
                    }
                    save_line = in.readLine();
                }
                in.close();
                temp_file.setWritable(true);
                temp_file.delete();
            } catch (IOException exception) {
                processException("I got an IOException while trying to load the temporary data.", exception);
                return;
            }
    }

    // saving
    /** This method saves the warp data from the server in a readable, modifiable format in the <tt>warps.txt</tt> file in the myUltraWarps folder. If this method is called,
     * any changes made in the <tt>warp.txt</tt> file itself will be overwritten using the information stored on the server. In addition to being called using a command and
     * when myUltraWarps is disabled, this method is always called after loading the warp data from the <tt>warps.txt</tt> in order to refresh and reformat any changed areas
     * of the <tt>warps.txt</tt> file.
     * 
     * @param sender
     *            is the Player or <tt>console</tt> that executed the command to save the warps data from the server to the <tt>warps.txt</tt>. When myUltraWarps is disabled
     *            and this method is called, <tt>console</tt> is used as <tt><b>sender</b></tt>.
     * @param display_message
     *            designates whether or not this method should display confirmation messages at its finish. If this method is called to refresh the <tt>warps.txt</tt> after
     *            loading the warps data, this parameter will be <b>false</b>. If this method is called using a command or when myUltraWarps is disabled, this parameter will
     *            be <b>true</b>. */
    private void saveTheWarps(CommandSender sender, boolean display_message) {
        // check the warps file
        File warps_file = new File(getDataFolder(), "warps.txt");
        // save the warps
        try {
            if (!warps_file.exists()) {
                getDataFolder().mkdir();
                sender.sendMessage(COLOR + "I couldn't find a warps.txt file. I'll make a new one.");
                warps_file.createNewFile();
            }
            BufferedWriter out = new BufferedWriter(new FileWriter(warps_file));
            for (int i = 0; i < warps.size(); i++) {
                out.write(warps.get(i).toString());
                if (i < warps.size() - 1)
                    out.newLine();
            }
            out.flush();
            out.close();
        } catch (IOException exception) {
            processException("I got an IOException while trying to save your warps.", exception);
            return;
        }
        if (display_message) {
            if (warps.size() > 1)
                sender.sendMessage(COLOR + "Your " + warps.size() + " warps have been saved.");
            else if (warps.size() == 1)
                sender.sendMessage(COLOR + "Your 1 warp has been saved.");
            else
                sender.sendMessage(COLOR + "You have no warps to save!");
            if (sender instanceof Player)
                if (warps.size() > 1)
                    console.sendMessage(COLOR + ((Player) sender).getName() + " saved " + warps.size() + " warps to file.");
                else if (warps.size() == 1)
                    console.sendMessage(COLOR + ((Player) sender).getName() + " saved the server's 1 warp to file.");
                else
                    console.sendMessage(COLOR + ((Player) sender).getName() + " tried to save the server's warps to file, but there were no warps on the server to save.");
        }
    }

    /** This method saves the switch warp data from the server in a readable, modifiable format in the <tt>switches.txt</tt> file in the myUltraWarps folder. If this method is
     * called, any changes made in the <tt>switches.txt</tt> file itself will be overwritten using the information stored on the server. In addition to being called using a
     * command and when myUltraWarps is disabled, this method is always called after loading the switch warp data from the <tt>switches.txt</tt> in order to refresh and
     * reformat any changed areas of the <tt>switches.txt</tt> file.
     * 
     * @param sender
     *            is the Player or <tt>console</tt> that executed the command to save the switch warps data from the server to the <tt>switches.txt</tt>. When myUltraWarps is
     *            disabled and this method is called, <tt>console</tt> is used as <tt><b>sender</b></tt>.
     * @param display_message
     *            designates whether or not this method should display confirmation messages at its finish. If this method is called to refresh the <tt>switches.txt</tt> after
     *            loading the warps data, this parameter will be <b>false</b>. If this method is called using a command or when myUltraWarps is disabled, this parameter will
     *            be <b>true</b>. */
    private void saveTheSwitches(CommandSender sender, boolean display_message) {
        // check the switches file
        File switches_file = new File(getDataFolder(), "switches.txt");
        // save the switches
        try {
            if (!switches_file.exists()) {
                getDataFolder().mkdir();
                sender.sendMessage(COLOR + "I couldn't find a switches.txt file. I'll make a new one.");
                switches_file.createNewFile();
            }
            BufferedWriter out = new BufferedWriter(new FileWriter(switches_file));
            for (UltraSwitch my_switch : switches) {
                out.write(my_switch.toString());
                out.newLine();
            }
            out.flush();
            out.close();
        } catch (IOException exception) {
            processException("I got an IOException while trying to save your switches.", exception);
            exception.printStackTrace();
            return;
        }
        if (display_message) {
            if (switches.size() > 1)
                sender.sendMessage(COLOR + "Your " + switches.size() + " switches have been saved.");
            else if (switches.size() == 1)
                sender.sendMessage(COLOR + "Your 1 switch has been saved.");
            else
                sender.sendMessage(COLOR + "You have no switches to save!");
            if (sender instanceof Player)
                if (switches.size() > 1)
                    console.sendMessage(COLOR + ((Player) sender).getName() + " saved " + switches.size() + " switches to file.");
                else if (switches.size() == 1)
                    console.sendMessage(COLOR + ((Player) sender).getName() + " saved the server's 1 switch to file.");
                else
                    console.sendMessage(COLOR + ((Player) sender).getName() + " tried to save the server's warps to file, but there were no switches on the server to save.");
        }
    }

    /** This method saves the myUltraWarps configuration data from the server in the <tt>config.txt</tt> file in the myUltraWarps folder. If this method is called, any changes
     * made in the <tt>config.txt</tt> file itself will be overwritten using the information stored on the server. In addition to being called using a command and when
     * myUltraWarps is disabled, this method is always called after loading the config data from the <tt>config.txt</tt> in order to refresh and reformat any changed areas of
     * the <tt>config.txt</tt> file.
     * 
     * @param sender
     *            is the Player or <tt>console</tt> that executed the command to save the configuration data from the server to the <tt>config.txt</tt>. When myUltraWarps is
     *            disabled and this method is called, <tt>console</tt> is used as <tt><b>sender</b></tt>.
     * @param display_message
     *            designates whether or not this method should display confirmation messages at its finish. If this method is called to refresh the <tt>config.txt</tt> after
     *            loading the warps data, this parameter will be <b>false</b>. If this method is called using a command or when myUltraWarps is disabled, this parameter will
     *            be <b>true</b>. */
    private void saveTheConfig(CommandSender sender, boolean display_message) {
        File config_file = new File(getDataFolder(), "config.txt");
        // save the configurations
        try {
            if (!config_file.exists()) {
                getDataFolder().mkdir();
                sender.sendMessage(COLOR + "I couldn't find a config.txt file. I'll make a new one.");
                config_file.createNewFile();
            }
            BufferedWriter out = new BufferedWriter(new FileWriter(config_file));
            out.write("Remember to use /myUltraWarps save the config before you modify this file if autosave is off and use /myUltraWarps load the config when you're done.");
            out.newLine();
            out.newLine();
            out.write("Do you want to be able to change settings for permissions-based groups of users? ");
            out.newLine();
            if (use_group_settings)
                if (Vault == null)
                    out.write("   Group settings are enabled, but you don't have Vault! You need Vault and a compatible permissions plugin for group settings to work.");
                else if (permissions == null)
                    out.write("   Group settings are enabled, but you don't have a permissions plugin compatible with Vault! You need one for group settings to work.");
                else
                    out.write("   Group settings are enabled right now.");
            else {
                out.write("   Group settings are disabled right now.");
                out.newLine();
                out.write("   You need Vault and a compatible permissions plugin in order for this to work.");
            }
            out.newLine();
            out.newLine();
            out.write("Do you want myUltraWarps to check for updates every time it is enabled? ");
            out.newLine();
            if (auto_update)
                out.write("   Right now, myUltraWarps will auto-update.");
            else
                out.write("   Right now, myUltraWarps will not auto-update! I REALLY think you should let it auto-update!");
            out.newLine();
            out.newLine();
            out.write("Do you want myUltraWarps to automatically save the warps file every time a change is made? ");
            out.newLine();
            if (autosave_warps)
                out.write("   Right now, autosave is on for warps.");
            else
                out.write("   Right now, autosave is off for warps.");
            out.newLine();
            out.write("Do you want myUltraWarps to automatically save the switches file every time a change is made? ");
            out.newLine();
            if (autosave_switches)
                out.write("   Right now, autosave is on for switches.");
            else
                out.write("   Right now, autosave is off for switches.");
            out.newLine();
            out.write("Do you want myUltraWarps to automatically save the config file every time a change is made? ");
            out.newLine();
            if (autosave_config)
                out.write("   Right now, autosave is on for the config.");
            else
                out.write("   Right now, autosave is off for the config.");
            out.newLine();
            out.newLine();
            out.write("You can set the messages that appear when someone teleports to the spawn point for each world here.");
            out.newLine();
            for (World world : server.getWorlds()) {
                String spawn_message = spawn_messages_by_world.get(world);
                if (spawn_message != null) {
                    String world_name = world.getWorldFolder().getName();
                    if (world_name.endsWith("_nether"))
                        world_name = world_name.substring(0, world_name.length() - 7) + " (The Nether)";
                    else if (world_name.endsWith("_the_end"))
                        world_name = world_name.substring(0, world_name.length() - 8) + " (The End)";
                    out.write("     " + world_name + ": " + spawn_message);
                    out.newLine();
                }
            }
            out.newLine();
            out.write("global settings:");
            out.newLine();
            // get the server settings or use myUltraWarps defaults if for some reason the sevrer settings aren't available
            SettingsSet global_set = settings.get("[server]");
            if (global_set == null)
                global_set = new SettingsSet();
            out.write("     Do you want players to automatically teleport to their homes when they respawn? ");
            out.newLine();
            if (global_set.home_on_death)
                out.write("        Right now, players automatically teleport home after they die.");
            else
                out.write("        Right now, players don't automatically teleport home after they die.");
            out.newLine();
            out.write("     default warp message: " + global_set.default_warp);
            out.newLine();
            out.write("     default no warp message: " + global_set.default_no_warp);
            out.newLine();
            if (global_set.max_warps != -1)
                out.write("     max warps: " + global_set.max_warps);
            else
                out.write("     max warps: infinite");
            out.newLine();
            out.write("     cool down time: " + writeTime(global_set.cooldown, false));
            out.newLine();
            if (global_set.warp_history_length != -1)
                out.write("     warp history length: " + global_set.warp_history_length);
            else
                out.write("     warp history length: infinite");
            out.newLine();
            if (global_set.death_history_length != -1)
                out.write("     death history length: " + global_set.death_history_length);
            else
                out.write("     death history length: infinite");
            out.newLine();
            out.newLine();
            if (use_group_settings && permissions != null && permissions.getGroups() != null && permissions.getGroups().length > 0) {
                out.write("group settings:");
                out.newLine();
                for (String group : permissions.getGroups()) {
                    if (!group.equals("default")) {
                        SettingsSet group_set = settings.get("[" + group + "]");
                        if (group_set == null)
                            group_set = global_set;
                        out.write("     " + group + ":");
                        out.newLine();
                        out.write("          Do you want players in this group to automatically teleport to their homes when they respawn? ");
                        out.newLine();
                        if (group_set.home_on_death)
                            out.write("             Right now, players in this group automatically teleport home after they die.");
                        else
                            out.write("             Right now, players in this group don't automatically teleport home after they die.");
                        out.newLine();
                        out.write("          default warp message: " + group_set.default_warp);
                        out.newLine();
                        out.write("          default no warp message: " + group_set.default_no_warp);
                        out.newLine();
                        if (group_set.max_warps != -1)
                            out.write("          max warps: " + group_set.max_warps);
                        else
                            out.write("          max warps: infinite");
                        out.newLine();
                        out.write("          cool down time: " + writeTime(group_set.cooldown, false));
                        out.newLine();
                        if (group_set.warp_history_length != -1)
                            out.write("          warp history length: " + group_set.warp_history_length);
                        else
                            out.write("          warp history length: infinite");
                        out.newLine();
                        if (group_set.death_history_length != -1)
                            out.write("          death history length: " + group_set.death_history_length);
                        else
                            out.write("          death history length: infinite");
                        out.newLine();
                    }
                }
                out.newLine();
            } else {
                if (!use_group_settings)
                    out.write("Group settings are off; otherwise, group settings stuff would go here!");
                else if (use_group_settings && permissions == null)
                    out.write("You need Vault to change group settings!");
                else
                    out.write("I would put your group settings stuff here, but even though you have Vault and a good permissions plugin, you have no group set up yet!");
                out.newLine();
                out.newLine();
            }
            // compile a list of all the individual settings in settings
            HashMap<String, SettingsSet> player_sets = new HashMap<String, SettingsSet>();
            for (String key : settings.keySet())
                if (!key.startsWith("[") && !key.startsWith("]"))
                    player_sets.put(key, settings.get(key));
            // if there were no individual settings, add an example individual setting
            if (player_sets.size() == 0)
                player_sets.put("1mAnExampl3", new SettingsSet());
            // write!
            out.write("individual settings:");
            out.newLine();
            for (String key : player_sets.keySet()) {
                SettingsSet player_set = player_sets.get(key);
                out.write("     " + key + ":");
                out.newLine();
                out.write("          Do you want " + key + " to automatically teleport to their home when they respawn? ");
                out.newLine();
                if (player_set.home_on_death)
                    out.write("             Right now, " + key + " automatically teleports home after they die.");
                else
                    out.write("             Right now, " + key + " doesn't automatically teleport home after they die.");
                out.newLine();
                out.write("          default warp message: " + player_set.default_warp);
                out.newLine();
                out.write("          default no warp message: " + player_set.default_no_warp);
                out.newLine();
                if (player_set.max_warps != -1)
                    out.write("          max warps: " + player_set.max_warps);
                else
                    out.write("          max warps: infinite");
                out.newLine();
                out.write("          cool down time: " + writeTime(player_set.cooldown, false));
                out.newLine();
                if (player_set.warp_history_length != -1)
                    out.write("          warp history length: " + player_set.warp_history_length);
                else
                    out.write("          warp history length: infinite");
                out.newLine();
                if (player_set.death_history_length != -1)
                    out.write("          death history length: " + player_set.death_history_length);
                else
                    out.write("          death history length: infinite");
                out.newLine();
            }
            out.close();
        } catch (IOException exception) {
            processException("I got an IOException while trying to save your configurations.", exception);
            exception.printStackTrace();
            return;
        }
        if (display_message) {
            sender.sendMessage(COLOR + "Your configurations have been saved.");
            if (sender instanceof Player)
                console.sendMessage(COLOR + ((Player) sender).getName() + " saved the server's configurations to file.");
        }
    }

    /** This method saves important data from a read-only temporary file (<tt>temp.txt</tt>) when myUltraWarps is enabled. This data includes blocked and trusted players, info
     * messages concerning events such as broken warping switches, and more. Unlike other myUltraWarps loading methods, this method cannot be called using a command. It is
     * executed only when myUltraWarps is enabled and does not display any confirmational messages. */
    private void saveTheTemporaryData() {
        File temp_file = new File(getDataFolder(), "temp.txt");
        try {
            // check the temporary file
            if (!temp_file.exists()) {
                getDataFolder().mkdir();
                temp_file.createNewFile();
            }
            temp_file.setWritable(true);
            // save the temporary data
            BufferedWriter out = new BufferedWriter(new FileWriter(temp_file));
            out.write("==== warp histories ====");
            out.newLine();
            for (String key : warp_histories.keySet()) {
                out.write("== " + key + " ==");
                out.newLine();
                for (UltraWarp warp : warp_histories.get(key)) {
                    out.write(warp.toString());
                    out.newLine();
                }
                if (last_warp_indexes.get(key) != null) {
                    out.write((Integer) last_warp_indexes.get(key));
                    out.newLine();
                }
            }
            out.write("==== death histories ====");
            out.newLine();
            for (String key : death_histories.keySet()) {
                out.write("== " + key + " ==");
                out.newLine();
                for (Location death : death_histories.get(key)) {
                    out.write(death.getWorld().getName() + "," + death.getX() + "," + death.getY() + "," + death.getZ() + "," + death.getYaw() + "," + death.getPitch());
                    out.newLine();
                }
                if (last_warp_to_death_indexes.get(key) != null) {
                    out.write(last_warp_to_death_indexes.get(key));
                    out.newLine();
                }
            }
            out.write("==== blocked players ====");
            out.newLine();
            for (String key : blocked_players.keySet())
                if (blocked_players.get(key) != null && blocked_players.get(key).size() > 0) {
                    out.write("== " + key + " ==");
                    out.newLine();
                    for (String blocked_player : blocked_players.get(key)) {
                        out.write(blocked_player);
                        out.newLine();
                    }
                }
            out.write("==== cool down times ====");
            out.newLine();
            for (String key : cooling_down_players.keySet())
                if (cooling_down_players.get(key) != null) {
                    out.write("== " + key + " ==");
                    out.newLine();
                    out.write(String.valueOf(cooling_down_players.get(key)));
                    out.newLine();
                }
            out.flush();
            out.close();
            temp_file.setReadOnly();
        } catch (IOException exception) {
            processException("I got an IOException while trying to save your temporary data.", exception);
            exception.printStackTrace();
            return;
        }
    }

    // plugin commands
    /** This method is called when <b><tt>sender</tt></b> uses the command <i>/mUW help (page #)</i> or <i>/mUW</i>. It lists the myUltraWarps commands that <b>
     * <tt>sender</b></tt> is permitted to use and how to use them with usages and brief descriptions of how the commands work and what they do.
     * 
     * @param sender
     *            is the Player or <tt>console</tt> who executed the command. */
    private void displayHelp(CommandSender sender, String[] parameters) {
        // establish the number of characters in a page
        int lines_per_page = 27;
        if (sender instanceof Player)
            lines_per_page = 10;
        // read the requested page number
        int page_number = 0;
        int extra_param = 0;
        if (parameters.length > 0 && parameters[0].equalsIgnoreCase("help"))
            extra_param++;
        if (parameters.length > extra_param)
            try {
                page_number = Integer.parseInt(parameters[extra_param]);
            } catch (NumberFormatException exception) {
                sender.sendMessage(ChatColor.RED + "How long has \"" + parameters[extra_param] + "\" been an integer? I must really be out of the loop.");
            }
        if (page_number == 0) {
            sender.sendMessage(colorCode("&f\"()\" indicate an optional parameter while \"[]\" indicate a required parameter. If a parameter is in quotes, it means that that word itself is the parameter; otherwise, substitute the piece of data for the parameter. Almost everything is not case-sensitive and you can use even just a single letter to search for it. For example, instead of typing &a&o/warp Play3r's house&f, you can just type &a&o/warp p's h &fif you like. Use &o&a/mUW help [page #] &fto go through the help pages. &cT&fh&ca&ft&c'&fs &ca&fl&cl&f, &cf&fo&cl&fk&cs&f!"));
        } else if (page_number < 0) {
            sender.sendMessage(ChatColor.RED + "Last time I checked, negative page numbers don't make sense.");
        } else {
            // do it
            int number_of_lines = 0;
            ArrayList<String> pages = new ArrayList<String>();
            String current_page = "";
            for (int i = 0; i < help_pages.size(); i++) {
                String help_line = (String) help_pages.get(i)[0];
                String basic_permission_node = (String) help_pages.get(i)[1];
                boolean included_with_user = (Boolean) help_pages.get(i)[2];
                int chat_lines_in_this_line = (Integer) help_pages.get(i)[3];
                if (!(sender instanceof Player) || sender.hasPermission("myultrawarps.admin") || (included_with_user && sender.hasPermission("myultrawarps.user"))
                        || sender.hasPermission(basic_permission_node) || sender.hasPermission(basic_permission_node + ".other")) {
                    if (i != 0)
                        current_page = current_page + "\n";
                    number_of_lines = number_of_lines + chat_lines_in_this_line;
                    if (number_of_lines > lines_per_page) {
                        pages.add(current_page);
                        current_page = "";
                        number_of_lines = chat_lines_in_this_line;
                    }
                    current_page = current_page + help_line;
                }
            }
            pages.add(current_page);
            if (page_number > pages.size())
                if (pages.size() == 1)
                    sender.sendMessage(ChatColor.RED + "There is only one help page for you.");
                else
                    sender.sendMessage(ChatColor.RED + "There are only " + pages.size() + " help pages for you.");
            else
                sender.sendMessage(colorCode(pages.get(page_number - 1)));
        }
    }

    /** This method is called when <b><tt>sender</tt></b> uses the command <i>/back (amount)</i>. <b><tt>sender</b></tt> must be a Player for this method to be called. This
     * command teleports players back through their warping hostory. Every time someone is teleported to a warp or another player, that teleportation is saved in the player's
     * warp history; <i>/back</i> works like the back button on an Internet browser, teleporting players backward through their warp history.
     * 
     * @param sender
     *            is the Player who executed the command.
     * @see {@link #forward(CommandSender) forward(CommandSender)} */
    private void back(CommandSender sender, String[] parameters) {
        Player player = (Player) sender;
        int amount = 1;
        if (parameters.length > 0)
            try {
                amount = Integer.parseInt(parameters[0]);
                if (amount == 0) {
                    sender.sendMessage(COLOR + "Well, here you are. You went back 0 warps through your history.");
                    return;
                } else if (amount < 0) {
                    sender.sendMessage(ChatColor.RED + "Going back backwards.... Sorry, but I can't see into the future. At least...not that far ahead.");
                    return;
                }
            } catch (NumberFormatException exception) {
                sender.sendMessage(ChatColor.RED + "Since when is \"" + parameters[0] + "\" an integer?");
                return;
            }
        ArrayList<UltraWarp> my_warp_histories = warp_histories.get(player.getName());
        Integer last_warp_index = last_warp_indexes.get(player.getName());
        // default the last warp index to the latest warp in the warp history
        if (last_warp_index == null)
            last_warp_index = my_warp_histories.size() - 1;
        UltraWarp last_warp = null;
        if (my_warp_histories != null && last_warp_index >= amount)
            last_warp = my_warp_histories.get(last_warp_index - amount);
        else {
            if (my_warp_histories == null || my_warp_histories.size() == 0)
                sender.sendMessage(ChatColor.RED + "You haven't warped anywhere yet!");
            else if (last_warp_index > 1)
                sender.sendMessage(ChatColor.RED + "You can only go back " + last_warp_index + " more warps.");
            else if (last_warp_index == 1)
                sender.sendMessage(ChatColor.RED + "You can only go back one more warp.");
            else
                sender.sendMessage(ChatColor.RED + "Sorry, but I don't keep track of that many warps. This is as far back as you can go.");
            return;
        }
        if (last_warp != null) {
            boolean player_is_listed = false;
            if (last_warp.listed_users != null)
                for (String listed_player : last_warp.listed_users)
                    if (listed_player.equals(player.getName()))
                        player_is_listed = true;
            if (player.hasPermission("myultrawarps.admin") || player.hasPermission("myultrawarps.warptowarp.other") || last_warp.owner.equals(player.getName())
                    || (!last_warp.restricted && !player_is_listed) || (last_warp.restricted && player_is_listed)) {
                teleport(player, null, last_warp, true, null);
                last_warp_indexes.put(player.getName(), last_warp_index - amount);
            } else
                player.sendMessage(colorCode(last_warp.no_warp_message.replaceAll("\\[player\\]", player.getName())));
        }
    }

    /** This method is called when <b><tt>sender</tt></b> uses the command <i>/block [player]</i>. <b><tt>Sender</b></tt> must be a Player for this method to be called. This
     * command adds <tt>[player]</tt> to <b><tt>sender</b></tt>'s blocked players list, which means that any teleportation requests sent by <tt>[player]</tt> to <b>
     * <tt>sender</b></tt> will be automatically stopped.
     * 
     * @param sender
     *            is the Player who executed the command.
     * @see {@link #unblock(CommandSender) unblock(CommandSender)} and {@link #blockList(int, CommandSender) blockList(int, CommandSender)} */
    private void block(CommandSender sender, String[] parameters) {
        Player player = (Player) sender;
        String blocked_player = null;
        Player target_player = null;
        for (Player my_player : server.getOnlinePlayers())
            if (my_player.getName().toLowerCase().startsWith(parameters[0].toLowerCase()) && !my_player.getName().equals(player.getName())) {
                target_player = my_player;
                blocked_player = my_player.getName();
                break;
            }
        if (blocked_player == null)
            for (OfflinePlayer my_player : server.getOfflinePlayers())
                if (my_player.getName().toLowerCase().startsWith(parameters[0].toLowerCase()) && !my_player.getName().equals(player.getName())) {
                    blocked_player = my_player.getName();
                    break;
                }
        if (blocked_player == null)
            blocked_player = parameters[0];
        if (target_player == null || !target_player.hasPermission("myultrawarps.admin")) {
            ArrayList<String> my_blocked_players = blocked_players.get(player.getName());
            if (my_blocked_players == null)
                my_blocked_players = new ArrayList<String>();
            my_blocked_players.add(blocked_player);
            blocked_players.put(player.getName(), my_blocked_players);
            sender.sendMessage(COLOR + blocked_player + " can no longer send you teleportation requests.");
            // remove all to and from teleportation requests to this player from the now blocked player
            ArrayList<String> requesting_players = from_teleport_requests.get(blocked_player);
            if (requesting_players == null)
                requesting_players = new ArrayList<String>();
            else {
                String remove = requesting_players.get(0);
                while (requesting_players.contains(remove))
                    requesting_players.remove(remove);
            }
            from_teleport_requests.put(blocked_player, requesting_players);
            requesting_players = to_teleport_requests.get(blocked_player);
            if (requesting_players == null)
                requesting_players = new ArrayList<String>();
            else {
                String remove = requesting_players.get(0);
                while (requesting_players.contains(remove))
                    requesting_players.remove(remove);
            }
            to_teleport_requests.put(blocked_player, requesting_players);
        } else if (blocked_player != null && target_player != null)
            player.sendMessage(ChatColor.RED + "Sorry, but " + target_player.getName()
                    + " is a myUltraWarps admin. They have power over everything myUltraWarps-related and you're not allowed to block them.");
        else if (player.getName().toLowerCase().startsWith(blocked_player))
            player.sendMessage(ChatColor.RED + "Now why would you want to block yourself?");
    }

    /** This method is called when <b><tt>sender</tt></b> uses the command <i>/block list (player)</i>. <b><tt>sender</b></tt> must be a Player for this method to be called.
     * This command lists the players that <tt><b>sender</tt></b> has blocked or, if another player's name is given, it lists the players that the designated player has
     * blocked.
     * 
     * @param extra_param
     *            is equal to 0 if the command is used as one word (<i>/blocklist</i>) or 1 if the command is used as two words (<i>/block list</i>).
     * @param sender
     *            is the Player who executed the command.
     * @see {@link #block(CommandSender) block(CommandSender)} and {@link #unblock(CommandSender) unblock(CommandSender)} */
    private void blockList(int extra_param, CommandSender sender, String[] parameters) {
        String target = sender.getName();
        if (parameters.length > extra_param)
            target = autoCompleteName(parameters[extra_param]);
        if (target == null) {
            sender.sendMessage(ChatColor.RED + "I don't know anyone whose name begins with \"" + parameters[extra_param] + "\"!");
            return;
        }
        if (blocked_players.get(target) != null && blocked_players.get(target).size() > 0
                && (target.equals(sender.getName()) || sender.hasPermission("myultrawarps.blocklist.other") || sender.hasPermission("myultrawarps.admin"))) {
            ArrayList<String> players = blocked_players.get(target);
            String list = players.get(0);
            if (players.size() == 2)
                list = list + " and " + players.get(1);
            else if (players.size() > 2)
                for (int i = 1; i < players.size(); i++) {
                    list = list + ", ";
                    if (i == players.size() - 1)
                        list = list + "and ";
                    list = list + players.get(i);
                }
            if (target.equals(sender.getName()))
                sender.sendMessage(COLOR + "You've blocked " + list + ".");
            else
                sender.sendMessage(COLOR + target + " blocked " + list + ".");
        } else if (!target.equals(sender.getName()) && !sender.hasPermission("myultrawarps.blocklist.other") && !sender.hasPermission("myultrawarps.admin")) {
            sender.sendMessage(ChatColor.RED + "Sorry, but you don't have permission to see who other people blocked.");
        } else if (target.equals(sender.getName()))
            sender.sendMessage(COLOR + "You haven't blocked anyone yet.");
        else
            sender.sendMessage(COLOR + target + " hasn't blocked anyone yet.");
    }

    /** This method is called when <b><tt>sender</tt></b> uses the command <i>/create (owner's) [warp] (options)</i>. <b><tt>sender</b></tt> must be a Player for this method to
     * be called. This command creates a new warp where <b><tt>sender</b></tt> is standing.
     * 
     * @param extra_param
     *            is equal to 0 if the command is used as one word (<i>/create</i> or <i>/createwarp</i>) or 1 if the command is used as two words (<i>/create warp</i>).
     * @param sender
     *            is the Player who executed the command.
     * @options "type:[type]" sets the type of warp. [type] can be "open" (listed and unrestricted), "secret" (unlisted and unrestricted), "advertised" (listed and
     *          restricted), or "private" (unlisted and restricted). [type] can be autocompleted; for example, "type:s" is sufficient to specify that a warp should be secret.
     *          By default, the type of the warp is private.
     *          <hr>
     *          "warp:[warp]" sets the warp message (the message that is displayed to players who warp to the warp). "[player]" can be used to specify places in the message in
     *          which the name of the person warping to the warp will be inserted. Color codes may be used anywhere in the warp message. The default warp message will be
     *          specified in the <tt>config.txt</tt>.
     *          <hr>
     *          "nowarp:[no warp]" sets the no warp message (the message that is displayed to players attempt to warp to the warp, but are not allowed). "[player]" can be used
     *          to specify places in the message in which the name of the person warping to the warp will be inserted. Color codes may be used anywhere in the warp message.
     *          The default warp message will be specified in the <tt>config.txt</tt>.
     *          <hr>
     *          "giveto:[player]" sets [player] as the owner of the new warp. [player] can be autocompleted if the specified player has played on the server before. By
     *          default, the owner of the new warp is the creator of the new warp. */
    private void createWarp(int extra_param, CommandSender sender, String[] parameters) {
        Player player = (Player) sender;
        // establish all the default values
        boolean listed = false, restricted = true;
        SettingsSet set = getSettings(player.getName());
        String owner = player.getName(), warp_message =
                set.default_warp.replaceAll("\\[warp\\]", parameters[extra_param].replaceAll("_", " ")).replaceAll("\\[owner\\]", owner), no_warp_message =
                set.default_no_warp.replaceAll("\\[warp\\]", parameters[extra_param].replaceAll("_", " ")).replaceAll("\\[owner\\]", owner);
        String[] listed_users = null;
        boolean player_is_owner = true;
        parsing_warp_message = false;
        parsing_no_warp_message = false;
        // search the parameters for non-default characteristics
        for (int j = extra_param + 1; j < parameters.length; j++) {
            if (parameters[j].toLowerCase().startsWith("type:o")) {
                listed = true;
                restricted = false;
            } else if (parameters[j].toLowerCase().startsWith("type:s")) {
                listed = false;
                restricted = false;
            } else if (parameters[j].toLowerCase().startsWith("type:a")) {
                listed = true;
                restricted = true;
                stopParsingMessages(warp_message, no_warp_message, parameters[extra_param], owner, player_is_owner, sender, "");
            } else if (parameters[j].toLowerCase().startsWith("type:p")) {
                listed = false;
                restricted = true;
                stopParsingMessages(warp_message, no_warp_message, parameters[extra_param], owner, player_is_owner, sender, "");
            } else if (parameters[j].toLowerCase().startsWith("warp:")) {
                warp_message = parameters[j].substring(5);
                stopParsingMessages(warp_message, no_warp_message, parameters[extra_param], owner, player_is_owner, sender, "");
                parsing_warp_message = true;
            } else if (parameters[j].toLowerCase().startsWith("nowarp:")) {
                no_warp_message = parameters[j].substring(7);
                stopParsingMessages(warp_message, no_warp_message, parameters[extra_param], owner, player_is_owner, sender, "");
            } else if (parameters[j].toLowerCase().startsWith("giveto:")) {
                String temp_old_owner = owner;
                owner = autoCompleteName(parameters[j].substring(7));
                player_is_owner = player != null && player.getName().equals(owner);
                // update the warp and no warp messages
                if (warp_message.contains(temp_old_owner))
                    warp_message = warp_message.replaceAll(temp_old_owner, owner);
                if (no_warp_message.contains(temp_old_owner))
                    no_warp_message = no_warp_message.replaceAll(temp_old_owner, owner);
                stopParsingMessages(warp_message, no_warp_message, parameters[extra_param], owner, player_is_owner, sender, "");
            } else if (parameters[j].toLowerCase().startsWith("list:")) {
                stopParsingMessages(warp_message, no_warp_message, parameters[extra_param], owner, player_is_owner, sender, "");
                listed_users = parameters[j].substring(5).split(",");
                if (listed_users.length > 0 && !(listed_users.length == 1 && listed_users[0].equals("")))
                    // retrieve full player names
                    for (int i = 0; i < listed_users.length; i++)
                        listed_users[i] = autoCompleteName(listed_users[i]);
            } else if (parsing_warp_message)
                warp_message = warp_message + " " + parameters[j];
            else if (parsing_no_warp_message)
                no_warp_message = no_warp_message + " " + parameters[j];
        }
        if (listed_users == null)
            listed_users = new String[0];
        // see if the user has reached the maximum number of warps they're allowed to have
        boolean maxed_out = false;
        if (set.max_warps != -1) {
            int number_of_warps = 0;
            for (UltraWarp warp : warps)
                if (warp.owner.equalsIgnoreCase(player.getName()))
                    number_of_warps++;
            if (number_of_warps >= set.max_warps) {
                player.sendMessage(ChatColor.RED + "Sorry, but you're only allowed to create " + (Integer) set.max_warps + " warps and you've already reached your limit.");
                return;
            }
        }
        if ((!maxed_out || player.hasPermission("myultrawarps.admin"))
                && !parameters[extra_param].equalsIgnoreCase("info")
                && !parameters[extra_param].equalsIgnoreCase("all")
                && !parameters[extra_param].equalsIgnoreCase("list")
                && !parameters[extra_param].toLowerCase().endsWith("'s")
                && (player.getName().toLowerCase().startsWith(owner.toLowerCase()) || player.hasPermission("myultrawarps.create.other") || player
                        .hasPermission("myultrawarps.admin"))) {
            // delete the old warp if it exists
            for (int i = 0; i < warps.size(); i++)
                if (warps.get(i).owner.equals(owner) && warps.get(i).name.equals(parameters[extra_param]))
                    warps.remove(i);
            // find out where the new warp needs to be in the list to be
            // properly alphabetized
            int insertion_index = 0;
            for (UltraWarp warp : warps)
                if (warp.name.compareToIgnoreCase(parameters[extra_param]) < 0
                        || (warp.name.compareToIgnoreCase(parameters[extra_param]) == 0 && warp.owner.compareToIgnoreCase(owner) <= 0))
                    insertion_index++;
            // create the warp
            warps.add(insertion_index, new UltraWarp(owner, parameters[extra_param], listed, restricted, warp_message, no_warp_message, listed_users, player.getLocation()));
            if (autosave_warps)
                saveTheWarps(sender, false);
            if (player.getName().toLowerCase().startsWith(owner.toLowerCase()))
                player.sendMessage(COLOR + "You made a warp called \"" + parameters[extra_param] + ".\"");
            else
                player.sendMessage(COLOR + "You made a warp called \"" + parameters[extra_param] + "\" for " + owner + ".");
        } else if (parameters[extra_param].equalsIgnoreCase("info"))
            sender.sendMessage(ChatColor.RED + "Sorry, but you can't name a warp \"info\" because it interferes with the command " + COLOR + "/warp info" + ChatColor.RED
                    + ".");
        else if (parameters[extra_param].equalsIgnoreCase("all"))
            sender.sendMessage(ChatColor.RED + "Sorry, but you can't name a warp \"all\" because it interferes with the command " + COLOR + "/warp all" + ChatColor.RED + ".");
        else if (parameters[extra_param].equalsIgnoreCase("list"))
            sender.sendMessage(ChatColor.RED + "Sorry, but you can't name a warp \"list\" because it interferes with the command " + COLOR + "/warp list" + ChatColor.RED
                    + ".");
        else if (parameters[extra_param].toLowerCase().endsWith("'s"))
            sender.sendMessage(ChatColor.RED
                    + "Sorry, but you can't make a warp with a name ending in \"'s\" because I check for that to see whether you're specifying an owner or just giving a warp name alone and if the warp name has that \"'s\", I get very confused.");
        else if (!player.getName().equalsIgnoreCase(owner) && !(player.hasPermission("myultrawarps.create.other") || player.hasPermission("myultrawarps.admin"))) {
            // check if the player receiving the warp already has that warp
            boolean warp_already_exists = false;
            for (int i = 0; i < warps.size(); i++)
                if (warps.get(i).owner.toLowerCase().startsWith(owner.toLowerCase()) && warps.get(i).name.toLowerCase().startsWith(parameters[extra_param].toLowerCase()))
                    warp_already_exists = true;
            if (!warp_already_exists) {
                // find out where the new warp needs to be in the list to be
                // properly alphabetized
                int insertion_index = 0;
                for (UltraWarp warp : warps)
                    if (warp.name.compareToIgnoreCase(parameters[extra_param]) < 0
                            || (warp.name.compareToIgnoreCase(parameters[extra_param]) == 0 && warp.owner.compareToIgnoreCase(owner) <= 0))
                        insertion_index++;
                // create the warp
                warps.add(insertion_index,
                        new UltraWarp(owner, parameters[extra_param], listed, restricted, warp_message, no_warp_message, listed_users, player.getLocation()));
                player.sendMessage(COLOR + "You made a warp called \"" + parameters[extra_param] + "\" for " + owner + ".");
            } else
                player.sendMessage(ChatColor.RED + owner + " already has a warp called \"" + parameters[extra_param] + "\" and you're not allowed to overwrite it.");
        }
    }

    /** This method is called when <b><tt>sender</tt></b> uses the command <i>/change (owner's) [warp] [options]</i>. This command changes the options of the specified warp.
     * 
     * @param extra_param
     *            is equal to 0 if the command is used as one word (<i>/change</i> or <i>/changewarp</i>) or 1 if the command is used as two words (<i>/change warp</i>).
     * @param sender
     *            is the Player or <tt>console</tt> who executed the command.
     * @options "name:[name]" sets the new name of the warp.
     *          <hr>
     *          "type:[type]" sets the type of warp. [type] can be "open" (listed and unrestricted), "secret" (unlisted and unrestricted), "advertised" (listed and
     *          restricted), or "private" (unlisted and restricted). [type] can be autocompleted; for example, "type:s" is sufficient to specify that a warp should be secret.
     *          By default, the type of the warp is private.
     *          <hr>
     *          "warp:[warp]" sets the warp message (the message that is displayed to players who warp to the warp). "[player]" can be used to specify places in the message in
     *          which the name of the person warping to the warp will be inserted. Color codes may be used anywhere in the warp message. The default warp message will be
     *          specified in the <tt>config.txt</tt>.
     *          <hr>
     *          "nowarp:[no warp]" sets the no warp message (the message that is displayed to players attempt to warp to the warp, but are not allowed). "[player]" can be used
     *          to specify places in the message in which the name of the person warping to the warp will be inserted. Color codes may be used anywhere in the warp message.
     *          The default warp message will be specified in the <tt>config.txt</tt>.
     *          <hr>
     *          "giveto:[player]" sets [player] as the owner of the new warp. [player] can be autocompleted if the specified player has played on the server before. By
     *          default, the owner of the new warp is the creator of the new warp. */
    private void changeWarp(int extra_param, CommandSender sender, String[] parameters) {
        // [changewarp/change warp/modifywarp/modify warp]
        Player player = null;
        if (sender instanceof Player)
            player = (Player) sender;
        UltraWarp warp = UltraWarp.getWarp(extra_param, sender, parameters);
        if (warp != null) {
            // search through the parameters for info changes
            boolean listed = warp.listed, restricted = warp.restricted, old_listed = warp.listed, old_restricted = warp.restricted;
            String warp_message = warp.warp_message, no_warp_message = warp.no_warp_message, old_warp_message = warp.warp_message, old_no_warp_message = warp.no_warp_message, owner =
                    warp.owner, name = warp.name, old_owner = warp.owner, old_name = warp.name, result_message = "";
            String[] listed_users = warp.listed_users, old_listed_users = warp.listed_users;
            boolean player_is_owner = false;
            if (player != null && player.getName().toLowerCase().startsWith(owner.toLowerCase()))
                player_is_owner = true;
            parsing_warp_message = false;
            parsing_no_warp_message = false;
            for (int j = extra_param + 1; j < parameters.length; j++) {
                if (parameters[j].toLowerCase().startsWith("type:o")) {
                    result_message = stopParsingMessages(warp_message, no_warp_message, name, owner, player_is_owner, sender, result_message);
                    listed = true;
                    restricted = false;
                    if (!result_message.equals(""))
                        result_message = result_message + "\n";
                    if (player_is_owner)
                        result_message = result_message + COLOR + "\"" + name + "\" is now an " + ChatColor.WHITE + "open " + COLOR + "warp.";
                    else
                        result_message = result_message + COLOR + owner + "'s \"" + name + "\" is now an " + ChatColor.WHITE + "open " + COLOR + "warp.";
                } else if (parameters[j].toLowerCase().startsWith("type:s")) {
                    result_message = stopParsingMessages(warp_message, no_warp_message, name, owner, player_is_owner, sender, result_message);
                    listed = false;
                    restricted = false;
                    if (!result_message.equals(""))
                        result_message = result_message + "\n";
                    if (player_is_owner)
                        result_message = result_message + COLOR + "\"" + name + "\" is now a " + ChatColor.GRAY + "secret " + COLOR + "warp.";
                    else
                        result_message = result_message + COLOR + owner + "'s \"" + name + "\" is now a " + ChatColor.GRAY + "secret " + COLOR + "warp.";
                } else if (parameters[j].toLowerCase().startsWith("type:a")) {
                    result_message = stopParsingMessages(warp_message, no_warp_message, name, owner, player_is_owner, sender, result_message);
                    listed = true;
                    restricted = true;
                    if (!result_message.equals(""))
                        result_message = result_message + "\n";
                    if (player_is_owner)
                        result_message = result_message + COLOR + "\"" + name + "\" is now an " + ChatColor.AQUA + "advertised " + COLOR + "warp.";
                    else
                        result_message = result_message + COLOR + owner + "'s \"" + name + "\" is now an " + ChatColor.AQUA + "advertised " + COLOR + "warp.";
                } else if (parameters[j].toLowerCase().startsWith("type:p")) {
                    result_message = stopParsingMessages(warp_message, no_warp_message, name, owner, player_is_owner, sender, result_message);
                    listed = false;
                    restricted = true;
                    if (!result_message.equals(""))
                        result_message = result_message + "\n";
                    if (player_is_owner)
                        result_message = result_message + COLOR + "\"" + name + "\" is now a " + ChatColor.DARK_GRAY + "private " + COLOR + "warp.";
                    else
                        result_message = result_message + COLOR + owner + "'s \"" + name + "\" is now a " + ChatColor.DARK_GRAY + "private " + COLOR + "warp.";
                } else if (parameters[j].toLowerCase().startsWith("name:")) {
                    result_message = stopParsingMessages(warp_message, no_warp_message, name, owner, player_is_owner, sender, result_message);
                    String temp_old_name = name;
                    name = parameters[j].substring(5);
                    if (!name.equalsIgnoreCase("info") && !name.equalsIgnoreCase("all") && !name.equalsIgnoreCase("list")) {
                        if (!result_message.equals(""))
                            result_message = result_message + "\n";
                        if (player_is_owner)
                            result_message = result_message + COLOR + "\"" + old_name + "\" has been renamed \"" + name + ".\"";
                        else
                            result_message = result_message + COLOR + owner + "'s \"" + old_name + "\" has been renamed \"" + name + ".\"";
                        // update the warp and no warp messages
                        boolean updated_warp_message = false, updated_no_warp_message = false;
                        String temp_old_message_name = temp_old_name.replaceAll("_", " "), message_name = name.replaceAll("_", " ");
                        if (warp_message.contains(temp_old_message_name)) {
                            warp_message = warp_message.replaceAll(temp_old_message_name, message_name);
                            updated_warp_message = true;
                        }
                        if (no_warp_message.contains(temp_old_message_name)) {
                            no_warp_message = no_warp_message.replaceAll(temp_old_message_name, message_name);
                            updated_no_warp_message = true;
                        }
                        if (!result_message.equals(""))
                            result_message = result_message + "\n";
                        if (updated_warp_message)
                            if (updated_no_warp_message)
                                result_message = result_message + COLOR + "I also updated the warp and no warp messages.";
                            else
                                result_message = result_message + COLOR + "I also updated the warp message.";
                        else if (updated_no_warp_message)
                            result_message = result_message + COLOR + "I also updated the no warp message.";
                    } else {
                        if (!result_message.equals(""))
                            result_message = result_message + "\n";
                        result_message =
                                result_message + ChatColor.RED + "Sorry, but you can't make a warp called \"" + name + "\" because it interferes with the command " + COLOR
                                        + "/warp " + name + ChatColor.RED + ".";
                        name = temp_old_name;
                    }
                } else if (parameters[j].toLowerCase().startsWith("warp:")) {
                    result_message = stopParsingMessages(warp_message, no_warp_message, name, owner, player_is_owner, sender, result_message);
                    warp_message = parameters[j].substring(5);
                    parsing_warp_message = true;
                } else if (parameters[j].toLowerCase().startsWith("nowarp:")) {
                    result_message = stopParsingMessages(warp_message, no_warp_message, name, owner, player_is_owner, sender, result_message);
                    no_warp_message = parameters[j].substring(7);
                    parsing_no_warp_message = true;
                } else if (parameters[j].toLowerCase().startsWith("giveto:")) {
                    result_message = stopParsingMessages(warp_message, no_warp_message, name, owner, player_is_owner, sender, result_message);
                    String temp_old_owner = owner;
                    owner = autoCompleteName(parameters[j].substring(7));
                    if (!result_message.equals(""))
                        result_message = result_message + "\n";
                    if (player != null && player.getName().toLowerCase().startsWith(temp_old_owner.toLowerCase()))
                        result_message = result_message + COLOR + "You gave \"" + name + "\" to " + owner + ".";
                    else
                        result_message = result_message + COLOR + "You gave " + temp_old_owner + "'s \"" + name + "\" to " + owner + ".";
                    // update the warp and no warp messages
                    boolean updated_warp_message = false, updated_no_warp_message = false;
                    if (warp_message.contains(temp_old_owner)) {
                        warp_message = warp_message.replaceAll(temp_old_owner, owner);
                        updated_warp_message = true;
                    }
                    if (no_warp_message.contains(temp_old_owner)) {
                        no_warp_message = no_warp_message.replaceAll(temp_old_owner, owner);
                        updated_no_warp_message = true;
                    }
                    if (!result_message.equals(""))
                        result_message = result_message + "\n";
                    if (updated_warp_message)
                        if (updated_no_warp_message)
                            result_message = result_message + COLOR + "I also updated the warp and no warp messages.";
                        else
                            result_message = result_message + COLOR + "I also updated the warp message.";
                    else if (updated_no_warp_message)
                        result_message = result_message + COLOR + "I also updated the no warp message.";
                } else if (parameters[j].toLowerCase().startsWith("list:")) {
                    result_message = stopParsingMessages(warp_message, no_warp_message, name, owner, player_is_owner, sender, result_message);
                    String[] listed_users_list = parameters[j].substring(5).split(",");
                    if (listed_users_list.length > 0 && !(listed_users_list.length == 1 && listed_users_list[0].equals(""))) {
                        // retrieve full player names
                        for (int i = 0; i < listed_users_list.length; i++)
                            listed_users_list[i] = autoCompleteName(listed_users_list[i]);
                        // state the change
                        if (!result_message.equals(""))
                            result_message = result_message + "\n";
                        if (restricted) {
                            if (player_is_owner)
                                if (listed_users_list.length == 1)
                                    result_message = result_message + COLOR + listed_users_list[0] + " is now allowed to use \"" + name + ".\"";
                                else if (listed_users_list.length == 2)
                                    result_message =
                                            result_message + COLOR + listed_users_list[0] + " and " + listed_users_list[1] + " are now allowed to use \"" + name + ".\"";
                                else {
                                    String message = COLOR + "";
                                    for (int i = 0; i < listed_users_list.length - 1; i++)
                                        message = message + listed_users_list[i] + ", ";
                                    result_message =
                                            result_message + message + " and " + listed_users_list[listed_users_list.length - 1] + " are now allowed to use \"" + name + ".\"";
                                }
                            else if (listed_users_list.length == 1)
                                result_message = result_message + COLOR + listed_users_list[0] + " is now allowed to use " + owner + "'s \"" + name + ".\"";
                            else if (listed_users_list.length == 2)
                                result_message =
                                        result_message + COLOR + listed_users_list[0] + " and " + listed_users_list[1] + " are now allowed to use " + owner + "'s \"" + name
                                                + ".\"";
                            else {
                                String message = COLOR + "";
                                for (int i = 0; i < listed_users_list.length - 1; i++)
                                    message = message + listed_users_list[i] + ", ";
                                result_message =
                                        result_message + message + " and " + listed_users_list[listed_users_list.length - 1] + " are now allowed to use " + owner + "'s \""
                                                + name + ".\"";
                            }
                        } else {
                            if (player_is_owner)
                                if (listed_users_list.length == 1)
                                    result_message = result_message + COLOR + listed_users_list[0] + " is no longer allowed to use \"" + name + ".\"";
                                else if (listed_users_list.length == 2)
                                    result_message =
                                            result_message + COLOR + listed_users_list[0] + " and " + listed_users_list[1] + " are no longer allowed to use \"" + name + ".\"";
                                else {
                                    String message = COLOR + "";
                                    for (int i = 0; i < listed_users_list.length - 1; i++)
                                        message = message + listed_users_list[i] + ", ";
                                    result_message =
                                            result_message + message + " and " + listed_users_list[listed_users_list.length - 1] + " are no longer allowed to use \"" + name
                                                    + ".\"";
                                }
                            else if (listed_users_list.length == 1)
                                result_message = result_message + COLOR + listed_users_list[0] + " is no longer allowed to use " + owner + "'s \"" + name + ".\"";
                            else if (listed_users_list.length == 2)
                                result_message =
                                        result_message + COLOR + listed_users_list[0] + " and " + listed_users_list[1] + " are no longer allowed to use " + owner + "'s \""
                                                + name + ".\"";
                            else {
                                String message = COLOR + "";
                                for (int i = 0; i < listed_users_list.length - 1; i++)
                                    message = message + listed_users_list[i] + ", ";
                                result_message =
                                        result_message + message + " and " + listed_users_list[listed_users_list.length - 1] + " are no longer allowed to use " + owner
                                                + "'s \"" + name + ".\"";
                            }
                        }
                        String[] temp_listed_users = listed_users;
                        listed_users = new String[listed_users_list.length + temp_listed_users.length];
                        for (int i = 0; i < listed_users.length; i++) {
                            if (i < temp_listed_users.length)
                                listed_users[i] = temp_listed_users[i];
                            else
                                listed_users[i] = listed_users_list[i - temp_listed_users.length];
                        }
                    }
                } else if (parameters[j].toLowerCase().startsWith("unlist:")) {
                    result_message = stopParsingMessages(warp_message, no_warp_message, name, owner, player_is_owner, sender, result_message);
                    String[] unlisted_users_list = parameters[j].substring(7).split(",");
                    if (unlisted_users_list.length > 0 && !(unlisted_users_list.length == 1 && unlisted_users_list[0].equals(""))) {
                        // retrieve full player names
                        for (int i = 0; i < unlisted_users_list.length; i++)
                            unlisted_users_list[i] = autoCompleteName(unlisted_users_list[i]);
                        // state the change
                        if (!result_message.equals(""))
                            result_message = result_message + "\n";
                        if (restricted) {
                            if (player_is_owner)
                                if (unlisted_users_list.length == 1)
                                    result_message = result_message + COLOR + unlisted_users_list[0] + " is no longer allowed to use \"" + name + ".\"";
                                else if (unlisted_users_list.length == 2)
                                    result_message =
                                            result_message + COLOR + unlisted_users_list[0] + " and " + unlisted_users_list[1] + " are no longer allowed to use \"" + name
                                                    + ".\"";
                                else {
                                    String message = COLOR + "";
                                    for (int i = 0; i < unlisted_users_list.length - 1; i++)
                                        message = message + unlisted_users_list[i] + ", ";
                                    result_message =
                                            result_message + message + " and " + unlisted_users_list[unlisted_users_list.length - 1] + " are no longer allowed to use \""
                                                    + name + ".\"";
                                }
                            else if (unlisted_users_list.length == 1)
                                result_message = result_message + COLOR + unlisted_users_list[0] + " is no longer allowed to use " + owner + "'s \"" + name + ".\"";
                            else if (unlisted_users_list.length == 2)
                                result_message =
                                        result_message + COLOR + unlisted_users_list[0] + " and " + unlisted_users_list[1] + " are no longer allowed to use " + owner
                                                + "'s \"" + name + ".\"";
                            else {
                                String message = COLOR + "";
                                for (int i = 0; i < unlisted_users_list.length - 1; i++)
                                    message = message + unlisted_users_list[i] + ", ";
                                result_message =
                                        result_message + message + " and " + unlisted_users_list[unlisted_users_list.length - 1] + " are no longer allowed to use " + owner
                                                + "'s \"" + name + ".\"";
                            }
                        } else {
                            if (player_is_owner)
                                if (unlisted_users_list.length == 1)
                                    result_message = result_message + COLOR + unlisted_users_list[0] + " is now allowed to use \"" + name + ".\"";
                                else if (unlisted_users_list.length == 2)
                                    result_message =
                                            result_message + COLOR + unlisted_users_list[0] + " and " + unlisted_users_list[1] + " are now allowed to use \"" + name + ".\"";
                                else {
                                    String message = COLOR + "";
                                    for (int i = 0; i < unlisted_users_list.length - 1; i++)
                                        message = message + unlisted_users_list[i] + ", ";
                                    result_message =
                                            result_message + message + " and " + unlisted_users_list[unlisted_users_list.length - 1] + " are now allowed to use \"" + name
                                                    + ".\"";
                                }
                            else if (unlisted_users_list.length == 1)
                                result_message = result_message + COLOR + unlisted_users_list[0] + " is now allowed to use " + owner + "'s \"" + name + ".\"";
                            else if (unlisted_users_list.length == 2)
                                result_message =
                                        result_message + COLOR + unlisted_users_list[0] + " and " + unlisted_users_list[1] + " are now allowed to use " + owner + "'s \""
                                                + name + ".\"";
                            else {
                                String message = COLOR + "";
                                for (int i = 0; i < unlisted_users_list.length - 1; i++)
                                    message = message + unlisted_users_list[i] + ", ";
                                result_message =
                                        result_message + message + " and " + unlisted_users_list[unlisted_users_list.length - 1] + " are now allowed to use " + owner
                                                + "'s \"" + name + ".\"";
                            }
                        }
                    }
                    // convert to ArrayList
                    ArrayList<String> listed_users_list = new ArrayList<String>();
                    for (String user : listed_users)
                        listed_users_list.add(user);
                    // remove unlisted users
                    for (String user : unlisted_users_list)
                        while (listed_users_list.contains(user))
                            listed_users_list.remove(user);
                    // convert back to array
                    listed_users = new String[listed_users_list.size()];
                    for (int i = 0; i < listed_users_list.size(); i++) {
                        if (listed_users_list.get(i) != null && !listed_users_list.get(i).equals(""))
                            listed_users[i] = listed_users_list.get(i);
                    }
                } else if (parsing_warp_message)
                    warp_message = warp_message + " " + parameters[j];
                else if (parsing_no_warp_message)
                    no_warp_message = no_warp_message + " " + parameters[j];
            }
            result_message = stopParsingMessages(warp_message, no_warp_message, name, owner, player_is_owner, sender, result_message);
            if (!name.equalsIgnoreCase("info")
                    && !name.equalsIgnoreCase("list")
                    && !name.equals("all")
                    && (player == null || owner.equalsIgnoreCase(player.getName()) || player.hasPermission("myultrawarps.change.other") || player
                            .hasPermission("myultrawarps.admin"))) {
                if (name.equals(old_name) && owner.equals(old_owner) && listed == old_listed && restricted == old_restricted && warp_message.equals(old_warp_message)
                        && no_warp_message.equals(old_no_warp_message) && listed_users.equals(old_listed_users))
                    sender.sendMessage(ChatColor.RED + "You didn't change anything!");
                else {
                    UltraWarp new_warp = new UltraWarp(owner, name, listed, restricted, warp_message, no_warp_message, listed_users, warp.location);
                    // change the warp's info
                    warps.remove(UWindex);
                    // find out where the new warp needs to be in the list to be
                    // properly alphabetized
                    int insertion_index = 0;
                    for (UltraWarp my_warp : warps)
                        if (my_warp.name.compareToIgnoreCase(name) < 0 || (my_warp.name.compareToIgnoreCase(name) == 0 && my_warp.owner.compareToIgnoreCase(owner) <= 0))
                            insertion_index++;
                    warps.add(insertion_index, new_warp);
                    sender.sendMessage(result_message);
                    if (autosave_warps)
                        saveTheWarps(sender, false);
                    if (!name.equals(old_name) || !owner.equals(old_owner)) {
                        // change the info for any switches linked to that warp
                        int number_of_affected_switches = 0;
                        for (int i = 0; i < switches.size(); i++)
                            if (switches.get(i).warp_name.equals(old_name) && switches.get(i).warp_owner.equals(old_owner)) {
                                number_of_affected_switches++;
                                UltraSwitch new_switch =
                                        new UltraSwitch(name, owner, switches.get(i).block, switches.get(i).cooldown_time, switches.get(i).max_uses,
                                                switches.get(i).global_cooldown, switches.get(i).cost, switches.get(i).exempted_players);
                                switches.remove(i);
                                // find out where the new switch needs to be in
                                // the list to be properly alphabetized
                                insertion_index = 0;
                                for (UltraSwitch my_switch : switches)
                                    if (my_switch.warp_name.compareToIgnoreCase(warp.name) < 0
                                            || (my_switch.warp_name.compareToIgnoreCase(warp.name) == 0 && my_switch.warp_owner.compareToIgnoreCase(warp.owner) <= 0))
                                        insertion_index++;
                                switches.add(insertion_index, new_switch);
                            }
                        if (number_of_affected_switches == 1)
                            sender.sendMessage(COLOR + "The switch that was linked to \"" + old_name + "\" has also been updated.");
                        else if (number_of_affected_switches > 1)
                            sender.sendMessage(COLOR + "The " + number_of_affected_switches + " switches that were linked to \"" + old_name + "\" have also been updated.");
                    }
                }
            } else if (name.equalsIgnoreCase("info"))
                sender.sendMessage(ChatColor.RED + "Sorry, but you can't name a warp \"info\" because it interferes with the command " + COLOR + "/warp info" + ChatColor.RED
                        + ".");
            else if (name.equalsIgnoreCase("all"))
                sender.sendMessage(ChatColor.RED + "Sorry, but you can't name a warp \"all\" because it interferes with the command " + COLOR + "/warp all" + ChatColor.RED
                        + ".");
            else if (name.equalsIgnoreCase("list"))
                sender.sendMessage(ChatColor.RED + "Sorry, but you can't name a warp \"list\" because it interferes with the command " + COLOR + "/warp list" + ChatColor.RED
                        + ".");
            else {
                // check if the player receiving the warp already has that
                // warp
                boolean warp_already_exists = false;
                for (int i = 0; i < warps.size(); i++)
                    if (warps.get(i).owner.toLowerCase().startsWith(owner.toLowerCase()) && warps.get(i).name.toLowerCase().startsWith(parameters[extra_param].toLowerCase()))
                        warp_already_exists = true;
                if (!warp_already_exists || !(sender instanceof Player) || sender.hasPermission("myultrawarps.change.other") || sender.hasPermission("myultrawarps.admin")) {
                    warps.remove(UWindex);
                    // find out where the new warp needs to be in the list to be
                    // properly alphabetized
                    int insertion_index = 0;
                    for (UltraWarp my_warp : warps)
                        if (my_warp.name.compareToIgnoreCase(name) < 0 || (my_warp.name.compareToIgnoreCase(name) == 0 && my_warp.owner.compareToIgnoreCase(owner) < 0))
                            insertion_index++;
                    // create the changed warp
                    warps.add(insertion_index, new UltraWarp(owner, name, listed, restricted, warp_message, no_warp_message, listed_users, warp.location));
                    sender.sendMessage(result_message);
                    if (autosave_warps)
                        saveTheWarps(sender, false);
                } else
                    sender.sendMessage(ChatColor.RED + "You're not allowed to modify " + owner + "'s \"" + name + ".\"");
            }
        } else if (player != null && player.getName().equals(UWowner))
            player.sendMessage(ChatColor.RED + "I couldn't find \"" + UWname + ".\"");
        else if (UWowner != null)
            sender.sendMessage(ChatColor.RED + "I couldn't find \"" + UWname + "\" in " + UWowner + "'s warps.");
        else
            sender.sendMessage(ChatColor.RED + "I couldn't find \"" + UWname + ".\"");
    }

    /** This method is called when <b><tt>sender</tt></b> uses the command <i>/default warp (for:[target]) [message]</i> or <i>/default no warp (for:[target]) [message]</i>.
     * This command changes the configured default warp message for [target]. [target] can be a player, a permissions group if [target] is surrounded by brackets ("[]"), or
     * for the entire server if [target] is "[server]".
     * 
     * @param extra_param
     *            is equal to the number of parameters that do not specify either the target of the default message or the message itself (e.g. 2 for <i>/default warp
     *            message</i>, 0 for <i>/defaultwarp</i>, or 1 for <i>/default warp</i>).
     * @param sender
     *            is the Player or <tt>console</tt> who executed the command.
     * @see {@link #changeMaxWarps(int, CommandSender) changeMaxWarps(int, CommandSender)} */
    private void changeDefaultMessage(int extra_param, CommandSender sender, String[] parameters) {
        Player player = null;
        if (sender instanceof Player)
            player = (Player) sender;
        Player online_target_player = null;
        // determine if the warp or no warp message is being changed
        boolean change_warp_message = true;
        if (parameters[0].toLowerCase().startsWith("no"))
            change_warp_message = false;
        // get the change's target
        String config_target = "server";
        if (player != null)
            config_target = player.getName();
        boolean target_is_group = false;
        if (extra_param < parameters.length && parameters[extra_param].equalsIgnoreCase("for")) {
            config_target = parameters[extra_param + 1];
            if (config_target.toLowerCase().startsWith("group:")) {
                config_target = config_target.substring(6);
                target_is_group = true;
            } else if (config_target.equalsIgnoreCase("global"))
                config_target = "server";
            else if (!config_target.equals("server")) {
                for (Player my_player : server.getOnlinePlayers())
                    if (my_player.getName().toLowerCase().startsWith(config_target.toLowerCase())) {
                        config_target = my_player.getName();
                        online_target_player = my_player;
                    }
                if (online_target_player == null)
                    for (OfflinePlayer my_player : server.getOfflinePlayers())
                        if (my_player.getName().toLowerCase().startsWith(config_target.toLowerCase()))
                            config_target = my_player.getName();
            }
            extra_param = extra_param + 2;
        }
        // read the new message
        String new_message = "";
        if (parameters[extra_param - 1].toLowerCase().startsWith("warp:"))
            new_message = parameters[extra_param - 1].substring(5);
        else if (parameters[extra_param - 1].toLowerCase().startsWith("nowarp:"))
            new_message = parameters[extra_param - 1].substring(7);
        for (int i = extra_param; i < parameters.length; i++) {
            if (!new_message.equals(""))
                new_message += " ";
            new_message += parameters[i];
        }
        if ((player != null && config_target.equals(player.getName())) || player == null || player.hasPermission("myultrawarps.admin")) {
            if (config_target.equals("server")) {
                if (change_warp_message) {
                    SettingsSet set = settings.get("[server]");
                    set = set.setDefaultWarpMessage(new_message);
                    settings.put("[server]", set);
                    if (new_message.endsWith(".") || new_message.endsWith("!") || new_message.endsWith("?") || new_message.equals(""))
                        sender.sendMessage(COLOR + "You changed the default warp message to \"" + ChatColor.WHITE + colorCode(new_message) + COLOR + "\"");
                    else
                        sender.sendMessage(COLOR + "You changed the default warp message to \"" + ChatColor.WHITE + colorCode(new_message) + COLOR + ".\"");
                } else {
                    SettingsSet set = settings.get("[server]");
                    set = set.setDefaultNoWarpMessage(new_message);
                    settings.put("[server]", set);
                    if (new_message.endsWith(".") || new_message.endsWith("!") || new_message.endsWith("?") || new_message.equals(""))
                        sender.sendMessage(COLOR + "You changed the default no warp message to \"" + ChatColor.WHITE + colorCode(new_message) + COLOR + "\"");
                    else
                        sender.sendMessage(COLOR + "You changed the default no warp message to \"" + ChatColor.WHITE + colorCode(new_message) + COLOR + ".\"");
                }
            } else if (target_is_group) {
                // simultaneously autocomplete the group name and see if it even exists at all
                boolean group_exists = false;
                for (String group : permissions.getGroups())
                    if (group.toLowerCase().startsWith(config_target.toLowerCase())) {
                        config_target = group;
                        group_exists = true;
                    }
                if (group_exists) {
                    SettingsSet set = settings.get("[" + config_target + "]");
                    if (set == null)
                        set = settings.get("[server]");
                    if (change_warp_message) {
                        set = set.setDefaultWarpMessage(new_message);
                        if (new_message.endsWith(".") || new_message.endsWith("!") || new_message.endsWith("?") || new_message.equals(""))
                            sender.sendMessage(COLOR + "You changed the default warp message for the " + config_target + " group to \"" + ChatColor.WHITE
                                    + colorCode(new_message) + COLOR + "\"");
                        else
                            sender.sendMessage(COLOR + "You changed the default warp message for the " + config_target + " group to \"" + ChatColor.WHITE
                                    + colorCode(new_message) + COLOR + ".\"");
                    } else {
                        set = set.setDefaultNoWarpMessage(new_message);
                        if (new_message.endsWith(".") || new_message.endsWith("!") || new_message.endsWith("?") || new_message.equals(""))
                            sender.sendMessage(COLOR + "You changed the default no warp message for the " + config_target + " group to \"" + ChatColor.WHITE
                                    + colorCode(new_message) + COLOR + "\"");
                        else
                            sender.sendMessage(COLOR + "You changed the default no warp message for the " + config_target + "mgroup to \"" + ChatColor.WHITE
                                    + colorCode(new_message) + COLOR + ".\"");
                    }
                    settings.put("[" + config_target + "]", set);
                } else
                    sender.sendMessage(ChatColor.RED + "Sorry, but I couldn't find a group called \"" + config_target + ".\"");
            } else {
                // use the Player itself if they're online; if not, use their name
                Player target = server.getPlayerExact(config_target);
                SettingsSet set;
                if (target == null)
                    set = getSettings(config_target);
                else
                    set = getSettings(target.getName());
                if (change_warp_message) {
                    set = set.setDefaultWarpMessage(new_message);
                    if (player != null && player.getName().equals(config_target))
                        if (new_message.endsWith(".") || new_message.endsWith("!") || new_message.endsWith("?") || new_message.equals(""))
                            sender.sendMessage(COLOR + "You changed your default warp message to \"" + ChatColor.WHITE + colorCode(new_message) + COLOR + "\"");
                        else
                            sender.sendMessage(COLOR + "You changed your default warp message to \"" + ChatColor.WHITE + colorCode(new_message) + COLOR + ".\"");
                    else if (new_message.endsWith(".") || new_message.endsWith("!") || new_message.endsWith("?") || new_message.equals(""))
                        sender.sendMessage(COLOR + "You changed " + config_target + "'s default warp message to \"" + ChatColor.WHITE + colorCode(new_message) + COLOR + "\"");
                    else
                        sender.sendMessage(COLOR + "You changed " + config_target + "'s default warp message to \"" + ChatColor.WHITE + colorCode(new_message) + COLOR + ".\"");
                } else {
                    set = set.setDefaultNoWarpMessage(new_message);
                    if (player != null && player.getName().equals(config_target))
                        if (new_message.endsWith(".") || new_message.endsWith("!") || new_message.endsWith("?") || new_message.equals(""))
                            sender.sendMessage(COLOR + "You changed your default no warp message to \"" + ChatColor.WHITE + colorCode(new_message) + COLOR + "\"");
                        else
                            sender.sendMessage(COLOR + "You changed your default no warp message to \"" + ChatColor.WHITE + colorCode(new_message) + COLOR + ".\"");
                    else if (new_message.endsWith(".") || new_message.endsWith("!") || new_message.endsWith("?") || new_message.equals(""))
                        sender.sendMessage(COLOR + "You changed " + config_target + "'s default no warp message to \"" + ChatColor.WHITE + colorCode(new_message) + COLOR
                                + "\"");
                    else
                        sender.sendMessage(COLOR + "You changed " + config_target + "'s default no warp message to \"" + ChatColor.WHITE + colorCode(new_message) + COLOR
                                + ".\"");
                }
                settings.put(config_target, set);
            }
            if (autosave_config)
                saveTheConfig(sender, false);
        } else
            player.sendMessage(ChatColor.RED + "Sorry, but you're only allowed to change your own default messages.");
    }

    /** This method is called when <b><tt>sender</tt></b> uses the command <i>/max warps (for:[target]) [max warps]</i>. This command changes the max warps for [target].
     * [target] can be a player, a permissions group if [target] is surrounded by brackets ("[]"), or for the entire server if [target] is "[server]".
     * 
     * @param extra_param
     *            is equal to 0 if the command is used as one word (<i>/maxwarps</i> or <i>/max warps</i>) or 1 if the command is used as two words (<i>/change warp</i>).
     * @param sender
     *            is the Player or <tt>console</tt> who executed the command.
     * @see {@link #changeDefaultMessage(int, CommandSender) changeDefaultMessage(int, CommandSender)} */
    private void changeMaxWarps(int extra_param, CommandSender sender, String[] parameters) {
        Player player = null;
        if (sender instanceof Player)
            player = (Player) sender;
        // get the change's target
        String config_target = "server";
        if (player != null)
            config_target = player.getName();
        boolean target_is_group = false;
        Player online_target_player = null;
        if (parameters[extra_param].equalsIgnoreCase("for")) {
            config_target = parameters[extra_param + 1];
            if (config_target.toLowerCase().startsWith("group:")) {
                config_target = config_target.substring(6);
                target_is_group = true;
            } else if (config_target.equalsIgnoreCase("global"))
                config_target = "server";
            else if (!config_target.equals("server")) {
                for (Player my_player : server.getOnlinePlayers())
                    if (my_player.getName().toLowerCase().startsWith(config_target.toLowerCase())) {
                        config_target = my_player.getName();
                        online_target_player = my_player;
                    }
                if (online_target_player == null)
                    for (OfflinePlayer my_player : server.getOfflinePlayers())
                        if (my_player.getName().toLowerCase().startsWith(config_target.toLowerCase()))
                            config_target = my_player.getName();
            }
            extra_param = extra_param + 2;
        }
        // read the new max warps setting
        int new_max_warps = -2;
        if (parameters.length > extra_param) {
            try {
                new_max_warps = Integer.parseInt(parameters[extra_param]);
            } catch (NumberFormatException exception) {
                if (parameters[extra_param].equalsIgnoreCase("infinite") || parameters[extra_param].equalsIgnoreCase("infinity"))
                    new_max_warps = -1;
                else
                    sender.sendMessage(ChatColor.RED + "I don't know what \"" + parameters[extra_param]
                            + "\" means, but I know it's not the word \"infinite\" or the word \"infinity\" or an integer.");
            }
        } else
            sender.sendMessage(ChatColor.RED + "You forgot to tell me what you want me to change the max warps to!");
        if (new_max_warps != -2) {
            if (config_target.equals("server")) {
                SettingsSet set = settings.get("[server]");
                set = set.setMaxWarps(new_max_warps);
                settings.put("[server]", set);
                if (new_max_warps != -1)
                    sender.sendMessage(COLOR + "You changed the default maximum number of warps to " + new_max_warps + ".");
                else
                    sender.sendMessage(COLOR + "Everyone can now make as many warps as they want.");
            } else if (target_is_group) {
                boolean group_exists = false;
                for (String group : permissions.getGroups())
                    if (group.toLowerCase().startsWith(config_target.toLowerCase())) {
                        config_target = group;
                        group_exists = true;
                    }
                if (group_exists) {
                    SettingsSet set = settings.get("[" + config_target + "]");
                    set = set.setMaxWarps(new_max_warps);
                    if (new_max_warps != -1)
                        sender.sendMessage(COLOR + "You changed the default maximum number of warps for the " + config_target + " group to " + new_max_warps + ".");
                    else
                        sender.sendMessage(COLOR + "Everyone in the " + config_target + " group can now make as many warps as they want.");
                    settings.put("[" + config_target + "]", set);
                } else
                    sender.sendMessage(ChatColor.RED + "Sorry, but I couldn't find a group called \"" + config_target + ".\"");
            } else {
                SettingsSet set = getSettings(config_target);
                set = set.setMaxWarps(new_max_warps);
                if (player != null && player.getName().equals(config_target))
                    if (new_max_warps != -1)
                        sender.sendMessage(COLOR + "You can now make a maximum of " + new_max_warps
                                + " warps\n...but, uh...you're a myUltraWarps admin, so you can still make as many warps as you want....");
                    else
                        sender.sendMessage(COLOR
                                + "You can now make as many warps as you want\n...but, uh...you're a myUltraWarps admin, so you could already make as many warps as you want....");
                else if (new_max_warps != -1) {
                    sender.sendMessage(COLOR + config_target + " can now make a maximum of " + new_max_warps + " warps.");
                    if ((online_target_player != null && online_target_player.hasPermission("myultrawarps.admin"))
                            || (permissions != null && permissions.has((World) null, config_target, "myultrawraps.admin")))
                        sender.sendMessage(COLOR + "...but, uh..." + config_target + " is a myUltraWarps admin, so they can still make as many warps as they want....");
                } else {
                    sender.sendMessage(COLOR + config_target + " can now make a maximum of " + new_max_warps + " warps.");
                    if ((online_target_player != null && online_target_player.hasPermission("myultrawarps.admin"))
                            || (permissions != null && permissions.has((World) null, config_target, "myultrawraps.admin")))
                        sender.sendMessage(COLOR + "...but, uh..." + config_target + " is a myUltraWarps admin, so they could already make as many warps as they want....");
                }
                settings.put(config_target, set);
            }
            if (autosave_config)
                saveTheConfig(sender, false);
        }
    }

    /** This method is called when <b><tt>sender</tt></b> uses the command <i>/mUW update</i> and every time myUltraWarps is enabled if the myUltraWarps updater is configured
     * to auto-update in the <tt>config.txt</tt>. This method checks to see if any new versions of myUltraWarps are available on BukkitDev and if there are, download the
     * newest version into the myUltraWarps plugin data folder.
     * 
     * @param sender
     *            is the Player or <tt>console</tt> who executed the command. */
    @SuppressWarnings("resource")
    private void checkForUpdates(CommandSender sender) {
        // check for updates
        URL url = null;
        try {
            url = new URL("http://dev.bukkit.org/server-mods/myultrawarps-v0/files.rss/");
        } catch (MalformedURLException exception) {
            processException("Nooo! Bad U.R.L.! Bad U.R.L.! The updater screwed up!", exception);
        }
        if (url != null) {
            String new_version_name = null, new_version_link = null;
            try {
                // Set header values intial to the empty string
                String title = "";
                String link = "";
                // First create a new XMLInputFactory
                XMLInputFactory inputFactory = XMLInputFactory.newInstance();
                // Setup a new eventReader
                InputStream in = null;
                try {
                    in = url.openStream();
                } catch (IOException exception) {
                    sender.sendMessage(ChatColor.DARK_RED + "The myUltraWarps updater can't connect to BukkitDev!");
                    return;
                }
                XMLEventReader eventReader = inputFactory.createXMLEventReader(in);
                // Read the XML document
                while (eventReader.hasNext()) {
                    XMLEvent event = eventReader.nextEvent();
                    if (event.isStartElement()) {
                        if (event.asStartElement().getName().getLocalPart().equals("title")) {
                            event = eventReader.nextEvent();
                            title = event.asCharacters().getData();
                            continue;
                        }
                        if (event.asStartElement().getName().getLocalPart().equals("link")) {
                            event = eventReader.nextEvent();
                            link = event.asCharacters().getData();
                            continue;
                        }
                    } else if (event.isEndElement()) {
                        if (event.asEndElement().getName().getLocalPart().equals("item")) {
                            new_version_name = title;
                            new_version_link = link;
                            // All done, we don't need to know about older
                            // files.
                            break;
                        }
                    }
                }
            } catch (XMLStreamException exception) {
                processException("Gah! XMLStreamExceptionThing! Come quick! Tell REALDrummer!", exception);
                return;
            }
            boolean new_version_is_out = false;
            String version = getDescription().getVersion(), newest_online_version = "";
            if (new_version_name == null) {
                tellOps(ChatColor.DARK_RED + "Awww. Something went wrong getting the name of the newest version of myUltraWarps.", true);
                return;
            }
            if (new_version_name.split("v").length == 2) {
                newest_online_version = new_version_name.split("v")[new_version_name.split("v").length - 1].split(" ")[0];
                // get the newest file's version number
                if (!version.contains("-DEV") && !version.contains("-PRE") && !version.equalsIgnoreCase(newest_online_version))
                    try {
                        if (Double.parseDouble(version) < Double.parseDouble(newest_online_version))
                            new_version_is_out = true;
                    } catch (NumberFormatException exception) {
                        //
                    }
            } else
                sender.sendMessage(ChatColor.RED
                        + "Oh, no! REALDrummer forgot to put the version number in the title of the plugin on BukkitDev! The updater won't work! Quick! Tell him he messed up and he needs to fix it right away!");
            if (new_version_is_out) {
                String fileLink = null;
                try {
                    // Open a connection to the page
                    BufferedReader reader = new BufferedReader(new InputStreamReader(new URL(new_version_link).openConnection().getInputStream()));
                    String line;
                    while ((line = reader.readLine()) != null)
                        // Search for the download link
                        if (line.contains("<li class=\"user-action user-action-download\">"))
                            // Get the raw link
                            fileLink = line.split("<a href=\"")[1].split("\">Download</a>")[0];
                    reader.close();
                    reader = null;
                } catch (Exception exception) {
                    processException("Uh-oh! The myUltraWarps updater couldn't contact bukkitdev.org!", exception);
                    return;
                }
                if (fileLink != null) {
                    if (!new File(this.getDataFolder(), "myUltraWarps.jar").exists()) {
                        BufferedInputStream in = null;
                        FileOutputStream fout = null;
                        try {
                            // Download the file
                            url = new URL(fileLink);
                            in = new BufferedInputStream(url.openStream());
                            fout = new FileOutputStream(this.getDataFolder().getAbsolutePath() + "/myUltraWarps.jar");
                            byte[] data = new byte[1024];
                            int count;
                            while ((count = in.read(data, 0, 1024)) != -1)
                                fout.write(data, 0, count);
                            if (!(sender instanceof Player))
                                sender.sendMessage(COLOR + "" + ChatColor.UNDERLINE + "'DING!' Your myUltraWarps v" + newest_online_version
                                        + " is ready and it smells AWESOME!! I downloaded it to your myUltraWarps folder! Go get it!");
                            for (Player player : server.getOnlinePlayers())
                                if (player.hasPermission("myultrawarps.admin") && (!(sender instanceof Player) || !sender.getName().equals(player.getName())))
                                    player.sendMessage(COLOR + "" + ChatColor.UNDERLINE + "'DING!' Your myUltraWarps v" + newest_online_version
                                            + " is ready and it smells AWESOME!! I downloaded it to your myUltraWarps folder! Go get it!");
                        } catch (Exception exception) {
                            processException("Shoot. myUltraWarps v" + newest_online_version
                                    + " is out, but something messed up the download. You're gonna have to go to BukkitDev and get it yourself. Sorry.", exception);
                        } finally {
                            try {
                                if (in != null)
                                    in.close();
                                if (fout != null)
                                    fout.close();
                            } catch (Exception ex) {
                                //
                            }
                        }
                    } else
                        sender.sendMessage(ChatColor.RED
                                + "O_O Why is the newest version of myUltraWarps still sitting in your plugin folder?! Hurry up and put it on your server!");
                }
            } else
                sender.sendMessage(COLOR + "Sorry, but no new versions of myUltraWarps are out yet.");
        }
    }

    /** This method is called when <b><tt>sender</tt></b> uses the command <i>/death (amount)</i>. <b><tt>Sender</b></tt> must be a Player for this method to be called. This
     * command teleports players back through their death hostory. Every time someone dies, that location is saved in the player's death history; <i>/death</i> works like the
     * back button on an Internet browser, teleporting players backward through their death history.
     * 
     * @param sender
     *            is the Player who executed the command.
     * @see {@link #deathForward(CommandSender) deathForward(CommandSender)} */
    private void death(CommandSender sender, String[] parameters) {
        Player player = (Player) sender;
        int amount = 1;
        if (parameters.length > 0)
            try {
                amount = Integer.parseInt(parameters[0]);
                if (amount == 0) {
                    sender.sendMessage(COLOR + "Well, here you are. You went back 0 deaths through your history.");
                    return;
                } else if (amount < 0) {
                    sender.sendMessage(ChatColor.RED + "Sorry, but I can't see into the future. At least...not that far ahead.");
                    return;
                }
            } catch (NumberFormatException exception) {
                sender.sendMessage(ChatColor.RED + "Since when is \"" + parameters[0] + "\" an integer?");
                return;
            }
        ArrayList<Location> death_history = death_histories.get(player.getName());
        if (death_history == null || death_history.size() == 0) {
            sender.sendMessage(ChatColor.RED + "You haven't died yet!");
            return;
        }
        Integer last_warp_to_death_index = last_warp_to_death_indexes.get(player.getName());
        Location last_death = null;
        if (last_warp_to_death_index == null)
            last_warp_to_death_index = death_history.size() - 1;
        if (last_warp_to_death_index + 1 >= amount)
            last_death = death_history.get(last_warp_to_death_index + 1 - amount);
        else {
            if (last_warp_to_death_index > 1)
                sender.sendMessage(ChatColor.RED + "You can only go back " + last_warp_to_death_index + " more deaths.");
            else if (last_warp_to_death_index == 1)
                sender.sendMessage(ChatColor.RED + "You can only go back one more death.");
            else
                sender.sendMessage(ChatColor.RED + "Sorry, but I don't keep track of that many deaths. This is as far back as you can go.");
            return;
        }
        if (last_death != null) {
            teleport(player, null, new UltraWarp(COLOR + "HERE LIES " + player.getName() + " (death #" + (death_history.size() - last_warp_to_death_index) + "/"
                    + death_history.size() + ")", last_death), true, null);
            last_warp_to_death_indexes.put(player.getName(), last_warp_to_death_index - amount);
        }
    }

    /** This method is called when <b><tt>sender</tt></b> uses the command <i>/deathfwd (amount)</i>. <b><tt>Sender</b></tt> must be a Player for this method to be called. This
     * command teleports players forward through their death hostory. Every time someone dies, that location is saved in the player's death history; <i>/deathfwd</i> works
     * like the forward button on an Internet browser, teleporting players forward through their death history.
     * 
     * @param sender
     *            is the Player who executed the command.
     * @see {@link #death(CommandSender) death(CommandSender)} */
    private void deathForward(CommandSender sender, String[] parameters) {
        Player player = (Player) sender;
        ArrayList<Location> death_history = death_histories.get(player.getName());
        Integer last_warp_to_death_index = last_warp_to_death_indexes.get(player.getName());
        int amount = 1;
        if (parameters.length > 0)
            try {
                amount = Integer.parseInt(parameters[0]);
                if (amount == 0) {
                    player.sendMessage(ChatColor.RED + "You're already at the place you were at 0 deaths ago.");
                    return;
                } else if (amount < 0) {
                    player.sendMessage(ChatColor.RED + "Uh...negative forward? Can you just give me a positive integer, please?");
                    return;
                }
            } catch (NumberFormatException exception) {
                player.sendMessage(ChatColor.RED + "Since when is \"" + parameters[0] + "\" an integer?");
                return;
            }
        if (death_history == null || death_history.size() == 0 || last_warp_to_death_index == null) {
            player.sendMessage(ChatColor.RED + "You haven't died yet!");
            death_histories.put(player.getName(), new ArrayList<Location>());
        } else if (death_history.size() <= last_warp_to_death_index + 1 + amount) {
            if (death_history.size() - last_warp_to_death_index - 2 > 1)
                player.sendMessage(ChatColor.RED + "You can only go forward " + (death_history.size() - last_warp_to_death_index - 2) + " deaths.");
            else if (death_history.size() - last_warp_to_death_index - 2 == 1)
                player.sendMessage(ChatColor.RED + "You can only go forward one death.");
            else
                player.sendMessage(ChatColor.RED + "You're already at the last death in your history.");
        } else {
            Location death = death_history.get(last_warp_to_death_index + 1 + amount);
            if (death != null) {
                teleport(player, null, new UltraWarp(COLOR + "HERE LIES " + player.getName() + " (death #" + (death_history.size() - last_warp_to_death_index - amount - 1)
                        + "/" + death_history.size() + ")", death), true, null);
                last_warp_to_death_indexes.put(player.getName(), last_warp_to_death_index + amount);
            }
        }
    }

    /** This method is called when <b><tt>sender</tt></b> uses the command <i>/delete (owner's) [warp]</i>. This command deletes the specified warp.
     * 
     * @param extra_param
     *            is equal to 0 if the command is used as one word (<i>/delete</i> or <i>/deletewarp</i>) or 1 if the command is used as two words (<i>/delete warp</i>).
     * @param sender
     *            is the Player or <tt>console</tt> who executed the command. */
    private void deleteWarp(int extra_param, CommandSender sender, String[] parameters) {
        Player player = null;
        if (sender instanceof Player)
            player = (Player) sender;
        int index = UltraWarp.getWarpIndex(extra_param, sender, parameters);
        // delete the warp or tell the player it can't be done
        if (warps.get(index) != null
                && (player == null || player.getName().equals(warps.get(index).owner) || player.hasPermission("myultrawarps.get(i)s.delete.other") || player
                        .hasPermission("myultrawarps.get(i)s.admin"))) {
            if (player != null && warps.get(index).owner.equals(player.getName()))
                player.sendMessage(COLOR + "You deleted \"" + warps.get(index).name + ".\"");
            else
                sender.sendMessage(COLOR + "You deleted " + warps.get(index).owner + "'s warp \"" + warps.get(index).name + ".\"");
            int switches_deleted = 0;
            for (int i = 0; i < switches.size(); i++)
                if (warps.get(i).name.equals(switches.get(i).warp_name) && warps.get(i).owner.equals(switches.get(i).warp_owner)) {
                    switches.remove(i);
                    i--;
                    switches_deleted++;
                }
            if (switches_deleted > 0) {
                if (autosave_switches)
                    saveTheSwitches(sender, false);
                if (player != null && warps.get(index).owner.equals(player.getName()))
                    if (switches_deleted == 1)
                        player.sendMessage(COLOR + "You also unlinked your switch that was linked to it.");
                    else
                        player.sendMessage(COLOR + "You also unlinked your " + switches_deleted + " switches that were linked to it.");
                else if (switches_deleted == 1)
                    sender.sendMessage(COLOR + "You also unlinked a switch that was linked to it.");
                else
                    sender.sendMessage(COLOR + "You also unlinked " + switches_deleted + " switches that were linked to it.");
            }
            warps.remove(index);
            if (autosave_warps)
                saveTheWarps(sender, false);
        } else if (warps.get(index) != null && player != null)
            player.sendMessage(ChatColor.RED + "You don't have permission to delete " + warps.get(index).owner + "'s \"" + warps.get(index).name + ".\"");
        else if (player != null && (UWowner == null || player.getName().equals(UWowner)))
            player.sendMessage(ChatColor.RED + "I couldn't find \"" + UWname + ".\"");
        else if (UWowner != null)
            sender.sendMessage(ChatColor.RED + "I couldn't find \"" + UWname + "\" in " + UWowner + "'s warps.");
    }

    /** This method is called when <b><tt>sender</tt></b> uses the command <i>/forward (amount)</i>. <b><tt>Sender</b></tt> must be a Player for this method to be called. This
     * command teleports players back through their warping hostory. Every time someone is teleported to a warp or another player, that teleportation is saved in the player's
     * warp history; <i>/forward</i> works like the back button on an Internet browser, teleporting players backward through their warp history.
     * 
     * @param sender
     *            is the Player who executed the command.
     * @see {@link #back(CommandSender) back(CommandSender)} */
    private void forward(CommandSender sender, String[] parameters) {
        Player player = (Player) sender;
        ArrayList<UltraWarp> warp_history = warp_histories.get(player.getName());
        Integer last_warp_index = last_warp_indexes.get(player.getName());
        int amount = 1;
        if (parameters.length > 0)
            try {
                amount = Integer.parseInt(parameters[0]);
                if (amount == 0) {
                    player.sendMessage(ChatColor.RED + "You're already at the place you were at 0 warps ago.");
                    return;
                } else if (amount < 0) {
                    player.sendMessage(ChatColor.RED + "Uh...negative forward? Can you just give me a positive integer, please?");
                    return;
                }
            } catch (NumberFormatException exception) {
                player.sendMessage(ChatColor.RED + "Since when is \"" + parameters[0] + "\" an integer?");
                return;
            }
        if (warp_history == null || warp_history.size() == 0 || last_warp_index == null) {
            player.sendMessage(ChatColor.RED + "You haven't warped anywhere yet!");
            warp_histories.put(player.getName(), new ArrayList<UltraWarp>());
        } else if (warp_history.size() <= last_warp_index + amount) {
            if (warp_history.size() - last_warp_index - 2 > 1)
                player.sendMessage(ChatColor.RED + "You can only go forward " + (warp_history.size() - last_warp_index - 2) + " warps.");
            else if (warp_history.size() - last_warp_index - 2 == 1)
                player.sendMessage(ChatColor.RED + "You can only go forward one warp.");
            else
                player.sendMessage(ChatColor.RED + "You're already at the last warp in your history.");
        } else {
            UltraWarp warp = warp_history.get(last_warp_index + amount);
            if (warp != null) {
                boolean player_is_listed = false;
                for (String listed_player : warp.listed_users)
                    if (listed_player.equals(player.getName()))
                        player_is_listed = true;
                if (player.hasPermission("myultrawarps.admin") || player.hasPermission("myultrawarps.warptowarp.other") || warp.owner.equals(player.getName())
                        || (!warp.restricted && !player_is_listed) || (warp.restricted && player_is_listed)) {
                    teleport(player, null, warp, true, null);
                    last_warp_indexes.put(player.getName(), last_warp_index + amount);
                } else
                    player.sendMessage(colorCode(warp.no_warp_message.replaceAll("\\[player\\]", player.getName())));
            }
        }
    }

    /** This method is called when <b><tt>sender</tt></b> uses the command <i>/from [player]</i>. <b><tt>Sender</b></tt> must be a Player for this method to be called. This
     * command teleports the designated player to <b><tt>sender</tt></b> or sends a request to the target player asking them if they would teleport to <b><tt>sender</b></tt>.
     * 
     * @param sender
     *            is the Player who executed the command. */
    private void from(CommandSender sender, String[] parameters) {
        Player player = (Player) sender;
        // find the target player
        Player target_player = null;
        for (Player my_player : server.getOnlinePlayers())
            if (my_player.getName().toLowerCase().startsWith(parameters[0].toLowerCase()))
                target_player = my_player;
        // make sure the target was found, isn't blocked, and has permission to teleport to other people (or the person teleporting them is an admin since
        // admins are supreme)
        if (target_player != null
                && !target_player.equals(player)
                && (target_player.hasPermission("myultrawarps.from.accept") || target_player.hasPermission("myultrawarps.user")
                        || target_player.hasPermission("myultrawarps.admin") || player.hasPermission("myultrawarps.admin"))
                && (blocked_players.get(target_player.getName()) == null || !blocked_players.get(target_player.getName()).contains(player.getName()))) {
            if (player.hasPermission("myultrawarps.from.norequest") || player.hasPermission("myultrawarps.admin")
                    || (to_teleport_requests.get(player.getName()) != null && to_teleport_requests.get(player.getName()).contains(target_player.getName()))) {
                // remove any to teleportation requests from the target player
                ArrayList<String> requesting_players = to_teleport_requests.get(player.getName());
                if (requesting_players == null)
                    requesting_players = new ArrayList<String>();
                while (requesting_players.contains(target_player.getName()))
                    requesting_players.remove(target_player.getName());
                to_teleport_requests.put(player.getName(), requesting_players);
                if (teleport(target_player, new UltraWarp("&aThis is the spot you were at before " + player.getName() + " teleported you to them.", target_player
                        .getLocation()), new UltraWarp("&aThis is the spot you were at when you were teleported to " + player.getName() + ".", player.getLocation()), false,
                        player)) {
                    target_player.sendMessage(COLOR + "Here's your " + player.getName() + "!");
                    player.sendMessage(COLOR + "Look! I brought you a " + target_player.getName() + "!");
                }
            } else {
                player.sendMessage(COLOR + "Hang on. Let me ask " + target_player.getName() + " if it's okay.");
                target_player.sendMessage(COLOR + player.getName() + " would like to teleport you to them. Is that okay?");
                ArrayList<String> requesting_players = from_teleport_requests.get(target_player.getName());
                if (requesting_players == null)
                    requesting_players = new ArrayList<String>();
                requesting_players.add(player.getName());
                from_teleport_requests.put(target_player.getName(), requesting_players);
            }
        } else if (target_player != null && blocked_players.get(target_player.getName()) != null && blocked_players.get(target_player.getName()).contains(player.getName()))
            player.sendMessage(ChatColor.RED + "Sorry, but " + target_player.getName() + " has blocked you. You can't send them teleportation requests anymore.");
        else if (target_player != null && target_player.equals(player))
            player.sendMessage(ChatColor.RED + "Can you explain to me how I'm supposed to teleport you to yourself?");
        else if (target_player != null && !target_player.hasPermission("myultrawarps.to") && !target_player.hasPermission("myultrawarps.user")
                && !target_player.hasPermission("myultrawarps.admin") && !player.hasPermission("myultrawarps.admin"))
            player.sendMessage(ChatColor.RED + "Sorry, but " + target_player.getName() + " doesn't have permission to teleport to other poeple.");
        else
            player.sendMessage(ChatColor.RED + "I couldn't find \"" + parameters[0] + "\" anywhere.");
    }

    /** This method is called when <b><tt>sender</tt></b> uses the command <i>/full switch list (filters)</i>. This command lists every switch on the server alphabetized either
     * by warp name or by warp owner. By default, the list is organized by warp owner; however, if <b><tt>sender</tt></b> specifies that the list be organized by warp name
     * instead, this command will default to organizing the list by warp name for that user or <tt>console</tt> instead of by warp owner until myUltraWarps is reloaded or that
     * user or <tt>console</tt> specifies otherwise.
     * 
     * @param sender
     *            is the Player or <tt>console</tt> who executed the command.
     * 
     * @filters "page [#]" designates the page that they want to see. The switch list is sometimes too long to fit in 10 lines in the Minecraft chat box, so myUltraWarps
     *          compiles the list and divides it into pages. Pages are three times as long if they are output to the console instead to a Player in game.
     *          <hr>
     *          "by owner" designates that the list should be organized and alphabetized by the warps' owners' names; "by name" designates that the list should be alphabetized
     *          by the names of the switches' associated warps. By default, the list is organized by warp owner; however, if <b><tt>sender</tt></b> specifies that the list be
     *          organized by warp name instead, this command will default to organizing the list by warp name for that user or <tt>console</tt> instead of by warp owner until
     *          myUltraWarps is reloaded or that user or <tt>console</tt> specifies otherwise.
     *          <hr>
     *          "type:[type]" can filter the switches by warp type (open, secret, advertised, or private).
     *          <hr> */
    private void fullSwitchList(CommandSender sender, String[] parameters) {
        if (switches.size() == 0) {
            sender.sendMessage(ChatColor.RED + "No one has made any switches yet!");
            return;
        }
        Player player = null;
        String sender_name = "[console]";
        if (sender instanceof Player) {
            player = (Player) sender;
            sender_name = player.getName();
        }
        boolean by_name = false;
        if (full_list_organization_by_user.containsKey(sender_name))
            by_name = full_list_organization_by_user.get(sender_name);
        int extra_param = 0;
        if (parameters.length > 1 && parameters[0].equalsIgnoreCase("warp") && parameters[1].equalsIgnoreCase("list"))
            extra_param = 2;
        String owner = null, type = null;
        int page_number = 1;
        for (int i = extra_param; i < parameters.length; i++) {
            if (parameters[i].toLowerCase().startsWith("type:o"))
                type = "open";
            else if (parameters[i].toLowerCase().startsWith("type:a"))
                type = "advertised";
            else if (parameters[i].toLowerCase().startsWith("type:s"))
                type = "secret";
            else if (parameters[i].toLowerCase().startsWith("type:p"))
                type = "private";
            else if (parameters[i].toLowerCase().startsWith("owner:")) {
                owner = autoCompleteName(parameters[i].substring(6));
                by_name = false;
            } else if (parameters[i].equalsIgnoreCase("page")) {
                if (parameters.length <= i + 1) {
                    sender.sendMessage(ChatColor.RED + "You forgot to tell me which page you want me to show you!");
                    return;
                }
                try {
                    page_number = Integer.parseInt(parameters[i + 1]);
                } catch (NumberFormatException exception) {
                    sender.sendMessage(ChatColor.RED + "Since when is \"" + parameters[i + 1] + "\" an integer?");
                    return;
                }
                if (page_number == 0) {
                    sender.sendMessage(ChatColor.RED + "I think you know very well that there is no page 0, you little trouble maker. Nice try.");
                    return;
                } else if (page_number < 0) {
                    sender.sendMessage(ChatColor.RED + "Negative page numbers? Really? Try again.");
                    return;
                }
            } else if (parameters[i].equals("by"))
                if (parameters.length <= i + 1) {
                    sender.sendMessage(ChatColor.RED + "You forgot to tell me how you want me to organize the list!");
                    return;
                } else if (parameters[i + 1].equalsIgnoreCase("owner"))
                    by_name = false;
                else if (parameters[i + 1].equalsIgnoreCase("name"))
                    by_name = true;
                else {
                    sender.sendMessage(ChatColor.RED + "I'm not sure how to organize a list \"by " + parameters[i + 1]
                            + "\". I can only organize this list by owner or by name.");
                    return;
                }
        }
        // save the user's organization preference
        full_list_organization_by_user.put(sender_name, by_name);
        // if the list is organized alphabetically by warp name...
        String output = "";
        if (by_name)
            for (UltraSwitch _switch : switches) {
                UltraWarp warp = UltraWarp.getWarp(0, sender, _switch.warp_owner + "'s", _switch.warp_name);
                if (warp != null && (type == null || warp.getType().equals(type)) && (owner == null || warp.owner.equals(owner)))
                    output += (output.equals("") ? "" : ", ") + warp.getColoredName();
            }
        // ...or if the list is organized alphabetically by owner...
        else {
            // "switch items" are like the paragraphs that make up the by owner warp list; each warp item lists all the warps owned by one player
            ArrayList<String> switch_items = new ArrayList<String>();
            for (int i = 0; i < switches.size(); i++) {
                UltraWarp warp = UltraWarp.getWarp(0, sender, switches.get(i).warp_owner + "'s", switches.get(i).warp_name);
                if (warp == null || type != null && !warp.getType().equals(type) || owner != null && !warp.owner.equals(owner))
                    continue;
                // find out how many switches this warp is linked to
                int number = 1;
                while (i + 1 < switches.size() && switches.get(i + 1).warp_owner.equals(warp.owner) && switches.get(i + 1).warp_name.equals(warp.name)) {
                    number++;
                    i++;
                }
                // try to find the warp item for this warp's owner
                int switch_item_index = -1;
                for (int j = 0; j < switch_items.size(); j++)
                    if (switch_items.get(j).startsWith(COLOR + warp.owner)) {
                        switch_item_index = j;
                        break;
                    }
                // if there was a preexisting warp item for this warp's owner, add this new warp's name to the end of the list
                if (switch_item_index != -1)
                    switch_items.set(switch_item_index, switch_items.get(switch_item_index) + ChatColor.WHITE + ", " + warp.getColoredName()
                            + (number > 1 ? ChatColor.WHITE + " x" + number : ""));
                /* if there wasn't a preexisting warp item for this warp's owner, figure out where the new warp item should go in the list (organizing the list alphabetically)
                 * and add a new one */
                else {
                    String switch_item = COLOR + warp.owner + "'s switches: " + warp.getColoredName() + (number > 1 ? ChatColor.WHITE + " x" + number : "");
                    switch_item_index = 0;
                    while (switch_item_index < switch_items.size() && switch_items.get(switch_item_index).compareTo(switch_item) < 0)
                        switch_item_index++;
                    debug("new warp item's index: " + switch_item_index);
                    switch_items.add(switch_item_index, switch_item);
                }
            }
            output = combine(switch_items.toArray(), "\n");
        }
        sender.sendMessage(paginate(output, "/full switch list page [#]", "There are only [total] of switches.", page_number, sender instanceof Player));
    }

    /** This method is called when <b><tt>sender</tt></b> uses the command <i>/full warp list (filters)</i>. This command lists every warp on the server alphabetized either by
     * name or by owner. By default, the list is organized by owner; however, if <b><tt>sender</tt></b> specifies that the list be organized by name instead, this command will
     * default to organizing the list by name for that user or <tt>console</tt> instead of by owner until myUltraWarps is reloaded or that user or <tt>console</tt> specifies
     * otherwise.
     * 
     * @param sender
     *            is the Player or <tt>console</tt> who executed the command.
     * 
     * @filters "page [#]" designates the page that they want to see. The warps list is usually far too long to fit in 10 lines in the Minecraft chat box even on a small
     *          server, so myUltraWarps compiles the list and divides it into pages. Pages are three times as long if they are output to the console instead to a Player in
     *          game.
     *          <hr>
     *          "by owner" designates that the list should be organized and alphabetized by the warp owners' names; "by name" designates that the list should be alphabetized
     *          by the names of the warp. By default, the list is organized by owner; however, if <b><tt>sender</tt></b> specifies that the list be organized by name instead,
     *          this command will default to organizing the list by name for that user or <tt>console</tt> instead of by owner until myUltraWarps is reloaded or that user or
     *          <tt>console</tt> specifies otherwise.
     *          <hr>
     *          "type:[type]" can filter the warps by type (open, secret, advertised, or private).
     *          <hr> */
    private void fullWarpList(CommandSender sender, String[] parameters) {
        if (warps.size() == 0) {
            sender.sendMessage(ChatColor.RED + "No one has made any warps yet!");
            return;
        }
        Player player = null;
        String sender_name = "[console]";
        if (sender instanceof Player) {
            player = (Player) sender;
            sender_name = player.getName();
        }
        boolean by_name = false;
        if (full_list_organization_by_user.containsKey(sender_name))
            by_name = full_list_organization_by_user.get(sender_name);
        int extra_param = 0;
        if (parameters.length > 1 && parameters[0].equalsIgnoreCase("warp") && parameters[1].equalsIgnoreCase("list"))
            extra_param = 2;
        String owner = null, type = null;
        int page_number = 1;
        for (int i = extra_param; i < parameters.length; i++) {
            if (parameters[i].toLowerCase().startsWith("type:o"))
                type = "open";
            else if (parameters[i].toLowerCase().startsWith("type:a"))
                type = "advertised";
            else if (parameters[i].toLowerCase().startsWith("type:s"))
                type = "secret";
            else if (parameters[i].toLowerCase().startsWith("type:p"))
                type = "private";
            else if (parameters[i].toLowerCase().startsWith("owner:")) {
                owner = autoCompleteName(parameters[i].substring(6));
                by_name = false;
            } else if (parameters[i].equalsIgnoreCase("page")) {
                if (parameters.length <= i + 1) {
                    sender.sendMessage(ChatColor.RED + "You forgot to tell me which page you want me to show you!");
                    return;
                }
                try {
                    page_number = Integer.parseInt(parameters[i + 1]);
                } catch (NumberFormatException exception) {
                    sender.sendMessage(ChatColor.RED + "Since when is \"" + parameters[i + 1] + "\" an integer?");
                    return;
                }
                if (page_number == 0) {
                    sender.sendMessage(ChatColor.RED + "I think you know very well that there is no page 0, you little trouble maker. Nice try.");
                    return;
                } else if (page_number < 0) {
                    sender.sendMessage(ChatColor.RED + "Negative page numbers? Really? Try again.");
                    return;
                }
            } else if (parameters[i].equals("by"))
                if (parameters.length <= i + 1) {
                    sender.sendMessage(ChatColor.RED + "You forgot to tell me how you want me to organize the list!");
                    return;
                } else if (parameters[i + 1].equalsIgnoreCase("owner"))
                    by_name = false;
                else if (parameters[i + 1].equalsIgnoreCase("name"))
                    by_name = true;
                else {
                    sender.sendMessage(ChatColor.RED + "I'm not sure how to organize a list \"by " + parameters[i + 1]
                            + "\". I can only organize this list by owner or by name.");
                    return;
                }
        }
        // save the user's organization preference
        full_list_organization_by_user.put(sender_name, by_name);
        // if the list is organized alphabetically by warp name...
        String output = "";
        if (by_name)
            for (UltraWarp warp : warps) {
                if ((type == null || warp.getType().equals(type)) && (owner == null || warp.owner.equals(owner)))
                    output += (output.equals("") ? "" : ", ") + warp.getColoredName();
            }
        // ...or if the list is organized alphabetically by owner...
        else {
            // "warp items" are like the paragraphs that make up the by owner warp list; each warp item lists all the warps owned by one player
            ArrayList<String> warp_items = new ArrayList<String>();
            for (UltraWarp warp : warps) {
                if (type != null && !warp.getType().equals(type) || owner != null && !warp.owner.equals(owner))
                    continue;
                int warp_item_index = -1;
                // try to find the warp item for this warp's owner
                for (int i = 0; i < warp_items.size(); i++)
                    if (warp_items.get(i).startsWith(COLOR + warp.owner)) {
                        warp_item_index = i;
                        break;
                    }
                // if there was a preexisting warp item for this warp's owner, add this new warp's name to the end of the list
                if (warp_item_index != -1)
                    warp_items.set(warp_item_index, warp_items.get(warp_item_index) + ChatColor.WHITE + ", " + warp.getColoredName());
                // if there wasn't a preexisting warp item for this warp's owner, figure out where the new warp item should go in the list (organizing
                // the list alphabetically) and add a new one
                else {
                    String warp_item = COLOR + warp.owner + "'s warps: " + warp.getColoredName();
                    warp_item_index = 0;
                    while (warp_item_index < warp_items.size() && warp_items.get(warp_item_index).compareTo(warp_item) < 0)
                        warp_item_index++;
                    debug("new warp item's index: " + warp_item_index);
                    warp_items.add(warp_item_index, warp_item);
                }
            }
            output = combine(warp_items.toArray(), "\n");
        }
        sender.sendMessage(paginate(output, "/full warp list page [#]", "There are only [total] of warps.", page_number, sender instanceof Player));
    }

    /** This method is called when <b><tt>sender</tt></b> uses the command <i>/jump</i>. <b><tt>Sender</b></tt> must be a Player for this method to be called. This command
     * teleports the player to the block they're pointing at.
     * 
     * @param sender
     *            is the Player who executed the command. */
    @SuppressWarnings("deprecation")
    private void jump(CommandSender sender) {
        Player player = (Player) sender;
        Block block = getTargetBlock(player, true);
        if (block != null) {
            Short[] half_height_block_IDs = { 44, 93, 94, 96, 111, 126, 149, 150, 151 }, over_height_block_IDs = { 85, 107, 113 };
            // adjust the y based on the height of the block we're teleporting to
            double y = block.getLocation().getY() + 1;
            if (contains(half_height_block_IDs, (short) block.getTypeId())
            // make sure that if it's a slab, it is in the lower position
                    && (block.getType() != Material.STEP || block.getData() < 8))
                y -= 0.5;
            else if (contains(over_height_block_IDs, (short) block.getTypeId()))
                y += 0.5;
            teleport(player, null, new UltraWarp(COLOR + "You jumped!", new Location(block.getLocation().getWorld(), block.getLocation().getX() + 0.5, y, block.getLocation()
                    .getZ() + 0.5, player.getLocation().getYaw(), player.getLocation().getPitch())), true, null);
        } else
            player.sendMessage(ChatColor.RED + "Sorry, but I can't see that far!");
    }

    /** This method is called when <b><tt>sender</tt></b> uses the command <i>/home (owner's)</i>. <b><tt>Sender</b></tt> must be a Player for this method to be called. This
     * command teleports the player to their "home" warp or to the home of the specified player.
     * 
     * @param sender
     *            is the Player who executed the command. */
    private void home(CommandSender sender, String[] parameters) {
        Player player = (Player) sender;
        UltraWarp warp = null;
        String owner;
        // extract the name of the player and the name of the warp
        if (parameters.length > 0 && parameters[0].toLowerCase().endsWith("'s"))
            owner = autoCompleteName(parameters[0].substring(0, parameters[0].length() - 2));
        else
            owner = player.getName();
        // locate the warp in the list of warps
        for (int i = 0; i < warps.size(); i++)
            if (warps.get(i).name.equals("home") && warps.get(i).owner.toLowerCase().startsWith(owner.toLowerCase()))
                warp = warps.get(i);
        if (warp != null) {
            if (player.getName().equals(owner) || player.hasPermission("myultrawarps.home.other") || player.hasPermission("myultrawarps.admin")) {
                if (teleport(player, new UltraWarp("&aThis is the spot you were at before you teleported home.", player.getLocation()), warp, false, null))
                    if (player.getName().equals(owner) && !warp.warp_message.equals(""))
                        player.sendMessage(colorCode(warp.warp_message.replaceAll("\\[player\\]", player.getName())));
                    else if (!warp.warp_message.equals(""))
                        player.sendMessage(colorCode("&aWelcome home...wait, you're not " + warp.owner + "! &o" + warp.owner.toUpperCase() + "!!!!"));
            } else
                player.sendMessage(colorCode(warp.no_warp_message.replaceAll("\\[player\\]", player.getName())));
        } else {
            // tell the player the warp wasn't found
            if (player.getName().toLowerCase().startsWith(owner.toLowerCase()))
                player.sendMessage(ChatColor.RED + "You need to set your home before you can warp to it!");
            else
                player.sendMessage(ChatColor.RED + "I couldn't find " + owner + "'s home.");
        }
    }

    /** This method is called when <b><tt>sender</tt></b> uses the command <i>/link (owner's) [warp]</i>. <b><tt>Sender</b></tt> must be a Player for this method to be called.
     * This command links a warp to the switch that <b><tt>sender</b></tt> is pointing at. (You could also think of it as creating a new warp switch ["UltraSwitch"]).
     * 
     * @param extra_param
     *            is equal to 0 if the command is used as one word (<i>/link</i> or <i>/linkwarp</i>) or 1 if the command is used as two words (<i>/link warp</i>).
     * @param sender
     *            is the Player who executed the command. */
    private void linkWarp(int extra_param, CommandSender sender, String[] parameters) {
        Player player = (Player) sender;
        Block target_block = getTargetBlock(player, false);
        UltraWarp warp = null;
        if (target_block != null && UltraSwitch.getSwitchType(target_block) != null) {
            // check to make sure that that switch isn't already linked to something
            for (UltraSwitch my_switch : switches)
                if (target_block.getLocation().equals(my_switch.location) && UltraSwitch.getSwitchType(target_block).equals(my_switch.switch_type)) {
                    if (player.getName().equals(my_switch.warp_owner))
                        sender.sendMessage(ChatColor.RED + "This " + my_switch.switch_type + " is already linked to \"" + my_switch.warp_name + "\".");
                    else
                        sender.sendMessage(ChatColor.RED + "This " + my_switch.switch_type + " is already linked to " + my_switch.warp_owner + "'s \"" + my_switch.warp_name
                                + "\".");
                    return;
                }
            warp = UltraWarp.getWarp(extra_param, sender, parameters);
            if (warp != null && (player.getName().equals(warp.owner) || player.hasPermission("myultrawarps.link.other") || player.hasPermission("myultrawarps.admin"))) {
                // search for non-default settings changes
                boolean parse_cooldown_time = false, global = false;
                double cost = 0;
                int max_uses = 0, cooldown_time = 0;
                String cooldown_time_string = null;
                String[] exempted_players = new String[0];
                for (int j = 0; j < parameters.length; j++) {
                    if (parameters[j].toLowerCase().startsWith("cooldown:")) {
                        parse_cooldown_time = true;
                        cooldown_time_string = parameters[j].substring(9);
                    } else if (parameters[j].toLowerCase().startsWith("uses:")) {
                        parse_cooldown_time = false;
                        try {
                            max_uses = Integer.parseInt(parameters[j].substring(5));
                        } catch (NumberFormatException exception) {
                            sender.sendMessage(ChatColor.RED + "You can't use something \"" + parameters[j].substring(5) + "\" times!");
                            return;
                        }
                    } else if (parameters[j].equalsIgnoreCase("global:true")) {
                        parse_cooldown_time = false;
                        global = true;
                    } else if (parse_cooldown_time) {
                        if (cooldown_time_string == null || cooldown_time_string.equals(""))
                            cooldown_time_string = parameters[j];
                        else
                            cooldown_time_string += " " + parameters[j];
                    }
                }
                // get the cooldown time
                if (cooldown_time_string != null)
                    cooldown_time = readTime(cooldown_time_string);
                // find out where the new switch needs to be in the list to be properly alphabetized
                int insertion_index = 0;
                for (UltraSwitch my_switch : switches)
                    if (my_switch.warp_name.compareToIgnoreCase(warp.name) < 0
                            || (my_switch.warp_name.compareToIgnoreCase(warp.name) == 0 && my_switch.warp_owner.compareToIgnoreCase(warp.owner) <= 0))
                        insertion_index++;
                // make the switch
                switches.add(insertion_index, new UltraSwitch(warp.name, warp.owner, target_block, cooldown_time, max_uses, global, cost, exempted_players));
                if (autosave_switches)
                    saveTheSwitches(sender, false);
                if (player.getName().toLowerCase().startsWith(warp.owner.toLowerCase()))
                    player.sendMessage(COLOR + "You linked \"" + warp.name + "\" to this " + UltraSwitch.getSwitchType(target_block) + ".");
                else
                    player.sendMessage(COLOR + "You linked " + warp.owner + "'s \"" + warp.name + "\" to this " + UltraSwitch.getSwitchType(target_block) + ".");
            } else if (warp != null)
                player.sendMessage(ChatColor.RED + "You're not allowed to link warps that don't belong to you!");
            else {
                if (player.getName().equals(UWowner))
                    player.sendMessage(ChatColor.RED + "I couldn't find \"" + UWname + ".\"");
                else
                    player.sendMessage(ChatColor.RED + "I couldn't find \"" + UWname + "\" in " + UWowner + "'s warps.");
            }
        } else if (target_block != null)
            player.sendMessage(ChatColor.RED + "You can only link warps to buttons, pressure plates (non-weighted), or levers.");
        else
            player.sendMessage(ChatColor.RED + "Please point at the switch you want to link your warp to and try " + COLOR + "/link " + ChatColor.RED + "again.");
    }

    /** This method is called when <b><tt>sender</tt></b> uses the command <i>/move (owner's) [warp]</i>. <b><tt>Sender</b></tt> must be a Player for this method to be called.
     * This command moves the specified warp to the spot where <b><tt>sender</b></tt> is standing.
     * 
     * @param extra_param
     *            is equal to 0 if the command is used as one word (<i>/move</i> or <i>/movewarp</i>) or 1 if the command is used as two words (<i>/move warp</i>).
     * @param sender
     *            is the Player who executed the command. */
    private void moveWarp(int extra_param, CommandSender sender, String[] parameters) {
        Player player = (Player) sender;
        UltraWarp warp = UltraWarp.getWarp(extra_param, sender, parameters);
        // change the location of the warp or tell the player it can't be done
        if (warp != null && (player.getName().equals(warp.owner) || player.hasPermission("myultrawarps.change.other") || player.hasPermission("myultrawarps.admin"))) {
            warps.set(UWindex, new UltraWarp(warp.owner, warp.name, warp.listed, warp.restricted, warp.warp_message, warp.no_warp_message, warp.listed_users, player
                    .getLocation()));
            if (autosave_warps)
                saveTheWarps(sender, false);
            if (player.getName().equals(warp.owner))
                player.sendMessage(COLOR + "You moved \"" + warps.get(UWindex).name + ".\"");
            else
                player.sendMessage(COLOR + "You moved " + warps.get(UWindex).owner + "'s warp \"" + warps.get(UWindex).name + ".\"");
        } else if (warp != null)
            player.sendMessage(ChatColor.RED + "You don't have permission to modify this warp.");
        else if (player.getName().equals(UWowner))
            player.sendMessage(ChatColor.RED + "I couldn't find \"" + UWname + ".\"");
        else
            player.sendMessage(ChatColor.RED + "I couldn't find \"" + UWname + "\" in " + UWowner + "'s warps.");
    }

    /** This method is called when <b><tt>sender</tt></b> uses the command <i>/send [player] ("to") ["there"/"warp" (owner's) [warp]/"player" [player]]</i>. This command
     * teleports the specified player either to the spot that <b><tt>sender</b></tt> is pointing at (using "there"), the specified warp (using "warp" [warp]), or the specified
     * target player (using "player" [player]). <b><tt>Sender</b></tt> must be a Player if they attempt to send the target player "there".
     * 
     * @param sender
     *            is the Player who executed the command. */
    private void send(CommandSender sender, String[] parameters) {
        Player player = null;
        if (sender instanceof Player)
            player = (Player) sender;
        Player target_player = null;
        for (Player my_player : server.getOnlinePlayers())
            if (my_player.getName().toLowerCase().startsWith(parameters[0].toLowerCase()))
                target_player = my_player;
        int extra_param = 0;
        if (parameters[1].equalsIgnoreCase("to"))
            extra_param++;
        if (target_player != null) {
            if (parameters[1 + extra_param].equalsIgnoreCase("there")) {
                if (player != null) {
                    Block target_block = getTargetBlock(player, true);
                    if (target_block != null) {
                        Location target_location = target_block.getLocation();
                        target_location.setY(target_location.getY() + 1);
                        if (teleport(target_player, new UltraWarp("God", "coordinates", false, false, "&aThis is the spot you were at before " + player.getName()
                                + " teleported you elsewhere.", "", null, target_player.getLocation()), new UltraWarp("God", "coordinates", false, false,
                                "&aThis is the spot you were at when you were teleported by " + player.getName() + ".", "", null, target_location), false, player)) {
                            target_player.sendMessage(COLOR + player.getName() + " teleported you here.");
                            if (target_player.getName().toLowerCase().startsWith("a") || target_player.getName().toLowerCase().startsWith("e")
                                    || target_player.getName().toLowerCase().startsWith("i") || target_player.getName().toLowerCase().startsWith("o")
                                    || target_player.getName().toLowerCase().startsWith("u"))
                                player.sendMessage(COLOR + "Hark! Over yonder! An " + target_player.getName() + " cometh!");
                            else
                                player.sendMessage(COLOR + "Hark! Over yonder! A " + target_player.getName() + " cometh!");
                        }
                    } else
                        player.sendMessage(ChatColor.RED + "The block you targeted is too far away.");
                } else
                    sender.sendMessage(ChatColor.RED + "Please point out the place you want to teleport " + target_player.getName()
                            + ". Oh, yeah. You still can't. You're still a console.");
            } else if (parameters[1 + extra_param].equalsIgnoreCase("warp")) {
                if (parameters.length >= 3 + extra_param) {
                    UltraWarp warp = UltraWarp.getWarp(2 + extra_param, sender, parameters);
                    if (warp != null) {
                        // save the player's location before warping
                        String sender_name = "someone";
                        if (player != null)
                            sender_name = player.getName();
                        if (teleport(target_player, new UltraWarp("God", "coordinates", false, false, "&aThis is the spot you were at before " + sender_name
                                + " teleported you elsewhere.", "", null, target_player.getLocation()), warp, false, player)) {
                            if (sender_name.equals("someone"))
                                sender_name = "Someone";
                            target_player.sendMessage(COLOR + sender_name + " telported you to " + warp.owner + "'s \"" + warp.name + ".\"");
                            if (player != null && warp.owner.equals(player.getName()))
                                player.sendMessage(COLOR + "I sent " + target_player.getName() + " to \"" + warp.name + ".\"");
                            else
                                sender.sendMessage(COLOR + "I sent " + target_player.getName() + " to " + warp.owner + "'s \"" + warp.name + ".\"");
                        }
                    } else if (player != null && (UWowner == null || player.getName().equals(UWowner)))
                        player.sendMessage(ChatColor.RED + "I couldn't find \"" + UWname + ".\"");
                    else if (UWowner != null)
                        sender.sendMessage(ChatColor.RED + "I couldn't find \"" + UWname + "\" in " + UWowner + "'s warps.");
                } else
                    sender.sendMessage(ChatColor.RED + "You forgot to tell me what warp you want to warp " + target_player.getName() + " to!");
            } else if (parameters[1 + extra_param].equalsIgnoreCase("player")) {
                if (parameters.length >= 3 + extra_param) {
                    Player final_destination_player = null;
                    for (Player online_player : server.getOnlinePlayers()) {
                        if (online_player.getName().toLowerCase().startsWith(parameters[2 + extra_param].toLowerCase()))
                            final_destination_player = online_player;
                    }
                    if (final_destination_player != null) {
                        // save the player's location before warping
                        String sender_name = "someone";
                        if (player != null)
                            sender_name = player.getName();
                        if (teleport(target_player, new UltraWarp("God", "coordinates", false, false, "&aThis is the spot you were at before " + sender_name
                                + " teleported you elsewhere.", "", null, target_player.getLocation()), new UltraWarp("God", "coordinates", false, false,
                                "&aThis is the spot you were at when you were teleported by " + sender_name + ".", "", null, target_player.getLocation()), false, sender)) {
                            target_player.sendMessage(COLOR + sender_name + " teleported you to " + final_destination_player.getName() + ".");
                            sender.sendMessage(COLOR + "I sent " + target_player.getName() + " to " + final_destination_player.getName() + ".");
                        }
                    } else {
                        for (OfflinePlayer offline_player : server.getOfflinePlayers())
                            if (offline_player.getName().toLowerCase().startsWith(parameters[0].toLowerCase())) {
                                sender.sendMessage(ChatColor.RED + offline_player.getName() + " is not online right now.");
                                return;
                            }
                        sender.sendMessage(ChatColor.RED + "Sorry, but I've never seen anyone with a name starting with \"" + parameters[0] + "\" come on this server.");
                    }
                } else
                    sender.sendMessage(ChatColor.RED + "You forgot to tell me which player you want me to warp " + target_player.getName() + " to!");
            } else
                sender.sendMessage("/send [player] (\"to\") [\"there\"/\"warp\" [warp]/\"player\" [player]]");
        } else {
            for (OfflinePlayer offline_player : server.getOfflinePlayers())
                if (offline_player.getName().toLowerCase().startsWith(parameters[0].toLowerCase())) {
                    sender.sendMessage(ChatColor.RED + offline_player.getName() + " is not online right now.");
                    return;
                }
            sender.sendMessage(ChatColor.RED + "Sorry, but I've never seen anyone with a name starting with \"" + parameters[0] + "\" come on this server.");
        }
    }

    /** This method is called when <b><tt>sender</tt></b> uses the command <i>/set home (owner's)</i>. <b><tt>Sender</b></tt> must be a Player for this method to be called.
     * This command creates a specialized warp called "home" for the designated player (<b><tt>sender</b></tt>'s by default) which has special default warp and no warp
     * messages, a teleportation command that works specifically for it (<i>/home</i>; see {@link #home(CommandSender) home(CommandSender)}), command nodes specific to it
     * (myultrawarps.home, myultrawarps.sethome, and more), and the special ability to let players automatically teleport to it when they respawn after dying (if the player
     * has the permission myultrawarps.respawnhome and the settings for that player in the <tt>config.txt</tt> allows for it).
     * 
     * @param sender
     *            is the Player who executed the command. */
    private void setHome(CommandSender sender, String[] parameters) {
        Player player = (Player) sender;
        int extra_param = 0;
        if (parameters != null && parameters.length > 0 && parameters[0].equalsIgnoreCase("home"))
            extra_param++;
        String owner = null;
        // check if the home is for someone else
        if (parameters != null && parameters.length > extra_param && parameters[extra_param].toLowerCase().endsWith("'s"))
            owner = autoCompleteName(parameters[extra_param].substring(0, parameters[extra_param].length() - 2));
        else
            owner = player.getName();
        if ((player.getName().toLowerCase().startsWith(owner.toLowerCase())) || player.hasPermission("myultrawarps.sethome.other")
                || player.hasPermission("myultrawarps.admin")) {
            // delete the old home if it exists
            for (int i = 0; i < warps.size(); i++)
                if (warps.get(i).name.equals("home") && warps.get(i).owner.toLowerCase().startsWith(owner.toLowerCase()))
                    warps.remove(i);
            // set the new home
            warps.add(new UltraWarp(owner, "home", false, true, "&aWelcome home, " + owner + ". We have awaited your return.",
                    "&cYou're not allowed to just warp to other people's homes! The nerve!", new String[0], player.getLocation()));
            if (autosave_warps)
                saveTheWarps(sender, false);
            if (owner.equals(player.getName()))
                player.sendMessage(COLOR + "Henceforth, this shall be your new home.");
            else
                player.sendMessage(COLOR + "Henceforth, this shall be " + owner + "'s new home.");
        } else
            player.sendMessage(ChatColor.RED + "You can't set someone else's home!");
    }

    /** This method is called when <b><tt>sender</tt></b> uses the command <i>/set spawn</i>. <b><tt>Sender</b></tt> must be a Player for this method to be called. This command
     * sets the location of the spawn point for the world which <b><tt>sender</b></tt> is currently in to <b><tt>sender</b></tt>'s current location.
     * 
     * @param sender
     *            is the Player who executed the command. */
    private void setSpawn(CommandSender sender) {
        Player player = (Player) sender;
        player.getWorld().setSpawnLocation(player.getLocation().getBlockX(), player.getLocation().getBlockY(), player.getLocation().getBlockZ());
        String world_name;
        if (player.getWorld().getWorldFolder().getName().endsWith("_nether"))
            world_name = "The Nether";
        else if (player.getWorld().getWorldFolder().getName().endsWith("_the_end"))
            world_name = "The End";
        else
            world_name = player.getWorld().getWorldFolder().getName();
        player.sendMessage(COLOR + "Henceforth, this shall be " + world_name + "'s new spawn point.");
    }

    /** This method is called when <b><tt>sender</tt></b> uses the command <i>/spawn</i>. <b><tt>Sender</b></tt> must be a Player for this method to be called. This command
     * teleports <b><tt>sender</b></tt> to the spawn point for the world that they are currently in.
     * 
     * @param sender
     *            is the Player who executed the command. */
    private void spawn(CommandSender sender, String[] parameters) {
        Player player = (Player) sender;
        String world_name = player.getWorld().getWorldFolder().getName();
        if (world_name.endsWith("_nether"))
            world_name = "The Nether";
        else if (world_name.endsWith("_the_end"))
            world_name = "The End";
        String warp_message = spawn_messages_by_world.get(player.getWorld());
        if (warp_message == null)
            warp_message = COLOR + "Welcome to " + player.getWorld().getWorldFolder().getName() + ", " + player.getName() + ".";
        teleport(player, new UltraWarp(COLOR + "This is the spot you were at before you teleported to " + world_name + "'s spawn point.", player.getLocation()),
                new UltraWarp(warp_message, player.getWorld().getSpawnLocation()), true, null);
    }

    /** This method is called when <b><tt>sender</tt></b> uses the command <i>/switch list</i>. If <b><tt>sender</b></tt> is a Player, this command lists all of the switches
     * owned by <b><tt>sender</b></tt>; if <b><tt>sender</b></tt> is a <tt>console</tt>, then this command will redirect to <i>/full switch list</i> (see
     * {@link #fullSwitchList(CommandSender) fullSwitchList(CommandSender)}). The list in either case is displayed based on the warp that is linked to that switch and the
     * number of switches linked to that warp. For example, if <b><tt>sender</b></tt> had a warp called "herkaderkala" that was linked to two switches, one of the items in the
     * list would be displayed as "herkaderkala x2".
     * 
     * @param sender
     *            is the Player or <tt>console</tt> who executed the command. */
    private void switchList(CommandSender sender, String[] parameters) {
        Player player = null;
        if (sender instanceof Player)
            player = (Player) sender;
        if (switches.size() > 0 && player == null)
            fullSwitchList(sender, parameters);
        else if (switches.size() > 0 && player != null) {
            // read the parameters to figure out what page is needed
            byte page_number = 1;
            for (byte i = 0; i < parameters.length; i++)
                if (parameters[i].equalsIgnoreCase("page")) {
                    if (parameters.length <= i + 1) {
                        sender.sendMessage(ChatColor.RED + "You forgot to tell me which page you want me to show you!");
                        return;
                    }
                    try {
                        page_number = Byte.parseByte(parameters[i + 1]);
                    } catch (NumberFormatException exception) {
                        sender.sendMessage(ChatColor.RED + "Since when is \"" + parameters[i + 1] + "\" an integer?");
                        return;
                    }
                    if (page_number == 0) {
                        sender.sendMessage(ChatColor.RED + "I think you know very well that there is no page 0, you little trouble maker. Nice try.");
                        return;
                    } else if (page_number < 0) {
                        sender.sendMessage(ChatColor.RED + "Negative page numbers? Really? Try again.");
                        return;
                    }
                }
            // make lists of the warps associated with the switches and the number of switches each of those warps is associated with
            ArrayList<UltraWarp> switch_warps = new ArrayList<UltraWarp>();
            ArrayList<Integer> switch_warp_quantities = new ArrayList<Integer>();
            for (int i = 0; i < switches.size(); i++) {
                if (switches.get(i).warp_owner.equals(player.getName())) {
                    // locate the warp
                    UltraWarp warp = null;
                    for (UltraWarp my_warp : warps)
                        if (my_warp.name.equals(switches.get(i).warp_name) && my_warp.owner.equals(switches.get(i).warp_owner))
                            warp = my_warp;
                    if (warp == null) {
                        switches.remove(i);
                        i--;
                    } else {
                        int counter = 0;
                        int index = -1;
                        for (int j = 0; j < switch_warps.size(); j++)
                            if (switch_warps.get(j).equals(warp)) {
                                counter = switch_warp_quantities.get(j);
                                index = j;
                                j = switch_warps.size();
                            }
                        if (counter == 0) {
                            switch_warps.add(warp);
                            switch_warp_quantities.add(1);
                        } else
                            switch_warp_quantities.set(index, counter + 1);
                    }
                }
            }
            // construct the output String
            String output = "";
            if (switch_warps.size() > 0) {
                output = COLOR + "your switches: ";
                for (int i = 0; i < switch_warps.size(); i++) {
                    UltraWarp warp = switch_warps.get(i);
                    if (i > 0)
                        output += ChatColor.WHITE + ", ";
                    output += warp.getColoredName() + (switch_warp_quantities.get(i) > 1 ? ChatColor.WHITE + " x" + switch_warp_quantities.get(i) : "");
                }
            } else
                output = ChatColor.RED + "You don't have any switches yet!";
            player.sendMessage(paginate(output, "/switch list page [#]", "You only have [total] of switches!", page_number, sender instanceof Player));
        } else
            sender.sendMessage(ChatColor.RED + "No one has made any switches yet!");
    }

    /** This method is called when <b><tt>sender</tt></b> uses the command <i>/switch info (owner's) (warp)</i>. This command displays all information pertaining to the
     * switch(es) specified. If a warp is designated, all switches linked to the designated warp will be specified and the information on all of them given; if no warp is
     * designated, the switch that <b><tt>sender</b></tt> is pointing at (which only works if <b><tt>sender</b></tt> is a Player) is specified and the information pertaining
     * to that specific switch will be displayed.
     * 
     * @param extra_param
     *            is equal to 0 if the command is used as one word (<i>/switchinfo</i>) or 1 if the command is used as two words (<i>/switch warp</i>).
     * @param sender
     *            is the Player or <tt>console</tt> who executed the command. */
    private void switchInfo(int extra_param, CommandSender sender, String[] parameters) {
        // sign post=63, wall sign=68, lever=69, stone pressure plate=70, wooden
        // pressure plate=72, stone button=77, wooden button = 143
        Player player = null;
        Block target_block = null;
        if (sender instanceof Player)
            player = (Player) sender;
        if (player != null)
            target_block = getTargetBlock(player, false);
        if (parameters.length > extra_param) {
            UltraWarp.getWarp(extra_param, sender, parameters);
            if (player == null || (player.getName().toLowerCase().startsWith(UWowner.toLowerCase())) || player.hasPermission("myultrawarps.switchinfo.other")
                    || player.hasPermission("myultrawarps.admin")) {
                // find all the switches linked to the specified warp
                ArrayList<UltraSwitch> temp = new ArrayList<UltraSwitch>();
                for (UltraSwitch my_switch : switches)
                    if (my_switch.warp_owner.equals(UWowner) && my_switch.warp_name.toLowerCase().startsWith(UWname.toLowerCase()))
                        temp.add(my_switch);
                if (temp.size() == 0) {
                    if (player == null)
                        console.sendMessage(COLOR + "There are no switches linked to " + UWowner + "'s warp \"" + UWname + ".\"");
                    else if (player.getName().equals(UWowner))
                        player.sendMessage(COLOR + "There are no switches linked to \"" + UWname + ".\"");
                    else
                        player.sendMessage(COLOR + "There are no switches linked to " + UWowner + "'s warp \"" + UWname + ".\"");
                } else if (temp.size() > 0) {
                    for (UltraSwitch my_switch : temp) {
                        if (player == null)
                            console.sendMessage(ChatColor.WHITE + colorCode(my_switch.toString()));
                        else
                            player.sendMessage(ChatColor.WHITE + colorCode(my_switch.toString()));
                    }
                }
            } else
                player.sendMessage(ChatColor.RED + "You don't have permission to see info on other people's switches.");
        } else if (target_block != null && UltraSwitch.getSwitchType(target_block) != null) {
            // get information by the switch the player is pointing at
            UltraSwitch switch_found = null;
            for (UltraSwitch my_switch : switches)
                if (my_switch.x == target_block.getX() && my_switch.y == target_block.getY() && my_switch.z == target_block.getZ()
                        && my_switch.world.equals(target_block.getWorld()) && my_switch.switch_type.equals(UltraSwitch.getSwitchType(target_block)))
                    switch_found = my_switch;
            if (switch_found != null
                    && (player == null || switch_found.warp_owner.equals(player.getName()) || player.hasPermission("myultrawarps.switchinfo.other") || player
                            .hasPermission("myultrawarps.admin")))
                if (player == null)
                    console.sendMessage(ChatColor.WHITE + switch_found.toString());
                else
                    player.sendMessage(ChatColor.WHITE + switch_found.toString());
            else if (switch_found == null)
                if (player == null)
                    console.sendMessage(COLOR + "There are no warps linked to this " + UltraSwitch.getSwitchType(target_block) + ".");
                else
                    player.sendMessage(COLOR + "There are no warps linked to this " + UltraSwitch.getSwitchType(target_block) + ".");
            else if (player != null)
                player.sendMessage(ChatColor.RED + "You don't have permission to see info on other people's switches.");
        } else if (!(sender instanceof Player))
            console.sendMessage(ChatColor.RED + "You must specify a warp for me to check if any switches are linked to it.");
        else if (player != null)
            player.sendMessage(ChatColor.RED + "You must either specify a warp for me to check or point at a switch for me to check.");
    }

    /** This method is called when <b><tt>sender</tt></b> uses the command <i>/to [player]</i>. <b><tt>Sender</b></tt> must be a Player for this method to be called. This
     * command teleports <b><tt>sender</b></tt> to the specified player.
     * 
     * @param sender
     *            is the Player or <tt>console</tt> who executed the command. */
    private void to(CommandSender sender, String[] parameters) {
        Player player = (Player) sender;
        // find the target player
        Player target_player = null;
        for (Player my_player : server.getOnlinePlayers())
            if (my_player.getName().toLowerCase().startsWith(parameters[0].toLowerCase()) && !my_player.equals(player))
                target_player = my_player;
        if (target_player == null) {
            // if the player designated themselves
            if (player.getName().toLowerCase().startsWith(parameters[0].toLowerCase()))
                player.sendMessage(ChatColor.RED + "You can't teleport to yourself! That makes no sense!");
            // if the designated player doesn't exist or isn't online
            else
                player.sendMessage(ChatColor.RED + "I couldn't find \"" + parameters[0] + "\" anywhere.");
            return;
        }
        // if player is blocked by target_player
        if (blocked_players.get(target_player.getName()) != null && blocked_players.get(target_player.getName()).contains(player.getName())
                && !player.hasPermission("myultrawarps.admin")) {
            player.sendMessage(ChatColor.RED + "Sorry, but " + target_player.getName() + " has blocked you. You can't send them teleportation requests anymore.");
            return;
        }
        // if the server has myScribe and target_player is a.f.k.
        if (server.getPluginManager().getPlugin("myScribe") != null && myScribe.AFK_players.contains(target_player.getName())) {
            player.sendMessage(ChatColor.RED + "Sorry, but " + target_player.getName() + " is a.f.k. right now. " + ChatColor.BLUE + "MyScribe" + ChatColor.RED
                    + " will tell you when they get back.");
            return;
        }
        // teleport them immdeiately if they have permission to bypass the request system or target_player already sent a /from request to them
        if (player.hasPermission("myultrawarps.to.norequest") || player.hasPermission("myultrawarps.admin")
                || (from_teleport_requests.get(player.getName()) != null && from_teleport_requests.get(player.getName()).contains(target_player.getName()))) {
            // remove any to teleportation requests from the target player
            ArrayList<String> requesting_players = from_teleport_requests.get(player.getName());
            if (requesting_players == null)
                requesting_players = new ArrayList<String>();
            while (requesting_players.contains(target_player.getName()))
                requesting_players.remove(target_player.getName());
            from_teleport_requests.put(player.getName(), requesting_players);
            if (teleport(player, new UltraWarp("God", "coordinates", false, false, "&aThis is the spot you were at before you teleported to " + target_player.getName() + ".",
                    "", null, player.getLocation()), new UltraWarp("God", "coordinates", false, false, "&aThis is the spot you were at when you teleported to "
                    + target_player.getName() + ".", "", null, target_player.getLocation()), false, target_player)) {
                if (player.getName().toLowerCase().startsWith("a") || player.getName().toLowerCase().startsWith("e") || player.getName().toLowerCase().startsWith("i")
                        || player.getName().toLowerCase().startsWith("o") || player.getName().toLowerCase().startsWith("u"))
                    player.sendMessage(COLOR + "You found an " + target_player.getName() + "!");
                else
                    player.sendMessage(COLOR + "You found a " + target_player.getName() + "!");
                target_player.sendMessage(COLOR + player.getName() + " has come to visit you.");
            }
        } // we already checked to make sure they had permission to use /to when the command was given; there is no need to check again
        else {
            // send a request to the target player
            player.sendMessage(COLOR + "Hang on. Let me ask " + target_player.getName() + " if it's okay.");
            target_player.sendMessage(COLOR + player.getName() + " would like to teleport to you. Is that okay?");
            ArrayList<String> requesting_players = to_teleport_requests.get(target_player.getName());
            if (requesting_players == null)
                requesting_players = new ArrayList<String>();
            requesting_players.add(player.getName());
            to_teleport_requests.put(target_player.getName(), requesting_players);
            // schedule reminders every 20 seconds until the request is cancelled after 60 seconds
            // server.getScheduler().scheduleSyncDelayedTask(this, new myUltraWarps$1(player, "follow through on /to request", target_player), 400);
        }
    }

    @SuppressWarnings("deprecation")
    private void top(CommandSender sender) {
        Player player = (Player) sender;
        boolean skipped_ceiling = true, passed_air = false;
        if (player.getLocation().getWorld().getWorldFolder().getName().endsWith("_nether"))
            skipped_ceiling = false;
        Short[] half_height_block_IDs = { 44, 93, 94, 96, 111, 126, 149, 150, 151 }, over_height_block_IDs = { 85, 107, 113 };
        for (float y = player.getWorld().getMaxHeight() - 1; y >= 0; y--) {
            Location location =
                    new Location(player.getLocation().getWorld(), player.getLocation().getBlockX() + 0.5, y, player.getLocation().getBlockZ() + 0.5, player.getLocation()
                            .getYaw(), player.getLocation().getPitch());
            // if the block is solid, then that's our destination (unless the Nether ceiling needs to be skipped)
            if (!contains(NON_SOLID_BLOCK_IDS, (short) location.getBlock().getTypeId()) || location.getBlock().isLiquid()) {
                if (skipped_ceiling && passed_air) {
                    // adjust the y based on the height of the block we're teleporting to
                    if (contains(half_height_block_IDs, (short) location.getBlock().getTypeId())
                    // make sure that if it's a slab, it is in the lower position
                            && (location.getBlock().getType() != Material.STEP || location.getBlock().getData() < 8))
                        y -= 0.5;
                    else if (contains(over_height_block_IDs, (short) location.getBlock().getTypeId()))
                        y += 0.5;
                    location.setY(y + 1.0);
                    teleport(player, null, new UltraWarp("&aYou've reached the top!", location), true, null);
                    return;
                } else if (!skipped_ceiling)
                    skipped_ceiling = true;
            } else if (skipped_ceiling)
                passed_air = true;
        }
        // if it gets here, then they must not have found any solid blocks!
        player.sendMessage(ChatColor.RED + "I can't find any solid blocks anywhere above or below you! What gives?");
    }

    private void unblock(CommandSender sender, String[] parameters) {
        Player player = (Player) sender;
        String blocked_player = autoCompleteName(parameters[0]);
        if (blocked_players.get(player.getName()) != null && blocked_players.get(player.getName()).contains(blocked_player)) {
            ArrayList<String> my_blocked_players = blocked_players.get(player.getName());
            my_blocked_players.remove(blocked_player);
            blocked_players.put(player.getName(), my_blocked_players);
        } else
            player.sendMessage(ChatColor.RED + "You never blocked " + blocked_player + ".");
    }

    private void unlinkWarp(int extra_param, CommandSender sender, String[] parameters) {
        Player player = null;
        if (sender instanceof Player)
            player = (Player) sender;
        Block target_block = null;
        if (player != null)
            target_block = getTargetBlock(player, false);
        if (parameters.length > extra_param) {
            // unlink all switches associated with a warp
            UltraWarp warp = UltraWarp.getWarp(extra_param, sender, parameters);
            if (warp.owner != null) {
                if (player == null || (player.getName().equals(warp.owner)) || player.hasPermission("myultrawarps.unlink.other") || player.hasPermission("myultrawarps.admin")) {
                    // locate the switches as specified and delete them
                    int number_of_switches_unlinked = 0;
                    for (int i = 0; i < switches.size(); i++) {
                        if (switches.get(i).warp_name.equals(warp.name) && switches.get(i).warp_owner.equals(warp.owner)) {
                            switches.remove(i);
                            i--;
                            number_of_switches_unlinked++;
                        }
                    }
                    if (autosave_switches)
                        saveTheSwitches(sender, false);
                    String full_warp_name = "\"" + warp.name + "\"";
                    if (player == null || !player.getName().equals(warp.owner))
                        full_warp_name = warp.owner + "'s \"" + warp.name + "\"";
                    if (number_of_switches_unlinked == 0)
                        sender.sendMessage(ChatColor.RED + "There are no switches linked to " + full_warp_name + "!");
                    else if (number_of_switches_unlinked == 1)
                        sender.sendMessage(COLOR + "I unlinked the one switch linked to " + full_warp_name + ".");
                    else
                        sender.sendMessage(COLOR + "I unlinked the " + number_of_switches_unlinked + " switches linked to " + full_warp_name + ".");
                } else
                    sender.sendMessage(ChatColor.RED + "You don't have permission to unlink other people's switches.");
            } else
                sender.sendMessage(ChatColor.RED + "You need to specify the owner's name. I can't look through your own warps! You're a console!");
        } else if (target_block != null && UltraSwitch.getSwitchType(target_block) != null) {
            if (player != null) {
                // unlink a single switch
                int index = -1;
                for (int i = 0; i < switches.size(); i++)
                    if (switches.get(i).location.equals(target_block.getLocation()) && switches.get(i).switch_type.equals(UltraSwitch.getSwitchType(target_block))) {
                        index = i;
                        break;
                    }
                if (index != -1
                        && (player.getName().equals(switches.get(index).warp_owner) || player.hasPermission("myultrawarps.unlink.other") || player
                                .hasPermission("myultrawarps.admin"))) {
                    if (player.getName().equals(switches.get(index).warp_owner))
                        sender.sendMessage(COLOR + "You unlinked \"" + switches.get(index).warp_name + "\" from this " + UltraSwitch.getSwitchType(target_block) + ".");
                    else
                        sender.sendMessage(COLOR + "You unlinked " + switches.get(index).warp_owner + "'s \"" + switches.get(index).warp_name + "\" from this "
                                + UltraSwitch.getSwitchType(target_block) + ".");
                    switches.remove(index);
                    if (autosave_switches)
                        saveTheSwitches(sender, false);
                } else if (index == -1)
                    sender.sendMessage(ChatColor.RED + "That " + UltraSwitch.getSwitchType(target_block) + " doesn't have a warp linked to it.");
                else
                    sender.sendMessage(ChatColor.RED + "You're not allowed to unlink other people's warps.");

            } else
                console.sendMessage(ChatColor.RED
                        + "You need to specify the warp that you want to unlink all switches from because you can't point out a specific switch to me...'cause you're a console!");
        } else if (player != null)
            sender.sendMessage(ChatColor.RED + "You can either point out a switch you want to unlink a warp from or specify a warp that I will unlink all switches from.");
        else
            sender.sendMessage(ChatColor.RED
                    + "You need to specify the warp that you want to unlink all switches from because you can't point out a specific switch to me...'cause you're a console!");
    }

    private void warp(CommandSender sender, String[] parameters) {
        Player player = (Player) sender;
        UltraWarp warp = UltraWarp.getWarp(0, sender, parameters);
        if (warp != null) {
            boolean listed = false;
            if (warp.listed_users != null && warp.listed_users.length > 0)
                for (String user : warp.listed_users)
                    if (player.getName().equalsIgnoreCase(user))
                        listed = true;
            if (warp.owner.equals(player.getName()) || player.hasPermission("myultrawarps.warptowarp.other") || player.hasPermission("myultrawarps.admin")
                    || (!warp.restricted && !listed) || (warp.restricted && listed)) {
                // save the player's location before warping
                String warp_name = warp.name;
                if (!warp.owner.equals(player.getName()))
                    warp_name = warp.owner + "'s " + warp.name;
                teleport(player, new UltraWarp("God", "coordinates", false, false, "&aThis is the spot you were at before you warped to " + warp_name + ".", "", null, player
                        .getLocation()), warp, true, null);
            } else
                player.sendMessage(colorCode(warp.no_warp_message.replaceAll("\\[player\\]", player.getName())));
        } else {
            // tell the player the warp wasn't found
            if (player.getName().equals(UWowner))
                player.sendMessage(ChatColor.RED + "I couldn't find \"" + UWname + ".\"");
            else
                player.sendMessage(ChatColor.RED + "I couldn't find \"" + UWname + "\" in " + UWowner + "'s warps.");
        }
    }

    private void warpAll(int extra_param, CommandSender sender, String[] parameters) {
        Player player = null;
        if (sender instanceof Player)
            player = (Player) sender;
        if (parameters[extra_param].equalsIgnoreCase("to"))
            extra_param++;
        if (parameters[extra_param].equalsIgnoreCase("here")) {
            if (player != null) {
                for (Player everyone : server.getOnlinePlayers()) {
                    if (player == null || !everyone.equals(player)) {
                        teleport(everyone, new UltraWarp(COLOR + "This is the spot you were at before " + player.getName() + " teleported everyone to them.", everyone
                                .getLocation()), new UltraWarp(COLOR + "This is the spot where " + player.getName() + " teleported everyone to them.", player.getLocation()),
                                false, player);
                        everyone.sendMessage(COLOR + player.getName() + " brought everyone to this location for something important.");
                    }
                }
                player.sendMessage(COLOR + "Everyone is present and accounted for.");
            } else
                console.sendMessage(ChatColor.RED + "I can't warp anyone to you! You have no location!");
        } else if (parameters[extra_param].equalsIgnoreCase("there")) {
            if (player != null) {
                Block target_block = getTargetBlock(player, true);
                if (target_block != null) {
                    Location target_location = target_block.getLocation();
                    target_location.setY(target_location.getY() + 1);
                    if (target_location.getBlock().getType() != Material.AIR) {
                        target_location.getChunk().load();
                        for (Player everyone : server.getOnlinePlayers()) {
                            if (player == null || !everyone.equals(player)) {
                                teleport(everyone, new UltraWarp(COLOR + "This is the spot you were at before " + player.getName() + " teleported everyone elsewhere.",
                                        everyone.getLocation()),
                                        new UltraWarp(COLOR + "This is the spot where " + player.getName() + " teleported everyone.", target_location), false, player);
                                everyone.sendMessage(COLOR + player.getName() + " brought everyone to this location for something important.");
                            }
                        }
                        player.sendMessage(COLOR + "Everyone is present and accounted for.");
                    } else
                        player.sendMessage(ChatColor.RED + "Sorry, but I can't see that far!");
                } else
                    player.sendMessage(ChatColor.RED + "Sorry, but I can't see that far!");
            } else
                console.sendMessage(ChatColor.RED + "Please point out the place you want to teleport everyone. Oh, yeah. You still can't. You're still a console.");
        } else if (parameters[extra_param].equalsIgnoreCase("warp")) {
            if (parameters.length >= extra_param + 2 && parameters[extra_param + 1] != null && !parameters[extra_param].equals("")) {
                UltraWarp warp = UltraWarp.getWarp(extra_param + 1, sender, parameters);
                if (warp != null) {
                    warp.location.getChunk().load();
                    for (Player everyone : server.getOnlinePlayers()) {
                        if (player == null || !everyone.equals(player)) {
                            String sender_name = "someone";
                            if (player != null)
                                sender_name = player.getName();
                            teleport(everyone, new UltraWarp(COLOR + "This is the spot you were at before " + sender_name + " teleported everyone elsewhere.", everyone
                                    .getLocation()), new UltraWarp(COLOR + "This is the spot where " + sender_name + " teleported everyone.", warp.location), false, sender);
                            if (sender_name.equals("someone"))
                                sender_name = "Someone";
                            everyone.sendMessage(COLOR + sender_name + " brought everyone to this location for something important.");
                        }
                    }
                    if (player != null && warp.owner.equals(player.getName()))
                        player.sendMessage(COLOR + "I sent everyone to \"" + warp.name + ".\"");
                    else
                        sender.sendMessage(COLOR + "I sent everyone to " + warp.owner + "'s \"" + warp.name + ".\"");
                } else if (player != null && player.getName().equals(UWowner))
                    player.sendMessage(ChatColor.RED + "I couldn't find \"" + UWname + ".\"");
                else if (UWowner != null)
                    sender.sendMessage(ChatColor.RED + "I couldn't find \"" + UWname + "\" in " + UWowner + "'s warps.");
                else
                    sender.sendMessage(ChatColor.RED + "I couldn't find \"" + UWname + ".\"");
            } else
                sender.sendMessage(ChatColor.RED + "You forgot to tell me what warp you want to warp everyone to!");
        } else if (parameters[extra_param].equalsIgnoreCase("player")) {
            if (parameters.length >= extra_param + 2 && parameters[extra_param + 1] != null && !parameters[extra_param].equals("")) {
                Player target_player = null;
                for (Player online_player : server.getOnlinePlayers()) {
                    if (online_player.getName().toLowerCase().startsWith(parameters[extra_param + 1].toLowerCase()))
                        target_player = online_player;
                }
                if (target_player != null) {
                    // you don't need to load the chunk because the target player is already there!
                    for (Player everyone : server.getOnlinePlayers()) {
                        if (player == null || !everyone.equals(player)) {
                            String sender_name = "someone";
                            if (player != null)
                                sender_name = player.getName();
                            teleport(everyone, new UltraWarp(COLOR + "This is the spot you were at before " + sender_name + " teleported everyone elsewhere.", everyone
                                    .getLocation()), new UltraWarp(COLOR + "This is the spot where " + sender_name + " teleported everyone.", target_player.getLocation()),
                                    false, sender);
                            if (sender_name.equals("someone"))
                                sender_name = "Someone";
                            everyone.sendMessage(COLOR + sender_name + " brought everyone to this location for something important.");
                        }
                    }
                    sender.sendMessage(COLOR + "I teleported everyone to " + target_player.getName() + ".");
                } else
                    sender.sendMessage(ChatColor.RED + "\"" + parameters[extra_param + 1] + "\" is not online right now.");
            } else
                sender.sendMessage(ChatColor.RED + "You forgot to tell me which player you want me to warp everyone to!");
        }
    }

    private void warpInfo(int extra_param, CommandSender sender, String[] parameters) {
        Player player = null;
        if (sender instanceof Player)
            player = (Player) sender;
        UltraWarp warp = UltraWarp.getWarp(extra_param, sender, parameters);
        if (warp != null
                && (player == null || player.getName().equals(UWowner) || player.hasPermission("myultrawarps.warpinfo.other") || player.hasPermission("myultrawarps.admin"))) {
            String info = warp.toString();
            // insert ChatColor.WHITE at the end of warp and no warp messages
            info =
                    info.replaceAll("\"" + warp.warp_message + "\"", "\"" + warp.warp_message + "&f\"").replaceAll("\"" + warp.no_warp_message + "\"",
                            "\"" + warp.no_warp_message + "&f\"");
            sender.sendMessage(ChatColor.WHITE + colorCode(info));
        } else if (warp != null)
            sender.sendMessage(ChatColor.RED + "You don't have permission to view information about this warp.");
        else {
            // tell the player the warp wasn't found
            if (player != null && player.getName().equals(UWowner) || UWowner == null)
                sender.sendMessage(ChatColor.RED + "I couldn't find \"" + UWname + ".\"");
            else
                sender.sendMessage(ChatColor.RED + "I couldn't find \"" + UWname + "\" in " + UWowner + "'s warps.");
        }
    }

    private void warpList(CommandSender sender, String[] parameters) {
        String player = null;
        if (sender instanceof Player)
            player = ((Player) sender).getName();
        int page_number = 1, extra_param = 0;
        // skip the "list" parameter if there is one
        if (parameters.length > 0 && parameters[0].equals("list"))
            extra_param++;
        // read (owner's) parameters
        boolean other_player_specified = false;
        if (parameters.length > extra_param && parameters[extra_param].endsWith("'s")) {
            if (sender instanceof Player && !sender.hasPermission("myultrawarps.list.other") && !sender.hasPermission("myultrawarps.list.full")
                    && !sender.hasPermission("myultrawarps.admin")) {
                sender.sendMessage(ChatColor.RED + "Sorry, but you're not allowed to see other people's warp lists.");
                return;
            }
            player = autoCompleteName(parameters[extra_param].substring(0, parameters[extra_param].length() - 2));
            if (player == null) {
                sender.sendMessage(ChatColor.RED + "I'm not sure who \"" + parameters[extra_param].substring(0, parameters[extra_param].length() - 2) + "\" is.");
                return;
            }
            other_player_specified = true;
            extra_param++;
        }
        // read page numbers
        if (parameters.length > extra_param) {
            try {
                page_number = Integer.parseInt(parameters[extra_param]);
                if (page_number <= 0) {
                    sender.sendMessage(ChatColor.RED + "Okay, page " + parameters[extra_param] + "? Really?");
                    return;
                }
            } catch (NumberFormatException exception) {
                sender.sendMessage(ChatColor.RED + "Since when is \"" + parameters[extra_param] + "\" an integer?");
                return;
            }
        }
        // compile the two warp lists
        String your_warps_output = null, listed_warps_output = null;
        for (UltraWarp warp : warps)
            if (player != null && warp.owner.equals(player))
                if (your_warps_output == null)
                    if (!other_player_specified)
                        your_warps_output = COLOR + "your warps: " + warp.getColoredName();
                    else
                        your_warps_output = COLOR + player + "'s warps: " + warp.getColoredName();
                else
                    your_warps_output = your_warps_output + ChatColor.WHITE + ", " + warp.getColoredName();
            else if (warp.listed && !other_player_specified)
                if (listed_warps_output == null)
                    if (player != null)
                        listed_warps_output = COLOR + "other listed warps: " + warp.getColoredName();
                    else
                        listed_warps_output = COLOR + "listed warps: " + warp.getColoredName();
                else
                    listed_warps_output = listed_warps_output + ChatColor.WHITE + ", " + warp.getColoredName();
        // if it wasn't the console using this command without specifying a player, format the your_warp_output
        if (your_warps_output == null)
            if (player != null)
                if (sender instanceof Player && !other_player_specified)
                    your_warps_output = ChatColor.RED + "You don't have any warps yet!";
                else
                    your_warps_output = ChatColor.RED + player + " doesn't have any warps yet!";
            else
                your_warps_output = "";
        // if another player wasn't specified, format the listed_warps_output
        if (listed_warps_output == null)
            if (!other_player_specified) {
                listed_warps_output = ChatColor.RED + "No one else has any listed warps yet!";
                if (!(sender instanceof Player) || sender.hasPermission("myultrawarps.list.full"))
                    listed_warps_output +=
                            "\nIf you would like to see a complete list of warps on the server, use " + ChatColor.ITALIC + "/full warp list" + ChatColor.RED + ".";
            } else
                listed_warps_output = "";
        // add a line break between the two parts if both parts have information to give
        if (!your_warps_output.equals("") && !listed_warps_output.equals(""))
            your_warps_output += "\n";
        String command_format = "/warp list [#]", not_enough_pages_message = "You only have [total] of warps.";
        if (other_player_specified) {
            command_format = "/warp list " + player + "'s [#]";
            not_enough_pages_message = player + " only has [total] of warps.";
        }
        sender.sendMessage(paginate(your_warps_output + listed_warps_output, command_format, not_enough_pages_message, page_number, sender instanceof Player));
    }

    private void warpToCoordinate(CommandSender sender, String[] parameters) {
        // warp (world) [x] [y] [z] (world)
        Player player = (Player) sender;
        double x = 0, y = 0, z = 0;
        String world_name = "";
        World world = null;
        int extra_param = 0;
        boolean fail = true;
        while (fail && extra_param + 2 < parameters.length) {
            fail = false;
            try {
                x = Double.parseDouble(parameters[extra_param]);
                y = Double.parseDouble(parameters[extra_param + 1]);
                z = Double.parseDouble(parameters[extra_param + 2]);
            } catch (NumberFormatException exception) {
                fail = true;
                if (!world_name.equals(""))
                    world_name = world_name + " " + parameters[extra_param];
                else
                    world_name = parameters[extra_param];
                extra_param++;
            }
        }
        // don't let anyone teleport past +-2,000,000 x or z
        if (Math.abs(x) >= 2000000 || Math.abs(z) >= 2000000) {
            sender.sendMessage(ChatColor.RED + "Sorry, but you're not allowed to warp past 2,000,000 blocks from the center of the map.");
            return;
        }
        if (world_name.equals("") && parameters.length >= 4) {
            for (int i = 3; i < parameters.length; i++)
                world_name = world_name + parameters[i];
        }
        if (!world_name.equals("")) {
            for (World my_world : server.getWorlds())
                if (my_world.getWorldFolder().getName().toLowerCase().startsWith(world_name.toLowerCase()))
                    world = my_world;
        } else
            world = player.getWorld();
        if (!fail && world != null) {
            String message = COLOR + "Welcome to (" + Math.round(x) + ", " + Math.round(y) + ", " + Math.round(z) + ").";
            if (!world.equals(player.getWorld())) {
                world_name = world.getWorldFolder().getName();
                if (world_name.endsWith("_nether"))
                    world_name = "The Nether";
                else if (world_name.endsWith("_the_end"))
                    world_name = "The End";
                message = COLOR + "&aWelcome to (" + Math.round(x) + ", " + Math.round(y) + ", " + Math.round(z) + ") in \"" + world_name + ".\"";
            }
            // save the player's current location before warping
            teleport(player, new UltraWarp("&aThis is the spot you were at before you warped to (" + Math.round(x) + ", " + Math.round(y) + ", " + Math.round(z) + ")", player
                    .getLocation()), new UltraWarp(message, new Location(world, (int) x + 0.5, (int) y, (int) z + 0.5, player.getLocation().getYaw(), player.getLocation()
                    .getPitch())), true, null);
        } else if (world == null) {
            // if it couldn't find the specified world
            player.sendMessage(ChatColor.RED + "The world you specified doesn't exist.");
            if (server.getWorlds().size() == 1)
                player.sendMessage(ChatColor.RED + "Your server only has one world called \"" + server.getWorlds().get(0).getWorldFolder().getName() + ".\"");
            else if (server.getWorlds().size() == 2)
                player.sendMessage(ChatColor.RED + "Your server has two worlds: \"" + server.getWorlds().get(0).getWorldFolder().getName() + "\" and \""
                        + server.getWorlds().get(1).getWorldFolder().getName() + ".\"");
            else if (server.getWorlds().size() > 2) {
                String message = ChatColor.RED + "Your server has " + server.getWorlds().size() + " worlds: ";
                for (int i = 0; i < server.getWorlds().size(); i++) {
                    if (i < server.getWorlds().size() - 1)
                        message = message + "\"" + server.getWorlds().get(i).getWorldFolder().getName() + "\", ";
                    else
                        message = message + " and \"" + server.getWorlds().get(i).getWorldFolder().getName() + ".\"";
                }
                player.sendMessage(message);
            }
        } else if (fail) {
            player.sendMessage(ChatColor.RED + "Either you put too many parameters in for /warp or you put a letter in one of your coordinates.");
        } else
            player.sendMessage(ChatColor.RED + "I couldn't find \"" + world_name + ".\"");
    }

    private void warpsAround(int extra_param, CommandSender sender, String[] parameters) {
        Player player = null;
        if (sender instanceof Player)
            player = (Player) sender;
        int radius = 20;
        Location target_location = null;
        String target_name = null;
        if (parameters[extra_param].equalsIgnoreCase("player")) {
            extra_param++;
            if (parameters.length <= extra_param)
                sender.sendMessage(ChatColor.RED + "You forgot which player you want the search centered around!");
            else {
                for (Player target_player : server.getOnlinePlayers())
                    if (target_player.getName().toLowerCase().startsWith(parameters[extra_param].toLowerCase())) {
                        target_name = target_player.getName();
                        target_location = target_player.getLocation();
                        break;
                    }
                if (target_location == null)
                    sender.sendMessage(ChatColor.RED + "\"" + parameters[extra_param] + "\" is nowhere to be found.");
            }
        } else if (parameters[extra_param].equalsIgnoreCase("warp")) {
            extra_param++;
            if (parameters.length > extra_param && parameters[extra_param].toLowerCase().endsWith("'s"))
                extra_param++;
            if (parameters.length <= extra_param)
                sender.sendMessage(ChatColor.RED + "You forgot which warp you want the search centered around!");
            else {
                UltraWarp warp = UltraWarp.getWarp(extra_param, sender, parameters);
                if (warp != null) {
                    target_location = new Location(warp.location.getWorld(), warp.location.getX(), warp.location.getY(), warp.location.getZ());
                    if (player != null && UWowner.equals(player.getName()))
                        target_name = "\"" + warp.name + "\"";
                    else
                        target_name = warp.owner + "'s \"" + warp.name + "\"";
                } else if (player != null && player.getName().equals(UWowner))
                    sender.sendMessage(ChatColor.RED + "Sorry, but I couldn't find \"" + UWname + ".\"");
                else
                    sender.sendMessage(ChatColor.RED + "Sorry, but I couldn't find " + UWowner + "'s \"" + UWname + ".\"");
            }
        } else if (parameters[extra_param].equalsIgnoreCase("there")) {
            target_name = "that spot";
            if (player == null) {
                sender.sendMessage(ChatColor.RED
                        + "You want the search centered there? Where? Oh, wait. You're still a console and you still have no fingers to point out a location with.");
                return;
            } else {
                Block target_block = getTargetBlock(player, true);
                if (target_block == null) {
                    sender.sendMessage(ChatColor.RED + "Sorry, but I can't see that far!");
                    return;
                }
                target_location = target_block.getLocation();
            }
        } else if (parameters[extra_param].equalsIgnoreCase("here") || parameters[extra_param].equalsIgnoreCase("me")) {
            target_name = "you";
            if (player == null)
                sender.sendMessage(ChatColor.RED + "You have no body. How can I center a search around you, huh? Silly...");
            else
                target_location = player.getLocation();
        } else
            sender.sendMessage(ChatColor.RED + "I don't understand what \"" + parameters[extra_param] + "\" means.");
        if (target_location != null) {
            if (parameters.length > extra_param + 1) {
                try {
                    radius = Integer.parseInt(parameters[extra_param + 1]);
                } catch (NumberFormatException exception) {
                    sender.sendMessage(ChatColor.RED + "Since when is \"" + parameters[extra_param + 1] + "\" an integer?");
                    return;
                }
            }
            ArrayList<UltraWarp> nearby_warps = new ArrayList<UltraWarp>();
            for (UltraWarp warp : warps)
                if (warp.location.getX() >= target_location.getBlockX() - radius && warp.location.getX() <= target_location.getBlockX() + radius
                        && warp.location.getY() >= target_location.getBlockY() - radius && warp.location.getY() <= target_location.getBlockY() + radius
                        && warp.location.getZ() >= target_location.getBlockZ() - radius && warp.location.getZ() <= target_location.getBlockZ() + radius
                        && warp.location.getWorld().equals(target_location.getWorld()))
                    nearby_warps.add(warp);
            if (nearby_warps.size() > 0) {
                String output = COLOR + "There are " + nearby_warps.size() + " warps within " + radius + " blocks of " + target_name + ": ";
                if (nearby_warps.size() == 1)
                    output = COLOR + "There is one warp within " + radius + " blocks of " + target_name + ": ";
                for (UltraWarp warp : nearby_warps) {
                    if (!output.endsWith(": "))
                        output = output + ChatColor.WHITE + ", ";
                    output = output + warp.getColoredOwner() + "'s " + warp.name;
                }
                sender.sendMessage(output);
            } else
                sender.sendMessage(ChatColor.RED + "There are no warps within " + radius + " blocks of " + target_name + ".");
        }
    }
}
