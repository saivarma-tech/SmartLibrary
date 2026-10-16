package smartlibrary.web;

import java.util.List;
import java.util.Map;

/** Dependency-free JSON API codec regression check; run as a Java application. */
public final class JsonCodecTest {
    private JsonCodecTest() { }
    public static void main(String[] args) {
        Map<String, Object> parsed = JsonCodec.parseObject("{\"name\":\"Asha \\\"Reader\\\"\",\"copies\":3,\"active\":true,\"tags\":[\"science\",\"history\"]}");
        check(parsed.get("name").equals("Asha \"Reader\""), "escaped quotes parse");
        check(parsed.get("copies").equals(3L), "integer remains an integer");
        check(parsed.get("active").equals(Boolean.TRUE), "boolean parses");
        check(((List<?>)parsed.get("tags")).size() == 2, "arrays parse");
        String encoded = JsonCodec.stringify(Map.of("quote", "A\nB", "items", List.of(1, 2)));
        check(JsonCodec.parseObject(encoded).get("quote").equals("A\nB"), "strings round-trip");
        System.out.println("JsonCodecTest: PASS");
    }
    private static void check(boolean condition, String description) {
        if (!condition) throw new AssertionError(description);
    }
}
