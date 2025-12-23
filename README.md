# Library Management System

A simple library system for managing books and rentals with user accounts using Java.

## Requirements

- Java 14 or higher

## Getting Started

```bash
java Main
```

No setup required. Just compile and run.
## Features

### Book Management
- Browse all available and borrowed books
- Search by title
- Two types of books:
  - **Free books** - Adopt them (removed from library until you donate back)
  - **Paid books** - Rent them (daily rate, due dates, late fees)

### Donations
- Donate books permanently to the library
- Lend books temporarily (auto-returned after set period)

### User System
- Sign up and login (usernames are case-insensitive)
- View your borrowed books
- Delete your account anytime

### Late Fees
When you return a rented book late, you pay 50% of the daily rate for each overdue day.

## How Books Work

**Free books (price = $0):**
- Can only be adopted
- Removed from catalog when adopted
- Return by donating back

**Paid books (price > $0):**
- Can only be rented
- Stay in catalog while borrowed
- Shows rental duration and due date

## ISBN System

Use simple numbers like 1, 2, 3. The system handles formatting automatically.
- Input "001" or "1" both work
- No duplicate ISBNs allowed
- Displays without leading zeros

## Storage

Everything saves automatically to files:
- `books.dat` - book catalog
- `users.dat` - user accounts

## First Run

Uncomment `initializeLibrary()` in Main.java, run once to create starter books, then comment it out again.

## Input Validation

All inputs are validated:
- No blank fields
- Usernames: letters, numbers, underscore only
- Positive numbers for days and prices
- Can only return books you borrowed


