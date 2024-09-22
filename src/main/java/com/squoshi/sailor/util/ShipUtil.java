package com.squoshi.sailor.util;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import org.joml.Vector3dc;
import org.valkyrienskies.core.api.ships.LoadedServerShip;

import java.util.concurrent.atomic.AtomicBoolean;

import static com.squoshi.sailor.Sailor.log;

public class ShipUtil {
    public static boolean isShipInWater(LoadedServerShip ship, Level level) {
        Vector3dc shipInWorldPos = ship.getTransform().getPositionInWorld();
        AtomicBoolean inWater = new AtomicBoolean(false);
        BlockPos pos = new BlockPos((int) shipInWorldPos.x(), (int) shipInWorldPos.y(), (int) shipInWorldPos.z());
        if (level.isWaterAt(pos)) {
            inWater.set(true);
        }
        log("Ship is in water: " + inWater.get());
        return inWater.get();
    }
}
