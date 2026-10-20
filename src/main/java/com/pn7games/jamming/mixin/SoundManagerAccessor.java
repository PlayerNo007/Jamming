package com.pn7games.jamming.mixin;

import net.minecraft.client.resources.sounds.Sound;
import net.minecraft.client.sounds.SoundEngine;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.ResourceProvider;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

// Access to private parts of vanilla SoundManager.
@Mixin(SoundManager.class)
public interface SoundManagerAccessor {

    @Accessor("soundEngine")
    SoundEngine jamming$getSoundEngine();

    @Invoker("validateSoundResource")
    static boolean jamming$validateSoundResource(Sound sound, Identifier event, ResourceProvider resources) {
        throw new AssertionError(); // replaced by Mixin at runtime
    }
}
