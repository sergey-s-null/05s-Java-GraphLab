package sample.tasksControllers;

import javafx.fxml.Initializable;
import javafx.scene.Parent;
import sample.Graph.GraphGroup;

import java.net.URL;
import java.util.ResourceBundle;

public class GraphCharacteristicController extends TaskController implements Initializable {

    @Override
    public void initialize(URL location, ResourceBundle resources) {

    }

    @Override
    public Parent getRoot() {
        return null;
    }

    @Override
    public boolean validateGraph(GraphGroup graphGroup) {
        return false;
    }
}
