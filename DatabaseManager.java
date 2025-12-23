import java.io.*;
import java.util.HashMap;
import java.util.Map;

public class DatabaseManager {
    private static final String USERS_FILE = "users.dat";
    private static Map<String, UserData> users = new HashMap<>();
    
    static class UserData implements Serializable {
        int id;
        String username;
        String password;
        String name;
        
        UserData(int id, String username, String password, String name) {
            this.id = id;
            this.username = username;
            this.password = password;
            this.name = name;
        }
    }

    public static void initializeDatabase() {
        loadUsers();
        System.out.println("User storage initialized.");
    }

    @SuppressWarnings("unchecked")
    private static void loadUsers() {
        File file = new File(USERS_FILE);
        if (file.exists()) {
            try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file))) {
                users = (Map<String, UserData>) ois.readObject();
                System.out.println("Loaded " + users.size() + " existing users.");
            } catch (IOException | ClassNotFoundException e) {
                System.out.println("Starting with fresh user database.");
                users = new HashMap<>();
            }
        }
    }
    
    private static void saveUsers() {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(USERS_FILE))) {
            oos.writeObject(users);
        } catch (IOException e) {
            System.out.println("Error saving users: " + e.getMessage());
        }
    }
    
    private static boolean isValidUsername(String username) {
        if (username == null || username.isEmpty()) {
            return false;
        }
        return username.matches("^[a-zA-Z0-9_]+$");
    }

    public static boolean registerUser(String username, String password, String name) {
        if (!isValidUsername(username)) {
            System.out.println("Invalid username! Use only letters, numbers, and underscores.");
            return false;
        }
        
        // Store in lowercase for case-insensitive matching
        String usernameLower = username.toLowerCase();
        
        if (users.containsKey(usernameLower)) {
            System.out.println("Username already exists!");
            return false;
        }
        
        int newId = users.size() + 1;
        users.put(usernameLower, new UserData(newId, username, password, name));
        saveUsers();
        return true;
    }

    public static User loginUser(String username, String password) {
        String usernameLower = username.toLowerCase();
        UserData userData = users.get(usernameLower);
        
        if (userData != null && userData.password.equals(password)) {
            return new User(userData.id, userData.username, userData.name);
        }
        
        return null;
    }

    public static boolean deleteUser(int userId, String password) {
        for (Map.Entry<String, UserData> entry : users.entrySet()) {
            UserData userData = entry.getValue();
            if (userData.id == userId && userData.password.equals(password)) {
                users.remove(entry.getKey());
                saveUsers();
                return true;
            }
        }
        return false;
    }

    public static boolean usernameExists(String username) {
        return users.containsKey(username.toLowerCase());
    }

    public static void closeConnection() {
        saveUsers();
        System.out.println("User data saved.");
    }
}
