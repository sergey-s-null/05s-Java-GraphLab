package sample.Graph.Elements;


import Jama.Matrix;
import javafx.beans.value.ObservableValue;
import javafx.scene.shape.*;
import org.apache.commons.math3.geometry.euclidean.twod.Vector2D;
import sample.Graph.GraphGroup;
import sample.Main;


public class BinaryEdge extends Edge {
    private static final double defaultArcRadius = 20000;

    private final Vertex firstVertex, secondVertex;
    private Arrow firstArrow = new Arrow(), secondArrow = new Arrow();
    private double pointAngle = 0, pointRadiusCoef = 0.5;

    // constructors
    public BinaryEdge(GraphGroup graphGroup, Vertex firstVertex, Vertex secondVertex) {
        super(graphGroup);

        this.firstVertex = firstVertex;
        this.secondVertex = secondVertex;

        initArc();
        initCircle();
        initWeight();
        getChildren().addAll(arc, circle, firstArrow, secondArrow, weightText);
        update();

        connect();
        weight.addListener((obj, prevValuew, newValue) -> {
            changedWeight();
        });
        direction.addListener((observable, oldValue, newValue) -> {
            changedDirection(newValue);
        });

        setDirection(Direction.SecondVertex, false);
    }

    //
    @Override
    public boolean hasDirectionTo(Vertex vertex) {
        switch (direction.getValue()) {
            case FirstVertex:
                return firstVertex == vertex;
            case SecondVertex:
                return secondVertex == vertex;
            case Both:
                return firstVertex == vertex || secondVertex == vertex;
            default:
                return false;
        }
    }

    public double getPointAngle() {
        return pointAngle;
    }

    public double getPointRadiusCoef() {
        return pointRadiusCoef;
    }

    @Override
    public void setSelected(boolean flag) {
        //ignore
    }

    @Override
    public void setSelectedAsPath(boolean flag) {
        if (flag) {
            arc.setStroke(Style.pathColor);
            circle.setStroke(Style.pathColor);
            firstArrow.setStroke(Style.pathColor);
            secondArrow.setStroke(Style.pathColor);
        }
        else {
            arc.setStroke(Style.lineColor);
            circle.setStroke(Style.lineColor);
            firstArrow.setStroke(Style.lineColor);
            secondArrow.setStroke(Style.lineColor);
        }
    }

    // updates
    @Override
    public void update() {
        Vector2D circleCenter = calculateCircleCenter();
        Vector2D arcCenter = calculateArcCenter(circleCenter);
        if (arcCenter != null && arcCenter.distance(circleCenter) > defaultArcRadius) arcCenter = null;
        double arcRadius = arcCenter == null ? defaultArcRadius : circleCenter.subtract(arcCenter).getNorm();

        boolean sweepFlag = calculateSweepFlag(circleCenter),
                largeFlag = calculateLargeFlag(circleCenter);

        updateCircle(circleCenter);
        updateArrows(arcRadius, arcCenter, sweepFlag);
        updateArc(arcRadius, sweepFlag, largeFlag);
        updateWeight(circleCenter);
    }
    //    calculates
    private Vector2D calculateCircleCenter() {
        Vector2D afterRotate = Main.rotate(new Vector2D(secondVertex.getCenterX() - firstVertex.getCenterX(),
                secondVertex.getCenterY() - firstVertex.getCenterY()), pointAngle);
        return afterRotate.scalarMultiply(pointRadiusCoef).add(new Vector2D(firstVertex.getCenterX(), firstVertex.getCenterY()));
    }

    private Vector2D calculateArcCenter(Vector2D circlePos) {
        // пересечение двух прямых - центр окружности
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

    private void updateCircle(Vector2D circlePos) {
        circle.setCenterX(circlePos.getX());
        circle.setCenterY(circlePos.getY());
    }

    private void updateArrows(double arcRadius, Vector2D arcCenter, boolean sweepFlag) {
        if (arcCenter == null) {
            updateArrow(firstArrow, firstVertex, secondVertex);
            updateArrow(secondArrow, secondVertex, firstVertex);
        }
        else {
            double cos_A = 1 - 0.5 * Math.pow(Style.vertexCircleRadius / arcRadius, 2);
            double angle = sweepFlag ? -Math.acos(cos_A) : Math.acos(cos_A); //between vertex center and arrow position relatively arc center

            updateArrow(firstVertex, firstArrow, arcCenter, angle);
            updateArrow(secondVertex, secondArrow, arcCenter, -angle);
        }
    }

    private void updateArrow(Arrow arrow, Vertex owner, Vertex another) {
        Vector2D anotherToOwner = owner.getCenterPos().subtract(another.getCenterPos());
        Vector2D arrowPos = Main.normalizeOrZero(anotherToOwner).scalarMultiply(-Style.vertexCircleRadius).
                add(owner.getCenterPos());
        arrow.setPosition(arrowPos, anotherToOwner);
    }

    private void updateArrow(Vertex vertex, Arrow arrow, Vector2D arcCenter, double angle) {
        if (arcCenter == null)
            return;

        Vector2D vertexPos = vertex.getCenterPos();
        Vector2D arrowPos = Main.rotate(vertexPos.subtract(arcCenter), angle).add(arcCenter);
        Vector2D directionVector = arrowPos.subtract(vertexPos).scalarMultiply(-1);
        arrow.setPosition(arrowPos, directionVector);
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

    private void updateWeight(Vector2D circleCenter) {
        weightText.setText(Double.toString(weight.get()));
        weightText.setX(circleCenter.getX() - weightText.getLayoutBounds().getWidth() / 2);
        weightText.setY(circleCenter.getY());
    }

    // position
    @Override
    public void setPosition(double x, double y) {
        //validate x, y
        if (x < Style.edgeCircleRadius)
            x = Style.edgeCircleRadius;
        else if (x > graphGroup.getWidth() - Style.edgeCircleRadius)
            x = graphGroup.getWidth() - Style.edgeCircleRadius;
        if (y < Style.edgeCircleRadius)
            y = Style.edgeCircleRadius;
        else if (y > graphGroup.getHeight() - Style.edgeCircleRadius)
            y = graphGroup.getHeight() - Style.edgeCircleRadius;

        pointRadiusCoef = Math.sqrt(Math.pow(x - firstVertex.getCenterX(), 2) + Math.pow(y - firstVertex.getCenterY(), 2)) /
            Math.sqrt(Math.pow(secondVertex.getCenterX() - firstVertex.getCenterX(), 2) + Math.pow(secondVertex.getCenterY() - firstVertex.getCenterY(), 2));
        pointAngle = Math.atan2(x - firstVertex.getCenterX(), y - firstVertex.getCenterY()) -
                     Math.atan2(secondVertex.getCenterX() - firstVertex.getCenterX(), secondVertex.getCenterY() - firstVertex.getCenterY());
        update();
    }

    public void setPositionBy(double pointAngle, double pointRadiusCoef) {
        this.pointAngle = pointAngle;
        this.pointRadiusCoef = pointRadiusCoef;
        update();
    }

    // vertices
    @Override
    public Vertex getFirstVertex() {
        return firstVertex;
    }

    @Override
    public Vertex getSecondVertex() {
        return secondVertex;
    }

    @Override
    public Vertex hasDirectionFrom(Vertex vertexFrom) {
        if (firstVertex == vertexFrom) {
            return hasDirectionTo(secondVertex) ? secondVertex : null;
        }
        else if (secondVertex == vertexFrom) {
            return hasDirectionTo(firstVertex) ? firstVertex : null;
        }
        else {
            throw new RuntimeException(this + ": received vertex is not equals any edge's vertices.");
        }
    }

    @Override
    public void connect() {
        setOnMouseClicked(graphGroup::onMouseClick_edge);
        setOnMousePressed(graphGroup::onMousePress_edge);

        firstVertex.addIncidentEdge(this);
        secondVertex.addIncidentEdge(this);
        firstVertex.positionObservable().addListener(this::changedVertexPosition);
        secondVertex.positionObservable().addListener(this::changedVertexPosition);
    }

    @Override
    public void disconnect() {
        setOnMouseClicked(null);
        setOnMousePressed(null);

        firstVertex.removeIncidentEdge(this);
        secondVertex.removeIncidentEdge(this);
        firstVertex.positionObservable().removeListener(this::changedVertexPosition);
        secondVertex.positionObservable().removeListener(this::changedVertexPosition);
    }

    // events
    private void changedVertexPosition(ObservableValue<? extends Vector2D> obs, Vector2D oldValue, Vector2D newValue) {
        update();
    }

    private void changedWeight() {
        updateWeight(new Vector2D(circle.getCenterX(), circle.getCenterY()));
    }

    private void changedDirection(Direction direction) {
        switch (direction) {
            case Both:
                firstArrow.setVisible(false);
                secondArrow.setVisible(false);
                break;
            case FirstVertex:
                firstArrow.setVisible(true);
                secondArrow.setVisible(false);
                break;
            case SecondVertex:
                firstArrow.setVisible(false);
                secondArrow.setVisible(true);
                break;
        }
    }


}
