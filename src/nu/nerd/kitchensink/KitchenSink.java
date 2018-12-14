package nu.nerd.kitchensink;

import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import nu.nerd.kitchensink.ServerListPing17.StatusResponse;
import org.bukkit.Art;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Note;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Zombie;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.ShapelessRecipe;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitScheduler;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.BlockIterator;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

public class KitchenSink extends JavaPlugin {

    private static final int ONE_MINUTE_TICKS = 60 * 20;

    private final KitchenSinkListener listener = new KitchenSinkListener(this);
    private final LagCheck lagCheck = new LagCheck();
    public final Configuration config = new Configuration(this);
    public final List<Recipe> recipeList = new ArrayList<>();
    public int countdownTime = 0;
    public long countdownTicks = 0;
    public String countdownMessage = "";
    public boolean countdownActive = false;
    public BukkitTask countdownTask;
    public long nextRestart = -1;

    /**
     * Location of the next portal to be created. The "safe-portals" setting can
     * be bypassed by an admin looking at one of the blocks that constitutes a
     * portal frame (except the corners) and running /allow-portal.
     */
    public Location nextPortal;

    /**
     * Key of Player metadata used to record most recently selected painting.
     */
    public static final String PAINTING_META_KEY = "KitchenSink.painting";

    /**
     * Key of Player metadata set to signify that the next right click on a
     * horse is an attempted lock/unlock. Value is the new boolean lock state.
     */
    public static final String HORSE_DO_LOCK_KEY = "KitchenSink.do_lock";

    /**
     * Key of Horse metadata set to signify that the horse is unlocked for
     * riding by players other than the owner. If absent, horse is locked.
     */
    public static final String HORSE_UNLOCKED_KEY = "KitchenSink.unlocked";

    /**
     * Key of Player metadata set to signify that the next right click on a
     * Tameable mob owned by the player will un-tame the mob.
     */
    public static final String UNTAME_KEY = "KitchenSink.untame";

    /**
     * The name of the subdirectory of the KitchenSink data directory containing
     * host keys. Each user has a separate file named after them containing the
     * prefix of their host key, without the server name suffix.
     * http://www.sk89q.com/
     * 2012/07/fixing-the-minecraft-session-stealer-exploit/
     */
    public static final String HOST_KEYS_DIRECTORY = "hostkeys";

    /**
     * Key of Player metadata which, when set, indicates that the next punch of
     * a noteblock by a player should change the note of the noteblock.
     */
    public static final String NOTEBLOCK_META_KEY = "KitchenSink.noteblock";

    /**
     * Map from lower case in-game enchantment names to the Bukkit Enchantment
     * instance.
     *
     * Also maps the internal Bukkit Enchantment names in lower case.
     */
    private static final TreeMap<String, Enchantment> ENCHANTMENT_NAMES = new TreeMap<>();

    static {
        // Internal names.
        for (Enchantment enchant : Enchantment.values()) {
            ENCHANTMENT_NAMES.put(enchant.getKey().getNamespace().toLowerCase(), enchant);
        }

        // Common, in-game names.
        ENCHANTMENT_NAMES.put("power", Enchantment.ARROW_DAMAGE);
        ENCHANTMENT_NAMES.put("flame", Enchantment.ARROW_FIRE);
        ENCHANTMENT_NAMES.put("infinity", Enchantment.ARROW_INFINITE);
        ENCHANTMENT_NAMES.put("punch", Enchantment.ARROW_KNOCKBACK);
        ENCHANTMENT_NAMES.put("sharpness", Enchantment.DAMAGE_ALL);
        ENCHANTMENT_NAMES.put("bane_of_arthropods", Enchantment.DAMAGE_ARTHROPODS);
        ENCHANTMENT_NAMES.put("smite", Enchantment.DAMAGE_UNDEAD);
        ENCHANTMENT_NAMES.put("haste", Enchantment.DIG_SPEED);
        ENCHANTMENT_NAMES.put("unbreaking", Enchantment.DURABILITY);
        ENCHANTMENT_NAMES.put("fortune", Enchantment.LOOT_BONUS_BLOCKS);
        ENCHANTMENT_NAMES.put("looting", Enchantment.LOOT_BONUS_MOBS);
        ENCHANTMENT_NAMES.put("respiration", Enchantment.OXYGEN);
        ENCHANTMENT_NAMES.put("protection", Enchantment.PROTECTION_ENVIRONMENTAL);
        ENCHANTMENT_NAMES.put("blast_protection", Enchantment.PROTECTION_EXPLOSIONS);
        ENCHANTMENT_NAMES.put("feather_falling", Enchantment.PROTECTION_FALL);
        ENCHANTMENT_NAMES.put("fire_protection", Enchantment.PROTECTION_FIRE);
        ENCHANTMENT_NAMES.put("projectile_protection", Enchantment.PROTECTION_PROJECTILE);
        ENCHANTMENT_NAMES.put("aqua_affinity", Enchantment.WATER_WORKER);
    }

    /**
     * Return the enchantment whose internal or common name exactly matches
     * prefix, or else the first enchantment that begins with prefix if there is
     * no exact match.
     *
     * @param prefix the start of the enchantment name.
     * @return the corresponding Enchantment.
     */
    private static Enchantment getEnchantment(String prefix) {
        prefix = prefix.toLowerCase();
        Enchantment enchantment = ENCHANTMENT_NAMES.get(prefix);
        if (enchantment == null) {
            // Map entries with key >= prefix.
            SortedMap<String, Enchantment> tail = ENCHANTMENT_NAMES.tailMap(prefix);
            if (tail.firstKey().startsWith(prefix)) {
                // Return the first prefix match.
                return tail.get(tail.firstKey());
            }
        }
        return enchantment;
    }

    @Override
    public void onDisable() {
        getServer().getScheduler().cancelTasks(this);

        Iterator<Recipe> recipeIterator = getServer().recipeIterator();
        while (recipeIterator.hasNext()) {
            Recipe r = recipeIterator.next();
            if (recipeList.contains(r)) {
                recipeIterator.remove();
                recipeList.remove(r);
            }
        }

        if (config.BUNGEE_DISCONNECT_ON_RESTART) {
            for (Player player : getServer().getOnlinePlayers()) {
                proxyKick(player);
            }
        }

    }

    @Override
    public void onEnable() {
        File config_file = new File(getDataFolder(), "config.yml");
        if (!config_file.exists()) {
            getConfig().options().copyDefaults(true);
            saveConfig();
        }

        config.load();

        if (config.ANIMAL_COUNT) {
            final BukkitScheduler sched = getServer().getScheduler();
            Runnable task = () -> {
                System.out.println("-!- Starting Mob count");
                System.out.println("-!- " + getMultiworldMobCount());
            };
            sched.runTaskTimerAsynchronously(this, task, ONE_MINUTE_TICKS, 10 * ONE_MINUTE_TICKS);
        }

        if (config.CULL_ZOMBIES) {
            final BukkitScheduler sched = getServer().getScheduler();
            Runnable task = () -> {
                try {
                    Collection<LivingEntity> livingEntities = getServer().getWorlds().get(0).getEntitiesByClass(LivingEntity.class);
                    for (LivingEntity mob : livingEntities) {
                        if (mob.getType() == EntityType.ZOMBIE) {
                            Zombie zombie = (Zombie) mob;
                            if (zombie.getTarget() != null
                                && zombie.getTarget().getType() == EntityType.VILLAGER
                                && zombie.getRemoveWhenFarAway()) {
                                zombie.remove();
                            }
                        }
                    }
                } catch (Exception ex) {
                }
            };
            sched.runTaskTimerAsynchronously(this, task, config.CULL_ZOMBIES_INTERVAL, config.CULL_ZOMBIES_INTERVAL);
        }

        if (config.LEATHERLESS_BOOKS) {
            ShapelessRecipe cheapBook = new ShapelessRecipe(new ItemStack(Material.BOOK));
            cheapBook.addIngredient(3, Material.PAPER);
            getServer().addRecipe(cheapBook);
            recipeList.add(cheapBook);
        }

        if (!config.BLOCK_CRAFT.isEmpty()) {
            Iterator<Recipe> it = getServer().recipeIterator();
            while (it.hasNext()) {
                Recipe recipe = it.next();
                for (Material material : config.BLOCK_CRAFT) {
                    if (recipe != null && recipe.getResult().getType() == material) {
                        it.remove();
                    }
                }
            }
        }

        SimpleDateFormat dateFormat = new SimpleDateFormat("HH-mm");

        for (String s : config.RESTART_TIMES) {
            try {
                Calendar cal = Calendar.getInstance();
                cal.set(Calendar.HOUR_OF_DAY, 0);
                cal.set(Calendar.MINUTE, 0);
                cal.set(Calendar.SECOND, 0);
                cal.set(Calendar.MILLISECOND, 0);

                Date parsed = dateFormat.parse(s);
                Calendar parsedCal = Calendar.getInstance();
                parsedCal.setTime(parsed);

                cal.add(Calendar.HOUR_OF_DAY, parsedCal.get(Calendar.HOUR_OF_DAY));
                cal.add(Calendar.MINUTE, parsedCal.get(Calendar.MINUTE));
                cal.add(Calendar.SECOND, parsedCal.get(Calendar.SECOND));
                cal.add(Calendar.MILLISECOND, parsedCal.get(Calendar.MILLISECOND));

                long now = System.currentTimeMillis();

                if (now > cal.getTimeInMillis()) {
                    cal.add(Calendar.DATE, 1);
                }

                long calMillis = cal.getTimeInMillis();
                if (nextRestart == -1 || nextRestart > calMillis) {
                    nextRestart = calMillis;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        getServer().getScheduler().scheduleSyncRepeatingTask(this, lagCheck, 20, 20);
        getServer().getPluginManager().registerEvents(listener, this);

    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String name, String[] args) {
        if (command.getName().equalsIgnoreCase("ksreload")) {
            config.load();
            getLogger().info("Config Reloaded!");
            sender.sendMessage(ChatColor.LIGHT_PURPLE + "[KitchenSink] Config Reloaded!");
        }
        if (command.getName().equalsIgnoreCase("unenchant") && sender instanceof Player) {
            Player player = (Player) sender;
            PlayerInventory inv = player.getInventory();
            try {
                if (inv.getItemInMainHand().getType().equals(Material.ENCHANTED_BOOK)) {
                    inv.setItemInMainHand(new ItemStack(Material.BOOK));
                } else {
                    for (Enchantment e : inv.getItemInMainHand().getEnchantments().keySet()) {
                        inv.getItemInMainHand().removeEnchantment(e);
                    }
                }
                player.sendMessage("Enchantments removed.");
            } catch (Exception e) {
                player.sendMessage("No enchantments removed.");
            }
            return true;
        }
        if (command.getName().equalsIgnoreCase("lag")) {
            if (sender.hasPermission("kitchensink.lag")) {
                sendLagStats(sender);
                return true;
            }
        }
        if (command.getName().equalsIgnoreCase("list")) {
            if (sender.hasPermission("kitchensink.list")) {
                sendList(sender);
                return true;
            }
        }
        if (command.getName().equalsIgnoreCase("ksinventory")) {
            if (args.length >= 1) {
                Player clearee = getServer().getPlayer(args[0]);
                if (args.length == 2) {
                    if (args[1].equals("clear")) {
                        clearee.getInventory().clear();
                        clearee.getInventory().setArmorContents(new ItemStack[clearee.getInventory().getArmorContents().length]);
                        clearee.saveData();
                        sender.sendMessage("Inventory Cleared.");
                        return true;
                    }
                }
                if (clearee != null && sender instanceof Player) {
                    ((Player) sender).openInventory(clearee.getPlayer().getInventory());
                }
                return true;
            }
        }
        if (command.getName().equalsIgnoreCase("painting")) {
            // No arguments ==> list all painting types.
            if (args.length == 0) {
                StringBuilder message = new StringBuilder();
                message.append(ChatColor.GOLD);
                message.append("Available paintings: ");
                for (int i = 0; i < Art.values().length; ++i) {
                    Art art = Art.values()[i];
                    message.append(ChatColor.YELLOW);
                    message.append(art.name().toLowerCase());
                    message.append(ChatColor.GRAY);
                    message.append(" (");
                    message.append(art.getBlockWidth());
                    message.append('x');
                    message.append(art.getBlockHeight());
                    message.append(")");
                    if (i < Art.values().length - 1) {
                        message.append(", ");
                    }
                }
                sender.sendMessage(message.toString());
                return true;
            } else if (args.length == 1) {
                if (sender instanceof Player) {
                    try {
                        Player player = (Player) sender;
                        Art art = Art.getByName(args[0]);
                        player.setMetadata("KitchenSink.painting", new FixedMetadataValue(this, art));
                        sender.sendMessage(ChatColor.GOLD + "The next painting you place will be: "
                                           + ChatColor.YELLOW + art.name().toLowerCase());
                    } catch (Exception ex) {
                        sender.sendMessage(ChatColor.RED + "Unknown painting: " + args[0]);
                    }
                } else {
                    sender.sendMessage("You need to be in-game to place paintings.");
                }
                return true;
            }
        }

        if (command.getName().equalsIgnoreCase("nextrestart")) {
            int time = (int) ((nextRestart - System.currentTimeMillis()) / 1000L);

            if (time < 120) {
                sender.sendMessage("The server will restart in " + time + " second" + ((time == 1) ? "" : "s"));
            } else {
                sender.sendMessage("The server will restart in " + time / 60 + " minute" + ((time == 1) ? "" : "s"));
            }

            return true;
        }

        if (command.getName().equalsIgnoreCase("lock-horse")) {
            setHorseLockState(sender, true);
            return true;
        } else if (command.getName().equalsIgnoreCase("unlock-horse")) {
            setHorseLockState(sender, false);
            return true;
        } else if (command.getName().equalsIgnoreCase("untame")) {
            if (config.UNTAME_PETS) {
                if (sender instanceof Player) {
                    Player player = (Player) sender;
                    player.setMetadata(UNTAME_KEY, new FixedMetadataValue(this, null));
                    sender.sendMessage(ChatColor.GOLD + "Right click on a pet that you own.");
                } else {
                    sender.sendMessage("You need to be in-game to untame pets.");
                }
            } else {
                sender.sendMessage(ChatColor.RED + "That command is disabled.");
            }
            return true;
        }

        if (command.getName().equalsIgnoreCase("allow-portal")) {
            // Running /allow-portal while looking at some invalid location
            // invalidates the previously set allowed portal location:
            nextPortal = null;
            if (sender instanceof Player) {
                if (config.SAFE_PORTALS) {
                    Player player = (Player) sender;
                    Block block = getTargetBlock(player);
                    if (block != null && block.getType() == Material.OBSIDIAN) {
                        nextPortal = block.getLocation();
                        sender.sendMessage(
                        String.format("%sYou can now light a single portal containing the block at (%d, %d, %d).",
                            ChatColor.GOLD.toString(), nextPortal.getBlockX(), nextPortal.getBlockY(), nextPortal.getBlockZ()));
                    } else {
                        sender.sendMessage(ChatColor.RED + "You need to be looking at the non-corner parts of an obsidian portal frame.");
                    }
                } else {
                    sender.sendMessage(ChatColor.RED + "Safe portals are not enabled. Anybody can light portals.");
                }
            } else {
                sender.sendMessage(ChatColor.RED + "You need to be in-game to run that.");
            }
            return true;
        }

        if (command.getName().equalsIgnoreCase("mob-count")) {
            if (args.length == 0) {
                if (!(sender instanceof Player)) {
                    sender.sendMessage(getMobCount(null).toString());
                } else {
                    sender.sendMessage(getMobCount(((Player) sender).getWorld()).toString());
                }
                return true;
            } else if (args.length == 1 && args[0].equalsIgnoreCase("dump")) {
                sender.sendMessage("Dumping mobs");
                dumpMobCount();
                return true;
            } else if (args.length == 1 && args[0].equalsIgnoreCase("all")) {
                sender.sendMessage(getMultiworldMobCount().toString());
                return true;
            }

            return false;
        }

        // Unfortunately, many plugins don't correctly apply enchantments to
        // enchanted books ("enchantment holders").
        if (command.getName().equalsIgnoreCase("enchant-book") && sender instanceof Player) {
            Player player = (Player) sender;

            if (args.length != 2) {
                player.sendMessage(ChatColor.RED + "Usage: /enchant-book <enchantment> <level>");
                return true;
            }

            ItemStack item = player.getInventory().getItemInMainHand();
            if (item.getType() != Material.BOOK && item.getType() != Material.ENCHANTED_BOOK) {
                player.sendMessage(ChatColor.RED + "You must be holding a book or enchanted book.");
                return true;
            }

            if (item.getAmount() > 1) {
                player.sendMessage(ChatColor.RED + "You can only enchant one book at a time.");
                return true;
            }

            Enchantment enchantment = getEnchantment(args[0]);
            if (enchantment == null) {
                player.sendMessage(ChatColor.RED + args[0] + " is not a valid enchantment.");
                return true;
            }

            int level;
            try {
                level = Integer.parseInt(args[1]);
                if (level < 1) {
                    player.sendMessage(ChatColor.RED + "The level must be 1 or higher.");
                    return true;
                }
            } catch (Exception ex) {
                player.sendMessage(ChatColor.RED + args[1] + " is not an integer.");
                return true;
            }

            // If holding an unenchanted book, replace with enchanted book.
            if (item.getType() == Material.BOOK) {
                item.setType(Material.ENCHANTED_BOOK);
            }

            EnchantmentStorageMeta meta = (EnchantmentStorageMeta) item.getItemMeta();
            meta.addStoredEnchant(enchantment, level, true);
            item.setItemMeta(meta);
            player.getInventory().setItemInMainHand(item);

            // Log successful enchants.
            getLogger().info(sender.getName() + " enchanted " + item.getAmount() + " book: " + enchantment.getKey().getNamespace() + " " + level);
            return true;
        } // /enchant-book

        if (command.getName().equalsIgnoreCase("ping-server")) {
            String host;
            int port = 25565;
            String parameter = "combined";
            String format = null;

            if (args.length >= 1) {
                if (args[0].contains(":")) {
                    String[] host_parts = args[0].split(":");
                    host = host_parts[0];
                    port = Integer.parseInt(host_parts[1]);
                } else {
                    host = args[0];
                }

                if (args.length >= 2) {
                    parameter = args[1];
                }

                ServerListPing17 pinger = new ServerListPing17();
                pinger.setAddress(host, port);

                StatusResponse sr = null;
                try {
                    if (parameter.equalsIgnoreCase("combined")) {
                        format = "%(DESCRIPTION) | %(PLAYERS_ONLINE) of %(PLAYERS_MAX) players online";
                    } else if (parameter.equalsIgnoreCase("description")) {
                        format = "%(DESCRIPTION)";
                    } else if (parameter.equalsIgnoreCase("players_online") || parameter.equalsIgnoreCase("online")) {
                        format = "%(PLAYERS_ONLINE)";
                    } else if (parameter.equalsIgnoreCase("players_max") || parameter.equalsIgnoreCase("max")) {
                        format = "%(PLAYERS_MAX)";
                    } else if (parameter.equalsIgnoreCase("version_name")) {
                        format = "%(VERSION_NAME)";
                    } else if (parameter.equalsIgnoreCase("version_protocol")) {
                        format = "%(VERSION_PROTOCOL)";
                    } else if (parameter.equalsIgnoreCase("custom") && args.length >= 3) {
                        StringBuilder sb = new StringBuilder();
                        for (int i = 2; i < args.length; i++) {
                            sb.append(args[i]);
                            if (i < args.length - 1) {
                                sb.append(" ");
                            }
                        }
                        format = sb.toString();
                    } else {
                        return false;
                    }

                    sr = pinger.fetchData();
                } catch (Exception e) {
                    sender.sendMessage("Unable to ping server");
                }

                try {
                    Hashtable<String, Object> parameters = new Hashtable<>();
                    parameters.put("DESCRIPTION", sr.getDescription());
                    parameters.put("PLAYERS_ONLINE", Integer.toString(sr.getPlayers().getOnline()));
                    parameters.put("PLAYERS_MAX", Integer.toString(sr.getPlayers().getMax()));
                    parameters.put("VERSION_NAME", sr.getVersion().getName());
                    parameters.put("VERSION_PROTOCOL", sr.getVersion().getProtocol());

                    sender.sendMessage(this.dictFormat(format, parameters));
                } catch (Exception e) {
                    sender.sendMessage("Invalid format string: \"" + format + "\"");
                }

                return true;
            }
        }

        if (command.getName().equalsIgnoreCase("countdown")) {
            if (sender.hasPermission("kitchensink.countdown")) {
                if (countdownActive) {
                    sender.sendMessage(ChatColor.GRAY + "A countdown is already in progress.");
                    return true;
                } else if (args.length > 0) {
                    List<String> Args = new ArrayList<>(Arrays.asList(args));
                    try {
                        countdownTime = Integer.parseInt(Args.get(0));
                        if (countdownTime > config.COUNTDOWN_MAX_TIME) {
                            countdownTime = config.COUNTDOWN_MAX_TIME;
                        }
                        Args.remove(0);
                    } catch (NumberFormatException e) {
                        countdownTime = 10;
                    }
                    countdownTicks = countdownTime * 20;

                    if (!Args.isEmpty()) {
                        for (String arg : Args) {
                            countdownMessage += arg + " ";
                        }
                    } else {
                        countdownMessage = "Go!";
                    }

                    countdownActive = true;
                    countdownTask = this.getServer().getScheduler().runTaskTimer(this, () -> {
                        if (countdownTime > 0) {
                            getServer().broadcastMessage(
                                ChatColor.translateAlternateColorCodes('&', config.COUNTDOWN_COLOR + config.COUNTDOWN_STYLE)
                                + config.COUNTDOWN_FORMAT.split("\\$s")[0] + countdownTime
                                + (config.COUNTDOWN_FORMAT.split("\\$s").length > 1 ? config.COUNTDOWN_FORMAT.split("\\$s")[1] : ""));
                            countdownTime--;
                        } else {
                            countdownActive = false;
                            getServer().broadcastMessage(
                                ChatColor.translateAlternateColorCodes('&', config.COUNTDOWN_MSG_COLOR + config.COUNTDOWN_MSG_STYLE)
                                + countdownMessage);
                            countdownMessage = "";
                            countdownTask.cancel();
                            countdownTask = null;
                        }
                    }, 0L, 20L);
                    return true;
                }
            }
        }

        if (command.getName().equalsIgnoreCase("cdcancel")) {
            if (sender.hasPermission("kitchensink.countdown")) {
                if (countdownTask != null) {
                    countdownTask.cancel();
                    countdownActive = false;
                    countdownMessage = "";
                    countdownTask = null;
                    sender.sendMessage(ChatColor.GRAY + "Countdown has been cancelled.");
                } else {
                    sender.sendMessage(ChatColor.GRAY + "No cooldowns are active.");
                }
                return true;
            }
        }

        if (command.getName().equalsIgnoreCase("cdconfig")) {
            if (sender.hasPermission("kitchensink.admin")) {
                if (args.length > 0) {
                    switch (args[0].toLowerCase()) {
                    case "maxtime":
                        if (args.length < 2) {
                            sender.sendMessage(ChatColor.RED + "Usage: /cdconfig maxTime <seconds>");
                            return true;
                        }
                        try {
                            config.setCountDownSetting(countdown.maxtime, Math.abs(Integer.parseInt(args[1])));
                            sender.sendMessage(ChatColor.GRAY + "Countdown maxTime has been set to: " + config.COUNTDOWN_MAX_TIME + " seconds.");
                        } catch (NumberFormatException e) {
                            sender.sendMessage(ChatColor.GRAY + "You must provide a number of seconds when attempting to set maxTime.");
                            return false;
                        }
                        return true;
                    case "format":
                        if (args.length < 2) {
                            sender.sendMessage(ChatColor.RED + "Usage: /cdconfig format __$s__");
                            sender.sendMessage(ChatColor.GRAY
                                               + "A valid format is any string containing $s (seconds field) that is less than 50 characters.");
                            return true;
                        }
                        if (args[1].contains("$s") && args[1].length() <= 50) {
                            config.setCountDownSetting(countdown.format, args[1]);
                            sender.sendMessage(ChatColor.GRAY + "Format changed to: "
                                               + ChatColor.translateAlternateColorCodes('&', config.COUNTDOWN_COLOR
                                                                                             + config.COUNTDOWN_STYLE + config.COUNTDOWN_FORMAT));
                        } else {
                            sender.sendMessage(ChatColor.GRAY + "A valid countdown format must contain $s and be 50 characters or less.");
                            return false;
                        }
                        return true;
                    case "color":
                        if (args.length < 2) {
                            sender.sendMessage(ChatColor.RED + "Usage: /cdconfig color <&a-&f> ");
                            return true;
                        }
                        if (args[1].matches("&([0-9a-fA-F])")) {
                            config.setCountDownSetting(countdown.color, args[1]);
                            sender.sendMessage(ChatColor.GRAY + "Color changed to: "
                                               + ChatColor.translateAlternateColorCodes('&', config.COUNTDOWN_COLOR
                                                                                             + config.COUNTDOWN_STYLE + config.COUNTDOWN_FORMAT));
                        } else {
                            sender.sendMessage(ChatColor.GRAY + "Valid countdown color codes: http://minecraft.gamepedia.com/Formatting_codes.");
                            return false;
                        }
                        return true;
                    case "style":
                        if (args.length < 2) {
                            sender.sendMessage(ChatColor.RED + "Usage: /cdconfig style <&m-&o> ");
                            return true;
                        }
                        if (args[1].matches("&([m-oM-OrR])")) {
                            config.setCountDownSetting(countdown.style, args[1]);
                            sender.sendMessage(ChatColor.GRAY + "Style changed to: "
                                               + ChatColor.translateAlternateColorCodes('&', config.COUNTDOWN_COLOR
                                                                                             + config.COUNTDOWN_STYLE + config.COUNTDOWN_FORMAT));
                        } else {
                            sender.sendMessage(ChatColor.GRAY + "Valid countdown style codes: http://minecraft.gamepedia.com/Formatting_codes.");
                            return false;
                        }
                        return true;
                    case "msgcolor":
                        if (args.length < 2) {
                            sender.sendMessage(ChatColor.RED + "Usage: /cdconfig msgColor <&a-&f> ");
                            return true;
                        }
                        if (args[1].matches("&([0-9a-fA-F])")) {
                            config.setCountDownSetting(countdown.msgcolor, args[1]);
                            sender.sendMessage(ChatColor.GRAY + "Message color changed to: "
                                               + ChatColor.translateAlternateColorCodes('&', config.COUNTDOWN_MSG_COLOR
                                                                                             + config.COUNTDOWN_MSG_STYLE) + "Go!");
                        } else {
                            sender.sendMessage(ChatColor.GRAY + "Valid countdown color codes: http://minecraft.gamepedia.com/Formatting_codes.");
                            return false;
                        }
                        return true;
                    case "msgstyle":
                        if (args.length < 2) {
                            sender.sendMessage(ChatColor.RED + "Usage: /cdconfig msgStyle <&m-&o> ");
                            return true;
                        }
                        if (args[1].matches("&([m-oM-OrR])")) {
                            config.setCountDownSetting(countdown.msgstyle, args[1]);
                            sender.sendMessage(ChatColor.GRAY + "Message style changed to: "
                                               + ChatColor.translateAlternateColorCodes('&', config.COUNTDOWN_MSG_COLOR
                                                                                             + config.COUNTDOWN_MSG_STYLE) + "Go!");
                        } else {
                            sender.sendMessage(ChatColor.GRAY + "Valid countdown style codes: http://minecraft.gamepedia.com/Formatting_codes.");
                            return false;
                        }
                        return true;
                    }
                }
            }
        }

        if (command.getName().equalsIgnoreCase("note") && sender.hasPermission("kitchensink.noteblocks")) {

            if (!(sender instanceof Player)) {
                sender.sendMessage("Only players can run that command.");
                return true;
            }

            if (args.length == 0) {
                sender.sendMessage(ChatColor.GOLD + "Usage: /note [high] <note>");
                sender.sendMessage(ChatColor.GOLD + "where <note> is a sharp, flat or natural note (e.g. \"Ab\").");
                sender.sendMessage(ChatColor.GOLD + "Use [high] to sound a higher note.");
                return true;
            }

            int octave = 0;
            int noteIndex = 0;
            if (args.length > 1) {
                if (args[0].equalsIgnoreCase("high")) {
                    octave = 1;
                    noteIndex = 1;
                }
            }

            String noteString = args[noteIndex];

            Note note;

            try {
                note = Note.natural(octave, Note.Tone.valueOf(Character.toString(noteString.charAt(0)).toUpperCase()));
            } catch (Exception e) {
                sender.sendMessage(ChatColor.RED + "The note you gave is not valid!");
                return false;
            }

            if (noteString.length() == 2) {
                String modifier = Character.toString(noteString.charAt(1));

                switch (modifier) {
                case "#":
                    note = note.sharped();
                    break;
                case "b":
                    note = note.flattened();
                    break;
                default:
                    sender.sendMessage(ChatColor.RED + "The note you gave is not valid!");
                    return false;
                }
            }

            Player senderAsPlayer = (Player) sender;
            senderAsPlayer.setMetadata(NOTEBLOCK_META_KEY, new FixedMetadataValue(this, note));
            sender.sendMessage(ChatColor.GOLD + "Punch the note block to apply the note.");
            return true;

        }

        return false;
    }

    public void sendLagStats(CommandSender sender) {
        float tps = 0;
        for (Long l : lagCheck.history) {
            if (l != null) {
                tps += 20 / (l / (float) 1000);
            }
        }
        tps = tps / lagCheck.history.size();
        if (tps > 20) {
            tps = 20;
        }
        long memUsed = (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) / 1048576;
        long memMax = Runtime.getRuntime().maxMemory() / 1048576;

        sender.sendMessage(String.format("TPS: %5.2f Mem: %dM/%dM", tps, memUsed, memMax));
    }

    public void sendList(CommandSender sender) {
        ArrayList<String> list = new ArrayList<>();
        for (Player player : getServer().getOnlinePlayers()) {
            list.add(player.getName());
        }
        list.sort(String.CASE_INSENSITIVE_ORDER);
        sender.sendMessage("Players Online: " + list.size());
        if (list.size() == 0) {
            return;
        }
        StringBuilder onlinelist = new StringBuilder("Players:");
        int index = 0;
        for (String p : list) {
            ChatColor color = ChatColor.GRAY;
            if (index++ % 2 == 0) {
                color = ChatColor.WHITE;
            }
            onlinelist.append(" ").append(color).append(p);
        }
        sender.sendMessage(onlinelist.toString());
    }

    /**
     * Load the contents of the host key file for the specified player.
     *
     * @param player the name of the player.
     * @return a non-null string that is the corresponding prefix of the host
     *         name that the player must connect with, or the empty string if
     *         there are no restrictions.
     */
    public String getHostKey(Player player) {
        File hostKeysDir = new File(getDataFolder(), HOST_KEYS_DIRECTORY);
        File hostKeyFile = new File(hostKeysDir, player.getUniqueId().toString());
        try {
            try (BufferedReader reader = new BufferedReader(new FileReader(hostKeyFile))) {
                return reader.readLine();
            }
        } catch (IOException ex) {
            return "";
        }
    }

    /**
     * Handle the /lock-horse and /unlock-horse commands.
     *
     * The player must subsequently right click on the horse.
     *
     * @param sender the sender of the command.
     * @param locked true if the request is to lock the horse; false for unlock.
     */
    protected void setHorseLockState(CommandSender sender, boolean locked) {
        if (config.LOCK_HORSES) {
            if (sender instanceof Player) {
                Player player = (Player) sender;
                player.setMetadata(HORSE_DO_LOCK_KEY, new FixedMetadataValue(this, locked));
                sender.sendMessage(ChatColor.GOLD + "Right click on a horse that you own.");
            } else {
                sender.sendMessage("You need to be in-game to lock horses.");
            }
        } else {
            sender.sendMessage(ChatColor.RED + "That command is disabled.");
        }
    }

    /**
     * Returns counts for all mobs
     */
    public HashMap<String, Integer> getMobCount(World world) {
        HashMap<String, Integer> counts = new HashMap<>();

        if (world == null) {
            world = getServer().getWorlds().get(0);
        }

        try {
            Collection<LivingEntity> livingEntities;
            livingEntities = world.getEntitiesByClass(LivingEntity.class);
            for (LivingEntity animal : livingEntities) {
                if (counts.containsKey(animal.getType().name())) {
                    counts.put(animal.getType().name(), counts.get(animal.getType().name()) + 1);
                } else {
                    counts.put(animal.getType().name(), 1);
                }
            }
        } catch (Exception ex) {
        }

        return counts;
    }

    public HashMap<String, Integer> getMultiworldMobCount() {
        HashMap<String, Integer> counts = new HashMap<>();
        try {
            for (World world : getServer().getWorlds()) {
                Collection<LivingEntity> livingEntities;
                livingEntities = world.getEntitiesByClass(LivingEntity.class);
                for (LivingEntity animal : livingEntities) {
                    if (counts.containsKey(animal.getType().name())) {
                        counts.put(animal.getType().name(), counts.get(animal.getType().name()) + 1);
                    } else {
                        counts.put(animal.getType().name(), 1);
                    }
                }
            }
        } catch (Exception ex) {
        }
        return counts;
    }

    /**
     * Returns counts for all mobs
     */
    public void dumpMobCount() {
        HashMap<String, Integer> counts = new HashMap<>();
        Map<String, ArrayList<Map<String, Integer>>> locations = new HashMap<>();

        try {
            Collection<LivingEntity> livingEntities = getServer().getWorlds().get(0).getEntitiesByClass(LivingEntity.class);
            for (LivingEntity animal : livingEntities) {
                if (counts.containsKey(animal.getType().name())) {
                    counts.put(animal.getType().name(), counts.get(animal.getType().name()) + 1);
                } else {
                    counts.put(animal.getType().name(), 1);
                }

                Map<String, Integer> location = new HashMap<>();
                location.put("x", animal.getLocation().getBlockX());
                location.put("y", animal.getLocation().getBlockY());
                location.put("z", animal.getLocation().getBlockZ());

                if (!locations.containsKey(animal.getType().name())) {
                    locations.put(animal.getType().name(), new ArrayList<>());
                }
                locations.get(animal.getType().name()).add(location);
            }

            File mobsFile = new File(getDataFolder(), "mobs.yml");
            YamlConfiguration mobsConfig = new YamlConfiguration();
            for (String mob : counts.keySet()) {
                mobsConfig.set(mob + ".count", counts.get(mob));
                mobsConfig.set(mob + ".locations", locations.get(mob));
            }
            mobsConfig.save(mobsFile);
        } catch (Exception ex) {
        }
    }

    public String dictFormat(String format, Hashtable<String, Object> values) {
        StringBuilder convFormat = new StringBuilder(format);
        Enumeration<String> keys = values.keys();
        ArrayList<Object> valueList = new ArrayList<>();

        int currentPos = 1;
        while (keys.hasMoreElements()) {
            String key = keys.nextElement();
            String formatKey = "%(" + key + ")";
            String formatPos = "%" + currentPos + "$s";

            int index = -1;
            while ((index = convFormat.indexOf(formatKey, index)) != -1) {
                convFormat.replace(index, index + formatKey.length(), formatPos);
                index += formatPos.length();
            }

            valueList.add(values.get(key));

            ++currentPos;
        }

        return String.format(convFormat.toString(), valueList.toArray());
    }

    public static Block getTargetBlock(LivingEntity entity) {
        BlockIterator iterator = new BlockIterator(entity.getLocation(), entity.getEyeHeight(), 20);
        Block result;
        while (iterator.hasNext()) {
            result = iterator.next();
            if (!result.getType().equals(Material.AIR)) {
                return result;
            }
        }
        return null;
    }

    public void proxyKick(Player player) {
        ByteArrayDataOutput out = ByteStreams.newDataOutput();
        out.writeUTF("KickPlayer");
        out.writeUTF(player.getName());
        out.writeUTF("Server closed");
        player.sendPluginMessage(this, "BungeeCord", out.toByteArray());
    }
}
