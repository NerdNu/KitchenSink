
package nu.nerd.kitchensink;

import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;


public class SprintTask extends BukkitRunnable {

    private Player player;

    public SprintTask(Player player) {
        this.player = player;
    }

    public void run() {
        if (player.isSprinting()) {
            player.setSprinting(false);
        }
    }

}
