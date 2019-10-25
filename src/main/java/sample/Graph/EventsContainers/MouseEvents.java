package sample.Graph.EventsContainers;

import javafx.scene.input.MouseEvent;

import java.util.function.Consumer;

public class MouseEvents {
    protected Consumer<MouseEvent>
              onGraphGroupClick,
              onLeftClickVertex, onRightClickVertex,
              onLeftClickEdge, onRightClickEdge,
              onPressVertex,
              onPressEdge,
              onGraphGroupDrag,
              onGraphGroupRelease;

    public MouseEvents() {
        clearEvents();
    }

    protected void clearEvents() {
        onGraphGroupClick = (event) -> {};
        onLeftClickVertex = (event) -> {};
        onRightClickVertex = (event) -> {};
        onLeftClickEdge = (event) -> {};
        onRightClickEdge = (event) -> {};
        onPressVertex = (event) -> {};
        onPressEdge = (event) -> {};
        onGraphGroupDrag = (event) -> {};
        onGraphGroupRelease = (event) -> {};
    }

    public void deactivate() {}

    public void callOnClickEvent(MouseEvent event) {
        onGraphGroupClick.accept(event);
    }

    public void callOnLeftClickVertex(MouseEvent event) {
        onLeftClickVertex.accept(event);
    }

    public void callOnRightClickVertex(MouseEvent event) {
        onRightClickVertex.accept(event);
    }

    public void callOnLeftClickEdge(MouseEvent event) {
        onLeftClickEdge.accept(event);
    }

    public void callOnRightClickEdge(MouseEvent event) {
        onRightClickEdge.accept(event);
    }

    public void callOnPressVertex(MouseEvent event) {
        onPressVertex.accept(event);
    }

    public void callOnPressEdge(MouseEvent event) {
        onPressEdge.accept(event);
    }

    public void callOnDrag(MouseEvent event) {
        onGraphGroupDrag.accept(event);
    }

    public void callOnRelease(MouseEvent event) {
        onGraphGroupRelease.accept(event);
    }
}
