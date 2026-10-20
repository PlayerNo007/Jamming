package com.pn7games.jamming.mixin;

import com.google.common.collect.Multimap;
import com.mojang.blaze3d.audio.Channel;
import com.pn7games.jamming.client.JammingPlayer;
import com.pn7games.jamming.client.MusicPause;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.client.sounds.ChannelAccess;
import net.minecraft.client.sounds.SoundEngine;
import net.minecraft.sounds.SoundSource;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Map;

@Mixin(SoundEngine.class)
public abstract class SoundEngineMixin implements MusicPause {

    @Shadow
    private boolean loaded;

    @Shadow
    @Final
    private Map<SoundInstance, ChannelAccess.ChannelHandle> instanceToChannel;

    @Shadow
    @Final
    private Multimap<SoundSource, SoundInstance> instanceBySource;

    // Pause/unpause only the channels that play MUSIC. Other sounds keep playing.
    @Override
    public void jamming$setMusicPaused(boolean paused) {
        if (!loaded) {
            return;
        }
        for (SoundInstance sound : instanceBySource.get(SoundSource.MUSIC)) {
            ChannelAccess.ChannelHandle channel = instanceToChannel.get(sound);
            if (channel != null) {
                channel.execute(paused ? Channel::pause : Channel::unpause);
            }
        }
    }

    // Vanilla resumes ALL sounds when you close the pause menu. Keep our music paused.
    @Inject(method = "resume", at = @At("TAIL"))
    private void jamming$stayPaused(CallbackInfo ci) {
        if (JammingPlayer.shouldMusicBePaused()) {
            jamming$setMusicPaused(true);
        }
    }
}
