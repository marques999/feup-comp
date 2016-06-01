package java2pdg;

import java2pdg.analyser.Statement;

import java.util.*;

public class DataFlowTest
{
    public static void main(final String[] args)
    {
        generateDataFlow();
    }

    private static void generateDataFlow()
    {
        final HashMap<String, String> lastDef = new HashMap<>();
        final LinkedList<Integer> queue = new LinkedList<>();
        final HashMap<Integer, HashSet<String>> definicoes = new HashMap<>();
        final HashSet<SimplePair> edgeConnections = new HashSet<>();

        definicoes.put(1, new HashSet<>(Collections.singletonList("sum")));
        definicoes.put(2, new HashSet<>(Collections.singletonList("i")));
        definicoes.put(3, new HashSet<>());
        definicoes.put(4, new HashSet<>(Collections.singletonList("sum")));
        definicoes.put(5, new HashSet<>(Collections.singletonList("i")));

        final HashMap<Integer, HashSet<String>> usos = new HashMap<>();

        usos.put(1, new HashSet<>());
        usos.put(2, new HashSet<>());
        usos.put(3, new HashSet<>((Collections.singletonList("i"))));
        usos.put(4, new HashSet<>(Arrays.asList("i", "sum")));
        usos.put(5, new HashSet<>((Collections.singletonList("i"))));

        final HashMap<Integer, ArrayList<Integer>> sucessores = new HashMap<>();

        sucessores.put(1, new ArrayList<>(Collections.singletonList(2)));
        sucessores.put(2, new ArrayList<>(Collections.singletonList(3)));
        sucessores.put(3, new ArrayList<>(Collections.singletonList(4)));
        sucessores.put(4, new ArrayList<>(Collections.singletonList(5)));
        sucessores.put(5, new ArrayList<>(Collections.singletonList(3)));

        final HashMap<String, Integer> currentLastDef = new HashMap<>();
        final HashMap<Integer, Statement> statementDef = new HashMap<>();

        queue.addAll(sucessores.keySet());

        while (!queue.isEmpty())
        {
            final Integer currentNodeId = queue.poll();

            System.out.println("visiting node " + currentNodeId);

            final HashSet<String> defSet = definicoes.get(currentNodeId);
            final HashSet<String> useSet = usos.get(currentNodeId);

            Statement previousDefs = statementDef.get(currentNodeId);

            if (previousDefs == null)
            {
                statementDef.put(currentNodeId, new Statement());
                previousDefs = statementDef.get(currentNodeId);
            }

            boolean statementChanged = false;

            for (final String currentVariable : useSet)
            {
                Integer lastDefStatement = currentLastDef.get(currentVariable);

                if (lastDefStatement != null)
                {
                    previousDefs.updateVariable(currentVariable, lastDefStatement);

                    if (previousDefs.hasChanged())
                    {
                        statementChanged = true;
                    }

                    edgeConnections.add(new SimplePair(lastDefStatement, currentNodeId));
                }
            }

            for (final String currentVariable : defSet)
            {
                currentLastDef.put(currentVariable, currentNodeId);
            }

            if (statementChanged)
            {
                queue.addAll(sucessores.get(currentNodeId));
            }
        }

        for (final SimplePair connection : edgeConnections)
        {
            // adicionar arestas ao program dependency graph
            System.out.println("connecting " + connection.getFirst() + " to " + connection.getSecond());
        }
    }
}
