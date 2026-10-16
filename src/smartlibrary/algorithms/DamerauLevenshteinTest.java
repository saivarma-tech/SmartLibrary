package smartlibrary.algorithms;

public class DamerauLevenshteinTest {

    public static void main(String[] args) {

        String first = "algoritmh";
        String second = "algorithm";

        int distance =
                DamerauLevenshteinDistance.calculate(
                        first,
                        second
                );

        System.out.println(
                "First Word  : " + first
        );

        System.out.println(
                "Second Word : " + second
        );

        System.out.println(
                "Damerau-Levenshtein Distance: "
                        + distance
        );

        DamerauLevenshteinDistance.displayMatrix(
                first,
                second
        );
    }
}