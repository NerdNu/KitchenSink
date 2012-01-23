package nu.nerd.kitchensink.blocks;

import net.minecraft.server.BlockJukeBox;
import net.minecraft.server.TileEntityRecordPlayer;
import net.minecraft.server.World;

public class NoDropsJukeBox extends BlockJukeBox {
    public NoDropsJukeBox(int param1, int param2) {
        super(param1, param2);
    }

    @Override
    public void dropRecord(World world, int i, int j, int k) {
        TileEntityRecordPlayer tile = (TileEntityRecordPlayer)world.getTileEntity(i, j, k);
        if (tile == null)
            return;

        int i2 = tile.record;
        if (i2 == 0)
            return;

        world.f(1005, i, j, k, 0);
        world.a((String)null, i, j, k);
        tile.record = 0;
        tile.update();
        world.setData(i, j, k, 0);
    }
}
