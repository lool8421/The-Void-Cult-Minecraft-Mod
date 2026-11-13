package com.thevoidcult.items;

import org.joml.Vector3f;

import java.util.Arrays;
import java.util.List;

public enum SinsList {
    NONE(new Vector3f(1.0f, 1.0f, 1.0f)),         // White
    WRATH(new Vector3f(0.85f, 0.10f, 0.10f)),     // Crimson
    GLUTTONY(new Vector3f(0.90f, 0.55f, 0.10f)),  // Orange
    GREED(new Vector3f(0.95f, 0.85f, 0.15f)),     // Gold
    SLOTH(new Vector3f(0.25f, 0.55f, 0.85f)),     // Blue
    ENVY(new Vector3f(0.15f, 0.80f, 0.25f)),      // Green
    PRIDE(new Vector3f(0.65f, 0.20f, 0.90f));     // Purple

    public static final List<SinsList> WORKING_SINS = Arrays.stream(values())
            .filter(sin -> sin != NONE)
            .toList();

    private final Vector3f particleColor;

    SinsList(Vector3f particleColor) {
        this.particleColor = particleColor;
    }

    public Vector3f getParticleColor() {
        return this.particleColor;
    }
}
