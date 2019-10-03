package sample.Graph.GraphActions;

import sample.Graph.Elements.Edge;
import sample.Graph.GraphActionsController;

public class ChangeDirectionEdge extends EdgeAction {
    public static void create(Edge edge, Edge.Direction oldDirection,
                              Edge.Direction newDirection)
    {
        GraphActionsController.addAction(new ChangeDirectionEdge(edge, oldDirection,
                newDirection));
    }

    private Edge.Direction oldDirection, newDirection;

    private ChangeDirectionEdge(Edge edge, Edge.Direction oldDirection,
                               Edge.Direction newDirection)
    {
        super(edge);
        this.oldDirection = oldDirection;
        this.newDirection = newDirection;
    }

    @Override
    public void undo() {
        edge.setDirection(oldDirection);
    }

    @Override
    public void redo() {
        edge.setDirection(newDirection);
    }
}
