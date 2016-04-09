package comp.ast;
import com.eclipsesource.json.JsonArray;
import com.eclipsesource.json.JsonObject;

import comp.pdg.Node;

public class VariableRead extends Node
{
	private final TypeReference typeReference;
	private final LocalVariableReference localVariable;
	
	public VariableRead(final JsonObject jsonNode, final Node parentNode)
	{
		super(jsonNode, parentNode, 0);
		//----------------------------------------
		final JsonArray nodeChildren = jsonNode.get("children").asArray();
		//----------------------------------------
		typeReference = new TypeReference(nodeChildren.get(0).asObject(), this);
		localVariable = new LocalVariableReference(nodeChildren.get(1).asObject(), this);
	}
	
	public final String getName()
	{
		return localVariable.getName();
	}
	
	public final String getType()
	{
		return typeReference.getType();
	}
}