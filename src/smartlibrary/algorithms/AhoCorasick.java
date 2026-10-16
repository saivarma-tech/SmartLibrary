package smartlibrary.algorithms;

import java.util.*;

public class AhoCorasick {

    // ==========================================
    // TRIE NODE
    // ==========================================

    static class Node {

        Map<Character, Node> children;

        Node failure;

        boolean isEnd;

        Node() {
            children = new HashMap<>();
            failure = null;
            isEnd = false;
        }
    }


    // ==========================================
    // ROOT OF TRIE
    // ==========================================

    private Node root;


    // ==========================================
    // CONSTRUCTOR
    // ==========================================

    public AhoCorasick() {

        root = new Node();
        root.failure = root;
    }


    // ==========================================
    // ADD PATTERN
    // ==========================================

    public void addPattern(String pattern) {

        if (pattern == null || pattern.isEmpty()) {
            return;
        }

        pattern = pattern.toLowerCase();

        Node current = root;

        for (char c : pattern.toCharArray()) {

            if (!current.children.containsKey(c)) {

                current.children.put(c, new Node());
            }

            current = current.children.get(c);
        }

        current.isEnd = true;
    }


    // ==========================================
    // BUILD FAILURE LINKS
    // ==========================================

    public void buildFailureLinks() {

        Queue<Node> queue = new LinkedList<>();

        // First level nodes
        for (Node child : root.children.values()) {

            child.failure = root;

            queue.add(child);
        }


        // BFS
        while (!queue.isEmpty()) {

            Node current = queue.poll();

            for (Map.Entry<Character, Node> entry
                    : current.children.entrySet()) {

                char character = entry.getKey();

                Node child = entry.getValue();

                Node failureNode =
                        current.failure;


                while (failureNode != root &&
                       !failureNode.children.containsKey(character)) {

                    failureNode =
                            failureNode.failure;
                }


                if (failureNode.children.containsKey(character) &&
                    failureNode.children.get(character) != child) {

                    child.failure =
                            failureNode.children.get(character);

                } else {

                    child.failure = root;
                }


                queue.add(child);
            }
        }
    }


    // ==========================================
    // SEARCH MULTIPLE PATTERNS
    // ==========================================

    public boolean search(String text) {

        if (text == null || text.isEmpty()) {
            return false;
        }

        text = text.toLowerCase();

        Node current = root;

        for (char character : text.toCharArray()) {

            while (current != root &&
                   !current.children.containsKey(character)) {

                current = current.failure;
            }

            if (current.children.containsKey(character)) {

                current =
                        current.children.get(character);

            } else {

                current = root;
            }


            // Check if a pattern was found
            if (current.isEnd) {
                return true;
            }


            // Check failure chain
            Node failureNode =
                    current.failure;

            while (failureNode != root) {

                if (failureNode.isEnd) {
                    return true;
                }

                failureNode =
                        failureNode.failure;
            }
        }

        return false;
    }
}