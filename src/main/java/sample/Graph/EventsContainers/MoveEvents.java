package sample.Graph.EventsContainers;

import org.apache.commons.math3.geometry.euclidean.twod.Vector2D;
import sample.Graph.Elements.BinaryEdge;
import sample.Graph.Elements.Edge;
import sample.Graph.Elements.UnaryEdge;
import sample.Graph.Elements.Vertex;
import sample.Graph.GraphActions.MoveBinaryEdge;
import sample.Graph.GraphActions.MoveUnaryEdge;
import sample.Graph.GraphActions.MoveVertex;
import sample.Graph.GraphActionsController;
import sample.Graph.GraphGroup;

public class MoveEvents extends ContextMenusEvents {
    private GraphActionsController actionsController;
    private Vector2D mousePressedPos = new Vector2D(0, 0),
                     vertexStartPos = new Vector2D(0, 0);

    public MoveEvents(GraphGroup graphGroup) {
        super(graphGroup);
        this.actionsController = graphGroup.getActionsController();

        configurePressEvents();
        configureDragEvent();
        configureReleaseEvent();
    }

    private void configurePressEvents() {
        onPressVertex = (event) -> {
            movingVertex = (Vertex) event.getSource();
            movingEdge = null;
            mousePressedPos = new Vector2D(event.getX(), event.getY());
            vertexStartPos = new Vector2D(movingVertex.getCenterX(), movingVertex.getCenterY());
            MoveVertex.savePos(vertexStartPos);
        };
        onPressEdge = (event) -> {
            movingVertex = null;
            movingEdge = (Edge)event.getSource();
            if (movingEdge.getClass() == UnaryEdge.class) {
                MoveUnaryEdge.saveCirclePos(((UnaryEdge) movingEdge).getCirclePos());
            }
            else if (movingEdge.getClass() == BinaryEdge.class) {
                BinaryEdge edge = (BinaryEdge) movingEdge;
                MoveBinaryEdge.saveParams(edge.getPointAngle(), edge.getPointRadiusCoef());
            }
        };
    }

    private void configureDragEvent() {
        onGraphGroupDrag = (event) -> {
            if (movingVertex != null) {
                Vector2D newVertexPos = vertexStartPos.add(new Vector2D(event.getX(), event.getY())).subtract(mousePressedPos);
                movingVertex.setCenter(newVertexPos);
            }
            else if (movingEdge != null) {
                movingEdge.setPosition(event.getX(), event.getY());
            }
        };
    }

    private void configureReleaseEvent() {
        onGraphGroupRelease = (event) -> {
            if (movingVertex != null) {
                actionsController.addAction(new MoveVertex(movingVertex));
                movingVertex = null;
            }

            if (movingEdge != null) {
                if (movingEdge.getClass() == UnaryEdge.class) {
                    UnaryEdge edge = (UnaryEdge) movingEdge;
                    actionsController.addAction(new MoveUnaryEdge(edge));
                }
                else if (movingEdge.getClass() == BinaryEdge.class) {
                    BinaryEdge edge = (BinaryEdge) movingEdge;
                    actionsController.addAction(new MoveBinaryEdge(edge));
                }
                movingEdge = null;
            }
        };
    }

}
