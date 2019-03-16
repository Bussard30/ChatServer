package main.types;

public class User
{
	
	private Email email;
	private Username username;
	private Password password;
	private UUID uuid;
	
	public User(Email email, Username username, Password password, UUID uuid)
	{
		this.email = email;
		this.username = username;
		this.password = password;
		this.uuid = uuid;
	}
}
