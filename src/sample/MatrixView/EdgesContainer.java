package sample.MatrixView;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import sample.Graph.Elements.BinaryEdge;
import sample.Graph.Elements.Edge;
import sample.Graph.Elements.UnaryEdge;
import sample.Graph.Elements.Vertex;

import java.util.HashSet;
import java.util.Set;


public class EdgesContainer {
    private Multimap<Vertex, Edge> vertexToEdges = HashMultimap.create();
    private Multimap<Vertex, Edge> vertexToUnaryEdges = HashMultimap.create();

    public void add(Edge edge) {
        if (edge.getClass() == UnaryEdge.class) {
            vertexToUnaryEdges.put(edge.getFirstVertex(), edge);
        }
        else if (edge.getClass() == BinaryEdge.class) {
            vertexToEdges.put(edge.getFirstVertex(), edge);
            vertexToEdges.put(edge.getSecondVertex(), edge);
        }
    }

    public void remove(Edge edge) {
        if (edge.getClass() == UnaryEdge.class) {
            vertexToUnaryEdges.remove(edge.getFirstVertex(), edge);
        }
        else if (edge.getClass() == BinaryEdge.class) {
            vertexToEdges.remove(edge.getFirstVertex(), edge);
            vertexToEdges.remove(edge.getSecondVertex(), edge);
        }
    }

    public Set<Edge> get(Vertex vertex1, Vertex vertex2) {
        Set<Edge> result = new HashSet<>(vertexToEdges.get(vertex1));
        result.retainAll(vertexToEdges.get(vertex2));

        return result;
    }

    public Set<Edge> get(Vertex vertex) {
        return new HashSet<>(vertexToUnaryEdges.get(vertex));
    }

}
