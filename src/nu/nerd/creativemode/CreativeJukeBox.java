package nu.nerd.creativemode;

import net.minecraft.server.BlockJukeBox;
import net.minecraft.server.EntityHuman;
import net.minecraft.server.TileEntityRecordPlayer;
import net.minecraft.server.World;

public class CreativeJukeBox extends BlockJukeBox {
    public CreativeJukeBox(int param1, int param2) {
        super(param1, param2);
    }

    @Override
    public void c_(World world, int i, int j, int k) {
        TileEntityRecordPlayer tile = (TileEntityRecordPlayer)world.getTileEntity(i, j, k);
        if (tile == null)
            return;

        int i2 = tile.a;
        if (i2 == 0)
            return;

        world.f(1005, i, j, k, 0);
        world.a((EntityHuman)null, i, j, k);
        tile.a = 0;
        tile.update();
        world.setData(i, j, k, 0);
    }
}
