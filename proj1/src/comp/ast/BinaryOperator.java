package comp.ast;
import com.eclipsesource.json.JsonArray;
import com.eclipsesource.json.JsonObject;

public class BinaryOperator implements Expression
{
	private final String operator;
	private final TypeReference type;
	private final VariableRead lhs;
	private final VariableRead rhs;

	public BinaryOperator(final JsonObject jsonNode)
	{
		operator = jsonNode.getString("content", null);

		final JsonArray jsonOperator = jsonNode.get("children").asArray();

		type = new TypeReference(jsonOperator.get(0).asObject(), null);
		lhs = new VariableRead(jsonOperator.get(1).asObject(), null);
		rhs = new VariableRead(jsonOperator.get(2).asObject(), null);
	}

	@Override
	public String getValue()
	{
		return lhs.getName() + " " + operator + " " + rhs.getName();
	}

	@Override
	public String getType()
	{
		return type.getType();
	}
}