package sample;

import Jama.Matrix;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.apache.commons.math3.exception.MathArithmeticException;
import org.apache.commons.math3.geometry.euclidean.twod.Vector2D;


public class Main extends Application {

    public static Vector2D rotate(Vector2D vector, double angle) {
        Matrix rotateMatrix = new Matrix(new double[][] {
                {Math.cos(angle), Math.sin(angle)},
                {-Math.sin(angle), Math.cos(angle)},
        });
        Matrix vectorMatrix = new Matrix(new double[][] {
                {vector.getX()},
                {vector.getY()},
        });
        Matrix res = rotateMatrix.times(vectorMatrix);
        return new Vector2D(res.getArray()[0][0], res.getArray()[1][0]);
    }

    public static Vector2D normalizeOrZero(Vector2D vector) {
        try {
            return vector.normalize();
        }
        catch (MathArithmeticException e) {
            return new Vector2D(0, 0);
        }
    }

    @Override
    public void start(Stage primaryStage) throws Exception{
        FXMLLoader loader = new FXMLLoader(getClass().getResource("sample.fxml"));
        Parent root = loader.load();


        Controller controller = loader.getController();
        controller.init();


        primaryStage.setTitle("Графойд by Laiser399   ( ͡° ͜ʖ ͡°)");
        primaryStage.setScene(new Scene(root, 300, 275));
        primaryStage.show();

        primaryStage.setMinWidth(600);
        primaryStage.setMinHeight(520);
        
    }


    public static void main(String[] args) {

        launch(args);
    }
}
