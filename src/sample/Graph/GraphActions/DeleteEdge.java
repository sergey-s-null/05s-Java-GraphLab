package sample.Graph.GraphActions;

import sample.Graph.Elements.Edge;
import sample.Graph.GraphActionsController;
import sample.Graph.GraphGroup;

public class DeleteEdge extends EdgeAction {
    public static void create(Edge edge, GraphGroup graphGroup) {
        GraphActionsController.addAction(new DeleteEdge(edge, graphGroup));
    }

    private GraphGroup graphGroup;

    public DeleteEdge(Edge edge, GraphGroup graphGroup) {
        super(edge);
        this.graphGroup = graphGroup;
    }

    @Override
    public void undo() {
        edge.connect();
        graphGroup.addEdge(edge, false);
    }

    @Override
    public void redo() {
        edge.disconnect();
        graphGroup.removeEdge(edge, false);
    }
}
