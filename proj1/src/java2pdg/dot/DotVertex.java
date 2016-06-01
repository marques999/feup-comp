package java2pdg.dot;

public class DotVertex
{
	private int mCount;

	public DotVertex(final String vertexName)
	{
		nName = vertexName;
		mCount = 0;
		mLabel = null;
		mVertexColor = null;
		mTextColor = null;
		mShape = null;
	}

	public int getCount()
	{
		return mCount;
	}

	private String nName;
	private String mLabel;
	private String mVertexColor;
	private String mTextColor;
	private String mShape;

	public void setLabel(final String vertexLabel)
	{
		mLabel = vertexLabel;
		mCount++;
	}

	public void resetLabel()
	{
		mLabel = null;
		mCount--;
	}

	public void setColor(final String vertexColor)
	{
		mVertexColor = vertexColor;
		mCount++;
	}

	public void resetColor()
	{
		mVertexColor = null;
		mCount--;
	}

	public void setShape(final String vertexShape)
	{
		mShape = vertexShape;
		mCount++;
	}

	public void resetShape()
	{
		mShape = null;
		mCount--;
	}

	public void setTextColor(final String textColor)
	{
		mTextColor = textColor;
		mCount++;
	}

	public void resetTextColor()
	{
		mTextColor = null;
		mCount--;
	}

	public boolean hasProperties()
	{
		return mCount > 0;
	}

	public void applyOptions(final StringBuilder paramGraph)
	{
		int numberOptions = 0;

		paramGraph.append(nName + " [");

		if (mVertexColor != null)
		{
			paramGraph.append("color=" + mVertexColor);
			numberOptions++;

			if (numberOptions < mCount)
			{
				paramGraph.append(",");
			}
		}

		if (mLabel != null)
		{
			paramGraph.append("label=\"" + mLabel + "\"");
			numberOptions++;

			if (numberOptions < mCount)
			{
				paramGraph.append(",");
			}
		}

		if (mShape != null)
		{
			paramGraph.append("shape=" + mShape);
			numberOptions++;

			if (numberOptions < mCount)
			{
				paramGraph.append(",");
			}
		}

		if (mTextColor != null)
		{
			paramGraph.append("textcolor=" + mTextColor);
		}

		paramGraph.append("];\n");
	}
}