package sample.Parser;


import Jama.Matrix;
import org.apache.commons.math3.geometry.euclidean.twod.Vector2D;
import sample.Graph.Elements.BinaryEdge;
import sample.Graph.Elements.Edge;
import sample.Graph.Elements.UnaryEdge;
import sample.Graph.Elements.Vertex;
import sample.Graph.GraphGroup;
import sample.Main;

import java.util.*;

public class GraphData {
    private static VerticesData generateDefaultVerticesData(int count, Resolution resolution) {
        double radius = Math.min(resolution.getWidth(), resolution.getHeight()) * 0.4;
        double angle = 2 * Math.PI / count;
        Vector2D center = new Vector2D(resolution.getWidth() / 2, resolution.getHeight() / 2);

        VerticesData result = new VerticesData();
        Vector2D baseVector = new Vector2D(radius, 0);
        for (int i = 0; i < count; ++i) {
            Vector2D vertexPos = Main.rotate(baseVector, angle * i).add(center);
            result.add(new VertexData(Integer.toString(i), vertexPos.getX(), vertexPos.getY()));
        }
        return result;
    }

    private static VerticesData generateVerticesDataBy(Set<String> names, Resolution resolution) {
        double radius = Math.min(resolution.getWidth(), resolution.getHeight()) * 0.4;
        double angle = 2 * Math.PI / names.size();
        Vector2D center = new Vector2D(resolution.getWidth() / 2, resolution.getHeight() / 2);

        VerticesData result = new VerticesData();
        Vector2D baseVector = new Vector2D(radius, 0);
        int index = 0;
        for (String name : names) {
            Vector2D vertexPos = Main.rotate(baseVector, angle * index++).add(center);
            result.add(new VertexData(name, vertexPos.getX(), vertexPos.getY()));
        }
        return result;
    }

    private static void validVerticesData(VerticesData data) throws Exception {
        if (data != null) {
            if (!data.isNamesUnique())
                throw new Exception("Found repetitive names of vertices.");
            if (!data.isNamesValid())
                throw new Exception("Found invalid name of vertex.");
        }
    }

    private static void validResolution(Resolution resolution) throws Exception {
        if (resolution != null) {
            if (resolution.getWidth() < GraphGroup.minWidth)
                throw new Exception("Minimal width is " + GraphGroup.minWidth + ".");
            if (resolution.getHeight() < GraphGroup.minHeight)
                throw new Exception("Minimal height is " + GraphGroup.minHeight + ".");
        }
    }

    // adjacency
    private static void validAdjacencyMatrix(Matrix matrix) throws Exception {
        if (matrix == null)
            throw new Exception("Matrix definition not found.");
        if (matrix.getRowDimension() != matrix.getColumnDimension())
            throw new Exception("Matrix is not square.");
    }

    public static GraphData makeByAdjacency(Matrix adjacencyMatrix, VerticesData verticesData,
                                            Resolution resolution)
            throws Exception
    {
        validAdjacencyMatrix(adjacencyMatrix);
        validVerticesData(verticesData);
        validResolution(resolution);
        if (adjacencyMatrix.getRowDimension() != verticesData.count())
            throw new Exception("Adjacency matrix dimension does not equals number of vertices.");

        GraphData result = new GraphData();
        // нижний треугольник
        for (int row = 0; row < adjacencyMatrix.getRowDimension(); ++row) {
            for (int col = 0; col < row; ++col) {
                double val1 = adjacencyMatrix.get(row, col),
                       val2 = adjacencyMatrix.get(col, row);

                if (val1 == val2 && val1 != 0) {
                    result.add(new BinaryEdgeData(verticesData.getName(row),
                            verticesData.getName(col), val1, 0));
                }
                else {
                    if (val1 != 0) {
                        result.add(new BinaryEdgeData(verticesData.getName(row),
                                verticesData.getName(col), val1, 1,
                                0.15, 0.5));
                    }
                    if (val2 != 0) {
                        result.add(new BinaryEdgeData(verticesData.getName(row),
                                verticesData.getName(col), val2, -1,
                                -0.15, 0.5));
                    }
                }

            }
        }
        // диагональ
        for (int i = 0; i < adjacencyMatrix.getRowDimension(); ++i) {
            double val = adjacencyMatrix.get(i, i);
            if (val != 0) {
                result.add(new UnaryEdgeData(verticesData.getName(i), val));
            }
        }

        result.setVerticesData(verticesData);

        if (resolution != null) {
            result.setResolution(resolution);
        }

        return result;
    }

    public static GraphData makeByAdjacency(Matrix adjacencyMatrix, Resolution resolution)
            throws Exception
    {
        return makeByAdjacency(adjacencyMatrix,
                generateDefaultVerticesData(adjacencyMatrix.getRowDimension(), resolution),
                resolution);
    }

    // incident
    private static void validIncidentMatrix(Matrix matrix) throws Exception {
        if (matrix == null)
            throw new Exception("Matrix definition not found.");
    }

    public static GraphData makeByIncident(Matrix incidentMatrix, VerticesData verticesData,
                                           Resolution resolution) throws Exception
    {
        validIncidentMatrix(incidentMatrix);
        validVerticesData(verticesData);
        validResolution(resolution);
        if (incidentMatrix.getRowDimension() != verticesData.count())
            throw new Exception("Row count in incident matrix does not match to vertices count.");

        GraphData result = new GraphData();
        for (int col = 0; col < incidentMatrix.getColumnDimension(); ++col) {
            List<Integer> indexes = new ArrayList<>();
            List<Double> values = new ArrayList<>();
            for (int row = 0; row < incidentMatrix.getRowDimension(); ++row) {
                double value = incidentMatrix.get(row, col);
                if (value != 0) {
                    indexes.add(row);
                    values.add(value);
                }
            }
            if (indexes.size() == 1) {
                int row = indexes.get(0);
                double value = values.get(0);
                result.add(new UnaryEdgeData(verticesData.getName(row), value));
            }
            else if (indexes.size() == 2) {
                int row1 = indexes.get(0), row2 = indexes.get(1);
                double val1 = values.get(0), val2 = values.get(1);
                if (Math.abs(val1) != Math.abs(val2))
                    throw new Exception("Values in column " + col + " does not match.");
                int direction = 0;
                if (val1 > 0 && val2 < 0) direction = -1;
                else if (val1 < 0 && val2 > 0) direction = 1;

                result.add(new BinaryEdgeData(verticesData.getName(row1),
                        verticesData.getName(row2), Math.abs(val1), direction));
            }
            else {
                throw new Exception("Error in column " + col + " in incident matrix.");
            }
        }

        result.setVerticesData(verticesData);

        if (resolution != null) {
            result.setResolution(resolution);
        }

        return result;
    }

    public static GraphData makeByIncident(Matrix incidentMatrix, Resolution resolution)
            throws Exception
    {
        validIncidentMatrix(incidentMatrix);
        return makeByIncident(incidentMatrix,
                generateDefaultVerticesData(incidentMatrix.getRowDimension(), resolution),
                resolution);
    }

    // edges
    private static void validEdgesAndVerticesData(EdgesData edgesData, VerticesData verticesData)
            throws Exception
    {
        if (edgesData == null)
            throw new Exception("Edges definition not found.");
        if (!edgesData.isVerticesNamesValid())
            throw new Exception("Found invalid name of vertex in Edges definition.");

        validVerticesData(verticesData);

        Set<String> retainedNames = new HashSet<>(edgesData.getVerticesNames());
        retainedNames.retainAll(verticesData.getNames());
        if (retainedNames.size() != edgesData.getVerticesNames().size())
            throw new Exception("Found vertex in Edges definition that does not contains in Vertex definition");
    }

    public static GraphData makeByEdges(EdgesData edgesData, VerticesData verticesData,
                                        Resolution resolution) throws Exception
    {
        validEdgesAndVerticesData(edgesData, verticesData);
        validResolution(resolution);

        GraphData result = new GraphData(verticesData, edgesData);
        if (resolution != null) {
            result.setResolution(resolution);
        }
        return result;
    }

    public static GraphData makeByEdges(EdgesData edgesData, Resolution resolution) throws Exception
    {
        return makeByEdges(edgesData,
                generateVerticesDataBy(edgesData.getVerticesNames(), resolution), resolution);
    }

    public static GraphData makeByGraph(Collection<Vertex> vertices, Collection<Edge> edges,
                                        double width, double height)
    {
        GraphData result = new GraphData();
        for (Vertex vertex : vertices) {
            result.add(new VertexData(vertex.getName(), vertex.getCenterX(), vertex.getCenterY()));
        }

        for (Edge edge : edges) {
            if (edge instanceof UnaryEdge)
                result.add(EdgeData.makeBy((UnaryEdge) edge));
            else if (edge instanceof BinaryEdge)
                result.add(EdgeData.makeBy((BinaryEdge) edge));
        }

        result.setResolution(new Resolution(width, height));

        return result;
    }

    //-----------|
    //   class   |
    //-----------|
    private VerticesData verticesData = new VerticesData();
    private EdgesData edgesData = new EdgesData();
    private Resolution resolution = new Resolution();
    private GraphData() {}

    private GraphData(VerticesData verticesData, EdgesData edgesData) {
        this.verticesData = verticesData;
        this.edgesData = edgesData;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("Vertices:\n");
        for (VertexData data : verticesData.get()) {
            builder.append("   ");
            builder.append(data);
            builder.append('\n');
        }
        builder.append("Edges:\n");
        for (EdgeData data : edgesData.getEdges()) {
            builder.append("   ");
            builder.append(data);
            builder.append('\n');
        }
        builder.append(resolution.getWidth());
        builder.append("x");
        builder.append(resolution.getHeight());
        return builder.toString();
    }

    // setters
    private void setVerticesData(VerticesData verticesData) {
        this.verticesData = verticesData;
    }

    private void add(VertexData data) {
        verticesData.add(data);
    }

    private void add(EdgeData data) {
        edgesData.add(data);
    }

    private void setResolution(Resolution resolution) {
        this.resolution = resolution;
    }

    // getters
    public EdgesData getEdgesData() {
        return edgesData;
    }

    public VerticesData getVerticesData() {
        return verticesData;
    }

    public Resolution getResolution() {
        return resolution;
    }
}
