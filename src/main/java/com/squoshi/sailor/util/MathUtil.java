package com.squoshi.sailor.util;

import org.joml.Vector3d;

public class MathUtil {
    public static Vector3d project(Vector3d vec1, Vector3d vec2) {
        return vec1.mul(vec1.dot(vec2) / Math.pow(vec1.length(), 2));
    }
}