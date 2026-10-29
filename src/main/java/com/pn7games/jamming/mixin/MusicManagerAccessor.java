package com.pn7games.jamming.mixin;

import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.client.sounds.MusicManager;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

// Access to private parts of vanilla MusicManager.
@Mixin(MusicManager.class)
public interface MusicManagerAccessor {

    @Nullable
    @Accessor("currentMusic")
    SoundInstance jamming$getCurrentMusic();
}
