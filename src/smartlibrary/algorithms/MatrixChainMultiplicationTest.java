package smartlibrary.algorithms;

public class MatrixChainMultiplicationTest {

    public static void main(String[] args) {

        /*
         * A1 = 10 x 30
         * A2 = 30 x 5
         * A3 = 5 x 60
         */
        int[] dimensions = {
                10,
                30,
                5,
                60
        };

        int minimumCost =
                MatrixChainMultiplication
                        .calculateMinimumCost(
                                dimensions
                        );

        System.out.println(
                "Matrix Dimensions:"
        );

        System.out.println(
                "A1 = 10 x 30"
        );

        System.out.println(
                "A2 = 30 x 5"
        );

        System.out.println(
                "A3 = 5 x 60"
        );

        System.out.println();

        System.out.println(
                "Minimum Scalar Multiplications: "
                        + minimumCost
        );

        String optimalOrder =
                MatrixChainMultiplication
                        .getOptimalParenthesization(
                                dimensions
                        );

        System.out.println(
                "Optimal Parenthesization: "
                        + optimalOrder
        );

        MatrixChainMultiplication.displayMatrix(
                dimensions
        );
    }
}