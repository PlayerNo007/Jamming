package com.pn7games.jamming.music;

import net.minecraft.resources.Identifier;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

// Every music track the game knows (vanilla, music discs, resource packs, other mods).
// A "track" is one sound FILE, e.g. minecraft:music/game/calm1.
// An "event" is what the game asks for, e.g. minecraft:music.game, which contains many tracks.
// Rebuilt after every sound reload (see SoundManagerMixin).
public final class MusicLibrary {

    // event -> the files listed directly in it
    private static Map<Identifier, Set<Identifier>> filesByEvent = Map.of();
    // event -> other events it includes (e.g. music.creative includes music.game)
    private static Map<Identifier, Set<Identifier>> refsByEvent = Map.of();
    private static List<Identifier> allTracks = List.of();
    // tracksFor() results, cleared on every reload
    private static final Map<Identifier, Set<Identifier>> CACHE = new HashMap<>();
    // track -> every event that can play it (the reverse of tracksFor), built on first use
    private static Map<Identifier, Set<Identifier>> eventsByTrack = null;

    private MusicLibrary() {
    }

    public static void publish(Map<Identifier, Set<Identifier>> files, Map<Identifier, Set<Identifier>> refs) {
        filesByEvent = Map.copyOf(files);
        refsByEvent = Map.copyOf(refs);
        Set<Identifier> all = new HashSet<>();
        files.values().forEach(all::addAll);
        List<Identifier> sorted = new ArrayList<>(all);
        Collections.sort(sorted);
        allTracks = List.copyOf(sorted);
        CACHE.clear();
        eventsByTrack = null;
    }

    // Every event that can play this track, e.g. music.game + music.overworld.cherry_grove.
    // This tells us WHERE a track plays (dimension, biome), which the file path does not:
    // vanilla keeps Nether and End music in the music/game/ folder too.
    public static Set<Identifier> eventsOf(Identifier track) {
        if (eventsByTrack == null) {
            Map<Identifier, Set<Identifier>> reverse = new HashMap<>();
            Set<Identifier> events = new HashSet<>(filesByEvent.keySet());
            events.addAll(refsByEvent.keySet());
            for (Identifier event : events) {
                for (Identifier t : tracksFor(event)) {
                    reverse.computeIfAbsent(t, k -> new HashSet<>()).add(event);
                }
            }
            eventsByTrack = reverse;
        }
        return eventsByTrack.getOrDefault(track, Set.of());
    }

    public static List<Identifier> allTracks() {
        return allTracks;
    }

    // All tracks of an event, including the events it refers to.
    public static Set<Identifier> tracksFor(Identifier event) {
        return CACHE.computeIfAbsent(event, MusicLibrary::collect);
    }

    private static Set<Identifier> collect(Identifier event) {
        Set<Identifier> tracks = new HashSet<>();
        Set<Identifier> visited = new HashSet<>();
        Deque<Identifier> todo = new ArrayDeque<>();
        todo.add(event);
        while (!todo.isEmpty()) {
            Identifier current = todo.poll();
            if (!visited.add(current)) {
                continue; // already checked (events can refer to each other in a loop)
            }
            tracks.addAll(filesByEvent.getOrDefault(current, Set.of()));
            todo.addAll(refsByEvent.getOrDefault(current, Set.of()));
        }
        return Set.copyOf(tracks);
    }

    // Which sound events count as music.
    public static boolean isMusicEvent(Identifier event) {
        return event.getPath().contains("music");
    }
}
