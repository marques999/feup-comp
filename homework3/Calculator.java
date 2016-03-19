import java.util.ArrayList;
import java.util.Scanner;

public class Calculator
{
	private static int checkSymbol(char currentChar)
	{
		switch (currentChar)
		{
		case '+':
			return TokenID.ADD;
		case '-':
			return TokenID.SUB;
		case '*':
			return TokenID.MUL;
		case '/':
			return TokenID.DIV;
		}

		return -1;
	}

	private static int checkInteger(final String paramString, int i)
	{
		int j = i;

		for (; j < paramString.length(); j++)
		{
			char currentChar = paramString.charAt(j);

			if (currentChar < '0' || currentChar > '9')
			{
				break;
			}
		}

		return j;
	}

	private static int consumeWhitespace(final String inputString, int i)
	{
		while (i < inputString.length() && inputString.charAt(i) == ' ')
		{
			i++;
		}

		return i;
	}

	private static boolean analyzeTokens(final String inputString)
	{
		int i = 0;
		boolean foundSymbol = false;

		while (i < inputString.length())
		{
			i = consumeWhitespace(inputString, i);

			if (i >= inputString.length())
			{
				return foundSymbol;
			}

			char currentSymbol = inputString.charAt(i);
			int symbolId = checkSymbol(currentSymbol);

			if (symbolId > 0)
			{
				foundSymbol = true;
				Seq.addToken(new Token(Character.toString(currentSymbol), symbolId));
				i++;
			}
			else
			{
				int nextPosition = checkInteger(inputString, i);

				if (nextPosition != i)
				{
					foundSymbol = true;
					Seq.addToken(new Token(inputString.substring(i, nextPosition), TokenID.INT));
					i = nextPosition;
				}
				else
				{
					return false;
				}
			}
		}

		return true;
	}

	private static boolean Start()
	{
		return Expr() && Seq.next() == null;
	}

	private static boolean Expr()
	{
		if (Term())
		{
			return ExprP();
		}

		return false;
	}

	private static boolean Term()
	{
		final Token nextToken = Seq.next();

		if (nextToken != null && nextToken.id == TokenID.INT)
		{
			return TermP();
		}

		return false;
	}

	private static boolean TermP()
	{
		Token nextToken = Seq.peek();

		if (nextToken == null)
		{
			return true;
		}

		if (nextToken.id == TokenID.MUL || nextToken.id == TokenID.DIV)
		{
			Seq.consume();
			nextToken = Seq.next();

			if (nextToken != null && nextToken.id == TokenID.INT)
			{
				return TermP();
			}
			else
			{
				return false;
			}
		}

		return true;
	}

	private static boolean ExprP()
	{
		final Token nextToken = Seq.peek();

		if (nextToken == null)
		{
			return true;
		}

		if (nextToken.id == TokenID.ADD || nextToken.id == TokenID.SUB)
		{
			Seq.consume();

			if (Term())
			{
				return ExprP();
			}
			else
			{
				return false;
			}
		}

		return true;
	}

	public static void main(String[] args)
	{
		final Scanner inputScanner = new Scanner(System.in);

		if (analyzeTokens(inputScanner.nextLine()))
		{
			final ArrayList<Token> tokens = Seq.tokens;

			for (final Token token : tokens)
			{
				System.out.println("<token> type = " + token.id + ", image = " + token.image);
			}

			if (Start())
			{
				System.out.println("String accepted!");
			}
			else
			{
				System.out.println("String rejected!");
			}
		}
		else
		{
			System.out.println("String rejected, found invalid tokens!");
		}

		inputScanner.close();
	}
}