package nu.nerd.kitchensink;

import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.minecraft.server.Block;
import nu.nerd.kitchensink.blocks.NoDropsChest;
import nu.nerd.kitchensink.blocks.NoDropsJukeBox;
import org.bukkit.plugin.java.JavaPlugin;

public class KitchenSink extends JavaPlugin {
    private KitchenSinkListener listener = new KitchenSinkListener(this);
    public final Configuration config = new Configuration(this);
    public final static Logger log = Logger.getLogger("Minecraft");

    @Override
    public void onDisable() {
        sendToLog(Level.INFO, getDescription().getVersion() + " disabled.");
    }

    @Override
    public void onEnable() {
        File config_file = new File(getDataFolder(), "config.yml");
        if (!config_file.exists()) {
            getConfig().options().copyDefaults(true);
            saveConfig();
        }

        config.load();

        if (config.DISABLE_DROPS) {
            try {
                Block.byId[54] = null;
                NoDropsChest chest = new NoDropsChest(54);
                chest.a("chest");
                Block.t[54] = true;

                Block.byId[84] = null;
                NoDropsJukeBox jukebox = new NoDropsJukeBox(84, 74);
                jukebox.a("jukebox");
                Block.t[84] = true;
            } catch (Exception err) {
                sendToLog(Level.WARNING, err.getMessage() + "\n" + err.getStackTrace().toString());
            }
        }

        sendToLog(Level.INFO, getDescription().getVersion() + " enabled.");
    }

    public void sendToLog(Level level, String message) {
        log.log(level, "[{0}] {1}", new Object[]{getDescription().getName(), message});
    }
}
