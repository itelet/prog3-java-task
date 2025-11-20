package app.models;

public class User {
    public enum Permission {
        ADMIN,      // Can do anything
        PERMITTED,  // Can only modify certain cards
        READ_ONLY   // Can only see selected cards
    }

    private String username;
    private String hashedPassword;
    private Permission permission;

    public User() {}

    public User(String username, String hashedPassword, Permission permission) {
        this.username = username;
        this.hashedPassword = hashedPassword;
        this.permission = permission;
    }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getHashedPassword() { return hashedPassword; }
    public void setHashedPassword(String hashedPassword) { this.hashedPassword = hashedPassword; }

    public Permission getPermission() { return permission; }
    public void setPermission(Permission permission) { this.permission = permission; }
}

