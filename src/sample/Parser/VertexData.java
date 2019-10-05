package sample.Parser;


public class VertexData {
    private String name;
    private double x, y;

    public VertexData(String name, double x, double y) {
        this.name = name;
        this.x = x;
        this.y = y;
    }

    public VertexData(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return name + " " + x + " " + y;
    }

    public String getName() {
        return name;
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }
}
