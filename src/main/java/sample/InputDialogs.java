package sample;

import javafx.scene.control.TextInputDialog;
import sample.Graph.Elements.Vertex;
import sample.Graph.GraphGroup;

public class InputDialogs {
    private static TextInputDialog dialog = new TextInputDialog();

    static Double getGraphWidth(double initWidth) {
        dialog.setTitle("¯\\_(ツ)_/¯");
        dialog.setHeaderText("Введите ширину поля");
        dialog.setContentText("Ширина:");
        dialog.getEditor().setText(Double.toString(initWidth));

        while (true) {
            dialog.showAndWait();
            String res = dialog.getResult();
            if (res == null)
                return null;

            try {
                double doubleRes = Double.parseDouble(res);
                if (isValidGraphWidth(doubleRes)) {
                    return doubleRes;
                }
                else {
                    GraphAlert.showErrorAndWait("Ширина должна быть в диапазоне от " +
                            GraphGroup.minWidth + " до " + GraphGroup.maxWidth + ".");
                }
            }
            catch (NumberFormatException e) {
                GraphAlert.showErrorAndWait("Неверный формат числа.");
            }
        }
    }

    private static boolean isValidGraphWidth(double width) {
        return width >= GraphGroup.minWidth && width <= GraphGroup.maxWidth;
    }

    public static Double getGraphHeight(double initHeight) {
        dialog.setTitle("༼ つ ◕_◕ ༽つ");
        dialog.setHeaderText("Введите высоту поля");
        dialog.setContentText("Высота:");
        dialog.getEditor().setText(Double.toString(initHeight));

        while (true) {
            dialog.showAndWait();
            String res = dialog.getResult();
            if (res == null)
                return null;

            try {
                double doubleRes = Double.parseDouble(res);
                if (isValidGraphHeight(doubleRes)) {
                    return doubleRes;
                }
                else {
                    GraphAlert.showErrorAndWait("Высота должна быть в диапазоне от " +
                            GraphGroup.minHeight + " до " + GraphGroup.maxHeight + ".");
                }
            }
            catch (NumberFormatException e) {
                GraphAlert.showErrorAndWait("Неверный формат числа.");
            }
        }
    }

    private static boolean isValidGraphHeight(double height) {
        return height >= GraphGroup.minHeight && height <= GraphGroup.maxHeight;
    }

    // TODO отдельный диалог с двумя полями
    public static String getTabText(String initText) {
        dialog.setTitle("┌( ಠ_ಠ)┘");
        dialog.setHeaderText("Введите название вкладки");
        dialog.setContentText("Название:");
        dialog.getEditor().setText(initText);

        while (true) {
            dialog.showAndWait();
            String res = dialog.getResult();
            if (res == null)
                return null;

            if (GraphTab.isValidTabText(res)) {
                return res;
            }
            else {
                GraphAlert.showErrorAndWait("Длина названия от 1 до 16 символов.");
            }
        }
    }

    public static String getVertexName(String defaultName) {
        dialog.setTitle("Что тут обычно пишется?");
        dialog.setHeaderText("Введите имя вершины (длина от 1 до 10, запрещенные символы: \"%()[]{},\" и пробельные)");
        dialog.setContentText("Имя:");
        dialog.getEditor().setText(defaultName);

        while (true) {
            dialog.showAndWait();
            String res = dialog.getResult();
            if (res == null)
                return null;

            if (!Vertex.isNameValid(res)) {
                GraphAlert.showErrorAndWait("Неверное имя.");
                continue;
            }
            return res;
        }
    }

    public static Double getEdgeWeight(double defaultWeight) {
        dialog.setTitle("Так и не понял, что тут писать.");
        dialog.setHeaderText("Введите вес ребра");
        dialog.setContentText("Вес:");
        dialog.getEditor().setText(Double.toString(defaultWeight));

        while (true) {
            dialog.showAndWait();
            String res = dialog.getResult();
            if (res == null)
                return null;

            try {
                double weight = Double.parseDouble(res);
                if (weight == 0)
                    GraphAlert.showErrorAndWait("Вес не может быть равен 0.");
                else
                    return weight;
            }
            catch (NumberFormatException e) {
                GraphAlert.showErrorAndWait("Неверный формат числа.");
            }
        }
    }




}
