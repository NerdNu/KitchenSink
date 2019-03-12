package nu.nerd.kitchensink;

import de.diddiz.LogBlock.Actor;
import de.diddiz.LogBlock.BlockChange;
import de.diddiz.LogBlock.Consumer;
import de.diddiz.LogBlock.LogBlock;
import de.diddiz.LogBlock.QueryParams;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.inventory.ItemStack;

import java.sql.SQLException;

public class LogBlockHook {

    private final LogBlock _logBlockPlugin;

    public LogBlockHook(LogBlock logBlockPlugin) {
        _logBlockPlugin = logBlockPlugin;
    }

    private boolean isEnabled() {
        return _logBlockPlugin != null && KitchenSink.PLUGIN.config.HOOK_LOGBLOCK;
    }

    public void logKill(Location location, Actor killer, Actor victim, ItemStack weapon) {
        if (isEnabled()) {
            _logBlockPlugin.getConsumer().queueKill(location, killer, victim, weapon);
        }
    }

    public String getBlockPlacer(Block block) {
        if (!isEnabled()) {
            return null;
        }
        QueryParams query = new QueryParams(_logBlockPlugin);
        query.setLocation(block.getLocation());
        query.needPlayer = true;
        query.needDate = true;
        query.needData = true;
        query.bct = QueryParams.BlockChangeType.CREATED;
        query.limit = 1;
        try {
            return _logBlockPlugin.getBlockChanges(query)
                .stream()
                .map(BlockChange::toString)
                .reduce(String::concat)
                .orElse("no result");
        } catch (SQLException e) {
            e.printStackTrace();
            return "no result";
        }
    }

}
