package sample.Graph.Elements;

import javafx.scene.Group;

public abstract class Edge extends Group {
    public enum Direction {
        Both,
        FirstVertex,
        SecondVertex
    }

    protected static final double radiusCircle = 8, strokeWidthCircle = 2,
            strokeWidth = 2,
            arrowLength = 9, arrowRotateAngle = 0.4;

    abstract public void update();

    abstract public void move(double x, double y);

    abstract public void disconnectVertexes();

    abstract public void connectVertexes();

    abstract public void setDirection(Direction direction);

    abstract public Direction getDirection();

    abstract public boolean equalsDirection(Direction direction);

}
