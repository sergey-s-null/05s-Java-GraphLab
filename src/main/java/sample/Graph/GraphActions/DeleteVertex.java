package sample.Graph.GraphActions;

import sample.Graph.Elements.Edge;
import sample.Graph.Elements.Vertex;
import sample.Graph.GraphGroup;

import java.util.Set;

public class DeleteVertex extends VertexAction {
    private GraphGroup graphGroup;
    private Set<Edge> savedEdges;

    public DeleteVertex(Vertex vertex, Set<Edge> savedEdges, GraphGroup graphGroup) {
        super(vertex);
        this.savedEdges = savedEdges;
        this.graphGroup = graphGroup;
    }

    @Override
    public void undo() {
        this.graphGroup.addVertex(vertex, false);
        if (savedEdges != null)
            for (Edge edge : savedEdges)
                graphGroup.addEdge(edge, false);
    }

    @Override
    public void redo() {
        savedEdges = vertex.getIncidentEdgesCopy();
        graphGroup.removeVertexWithEdges(vertex, false);
    }
}
