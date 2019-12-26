package sample;

import Jama.Matrix;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import javafx.scene.paint.Color;
import sample.Graph.Elements.BinaryEdge;
import sample.Graph.Elements.Edge;
import sample.Graph.Elements.Vertex;
import sample.Graph.GraphGroup;
import sample.Graph.GraphPath;
import sample.Graph.SpiderPath;
import sample.Graph.VerticesPair;

import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static java.lang.Math.min;

public class GraphAlgorithms {
    // Spider and bugs
    public static Optional<SpiderPath> niceSpiderPath(Vertex spiderVertex, Collection<Vertex> bugsVertices,
                                            Collection<Edge> edges, double maxLength)
    {
        Map<Vertex, SpiderPath> spiderToBugs = new HashMap<>();
        for (Vertex bugVertex : bugsVertices) {
            GraphPath path = AStarSearch(spiderVertex, bugVertex, edges).orElse(null);
            if (path != null) spiderToBugs.put(bugVertex, SpiderPath.of(path));
        }

        Map<Vertex, Map<Vertex, SpiderPath> > betweenBugs = new HashMap<>();
        for (Vertex bug1 : bugsVertices) {
            for (Vertex bug2 : bugsVertices) {
                if (bug1 == bug2) continue;
                GraphPath path = AStarSearch(bug1, bug2, edges).orElse(null);
                if (path != null)
                    betweenBugs.computeIfAbsent(bug1, vertex -> new HashMap<>()).put(bug2, SpiderPath.of(path));
            }
        }

        List<SpiderPath> bestPaths = new ArrayList<>();
        for (SpiderPath toFirstBug : spiderToBugs.values()) {
            SpiderPath newPath = niceRecursion(toFirstBug, betweenBugs, maxLength);
            if (newPath != null) bestPaths.add(newPath);
        }

        return Optional.ofNullable(chooseBest(bestPaths));
    }

    private static SpiderPath niceRecursion(SpiderPath path, Map<Vertex, Map<Vertex, SpiderPath> > betweenBugs,
                                      double maxLength)
    {
        if (path.getLength() > maxLength) return null;

        List<SpiderPath> paths = new ArrayList<>();
        paths.add(path);

        Map<Vertex, SpiderPath> nextBugs = betweenBugs.getOrDefault(path.getLastVertex(), new HashMap<>());
        for (Vertex nextBug : nextBugs.keySet()) {
            if (path.containsBug(nextBug)) continue;
            SpiderPath newPath = niceRecursion(SpiderPath.concat(path, nextBugs.get(nextBug)), betweenBugs, maxLength);
            if (newPath != null) paths.add(newPath);
        }

        return chooseBest(paths);
    }

    private static SpiderPath chooseBest(List<SpiderPath> paths) {
        SpiderPath best = null;
        for (SpiderPath path : paths) {
            if (best == null || path.getBugsCount() > best.getBugsCount() ||
                    (path.getBugsCount() == best.getBugsCount() && path.getLength() < best.getLength()))
                best = path;
        }
        return best;
    }

    //2
    private static class IDAStarResult {
        public enum State {
            FOUND, NOT_FOUND, NEW_BOUND
        }

        public static IDAStarResult notFound() {
            IDAStarResult result = new IDAStarResult();
            result.state = State.NOT_FOUND;
            return result;
        }

        public static IDAStarResult found(GraphPath path) {
            IDAStarResult result = new IDAStarResult();
            result.state = State.FOUND;
            result.path = path;
            return result;
        }

        public static IDAStarResult newBound(double bound) {
            IDAStarResult result = new IDAStarResult();
            result.state = State.NEW_BOUND;
            result.newBound = bound;
            return result;
        }


        private State state;
        private GraphPath path;
        private double newBound;

        private IDAStarResult() {}

        public State getState() {
            return state;
        }

        public GraphPath getPath() {
            return path;
        }

        public double getNewBound() {
            return newBound;
        }
    }


    public static GraphPath breadthSearch(Vertex vertexFrom, Vertex vertexTo) {
        Set<Vertex> usedVertices = new HashSet<>();
        Deque<GraphPath> pathDeque = new ArrayDeque<>();
        pathDeque.addLast(new GraphPath(vertexFrom));
        usedVertices.add(vertexFrom);

        while (pathDeque.size() > 0) {
            GraphPath currentPath = pathDeque.removeFirst();
            Vertex pathLastVertex = currentPath.getLastVertex();
            if (pathLastVertex == vertexTo)
                return currentPath;

            List<Vertex.EdgeWithVertex> nextVertices = pathLastVertex.getNextVertices();
            for (Vertex.EdgeWithVertex pair : nextVertices) {
                if (usedVertices.contains(pair.vertex))
                    continue;

                GraphPath newPath = new GraphPath(currentPath);
                newPath.add(pair.edge, pair.vertex);
                pathDeque.addLast(newPath);
                usedVertices.add(pair.vertex);
            }
        }
        return null;
    }

    public static Optional<GraphPath> AStarSearch(Vertex start, Vertex goal, Collection<Edge> edges) {
        double weightToDistanceCoef = calcAverageWeightToDistance(edges);
        Set<Vertex> closed = new HashSet<>();
        SortedSet<GraphPath> pathsQueue = new TreeSet<>((p1, p2) -> {
            double f1 = p1.getLength() + calcHeuristic(p1.getLastVertex(), goal, weightToDistanceCoef);
            double f2 = p2.getLength() + calcHeuristic(p2.getLastVertex(), goal, weightToDistanceCoef);
            return Double.compare(f1, f2);
        });
        pathsQueue.add(new GraphPath(start));

        while (!pathsQueue.isEmpty()) {
            GraphPath currentPath = pathsQueue.first();
            pathsQueue.remove(currentPath);
            Vertex lastVertex = currentPath.getLastVertex();
            if (closed.contains(lastVertex))
                continue;
            if (lastVertex == goal)
                return Optional.of(currentPath);

            closed.add(lastVertex);
            for (Vertex.EdgeWithVertex pair : lastVertex.getNextVertices()) {
                GraphPath newPath = new GraphPath(currentPath);
                newPath.add(pair.edge, pair.vertex);
                pathsQueue.add(newPath);
            }
        }

        return Optional.empty();
    }

    public static Optional<GraphPath> IDAStarSearch(Vertex start, Vertex goal, Collection<Edge> edges) {
        double weightToDistanceCoef = calcAverageWeightToDistance(edges);
        Function<Vertex, Double> heuristic = vertex -> calcHeuristic(vertex, goal, weightToDistanceCoef);
        double bound = heuristic.apply(start);

        while (true) {
            IDAStarResult result = IDAStarRecursion(new GraphPath(start), goal, bound, heuristic);
            switch (result.getState()) {
                case FOUND:
                    return Optional.of(result.getPath());
                case NOT_FOUND:
                    return Optional.empty();
                case NEW_BOUND:
                    bound = result.getNewBound();
            }
        }
    }

    private static IDAStarResult IDAStarRecursion(GraphPath path, Vertex goal, double bound,
                                                  Function<Vertex, Double> heuristic)
    {
        double f = path.getLength() + heuristic.apply(path.getLastVertex());
        if (f > bound) return IDAStarResult.newBound(f);
        if (path.getLastVertex() == goal) return IDAStarResult.found(path);
        double min = Double.POSITIVE_INFINITY;
        for (Vertex.EdgeWithVertex pair : path.getLastVertex().getNextVertices()) {
            if (path.contains(pair.vertex)) continue;

            GraphPath nextPath = new GraphPath(path);
            nextPath.add(pair.edge, pair.vertex);
            IDAStarResult result = IDAStarRecursion(nextPath, goal, bound, heuristic);
            switch (result.getState()) {
                case FOUND:
                case NOT_FOUND:
                    return result;
                case NEW_BOUND:
                    if (result.getNewBound() < min) min = result.getNewBound();
            }
        }
        if (min == Double.POSITIVE_INFINITY)
            return IDAStarResult.notFound();
        return IDAStarResult.newBound(min);
    }

    private static double calcAverageWeightToDistance(Collection<Edge> edges) {
        // среднее отношение веса ребра к расстоянию между вершинами
        double sum = 0;
        for (Edge edge : edges) {
            Vertex v1 = edge.getFirstVertex(), v2 = edge.getSecondVertex();
            double distance = v1.getCenterPos().distance(v2.getCenterPos());
            sum += edge.getWeight() / distance;
        }
        return sum / edges.size();
    }

    private static double calcHeuristic(Vertex v1, Vertex goal, double weightToDistanceCoef) {
        if (v1 == goal)
            return 0;
        else
            return v1.getCenterPos().distance(goal.getCenterPos()) * weightToDistanceCoef;
    }

    //3
    public static Matrix floydAlgorithm(Matrix adjacencyMatrix) {
        Matrix result = adjacencyMatrix.copy();

        for (int row = 0; row < result.getRowDimension(); ++row) {
            for (int col = 0; col < result.getColumnDimension(); ++col) {
                if (row == col)
                    result.set(row, col, 0);
                else if (result.get(row, col) == 0)
                    result.set(row, col, Double.POSITIVE_INFINITY);
            }
        }

        for (int i = 0; i < result.getRowDimension(); ++i) {
            Matrix nextMatrix = result.copy();
            for (int row = 0; row < nextMatrix.getRowDimension(); ++row) {
                for (int col = 0; col < nextMatrix.getColumnDimension(); ++col) {
                    double val1 = result.get(row, col),
                           val2 = result.get(row, i),
                           val3 = result.get(i, col);
                    nextMatrix.set(row, col, min(val1, val2 + val3));
                }
            }
            result = nextMatrix;
        }
        return result;
    }

    public static Map<Vertex, Double> dijkstraAlgorithm(Vertex start, Collection<Vertex> vertices) {
        return dijkstraAlgorithm(start, vertices, (edge, vertex) -> edge.getWeight());
    }

    private static Map<Vertex, Double> dijkstraAlgorithm(Vertex start, Collection<Vertex> vertices,
                                                         BiFunction<Edge, Vertex, Double> weight)
    {
        Map<Vertex, Double> marks = new HashMap<>();
        for (Vertex vertex : vertices) marks.put(vertex, Double.POSITIVE_INFINITY);
        marks.put(start, 0.0);

        Queue<Vertex> unvisitedSorted = new PriorityQueue<>(vertices.size(),
                Comparator.comparingDouble(marks::get));
        unvisitedSorted.addAll(vertices);

        Set<Vertex> visited = new HashSet<>();

        while (!unvisitedSorted.isEmpty()) {
            Vertex vertex = unvisitedSorted.poll();
            visited.add(vertex);

            for (Vertex.EdgeWithVertex pair : vertex.getNextVertices()) {
                if (visited.contains(pair.vertex)) continue;

                double testValue = marks.get(vertex) + weight.apply(pair.edge, pair.vertex);
                double oldValue = marks.get(pair.vertex);
                if (testValue < oldValue) {
                    unvisitedSorted.remove(pair.vertex);
                    marks.put(pair.vertex, testValue);
                    unvisitedSorted.add(pair.vertex);
                }
            }
        }

        return marks;
    }

    public static Optional<Map<Vertex, Double> > bellmanFordAlgorithm(Vertex start, Collection<Vertex> vertices,
                                                                      Collection<Edge> edges)
    {
        return bellmanFordAlgorithm(start, vertices, edges, Edge::getWeight);
    }

    private static Optional<Map<Vertex, Double> > bellmanFordAlgorithm(Vertex start, Collection<Vertex> vertices,
                                                               Collection<Edge> edges, Function<Edge, Double> weight)
    {
        Map<Vertex, Double> distances = new HashMap<>();
        for (Vertex vertex : vertices) distances.put(vertex, Double.POSITIVE_INFINITY);
        distances.put(start, 0.0);

        Map<Vertex, Double> testInfCycles = new HashMap<>();
        for (int i = 0; i < vertices.size(); ++i) {
            if (i == vertices.size() - 1) testInfCycles.putAll(distances);

            for (Edge edge : edges) {
                Vertex v1 = edge.getFirstVertex(), v2 = edge.getSecondVertex();
                switch (edge.getDirection()) {
                    case SecondVertex: case Both:
                        if (distances.get(v2) > distances.get(v1) + weight.apply(edge))
                            distances.put(v2, distances.get(v1) + weight.apply(edge));
                }
                switch (edge.getDirection()) {
                    case FirstVertex: case Both:
                        if (distances.get(v1) > distances.get(v2) + weight.apply(edge))
                            distances.put(v1, distances.get(v2) + weight.apply(edge));
                }
            }
        }

        if (testInfCycles.equals(distances))
            return Optional.of(distances);
        else
            return Optional.empty();
    }

    public static Optional<Map<Vertex, Map<Vertex, Double> > > johnsonAlgorithm(
            GraphGroup graphGroup, Collection<Vertex> vertices, Collection<Edge> edges)
    {
        List<Vertex> vertices_ = new ArrayList<>(vertices);
        List<Edge> edges_ = new ArrayList<>(edges);

        Vertex s = new Vertex(graphGroup, 0, 0);
        s.disconnect();
        for (Vertex vertex : vertices) {
            Edge edge = new BinaryEdge(graphGroup, s, vertex);
            edge.disconnect();
            edges_.add(edge);
        }
        vertices_.add(s);

        Function<Edge, Double> weight = edge -> (edge.getFirstVertex() == s ? 0 : edge.getWeight());


        Map<Vertex, Map<Vertex, Double> > bellmanFordResult = new HashMap<>();
        for (Vertex vertex : vertices_) {
            Map<Vertex, Double> result = bellmanFordAlgorithm(vertex, vertices_, edges_).orElse(null);
            if (result == null) return Optional.empty();
            bellmanFordResult.put(vertex, result);
        }

        Function<Vertex, Double> fi = vertex -> bellmanFordResult.get(s).get(vertex);
        BiFunction<Edge, Vertex, Double> weight_fi = (edge, vertex) -> {
            Vertex anotherVertex = edge.getFirstVertex() == vertex ? edge.getSecondVertex() : edge.getFirstVertex();
            return weight.apply(edge) + fi.apply(anotherVertex) - fi.apply(vertex);
        };

        Map<Vertex, Map<Vertex, Double> > result = new HashMap<>();
        for (Vertex vertex : vertices) {
            Map<Vertex, Double> distances = dijkstraAlgorithm(vertex, vertices, weight_fi);
            for (Vertex vertex1 : vertices) {
                double validDistance = distances.get(vertex1) + fi.apply(vertex1) - fi.apply(vertex);

                Map<Vertex, Double> aga = result.computeIfAbsent(vertex, k -> new HashMap<>());
                aga.put(vertex1, validDistance);
            }
        }

        return Optional.of(result);
    }

    //4
    public static class GraphCharacteristic {
        public double radius, diameter;
        public List<Double> eccentricities;

        public GraphCharacteristic(double radius, double diameter, List<Double> eccentricities) {
            this.radius = radius;
            this.diameter = diameter;
            this.eccentricities = eccentricities;
        }
    }


    public static GraphCharacteristic graphCharacteristic(Matrix adjacencyMatrix) {
        Matrix floydMatrix = floydAlgorithm(adjacencyMatrix);

        List<Double> eccentricities = new ArrayList<>();
        double radius = Double.POSITIVE_INFINITY, diameter = Double.NEGATIVE_INFINITY;
        for (double[] row : floydMatrix.getArray()) {
            double eccentricity = maxInRow(row);
            eccentricities.add(eccentricity);
            if (eccentricity < radius)
                radius = eccentricity;
            if (eccentricity > diameter)
                diameter = eccentricity;
        }

        return new GraphCharacteristic(radius, diameter, eccentricities);
    }

    private static double maxInRow(double[] row) {
        double result = Double.NEGATIVE_INFINITY;
        for (double val : row) {
            if (val > result)
                result = val;
        }
        return result;
    }

    //5
    private static class VertexCharacteristic {
        public Vertex vertex;
        public int unaryEdgesCount;
        public int binaryEdgesCount;

        public VertexCharacteristic(Vertex vertex) {
            this.vertex = vertex;
            unaryEdgesCount = vertex.getUnaryEdges().size();
            binaryEdgesCount = vertex.getBinaryEdges().size();
        }

        @Override
        public String toString() {
            return "Char[" + vertex.getName() + ":u=" + unaryEdgesCount + ",b=" + binaryEdgesCount + "]";
        }
    }

    public static Optional<Map<Vertex, Vertex>> checkIsomorphism(GraphGroup firstGraph, GraphGroup secondGraph) {
        List<VertexCharacteristic> firstCharacteristics = makeCharacteristics(firstGraph);
        List<VertexCharacteristic> secondCharacteristics = makeCharacteristics(secondGraph);
        if (firstCharacteristics.size() != secondCharacteristics.size()) return Optional.empty();

        Matrix B = new Matrix(firstCharacteristics.size(), secondCharacteristics.size(), 1);
        for (int i = 0; i < firstCharacteristics.size(); ++i) {
            for (int j = 0; j < secondCharacteristics.size(); ++j) {
                if (!isAvailableIsomorphism(firstCharacteristics.get(i), secondCharacteristics.get(j)))
                    B.set(i, j, 0);
            }
        }
        for (int i = 0; i < B.getRowDimension(); ++i) {
            for (int j = 0; j < B.getColumnDimension(); ++j) {
                System.out.print(B.get(i, j) + " ");
            }
            System.out.println();
        }

        Map<Integer, Integer> result = new HashMap<>();
        boolean found = isomorphismRecursion(result, 0, B,
                firstGraph.getVertices(), secondGraph.getVertices());
        if (found) {
            return Optional.of(convertResult(result, firstGraph.getVertices(), secondGraph.getVertices()));
        }
        else {
            System.out.println("recursion failed");
            return Optional.empty();
        }
    }

    private static boolean isomorphismRecursion(Map<Integer, Integer> result, int currentIndex, Matrix B,
                                                List<Vertex> vertices1, List<Vertex> vertices2)
    {
        if (result.size() == B.getRowDimension()) return true;

        for (int i = 0; i < B.getColumnDimension(); ++i) {
            if (B.get(currentIndex, i) == 0) continue;
            if (result.containsValue(i)) continue;

            boolean availableIsomorphism = true;
            for (Map.Entry<Integer, Integer> pair : result.entrySet()) {
                int i1Graph1 = pair.getKey(), i2Graph1 = currentIndex;
                int i1Graph2 = pair.getValue(), i2Graph2 = i;
                if (!isAvailableIsomorphism(vertices1.get(i1Graph1), vertices1.get(i2Graph1),
                        vertices2.get(i1Graph2), vertices2.get(i2Graph2))) {
                    availableIsomorphism = false;
                    break;
                }
            }
            if (!availableIsomorphism) continue;

            result.put(currentIndex, i);
            if (isomorphismRecursion(result, currentIndex + 1, B, vertices1, vertices2)) return true;
            result.remove(currentIndex);
        }
        return false;
    }

    private static List<VertexCharacteristic> makeCharacteristics(GraphGroup graphGroup) {
        return graphGroup.getVertices().stream().map(VertexCharacteristic::new).collect(Collectors.toList());
    }

    private static boolean isAvailableIsomorphism(VertexCharacteristic first, VertexCharacteristic second) {
        return first.unaryEdgesCount == second.unaryEdgesCount && first.binaryEdgesCount == second.binaryEdgesCount;
    }

    private static boolean isAvailableIsomorphism(Vertex vertex1Graph1, Vertex vertex2Graph1,
                                                  Vertex vertex1Graph2, Vertex vertex2Graph2)
    {
        Set<BinaryEdge> between1 = Vertex.edgesBetween(vertex1Graph1, vertex2Graph1);
        Set<BinaryEdge> between2 = Vertex.edgesBetween(vertex1Graph2, vertex2Graph2);
        if (between1.size() == 0 && between2.size() == 0) return true;
        if (between1.size() != 1 || between2.size() != 1) return false;
        BinaryEdge firstEdge = between1.iterator().next();
        BinaryEdge secondEdge = between2.iterator().next();

        if (!firstEdge.isOriented() && !secondEdge.isOriented()) {
            return true;
        }
        else if (firstEdge.isOriented() && secondEdge.isOriented()) {
            return (firstEdge.isFromTo(vertex1Graph1, vertex2Graph1) && secondEdge.isFromTo(vertex1Graph2, vertex2Graph2))
                    || (firstEdge.isFromTo(vertex2Graph1, vertex1Graph1) && secondEdge.isFromTo(vertex2Graph2, vertex1Graph2));
        }
        else {
            return false;
        }
    }

    private static Map<Vertex, Vertex> convertResult(Map<Integer, Integer> result,
                                                     List<Vertex> vertices1, List<Vertex> vertices2)
    {
        Map<Vertex, Vertex> newResult = new HashMap<>();
        for (Map.Entry<Integer, Integer> pair : result.entrySet()) {
            newResult.put(vertices1.get(pair.getKey()), vertices2.get(pair.getValue()));
        }
        return newResult;
    }

    //6
    public static List<List<Integer>> findConnectivityComponents(Matrix adjacencyMatrix) {
        Matrix shortestDistMtx = floydAlgorithm(adjacencyMatrix);
        List<List<Integer> > components = new ArrayList<>();
        Set<Integer> unusedIndices = IntStream.range(0, shortestDistMtx.getRowDimension())
                .boxed().collect(Collectors.toSet());

        while (!unusedIndices.isEmpty()) {
            List<Integer> component = makeStrongComponentBy(unusedIndices.iterator().next(), shortestDistMtx);
            for (int i : component) unusedIndices.remove(i);
            components.add(component);
        }

        return components;
    }

    private static List<Integer> makeStrongComponentBy(int i, Matrix shortestDistMtx) {
        List<Integer> component = new ArrayList<>();
        for (int j = 0; j < shortestDistMtx.getColumnDimension(); ++j) {
            if (shortestDistMtx.get(i, j) < Double.POSITIVE_INFINITY &&
                    shortestDistMtx.get(j, i) < Double.POSITIVE_INFINITY)
            {
                component.add(j);
            }
        }
        return component;
    }

    //7
    public static boolean isGraphFull(Matrix adjMatrix) {
        for (int i = 0; i < adjMatrix.getRowDimension(); ++i) {
            for (int j = 0; j < adjMatrix.getColumnDimension(); ++j) {
                if (adjMatrix.get(i, j) == 0) return false;
            }
        }
        return true;
    }

    public static void makeGraphAddition(GraphGroup resultGraph, Matrix oldAdjMatrix) {
        for (int i = 0; i < oldAdjMatrix.getRowDimension(); ++i) {
            for (int j = i; j < oldAdjMatrix.getColumnDimension(); ++j) {
                if (i == j) {
                    double val = oldAdjMatrix.get(i, i);
                    Vertex vertex = resultGraph.getVertices().get(i);
                    if (val == 0)
                        resultGraph.addEdge(vertex, false);
                }
                else {
                    double to2nd = oldAdjMatrix.get(i, j), to1st = oldAdjMatrix.get(j, i);
                    Vertex v1 = resultGraph.getVertices().get(i),
                           v2 = resultGraph.getVertices().get(j);
                    if (to2nd == 0 && to1st == 0)
                        resultGraph.addEdge(v1, v2, Edge.Direction.Both, false);
                    else if (to2nd != 0 && to1st == 0)
                        resultGraph.addEdge(v1, v2, Edge.Direction.FirstVertex, false);
                    else if (to2nd == 0 && to1st != 0)
                        resultGraph.addEdge(v1, v2, Edge.Direction.SecondVertex, false);
                }
            }
        }
    }

    //8
    public static void applyBinaryOperation(GraphGroup resultGraph, Matrix firstAdjMtx, Matrix secondAdjMtx,
                                               BiFunction<Boolean, Boolean, Boolean> op)
    {
        for (int i = 0; i < firstAdjMtx.getRowDimension(); ++i) {
            for (int j = i; j < firstAdjMtx.getColumnDimension(); ++j) {
                if (i == j) {
                    boolean val1 = firstAdjMtx.get(i, i) != 0;
                    boolean val2 = secondAdjMtx.get(i, i) != 0;
                    Vertex vertex = resultGraph.getVertices().get(i);
                    if (op.apply(val1, val2))
                        resultGraph.addEdge(vertex, false);
                }
                else {
                    boolean to2nd_1 = firstAdjMtx.get(i, j) != 0;
                    boolean to2nd_2 = secondAdjMtx.get(i, j) != 0;
                    boolean to2ndResult = op.apply(to2nd_1, to2nd_2);

                    boolean to1st_1 = firstAdjMtx.get(j, i) != 0;
                    boolean to1st_2 = secondAdjMtx.get(j, i) != 0;
                    boolean to1stResult = op.apply(to1st_1, to1st_2);

                    Vertex v1 = resultGraph.getVertices().get(i);
                    Vertex v2 = resultGraph.getVertices().get(j);
                    if (to2ndResult && to1stResult)
                        resultGraph.addEdge(v1, v2, Edge.Direction.Both, false);
                    else if (to2ndResult)
                        resultGraph.addEdge(v1, v2, Edge.Direction.SecondVertex, false);
                    else if (to1stResult)
                        resultGraph.addEdge(v1, v2, Edge.Direction.FirstVertex, false);
                }
            }
        }
    }

    //9
    public static void makeSpanningTreeByPrim(GraphGroup emptyGraph, GraphGroup baseGraph, Vertex root) {
        Map<Vertex, Vertex> verticesMapping = copyVertices(baseGraph, emptyGraph);

        Set<Vertex> used = new HashSet<>();
        used.add(root);
        Set<Vertex> next = new HashSet<>(baseGraph.getVertices());
        next.remove(root);

        Multimap<VerticesPair, BinaryEdge> vertexPairToEdges = HashMultimap.create();
        for (Edge edge : baseGraph.getEdges()) {
            if (edge instanceof BinaryEdge) {
                BinaryEdge binaryEdge = (BinaryEdge) edge;
                vertexPairToEdges.put(VerticesPair.of(binaryEdge.getFirstVertex(),
                        binaryEdge.getSecondVertex()), binaryEdge);
            }
        }

        while (!next.isEmpty()) {
            BinaryEdge bestEdge = null;
            Vertex bestTo = null;
            for (Vertex from : used) {
                for (Vertex to : next) {
                    Collection<BinaryEdge> edges = vertexPairToEdges.get(VerticesPair.of(from, to));
                    for (BinaryEdge edge : edges) {
                        if (bestEdge == null || edge.getWeight() < bestEdge.getWeight()) {
                            bestEdge = edge;
                            bestTo = to;
                        }
                    }
                }
            }

            if (bestEdge == null) throw new RuntimeException("Not found bestEdge.");

            Vertex v1 = verticesMapping.get(bestEdge.getFirstVertex());
            Vertex v2 = verticesMapping.get(bestEdge.getSecondVertex());
            BinaryEdge edge = emptyGraph.addEdge(v1, v2, Edge.Direction.Both, false);
            edge.setWeight(bestEdge.getWeight(), false);

            used.add(bestTo);
            next.remove(bestTo);
        }
    }

    public static void makeSpanningTreeByKraskal(GraphGroup emptyGraph, GraphGroup baseGraph) {
        Map<Vertex, Vertex> verticesMapping = copyVertices(baseGraph, emptyGraph);

        PriorityQueue<BinaryEdge> sortedEdges = new PriorityQueue<>(Comparator.comparingDouble(Edge::getWeight));
        sortedEdges.addAll(baseGraph.getBinaryEdges());

        Map<Vertex, Set<Vertex> > groups = new HashMap<>();

        while (!sortedEdges.isEmpty()) {
            BinaryEdge next = sortedEdges.poll();
            Vertex v1 = next.getFirstVertex(), v2 = next.getSecondVertex();
            Set<Vertex> group1 = groups.get(v1), group2 = groups.get(v2);
            if (group1 == null && group2 == null) {
                Set<Vertex> newGroup = new HashSet<>();
                newGroup.add(v1);
                newGroup.add(v2);
                groups.put(v1, newGroup);
                groups.put(v2, newGroup);
            }
            else if (group1 == null) {
                group2.add(v1);
                groups.put(v1, group2);
            }
            else if (group2 == null) {
                group1.add(v2);
                groups.put(v2, group1);
            }
            else if (group1 != group2) {
                for (Vertex vertex : group2)
                    groups.put(vertex, group1);
                group1.addAll(group2);
            }

            if (group1 == null || group1 != group2) {
                BinaryEdge edge = emptyGraph.addEdge(verticesMapping.get(v1), verticesMapping.get(v2),
                        Edge.Direction.Both, false);
                edge.setWeight(next.getWeight(), false);
            }
        }
    }

    public static void makeSpanningTreeByBoruwka(GraphGroup emptyGraph, GraphGroup baseGraph) {
        Map<Vertex, Vertex> verticesMapping = copyVertices(baseGraph, emptyGraph);

        PriorityQueue<BinaryEdge> sortedEdges = new PriorityQueue<>(Comparator.comparingDouble(Edge::getWeight));
        sortedEdges.addAll(baseGraph.getBinaryEdges());

        Map<Vertex, Set<Vertex> > groups = new HashMap<>();
        for (Vertex vertex : baseGraph.getVertices()) {
            Set<Vertex> group = new HashSet<>();
            group.add(vertex);
            groups.put(vertex, group);
        }

        int addedEdges = 0;
        while (addedEdges < baseGraph.getVerticesCount() - 1 && !sortedEdges.isEmpty()) {
            BinaryEdge next = sortedEdges.poll();
            Vertex v1 = next.getFirstVertex(), v2 = next.getSecondVertex();
            Set<Vertex> group1 = groups.get(v1), group2 = groups.get(v2);
            if (group1 != group2) {
                for (Vertex vertex : group2)
                    groups.put(vertex, group1);
                group1.addAll(group2);

                addedEdges++;
                BinaryEdge edge = emptyGraph.addEdge(verticesMapping.get(v1), verticesMapping.get(v2),
                        Edge.Direction.Both, false);
                edge.setWeight(next.getWeight(), false);
            }
        }
    }

    private static Map<Vertex, Vertex> copyVertices(GraphGroup from, GraphGroup to) {
        Map<Vertex, Vertex> verticesMapping = new HashMap<>();
        for (Vertex vertex : from.getVertices()) {
            Vertex newVertex = to.addVertex(vertex.getCenterX(), vertex.getCenterY(), false);
            newVertex.setName(vertex.getName(), false);
            verticesMapping.put(vertex, newVertex);
        }
        return verticesMapping;
    }

    //13
    public static boolean hasCycle(GraphGroup graphGroup) {
        return hasCycleRecursion(new HashSet<>(), null, graphGroup.getVertices().get(0));
    }

    private static boolean hasCycleRecursion(Set<Vertex> used, Vertex prev, Vertex next) {
        for (Vertex.EdgeWithVertex pair : next.getNextVertices()) {
            if (pair.vertex == prev) continue;
            if (used.contains(pair.vertex)) return true;
            used.add(next);
            if (hasCycleRecursion(used, next, pair.vertex)) return true;
            used.remove(next);
        }
        return false;
    }

    public static Optional<GraphPath> findMinCycle(GraphGroup graphGroup) {
        GraphPath startPath = new GraphPath(graphGroup.getVertices().get(0));
        List<GraphPath> foundCycles = new ArrayList<>();
        for (Vertex.EdgeWithVertex pair : startPath.getLastVertex().getNextVertices())
            minCycleRecursion(startPath, pair).ifPresent(foundCycles::add);
        return selectShortest(foundCycles);
    }

    private static Optional<GraphPath> minCycleRecursion(GraphPath path, Vertex.EdgeWithVertex next) {
        if (path.contains(next.vertex))
            return new GraphPath(path, next.edge, next.vertex).highlightLastCycle();

        List<GraphPath> foundCycles = new ArrayList<>();
        for (Vertex.EdgeWithVertex pair : next.vertex.getNextVertices()) {
            if (path.getLastVertex() == pair.vertex) continue;
            path.add(next.edge, next.vertex);
            minCycleRecursion(path, pair).ifPresent(foundCycles::add);
            path.removeLast();
        }
        return selectShortest(foundCycles);
    }

    private static Optional<GraphPath> selectShortest(Collection<GraphPath> paths) {
        GraphPath result = null;
        for (GraphPath path : paths) {
            if (result == null || path.getLength() < result.getLength())
                result = path;
        }
        return Optional.ofNullable(result);
    }

    public static Optional<List<Integer>> makeCodePrefer(GraphGroup graphGroup) {
        List<Vertex> vertices = graphGroup.getVertices();
        Set<Vertex> removedVertices = new HashSet<>();
        List<Integer> result = new ArrayList<>();
        while (result.size() < vertices.size() - 2) {
            Integer index = firstUnusedHangingIndex(vertices, removedVertices).orElse(null);
            if (index == null) return Optional.empty();
            removedVertices.add(vertices.get(index));

            Integer resultIndex = getNeighborIndex(vertices.get(index), vertices, removedVertices).orElse(null);
            if (resultIndex == null) return Optional.empty();
            result.add(resultIndex);
        }
        return Optional.of(result);
    }

    private static Optional<Integer> firstUnusedHangingIndex(List<Vertex> vertices, Set<Vertex> used) {
        for (int i = 0; i < vertices.size(); ++i) {
            Vertex currentVertex = vertices.get(i);
            if (used.contains(currentVertex)) continue;

            List<BinaryEdge> edges = new ArrayList<>(currentVertex.getBinaryEdges());
            if (edges.size() == 1) {
                return Optional.of(i);
            }
            else if (edges.size() == 2) {
                Vertex first = edges.get(0).getAnother(currentVertex).orElse(null);
                Vertex second = edges.get(1).getAnother(currentVertex).orElse(null);
                if (first != null && second != null) {
                    if (used.contains(first) ^ used.contains(second))
                        return Optional.of(i);
                }
            }
        }
        return Optional.empty();
    }

    private static Optional<Integer> getNeighborIndex(Vertex of, List<Vertex> vertices, Set<Vertex> used) {
        List<BinaryEdge> edges = new ArrayList<>(of.getBinaryEdges());
        if (edges.size() == 1) {
            Vertex another = edges.get(0).getAnother(of).orElse(null);
            if (another == null || used.contains(another)) return Optional.empty();
            int index = vertices.indexOf(another);
            return index >= 0 ? Optional.of(index) : Optional.empty();
        }
        else if (edges.size() == 2) {
            Vertex neighbor1 = edges.get(0).getAnother(of).orElse(null);
            Vertex neighbor2 = edges.get(1).getAnother(of).orElse(null);
            if (neighbor1 == null || neighbor2 == null) return Optional.empty();

            if (used.contains(neighbor1) && !used.contains(neighbor2))
                return Optional.of(vertices.indexOf(neighbor2));
            else if (used.contains(neighbor2) && !used.contains(neighbor1))
                return Optional.of(vertices.indexOf(neighbor1));
            else
                return Optional.empty();
        }
        else {
            return Optional.empty();
        }
    }

    public static List<Integer> findTreeCenters(Matrix adjacencyMatrix) {
        Matrix shortestDistances = floydAlgorithm(adjacencyMatrix);
        List<Double> eccentricities = toEccentricities(shortestDistances);
        return findIndicesOfMinimal(eccentricities);
    }

    private static List<Double> toEccentricities(Matrix shortestDistances) {
        List<Double> result = new ArrayList<>();
        for (int i = 0; i < shortestDistances.getRowDimension(); ++i) {
            double eccentricity = Double.NEGATIVE_INFINITY;
            for (int j = 0; j < shortestDistances.getColumnDimension(); ++j) {
                if (shortestDistances.get(i, j) > eccentricity)
                    eccentricity = shortestDistances.get(i, j);
            }
            result.add(eccentricity);
        }
        return result;
    }

    private static List<Integer> findIndicesOfMinimal(List<Double> values) {
        double min = Collections.min(values);
        List<Integer> result = new ArrayList<>();
        for (int i = 0; i < values.size(); ++i) {
            if (min == values.get(i))
                result.add(i);
        }
        return result;
    }

    //14
    private static class IndicesPair {
        public int i, j;
        public IndicesPair(int i, int j) {
            this.i = i;
            this.j = j;
        }
    }

    public static int colorizeGraph(GraphGroup graphGroup, Matrix adjMatrix) {
        int chromaticNumber = findChromaticNumber(adjMatrix);
        Matrix matrix = makeMtxSymmetric(adjMatrix, (a, b) -> a == 0 ? b : a);

        Map<Integer, Integer> colorizedVertices = new HashMap<>();
        colorizedVertices.put(0, 0);

        boolean res = colorizeRecursion(colorizedVertices, matrix, chromaticNumber);
        if (!res)
            throw new RuntimeException("Что-то не так. (Либо алгоритм вычисления хроматического числа, либо раскраски.)");

        List<Color> colors = generateColors(chromaticNumber);
        for (Map.Entry<Integer, Integer> pair : colorizedVertices.entrySet()) {
            graphGroup.getVertices().get(pair.getKey()).colorize(colors.get(pair.getValue()));
        }

        return chromaticNumber;
    }

    private static int findChromaticNumber(Matrix adjMatrix) {
        Matrix matrix = GraphAlgorithms.makeMtxSymmetric(adjMatrix, (a, b) -> a == 0 ? b : a);

        IndicesPair cell;
        while ((cell = findUnconnectedVertices(matrix).orElse(null)) != null) {
            int row = cell.i, col = cell.j;
            for (int i = 0; i < matrix.getRowDimension(); ++i) {
                if (i == row || i == col) continue;
                double val = matrix.get(i, col);
                if (val != 0) matrix.set(i, row, val);
            }
            matrix = GraphAlgorithms.subMatrix(matrix, col, col);
        }

        return matrix.getRowDimension();
    }

    private static boolean colorizeRecursion(Map<Integer, Integer> colorizedVertices, Matrix symmetricAdjMtx, int colorsCount) {
        if (checkColorCollision(colorizedVertices, symmetricAdjMtx)) return false;
        if (colorizedVertices.size() == symmetricAdjMtx.getRowDimension()) return true;

        // пытаемся раскрасить одну нераскрашенную вершину
        int nextVertex = 0;
        for (int i = 0; i < symmetricAdjMtx.getRowDimension(); ++i) {
            if (!colorizedVertices.containsKey(i)) nextVertex = i;
        }

        Set<Integer> potentialColors = IntStream.range(0, colorsCount).boxed().collect(Collectors.toSet());
        for (Map.Entry<Integer, Integer> pair : colorizedVertices.entrySet()) {
            if (symmetricAdjMtx.get(pair.getKey(), nextVertex) != 0)
                potentialColors.remove(pair.getValue());
        }

        if (potentialColors.size() == 0) return false;

        for (int color : potentialColors) {
            colorizedVertices.put(nextVertex, color);
            if (colorizeRecursion(colorizedVertices, symmetricAdjMtx, colorsCount)) return true;
            colorizedVertices.remove(nextVertex);
        }
        return false;
    }

    private static boolean checkColorCollision(Map<Integer, Integer> colorizedVertices, Matrix symmetricAdjMtx) {
        for (Map.Entry<Integer, Integer> pair1 : colorizedVertices.entrySet()) {
            for (Map.Entry<Integer, Integer> pair2 : colorizedVertices.entrySet()) {
                if (pair1 == pair2) continue;
                if (symmetricAdjMtx.get(pair1.getKey(), pair2.getKey()) != 0 &&
                        pair1.getValue().equals(pair2.getValue()))
                    return true;
            }
        }
        return false;
    }

    private static Optional<IndicesPair> findUnconnectedVertices(Matrix matrix) {
        for (int i = 0; i < matrix.getRowDimension(); ++i) {
            for (int j = i + 1; j < matrix.getColumnDimension(); ++j) {
                if (matrix.get(i, j) == 0) return Optional.of(new IndicesPair(i, j));
            }
        }
        return Optional.empty();
    }

    private static List<Color> generateColors(int colorsCount) {
        List<Color> result = new ArrayList<>();
        int groupsCount = (colorsCount - 1) / 6 + 1;
        int step = 128 / groupsCount;
        int shift = 0;
        for (int i = 0; i < groupsCount; ++i) {
            result.add(Color.rgb(255 - shift, 0, 0));
            result.add(Color.rgb(255 - shift, 0, 255 - shift));
            result.add(Color.rgb(0, 255 - shift, 0));
            result.add(Color.rgb(0, 255 - shift, 255 - shift));
            result.add(Color.rgb(255 - shift, 255 - shift, 0));
            result.add(Color.rgb(0, 0, 255 - shift));
            shift += step;
        }
        while (result.size() > colorsCount)
            result.remove(result.size() - 1);
        return result;
    }


    // common
    public static Matrix makeMtxSymmetric(Matrix squareMatrix, BiFunction<Double, Double, Double> converter) {
        Matrix result = squareMatrix.copy();
        for (int i = 0; i < squareMatrix.getRowDimension(); ++i) {
            for (int j = i + 1; j < squareMatrix.getColumnDimension(); ++j) {
                double resVal = converter.apply(squareMatrix.get(i, j), squareMatrix.get(j, i));
                result.set(i, j, resVal);
                result.set(j, i, resVal);
            }
        }
        return result;
    }

    public static Matrix subMatrix(Matrix matrix, int removeRow, int removeCol) {
        Matrix result = new Matrix(matrix.getRowDimension() - 1, matrix.getColumnDimension() - 1);
        for (int i = 0; i < result.getRowDimension(); ++i) {
            for (int j = 0; j < result.getColumnDimension(); ++j) {
                int row = i >= removeRow ? i + 1 : i;
                int col = j >= removeCol ? j + 1 : j;
                result.set(i, j, matrix.get(row, col));
            }
        }
        return result;
    }


}
