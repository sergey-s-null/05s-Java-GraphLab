package sample.Graph.Elements;

import javafx.beans.value.ObservableValue;
import javafx.scene.shape.*;
import org.apache.commons.math3.geometry.euclidean.twod.Vector2D;
import sample.Graph.GraphGroup;
import sample.Main;

public class UnaryEdge extends Edge {
    public static final Vector2D defaultCirclePos = new Vector2D(30, -30);
//    private static final double minCircleDistance = 50;

    private GraphGroup graphGroup;
    private final Vertex vertex;
    private Path arrow = new Path(new MoveTo(), new LineTo(), new LineTo());

    private Vector2D circlePos = defaultCirclePos;

    // constructor
    public UnaryEdge(GraphGroup graphGroup, Vertex vertex) {
        super();
        this.graphGroup = graphGroup;
        direction.setValue(Direction.FirstVertex);

        this.vertex = vertex;
        this.vertex.addIncidentEdge(this);

        initArc();
        initArrows();
        initCircle();
        initWeight();
        getChildren().addAll(arc, circle, arrow, weightText);
        update();

        connect();
        weight.addListener((obj, prevValue, newValue) -> {
            changedWeight();
        });
        direction.addListener((observable, oldValue, newValue) -> {
            changedDirection(newValue);
        });
    }

    public UnaryEdge(GraphGroup graphGroup, Vertex vertex, Vector2D circlePos) {
        this(graphGroup, vertex);
        this.circlePos = circlePos;
        update();
    }

    //
    @Override
    public boolean isDirectionTo(Vertex vertex) {
        return this.vertex == vertex;
    }

    public Vector2D getCirclePos() {
        return circlePos;
    }

    // init
    private void initArrows() {
        arrow.setStrokeWidth(strokeWidth);
        arrow.setStroke(lineColor);
    }

    // updates
    @Override
    public void update() {
        double arcRadius = 0.5 * circlePos.getNorm() - 0.25 * Vertex.radius;
        double cos_A = 1 - 3 * Math.pow(Vertex.radius, 2) /
                (8 * Math.pow(arcRadius, 2) + 4 * Vertex.radius * arcRadius);
        double angle_A = Math.acos(cos_A);

        Vector2D vertexCenter = new Vector2D(vertex.getCenterX(), vertex.getCenterY());
        Vector2D arcCenter = Main.normalizeOrZero(circlePos)
                .scalarMultiply(arcRadius + 0.5 * Vertex.radius).add(vertexCenter);

        Vector2D baseArcRadius = Main.normalizeOrZero(circlePos).scalarMultiply(-arcRadius);
        Vector2D firstArcConnectPos = Main.rotate(baseArcRadius, angle_A).add(arcCenter);
        Vector2D secondArcConnectPos = Main.rotate(baseArcRadius, -angle_A).add(arcCenter);

        updateCircle();
        updateArc(arcRadius, firstArcConnectPos, secondArcConnectPos);
        updateArrow(secondArcConnectPos, angle_A);
        updateWeight();
    }

    private void updateCircle() {
        circle.setCenterX(vertex.getCenterX() + circlePos.getX());
        circle.setCenterY(vertex.getCenterY() + circlePos.getY());
    }

    private void updateArc(double arcRadius, Vector2D firstPoint, Vector2D secondPoint) {
        MoveTo moveTo = (MoveTo)arc.getElements().get(0);
        ArcTo arcTo = (ArcTo)arc.getElements().get(1);

        moveTo.setX(firstPoint.getX());
        moveTo.setY(firstPoint.getY());
        arcTo.setX(secondPoint.getX());
        arcTo.setY(secondPoint.getY());
        arcTo.setRadiusX(arcRadius);
        arcTo.setRadiusY(arcRadius);
    }

    private void updateArrow(Vector2D arrowPos, double angle) {
        Vector2D normalArrow = Main.normalizeOrZero(new Vector2D(circlePos.getY(), -circlePos.getX()))
                .scalarMultiply(arrowLength);

        normalArrow = Main.rotate(normalArrow, -angle);
        Vector2D tailPos1 = Main.rotate(normalArrow, arrowRotateAngle).add(arrowPos);
        Vector2D tailPos2 = Main.rotate(normalArrow, -arrowRotateAngle).add(arrowPos);

        MoveTo moveToArrow = (MoveTo) arrow.getElements().get(0);
        LineTo lineTo1 = (LineTo) arrow.getElements().get(1);
        LineTo lineTo2 = (LineTo) arrow.getElements().get(2);

        moveToArrow.setX(tailPos1.getX());
        moveToArrow.setY(tailPos1.getY());
        lineTo1.setX(arrowPos.getX());
        lineTo1.setY(arrowPos.getY());
        lineTo2.setX(tailPos2.getX());
        lineTo2.setY(tailPos2.getY());
    }

    private void updateWeight() {
        weightText.setText(Double.toString(weight.get()));
        double x = vertex.getCenterX() + circlePos.getX(),
               y = vertex.getCenterY() + circlePos.getY();
        weightText.setX(x - weightText.getLayoutBounds().getWidth() / 2);
        weightText.setY(y);
    }

    // position
    @Override
    public void setPosition(double x, double y) {
        // validate x, y
        if (x < radiusCircle)
            x = radiusCircle;
        else if (x > graphGroup.getWidth() - radiusCircle)
            x = graphGroup.getWidth() - radiusCircle;
        if (y < radiusCircle)
            y = radiusCircle;
        else if (y > graphGroup.getHeight() - radiusCircle)
            y = graphGroup.getHeight() - radiusCircle;

        circlePos = new Vector2D(x - vertex.getCenterX(), y - vertex.getCenterY());
        update();
    }

    public void setPosition(Vector2D circlePosRelativeVertex) {
        this.circlePos = circlePosRelativeVertex;
        update();
    }

    // vertices
    @Override
    public Vertex getFirstVertex() {
        return getVertex();
    }

    @Override
    public Vertex getSecondVertex() {
        return getVertex();
    }

    public Vertex getVertex() {
        return vertex;
    }

    @Override
    public void connect() {
        setOnMouseClicked(graphGroup::onMouseClick_edge);
        setOnMousePressed(graphGroup::onMousePress_edge);

        vertex.addIncidentEdge(this);
        vertex.positionObservable().addListener(this::changedVertexPosition);
    }

    @Override
    public void disconnect() {
        setOnMouseClicked(null);
        setOnMousePressed(null);

        vertex.removeIncidentEdge(this);
        vertex.positionObservable().removeListener(this::changedVertexPosition);
    }

    // events
    private void changedVertexPosition(ObservableValue<? extends Vector2D> obs, Vector2D oldValue, Vector2D newValue) {
        update();
    }

    private void changedWeight() {
        updateWeight();
    }

    private void changedDirection(Direction newDirection) {
        if (!newDirection.equals(Direction.FirstVertex))
            setDirection(Direction.FirstVertex);
    }


}
