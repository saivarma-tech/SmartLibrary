package smartlibrary.algorithms;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

/** Exact small SAT instances, classic reductions, and a vertex-cover approximation. */
public final class ComplexityAlgorithms {
    private static final int MAX_EXACT_SAT_VARIABLES = 25;

    private ComplexityAlgorithms() { }

    /** Returns a satisfying assignment, or empty when the formula is unsatisfiable. */
    public static Optional<boolean[]> findSatisfyingAssignment(
            int variables, int[][] clauses) {
        validateFormula(variables, clauses);
        if (variables > MAX_EXACT_SAT_VARIABLES) {
            throw new IllegalArgumentException("exact SAT is limited to 25 variables");
        }
        long assignments = 1L << variables;
        for (long mask = 0; mask < assignments; mask++) {
            boolean allClausesSatisfied = true;
            for (int[] clause : clauses) {
                boolean clauseSatisfied = false;
                for (int literal : clause) {
                    boolean value = ((mask >>> (Math.abs((long) literal) - 1)) & 1L) != 0;
                    if (literal < 0) {
                        value = !value;
                    }
                    if (value) {
                        clauseSatisfied = true;
                        break;
                    }
                }
                if (!clauseSatisfied) {
                    allClausesSatisfied = false;
                    break;
                }
            }
            if (allClausesSatisfied) {
                boolean[] assignment = new boolean[variables + 1];
                for (int variable = 1; variable <= variables; variable++) {
                    assignment[variable] = ((mask >>> (variable - 1)) & 1L) != 0;
                }
                return Optional.of(assignment);
            }
        }
        return Optional.empty();
    }

    /** Brute-force SAT solver for small teaching and resource-constraint instances. */
    public static boolean isSatisfiable(int variables, int[][] clauses) {
        return findSatisfyingAssignment(variables, clauses).isPresent();
    }

    private static void validateFormula(int variables, int[][] clauses) {
        if (variables < 0 || clauses == null) {
            throw new IllegalArgumentException("invalid SAT formula");
        }
        for (int[] clause : clauses) {
            if (clause == null) {
                throw new IllegalArgumentException("clause cannot be null");
            }
            for (int literal : clause) {
                if (literal == 0 || Math.abs((long) literal) > variables) {
                    throw new IllegalArgumentException("literal outside variable range");
                }
            }
        }
    }

    /** A graph instance produced by the 3-SAT to CLIQUE reduction. */
    public static final class CliqueInstance {
        public final int vertices;
        public final int required;
        public final int[][] edges;

        private CliqueInstance(int vertices, int required, int[][] edges) {
            this.vertices = vertices;
            this.required = required;
            this.edges = deepCopy(edges);
        }

        public int[][] edges() {
            return deepCopy(edges);
        }
    }

    /**
     * Builds the standard reduction: one vertex per literal occurrence and
     * cross-clause edges between non-contradictory literals. The target clique
     * size is the number of clauses.
     */
    public static CliqueInstance threeSatToClique(int variables, int[][] clauses) {
        validateFormula(variables, clauses);
        for (int[] clause : clauses) {
            if (clause.length != 3) {
                throw new IllegalArgumentException(
                        "3-SAT to CLIQUE requires exactly three literals per clause");
            }
        }
        List<int[]> edges = new ArrayList<>();
        for (int firstClause = 0; firstClause < clauses.length; firstClause++) {
            for (int firstPosition = 0; firstPosition < 3; firstPosition++) {
                int firstLiteral = clauses[firstClause][firstPosition];
                for (int secondClause = firstClause + 1;
                     secondClause < clauses.length; secondClause++) {
                    for (int secondPosition = 0; secondPosition < 3; secondPosition++) {
                        int secondLiteral = clauses[secondClause][secondPosition];
                        if (firstLiteral != -secondLiteral) {
                            edges.add(new int[] {
                                    firstClause * 3 + firstPosition,
                                    secondClause * 3 + secondPosition
                            });
                        }
                    }
                }
            }
        }
        return new CliqueInstance(clauses.length * 3, clauses.length,
                edges.toArray(int[][]::new));
    }

    /** Returns the complement graph; a k-clique becomes a k-independent set. */
    public static int[][] cliqueToIndependentSet(int vertices, int[][] edges) {
        validateGraph(vertices, edges);
        boolean[][] adjacent = adjacencyMatrix(vertices, edges);
        List<int[]> complement = new ArrayList<>();
        for (int first = 0; first < vertices; first++) {
            for (int second = first + 1; second < vertices; second++) {
                if (!adjacent[first][second]) {
                    complement.add(new int[] {first, second});
                }
            }
        }
        return complement.toArray(int[][]::new);
    }

    /**
     * In the same graph, an independent set of size k corresponds to a vertex
     * cover of size n-k. The edge list therefore stays unchanged.
     */
    public static int[][] independentSetToVertexCover(int vertices, int[][] edges) {
        validateGraph(vertices, edges);
        return deepCopy(edges);
    }

    /** Full size bookkeeping for the Independent-Set to Vertex-Cover reduction. */
    public static final class VertexCoverInstance {
        public final int vertices;
        public final int requiredCoverSize;
        public final int[][] edges;

        private VertexCoverInstance(int vertices, int requiredCoverSize, int[][] edges) {
            this.vertices = vertices;
            this.requiredCoverSize = requiredCoverSize;
            this.edges = deepCopy(edges);
        }

        public int[][] edges() {
            return deepCopy(edges);
        }
    }

    public static VertexCoverInstance independentSetToVertexCover(
            int vertices, int[][] edges, int independentSetSize) {
        validateGraph(vertices, edges);
        if (independentSetSize < 0 || independentSetSize > vertices) {
            throw new IllegalArgumentException("independent-set size outside graph range");
        }
        return new VertexCoverInstance(vertices, vertices - independentSetSize, edges);
    }

    /** Composes CLIQUE -> INDEPENDENT-SET -> VERTEX-COVER size parameters. */
    public static VertexCoverInstance cliqueToVertexCover(
            int vertices, int[][] edges, int cliqueSize) {
        validateGraph(vertices, edges);
        if (cliqueSize < 0 || cliqueSize > vertices) {
            throw new IllegalArgumentException("clique size outside graph range");
        }
        int[][] complement = cliqueToIndependentSet(vertices, edges);
        return independentSetToVertexCover(vertices, complement, cliqueSize);
    }

    /**
     * Greedy maximal-matching algorithm for vertex cover. For each uncovered
     * edge it selects both endpoints; the returned cover is at most 2*OPT.
     */
    public static Set<Integer> vertexCoverTwoApprox(int vertices, int[][] edges) {
        validateGraph(vertices, edges);
        boolean[] selected = new boolean[vertices];
        Set<Integer> cover = new LinkedHashSet<>();
        for (int[] edge : edges) {
            int first = edge[0];
            int second = edge[1];
            if (!selected[first] && !selected[second]) {
                selected[first] = true;
                selected[second] = true;
                cover.add(first);
                cover.add(second);
            }
        }
        return Collections.unmodifiableSet(cover);
    }

    private static boolean[][] adjacencyMatrix(int vertices, int[][] edges) {
        boolean[][] adjacent = new boolean[vertices][vertices];
        for (int[] edge : edges) {
            adjacent[edge[0]][edge[1]] = true;
            adjacent[edge[1]][edge[0]] = true;
        }
        return adjacent;
    }

    private static void validateGraph(int vertices, int[][] edges) {
        if (vertices < 0 || edges == null) {
            throw new IllegalArgumentException("invalid graph");
        }
        for (int[] edge : edges) {
            if (edge == null || edge.length != 2 || edge[0] < 0 || edge[1] < 0
                    || edge[0] >= vertices || edge[1] >= vertices) {
                throw new IllegalArgumentException("invalid graph edge");
            }
        }
    }

    private static int[][] deepCopy(int[][] edges) {
        Objects.requireNonNull(edges, "edges cannot be null");
        int[][] copy = new int[edges.length][];
        for (int i = 0; i < edges.length; i++) {
            copy[i] = edges[i].clone();
        }
        return copy;
    }
}
