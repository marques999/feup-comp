package java2pdg.analyser;

import java.io.BufferedReader;
import java.io.IOException;

import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystems;
import java.nio.file.Files;

import com.eclipsesource.json.Json;
import com.eclipsesource.json.JsonObject;

public class AST
{
	private JsonObject ast;

	public AST(String astFile)
	{
		try
		{
			parseFile(astFile);
			System.out.println(ast);
		} 
		catch (final IOException ex)
		{
			ast = null;
			System.err.println("Could not open file to read AST from");
			ex.printStackTrace();
		}
	}

	private void parseFile(String astFile) throws IOException
	{
		try (final BufferedReader in = Files.newBufferedReader(FileSystems.getDefault().getPath(astFile), StandardCharsets.UTF_8))
		{
			ast = Json.parse(in).asObject();
		}
		catch (final IOException ex)
		{
			ex.printStackTrace();
		}
	}

	public JsonObject getTree()
	{
		return ast;
	}
}