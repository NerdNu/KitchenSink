package nu.nerd.kitchensink;

import java.util.List;

public class Configuration {
	private final KitchenSink plugin;

	public boolean ANIMAL_COUNT;
	public boolean LOG_ANIMAL_DEATH;
	public boolean LOG_PLAYER_DROPS;
	public int BUFF_DROPS;
	public int BUFF_SHEAR_DROPS;
	public boolean DISABLE_SNOW;
	public boolean DISABLE_DROPS;
	public boolean DISABLE_INVISIBILITY_ON_COMBAT;
	public boolean LOWER_STRENGTH_POTION_DAMAGE;
	public boolean BLOCK_CAPS;
	public boolean BLOCK_VILLAGERS;
	public boolean SAFE_ICE;
	public boolean SAFE_DISPENSERS;
	public boolean SAFE_BOATS;
	public int SAFE_BOATS_DELAY;
	public boolean SAFE_BOATS_DROP;
	public boolean SAFE_MINECARTS;
	public int SAFE_MINECARTS_DELAY;
	public boolean SAFE_MINECARTS_DROP;
	public boolean SAFE_SPECIAL_CARTS;
	public boolean REMOVE_ON_EXIT;
	public boolean SAFE_PORTALS;
	public int PEARL_DAMAGE;
	public boolean LEATHERLESS_BOOKS;
	public boolean LOCK_HORSES;
	public boolean INVULNERABLE_TAME_HORSES;
	public boolean HORSE_RECIPES;
	public int RESTART_TIME;
	public long NEXT_RESTART;

	public List<Integer> ALLOW_ENCH_ITEMS;
	public List<Integer> BLOCK_BREW;
	public List<Integer> DISABLED_LEFT_ITEMS;
	public List<Integer> DISABLED_RIGHT_ITEMS;
	public List<Integer> DISABLE_DISPENSED;
	public List<Integer> DISABLE_BUFF;

	public Configuration(KitchenSink instance) {
		plugin = instance;
	}

	public void save() {
		plugin.saveConfig();
	}

	public void load() {
		plugin.reloadConfig();

		ANIMAL_COUNT = plugin.getConfig().getBoolean("animal-count");
		LOG_ANIMAL_DEATH = plugin.getConfig().getBoolean("log-animals");
		LOG_PLAYER_DROPS = plugin.getConfig().getBoolean("log-player-drops");
		BUFF_DROPS = plugin.getConfig().getInt("buff-drops");
		BUFF_SHEAR_DROPS = plugin.getConfig().getInt("buff-shear-drops");
		DISABLE_SNOW = plugin.getConfig().getBoolean("disable-snowgrow");
		DISABLE_DROPS = plugin.getConfig().getBoolean("disable-drops");
		DISABLE_INVISIBILITY_ON_COMBAT = plugin.getConfig().getBoolean("disable-invisibility-on-combat");
		LOWER_STRENGTH_POTION_DAMAGE = plugin.getConfig().getBoolean("lower-strength-potion-damage");
		BLOCK_CAPS = plugin.getConfig().getBoolean("block-caps");
		BLOCK_VILLAGERS = plugin.getConfig().getBoolean("block-villagers");
		SAFE_ICE = plugin.getConfig().getBoolean("safe-ice");
		SAFE_DISPENSERS = plugin.getConfig().getBoolean("safe-dispensers");
		SAFE_BOATS = plugin.getConfig().getBoolean("safe-boats");
		SAFE_BOATS_DELAY = plugin.getConfig().getInt("safe-boats-delay");
		SAFE_BOATS_DROP = plugin.getConfig().getBoolean("safe-boats-drop");
		SAFE_MINECARTS = plugin.getConfig().getBoolean("safe-minecarts");
		SAFE_MINECARTS_DELAY = plugin.getConfig().getInt("safe-minecarts-delay");
		SAFE_MINECARTS_DROP = plugin.getConfig().getBoolean("safe-minecarts-drop");
		SAFE_SPECIAL_CARTS = plugin.getConfig().getBoolean("safe-special-carts");
		REMOVE_ON_EXIT = plugin.getConfig().getBoolean("remove-on-exit");
		SAFE_PORTALS = plugin.getConfig().getBoolean("safe-portals");
		PEARL_DAMAGE = plugin.getConfig().getInt("pearl-damage");
		LEATHERLESS_BOOKS = plugin.getConfig().getBoolean("leatherless-books");
		LOCK_HORSES = plugin.getConfig().getBoolean("lock-horses");
		HORSE_RECIPES = plugin.getConfig().getBoolean("horse-recipes");
		INVULNERABLE_TAME_HORSES = plugin.getConfig().getBoolean("invulnerable-tame-horses");
		ALLOW_ENCH_ITEMS = plugin.getConfig().getIntegerList("allow-enchant-items");
		BLOCK_BREW = plugin.getConfig().getIntegerList("block-brew");
		DISABLED_LEFT_ITEMS = plugin.getConfig().getIntegerList("disabled-items.left-click");
		DISABLED_RIGHT_ITEMS = plugin.getConfig().getIntegerList("disabled-items.right-click");
		DISABLE_DISPENSED = plugin.getConfig().getIntegerList("disabled-items.dispensed");
		DISABLE_BUFF = plugin.getConfig().getIntegerList("disable-buff");
		RESTART_TIME = plugin.getConfig().getInt("restart-time", 7200);
	}
}
