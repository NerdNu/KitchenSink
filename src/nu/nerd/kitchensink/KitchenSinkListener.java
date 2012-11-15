package nu.nerd.kitchensink;

import java.text.Normalizer;
import java.util.List;
import java.util.logging.Level;

import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Ageable;
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

// Potions

class KitchenSinkListener implements Listener {
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
    	if(event.getPlayer().hasPermission("kitchensink.admin")) {
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
    	//Begin old KS stuff
        if (!event.hasItem()) {
            return;
        }
        if (plugin.config.PEARL_DAMAGE > 0){
            if ((event.getAction() == Action.RIGHT_CLICK_BLOCK || event.getAction() == Action.RIGHT_CLICK_AIR) && event.getItem().getType() == Material.ENDER_PEARL ) {
                event.getPlayer().damage(plugin.config.PEARL_DAMAGE);
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
        if(!plugin.config.BLOCK_VILLAGERS){
            return;
        }
        Entity e = event.getRightClicked();
        if(e != null) {
            if(e instanceof Villager) {
                Villager v = (Villager) e;
                v.setTarget(event.getPlayer());
                event.getPlayer().damage(1, v);
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onPlayerChat(AsyncPlayerChatEvent  event) {
        if (event.isCancelled())
            return;

        String message = event.getMessage();
        message = ChatColor.stripColor(message);
        message = message.replaceAll("[ \\s\\u000a\\u000d\\u2028\\u2029\\u0009\\u000b\\u000c\\u000d\\u0020\\u00a0\\u1680\\u180e\\u2000\\u2001\\u2002\\u2003\\u2004\\u2005\\u2006\\u2007\\u2008\\u2009\\u200a\\u202f\\u205f\\u3000]{2,}", " ");
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
    	if(plugin.config.DISABLE_SNOW) {
    		if(event.getBlock().getRelative(BlockFace.DOWN).getType() != Material.OBSIDIAN){
    			event.setCancelled(true);
    		}
    	}
    }
    
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onBlockDispense(BlockDispenseEvent event) {
    	if(plugin.config.SAFE_DISPENSERS) {
    		if(plugin.config.DISABLE_DISPENSED.contains(event.getItem().getTypeId())) {
    			event.setCancelled(true);
    		}
    	}
    }
    
    @EventHandler(priority = EventPriority.MONITOR)
    public void onEntityDeath(EntityDeathEvent event) {
        if (event.getEntity() instanceof Ageable) {
            if (event.getEntity().getKiller() instanceof Player) {
                if (plugin.config.LOG_ANIMAL_DEATH) {
                    String player = event.getEntity().getKiller().getName();
                    Location l = event.getEntity().getLocation();
                    Chunk c = l.getChunk();
                    plugin.sendToLog(Level.INFO, "[MobKill] " + player + "|" + event.getEntityType().name() + "|" + l.getWorld().getName() + "|" + l.getX() + "|" + l.getY() + "|" + l.getZ() + "| C[" + c.getX() + "," + c.getZ() + "]");
                }
                if (plugin.config.BUFF_DROPS != 0) {
                    List<ItemStack> items = event.getDrops();
                    Location l = event.getEntity().getLocation();
                    for (int i = 0; i < plugin.config.BUFF_DROPS; i++) {
                        for (ItemStack a : items) {
                        	if (!plugin.config.DISABLE_BUFF.contains(a.getTypeId())){
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
	Player player = (Player)event.getEntity();
   	if(plugin.config.LOG_PLAYER_DROPS) {
            String loot = "[drops]" + player.getName();
            for(ItemStack is : event.getDrops()) {
                loot += " ," + is.getTypeId() + ":" + is.getAmount();
            }
            plugin.getLogger().info(loot);
	}
    }
    
    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerShearEntity(PlayerShearEntityEvent event) {
        if (plugin.config.BUFF_SHEAR_DROPS != 0 && event.getEntity() instanceof Sheep) {
            Sheep entity = (Sheep) event.getEntity();
            Location l = entity.getLocation();
            for (int i = 0; i < plugin.config.BUFF_SHEAR_DROPS; i++){
            	l.getWorld().dropItemNaturally(l, new ItemStack(Material.WOOL, plugin.config.BUFF_SHEAR_DROPS, (byte)entity.getColor().ordinal()));
            }
        }
    }
    
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPortalCreate(PortalCreateEvent event) {
        if (event.isCancelled())
            return;
        
        if (plugin.config.SAFE_PORTALS) {
            event.setCancelled(true);
            for(Block b : event.getBlocks()) {
                b.setTypeId(0);
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onBlockBreak(BlockBreakEvent event) {
        if(event.isCancelled()) {
            return;
        }
        if(plugin.config.SAFE_ICE) {
            if( event.getBlock().getType() == Material.ICE && !event.getPlayer().getGameMode().equals(GameMode.CREATIVE)) {
                event.getBlock().setType(Material.AIR);
            }
        }
    }
    
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onEnchantItem(PrepareItemEnchantEvent event) {
        if(!plugin.config.ALLOW_ENCH_ITEMS.isEmpty()) {
            if(!plugin.config.ALLOW_ENCH_ITEMS.contains(event.getItem().getTypeId())) {
                event.setCancelled(true);
            }
        }
    }
    
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onBrew(BrewEvent event) {
        if(!plugin.config.BLOCK_BREW.isEmpty()) {
            if(plugin.config.BLOCK_BREW.contains(event.getContents().getIngredient().getTypeId())) {
                event.setCancelled(true);
            }
        }
    }
}
