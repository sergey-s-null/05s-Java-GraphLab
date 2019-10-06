package sample.Graph.GraphActions;

import sample.Graph.Elements.Edge;
import sample.Graph.Elements.Vertex;
import sample.Graph.GraphActionsController;
import sample.Graph.GraphGroup;

import java.util.Set;

public class DeleteVertex extends VertexAction {
    public static void create(Vertex vertex, Set<Edge> savedEdges, GraphGroup graphGroup) {
        GraphActionsController.addAction(new DeleteVertex(vertex, savedEdges, graphGroup));
    }

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
        savedEdges = vertex.getEdges();
        graphGroup.removeVertexWithEdges(vertex, false);
    }
}
