package sample.Graph.GraphActions;

import sample.Graph.GraphActionsController;
import sample.Graph.GraphGroup;

public class ChangeHeight extends Action {
    public static void create(GraphGroup graphGroup, double oldHeight, double newHeight) {
        GraphActionsController.addAction(new ChangeHeight(graphGroup, oldHeight, newHeight));
    }

    private GraphGroup graphGroup;
    private double oldHeight, newHeight;

    public ChangeHeight(GraphGroup graphGroup, double oldHeight, double newHeight) {
        super();
        this.graphGroup = graphGroup;
        this.oldHeight = oldHeight;
        this.newHeight = newHeight;
    }

    @Override
    public void undo() {
        graphGroup.setHeight(oldHeight, false);
    }

    @Override
    public void redo() {
        graphGroup.setHeight(newHeight, false);
    }
}
