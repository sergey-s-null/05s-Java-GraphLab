package sample.Graph;

import sample.Graph.Elements.Edge;
import sample.Graph.Elements.Element;
import sample.Graph.Elements.Vertex;

import java.util.ArrayList;
import java.util.List;

public class GraphPath {
    protected List<Vertex> vertices = new ArrayList<>();
    protected List<Edge> edges = new ArrayList<>();
    protected double length = 0;


    public GraphPath(Vertex firstVertex) {
        vertices.add(firstVertex);
    }

    public GraphPath(GraphPath other) {
        vertices.addAll(other.vertices);
        edges.addAll(other.edges);
        length = other.length;
    }

    public void add(Edge edge, Vertex nextVertex) {
        edges.add(edge);
        vertices.add(nextVertex);
        length += edge.getWeight();
    }

    public void removeLast() {
        if (edges.size() == 0) return;
        length -= edges.get(edges.size() - 1).getWeight();
        edges.remove(edges.size() - 1);
        vertices.remove(vertices.size() - 1);
    }

    public boolean contains(Vertex vertex) {
        return vertices.contains(vertex);
    }

    public Vertex getFirstVertex() {
        return vertices.get(0);
    }

    public Vertex getLastVertex() {
        return vertices.get(vertices.size() - 1);
    }

    public double getLength() {
        return length;
    }

    public List<Vertex> getVertices() {
        return vertices;
    }

    public void setSelectedAsPath(boolean flag) {
        for (Vertex vertex : vertices)
            vertex.setSelectedAsPath(flag);
        for (Edge edge : edges)
            edge.setSelectedAsPath(flag);
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append(vertices.get(0).getName());
        for (int i = 1; i < vertices.size(); ++i) {
            builder.append(" -> ");
            builder.append(vertices.get(i).getName());
        }
        return builder.toString();
    }
}
