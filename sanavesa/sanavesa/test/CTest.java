package sanavesa.test;

import java.util.Scanner;

import sanavesa.net.Client;

public class CTest extends Client
{
	public static void main(String[] args) throws InterruptedException
	{
		CTest c = new CTest();
		c.connect("127.0.0.1", 25565);
		
		Scanner s = new Scanner(System.in);
		s.next();
		s.close();
		
		c.disconnect();
//		System.exit(0);
		
//		Thread.sleep(1000);
//		c.disconnect();
		
		System.out.println("Still here");
	}
	
	@Override
	protected void onFailedToConnect(String ipAddress, int port)
	{
		System.out.println("failed to connect");
		System.exit(0);
	}
	
	@Override
	protected void onConnected(String ipAddress, int port)
	{
		System.out.println("connected");
	}
	
	@Override
	protected void onDisconnected(String ipAddress, int port)
	{
		System.out.println("disconnected");
	}
}
