package utils;

import java.util.ArrayList;
import java.util.List;

/**
 * Helpers CSV minimalistes.
 */
public final class CSVUtils {
    private CSVUtils(){}

    public static String escapeForCsv(String s) {
        if (s == null) return "";
        String out = s.replace("\"", "\"\"");
        if (out.contains(",") || out.contains("\"") || out.contains("\n")) {
            return "\"" + out + "\"";
        }
        return out;
    }

    public static String unescapeFromCsv(String s) {
        if (s == null) return "";
        s = s.trim();
        if (s.startsWith("\"") && s.endsWith("\"")) {
            s = s.substring(1, s.length()-1).replace("\"\"", "\"");
        }
        return s;
    }

    public static String[] splitCsvLine(String line) {
        // naive CSV splitter supporting quoted fields
        List<String> parts = new ArrayList<>();
        boolean inQuote = false;
        StringBuilder cur = new StringBuilder();
        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            if (c == '"') {
                inQuote = !inQuote;
                cur.append(c); // keep quotes so unescape can trim
            } else if (c == ',' && !inQuote) {
                parts.add(cur.toString().replaceAll("^\"|\"$", ""));
                cur.setLength(0);
            } else {
                cur.append(c);
            }
        }
        parts.add(cur.toString().replaceAll("^\"|\"$", ""));
        return parts.toArray(new String[0]);
    }
}
