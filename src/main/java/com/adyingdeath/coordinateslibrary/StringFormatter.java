package com.adyingdeath.coordinateslibrary;

public class StringFormatter {
    public static String of(String input, Object... replacements) {
        if (input == null || replacements == null) {
            return input;
        }

        // Temporarily replace all %% with a unique placeholder to avoid conflict with %xxx%
        String placeholder = "&P&";
        input = input.replace("%%", placeholder);

        // Replace all %xxx% with corresponding values from the replacements map
        for (int i = 1;i < replacements.length;i += 2) {
            String key = String.valueOf(replacements[i - 1]);
            String value = String.valueOf(replacements[i]);
            if (key != null && value != null) {
                input = input.replace(key, value);
            }
        }

        // Restore the original %% back to %
        input = input.replace(placeholder, "%");

        return input;
    }
}