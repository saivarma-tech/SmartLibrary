package smartlibrary.algorithms;

import java.util.Arrays;
import java.util.Comparator;

public class SuffixArray {

    private SuffixArray() {
        // Utility class
    }

    /**
     * Builds the suffix array using the doubling technique.
     *
     * Time Complexity:
     * O(n log^2 n)
     *
     * Space Complexity:
     * O(n)
     *
     * Example:
     * text = "banana"
     *
     * Suffix array:
     * [5, 3, 1, 0, 4, 2]
     */
    public static int[] buildSuffixArray(String text) {

        if (text == null || text.isEmpty()) {
            return new int[0];
        }

        int n = text.length();

        Integer[] suffixes = new Integer[n];

        int[] rank = new int[n];
        int[] newRank = new int[n];

        for (int i = 0; i < n; i++) {
            suffixes[i] = i;
            rank[i] = text.charAt(i);
        }

        for (int length = 1; length < n; length *= 2) {

            final int currentLength = length;

            Arrays.sort(
                    suffixes,
                    new Comparator<Integer>() {

                        @Override
                        public int compare(Integer first, Integer second) {

                            if (rank[first] != rank[second]) {
                                return Integer.compare(
                                        rank[first],
                                        rank[second]
                                );
                            }

                            int firstNextRank =
                                    first + currentLength < n
                                            ? rank[first + currentLength]
                                            : -1;

                            int secondNextRank =
                                    second + currentLength < n
                                            ? rank[second + currentLength]
                                            : -1;

                            return Integer.compare(
                                    firstNextRank,
                                    secondNextRank
                            );
                        }
                    }
            );

            newRank[suffixes[0]] = 0;

            for (int i = 1; i < n; i++) {

                int previous = suffixes[i - 1];
                int current = suffixes[i];

                boolean different =
                        rank[previous] != rank[current];

                int previousNextRank =
                        previous + length < n
                                ? rank[previous + length]
                                : -1;

                int currentNextRank =
                        current + length < n
                                ? rank[current + length]
                                : -1;

                if (different ||
                        previousNextRank != currentNextRank) {

                    newRank[current] =
                            newRank[previous] + 1;

                } else {

                    newRank[current] =
                            newRank[previous];
                }
            }

            System.arraycopy(
                    newRank,
                    0,
                    rank,
                    0,
                    n
            );

            if (rank[suffixes[n - 1]] == n - 1) {
                break;
            }
        }

        int[] result = new int[n];

        for (int i = 0; i < n; i++) {
            result[i] = suffixes[i];
        }

        return result;
    }

    /**
     * Returns the suffix starting at a particular index.
     */
    public static String getSuffix(
            String text,
            int startIndex) {

        if (text == null ||
                startIndex < 0 ||
                startIndex >= text.length()) {

            return "";
        }

        return text.substring(startIndex);
    }

    /**
     * Displays the suffix array in a readable format.
     */
    public static void displaySuffixArray(String text) {

        if (text == null || text.isEmpty()) {
            System.out.println("No text available for indexing.");
            return;
        }

        int[] suffixArray = buildSuffixArray(text);

        System.out.println();
        System.out.println("======================================");
        System.out.println("           SUFFIX ARRAY INDEX         ");
        System.out.println("======================================");

        System.out.println("Original Text: " + text);
        System.out.println();

        System.out.printf(
                "%-12s %-12s %s%n",
                "Suffix No.",
                "Start Index",
                "Suffix"
        );

        System.out.println("--------------------------------------");

        for (int i = 0; i < suffixArray.length; i++) {

            int startIndex = suffixArray[i];

            System.out.printf(
                    "%-12d %-12d %s%n",
                    i,
                    startIndex,
                    text.substring(startIndex)
            );
        }

        System.out.println("======================================");
    }
}