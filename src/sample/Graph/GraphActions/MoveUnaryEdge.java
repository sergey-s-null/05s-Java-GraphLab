package sample.Graph.GraphActions;

import org.apache.commons.math3.geometry.euclidean.twod.Vector2D;
import sample.Graph.Elements.UnaryEdge;

public class MoveUnaryEdge extends EdgeAction {
    private static Vector2D savedCirclePosRelativeVertex = null;

    public static void saveCirclePos(Vector2D circlePosRelativeVertex) {
        savedCirclePosRelativeVertex = circlePosRelativeVertex;
    }


    private Vector2D oldCirclePosRelativeVertex, newCirclePosRelativeVertex;

    public MoveUnaryEdge(UnaryEdge edge) {
        super(edge);

//        if (savedCirclePosRelativeVertex == null) {
//            System.out.println("WARNING! Called MoveUnaryEdge constructor without saving data.");
//        }
        oldCirclePosRelativeVertex = savedCirclePosRelativeVertex != null ?
                savedCirclePosRelativeVertex : edge.getCirclePos();
        savedCirclePosRelativeVertex = null;
        newCirclePosRelativeVertex = edge.getCirclePos();
    }

    @Override
    public void undo() {
        ((UnaryEdge) edge).setPosition(oldCirclePosRelativeVertex);
    }

    @Override
    public void redo() {
        ((UnaryEdge) edge).setPosition(newCirclePosRelativeVertex);
    }
}
