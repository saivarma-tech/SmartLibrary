package smartlibrary.algorithms;

public class LCPArray {

    private LCPArray() {
        // Utility class
    }

    /**
     * Builds the LCP array using Kasai Algorithm.
     *
     * LCP means Longest Common Prefix.
     *
     * lcp[i] stores the longest common prefix length
     * between:
     *
     * suffixArray[i]
     * suffixArray[i - 1]
     *
     * For the first suffix, LCP is 0.
     *
     * Time Complexity: O(n)
     * Space Complexity: O(n)
     */
    public static int[] buildLCPArray(
            String text,
            int[] suffixArray) {

        if (text == null ||
                text.isEmpty() ||
                suffixArray == null ||
                suffixArray.length == 0) {

            return new int[0];
        }

        int n = text.length();

        int[] lcp = new int[n];

        int[] inverseSuffixArray = new int[n];

        for (int i = 0; i < n; i++) {

            inverseSuffixArray[
                    suffixArray[i]
            ] = i;
        }

        int commonLength = 0;

        for (int suffixStart = 0;
                suffixStart < n;
                suffixStart++) {

            int suffixRank =
                    inverseSuffixArray[suffixStart];

            if (suffixRank == 0) {

                commonLength = 0;
                continue;
            }

            int previousSuffixStart =
                    suffixArray[suffixRank - 1];

            while (
                    suffixStart + commonLength < n
                            &&
                    previousSuffixStart + commonLength < n
                            &&
                    text.charAt(
                                    suffixStart + commonLength
                            )
                            ==
                    text.charAt(
                                    previousSuffixStart
                                            + commonLength
                            )
            ) {

                commonLength++;
            }

            lcp[suffixRank] = commonLength;

            if (commonLength > 0) {
                commonLength--;
            }
        }

        return lcp;
    }

    /**
     * Displays suffix array and LCP array together.
     */
    public static void displayLCPArray(String text) {

        if (text == null || text.isEmpty()) {

            System.out.println(
                    "No text available for LCP processing."
            );

            return;
        }

        int[] suffixArray =
                SuffixArray.buildSuffixArray(text);

        int[] lcpArray =
                buildLCPArray(
                        text,
                        suffixArray
                );

        System.out.println();
        System.out.println(
                "======================================"
        );

        System.out.println(
                "          LCP ARRAY USING KASAI       "
        );

        System.out.println(
                "======================================"
        );

        System.out.println(
                "Original Text: " + text
        );

        System.out.println();

        System.out.printf(
                "%-12s %-12s %-12s %s%n",
                "Suffix No.",
                "Start Index",
                "LCP Length",
                "Suffix"
        );

        System.out.println(
                "--------------------------------------"
        );

        for (int i = 0; i < suffixArray.length; i++) {

            int startIndex =
                    suffixArray[i];

            System.out.printf(
                    "%-12d %-12d %-12d %s%n",
                    i,
                    startIndex,
                    lcpArray[i],
                    text.substring(startIndex)
            );
        }

        System.out.println(
                "======================================"
        );
    }

    /**
     * Returns the longest common prefix of two suffixes.
     */
    public static String getLongestCommonPrefix(
            String text,
            int firstStart,
            int secondStart) {

        if (text == null ||
                firstStart < 0 ||
                secondStart < 0 ||
                firstStart >= text.length() ||
                secondStart >= text.length()) {

            return "";
        }

        int index = 0;

        while (
                firstStart + index < text.length()
                        &&
                secondStart + index < text.length()
                        &&
                text.charAt(firstStart + index)
                        ==
                text.charAt(secondStart + index)
        ) {

            index++;
        }

        return text.substring(
                firstStart,
                firstStart + index
        );
    }
}