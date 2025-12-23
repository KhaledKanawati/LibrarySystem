import java.util.ArrayList;
import java.util.List;
// Class User to manage users and user database
public class User {
    private final int id;
    private final String username;
    private final String name;
    private final List<Book> borrowedBooks;

    public User(int id, String username, String name) {
        this.id = id;
        this.username = username;
        this.name = name;
        this.borrowedBooks = new ArrayList<>();
    }

    public void addBook(Book book) {
        borrowedBooks.add(book);
    }

    public void removeBook(Book book) {
        borrowedBooks.remove(book);
    }

    public int getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public String getName() {
        return name;
    }

    public List<Book> getBooks() {
        return borrowedBooks;
    }

    @Override
    public String toString() {
        return String.format("User[ID: %d, Username: %s, Name: %s, Books: %d]", 
            id, username, name, borrowedBooks.size());
    }
}
