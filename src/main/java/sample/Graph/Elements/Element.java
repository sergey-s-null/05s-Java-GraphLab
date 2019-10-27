package sample.Graph.Elements;

import javafx.scene.Group;

public abstract class Element extends Group {

    public abstract void setSelectedAsPath(boolean flag);

    public abstract void setSelected(boolean flag);
}
