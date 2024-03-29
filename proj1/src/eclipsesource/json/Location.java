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

public class Location
{
	public int offset;
	public int line;
	public int column;

	Location(int paramOffset, int paramLine, int paramColumn)
	{
		offset = paramOffset;
		column = paramColumn;
		line = paramLine;
	}

	@Override
	public String toString()
	{
		return line + ":" + column;
	}

	@Override
	public int hashCode()
	{
		return offset;
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

		final Location paramOther = (Location) paramObject;
		return offset == paramOther.offset && column == paramOther.column && line == paramOther.line;
	}
}