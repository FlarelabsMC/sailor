package com.squoshi.sailor.physics;

import com.squoshi.sailor.Sailor;
import de.articdive.jnoise.pipeline.JNoise;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import org.apache.commons.lang3.tuple.Pair;
import org.joml.Vector3d;
import org.joml.Vector3dc;
import org.joml.primitives.AABBdc;
import org.joml.primitives.AABBic;
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

//        double mass = ship.getInertiaData().getMass();
        AABBdc worldAABB = ship.getWorldAABB();

//        AABBic shipAABB = ship.getShipAABB();
//        double worldSizeX = worldAABB.maxX() - worldAABB.minX();
//        double worldSizeZ = worldAABB.maxZ() - worldAABB.minZ();
//        double centerWorldX = worldSizeX / 2;
//        double bottomCenterWorldY = worldAABB.minY();
//        double centerWorldZ = worldSizeZ / 2;
//        Vector3d shipCenterExample = convertToShipSpace(new Vector3d(centerWorldX, bottomCenterWorldY, centerWorldZ), ship);

//        List<Pair<Vector3d, Double>> waveHeights = new ArrayList<>();
        for (double x = worldAABB.minX(); x < worldAABB.maxX(); x++) {
            for (double y = worldAABB.minY(); y < worldAABB.maxY(); y++) {
                for (double z = worldAABB.minZ(); z < worldAABB.maxZ(); z++) {
                    Sailor.LOGGER.info("Checking block at ({}, {}, {})", x, y, z);
                    Vector3d pos = convertToShipSpace(new Vector3d(x, y, z), ship);
//                    double oceanHeight = getOceanHeightAt(x, z, level.getGameTime(), oceanNoise);
//                    if (oceanHeight < 0) continue;
                }
            }
        }

//        Vector3d averagePos = new Vector3d();
//        double averageHeight = 0;
//        for (Pair<Vector3d, Double> waveHeight : waveHeights) {
//            averagePos.add(waveHeight.getLeft());
//            averageHeight += waveHeight.getRight();
//        }
//        averagePos.div(waveHeights.size());
//        averageHeight /= waveHeights.size();
//
//        forceApplier.applyInvariantForceToPos(
//                new Vector3d(0, averageHeight * mass, 0),
//                new Vector3d(averagePos.x(), averagePos.y(), averagePos.z())
//        );
    }

    private static double getOceanHeightAt(double x, double z, long time, JNoise oceanNoise) {
        return oceanNoise.evaluateNoise(x * 1e-2, z * 1e-2, time * 1e-2);
    }

    private static Vector3d convertToShipSpace(Vector3d worldPos, LoadedServerShip ship) {
        Vector3d pos = ship.getTransform().getWorldToShip().transformPosition(worldPos).sub(ship.getTransform().getPositionInShip());
        Sailor.LOGGER.info("Converted world pos {} to ship pos {}", worldPos, pos);
        return pos;
    }
}
