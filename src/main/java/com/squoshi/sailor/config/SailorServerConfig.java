package com.squoshi.sailor.config;

import com.google.gson.Gson;
import com.squoshi.sailor.Sailor;
import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.annotation.Config;
import me.shedaniel.autoconfig.annotation.ConfigEntry;

import java.util.LinkedHashMap;

@Config(name = Sailor.MODID)
public class SailorServerConfig implements ConfigData {
    private static final Gson GSON = new Gson();

    @ConfigEntry.Gui.RequiresRestart
    public LinkedHashMap<String, Double> entityWeights = new LinkedHashMap<String, Double>() {
        {
            this.put("minecraft:player", 1000d);
        }
    };

    public SailorServerConfig() {
    }
}
