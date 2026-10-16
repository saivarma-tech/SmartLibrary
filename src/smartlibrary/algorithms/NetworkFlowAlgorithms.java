package smartlibrary.algorithms;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/** Network-flow and reviewer-allocation algorithms for Module 4. */
public final class NetworkFlowAlgorithms {
    private NetworkFlowAlgorithms() { }

    /** A directed capacity edge. Vertices are numbered from zero. */
    public static final class Edge {
        public final int from;
        public final int to;
        public final int capacity;

        public Edge(int from, int to, int capacity) {
            if (capacity < 0) {
                throw new IllegalArgumentException("capacity must be nonnegative");
            }
            this.from = from;
            this.to = to;
            this.capacity = capacity;
        }
    }

    private static final class ResidualEdge {
        final int to;
        final int reverseIndex;
        int remaining;

        ResidualEdge(int to, int reverseIndex, int remaining) {
            this.to = to;
            this.reverseIndex = reverseIndex;
            this.remaining = remaining;
        }
    }

    /** Edmonds–Karp maximum flow: O(V E^2). */
    public static int edmondsKarp(int vertices, List<Edge> edges,
                                  int source, int sink) {
        List<List<ResidualEdge>> graph = buildResidualGraph(vertices, edges, source, sink);
        int flow = 0;
        int[] parentVertex = new int[vertices];
        int[] parentEdge = new int[vertices];

        while (findAugmentingPath(graph, source, sink, parentVertex, parentEdge)) {
            int amount = Integer.MAX_VALUE;
            for (int v = sink; v != source; v = parentVertex[v]) {
                amount = Math.min(amount,
                        graph.get(parentVertex[v]).get(parentEdge[v]).remaining);
            }
            for (int v = sink; v != source; v = parentVertex[v]) {
                ResidualEdge edge = graph.get(parentVertex[v]).get(parentEdge[v]);
                edge.remaining -= amount;
                graph.get(v).get(edge.reverseIndex).remaining = Math.addExact(
                        graph.get(v).get(edge.reverseIndex).remaining, amount);
            }
            flow = Math.addExact(flow, amount);
        }
        return flow;
    }

    /** Dinic maximum flow: blocking flows are sent through the level graph. */
    public static int dinic(int vertices, List<Edge> edges, int source, int sink) {
        List<List<ResidualEdge>> graph = buildResidualGraph(vertices, edges, source, sink);
        int[] level = new int[vertices];
        int flow = 0;
        while (buildLevels(graph, source, sink, level)) {
            int[] nextEdge = new int[vertices];
            int pushed;
            while ((pushed = sendBlockingFlow(graph, source, sink,
                    Integer.MAX_VALUE, level, nextEdge)) > 0) {
                flow = Math.addExact(flow, pushed);
            }
        }
        return flow;
    }

    private static List<List<ResidualEdge>> buildResidualGraph(
            int vertices, List<Edge> edges, int source, int sink) {
        if (vertices <= 0 || source < 0 || sink < 0
                || source >= vertices || sink >= vertices || source == sink) {
            throw new IllegalArgumentException("invalid network or source/sink");
        }
        Objects.requireNonNull(edges, "edges cannot be null");
        List<List<ResidualEdge>> graph = new ArrayList<>(vertices);
        for (int i = 0; i < vertices; i++) {
            graph.add(new ArrayList<>());
        }
        for (Edge edge : edges) {
            if (edge == null || edge.from < 0 || edge.from >= vertices
                    || edge.to < 0 || edge.to >= vertices) {
                throw new IllegalArgumentException("edge endpoint outside network");
            }
            int forwardIndex = graph.get(edge.from).size();
            int reverseIndex = graph.get(edge.to).size();
            if (edge.from == edge.to) {
                reverseIndex++;
            }
            graph.get(edge.from).add(new ResidualEdge(edge.to, reverseIndex, edge.capacity));
            graph.get(edge.to).add(new ResidualEdge(edge.from, forwardIndex, 0));
        }
        return graph;
    }

    private static boolean findAugmentingPath(List<List<ResidualEdge>> graph,
                                               int source, int sink,
                                               int[] parentVertex, int[] parentEdge) {
        Arrays.fill(parentVertex, -1);
        parentVertex[source] = source;
        ArrayDeque<Integer> queue = new ArrayDeque<>();
        queue.add(source);
        while (!queue.isEmpty() && parentVertex[sink] < 0) {
            int vertex = queue.remove();
            for (int i = 0; i < graph.get(vertex).size(); i++) {
                ResidualEdge edge = graph.get(vertex).get(i);
                if (edge.remaining > 0 && parentVertex[edge.to] < 0) {
                    parentVertex[edge.to] = vertex;
                    parentEdge[edge.to] = i;
                    queue.add(edge.to);
                }
            }
        }
        return parentVertex[sink] >= 0;
    }

    private static boolean buildLevels(List<List<ResidualEdge>> graph,
                                       int source, int sink, int[] level) {
        Arrays.fill(level, -1);
        level[source] = 0;
        ArrayDeque<Integer> queue = new ArrayDeque<>();
        queue.add(source);
        while (!queue.isEmpty()) {
            int vertex = queue.remove();
            for (ResidualEdge edge : graph.get(vertex)) {
                if (edge.remaining > 0 && level[edge.to] < 0) {
                    level[edge.to] = level[vertex] + 1;
                    queue.add(edge.to);
                }
            }
        }
        return level[sink] >= 0;
    }

    private static int sendBlockingFlow(List<List<ResidualEdge>> graph,
                                        int vertex, int sink, int amount,
                                        int[] level, int[] nextEdge) {
        if (vertex == sink) {
            return amount;
        }
        List<ResidualEdge> outgoing = graph.get(vertex);
        while (nextEdge[vertex] < outgoing.size()) {
            ResidualEdge edge = outgoing.get(nextEdge[vertex]);
            if (edge.remaining > 0 && level[edge.to] == level[vertex] + 1) {
                int pushed = sendBlockingFlow(graph, edge.to, sink,
                        Math.min(amount, edge.remaining), level, nextEdge);
                if (pushed > 0) {
                    edge.remaining -= pushed;
                    ResidualEdge reverse = graph.get(edge.to).get(edge.reverseIndex);
                    reverse.remaining = Math.addExact(reverse.remaining, pushed);
                    return pushed;
                }
            }
            nextEdge[vertex]++;
        }
        return 0;
    }

    /** Maximum bipartite reviewer-to-paper matching and a Konig minimum cover. */
    public static final class Matching {
        public final Map<Integer, Integer> reviewerToPaper;
        public final Set<Integer> reviewerCover;
        public final Set<Integer> paperCover;

        private Matching(Map<Integer, Integer> assignments,
                         Set<Integer> reviewerCover, Set<Integer> paperCover) {
            this.reviewerToPaper = Collections.unmodifiableMap(
                    new LinkedHashMap<>(assignments));
            this.reviewerCover = Collections.unmodifiableSet(
                    new LinkedHashSet<>(reviewerCover));
            this.paperCover = Collections.unmodifiableSet(
                    new LinkedHashSet<>(paperCover));
        }

        public int size() {
            return reviewerToPaper.size();
        }
    }

    /**
     * Computes a maximum reviewer-paper matching. Each eligible edge is
     * {@code {reviewerIndex, paperIndex}}; both partitions use zero-based indexes.
     * The returned cover has the same size as the maximum matching by Konig's theorem.
     */
    public static Matching match(int reviewers, int papers, int[][] eligible) {
        if (reviewers < 0 || papers < 0 || eligible == null) {
            throw new IllegalArgumentException("invalid bipartite graph");
        }
        List<List<Integer>> adjacency = new ArrayList<>(reviewers);
        for (int i = 0; i < reviewers; i++) {
            adjacency.add(new ArrayList<>());
        }
        for (int[] edge : eligible) {
            if (edge == null || edge.length != 2 || edge[0] < 0
                    || edge[0] >= reviewers || edge[1] < 0 || edge[1] >= papers) {
                throw new IllegalArgumentException("invalid reviewer-paper edge");
            }
            if (!adjacency.get(edge[0]).contains(edge[1])) {
                adjacency.get(edge[0]).add(edge[1]);
            }
        }

        int[] paperMatch = new int[papers];
        Arrays.fill(paperMatch, -1);
        for (int reviewer = 0; reviewer < reviewers; reviewer++) {
            augment(reviewer, adjacency, paperMatch, new boolean[papers]);
        }

        int[] reviewerMatch = new int[reviewers];
        Arrays.fill(reviewerMatch, -1);
        Map<Integer, Integer> assignments = new LinkedHashMap<>();
        for (int paper = 0; paper < papers; paper++) {
            if (paperMatch[paper] >= 0) {
                reviewerMatch[paperMatch[paper]] = paper;
                assignments.put(paperMatch[paper], paper);
            }
        }

        boolean[] reachableReviewer = new boolean[reviewers];
        boolean[] reachablePaper = new boolean[papers];
        ArrayDeque<Integer> queue = new ArrayDeque<>();
        for (int reviewer = 0; reviewer < reviewers; reviewer++) {
            if (reviewerMatch[reviewer] < 0) {
                reachableReviewer[reviewer] = true;
                queue.add(reviewer);
            }
        }
        while (!queue.isEmpty()) {
            int reviewer = queue.remove();
            for (int paper : adjacency.get(reviewer)) {
                if (reviewerMatch[reviewer] == paper || reachablePaper[paper]) {
                    continue;
                }
                reachablePaper[paper] = true;
                int matchedReviewer = paperMatch[paper];
                if (matchedReviewer >= 0 && !reachableReviewer[matchedReviewer]) {
                    reachableReviewer[matchedReviewer] = true;
                    queue.add(matchedReviewer);
                }
            }
        }
        Set<Integer> reviewerCover = new LinkedHashSet<>();
        Set<Integer> paperCover = new LinkedHashSet<>();
        for (int reviewer = 0; reviewer < reviewers; reviewer++) {
            if (!reachableReviewer[reviewer]) {
                reviewerCover.add(reviewer);
            }
        }
        for (int paper = 0; paper < papers; paper++) {
            if (reachablePaper[paper]) {
                paperCover.add(paper);
            }
        }
        return new Matching(assignments, reviewerCover, paperCover);
    }

    private static boolean augment(int reviewer, List<List<Integer>> adjacency,
                                   int[] paperMatch, boolean[] seen) {
        for (int paper : adjacency.get(reviewer)) {
            if (!seen[paper]) {
                seen[paper] = true;
                if (paperMatch[paper] < 0
                        || augment(paperMatch[paper], adjacency, paperMatch, seen)) {
                    paperMatch[paper] = reviewer;
                    return true;
                }
            }
        }
        return false;
    }
}
