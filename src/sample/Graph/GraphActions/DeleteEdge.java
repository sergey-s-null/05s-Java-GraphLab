package sample.Graph.GraphActions;

import sample.Graph.Elements.Edge;
import sample.Graph.GraphGroup;

public class DeleteEdge extends EdgeAction {
    private GraphGroup graphGroup;

    public DeleteEdge(Edge edge, GraphGroup graphGroup) {
        super(edge);
        this.graphGroup = graphGroup;
    }

    @Override
    public void undo() {
        graphGroup.addEdge(edge);
    }

    @Override
    public void redo() {
        graphGroup.removeEdge(edge);
    }
}
