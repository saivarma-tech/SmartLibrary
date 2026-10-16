package smartlibrary.algorithms;

public class SmithWatermanTest {

    public static void main(String[] args) {

        String first =
                "ACGTACGT";

        String second =
                "TAC";

        int score =
                SmithWaterman.calculateBestScore(
                        first,
                        second
                );

        System.out.println(
                "First Sequence  : " + first
        );

        System.out.println(
                "Second Sequence : " + second
        );

        System.out.println(
                "Best Local Alignment Score: "
                        + score
        );

        SmithWaterman.displayMatrix(
                first,
                second
        );

        String[] alignment =
                SmithWaterman.align(
                        first,
                        second
                );

        System.out.println();
        System.out.println(
                "========== LOCAL ALIGNMENT =========="
        );

        System.out.println(
                "Aligned First  : "
                        + alignment[0]
        );

        System.out.println(
                "Aligned Second : "
                        + alignment[1]
        );

        System.out.println(
                "======================================"
        );
    }
}