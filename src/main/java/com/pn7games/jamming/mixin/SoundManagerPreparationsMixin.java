package com.pn7games.jamming.mixin;

import com.pn7games.jamming.music.MusicLibrary;
import com.pn7games.jamming.music.MusicRegistrations;
import net.minecraft.client.resources.sounds.Sound;
import net.minecraft.client.resources.sounds.SoundEventRegistration;
import net.minecraft.client.sounds.SoundEngine;
import net.minecraft.client.sounds.WeighedSoundEvents;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceProvider;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

// Runs while the game reads every sounds.json (vanilla + resource packs + mods).
// For each music event we:
//  1. remember which files and which other events it contains (for MusicLibrary)
//  2. register every music FILE as its own little sound event, so one exact track can be played
@Mixin(targets = "net.minecraft.client.sounds.SoundManager$Preparations")
public abstract class SoundManagerPreparationsMixin implements MusicRegistrations {

    @Shadow
    @Final
    private Map<Identifier, WeighedSoundEvents> registry;

    @Shadow
    private Map<Identifier, Resource> soundCache;

    @Unique
    private final Map<Identifier, Set<Identifier>> jamming$files = new HashMap<>();
    @Unique
    private final Map<Identifier, Set<Identifier>> jamming$refs = new HashMap<>();

    @Inject(method = "handleRegistration", at = @At("TAIL"))
    private void jamming$collectMusic(Identifier event, SoundEventRegistration registration, CallbackInfo ci) {
        if (!MusicLibrary.isMusicEvent(event)) {
            return;
        }
        if (registration.isReplace()) {
            // a resource pack replaced this event: forget what earlier packs added
            jamming$files.remove(event);
            jamming$refs.remove(event);
        }
        ResourceProvider resources = ResourceProvider.fromMap(soundCache);
        for (Sound sound : registration.getSounds()) {
            Identifier location = sound.getLocation();
            switch (sound.getType()) {
                case FILE -> {
                    // Same check vanilla does: skip files that don't exist.
                    if (!SoundManagerAccessor.jamming$validateSoundResource(sound, event, resources)) {
                        continue;
                    }
                    jamming$files.computeIfAbsent(event, k -> new HashSet<>()).add(location);
                    WeighedSoundEvents single = new WeighedSoundEvents(location, registration.getSubtitle());
                    single.addSound(sound);
                    registry.put(location, single);
                }
                case SOUND_EVENT -> jamming$refs.computeIfAbsent(event, k -> new HashSet<>()).add(location);
            }
        }
    }

    // apply() copies everything into the real SoundManager (main thread). Then publish the music list.
    @Inject(method = "apply", at = @At("TAIL"))
    private void jamming$publishMusic(Map<Identifier, WeighedSoundEvents> target, Map<Identifier, Resource> cache,
                                      SoundEngine engine, CallbackInfo ci) {
        MusicLibrary.publish(jamming$files, jamming$refs);
    }

    @Override
    public Map<Identifier, Set<Identifier>> jamming$filesByEvent() {
        return jamming$files;
    }

    @Override
    public Map<Identifier, Set<Identifier>> jamming$refsByEvent() {
        return jamming$refs;
    }
}
