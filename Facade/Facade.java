/*
The Facade Design Pattern is a structural pattern that provides a simplified interface to a complex subsystem or group of classes. It’s commonly used when you want to hide the complexities of a system and make it easier to use. The facade pattern offers a high-level interface that makes a subsystem easier to interact with by providing a “facade” or "wrapper" around a set of other classes.

Key Points of the Facade Pattern
Simplifies interaction: The pattern simplifies interactions with complex subsystems by exposing only necessary functionality.
Decouples client and subsystem: It reduces the dependency between the client and the subsystem.
Increases readability: By providing a clean and easy-to-understand interface, it makes the code more readable.
Example Scenario
Imagine you’re working on a complex library system where multiple classes handle different responsibilities, like managing books, members, and inventory. The facade pattern can provide a single entry point to interact with this system, simplifying the library management process.

Implementation Example in Java
Let’s say we have a system with several subsystems:

BookInventory
Membership
Billing
The facade class, LibraryFacade, will simplify interactions with these classes.
*/

// Step 1 : Define the subclass system

class BookInventory {
    public void findBook(String bookTitle) {
        System.out.println("Searching for the book: " + bookTitle);
    }

    public void reserveBook(String bookTitle) {
        System.out.println("Reserving the book: " + bookTitle);
    }
}

class Membership {
    public boolean isMember(String memberId) {
        System.out.println("Checking membership for: " + memberId);
        return true; // Assume the member exists for simplicity
    }
}

class Billing {
    public void processPayment(String memberId, double amount) {
        System.out.println("Processing payment of $" + amount + " for member: " + memberId);
    }
}

// Step 2 : Create the Facade class
/* 
The LibraryFacade class will provide a simple interface to perform multiple actions with one method call.
*/

class LibraryFacade {
    private BookInventory bookInventory;
    private Membership membership;
    private Billing billing;

    public LibraryFacade() {
        this.bookInventory = new BookInventory();
        this.membership = new Membership();
        this.billing = new Billing();
    }

    public void borrowBook(String bookTitle, String memberId) {
        if (membership.isMember(memberId)) {
            bookInventory.findBook(bookTitle);
            bookInventory.reserveBook(bookTitle);
            billing.processPayment(memberId, 5.0); // Assume fixed borrowing fee
            System.out.println("Borrowing process completed for book: " + bookTitle);
        } else {
            System.out.println("Invalid member ID: " + memberId);
        }
    }
}

// Step 3 : Use the Facade in Client Code 
/* The client interacts only with the LibraryFacade, making the borrowing process easier to understand and use. */
public class FacadePatternExample {
    public static void main(String[] args) {
        LibraryFacade libraryFacade = new LibraryFacade();
        
        String bookTitle = "Design Patterns";
        String memberId = "M123";
        
        libraryFacade.borrowBook(bookTitle, memberId);
    }
}

/*
Advantages of Using the Facade Pattern

Simplifies usage: By hiding the subsystem’s complexity, clients can easily perform actions without understanding underlying details.

Reduces coupling: The client interacts only with the facade, reducing dependency on subsystem classes.

Improves maintenance: Changes in subsystem classes won’t affect the client if the facade remains the same.

  When to Use the Facade Pattern
  
When you need to simplify a complex set of interactions in your code.
When you want to provide a single entry point to a subsystem or set of functionalities.
When you’re working with a legacy codebase that has many interacting parts, and you need to provide a simpler interface for new code.

The facade pattern is particularly useful in scenarios where you’re developing an API or SDK to offer streamlined functionality while hiding the complex internals.

*/




  
