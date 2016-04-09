package comp.ast;
import com.eclipsesource.json.JsonObject;

import comp.pdg.Node;

public class LiteralExpression implements Expression
{
	private String literalValue;
	private TypeReference literalType;
	
	public LiteralExpression(final JsonObject jsonNode, final Node parentNode)
	{
		literalValue = jsonNode.getString("content", "0");
		literalType = new TypeReference(jsonNode.get("children").asArray().get(0).asObject(), null);
	}
	
	public String getValue()
	{
		return literalValue;
	}
	
	public String getType()
	{
		return literalType.getType();
	}
	
	public TypeReference getTypeReference()
	{
		return literalType;
	}
}