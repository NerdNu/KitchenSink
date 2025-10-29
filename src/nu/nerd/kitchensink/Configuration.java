package nu.nerd.kitchensink;

import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.EntityType;

import java.util.EnumMap;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

enum countdown {
    maxtime, format, color, style, msgcolor, msgstyle
}

public class Configuration {

    private final KitchenSink plugin;

    public boolean DEBUG_DISABLE_TNT;

    public boolean HOOK_COREPROTECT;

    public int BUFF_DROPS;
    public int BUFF_SHEAR_DROPS;
    public boolean DISABLE_SNOW;
    public boolean DISABLE_DROPS;
    public boolean DISABLE_TNT;
    public boolean DISABLE_GOLEM_NATURAL_SPAWN;
    public boolean ENABLE_SPIGOT_MERGING;

    public boolean BLOCK_CAPS;
    public boolean BLOCK_VILLAGERS;
    public boolean BLOCK_JOHNNY;
    public boolean BLOCK_SLIME_MOVING_RAILS_AND_CARPETS;

    public boolean SAFE_ICE;
    public boolean SAFE_DISPENSERS;
    public boolean SAFE_PORTALS;
    public boolean WARN_RESTART_ON_JOIN;
    public boolean WARN_RESTART_ON_INVENTORY_OPEN;
    public int COUNTDOWN_MAX_TIME;
    public String COUNTDOWN_FORMAT;
    public String COUNTDOWN_COLOR;
    public String COUNTDOWN_STYLE;
    public String COUNTDOWN_MSG_COLOR;
    public String COUNTDOWN_MSG_STYLE;
    public List<String> RESTART_TIMES;

    HashSet<Material> DISABLED_LEFT_ITEMS;
    HashSet<Material> DISABLED_RIGHT_ITEMS;
    HashSet<Material> DISABLE_DISPENSED;
    HashSet<Material> DISABLE_BUFF;

    public Map<EntityType, Set<Material>> DISABLED_DROPS;
    public EnumSet<EntityType> DISABLE_ENTITY_BLOCK_DAMAGE = EnumSet.noneOf(EntityType.class);
    public boolean ALLOW_EGG_HATCHING;
    public boolean DISABLE_PLAYER_DAMAGE_TO_VILLAGERS;
    public boolean NORMALIZE_CHAT;

    public Configuration(KitchenSink instance) {
        plugin = instance;
    }

    public void load() {
        plugin.reloadConfig();

        DEBUG_DISABLE_TNT = plugin.getConfig().getBoolean("debug.disable-tnt");
        BUFF_DROPS = plugin.getConfig().getInt("buff-drops");
        BUFF_SHEAR_DROPS = plugin.getConfig().getInt("buff-shear-drops");
        DISABLE_SNOW = plugin.getConfig().getBoolean("disable-snowgrow");
        DISABLE_DROPS = plugin.getConfig().getBoolean("disable-drops");
        DISABLE_TNT = plugin.getConfig().getBoolean("disable-tnt");
        DISABLE_GOLEM_NATURAL_SPAWN = plugin.getConfig().getBoolean("disable-golem-natural-spawn", false);
        ENABLE_SPIGOT_MERGING = plugin.getConfig().getBoolean("enable-spigot-item-merging", true);

        BLOCK_CAPS = plugin.getConfig().getBoolean("block-caps");
        BLOCK_VILLAGERS = plugin.getConfig().getBoolean("block-villagers");
        BLOCK_JOHNNY = plugin.getConfig().getBoolean("block-johnny", false);
        BLOCK_SLIME_MOVING_RAILS_AND_CARPETS = plugin.getConfig().getBoolean("block-slime-moving-rails-and-carpets");

        HOOK_COREPROTECT = plugin.getConfig().getBoolean("use-logblock", false);

        SAFE_ICE = plugin.getConfig().getBoolean("safe-ice");
        SAFE_DISPENSERS = plugin.getConfig().getBoolean("safe-dispensers");
        SAFE_PORTALS = plugin.getConfig().getBoolean("safe-portals");

        DISABLED_LEFT_ITEMS = getMaterialList("disabled-items.left-click");
        DISABLED_RIGHT_ITEMS = getMaterialList("disabled-items.right-click");
        DISABLE_DISPENSED = getMaterialList("disabled-items.dispensed");
        DISABLE_BUFF = getMaterialList("disable-buff");

        DISABLED_DROPS = new EnumMap<>(EntityType.class);
        ConfigurationSection disabledDropsSection = plugin.getConfig().getConfigurationSection("disabled-drops");
        if (disabledDropsSection != null) {
            for (String key : disabledDropsSection.getKeys(false)) {
                try {
                    EntityType type = EntityType.valueOf(key.toUpperCase());
                    List<String> matStrings = disabledDropsSection.getStringList(key);
                    Set<Material> mats = EnumSet.noneOf(Material.class);
                    for (String matString : matStrings) {
                        try {
                            mats.add(Material.valueOf(matString.toUpperCase()));
                        } catch (IllegalArgumentException e) {
                            plugin.getLogger().warning("disabled-drops." + key + " contains an invalid material" + matString);
                        }
                    }
                    DISABLED_DROPS.put(type, mats);
                } catch (IllegalArgumentException e) {
                    plugin.getLogger().warning("disabled-drops contains invalid entity type " + key);
                }
            }
        }
        for (String entityTypeName : plugin.getConfig().getStringList("disable-entity-block-damage")) {
            try {
                EntityType entityType = EntityType.valueOf(entityTypeName);
                DISABLE_ENTITY_BLOCK_DAMAGE.add(entityType);
            } catch (IllegalArgumentException ex) {
                plugin.getLogger().warning("disable-entity-block-damage contains invalid entity type " + entityTypeName);
            }
        }
        WARN_RESTART_ON_JOIN = plugin.getConfig().getBoolean("warn-restart-on-join");
        WARN_RESTART_ON_INVENTORY_OPEN = plugin.getConfig().getBoolean("warn-restart-on-inventory-open");
        COUNTDOWN_MAX_TIME = plugin.getConfig().getInt("countdown.maxtime", 15);
        COUNTDOWN_FORMAT = plugin.getConfig().getString("countdown.format", ">>$s<<");
        if (!COUNTDOWN_FORMAT.contains("$s")) {
            COUNTDOWN_FORMAT = ">>$s<<";
        }
        COUNTDOWN_COLOR = plugin.getConfig().getString("countdown.color", "&c");
        COUNTDOWN_STYLE = plugin.getConfig().getString("countdown.style", "&l");
        COUNTDOWN_MSG_COLOR = plugin.getConfig().getString("countdown.msgColor", "&a");
        COUNTDOWN_MSG_STYLE = plugin.getConfig().getString("countdown.msgStyle", "&l");
        RESTART_TIMES = plugin.getConfig().getStringList("restart-times");
        ALLOW_EGG_HATCHING = plugin.getConfig().getBoolean("allow-egg-hatching", true);
        DISABLE_PLAYER_DAMAGE_TO_VILLAGERS = plugin.getConfig().getBoolean("disable-player-damage-to-villagers", false);
        NORMALIZE_CHAT = plugin.getConfig().getBoolean("normalize-chat", true);
    }

    protected void setCountDownSetting(countdown setting, Object value) {
        plugin.reloadConfig();
        switch (setting) {
        case maxtime:
            COUNTDOWN_MAX_TIME = (Integer) value;
            plugin.getConfig().set("countdown.maxTime", value);
            break;
        case format:
            COUNTDOWN_FORMAT = (String) value;
            plugin.getConfig().set("countdown.format", value);
            break;
        case color:
            COUNTDOWN_COLOR = (String) value;
            plugin.getConfig().set("countdown.color", value);
            break;
        case style:
            COUNTDOWN_STYLE = (String) value;
            plugin.getConfig().set("countdown.style", value);
            break;
        case msgcolor:
            COUNTDOWN_MSG_COLOR = (String) value;
            plugin.getConfig().set("countdown.msgcolor", value);
        case msgstyle:
            COUNTDOWN_MSG_STYLE = (String) value;
            plugin.getConfig().set("countdown.msgstyle", value);
        }
        plugin.saveConfig();
    }

    // ------------------------------------------------------------------------
    /**
     * Turns a list of Strings into a set of Materials.
     *
     * @param key the configuration key associated with the string list.
     * @return a set of materials.
     */
    private HashSet<Material> getMaterialList(String key) {
        HashSet<Material> materials = new HashSet<>();
        for (String materialName : plugin.getConfig().getStringList(key)) {
            try {
                materials.add(Material.valueOf(materialName));
            } catch (Exception e) {
                plugin.getLogger().info("Bad material name in config: " + materialName + " under " + key);
            }
        }
        return materials;
    }

}
