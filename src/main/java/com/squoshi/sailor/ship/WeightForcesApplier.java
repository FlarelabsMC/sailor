package com.squoshi.sailor.ship;

import com.mojang.datafixers.util.Pair;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector3dc;
import org.valkyrienskies.core.api.ships.PhysShip;
import org.valkyrienskies.core.api.ships.ServerShip;
import org.valkyrienskies.core.api.ships.ShipForcesInducer;

import java.util.concurrent.ConcurrentLinkedQueue;

public class WeightForcesApplier implements ShipForcesInducer {
    private static final ConcurrentLinkedQueue<Pair<Vector3dc, Vector3dc>> queuedForces = new ConcurrentLinkedQueue<>();

    @Override
    public void applyForces(@NotNull PhysShip physShip) {
        queuedForces.forEach(force -> {
            physShip.applyRotDependentForceToPos(force.getFirst(), force.getSecond());
            queuedForces.remove(force);
        });
    }

    public void applyRotDependentForceToPos(Vector3dc force, Vector3dc pos) {
        queuedForces.add(Pair.of(force, pos));
    }

    public static WeightForcesApplier getOrCreateControl(ServerShip ship) {
        var control = ship.getAttachment(WeightForcesApplier.class);
        if (control == null) {
            control = new WeightForcesApplier();
            ship.saveAttachment(WeightForcesApplier.class, control);
        }

        return control;
    }
}
