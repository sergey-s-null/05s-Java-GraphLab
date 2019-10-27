package sample.Graph.Elements;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ObservableValue;
import javafx.geometry.VPos;
import javafx.scene.Group;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.StrokeType;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import org.apache.commons.math3.geometry.euclidean.twod.Vector2D;
import sample.Graph.GraphActions.Action;
import sample.Graph.GraphActions.MoveVertex;
import sample.Graph.GraphActions.RenameVertex;
import sample.Graph.GraphActionsController;
import sample.Graph.GraphGroup;
import sample.Parser.SimpleData.Resolution;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
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

    public class EdgeWithVertex {
        public Edge edge;
        public Vertex vertex;

        public EdgeWithVertex(Edge edge, Vertex vertex) {
            this.edge = edge;
            this.vertex = vertex;
        }
    }


    private final int id;
    private GraphGroup graphGroup;
    private GraphActionsController actionsController;
    private Circle circle = new Circle();
    private ObjectProperty<Vector2D> position = new SimpleObjectProperty<>(new Vector2D(0, 0));
    private Text name = new Text();
    private Set<Edge> incidentEdges = new HashSet<>();


    // constructors
    public Vertex(GraphGroup graphGroup, double x, double y) {
        this(graphGroup, x, y, Integer.toString(nextId));
    }

    public Vertex(GraphGroup graphGroup, double x, double y, String nameStr) {
        super();
        this.graphGroup = graphGroup;
        actionsController = graphGroup.getActionsController();

        connect();

        id = nextId++;
        initCircle();
        initName(nameStr);
        getChildren().addAll(circle, name);

        setCenter(x, y);
    }

    //
    public int getVertexId() {
        return id;
    }

    public void setSelected(boolean selected) {
        if (selected)
            circle.setFill(Style.vertexSelectColor);
        else
            circle.setFill(Style.vertexFillColor);
    }

    public Action getActionForNewResolution(Resolution resolution) {
        double x = getCenterX(), y = getCenterY();
        if (x > resolution.getWidth() - Style.vertexCircleRadius)
            x = resolution.getWidth() - Style.vertexCircleRadius;
        if (y > resolution.getHeight() - Style.vertexCircleRadius)
            y = resolution.getHeight() - Style.vertexCircleRadius;

        if (x != getCenterX() || y != getCenterY())
            return new MoveVertex(this, getCenterPos(), new Vector2D(x, y));
        else
            return null;
    }

    public List<EdgeWithVertex> getNextVertices() {
        List<EdgeWithVertex> result = new ArrayList<>();
        for (Edge edge : incidentEdges) {
            Vertex vertexTo = edge.hasDirectionFrom(this);
            if (vertexTo != null)
                result.add(new EdgeWithVertex(edge, vertexTo));
        }
        return result;
    }

    // init
    private void initCircle() {
        circle.setStrokeType(StrokeType.INSIDE);
        circle.setStrokeWidth(Style.lineWidth);
        circle.setStroke(Style.lineColor);
        circle.setFill(Style.vertexFillColor);
        circle.setRadius(Style.vertexCircleRadius);
    }

    private void initName(String name) {
        this.name.setTextAlignment(TextAlignment.CENTER);
        this.name.setTextOrigin(VPos.CENTER);
        this.name.setText(name);
        this.name.setFill(Style.textColor);
        this.name.setStroke(Style.textColor);
        this.name.setStrokeWidth(Style.vertexNameStrokeWidth);
    }

    // center methods
    public void setCenter(Vector2D radiusVector) {
        setCenter(radiusVector.getX(), radiusVector.getY());
    }

    public void setCenter(double x, double y) {
        if (x < Style.vertexCircleRadius)
            x = Style.vertexCircleRadius;
        else if (x > graphGroup.getWidth() - Style.vertexCircleRadius)
            x = graphGroup.getWidth() - Style.vertexCircleRadius;

        if (y < Style.vertexCircleRadius)
            y = Style.vertexCircleRadius;
        else if (y > graphGroup.getHeight() - Style.vertexCircleRadius)
            y = graphGroup.getHeight() - Style.vertexCircleRadius;

        circle.setCenterX(x);
        circle.setCenterY(y);
        position.setValue(new Vector2D(x, y));

        updateNameText();
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

    ObservableValue<Vector2D> positionObservable() {
        return position;
    }

    // name
    public void setName(String newName, boolean createAction) {
        if (createAction)
            actionsController.addAction(new RenameVertex(this, name.getText(), newName));
        name.setText(newName);
        updateNameText();
    }

    public String getName() {
        return name.getText();
    }

    public StringProperty nameProperty() {
        return name.textProperty();
    }

    // updates
    private void updateNameText() {
        name.setX(circle.getCenterX() - name.getLayoutBounds().getWidth()  / 2);
        name.setY(circle.getCenterY() - 2.5);
    }

    // incident edges
    void addIncidentEdge(Edge edge) {
        incidentEdges.add(edge);
    }

    void removeIncidentEdge(Edge edge) {
        incidentEdges.remove(edge);
    }

    public Set<Edge> getEdgesCopy() {
        return new HashSet<>(incidentEdges);
    }

    // events
    public void connect() {
        setOnMouseClicked(graphGroup::onMouseClick_vertex);
        setOnMousePressed(graphGroup::onMousePress_vertex);
    }

    public void disconnect() {
        setOnMouseClicked(null);
        setOnMousePressed(null);
    }

}
