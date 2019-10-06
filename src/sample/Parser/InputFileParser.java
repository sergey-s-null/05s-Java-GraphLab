package sample.Parser;

import Jama.Matrix;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
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

    private String removeComment(String line) {
        int index = line.indexOf('%');
        if (index >= 0)
            return line.substring(0, index);
        else
            return line;
    }

    private String readFileSkipCommentsAndSpaces(String filename) throws IOException {
        StringBuilder builder = new StringBuilder();
        BufferedReader reader = new BufferedReader(new FileReader(filename));
        while (reader.ready()) {
            String line = reader.readLine();
            builder.append(removeComment(line));
        }
        return builder.toString().replaceAll("\\s", "");
    }

    // очень сложное описание
    // первый индекс - номер паттерна, второй - номер группы в паттерне
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

    private double[] parseMatrixRow(String row) throws NumberFormatException {
        String[] items = row.split(",");
        double[] result = new double[items.length];
        for (int i = 0; i < items.length; ++i) {
            result[i] = Double.parseDouble(items[i]);
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

    public GraphData parseAdjacencyFile(String filename) throws Exception {
        TypesAndContents typesAndContents = parseFileForTypesAndContents(filename);
        List<String> types = typesAndContents.getTypes(),
                     contents = typesAndContents.getContents();

        Matrix matrix = null;
        VerticesData vertexDataList = null;
        Resolution resolution = null;
        for (int i = 0; i < types.size(); ++i) {
            switch (types.get(i)) {
                case "matrix":
                    if (matrix != null)
                        throw new Exception("Double \"Matrix\" definition.");
                    matrix = parseMatrix(contents.get(i));
                    break;
                case "vertices":
                    if (vertexDataList != null)
                        throw new Exception("Double \"Vertices\" definition.");
                    vertexDataList = parseVertices(contents.get(i));
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

        if (vertexDataList != null) {
            return GraphData.makeByAdjacency(matrix, vertexDataList, resolution);
        }
        else {
            return GraphData.makeByAdjacency(matrix, resolution);
        }
    }

    public GraphData parseIncidentFile(String filename) throws Exception {
        TypesAndContents typesAndContents = parseFileForTypesAndContents(filename);
        List<String> types = typesAndContents.getTypes(),
                     contents = typesAndContents.getContents();

        Matrix matrix = null;
        VerticesData vertexDataList = null;
        Resolution resolution = null;
        for (int i = 0; i < types.size(); ++i) {
            switch (types.get(i)) {
                case "matrix":
                    if (matrix != null)
                        throw new Exception("Double \"Matrix\" definition.");
                    matrix = parseMatrix(contents.get(i));
                    break;
                case "vertices":
                    if (vertexDataList != null)
                        throw new Exception("Double \"Vertices\" definition.");
                    vertexDataList = parseVertices(contents.get(i));
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

        if (vertexDataList != null) {
            return GraphData.makeByIncident(matrix, vertexDataList, resolution);
        }
        else {
            return GraphData.makeByIncident(matrix, resolution);
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

        if (verticesData != null) {
            return GraphData.makeByEdges(edgesData, verticesData, resolution);
        }
        else {
            return GraphData.makeByEdges(edgesData, resolution);
        }
    }
}
