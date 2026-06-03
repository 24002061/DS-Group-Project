package Classes;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Scanner;

public class SmartLibrary implements LibraryADT {

    private BookBST catalogue = new BookBST();
    private HistoryStack history = new HistoryStack();
    private Scanner sc = new Scanner(System.in);
    private HashMap<String, Book> titleIndex = new HashMap<>();
    private HashMap<String, List<Book>> authorIndex = new HashMap<>();
    private String userRole = "";
    private List<Integer> sessionBorrowedIsbns = new ArrayList<>();

    private final String CATALOGUE_FILE = "Database/catalogue.csv";
    private final String HISTORY_FILE = "Database/history.csv";

    public SmartLibrary() {
        loadData();
    }

    private void loadData() {
        try {
            File catFile = new File(CATALOGUE_FILE);
            if (catFile.exists()) {
                Scanner reader = new Scanner(catFile);
                while (reader.hasNextLine()) {
                    String[] data = reader.nextLine().split(",");
                    if (data.length == 5) {
                        Book b = new Book(Integer.parseInt(data[0]), data[1], data[2],
                                Integer.parseInt(data[3]), Integer.parseInt(data[4]));
                        catalogue.insert(b);
                        titleIndex.put(b.getTitle().toLowerCase(), b);
                        authorIndex.putIfAbsent(b.getAuthor().toLowerCase(), new ArrayList<>());
                        authorIndex.get(b.getAuthor().toLowerCase()).add(b);
                    }
                }
                reader.close();
            }

            File histFile = new File(HISTORY_FILE);
            if (histFile.exists()) {
                Scanner reader = new Scanner(histFile);
                while (reader.hasNextLine()) {
                    String[] data = reader.nextLine().split(",");
                    if (data.length == 2) {
                        Book b = catalogue.search(Integer.parseInt(data[0]));
                        if (b != null) {
                            history.push(b, data[1]);
                        }
                    }
                }
                reader.close();
            }
            System.out.println("System Initialised: Data loaded successfully.");
        } catch (FileNotFoundException e) {
            System.out.println("System Initialised: No previous save data found. Starting fresh.");
        } catch (Exception e) {
            System.out.println("Warning: Could not perfectly load all save data.");
        }
    }

    private void saveData() {
        try {
            PrintWriter catWriter = new PrintWriter(CATALOGUE_FILE);
            for (Book b : catalogue.getAllBooks()) {
                catWriter.println(b.toCSV());
            }
            catWriter.close();

            PrintWriter histWriter = new PrintWriter(HISTORY_FILE);
            for (HistoryRecord r : history.getAllRecords()) {
                histWriter.println(r.toCSV());
            }
            histWriter.close();

            System.out.println("Data saved successfully.");
        } catch (IOException e) {
            System.out.println("Error: Could not save data to files.");
        }
    }

    @Override
    public void addBook(int isbn, String title, String author, int copies) {
        if (catalogue.search(isbn) != null) {
            System.out.println("Error: A book with ISBN " + isbn + " already exists.");
            return;
        }

        Book newBook = new Book(isbn, title, author, copies);
        catalogue.insert(newBook);
        titleIndex.put(title.toLowerCase(), newBook);
        authorIndex.putIfAbsent(author.toLowerCase(), new ArrayList<>());
        authorIndex.get(author.toLowerCase()).add(newBook);

        System.out.println("Success: '" + title + "' added to the catalogue with " + copies + " copies.");
    }

    @Override
    public void addCopiesToBook(int isbn, int copies) {
        Book b = catalogue.search(isbn);
        if (b != null) {
            b.addCopies(copies);
            System.out.println("Success: Added " + copies + " copies. '" + b.getTitle() + "' now has " + b.getTotalCopies() + " total copies.");
        } else {
            System.out.println("Error: Book with ISBN " + isbn + " not found.");
        }
    }

    @Override
    public void deleteBook() {
        try {
            System.out.print("Enter ISBN to delete: ");
            int isbn = Integer.parseInt(sc.nextLine().trim());
            if (isbn <= 0) {
                System.out.println("Error: ISBN must be a positive number.");
                return;
            }
            Book b = catalogue.search(isbn);

            if (b == null) {
                System.out.println("Error: Book with ISBN " + isbn + " not found.");
                return;
            }

            System.out.println("\nSelected: " + b.toString());
            System.out.println("Options:");
            System.out.println(" - Enter a [number] to delete that many available copies.");
            System.out.println(" - Type 'all' to delete ALL available copies.");
            System.out.println(" - Type 'purge' to completely remove the book from the system.");
            System.out.println(" - Type 'exit' to cancel and return to the menu."); // <-- NEW OPTION
            System.out.print("Choice: ");

            String choice = sc.nextLine().trim().toLowerCase();

            // Check for exit first
            if (choice.equals("exit") || choice.equals("cancel")) {
                System.out.println("Deletion cancelled. Returning to menu.");
                return;
            } else if (choice.equals("purge")) {
                if (b.getAvailableCopies() < b.getTotalCopies()) {
                    System.out.println("Error: Cannot purge book. " + (b.getTotalCopies() - b.getAvailableCopies()) + " copies are currently borrowed.");
                } else {
                    catalogue.delete(isbn);
                    titleIndex.remove(b.getTitle().toLowerCase());
                    if (authorIndex.containsKey(b.getAuthor().toLowerCase())) {
                        authorIndex.get(b.getAuthor().toLowerCase()).remove(b);
                    }
                    System.out.println("Success: All records of '" + b.getTitle() + "' have been deleted.");
                }
            } else if (choice.equals("all")) {
                if (b.getAvailableCopies() == 0) {
                    System.out.println("Notice: No available copies to delete.");
                } else {
                    int removed = b.getAvailableCopies();
                    b.removeCopies(removed);
                    System.out.println("Success: Removed " + removed + " copies. " + b.getTotalCopies() + " total copies remain.");
                }
            } else {
                int amount = Integer.parseInt(choice);
                if (amount <= 0) {
                    System.out.println("Error: Amount must be greater than 0.");
                } else if (amount > b.getAvailableCopies()) {
                    System.out.println("Error: Cannot delete " + amount + " copies. Only " + b.getAvailableCopies() + " are available.");
                } else {
                    b.removeCopies(amount);
                    System.out.println("Success: Removed " + amount + " copies. " + b.getAvailableCopies() + " available copies remain.");
                }
            }
        } catch (NumberFormatException e) {
            System.out.println("Error: Invalid input. Expected a number, 'all', 'purge', or 'exit'.");
        }
    }

    @Override
    public void searchBookByIsbn(int isbn) {
        Book b = catalogue.search(isbn);
        if (b != null) {
            System.out.println("Found: " + b.toString());
        } else {
            System.out.println("Result: Book with ISBN " + isbn + " not found.");
        }
    }

    @Override
    public void searchBookByTitle(String title) {
        Book b = titleIndex.get(title.toLowerCase());
        if (b != null) {
            System.out.println("Found: " + b.toString());
        } else {
            System.out.println("Result: No book found with title '" + title + "'.");
        }
    }

    @Override
    public void searchBookByAuthor(String author) {
        List<Book> books = authorIndex.get(author.toLowerCase());
        if (books != null && !books.isEmpty()) {
            System.out.println("Found books by " + author + ":");
            for (Book b : books) {
                System.out.println(b.toString());
            }
        } else {
            System.out.println("Result: No books found by author '" + author + "'.");
        }
    }

    @Override
    public void borrowBook(int isbn) {
        if (isbn <= 0) {
            System.out.println("Error: ISBN must be a positive number.");
            return;
        }
        Book b = catalogue.search(isbn);
        if (b != null) {
            if (b.getAvailableCopies() > 0) {
                b.borrowCopy();
                sessionBorrowedIsbns.add(isbn);
                history.push(b, "Borrowed");
                System.out.println("Success: You borrowed '" + b.getTitle() + "'. " + b.getAvailableCopies() + " copies remaining.");
            } else {
                System.out.println("Error: '" + b.getTitle() + "' is currently out of stock.");
            }
        } else {
            System.out.println("Error: Book with ISBN " + isbn + " does not exist in the catalogue.");
        }
    }

    @Override
    public void returnBook(int isbn) {
        if (isbn <= 0) {
            System.out.println("Error: ISBN must be a positive number.");
            return;
        }
        if (!sessionBorrowedIsbns.contains(isbn)) {
            System.out.println("Error: You have not borrowed a book with ISBN " + isbn + " in this session.");
            return;
        }
        Book b = catalogue.search(isbn);
        if (b != null) {
            if (b.getAvailableCopies() < b.getTotalCopies()) {
                b.returnCopy();
                sessionBorrowedIsbns.remove(Integer.valueOf(isbn));
                history.push(b, "Returned");
                System.out.println("Success: You returned '" + b.getTitle() + "'. " + b.getAvailableCopies() + " copies now available.");
            } else {
                System.out.println("Error: All known copies of '" + b.getTitle() + "' are already in the library.");
            }
        } else {
            System.out.println("Error: This library does not own a book with ISBN " + isbn + ".");
        }
    }

    @Override
    public void viewLatestHistory() {
        history.show();
    }

    @Override
    public void viewBorrowedBooks() {
        System.out.println("\n<<--- Currently Borrowed Books --->>");
        List<Book> allBooks = catalogue.getAllBooks();
        boolean found = false;

        for (Book b : allBooks) {
            int borrowedAmount = b.getTotalCopies() - b.getAvailableCopies();
            if (borrowedAmount > 0) {
                System.out.println("[ISBN: " + b.getIsbn() + "] " + b.getTitle()
                        + " | Copies Out: " + borrowedAmount);
                found = true;
            }
        }

        if (!found) {
            System.out.println("All books are currently in the library.");
        }
        System.out.println("<<-------------------------------->>");
    }

    @Override
    public void printWholeCatalogue() {
        catalogue.printInOrder();
    }

    public void runMenu() {
        System.out.println("\nWelcome to the Smart Library System");

        while (true) {
            System.out.println("\n--- Login ---");
            System.out.println("Are you logging in as a [1] Student, [2] Librarian, or [3] Shut Down?");

            while (true) {
                System.out.print("Choice: ");
                String roleChoice = sc.nextLine().trim();
                if (roleChoice.equals("1")) {
                    userRole = "Student";
                    break;
                } else if (roleChoice.equals("2")) {
                    userRole = "Librarian";
                    break;
                } else if (roleChoice.equals("3")) {
                    System.out.println("Saving and shutting down. Goodbye!");
                    saveData();
                    return;
                }
                System.out.println("Invalid choice. Enter 1, 2, or 3.");
            }

            System.out.println("\nLogged in successfully as: " + userRole);

            boolean loggedIn = true;
            while (loggedIn) {
                printMenu();
                System.out.print("Enter Command: ");
                String command = sc.nextLine().trim().toLowerCase(); // Normalize input

                // Admin security checkpoint using semantic words
                if (userRole.equals("Student") && (command.equals("add") || command.equals("restock") || command.equals("delete"))) {
                    System.out.println("Permission Denied: Only Librarians can manage library inventory.");
                    continue;
                }

                if (command.equals("logout") || command.equals("10")) {
                    System.out.println("Logging out...");
                    loggedIn = false;
                    break;
                }

                if (command.equals("exit") || command.equals("quit") || command.equals("11")) {
                    System.out.println("Saving and shutting down. Goodbye!");
                    saveData();
                    return;
                }

                handleChoice(command);
            }
        }
    }

    private void printMenu() {
        System.out.println("\n<<----- SmartLibrary CLI Navigation (" + userRole + ") ----->>");
        System.out.println("Type one of the following command keywords to execute an action:\n");

        if (userRole.equals("Librarian")) {
            System.out.printf("  %-12s -> %s\n", "[1] add", "Register a completely new book title");
            System.out.printf("  %-12s -> %s\n", "[2] restock", "Add physical copies to an existing book");
            System.out.printf("  %-12s -> %s\n", "[3] delete", "Remove copies or purge a book entirely");
        } else {
            System.out.println("  [Inventory Management Commands Locked for Students]");
        }

        System.out.printf("  %-12s -> %s\n", "[4] search", "Find a book via ISBN, Title, or Author");
        System.out.printf("  %-12s -> %s\n", "[5] borrow", "Checkout a book copy");
        System.out.printf("  %-12s -> %s\n", "[6] return", "Check-in a borrowed book copy");
        System.out.printf("  %-12s -> %s\n", "[7] history", "View the chronological library audit log");
        System.out.printf("  %-12s -> %s\n", "[8] borrowed", "List all books currently missing copies");
        System.out.printf("  %-12s -> %s\n", "[9] catalog", "Print the complete library collection sorted by ISBN");
        System.out.printf("  %-12s -> %s\n", "[10] logout", "Log out of current profile back to main login panel");
        System.out.printf("  %-12s -> %s\n", "[11] exit", "Save database metrics and safely kill the application process");
        System.out.println("<<----------------------------------------------------------------->>");
    }

    private void handleChoice(String command) {
        switch (command) {
            case "add", "1":
                try {
                    System.out.print("Enter ISBN (Numbers only): ");
                    int isbn = Integer.parseInt(sc.nextLine().trim());
                    if (isbn <= 0) {
                        System.out.println("Error: ISBN must be a positive number.");
                        break;
                    }
                    System.out.print("Enter Title: ");
                    String title = sc.nextLine().replace(",", " ").trim();
                    if (title.isEmpty()) {
                        System.out.println("Error: Title cannot be empty.");
                        break;
                    }
                    System.out.print("Enter Author: ");
                    String author = sc.nextLine().replace(",", " ").trim();
                    if (author.isEmpty()) {
                        System.out.println("Error: Author cannot be empty.");
                        break;
                    }
                    System.out.print("Enter Initial Number of Copies: ");
                    int copies = Integer.parseInt(sc.nextLine().trim());
                    if (copies < 1) {
                        System.out.println("Error: Number of copies must be at least 1.");
                        break;
                    }
                    addBook(isbn, title, author, copies);
                } catch (NumberFormatException e) {
                    System.out.println("Error: Numeric inputs must be valid integers.");
                }
                break;

            case "restock", "2":
                try {
                    System.out.print("Enter ISBN to restock: ");
                    int isbn = Integer.parseInt(sc.nextLine().trim());
                    if (isbn <= 0) {
                        System.out.println("Error: ISBN must be a positive number.");
                        break;
                    }
                    System.out.print("Enter number of additional copies: ");
                    int copies = Integer.parseInt(sc.nextLine().trim());
                    if (copies < 1) {
                        System.out.println("Error: Number of copies must be greater than 0.");
                        break;
                    }
                    addCopiesToBook(isbn, copies);
                } catch (NumberFormatException e) {
                    System.out.println("Error: Numeric inputs must be valid integers.");
                }
                break;

            case "delete", "3":
                deleteBook();
                break;

            case "search", "4":
                System.out.println("Search options: [1] ISBN [2] Title [3] Author");
                System.out.print("Choice: ");
                String searchType = sc.nextLine().trim();

                if (searchType.equals("1")) {
                    try {
                        System.out.print("Enter ISBN: ");
                        searchBookByIsbn(Integer.parseInt(sc.nextLine().trim()));
                    } catch (NumberFormatException e) {
                        System.out.println("Error: ISBN must be a valid integer.");
                    }
                } else if (searchType.equals("2")) {
                    System.out.print("Enter Title: ");
                    String titleQuery = sc.nextLine().trim();
                    if (titleQuery.isEmpty()) {
                        System.out.println("Error: Title cannot be empty.");
                    } else {
                        searchBookByTitle(titleQuery);
                    }
                } else if (searchType.equals("3")) {
                    System.out.print("Enter Author: ");
                    String authorQuery = sc.nextLine().trim();
                    if (authorQuery.isEmpty()) {
                        System.out.println("Error: Author name cannot be empty.");
                    } else {
                        searchBookByAuthor(authorQuery);
                    }
                } else {
                    System.out.println("Invalid search options context.");
                }
                break;

            case "borrow", "5":
                try {
                    System.out.print("Enter ISBN to borrow: ");
                    borrowBook(Integer.parseInt(sc.nextLine().trim()));
                } catch (NumberFormatException e) {
                    System.out.println("Error: ISBN must be a valid integer.");
                }
                break;

            case "return", "6":
                try {
                    System.out.print("Enter ISBN to return: ");
                    returnBook(Integer.parseInt(sc.nextLine().trim()));
                } catch (NumberFormatException e) {
                    System.out.println("Error: ISBN must be a valid integer.");
                }
                break;

            case "history", "7":
                viewLatestHistory();
                break;

            case "borrowed", "8":
                viewBorrowedBooks();
                break;

            case "catalog", "9":
                printWholeCatalogue();
                break;

            default:
                System.out.println("Unknown command flag. Look at the guidelines chart above for reference.");
        }
    }
}
