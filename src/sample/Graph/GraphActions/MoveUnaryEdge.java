package sample.Graph.GraphActions;

import org.apache.commons.math3.geometry.euclidean.twod.Vector2D;
import sample.Graph.Elements.UnaryEdge;
import sample.Graph.GraphActionsController;

public class MoveUnaryEdge extends EdgeAction {
    public static void create(UnaryEdge edge) {
        GraphActionsController.addAction(new MoveUnaryEdge(edge));
    }

    private static Vector2D savedCirclePosRelativeVertex = null;

    public static void saveCirclePos(Vector2D circlePosRelativeVertex) {
        savedCirclePosRelativeVertex = circlePosRelativeVertex;
    }

    private Vector2D oldCirclePosRelativeVertex, newCirclePosRelativeVertex;

    private MoveUnaryEdge(UnaryEdge edge) {
        super(edge);

//        if (savedCirclePosRelativeVertex == null) {
//            System.out.println("WARNING! Called MoveUnaryEdge constructor without saving data.");
//        }
        oldCirclePosRelativeVertex = savedCirclePosRelativeVertex != null ?
                savedCirclePosRelativeVertex : edge.getCirclePosRelativeVertex();
        savedCirclePosRelativeVertex = null;
        newCirclePosRelativeVertex = edge.getCirclePosRelativeVertex();
    }

    @Override
    public void undo() {
        ((UnaryEdge) edge).move(oldCirclePosRelativeVertex);
    }

    @Override
    public void redo() {
        ((UnaryEdge) edge).move(newCirclePosRelativeVertex);
    }
}
