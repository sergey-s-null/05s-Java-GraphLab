package sample.Graph.Elements;


import Jama.Matrix;
import javafx.scene.Group;
import javafx.scene.paint.Color;
import javafx.scene.shape.*;
import org.apache.commons.math3.geometry.euclidean.twod.Vector2D;
import sample.Graph.GraphGroup;


public class Edge extends Group {
    private static final double radiusCircle = 8, strokeWidthCircle = 2, strokeWidth = 2;
    private static final double defaultArcRadius = 20000;

    private Vertex firstVertex, secondVertex;
    //private Path firstArrow = new Path(), secondArrow = new Path();
    private MoveTo moveTo = new MoveTo();
    private ArcTo arcTo = new ArcTo();
    private Circle circle = new Circle(radiusCircle);
    private double pointAngle = 0, pointRadiusCoef = 0.5;

    private void initArc() {

    }

    private void initArrows() {

    }
    private void initCircle() {

    }

    public Edge(GraphGroup graphGroup, Vertex firstVertex, Vertex secondVertex) {
        super();

        this.firstVertex = firstVertex;
        this.secondVertex = secondVertex;
        this.firstVertex.addIncidentEdge(this);
        this.secondVertex.addIncidentEdge(this);

        Path path = new Path(moveTo, arcTo);
        path.setStrokeWidth(strokeWidth);

        circle.setFill(Color.RED);
        circle.setStrokeWidth(strokeWidthCircle);
        circle.setStroke(Color.BLACK);



        getChildren().addAll(path, circle);
        update();

        setOnMouseClicked(graphGroup::onMouseClick_edge);
        setOnMousePressed(graphGroup::onMousePress_edge);
    }

    private Vector2D calculateCircleCenter() {
        Matrix rotateMatrix = new Matrix(new double[][] {
                {Math.cos(pointAngle), Math.sin(pointAngle)},
                {-Math.sin(pointAngle), Math.cos(pointAngle)},
        });
        Matrix v2Matrix = new Matrix(new double[][] {
                {secondVertex.getCenterX() - firstVertex.getCenterX()},
                {secondVertex.getCenterY() - firstVertex.getCenterY()},
        });
        Matrix resMatrix = rotateMatrix.times(v2Matrix);
        return new Vector2D(firstVertex.getCenterX() + resMatrix.getArray()[0][0] * pointRadiusCoef,
                            firstVertex.getCenterY() + resMatrix.getArray()[1][0] * pointRadiusCoef);
    }

    private double calculateArcRadius(Vector2D circlePos) {
        double A1 = circlePos.getX() - firstVertex.getCenterX(), B1 = circlePos.getY() - firstVertex.getCenterY(),
               C1 = -A1*(firstVertex.getCenterX() + circlePos.getX())*0.5 - B1*(firstVertex.getCenterY() + circlePos.getY())*0.5,
               A2 = secondVertex.getCenterX() - circlePos.getX(), B2 = secondVertex.getCenterY() - circlePos.getY(),
               C2 = -A2*(circlePos.getX() + secondVertex.getCenterX())*0.5 - B2*(circlePos.getY() + secondVertex.getCenterY())*0.5;

        Matrix mCoef = new Matrix(new double[][] {
                {A1, B1},
                {A2, B2},
        });
        Matrix mFreeMembers = new Matrix(new double[][] {
                {-C1},
                {-C2},
        });

        try {
            Matrix res = mCoef.solve(mFreeMembers);
            double x_center = res.getArray()[0][0],
                    y_center = res.getArray()[1][0];
            return Math.sqrt(Math.pow(circlePos.getX() - x_center, 2) + Math.pow(circlePos.getY() - y_center, 2));
        }
        catch (RuntimeException e) {
            //System.out.println("Ahh shit, here we go again...");
            return defaultArcRadius;
        }
    }

    private boolean calculateSweepFlag(Vector2D circlePos) {
        Matrix sweepMtx = new Matrix(new double[][] {
                {secondVertex.getCenterX() - firstVertex.getCenterX(), secondVertex.getCenterY() - firstVertex.getCenterY()},
                {circlePos.getX() - firstVertex.getCenterX(), circlePos.getY() - firstVertex.getCenterY()},
        });
        return sweepMtx.det() < 0;
    }

    private boolean calculateLargeFlag(Vector2D circlePos) {
        double vx1 = firstVertex.getCenterX() - circlePos.getX(), vy1 = firstVertex.getCenterY() - circlePos.getY(),
               vx2 = secondVertex.getCenterX() - circlePos.getX(), vy2 = secondVertex.getCenterY() - circlePos.getY();
        double cos_A = (vx1*vx2 + vy1*vy2) / Math.sqrt((vx1*vx1+vy1*vy1) * (vx2*vx2+vy2*vy2));
        return cos_A >= 0;
    }

    public void update() {
        Vector2D circleCenter = calculateCircleCenter();
        double radius = calculateArcRadius(circleCenter);

        circle.setCenterX(circleCenter.getX());
        circle.setCenterY(circleCenter.getY());

        moveTo.setX(firstVertex.getCenterX());
        moveTo.setY(firstVertex.getCenterY());

        arcTo.setRadiusX(radius);
        arcTo.setRadiusY(radius);
        arcTo.setX(secondVertex.getCenterX());
        arcTo.setY(secondVertex.getCenterY());
        arcTo.setSweepFlag(calculateSweepFlag(circleCenter));
        arcTo.setLargeArcFlag(calculateLargeFlag(circleCenter));
    }

    public void move(double x, double y) {
        //validate x, y
        if (x < radiusCircle)
            x = radiusCircle;
        else if (x > GraphGroup.width - radiusCircle)
            x = GraphGroup.width - radiusCircle;
        if (y < radiusCircle)
            y = radiusCircle;
        else if (y > GraphGroup.height - radiusCircle)
            y = GraphGroup.height - radiusCircle;

        // TODO pushing out

        pointRadiusCoef = Math.sqrt(Math.pow(x - firstVertex.getCenterX(), 2) + Math.pow(y - firstVertex.getCenterY(), 2)) /
            Math.sqrt(Math.pow(secondVertex.getCenterX() - firstVertex.getCenterX(), 2) + Math.pow(secondVertex.getCenterY() - firstVertex.getCenterY(), 2));
        pointAngle = Math.atan2(x - firstVertex.getCenterX(), y - firstVertex.getCenterY()) -
                     Math.atan2(secondVertex.getCenterX() - firstVertex.getCenterX(), secondVertex.getCenterY() - firstVertex.getCenterY());
        update();
    }

    public void disconnectVertexes() {
        firstVertex.removeIncidentEdge(this);
        secondVertex.removeIncidentEdge(this);
    }
}
