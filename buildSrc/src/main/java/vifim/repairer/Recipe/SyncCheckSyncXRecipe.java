package vifim.repairer.Recipe;

import org.jetbrains.annotations.NotNull;
import org.openrewrite.ExecutionContext;
import org.openrewrite.Recipe;
import org.openrewrite.java.JavaIsoVisitor;
import org.openrewrite.java.JavaTemplate;
import org.openrewrite.java.tree.*;
import org.openrewrite.marker.Markers;

import java.io.Serializable;
import java.util.*;
import java.util.stream.Collectors;

public class SyncCheckSyncXRecipe extends Recipe {

    static long startTime=System.currentTimeMillis();//get start time
    static long totalRepairTime=0;//get start time

//    static List<String> collectionApiList = Arrays.asList("removeAll", "addAll", "retainAll", "removeAll", "addAll", "retainAll", "get", "equals");

    @Override
    public String getDisplayName() {
        return "Check Lock()";
    }

    @Override
    public String getDescription() {
        return "check if each lock() has its own unlock() and repair it";
    }

    @Override
    public @NotNull JavaIsoVisitor<ExecutionContext> getVisitor() {
        // getVisitor() should always return a new instance of the visitor to avoid any state leaking between cycles
        return new LockCheckVisitor();
    }

    public class LockCheckVisitor extends JavaIsoVisitor<ExecutionContext> {
        private  JavaTemplate unLockTemplate = template("#{}").build();

        @Override
        public J.@NotNull CompilationUnit visitCompilationUnit(J.@NotNull CompilationUnit com_unit, @NotNull ExecutionContext context) {

            List<List<String>> threadVariableArgList = new ArrayList<>();
            List<Object> hasReverseOrder;

            long repairTime;//get repair time
            long endTime=System.currentTimeMillis();//get end time

            if (com_unit.getClasses().isEmpty()){
                return com_unit;
            }

            try {
                J.MethodDeclaration mainMethod = getMethodDeclarationByName(com_unit, "main");
                if (mainMethod.getBody() != null) {
                    for (Statement statement : mainMethod.getBody().getStatements()) {
                        if (statement instanceof J.VariableDeclarations) {
                            J.VariableDeclarations declarations = (J.VariableDeclarations) statement;
                            assert declarations.getTypeExpression() != null;
                            if (declarations.getTypeExpression().print().trim().equals("Thread")){
                                for (J.VariableDeclarations.NamedVariable variable : declarations.getVariables()) {
                                    Expression initializer = variable.getInitializer();
                                    if ((initializer instanceof J.NewClass) || (initializer instanceof J.Parentheses &&
                                            ((J.Parentheses<?>) initializer).getTree() instanceof J.NewClass)) {
                                        J.NewClass newClass;
                                        try {
                                            newClass = (J.NewClass) initializer;
                                        } catch (Exception e){
                                            newClass = (J.NewClass)((J.Parentheses<?>) initializer).getTree();
                                        }

                                        assert newClass.getClazz() != null;
                                        assert newClass.getConstructorType() != null;

                                        List<String> newClassArgs = new ArrayList<>();
                                        for (int i = 0; i < Objects.requireNonNull(newClass.getArguments()).size(); i++) {
                                            newClassArgs.add(newClass.getArguments().get(i).print().trim());
                                        }

                                        threadVariableArgList.add(newClassArgs);
                                    }
                                }
                            }
                        } else if (statement instanceof J.MethodInvocation) {
                            J.MethodInvocation invocation = (J.MethodInvocation) statement;
                            try {
                                J.NewClass newClass = (J.NewClass) ((J.Parentheses<?>) Objects.requireNonNull(invocation.getSelect())).getTree();
                                assert newClass.getConstructorType() != null;
                                assert newClass.getClazz() != null;
                                List<String> newClassArgs = new ArrayList<>();
                                for (int i = 0; i < Objects.requireNonNull(newClass.getArguments()).size(); i++) {
                                    newClassArgs.add(newClass.getArguments().get(i).print().trim());
                                }

                                threadVariableArgList.add(newClassArgs);
                            } catch (Exception ignored){
                            }
                        }
                    }
                }

                hasReverseOrder = checkReverseOrder(threadVariableArgList);
                if ((boolean)hasReverseOrder.get(0)) {

                    repairTime = System.currentTimeMillis();//get repair time

                    List<JRightPadded<Statement>> rightPaddedStatements = mainMethod.getBody().getStatements().stream()
                            .map(s -> new JRightPadded<>(s, Space.EMPTY, Markers.EMPTY))
                            .collect(Collectors.toList());
                    J.Synchronized syncStat = new J.Synchronized(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY,
                            new J.ControlParentheses<>(
                                    UUID.randomUUID(),
                                    Space.EMPTY,
                                    Markers.EMPTY,
                                    new JRightPadded<>(
                                            J.Identifier.build(
                                                    UUID.randomUUID(),
                                                    Space.EMPTY,
                                                    Markers.EMPTY,
                                                    "this",
                                                    null
                                            ),
                                            Space.EMPTY,
                                            Markers.EMPTY
                                    )
                            ),
                            new J.Block(
                                    UUID.randomUUID(),
                                    Space.EMPTY,
                                    Markers.EMPTY,
                                    new JRightPadded<>(false, Space.EMPTY, Markers.EMPTY),
                                    rightPaddedStatements,
                                    Space.EMPTY
                            )
                    );
                    List<Statement> syncStats = Collections.singletonList(syncStat);
                    List<JRightPadded<Statement>> rightPaddedStats = syncStats.stream()
                            .map(s -> new JRightPadded<>(s, Space.EMPTY, Markers.EMPTY))
                            .collect(Collectors.toList());

                    J.Block replaceBody = new J.Block(
                            UUID.randomUUID(),
                            Space.EMPTY,
                            Markers.EMPTY,
                            new JRightPadded<>(false, Space.EMPTY, Markers.EMPTY),
                            rightPaddedStats,
                            Space.EMPTY
                    );


                    com_unit = com_unit.withTemplate(unLockTemplate,
                            mainMethod.getBody().getCoordinates().replace(),
                            replaceBody);

                    endTime=System.currentTimeMillis();//get end time
                    long totalTime = endTime-startTime;
                    totalRepairTime += endTime-repairTime;
                    System.out.println("start time: "+(startTime)+"ms");//total run time
                    System.out.println("running time: "+(totalTime)+"ms");//total run time
                    System.out.println("detect time: "+(totalTime-totalRepairTime)+"ms");//total detect time
                    System.out.println("repair time: "+(totalRepairTime)+"ms");//total repair time
                    return com_unit;
                }
            } catch (Exception e){
                return com_unit;
            }
            return com_unit;
        }
    }

    public J.MethodDeclaration getMethodDeclarationByName(J.CompilationUnit _com_unit, String methodName){
        J.MethodDeclaration result = null;
        List<J.ClassDeclaration> classes = _com_unit.getClasses();
        for (J.ClassDeclaration classDecl : classes) {
            if (classDecl != null) {
                result = findMethodByName(classDecl, methodName);
                if (result != null){
                    return result;
                }
            }
        }
        return result;
    }

    private J.MethodDeclaration findMethodByName(J.ClassDeclaration classDecl, String methodName) {
        List<Statement> statements = classDecl.getBody().getStatements();
        for (Statement statement : statements) {
            if (statement instanceof J.MethodDeclaration && ((J.MethodDeclaration) statement).getSimpleName().equals(methodName)) {
                return (J.MethodDeclaration) statement;
            } else if (statement instanceof J.ClassDeclaration) {
                J.MethodDeclaration methodItem = findMethodByName((J.ClassDeclaration) statement, methodName);
                if (methodItem != null) {
                    return methodItem;
                }
            }
        }
        return null;
    }

    public static List<Serializable> hasReverseOrder (List <String> a, List <String> b) {
        int i = 0;
        int j = b.size () - 1;
        while (i < a.size()-1){
            while (j > 0){
                if ((a.get(i).equals(b.get(j))) && a.get(i+1).equals(b.get(j-1))){
                    return Arrays.asList(true,a.get(i), a.get(i+1));
                }
                j--;
            }
            i++;
            j = b.size()-1;
        }
        return Arrays.asList(false,-1, -1);
    }

    public static List<Object> checkReverseOrder (List<List<String>> list) {
        if (list == null || list.size () < 2) {
            return Arrays.asList(false,Arrays.asList(-1, -1),Arrays.asList(null, null));
        }
        for (int i = 0; i < list.size () - 1; i++) {
            for (int j = i + 1; j < list.size (); j++) {
                List<Serializable> resultList = hasReverseOrder(list.get (i), list.get (j));
                if ((boolean)resultList.get(0)){
                    return Arrays.asList(true, Arrays.asList(i, j), Arrays.asList(resultList.get(1), resultList.get(2)));
                }
            }
        }
        return Arrays.asList(false,Arrays.asList(-1, -1),Arrays.asList(null, null));
    }

}
