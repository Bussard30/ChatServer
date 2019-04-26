package main.main;

import datastorage.main.DSManager;
import networking.server.Diagnostics;
import networking.server.Server;
import networking.types.ProtocolWrapper;

public class Main
{
	//CONSTANTS
	public static ProtocolWrapper protocol = new ProtocolWrapper("1.0", "1.0");
	public static int port = 20518;
	private static Main main;
	
	private Server server;
	private DSManager dsm;
	private Diagnostics d;
	
	public static void main(String[] args)
	{
		main = new Main();
	}
	
	public Main()
	{
		server = new Server();
		dsm = new DSManager();
		d = new Diagnostics();
		
		server.startServer(55555);
	}
	
}
