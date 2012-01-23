package nu.nerd.kitchensink.blocks;

import net.minecraft.server.BlockChest;
import net.minecraft.server.World;

public class NoDropsChest extends BlockChest {
    public NoDropsChest(int paramInt) {
        super(paramInt);
    }

    @Override
    public void remove(World world, int i, int j, int k) {
        world.n(i, j, k);
    }
}
