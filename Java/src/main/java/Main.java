import com.github.javaparser.JavaParser;
import com.github.javaparser.ast.CompilationUnit;

import java.io.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Main {

    private static List<String> readFile(String filename) {
        try(BufferedReader reader = new BufferedReader(new FileReader(filename))) {
            List<String> lines = new ArrayList<>();
            String line;
            while((line = reader.readLine()) != null) {
                lines.add(line);
            }
            return lines;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return Collections.emptyList();
    }

    public static void main(String[] args) {
        String filename = args[0];
        int lineNumber = Integer.valueOf(args[1]) - 1;
        List<String> lines = readFile(filename);
        String replacementLine = lines.get(0);
        lines.remove(0);
        lines.remove(0);
        lines.set(lineNumber, replacementLine);
        String resultingFile = String.join(System.getProperty("line.separator"), lines);
        CompilationUnit cu = JavaParser.parse(resultingFile);
        System.out.println(cu.toString());

    }
}
