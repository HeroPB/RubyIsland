package me.herohd.rubyisland.utils;

import java.util.LinkedHashMap;
import java.util.Map;

public class NumberUtils {

    // Usiamo una LinkedHashMap per mantenere l'ordine di inserimento.
    // Questo è importante per controllare i suffissi più lunghi prima di quelli più corti.
    private static final Map<String, Double> SUFFIXES = new LinkedHashMap<>();

    static {
        // Inserisci i suffissi dal più lungo al più corto per una corrispondenza corretta
        SUFFIXES.put("kq", 1_000_000_000_000_000_000.0); // 1k Quadrilioni = 1 Quintilione (1E18)
        SUFFIXES.put("q",  1_000_000_000_000_000.0);   // Quadrilione (1E15)
        SUFFIXES.put("t",  1_000_000_000_000.0);       // Trilione (1E12)
        SUFFIXES.put("b",  1_000_000_000.0);           // Bilione (1E9)
        SUFFIXES.put("m",  1_000_000.0);               // Milione (1E6)
        SUFFIXES.put("k",  1_000.0);                   // Kilo (1E3)
    }

    /**
     * Converte una stringa con suffisso (es. "10k", "1.5M", "1kq") in un valore double.
     * @param numberString La stringa da convertire.
     * @return Il valore numerico come double.
     */
    public static double parseBigNumber(String numberString) {
        if (numberString == null || numberString.isEmpty()) {
            return 0;
        }

        String lowerCaseString = numberString.toLowerCase();

        for (Map.Entry<String, Double> entry : SUFFIXES.entrySet()) {
            String suffix = entry.getKey();
            Double multiplier = entry.getValue();

            if (lowerCaseString.endsWith(suffix)) {
                try {
                    String numberPart = numberString.substring(0, numberString.length() - suffix.length());
                    double value = Double.parseDouble(numberPart);
                    return value * multiplier;
                } catch (NumberFormatException | StringIndexOutOfBoundsException e) {
                    // Ignora se la parte numerica non è valida (es. solo "k")
                    return 0;
                }
            }
        }

        // Se nessun suffisso corrisponde, prova a parsare l'intera stringa
        try {
            return Double.parseDouble(numberString);
        } catch (NumberFormatException e) {
            return 0;
        }
    }
}