package smartlibrary.algorithms;

import java.util.ArrayList;
import java.util.List;

public class BitmaskResourceSelection {

    private BitmaskResourceSelection() {
        // Utility class
    }

    public static SelectionResult findBestSelection(
            String[] resourceNames,
            int[] weights,
            int[] values,
            int capacity) {

        validateInput(resourceNames, weights, values, capacity);

        int n = resourceNames.length;
        int totalMasks = 1 << n;

        int[] totalWeight = new int[totalMasks];
        int[] totalValue = new int[totalMasks];

        int bestMask = 0;
        int bestWeight = 0;
        int bestValue = 0;

        /*
         * Bitmask DP:
         *
         * Every mask represents one possible combination.
         *
         * Example for 4 resources:
         *
         * 0001 -> resource 1
         * 0010 -> resource 2
         * 0011 -> resources 1 and 2
         * 0101 -> resources 1 and 3
         *
         * We calculate the weight and value of every mask
         * using the previous mask:
         *
         * previousMask = mask without its last set bit
         */

        for (int mask = 1; mask < totalMasks; mask++) {

            int selectedBit = mask & -mask;
            int resourceIndex = Integer.numberOfTrailingZeros(selectedBit);

            int previousMask = mask ^ selectedBit;

            totalWeight[mask] =
                    totalWeight[previousMask] + weights[resourceIndex];

            totalValue[mask] =
                    totalValue[previousMask] + values[resourceIndex];

            if (totalWeight[mask] <= capacity) {

                if (totalValue[mask] > bestValue) {

                    bestMask = mask;
                    bestWeight = totalWeight[mask];
                    bestValue = totalValue[mask];

                } else if (totalValue[mask] == bestValue
                        && totalWeight[mask] < bestWeight) {

                    bestMask = mask;
                    bestWeight = totalWeight[mask];
                }
            }
        }

        List<String> selectedResources = new ArrayList<>();

        for (int i = 0; i < n; i++) {

            if ((bestMask & (1 << i)) != 0) {
                selectedResources.add(resourceNames[i]);
            }
        }

        return new SelectionResult(
                bestMask,
                bestWeight,
                bestValue,
                selectedResources
        );
    }

    private static void validateInput(
            String[] resourceNames,
            int[] weights,
            int[] values,
            int capacity) {

        if (resourceNames == null
                || weights == null
                || values == null) {

            throw new IllegalArgumentException(
                    "Input arrays cannot be null"
            );
        }

        if (resourceNames.length != weights.length
                || weights.length != values.length) {

            throw new IllegalArgumentException(
                    "All arrays must have the same length"
            );
        }

        if (resourceNames.length == 0) {

            throw new IllegalArgumentException(
                    "At least one resource is required"
            );
        }

        if (resourceNames.length > 20) {

            throw new IllegalArgumentException(
                    "Maximum supported resources are 20"
            );
        }

        if (capacity < 0) {

            throw new IllegalArgumentException(
                    "Capacity cannot be negative"
            );
        }

        for (int i = 0; i < weights.length; i++) {

            if (weights[i] < 0) {

                throw new IllegalArgumentException(
                        "Weight cannot be negative at index " + i
                );
            }

            if (values[i] < 0) {

                throw new IllegalArgumentException(
                        "Value cannot be negative at index " + i
                );
            }
        }
    }

    public static String maskToBinary(int mask, int numberOfResources) {

        String binary = Integer.toBinaryString(mask);

        while (binary.length() < numberOfResources) {
            binary = "0" + binary;
        }

        return binary;
    }

    public static class SelectionResult {

        private final int mask;
        private final int totalWeight;
        private final int totalValue;
        private final List<String> selectedResources;

        public SelectionResult(
                int mask,
                int totalWeight,
                int totalValue,
                List<String> selectedResources) {

            this.mask = mask;
            this.totalWeight = totalWeight;
            this.totalValue = totalValue;
            this.selectedResources = selectedResources;
        }

        public int getMask() {
            return mask;
        }

        public int getTotalWeight() {
            return totalWeight;
        }

        public int getTotalValue() {
            return totalValue;
        }

        public List<String> getSelectedResources() {
            return selectedResources;
        }

        @Override
        public String toString() {

            return "SelectionResult{" +
                    "mask=" + mask +
                    ", totalWeight=" + totalWeight +
                    ", totalValue=" + totalValue +
                    ", selectedResources=" + selectedResources +
                    '}';
        }
    }
}