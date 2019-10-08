package sample.Graph.GraphActions;

import sample.Graph.Elements.Edge;
import sample.Graph.GraphGroup;

public class CreateEdge extends EdgeAction {
    private GraphGroup graphGroup;

    public CreateEdge(Edge edge, GraphGroup graphGroup) {
        super(edge);
        this.graphGroup = graphGroup;
    }

    @Override
    public void undo() {
        edge.disconnect();
        graphGroup.removeEdge(edge, false);
    }

    @Override
    public void redo() {
        edge.connect();
        graphGroup.addEdge(edge, false);
    }
}
