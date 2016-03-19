import java.util.ArrayList;

public class Seq
{
	private static int i = 0;

	public static Token next()
	{
		if (i < tokens.size())
		{
			return tokens.get(i++);
		}

		return null;
	}

	public static void consume()
	{
		if (i < tokens.size())
		{
			i++;
		}
	}

	public static Token peek()
	{
		if (i < tokens.size())
		{
			return tokens.get(i);
		}

		return null;
	}

	public static ArrayList<Token> tokens = new ArrayList<Token>();

	public static void addToken(Token t)
	{
		tokens.add(t);
	}
}