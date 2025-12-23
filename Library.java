import java.io.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class Library {
    private static final String BOOKS_FILE = "books.dat";
    private final List<Book> bookCatalog;
    private final List<User> registeredUsers;
    private final List<RentTransaction> activeRentalTransactions;

    public Library() {
        this.bookCatalog = new ArrayList<>();
        this.registeredUsers = new ArrayList<>();
        this.activeRentalTransactions = new ArrayList<>();
        loadBooks();
    }
    
    @SuppressWarnings("unchecked")
    private void loadBooks() {
        File file = new File(BOOKS_FILE);
        if (file.exists()) {
            try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file))) {
                bookCatalog.addAll((List<Book>) ois.readObject());
                System.out.println("Loaded " + bookCatalog.size() + " existing books.");
            } catch (IOException | ClassNotFoundException e) {
                System.out.println("Starting with fresh book catalog.");
            }
        }
    }
    
    private void saveBooks() {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(BOOKS_FILE))) {
            oos.writeObject(bookCatalog);
        } catch (IOException e) {
            System.out.println("Error saving books: " + e.getMessage());
        }
    }

    public void addBook(Book book) {
        bookCatalog.add(book);
        saveBooks();
    }

    public void acceptPermanentDonation(User donor, String isbn, String title, String author, double rentalPrice) {
        if (isbnExists(isbn)) {
            System.out.println("\nError: ISBN " + isbn + " already exists. Please choose a different ISBN.");
            return;
        }
        
        Book book = new Book(isbn, title, author, rentalPrice);
        bookCatalog.add(book);
        saveBooks();
        System.out.println("\nThank you, " + donor.getName() + "!");
        System.out.println("'" + title + "' has been added to the library.");
    }
    
    public void acceptTemporaryLoan(User lender, String isbn, String title, String author, 
                                   double rentalPrice, int months) {
        if (months < 1) {
            System.out.println("Error: Minimum loan period is 1 month.");
            return;
        }
        
        if (isbnExists(isbn)) {
            System.out.println("\nError: ISBN " + isbn + " already exists. Please choose a different ISBN.");
            return;
        }
        
        Book book = new Book(isbn, title, author, rentalPrice);
        LocalDate lendUntilDate = LocalDate.now().plusMonths(months);
        book.setDonation(lender.getId(), "TEMPORARY", lendUntilDate);
        bookCatalog.add(book);
        saveBooks();
        
        System.out.println("\nThank you, " + lender.getName() + "!");
        System.out.println("'" + title + "' added to library.");
        System.out.println("Will be returned on: " + lendUntilDate);
    }
    
    public void processExpiredLoans() {
        List<Book> expiredBooks = new ArrayList<>();
        for (Book book : bookCatalog) {
            if (book.isTemporaryLoan() && book.isExpired()) {
                expiredBooks.add(book);
            }
        }
        
        if (!expiredBooks.isEmpty()) {
            System.out.println("\nNotice: " + expiredBooks.size() + " temporary loan(s) expired.");
            for (Book book : expiredBooks) {
                System.out.println("  - " + book.getTitle() + " (returned to donor)");
                bookCatalog.remove(book);
            }
            saveBooks();
        }
    }

    public void donateBook(Book book) {
        bookCatalog.add(book);
        System.out.println("Thank you for donating: " + book.getTitle());
    }

    public void registerUser(User user) {
        registeredUsers.add(user);
    }

    public void adoptBook(String isbn, int userId) throws BookNotFoundException, UserNotFoundException, BookNotAvailableException {
        Book book = findBookByIsbn(isbn);
        User user = findUserById(userId);

        if (!book.isFree()) {
            throw new BookNotAvailableException("Only free books can be adopted. This book costs $" + book.getRentalPricePerDay() + "/day.");
        }

        if (!book.isAvailable()) {
            throw new BookNotAvailableException("Book is not available");
        }

        // Remove from catalog since adopted books leave the library
        bookCatalog.remove(book);
        saveBooks();
        user.addBook(book);
        System.out.println("Book adopted by " + user.getName() + ". Donate it back to return it to the library.");
    }

    public void returnBook(String isbn, int userId) throws BookNotFoundException, UserNotFoundException {
        User user = findUserById(userId);
        
        if (user.getBooks().isEmpty()) {
            System.out.println("You have no borrowed books to return.");
            return;
        }
        
        Book book = findBookByIsbn(isbn);
        
        // Check if user actually borrowed this book
        boolean userHasBook = false;
        for (Book borrowedBook : user.getBooks()) {
            if (borrowedBook.getIsbn().equals(book.getIsbn())) {
                userHasBook = true;
                break;
            }
        }
        
        if (!userHasBook) {
            System.out.println("You haven't borrowed this book.");
            return;
        }
        
        double lateFee = book.calculateLateFee();
        
        book.returnBook();
        user.removeBook(book);
        saveBooks();
        
        System.out.println("Book returned successfully by " + user.getName());
        if (lateFee > 0) {
            System.out.println("Late fee: $" + String.format("%.2f", lateFee));
            System.out.println("(Book was " + book.getDaysLate() + " day(s) late at 50% rental rate)");
        }
    }

    public void rentBook(String isbn, int userId, int days) throws BookNotFoundException, UserNotFoundException, BookNotAvailableException {
        Book book = findBookByIsbn(isbn);
        User user = findUserById(userId);

        if (!book.isAvailable()) {
            throw new BookNotAvailableException("Book is not available for renting");
        }

        book.borrow();
        book.setRentalDueDate(days);
        user.addBook(book);
        RentTransaction transaction = new RentTransaction(book, user, days);
        activeRentalTransactions.add(transaction);
        saveBooks();
        
        System.out.println("Book rented successfully!");
        System.out.println(transaction);
        System.out.println("Due date: " + book.getDueDate());
        System.out.println("Late fee: 50% of rental rate per day after due date.");
    }

    public void showAllBooks() {
        if (bookCatalog.isEmpty()) {
            System.out.println("No books in the library");
            return;
        }

        List<Book> available = new ArrayList<>();
        List<Book> unavailable = new ArrayList<>();
        
        for (Book book : bookCatalog) {
            if (book.isAvailable()) {
                available.add(book);
            } else {
                unavailable.add(book);
            }
        }

        System.out.println("\n===== Available Books =====");
        if (available.isEmpty()) {
            System.out.println("None");
        } else {
            for (Book book : available) {
                System.out.println(book);
            }
        }
        
        System.out.println("\n===== Borrowed Books =====");
        if (unavailable.isEmpty()) {
            System.out.println("None");
        } else {
            for (Book book : unavailable) {
                System.out.println(book);
            }
        }
        System.out.println("==========================\n");
    }

    public void searchBookByTitle(String title) {
        List<Book> found = new ArrayList<>();
        for (Book book : bookCatalog) {
            if (book.getTitle().toLowerCase().contains(title.toLowerCase())) {
                found.add(book);
            }
        }

        if (found.isEmpty()) {
            System.out.println("No books found with title containing: " + title);
        } else {
            System.out.println("\n===== Search Results =====");
            for (Book book : found) {
                System.out.println(book);
            }
            System.out.println("==========================\n");
        }
    }
    
    public void showMyBooks(User user) {
        List<Book> books = user.getBooks();
        if (books.isEmpty()) {
            System.out.println("\nNo borrowed books.");
            return;
        }
        
        System.out.println("\n===== Your Borrowed Books =====");
        for (Book book : books) {
            System.out.println(book);
        }
        System.out.println("================================\n");
    }
    
    public void removeUser(User user) {
        registeredUsers.remove(user);
    }

    private Book findBookByIsbn(String isbn) throws BookNotFoundException {
        String normalizedIsbn = normalizeIsbn(isbn);
        
        for (Book book : bookCatalog) {
            if (normalizeIsbn(book.getIsbn()).equals(normalizedIsbn)) {
                return book;
            }
        }
        throw new BookNotFoundException("Book with ISBN " + isbn + " not found");
    }
    
    private String normalizeIsbn(String isbn) {
        // Strip leading zeros so "001" and "1" match
        return isbn.replaceFirst("^0+(?!$)", "").toLowerCase();
    }
    
    private boolean isbnExists(String isbn) {
        String normalizedIsbn = normalizeIsbn(isbn);
        for (Book book : bookCatalog) {
            if (normalizeIsbn(book.getIsbn()).equals(normalizedIsbn)) {
                return true;
            }
        }
        return false;
    }

    private User findUserById(int userId) throws UserNotFoundException {
        for (User user : registeredUsers) {
            if (user.getId() == userId) {
                return user;
            }
        }
        throw new UserNotFoundException("User with ID " + userId + " not found");
    }
}
