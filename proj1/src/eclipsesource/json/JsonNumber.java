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

@SuppressWarnings("serial")
class JsonNumber extends JsonValue
{
	private final String string;

	JsonNumber(final String paramString)
	{
		string = paramString;
	}

	@Override
	public String toString()
	{
		return string;
	}

	@Override
	void write(final JsonWriter paramWriter) throws IOException
	{
		paramWriter.writeNumber(string);
	}

	@Override
	public boolean isNumber()
	{
		return true;
	}

	@Override
	public int asInt()
	{
		return Integer.parseInt(string, 10);
	}

	@Override
	public long asLong()
	{
		return Long.parseLong(string, 10);
	}

	@Override
	public float asFloat()
	{
		return Float.parseFloat(string);
	}

	@Override
	public double asDouble()
	{
		return Double.parseDouble(string);
	}

	@Override
	public int hashCode()
	{
		return string.hashCode();
	}

	@Override
	public boolean equals(final Object paramObject)
	{
		if (this == paramObject)
		{
			return true;
		}

		if (paramObject == null)
		{
			return false;
		}

		if (getClass() != paramObject.getClass())
		{
			return false;
		}

		return string.equals(((JsonNumber) paramObject).string);
	}
}