package com.nindybun.burnergun.client.particles;

import com.nindybun.burnergun.client.particles.lightParticle.LightParticleType;
import com.nindybun.burnergun.common.BurnerGun;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraftforge.fmllegacy.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

public class ModParticles {
    public static final DeferredRegister<ParticleType<?>> PARTICLE = DeferredRegister.create(ForgeRegistries.PARTICLE_TYPES, BurnerGun.MOD_ID);

    public static final RegistryObject<SimpleParticleType> LIGHT_PARTICLE = PARTICLE.register("light_particle", LightParticleType::new);

}
