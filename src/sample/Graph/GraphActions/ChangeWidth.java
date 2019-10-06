package sample.Graph.GraphActions;

import sample.Graph.GraphActionsController;
import sample.Graph.GraphGroup;

public class ChangeWidth extends Action {
    public static void create(GraphGroup graphGroup, double oldWidth, double newWidth) {
        GraphActionsController.addAction(new ChangeWidth(graphGroup, oldWidth, newWidth));
    }

    private GraphGroup graphGroup;
    private double oldWidth, newWidth;

    public ChangeWidth(GraphGroup graphGroup, double oldWidth, double newWidth) {
        super();
        this.graphGroup = graphGroup;
        this.oldWidth = oldWidth;
        this.newWidth = newWidth;
    }

    @Override
    public void undo() {
        graphGroup.setWidth(oldWidth, false);
    }

    @Override
    public void redo() {
        graphGroup.setWidth(newWidth, false);
    }
}
