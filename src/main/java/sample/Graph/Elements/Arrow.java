package sample.Graph.Elements;

import javafx.scene.shape.LineTo;
import javafx.scene.shape.MoveTo;
import javafx.scene.shape.Path;
import org.apache.commons.math3.geometry.euclidean.twod.Vector2D;
import sample.Main;

public class Arrow extends Path {
    private MoveTo moveToFirstTail = new MoveTo();
    private LineTo lineToCenter = new LineTo(),
                   lineToSecondTail = new LineTo();

    public Arrow() {
        getElements().addAll(moveToFirstTail, lineToCenter, lineToSecondTail);
        setPosition(new Vector2D(0, 0), new Vector2D(0, 1));

        setStroke(Style.lineColor);
        setStrokeWidth(Style.lineWidth);
    }

    public void setPosition(Vector2D position, Vector2D directionVector) {
        Vector2D baseTail = Main.normalizeOrZero(directionVector).scalarMultiply(-Style.arrowTailsLength);
        Vector2D firstTailPos = Main.rotate(baseTail, Style.arrowTailsRotateAngle).add(position);
        Vector2D secondTailPos = Main.rotate(baseTail, -Style.arrowTailsRotateAngle).add(position);

        moveToFirstTail.setX(firstTailPos.getX());
        moveToFirstTail.setY(firstTailPos.getY());
        lineToCenter.setX(position.getX());
        lineToCenter.setY(position.getY());
        lineToSecondTail.setX(secondTailPos.getX());
        lineToSecondTail.setY(secondTailPos.getY());
    }
}
