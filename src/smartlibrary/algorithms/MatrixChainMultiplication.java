package smartlibrary.algorithms;

public class MatrixChainMultiplication {

    private MatrixChainMultiplication() {
        // Utility class
    }

    /**
     * Calculates minimum scalar multiplications.
     *
     * If dimensions are:
     *
     * A1 = p[0] x p[1]
     * A2 = p[1] x p[2]
     * ...
     *
     * Time Complexity: O(n^3)
     * Space Complexity: O(n^2)
     */
    public static int calculateMinimumCost(
            int[] dimensions) {

        if (
                dimensions == null
                        ||
                dimensions.length < 2
        ) {
            return 0;
        }

        int matrixCount =
                dimensions.length - 1;

        int[][] dp =
                new int[matrixCount + 1]
                        [matrixCount + 1];

        for (
                int chainLength = 2;
                chainLength <= matrixCount;
                chainLength++
        ) {

            for (
                    int i = 1;
                    i <= matrixCount - chainLength + 1;
                    i++
            ) {

                int j =
                        i + chainLength - 1;

                dp[i][j] =
                        Integer.MAX_VALUE;

                for (
                        int k = i;
                        k < j;
                        k++
                ) {

                    int cost =
                            dp[i][k]
                                    +
                            dp[k + 1][j]
                                    +
                            dimensions[i - 1]
                                    *
                            dimensions[k]
                                    *
                            dimensions[j];

                    if (
                            cost < dp[i][j]
                    ) {

                        dp[i][j] = cost;
                    }
                }
            }
        }

        return dp[1][matrixCount];
    }

    /**
     * Displays the Matrix-Chain DP table.
     */
    public static void displayMatrix(
            int[] dimensions) {

        if (
                dimensions == null
                        ||
                dimensions.length < 2
        ) {

            System.out.println(
                    "Invalid matrix dimensions."
            );

            return;
        }

        int matrixCount =
                dimensions.length - 1;

        int[][] dp =
                new int[matrixCount + 1]
                        [matrixCount + 1];

        for (
                int chainLength = 2;
                chainLength <= matrixCount;
                chainLength++
        ) {

            for (
                    int i = 1;
                    i <= matrixCount - chainLength + 1;
                    i++
            ) {

                int j =
                        i + chainLength - 1;

                dp[i][j] =
                        Integer.MAX_VALUE;

                for (
                        int k = i;
                        k < j;
                        k++
                ) {

                    int cost =
                            dp[i][k]
                                    +
                            dp[k + 1][j]
                                    +
                            dimensions[i - 1]
                                    *
                            dimensions[k]
                                    *
                            dimensions[j];

                    if (
                            cost < dp[i][j]
                    ) {

                        dp[i][j] = cost;
                    }
                }
            }
        }

        System.out.println();
        System.out.println(
                "======= MATRIX-CHAIN MULTIPLICATION ======="
        );

        System.out.println();

        System.out.print(
                "Dimensions: "
        );

        for (int dimension : dimensions) {

            System.out.print(
                    dimension + " "
            );
        }

        System.out.println();
        System.out.println();

        System.out.printf(
                "%-10s",
                "Matrix"
        );

        for (int j = 1; j <= matrixCount; j++) {

            System.out.printf(
                    "%-10s",
                    "A" + j
            );
        }

        System.out.println();

        System.out.println(
                "-------------------------------------------"
        );

        for (int i = 1; i <= matrixCount; i++) {

            System.out.printf(
                    "%-10s",
                    "A" + i
            );

            for (int j = 1; j <= matrixCount; j++) {

                if (i > j) {

                    System.out.printf(
                            "%-10s",
                            "-"
                    );

                } else if (i == j) {

                    System.out.printf(
                            "%-10s",
                            "0"
                    );

                } else {

                    System.out.printf(
                            "%-10d",
                            dp[i][j]
                    );
                }
            }

            System.out.println();
        }

        System.out.println(
                "==========================================="
        );

        System.out.println(
                "Minimum Scalar Multiplications: "
                        + dp[1][matrixCount]
        );
    }

    /**
     * Returns the optimal parenthesization.
     */
    public static String getOptimalParenthesization(
            int[] dimensions) {

        if (
                dimensions == null
                        ||
                dimensions.length < 2
        ) {
            return "";
        }

        int matrixCount =
                dimensions.length - 1;

        int[][] dp =
                new int[matrixCount + 1]
                        [matrixCount + 1];

        int[][] split =
                new int[matrixCount + 1]
                        [matrixCount + 1];

        for (
                int chainLength = 2;
                chainLength <= matrixCount;
                chainLength++
        ) {

            for (
                    int i = 1;
                    i <= matrixCount - chainLength + 1;
                    i++
            ) {

                int j =
                        i + chainLength - 1;

                dp[i][j] =
                        Integer.MAX_VALUE;

                for (
                        int k = i;
                        k < j;
                        k++
                ) {

                    int cost =
                            dp[i][k]
                                    +
                            dp[k + 1][j]
                                    +
                            dimensions[i - 1]
                                    *
                            dimensions[k]
                                    *
                            dimensions[j];

                    if (
                            cost < dp[i][j]
                    ) {

                        dp[i][j] = cost;
                        split[i][j] = k;
                    }
                }
            }
        }

        return buildParenthesization(
                split,
                1,
                matrixCount
        );
    }

    private static String buildParenthesization(
            int[][] split,
            int i,
            int j) {

        if (i == j) {

            return "A" + i;
        }

        int k =
                split[i][j];

        String left =
                buildParenthesization(
                        split,
                        i,
                        k
                );

        String right =
                buildParenthesization(
                        split,
                        k + 1,
                        j
                );

        return "(" + left + " × " + right + ")";
    }
}