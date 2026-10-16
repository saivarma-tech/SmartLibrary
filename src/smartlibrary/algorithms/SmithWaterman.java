package smartlibrary.algorithms;

public class SmithWaterman {

    private SmithWaterman() {
        // Utility class
    }

    /**
     * Smith-Waterman local sequence alignment.
     *
     * Match       = +3
     * Mismatch    = -3
     * Gap         = -2
     *
     * Important:
     * Negative scores are replaced with 0.
     *
     * Time Complexity: O(m * n)
     * Space Complexity: O(m * n)
     */
    public static int calculateBestScore(
            String first,
            String second) {

        if (first == null) {
            first = "";
        }

        if (second == null) {
            second = "";
        }

        int m = first.length();
        int n = second.length();

        int matchScore = 3;
        int mismatchScore = -3;
        int gapPenalty = -2;

        int[][] dp =
                new int[m + 1][n + 1];

        int bestScore = 0;

        for (int i = 1; i <= m; i++) {

            for (int j = 1; j <= n; j++) {

                int diagonalScore;

                if (
                        first.charAt(i - 1)
                                ==
                        second.charAt(j - 1)
                ) {

                    diagonalScore =
                            dp[i - 1][j - 1]
                                    + matchScore;

                } else {

                    diagonalScore =
                            dp[i - 1][j - 1]
                                    + mismatchScore;
                }

                int upScore =
                        dp[i - 1][j] + gapPenalty;

                int leftScore =
                        dp[i][j - 1] + gapPenalty;

                dp[i][j] =
                        Math.max(
                                0,
                                Math.max(
                                        diagonalScore,
                                        Math.max(
                                                upScore,
                                                leftScore
                                        )
                                )
                        );

                if (dp[i][j] > bestScore) {
                    bestScore = dp[i][j];
                }
            }
        }

        return bestScore;
    }

    /**
     * Displays the Smith-Waterman matrix.
     */
    public static void displayMatrix(
            String first,
            String second) {

        if (first == null) {
            first = "";
        }

        if (second == null) {
            second = "";
        }

        int m = first.length();
        int n = second.length();

        int matchScore = 3;
        int mismatchScore = -3;
        int gapPenalty = -2;

        int[][] dp =
                new int[m + 1][n + 1];

        int bestScore = 0;
        int bestRow = 0;
        int bestColumn = 0;

        for (int i = 1; i <= m; i++) {

            for (int j = 1; j <= n; j++) {

                int diagonalScore;

                if (
                        first.charAt(i - 1)
                                ==
                        second.charAt(j - 1)
                ) {

                    diagonalScore =
                            dp[i - 1][j - 1]
                                    + matchScore;

                } else {

                    diagonalScore =
                            dp[i - 1][j - 1]
                                    + mismatchScore;
                }

                int upScore =
                        dp[i - 1][j] + gapPenalty;

                int leftScore =
                        dp[i][j - 1] + gapPenalty;

                dp[i][j] =
                        Math.max(
                                0,
                                Math.max(
                                        diagonalScore,
                                        Math.max(
                                                upScore,
                                                leftScore
                                        )
                                )
                        );

                if (dp[i][j] > bestScore) {

                    bestScore = dp[i][j];
                    bestRow = i;
                    bestColumn = j;
                }
            }
        }

        System.out.println();
        System.out.println(
                "========== SMITH-WATERMAN MATRIX =========="
        );

        System.out.print("      ");

        for (int j = 0; j < n; j++) {

            System.out.printf(
                    "%4s",
                    second.charAt(j)
            );
        }

        System.out.println();

        for (int i = 0; i <= m; i++) {

            if (i == 0) {

                System.out.print("    ");

            } else {

                System.out.printf(
                        "%2s  ",
                        first.charAt(i - 1)
                );
            }

            for (int j = 0; j <= n; j++) {

                System.out.printf(
                        "%4d",
                        dp[i][j]
                );
            }

            System.out.println();
        }

        System.out.println(
                "============================================"
        );

        System.out.println(
                "Best Local Alignment Score: "
                        + bestScore
        );

        System.out.println(
                "Best Score Position: ["
                        + bestRow
                        + "]["
                        + bestColumn
                        + "]"
        );
    }

    /**
     * Returns the best local alignment.
     */
    public static String[] align(
            String first,
            String second) {

        if (first == null) {
            first = "";
        }

        if (second == null) {
            second = "";
        }

        int m = first.length();
        int n = second.length();

        int matchScore = 3;
        int mismatchScore = -3;
        int gapPenalty = -2;

        int[][] dp =
                new int[m + 1][n + 1];

        int bestScore = 0;
        int bestRow = 0;
        int bestColumn = 0;

        for (int i = 1; i <= m; i++) {

            for (int j = 1; j <= n; j++) {

                int diagonalScore;

                if (
                        first.charAt(i - 1)
                                ==
                        second.charAt(j - 1)
                ) {

                    diagonalScore =
                            dp[i - 1][j - 1]
                                    + matchScore;

                } else {

                    diagonalScore =
                            dp[i - 1][j - 1]
                                    + mismatchScore;
                }

                int upScore =
                        dp[i - 1][j] + gapPenalty;

                int leftScore =
                        dp[i][j - 1] + gapPenalty;

                dp[i][j] =
                        Math.max(
                                0,
                                Math.max(
                                        diagonalScore,
                                        Math.max(
                                                upScore,
                                                leftScore
                                        )
                                )
                        );

                if (dp[i][j] > bestScore) {

                    bestScore = dp[i][j];
                    bestRow = i;
                    bestColumn = j;
                }
            }
        }

        StringBuilder alignedFirst =
                new StringBuilder();

        StringBuilder alignedSecond =
                new StringBuilder();

        int i = bestRow;
        int j = bestColumn;

        while (
                i > 0
                        &&
                j > 0
                        &&
                dp[i][j] > 0
        ) {

            int currentScore =
                    dp[i][j];

            int diagonalScore;

            if (
                    first.charAt(i - 1)
                            ==
                    second.charAt(j - 1)
            ) {

                diagonalScore =
                        dp[i - 1][j - 1]
                                + matchScore;

            } else {

                diagonalScore =
                        dp[i - 1][j - 1]
                                + mismatchScore;
            }

            if (currentScore == diagonalScore) {

                alignedFirst.append(
                        first.charAt(i - 1)
                );

                alignedSecond.append(
                        second.charAt(j - 1)
                );

                i--;
                j--;

            } else if (
                    currentScore
                            ==
                    dp[i - 1][j] + gapPenalty
            ) {

                alignedFirst.append(
                        first.charAt(i - 1)
                );

                alignedSecond.append('-');

                i--;

            } else {

                alignedFirst.append('-');

                alignedSecond.append(
                        second.charAt(j - 1)
                );

                j--;
            }
        }

        return new String[] {
                alignedFirst.reverse().toString(),
                alignedSecond.reverse().toString()
        };
    }
}