package sample;

import Jama.Matrix;
import sample.Graph.Elements.Vertex;
import sample.Graph.GraphPath;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static java.lang.Math.min;

public class GraphAlgorithms {
    public static class GraphCharacteristic {
        public double radius, diameter;
        public List<Double> eccentricities = new ArrayList<>();

        public GraphCharacteristic(double radius, double diameter, List<Double> eccentricities) {
            this.radius = radius;
            this.diameter = diameter;
            this.eccentricities = eccentricities;
        }
    }

    //2
    public static GraphPath breadthSearch(Vertex vertexFrom, Vertex vertexTo) {
        Set<Vertex> usedVertices = new HashSet<>();
        Deque<GraphPath> pathDeque = new ArrayDeque<>();
        pathDeque.addLast(new GraphPath(vertexFrom));
        usedVertices.add(vertexFrom);

        while (pathDeque.size() > 0) {
            GraphPath currentPath = pathDeque.removeFirst();
            Vertex pathLastVertex = currentPath.getLastVertex();
//            if (usedVertices.contains(pathLastVertex))
//                continue;
//            usedVertices.add(pathLastVertex);
            if (pathLastVertex == vertexTo) {
                return currentPath;
            }

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

    //4
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
    public static boolean checkIsomorphism(Matrix adjacencyMtx1, Matrix adjacencyMtx2) {

        // TODO
        return false;
    }

    //6
    public static List<List<Integer> > findConnectivityComponents(Matrix adjacencyMatrix) {
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





}
