package comp.ast;
import com.eclipsesource.json.JsonObject;

import comp.pdg.Node;

public class TypeReference extends Node
{
	private String m_type;
	
	public TypeReference(final JsonObject jsonNode, final Node parentNode)
	{
		super(jsonNode, parentNode, 0);
		//----------------------------------------
		m_type = jsonNode.asObject().getString("content", null);
	}
	
	public final String getType()
	{
		return m_type;
	}
}