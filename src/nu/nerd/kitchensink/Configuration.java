package nu.nerd.kitchensink;

import java.util.List;


public class Configuration
{
    private final KitchenSink plugin;

    public boolean LOG_ANIMAL_DEATH;
    public boolean DISABLE_SNOW;
    public boolean DISABLE_DROPS;
    public boolean BLOCK_CAPS;
    public boolean SAFE_BOATS;
    public int SAFE_BOATS_DELAY;
    public boolean SAFE_BOATS_DROP;
    public boolean SAFE_MINECARTS;
    public int SAFE_MINECARTS_DELAY;
    public boolean SAFE_MINECARTS_DROP;

    public List<Integer> DISABLED_LEFT_ITEMS;
    public List<Integer> DISABLED_RIGHT_ITEMS;

    public Configuration(KitchenSink instance)
    {
        plugin = instance;
    }

    public void save()
    {
        plugin.saveConfig();
    }

    public void load()
    {
        plugin.reloadConfig();

        LOG_ANIMAL_DEATH = plugin.getConfig().getBoolean("log-animals");
        DISABLE_SNOW = plugin.getConfig().getBoolean("disable-snowgrow");
        DISABLE_DROPS = plugin.getConfig().getBoolean("disable-drops");
        BLOCK_CAPS = plugin.getConfig().getBoolean("block-caps");
        SAFE_BOATS = plugin.getConfig().getBoolean("safe-boats");
        SAFE_BOATS_DELAY = plugin.getConfig().getInt("safe-boats-delay");
        SAFE_BOATS_DROP = plugin.getConfig().getBoolean("safe-boats-drop");
        SAFE_MINECARTS = plugin.getConfig().getBoolean("safe-minecarts");
        SAFE_MINECARTS_DELAY = plugin.getConfig().getInt("safe-minecarts-delay");
        SAFE_MINECARTS_DROP = plugin.getConfig().getBoolean("safe-minecarts-drop");
        DISABLED_LEFT_ITEMS = plugin.getConfig().getIntegerList("disabled-items.left-click");
        DISABLED_RIGHT_ITEMS = plugin.getConfig().getIntegerList("disabled-items.right-click");
    }
}
