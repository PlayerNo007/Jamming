package com.pn7games.jamming.music;

import net.minecraft.resources.Identifier;

import java.util.Map;
import java.util.Set;

// Added to vanilla SoundManager.Preparations by SoundManagerPreparationsMixin.
// Collects the music found while sounds.json files load (on a background thread),
// then SoundManagerMixin hands it to MusicLibrary on the main thread.
public interface MusicRegistrations {

    Map<Identifier, Set<Identifier>> jamming$filesByEvent();

    Map<Identifier, Set<Identifier>> jamming$refsByEvent();
}
