package com.pn7games.jamming.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.pn7games.jamming.client.JammingPlayer;
import com.pn7games.jamming.music.MusicLibrary;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.client.sounds.MusicManager;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.Music;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

// Vanilla MusicManager still decides WHEN to play (delays, fading, toast).
// We only change WHICH track it plays.
@Mixin(MusicManager.class)
public abstract class MusicManagerMixin {

    // While the player paused the music, or a jukebox / note blocks play nearby, the music manager "sleeps":
    // no new songs start, and the (paused) current song is not stopped or replaced.
    // Without this, changing dimension (vanilla stops all sounds) would start a new, unpaused song.
    @Inject(method = "tick", at = @At("HEAD"), cancellable = true)
    private void jamming$waitWhilePaused(CallbackInfo ci) {
        if (JammingPlayer.shouldMusicBePaused()) {
            ci.cancel();
        }
    }

    // startPlaying(Music) gets "situational" music (e.g. music.game). Swap it for one exact track.
    @ModifyVariable(method = "startPlaying", at = @At("HEAD"), argsOnly = true)
    private Music jamming$chooseTrack(Music situational) {
        return JammingPlayer.chooseTrack(situational);
    }

    // Vanilla stops the current song when the situation's music has replaceCurrentMusic = true
    // (main menu, underwater, End boss...) and the playing sound is not that music.
    // We keep the song playing if:
    //  - the player picked it themselves (otherwise a click in the main menu would be replaced right away), or
    //  - it belongs to the new situation anyway (our sound id is the track, so vanilla's id check never matches).
    @ModifyReturnValue(method = "canReplace", at = @At("RETURN"))
    private static boolean jamming$keepFittingTrack(boolean original, Music music, SoundInstance playing) {
        if (!original) {
            return false;
        }
        Identifier track = playing.getIdentifier();
        if (JammingPlayer.isUserChoice(track)) {
            return false;
        }
        return !MusicLibrary.tracksFor(music.sound().value().location()).contains(track);
    }
}
