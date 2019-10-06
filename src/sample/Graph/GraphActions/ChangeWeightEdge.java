package sample.Graph.GraphActions;

import sample.Graph.Elements.Edge;
import sample.Graph.GraphActionsController;


public class ChangeWeightEdge extends EdgeAction {
    public static void create(Edge edge, double oldWeight, double newWeight) {
        GraphActionsController.addAction(new ChangeWeightEdge(edge, oldWeight, newWeight));
    }

    private double oldWeight, newWeight;

    private ChangeWeightEdge(Edge edge, double oldWeight, double newWeight) {
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
