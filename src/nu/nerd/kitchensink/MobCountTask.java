package nu.nerd.kitchensink;

import org.bukkit.World;
import org.bukkit.entity.LivingEntity;

import java.util.Collection;
import java.util.HashMap;

public class MobCountTask implements Runnable {

    @Override
    public void run() {
        System.out.println("-!- Starting Mob count");
        System.out.println("-!- " + getMultiworldMobCount());
    }

    private HashMap<String, Integer> getMultiworldMobCount() {
        HashMap<String, Integer> counts = new HashMap<>();
        try {
            for (World world : KitchenSink.PLUGIN.getServer().getWorlds()) {
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
        } catch (Exception ignored) {
        }
        return counts;
    }

}
