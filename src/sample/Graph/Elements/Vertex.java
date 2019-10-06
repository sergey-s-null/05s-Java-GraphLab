package sample.Graph.Elements;

import javafx.beans.property.StringProperty;
import javafx.geometry.VPos;
import javafx.scene.Group;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.StrokeType;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import org.apache.commons.math3.geometry.euclidean.twod.Vector2D;
import sample.Graph.GraphActions.RenameVertex;
import sample.Graph.GraphGroup;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class Vertex extends Group {
    public static boolean isNameValid(String name) {
        Pattern pattern = Pattern.compile("[^%\\s()\\[\\]{},]{1,10}");
        Matcher matcher = pattern.matcher(name);
        return matcher.matches();
    }

    private static int nextId = 0;

    private static final Color nameColor = Color.web("086070");
    private static final Color selectedFillColor = Color.ORANGE,
                               defaultFillColor = Color.web("A1D6E2"),
                               strokeColor = Color.web("BCBABE");
    private static final double strokeWidth = 2;
    public static final double radius = 12;

    private final int id;
    private Circle circle = new Circle();
    private double x = 0, y = 0;
    private Text name = new Text();
    private Set<Edge> incidentEdges = new HashSet<>();

    // init
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
        this.name.setFill(nameColor);
        this.name.setStroke(nameColor);
        this.name.setStrokeWidth(0.5);
    }

    private void initEvents(GraphGroup graphGroup) {
        setOnMouseClicked(graphGroup::onMouseClick_vertex);
        setOnMousePressed(graphGroup::onMousePress_vertex);
    }

    // constructors
    public Vertex(GraphGroup graphGroup, double x, double y, String nameStr) {
        super();

        id = nextId++;
        initCircle();
        initName(nameStr);
        initEvents(graphGroup);
        getChildren().addAll(circle, name);

        move(x, y);
    }

    public Vertex(GraphGroup graphGroup, double x, double y) {
        this(graphGroup, x, y, Integer.toString(nextId));
    }

    // updates
    private void update() {
        circle.setCenterX(x);
        circle.setCenterY(y);

        name.setX(x - name.getLayoutBounds().getWidth()  / 2);
        name.setY(y - 2.5);

        for (Edge edge : incidentEdges) {
            edge.update();
        }
    }

    // move
    public void move(double x, double y) {
        if (x < radius)
            x = radius;
        else if (x > GraphGroup.width - radius)
            x = GraphGroup.width - radius;

        if (y < radius)
            y = radius;
        else if (y > GraphGroup.height - radius)
            y = GraphGroup.height - radius;

        this.x = x;
        this.y = y;

        update();
    }

    public void move(Vector2D radiusVector) {
        move(radiusVector.getX(), radiusVector.getY());
    }

    // incident edges
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

    // vertex methods
    public int getVertexId() {
        return id;
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

    public void setSelected(boolean selected) {
        if (selected)
            circle.setFill(selectedFillColor);
        else
            circle.setFill(defaultFillColor);
    }

    // name
    public void setName(String newName, boolean createAction) {
        if (createAction)
            RenameVertex.create(this, name.getText(), newName);
        name.setText(newName);
        update();
    }

    public String getName() {
        return name.getText();
    }

    public StringProperty nameProperty() {
        return name.textProperty();
    }
}
