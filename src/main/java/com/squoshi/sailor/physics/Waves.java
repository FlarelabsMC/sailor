package com.squoshi.sailor.physics;

import com.squoshi.sailor.Sailor;
import de.articdive.jnoise.pipeline.JNoise;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import org.apache.commons.lang3.tuple.Pair;
import org.joml.Vector3d;
import org.joml.primitives.AABBdc;
import org.valkyrienskies.core.api.ships.LoadedServerShip;
import org.valkyrienskies.mod.common.util.GameTickForceApplier;

import java.util.ArrayList;
import java.util.List;

import static com.squoshi.sailor.util.MathUtil.convertBlockPosToShipyard;
import static com.squoshi.sailor.util.MathUtil.convertToShipSpace;

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
        AABBdc worldAABB = ship.getWorldAABB();

        List<Pair<Vector3d, Double>> waveHeights = new ArrayList<>();
        for (double x = worldAABB.minX(); x < worldAABB.maxX(); x++) {
            for (double y = worldAABB.minY(); y < worldAABB.maxY(); y++) {
                for (double z = worldAABB.minZ(); z < worldAABB.maxZ(); z++) {
                    Vector3d posOnShip = convertToShipSpace(new Vector3d(x, y, z), ship);
                    BlockPos blockPosOnShip = convertBlockPosToShipyard(new BlockPos((int) x, (int) y, (int) z), ship);

                    if (level.getBlockState(blockPosOnShip).isAir()) continue;

                    BlockPos bPos = new BlockPos((int) x, (int) y - 1, (int) z);
                    boolean isEmpty = !level.getFluidState(bPos).isEmpty();
                    if (isEmpty) continue;

                    int times = 0;
                    while (!isEmpty) {
                        bPos = bPos.above();
                        isEmpty = level.getFluidState(bPos).isEmpty();
                        times++;
                        if (times > 5) break;
                    }
                    if (!isEmpty) continue;

                    double oceanHeight = getOceanHeightAt(x, z, level.getGameTime(), oceanNoise);
                    Vector3d pos = new Vector3d(x, y + 1 + oceanHeight, z);

                    if (y <= pos.y()) {
                        waveHeights.add(Pair.of(posOnShip, oceanHeight));
                    }
                }
            }
        }
        if (waveHeights.isEmpty()) return;

        Vector3d averagePos = new Vector3d();
        double averageHeight = 0;
        for (Pair<Vector3d, Double> waveHeight : waveHeights) {
            averagePos.add(waveHeight.getLeft());
            averageHeight += waveHeight.getRight();
        }
        averagePos.div(waveHeights.size());
        averageHeight /= waveHeights.size();

        if (averageHeight < 0) return;

        Sailor.LOGGER.info("Applying force " + averageHeight * mass);

        forceApplier.applyInvariantForceToPos(
                new Vector3d(0, averageHeight * mass, 0),
                new Vector3d(averagePos.x(), averagePos.y(), averagePos.z())
        );
    }

    private static double getOceanHeightAt(double x, double z, long time, JNoise oceanNoise) {
        return oceanNoise.evaluateNoise(x * 1e-2, z * 1e-2, time * 1e-2);
    }
}
