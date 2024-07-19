package packets;

public class PacketUser {

	private String name;
	private String email;
	
	public PacketUser(String name, String email) {
		this.name = name;
		this.email = email;
	}

	public String getName() {
		return name;
	}

	public String getEmail() {
		return email;
	}

}