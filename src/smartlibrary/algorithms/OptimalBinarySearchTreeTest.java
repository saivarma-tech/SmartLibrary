package smartlibrary.algorithms;

public class OptimalBinarySearchTreeTest {

    public static void main(String[] args) {

        String[] keywords = {
                "algorithm",
                "database",
                "machine learning",
                "operating system",
                "data structures"
        };

        int[] searchFrequencies = {
                40,
                10,
                30,
                20,
                50
        };

        OptimalBinarySearchTree.Result result =
                OptimalBinarySearchTree.buildOptimalBST(
                        keywords,
                        searchFrequencies
                );

        System.out.println("======================================");
        System.out.println("OPTIMAL BINARY SEARCH TREE");
        System.out.println("======================================");

        System.out.println("Keywords and Search Frequencies:");

        for (int i = 0; i < keywords.length; i++) {

            System.out.println(
                    keywords[i]
                    + " -> "
                    + searchFrequencies[i]
            );
        }

        System.out.println();

        System.out.println(
                "Minimum Search Cost: "
                + result.getMinimumSearchCost()
        );

        System.out.println();

        System.out.println("Optimal Search Tree:");

        System.out.println(
                result.getTreeStructure()
        );

        System.out.println("======================================");
    }
}