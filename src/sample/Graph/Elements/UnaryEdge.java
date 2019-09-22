package sample.Graph.Elements;

import javafx.scene.paint.Color;
import javafx.scene.shape.*;
import org.apache.commons.math3.geometry.euclidean.twod.Vector2D;
import sample.Graph.GraphGroup;
import sample.Main;

public class UnaryEdge extends Edge {
    private final static double minCircleDistance = 50;

    protected Vertex firstVertex;
    protected Path firstArrow = new Path(new MoveTo(), new LineTo(), new LineTo());
    protected Path arc = new Path(new MoveTo(), new ArcTo());
    protected Circle circle = new Circle(radiusCircle);
    private Vector2D circlePosRelativeVertex = new Vector2D(minCircleDistance, -minCircleDistance);

    protected void initArc() {
        arc.setStrokeWidth(strokeWidth);
        ArcTo arcTo = (ArcTo)arc.getElements().get(1);
        arcTo.setLargeArcFlag(true);
//        arcTo.setSweepFlag(true);
    }

    protected void initArrows() {
        firstArrow.setStrokeWidth(strokeWidth);
    }

    protected void initCircle() {
        circle.setFill(Color.RED);
        circle.setStrokeWidth(strokeWidthCircle);
        circle.setStroke(Color.BLACK);
    }

    protected UnaryEdge() {

    }

    public UnaryEdge(GraphGroup graphGroup, Vertex vertex) {
        super();

        this.firstVertex = vertex;
        this.firstVertex.addIncidentEdge(this);

        initArc();
        initArrows();
        initCircle();
        getChildren().addAll(arc, circle, firstArrow);
        update();

        setOnMouseClicked(graphGroup::onMouseClick_edge);
        setOnMousePressed(graphGroup::onMousePress_edge);
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
        Vector2D normalArrow = new Vector2D(circlePosRelativeVertex.getY(), -circlePosRelativeVertex.getX())
                .normalize().scalarMultiply(arrowLength);
        normalArrow = Main.rotate(normalArrow, -angle);
        Vector2D tailPos1 = Main.rotate(normalArrow, arrowRotateAngle).add(arrowPos);
        Vector2D tailPos2 = Main.rotate(normalArrow, -arrowRotateAngle).add(arrowPos);

        MoveTo moveToArrow = (MoveTo)firstArrow.getElements().get(0);
        LineTo lineTo1 = (LineTo)firstArrow.getElements().get(1);
        LineTo lineTo2 = (LineTo)firstArrow.getElements().get(2);

        moveToArrow.setX(tailPos1.getX());
        moveToArrow.setY(tailPos1.getY());
        lineTo1.setX(arrowPos.getX());
        lineTo1.setY(arrowPos.getY());
        lineTo2.setX(tailPos2.getX());
        lineTo2.setY(tailPos2.getY());
    }

    private void updateCircle() {
        circle.setCenterX(firstVertex.getCenterX() + circlePosRelativeVertex.getX());
        circle.setCenterY(firstVertex.getCenterY() + circlePosRelativeVertex.getY());
    }

    @Override
    public void update() {
        double arcRadius = 0.5 * circlePosRelativeVertex.getNorm() - 0.25 * Vertex.radius;
        double cos_A = 1 - 3 * Math.pow(Vertex.radius, 2) /
                (8 * Math.pow(arcRadius, 2) + 4 * Vertex.radius * arcRadius);
        double angle_A = Math.acos(cos_A);

        Vector2D vertexCenter = new Vector2D(firstVertex.getCenterX(), firstVertex.getCenterY());
        Vector2D arcCenter = circlePosRelativeVertex.normalize().scalarMultiply(arcRadius + 0.5 * Vertex.radius)
                .add(vertexCenter);
        Vector2D baseArcRadius = circlePosRelativeVertex.normalize().scalarMultiply(-arcRadius);
        Vector2D firstArcConnectPos = Main.rotate(baseArcRadius, angle_A).add(arcCenter);
        Vector2D secondArcConnectPos = Main.rotate(baseArcRadius, -angle_A).add(arcCenter);

        updateCircle();
        updateArc(arcRadius, firstArcConnectPos, secondArcConnectPos);
        updateArrow(secondArcConnectPos, angle_A);
    }

    @Override
    public void move(double x, double y) {
        // TODO check x, y
        circlePosRelativeVertex = new Vector2D(x - firstVertex.getCenterX(), y - firstVertex.getCenterY());
        update();
    }

    @Override
    public void disconnectVertexes() {
        firstVertex.removeIncidentEdge(this);
    }
}
