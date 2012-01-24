package nu.nerd.kitchensink;

import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;

public class KitchenSink extends JavaPlugin {
    private KitchenSinkListener listener = new KitchenSinkListener(this);
    private LagCheck lagCheck = new LagCheck();
    public final Configuration config = new Configuration(this);
    public final static Logger log = Logger.getLogger("Minecraft");

    @Override
    public void onDisable() {
        getServer().getScheduler().cancelTasks(this);
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

        getServer().getScheduler().scheduleSyncRepeatingTask(this, lagCheck, 20, 20);
        getServer().getPluginManager().registerEvents(listener, this);

        sendToLog(Level.INFO, getDescription().getVersion() + " enabled.");
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String name, String[] args) {
        boolean success = false;

        if (command.getName().equalsIgnoreCase("lag")) {
            if (sender.hasPermission("kitchensink.lag")) {
                sendLagStats(sender);
                success = true;
            }
        }

        return success;
    }

    public void sendLagStats(CommandSender sender) {
        float tps = 0;
        for (Long l : lagCheck.history) {
            if (l != null)
                tps += 20 / (l / 1000);
        }
        tps = tps / lagCheck.history.size();

        long memUsed = (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) / 1048576;
        long memMax = Runtime.getRuntime().maxMemory() / 1048576;

        sender.sendMessage("TPS: " + tps + " Mem: " + memUsed + "M/" + memMax + "M");
    }

    public void sendToLog(Level level, String message) {
        log.log(level, "[" + getDescription().getName() + "] " + message);
    }
}
