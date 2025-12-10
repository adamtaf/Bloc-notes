package utils;

import java.util.ArrayList;
import java.util.List;

public final class CSVUtils {
    private CSVUtils(){}

    public static String escapeForCsv(String s, char separator) {
        if (s == null) return "";
        String out = s.replace("\"", "\"\"");
        if (out.indexOf(separator) >= 0 || out.contains("\"") || out.contains("\n") || out.contains("\r")) {
            return "\"" + out + "\"";
        }
        return out;
    }

    public static String unescapeFromCsv(String s) {
        if (s == null) return "";
        s = s.trim();
        if (s.length() >= 2 && s.startsWith("\"") && s.endsWith("\"")) {
            s = s.substring(1, s.length()-1).replace("\"\"", "\"");
        }
        return s;
    }

    public static String[] splitCsvLine(String line, char separator) {
        List<String> parts = new ArrayList<>();
        boolean inQuote = false;
        StringBuilder cur = new StringBuilder();
        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            if (c == '"') {
                inQuote = !inQuote;
                cur.append(c); // keep quotes for unescape
            } else if (c == separator && !inQuote) {
                parts.add(cur.toString());
                cur.setLength(0);
            } else {
                cur.append(c);
            }
        }
        parts.add(cur.toString());
        // trim quotes will be handled by unescapeFromCsv by callers
        return parts.toArray(new String[0]);
    }
}
