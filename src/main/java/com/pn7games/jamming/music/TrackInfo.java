package com.pn7games.jamming.music;

import net.minecraft.locale.Language;
import net.minecraft.resources.Identifier;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.TreeSet;

// How a track is shown to the player: title, artist, where it plays (dimensions) and in which biomes / situations.
// Vanilla has names for its tracks in the lang file (e.g. "Aaron Cherof - A Familiar Room").
public record TrackInfo(Identifier id, String title, String artist, List<String> dimensions, List<String> places) {

    public static final String OVERWORLD = "Overworld";
    public static final String NETHER = "Nether";
    public static final String END = "End";
    public static final String MENU = "Menu";
    public static final String DISC = "Disc";

    private static final String DISC_FOLDER = "records/";

    public static TrackInfo of(Identifier id) {
        String path = id.getPath();
        Language language = Language.getInstance();

        // Vanilla music: minecraft:music/game/calm1 -> lang key "music.game.calm1"
        // Music discs:  minecraft:records/cat      -> lang key "jukebox_song.minecraft.cat"
        String key = path.startsWith(DISC_FOLDER)
                ? "jukebox_song." + id.getNamespace() + "." + path.substring(DISC_FOLDER.length())
                : id.toShortLanguageKey();
        String name = language.has(key) ? language.getOrDefault(key) : prettify(lastSegment(path));

        // "Artist - Title" -> separate parts, so the list can show them in two styles.
        String title = name;
        String artist = "";
        int dash = name.indexOf(" - ");
        if (dash > 0) {
            artist = name.substring(0, dash);
            title = name.substring(dash + 3);
        }

        // Where it plays, read from the events that contain it.
        Set<String> dimensions = new TreeSet<>(TrackInfo::compareDimensions);
        Set<String> places = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);
        for (Identifier event : MusicLibrary.eventsOf(id)) {
            readEvent(event, dimensions, places, language);
        }
        if (dimensions.isEmpty()) {
            // No known event (e.g. a mod's own music system): fall back to the mod name.
            dimensions.add(id.getNamespace().equals(Identifier.DEFAULT_NAMESPACE) ? OVERWORLD : id.getNamespace());
        }
        return new TrackInfo(id, title, artist, List.copyOf(dimensions), List.copyOf(places));
    }

    // music.nether.crimson_forest -> Nether + "Crimson Forest"
    // music.overworld.cherry_grove -> Overworld + "Cherry Grove"
    // music.under_water           -> Overworld + "Underwater"
    private static void readEvent(Identifier event, Set<String> dimensions, Set<String> places, Language language) {
        String path = event.getPath();
        if (path.startsWith("music_disc")) {
            dimensions.add(DISC);
        } else if (path.equals("music.menu")) {
            dimensions.add(MENU);
        } else if (path.startsWith("music.nether")) {
            dimensions.add(NETHER);
            addBiome(event, "music.nether.", places, language);
        } else if (path.equals("music.end") || path.equals("music.dragon") || path.startsWith("music.end.")) {
            dimensions.add(END);
            if (path.equals("music.dragon")) {
                places.add("Ender Dragon");
            }
        } else if (path.equals("music.credits")) {
            dimensions.add(END); // the credits roll after the Ender Dragon
            places.add("Credits");
        } else {
            dimensions.add(OVERWORLD); // music.game, music.creative, music.under_water, music.overworld.*
            switch (path) {
                case "music.creative" -> places.add("Creative");
                case "music.under_water" -> places.add("Underwater");
                default -> addBiome(event, "music.overworld.", places, language);
            }
        }
    }

    // music.overworld.cherry_grove -> biome lang key "biome.minecraft.cherry_grove" -> "Cherry Grove"
    private static void addBiome(Identifier event, String prefix, Set<String> places, Language language) {
        String path = event.getPath();
        if (!path.startsWith(prefix)) {
            return;
        }
        String biome = path.substring(prefix.length());
        String key = "biome." + event.getNamespace() + "." + biome;
        places.add(language.has(key) ? language.getOrDefault(key) : prettify(biome));
    }

    // Overworld, Nether, End, Menu, Disc, then mods (alphabetically).
    public static int compareDimensions(String a, String b) {
        int order = Integer.compare(dimensionOrder(a), dimensionOrder(b));
        return order != 0 ? order : a.compareToIgnoreCase(b);
    }

    private static int dimensionOrder(String dimension) {
        return switch (dimension) {
            case OVERWORLD -> 0;
            case NETHER -> 1;
            case END -> 2;
            case MENU -> 3;
            case DISC -> 4;
            default -> 5;
        };
    }

    // "Overworld" or "Overworld / End"
    public String dimensionLabel() {
        return String.join(" / ", dimensions);
    }

    public boolean isDisc() {
        return dimensions.contains(DISC);
    }

    public boolean matches(String search) {
        String s = search.toLowerCase(Locale.ROOT);
        return title.toLowerCase(Locale.ROOT).contains(s)
                || artist.toLowerCase(Locale.ROOT).contains(s)
                || id.toString().contains(s);
    }

    // "#nether", "#cherry", "#under"
    public boolean matchesPlace(String search) {
        String s = search.toLowerCase(Locale.ROOT);
        List<String> all = new ArrayList<>(dimensions);
        all.addAll(places);
        return all.stream().anyMatch(p -> p.toLowerCase(Locale.ROOT).contains(s));
    }

    private static String lastSegment(String path) {
        return path.substring(path.lastIndexOf('/') + 1);
    }

    // "under_water" -> "Under Water"
    private static String prettify(String raw) {
        StringBuilder out = new StringBuilder();
        for (String word : raw.split("_")) {
            if (word.isEmpty()) {
                continue;
            }
            if (!out.isEmpty()) {
                out.append(' ');
            }
            out.append(Character.toUpperCase(word.charAt(0))).append(word.substring(1));
        }
        return out.toString();
    }
}
