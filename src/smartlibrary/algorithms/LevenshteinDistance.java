package smartlibrary.algorithms;

public class LevenshteinDistance {

    private LevenshteinDistance() {
        // Utility class
    }

    /**
     * Calculates the minimum number of edits required
     * to convert one string into another.
     *
     * Allowed operations:
     *
     * 1. Insert a character
     * 2. Delete a character
     * 3. Replace a character
     *
     * Time Complexity: O(m * n)
     * Space Complexity: O(m * n)
     */
    public static int calculate(
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

        int[][] dp =
                new int[m + 1][n + 1];

        for (int i = 0; i <= m; i++) {
            dp[i][0] = i;
        }

        for (int j = 0; j <= n; j++) {
            dp[0][j] = j;
        }

        for (int i = 1; i <= m; i++) {

            for (int j = 1; j <= n; j++) {

                if (
                        first.charAt(i - 1)
                                ==
                        second.charAt(j - 1)
                ) {

                    dp[i][j] =
                            dp[i - 1][j - 1];

                } else {

                    int insert =
                            dp[i][j - 1] + 1;

                    int delete =
                            dp[i - 1][j] + 1;

                    int replace =
                            dp[i - 1][j - 1] + 1;

                    dp[i][j] =
                            Math.min(
                                    insert,
                                    Math.min(
                                            delete,
                                            replace
                                    )
                            );
                }
            }
        }

        return dp[m][n];
    }

    /**
     * Displays the complete DP matrix.
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

        int[][] dp =
                new int[m + 1][n + 1];

        for (int i = 0; i <= m; i++) {
            dp[i][0] = i;
        }

        for (int j = 0; j <= n; j++) {
            dp[0][j] = j;
        }

        for (int i = 1; i <= m; i++) {

            for (int j = 1; j <= n; j++) {

                if (
                        first.charAt(i - 1)
                                ==
                        second.charAt(j - 1)
                ) {

                    dp[i][j] =
                            dp[i - 1][j - 1];

                } else {

                    dp[i][j] =
                            1 + Math.min(
                                    dp[i - 1][j],
                                    Math.min(
                                            dp[i][j - 1],
                                            dp[i - 1][j - 1]
                                    )
                            );
                }
            }
        }

        System.out.println();
        System.out.println(
                "========== LEVENSHTEIN DP MATRIX =========="
        );

        System.out.print("    ");

        for (int j = 0; j < n; j++) {
            System.out.print(
                    second.charAt(j) + " "
            );
        }

        System.out.println();

        for (int i = 0; i <= m; i++) {

            if (i == 0) {
                System.out.print("  ");
            } else {
                System.out.print(
                        first.charAt(i - 1) + " "
                );
            }

            for (int j = 0; j <= n; j++) {

                System.out.printf(
                        "%2d ",
                        dp[i][j]
                );
            }

            System.out.println();
        }

        System.out.println(
                "============================================"
        );

        System.out.println(
                "Minimum Edit Distance: "
                        + dp[m][n]
        );
    }
}