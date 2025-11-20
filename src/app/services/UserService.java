package app.services;

import app.models.User;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.FileReader;
import java.io.FileWriter;
import java.nio.file.*;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.List;

public class UserService {

    private static final String FILE = "users.json";
    private static final Gson gson = new Gson();
    private static User currentUser = null;

    public static String hashPassword(String password) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = md.digest(password.getBytes("UTF-8"));
            StringBuilder sb = new StringBuilder();
            for (byte b : hashBytes) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static boolean register(String username, String password) {
        List<User> users = loadUsers();
        
        // Prevent registering with "admin" username (reserved for default admin account)
        if (username.equalsIgnoreCase("admin")) {
            return false;
        }
        
        // Check if username already exists
        if (users.stream().anyMatch(u -> u.getUsername().equals(username))) {
            return false;
        }

        String hashedPassword = hashPassword(password);
        if (hashedPassword == null) {
            return false;
        }

        // New users are assigned READ_ONLY permission by default
        // Only admin can change permissions later
        User newUser = new User(username, hashedPassword, User.Permission.READ_ONLY);
        users.add(newUser);
        return saveUsers(users);
    }

    public static boolean login(String username, String password) {
        List<User> users = loadUsers();
        String hashedPassword = hashPassword(password);
        
        if (hashedPassword == null) {
            return false;
        }

        User user = users.stream()
                .filter(u -> u.getUsername().equals(username) && u.getHashedPassword().equals(hashedPassword))
                .findFirst()
                .orElse(null);

        if (user != null) {
            currentUser = user;
            return true;
        }
        return false;
    }

    public static void logout() {
        currentUser = null;
    }

    public static User getCurrentUser() {
        return currentUser;
    }

    public static boolean isLoggedIn() {
        return currentUser != null;
    }

    private static List<User> loadUsers() {
        try {
            List<User> users;
            if (!Files.exists(Path.of(FILE))) {
                users = new ArrayList<>();
            } else {
                users = gson.fromJson(new FileReader(FILE), new TypeToken<List<User>>(){}.getType());
                if (users == null) {
                    users = new ArrayList<>();
                }
            }
            
            // Ensure default admin account exists with ADMIN permission
            ensureAdminAccount(users);
            
            return users;
        } catch (Exception e) {
            e.printStackTrace();
            List<User> users = new ArrayList<>();
            ensureAdminAccount(users);
            return users;
        }
    }

    /**
     * Ensures the default admin account (username: "admin", password: "admin") exists with ADMIN permission
     */
    private static void ensureAdminAccount(List<User> users) {
        String adminUsername = "admin";
        String adminPassword = "admin";
        String adminHashedPassword = hashPassword(adminPassword);
        
        if (adminHashedPassword == null) {
            return; // Can't create admin if hashing fails
        }
        
        // Check if admin account exists
        User adminUser = users.stream()
                .filter(u -> u.getUsername().equals(adminUsername))
                .findFirst()
                .orElse(null);
        
        if (adminUser == null) {
            // Create admin account if it doesn't exist
            adminUser = new User(adminUsername, adminHashedPassword, User.Permission.ADMIN);
            users.add(adminUser);
            saveUsers(users);
        } else {
            // Ensure admin account has ADMIN permission
            if (adminUser.getPermission() != User.Permission.ADMIN) {
                adminUser.setPermission(User.Permission.ADMIN);
                // Update password hash if it's incorrect (in case password was changed)
                if (!adminUser.getHashedPassword().equals(adminHashedPassword)) {
                    adminUser.setHashedPassword(adminHashedPassword);
                }
                saveUsers(users);
            } else if (!adminUser.getHashedPassword().equals(adminHashedPassword)) {
                // If permission is correct but password hash is wrong, update it
                adminUser.setHashedPassword(adminHashedPassword);
                saveUsers(users);
            }
        }
    }

    private static boolean saveUsers(List<User> users) {
        try (FileWriter writer = new FileWriter(FILE)) {
            gson.toJson(users, writer);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Check if the current user is an admin
     */
    public static boolean isAdmin() {
        return currentUser != null && currentUser.getPermission() == User.Permission.ADMIN;
    }

    /**
     * Get all users (admin only)
     */
    public static List<User> getAllUsers() {
        if (!isAdmin()) {
            return new ArrayList<>();
        }
        return new ArrayList<>(loadUsers());
    }

    /**
     * Update a user's permission (admin only)
     * @param username The username of the user to update
     * @param newPermission The new permission level
     * @return true if successful, false otherwise
     */
    public static boolean updateUserPermission(String username, User.Permission newPermission) {
        if (!isAdmin()) {
            return false;
        }

        // Prevent changing the default admin account's permission
        if (username.equals("admin")) {
            return false;
        }

        List<User> users = loadUsers();
        User userToUpdate = users.stream()
                .filter(u -> u.getUsername().equals(username))
                .findFirst()
                .orElse(null);

        if (userToUpdate == null) {
            return false;
        }

        // Prevent admin from changing their own permission
        if (userToUpdate.getUsername().equals(currentUser.getUsername())) {
            return false;
        }

        userToUpdate.setPermission(newPermission);
        return saveUsers(users);
    }

    /**
     * Get a user by username (admin only)
     */
    public static User getUserByUsername(String username) {
        if (!isAdmin()) {
            return null;
        }
        return loadUsers().stream()
                .filter(u -> u.getUsername().equals(username))
                .findFirst()
                .orElse(null);
    }
}

