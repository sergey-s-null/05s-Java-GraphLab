package sample.Graph.Elements;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.geometry.VPos;
import javafx.scene.Group;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import org.apache.commons.math3.geometry.euclidean.twod.Vector2D;

public class Spider extends Group {
    private Text text = new Text("Паук");
    private Vertex vertex = null;
    private boolean isBug = false;
    private ChangeListener<? super Vector2D> onVertexPosChanged =
            (obs, oldVal, newVal) -> updatePosition(newVal);

    public Spider() {
        text.setTextAlignment(TextAlignment.CENTER);
        text.setTextOrigin(VPos.CENTER);
        text.setFill(Style.spiderColor);
        text.setStroke(Style.spiderColor);
        text.setStrokeWidth(Style.spiderStrokeWidth);

        getChildren().add(text);
    }

    public void connect(Vertex vertex) {
        disconnect();
        this.vertex = vertex;
        isBug = vertex.isBug();
        vertex.positionObservable().addListener(onVertexPosChanged);
        updatePosition(vertex.positionObservable().getValue());
    }

    public void disconnect() {
        if (vertex != null) {
            vertex.positionObservable().removeListener(onVertexPosChanged);
            vertex = null;
        }
    }

    private void updatePosition(Vector2D vertexPos) {
        double y;
        if (isBug)
            y = vertexPos.getY() - Style.spiderVerticalShift;
        else
            y = vertexPos.getY() + Style.spiderVerticalShift;

        text.setX(vertexPos.getX() - text.getLayoutBounds().getWidth() / 2);
        text.setY(y);
    }



}
