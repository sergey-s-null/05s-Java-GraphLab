package sample.Graph;

import sample.Graph.Elements.Edge;
import sample.Graph.Elements.Vertex;

import java.util.ArrayList;
import java.util.List;

public class ElementsContainer {
    private List<Vertex> vertexes = new ArrayList<>();
    private List<Edge> edges = new ArrayList<>();

    public ElementsContainer() {}

    public ElementsContainer(ElementsContainer other) {
        vertexes = new ArrayList<>(other.vertexes);
        edges = new ArrayList<>(other.edges);
    }

    public void addVertex(Vertex vertex) {
        vertexes.add(vertex);
    }

    public void removeVertex(Vertex vertex) {
        vertexes.remove(vertex);
    }

    public void addEdge(Edge edge) {
        edges.add(edge);
    }

    public void removeEdge(Edge edge) {
        edges.remove(edge);
    }


}
