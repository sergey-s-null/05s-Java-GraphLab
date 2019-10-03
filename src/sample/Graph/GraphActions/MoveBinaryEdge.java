package sample.Graph.GraphActions;

import sample.Graph.Elements.BinaryEdge;
import sample.Graph.GraphActionsController;

public class MoveBinaryEdge extends EdgeAction {
    public static void create(BinaryEdge edge) {
        GraphActionsController.addAction(new MoveBinaryEdge(edge));
    }

    private static Double savedPointAngle = null, savedPointRadiusCoef = null;

    public static void saveParams(double pointAngle, double pointRadiusCoef) {
        savedPointAngle = pointAngle;
        savedPointRadiusCoef = pointRadiusCoef;
    }

    private double oldPointAngle, oldPointRadiusCoef, newPointAngle, newPointRadiusCoef;

    private MoveBinaryEdge(BinaryEdge edge) {
        super(edge);

        // TODO remove
        if (savedPointAngle == null || savedPointRadiusCoef == null) {
            System.out.println("WARNING! Called MoveBinaryEdge constructor without saving params.");
        }
        oldPointAngle = savedPointAngle != null ? savedPointAngle : edge.getPointAngle();
        oldPointRadiusCoef = savedPointRadiusCoef != null ?
                savedPointRadiusCoef : edge.getPointRadiusCoef();
        savedPointAngle = null;
        savedPointRadiusCoef = null;

        newPointAngle = edge.getPointAngle();
        newPointRadiusCoef = edge.getPointRadiusCoef();
    }

    @Override
    public void undo() {
        ((BinaryEdge) edge).moveByPointData(oldPointAngle, oldPointRadiusCoef);
    }

    @Override
    public void redo() {
        ((BinaryEdge) edge).moveByPointData(newPointAngle, newPointRadiusCoef);
    }
}
