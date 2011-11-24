package nu.nerd.shit.stopdrops;

import net.minecraft.server.BlockChest;
import net.minecraft.server.World;

public class DropsChest extends BlockChest {
    protected DropsChest(int paramInt) {
        super(paramInt);
    }

    @Override
    public void remove(World world, int i, int j, int k) {
        world.n(i, j, k);
    }
}
