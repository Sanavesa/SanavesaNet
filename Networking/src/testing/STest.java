package testing;

import java.util.Scanner;

import networking.Server;
import networking.ServerClient;

public class STest extends Server
{
	public static void main(String[] args)
	{
		STest server = new STest();
		server.startServer(25565);
		
		Scanner s = new Scanner(System.in);
		s.next();
		s.close();
		
		System.exit(0);
	}
	
	@Override
	protected void onServerFailedToStart(int port)
	{
		System.out.println("Failed to start server");
		System.exit(0);
	}
	
	@Override
	protected void onClientConnected(ServerClient client)
	{
		System.out.println("Client connected to me!");
	}
	
	@Override
	protected void onClientDisconnected(ServerClient client)
	{
		System.out.println("Client left!");
	}
	
	@Override
	protected void onServerStarted(int port)
	{
		System.out.println("server start");
	}
	
	@Override
	protected void onServerStopped(int port)
	{
		System.out.println("server stop");
	}
}
