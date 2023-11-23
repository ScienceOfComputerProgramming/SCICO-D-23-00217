package vifim.repairer.Recipe;

import org.jetbrains.annotations.NotNull;
import org.openrewrite.ExecutionContext;
import org.openrewrite.Recipe;
import org.openrewrite.java.JavaIsoVisitor;
import org.openrewrite.java.JavaTemplate;
import org.openrewrite.java.tree.Expression;
import org.openrewrite.java.tree.J;
import org.openrewrite.java.tree.J.MethodDeclaration;
import org.openrewrite.java.tree.Statement;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class SyncCheckPhiloRecipe extends Recipe {

    static long startTime=System.currentTimeMillis();//get start time
    static long totalRepairTime=0;//get start time

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

        private  JavaTemplate unLockTemplate = template("synchronized(this){#{}}").build();

        @Override
        public J.@NotNull CompilationUnit visitCompilationUnit(J.@NotNull CompilationUnit com_unit, @NotNull ExecutionContext context) {

            List<List<Object>> threadVariableInfoList = new ArrayList<>();
            List<List<String>> threadVariableArgList = new ArrayList<>();
            List<Object> hasReverseOrder;
            List<List<String>> threadResourceList = new ArrayList<>();
            long repairTime;//get repair time
            long endTime=System.currentTimeMillis();//get end time

            if (com_unit.getClasses().isEmpty()) {
                return com_unit;
            }

            try{
                // collect ClassDeclaration
                MethodDeclaration mainMethod = getMethodDeclarationByName(com_unit, "main");
                if (mainMethod.getBody() != null) {
                    mainMethod.getBody().getStatements().stream()
                            .filter(statement -> statement instanceof J.VariableDeclarations || statement instanceof J.MethodInvocation)
                            .map(statement -> statement instanceof J.VariableDeclarations ?
                                    ((J.VariableDeclarations) statement).getVariables().stream()
                                            .filter(variable -> variable.getInitializer() instanceof J.NewClass)
                                            .map(variable -> (J.NewClass) variable.getInitializer())
                                            .collect(Collectors.toList()) :
                                    Collections.singletonList((J.NewClass) ((J.MethodInvocation) statement).getSelect()))
                            .flatMap(List::stream)
                            .forEach(newClass -> {

                                assert newClass.getClazz() != null;
                                assert newClass.getConstructorType() != null;

                                String variableName = String.valueOf(newClass.getId());
                                String newClassName = newClass.getClazz().print().trim();
                                List<String> newClassArgs = Objects.requireNonNull(newClass.getArguments()).stream()
                                        .map(Expression::print)
                                        .collect(Collectors.toList());

                                List<String> constructorArgs = newClass.getConstructorType().getParamNames();
                                Map<String, String> constructorMap = IntStream.range(0, newClassArgs.size())
                                        .boxed()
                                        .collect(Collectors.toMap(constructorArgs::get, newClassArgs::get,
                                                (n2, n1)->n1, LinkedHashMap::new));

                                threadVariableInfoList.add(Arrays.asList(variableName, newClassName, constructorMap));
                                threadVariableArgList.add(newClassArgs);
                            });
                }

                for (List<Object> threadVariable : threadVariableInfoList) {
                    String threadVariableClass = (String) threadVariable.get(1);
                    LinkedHashMap<String, String> constructorMap = (LinkedHashMap<String, String>) threadVariable.get(2);
                    MethodDeclaration constructorMethod = getMethodDeclarationByName(com_unit, threadVariableClass);

                    if (constructorMethod.getBody() != null) {
                        for (Statement statement : constructorMethod.getBody().getStatements()) {
                            if (statement instanceof J.Assignment) {
                                J.Assignment assignment = (J.Assignment) statement;
                                Expression left = assignment.getVariable();
                                Expression right = assignment.getAssignment();
                                String leftName = null;

                                if (right instanceof J.Identifier){

                                    if (left instanceof J.Identifier){
                                        leftName = ((J.Identifier) left).getSimpleName();
                                    } else if (left instanceof J.FieldAccess) {
                                        leftName = ((J.FieldAccess) left).getName().getSimpleName();
                                    }

                                    String rightName = ((J.Identifier) right).getSimpleName();
                                    String newVal = constructorMap.get(rightName);
                                    if (newVal != null) {
                                        ((Map<?, ?>) threadVariable.get(2)).remove(rightName);
                                        ((LinkedHashMap<String , String>)threadVariable.get(2)).put(newVal, leftName);
                                    }
                                }
                            }
                        }
                    }
                }

                hasReverseOrder = checkDeadlock(threadVariableArgList);

                if ((boolean) hasReverseOrder.get(0)) {

                    repairTime=System.currentTimeMillis();//get repair time

                    MethodDeclaration runMethod;
                    List<Integer> threadIndexList = new ArrayList<>();
                    Integer firstIndex = ((List<Integer>)hasReverseOrder.get(1)).get(0);
                    Integer secondIndex = ((List<Integer>)hasReverseOrder.get(1)).get(1);

                    if (threadVariableInfoList.get(firstIndex).get(1)
                            .equals(threadVariableInfoList.get(secondIndex).get(1))){
                        threadIndexList.add(firstIndex);
                    } else {
                        threadIndexList = (List<Integer>) hasReverseOrder.get(1);
                    }

                    for (Integer threadIndex : threadIndexList){
                        LinkedHashMap<String, String> threadResourceMap =
                                (LinkedHashMap<String, String>) threadVariableInfoList.get(threadIndex).get(2);
                        List<String> subResourceList = new ArrayList<>();
                        for (String threadResource : (List<String>)hasReverseOrder.get(2)){
                            subResourceList.add(threadResourceMap.get(threadResource)); // 冲突线程的两个锁对象列表,在构造函数中的名称
                        }
                        threadResourceList.add(subResourceList);
                        if (threadResourceList.size() == 2){
                            Collections.reverse(threadResourceList.get(1));
                        }

                        runMethod = com_unit.getClasses().stream()
                                .filter(Objects::nonNull)
                                .filter(classDeclaration -> classDeclaration.getSimpleName().
                                        equals(threadVariableInfoList.get(threadIndex).get(1)))
                                .flatMap(classDeclaration -> classDeclaration.getBody().getStatements().stream())
                                .filter(statement -> statement instanceof MethodDeclaration)
                                .map(statement -> (MethodDeclaration) statement)
                                .filter(methodDeclaration -> methodDeclaration.getSimpleName().equals("run"))
                                .findFirst().orElse(null);

                        assert runMethod != null;
                        List<J.Synchronized> syncList = new ArrayList<>();
                        List<String> lockInfoList = new ArrayList<>();
                        if (runMethod.getBody() != null) {
                            getSynchronizedBlocks(runMethod.getBody(), syncList, lockInfoList, 0);
                        }

                        int variableIndex = lockInfoList.indexOf(threadResourceList.get(threadIndex).get(0));

                        com_unit = com_unit.withTemplate(unLockTemplate,
                                syncList.get(variableIndex).getCoordinates().replace(),
                                syncList.get(variableIndex)
                        );
                    }

                    endTime=System.currentTimeMillis();//get end time
                    totalRepairTime += endTime-repairTime;
                    long totalTime = endTime-startTime;
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

    public void getSynchronizedBlocks(J.Block block, List<J.Synchronized> syncList, List<String> lockInfoList, int level) {
        block.getStatements().forEach(statement -> {
            if (statement instanceof J.Synchronized) {
                syncList.add((J.Synchronized) statement);
                String lock = ((J.Synchronized) statement).getLock().getTree().print().trim();
                lockInfoList.add(lock);
                getSynchronizedBlocks(((J.Synchronized) statement).getBody(), syncList, lockInfoList, level + 1);
            } else if (statement instanceof J.WhileLoop){
                getSynchronizedBlocks((J.Block) ((J.WhileLoop) statement).getBody(), syncList, lockInfoList, level+1);
            }
        });
    }

    public MethodDeclaration getMethodDeclarationByName(J.CompilationUnit _com_unit, String methodName){
        return _com_unit.getClasses().stream()
                .filter(Objects::nonNull)
                .map(classDecl -> classDecl.getBody().getStatements())
                .flatMap(List::stream)
                .filter(MethodDeclaration.class::isInstance)
                .map(statement -> (MethodDeclaration) statement)
                .filter(methodItem -> methodItem.getSimpleName().equals(methodName))
                .findFirst().orElse(null);
    }

    public static List<Object> checkDeadlock(List<List<String>> threadSyncList) {
        boolean deadlockFlag = true;
        int rowNum = threadSyncList.size();
        int colNum = threadSyncList.get(0).size();

        for (int colIdx = 1; colIdx <= colNum / 2; colIdx++) {
            for (int i = 0; i < rowNum; i++) {
                if (!threadSyncList.get(i).get(colIdx + 1).equals(threadSyncList.get((i + 1) % rowNum).get(colIdx))) {
                    deadlockFlag = false;
                    break;
                }
            }
        }
        return Arrays.asList(deadlockFlag, Arrays.asList(0, 0),
                Arrays.asList(threadSyncList.get(0).get(1), threadSyncList.get(0).get(2))
        );
    }
}
