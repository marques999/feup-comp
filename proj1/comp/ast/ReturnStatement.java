package comp.ast;
import java.io.InvalidObjectException;

import com.eclipsesource.json.JsonArray;
import com.eclipsesource.json.JsonObject;

import comp.pdg.Node;

public class ReturnStatement extends Node
{
	private final VariableRead variableRead;
	
	public ReturnStatement(final JsonObject jsonNode, final Node parentNode) throws InvalidObjectException
	{
		super(jsonNode, parentNode, 0);
		final JsonArray nodeChildren = jsonNode.get("children").asArray();
		//----------------------------------------
		variableRead = new VariableRead(nodeChildren.get(0).asObject(), this);
	}
	
	public final String getType()
	{
		return variableRead.getType();
	}
	
	public final String getName()
	{
		return variableRead.getName();
	}
}