import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

public class Book implements Serializable {
    private final String isbn;
    private final String title;
    private final String author;
    private boolean available;
    private final double rentalPricePerDay;
    
    private Integer donorUserId;
    private String donationType;
    private LocalDate lendUntilDate;
    private LocalDateTime borrowedAt;
    private LocalDate rentalDueDate;

    public Book(String isbn, String title, String author, double rentalPricePerDay) {
        this.isbn = isbn;
        this.title = title;
        this.author = author;
        this.available = true;
        this.rentalPricePerDay = rentalPricePerDay;
        this.donorUserId = null;
        this.donationType = null;
        this.lendUntilDate = null;
    }

    public void borrow() {
        this.available = false;
        this.borrowedAt = LocalDateTime.now();
    }

    public void returnBook() {
        this.available = true;
        this.borrowedAt = null;
        this.rentalDueDate = null;
    }

    public String getIsbn() {
        return isbn;
    }

    public String getTitle() {
        return title;
    }

    public String getAuthor() {
        return author;
    }

    public boolean isAvailable() {
        return available;
    }

    public double getRentalPricePerDay() {
        return rentalPricePerDay;
    }
    
    public void setDonation(int userId, String donationType, LocalDate lendUntilDate) {
        this.donorUserId = userId;
        this.donationType = donationType;
        this.lendUntilDate = lendUntilDate;
    }
    
    public Integer getDonorUserId() {
        return donorUserId;
    }
    
    public String getDonationType() {
        return donationType;
    }
    
    public LocalDate getLendUntilDate() {
        return lendUntilDate;
    }
    
    public boolean isTemporaryLoan() {
        return "TEMPORARY".equals(donationType);
    }
    
    public boolean isExpired() {
        if (lendUntilDate == null) return false;
        return LocalDate.now().isAfter(lendUntilDate);
    }
    
    public boolean isFree() {
        return rentalPricePerDay == 0.0;
    }
    
    public String getBorrowDuration() {
        if (borrowedAt == null) return "N/A";
        long days = ChronoUnit.DAYS.between(borrowedAt, LocalDateTime.now());
        if (days == 0) return "Today";
        if (days == 1) return "1 day";
        return days + " days";
    }
    
    public void setRentalDueDate(int days) {
        this.rentalDueDate = LocalDate.now().plusDays(days);
    }
    
    public LocalDate getDueDate() {
        return rentalDueDate;
    }
    
    public int getDaysLate() {
        if (rentalDueDate == null) return 0;
        long days = ChronoUnit.DAYS.between(rentalDueDate, LocalDate.now());
        return days > 0 ? (int) days : 0;
    }
    
    public double calculateLateFee() {
        int daysLate = getDaysLate();
        if (daysLate <= 0) return 0.0;
        // Late fee is 50% of the regular rental rate
        return daysLate * rentalPricePerDay * 0.5;
    }

    @Override
    public String toString() {
        // Remove leading zeros for display (001 shows as 1)
        String normalizedIsbn = isbn;
        while (normalizedIsbn.startsWith("0") && normalizedIsbn.length() > 1) {
            normalizedIsbn = normalizedIsbn.substring(1);
        }
        
        String priceInfo = isFree() ? "Free" : "$" + String.format("%.2f", rentalPricePerDay) + "/day";
        String statusInfo = available ? "Available" : "Borrowed (" + getBorrowDuration() + ")";
        
        return String.format("[ISBN: %s] %s by %s - %s - %s", 
            normalizedIsbn, title, author, priceInfo, statusInfo);
    }
}
