package com.squoshi.sailor.physics;

import de.articdive.jnoise.pipeline.JNoise;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import org.apache.commons.lang3.tuple.Pair;
import org.joml.Vector3d;
import org.joml.primitives.AABBdc;
import org.valkyrienskies.core.api.ships.LoadedServerShip;
import org.valkyrienskies.mod.common.util.GameTickForceApplier;

import java.util.ArrayList;
import java.util.List;

public class Waves {
    public static JNoise oceanNoise;

    public static void calculateWaves(LoadedServerShip ship, Level level) {
        if (ship == null) return;
        if (oceanNoise == null) return;
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
        List<Pair<Double, Vector3d>> forces = new ArrayList<>();
        for (double xx = 0 - centerX; xx < x - 1; xx++) {
            for (double zz = 0 - centerZ; zz < z - 1; zz++) {
                Vector3d posFinal = new Vector3d(xx, shipAABB.minY(), zz);
                Vector3d worldPos = new Vector3d(shipAABB.minX() + centerX + posFinal.x, shipAABB.minY(), shipAABB.minZ() + centerZ + posFinal.z);
                double oceanHeight = getOceanHeightAt(worldPos.x, worldPos.z, level.getGameTime(), oceanNoise);
                BlockHitResult clip = level.clip(new ClipContext(
                    new Vec3(centerX + posFinal.x, posFinal.y, centerZ + posFinal.z),
                    new Vec3(centerX + posFinal.x, posFinal.y - level.getMaxBuildHeight(), centerZ + posFinal.z),
                    ClipContext.Block.COLLIDER, ClipContext.Fluid.WATER,
                    null
                ));
                if (clip.getType() != BlockHitResult.Type.BLOCK) continue;
                Vec3 location = clip.getLocation();
                if (level.getFluidState(clip.getBlockPos()).isEmpty()) continue;
                if ((location.y() + oceanHeight) > shipAABB.minY()) continue;
                double vForce = Math.max(oceanHeight * (mass), 0);
                forces.add(Pair.of(vForce, posFinal));
            }
        }
        if (forces.isEmpty()) return;
        double vForce = 0;
        Vector3d posFinal = new Vector3d();
        for (Pair<Double, Vector3d> force : forces) {
            if (force.getLeft() > vForce) {
                vForce = force.getLeft();
                posFinal = force.getRight();
            }
        }
        if (vForce == 0) return;
        Vector3d forceToApply = new Vector3d(0, vForce, 0);
        forceApplier.applyInvariantForceToPos(forceToApply, posFinal);
    }

    private static double getOceanHeightAt(double x, double z, long time, JNoise oceanNoise) {
        return oceanNoise.evaluateNoise(x * 1e-2, z * 1e-2, time * 1e-2);
    }
}
