package sample.Parser;

import sample.Graph.GraphGroup;

public class Resolution {
    private double width, height;

    public Resolution(double width, double height) {
        this.width = width;
        this.height = height;
    }

    public Resolution() {
        width = GraphGroup.width;
        height = GraphGroup.height;
    }

    public double getWidth() {
        return width;
    }

    public double getHeight() {
        return height;
    }
}
