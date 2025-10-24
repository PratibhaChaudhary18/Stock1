import java.io.*;
import java.util.*;

// STOCK CLASS (Encapsulation)
class Stock implements Serializable {
    private String symbol;
    private double price;

    public Stock(String symbol, double price) {
        this.symbol = symbol;
        this.price = price;
    }

    public String getSymbol() { return symbol; }
    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }

    @Override
    public String toString() {
        return symbol + " @ ₹" + price;
    }
}

// ABSTRACT TRANSACTION CLASS (Abstraction)
abstract class Transaction implements Serializable {
    protected Stock stock;
    protected int quantity;
    protected Date date;

    public Transaction(Stock stock, int quantity) {
        this.stock = stock;
        this.quantity = quantity;
        this.date = new Date();
    }

    public abstract void execute(User user);

    @Override
    public String toString() {
        return date + " | " + stock.getSymbol() + " | Qty: " + quantity;
    }
}

// BUY TRANSACTION (Inheritance + Polymorphism)
class BuyTransaction extends Transaction {
    public BuyTransaction(Stock stock, int quantity) {
        super(stock, quantity);
    }

    @Override
    public void execute(User user) {
        double cost = stock.getPrice() * quantity;
        if (user.getBalance() < cost) {
            System.out.println("❌ Insufficient balance to buy " + stock.getSymbol());
            return;
        }
        user.setBalance(user.getBalance() - cost);
        user.getPortfolio().addStock(stock, quantity);
        user.addTransaction(this);
        System.out.println("✅ Bought " + quantity + " shares of " + stock.getSymbol());
    }
}

// SELL TRANSACTION (Inheritance + Polymorphism)
class SellTransaction extends Transaction {
    public SellTransaction(Stock stock, int quantity) {
        super(stock, quantity);
    }

    @Override
    public void execute(User user) {
        if (!user.getPortfolio().hasStock(stock, quantity)) {
            System.out.println("❌ Not enough shares to sell!");
            return;
        }
        double revenue = stock.getPrice() * quantity;
        user.setBalance(user.getBalance() + revenue);
        user.getPortfolio().removeStock(stock, quantity);
        user.addTransaction(this);
        System.out.println("✅ Sold " + quantity + " shares of " + stock.getSymbol());
    }
}

// PORTFOLIO CLASS (Composition)
class Portfolio implements Serializable {
    private Map<String, Integer> holdings = new HashMap<>();

    public void addStock(Stock stock, int qty) {
        holdings.put(stock.getSymbol(), holdings.getOrDefault(stock.getSymbol(), 0) + qty);
    }

    public void removeStock(Stock stock, int qty) {
        if (hasStock(stock, qty)) {
            holdings.put(stock.getSymbol(), holdings.get(stock.getSymbol()) - qty);
            if (holdings.get(stock.getSymbol()) <= 0)
                holdings.remove(stock.getSymbol());
        }
    }

    public boolean hasStock(Stock stock, int qty) {
        return holdings.containsKey(stock.getSymbol()) && holdings.get(stock.getSymbol()) >= qty;
    }

    public Map<String, Integer> getHoldings() {
        return holdings;
    }

    public void display() {
        System.out.println("\n--- Portfolio Summary ---");
        if (holdings.isEmpty()) System.out.println("No holdings yet!");
        else holdings.forEach((s, q) -> System.out.println(s + " → " + q + " shares"));
    }
}

// USER CLASS (Encapsulation + Composition)
class User implements Serializable {
    private String name;
    private double balance;
    private Portfolio portfolio;
    private List<Transaction> history;

    public User(String name, double balance) {
        this.name = name;
        this.balance = balance;
        this.portfolio = new Portfolio();
        this.history = new ArrayList<>();
    }

    public String getName() { return name; }
    public double getBalance() { return balance; }
    public void setBalance(double balance) { this.balance = balance; }
    public Portfolio getPortfolio() { return portfolio; }

    public void addTransaction(Transaction t) {
        history.add(t);
    }

    public void showHistory() {
        System.out.println("\n--- Transaction History ---");
        if (history.isEmpty()) System.out.println("No transactions yet!");
        else history.forEach(System.out::println);
    }
}

// FILE MANAGER (File I/O)
class FileManager {
    private static final String FILE_PATH = "portfolioData.ser";

    public static void saveUser(User user) {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(FILE_PATH))) {
            oos.writeObject(user);
            System.out.println("💾 Portfolio saved successfully!");
        } catch (IOException e) {
            System.out.println("Error saving portfolio: " + e.getMessage());
        }
    }

    public static User loadUser() {
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(FILE_PATH))) {
            User user = (User) ois.readObject();
            System.out.println("📂 Loaded portfolio data for user: " + user.getName());
            return user;
        } catch (Exception e) {
            return null; // File may not exist yet
        }
    }
}

// MAIN CLASS (User Interaction)
public class StockTradingPlatform {
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);

        // Initialize stock market data
        Map<String, Stock> market = new HashMap<>();
        market.put("AAPL", new Stock("AAPL", 1800));
        market.put("GOOGL", new Stock("GOOGL", 2500));
        market.put("TSLA", new Stock("TSLA", 3000));
        market.put("INFY", new Stock("INFY", 1600));

        // Load existing user or create new one
        User user = FileManager.loadUser();
        if (user == null) {
            System.out.print("Enter your name: ");
            String name = sc.nextLine();
            user = new User(name, 15000.0);
            System.out.println("Welcome, " + name + "! Starting balance ₹15000");
        }

        while (true) {
            System.out.println("\n===== STOCK TRADING MENU =====");
            System.out.println("1. View Market");
            System.out.println("2. Buy Stock");
            System.out.println("3. Sell Stock");
            System.out.println("4. View Portfolio");
            System.out.println("5. View Transaction History");
            System.out.println("6. Save & Exit");
            System.out.print("Enter your choice: ");
            int ch = sc.nextInt();

            switch (ch) {
                case 1 -> {
                    System.out.println("\n--- Market Data ---");
                    market.values().forEach(System.out::println);
                }
                case 2 -> {
                    System.out.print("Enter Stock Symbol: ");
                    String sym = sc.next().toUpperCase();
                    System.out.print("Enter Quantity: ");
                    int qty = sc.nextInt();

                    Stock s = market.get(sym);
                    if (s == null) {
                        System.out.println("❌ Invalid stock symbol!");
                        break;
                    }
                    new BuyTransaction(s, qty).execute(user);
                }
                case 3 -> {
                    System.out.print("Enter Stock Symbol: ");
                    String sym = sc.next().toUpperCase();
                    System.out.print("Enter Quantity: ");
                    int qty = sc.nextInt();

                    Stock s = market.get(sym);
                    if (s == null) {
                        System.out.println("❌ Invalid stock symbol!");
                        break;
                    }
                    new SellTransaction(s, qty).execute(user);
                }
                case 4 -> {
                    user.getPortfolio().display();
                    System.out.println("Available Balance: ₹" + user.getBalance());
                }
                case 5 -> user.showHistory();
                case 6 -> {
                    FileManager.saveUser(user);
                    System.out.println("Goodbye, " + user.getName() + "!");
                    System.exit(0);
                }
                default -> System.out.println("Invalid choice!");
            }
        }
    }
}
