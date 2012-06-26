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
    public boolean SAFE_MINECARTS;

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
        SAFE_MINECARTS = plugin.getConfig().getBoolean("safe-minecarts");
        DISABLED_LEFT_ITEMS = plugin.getConfig().getIntegerList("disabled-items.left-click");
        DISABLED_RIGHT_ITEMS = plugin.getConfig().getIntegerList("disabled-items.right-click");
    }
}
