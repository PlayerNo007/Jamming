package com.pn7games.jamming.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.pn7games.jamming.client.JammingConfig;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

// Vanilla picks a random wait before the next song (10-20 min by default, depends on Options > Music Frequency).
// If the player chose a "Gap" in the Jamming menu, we use that instead.
@Mixin(targets = "net.minecraft.client.sounds.MusicManager$MusicFrequency")
public abstract class MusicFrequencyMixin {

    @ModifyReturnValue(method = "getNextSongDelay", at = @At("RETURN"))
    private int jamming$useCustomGap(int original) {
        int seconds = JammingConfig.gapSeconds();
        return seconds == JammingConfig.VANILLA_GAP ? original : seconds * 20; // 20 ticks = 1 second
    }
}
