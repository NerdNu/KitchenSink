package nu.nerd.kitchensink;

import org.bukkit.entity.Player;

public class SprintTask implements Runnable {

    private Player player;

    SprintTask(Player player) {
        this.player = player;
    }

    @Override
    public void run() {
        if (player.isSprinting()) {
            player.setSprinting(false);
        }
    }

}
