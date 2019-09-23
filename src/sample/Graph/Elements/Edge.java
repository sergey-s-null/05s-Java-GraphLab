package sample.Graph.Elements;

import javafx.scene.Group;

public abstract class Edge extends Group {
    protected static final double radiusCircle = 8, strokeWidthCircle = 2,
            strokeWidth = 2,
            arrowLength = 9, arrowRotateAngle = 0.4;

    abstract public void update();

    abstract public void move(double x, double y);

    abstract public void disconnectVertexes();

}
