package analyser;

import data.AST;

public class AnalyseAst
{
	private static AST ast;
	private static Visitor visitor;

	public static void main(String args[])
	{
		ast = new AST("ast.json");
		visitor = new Visitor(ast);
	}

	public static AST getAST()
	{
		return ast;
	}

	public static Visitor getVisitor()
	{
		return visitor;
	}

	public static void setVisitor(final Visitor visitor)
	{
		AnalyseAst.visitor = visitor;
	}
}