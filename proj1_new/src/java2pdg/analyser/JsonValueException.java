package java2pdg.analyser;

import com.eclipsesource.json.JsonValue;

public class JsonValueException extends Exception
{
	private String exceptionMessage;
	
	public JsonValueException(final JsonValue jsonValue, final String expectedType)
	{
		exceptionMessage = "Found invalid JsonValue, got " 
				+ jsonValue.getClass().getSimpleName()
				+ ", was expecting"
				+ expectedType;
	}
	
	private static final long serialVersionUID = 1L;
	
	@Override
	public String toString()
	{
		return exceptionMessage;
	}
}