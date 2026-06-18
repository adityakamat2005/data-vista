package com.dataanalyzer.util;

import java.util.Set;

/**
 * Centralized definition of what counts as a "missing" value.
 * Beyond truly blank cells, this also recognizes common sentinel
 * strings used by other tools (e.g. R's "NA") to mark missing data.
 */
public final class MissingValueUtils {

    private static final Set<String> MISSING_TOKENS = Set.of(
            "NA", "N/A", "NULL", "NAN", "NONE", "MISSING", "-", "?"
    );

    private MissingValueUtils() {
    }

    public static boolean isMissing(String value) {
        if (value == null) {
            return true;
        }
        String trimmed = value.trim();
        if (trimmed.isEmpty()) {
            return true;
        }
        return MISSING_TOKENS.contains(trimmed.toUpperCase());
    }
}
