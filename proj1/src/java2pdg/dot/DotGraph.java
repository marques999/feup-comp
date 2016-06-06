package java2pdg.dot;

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

	private int[] dpiSizes =
	{
		46, 51, 57, 63, 70, 78, 86, 96, 106, 116, 128, 141, 155, 170, 187, 206, 226, 249
	};

	private HashMap<String, DotVertex> vertices = new HashMap<>();

	public void increaseDpi()
	{
		if (currentDpiPos < dpiSizes.length - 1)
		{
			++currentDpiPos;
		}
	}
	
	private int currentDpiPos = 7;
	
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

	public DotGraph(final String paramName)
	{
		clearGraph();
		startGraph(paramName);
		edges = new StringBuilder();
		graphName = paramName;

		File tempDir = new File(TEMP_DIR);

		if (!tempDir.exists()) {
			try{
				tempDir.mkdir();
			}
			catch(SecurityException e){
				e.printStackTrace();
			}
		}
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
	 * @brief aplica uma nova legenda ao v�rtice especificado
	 * @param paramVertex identificador do v�rtice a modificar
	 * @param paramLabel nova legenda do v�rtice
	 */
	public void setVertexLabel(final String paramVertex, final String paramLabel)
	{
		if (vertices.containsKey(paramVertex))
		{
			vertices.get(paramVertex).setLabel(paramLabel);
		}
	}

	/**
	 * @brief apaga a defini��o da legenda do v�rtice especificado
	 * @param paramVertex identificador do v�rtice a modificar
	 */
	public void resetVertexLabel(final String paramVertex)
	{
		if (vertices.containsKey(paramVertex))
		{
			vertices.get(paramVertex).resetLabel();
		}
	}

	/**
	 * @brief aplica uma nova cor de fundo ao v�rtice especificado
	 * @param paramVertex identificador do v�rtice a modificar
	 * @param paramColor nova cor de fundo do v�rtice
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
	 * @brief apaga a defini��o da cor de fundo do v�rtice especificado
	 * @param paramVertex identificador do v�rtice a modificar
	 */
	public void resetVertexColor(final String paramVertex)
	{
		if (vertices.containsKey(paramVertex))
		{
			vertices.get(paramVertex).resetColor();
		}
	}

	/**
	 * @brief aplica uma nova forma ao v�rtice especificado
	 * @param paramVertex identificador do v�rtice a modificar
	 * @param paramShape nova forma do v�rtice
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
	 * @brief apaga a defini��o da forma do v�rtice especificado
	 * @param paramVertex identificador do v�rtice a modificar
	 */
	public void resetVertexShape(final String paramVertex)
	{
		if (vertices.containsKey(paramVertex))
		{
			vertices.get(paramVertex).resetShape();
		}
	}

	/**
	 * @brief aplica uma cor ao texto do v�rtice especificado
	 * @param paramVertex identificador do v�rtice a modificar
	 * @param paramShape nova cor do texto do v�rtice
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
	 * @brief apaga a defini��o da cor do texto do v�rtice especificado
	 * @param paramVertex identificador do v�rtice a modificar
	 */
	public void resetVertexTextColor(final String paramVertex)
	{
		if (vertices.containsKey(paramVertex))
		{
			vertices.get(paramVertex).resetTextColor();
		}
	}

	/**
	 * @brief aplica um novo estilo �s arestas do grafo
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
	 * @brief apaga a defini��o do estilo das arestas do grafo
	 */
	public void resetEdgeStyle()
	{
		edgeStyle = null;
		edgeOptions--;
	}

	/**
	 * @brief aplica uma nova espessura �s arestas do grafo
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
	 * @brief apaga a defini��o da espessura das arestas do grafo
	 */
	public void resetEdgeWidth()
	{
		edgeWidth = 0.0f;
		edgeOptions--;
	}

	/**
	 * @brief aplica uma nova cor �s arestas do grafo
	 * @param paramColor nova cor das arestas do grafo
	 */
	public void setEdgeColor(final String paramColor)
	{
	
			edgeColor = paramColor;
			edgeOptions++;

	}

	/**
	 * @brief apaga a defini��o da cor das arestas do grafo
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

	public void pushVertex(final String vertexName)
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

	public void connectDirected(final String nodeA, final String nodeB)
	{
		connect(" -> ", null, nodeA, nodeB);
	}

	public void connectDirected(final String nodeA, final String nodeB, final String edgeLabel)
	{
		connect(" -> ", edgeLabel, nodeA, nodeB);
	}

	private List<String> edgeStyles = Arrays.asList(
		"dashed", "dotted", "filled"
	);

	private List<String> vertexShapes = Arrays.asList(
		"octagon", "box", "circle", "diamond", "folder", "component"
	);

	private List<String> dotColors = Arrays.asList(
		"blue", "black", "red", "green", "yellow"
	);

	/**
	 * formatos de representa��o suportados pela aplica��o DOT
	 */
	private final List<String> representationTypes = Arrays.asList(
		"dot", "neato", "fdp", "sfdp", "twopi", "circo"
	);

	/**
	 * formatos de sa�da suportados pela aplica��o DOT
	 */
	private final List<String> outputTypes = Arrays.asList(
		"gif", "fig", "pdf", "png", "ps", "svg"
	);

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

	private final void startGraph(final String graphName)
	{
		clearGraph();
		graph.append("digraph " + graphName + " {\n");
	}

	public final void finalizeGraph()
	{
		graph.append("}");
	}

	public final void startSubgraph(int clusterid)
	{
		if (!subgraphClosed)
		{
			finalizeSubgraph();
		}

		graph.append("subgraph cluster_" + clusterid + " {\n");
		subgraphClosed = false;
	}

	public final void finalizeSubgraph()
	{
		if (!subgraphClosed)
		{
			graph.append("}");
			subgraphClosed = true;
		}
	}

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