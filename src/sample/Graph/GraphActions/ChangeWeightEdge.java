package sample.Graph.GraphActions;

import sample.Graph.Elements.Edge;


public class ChangeWeightEdge extends EdgeAction {
    private double oldWeight, newWeight;

    public ChangeWeightEdge(Edge edge, double oldWeight, double newWeight) {
        super(edge);

        this.oldWeight = oldWeight;
        this.newWeight = newWeight;
    }

    @Override
    public void undo() {
        edge.setWeight(oldWeight, false);
    }

    @Override
    public void redo() {
        edge.setWeight(newWeight, false);
    }
}
