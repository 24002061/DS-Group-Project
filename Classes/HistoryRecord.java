package Classes;

public class HistoryRecord {

    private Book book;
    private String action;

    public HistoryRecord(Book book, String action) {
        this.book = book;
        this.action = action;
    }

    public Book getBook() {
        return book;
    }

    public String getAction() {
        return action;
    }

    public String toCSV() {
        return book.getIsbn() + "," + action;
    }
}
