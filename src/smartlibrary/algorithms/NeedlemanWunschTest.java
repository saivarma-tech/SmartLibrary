package smartlibrary.algorithms;

public class NeedlemanWunschTest {

    public static void main(String[] args) {

        String first =
                "GATTACA";

        String second =
                "GCATGCU";

        int score =
                NeedlemanWunsch.calculateScore(
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
                "Global Alignment Score: " + score
        );

        NeedlemanWunsch.displayMatrix(
                first,
                second
        );

        String[] alignment =
                NeedlemanWunsch.align(
                        first,
                        second
                );

        System.out.println();
        System.out.println(
                "========== GLOBAL ALIGNMENT =========="
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