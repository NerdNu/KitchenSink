package nu.nerd.kitchensink;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.bukkit.ChatColor;

import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.ShapelessRecipe;
import org.bukkit.plugin.java.JavaPlugin;

public class KitchenSink extends JavaPlugin {
	private KitchenSinkListener listener = new KitchenSinkListener(this);
	private LagCheck lagCheck = new LagCheck();
	public final Configuration config = new Configuration(this);
	public final static Logger log = Logger.getLogger("Minecraft");
        public final List<Recipe> recipeList = new ArrayList<Recipe>();

	@Override
	public void onDisable() {
		getServer().getScheduler().cancelTasks(this);
		sendToLog(Level.INFO, getDescription().getVersion() + " disabled.");
                
                Iterator<Recipe> recipeIterator = getServer().recipeIterator();
                while(recipeIterator.hasNext()){
                    Recipe r = recipeIterator.next();
                    if(recipeList.contains(r)){
                        recipeIterator.remove();
                        recipeList.remove(r);
                    }
                }
	}

	@Override
	public void onEnable() {
		File config_file = new File(getDataFolder(), "config.yml");
		if (!config_file.exists()) {
			getConfig().options().copyDefaults(true);
			saveConfig();
		}

		config.load();

		if (config.SAFE_BOATS) {
			getServer().getScheduler().scheduleSyncRepeatingTask(this, new Runnable() {
				public void run() {
					for (World world : getServer().getWorlds()) {
						for (Boat boat : world.getEntitiesByClass(Boat.class)) {
							if (boat.isEmpty()) {
								boat.remove();
								if (config.SAFE_BOATS_DROP) {
									world.dropItem(boat.getLocation(), new ItemStack(Material.BOAT, 1));
								}
							}
						}
					}
				}
			}, config.SAFE_BOATS_DELAY, config.SAFE_BOATS_DELAY);
		}

		if (config.SAFE_MINECARTS) {
			getServer().getScheduler().scheduleSyncRepeatingTask(this, new Runnable() {
				public void run() {
					for (World world : getServer().getWorlds()) {
						for (Minecart minecart : world.getEntitiesByClass(Minecart.class)) {
							if (minecart.isEmpty()) {
                                                            if (config.SAFE_SPECIAL_CARTS) {
                                                                if (minecart instanceof StorageMinecart || minecart instanceof PoweredMinecart) {
                                                                    continue;
                                                                }
                                                            } 
                                                            minecart.remove();
                                                            if (config.SAFE_MINECARTS_DROP) {
								world.dropItem(minecart.getLocation(), new ItemStack(Material.MINECART, 1));
                                                            }
							}
						}
					}
				}
			}, config.SAFE_MINECARTS_DELAY, config.SAFE_MINECARTS_DELAY);
		}
                if (config.ANIMAL_COUNT) {
                    getServer().getScheduler().scheduleAsyncRepeatingTask(this, new Runnable() {
                        public void run() {
                            System.out.println("-!- Starting Mob count");
                            HashMap<String, Integer> a = new HashMap<String, Integer>();
                            for (LivingEntity animal : getServer().getWorlds().get(0).getEntitiesByClass(LivingEntity.class)) {
                                if (a.containsKey(animal.getType().name())) {
                                    a.put(animal.getType().name(), a.get(animal.getType().name()) + 1);
                                } else {
                                    a.put(animal.getType().name(), 1);
                                } 
                            }
                            System.out.println("-!- " + a);
                        }
                    }, 1200, 12000); // 10 Minutes
                }
                
                if (config.LEATHERLESS_BOOKS){
                    ShapelessRecipe cheapBook = new ShapelessRecipe(new ItemStack(Material.BOOK));
                    cheapBook.addIngredient(3, Material.PAPER);
                    getServer().addRecipe(cheapBook);
                    recipeList.add(cheapBook);
                }

		getServer().getScheduler().scheduleSyncRepeatingTask(this, lagCheck, 20, 20);
		getServer().getPluginManager().registerEvents(listener, this);

		sendToLog(Level.INFO, getDescription().getVersion() + " enabled.");
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command, String name, String[] args) {
		boolean success = false;
                if (command.getName().equalsIgnoreCase("unenchant") && sender instanceof Player) {
                    Player player = (Player) sender;
                    try {
                        if (player.getItemInHand().getType().equals(Material.ENCHANTED_BOOK)){
                            player.setItemInHand(new ItemStack(Material.BOOK));
                        } else
                            for (Enchantment e : player.getItemInHand().getEnchantments().keySet()) {
                                player.getItemInHand().removeEnchantment(e);
                            }
                        player.sendMessage("Enchantments removed.");
                    } catch (Exception e) {
                        player.sendMessage("No enchantments removed.");
                    }
                }
		if (command.getName().equalsIgnoreCase("lag")) {
			if (sender.hasPermission("kitchensink.lag")) {
				sendLagStats(sender);
				success = true;
			}
		}
		if (command.getName().equalsIgnoreCase("list")) {
			if (sender.hasPermission("kitchensink.list")) {
				sendList(sender);
				success = true;
			}
		}
                if (command.getName().equalsIgnoreCase("ksinventory")) {
                    if (args.length >= 1) {
                        Player mutee = getServer().getPlayer(args[0]);
                        if (args.length == 2) {
                            if (args[1].equals("clear")) {	
                                mutee.getInventory().clear();
                                mutee.getInventory().setArmorContents(new ItemStack[ mutee.getInventory().getArmorContents().length]);
                                mutee.saveData();
                                sender.sendMessage("Inventory Cleared.");
                                return true;
                            }
                        }
                        if (mutee != null && sender instanceof Player) {
                            ((Player)sender).openInventory(mutee.getPlayer().getInventory());	
                        }
                        return true;
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

	public void sendList(CommandSender sender) {
		ArrayList<String> list = new ArrayList<String>();
		for (Player player : getServer().getOnlinePlayers()) {
			list.add(player.getName());
		}
		Collections.sort(list, String.CASE_INSENSITIVE_ORDER);
		sender.sendMessage("Players Online: " + list.size());
		if(list.size() == 0) { return; }
		String onlinelist = "Players:";
		int index = 0;
		for (String p:list) {
			ChatColor color = ChatColor.GRAY;
			if (index++ % 2 == 0) { color = ChatColor.WHITE; }
			onlinelist += " " + color + p;
		}
		sender.sendMessage(onlinelist);
	}

	public void sendToLog(Level level, String message) {
		log.log(level, "[" + getDescription().getName() + "] " + message);
	}
}
