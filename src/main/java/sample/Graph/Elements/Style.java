package sample.Graph.Elements;

import javafx.scene.paint.Color;

public class Style {
    //common
    static final double lineWidth = 2;
    static final Color lineColor = Color.web("BCBABE");
    static final Color textColor = Color.web("086070");
    static final Color pathColor = Color.web("C38864");

    //edge
    static final double edgeCircleRadius = 8;
    static final double weightStrokeWidth = 0.3;
    static final Color edgeFillColor = Color.web("F1F1F2");
    //   arrow
    static final double arrowTailsLength = 9, arrowTailsRotateAngle = 0.4;
    //vertex
    static final double vertexNameStrokeWidth = 0.5;
    static final double vertexCircleRadius = 12;
    static final Color vertexFillColor = Color.web("A1D6E2");
    static final Color vertexSelectColor = Color.ORANGE;

    //Glyph
    public static final Color glyphSelectColor = vertexSelectColor;

    //Spider and bugs
    static final Color spiderColor = Color.web("65009c");
    static final Color bugColor = Color.web("2d2c38");
    static final double spiderStrokeWidth = 0.6;
    static final double spiderVerticalShift = -22;

}
