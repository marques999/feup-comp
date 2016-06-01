/*
 * *****************************************************************************
 * Copyright (c) 2016 EclipseSource.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 * ****************************************************************************
 */
package eclipsesource.json;

import java.io.IOException;
import java.io.Reader;

public final class Json
{
	private Json()
	{
	}

	public static final JsonValue NULL = new JsonLiteral("null");
	public static final JsonValue TRUE = new JsonLiteral("true");
	public static final JsonValue FALSE = new JsonLiteral("false");

	public static JsonValue value(int value)
	{
		return new JsonNumber(Integer.toString(value, 10));
	}

	public static JsonValue value(long value)
	{
		return new JsonNumber(Long.toString(value, 10));
	}

	public static JsonValue value(float value)
	{
		if (Float.isInfinite(value) || Float.isNaN(value))
		{
			throw new IllegalArgumentException("Infinite and NaN values not permitted in JSON");
		}

		return new JsonNumber(cutOffPointZero(Float.toString(value)));
	}

	public static JsonValue value(double value)
	{
		if (Double.isInfinite(value) || Double.isNaN(value))
		{
			throw new IllegalArgumentException("Infinite and NaN values not permitted in JSON");
		}

		return new JsonNumber(cutOffPointZero(Double.toString(value)));
	}

	public static JsonValue value(String string)
	{
		return string == null ? NULL : new JsonString(string);
	}

	public static JsonValue value(boolean value)
	{
		return value ? TRUE : FALSE;
	}

	public static JsonValue array()
	{
		return new JsonArray();
	}

	public static JsonArray array(int... paramValues)
	{
		final JsonArray currentArray = new JsonArray();

		for (int currentValue : paramValues)
		{
			currentArray.add(currentValue);
		}

		return currentArray;
	}

	public static JsonArray array(long... paramValues)
	{
		final JsonArray currentArray = new JsonArray();

		for (long currentValue : paramValues)
		{
			currentArray.add(currentValue);
		}

		return currentArray;
	}

	public static JsonArray array(float... paramValues)
	{
		final JsonArray currentArray = new JsonArray();

		for (float currentValue : paramValues)
		{
			currentArray.add(currentValue);
		}
		
		return currentArray;
	}

	public static JsonArray array(double... paramValues)
	{
		final JsonArray currentArray = new JsonArray();

		for (double currentValue : paramValues)
		{
			currentArray.add(currentValue);
		}

		return currentArray;
	}

	public static JsonArray array(boolean... paramValues)
	{
		final JsonArray currentArray = new JsonArray();

		for (boolean currentValue : paramValues)
		{
			currentArray.add(currentValue);
		}

		return currentArray;
	}

	public static JsonArray array(String... paramStrings)
	{
		final JsonArray currentArray = new JsonArray();
		
		for (final String currentValue : paramStrings)
		{
			currentArray.add(currentValue);
		}

		return currentArray;
	}

	public static JsonObject object()
	{
		return new JsonObject();
	}

	public static JsonValue parse(final Reader paramReader) throws IOException
	{
		final DefaultHandler handler = new DefaultHandler();
		new JsonParser(handler).parse(paramReader);
		return handler.getValue();
	}

	private static String cutOffPointZero(String string)
	{
		if (string.endsWith(".0"))
		{
			return string.substring(0, string.length() - 2);
		}

		return string;
	}

	static class DefaultHandler extends JsonHandler<JsonArray, JsonObject>
	{
		protected JsonValue value;

		@Override
		public JsonArray startArray()
		{
			return new JsonArray();
		}

		@Override
		public JsonObject startObject()
		{
			return new JsonObject();
		}

		@Override
		public void endNull()
		{
			value = NULL;
		}

		@Override
		public void endBoolean(boolean bool)
		{
			value = bool ? TRUE : FALSE;
		}

		@Override
		public void endString(final String paramString)
		{
			value = new JsonString(paramString);
		}

		@Override
		public void endNumber(final String paramString)
		{
			value = new JsonNumber(paramString);
		}

		@Override
		public void endArray(JsonArray paramArray)
		{
			value = paramArray;
		}

		@Override
		public void endObject(JsonObject paramObject)
		{
			value = paramObject;
		}

		@Override
		public void endArrayValue(JsonArray paramArray)
		{
			paramArray.add(value);
		}

		@Override
		public void endObjectValue(JsonObject paramObject, String name)
		{
			paramObject.add(name, value);
		}

		JsonValue getValue()
		{
			return value;
		}
	}
}