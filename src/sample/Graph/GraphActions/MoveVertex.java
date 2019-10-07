package sample.Graph.GraphActions;

import org.apache.commons.math3.geometry.euclidean.twod.Vector2D;
import sample.Graph.Elements.Vertex;
import sample.Graph.GraphActionsController;

public class MoveVertex extends VertexAction {
    public static void create(Vertex vertex) {
        GraphActionsController.addAction(new MoveVertex(vertex));
    }

    private static Vector2D savedPos = null;

    public static void savePos(Vector2D pos) {
        savedPos = pos;
    }


    private Vector2D oldPos, newPos;

    private MoveVertex(Vertex vertex) {
        super(vertex);

//        if (savedPos == null) {
//            System.out.println("WARNING! Called MoveVertex constructor " +
//                    "without oldPos, but savedPos equals null");
//        }
        oldPos = savedPos != null ? savedPos : newPos;
        savedPos = null;

        this.newPos = vertex.getCenterPos();
    }

    public MoveVertex(Vertex vertex, Vector2D oldPos, Vector2D newPos) {
        super(vertex);
        this.oldPos = oldPos;
        this.newPos = newPos;
    }

    @Override
    public void undo() {
        vertex.setCenter(oldPos);
    }

    @Override
    public void redo() {
        vertex.setCenter(newPos);
    }
}
