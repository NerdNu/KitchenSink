package nu.nerd.kitchensink;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Art;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Chunk;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Note;
import org.bukkit.Sound;
import org.bukkit.Tag;
import org.bukkit.World;
import org.bukkit.World.Environment;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.block.ShulkerBox;
import org.bukkit.block.data.type.NoteBlock;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.*;
import org.bukkit.event.enchantment.PrepareItemEnchantEvent;
import org.bukkit.event.entity.*;
import org.bukkit.event.entity.EntityRegainHealthEvent.RegainReason;
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
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BlockStateMeta;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.potion.PotionData;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.potion.PotionType;
import org.bukkit.projectiles.ProjectileSource;
import org.bukkit.util.BlockIterator;

import net.luckperms.api.LuckPerms;
import net.luckperms.api.node.NodeType;
import net.luckperms.api.node.types.InheritanceNode;

class KitchenSinkListener implements Listener {

    private static final String REPLACED_CHARS = "[ \\s\\u000a\\u000d\\u2028\\u2029\\u0009\\u000b\\u000c\\u000d\\u0020\\u00a0\\u1680\\u180e\\u2000\\u2001\\u2002\\u2003\\u2004\\u2005\\u2006\\u2007\\u2008\\u2009\\u200a\\u202f\\u205f\\u3000]{2,}";

    /**
     * Material types of blocks that can be interacted with on right click.
     */
    private static final HashSet<Material> INTERACTABLE_TYPES;

    /**
     * A set containing all materials that fall under the corresponding
     * category.
     */
    private static final HashSet<Material> SHULKER_BOXES;
    private static final HashSet<Material> DOORS;
    private static final HashSet<Material> PLATES;
    private static final HashSet<Material> FENCES_AND_GATES;
    private static final HashSet<Material> CARPETS;

    static {
        INTERACTABLE_TYPES = new HashSet<>(Arrays.asList(Material.DISPENSER, Material.NOTE_BLOCK, Material.CHEST,
                                                         Material.CRAFTING_TABLE,
                                                         Material.FURNACE, Material.FURNACE, Material.LEVER, Material.JUKEBOX, Material.CAKE,
                                                         Material.REPEATER, Material.ENCHANTING_TABLE, Material.BREWING_STAND, Material.DRAGON_EGG,
                                                         Material.ENDER_CHEST, Material.BEACON, Material.ANVIL, Material.TRAPPED_CHEST,
                                                         Material.COMPARATOR, Material.HOPPER, Material.DROPPER, Material.DAYLIGHT_DETECTOR));

        PLATES = new HashSet<>(Arrays.asList(Material.ACACIA_BUTTON, Material.BIRCH_BUTTON, Material.DARK_OAK_BUTTON,
                                             Material.JUNGLE_BUTTON,
                                             Material.OAK_BUTTON, Material.SPRUCE_BUTTON, Material.STONE_BUTTON,
                                             Material.ACACIA_PRESSURE_PLATE,
                                             Material.BIRCH_PRESSURE_PLATE, Material.DARK_OAK_PRESSURE_PLATE,
                                             Material.HEAVY_WEIGHTED_PRESSURE_PLATE,
                                             Material.JUNGLE_PRESSURE_PLATE, Material.LIGHT_WEIGHTED_PRESSURE_PLATE,
                                             Material.OAK_PRESSURE_PLATE,
                                             Material.SPRUCE_PRESSURE_PLATE, Material.STONE_PRESSURE_PLATE));

        CARPETS = new HashSet<>(Arrays.asList(Material.BLACK_CARPET, Material.BLUE_CARPET, Material.BROWN_CARPET, Material.CYAN_CARPET,
                                              Material.GRAY_CARPET, Material.GREEN_CARPET, Material.LIGHT_BLUE_CARPET, Material.LIGHT_GRAY_CARPET,
                                              Material.LIME_CARPET, Material.MAGENTA_CARPET, Material.ORANGE_CARPET, Material.PINK_CARPET,
                                              Material.PURPLE_CARPET, Material.RED_CARPET, Material.WHITE_CARPET, Material.YELLOW_CARPET));

        FENCES_AND_GATES = new HashSet<>(Arrays.asList(Material.ACACIA_FENCE_GATE, Material.BIRCH_FENCE_GATE, Material.DARK_OAK_FENCE_GATE,
                                                       Material.JUNGLE_FENCE_GATE, Material.OAK_FENCE_GATE, Material.SPRUCE_FENCE_GATE,
                                                       Material.ACACIA_FENCE, Material.BIRCH_FENCE, Material.DARK_OAK_FENCE, Material.JUNGLE_FENCE,
                                                       Material.OAK_FENCE, Material.SPRUCE_FENCE));

        DOORS = new HashSet<>(Arrays.asList(Material.ACACIA_DOOR, Material.ACACIA_TRAPDOOR, Material.BIRCH_DOOR, Material.BIRCH_TRAPDOOR,
                                            Material.DARK_OAK_DOOR, Material.DARK_OAK_TRAPDOOR, Material.IRON_DOOR, Material.IRON_TRAPDOOR,
                                            Material.JUNGLE_DOOR, Material.JUNGLE_TRAPDOOR, Material.OAK_DOOR, Material.OAK_TRAPDOOR,
                                            Material.SPRUCE_DOOR, Material.SPRUCE_TRAPDOOR));

        SHULKER_BOXES = new HashSet<>(Arrays.asList(Material.BLACK_SHULKER_BOX, Material.BLUE_SHULKER_BOX,
                                                    Material.BROWN_SHULKER_BOX, Material.CYAN_SHULKER_BOX,
                                                    Material.GRAY_SHULKER_BOX, Material.GREEN_SHULKER_BOX,
                                                    Material.LIGHT_BLUE_SHULKER_BOX, Material.LIGHT_GRAY_SHULKER_BOX,
                                                    Material.LIME_SHULKER_BOX, Material.MAGENTA_SHULKER_BOX,
                                                    Material.ORANGE_SHULKER_BOX, Material.PINK_SHULKER_BOX,
                                                    Material.PURPLE_SHULKER_BOX, Material.ORANGE_SHULKER_BOX,
                                                    Material.SHULKER_BOX));

        INTERACTABLE_TYPES.addAll(Tag.BEDS.getValues());
        INTERACTABLE_TYPES.addAll(Tag.BUTTONS.getValues());
        INTERACTABLE_TYPES.addAll(DOORS);
        INTERACTABLE_TYPES.addAll(PLATES);
        INTERACTABLE_TYPES.addAll(FENCES_AND_GATES);
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
    public void onPlayerInteract(PlayerInteractEvent event) {

        Player player = event.getPlayer();
        if (event.getAction() == Action.LEFT_CLICK_BLOCK
            && player.hasPermission("kitchensink.noteblocks")
            && player.hasMetadata(KitchenSink.NOTEBLOCK_META_KEY)) {

            Note note = (Note) player.getMetadata(KitchenSink.NOTEBLOCK_META_KEY).get(0).value();
            if (event.getClickedBlock().getBlockData() instanceof NoteBlock) {
                NoteBlock clicked = (NoteBlock) event.getClickedBlock().getBlockData();
                clicked.setNote(note);
                player.sendMessage(Component.text("Note block set to note " + note.getTone() +
                        (note.isSharped() ? "#" : "") + " successfully!", NamedTextColor.GOLD));
            } else {
                player.sendMessage(Component.text("That block isn't a note block.", NamedTextColor.RED));
            }
            event.setCancelled(true);
            player.removeMetadata(KitchenSink.NOTEBLOCK_META_KEY, plugin);
        }

        if (!event.hasItem()) {
            return;
        }

        ItemStack stack = event.getItem();
        if ((event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK) &&
                plugin.config.DISABLED_RIGHT_ITEMS.contains(stack.getType())) {
                event.setCancelled(true);
            }


        if ((event.getAction() == Action.LEFT_CLICK_AIR || event.getAction() == Action.LEFT_CLICK_BLOCK) &&
                plugin.config.DISABLED_LEFT_ITEMS.contains(stack.getType())) {
                event.setCancelled(true);
            }


        if (event.getAction() == Action.RIGHT_CLICK_BLOCK && plugin.config.DISABLE_TNT &&
                stack.getType() == Material.FLINT_AND_STEEL && event.getClickedBlock().getType() == Material.TNT) {
                    event.setCancelled(true);
                }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onBlockBurn(BlockBurnEvent event) {
        if (plugin.config.DISABLE_TNT && event.getBlock().getType() == Material.TNT) {
                event.setCancelled(true);
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
        } else if (entity instanceof Vindicator && plugin.config.BLOCK_JOHNNY) {
                boolean isOffHand = event.getHand().equals(EquipmentSlot.OFF_HAND);
                Player player = event.getPlayer();
                ItemStack item = (isOffHand) ? player.getInventory().getItemInOffHand() : player.getInventory().getItemInMainHand();
                if (item.getItemMeta().getDisplayName().equalsIgnoreCase("Johnny")) {
                    event.setCancelled(true);
                    plugin.getLogger().info(String.format("Blocked Johnny Vindicator. Player: %s (%s) Loc: %s", player.getName(),
                                                          player.getUniqueId().toString(), entity.getLocation().toString()));
                    player.sendMessage(Component.text("You are not allowed to do that.", NamedTextColor.RED));
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
            if (plugin.config.DISABLE_DISPENSED.contains(event.getItem().getType())) {
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
    public void onTNTPrime(TNTPrimeEvent event) {
        if (plugin.config.DISABLE_TNT) {
                event.setCancelled(true);
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
        Entity entity = event.getEntity();
        EntityType type = entity.getType();
        if (plugin.config.DISABLED_DROPS.containsKey(type)) {
            Set<Material> mats = plugin.config.DISABLED_DROPS.get(type);
            event.getDrops().removeIf(is -> mats.contains(is.getType()));
        }
        if (plugin.config.BUFF_DROPS > 1 && event.getEntity() instanceof Animals) {
            List<ItemStack> items = event.getDrops();
            Location l = event.getEntity().getLocation();
            for (ItemStack a : items) {
                if (!plugin.config.DISABLE_BUFF.contains(a.getType())) {
                    // Drops already drop *once* from the event itself.
                    for (int i = 1; i < plugin.config.BUFF_DROPS; i++) {
                        l.getWorld().dropItemNaturally(l, a);
                    }
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerShearEntity(PlayerShearEntityEvent event) {
        if (plugin.config.BUFF_SHEAR_DROPS > 1 && event.getEntity() instanceof Sheep) {
            List<ItemStack> drops = new ArrayList<>(event.getDrops());

            for(ItemStack item : drops) {
                if(item.getType().name().toLowerCase().endsWith("wool")) {
                    // Minecraft drops 1 - 3 wool. Mutiply by BUFF_SHEAR_DROPS, minus
                    // the drops dropped by the event.
                    int count = (1 + (int) (3 * Math.random())) * (plugin.config.BUFF_SHEAR_DROPS - 1);

                    item.setAmount(count);
                }
            }

            event.setDrops(drops);
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
        if (event.getWorld().getEnvironment() == Environment.THE_END) {
            // Don't interfere with creation of the end's obsidian platform.
            return;
        }

        if (plugin.config.SAFE_PORTALS) {
            boolean allowed = false;
            if (plugin.nextPortal != null) {
                for (BlockState blockState : event.getBlocks()) {
                    if (blockState != null
                        && blockState.getLocation().getBlockX() == plugin.nextPortal.getBlockX()
                        && blockState.getLocation().getBlockY() == plugin.nextPortal.getBlockY()
                        && blockState.getLocation().getBlockZ() == plugin.nextPortal.getBlockZ()) {
                        allowed = true;
                        plugin.nextPortal = null;
                        break;
                    }
                }
            }
            if (!allowed) {
                int dropCount = 0;
                Location loc = null;

                event.setCancelled(true);
                for (BlockState blockState : event.getBlocks()) {
                    if (blockState.getType().isSolid()) {
                        blockState.getBlock().setType(Material.AIR);
                        try {
                            event.getWorld().dropItemNaturally(blockState.getLocation(), new ItemStack(blockState.getType()));
                            if (loc == null) {
                                loc = blockState.getLocation();
                            }
                            ++dropCount;
                        } catch (Exception ex) {
                            plugin.getLogger().info("Exception dropping block: " + blockState.getType() + " " +
                                                    blockState.getLocation().getBlockX() + " " +
                                                    blockState.getLocation().getBlockY() + " " +
                                                    blockState.getLocation().getBlockZ() + " " + ex.getMessage());
                        }
                    }
                }
                if(loc != null) {
                    plugin.getLogger().info("Portal ignition prevented at " + blockLocationToString(loc) +
                            ", dropping " + dropCount + " portal frame blocks.");
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
                if (b.getType() == Material.RAIL ||
                    b.getType() == Material.POWERED_RAIL ||
                    b.getType() == Material.DETECTOR_RAIL ||
                    b.getType() == Material.ACTIVATOR_RAIL ||
                    CARPETS.contains(b.getType())) {
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
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
          // if configured, disable damage to villagers from players
        if (plugin.config.DISABLE_PLAYER_DAMAGE_TO_VILLAGERS && event.getEntityType() == EntityType.VILLAGER) {
            if (event.getDamager() instanceof Player) {
                // cancel the damage
                event.setCancelled(true);

                // tell the attacker
                Player player = (Player) event.getDamager();
                player.sendMessage(Component.text("Villagers are protected against damage from players.",
                        NamedTextColor.DARK_RED));
            } else if (event.getDamager() instanceof Projectile) {
                Projectile damageProjectile = (Projectile) event.getDamager();
                if (damageProjectile.getShooter() instanceof Player) {
                    // cancel the damage
                    event.setCancelled(true);

                    // tell the attacker
                    Player player = (Player) damageProjectile.getShooter();
                    player.sendMessage(Component.text("Villagers are protected against damage from players.",
                            NamedTextColor.DARK_RED));
                }
            }
        } // config.DISABLE_PLAYER_DAMAGE_TO_VILLAGERS
    } // onEntityDamageByEntity

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
    public void onPlayerJoin(PlayerJoinEvent event) {
        if (plugin.config.WARN_RESTART_ON_JOIN) {
            int time = (int) ((plugin.nextRestart - System.currentTimeMillis()) / 1000L);
            if (time < 90 && time > 0) {
                Player player = event.getPlayer();
                player.sendMessage(Component.text("Warning: There will be a restart in about "
                        + time + " seconds!", NamedTextColor.LIGHT_PURPLE));
                player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0F, 1.0F);
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
                player.sendMessage(Component.text("WARNING: There will be a restart in about " +
                        time + " seconds!", NamedTextColor.RED));
                player.sendMessage(Component.text("Having an inventory open when a restart occurs may" +
                        " result in loss of items.", NamedTextColor.RED));
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

    /**
     * This is an attempt to mostly replicate the pre-1.21 item merge behaviour.
     *
     * In Spigot, the item would always merge into the item that ticked first in that tick.
     * This is not exposed to the PaperMC API, so the next best idea I could come up with was to
     * just randomize the merge 50/50. That's because, with Spigot, the target was either ticked first
     * or it wasn't, and there didn't seem to be a pattern with it.
     */
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onItemMerge(ItemMergeEvent event) {

        // 50/50 chance to swap merge direction.
        if (ThreadLocalRandom.current().nextBoolean()) {

            Item targetItem = event.getTarget();
            Item sourceItem = event.getEntity();

            event.setCancelled(true);

            ItemStack targetStack = targetItem.getItemStack();
            ItemStack sourceStack = sourceItem.getItemStack();

            int spaceLeft = sourceStack.getMaxStackSize() - sourceStack.getAmount();
            int toTransfer = Math.min(spaceLeft, targetStack.getAmount());

            // Merge items.
            if (toTransfer > 0) {
                ItemStack combined = sourceStack.clone();
                combined.setAmount(sourceStack.getAmount() + toTransfer);
                sourceItem.setItemStack(combined);

                targetStack.setAmount(targetStack.getAmount() - toTransfer);
                targetItem.setItemStack(targetStack);
            }

            // Delete merging item if it merged completely.
            if (targetItem.getItemStack().getAmount() <= 0) {
                targetItem.remove();
            }
        }
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
