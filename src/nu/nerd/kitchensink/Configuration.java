package nu.nerd.kitchensink;

import java.util.List;


public class Configuration
{
    private final KitchenSink plugin;

    public boolean LOG_ANIMAL_DEATH;
    public int BUFF_DROPS;
    public int BUFF_SHEAR_DROPS;
    public boolean DISABLE_SNOW;
    public boolean DISABLE_DROPS;
    public boolean BLOCK_CAPS;
    public boolean SAFE_BOATS;
    public int SAFE_BOATS_DELAY;
    public boolean SAFE_BOATS_DROP;
    public boolean SAFE_MINECARTS;
    public int SAFE_MINECARTS_DELAY;
    public boolean SAFE_MINECARTS_DROP;
    public boolean REMOVE_ON_EXIT;
    public boolean SAFE_SPECIAL_CARTS;
    public boolean ANIMAL_COUNT;
    public int PEARL_DAMAGE;

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
        BUFF_DROPS = plugin.getConfig().getInt("buff-drops");
        BUFF_SHEAR_DROPS = plugin.getConfig().getInt("buff-shear-drops");
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
        REMOVE_ON_EXIT = plugin.getConfig().getBoolean("remove-on-exit");
        SAFE_SPECIAL_CARTS = plugin.getConfig().getBoolean("safe-special-carts");
        ANIMAL_COUNT = plugin.getConfig().getBoolean("animal-count");
        PEARL_DAMAGE = plugin.getConfig().getInt("pearl-damage");
    }
}
