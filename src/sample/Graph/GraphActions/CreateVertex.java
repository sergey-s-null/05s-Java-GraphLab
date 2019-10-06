package sample.Graph.GraphActions;

import sample.Graph.Elements.Edge;
import sample.Graph.Elements.Vertex;
import sample.Graph.GraphActionsController;
import sample.Graph.GraphGroup;

import java.util.HashSet;
import java.util.Set;

public class CreateVertex extends VertexAction {
    public static void create(Vertex vertex, GraphGroup graphGroup) {
        GraphActionsController.addAction(new CreateVertex(vertex, graphGroup));
    }

    private GraphGroup graphGroup;
    private Set<Edge> savedEdges = new HashSet<>();

    public CreateVertex(Vertex vertex, GraphGroup graphGroup) {
        super(vertex);
        this.graphGroup = graphGroup;
    }

    @Override
    public void undo() {
        savedEdges = vertex.getEdges();
        graphGroup.removeVertexWithEdges(vertex, false);
    }

    @Override
    public void redo() {
        graphGroup.addVertex(vertex, false);
        for (Edge edge : savedEdges)
            graphGroup.addEdge(edge, false);
    }
}
