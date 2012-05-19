package nu.nerd.kitchensink;

import java.util.ArrayList;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.craftbukkit.inventory.CraftItemStack;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Minecart;
import org.bukkit.entity.Vehicle;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.ItemSpawnEvent;
import org.bukkit.event.player.PlayerChatEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;
import org.bukkit.event.vehicle.VehicleDestroyEvent;
import org.bukkit.event.vehicle.VehicleExitEvent;
import org.bukkit.inventory.ItemStack;


class KitchenSinkListener implements Listener {
    private final KitchenSink plugin;
    private final ArrayList<Integer> destroyed = new ArrayList<Integer>();
    private final ArrayList<Integer> exit = new ArrayList<Integer>();

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
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (!event.hasItem())
            return;

        ItemStack stack = event.getItem();
        if (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK) {
            if (plugin.config.DISABLED_RIGHT_ITEMS.contains(stack.getTypeId()))
                event.setCancelled(true);
        }

        if (event.getAction() == Action.LEFT_CLICK_AIR || event.getAction() == Action.LEFT_CLICK_BLOCK) {
            if (plugin.config.DISABLED_LEFT_ITEMS.contains(stack.getTypeId()))
                event.setCancelled(true);
        }

        if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
            if (event.isCancelled())
                return;

            Block block = event.getClickedBlock();
            Location loc = block.getLocation();

            /*if (stack.getType() == Material.BOAT && plugin.config.SAFE_BOATS) {
                loc.setX(loc.getX() + 0.5);
                loc.setY(loc.getY() + 1);
                loc.setZ(loc.getZ() + 0.5);
 
                if (block.getType() == Material.SNOW)
                    loc.setY(loc.getY() - 1);

                if (event.getPlayer().getVehicle() != null) {
                    VehicleExitEvent e = new VehicleExitEvent((Vehicle) event.getPlayer().getVehicle(), event.getPlayer());
                    onVehicleExit(e);
                }

                Boat boat = loc.getWorld().spawn(loc, Boat.class);
                boat.setPassenger(event.getPlayer());
                event.setCancelled(true);
                if (event.getPlayer().getGameMode() == GameMode.SURVIVAL)
                    --((CraftItemStack)stack).getHandle().count;
            }*/

            if (stack.getType() == Material.MINECART && plugin.config.SAFE_MINECARTS) {
                if (block.getType() == Material.RAILS || block.getType() == Material.POWERED_RAIL || block.getType() == Material.DETECTOR_RAIL) {
                    loc.setX(loc.getX() + 0.5);
                    loc.setY(loc.getY() + 0.5);
                    loc.setZ(loc.getZ() + 0.5);

                    if (event.getPlayer().getVehicle() != null) {
                        VehicleExitEvent e = new VehicleExitEvent((Vehicle) event.getPlayer().getVehicle(), event.getPlayer());
                        onVehicleExit(e);
                    }

                    Minecart minecart = loc.getWorld().spawn(loc, Minecart.class);
                    minecart.setPassenger(event.getPlayer());
                    event.setCancelled(true);
                    if (event.getPlayer().getGameMode() == GameMode.SURVIVAL)
                        --((CraftItemStack)stack).getHandle().count;
                }
            }
        }
    }

    @EventHandler
    public void onPlayerChat(PlayerChatEvent event) {
        if (event.isCancelled())
            return;

        String message = event.getMessage();
        message = ChatColor.stripColor(message);
        message = message.replaceAll("[ \\s\\u000a\\u000d\\u2028\\u2029\\u0009\\u000b\\u000c\\u000d\\u0020\\u00a0\\u1680\\u180e\\u2000\\u2001\\u2002\\u2003\\u2004\\u2005\\u2006\\u2007\\u2008\\u2009\\u200a\\u202f\\u205f\\u3000]{2,}", " ");
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

    @EventHandler
    public void onPlayerTeleport(PlayerTeleportEvent event) {
        if (event.isCancelled())
            return;

        Chunk chunk = event.getPlayer().getWorld().getChunkAt(event.getTo());
        event.getPlayer().getWorld().refreshChunk(chunk.getX(), chunk.getZ());

        if (event.getCause() != TeleportCause.UNKNOWN) {
            Entity vehicle = event.getPlayer().getVehicle();
            /*if (vehicle instanceof Boat && plugin.config.SAFE_BOATS)
                vehicle.remove();*/
            if (vehicle instanceof Minecart && plugin.config.SAFE_MINECARTS)
                vehicle.remove();
        }
    }

    @EventHandler
    public void onVehicleDestroy(VehicleDestroyEvent event) {
        if (event.isCancelled())
            return;

        Vehicle vehicle = event.getVehicle();
        
        if(exit.contains(vehicle.getEntityId()))
        {
        	event.setCancelled(true);
        	exit.remove((Integer)vehicle.getEntityId());
        	return;
        }
        
        /*if (vehicle instanceof Boat && plugin.config.SAFE_BOATS) {
            destroyed.add(vehicle.getEntityId());
        }*/

        if (vehicle instanceof Minecart && plugin.config.SAFE_MINECARTS) {
            destroyed.add(vehicle.getEntityId());
        }
    }

    @EventHandler
    public void onVehicleExit(VehicleExitEvent event) {
        if (event.isCancelled())
            return;

        Vehicle vehicle = event.getVehicle();
        // if we already removed it don't do it again
        if (vehicle.isDead())
            return;

        Location loc = vehicle.getLocation();

        // don't give them a boat/minecart if they broke it
        if (destroyed.contains(vehicle.getEntityId())) {
            destroyed.remove((Integer)vehicle.getEntityId());
            return;
        }
        

        /*if (vehicle instanceof Boat && plugin.config.SAFE_BOATS) {
            loc.getWorld().dropItem(loc, new ItemStack(Material.BOAT, 1));
            vehicle.remove();
        }*/
        if (vehicle instanceof Minecart && plugin.config.SAFE_MINECARTS) {
            loc.getWorld().dropItem(loc, new ItemStack(Material.MINECART, 1));
            exit.add(vehicle.getEntityId());
            vehicle.remove();
        }
    }
}
