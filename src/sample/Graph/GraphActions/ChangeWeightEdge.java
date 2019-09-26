package sample.Graph.GraphActions;

import sample.Graph.Elements.Edge;

// TODO add this to graphGroup
public class ChangeWeightEdge extends EdgeAction {
    private double oldWeight, newWeight;

    public ChangeWeightEdge(Edge edge, double oldWeight, double newWeight) {
        super(edge);

        this.oldWeight = oldWeight;
        this.newWeight = newWeight;
    }

    @Override
    public void undo() {
        // TODO changeEdgeWeight
    }

    @Override
    public void redo() {
        // TODO changeEdgeWeight
    }
}
