package smartlibrary.algorithms;

public class OptimalBinarySearchTree {

    private OptimalBinarySearchTree() {
        // Utility class
    }

    public static Result buildOptimalBST(
            String[] keys,
            int[] frequencies) {

        validateInput(keys, frequencies);

        int n = keys.length;

        int[][] cost = new int[n][n];
        int[][] root = new int[n][n];

        /*
         * Prefix sum helps calculate the frequency sum
         * of any interval quickly.
         */
        int[] prefixSum = new int[n + 1];

        for (int i = 0; i < n; i++) {
            prefixSum[i + 1] =
                    prefixSum[i] + frequencies[i];
        }

        /*
         * Cost of a single key is its frequency.
         */
        for (int i = 0; i < n; i++) {
            cost[i][i] = frequencies[i];
            root[i][i] = i;
        }

        /*
         * length represents the number of keys
         * included in the current interval.
         */
        for (int length = 2; length <= n; length++) {

            for (int start = 0;
                 start <= n - length;
                 start++) {

                int end = start + length - 1;

                cost[start][end] = Integer.MAX_VALUE;

                int frequencySum =
                        prefixSum[end + 1] - prefixSum[start];

                /*
                 * Try every key as the root of this interval.
                 */
                for (int currentRoot = start;
                     currentRoot <= end;
                     currentRoot++) {

                    int leftCost = 0;
                    int rightCost = 0;

                    if (currentRoot > start) {
                        leftCost =
                                cost[start][currentRoot - 1];
                    }

                    if (currentRoot < end) {
                        rightCost =
                                cost[currentRoot + 1][end];
                    }

                    int currentCost =
                            leftCost
                            + rightCost
                            + frequencySum;

                    if (currentCost < cost[start][end]) {

                        cost[start][end] = currentCost;
                        root[start][end] = currentRoot;
                    }
                }
            }
        }

        String treeStructure =
                buildTreeStructure(
                        keys,
                        root,
                        0,
                        n - 1,
                        0
                );

        return new Result(
                cost[0][n - 1],
                treeStructure,
                cost,
                root
        );
    }

    private static String buildTreeStructure(
            String[] keys,
            int[][] root,
            int start,
            int end,
            int level) {

        if (start > end) {
            return "";
        }

        int currentRoot =
                root[start][end];

        StringBuilder result =
                new StringBuilder();

        result.append("  ".repeat(level))
                .append("Root: ")
                .append(keys[currentRoot])
                .append("\n");

        if (currentRoot > start) {

            result.append("  ".repeat(level))
                    .append("Left Subtree:\n");

            result.append(
                    buildTreeStructure(
                            keys,
                            root,
                            start,
                            currentRoot - 1,
                            level + 1
                    )
            );
        }

        if (currentRoot < end) {

            result.append("  ".repeat(level))
                    .append("Right Subtree:\n");

            result.append(
                    buildTreeStructure(
                            keys,
                            root,
                            currentRoot + 1,
                            end,
                            level + 1
                    )
            );
        }

        return result.toString();
    }

    private static void validateInput(
            String[] keys,
            int[] frequencies) {

        if (keys == null || frequencies == null) {
            throw new IllegalArgumentException(
                    "Input arrays cannot be null"
            );
        }

        if (keys.length == 0) {
            throw new IllegalArgumentException(
                    "At least one key is required"
            );
        }

        if (keys.length != frequencies.length) {
            throw new IllegalArgumentException(
                    "Keys and frequencies must have equal length"
            );
        }

        for (int i = 0; i < keys.length; i++) {

            if (keys[i] == null
                    || keys[i].trim().isEmpty()) {

                throw new IllegalArgumentException(
                        "Key cannot be empty at index " + i
                );
            }

            if (frequencies[i] < 0) {

                throw new IllegalArgumentException(
                        "Frequency cannot be negative at index " + i
                );
            }
        }
    }

    public static class Result {

        private final int minimumSearchCost;
        private final String treeStructure;
        private final int[][] costTable;
        private final int[][] rootTable;

        public Result(
                int minimumSearchCost,
                String treeStructure,
                int[][] costTable,
                int[][] rootTable) {

            this.minimumSearchCost = minimumSearchCost;
            this.treeStructure = treeStructure;
            this.costTable = costTable;
            this.rootTable = rootTable;
        }

        public int getMinimumSearchCost() {
            return minimumSearchCost;
        }

        public String getTreeStructure() {
            return treeStructure;
        }

        public int[][] getCostTable() {
            return costTable;
        }

        public int[][] getRootTable() {
            return rootTable;
        }
    }
}