package nu.nerd.creativemode;

import net.minecraft.server.BlockEnderPortal;
import net.minecraft.server.Entity;
import net.minecraft.server.Material;
import net.minecraft.server.World;

public class CreativeEnderPortal extends BlockEnderPortal {
    CreativeEnderPortal(int id, Material mat) {
        super(id, mat);
    }

    @Override
    public void a(World world, int i, int j, int k, Entity entity) {
        return;
    }
}
