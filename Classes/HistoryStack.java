package Classes;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

public class HistoryStack {

    private Stack<HistoryRecord> stack = new Stack<>();

    public void push(Book b, String action) {
        stack.push(new HistoryRecord(b, action));
    }

    public List<HistoryRecord> getAllRecords() {
        return new ArrayList<>(stack);
    }

    public void show() {
        if (stack.isEmpty()) {
            System.out.println("History is empty. No recent activity.");
        } else {
            System.out.println("\n--- Library Transaction History (Most Recent First) ---");
            for (int i = stack.size() - 1; i >= 0; i--) {
                HistoryRecord record = stack.get(i);
                Book b = record.getBook();
                System.out.println("[" + record.getAction().toUpperCase() + "] - "
                        + "[ISBN: " + b.getIsbn() + "] " + b.getTitle());
            }
            System.out.println("-------------------------------------------------------");
        }
    }
}
