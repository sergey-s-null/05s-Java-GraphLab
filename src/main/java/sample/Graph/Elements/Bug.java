package sample.Graph.Elements;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.geometry.VPos;
import javafx.scene.Group;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import org.apache.commons.math3.geometry.euclidean.twod.Vector2D;

public class Bug extends Group {
    private Text text = new Text("Муха");
    private Vertex vertex = null;
    private ChangeListener<? super Vector2D> onVertexPosChanged =
            (obs, oldVal, newVal) -> updatePosition(newVal);

    public Bug(Vertex vertex) {
        this();
        connect(vertex);
    }

    public Bug() {
        text.setTextAlignment(TextAlignment.CENTER);
        text.setTextOrigin(VPos.CENTER);
        text.setFill(Style.bugColor);
        text.setStroke(Style.bugColor);
        text.setStrokeWidth(Style.spiderStrokeWidth);

        getChildren().add(text);
    }

    public void connect(Vertex vertex) {
        disconnect();
        this.vertex = vertex;
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
        text.setX(vertexPos.getX() - text.getLayoutBounds().getWidth() / 2);
        text.setY(vertexPos.getY() + Style.spiderVerticalShift);
    }
}
