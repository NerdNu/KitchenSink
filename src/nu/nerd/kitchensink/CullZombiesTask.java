package nu.nerd.kitchensink;

import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Zombie;

import java.util.Collection;

public class CullZombiesTask implements Runnable {

    @Override
    public void run() {
        try {
            Collection<LivingEntity> livingEntities = KitchenSink.PLUGIN.getServer().getWorlds().get(0).getEntitiesByClass(LivingEntity.class);
            for (LivingEntity mob : livingEntities) {
                if (mob.getType() == EntityType.ZOMBIE) {
                    Zombie zombie = (Zombie) mob;
                    if (zombie.getTarget() != null && zombie.getTarget().getType() == EntityType.VILLAGER && zombie.getRemoveWhenFarAway()) {
                        zombie.remove();
                    }
                }
            }
        } catch (Exception ignored) {
        }
    }

}
