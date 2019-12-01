package sample;

import Jama.Matrix;
import javafx.application.Application;
import javafx.collections.transformation.SortedList;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.apache.commons.math3.exception.MathArithmeticException;
import org.apache.commons.math3.geometry.euclidean.twod.Vector2D;

import java.io.IOException;
import java.util.*;


public class Main extends Application {
    // static функции, не нашедшии себе места, поэтому я их приютил тут
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


    static Random random = new Random();
    static class Test {
        public double value = random.nextDouble();

        @Override
        public String toString() {
            return "Test[" + value + "]";
        }
    }

    public static void main(String[] args) {
//        Queue<Test> queue = new PriorityQueue<>(Comparator.comparingDouble(o -> o.value));
//        for (int i = 0; i < 10; ++i)
//            queue.add(new Test());
//
//
//
//        System.out.println(queue);
//        Test test = queue.element();
//        test.value = 10;
//        queue.add(test);
//        System.out.println(queue);
//
//
//        System.exit(0);

        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/main.fxml"));
        Parent root = loader.load();

        primaryStage.setTitle("Графойд by Laiser399   ( ͡° ͜ʖ ͡°)");
        primaryStage.setScene(new Scene(root, 300, 275));
        primaryStage.show();

        primaryStage.setMinWidth(800);
        primaryStage.setMinHeight(700);

        MainController controller = loader.getController();
        primaryStage.setOnCloseRequest(event -> {
            controller.onExit();
            event.consume();
        });
    }
}
