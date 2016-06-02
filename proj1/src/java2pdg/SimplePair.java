package java2pdg;

public class SimplePair
{
	private int first;
	private int second;

	public SimplePair(int paramKey, int paramValue)
	{
		first = paramKey;
		second = paramValue;
	}

	public int getFirst()
	{
		return first;
	}

	public int getSecond()
	{
		return second;
	}

	@Override
	public int hashCode()
	{
		return (first + second) * ((first + second) + 1) / 2 + first;
	}

	@Override
	public boolean equals(final Object paramObject)
	{
		return paramObject instanceof SimplePair && first == ((SimplePair) paramObject).first && second == ((SimplePair) paramObject).second;
	}
}