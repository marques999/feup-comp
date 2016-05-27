package other;

public class Log
{
	public static final void error(String msg)
	{
		System.err.println("Error: " + msg);
		System.err.flush();
	}

	public static final void info(String msg)
	{
		System.out.println("Log:   " + msg);
		System.out.flush();
	}
}