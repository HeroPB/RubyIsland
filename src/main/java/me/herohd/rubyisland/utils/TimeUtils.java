package me.herohd.rubyisland.utils;

import java.util.concurrent.TimeUnit;

public class TimeUtils {

    /**
     * Converte i millisecondi in una stringa formattata HH:MM:SS o MM:SS.
     * @param millis I millisecondi da formattare.
     * @return Una stringa che rappresenta il tempo.
     */
    public static String formatTime(long millis) {
        if (millis < 0) {
            return "00:00";
        }

        long hours = TimeUnit.MILLISECONDS.toHours(millis);
        long minutes = TimeUnit.MILLISECONDS.toMinutes(millis) % 60;
        long seconds = TimeUnit.MILLISECONDS.toSeconds(millis) % 60;

        if (hours > 0) {
            return String.format("%02d:%02d:%02d", hours, minutes, seconds);
        } else {
            return String.format("%02d:%02d", minutes, seconds);
        }
    }
}
