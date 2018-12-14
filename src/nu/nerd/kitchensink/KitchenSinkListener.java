package nu.nerd.kitchensink;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;

import org.bukkit.Art;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Chunk;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Note;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.NoteBlock;
import org.bukkit.block.ShulkerBox;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.AbstractHorse;
import org.bukkit.entity.Ageable;
import org.bukkit.entity.Animals;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Horse;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Ocelot;
import org.bukkit.entity.Painting;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.Sheep;
import org.bukkit.entity.Tameable;
import org.bukkit.entity.Villager;
import org.bukkit.entity.Vindicator;
import org.bukkit.entity.Wolf;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockBurnEvent;
import org.bukkit.event.block.BlockDispenseEvent;
import org.bukkit.event.block.BlockPhysicsEvent;
import org.bukkit.event.block.BlockPistonEvent;
import org.bukkit.event.block.BlockPistonExtendEvent;
import org.bukkit.event.block.BlockPistonRetractEvent;
import org.bukkit.event.block.EntityBlockFormEvent;
import org.bukkit.event.enchantment.PrepareItemEnchantEvent;
import org.bukkit.event.entity.AreaEffectCloudApplyEvent;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.EntityRegainHealthEvent;
import org.bukkit.event.entity.EntityRegainHealthEvent.RegainReason;
import org.bukkit.event.entity.ExplosionPrimeEvent;
import org.bukkit.event.entity.ItemSpawnEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.hanging.HangingPlaceEvent;
import org.bukkit.event.inventory.BrewEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerEggThrowEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerShearEntityEvent;
import org.bukkit.event.player.PlayerToggleSprintEvent;
import org.bukkit.event.world.PortalCreateEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.HorseInventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BlockStateMeta;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.inventory.meta.SpawnEggMeta;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.potion.PotionData;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.potion.PotionType;
import org.bukkit.util.BlockIterator;

class KitchenSinkListener implements Listener {

    private static final String REPLACED_CHARS = "[ \\s\\u000a\\u000d\\u2028\\u2029\\u0009\\u000b\\u000c\\u000d\\u0020\\u00a0\\u1680\\u180e\\u2000\\u2001\\u2002\\u2003\\u2004\\u2005\\u2006\\u2007\\u2008\\u2009\\u200a\\u202f\\u205f\\u3000]{2,}";

    /**
     * Material types of blocks that can be interacted with on right click.
     */
    private static final HashSet<Material> INTERACTABLE_TYPES;

    /**
     * A set containing all materials that fall under the umbrella "Shulker Box".
     */
    private static final HashSet<Material> SHULKER_BOXES;

    static {
        INTERACTABLE_TYPES = new HashSet<>(Arrays.asList(
                Material.DISPENSER, Material.NOTE_BLOCK, Material.BED, Material.BED_BLOCK, Material.CHEST,
                Material.SIGN, Material.WORKBENCH, Material.FURNACE, Material.BURNING_FURNACE, Material.WOOD_DOOR, Material.WOODEN_DOOR,
                Material.ACACIA_DOOR, Material.BIRCH_DOOR, Material.DARK_OAK_DOOR, Material.JUNGLE_DOOR, Material.SPRUCE_DOOR, Material.LEVER,
                Material.IRON_DOOR_BLOCK, Material.STONE_BUTTON, Material.JUKEBOX, Material.CAKE_BLOCK, Material.DIODE_BLOCK_OFF,
                Material.DIODE_BLOCK_ON, Material.TRAP_DOOR, Material.ACACIA_FENCE_GATE, Material.BIRCH_FENCE_GATE, Material.DARK_OAK_FENCE_GATE,
                Material.FENCE_GATE, Material.JUNGLE_FENCE_GATE, Material.SPRUCE_FENCE_GATE, Material.ENCHANTMENT_TABLE, Material.BREWING_STAND,
                Material.DRAGON_EGG, Material.ENDER_CHEST, Material.BEACON, Material.WOOD_BUTTON, Material.ANVIL, Material.TRAPPED_CHEST,
                Material.REDSTONE_COMPARATOR_OFF, Material.REDSTONE_COMPARATOR_ON, Material.HOPPER, Material.DROPPER, Material.DAYLIGHT_DETECTOR,
                Material.DAYLIGHT_DETECTOR_INVERTED));

        SHULKER_BOXES = new HashSet<>(Arrays.asList(
                Material.BLACK_SHULKER_BOX, Material.BLUE_SHULKER_BOX, Material.BROWN_SHULKER_BOX, Material.CYAN_SHULKER_BOX, Material.GRAY_SHULKER_BOX, 
                Material.GREEN_SHULKER_BOX, Material.LIGHT_BLUE_SHULKER_BOX, Material.LIME_SHULKER_BOX, Material.MAGENTA_SHULKER_BOX,
                Material.ORANGE_SHULKER_BOX, Material.PINK_SHULKER_BOX, Material.PURPLE_SHULKER_BOX, Material.ORANGE_SHULKER_BOX));

        INTERACTABLE_TYPES.addAll(SHULKER_BOXES);
    }
    private final KitchenSink plugin;

    KitchenSinkListener(KitchenSink instance) {
        plugin = instance;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onItemSpawn(ItemSpawnEvent event) {
        if (event.isCancelled()) {
            return;
        }

        if (plugin.config.DISABLE_DROPS) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerLogin(PlayerLoginEvent event) {
        final Player player = event.getPlayer();
        if (player.hasPermission("kitchensink.admin")) {
            // By default, always allow admins to log in.
            event.allow();

            if (plugin.config.HOST_KEYS_CHECK) {
                String hostPrefix = event.getHostname();
                int colonIndex = hostPrefix.indexOf('.');
                if (colonIndex != -1) {
                    hostPrefix = hostPrefix.substring(0, colonIndex);
                }

                String hostKey = plugin.getHostKey(player);
                if (!hostPrefix.equals(hostKey)) {
                    // The host key check failed.
                    // Do not leak host key details into the server log.
                    plugin.getLogger().warning(player.getName() + " connected with an invalid host key.");
                    if (plugin.config.HOST_KEYS_DROP_PERMISSIONS && dropToDefaultPermissions(player)) {
                        plugin.getLogger().info(player.getName() + "'s permissions were reduced to default.");
                        Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, () -> {
                            player.sendMessage(ChatColor.DARK_RED + "You have logged in with an invalid host key.");
                            player.sendMessage(ChatColor.DARK_RED + "As a security precaution, your permissions have been reduced to default.");
                        });
                    } else {
                        event.disallow(PlayerLoginEvent.Result.KICK_OTHER, "Login denied: invalid host key. Please contact a tech.");
                    }
                }
            }
        }
    }

    /**
     * Remove all top level permissions groups of the player and ensure that
     * they are in the default group only.
     *
     * If a player happens to be Op, they are deopped too.
     *
     * @param player the player to modify.
     * @return true if successfully modified, or false on error.
     */
    protected boolean dropToDefaultPermissions(Player player) {
        try {
            player.setOp(false);
            for (World world : Bukkit.getServer().getWorlds()) {
                for (String group : ApiLayer.getGroups(world.getName(), CalculableType.USER, player.getName())) {
                    if (!group.equalsIgnoreCase("default")) {
                        ApiLayer.removeGroup(world.getName(), CalculableType.USER, player.getName(), group);
                    }
                }
                ApiLayer.addGroup(world.getName(), CalculableType.USER, player.getName(), "default");
            }
            return true;
        } catch (Exception ex) {
            plugin.getLogger().severe(ex.getClass().getName() + ": " + ex.getMessage() + " dropping permissions for " + player.getName());
            return false;
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerInteract(PlayerInteractEvent event) {

        Player player = event.getPlayer();
        if (event.getAction() == Action.LEFT_CLICK_BLOCK
            && player.hasPermission("kitchensink.noteblocks")
            && player.hasMetadata(KitchenSink.NOTEBLOCK_META_KEY)) {

            Note note = (Note) player.getMetadata(KitchenSink.NOTEBLOCK_META_KEY).get(0).value();
            if (event.getClickedBlock().getState() instanceof NoteBlock) {
                NoteBlock clicked = (NoteBlock) event.getClickedBlock().getState();
                clicked.setNote(note);
                clicked.play();
                player.sendMessage(ChatColor.GOLD + "Note block set to note " + note.getTone().toString() + (note.isSharped() ? "#" : "")
                                   + " successfully!");
            } else {
                player.sendMessage(ChatColor.RED + "That block isn't a note block.");
            }
            event.setCancelled(true);
            player.removeMetadata(KitchenSink.NOTEBLOCK_META_KEY, plugin);
        }

        if (!event.hasItem()) {
            return;
        }
        if (plugin.config.PEARL_DAMAGE > 0) {
            if ((event.getAction() == Action.RIGHT_CLICK_BLOCK || event.getAction() == Action.RIGHT_CLICK_AIR)
                && event.getItem().getType() == Material.ENDER_PEARL) {
                if (event.getClickedBlock() == null || !INTERACTABLE_TYPES.contains(event.getClickedBlock().getType())) {
                    event.getPlayer().damage(plugin.config.PEARL_DAMAGE);
                }
            }
        }

        ItemStack stack = event.getItem();
        if (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK) {
            if (plugin.config.DISABLED_RIGHT_ITEMS.contains(stack.getTypeId())) {
                event.setCancelled(true);
            }
        }

        if (event.getAction() == Action.LEFT_CLICK_AIR || event.getAction() == Action.LEFT_CLICK_BLOCK) {
            if (plugin.config.DISABLED_LEFT_ITEMS.contains(stack.getTypeId())) {
                event.setCancelled(true);
            }
        }

        if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
            if (plugin.config.DISABLE_TNT) {
                if (stack.getType() == Material.FLINT_AND_STEEL && event.getClickedBlock().getType() == Material.TNT) {
                    event.setCancelled(true);
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onBlockBurn(BlockBurnEvent event) {
        if (plugin.config.DISABLE_TNT) {
            if (event.getBlock().getType() == Material.TNT) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onPlayerInteractEntity(PlayerInteractEntityEvent event) {
        Entity entity = event.getRightClicked();
        if (plugin.config.BLOCK_VILLAGERS && entity instanceof Villager) {
            Villager v = (Villager) entity;
            v.setTarget(event.getPlayer());
            event.getPlayer().damage(1, v);
            event.setCancelled(true);
        } else if (entity instanceof Tameable) {
            Tameable tameable = (Tameable) entity;
            Player player = event.getPlayer();
            boolean isPetAdmin = player.hasPermission("KitchenSink.petadmin");

            if (plugin.config.UNTAME_PETS) {
                if (player.hasMetadata(KitchenSink.UNTAME_KEY)) {
                    player.removeMetadata(KitchenSink.UNTAME_KEY, plugin);
                    event.setCancelled(true);

                    if (tameable.isTamed()) {
                        // Warn admins when they bypass ownership.
                        if (isPetAdmin && tameable.getOwner() != player) {
                            String owner = tameable.getOwner() != null ? tameable.getOwner().getName() : "nobody";
                            player.sendMessage(ChatColor.YELLOW + "That pet belongs to " + owner + ".");
                        }

                        if (tameable.getOwner() == player || isPetAdmin) {
                            // Prevent the existence of saddle-wearing
                            // untameable untamed horses.
                            if (tameable instanceof Horse) {
                                Horse horse = (Horse) tameable;
                                HorseInventory inventory = horse.getInventory();
                                Location loc = horse.getLocation();
                                for (ItemStack item : inventory.getContents()) {
                                    if (item != null) {
                                        loc.getWorld().dropItemNaturally(loc, item);
                                    }
                                }
                                inventory.clear();
                                if (horse.isCarryingChest()) {
                                    horse.setCarryingChest(false);
                                    loc.getWorld().dropItemNaturally(loc, new ItemStack(Material.CHEST));
                                }
                            }

                            // Make sure that the pet being untamed is standing,
                            // as sitting mobs
                            // without an owner can not be made to stand
                            if (tameable instanceof Wolf && ((Wolf) entity).isSitting()) {
                                ((Wolf) entity).setSitting(false);
                            }
                            if (tameable instanceof Ocelot && ((Ocelot) entity).isSitting()) {
                                ((Ocelot) entity).setSitting(false);
                            }

                            tameable.setTamed(false);
                            tameable.setOwner(null);
                            player.sendMessage(ChatColor.GOLD + "Pet untamed.");
                        } else {
                            player.sendMessage(ChatColor.RED + "You do not own that pet.");
                        }
                    } else {
                        player.sendMessage(ChatColor.RED + "That animal is not tame.");
                    }
                    return;
                }
            }

            if (plugin.config.LOCK_HORSES && entity instanceof Horse) {
                Horse horse = (Horse) entity;
                Location oldLocation = player.getLocation();
                if (player.hasMetadata(KitchenSink.HORSE_DO_LOCK_KEY)) {
                    event.setCancelled(true);
                    boolean newHorseLockState = false;
                    for (MetadataValue meta : player.getMetadata(KitchenSink.HORSE_DO_LOCK_KEY)) {
                        if (meta.getOwningPlugin() == plugin) {
                            newHorseLockState = (Boolean) meta.value();
                            break;
                        }
                    }
                    player.removeMetadata(KitchenSink.HORSE_DO_LOCK_KEY, plugin);

                    if (horse.isTamed()) {
                        if (horse.getOwner() == player || isPetAdmin) {
                            // Warn admins when they bypass ownership.
                            if (isPetAdmin && horse.getOwner() != player) {
                                player.sendMessage(ChatColor.YELLOW + "That horse belongs to " + horse.getOwner().getName() + ".");
                            }

                            // Default, locked horses lack the "unlocked"
                            // metadata.
                            if (newHorseLockState) {
                                entity.removeMetadata(KitchenSink.HORSE_UNLOCKED_KEY, plugin);
                                player.sendMessage(ChatColor.GOLD + "Horse locked.");
                            } else {
                                entity.setMetadata(KitchenSink.HORSE_UNLOCKED_KEY, new FixedMetadataValue(plugin, null));
                                player.sendMessage(ChatColor.GOLD + "Horse unlocked.");
                            }
                        } else {
                            player.sendMessage(ChatColor.RED + "You do not own that horse.");
                        }
                    } else {
                        player.sendMessage(ChatColor.RED + "You do not own that horse.");
                    }
                } else {
                    // Handle an attempt to mount the horse or attach a lead.
                    if (horse.isTamed() && horse.getOwner() != player && !horse.hasMetadata(KitchenSink.HORSE_UNLOCKED_KEY)) {
                        event.setCancelled(true);
                        player.sendMessage(ChatColor.RED + "That horse is locked by its owner.");
                    }
                }
                if (event.isCancelled()) {
                    // Try to restore the player's old look angle.
                    player.teleport(oldLocation);
                }
            }
        } else if (entity instanceof Vindicator) {
            // Block players from Johnny-tagging Vindicators
            if (plugin.config.BLOCK_JOHNNY) {
                boolean isOffHand = event.getHand().equals(EquipmentSlot.OFF_HAND);
                Player player = event.getPlayer();
                ItemStack item = (isOffHand) ? player.getInventory().getItemInOffHand() : player.getInventory().getItemInMainHand();
                if (item.getItemMeta().getDisplayName().equalsIgnoreCase("Johnny")) {
                    event.setCancelled(true);
                    plugin.getLogger().info(String.format("Blocked Johnny Vindicator. Player: %s (%s) Loc: %s", player.getName(),
                                                          player.getUniqueId().toString(), entity.getLocation().toString()));
                    player.sendMessage(String.format("%sYou are not allowed to do that.", ChatColor.RED));
                }
            }
        }
    }

    @EventHandler
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        if (event.isCancelled()) {
            return;
        }

        String message = event.getMessage();

        if (plugin.config.NORMALIZE_CHAT) {
            boolean sarcasmDetected = message.startsWith(ChatColor.ITALIC.toString());
            message = ChatColor.stripColor(message);
            message = message.replaceAll(REPLACED_CHARS, " ");
            message = Normalizer.normalize(message, Normalizer.Form.NFC);
            if (sarcasmDetected) {
                message = ChatColor.ITALIC + message;
            }
            event.setMessage(message);
        }

        if (plugin.config.BLOCK_CAPS) {
            int upperCount = 0;
            int messageLength = 0;
            for (Player p : plugin.getServer().getOnlinePlayers()) {
                message = message.replace(p.getName(), "");
            }
            for (char c : message.toCharArray()) {
                if (Character.isUpperCase(c)) {
                    upperCount++;
                }
                if (Character.isLetter(c) || c == ' ') {
                    messageLength++;
                }
            }
            if ((upperCount > messageLength / 2) && messageLength > 8) {
                event.getPlayer().sendMessage(ChatColor.DARK_GREEN + "Please don't type in all caps.");
                event.setCancelled(true);
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onEntityBlockForm(EntityBlockFormEvent event) {
        if (plugin.config.DISABLE_SNOW) {
            if (event.getBlock().getRelative(BlockFace.DOWN).getType() != Material.OBSIDIAN) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onBlockDispense(BlockDispenseEvent event) {
        if (plugin.config.SAFE_DISPENSERS) {
            if (plugin.config.DISABLE_DISPENSED.contains(event.getItem().getTypeId())) {
                event.setCancelled(true);
            }
        }
    }

    /**
     * When configured, extinguish flaming arrows that hit TNT.
     *
     * WorldGuard's block-tnt setting prevents the explosion but does not
     * prevent the TNT from being turned into a primed TNT entity. This
     * implementation makes a best effort attempt to prevent decorative TNT from
     * being removed by flaming arrows.
     *
     * At the time of writing, the Bukkit API has several bugs relevant to this
     * task that remain unresolved after 3 years:
     * <ul>
     * <li>https://bukkit.atlassian.net/browse/BUKKIT-770</li>
     * <li>https://bukkit.atlassian.net/browse/BUKKIT-3885</li>
     * <li>https://bukkit.atlassian.net/browse/BUKKIT-2231</li>
     * </ul>
     *
     * There is no easy way to detect when TNT has been set off by an arrow.
     * BlockIgniteEvent is not raised. BlockExplodeEvent also doesn't get
     * raised.
     *
     * The location of the arrow in the ProjectileHitEvent can be up to 3 blocks
     * away from where the arrow sticks into a block. You can ray trace the
     * arrow's trajectory based on its final position and velocity, per
     * https://bukkit.org/threads/getting-block-hit-by-projectile-arrow.49071/
     * and that will give you the block that the arrow is sticking into, but if
     * that block is adjacent to a TNT, and the arrow is sufficiently close to
     * the TNT, then the TNT will ignite.
     *
     * However, even if you get all of that right, occasionally the TNT will be
     * ignited before the ProjectileHitEvent is fired, meaning that it is not
     * possible to stop the TNT block from being removed from the world. In that
     * case, we intercept ExplosionPrimeEvent and remove the primed TNT entity.
     */
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onProjectileHit(ProjectileHitEvent event) {
        if (plugin.config.DISABLE_TNT) {
            Projectile projectile = event.getEntity();
            if (projectile.getType() == EntityType.ARROW &&
                projectile.getFireTicks() > 0) {
                World world = projectile.getWorld();
                BlockIterator iterator = new BlockIterator(world,
                    projectile.getLocation().toVector(),
                    projectile.getVelocity().normalize(), 0, 4);

                Block hitBlock;
                while (iterator.hasNext()) {
                    hitBlock = iterator.next();
                    if (hitBlock.getType() != Material.AIR) {
                        if (plugin.config.DEBUG_DISABLE_TNT) {
                            plugin.getLogger().info("DEBUG: Lit arrow hit " +
                                                    blockLocationToString(hitBlock.getLocation()));
                        }

                        for (int x = -1; x <= 1; ++x) {
                            for (int z = -1; z <= 1; ++z) {
                                for (int y = -1; y <= 1; ++y) {
                                    Block neighbour = hitBlock.getRelative(x, y, z);
                                    if (neighbour != null && neighbour.getType() == Material.TNT) {
                                        projectile.setFireTicks(0);

                                        if (plugin.config.DEBUG_DISABLE_TNT) {
                                            Location loc = neighbour.getLocation();
                                            plugin.getLogger().info("DEBUG: Arrow extenguished due to TNT at " + blockLocationToString(loc));
                                        }
                                    }
                                }
                            }
                        }
                        break;
                    }
                }
            }
        }
    } // onProjectileHit

    /**
     * Sometimes a flaming arrow will light TNT and there is no way to stop the
     * TNT turning into an entity.
     *
     * See the comment for {@link #onProjectileHit(ProjectileHitEvent)}.
     */
    @EventHandler()
    public void onExplosionPrime(ExplosionPrimeEvent event) {
        if (plugin.config.DISABLE_TNT && event.getEntityType() == EntityType.PRIMED_TNT) {
            event.getEntity().remove();
            event.setCancelled(true);

            if (plugin.config.DEBUG_DISABLE_TNT) {
                Location loc = event.getEntity().getLocation();
                plugin.getLogger().info("DEBUG: Cancelled ExplosionPrimeEvent at " + blockLocationToString(loc));
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onBlockPhysics(BlockPhysicsEvent event) {
        if (plugin.config.DISABLE_TNT) {
            if (event.getBlock().getType() == Material.TNT) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onEntityExplode(EntityExplodeEvent event) {
        if (plugin.config.DISABLE_ENTITY_BLOCK_DAMAGE.contains(event.getEntityType())) {
            event.blockList().clear();
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onEntityDeath(EntityDeathEvent event) {
        if (plugin.config.DISABLE_PEARL_DROPS_IN_END) {
            Location l = event.getEntity().getLocation();
            if (l.getWorld().getEnvironment() == World.Environment.THE_END) {
                event.getDrops().removeIf(is -> is.getType() == Material.ENDER_PEARL);
            }
        }
        if (plugin.config.DISABLED_DROPS.containsKey(event.getEntity().getType())) {
            Set<Material> mats = plugin.config.DISABLED_DROPS.get(event.getEntity().getType());
            event.getDrops().removeIf(is -> mats.contains(is.getType()));
        }
        if (event.getEntity() instanceof Ageable || event.getEntity().getCustomName() != null) {
            Player killer = event.getEntity().getKiller();
            if (killer != null) {
                if (plugin.config.LOG_ANIMAL_DEATH) {
                    Location l = event.getEntity().getLocation();
                    Chunk c = l.getChunk();
                    String message = "[MobKill] " + killer.getName() + "|" + event.getEntityType().name()
                                     + "|" + l.getWorld().getName() + "|" + l.getBlockX() + "|" + l.getBlockY() + "|" + l.getBlockZ()
                                     + "|C[" + c.getX() + "," + c.getZ() + "]";
                    if (event.getEntity() instanceof Tameable) {
                        Tameable tameable = (Tameable) event.getEntity();
                        if (tameable instanceof Ocelot) {
                            Ocelot ocelot = (Ocelot) tameable;
                            message += "|" + ocelot.getCatType().name();
                        } else if (tameable instanceof Horse) {
                            AbstractHorse abstractHorse = (AbstractHorse) tameable;
                            Horse horse = (Horse) abstractHorse;
                            message += "|" + horse.getColor().name() + "," + horse.getStyle().name() + "|";
                            for (ItemStack item : abstractHorse.getInventory()) {
                                if (item != null && item.getType() != Material.AIR) {
                                    message += getItemDescription(item) + ",";
                                }
                            }
                        }
                        if (tameable.isTamed() && tameable.getOwner() != null) {
                            message += "|Owner:" + tameable.getOwner().getName();
                        }
                    }
                    if (event.getEntity().getCustomName() != null) {
                        message += "|named " + event.getEntity().getCustomName();
                    }
                    plugin.getLogger().info(message);
                }
                if (plugin.config.BUFF_DROPS > 1 && event.getEntity() instanceof Animals) {
                    List<ItemStack> items = event.getDrops();
                    Location l = event.getEntity().getLocation();
                    for (ItemStack a : items) {
                        if (!plugin.config.DISABLE_BUFF.contains(a.getTypeId())) {
                            // Drops already drop *once* from the event itself.
                            for (int i = 1; i < plugin.config.BUFF_DROPS; i++) {
                                l.getWorld().dropItemNaturally(l, a);
                            }
                        }
                    }
                }
            }
        }
    }

    // -------------------------------------------------------------------------
    /**
     * If enabled, logs the player's inventory upon death.
     */
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerDeath(PlayerDeathEvent event) {
        if (plugin.config.LOG_PLAYER_DROPS) {
            Player player = event.getEntity();
            String dropLoc = blockLocationToString(player.getLocation());
            String loot = "[drops] " + player.getName() + " at " + dropLoc + ": " +
            event.getDrops().stream()
                            .map(this::getItemDescription)
                            .collect(Collectors.joining(","));
            plugin.getLogger().info(loot);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerShearEntity(PlayerShearEntityEvent event) {
        if (plugin.config.BUFF_SHEAR_DROPS > 1 && event.getEntity() instanceof Sheep) {
            Sheep entity = (Sheep) event.getEntity();
            Location l = entity.getLocation();

            // Minecraft drops 1 - 3 wool. Mutiply by BUFF_SHEAR_DROPS, minus
            // the drops dropped by the event.
            int count = (1 + (int) (3 * Math.random())) * (plugin.config.BUFF_SHEAR_DROPS - 1);
            l.getWorld().dropItemNaturally(l, new ItemStack(Material.WOOL, count, (byte) entity.getColor().ordinal()));
        }
    }

    /**
     * There's no "elegant" way to work out who was trying to light a portal and
     * allow an admin to bypass the safe portals check. EntityCreatePortalEvent
     * is only fired when the enderdragon dies - not when a player lights a
     * portal.
     *
     * So, the /allow-portal command, run by an admin, disables the safe portals
     * check for a specific portal location, allowing the portal to be created.
     */
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPortalCreate(PortalCreateEvent event) {
        if (plugin.config.SAFE_PORTALS) {
            boolean allowed = false;
            if (plugin.nextPortal != null) {
                for (Block block : event.getBlocks()) {
                    if (block != null
                        && block.getLocation().getBlockX() == plugin.nextPortal.getBlockX()
                        && block.getLocation().getBlockY() == plugin.nextPortal.getBlockY()
                        && block.getLocation().getBlockZ() == plugin.nextPortal.getBlockZ()) {
                        allowed = true;
                        plugin.nextPortal = null;
                        break;
                    }
                }
            }
            if (!allowed) {
                event.setCancelled(true);
                for (Block block : event.getBlocks()) {
                    block.setTypeId(0);
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onBlockPistonExtend(BlockPistonExtendEvent e) {
        preventRailAndCarpetDuplication(e, e.getBlocks());
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onBlockPistonRetract(BlockPistonRetractEvent e) {
        preventRailAndCarpetDuplication(e, e.getBlocks());
    }

    protected void preventRailAndCarpetDuplication(BlockPistonEvent e, List<Block> movedBlocks) {
        if (plugin.config.BLOCK_SLIME_MOVING_RAILS_AND_CARPETS) {
            boolean hasRailOrCarpet = false;
            boolean hasSlime = false;
            for (Block b : movedBlocks) {
                if (b.getType() == Material.CARPET ||
                    b.getType() == Material.RAILS ||
                    b.getType() == Material.POWERED_RAIL ||
                    b.getType() == Material.DETECTOR_RAIL ||
                    b.getType() == Material.ACTIVATOR_RAIL) {
                    hasRailOrCarpet = true;
                }

                if (b.getType() == Material.SLIME_BLOCK) {
                    hasSlime = true;
                }

                if (hasRailOrCarpet && hasSlime) {
                    e.setCancelled(true);
                    break;
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onBlockBreak(BlockBreakEvent event) {
        if (event.isCancelled()) {
            return;
        }
        if (plugin.config.SAFE_ICE) {
            if (event.getBlock().getType() == Material.ICE && !event.getPlayer().getGameMode().equals(GameMode.CREATIVE)) {
                event.getBlock().setType(Material.AIR);
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onEnchantItem(PrepareItemEnchantEvent event) {
        if (!plugin.config.ALLOW_ENCH_ITEMS.isEmpty()) {
            if (!plugin.config.ALLOW_ENCH_ITEMS.contains(event.getItem().getTypeId())) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onBrew(BrewEvent event) {
        if (!plugin.config.BLOCK_BREW.isEmpty()) {
            if (plugin.config.BLOCK_BREW.contains(event.getContents().getIngredient().getTypeId())) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onEntityRegainHealth(EntityRegainHealthEvent event) {
        if (event.getEntity() instanceof Player) {
            if (event.getRegainReason() == RegainReason.MAGIC) {
                event.setAmount(event.getAmount() * plugin.config.HEALTH_POTION_MULTIPLIER);
            } else if (event.getRegainReason() == RegainReason.MAGIC_REGEN) {
                event.setAmount(event.getAmount() * plugin.config.REGEN_POTION_MULTIPLIER);
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        if (plugin.config.DISABLE_INVISIBILITY_ON_COMBAT && !event.isCancelled() && event.getEntity() instanceof Player) {
            if (event.getDamager() instanceof Player) {
                Player damagerPlayer = (Player) event.getDamager();
                if (damagerPlayer.hasPotionEffect(PotionEffectType.INVISIBILITY)) {
                    damagerPlayer.removePotionEffect(PotionEffectType.INVISIBILITY);
                }

                if (((Player) event.getEntity()).hasPotionEffect(PotionEffectType.INVISIBILITY)) {
                    ((Player) event.getEntity()).removePotionEffect(PotionEffectType.INVISIBILITY);
                }
            } else if (event.getDamager() instanceof Projectile) {
                Projectile damageProjectile = (Projectile) event.getDamager();

                if (damageProjectile.getShooter() instanceof Player) {
                    Player damagerPlayer = (Player) damageProjectile.getShooter();
                    if (damagerPlayer.hasPotionEffect(PotionEffectType.INVISIBILITY)) {
                        damagerPlayer.removePotionEffect(PotionEffectType.INVISIBILITY);
                    }

                    if (((Player) event.getEntity()).hasPotionEffect(PotionEffectType.INVISIBILITY)) {
                        ((Player) event.getEntity()).removePotionEffect(PotionEffectType.INVISIBILITY);
                    }
                }
            }
        } // config.DISABLE_INVISIBILITY_ON_COMBAT
        if (plugin.config.LOWER_STRENGTH_POTION_DAMAGE && event.getEntity() instanceof Player && event.getDamager() instanceof Player) {
            Player damager = (Player) event.getDamager();
            if (damager.hasPotionEffect(PotionEffectType.INCREASE_DAMAGE)) {
                for (PotionEffect pe : damager.getActivePotionEffects()) {
                    if (pe.getType().equals(PotionEffectType.INCREASE_DAMAGE)) {
                        double newDamage = event.getDamage();
                        if (pe.getAmplifier() == 1) { // str2
                            newDamage = newDamage / 2.6;
                            newDamage = newDamage + 6;
                        } else if (pe.getAmplifier() == 0) { // str1
                            newDamage = newDamage / 1.3;
                            newDamage = newDamage + 3;
                        }
                        event.setDamage(newDamage);
                    }
                }
            }
        } // config.LOWER_STRENGTH_POTION_DAMAGE
          // if configured, disable damage to villagers from players
        if (plugin.config.DISABLE_PLAYER_DAMAGE_TO_VILLAGERS && event.getEntityType() == EntityType.VILLAGER) {
            if (event.getDamager() instanceof Player) {
                // cancel the damage
                event.setCancelled(true);

                // tell the attacker
                Player player = (Player) event.getDamager();
                player.sendMessage(ChatColor.DARK_RED + "Villagers are protected against damage from players.");
            } else if (event.getDamager() instanceof Projectile) {
                Projectile damageProjectile = (Projectile) event.getDamager();
                if (damageProjectile.getShooter() instanceof Player) {
                    // cancel the damage
                    event.setCancelled(true);

                    // tell the attacker
                    Player player = (Player) damageProjectile.getShooter();
                    player.sendMessage(ChatColor.DARK_RED + "Villagers are protected against damage from players.");
                }
            }
        } // config.DISABLE_PLAYER_DAMAGE_TO_VILLAGERS
    } // onEntityDamageByEntity

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onEntityDamage(EntityDamageEvent event) {
        if (plugin.config.INVULNERABLE_TAME_HORSES && event.getEntityType() == EntityType.HORSE && event.getCause() != DamageCause.VOID) {
            Horse horse = (Horse) event.getEntity();
            if (horse.isTamed() && horse.getPassenger() == null) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onHangingPlaceEvent(HangingPlaceEvent event) {
        if (event.getEntity() instanceof Painting) {
            List<MetadataValue> metaList = event.getPlayer().getMetadata(KitchenSink.PAINTING_META_KEY);
            if (metaList.size() == 1) {
                MetadataValue meta = metaList.get(0);
                if (meta.getOwningPlugin() == plugin && meta.value() instanceof Art) {
                    Painting painting = (Painting) event.getEntity();
                    painting.setArt((Art) meta.value());
                }

                // After placing a painting, clear the metadata so the next
                // painting is random again.
                event.getPlayer().removeMetadata(KitchenSink.PAINTING_META_KEY, plugin);
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerToggleSprint(PlayerToggleSprintEvent event) {
        if (event.isSprinting() && plugin.config.SPRINT_MAX_TICKS > 0) {
            plugin.getServer().getScheduler().runTaskLater(plugin, new SprintTask(event.getPlayer()), plugin.config.SPRINT_MAX_TICKS);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerJoin(PlayerJoinEvent event) {
        if (plugin.config.WARN_RESTART_ON_JOIN) {
            int time = (int) ((plugin.nextRestart - System.currentTimeMillis()) / 1000L);
            if (time < 90 && time > 0) {
                event.getPlayer().sendMessage(ChatColor.LIGHT_PURPLE + "Warning: There will be a restart in about " + time + " seconds!");
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onInventoryOpen(InventoryOpenEvent event) {
        if (plugin.config.WARN_RESTART_ON_INVENTORY_OPEN) {
            int time = (int) ((plugin.nextRestart - System.currentTimeMillis()) / 1000L);
            if (time < 90 && time > 0) {
                if (!(event.getPlayer() instanceof Player)) {
                    return;
                }

                Player player = (Player) event.getPlayer();
                player.sendMessage(ChatColor.RED + "WARNING: There will be a restart in about " + time + " seconds!");
                player.sendMessage(ChatColor.RED + "Having an inventory open when a restart occurs may result in loss of items.");
                player.playSound(player.getLocation(), Sound.BLOCK_ANVIL_LAND, 2f, 1f);
            }
        }

    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerEggThrow(PlayerEggThrowEvent event) {
        if (event.isHatching() && !plugin.config.ALLOW_EGG_HATCHING) {
            event.setHatching(false);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onCreatureSpawn(CreatureSpawnEvent event) {
        // Stop iron golems from spawning in villages
        if (event.getSpawnReason().equals(CreatureSpawnEvent.SpawnReason.VILLAGE_DEFENSE)) {
            event.setCancelled(plugin.config.DISABLE_GOLEM_NATURAL_SPAWN);
        }
    }

    @EventHandler
    public void onAreaEffectApply(AreaEffectCloudApplyEvent event) {
        // Block lingering potions from being used for pvp
        if (plugin.config.DISABLE_LINGERING_POTION_PVP) {
            if (event.getEntity().getSource() instanceof Player) {
                PotionType type = event.getEntity().getBasePotionData().getType();
                List<PotionType> blacklist = new ArrayList<>();
                blacklist.add(PotionType.INSTANT_DAMAGE);
                blacklist.add(PotionType.POISON);
                blacklist.add(PotionType.SLOWNESS);
                blacklist.add(PotionType.WEAKNESS);
                event.getAffectedEntities().removeIf(ent -> ent instanceof Player && blacklist.contains(type));
            }
        }
    }

    /**
     * Return a string describing a dropped item stack.
     *
     * The string contains the material type name, data value and amount, as
     * well as a list of enchantments. It is used in methods that log drops.
     *
     * If the item is a shulker box, the contents will be recursively added
     * to the description.
     *
     * @param item the droppped item stack.
     * @return a string describing a dropped item stack.
     */
    public String getItemDescription(ItemStack item) {
        StringBuilder description = new StringBuilder();
        description.append(item.getAmount()).append('x').append(item.getType().name()).append(':').append(item.getDurability());
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            if (meta instanceof BlockStateMeta) {
                BlockStateMeta blockStateMeta = (BlockStateMeta) meta;
                if (blockStateMeta.getBlockState() instanceof ShulkerBox) {
                    description.append(" *** Contents: ");
                    ShulkerBox shulkerBox = (ShulkerBox) blockStateMeta.getBlockState();
                    shulkerBox.getInventory().forEach(itemStack -> {
                        if (itemStack != null) description.append(getItemDescription(itemStack));
                    });
                    description.append(" **** ");
                }
            } else if (meta instanceof SkullMeta) {
                SkullMeta skullMeta = (SkullMeta) meta;
                if (skullMeta.getOwner() != null) {
                    description.append(" of \"").append(skullMeta.getOwner()).append("\"");
                }
            } else if (meta instanceof SpawnEggMeta) {
                SpawnEggMeta eggMeta = (SpawnEggMeta) meta;
                description.append(" of ").append(eggMeta.getSpawnedType());
            } else if (meta instanceof EnchantmentStorageMeta) {
                EnchantmentStorageMeta bookEnchants = (EnchantmentStorageMeta) meta;
                description.append(" with").append(enchantsToString(bookEnchants.getStoredEnchants()));
            } else if (meta instanceof BookMeta) {
                BookMeta bookMeta = (BookMeta) meta;
                if (bookMeta.getTitle() != null) {
                    description.append(" titled \"").append(bookMeta.getTitle()).append("\"");
                }
                if (bookMeta.getAuthor() != null) {
                    description.append(" by ").append(bookMeta.getAuthor());
                }
            } else if (meta instanceof PotionMeta) {
                PotionMeta potionMeta = (PotionMeta) meta;
                description.append(" of ");
                PotionData data = potionMeta.getBasePotionData();
                description.append(data.getType());
                if (data.isExtended()) {
                    description.append(" extended");
                }
                if (data.isUpgraded()) {
                    description.append(" upgraded");
                }

                List<PotionEffect> effects = potionMeta.getCustomEffects();
                if (effects != null && !effects.isEmpty()) {
                    description.append(" with ");
                    String sep = "";
                    for (PotionEffect effect : potionMeta.getCustomEffects()) {
                        description.append(sep).append(potionToString(effect));
                        sep = "+";
                    }
                }
            }

            if (meta.getDisplayName() != null) {
                description.append(" named \"").append(meta.getDisplayName()).append("\"").append(ChatColor.WHITE);
            }

            List<String> lore = meta.getLore();
            if (lore != null && !lore.isEmpty()) {
                description.append(" lore \"").append(String.join("|", lore)).append("\"").append(ChatColor.WHITE);
            }
        }

        description.append(enchantsToString(item.getEnchantments()));
        return description.toString();
    }

    /**
     * Return the string description of a potion effect.
     *
     * @param effect the effect.
     * @return the description.
     */
    public String potionToString(PotionEffect effect) {
        StringBuilder description = new StringBuilder();
        description.append(effect.getType().getName()).append("/");
        description.append(effect.getAmplifier() + 1).append("/");
        description.append(effect.getDuration() / 20.0).append('s');
        return description.toString();
    }

    /**
     * Return the string description of a set of enchantments.
     *
     * @param enchants map from enchantment type to level, from the Bukkit API.
     * @return the description.
     */
    public String enchantsToString(Map<Enchantment, Integer> enchants) {
        StringBuilder description = new StringBuilder();
        if (enchants.size() > 0) {
            description.append(" (");
            String sep = "";
            for (Entry<Enchantment, Integer> entry : enchants.entrySet()) {
                description.append(sep).append(entry.getKey().getName()).append(':').append(entry.getValue());
                sep = ",";
            }
            description.append(')');
        }
        return description.toString();
    }

    /**
     * Return the integer block coordinates of a Location as a String.
     *
     * @param loc the Location.
     * @return the integer block coordinates of a Location as a String.
     */
    public String blockLocationToString(Location loc) {
        return "(" + loc.getWorld().getName() + ", " + loc.getBlockX() + ", " + loc.getBlockY() + ", " + loc.getBlockZ() + ")";
    }
}
