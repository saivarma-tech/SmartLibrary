package smartlibrary.algorithms;

public class BitmaskResourceSelectionTest {

    public static void main(String[] args) {

        String[] resourceNames = {
                "Algorithms Book",
                "Data Structures Book",
                "AI Research Paper",
                "Database Book",
                "Operating Systems Book"
        };

        int[] weights = {
                4,
                3,
                2,
                5,
                4
        };

        int[] values = {
                10,
                8,
                6,
                9,
                7
        };

        int capacity = 7;

        BitmaskResourceSelection.SelectionResult result =
                BitmaskResourceSelection.findBestSelection(
                        resourceNames,
                        weights,
                        values,
                        capacity
                );

        System.out.println("======================================");
        System.out.println("BITMASK DP RESOURCE SELECTION");
        System.out.println("======================================");

        System.out.println("Maximum Capacity: " + capacity);

        System.out.println("Selected Mask: "
                + BitmaskResourceSelection.maskToBinary(
                        result.getMask(),
                        resourceNames.length
                ));

        System.out.println("Total Weight: "
                + result.getTotalWeight());

        System.out.println("Total Value: "
                + result.getTotalValue());

        System.out.println();
        System.out.println("Selected Resources:");

        for (String resource : result.getSelectedResources()) {
            System.out.println("- " + resource);
        }

        System.out.println("======================================");
    }
}