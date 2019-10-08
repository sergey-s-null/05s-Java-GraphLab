package sample.Parser;

import Jama.Matrix;
import org.apache.commons.math3.geometry.euclidean.twod.Vector2D;
import sample.Graph.GraphGroup;
import sample.Main;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class InputFileParser {
    private class TypesAndContents {
        List<String> types, contents;

        private TypesAndContents(List<String> types, List<String> contents) {
            this.types = types;
            this.contents = contents;
        }

        private List<String> getTypes() {
            return types;
        }

        private List<String> getContents() {
            return contents;
        }
    }

    public GraphData parseAdjacencyFile(String filename) throws Exception {
        TypesAndContents typesAndContents = parseFileForTypesAndContents(filename);
        List<String> types = typesAndContents.getTypes(),
                contents = typesAndContents.getContents();

        Matrix matrix = null;
        VerticesData verticesData = null;
        Resolution resolution = null;
        for (int i = 0; i < types.size(); ++i) {
            switch (types.get(i)) {
                case "matrix":
                    if (matrix != null)
                        throw new Exception("Double \"Matrix\" definition.");
                    matrix = parseMatrix(contents.get(i));
                    break;
                case "vertices":
                    if (verticesData != null)
                        throw new Exception("Double \"Vertices\" definition.");
                    verticesData = parseVertices(contents.get(i));
                    break;
                case "resolution":
                    if (resolution != null)
                        throw new Exception("Double \"Resolution\" definition.");
                    resolution = parseResolution(contents.get(i));
                    break;
                default:
                    throw new Exception("Unknown type: \"" + types.get(i) + "\".");
            }
        }

        validAdjacencyMatrix(matrix);

        if (resolution == null)
            resolution = new Resolution();
        validResolution(resolution);

        if (verticesData == null)
            verticesData = generateVerticesData(matrix.getRowDimension(), resolution);
        validVerticesData(verticesData);

        if (matrix.getRowDimension() != verticesData.count())
            throw new Exception("Adjacency matrix dimension does not equals number of vertices.");


        return GraphData.makeByAdjacency(matrix, verticesData, resolution);
    }

    public GraphData parseIncidentFile(String filename) throws Exception {
        TypesAndContents typesAndContents = parseFileForTypesAndContents(filename);
        List<String> types = typesAndContents.getTypes(),
                contents = typesAndContents.getContents();

        Matrix matrix = null;
        List<List<Double> > edgesData = null;
        VerticesData verticesData = null;
        Resolution resolution = null;
        for (int i = 0; i < types.size(); ++i) {
            switch (types.get(i)) {
                case "matrix":
                    if (matrix != null)
                        throw new Exception("Double \"Matrix\" definition.");
                    matrix = parseMatrix(contents.get(i));
                    break;
                case "edges":
                    if (edgesData != null)
                        throw new Exception("Double \"Edges\" definition.");
                    edgesData = parseEdgesInIncidentFile(contents.get(i));
                    break;
                case "vertices":
                    if (verticesData != null)
                        throw new Exception("Double \"Vertices\" definition.");
                    verticesData = parseVertices(contents.get(i));
                    break;
                case "resolution":
                    if (resolution != null)
                        throw new Exception("Double \"Resolution\" definition.");
                    resolution = parseResolution(contents.get(i));
                    break;
                default:
                    throw new Exception("Unknown type: \"" + types.get(i) + "\".");
            }
        }

        validIncidentMatrix(matrix);
        validEdgesDataIncidentFile(matrix, edgesData);

        if (resolution == null)
            resolution = new Resolution();
        validResolution(resolution);

        if (verticesData == null)
            verticesData = generateVerticesData(matrix.getRowDimension(), resolution);
        validVerticesData(verticesData);

        if (matrix.getRowDimension() != verticesData.count())
            throw new Exception("Row count in incident matrix does not match to vertices count.");

        if (edgesData == null) {
            return GraphData.makeByIncident(matrix, verticesData, resolution);
        }
        else {
            return GraphData.makeByIncident(matrix, edgesData, verticesData, resolution);
        }
    }

    public GraphData parseEdgesFile(String filename) throws Exception {
        TypesAndContents typesAndContents = parseFileForTypesAndContents(filename);
        List<String> types = typesAndContents.getTypes(),
                contents = typesAndContents.getContents();

        EdgesData edgesData = null;
        VerticesData verticesData = null;
        Resolution resolution = null;
        for (int i = 0; i < types.size(); ++i) {
            switch (types.get(i)) {
                case "edges":
                    if (edgesData != null)
                        throw new Exception("Double \"Edges\" definition.");
                    edgesData = parseEdges(contents.get(i));
                    break;
                case "vertices":
                    if (verticesData != null)
                        throw new Exception("Double \"Vertices\" definition.");
                    verticesData = parseVertices(contents.get(i));
                    break;
                case "resolution":
                    if (resolution != null)
                        throw new Exception("Double \"Resolution\" definition.");
                    resolution = parseResolution(contents.get(i));
                    break;
                default:
                    throw new Exception("Unknown type: \"" + types.get(i) + "\".");
            }
        }

        validEdgesData(edgesData);

        if (resolution == null)
            resolution = new Resolution();
        validResolution(resolution);

        if (verticesData == null)
            verticesData = generateVerticesDataBy(edgesData.getVerticesNames(), resolution);
        validVerticesData(verticesData);

        validEdgesByVerticesData(edgesData, verticesData);

        return GraphData.makeByEdges(edgesData, verticesData, resolution);
    }

    // types and contents
    private TypesAndContents parseFileForTypesAndContents(String filename) throws Exception {
        String fileContent = readFileSkipCommentsAndSpaces(filename);
        Pattern pattern = Pattern.compile("([a-zA-Z]+)\\{([^}]*)}(;)");

        List<List<String> > groups;
        try {
            groups = parseExactBy(pattern, fileContent);
        }
        catch (ParseException e) {
            throw new Exception("Error at \"" + e.getMessage() + "\".");
        }

        List<String> types = new ArrayList<>(),
                contents = new ArrayList<>();
        for (List<String> group : groups) {
            types.add(group.get(0).toLowerCase());
            contents.add(group.get(1));
        }

        return new TypesAndContents(types, contents);
    }

    private String readFileSkipCommentsAndSpaces(String filename) throws IOException {
        StringBuilder builder = new StringBuilder();
        BufferedReader reader = new BufferedReader(new FileReader(filename));
        while (reader.ready()) {
            String line = reader.readLine();
            builder.append(removeComment(line));
        }
        reader.close();
        return builder.toString().replaceAll("\\s", "");
    }

    private String removeComment(String line) {
        int index = line.indexOf('%');
        if (index >= 0)
            return line.substring(0, index);
        else
            return line;
    }

    // simple parsers
    private Matrix parseMatrix(String content) throws Exception {
        Pattern rowPattern = Pattern.compile("\\[([^]]+)](,?)");
        List<List<String> > groups;
        try {
            groups = parseExactBy(rowPattern, content);
        }
        catch (ParseException e) {
            throw new Exception("Error at \"" + e.getMessage() + "\" in Matrix definition.");
        }

        if (groups.size() == 0)
            throw new Exception("Matrix is empty.");

        double[][] doubleRows = new double[groups.size()][];
        for (int i = 0; i < groups.size(); ++i) {
            double[] parsedRow;
            try {
                parsedRow = parseMatrixRow(groups.get(i).get(0));
            }
            catch (NumberFormatException e) {
                throw new Exception("Error while parsing row in matrix: " + groups.get(i).get(0));
            }

            if (i > 0 && parsedRow.length != doubleRows[i - 1].length)
                throw new Exception("Number of columns does not match.");
            doubleRows[i] = parsedRow;
        }

        return new Matrix(doubleRows);
    }

    private double[] parseMatrixRow(String row) throws NumberFormatException {
        String[] items = row.split(",");
        double[] result = new double[items.length];
        for (int i = 0; i < items.length; ++i) {
            result[i] = Double.parseDouble(items[i]);
        }
        return result;
    }

    private List<List<Double> > parseEdgesInIncidentFile(String content) throws Exception {
        Pattern pattern = Pattern.compile("\\(([^()]+),([^()]+)\\)(,?)");
        List<List<String> > groups;
        try {
            groups = parseExactBy(pattern, content);
        }
        catch (ParseException e) {
            throw new Exception("Error at \"" + e.getMessage() + "\" in Edges definition.");
        }

        List<List<Double> > result = new ArrayList<>();
        for (List<String> group : groups) {
            try {
                List<Double> pair = new ArrayList<>();
                pair.add(Double.parseDouble(group.get(0)));
                pair.add(Double.parseDouble(group.get(1)));
                result.add(pair);
            }
            catch (NumberFormatException e) {
                throw new Exception("Wrong double value format in Edges definition.");
            }
        }
        return result;
    }

    private EdgesData parseEdges(String content) throws Exception {
        Pattern pattern = Pattern.compile("\\(([^()]+)\\)(,?)");
        List<List<String> > groups;
        try {
            groups = parseExactBy(pattern, content);
        }
        catch (ParseException e) {
            throw new Exception("Error at \"" + e.getMessage() + "\" in Edges definition.");
        }

        EdgesData result = new EdgesData();
        for (List<String> group : groups) {
            result.add(parseEdge(group.get(0)));
        }
        return result;
    }

    private EdgeData parseEdge(String content) throws Exception {
        String[] items = content.split(",");
        if (items.length == 4) {
            double weight, x, y;
            String vertexName = items[1];
            try {
                weight = Double.parseDouble(items[0]);
                x = Double.parseDouble(items[2]);
                y = Double.parseDouble(items[3]);
            }
            catch (NumberFormatException e) {
                throw new Exception("Error while parse edges.");
            }
            return new UnaryEdgeData(vertexName, weight, x, y);
        }
        else if (items.length == 6) {
            double weight, angle, radius;
            int direction;
            String vertexName1 = items[1], vertexName2 = items[2];
            try {
                weight = Double.parseDouble(items[0]);
                direction = Integer.parseInt(items[3]);
                angle = Double.parseDouble(items[4]);
                radius = Double.parseDouble(items[5]);
            }
            catch (NumberFormatException e) {
                throw new Exception("Error while parse edges.");
            }
            return new BinaryEdgeData(vertexName1, vertexName2, weight, direction, angle, radius);
        }
        else {
            throw new Exception("Wrong number of parameters in Edges definition.");
        }
    }

    private VerticesData parseVertices(String content) throws Exception {
        Pattern pattern = Pattern.compile("([^()]+)\\(([^()]+),([^()]+)\\)(,?)");

        List<List<String> > groups;
        try {
            groups = parseExactBy(pattern, content);
        }
        catch (ParseException e) {
            throw new Exception("Error at \"" + e.getMessage() + "\" in Vertices definition.");
        }

        VerticesData result = new VerticesData();
        for (List<String> group : groups) {
            result.add(new VertexData(group.get(0),
                    Double.parseDouble(group.get(1)),
                    Double.parseDouble(group.get(2))
            ));
        }
        return result;
    }

    private Resolution parseResolution(String content) throws Exception {
        String[] values = content.split(",");
        if (values.length != 2)
            throw new Exception("Wrong number of arguments in Resolution definition.");

        try {
            double width = Double.parseDouble(values[0]),
                    height = Double.parseDouble(values[1]);
            return new Resolution(width, height);
        }
        catch (NumberFormatException e) {
            throw new Exception("Error while parse resolution values.");
        }
    }

    // validations
    private void validAdjacencyMatrix(Matrix matrix) throws Exception {
        if (matrix == null)
            throw new Exception("Matrix definition not found.");
        if (matrix.getRowDimension() != matrix.getColumnDimension())
            throw new Exception("Matrix is not square.");
    }

    private void validIncidentMatrix(Matrix matrix) throws Exception {
        if (matrix == null)
            throw new Exception("Matrix definition not found.");
    }

    private void validEdgesData(EdgesData edgesData) throws Exception {
        if (edgesData == null)
            throw new Exception("Edges definition not found.");
        if (!edgesData.isVerticesNamesValid())
            throw new Exception("Found invalid name of vertex in Edges definition.");
    }

    private void validEdgesByVerticesData(EdgesData edgesData, VerticesData verticesData)
            throws Exception
    {
        Set<String> retainedNames = new HashSet<>(edgesData.getVerticesNames());
        retainedNames.retainAll(verticesData.getNames());
        if (retainedNames.size() != edgesData.getVerticesNames().size())
            throw new Exception("Found vertex in Edges definition that does not contains in Vertex definition");
    }

    private void validEdgesDataIncidentFile(Matrix incidentMatrix,
                               List<List<Double> > edgesData) throws Exception
    {
        if (edgesData != null) {
            if (incidentMatrix.getColumnDimension() != edgesData.size())
                throw new Exception("Count of pair in Edges definition does " +
                        "not equals to matrix columns count.");
        }
    }

    private void validVerticesData(VerticesData data) throws Exception {
        if (data != null) {
            if (!data.isNamesUnique())
                throw new Exception("Found repetitive names of vertices.");
            if (!data.isNamesValid())
                throw new Exception("Found invalid name of vertex.");
        }
    }

    private void validResolution(Resolution resolution) throws Exception {
        if (resolution != null) {
            if (resolution.getWidth() < GraphGroup.minWidth)
                throw new Exception("Minimal width is " + GraphGroup.minWidth + ".");
            if (resolution.getHeight() < GraphGroup.minHeight)
                throw new Exception("Minimal height is " + GraphGroup.minHeight + ".");
        }
    }

    // default generation
    private VerticesData generateVerticesData(int count, Resolution resolution) {
        double radius = Math.min(resolution.getWidth(), resolution.getHeight()) * 0.4;
        double angle = 2 * Math.PI / count;
        Vector2D center = new Vector2D(resolution.getWidth() / 2, resolution.getHeight() / 2);

        VerticesData result = new VerticesData();
        Vector2D baseVector = new Vector2D(radius, 0);
        for (int i = 0; i < count; ++i) {
            Vector2D vertexPos = Main.rotate(baseVector, angle * i).add(center);
            result.add(new VertexData(Integer.toString(i), vertexPos.getX(), vertexPos.getY()));
        }
        return result;
    }

    private VerticesData generateVerticesDataBy(Set<String> names, Resolution resolution) {
        double radius = Math.min(resolution.getWidth(), resolution.getHeight()) * 0.4;
        double angle = 2 * Math.PI / names.size();
        Vector2D center = new Vector2D(resolution.getWidth() / 2, resolution.getHeight() / 2);

        VerticesData result = new VerticesData();
        Vector2D baseVector = new Vector2D(radius, 0);
        int index = 0;
        for (String name : names) {
            Vector2D vertexPos = Main.rotate(baseVector, angle * index++).add(center);
            result.add(new VertexData(name, vertexPos.getX(), vertexPos.getY()));
        }
        return result;
    }


    // парсит строку content на элементы elementPattern
    // строгое совпадение (между elementPattern'ами нет символов, до и после - тоже)
    // последняя группа elementPattern'а - разделитель (если в результате парса
    //    он оказывается пустым, парсинг прекращается)
    // List<List<String> > первый индекс - номер паттерна, второй - номер группы в паттерне
    // последняя группа паттерна не сохраняется (т.к. это запятая)
    private List<List<String> > parseExactBy(Pattern elementPattern, String content)
            throws ParseException
    {
        Matcher matcher = elementPattern.matcher(content);

        List<List<String> > groups = new ArrayList<>();

        int start = 0, separatorIndex = matcher.groupCount();
        while (matcher.find(start)) {
            if (matcher.start() != start) {
                String errorString = content.substring(start,
                        Math.min(start + 20, content.length()));
                throw new ParseException(errorString, start);
            }

            List<String> newGroup = new ArrayList<>();
            for (int i = 0; i < matcher.groupCount() - 1; ++i)
                newGroup.add(matcher.group(i + 1));
            groups.add(newGroup);

            start = matcher.end();

            if (matcher.group(separatorIndex).isEmpty())
                break;
        }
        if (start != content.length()) {
            String errorString = content.substring(start,
                    Math.min(start + 20, content.length()));
            throw new ParseException(errorString, start);
        }

        return groups;
    }


}
