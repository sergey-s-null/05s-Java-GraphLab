package sample.Graph.Elements;


import Jama.Matrix;
import javafx.scene.Group;
import javafx.scene.paint.Color;
import javafx.scene.shape.*;
import org.apache.commons.math3.exception.MathArithmeticException;
import org.apache.commons.math3.geometry.euclidean.twod.Vector2D;
import sample.Graph.GraphGroup;
import sample.Main;


public class Edge extends Group {
    private static final double radiusCircle = 8, strokeWidthCircle = 2,
            strokeWidth = 2,
            defaultArcRadius = 20000,
            arrowLength = 9, arrowRotateAngle = 0.4;

    private Vertex firstVertex, secondVertex;
    private Path firstArrow = new Path(new MoveTo(), new LineTo(), new LineTo()),
                 secondArrow = new Path(new MoveTo(), new LineTo(), new LineTo());
    private Path arc = new Path(new MoveTo(), new ArcTo());
    private Circle circle = new Circle(radiusCircle);

    private double pointAngle = 0, pointRadiusCoef = 0.5;

    private void initArc() {
        arc.setStrokeWidth(strokeWidth);
    }

    private void initArrows() {
        firstArrow.setStrokeWidth(strokeWidth);
        secondArrow.setStrokeWidth(strokeWidth);
    }

    private void initCircle() {
        circle.setFill(Color.RED);
        circle.setStrokeWidth(strokeWidthCircle);
        circle.setStroke(Color.BLACK);
    }

    public Edge(GraphGroup graphGroup, Vertex firstVertex, Vertex secondVertex) {
        super();

        this.firstVertex = firstVertex;
        this.secondVertex = secondVertex;
        this.firstVertex.addIncidentEdge(this);
        this.secondVertex.addIncidentEdge(this);

        initArc();
        initArrows();
        initCircle();
        getChildren().addAll(arc, circle, firstArrow, secondArrow);
        update();

        setOnMouseClicked(graphGroup::onMouseClick_edge);
        setOnMousePressed(graphGroup::onMousePress_edge);
    }

    private Vector2D calculateCircleCenter() {
        Vector2D afterRotate = Main.rotate(new Vector2D(secondVertex.getCenterX() - firstVertex.getCenterX(),
                secondVertex.getCenterY() - firstVertex.getCenterY()), pointAngle);
        return afterRotate.scalarMultiply(pointRadiusCoef).add(new Vector2D(firstVertex.getCenterX(), firstVertex.getCenterY()));
    }

    private Vector2D calculateArcCenter(Vector2D circlePos) {
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
            return new Vector2D(res.getArray()[0][0], res.getArray()[1][0]);
        }
        catch (RuntimeException e) {
            //System.out.println("Ahh shit, here we go again...");
            return null;
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

    private void updateArc(double arcRadius, boolean sweepFlag, boolean largeFlag) {
        MoveTo moveTo = (MoveTo)arc.getElements().get(0);
        moveTo.setX(firstVertex.getCenterX());
        moveTo.setY(firstVertex.getCenterY());

        ArcTo arcTo = (ArcTo)arc.getElements().get(1);
        arcTo.setRadiusX(arcRadius);
        arcTo.setRadiusY(arcRadius);
        arcTo.setX(secondVertex.getCenterX());
        arcTo.setY(secondVertex.getCenterY());
        arcTo.setSweepFlag(sweepFlag);
        arcTo.setLargeArcFlag(largeFlag);
    }

    private void updateArrow(Path arrow, Vertex owner, Vertex another) {
        // TODO think about upgrade this + updateArrow(second)
        Vector2D ownerToAnotherNormal = null;
        try {
            ownerToAnotherNormal = new Vector2D(another.getCenterX() - owner.getCenterX(),
                    another.getCenterY() - owner.getCenterY()).normalize();
        }
        catch (MathArithmeticException e) {
            return;
        }

        Vector2D arrowPos = new Vector2D(owner.getCenterX(), owner.getCenterY())
                .add(ownerToAnotherNormal.scalarMultiply(Vertex.radius));

        Vector2D baseTail = ownerToAnotherNormal.scalarMultiply(arrowLength);
        Vector2D tail1Pos = Main.rotate(baseTail, arrowRotateAngle).add(arrowPos);
        Vector2D tail2Pos = Main.rotate(baseTail, -arrowRotateAngle).add(arrowPos);

        MoveTo moveTo = (MoveTo)arrow.getElements().get(0);
        LineTo lineToArrowPos = (LineTo)arrow.getElements().get(1);
        LineTo lineTo = (LineTo)arrow.getElements().get(2);

        moveTo.setX(tail1Pos.getX());
        moveTo.setY(tail1Pos.getY());
        lineToArrowPos.setX(arrowPos.getX());
        lineToArrowPos.setY(arrowPos.getY());
        lineTo.setX(tail2Pos.getX());
        lineTo.setY(tail2Pos.getY());
    }

    private void updateArrow(Vertex vertex, Path arrow, Vector2D arcCenter, double angle) {
        if (arcCenter == null)
            return;

        Vector2D vertexPos = new Vector2D(vertex.getCenterX(), vertex.getCenterY());
        Vector2D arrowPos = Main.rotate(vertexPos.subtract(arcCenter), angle).
                add(arcCenter);
        Vector2D baseTail = null;
        try {
            baseTail = arrowPos.subtract(vertexPos).normalize().scalarMultiply(arrowLength);
        }
        catch (MathArithmeticException e) {
            return;
        }
        Vector2D tail1Pos = Main.rotate(baseTail, arrowRotateAngle).add(arrowPos);
        Vector2D tail2Pos = Main.rotate(baseTail, -arrowRotateAngle).add(arrowPos);

        MoveTo moveTo = (MoveTo)arrow.getElements().get(0);
        LineTo lineToArrowPos = (LineTo)arrow.getElements().get(1);
        LineTo lineTo = (LineTo)arrow.getElements().get(2);

        moveTo.setX(tail1Pos.getX());
        moveTo.setY(tail1Pos.getY());
        lineToArrowPos.setX(arrowPos.getX());
        lineToArrowPos.setY(arrowPos.getY());
        lineTo.setX(tail2Pos.getX());
        lineTo.setY(tail2Pos.getY());
    }

    private void updateArrows(double arcRadius, Vector2D arcCenter, boolean sweepFlag) {
        if (arcCenter == null) {
            updateArrow(firstArrow, firstVertex, secondVertex);
            updateArrow(secondArrow, secondVertex, firstVertex);
        }
        else {
            double cos_A = 1 - 0.5 * Math.pow(Vertex.radius / arcRadius, 2);
            double angle = sweepFlag ? -Math.acos(cos_A) : Math.acos(cos_A); //between vertex center and arrow position relatively arc center

            updateArrow(firstVertex, firstArrow, arcCenter, angle);
            updateArrow(secondVertex, secondArrow, arcCenter, -angle);
        }
    }

    private void updateCircle(Vector2D circlePos) {
        circle.setCenterX(circlePos.getX());
        circle.setCenterY(circlePos.getY());
    }

    public void update() {
        Vector2D circleCenter = calculateCircleCenter();
        Vector2D arcCenter = calculateArcCenter(circleCenter);
        double arcRadius = arcCenter == null ? defaultArcRadius : circleCenter.subtract(arcCenter).getNorm();
        boolean sweepFlag = calculateSweepFlag(circleCenter),
                largeFlag = calculateLargeFlag(circleCenter);

        updateCircle(circleCenter);
        updateArrows(arcRadius, arcCenter, sweepFlag);
        updateArc(arcRadius, sweepFlag, largeFlag);
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
