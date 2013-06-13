package jas.common.spawner.creature.handler.parsing;

import jas.common.JASLog;
import jas.common.spawner.creature.type.CreatureTypeRegistry;

public class ParsingHelper {

    /**
     * Attempt to Parse an Integer. Performs no filters.
     * 
     * @param value String to be Parsed
     * @param fallBack Default value if value cannot be parsed
     * @param fieldName FieldName that is being parsed for error reporting. Null will omit it.
     * @return
     */
    public static int parseInteger(String value, int fallBack, String fieldName) {
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            if (fieldName != null) {
                JASLog.warning(
                        "Error Parsing %s for an integer. %s was unreadable. The Default value of %s will be used.",
                        value, fieldName, fallBack);
            } else {
                JASLog.warning("Error Parsing %s for an integer. The Default value of %s will be used.", value,
                        fallBack);
            }
            return fallBack;
        }
    }

    /**
     * Attempt to Parse an Integer. Filters all non-numeric Characters, excluding the negative (-) sign.
     */
    public static int parseFilteredInteger(String value, int fallBack, String fieldName) {
        try {
            return Integer.parseInt(value.replaceAll("[^\\d-]", ""));
        } catch (NumberFormatException e) {
            if (fieldName != null) {
                JASLog.warning(
                        "Error Parsing %s for an integer. %s was unreadable. The Default value of %s will be used.",
                        value, fieldName, fallBack);
            } else {
                JASLog.warning("Error Parsing %s for an integer. The Default value of %s will be used.", value,
                        fallBack);
            }
            return fallBack;
        }
    }

    /**
     * Attempt to Parse an Long. Filters all non-numeric Characters, excluding the negative (-) sign.
     */
    public static Long parseFilteredLong(String value, Long fallBack, String fieldName) {
        try {
            return Long.parseLong(value.replaceAll("[^\\d-]", ""));
        } catch (NumberFormatException e) {
            if (fieldName != null) {
                JASLog.warning(
                        "Error Parsing %s for an long. %s was unreadable. The Default value of %s will be used.",
                        value, fieldName, fallBack);
            } else {
                JASLog.warning("Error Parsing %s for an long. The Default value of %s will be used.", value, fallBack);
            }
            return fallBack;
        }
    }

    /**
     * Attempt to Parse an Float. Filters all non-numeric Characters, excluding the negative (-) and (.) sign.
     */
    public static float parseFilteredFloat(String value, float fallBack, String fieldName) {
        try {
            return Float.parseFloat(value.replaceAll("[^\\d.-]", ""));
        } catch (NumberFormatException e) {
            if (fieldName != null) {
                JASLog.warning(
                        "Error Parsing %s for an double. %s was unreadable. The Default value of %s will be used.",
                        value, fieldName, fallBack);
            } else {
                JASLog.warning("Error Parsing %s for an double. The Default value of %s will be used.", value, fallBack);
            }
            return fallBack;
        }
    }

    /**
     * Attempt to Parse an Double. Filters all non-numeric Characters, excluding the negative (-) and (.) sign.
     */
    public static double parseFilteredDouble(String value, double fallBack, String fieldName) {
        try {
            return Double.parseDouble(value.replaceAll("[^\\d.-]", ""));
        } catch (NumberFormatException e) {
            if (fieldName != null) {
                JASLog.warning(
                        "Error Parsing %s for an double. %s was unreadable. The Default value of %s will be used.",
                        value, fieldName, fallBack);
            } else {
                JASLog.warning("Error Parsing %s for an double. The Default value of %s will be used.", value, fallBack);
            }
            return fallBack;
        }
    }

    /**
     * Attempt to Parse an Boolean
     * 
     * @param value String to be Parsed
     * @param fallBack Default value if value cannot be parsed
     * @param fieldName FieldName that is being parsed for error reporting. Null will omit it.
     * @return
     */
    public static boolean parseBoolean(String value, boolean fallBack, String fieldName) {
        if (!value.equalsIgnoreCase("true") && !value.equalsIgnoreCase("false")) {
            if (fieldName != null) {
                JASLog.warning(
                        "Error Parsing %s for a boolean. %s was unreadable. The Default value of %s will be used.",
                        value, fieldName, fallBack);
            } else {
                JASLog.warning("Error Parsing %s for a boolean. The Default value of %s will be used.", value, fallBack);
            }
            return fallBack;
        } else {
            return Boolean.parseBoolean(value);
        }
    }

    /**
     * Attempt to Parse CreatureTypeID
     * 
     * @param value String to be Parsed
     * @param fallBack Default value if value cannot be parsed
     * @param fieldName FieldName that is being parsed for error reporting. Null will omit it.
     * @return
     */
    public static String parseCreatureTypeID(String value, String fallBack, String fieldName) {
        if (!CreatureTypeRegistry.NONE.equalsIgnoreCase(value)
                && CreatureTypeRegistry.INSTANCE.getCreatureType(value) == null) {
            if (fieldName != null) {
                JASLog.warning(
                        "Error parsing entry %s. CreatureType of %s was unreadable. Value will be set to default %s",
                        value, fieldName, fallBack);
            } else {
                JASLog.warning("Error parsing entry %s. Value will be set to default %s", value, fallBack);
            }
            return fallBack;
        }
        return value.toUpperCase();
    }
}
