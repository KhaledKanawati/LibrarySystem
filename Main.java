import java.util.Scanner;

public class Main {
    private static final Library library = new Library();
    private static final Scanner scanner = new Scanner(System.in);
    private static User currentUser = null;

    public static void main(String[] args) {
        DatabaseManager.initializeDatabase();
        initializeLibrary(); // Run once to add default books, then comment out again
        
        if (loginOrSignup()) {
            runMenu();
        }
        
        DatabaseManager.closeConnection();
    }

    private static void initializeLibrary() {
        library.addBook(new Book("1", "Harry Potter and the Sorcerer's Stone", "J.K. Rowling", 2.5));
        library.addBook(new Book("2", "Harry Potter and the Chamber of Secrets", "J.K. Rowling", 3.0));
        library.addBook(new Book("3", "Harry Potter and the Prisoner of Azkaban", "J.K. Rowling", 4.0));
        library.addBook(new Book("4", "1984", "George Orwell", 0.0));
    }
    
    private static boolean loginOrSignup() {
        while (true) {
            System.out.println("\n===== Welcome to Library Management System =====");
            System.out.println("1. Login");
            System.out.println("2. Sign Up");
            System.out.println("0. Exit");
            System.out.print("Choose an option: ");
            
            try {
                int choice = Integer.parseInt(scanner.nextLine());
                
                switch (choice) {
                    case 1 -> {
                        if (login()) return true;
                    }
                    case 2 -> {
                        if (signup()) return true;
                    }
                    case 0 -> {
                        System.out.println("Goodbye!");
                        return false;
                    }
                    default -> System.out.println("Invalid option. Please try again.");
                }
            } catch (NumberFormatException e) {
                System.out.println("Please enter a valid number.");
            }
        }
    }
    
    private static boolean login() {
        System.out.print("\nEnter username: ");
        String username = scanner.nextLine().trim();
        
        if (username.isEmpty()) {
            System.out.println("Error: Username cannot be blank.");
            return false;
        }
        
        System.out.print("Enter password: ");
        String password = scanner.nextLine();
        
        if (password.isEmpty()) {
            System.out.println("Error: Password cannot be blank.");
            return false;
        }
        
        currentUser = DatabaseManager.loginUser(username, password);
        
        if (currentUser != null) {
            System.out.println("\nWelcome back, " + currentUser.getName() + "!");
            library.registerUser(currentUser); // Add to library session
            return true;
        } else {
            System.out.println("Invalid username or password!");
            return false;
        }
    }
    
    private static boolean signup() {
        System.out.print("\nEnter username: ");
        String username = scanner.nextLine().trim();
        
        if (username.isEmpty()) {
            System.out.println("Error: Username cannot be blank.");
            return false;
        }
        
        if (DatabaseManager.usernameExists(username)) {
            System.out.println("Username already exists! Please try another.");
            return false;
        }
        
        System.out.print("Enter password: ");
        String password = scanner.nextLine();
        
        if (password.isEmpty()) {
            System.out.println("Error: Password cannot be blank.");
            return false;
        }
        
        System.out.print("Enter your full name: ");
        String name = scanner.nextLine().trim();
        
        if (name.isEmpty()) {
            System.out.println("Error: Name cannot be blank.");
            return false;
        }
        
        if (DatabaseManager.registerUser(username, password, name)) {
            System.out.println("\nAccount created successfully! Please login.");
            return false; // Make them login after signup
        } else {
            System.out.println("Registration failed. Please try again.");
            return false;
        }
    }

    private static void runMenu() {
        try (scanner) {
            boolean running = true;
            
            while (running) {
                library.processExpiredLoans();
                
                printMenu();
                
                try {
                    int userChoice = Integer.parseInt(scanner.nextLine());
                    
                    switch (userChoice) {
                        case 1 -> library.showAllBooks();
                        case 2 -> searchBook();
                        case 3 -> adoptBook();
                        case 4 -> rentBook();
                        case 5 -> returnBook();
                        case 6 -> library.showMyBooks(currentUser);
                        case 7 -> donateBookToLibrary();
                        case 8 -> lendBookToLibrary();
                        case 9 -> deleteAccount();
                        case 10 -> {
                            System.out.println("\nLogging out...");
                            currentUser = null;
                            if (!loginOrSignup()) {
                                running = false;
                            }
                        }
                        case 0 -> {
                            System.out.println("Goodbye!");
                            running = false;
                        }
                        default -> System.out.println("Invalid option. Please try again.");
                    }
                    
                } catch (NumberFormatException e) {
                    System.out.println("Please enter a valid number.");
                } catch (Exception e) {
                    System.out.println("Error: " + e.getMessage());
                }
                
                System.out.println();
            }
        }
    }
    // better than convential switch and was giving warning till I switched
    private static void printMenu() {
        System.out.println("\n===== Library Management System =====");
        System.out.println("Logged in as: " + currentUser.getName());
        System.out.println("--------------------------------------");
        System.out.println(" Books:");
        System.out.println("  1. Show all books");
        System.out.println("  2. Search book by title");
        System.out.println("  3. Adopt book (free only)");
        System.out.println("  4. Rent book (paid)");
        System.out.println("  5. Return book");
        System.out.println("  6. View my borrowed books");
        System.out.println("\n Give Books to Library:");
        System.out.println("  7. Donate book permanently");
        System.out.println("  8. Lend book temporarily");
        System.out.println("\n  Account:");
        System.out.println("  9. Delete my account");
        System.out.println("  10. Logout");
        System.out.println("  0. Exit");
        System.out.print("\nChoose an option: ");
    }

    private static void searchBook() {
        System.out.print("Enter title to search: ");
        String title = scanner.nextLine().trim();
        
        if (title.isEmpty()) {
            System.out.println("Error: Title cannot be blank.");
            return;
        }
        
        library.searchBookByTitle(title);
    }

    private static void adoptBook() {
        try {
            System.out.print("Enter ISBN (e.g., 1, 001, or 50000): ");
            String isbn = scanner.nextLine().trim();
            
            if (isbn.isEmpty()) {
                System.out.println("Error: ISBN cannot be blank.");
                return;
            }

            library.adoptBook(isbn, currentUser.getId());
        } catch (BookNotFoundException | UserNotFoundException | BookNotAvailableException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    private static void rentBook() {
        try {
            System.out.print("Enter ISBN (e.g., 1, 001, or 50000): ");
            String isbn = scanner.nextLine().trim();
            
            if (isbn.isEmpty()) {
                System.out.println("Error: ISBN cannot be blank.");
                return;
            }
            
            System.out.print("Enter number of days: ");
            String daysInput = scanner.nextLine().trim();
            
            if (daysInput.isEmpty()) {
                System.out.println("Error: Number of days cannot be blank.");
                return;
            }
            
            int days = Integer.parseInt(daysInput);
            
            if (days <= 0) {
                System.out.println("Error: Number of days must be positive.");
                return;
            }

            library.rentBook(isbn, currentUser.getId(), days);
        } catch (NumberFormatException e) {
            System.out.println("Error: Please enter a valid number for days.");
        } catch (BookNotFoundException | UserNotFoundException | BookNotAvailableException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    private static void returnBook() {
        try {
            System.out.print("Enter ISBN (e.g., 1, 001, or 50000): ");
            String isbn = scanner.nextLine().trim();
            
            if (isbn.isEmpty()) {
                System.out.println("Error: ISBN cannot be blank.");
                return;
            }

            library.returnBook(isbn, currentUser.getId());
        } catch (BookNotFoundException | UserNotFoundException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }
    
    private static void deleteAccount() { 
        System.out.println("\nWARNING: This will delete your account permanently!");
        System.out.print("Enter password to confirm: ");
        String password = scanner.nextLine();
        
        if (DatabaseManager.deleteUser(currentUser.getId(), password)) {
            System.out.println("\nAccount deleted successfully. Goodbye!");
            library.removeUser(currentUser);
            currentUser = null;
            System.exit(0);
        } else {
            System.out.println("Failed to delete account. Incorrect password or error occurred.");
        }
    }
    
    private static void donateBookToLibrary() {
        System.out.println("\n=== Donate a Book ===");
        System.out.println("The book becomes library property. You won't get it back.\n");
        
        System.out.print("Enter ISBN for your book: ");
        String isbn = scanner.nextLine().trim();
        
        if (isbn.isEmpty()) {
            System.out.println("Error: ISBN cannot be blank.");
            return;
        }
        
        System.out.print("Enter title: ");
        String title = scanner.nextLine().trim();
        
        if (title.isEmpty()) {
            System.out.println("Error: Title cannot be blank.");
            return;
        }
        
        System.out.print("Enter author: ");
        String author = scanner.nextLine().trim();
        
        if (author.isEmpty()) {
            System.out.println("Error: Author cannot be blank.");
            return;
        }
        
        System.out.print("Enter suggested rental price per day ($): ");
        String priceInput = scanner.nextLine().trim();
        
        if (priceInput.isEmpty()) {
            System.out.println("Error: Price cannot be blank.");
            return;
        }
        
        try {
            double price = Double.parseDouble(priceInput);
            
            if (price < 0) {
                System.out.println("Error: Price cannot be negative.");
                return;
            }
            
            library.acceptPermanentDonation(currentUser, isbn, title, author, price);
        } catch (NumberFormatException e) {
            System.out.println("Error: Please enter a valid price.");
        }
    }
    
    private static void lendBookToLibrary() {
        System.out.println("\n=== Lend a Book Temporarily ===");
        System.out.println("Book will be available for the loan period, then returned to you.\n");
        
        System.out.print("Enter ISBN for your book: ");
        String isbn = scanner.nextLine().trim();
        
        if (isbn.isEmpty()) {
            System.out.println("Error: ISBN cannot be blank.");
            return;
        }
        
        System.out.print("Enter title: ");
        String title = scanner.nextLine().trim();
        
        if (title.isEmpty()) {
            System.out.println("Error: Title cannot be blank.");
            return;
        }
        
        System.out.print("Enter author: ");
        String author = scanner.nextLine().trim();
        
        if (author.isEmpty()) {
            System.out.println("Error: Author cannot be blank.");
            return;
        }
        
        System.out.print("Enter suggested rental price per day ($): ");
        String priceInput = scanner.nextLine().trim();
        
        if (priceInput.isEmpty()) {
            System.out.println("Error: Price cannot be blank.");
            return;
        }
        
        System.out.print("Enter loan period in months (minimum 1): ");
        String monthsInput = scanner.nextLine().trim();
        
        if (monthsInput.isEmpty()) {
            System.out.println("Error: Loan period cannot be blank.");
            return;
        }
        
        try {
            double price = Double.parseDouble(priceInput);
            int months = Integer.parseInt(monthsInput);
            
            if (price < 0) {
                System.out.println("Error: Price cannot be negative.");
                return;
            }
            
            if (months < 1) {
                System.out.println("Error: Loan period must be at least 1 month.");
                return;
            }
            
            library.acceptTemporaryLoan(currentUser, isbn, title, author, price, months);
        } catch (NumberFormatException e) {
            System.out.println("Error: Please enter valid numbers for price and months.");
        }
    }
}
