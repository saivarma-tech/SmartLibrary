package smartlibrary.algorithms;

public class NeedlemanWunsch {

    private NeedlemanWunsch() {
        // Utility class
    }

    /**
     * Performs global sequence alignment.
     *
     * Match       = +1
     * Mismatch    = -1
     * Gap         = -2
     *
     * Time Complexity: O(m * n)
     * Space Complexity: O(m * n)
     */
    public static int calculateScore(
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

        int matchScore = 1;
        int mismatchScore = -1;
        int gapPenalty = -2;

        int[][] dp =
                new int[m + 1][n + 1];

        for (int i = 1; i <= m; i++) {

            dp[i][0] =
                    dp[i - 1][0] + gapPenalty;
        }

        for (int j = 1; j <= n; j++) {

            dp[0][j] =
                    dp[0][j - 1] + gapPenalty;
        }

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
                                diagonalScore,
                                Math.max(
                                        upScore,
                                        leftScore
                                )
                        );
            }
        }

        return dp[m][n];
    }

    /**
     * Displays the dynamic programming matrix.
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

        int matchScore = 1;
        int mismatchScore = -1;
        int gapPenalty = -2;

        int[][] dp =
                new int[m + 1][n + 1];

        for (int i = 1; i <= m; i++) {

            dp[i][0] =
                    dp[i - 1][0] + gapPenalty;
        }

        for (int j = 1; j <= n; j++) {

            dp[0][j] =
                    dp[0][j - 1] + gapPenalty;
        }

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
                                diagonalScore,
                                Math.max(
                                        upScore,
                                        leftScore
                                )
                        );
            }
        }

        System.out.println();
        System.out.println(
                "========== NEEDLEMAN-WUNSCH MATRIX =========="
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
                "============================================="
        );

        System.out.println(
                "Global Alignment Score: "
                        + dp[m][n]
        );
    }

    /**
     * Returns one possible global alignment.
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

        int matchScore = 1;
        int mismatchScore = -1;
        int gapPenalty = -2;

        int[][] dp =
                new int[m + 1][n + 1];

        for (int i = 1; i <= m; i++) {

            dp[i][0] =
                    dp[i - 1][0] + gapPenalty;
        }

        for (int j = 1; j <= n; j++) {

            dp[0][j] =
                    dp[0][j - 1] + gapPenalty;
        }

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
                                diagonalScore,
                                Math.max(
                                        upScore,
                                        leftScore
                                )
                        );
            }
        }

        StringBuilder alignedFirst =
                new StringBuilder();

        StringBuilder alignedSecond =
                new StringBuilder();

        int i = m;
        int j = n;

        while (i > 0 || j > 0) {

            if (
                    i > 0
                            &&
                    j > 0
                            &&
                    dp[i][j]
                            ==
                    dp[i - 1][j - 1]
                            +
                    (
                            first.charAt(i - 1)
                                    ==
                            second.charAt(j - 1)
                                    ? matchScore
                                    : mismatchScore
                    )
            ) {

                alignedFirst.append(
                        first.charAt(i - 1)
                );

                alignedSecond.append(
                        second.charAt(j - 1)
                );

                i--;
                j--;

            } else if (
                    i > 0
                            &&
                    dp[i][j]
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