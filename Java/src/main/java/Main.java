import com.github.javaparser.JavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.body.ConstructorDeclaration;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.NameExpr;
import com.github.javaparser.ast.expr.VariableDeclarationExpr;
import com.github.javaparser.ast.nodeTypes.NodeWithSimpleName;
import java.io.*;
import java.util.*;
import java.util.stream.Collectors;

public class Main {

    private static boolean isValidName(String name) {
        return !Character.isUpperCase(name.charAt(0)) && Character.isLetter(name.charAt(0));
    }

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

    private static CompilationUnit loadCU(String filename, int lineNumber, boolean shouldReplace) {
        List<String> lines = readFile(filename);
        String replacementLine = lines.get(0);
        lines.remove(0);
        lines.remove(0);
        if (shouldReplace) {
            lines.set(lineNumber, replacementLine);
        }
        while(!lines.isEmpty()) {
            int last = lines.size() - 1;
            if (!lines.get(last).contains("}")) {
                lines.remove(last);
            } else {
                break;
            }
        }
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

    private static boolean checkThatVarsAreAvailable(
            Node node, Set<String> fieldNames, Set<String> oldNames, int lineNumber
    ) {
        int beginLine = node.getRange().get().begin.line;
        boolean gotAll = true;
        if (beginLine == lineNumber && node instanceof NameExpr) {
            String name = ((NameExpr) node).getNameAsString();
            if (isValidName(name)) {
         //       System.out.println(node.toString());
                Node cur = node;
                boolean localGot = fieldNames.contains(name) || oldNames.contains(name);
                while (cur.getParentNode().isPresent()) {
                    Node parent = cur.getParentNode().get();
                    if (parent instanceof MethodDeclaration || parent instanceof ConstructorDeclaration) {
                        localGot |= parent
                                .findAll(VariableDeclarationExpr.class)
                                .stream()
                                .anyMatch(e -> e.getVariables()
                                        .stream()
                                        .filter(v -> v.getRange().get().begin.line <= lineNumber)
                                        .anyMatch(v -> v.getNameAsString().equals(name)));
                        if (parent instanceof MethodDeclaration)
                            localGot |= ((MethodDeclaration) parent)
                                .getParameters()
                                .stream()
                                .anyMatch(v -> v.getNameAsString().equals(name));
                        else
                            localGot |= ((ConstructorDeclaration) parent)
                                    .getParameters()
                                    .stream()
                                    .anyMatch(v -> v.getNameAsString().equals(name));
                    }
                    cur = parent;
                }
                gotAll &= localGot;
            }
        }
        gotAll &= node.getChildNodes().stream().allMatch(v -> checkThatVarsAreAvailable(v, fieldNames, oldNames, lineNumber));
        return gotAll;
    }

    private static void extractNames(Node node, int lineNumber, Set<String> names) {
        int beginLine = node.getRange().get().begin.line;
        if (beginLine == lineNumber && node instanceof NameExpr) {
            names.add(((NameExpr) node).getNameAsString());
        }
        node.getChildNodes().forEach(n -> extractNames(n, lineNumber, names));
    }

    private static void verifyOldFile(String filename) {
        CompilationUnit oldCU = loadCU(filename, -1, false);
        System.out.println("OK");
    }

    private static void verifyWithReplacement(String filename, int lineNumber) {
        CompilationUnit oldCU = loadCU(filename, lineNumber - 1, false);
        Set<String> extractedNames = new HashSet<>();
        extractNames(oldCU, lineNumber, extractedNames);
        CompilationUnit updatedCU = loadCU(filename, lineNumber - 1, true);
//        AstPrinter.printAst(cu);
//        System.out.println("----------------------");
//        System.out.println(cu.toString());
        boolean gotAll = checkThatVarsAreAvailable(updatedCU, getFieldNames(updatedCU), extractedNames, lineNumber);
        if (gotAll) {
            System.out.println("OK");
        } else {
            System.out.println("FAIL");
        }
    }

    public static void main(String[] args) {
        String filename = args[0];
        int lineNumber = Integer.valueOf(args[1]);
        try {
            if (lineNumber == -1) {
                verifyOldFile(filename);
            } else {
                verifyWithReplacement(filename, lineNumber);
            }
        } catch (Exception e) {
            System.out.println("Exception: " + e.getMessage());
        }

    }
}
