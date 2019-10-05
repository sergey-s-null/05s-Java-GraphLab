package sample;

import Jama.Matrix;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.apache.commons.math3.exception.MathArithmeticException;
import org.apache.commons.math3.geometry.euclidean.twod.Vector2D;

import java.util.regex.Matcher;
import java.util.regex.Pattern;


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


        primaryStage.setTitle("Графойд by Laiser399");
        primaryStage.setScene(new Scene(root, 300, 275));
        primaryStage.show();

        primaryStage.setMinWidth(600);
        primaryStage.setMinHeight(520);
        
    }


    public static void main(String[] args) {
//        String testStr1 = "[1,2.4,4],[-2,3,-98.9]";
//        Pattern pattern1 = Pattern.compile("(\\[([^]]+)],?){1,3}");
//        Matcher matcher1 = pattern1.matcher(testStr1);
//        if (matcher1.matches()) {
//            for (int i = 0; i <= matcher1.groupCount(); ++i) {
//                System.out.println(matcher1.group(i));
//            }
//        }


//        String testStr = "[1,2.5,-3],[444,2.23],";
//        Pattern pattern = Pattern.compile("\\[([^]]+)],?");
//        Matcher matcher = pattern.matcher(testStr);
//
//        int start = 0;
//        while (matcher.find(start)) {
//            for (int i = 0; i <= matcher.groupCount(); ++i) {
//                System.out.println("" + i + ": " + matcher.group(i));
//            }
//            start = matcher.end();
//        }

        launch(args);
    }
}
