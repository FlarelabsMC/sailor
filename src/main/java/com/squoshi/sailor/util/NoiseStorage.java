package com.squoshi.sailor.util;

public class NoiseStorage {
    public static MathUtil.PerlinNoise storedNoise;

    public static void storeNoise(MathUtil.PerlinNoise noise) {
        storedNoise = noise;
    }

    public static MathUtil.PerlinNoise getStoredNoise() {
        return storedNoise;
    }
}