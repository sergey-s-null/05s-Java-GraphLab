package sample.Graph.Elements;

import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.StrokeType;
import org.apache.commons.math3.geometry.euclidean.twod.Vector2D;
import sample.Graph.GraphGroup;
import java.util.HashSet;
import java.util.Set;


public class Vertex extends Circle {
    public static final double radius = 12;

    private Set<Edge> incidentEdges = new HashSet<>();

    public Vertex(GraphGroup graphGroup, double x, double y) {
        super(x, y, radius);

        move(x, y);

        setStrokeType(StrokeType.INSIDE);
        setStrokeWidth(2);
        setStroke(Color.GREEN);
        setFill(Color.GRAY);

        setOnMouseClicked(graphGroup::onMouseClick_vertex);
        setOnMousePressed(graphGroup::onMousePress_vertex);

    }

    public void move(double x, double y) {
        if (x < radius)
            setCenterX(radius);
        else if (x > GraphGroup.width - radius)
            setCenterX(GraphGroup.width - radius);
        else
            setCenterX(x);

        if (y < radius)
            setCenterY(radius);
        else if (y > GraphGroup.height - radius)
            setCenterY(GraphGroup.height - radius);
        else
            setCenterY(y);

        for (Edge edge : incidentEdges) {
            edge.update();
        }
    }

    public void move(Vector2D radiusVector) {
        move(radiusVector.getX(), radiusVector.getY());
    }

    public void addIncidentEdge(Edge edge) {
        incidentEdges.add(edge);
    }

    public void removeIncidentEdge(Edge edge) {
        incidentEdges.remove(edge);
    }

    public Set<Edge> getEdges() {
        return incidentEdges;
    }
}
