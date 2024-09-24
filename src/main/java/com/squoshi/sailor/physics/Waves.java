package com.squoshi.sailor.physics;

import com.squoshi.sailor.util.MathUtil;
import com.squoshi.sailor.util.NoiseStorage;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
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
                Vec3 p = new Vec3(x, shipAABB.minY(), z);
                BlockHitResult clip = level.clip(new ClipContext(p.add(0, 1, 0), p.subtract(0, 1, 0), ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, null));
                Vec3 pos = clip.getLocation();
                Vector3d posFinal = ship.getTransform().getWorldToShip().transformPosition(new Vector3d(pos.x, pos.y, pos.z)).sub(ship.getTransform().getPositionInShip());
                double oceanHeight = getOceanHeightAt(posFinal.x, posFinal.z, level.getGameTime(), oceanNoise);
                if (oceanHeight < 0) oceanHeight = 0;
                Vector3d forceToApply = new Vector3d(0, oceanHeight * mass, 0);
                forceApplier.applyInvariantForceToPos(forceToApply, posFinal);
            }
        }
        storeNoise(oceanNoise);
    }

    private static double getOceanHeightAt(double x, double z, long time, MathUtil.PerlinNoise oceanNoise) {
        Vector3dc noise3d = oceanNoise.noise3d(x, time, z);
        double noiseHeight = noise3d.y();
//        if (noiseHeight < 0) noiseHeight = 0;
        return noiseHeight;
    }
}
