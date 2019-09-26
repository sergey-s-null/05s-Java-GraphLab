package sample.Graph.Elements;

import javafx.geometry.VPos;
import javafx.scene.Group;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.StrokeType;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import org.apache.commons.math3.geometry.euclidean.twod.Vector2D;
import sample.Graph.GraphGroup;
import java.util.HashSet;
import java.util.Set;


public class Vertex extends Group {
    private static final String defaultName = "unnamed";
    private static final Color selectedFillColor = Color.ORANGE,
                               defaultFillColor = Color.GRAY,
                               strokeColor = Color.GREEN;
    private static final double strokeWidth = 2;
    public static final double radius = 12;

    private Circle circle = new Circle();
    private Text name = new Text();
    private Set<Edge> incidentEdges = new HashSet<>();

    private void initCircle() {
        circle.setStrokeType(StrokeType.INSIDE);
        circle.setStrokeWidth(strokeWidth);
        circle.setStroke(strokeColor);
        circle.setFill(defaultFillColor);
        circle.setRadius(radius);
    }

    private void initName(String name) {
        this.name.setTextAlignment(TextAlignment.CENTER);
        this.name.setTextOrigin(VPos.CENTER);
        this.name.setText(name);
        this.name.setFill(Color.PURPLE);
        this.name.setStroke(Color.PURPLE);
        this.name.setStrokeWidth(0.5);
    }

    private void initEvents(GraphGroup graphGroup) {
        setOnMouseClicked(graphGroup::onMouseClick_vertex);
        setOnMousePressed(graphGroup::onMousePress_vertex);
    }

    public Vertex(GraphGroup graphGroup, double x, double y) {
        super();

        initCircle();
        initName(defaultName);
        initEvents(graphGroup);
        getChildren().addAll(circle, name);

        move(x, y);
    }

    public Vertex(GraphGroup graphGroup, double x, double y, String nameStr) {
        super();

        initCircle();
        initName(nameStr);
        initEvents(graphGroup);
        getChildren().addAll(circle, this.name);

        move(x, y);
    }

    public void move(double x, double y) {
        if (x < radius)
            x = radius;
        else if (x > GraphGroup.width - radius)
            x = GraphGroup.width - radius;

        if (y < radius)
            y = radius;
        else if (y > GraphGroup.height - radius)
            y = GraphGroup.height - radius;

        circle.setCenterX(x);
        circle.setCenterY(y);

        name.setX(x - name.getLayoutBounds().getWidth()  / 2);
        name.setY(y - 2.5);

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

    public void removeAllIncidentEdges() {
        incidentEdges.clear();
    }

    public Set<Edge> getEdges() {
        return new HashSet<>(incidentEdges);
    }

    public void setSelected(boolean selected) {
        if (selected)
            circle.setFill(selectedFillColor);
        else
            circle.setFill(defaultFillColor);
    }

    public double getCenterX() {
        return circle.getCenterX();
    }

    public double getCenterY() {
        return circle.getCenterY();
    }

    public Vector2D getCenterPos() {
        return new Vector2D(circle.getCenterX(), circle.getCenterY());
    }

    public void rename(String nameStr) {
        name.setText(nameStr);
        // TODO update
    }

    public String getName() {
        return name.getText();
    }

}
