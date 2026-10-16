package smartlibrary.algorithms;

import java.util.Arrays;

public class SuffixAutomation {

    private static class State {

        int length;
        int link;
        int[] next;

        State() {

            length = 0;
            link = -1;
            next = new int[128];

            Arrays.fill(next, -1);
        }
    }

    private State[] states;
    private int size;
    private int last;

    public SuffixAutomation(String text) {

        int capacity =
                Math.max(2, text.length() * 2);

        states = new State[capacity];

        for (int i = 0; i < capacity; i++) {
            states[i] = new State();
        }

        size = 1;
        last = 0;

        for (int i = 0; i < text.length(); i++) {

            extend(text.charAt(i));
        }
    }

    /**
     * Adds one character to the suffix automaton.
     */
    private void extend(char character) {

        int current = size++;

        states[current].length =
                states[last].length + 1;

        int previous = last;

        while (
                previous != -1
                        &&
                states[previous].next[character] == -1
        ) {

            states[previous].next[character] =
                    current;

            previous =
                    states[previous].link;
        }

        if (previous == -1) {

            states[current].link = 0;

        } else {

            int nextState =
                    states[previous].next[character];

            if (
                    states[previous].length + 1
                            ==
                    states[nextState].length
            ) {

                states[current].link =
                        nextState;

            } else {

                int clone = size++;

                states[clone].length =
                        states[previous].length + 1;

                states[clone].link =
                        states[nextState].link;

                states[clone].next =
                        states[nextState].next.clone();

                while (
                        previous != -1
                                &&
                        states[previous].next[character]
                                ==
                        nextState
                ) {

                    states[previous].next[character] =
                            clone;

                    previous =
                            states[previous].link;
                }

                states[nextState].link = clone;

                states[current].link = clone;
            }
        }

        last = current;
    }

    /**
     * Checks whether a complete pattern exists.
     *
     * Time Complexity: O(pattern length)
     */
    public boolean contains(String pattern) {

        if (pattern == null || pattern.isEmpty()) {
            return true;
        }

        int current = 0;

        for (int i = 0; i < pattern.length(); i++) {

            char character =
                    pattern.charAt(i);

            if (character >= 128) {
                return false;
            }

            int nextState =
                    states[current].next[character];

            if (nextState == -1) {
                return false;
            }

            current = nextState;
        }

        return true;
    }

    /**
     * Returns the length of the longest prefix of the
     * pattern found in the document.
     */
    public int longestPrefixMatch(String pattern) {

        if (pattern == null || pattern.isEmpty()) {
            return 0;
        }

        int current = 0;
        int matchedLength = 0;

        for (int i = 0; i < pattern.length(); i++) {

            char character =
                    pattern.charAt(i);

            if (character >= 128) {
                break;
            }

            int nextState =
                    states[current].next[character];

            if (nextState == -1) {
                break;
            }

            current = nextState;
            matchedLength++;
        }

        return matchedLength;
    }

    /**
     * Returns the number of states created.
     */
    public int getStateCount() {
        return size;
    }
}