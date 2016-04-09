package comp.dot;

public class DotEdge
{
	private final String edgeStyle;
	private final String edgeColor;

	public DotEdge(final String paramStyle, final String paramColor, float paramWidth)
	{
		edgeStyle = paramStyle;
		edgeColor = paramColor;
		edgeWidth = paramWidth;
		styleEnabled = false;
	}

	private float edgeWidth;
	private boolean styleEnabled;

	public void apply(final DotGraph dotGraph)
	{
		if (edgeColor != null)
		{
			dotGraph.setEdgeColor(edgeColor);
			styleEnabled = true;
		}

		if (edgeStyle != null)
		{
			dotGraph.setEdgeStyle(edgeColor);
			styleEnabled = true;
		}

		if (edgeWidth > 0.0f)
		{
			dotGraph.setEdgeWidth(edgeWidth);
			styleEnabled = true;
		}
	}

	public void reset(final DotGraph dotGraph)
	{
		if (!styleEnabled)
		{
			return;
		}

		if (edgeColor != null)
		{
			dotGraph.resetEdgeColor();
		}

		if (edgeStyle != null)
		{
			dotGraph.resetEdgeStyle();
		}

		if (edgeWidth > 0.0f)
		{
			dotGraph.resetEdgeWidth();
		}
	}
}