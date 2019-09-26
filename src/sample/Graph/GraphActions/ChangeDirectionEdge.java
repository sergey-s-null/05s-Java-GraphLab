package sample.Graph.GraphActions;

import sample.Graph.Elements.Edge;

public class ChangeDirectionEdge extends EdgeAction {
    private Edge.Direction oldDirection, newDirection;

    public ChangeDirectionEdge(Edge edge, Edge.Direction oldDirection, Edge.Direction newDirection) {
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
