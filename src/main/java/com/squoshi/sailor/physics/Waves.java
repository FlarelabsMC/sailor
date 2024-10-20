package com.squoshi.sailor.physics;

import com.squoshi.sailor.Sailor;
import com.squoshi.sailor.util.MathUtil;
import com.squoshi.sailor.util.NoiseStorage;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3d;
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
        double x = shipAABB.maxX() - shipAABB.minX();
        double z = shipAABB.maxZ() - shipAABB.minZ();
        double centerX = x / 2;
        double centerZ = z / 2;
        for (double xx = 0 - centerX; xx < x - 1; xx++) {
            for (double zz = 0 - centerZ; zz < z - 1; zz++) {
                Vector3d posFinal = new Vector3d(xx, shipAABB.minY(), zz);
                Vector3d worldPos = new Vector3d(shipAABB.minX() + centerX + posFinal.x, shipAABB.minY(), shipAABB.minZ() + centerZ + posFinal.z);
                double oceanHeight = getOceanHeightAt(worldPos.x, worldPos.z, level.getGameTime(), oceanNoise) * 8;
                Sailor.LOGGER.info("oceanHeight: " + oceanHeight + " at " + worldPos.x + ", " + worldPos.z);
                BlockHitResult clip = level.clip(new ClipContext(
                    new Vec3(centerX + posFinal.x, posFinal.y, centerZ + posFinal.z),
                    new Vec3(centerX + posFinal.x, posFinal.y - 0.01, centerZ + posFinal.z),
                    ClipContext.Block.COLLIDER, ClipContext.Fluid.WATER,
                    null
                ));
                if (clip.getType() != BlockHitResult.Type.BLOCK) continue;
                Vec3 location = clip.getLocation();
                Vec3 pos = new Vec3(location.x, location.y + oceanHeight, location.z);
                if (pos.y < posFinal.y) continue;
                double vForce = Math.max(oceanHeight * (mass / 70), 0);
//                Sailor.LOGGER.info("vForce: " + vForce + " at " + posFinal.x + ", " + posFinal.z);
                Vector3d forceToApply = new Vector3d(0, vForce, 0);
                forceApplier.applyInvariantForceToPos(forceToApply, posFinal);
            }
        }
        storeNoise(oceanNoise);
    }

    private static double getOceanHeightAt(double x, double z, long time, MathUtil.PerlinNoise oceanNoise) {
        return oceanNoise.noiseMC(x, time, z);
    }
}
