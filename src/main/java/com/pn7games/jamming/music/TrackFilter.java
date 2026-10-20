package com.pn7games.jamming.music;

import java.util.Locale;

// Smart search: "calm @C418 #nether" = text "calm", artist contains "C418", plays in a place containing "nether".
// Plain text comes first; everything after @ is the artist, everything after # is a dimension or biome.
public record TrackFilter(String text, String artist, String place) {

    public static TrackFilter parse(String input) {
        StringBuilder text = new StringBuilder();
        StringBuilder artist = new StringBuilder();
        StringBuilder place = new StringBuilder();
        StringBuilder target = text;
        for (int i = 0; i < input.length(); i++) {
            char c = input.charAt(i);
            boolean startsWord = i == 0 || Character.isWhitespace(input.charAt(i - 1));
            if (startsWord && c == '@') {
                target = artist;
            } else if (startsWord && c == '#') {
                target = place;
            } else {
                target.append(c);
            }
        }
        return new TrackFilter(clean(text), clean(artist), clean(place));
    }

    private static String clean(StringBuilder s) {
        return s.toString().trim().toLowerCase(Locale.ROOT);
    }

    public boolean test(TrackInfo track) {
        return (text.isEmpty() || track.matches(text))
                && (artist.isEmpty() || track.artist().toLowerCase(Locale.ROOT).contains(artist))
                && (place.isEmpty() || track.matchesPlace(place));
    }
}
