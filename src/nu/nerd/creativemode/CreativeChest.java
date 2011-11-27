package nu.nerd.creativemode;

import net.minecraft.server.BlockChest;
import net.minecraft.server.World;

public class CreativeChest extends BlockChest {
    protected CreativeChest(int paramInt) {
        super(paramInt);
    }

    @Override
    public void remove(World world, int i, int j, int k) {
        world.n(i, j, k);
    }
}
