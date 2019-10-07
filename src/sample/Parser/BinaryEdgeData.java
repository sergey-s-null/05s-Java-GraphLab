package sample.Parser;

import sample.Graph.Elements.BinaryEdge;
import sample.Graph.Elements.Edge;
import sample.Graph.Elements.Vertex;
import sample.Graph.GraphGroup;

import java.util.Map;

public class BinaryEdgeData extends EdgeData {
    private String vertexName1, vertexName2;
    private int direction;
    private double angle, radius;

    public BinaryEdgeData(String vertexName1, String vertexName2, double weight, int direction,
                          double angle, double radius)
    {
        this.vertexName1 = vertexName1;
        this.vertexName2 = vertexName2;
        this.weight = weight;

        if (direction > 1) direction = 1;
        else if (direction < -1) direction = -1;
        this.direction = direction;

        this.angle = angle;
        this.radius = radius;
    }

    public BinaryEdgeData(String vertexName1, String vertexName2, double weight, int direction)
    {
        this(vertexName1, vertexName2, weight, direction, 0, 0.5);
    }

    @Override
    public String toString() {
        return "BinaryEdge(" +
                weight + ", " +
                vertexName1 + ", " +
                vertexName2 + ", " +
                direction + ", " +
                angle + ", " +
                radius + ")";
    }

    @Override
    public boolean isBinary() {
        return true;
    }

    @Override
    public String getVertexName1() {
        return vertexName1;
    }

    @Override
    public String getVertexName2() {
        return vertexName2;
    }

    @Override
    public int getDirection() {
        return direction;
    }

    public double getAngle() {
        return angle;
    }

    public double getRadius() {
        return radius;
    }

    @Override
    public Edge create(GraphGroup graphGroup, Map<String, Vertex> nameToVertex) {
        BinaryEdge edge = new BinaryEdge(graphGroup, nameToVertex.get(vertexName1),
                nameToVertex.get(vertexName2));
        edge.setPositionBy(angle, radius);
        switch (direction) {
            case -1:
                edge.setDirection(Edge.Direction.FirstVertex);
                break;
            case 0:
                edge.setDirection(Edge.Direction.Both);
                break;
            case 1:
                edge.setDirection(Edge.Direction.SecondVertex);
                break;
        }
        edge.setWeight(weight, false);
        return edge;
    }
}
