package com.adyingdeath.coordinateslibrary.effect;

import org.bukkit.Location;
import org.bukkit.Particle;
import org.jetbrains.annotations.NotNull;

public class ParticleEffect {
    /** 生成单个粒子 */
    public static void single(@NotNull Particle particle, @NotNull Location location) {
        location.getWorld().spawnParticle(particle, location, 1, 0, 0, 0, 0);
    }
    public static void circle(@NotNull Particle particle, @NotNull Location location, double radius, int count) {
        double unit = Math.PI / count;
        for(double i = Math.PI * 2 - Math.random() * unit;i > 0;i -= unit){
            ParticleEffect.single(particle, location.clone().add(Math.cos(i) * radius, 0, Math.sin(i) * radius));
        }
    }
}
