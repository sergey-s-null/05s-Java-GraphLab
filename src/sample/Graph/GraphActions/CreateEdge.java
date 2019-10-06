package sample.Graph.GraphActions;

import sample.Graph.Elements.Edge;
import sample.Graph.GraphActionsController;
import sample.Graph.GraphGroup;

public class CreateEdge extends EdgeAction {
    public static void create(Edge edge, GraphGroup graphGroup) {
        GraphActionsController.addAction(new CreateEdge(edge, graphGroup));
    }

    private GraphGroup graphGroup;

    public CreateEdge(Edge edge, GraphGroup graphGroup) {
        super(edge);
        this.graphGroup = graphGroup;
    }

    @Override
    public void undo() {
        edge.disconnectVertices();
        graphGroup.removeEdge(edge, false);
    }

    @Override
    public void redo() {
        edge.connectVertices();
        graphGroup.addEdge(edge, false);
    }
}
