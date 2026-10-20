package com.pn7games.jamming.client;

// Added to vanilla SoundEngine by SoundEngineMixin: pause/unpause only the MUSIC channels.
public interface MusicPause {

    void jamming$setMusicPaused(boolean paused);
}
