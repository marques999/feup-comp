package dot;
public class DotVertex
{
	private int count;

	public DotVertex(final String vertexName)
	{
		name = vertexName;
		count = 0;
		label = null;
		color = null;
		textColor = null;
		shape = null;
	}

	public int getCount()
	{
		return count;
	}

	private String name;
	private String label;
	private String color;
	private String textColor;
	private String shape;

	public void setLabel(final String vertexLabel)
	{
		label = vertexLabel;
		count++;
	}

	public void resetLabel()
	{
		label = null;
		count--;
	}

	public void setColor(final String vertexColor)
	{
		color = vertexColor;
		count++;
	}

	public void resetColor()
	{
		color = null;
		count--;
	}

	public void setShape(final String vertexShape)
	{
		shape = vertexShape;
		count++;
	}

	public void resetShape()
	{
		shape = null;
		count--;
	}

	public void setTextColor(final String vertexTextColor)
	{
		textColor = vertexTextColor;
		count++;
	}

	public void resetTextColor()
	{
		textColor = null;
		count--;
	}

	public boolean hasProperties()
	{
		return count > 0;
	}

	public void applyOptions(final StringBuilder graph)
	{
		int numberOptions = 0;

		graph.append(name + " [");

		if (color != null)
		{
			graph.append("color=" + color);
			numberOptions++;

			if (numberOptions < count)
			{
				graph.append(",");
			}
		}

		if (label != null)
		{
			graph.append("label=\"" + label + "\"");
			numberOptions++;

			if (numberOptions < count)
			{
				graph.append(",");
			}
		}

		if (shape != null)
		{
			graph.append("shape=" + shape);
			numberOptions++;

			if (numberOptions < count)
			{
				graph.append(",");
			}
		}

		if (textColor != null)
		{
			graph.append("textcolor=" + textColor);
		}

		graph.append("];\n");
	}
}