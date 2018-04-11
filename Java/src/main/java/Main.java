import com.github.javaparser.JavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.NameExpr;
import com.github.javaparser.ast.expr.VariableDeclarationExpr;
import com.github.javaparser.ast.nodeTypes.NodeWithSimpleName;
import java.io.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class Main {

    private static List<String> readFile(String filename) {
        try (BufferedReader reader = new BufferedReader(new FileReader(filename))) {
            List<String> lines = new ArrayList<>();
            String line;
            while ((line = reader.readLine()) != null) {
                lines.add(line);
            }
            return lines;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return Collections.emptyList();
    }

    private static CompilationUnit loadCU(String filename, int lineNumber) {
        List<String> lines = readFile(filename);
        String replacementLine = lines.get(0);
        lines.remove(0);
        lines.remove(0);
        lines.set(lineNumber, replacementLine);
        try {
            //create a temp file
            File temp = File.createTempFile("temp-file", ".java");
            PrintWriter writer = new PrintWriter(temp);
            for (String line : lines) {
                writer.println(line);
            }
            writer.close();
            return JavaParser.parse(temp);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private static Set<String> getFieldNames(CompilationUnit cu) {
        return cu
                .findAll(FieldDeclaration.class)
                .stream()
                .flatMap(f -> f.getVariables()
                                .stream()
                                .map(NodeWithSimpleName::getNameAsString)
                ).collect(Collectors.toSet());
    }

    private static boolean checkThatVarsAreAvailable(Node node, Set<String> fieldNames, int lineNumber) {
        int beginLine = node.getRange().get().begin.line;
        boolean gotAll = true;
        if (beginLine == lineNumber && node instanceof NameExpr) {
            String name = ((NameExpr) node).getNameAsString();
            if (!Character.isUpperCase(name.charAt(0))) {
//                System.out.println(node.toString());
                Node cur = node;
                boolean localGot = fieldNames.contains(name);
                while (cur.getParentNode().isPresent()) {
                    Node parent = cur.getParentNode().get();
                    if (parent instanceof MethodDeclaration) {
                        localGot |= parent
                                .findAll(VariableDeclarationExpr.class)
                                .stream()
                                .anyMatch(e -> e.getVariables()
                                        .stream()
                                        .filter(v -> v.getRange().get().begin.line <= lineNumber)
                                        .anyMatch(v -> v.getNameAsString().equals(name)));
                        localGot |= ((MethodDeclaration) parent)
                                .getParameters()
                                .stream()
                                .anyMatch(v -> v.getNameAsString().equals(name));
                    }
                    cur = parent;
                }
                gotAll &= localGot;
            }
        }
        gotAll &= node.getChildNodes().stream().allMatch(v -> checkThatVarsAreAvailable(v, fieldNames, lineNumber));
        return gotAll;
    }

    public static void main(String[] args) {
        String filename = args[0];
        int lineNumber = Integer.valueOf(args[1]);
        CompilationUnit cu = loadCU(filename, lineNumber - 1);
//        AstPrinter.printAst(cu);
//        System.out.println("----------------------");
//        System.out.println(cu.toString());
        boolean gotAll = checkThatVarsAreAvailable(cu, getFieldNames(cu), lineNumber);
        if (gotAll) {
            System.out.println("OK");
        } else {
            System.out.println("FAIL");
        }
    }
}
