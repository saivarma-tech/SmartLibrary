package smartlibrary.web;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Small JSON reader/writer for the dependency-free local HTTP API. */
final class JsonCodec {
    private JsonCodec() { }

    static Object parse(String source) {
        Parser parser = new Parser(source == null ? "" : source);
        Object value = parser.value();
        parser.space();
        if (parser.at != parser.text.length()) throw new IllegalArgumentException("Unexpected text after JSON value.");
        return value;
    }

    static Map<String, Object> parseObject(String source) {
        Object value = parse(source);
        if (!(value instanceof Map<?, ?> map)) throw new IllegalArgumentException("Expected a JSON object.");
        Map<String, Object> result = new LinkedHashMap<>();
        map.forEach((key, item) -> result.put(String.valueOf(key), item));
        return result;
    }

    static String stringify(Object value) {
        if (value == null) return "null";
        if (value instanceof String string) return quote(string);
        if (value instanceof Number || value instanceof Boolean) return String.valueOf(value);
        if (value instanceof Map<?, ?> map) {
            List<String> items = new ArrayList<>();
            map.forEach((key, item) -> items.add(quote(String.valueOf(key)) + ":" + stringify(item)));
            return "{" + String.join(",", items) + "}";
        }
        if (value instanceof Iterable<?> iterable) {
            List<String> items = new ArrayList<>();
            iterable.forEach(item -> items.add(stringify(item)));
            return "[" + String.join(",", items) + "]";
        }
        return quote(String.valueOf(value));
    }

    private static String quote(String value) {
        StringBuilder out = new StringBuilder("\"");
        for (char c : value.toCharArray()) {
            switch (c) {
                case '"' -> out.append("\\\"");
                case '\\' -> out.append("\\\\");
                case '\n' -> out.append("\\n");
                case '\r' -> out.append("\\r");
                case '\t' -> out.append("\\t");
                default -> { if (c < 0x20) out.append(String.format("\\u%04x", (int)c)); else out.append(c); }
            }
        }
        return out.append('"').toString();
    }

    private static final class Parser {
        final String text;
        int at;
        Parser(String text) { this.text = text; }
        void space() { while (at < text.length() && Character.isWhitespace(text.charAt(at))) at++; }
        Object value() {
            space();
            if (at >= text.length()) throw new IllegalArgumentException("JSON body is empty.");
            char c = text.charAt(at);
            if (c == '{') return object();
            if (c == '[') return array();
            if (c == '"') return string();
            if (text.startsWith("true", at)) { at += 4; return true; }
            if (text.startsWith("false", at)) { at += 5; return false; }
            if (text.startsWith("null", at)) { at += 4; return null; }
            return number();
        }
        Map<String, Object> object() {
            at++; space(); Map<String, Object> result = new LinkedHashMap<>();
            if (take('}')) return result;
            while (true) {
                space(); if (at >= text.length() || text.charAt(at) != '"') throw new IllegalArgumentException("Expected a JSON object key.");
                String key = string(); space(); require(':'); result.put(key, value()); space();
                if (take('}')) return result; require(',');
            }
        }
        List<Object> array() {
            at++; space(); List<Object> result = new ArrayList<>();
            if (take(']')) return result;
            while (true) { result.add(value()); space(); if (take(']')) return result; require(','); }
        }
        String string() {
            require('"'); StringBuilder out = new StringBuilder();
            while (at < text.length()) {
                char c = text.charAt(at++);
                if (c == '"') return out.toString();
                if (c != '\\') { out.append(c); continue; }
                if (at >= text.length()) break;
                char e = text.charAt(at++);
                switch (e) {
                    case '"', '\\', '/' -> out.append(e);
                    case 'b' -> out.append('\b'); case 'f' -> out.append('\f');
                    case 'n' -> out.append('\n'); case 'r' -> out.append('\r'); case 't' -> out.append('\t');
                    case 'u' -> { if (at + 4 > text.length()) throw new IllegalArgumentException("Invalid Unicode escape."); out.append((char)Integer.parseInt(text.substring(at, at + 4), 16)); at += 4; }
                    default -> throw new IllegalArgumentException("Invalid JSON escape.");
                }
            }
            throw new IllegalArgumentException("Unterminated JSON string.");
        }
        Number number() {
            int start = at;
            while (at < text.length() && "-+0123456789.eE".indexOf(text.charAt(at)) >= 0) at++;
            if (start == at) throw new IllegalArgumentException("Invalid JSON value.");
            String n = text.substring(start, at);
            try {
                if (n.contains(".") || n.contains("e") || n.contains("E")) return Double.valueOf(n);
                return Long.valueOf(n);
            }
            catch (NumberFormatException exception) { throw new IllegalArgumentException("Invalid JSON number."); }
        }
        boolean take(char c) { if (at < text.length() && text.charAt(at) == c) { at++; return true; } return false; }
        void require(char c) { if (!take(c)) throw new IllegalArgumentException("Expected '" + c + "' in JSON."); }
    }
}
