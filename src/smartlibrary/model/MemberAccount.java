package smartlibrary.model;

public class MemberAccount {
    private final String name;
    private final String email;
    private final String salt;
    private final String passwordHash;
    private final long createdAt;

    public MemberAccount(String name, String email, String salt, String passwordHash, long createdAt) {
        this.name = name; this.email = email.toLowerCase(); this.salt = salt;
        this.passwordHash = passwordHash; this.createdAt = createdAt;
    }
    public String getName() { return name; }
    public String getEmail() { return email; }
    public String getSalt() { return salt; }
    public String getPasswordHash() { return passwordHash; }
    public long getCreatedAt() { return createdAt; }
}
