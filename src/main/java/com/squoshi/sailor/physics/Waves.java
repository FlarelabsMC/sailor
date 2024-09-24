package com.squoshi.sailor.physics;

import com.squoshi.sailor.util.MathUtil;
import com.squoshi.sailor.util.NoiseStorage;
import net.minecraft.world.level.Level;
import org.joml.Vector3d;
import org.joml.Vector3dc;
import org.joml.primitives.AABBdc;
import org.valkyrienskies.core.api.ships.LoadedServerShip;
import org.valkyrienskies.mod.common.util.GameTickForceApplier;

public class Waves extends NoiseStorage {
    public static MathUtil.PerlinNoise oceanNoise = new MathUtil.PerlinNoise();

    public static void calculateWaves(LoadedServerShip ship, Level level) {
        if (ship == null) return;
        if (ship.getAttachment(GameTickForceApplier.class) == null) {
            ship.saveAttachment(GameTickForceApplier.class, new GameTickForceApplier());
        }
        GameTickForceApplier forceApplier = ship.getAttachment(GameTickForceApplier.class);
        double mass = ship.getInertiaData().getMass();
        AABBdc shipAABB = ship.getWorldAABB();
        for (int x = (int) shipAABB.minX(); x <= shipAABB.maxX(); x++) {
            for (int z = (int) shipAABB.minZ(); z <= shipAABB.maxZ(); z++) {
                Vector3d pos = new Vector3d(x, shipAABB.minY(), z);
                Vector3d posFinal = ship.getTransform().getWorldToShip().transformPosition(pos).sub(ship.getTransform().getPositionInShip());
                double oceanHeight = getOceanHeightAt(posFinal.x, posFinal.z, level.getGameTime(), oceanNoise);
                if (oceanHeight < 0) oceanHeight = 0;
                Vector3d forceToApply = new Vector3d(0, oceanHeight * mass / 2, 0);
                forceApplier.applyInvariantForceToPos(forceToApply, posFinal);
            }
        }
        storeNoise(oceanNoise);
    }

    private static double getOceanHeightAt(double x, double z, long time, MathUtil.PerlinNoise oceanNoise) {
        Vector3dc noise3d = oceanNoise.noise3d(x, time, z);
        return noise3d.y();
    }
}
