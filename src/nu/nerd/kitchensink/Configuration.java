package nu.nerd.kitchensink;

import java.util.List;


public class Configuration
{
    private final KitchenSink plugin;

    public boolean DISABLE_DROPS;
    public boolean BLOCK_CAPS;
    public boolean SAFE_VEHICLES;

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

        DISABLE_DROPS = plugin.getConfig().getBoolean("disable-drops");
        BLOCK_CAPS = plugin.getConfig().getBoolean("block-caps");
        SAFE_VEHICLES = plugin.getConfig().getBoolean("safe-vehicles");
        DISABLED_LEFT_ITEMS = plugin.getConfig().getIntegerList("disabled-items.left-click");
        DISABLED_RIGHT_ITEMS = plugin.getConfig().getIntegerList("disabled-items.right-click");
    }
}
