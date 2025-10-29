package nu.nerd.kitchensink.hooks;

import net.coreprotect.CoreProtectAPI;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import nu.nerd.kitchensink.KitchenSink;
import org.bukkit.*;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Class for providing methods that allow access to CoreProtect's data.
 */
public class CoreProtectHook {

    KitchenSink plugin;
    CoreProtectAPI coreProtectAPI;

    public CoreProtectHook(KitchenSink plugin, CoreProtectAPI coreProtectAPI) {
        this.plugin = plugin;
        this.coreProtectAPI = coreProtectAPI;
    }

    /**
     * Outputs the top 10 blocks placed and broken for a player when "/trace [playername]" is run
     *
     * @param commandSender the thing running the command.
     * @param nameToCheck the name being checked.
     */
    public void trace(CommandSender commandSender, String nameToCheck) {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {

            OfflinePlayer player = Bukkit.getOfflinePlayer(nameToCheck);
            if(!player.hasPlayedBefore()) {
                Bukkit.getScheduler().runTask(plugin, () -> {
                    commandSender.sendMessage(Component.text("That player hasn't played before.",
                            NamedTextColor.RED));
                });
            } else {
                List<String[]> resultsPlaced = lookupTrace(
                        parseTime(30, 0, 0, 0),
                        nameToCheck,
                        new ArrayList<>(),
                        new ArrayList<>(List.of(1))
                );

                List<String[]> resultsRemoved = lookupTrace(
                        parseTime(30, 0, 0, 0),
                        nameToCheck,
                        new ArrayList<>(),
                        new ArrayList<>(List.of(0))
                );

                Map<Material, Integer> mapPlaced = countBlocks(resultsPlaced);
                Map<Material, Integer> mapRemoved = countBlocks(resultsRemoved);

                Iterator<Map.Entry<Material, Integer>> placedIterator = mapPlaced.entrySet().iterator();
                Iterator<Map.Entry<Material, Integer>> removedIterator = mapRemoved.entrySet().iterator();

                Component outputResult = Component.text("Logged information for: " + player.getName(),
                                NamedTextColor.GOLD)
                        .appendNewline().appendNewline()
                        .append(Component.text(String.format("%-40s %s", "Placed", "Broken"), NamedTextColor.GOLD));

                while(placedIterator.hasNext() && removedIterator.hasNext()) {
                    Map.Entry<Material, Integer> placedEntry = placedIterator.next();
                    Map.Entry<Material, Integer> removedEntry = removedIterator.next();

                    String placedText = placedEntry.getKey().name().toLowerCase() + ": " + placedEntry.getValue();
                    String removedText = removedEntry.getKey().name() .toLowerCase()+ ": " + removedEntry.getValue();

                    outputResult = outputResult.appendNewline()
                            .append(Component.text(String.format("%-40s", placedText), NamedTextColor.GREEN))
                            .append(Component.text(removedText, NamedTextColor.RED));
                }

                Component finalOutput = outputResult;
                Bukkit.getScheduler().runTask(plugin, () -> {
                    commandSender.sendMessage(finalOutput);
                    if(commandSender instanceof Player) {
                        Player senderPlayer = (Player) commandSender;
                        Bukkit.getScheduler().runTaskLater(plugin, () ->
                                senderPlayer.performCommand("bminfo " + player.getName()), 20L);
                        Bukkit.getScheduler().runTaskLater(plugin, () ->
                                senderPlayer.performCommand("bminfo -n " + player.getName()), 20L * 2);
                    }
                });
            }
        });
    }

    /**
     * Outputs the top 10 x-ray suspects based on diamond ore and ancient debris placed/broken
     * in the last 7 days.
     *
     * @param commandSender the thing running the command.
     */
    public void xrayTop(CommandSender commandSender) {

        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {

            // Add null checks for worlds
            World overworld = Bukkit.getWorld("world");
            World nether = Bukkit.getWorld("world_nether");

            if (overworld == null) {
                Bukkit.getScheduler().runTask(plugin, () -> {
                    commandSender.sendMessage(Component.text("Error: Overworld not found!", NamedTextColor.RED));
                });
                return;
            }

            if (nether == null) {
                Bukkit.getScheduler().runTask(plugin, () -> {
                    commandSender.sendMessage(Component.text("Error: Nether world not found!", NamedTextColor.RED));
                });
                return;
            }

            Location overworldLocation = new Location(overworld, 0, 0, 0);
            Location netherLocation = new Location(nether, 0, 0, 0);

            // Get diamond data
            List<String[]> diamondPlaced = lookupXrayTop(
                    parseTime(7, 0, 0, 0),
                    new ArrayList<>(List.of(Material.DIAMOND_ORE, Material.DEEPSLATE_DIAMOND_ORE)),
                    new ArrayList<>(List.of(1)),
                    overworldLocation
            );

            List<String[]> diamondBroken = lookupXrayTop(
                    parseTime(7, 0, 0, 0),
                    new ArrayList<>(List.of(Material.DIAMOND_ORE, Material.DEEPSLATE_DIAMOND_ORE)),
                    new ArrayList<>(List.of(0)),
                    overworldLocation
            );

            // Get ancient debris data
            List<String[]> debrisPlaced = lookupXrayTop(
                    parseTime(7, 0, 0, 0),
                    new ArrayList<>(List.of(Material.ANCIENT_DEBRIS)),
                    new ArrayList<>(List.of(1)),
                    netherLocation
            );

            List<String[]> debrisBroken = lookupXrayTop(
                    parseTime(7, 0, 0, 0),
                    new ArrayList<>(List.of(Material.ANCIENT_DEBRIS)),
                    new ArrayList<>(List.of(0)),
                    netherLocation
            );

            // Check if any lookups failed
            if (diamondPlaced == null || diamondBroken == null || debrisPlaced == null || debrisBroken == null) {
                Bukkit.getScheduler().runTask(plugin, () -> {
                    commandSender.sendMessage(Component.text("CoreProtect lookup failed - check server logs", NamedTextColor.RED));
                });
                return;
            }

            // Check if all results are empty
            if (diamondPlaced.isEmpty() && diamondBroken.isEmpty() && debrisPlaced.isEmpty() && debrisBroken.isEmpty()) {
                Bukkit.getScheduler().runTask(plugin, () -> {
                    commandSender.sendMessage(Component.text("No ore activity found in the last 7 days.", NamedTextColor.YELLOW));
                });
                return;
            }

            // Count by player
            Map<String, Integer> diamondPlacedCount = countBlocksByPlayer(diamondPlaced);
            Map<String, Integer> diamondBrokenCount = countBlocksByPlayer(diamondBroken);
            Map<String, Integer> debrisPlacedCount = countBlocksByPlayer(debrisPlaced);
            Map<String, Integer> debrisBrokenCount = countBlocksByPlayer(debrisBroken);

            // Get top 10 for each category
            Map<String, Integer> topDiamondPlaced = getTopTenPlayers(diamondPlacedCount);
            Map<String, Integer> topDiamondBroken = getTopTenPlayers(diamondBrokenCount);
            Map<String, Integer> topDebrisPlaced = getTopTenPlayers(debrisPlacedCount);
            Map<String, Integer> topDebrisBroken = getTopTenPlayers(debrisBrokenCount);

            // Combine player data
            Map<String, PlayerStats> diamondStats = combinePlayerStats(topDiamondPlaced, topDiamondBroken);
            Map<String, PlayerStats> debrisStats = combinePlayerStats(topDebrisPlaced, topDebrisBroken);

            // DEBUG: Log combined stats
            plugin.getLogger().info("Combined diamond stats: " + diamondStats.size());
            plugin.getLogger().info("Combined debris stats: " + debrisStats.size());

            // Get top 10 combined stats (sorted by total activity)
            Map<String, PlayerStats> topDiamondStats = getTopTenPlayerStats(diamondStats);
            Map<String, PlayerStats> topDebrisStats = getTopTenPlayerStats(debrisStats);

            List<Map.Entry<String, PlayerStats>> diamondList = new ArrayList<>(topDiamondStats.entrySet());
            List<Map.Entry<String, PlayerStats>> debrisList = new ArrayList<>(topDebrisStats.entrySet());

            int maxRows = Math.max(diamondList.size(), debrisList.size());

            Component outputResult = Component.text("Top " + maxRows +  " x-ray suspects (last 7 days):",
                            NamedTextColor.GOLD)
                    .appendNewline().appendNewline()
                    .append(Component.text(String.format("%-30s %s", "Diamond Ore (P/B)", "Ancient Debris (P/B)"), NamedTextColor.GOLD));

            // Handle cases where lists might be different sizes
            for (int i = 0; i < maxRows; i++) {
                String diamondText = "";
                String debrisText = "";

                if (i < diamondList.size()) {
                    Map.Entry<String, PlayerStats> diamondEntry = diamondList.get(i);
                    diamondText = diamondEntry.getKey() + ": " +
                            diamondEntry.getValue().placed + "/" + diamondEntry.getValue().broken;
                }

                if (i < debrisList.size()) {
                    Map.Entry<String, PlayerStats> debrisEntry = debrisList.get(i);
                    debrisText = debrisEntry.getKey() + ": " +
                            debrisEntry.getValue().placed + "/" + debrisEntry.getValue().broken;
                }

                outputResult = outputResult.appendNewline()
                        .append(Component.text(String.format("%-30s", diamondText), NamedTextColor.YELLOW))
                        .append(Component.text(debrisText, NamedTextColor.LIGHT_PURPLE));
            }

            // If no data to display
            if (maxRows == 0) {
                outputResult = outputResult.appendNewline()
                        .append(Component.text("No suspicious ore activity detected.", NamedTextColor.GREEN));
            }

            Component finalResult = outputResult;
            Bukkit.getScheduler().runTask(plugin, () -> commandSender.sendMessage(finalResult));

        });
    }

    /*
    Trace methods
     */

    /**
     * Performs a CoreProtect lookup through the API. Has some pre-filled arguments.
     *
     * @param time the amount of time to check, in seconds.
     * @param nameToCheck the name of the player being checked
     * @param blocksToCheck the list of blocks to be checked
     * @param actions the actions to be considered in the lookup
     * @return a list of incidents
     */
    private List<String[]> lookupTrace(int time, String nameToCheck, List<Object> blocksToCheck, List<Integer> actions) {
        return coreProtectAPI.performLookup(
                time,
                List.of(nameToCheck),
                new ArrayList<>(),
                blocksToCheck,
                new ArrayList<>(),
                actions,
                0,
                null);
    }

    /**
     * Counts the number of blocks in the lookup results.
     *
     * @param results the results from the lookup
     * @return a map of block types to counts
     */
    private Map<Material, Integer> countBlocks(List<String[]> results) {
        Map<Material, Integer> mapResults = new HashMap<>();
        for(String[] entry : results) {
            CoreProtectAPI.ParseResult result = coreProtectAPI.parseResult(entry);
            if(result == null) continue;

            Material blockType = result.getType();
            mapResults.merge(blockType, 1, Integer::sum);
        }
        return getTopTen(mapResults);
    }

    /**
     * Gets the top ten entries from a map sorted by value in descending order.
     *
     * @param originalMap the original map
     * @return a new map with the top ten entries
     */
    private Map<Material, Integer> getTopTen(Map<Material, Integer> originalMap) {
        return originalMap.entrySet()
                .stream()
                .sorted(Map.Entry.<Material, Integer>comparingByValue().reversed())
                .limit(10)
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (e1, e2) -> e1,
                        LinkedHashMap::new
                ));
    }

    /*
    Xray-top methods
     */

    /**
     * Class to hold placed and broken stats for a player.
     */
    private static class PlayerStats {
        int placed;
        int broken;

        /**
         * Constructor for PlayerStats.
         *
         * @param placed number of blocks placed
         * @param broken number of blocks broken
         */
        PlayerStats(int placed, int broken) {
            this.placed = placed;
            this.broken = broken;
        }

        /**
         * Gets the total number of blocks placed and broken.
         *
         * @return total blocks
         */
        int getTotal() {
            return placed + broken;
        }
    }

    /**
     * Performs a CoreProtect lookup through the API for xray-top.
     *
     * @param time the amount of time to check, in seconds.
     * @param blocksToCheck the list of blocks to be checked
     * @param actions the actions to be considered in the lookup
     * @param location the location to perform the lookup at
     * @return a list of incidents
     */
    private List<String[]> lookupXrayTop(int time, List<Object> blocksToCheck, List<Integer> actions, Location location) {
        return coreProtectAPI.performLookup(
                time,
                null,
                new ArrayList<>(),
                blocksToCheck,
                new ArrayList<>(),
                actions,
                5000,
                location);
    }

    /**
     * Counts the number of blocks by player in the lookup results.
     *
     * @param results the results from the lookup
     * @return a map of player names to counts
     */
    private Map<String, Integer> countBlocksByPlayer(List<String[]> results) {
        Map<String, Integer> playerCounts = new HashMap<>();
        for(String[] entry : results) {
            CoreProtectAPI.ParseResult result = coreProtectAPI.parseResult(entry);
            if(result == null) continue;

            String playerName = result.getPlayer();
            playerCounts.merge(playerName, 1, Integer::sum);
        }
        return playerCounts;
    }

    /**
     * Gets the top ten players from a map sorted by value in descending order.
     *
     * @param originalMap the original map
     * @return a new map with the top ten players
     */
    private Map<String, Integer> getTopTenPlayers(Map<String, Integer> originalMap) {
        return originalMap.entrySet()
                .stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .limit(10)
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (e1, e2) -> e1,
                        LinkedHashMap::new
                ));
    }

    /**
     * Combines placed and broken stats into a single map.
     *
     * @param placedMap map of players to placed counts
     * @param brokenMap map of players to broken counts
     * @return a map of players to PlayerStats
     */
    private Map<String, PlayerStats> combinePlayerStats(Map<String, Integer> placedMap, Map<String, Integer> brokenMap) {
        Map<String, PlayerStats> combined = new HashMap<>();

        // Add all players from placed map
        for(Map.Entry<String, Integer> entry : placedMap.entrySet()) {
            String player = entry.getKey();
            int placed = entry.getValue();
            int broken = brokenMap.getOrDefault(player, 0);
            combined.put(player, new PlayerStats(placed, broken));
        }

        // Add any players that only appear in broken map
        for(Map.Entry<String, Integer> entry : brokenMap.entrySet()) {
            String player = entry.getKey();
            if(!combined.containsKey(player)) {
                int broken = entry.getValue();
                combined.put(player, new PlayerStats(0, broken));
            }
        }

        return combined;
    }

    /**
     * Gets the top ten players based on total activity (placed + broken).
     *
     * @param originalMap the original map of players to PlayerStats
     * @return a new map with the top ten players
     */
    private Map<String, PlayerStats> getTopTenPlayerStats(Map<String, PlayerStats> originalMap) {
        return originalMap.entrySet()
                .stream()
                .sorted(Map.Entry.<String, PlayerStats>comparingByValue(
                        (stats1, stats2) -> Integer.compare(stats2.getTotal(), stats1.getTotal())))
                .limit(10)
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (e1, e2) -> e1,
                        LinkedHashMap::new
                ));
    }

    /*
    General methods
     */

    /**
     * Parses time components into total seconds.
     * @param days number of days
     * @param hours number of hours
     * @param minutes number of minutes
     * @param seconds number of seconds
     * @return total time in seconds
     */
    private int parseTime(int days, int hours, int minutes, int seconds) {
        return (days * 24 * 60 * 60) + (hours * 60 * 60) + (minutes * 60) + seconds;
    }
}
