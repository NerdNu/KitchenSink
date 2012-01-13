package nu.nerd.creativemode;

import org.bukkit.Material;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerListener;
import org.bukkit.inventory.ItemStack;


class CreativePlayerListener extends PlayerListener {
    @Override
    public void onPlayerDropItem(PlayerDropItemEvent event) {
        event.setCancelled(true);
    }

    @Override
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (!event.hasItem())
            return;

        ItemStack stack = event.getItem();
        if (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK) {
            if (stack.getTypeId() == 383 || stack.getTypeId() == 332 || stack.getTypeId() == 344)
                event.setCancelled(true);
            if (stack.getType() == Material.EYE_OF_ENDER || stack.getType() == Material.BOW)
                event.setCancelled(true);
        }
    }
}