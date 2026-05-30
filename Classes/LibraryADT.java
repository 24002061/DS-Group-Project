package Classes;

public interface LibraryADT {

    void addBook(int isbn, String title, String author, int copies);

    void addCopiesToBook(int isbn, int copies);

    void searchBookByIsbn(int isbn);

    void searchBookByTitle(String title);

    void searchBookByAuthor(String author);

    void borrowBook(int isbn);

    void returnBook(int isbn);

    void viewLatestHistory();

    void viewBorrowedBooks();

    void deleteBook();

    void printWholeCatalogue();
}
