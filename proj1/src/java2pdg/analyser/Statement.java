package java2pdg.analyser;

import java.util.HashMap;

public class Statement
{
    private HashMap<String, Integer> lastDefs;

    public Statement()
    {
        lastDefs = new HashMap<>();
        statementChanged = false;
    }

    private boolean statementChanged;

    public boolean hasChanged()
    {
        return statementChanged;
    }

    public void updateVariable(final String variableName, int lastDef)
    {
        if (lastDefs.containsKey(variableName))
        {
            statementChanged = lastDefs.put(variableName, lastDef) != lastDef;
        }
        else
        {
            lastDefs.put(variableName, lastDef);
            statementChanged = true;
        }
    }
}
