package com.squoshi.sailor;

import com.mojang.logging.LogUtils;
import com.squoshi.sailor.config.SailorServerConfig;
import com.squoshi.sailor.util.MathUtil;
import com.squoshi.sailor.util.ShipUtil;
import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.serializer.GsonConfigSerializer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.fml.common.Mod;
import org.joml.Vector3d;
import org.joml.Vector3dc;
import org.joml.primitives.AABBd;
import org.joml.primitives.AABBic;
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

    public static SailorServerConfig CONFIG;

    public static MathUtil.PerlinNoise oceanNoise = new MathUtil.PerlinNoise();

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
        MinecraftForge.EVENT_BUS.addListener(this::onPlayerTick);

        AutoConfig.register(SailorServerConfig.class, GsonConfigSerializer::new);
        CONFIG = AutoConfig.getConfigHolder(SailorServerConfig.class).getConfig();
    }

    public void onLevelTick(TickEvent.LevelTickEvent event) {
        if (event.level.isClientSide()) return;
        oceanNoise.setSeed(event.level.getServer().getWorldData().worldGenOptions().seed());

        if (event.level.getGameTime() % 20 == 0) return;
        VSGameUtilsKt.getShipObjectWorld(event.level.getServer()).getLoadedShips().forEach(ship -> {
            if (!ShipUtil.isShipInWater(ship, event.level)) return;
            WindAndWaves windAndWaves = calculateWindAndWaves(ship, event.level.getGameTime(), event.level);
//            if (windAndWaves == null || windAndWaves.forceApplier == null) return;

            // TODO: Add wind
        });
    }

    public static WindAndWaves calculateWindAndWaves(LoadedServerShip ship, long time, Level level) {
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

        AABBic shipAABB = ship.getShipAABB();
        if (shipAABB == null) return null;

        for (int x = shipAABB.minX(); x <= shipAABB.maxX(); x++) {
            for (int z = shipAABB.minZ(); z <= shipAABB.maxZ(); z++) {
                BlockPos blockPos = new BlockPos(x, shipAABB.minY(), z);
                if (!level.getBlockState(blockPos).isAir()) {
                    Vector3d blockPosInWorld = new Vector3d(x, shipAABB.minY(), z);

                    blockPosInWorld = ship.getTransform().getWorldToShip().transformPosition(blockPosInWorld).sub(ship.getTransform().getPositionInShip());
                    double noiseHeight = noise3d.y();
                    if (noiseHeight < 0) noiseHeight = 0;

                    Vector3d forceToApply = new Vector3d(0, noiseHeight * mass * 0.00000002, 0);

                    forceApplier.applyInvariantForceToPos(forceToApply, blockPosInWorld);
                }
            }
        }
    
        return new WindAndWaves(noise3d, null, forceApplier, ship, breeze, force);
    }

    public static void onFall(Level pLevel, BlockPos pPos, Entity pEntity, float pFallDistance) {
        double entityMass = getEntityMass(pEntity);
        VSGameUtilsKt.getShipsIntersecting(pLevel, new AABBd(pPos.getX(), pPos.getY(), pPos.getZ(), pPos.getX() + 1, pPos.getY() + 1, pPos.getZ() + 1)).forEach(s -> {
            if (s != null) {
                LoadedServerShip ship = VSGameUtilsKt.getShipObjectManagingPos((ServerLevel) pLevel, s.getTransform().getPositionInShip());
                if (ship == null) return;
                if (ship.getAttachment(GameTickForceApplier.class) == null) {
                    ship.saveAttachment(GameTickForceApplier.class, new GameTickForceApplier());
                }
                GameTickForceApplier forcesApplier = ship.getAttachment(GameTickForceApplier.class);
                Vec3 playerPosInWorld = new Vec3(pEntity.getX(), pEntity.getY(), pEntity.getZ());
                BlockHitResult clip = pLevel.clip(new ClipContext(playerPosInWorld, playerPosInWorld.subtract(0, 2, 0), ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, null));
                Vec3 pos = clip.getLocation();
                Vector3d posFinal = ship.getTransform().getWorldToShip().transformPosition(new Vector3d(pos.x, pos.y, pos.z)).sub(ship.getTransform().getPositionInShip());
                LOGGER.info("{}", pos);
                double fallForce = -(60 * entityMass * Math.sqrt(2 * 10 * pFallDistance));
                forcesApplier.applyInvariantForceToPos(new Vector3d(0, fallForce, 0), new Vector3d(posFinal.x, posFinal.y, posFinal.z));
            }
        });
    }

    public void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.player.level().isClientSide()) return;
        VSGameUtilsKt.getShipsIntersecting(event.player.level(), event.player.getBoundingBox()).forEach(s -> {
            if (s != null) {
                LoadedServerShip ship = VSGameUtilsKt.getShipObjectManagingPos((ServerLevel) event.player.level(), s.getTransform().getPositionInShip());
                if (ship == null) return;
                if (ship.getAttachment(GameTickForceApplier.class) == null) {
                    ship.saveAttachment(GameTickForceApplier.class, new GameTickForceApplier());
                }
                GameTickForceApplier forcesApplier = ship.getAttachment(GameTickForceApplier.class);
                Vector3d pos = new Vector3d(event.player.getX(), event.player.getY(), event.player.getZ());
                Vector3d posFinal = ship.getTransform().getWorldToShip().transformPosition(pos).sub(ship.getTransform().getPositionInShip());
                double entityMass = getEntityMass(event.player);
                double idleForce = -entityMass;
                forcesApplier.applyInvariantForceToPos(new Vector3d(0, idleForce, 0), posFinal);
            }
        });
    }

    // Extra:
    public record WindAndWaves(Vector3dc noise, Vector3dc rotation, GameTickForceApplier forceApplier, LoadedServerShip ship, Vector3dc breeze, Vector3dc force) { }

    public static double getEntityMass(Entity entity) {
        return CONFIG.entityWeights.getOrDefault(BuiltInRegistries.ENTITY_TYPE.getKey(entity.getType()).toString(), 1000d);
    }
}