package com.squoshi.sailor.util;

import net.minecraft.core.BlockPos;
import org.joml.Vector3d;
import org.valkyrienskies.core.api.ships.LoadedServerShip;

public class MathUtil {
    public static Vector3d project(Vector3d vec1, Vector3d vec2) {
        return vec1.mul(vec1.dot(vec2) / Math.pow(vec1.length(), 2));
    }

    /**
     * Converts a world position to a position relative to the provided ship
     * @param worldPos The position in the Minecraft world
     * @param ship The ship to convert the position to
     * @return The position in ship space (center of the ship is equal to 0,0,0)
     */
    public static Vector3d convertToShipSpace(Vector3d worldPos, LoadedServerShip ship) {
        Vector3d vec = new Vector3d(worldPos);
        return ship.getTransform().getWorldToShip().transformPosition(vec).sub(ship.getTransform().getPositionInShip());
    }

    /**
     * Converts a block position to a position relative to the provided ship
     * @param blockPos The block position in the Minecraft world
     * @param ship The ship to convert the position to
     * @return The block position in ship space (center of the ship is equal to 0,0,0)
     */
    public static BlockPos convertBlockPosToShipyard(BlockPos blockPos, LoadedServerShip ship) {
        Vector3d vec = new Vector3d(blockPos.getX(), blockPos.getY(), blockPos.getZ());
        Vector3d shipyardPos = ship.getTransform().getWorldToShip().transformPosition(vec);
        return new BlockPos((int) shipyardPos.x, (int) shipyardPos.y, (int) shipyardPos.z);
    }
}