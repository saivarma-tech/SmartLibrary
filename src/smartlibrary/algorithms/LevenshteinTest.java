package smartlibrary.algorithms;

public class LevenshteinTest {

    public static void main(String[] args) {

        String first = "algorithum";
        String second = "algorithm";

        int distance =
                LevenshteinDistance.calculate(
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
                "Edit Distance: " + distance
        );

        LevenshteinDistance.displayMatrix(
                first,
                second
        );
    }
}