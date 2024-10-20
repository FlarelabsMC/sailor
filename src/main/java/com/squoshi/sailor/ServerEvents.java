package com.squoshi.sailor;

import com.squoshi.sailor.physics.Waves;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.server.ServerAboutToStartEvent;
import org.joml.Vector3d;
import org.joml.primitives.AABBd;
import org.valkyrienskies.core.api.ships.LoadedServerShip;
import org.valkyrienskies.mod.common.VSGameUtilsKt;
import org.valkyrienskies.mod.common.util.GameTickForceApplier;

public class ServerEvents {
    public static void onLevelTick(TickEvent.LevelTickEvent event) {
        if (event.level.isClientSide()) return;

        VSGameUtilsKt.getShipObjectWorld(event.level.getServer()).getLoadedShips().forEach(ship -> {
            Waves.calculateWaves(ship, event.level);
            // TODO: Add wind
        });
    }

    public static void serverAboutToStart(ServerAboutToStartEvent event) {
        Waves.oceanNoise.setSeed(event.getServer().getWorldData().worldGenOptions().seed());
    }

    public static void onFall(Level pLevel, BlockPos pPos, Entity pEntity, float pFallDistance) {
        double entityMass = Sailor.getEntityMass(pEntity);
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
                double fallForce = -(60 * entityMass * Math.sqrt(2 * 10 * pFallDistance));
                forcesApplier.applyInvariantForceToPos(new Vector3d(0, fallForce, 0), new Vector3d(posFinal.x, posFinal.y, posFinal.z));
            }
        });
    }

    public static void stepOn(BlockPos pPos, Entity pEntity) {
        if (pEntity.level().isClientSide()) return;
        AABB aabb = new AABB(pPos.getX(), pPos.getY(), pPos.getZ(), pPos.getX() + 1, pPos.getY() + 1, pPos.getZ() + 1);
        VSGameUtilsKt.getShipsIntersecting(pEntity.level(), aabb).forEach(s -> {
            if (s != null) {
                LoadedServerShip ship = VSGameUtilsKt.getShipObjectManagingPos((ServerLevel) pEntity.level(), s.getTransform().getPositionInShip());
                if (ship == null) return;
                if (ship.getAttachment(GameTickForceApplier.class) == null) {
                    ship.saveAttachment(GameTickForceApplier.class, new GameTickForceApplier());
                }
                GameTickForceApplier forcesApplier = ship.getAttachment(GameTickForceApplier.class);
                Vector3d pos = new Vector3d(pEntity.getX(), pEntity.getY(), pEntity.getZ());
                Vector3d posFinal = ship.getTransform().getWorldToShip().transformPosition(pos).sub(ship.getTransform().getPositionInShip());
                double entityMass = Sailor.getEntityMass(pEntity);
                double idleForce = -entityMass;
                forcesApplier.applyInvariantForceToPos(new Vector3d(0, idleForce, 0), posFinal);
            }
        });
    }
}
