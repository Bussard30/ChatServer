package main.types;

public class UUID
{
	private String uuid;
	
	public UUID()
	{
		uuid = java.util.UUID.randomUUID().toString();
	}
	
	public UUID(String uuid)
	{
		this.uuid = uuid;
	}
	
	public String getUUID()
	{
		return uuid;
	}
}
