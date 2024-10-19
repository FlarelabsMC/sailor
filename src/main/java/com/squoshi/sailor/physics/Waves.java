package com.squoshi.sailor.physics;

import com.squoshi.sailor.Sailor;
import com.squoshi.sailor.util.MathUtil;
import com.squoshi.sailor.util.NoiseStorage;
import net.minecraft.world.level.Level;
import org.valkyrienskies.core.api.ships.LoadedServerShip;
import org.valkyrienskies.mod.common.util.GameTickForceApplier;

public class Waves extends NoiseStorage {
    public static MathUtil.PerlinNoise oceanNoise = new MathUtil.PerlinNoise();

    public static void calculateWaves(LoadedServerShip ship, Level level) {
        if (ship == null) return;
        if (ship.getAttachment(GameTickForceApplier.class) == null) {
            ship.saveAttachment(GameTickForceApplier.class, new GameTickForceApplier());
        }
//        GameTickForceApplier forceApplier = ship.getAttachment(GameTickForceApplier.class);
//        double mass = ship.getInertiaData().getMass();
//        AABBdc shipAABB = ship.getWorldAABB();
//        for (int x = (int) shipAABB.minX(); x <= shipAABB.maxX(); x++) {
//            for (int z = (int) shipAABB.minZ(); z <= shipAABB.maxZ(); z++) {
//                Vector3d pos = new Vector3d(x, shipAABB.minY(), z);
//                Vector3d posFinal = ship.getTransform().getWorldToShip().transformPosition(pos).sub(ship.getTransform().getPositionInShip());
//                double oceanHeight = getOceanHeightAt(posFinal.x, posFinal.z, level.getGameTime(), oceanNoise);
//                Sailor.LOGGER.info("oceanHeight: " + 9.81 * oceanHeight * mass);
//                double vForce = Math.max(9.81 * oceanHeight * mass, 0);
////                double vForce = Math.max((oceanHeight * 1e6) / (mass / 60), 0);
//                Sailor.LOGGER.info("vForce: " + vForce);
////                Vector3d forceToApply = new Vector3d(0, vForce, 0);
////                forceApplier.applyInvariantForceToPos(forceToApply, posFinal);
//            }
//        }
        Sailor.LOGGER.info(oceanNoise.getSeed() + " oceanNoiseSeed");
        Sailor.LOGGER.info(oceanNoise.noiseMC(10, 1, 10) + " oceanNoise");
        Sailor.LOGGER.info(getOceanHeightAt(0, 0, level.getGameTime(), oceanNoise) + " oceanHeight");
        storeNoise(oceanNoise);
    }

    private static double getOceanHeightAt(double x, double z, long time, MathUtil.PerlinNoise oceanNoise) {
        return oceanNoise.noiseMC(x, time, z);
    }
}
