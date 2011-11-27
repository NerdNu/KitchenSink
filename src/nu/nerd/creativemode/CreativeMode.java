package nu.nerd.creativemode;

import java.util.logging.Level;
import java.util.logging.Logger;
import net.minecraft.server.Block;
import net.minecraft.server.BlockChest;
import net.minecraft.server.BlockEnderPortal;
import net.minecraft.server.BlockJukeBox;
import net.minecraft.server.Material;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.event.Event.Priority;
import org.bukkit.event.Event.Type;

public class CreativeMode extends JavaPlugin {
    private CreativePlayerListener pl = new CreativePlayerListener();
    public final static Logger log = Logger.getLogger("Minecraft");

    @Override
    public void onDisable() {
        log.log(Level.INFO, "[" + getDescription().getName() + "] " + getDescription().getVersion() + " disabled.");
    }

    @Override
    public void onEnable() {
        try {
            Block.byId[54] = null;
            BlockChest chest = new CreativeChest(54);
            chest.a("chest");
            Block.t[54] = true;

            Block.byId[84] = null;
            BlockJukeBox jukebox = new CreativeJukeBox(84, 74);
            jukebox.a("jukebox");
            Block.t[84] = true;

            Block.byId[119] = null;
            BlockEnderPortal portal = new CreativeEnderPortal(119, Material.PORTAL);
        } catch (Exception err) {
            err.printStackTrace();
        }

        PluginManager pm = this.getServer().getPluginManager();
        pm.registerEvent(Type.PLAYER_DROP_ITEM, pl, Priority.Highest, this);
        pm.registerEvent(Type.PLAYER_INTERACT, pl, Priority.Highest, this);

        log.log(Level.INFO, "[" + getDescription().getName() + "] " + getDescription().getVersion() + " enabled.");
    }
}
