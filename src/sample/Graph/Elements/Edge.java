package sample.Graph.Elements;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.geometry.VPos;
import javafx.scene.Group;
import javafx.scene.paint.Color;
import javafx.scene.shape.ArcTo;
import javafx.scene.shape.Circle;
import javafx.scene.shape.MoveTo;
import javafx.scene.shape.Path;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;


public abstract class Edge extends Group {
    public enum Direction {
        Both,
        FirstVertex,
        SecondVertex
    }

    protected static final double radiusCircle = 8, strokeWidthCircle = 2,
            strokeWidth = 2,
            arrowLength = 9, arrowRotateAngle = 0.4;
    protected static final Color lineColor = Color.web("BCBABE"),
                                 circleColor = Color.web("F1F1F2"),
                                 weightColor = Color.web("086070");

    protected Path arc = new Path(new MoveTo(), new ArcTo());
    protected Circle circle = new Circle(radiusCircle);
    protected Text weightText = new Text();
    protected DoubleProperty weight = new SimpleDoubleProperty(1.0);

    protected void initArc() {
        arc.setStrokeWidth(strokeWidth);
        arc.setStroke(lineColor);
        ArcTo arcTo = (ArcTo)arc.getElements().get(1);
        arcTo.setLargeArcFlag(true);
    }

    protected void initCircle() {
        circle.setFill(circleColor);
        circle.setStrokeWidth(strokeWidthCircle);
        circle.setStroke(lineColor);
    }

    protected void initWeight() {
        weightText.setTextAlignment(TextAlignment.CENTER);
        weightText.setTextOrigin(VPos.CENTER);
        weightText.setText(Double.toString(weight.get()));
        weightText.setFill(weightColor);
        weightText.setStroke(weightColor);
        weightText.setStrokeWidth(0.3);
    }

    abstract public void update();

    abstract public void move(double x, double y);

    abstract public void disconnectVertexes();

    abstract public void connectVertexes();

    abstract public void setDirection(Direction direction);

    abstract public Direction getDirection();

    abstract public boolean equalsDirection(Direction direction);

    public void setWeight(double weight) {
        this.weight.set(weight);
    }

    public double getWeight() {
        return weight.get();
    }

    abstract public Vertex getFirstVertex();

    abstract public Vertex getSecondVertex();

    public DoubleProperty weightProperty() {
        return weight;
    }

}
