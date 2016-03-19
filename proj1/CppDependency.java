public class CppDependency
{
	public static void main(String[] args)
	{
		DotGraph g = new DotGraph("Example");
		g.setEdgeColor("blue");
		g.setEdgeStyle("dotted");
		g.setDpi(15);
		g.connectDirected("A", "B", "X");
		g.setVertexColor("A", "red");
		g.setVertexColor("B", "yellow");
		g.setVertexLabel("A", "WHAT");
		g.setVertexShape("A", "box");
		g.setVertexShape("B", "box");
		g.resetEdgeColor();
		g.connectDirected("B", "C", "Y");
		g.resetEdgeStyle();
		g.setEdgeWidth(5.0f);
		g.setEdgeColor("green");
		g.connectDirected("B", "D", "Z");
		g.generateDotFile("myGraph.dot");
		g.outputGraph("png", "dot");
	}
}