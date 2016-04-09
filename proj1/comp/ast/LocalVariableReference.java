package comp.ast;
import com.eclipsesource.json.JsonObject;

import comp.pdg.Node;

public final class LocalVariableReference extends Node
{
	private String m_variable;
	private TypeReference m_type;

	public LocalVariableReference(final JsonObject jsonNode, final Node parentNode)
	{
		super(jsonNode, parentNode, 0);
		//----------------------------------------
		m_variable = jsonNode.get("content").asString();
		m_type = new TypeReference(jsonNode.get("children").asArray().get(0).asObject(), this);
	}

	public final String getName()
	{
		return m_variable;
	}
	
	public final TypeReference getType()
	{
		return m_type;
	}
}