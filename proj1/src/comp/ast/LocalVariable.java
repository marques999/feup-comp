package comp.ast;
import com.eclipsesource.json.JsonArray;
import com.eclipsesource.json.JsonObject;

import comp.pdg.Node;

public class LocalVariable
{
	private Expression value;
	private String name;
	private TypeReference type;
	private JsonArray children;

	public LocalVariable(final JsonObject jsonNode, final Node parentNode)
	{
		children = jsonNode.get("children").asArray();
		name = jsonNode.getString("content", null);
		type = new TypeReference(children.get(0).asObject(), null);
		
		final JsonObject expression = children.get(1).asObject();
		final String expressionType = expression.getString("name", "Unknown");
		
		if (expressionType.equals("Literal"))
		{
			value = new LiteralExpression(expression, null);
		}
		else if (expressionType.equals("BinaryOperator"))
		{
			value = new BinaryOperator(expression);
		}
	}

	public String getValue()
	{
		return value.getValue();
	}

	public String getType()
	{
		return type.getType();
	}

	public TypeReference getTypeReference()
	{
		return type;
	}
	
	@Override
	public final String toString()
	{
		return type.getType() + " " + name + " = " + value.getValue();
	}
}