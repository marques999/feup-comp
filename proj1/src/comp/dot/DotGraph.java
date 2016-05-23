package comp.dot;
import java.awt.Desktop;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class DotGraph
{
	private final String TEMP_DIR = "output";
	private final String DOT_PATH = "dot";

	/**
	 * Resolução da imagem em pontos por polegada (DPI)
	 * Valor por omissão: 96 dpi
	 * Valores superiores são 10% maiores em relação ao anterior.
	 * Valores inferiores são 10% menores em relação ao posterior.
	 */
	private int[] dpiSizes =
	{
		46, 51, 57, 63, 70, 78, 86, 96, 106, 116, 128, 141, 155, 170, 187, 206, 226, 249
	};

	private HashMap<String, DotVertex> vertices = new HashMap<>();

	/**
	 * Define the index in the image size array.
	 */
	private int currentDpiPos = 7;

	/**
	 * increases the generated image size (in dpi)
	 */
	public void increaseDpi()
	{
		if (currentDpiPos < dpiSizes.length - 1)
		{
			++currentDpiPos;
		}
	}

	/**
	 * decreases the generated image size (in dpi)
	 */
	public void decreaseDpi()
	{
		if (currentDpiPos > 0)
		{
			--currentDpiPos;
		}
	}

	public void setDpi(int dpiLevel)
	{
		if (currentDpiPos > 0 && currentDpiPos < dpiSizes.length - 1)
		{
			currentDpiPos = dpiLevel;
		}
	}

	/**
	 * returns the current image size (in dpi)
	 */
	public int getDpi()
	{
		return dpiSizes[currentDpiPos];
	}

	private StringBuilder graph;
	private StringBuilder edges;
	private String graphName;
	private String edgeColor = null;
	private String edgeStyle = null;

	private int edgeOptions = 0;
	private float edgeWidth;
	private boolean subgraphClosed = true;

	/**
	 * Constructor: creates a new GraphViz object that will contain a graph.
	 */
	public DotGraph(final String paramName)
	{
		clearGraph();
		startGraph(paramName);
		edges = new StringBuilder();
		graphName = paramName;
	}

	public StringBuilder applyVertexOptions()
	{
		final StringBuilder sb = new StringBuilder();

		vertices.forEach((v, k) -> {
			if (k.hasProperties())
			{
				k.applyOptions(sb);
			}
		});

		return sb;
	}

	/**
	 * @brief aplica uma nova legenda ao vértice especificado
	 * @param paramVertex identificador do vértice a modificar
	 * @param paramLabel nova legenda do vértice
	 */
	public void setVertexLabel(final String paramVertex, final String paramLabel)
	{
		if (vertices.containsKey(paramVertex))
		{
			vertices.get(paramVertex).setLabel(paramLabel);
		}
	}

	/**
	 * @brief apaga a definição da legenda do vértice especificado
	 * @param paramVertex identificador do vértice a modificar
	 */
	public void resetVertexLabel(final String paramVertex)
	{
		if (vertices.containsKey(paramVertex))
		{
			vertices.get(paramVertex).resetLabel();
		}
	}

	/**
	 * @brief aplica uma nova cor de fundo ao vértice especificado
	 * @param paramVertex identificador do vértice a modificar
	 * @param paramColor nova cor de fundo do vértice
	 */
	public void setVertexColor(final String paramVertex, final String paramColor)
	{
		if (dotColors.contains(paramColor))
		{
			if (vertices.containsKey(paramVertex))
			{
				vertices.get(paramVertex).setColor(paramColor);
			}
		}
		else
		{
			System.err.println("<[ERROR]> invalid color specified for vertex with id=" + paramVertex);
		}
	}

	/**
	 * @brief apaga a definição da cor de fundo do vértice especificado
	 * @param paramVertex identificador do vértice a modificar
	 */
	public void resetVertexColor(final String paramVertex)
	{
		if (vertices.containsKey(paramVertex))
		{
			vertices.get(paramVertex).resetColor();
		}
	}

	/**
	 * @brief aplica uma nova forma ao vértice especificado
	 * @param paramVertex identificador do vértice a modificar
	 * @param paramShape nova forma do vértice
	 */
	public void setVertexShape(final String paramVertex, final String paramShape)
	{
		if (vertexShapes.contains(paramShape))
		{
			if (vertices.containsKey(paramVertex))
			{
				vertices.get(paramVertex).setShape(paramShape);
			}
		}
		else
		{
			System.err.println("<[ERROR]> invalid shape specified for vertex with id=" + paramVertex);
		}
	}

	/**
	 * @brief apaga a definição da forma do vértice especificado
	 * @param paramVertex identificador do vértice a modificar
	 */
	public void resetVertexShape(final String paramVertex)
	{
		if (vertices.containsKey(paramVertex))
		{
			vertices.get(paramVertex).resetShape();
		}
	}

	/**
	 * @brief aplica uma cor ao texto do vértice especificado
	 * @param paramVertex identificador do vértice a modificar
	 * @param paramShape nova cor do texto do vértice
	 */
	public void setVertexTextColor(final String paramVertex, final String paramTextColor)
	{
		if (dotColors.contains(paramTextColor))
		{
			if (vertices.containsKey(paramVertex))
			{
				vertices.get(paramVertex).setTextColor(paramTextColor);
			}
		}
		else
		{
			System.err.println("<[ERROR]> invalid text color specified for vertex with id=" + paramVertex);
		}
	}

	/**
	 * @brief apaga a definição da cor do texto do vértice especificado
	 * @param paramVertex identificador do vértice a modificar
	 */
	public void resetVertexTextColor(final String paramVertex)
	{
		if (vertices.containsKey(paramVertex))
		{
			vertices.get(paramVertex).resetTextColor();
		}
	}

	/**
	 * @brief aplica um novo estilo às arestas do grafo
	 * @param paramStyle novo estilo das arestas do grafo
	 */
	public void setEdgeStyle(final String paramStyle)
	{
		if (edgeStyles.contains(paramStyle))
		{
			edgeStyle = paramStyle;
			edgeOptions++;
		}
		else
		{
			System.err.println("<[ERROR]> invalid color specified for graph edges, reverting...");
		}
	}

	/**
	 * @brief apaga a definição do estilo das arestas do grafo
	 */
	public void resetEdgeStyle()
	{
		edgeStyle = null;
		edgeOptions--;
	}

	/**
	 * @brief aplica uma nova espessura às arestas do grafo
	 * @param paramWidth nova espesura das arestas do grafo
	 */
	public void setEdgeWidth(final float paramWidth)
	{
		if (paramWidth > 0.0f)
		{
			edgeWidth = paramWidth;
			edgeOptions++;
		}
		else
		{
			System.err.println("<[ERROR]> invalid width specified for graph edges, reverting...");
		}
	}

	/**
	 * @brief apaga a definição da espessura das arestas do grafo
	 */
	public void resetEdgeWidth()
	{
		edgeWidth = 0.0f;
		edgeOptions--;
	}

	/**
	 * @brief aplica uma nova cor ás arestas do grafo
	 * @param paramColor nova cor das arestas do grafo
	 */
	public void setEdgeColor(final String paramColor)
	{
		if (dotColors.contains(paramColor))
		{
			edgeColor = paramColor;
			edgeOptions++;
		}
		else
		{
			System.err.println("<[ERROR]> invalid color specified for graph edges, reverting...");
		}
	}

	/**
	 * @brief apaga a definição da cor das arestas do grafo
	 */
	public void resetEdgeColor()
	{
		edgeColor = null;
		edgeOptions--;
	}

	/**
	 * @brief aplica no grafo os estilos das arestas definidos pelo utilizador
	 */
	public void applyEdgeStyles(final String edgeLabel)
	{
		int currentOptions = 0;

		edges.append(" [");

		if (edgeLabel != null)
		{
			edges.append("label=\"" + edgeLabel + "\"");

			if (edgeOptions > 0)
			{
				edges.append(",");
			}
		}

		if (edgeColor != null)
		{
			edges.append("color=" + edgeColor);
			edges.append(",fontcolor=" + edgeColor);
			currentOptions++;

			if (currentOptions < edgeOptions)
			{
				edges.append(",");
			}
		}

		if (edgeStyle != null)
		{
			edges.append("style=" + edgeStyle);
			currentOptions++;

			if (currentOptions < edgeOptions)
			{
				edges.append(",");
			}
		}

		if (edgeWidth > 0.0f)
		{
			edges.append("penwidth=" + edgeWidth);
		}

		edges.append("]");
	}

	private void pushVertex(final String vertexName)
	{
		if (!vertices.containsKey(vertexName))
		{
			vertices.put(vertexName, new DotVertex(vertexName));
		}
	}

	public void connectMultiple(final String connectorType, final String[] nodes)
	{
		for (int i = 0; i < nodes.length; i++)
		{
			pushVertex(nodes[i]);

			if (i == nodes.length - 1)
			{
				edges.append(nodes[i]);
			}
			else
			{
				edges.append(nodes[i] + connectorType);
			}

			if (edgeOptions > 0)
			{
				applyEdgeStyles(null);
			}
		}
	}

	private void connect(final String connectorType, final String edgeLabel, final String nodeA, final String nodeB)
	{
		pushVertex(nodeA);
		pushVertex(nodeB);
		edges.append(nodeA + connectorType + nodeB);

		if (edgeLabel != null || edgeOptions > 0)
		{
			applyEdgeStyles(edgeLabel);
		}

		edges.append(";\n");
	}

	public void connectUndirected(final String nodeA, final String nodeB)
	{
		connect(" -- ", null, nodeA, nodeB);
	}

	public void connectDirected(final String nodeA, final String nodeB)
	{
		connect(" -> ", null, nodeA, nodeB);
	}

	public void connectUndirected(final String nodeA, final String nodeB, final String edgeLabel)
	{
		connect(" -- ", edgeLabel, nodeA, nodeB);
	}

	public void connectDirected(final String nodeA, final String nodeB, final String edgeLabel)
	{
		connect(" -> ", edgeLabel, nodeA, nodeB);
	}

	private List<String> edgeStyles = Arrays.asList(
		"dashed", "dotted", "filled");

	private List<String> vertexShapes = Arrays.asList(
		"octagon", "box", "circle", "record");

	private List<String> dotColors = Arrays.asList(
		"blue", "black", "red", "green", "yellow");

	/**
	 * formatos de representação suportados pela aplicação DOT
	 */
	private final List<String> representationTypes = Arrays.asList(
		"dot", "neato", "fdp", "sfdp", "twopi", "circo");

	/**
	 * formatos de saída suportados pela aplicação DOT
	 */
	private final List<String> outputTypes = Arrays.asList(
		"gif", "fig", "pdf", "png", "ps", "svg");

	/**
	 * Adds a newline to the graph's source.
	 */
	private void clearGraph()
	{
		graph = new StringBuilder();
		subgraphClosed = true;
	}

	/**
	 * Returns the graph as an image in binary format.
	 * @param dot_source source of the graph to be drawn.
	 * @return A byte array containing the image of the graph.
	 */
	public void outputGraph(final String outputType, final String representationType)
	{
		if (outputTypes.contains(outputType))
		{
			if (representationTypes.contains(representationType))
			{
				final File dot = generateDotFile();

				if (dot != null)
				{
					generateGraph(dot, outputType, representationType);
				}
				else
				{
					System.err.println("<[ERROR]> dot file was not generated successfully!");
				}
			}
			else
			{
				System.err.println("<[ERROR]> user has chosen an invalid representation type!");
			}
		}
		else
		{
			System.err.println("<[ERROR]> user has chosen an invalid output file type!");
		}
	}

	/**
	 * It will call the external dot program, and return the image in binary format.
	 * @param outputFile Source of the graph (in dot language).
	 * @param outputType  type of the output image to be produced, e.g.: gif, dot, fig, pdf, ps, svg, png.
	 * @param representationType type of how you want to represent the graph:
	 * @return The image of the graph in .gif format.
	 */
	private void generateGraph(final File outputFile, final String outputType, final String representationType)
	{
		try
		{
			final File img = new File(graphName + "." + outputType);

			int returnCode = Runtime.getRuntime().exec(new String[] {
				DOT_PATH, "-T" + outputType,
				"-K" + representationType,
				"-Gdpi=" + dpiSizes[currentDpiPos],
				outputFile.getAbsolutePath(), "-o",
				img.getAbsolutePath()
			}).waitFor();

			if (returnCode != 0)
			{
				System.err.println("<[ERROR]> dot returned an error, please check your syntax!");
			}
			else
			{
				Desktop.getDesktop().open(img);
			}
		}
		catch (IOException ex)
		{
			System.err.println("<[ERROR]> " + ex.getMessage());
		}
		catch (InterruptedException ex)
		{
			System.err.println("<[ERROR]> the execution of the external program was interrupted!");
		}
	}

	/**
	 * Writes the source of the graph in a file, and returns the written file as a File object.
	 * @param str Source of the graph (in dot language).
	 * @return The file (as a File object) that contains the source of the graph.
	 */
	private File generateDotFile(final File outputFile)
	{
		startGraph(graphName);
		graph.append(applyVertexOptions());
		graph.append(edges);
		finalizeGraph();
		System.out.println(graph.toString());

		try (final FileWriter fout = new FileWriter(outputFile))
		{
			fout.write(graph.toString());
		}
		catch (IOException ex)
		{
			System.err.println(ex.getLocalizedMessage());
			return null;
		}

		return outputFile;
	}

	private File generateDotFile()
	{
		try
		{
			return generateDotFile(File.createTempFile("graph_", ".dot.tmp", new File(TEMP_DIR)));
		}
		catch (IOException ex)
		{
			System.err.println(ex.getLocalizedMessage());
		}

		return null;
	}

	public File generateDotFile(final String outputFile)
	{
		return generateDotFile(new File(outputFile));
	}

	/**
	 * Returns a string that is used to start a graph.
	 * @return A string to open a graph.
	 */
	private final void startGraph(final String graphName)
	{
		clearGraph();
		graph.append("digraph " + graphName + " {\n");
	}

	/**
	 * Returns a string that is used to end a graph.
	 * @return A string to close a graph.
	 */
	public final void finalizeGraph()
	{
		graph.append("}");
	}

	/**
	 * Takes the cluster or subgraph id as input parameter and returns a string
	 * that is used to start a subgraph.
	 */
	public final void startSubgraph(int clusterid)
	{
		if (!subgraphClosed)
		{
			finalizeSubgraph();
		}

		graph.append("subgraph cluster_" + clusterid + " {\n");
		subgraphClosed = false;
	}

	/**
	 * Returns a string that is used to end a graph.
	 */
	public final void finalizeSubgraph()
	{
		if (!subgraphClosed)
		{
			graph.append("}");
			subgraphClosed = true;
		}
	}

	/**
	 * Read a DOT graph from a text file
	 * @param inputFile input text file containing the DOT graph source.
	 */
	public void readSource(final String inputFile)
	{
		String currentLine;
		clearGraph();

		try (final FileInputStream fs = new FileInputStream(inputFile);
			final DataInputStream ds = new DataInputStream(fs);
			final BufferedReader br = new BufferedReader(new InputStreamReader(ds)))
		{
			while ((currentLine = br.readLine()) != null)
			{
				graph.append(currentLine);
			}
		}
		catch (IOException e)
		{
			clearGraph();
			System.err.println("Error: " + e.getMessage());
		}
	}
}