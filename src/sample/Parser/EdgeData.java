package sample.Parser;

import sample.Graph.Elements.Edge;
import sample.Graph.Elements.Vertex;
import sample.Graph.GraphGroup;

import java.util.Map;

abstract public class EdgeData {
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
