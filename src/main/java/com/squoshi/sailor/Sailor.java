package com.squoshi.sailor;

import com.mojang.logging.LogUtils;
import com.squoshi.sailor.config.SailorServerConfig;
import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.serializer.GsonConfigSerializer;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import org.slf4j.Logger;

@Mod(Sailor.MODID)
@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
public class Sailor {
    public static final String MODID = "sailor";
    public static final Logger LOGGER = LogUtils.getLogger();
    public static final boolean DEBUG = false;

    public static SailorServerConfig CONFIG;

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
        MinecraftForge.EVENT_BUS.addListener(ServerEvents::onLevelTick);
        MinecraftForge.EVENT_BUS.addListener(ServerEvents::serverAboutToStart);

        AutoConfig.register(SailorServerConfig.class, GsonConfigSerializer::new);
        CONFIG = AutoConfig.getConfigHolder(SailorServerConfig.class).getConfig();
    }

    public static double getEntityMass(Entity entity) {
        return CONFIG.entityWeights.getOrDefault(BuiltInRegistries.ENTITY_TYPE.getKey(entity.getType()).toString(), 1000d);
    }
}