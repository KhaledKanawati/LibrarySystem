
// Class RentTransaction to manage rental transactions
public class RentTransaction {
    private final Book rentedBook;
    private final User rentingUser;
    private final int rentalDurationDays;
    private final double totalRentalCost;

    public RentTransaction(Book book, User user, int days) {
        this.rentedBook = book;
        this.rentingUser = user;
        this.rentalDurationDays = days;
        this.totalRentalCost = book.getRentalPricePerDay() * days;
    }

    public Book getBook() {
        return rentedBook;
    }

    public User getUser() {
        return rentingUser;
    }

    public int getDays() {
        return rentalDurationDays;
    }

    public double getTotalCost() {
        return totalRentalCost;
    }

    @Override
    public String toString() {
        return String.format("Rental: %s for %d days - Total: $%.2f", 
            rentedBook.getTitle(), rentalDurationDays, totalRentalCost);
    }
}
