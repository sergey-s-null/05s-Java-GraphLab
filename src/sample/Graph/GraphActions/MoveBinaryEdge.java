package sample.Graph.GraphActions;

import sample.Graph.Elements.BinaryEdge;

public class MoveBinaryEdge extends EdgeAction {
    private static Double savedPointAngle = null, savedPointRadiusCoef = null;

    public static void saveParams(double pointAngle, double pointRadiusCoef) {
        savedPointAngle = pointAngle;
        savedPointRadiusCoef = pointRadiusCoef;
    }

    private double oldPointAngle, oldPointRadiusCoef, newPointAngle, newPointRadiusCoef;

    public MoveBinaryEdge(BinaryEdge edge, double oldPointAngle, double oldPointRadiusCoef,
                          double newPointAngle, double newPointRadiusCoef) {
        super(edge);
        this.oldPointAngle = oldPointAngle;
        this.oldPointRadiusCoef = oldPointRadiusCoef;
        this.newPointAngle = newPointAngle;
        this.newPointRadiusCoef = newPointRadiusCoef;


    }

    public MoveBinaryEdge(BinaryEdge edge) {
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
