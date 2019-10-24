package sample.Parser;

import Jama.Matrix;
import org.apache.commons.math3.geometry.euclidean.twod.Vector2D;
import sample.Parser.ComplexData.EdgesData;
import sample.Parser.ComplexData.VerticesData;
import sample.Parser.Exceptions.EqualsNamesException;
import sample.Parser.SimpleData.*;

import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

public class OutputFileSaver {
    // adjacency
    public void saveAsAdjacency(String filename, Matrix adjacencyMatrix,
                                VerticesData verticesData, Resolution resolution)
            throws IOException
    {
        String saveString = "Matrix{" + toString(adjacencyMatrix) + "};\n" +
                "Vertices{" + toString(verticesData) + "};\n" +
                "Resolution{" + toString(resolution) + "};\n";

        writeInFile(filename, saveString);
    }

    // incident
    public void saveAsIncident(String filename, GraphData data)
            throws IOException, EqualsNamesException
    {
        if (!data.getVerticesData().isNamesUnique())
            throw new EqualsNamesException("Found equals names.");

        Matrix incidentMatrix = makeIncidentMatrix(data);

        String saveString = "Matrix{" + toString(incidentMatrix) + "};\n" +
                "Edges{" + toStringValues(data.getEdgesData()) + "};\n" +
                "Vertices{" + toString(data.getVerticesData()) + "};\n" +
                "Resolution{" + toString(data.getResolution()) + "};\n";

        writeInFile(filename, saveString);
    }

    private Matrix makeIncidentMatrix(GraphData data) {
        int rowCount = data.getVerticesData().count(),
                colCount = data.getEdgesData().getEdges().size();

        Matrix matrix = new Matrix(rowCount, colCount);
        for (int col = 0; col < colCount; ++col) {
            EdgeData edgeData = data.getEdgesData().get(col);
            if (edgeData.isBinary()) {
                int indexFirstVertex = data.getVerticesData().getIndex(edgeData.getVertexName1());
                int indexSecondVertex = data.getVerticesData().getIndex(edgeData.getVertexName2());
                double weight1 = edgeData.getWeight(), weight2 = edgeData.getWeight();
                if (edgeData.getDirection() == -1)
                    weight2 = -weight2;
                else if (edgeData.getDirection() == 1)
                    weight1 = -weight1;

                matrix.set(indexFirstVertex, col, weight1);
                matrix.set(indexSecondVertex, col, weight2);
            }
            else {
                int indexVertex = data.getVerticesData().getIndex(edgeData.getVertexName1());
                matrix.set(indexVertex, col, edgeData.getWeight());
            }
        }
        return matrix;
    }

    // edges
    public void saveAsEdges(String filename, GraphData data) throws IOException {
        String saveString = "Edges{" + toString(data.getEdgesData()) + "};\n" +
                "Vertices{" + toString(data.getVerticesData()) + "};\n" +
                "Resolution{" + toString(data.getResolution()) + "};\n";

        writeInFile(filename, saveString);
    }

    //
    private void writeInFile(String filename, String content) throws IOException {
        FileWriter writer = new FileWriter(filename);
        writer.write(content);
        writer.close();
    }

    // toString
    private StringBuilder toString(Matrix matrix) {
        StringBuilder builder = new StringBuilder();
        for (int row = 0; row < matrix.getRowDimension(); ++row) {
            if (row > 0)
                builder.append(", \n\t");
            builder.append('[');
            for (int col = 0; col < matrix.getColumnDimension(); ++col) {
                if (col > 0)
                    builder.append(", ");
                builder.append(matrix.get(row, col));
            }
            builder.append(']');
        }
        return builder;
    }

    private StringBuilder toString(EdgesData data) {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < data.getEdges().size(); ++i) {
            EdgeData edgeData = data.getEdges().get(i);

            if (i > 0)
                builder.append(", \n\t");
            builder.append('(');
            builder.append(edgeData.getWeight());
            builder.append(", ");
            builder.append(edgeData.getVertexName1());
            builder.append(", ");
            if (edgeData.isBinary()) {
                builder.append(edgeData.getVertexName2());
                builder.append(", ");
                builder.append(edgeData.getDirection());
                builder.append(", ");
                builder.append(((BinaryEdgeData) edgeData).getAngle());
                builder.append(", ");
                builder.append(((BinaryEdgeData) edgeData).getRadius());
            }
            else {
                Vector2D circlePos = ((UnaryEdgeData) edgeData).getCirclePos();
                builder.append(circlePos.getX());
                builder.append(", ");
                builder.append(circlePos.getY());
            }
            builder.append(')');
        }
        return builder;
    }

    private StringBuilder toStringValues(EdgesData data) {
        StringBuilder builder = new StringBuilder();
        boolean isFirst = true;
        for (EdgeData edgeData : data.getEdges()) {
            if (!isFirst)
                builder.append(", \n\t");
            isFirst = false;

            builder.append('(');
            if (edgeData instanceof BinaryEdgeData)
                builder.append(((BinaryEdgeData) edgeData).getAngle());
            else
                builder.append(((UnaryEdgeData) edgeData).getCirclePos().getX());
            builder.append(", ");
            if (edgeData instanceof BinaryEdgeData)
                builder.append(((BinaryEdgeData) edgeData).getRadius());
            else
                builder.append(((UnaryEdgeData) edgeData).getCirclePos().getY());
            builder.append(')');
        }
        return builder;
    }

    private StringBuilder toString(VerticesData data) {
        StringBuilder builder = new StringBuilder();
        List<VertexData> verticesDataList = data.get();
        for (int i = 0; i < verticesDataList.size(); ++i) {
            if (i > 0)
                builder.append(", \n\t");
            VertexData vertexData = verticesDataList.get(i);
            builder.append(vertexData.getName());
            builder.append('(');
            builder.append(vertexData.getX());
            builder.append(", ");
            builder.append(vertexData.getY());
            builder.append(')');
        }
        return builder;
    }

    private String toString(Resolution resolution) {
        return resolution.getWidth() + ", " + resolution.getHeight();
    }


}
