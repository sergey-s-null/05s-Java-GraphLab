package sample.Parser;

import org.apache.commons.math3.geometry.euclidean.twod.Vector2D;
import sample.Graph.Elements.Edge;
import sample.Graph.Elements.UnaryEdge;
import sample.Graph.Elements.Vertex;
import sample.Graph.GraphGroup;

import java.util.Map;

public class UnaryEdgeData extends EdgeData {
    private String vertexName;
    private Vector2D circlePos;

    public UnaryEdgeData(String vertexName, double weight, double xCircle, double yCircle) {
        this.vertexName = vertexName;
        this.weight = weight;
        circlePos = new Vector2D(xCircle, yCircle);
    }

    public UnaryEdgeData(String vertexName, double weight) {
        this(vertexName, weight, UnaryEdge.defaultCirclePos.getX(),
                UnaryEdge.defaultCirclePos.getY());
    }

    @Override
    public String toString() {
        return "Unary(" +
                weight + ", " +
                vertexName + ", " +
                circlePos.getX() + ", " +
                circlePos.getY() + ')';
    }

    @Override
    public boolean isBinary() {
        return false;
    }

    @Override
    public String getVertexName1() {
        return vertexName;
    }

    @Override
    public String getVertexName2() {
        return vertexName;
    }

    @Override
    public int getDirection() {
        return 0;
    }

    @Override
    public Edge create(GraphGroup graphGroup, Map<String, Vertex> nameToVertex) {
        return new UnaryEdge(graphGroup, nameToVertex.get(vertexName), circlePos);
    }
}
