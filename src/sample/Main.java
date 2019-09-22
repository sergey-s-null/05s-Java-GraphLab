package sample;

import Jama.Matrix;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
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

    @Override
    public void start(Stage primaryStage) throws Exception{
        FXMLLoader loader = new FXMLLoader(getClass().getResource("sample.fxml"));
        Parent root = loader.load();


        Controller controller = loader.getController();
        controller.init();


        primaryStage.setTitle("Hello World");
        primaryStage.setScene(new Scene(root, 300, 275));
        primaryStage.show();


    }


    public static void main(String[] args) {
//        Matrix a = new Matrix(new double[][] {
//                {3, 2},
//                {-2, 1},
//        });
//        Matrix b = new Matrix(new double[][] {
//                {1},
//                {4},
//        });
//
//        Matrix res = a.solve(b);
//        for (double[] arr : res.getArray()) {
//            for (double val : arr) {
//                System.out.print(val + "   ");
//            }
//            System.out.println();
//        }
//        Matrix a = new Matrix(new double[][] {
//                {3, 2},
//                {-6, -4},
//        });
//        Matrix b = new Matrix(new double[][] {
//                {0},
//                {1},
//        });
//
//
//        try {
//            Matrix res = a.solve(b);
//            for (double[] arr : res.getArray()) {
//                for (double val : arr) {
//                    System.out.print(val + "   ");
//                }
//                System.out.println();
//            }
//        }
//        catch (RuntimeException e) {
//            System.out.println(e.getMessage());
//        }





        launch(args);
    }
}
