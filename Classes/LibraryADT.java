package Classes;

public interface LibraryADT {

    // System Orchestration
    void runMenu(); // <-- Added here to hide implementation specifics from Main!

    // Inventory Management (Librarian)
    void addBook(int isbn, String title, String author, int copies);

    void addCopiesToBook(int isbn, int copies);

    void deleteBook(); 

    // Search Functions
    void searchBookByIsbn(int isbn);

    void searchBookByTitle(String title);

    void searchBookByAuthor(String author);

    // Transaction Functions (Student/Librarian)
    void borrowBook(int isbn);

    void returnBook(int isbn);

    // View & Audit Functions
    void viewLatestHistory();

    void viewBorrowedBooks();

    void printWholeCatalogue();
}