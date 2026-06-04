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
            System.out.println("No recent borrowing or return activity.");
        } else {
            System.out.println("\n<<-- Recent Library Transaction Records -->>");
            
            // 1. Create a temporary stack and copy all elements into it
            Stack<HistoryRecord> tempStack = new Stack<>();
            tempStack.addAll(stack);
            
            // 2. Pop elements one by one from the temp stack (this automatically gives LIFO order)
            while (!tempStack.isEmpty()) {
                HistoryRecord record = tempStack.pop(); // Pure Stack operation!
                Book b = record.getBook();
                System.out.println("[" + record.getAction().toUpperCase() + "] - "
                        + "[ISBN: " + b.getIsbn() + "] " + b.getTitle());
            }
            System.out.println("<<----------------------------------------------------->>");
        }
    }
}