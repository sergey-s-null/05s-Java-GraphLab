package sample.Parser;

import org.apache.commons.math3.geometry.euclidean.twod.Vector2D;
import sample.Graph.Elements.BinaryEdge;
import sample.Graph.Elements.Edge;
import sample.Graph.Elements.UnaryEdge;
import sample.Graph.Elements.Vertex;
import sample.Graph.GraphGroup;

import java.util.Map;

abstract public class EdgeData {
    public static EdgeData makeBy(UnaryEdge edge) {
        Vector2D circlePos = edge.getCirclePosRelativeVertex();
        return new UnaryEdgeData(edge.getVertex().getName(), edge.getWeight(),
                circlePos.getX(), circlePos.getY());
    }

    public static EdgeData makeBy(BinaryEdge edge) {
        String firstName = edge.getFirstVertex().getName(),
               secondName = edge.getSecondVertex().getName();
        int direction = 0;
        switch (edge.getDirection()) {
            case FirstVertex: direction = -1; break;
            case Both: direction = 0; break;
            case SecondVertex: direction = 1; break;
        }
        return new BinaryEdgeData(firstName, secondName, edge.getWeight(), direction,
                edge.getPointAngle(), edge.getPointRadiusCoef());
    }

    protected double weight;

    abstract public boolean isBinary();

    abstract public String getVertexName1();

    abstract public String getVertexName2();

    public double getWeight() {
        return weight;
    }

    abstract public int getDirection();

    abstract public Edge create(GraphGroup graphGroup, Map<String, Vertex> nameToVertex);
}
