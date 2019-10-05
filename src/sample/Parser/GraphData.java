package sample.Parser;


import Jama.Matrix;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class GraphData {
    private static VerticesData generateDefaultVerticesData(int count) {
        VerticesData result = new VerticesData();
        for (int i = 0; i < count; ++i) {
            result.add(new VertexData(Integer.toString(i), -1, -1));
        }
        return result;
    }

    private static VerticesData generateVerticesDataBy(Set<String> names) {
        VerticesData result = new VerticesData();
        for (String name : names)
            result.add(new VertexData(name));
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

    // adjacency
    private static void validAdjacencyMatrix(Matrix matrix) throws Exception {
        if (matrix == null)
            throw new Exception("Matrix definition not found.");
        if (matrix.getRowDimension() != matrix.getColumnDimension())
            throw new Exception("Matrix is not square.");
    }

    public static GraphData makeByAdjacency(Matrix adjacencyMatrix, VerticesData verticesData,
                                            List<Double> resolution)
            throws Exception
    {
        validAdjacencyMatrix(adjacencyMatrix);
        validVerticesData(verticesData);
        if (adjacencyMatrix.getRowDimension() != verticesData.count())
            throw new Exception("Adjacency matrix dimension does not equals number of vertices.");

        GraphData result = new GraphData();
        // нижний треугольник
        for (int row = 0; row < adjacencyMatrix.getRowDimension(); ++row) {
            for (int col = 0; col < row; ++col) {
                double val1 = adjacencyMatrix.get(row, col),
                       val2 = adjacencyMatrix.get(col, row);

                if (val1 == val2 && val1 != 0) {
                    result.add(new EdgeData(verticesData.getName(row),
                            verticesData.getName(col), val1, 0));
                }
                else {
                    if (val1 != 0) {
                        result.add(new EdgeData(verticesData.getName(row),
                                verticesData.getName(col), val1, 1));
                    }
                    if (val2 != 0) {
                        result.add(new EdgeData(verticesData.getName(row),
                                verticesData.getName(col), val2, -1));
                    }
                }

            }
        }
        // диагональ
        for (int i = 0; i < adjacencyMatrix.getRowDimension(); ++i) {
            double val = adjacencyMatrix.get(i, i);
            if (val != 0) {
                result.add(new EdgeData(verticesData.getName(i), val));
            }
        }

        result.setVerticesData(verticesData);

        if (resolution != null) {
            result.setResolution(resolution.get(0), resolution.get(1));
        }

        return result;
    }

    public static GraphData makeByAdjacency(Matrix adjacencyMatrix, List<Double> resolution)
            throws Exception
    {
        return makeByAdjacency(adjacencyMatrix,
                generateDefaultVerticesData(adjacencyMatrix.getRowDimension()), resolution);
    }

    // incident
    private static void validIncidentMatrix(Matrix matrix) throws Exception {
        if (matrix == null)
            throw new Exception("Matrix definition not found.");
    }

    public static GraphData makeByIncident(Matrix incidentMatrix, VerticesData verticesData,
                                           List<Double> resolution) throws Exception
    {
        validIncidentMatrix(incidentMatrix);
        validVerticesData(verticesData);
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
                result.add(new EdgeData(verticesData.getName(row), value));
            }
            else if (indexes.size() == 2) {
                int row1 = indexes.get(0), row2 = indexes.get(1);
                double val1 = values.get(0), val2 = values.get(1);
                if (Math.abs(val1) != Math.abs(val2))
                    throw new Exception("Values in column " + col + " does not match.");
                int direction = 0;
                if (val1 > 0 && val2 < 0)
                    direction = -1;
                else if (val1 < 0 && val2 > 0)
                    direction = 1;
                result.add(new EdgeData(verticesData.getName(row1),
                        verticesData.getName(row2), Math.abs(val1), direction));
            }
            else {
                throw new Exception("Error in column " + col + " in incident matrix.");
            }
        }

        result.setVerticesData(verticesData);

        if (resolution != null) {
            result.setResolution(resolution.get(0), resolution.get(1));
        }

        return result;
    }

    public static GraphData makeByIncident(Matrix incidentMatrix, List<Double> resolution)
            throws Exception
    {
        validIncidentMatrix(incidentMatrix);
        return makeByIncident(incidentMatrix,
                generateDefaultVerticesData(incidentMatrix.getRowDimension()), resolution);
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
                                        List<Double> resolution) throws Exception
    {
        validEdgesAndVerticesData(edgesData, verticesData);

        GraphData result = new GraphData(verticesData, edgesData);
        if (resolution != null) {
            result.setResolution(resolution.get(0), resolution.get(1));
        }
        return result;
    }

    public static GraphData makeByEdges(EdgesData edgesData, List<Double> resolution) throws Exception
    {
        return makeByEdges(edgesData, generateVerticesDataBy(edgesData.getVerticesNames()), resolution);
    }

    private VerticesData verticesData = new VerticesData();
    private EdgesData edgesData = new EdgesData();
    private double width = -1, height = -1;

    private GraphData() {}

    private GraphData(VerticesData verticesData, EdgesData edgesData) {
        this.verticesData = verticesData;
        this.edgesData = edgesData;
    }

    private GraphData(EdgesData edgesData) {
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
        builder.append(width);
        builder.append("x");
        builder.append(height);
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

    private void setResolution(double width, double height) {
        this.width = width;
        this.height = height;
    }

    // getters
    public EdgesData getEdgesData() {
        return edgesData;
    }

    public VerticesData getVerticesData() {
        return verticesData;
    }

    public double getWidth() {
        return width;
    }

    public double getHeight() {
        return height;
    }
}
