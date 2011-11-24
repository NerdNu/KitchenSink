package nu.nerd.shit.stopdrops;

import java.lang.reflect.Method;
import java.util.logging.Logger;
import net.minecraft.server.Block;
import net.minecraft.server.BlockChest;
import net.minecraft.server.BlockJukeBox;
import net.minecraft.server.StepSound;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.event.Event.Priority;
import org.bukkit.event.Event.Type;

public class StopDrops extends JavaPlugin {
    private DropsPlayerListener pl = new DropsPlayerListener();

    @Override
    public void onDisable() {
        System.out.println("Drops enabled");
    }

    @Override
    public void onEnable() {
        try {
            Block.byId[54] = null;
            BlockChest chest = new DropsChest(54);
            chest.a("chest");
            Block.t[54] = true;

            Block.byId[84] = null;
            BlockJukeBox jukebox = new DropsJukeBox(84, 74);
            jukebox.a("jukebox");
            Block.t[84] = true;
        } catch (Exception err) {
            err.printStackTrace();
        }

        PluginManager pm = this.getServer().getPluginManager();
        pm.registerEvent(Type.PLAYER_DROP_ITEM, pl, Priority.Highest, this);
        pm.registerEvent(Type.PLAYER_INTERACT, pl, Priority.Highest, this);
        System.out.println("Drops disabled");
    }
}
