package com.squoshi.sailor;

import com.mojang.logging.LogUtils;
import com.squoshi.sailor.ship.WeightForcesApplier;
import com.squoshi.sailor.util.MathUtil;
import com.squoshi.sailor.util.ShipUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.fml.common.Mod;
import org.joml.Vector3d;
import org.joml.Vector3dc;
import org.joml.primitives.AABBd;
import org.slf4j.Logger;
import org.valkyrienskies.core.api.ships.LoadedServerShip;
import org.valkyrienskies.mod.common.VSGameUtilsKt;
import org.valkyrienskies.mod.common.util.GameTickForceApplier;

@Mod(Sailor.MODID)
@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
public class Sailor {
    public static final String MODID = "sailor";
    public static final Logger LOGGER = LogUtils.getLogger();
    public static final boolean DEBUG = false;

    // Noise values:
    public static MathUtil.PerlinNoise oceanNoise = new MathUtil.PerlinNoise();

    public static Vector3dc seededRandom2DPos(double yPos, double x1, double z1, double x2, double z2) {
        return new Vector3d(oceanNoise.randomFromSeed(x1, x2), yPos, oceanNoise.randomFromSeed(z1, z2));
    }

    // Necessities:
    public static void log(String message, Object... args) {
        if (DEBUG) {
            LOGGER.info(message, args);
        }
    }

    public static void log(String message) {
        if (DEBUG) {
            LOGGER.info(message);
        }
    }

    public Sailor() {
        MinecraftForge.EVENT_BUS.addListener(this::onLevelTick);
    }

    // Main:
    public void onLevelTick(TickEvent.LevelTickEvent event) {
        if (event.level.isClientSide()) return;
        if (!event.level.dimension().location().equals(new ResourceLocation("minecraft:overworld"))) return;

        oceanNoise.setSeed(event.level.getServer().getWorldData().worldGenOptions().seed());

        if (event.level.getGameTime() % 4 != 0) return;
        VSGameUtilsKt.getShipObjectWorld(event.level.getServer()).getLoadedShips().forEach(ship -> {
            WindAndWaves windAndWaves = calculateWindAndWaves(ship, event.level.getGameTime());

            if (windAndWaves == null || windAndWaves.forceApplier == null) return;
            if (!ShipUtil.isShipInWater(ship, event.level)) return;

            log("Wind and waves: {}", windAndWaves);
            Vector3dc shipInWorldPos = ship.getTransform().getPositionInWorld();

            if (ship.getShipAABB() == null) return;
            double scaleX = ship.getShipAABB().maxX() - ship.getShipAABB().minX();
            double scaleY = ship.getShipAABB().maxY() - ship.getShipAABB().minY();
            double scaleZ = ship.getShipAABB().maxZ() - ship.getShipAABB().minZ();
            AABB shipAABB = new AABB(shipInWorldPos.x(), shipInWorldPos.y(), shipInWorldPos.z(), shipInWorldPos.x() + scaleX, shipInWorldPos.y() + scaleY, shipInWorldPos.z() + scaleZ);
            log("Ship AABB: {}", shipAABB);

            Vector3dc shipMinXYZ = new Vector3d(shipAABB.minX, shipAABB.minY, shipAABB.minZ);
            Vector3dc shipMaxXYZ = new Vector3d(shipAABB.maxX, shipAABB.maxY, shipAABB.maxZ);
            Vector3dc randomPosOnShip = seededRandom2DPos(shipMinXYZ.y(), shipMinXYZ.x(), shipMinXYZ.z(), shipMaxXYZ.x(), shipMaxXYZ.z());
            log("A: Ship min XYZ: {}", shipMinXYZ);
            log("B: Random pos on ship: {}", randomPosOnShip);
            log("C: Ship max XYZ: {}", shipMaxXYZ);

            Vector3dc n = new Vector3d(windAndWaves.noise.x(), windAndWaves.noise.y(), windAndWaves.noise.z()).mul(10);
            log("D: Noise: {}", n);
//            temporarily disabled for testing weight
//            windAndWaves.forceApplier.applyInvariantForceToPos(n, randomPosOnShip);

            // TODO: Add wind
        });
    }

    public static WindAndWaves calculateWindAndWaves(LoadedServerShip ship, long time) {
        if (ship == null) return null;
        if (ship.getVelocity().length() > 2.5) return null;
        if (ship.getAttachment(GameTickForceApplier.class) == null) {
            ship.saveAttachment(GameTickForceApplier.class, new GameTickForceApplier());
        }
        GameTickForceApplier forceApplier = ship.getAttachment(GameTickForceApplier.class);

        double mass = ship.getInertiaData().getMass();
        Vector3d velocity = new Vector3d(ship.getVelocity().x(), 0, ship.getVelocity().z());

        Vector3d breeze = new Vector3d(10, 0, 10).mul(0.1 * 3.2);
        Vector3d force = breeze.sub(velocity).mul(mass * 0.1);

        Vector3dc pos = ship.getTransform().getPositionInWorld();
        Vector3dc noise3d = oceanNoise.noise3d(pos.x(), time, pos.z()).mul(new Vector3d(100, 1, 100), new Vector3d()); // perlin noise position
        return new WindAndWaves(noise3d, null, forceApplier, ship, breeze, force);
    }

    // Mixin reloadables:
    public static void onFall(Level pLevel, BlockState pState, BlockPos pPos, Entity pEntity, float pFallDistance) {
        VSGameUtilsKt.getShipsIntersecting(pLevel, new AABBd(pPos.getX(), pPos.getY(), pPos.getZ(), pPos.getX() + 1, pPos.getY() + 1, pPos.getZ() + 1)).forEach(s -> {
            if (s != null) {
                Vector3dc positionInShip = s.getTransform().getPositionInShip();
                LoadedServerShip ship = VSGameUtilsKt.getShipObjectManagingPos((ServerLevel) pLevel, positionInShip);
                if (ship == null) return;
                WeightForcesApplier forcesApplier = WeightForcesApplier.getOrCreateControl(ship);
//                Vector3dc diff = ship.getTransform().getPositionInWorld().sub(new Vector3d(pEntity.getX(), pEntity.getY(), pEntity.getZ()), new Vector3d());
//                          ^^^^ this doesn't work at all, not sure why

                /*
                    param 1: force - direction and magnitude of the force
                    param 2: pos - center of the force, relative to the ship's center of mass in world coordinates
                    keep in mind that pos is relative to the SHIPYARD position, not the world position
                 */
//                forcesApplier.applyRotDependentForceToPos(new Vector3d(0, -pFallDistance / 150, 0), /* NYI */);
            }
        });
    }

    // Extra:
    public record WindAndWaves(Vector3dc noise, Vector3dc rotation, GameTickForceApplier forceApplier, LoadedServerShip ship, Vector3dc breeze, Vector3dc force) { }
}