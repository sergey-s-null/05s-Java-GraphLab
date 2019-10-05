package sample.Parser;

public class EdgeData {
    private String vertexName1, vertexName2;
    private double weight;
    private int direction;
    private boolean binary;

    public EdgeData(String vertexName1, String vertexName2,
                    double weight, int direction)
    {
        binary = true;
        this.vertexName1 = vertexName1;
        this.vertexName2 = vertexName2;
        this.weight = weight;
        this.direction = direction;
    }

    public EdgeData(String vertexName, double weight) {
        binary = false;
        this.vertexName1 = vertexName;
        this.vertexName2 = vertexName;
        this.weight = weight;
        this.direction = 1;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append(vertexName1);
        if (binary) {
            builder.append(" ");
            builder.append(vertexName2);
        }
        builder.append(" ");
        builder.append(weight);
        if (binary) {
            builder.append(" ");
            builder.append(direction);
        }
        return builder.toString();
    }

    public boolean isBinary() {
        return binary;
    }

    public String getVertexName1() {
        return vertexName1;
    }

    public String getVertexName2() {
        return vertexName2;
    }

    public double getWeight() {
        return weight;
    }

    public int getDirection() {
        return direction;
    }
}
