package nu.nerd.shit.stopdrops;

import java.util.logging.Logger;
import net.minecraft.server.BlockJukeBox;
import net.minecraft.server.EntityHuman;
import net.minecraft.server.TileEntityRecordPlayer;
import net.minecraft.server.World;
import org.bukkit.Material;

public class DropsJukeBox extends BlockJukeBox {
    public DropsJukeBox(int param1, int param2) {
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
