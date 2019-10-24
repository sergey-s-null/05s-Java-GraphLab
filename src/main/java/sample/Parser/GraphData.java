package sample.Parser;


import Jama.Matrix;
import sample.Graph.Elements.BinaryEdge;
import sample.Graph.Elements.Edge;
import sample.Graph.Elements.UnaryEdge;
import sample.Graph.Elements.Vertex;
import sample.Parser.ComplexData.EdgesData;
import sample.Parser.ComplexData.VerticesData;
import sample.Parser.SimpleData.*;

import java.util.*;

public class GraphData {
    // adjacency
    static GraphData makeByAdjacency(Matrix adjacencyMatrix, VerticesData verticesData,
                                            Resolution resolution)
    {
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

        result.setResolution(resolution);

        return result;
    }

    // incident
    static GraphData makeByIncident(Matrix incidentMatrix, VerticesData verticesData,
                                           Resolution resolution) throws Exception
    {
        return makeByIncident(incidentMatrix, null, verticesData, resolution);
    }

    static GraphData makeByIncident(Matrix incidentMatrix, List<List<Double> > edgesData,
                                    VerticesData verticesData,Resolution resolution) throws Exception
    {
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
                if (edgesData == null)
                    result.add(new UnaryEdgeData(verticesData.getName(row), value));
                else
                    result.add(new UnaryEdgeData(verticesData.getName(row), value,
                            edgesData.get(col).get(0), edgesData.get(col).get(1)));
            }
            else if (indexes.size() == 2) {
                int row1 = indexes.get(0), row2 = indexes.get(1);
                double val1 = values.get(0), val2 = values.get(1);
                if (Math.abs(val1) != Math.abs(val2))
                    throw new Exception("Values in column " + col + " does not match.");
                int direction = 0;
                if (val1 > 0 && val2 < 0) direction = -1;
                else if (val1 < 0 && val2 > 0) direction = 1;

                if (edgesData == null)
                    result.add(new BinaryEdgeData(verticesData.getName(row1),
                            verticesData.getName(row2), Math.abs(val1), direction));
                else
                    result.add(new BinaryEdgeData(verticesData.getName(row1),
                            verticesData.getName(row2), Math.abs(val1), direction,
                            edgesData.get(col).get(0), edgesData.get(col).get(1)));
            }
            else {
                throw new Exception("Error in column " + col + " in incident matrix.");
            }
        }

        result.setVerticesData(verticesData);

        result.setResolution(resolution);

        return result;
    }

    // edges
    static GraphData makeByEdges(EdgesData edgesData, VerticesData verticesData,
                                 Resolution resolution) throws Exception
    {
        GraphData result = new GraphData(verticesData, edgesData);
        result.setResolution(resolution);
        return result;
    }

    // output collection
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
