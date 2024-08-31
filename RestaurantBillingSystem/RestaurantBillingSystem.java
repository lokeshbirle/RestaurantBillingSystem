import java.io.EOFException;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Scanner;

class Item implements Serializable {
    String item;
    float price;
    int qty;

    public Item(String item, float price, int qty) {
        this.item = item;
        this.price = price;
        this.qty = qty;
    }
}

class Order implements Serializable {
    String customer;
    String date;
    int numOfItems;
    Item[] items;

    public Order(String customer, String date, int numOfItems, Item[] items) {
        this.customer = customer;
        this.date = date;
        this.numOfItems = numOfItems;
        this.items = items;
    }
}

public class RestaurantBillingSystem {

    static void generateBillHeader(String name, String date) {
        System.out.println("\n\n");
        System.out.println("\t    THE KAKA RESTAURANT");
        System.out.println("\n\t   -----------------------");
        System.out.println("Date: " + date);
        System.out.println("Invoice To: " + name);
        System.out.println();
        System.out.println("---------------------------------------");
        System.out.println("Items\t\tQty\t\tTotal\t\t");
        System.out.println("---------------------------------------");
        System.out.println();
    }

    static void generateBillBody(String item, int qty, float price) {
        System.out.printf("%s\t\t%d\t\t%.2f\t\t\n", item, qty, qty * price);
    }

    static void generateBillFooter(float total) {
        System.out.println();
        float discount = 0.1f * total;
        float netTotal = total - discount;
        float cgst = 0.09f * netTotal;
        float grandTotal = netTotal + 2 * cgst;
        System.out.println("---------------------------------------");
        System.out.printf("Sub Total\t\t\t%.2f\n", total);
        System.out.printf("Discount @10%%\t\t\t%.2f\n", discount);
        System.out.println("\t\t\t\t-------");
        System.out.printf("Net Total\t\t\t%.2f\n", netTotal);
        System.out.printf("CGST @9%%\t\t\t%.2f\n", cgst);
        System.out.printf("SGST @9%%\t\t\t%.2f\n", cgst);
        System.out.println("---------------------------------------");
        System.out.printf("Grand Total\t\t\t%.2f\n", grandTotal);
        System.out.println("---------------------------------------\n");
    }

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        char saveBill;
        char contFlag = 'y';

        while (contFlag == 'y') {
            float total = 0;
            boolean invoiceFound = false;

            System.out.println("\t============ ADV. RESTAURANT ============");
            System.out.println("\nPlease select your preferred operation:");
            System.out.println("1. Generate Invoice");
            System.out.println("2. Show all Invoices");
            System.out.println("3. Search Invoice");
            System.out.println("4. Exit");

            System.out.print("\nYour choice: ");
            int opt = scanner.nextInt();
            scanner.nextLine(); // Consume newline

            switch (opt) {
                case 1:
                    System.out.print("\nPlease enter the name of the customer: ");
                    String customer = scanner.nextLine();
                    String date = new SimpleDateFormat("dd-MM-yyyy").format(new Date());
                    System.out.print("\nPlease enter the number of items: ");
                    int n = scanner.nextInt();
                    scanner.nextLine(); // Consume newline

                    Item[] items = new Item[n];
                    for (int i = 0; i < n; i++) {
                        System.out.printf("\n\nPlease enter the item %d: ", i + 1);
                        String itemName = scanner.nextLine();
                        System.out.print("Please enter the quantity: ");
                        int qty = scanner.nextInt();
                        System.out.print("Please enter the unit price: ");
                        float price = scanner.nextFloat();
                        scanner.nextLine(); // Consume newline
                        items[i] = new Item(itemName, price, qty);
                        total += qty * price;
                    }

                    Order order = new Order(customer, date, n, items);

                    // Generate HTML Bill
                    generateBillHeader(order.customer, order.date);
                    for (Item item : order.items) {
                        generateBillBody(item.item, item.qty, item.price);
                    }
                    generateBillFooter(total);

                    System.out.print("\nDo you want to save the invoice [y/n]: ");
                    saveBill = scanner.nextLine().charAt(0);

                    if (saveBill == 'y') {
                        try (FileOutputStream fos = new FileOutputStream("RestaurantBill.dat", true);
                                ObjectOutputStream oos = new ObjectOutputStream(fos)) {
                            oos.writeObject(order);
                            System.out.println("\nSuccessfully saved");
                        } catch (IOException e) {
                            System.out.println("\nError saving: " + e.getMessage());
                        }
                    }
                    break;

                case 2:
                    System.out.println("\n  *****Your Previous Invoices*****");
                    try (FileInputStream fis = new FileInputStream("RestaurantBill.dat");
                            ObjectInputStream ois = new ObjectInputStream(fis)) {
                        while (true) {
                            Order savedOrder = (Order) ois.readObject();
                            float savedTotal = 0;
                            generateBillHeader(savedOrder.customer, savedOrder.date);
                            for (Item item : savedOrder.items) {
                                generateBillBody(item.item, item.qty, item.price);
                                savedTotal += item.qty * item.price;
                            }
                            generateBillFooter(savedTotal);
                        }
                    } catch (EOFException e) {
                        // End of file reached
                    } catch (IOException | ClassNotFoundException e) {
                        System.out.println("\nError reading file: " + e.getMessage());
                    }
                    break;

                case 3:
                    System.out.print("Enter the name of the customer: ");
                    String searchName = scanner.nextLine();
                    System.out.println("\t*****Invoice of " + searchName + "*****");
                    try (FileInputStream fis = new FileInputStream("RestaurantBill.dat");
                            ObjectInputStream ois = new ObjectInputStream(fis)) {
                        boolean found = false;
                        while (true) {
                            Order savedOrder = (Order) ois.readObject();
                            if (savedOrder.customer.equals(searchName)) {
                                float searchTotal = 0;
                                generateBillHeader(savedOrder.customer, savedOrder.date);
                                for (Item item : savedOrder.items) {
                                    generateBillBody(item.item, item.qty, item.price);
                                    searchTotal += item.qty * item.price;
                                }
                                generateBillFooter(searchTotal);
                                found = true;
                                break;
                            }
                        }
                        if (!found) {
                            System.out.println("Sorry, the invoice for " + searchName + " does not exist.");
                        }
                    } catch (EOFException e) {
                        // End of file reached
                    } catch (IOException | ClassNotFoundException e) {
                        System.out.println("\nError reading file: " + e.getMessage());
                    }
                    break;

                case 4:
                    System.out.println("\n\t\t Bye Bye :)\n\n");
                    scanner.close();
                    System.exit(0);
                    break;

                default:
                    System.out.println("Sorry, invalid option. Please choose again.");
                    break;
            }
            System.out.print("\nDo you want to perform another operation? [y/n]: ");
            contFlag = scanner.nextLine().charAt(0);
        }

        System.out.println("\n\t\t Bye Bye :)\n\n");
    }
}
