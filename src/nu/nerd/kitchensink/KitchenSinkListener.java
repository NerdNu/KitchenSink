package nu.nerd.kitchensink;

import java.text.Normalizer;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.logging.Level;

import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Ageable;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Sheep;
import org.bukkit.entity.Vehicle;
import org.bukkit.entity.Villager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.EntityBlockFormEvent;
import org.bukkit.event.block.BlockDispenseEvent;
import org.bukkit.event.enchantment.PrepareItemEnchantEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.ItemSpawnEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.BrewEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerShearEntityEvent;
import org.bukkit.event.vehicle.VehicleExitEvent;
import org.bukkit.event.world.PortalCreateEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffectType;

class KitchenSinkListener implements Listener {
	private static final String REPLACED_CHARS = "[ \\s\\u000a\\u000d\\u2028\\u2029\\u0009\\u000b\\u000c\\u000d\\u0020\\u00a0\\u1680\\u180e\\u2000\\u2001\\u2002\\u2003\\u2004\\u2005\\u2006\\u2007\\u2008\\u2009\\u200a\\u202f\\u205f\\u3000]{2,}";

	/**
	 * Material types of blocks that can be interacted with on right click.
	 */
	private static final HashSet<Integer> INTERACTABLE_TYPES;
	static {
		INTERACTABLE_TYPES = new HashSet<Integer>(Arrays.asList(23, 25, 26, 54, 58, 61, 62, 64, 69, 71, 77, 84, 92, 93, 94, 95, 96, 107, 116, 117, 122, 130, 138, 143, 145, 146, 149, 150, 154, 158));
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
		if (event.getPlayer().hasPermission("kitchensink.admin")) {
			event.allow();
			return;
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
			if (plugin.config.DISABLED_RIGHT_ITEMS.contains(stack.getTypeId()))
				event.setCancelled(true);
		}

		if (event.getAction() == Action.LEFT_CLICK_AIR || event.getAction() == Action.LEFT_CLICK_BLOCK) {
			if (plugin.config.DISABLED_LEFT_ITEMS.contains(stack.getTypeId()))
				event.setCancelled(true);
		}
	}

	@EventHandler(priority = EventPriority.NORMAL)
	public void onPlayerInteractEntity(PlayerInteractEntityEvent event) {
		if (!plugin.config.BLOCK_VILLAGERS) {
			return;
		}
		Entity e = event.getRightClicked();
		if (e != null) {
			if (e instanceof Villager) {
				Villager v = (Villager) e;
				v.setTarget(event.getPlayer());
				event.getPlayer().damage(1, v);
				event.setCancelled(true);
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
					plugin.sendToLog(Level.INFO, "[MobKill] " + killer.getName() + "|" + event.getEntityType().name() + "|" + l.getWorld().getName()
									+ "|" + l.getX() + "|" + l.getY() + "|" + l.getZ() + "| C[" + c.getX() + "," + c.getZ() + "]");
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
			String loot = "[drops]" + player.getName();
			for (ItemStack is : event.getDrops()) {
				loot += " ," + is.getTypeId() + ":" + is.getAmount();
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

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onPortalCreate(PortalCreateEvent event) {
		if (event.isCancelled())
			return;

		if (plugin.config.SAFE_PORTALS) {
			event.setCancelled(true);
			for (Block b : event.getBlocks()) {
				b.setTypeId(0);
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
	}
}
