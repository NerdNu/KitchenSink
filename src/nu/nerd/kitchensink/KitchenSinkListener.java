package nu.nerd.kitchensink;

import java.text.Normalizer;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.bukkit.Art;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Chunk;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Ageable;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Horse;
import org.bukkit.entity.Ocelot;
import org.bukkit.entity.Painting;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.Sheep;
import org.bukkit.entity.Tameable;
import org.bukkit.entity.Vehicle;
import org.bukkit.entity.Villager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockDispenseEvent;
import org.bukkit.event.block.EntityBlockFormEvent;
import org.bukkit.event.enchantment.PrepareItemEnchantEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityRegainHealthEvent;
import org.bukkit.event.entity.EntityRegainHealthEvent.RegainReason;
import org.bukkit.event.entity.ItemSpawnEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.hanging.HangingPlaceEvent;
import org.bukkit.event.inventory.BrewEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerShearEntityEvent;
import org.bukkit.event.vehicle.VehicleExitEvent;
import org.bukkit.event.world.PortalCreateEvent;
import org.bukkit.inventory.HorseInventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.event.player.PlayerToggleSprintEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import de.bananaco.bpermissions.api.ApiLayer;
import de.bananaco.bpermissions.api.util.CalculableType;

class KitchenSinkListener implements Listener {
	private static final String REPLACED_CHARS = "[ \\s\\u000a\\u000d\\u2028\\u2029\\u0009\\u000b\\u000c\\u000d\\u0020\\u00a0\\u1680\\u180e\\u2000\\u2001\\u2002\\u2003\\u2004\\u2005\\u2006\\u2007\\u2008\\u2009\\u200a\\u202f\\u205f\\u3000]{2,}";

	/**
	 * Material types of blocks that can be interacted with on right click.
	 */
	private static final HashSet<Integer> INTERACTABLE_TYPES;
	static {
		INTERACTABLE_TYPES = new HashSet<Integer>(Arrays.asList(23, 25, 26, 54, 58, 61, 62, 64, 69, 71, 77, 84, 92, 93, 94, 95, 96, 107, 116, 117,
			122, 130, 138, 143, 145, 146, 149, 150, 154, 158));
	}
	private final KitchenSink plugin;

	KitchenSinkListener(KitchenSink instance) {
		plugin = instance;
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onItemSpawn(ItemSpawnEvent event) {
		if (event.isCancelled())
			return;

		if (plugin.config.DISABLE_DROPS)
			event.setCancelled(true);
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

				String hostKey = plugin.getHostKey(player.getName());
				if (!hostPrefix.equals(hostKey)) {
					// The host key check failed.
					// Do not leak host key details into the server log.
					plugin.getLogger().warning(player.getName() + " connected with an invalid host key.");
					if (plugin.config.HOST_KEYS_DROP_PERMISSIONS && dropToDefaultPermissions(player)) {
						plugin.getLogger().info(player.getName() + "'s permissions were reduced to default.");
						Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
							@Override
							public void run() {
								player.sendMessage(ChatColor.DARK_RED + "You have logged in with an invalid host key.");
								player.sendMessage(ChatColor.DARK_RED + "As a security precaution, your permissions have been reduced to default.");
							}
						});
					} else {
						event.disallow(PlayerLoginEvent.Result.KICK_OTHER, "Login denied: invalid host key. Please contact a tech.");
					}
				}
			}
			return;
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
	public void onVehicleExit(VehicleExitEvent event) {
		if (plugin.config.REMOVE_ON_EXIT) {
			Vehicle vehicle = event.getVehicle();
			vehicle.remove();
		}
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onPlayerInteract(PlayerInteractEvent event) {
		if (!event.hasItem()) {
			return;
		}
		if (plugin.config.PEARL_DAMAGE > 0) {
			if ((event.getAction() == Action.RIGHT_CLICK_BLOCK || event.getAction() == Action.RIGHT_CLICK_AIR)
							&& event.getItem().getType() == Material.ENDER_PEARL) {
				if (event.getClickedBlock() == null || !INTERACTABLE_TYPES.contains(event.getClickedBlock().getTypeId())) {
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
	}

	@EventHandler(priority = EventPriority.NORMAL)
	public void onPlayerInteractEntity(PlayerInteractEntityEvent event) {
		Entity entity = event.getRightClicked();
		if (plugin.config.BLOCK_VILLAGERS && entity instanceof Villager) {
			Villager v = (Villager) entity;
			v.setTarget(event.getPlayer());
			event.getPlayer().damage(1, v);
			event.setCancelled(true);
			return;
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
							player.sendMessage(ChatColor.YELLOW + "That pet belongs to " + tameable.getOwner().getName() + ".");
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
		}
	}

	@EventHandler
	public void onPlayerChat(AsyncPlayerChatEvent event) {
		if (event.isCancelled())
			return;

		String message = event.getMessage();
		message = ChatColor.stripColor(message);
		message = message.replaceAll(REPLACED_CHARS, " ");
		message = Normalizer.normalize(message, Normalizer.Form.NFD);
		event.setMessage(message);

		if (plugin.config.BLOCK_CAPS) {
			int upperCount = 0;
			for (int i = 0; i < message.length(); i++) {
				if (Character.isUpperCase(message.charAt(i)))
					upperCount++;
			}

			if ((upperCount > message.length() / 2) && message.length() > 8) {
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

	@EventHandler(priority = EventPriority.MONITOR)
	public void onEntityDeath(EntityDeathEvent event) {
		if (event.getEntity() instanceof Ageable) {
			Player killer = event.getEntity().getKiller();
			if (killer != null) {
				if (plugin.config.LOG_ANIMAL_DEATH) {
					Location l = event.getEntity().getLocation();
					Chunk c = l.getChunk();
					String message = "[MobKill] " + killer.getName() + "|" + event.getEntityType().name() +
										"|" + l.getWorld().getName() + "|" + l.getBlockX() + "|" + l.getBlockY() + "|" + l.getBlockZ() +
										"|C[" + c.getX() + "," + c.getZ() + "]";
					if (event.getEntity() instanceof Tameable) {
						Tameable tameable = (Tameable) event.getEntity();
						if (tameable instanceof Ocelot) {
							Ocelot ocelot = (Ocelot) tameable;
							message += "|" + ocelot.getCatType().name();
						} else if (tameable instanceof Horse) {
							Horse horse = (Horse) tameable;
							message += "|" + horse.getVariant().name() + "," + horse.getColor().name() + "," + horse.getStyle().name() + "|";
							for (ItemStack item : horse.getInventory()) {
								if (item != null && item.getType() != Material.AIR) {
									message += getItemDescription(item) + ",";
								}
							}
						}
						if (tameable.isTamed()) {
							message += "|Owner:" + tameable.getOwner().getName();
						}
					}
					plugin.getLogger().info(message);
				}
				if (plugin.config.BUFF_DROPS > 1) {
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

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onPlayerDeath(PlayerDeathEvent event) {
		Player player = event.getEntity();
		if (plugin.config.LOG_PLAYER_DROPS) {
			String loot = "[drops] " + player.getName();
			for (ItemStack is : event.getDrops()) {
				loot += ", " + getItemDescription(is);
			}
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
					if (block != null &&
					    block.getLocation().getBlockX() == plugin.nextPortal.getBlockX() &&
						block.getLocation().getBlockY() == plugin.nextPortal.getBlockY() &&
						block.getLocation().getBlockZ() == plugin.nextPortal.getBlockZ()) {
						allowed = true;
						plugin.nextPortal = null;
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
		if (plugin.config.DISABLE_INVISIBILITY_ON_COMBAT && event.getEntity() instanceof Player) {
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
		}
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
		}
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onEntityDamage(EntityDamageEvent event) {
		if (plugin.config.INVULNERABLE_TAME_HORSES && event.getEntityType() == EntityType.HORSE) {
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

	/**
	 * Return a string describing a dropped item stack.
	 * 
	 * The string contains the material type name, data value and amount, as
	 * well as a list of enchantments. It is used in methods that log drops.
	 * 
	 * @param item the droppped item stack.
	 * @return a string describing a dropped item stack.
	 */
	public String getItemDescription(ItemStack item) {
		StringBuilder description = new StringBuilder();
		description.append(item.getAmount()).append('x').append(item.getType().name()).append(':').append(item.getData().getData());

		Map<Enchantment, Integer> enchants = item.getEnchantments();
		if (enchants.size() > 0) {
			description.append('(');
			boolean first = true;
			for (Entry<Enchantment, Integer> entry : enchants.entrySet()) {
				if (first) {
					first = false;
				} else {
					description.append(',');
				}
				description.append(entry.getKey().getName()).append(':').append(entry.getValue());
			}
			description.append(')');
		}
		return description.toString();
	}
}
