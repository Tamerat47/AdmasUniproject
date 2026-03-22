/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Main.java to edit this template
 */
package stemstorepos;

/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Main.java to edit this template
 */
// ULTIMATE VERSION - COMPLETE POS SYSTEM

import java.text.MessageFormat;
import java.awt.print.PrinterException;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;
import java.io.*;
import javax.swing.border.TitledBorder;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.Timer;
import java.io.Serializable;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.File;

class Product implements Serializable {
    private static final long serialVersionUID = 1L;
    int id;
    String name;
    String variant;
    double price;
    int stock;
    String unit;

    Product(int id, String name, String variant, double price, int stock, String unit) {
        this.id = id;
        this.name = name;
        this.variant = variant;
        this.price = price;
        this.stock = stock;
        this.unit = unit;
    }
    
    String getFullName() {
        if (variant == null || variant.isEmpty()) {
            return name;
        }
        return name + " (" + variant + ")";
    }
}

class Sale implements Serializable {
    private static final long serialVersionUID = 1L;
    int receiptNo;
    String date;
    String time;
    String cashier;
    ArrayList<SaleItem> items;
    double subtotal;
    double tax;
    double grandTotal;
    
    Sale(int receiptNo, String date, String time, String cashier) {
        this.receiptNo = receiptNo;
        this.date = date;
        this.time = time;
        this.cashier = cashier;
        this.items = new ArrayList<>();
    }
}

class SaleItem implements Serializable {
    private static final long serialVersionUID = 1L;
    String name;
    String variant;
    double qty;
    double price;
    double total;
    
    SaleItem(String name, String variant, double qty, double price, double total) {
        this.name = name;
        this.variant = variant;
        this.qty = qty;
        this.price = price;
        this.total = total;
    }
}

class User implements Serializable {
    private static final long serialVersionUID = 1L;
    String username, password, role;
    boolean isLoggedIn;
    String loginTime;
    String logoutTime;
    String lastLoginDate;
    
    User(String u, String p, String r) {
        username = u;
        password = p;
        role = r;
        isLoggedIn = false;
        loginTime = "";
        logoutTime = "";
        lastLoginDate = "";
    }
}

public class STEMStorePOS {

    static JFrame frame;
    static CardLayout layout = new CardLayout();
    static JPanel mainPanel = new JPanel(layout);

    static ArrayList<Product> inventory = new ArrayList<>();
    static ArrayList<User> users = new ArrayList<>();
    static ArrayList<Sale> salesHistory = new ArrayList<>();

    static DefaultTableModel cartModel;
    static JTextArea receiptArea;
    static double total = 0;
    static String currentCashier = "";
    static JTable inventoryTable;
    static DefaultTableModel inventoryModel;
    static int currentReceiptNo = 1000;
    
    static final String SALES_FILE = "sales_history.dat";
    static final String INVENTORY_FILE = "inventory.dat";
    static final String USERS_FILE = "users.dat";
    static final String RECEIPT_COUNTER_FILE = "receipt_counter.dat";

    public static void main(String[] args) {
        loadInventory();
        loadUsers();
        loadSalesHistory();
        loadReceiptCounter();
        
        users.add(new User("admin","1234","admin"));
        users.add(new User("teller1","1111","cashier"));
        users.add(new User("teller2","2222","cashier"));
        
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("Shutdown hook: Saving all data...");
            saveAllData();
        }));

        frame = new JFrame("STEM STORE POS - Complete Inventory Management System");
        frame.setSize(1300, 750);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLocationRelativeTo(null);

        mainPanel.add(startScreen(),"start");
        mainPanel.add(loginScreen("admin"),"adminLogin");
        mainPanel.add(loginScreen("cashier"),"cashierLogin");
        mainPanel.add(adminDashboard(),"admin");
        mainPanel.add(cashierPanel(),"cashier");

        frame.add(mainPanel);
        frame.setVisible(true);
    }
    
    static void loadInitialInventory() {
        inventory.add(new Product(1,"Orange","",30,50,"kg"));
        inventory.add(new Product(2,"Onion","",25,40,"kg"));
        inventory.add(new Product(3,"Soap","",15,100,"pcs"));
        inventory.add(new Product(4,"Rice","",80,30,"kg"));
        inventory.add(new Product(5,"Sugar","",45,25,"kg"));
        inventory.add(new Product(6,"Milk","",60,20,"pcs"));
        inventory.add(new Product(7,"Bread","",25,15,"pcs"));
        
        inventory.add(new Product(8,"Cooking Oil","1L",120,30,"bottle"));
        inventory.add(new Product(9,"Cooking Oil","3L",350,20,"bottle"));
        inventory.add(new Product(10,"Cooking Oil","5L",550,15,"bottle"));
        
        inventory.add(new Product(11,"Mineral Water","500ml",10,100,"bottle"));
        inventory.add(new Product(12,"Mineral Water","1L",20,80,"bottle"));
        inventory.add(new Product(13,"Mineral Water","1.5L",30,60,"bottle"));
        
        inventory.add(new Product(14,"Coca Cola","330ml",25,120,"can"));
        inventory.add(new Product(15,"Coca Cola","1L",60,50,"bottle"));
        inventory.add(new Product(16,"Coca Cola","2L",100,40,"bottle"));
    }    
        
    // Save all data to files
    static void saveAllData() {
        saveSalesHistory();
        saveInventory();
        saveUsers();
        saveReceiptCounter();
    }
    
    // Save sales history to file
    static void saveSalesHistory() {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(SALES_FILE))) {
            oos.writeObject(salesHistory);
        } catch (IOException e) {
            System.err.println("Error saving sales history: " + e.getMessage());
        }
    }
    
    // Load sales history from file
    @SuppressWarnings("unchecked")
    static void loadSalesHistory() {
        File file = new File(SALES_FILE);
        if (file.exists()) {
            try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file))) {
                salesHistory = (ArrayList<Sale>) ois.readObject();
            } catch (IOException | ClassNotFoundException e) {
                System.err.println("Error loading sales history: " + e.getMessage());
                salesHistory = new ArrayList<>();
            }
        } else {
            salesHistory = new ArrayList<>();
        }
    }
    
    // Save inventory to file
       
    static void saveInventory() {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(INVENTORY_FILE))) {
            oos.writeObject(inventory);
            System.out.println("Inventory saved successfully. Size: " + inventory.size()); // Debug line
            // Print first product to verify
            if(inventory.size() > 0) {
                System.out.println("First product saved: " + inventory.get(0).getFullName() + " Stock: " + inventory.get(0).stock);
            }
        } catch (IOException e) {
            System.err.println("Error saving inventory: " + e.getMessage());
            e.printStackTrace(); // Print full error
        }
    }
    
    // Load inventory from file
    @SuppressWarnings("unchecked")
       
    static void loadInventory() {
        File file = new File(INVENTORY_FILE);
        System.out.println("Loading inventory from: " + file.getAbsolutePath()); // Debug line
        if (file.exists()) {
            try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file))) {
                inventory = (ArrayList<Product>) ois.readObject();
                System.out.println("Inventory loaded successfully. Size: " + inventory.size()); // Debug line
                // Print first product to verify
                if(inventory.size() > 0) {
                    System.out.println("First product: " + inventory.get(0).getFullName() + " Stock: " + inventory.get(0).stock);
                }
            } catch (IOException | ClassNotFoundException e) {
                System.err.println("Error loading inventory: " + e.getMessage());
                e.printStackTrace(); // Print full error
                loadInitialInventory();
            }
        } else {
            System.out.println("Inventory file not found, loading initial inventory");
            loadInitialInventory();
        }
    }
    
    // Save users to file
    static void saveUsers() {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(USERS_FILE))) {
            oos.writeObject(users);
        } catch (IOException e) {
            System.err.println("Error saving users: " + e.getMessage());
        }
    }
    
    // Load users from file
    @SuppressWarnings("unchecked")
     static void loadUsers() {
        File file = new File(USERS_FILE);
        if (file.exists()) {
            try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file))) {
                users = (ArrayList<User>) ois.readObject();
                // Initialize any missing fields for existing users
                for(User u : users) {
                    if(u.loginTime == null) u.loginTime = "";
                    if(u.logoutTime == null) u.logoutTime = "";
                    if(u.lastLoginDate == null) u.lastLoginDate = "";
                }
            } catch (IOException | ClassNotFoundException e) {
                System.err.println("Error loading users: " + e.getMessage());
                loadDefaultUsers();
            }
        } else {
            loadDefaultUsers();
        }
    }
    
    // Save receipt counter
    static void saveReceiptCounter() {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(RECEIPT_COUNTER_FILE))) {
            oos.writeInt(currentReceiptNo);
        } catch (IOException e) {
            System.err.println("Error saving receipt counter: " + e.getMessage());
        }
    }
     
    // Load receipt counter
    static void loadReceiptCounter() {
        File file = new File(RECEIPT_COUNTER_FILE);
        if (file.exists()) {
            try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file))) {
                currentReceiptNo = ois.readInt();
            } catch (IOException e) {
                System.err.println("Error loading receipt counter: " + e.getMessage());
                currentReceiptNo = 1000;
            }
        } else {
            currentReceiptNo = 1000;
        }
    }
    
    // Load default users
    static void loadDefaultUsers() {
        users.clear();
        User admin = new User("admin","1234","admin");
        admin.isLoggedIn = false;
        admin.loginTime = "";
        admin.logoutTime = "";
        admin.lastLoginDate = "";
        users.add(admin);
        
        User teller1 = new User("teller1","1111","cashier");
        teller1.isLoggedIn = false;
        teller1.loginTime = "";
        teller1.logoutTime = "";
        teller1.lastLoginDate = "";
        users.add(teller1);
        
        User teller2 = new User("teller2","2222","cashier");
        teller2.isLoggedIn = false;
        teller2.loginTime = "";
        teller2.logoutTime = "";
        teller2.lastLoginDate = "";
        users.add(teller2);
    }

    static JPanel startScreen(){
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(new Color(240, 248, 255));
        GridBagConstraints gbc = new GridBagConstraints();
        
        JLabel title = new JLabel("STEM STORE POS SYSTEM", SwingConstants.CENTER);
        title.setFont(new Font("Arial", Font.BOLD, 32));
        title.setForeground(new Color(0, 102, 204));
        
        JLabel subtitle = new JLabel("Complete Inventory Management Solution", SwingConstants.CENTER);
        subtitle.setFont(new Font("Arial", Font.PLAIN, 16));
        subtitle.setForeground(new Color(100, 100, 100));
        
        JButton admin = new JButton("LOGIN AS ADMIN");
        JButton cashier = new JButton("LOGIN AS CASHIER");
        
        styleButton(admin, new Color(0, 102, 204));
        styleButton(cashier, new Color(46, 139, 87));
        
        admin.setPreferredSize(new Dimension(250, 45));
        cashier.setPreferredSize(new Dimension(250, 45));
        
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.insets = new Insets(20, 20, 10, 20);
        panel.add(title, gbc);
        
        gbc.gridy = 1;
        panel.add(subtitle, gbc);
        
        gbc.gridy = 2;
        gbc.insets = new Insets(30, 20, 20, 20);
        panel.add(admin, gbc);
        
        gbc.gridy = 3;
        panel.add(cashier, gbc);

        admin.addActionListener(e->layout.show(mainPanel,"adminLogin"));
        cashier.addActionListener(e->layout.show(mainPanel,"cashierLogin"));

        return panel;
    }
    
    static void styleButton(JButton button, Color bgColor) {
        button.setBackground(bgColor);
        button.setForeground(Color.WHITE);
        button.setFont(new Font("Arial", Font.BOLD, 14));
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
    }

    static JPanel loginScreen(String role){
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(new Color(240, 248, 255));
        GridBagConstraints gbc = new GridBagConstraints();
        
        JLabel title = new JLabel(role.toUpperCase() + " LOGIN", SwingConstants.CENTER);
        title.setFont(new Font("Arial", Font.BOLD, 28));
        
        JTextField user = new JTextField(20);
        JPasswordField pass = new JPasswordField(20);
        JButton login = new JButton("LOGIN");
        JButton back = new JButton("BACK");
        
        user.setFont(new Font("Arial", Font.PLAIN, 14));
        pass.setFont(new Font("Arial", Font.PLAIN, 14));
        
        styleButton(login, new Color(0, 102, 204));
        styleButton(back, new Color(128, 128, 128));
        
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        panel.add(title, gbc);
        
        gbc.gridy = 1;
        gbc.gridwidth = 1;
        gbc.anchor = GridBagConstraints.EAST;
        panel.add(new JLabel("Username:"), gbc);
        gbc.gridx = 1;
        gbc.anchor = GridBagConstraints.WEST;
        panel.add(user, gbc);
        
        gbc.gridy = 2;
        gbc.gridx = 0;
        gbc.anchor = GridBagConstraints.EAST;
        panel.add(new JLabel("Password:"), gbc);
        gbc.gridx = 1;
        gbc.anchor = GridBagConstraints.WEST;
        panel.add(pass, gbc);
        
        // Center the button row
        gbc.gridy = 3;
        gbc.gridx = 0;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 0));
        buttonPanel.setOpaque(false);
        buttonPanel.add(login);
        buttonPanel.add(back);
        panel.add(buttonPanel, gbc);

        login.addActionListener(e->{
            boolean loggedIn = false;
            for(User u: users){
                 if(u.username.equals(user.getText()) && u.password.equals(new String(pass.getPassword())) && u.role.equals(role)){
                    loggedIn = true;
                    
                    // Track login information
                    u.isLoggedIn = true;
                    // Ensure fields are initialized
                    if(u.loginTime == null) u.loginTime = "";
                    if(u.logoutTime == null) u.logoutTime = "";
                    if(u.lastLoginDate == null) u.lastLoginDate = "";
                    
                    u.loginTime = new SimpleDateFormat("HH:mm:ss").format(new Date());
                    u.lastLoginDate = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(new Date());
                    u.logoutTime = ""; // Clear previous logout time
                    saveUsers();
                    
                    if(role.equals("admin")) {
                        layout.show(mainPanel,"admin");
                    } else {
                        currentCashier = u.username;
                        layout.show(mainPanel,"cashier");
                    }
                    break;
                }
            }
            if(!loggedIn) {
                JOptionPane.showMessageDialog(panel, "Invalid username or password!", "Login Failed", JOptionPane.ERROR_MESSAGE);
            }
        });

        back.addActionListener(e->layout.show(mainPanel,"start"));

        return panel;
    }

    static JPanel adminDashboard(){
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(new Color(240, 248, 255));

        // Top Panel
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBackground(new Color(0, 102, 204));
        JLabel title = new JLabel("ADMIN DASHBOARD", SwingConstants.CENTER);
        title.setFont(new Font("Arial", Font.BOLD, 24));
        title.setForeground(Color.WHITE);
        topPanel.add(title, BorderLayout.CENTER);
        
        JButton logoutMain = new JButton("LOGOUT");
        styleButton(logoutMain, new Color(220, 53, 69));
        logoutMain.setPreferredSize(new Dimension(100, 35));
        topPanel.add(logoutMain, BorderLayout.EAST);
        topPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        panel.add(topPanel, BorderLayout.NORTH);

        // Menu Panel with better styling
        JPanel menu = new JPanel();
        menu.setLayout(new GridLayout(7, 1, 15, 15));
        menu.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        menu.setBackground(new Color(245, 245, 245));
        
        // Create clean text buttons (no emojis)
        JButton inventoryBtn = createStyledMenuButton("INVENTORY", new Color(52, 152, 219));
        JButton addBtn = createStyledMenuButton("ADD PRODUCT", new Color(46, 204, 113));
        JButton editBtn = createStyledMenuButton("EDIT PRODUCT", new Color(241, 196, 15));
        JButton historyBtn = createStyledMenuButton("SALES HISTORY", new Color(155, 89, 182));
        JButton usersBtn = createStyledMenuButton("MANAGE USERS", new Color(52, 73, 94));
        JButton reportsBtn = createStyledMenuButton("SALES REPORTS", new Color(230, 126, 34));
        JButton logout = createStyledMenuButton("LOGOUT", new Color(231, 76, 60));

        CardLayout adminLayout = new CardLayout();
        JPanel content = new JPanel(adminLayout);
        content.setBackground(Color.WHITE);
        content.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        content.add(inventoryPanel(),"inv");
        content.add(addProductPanel(),"add");
        content.add(editProductPanel(),"edit");
        content.add(salesHistoryPanel(),"history");
        content.add(userManagementPanel(),"users");
        content.add(reportsPanel(),"reports");

        inventoryBtn.addActionListener(e->adminLayout.show(content,"inv"));
        addBtn.addActionListener(e->adminLayout.show(content,"add"));
        editBtn.addActionListener(e->{
            refreshInventoryTable();
            adminLayout.show(content,"edit");
        });
        historyBtn.addActionListener(e->{
            refreshSalesHistory();
            adminLayout.show(content,"history");
        });
        usersBtn.addActionListener(e->adminLayout.show(content,"users"));
        reportsBtn.addActionListener(e->adminLayout.show(content,"reports"));
        
        logout.addActionListener(e->{
            // Record logout time for admin
            for(User u : users) {
                if(u.username.equals("admin")) {
                    u.isLoggedIn = false;
                    if(u.logoutTime == null) u.logoutTime = "";
                    u.logoutTime = new SimpleDateFormat("HH:mm:ss").format(new Date());
                    saveUsers();
                    break;
                }
            }
            currentCashier = "";
            layout.show(mainPanel,"start");
        });
        
        logoutMain.addActionListener(e->{
            // Record logout time for admin
            for(User u : users) {
                if(u.username.equals("admin")) {
                    u.isLoggedIn = false;
                    if(u.logoutTime == null) u.logoutTime = "";
                    u.logoutTime = new SimpleDateFormat("HH:mm:ss").format(new Date());
                    saveUsers();
                    break;
                }
            }
            currentCashier = "";
            layout.show(mainPanel,"start");
        }); 

        menu.add(inventoryBtn);
        menu.add(addBtn);
        menu.add(editBtn);
        menu.add(historyBtn);
        menu.add(usersBtn);
        menu.add(reportsBtn);
        menu.add(logout);

        // Add a scroll pane for menu if needed
        JScrollPane menuScroll = new JScrollPane(menu);
        menuScroll.setBorder(null);
        menuScroll.setBackground(new Color(245, 245, 245));
        
        panel.add(menuScroll, BorderLayout.WEST);
        panel.add(content, BorderLayout.CENTER);

        return panel;
    }
    
    // Helper method to create styled menu buttons
    static JButton createStyledMenuButton(String text, Color bgColor) {
        JButton button = new JButton(text);
        button.setFont(new Font("Arial", Font.BOLD, 14));
        button.setBackground(bgColor);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createEmptyBorder(12, 20, 12, 20));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setPreferredSize(new Dimension(200, 50));
        
        // Add hover effect
        button.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                button.setBackground(bgColor.darker());
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                button.setBackground(bgColor);
            }
        });
        
        return button;
    }
    
    // Helper method to create icon buttons with better styling
    static JButton createIconButton(String text, Color bgColor) {
        JButton button = new JButton(text);
        button.setFont(new Font("Segoe UI", Font.BOLD, 14));
        button.setBackground(bgColor);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createEmptyBorder(12, 15, 12, 15));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setPreferredSize(new Dimension(200, 50));
        
        // Add rounded corners effect
        button.setOpaque(true);
        
        // Add hover effect with animation
        button.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                button.setBackground(bgColor.darker());
                button.setFont(new Font("Segoe UI", Font.BOLD, 15));
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                button.setBackground(bgColor);
                button.setFont(new Font("Segoe UI", Font.BOLD, 14));
            }
        });
        
        return button;
    }

    static JPanel inventoryPanel(){
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Current Inventory"));
        
        inventoryModel = new DefaultTableModel(new String[]{"ID","Product Name","Variant","Price (ETB)","Stock","Unit"},0);
        inventoryTable = new JTable(inventoryModel);
        inventoryTable.setFont(new Font("Arial", Font.PLAIN, 12));
        inventoryTable.getTableHeader().setFont(new Font("Arial", Font.BOLD, 12));
        
        refreshInventoryTable();
        
        JScrollPane scrollPane = new JScrollPane(inventoryTable);
        panel.add(scrollPane, BorderLayout.CENTER);
        
        JButton refreshBtn = new JButton("Refresh");
        refreshBtn.addActionListener(e -> refreshInventoryTable());
        JPanel bottomPanel = new JPanel();
        bottomPanel.add(refreshBtn);
        panel.add(bottomPanel, BorderLayout.SOUTH);
        
        return panel;
    }
    
    static void refreshInventoryTable() {
        inventoryModel.setRowCount(0);
        for(Product p: inventory){
            inventoryModel.addRow(new Object[]{p.id, p.name, p.variant.isEmpty() ? "-" : p.variant, p.price, p.stock, p.unit});
        }
    }

    static JPanel addProductPanel(){
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Add New Product"));
        
        // Create split pane to show inventory table on top
        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        splitPane.setResizeWeight(0.5);
        
        // Inventory Table Panel (Top)
        JPanel inventoryPanel = new JPanel(new BorderLayout());
        inventoryPanel.setBorder(BorderFactory.createTitledBorder("Current Inventory"));
        
        DefaultTableModel topInventoryModel = new DefaultTableModel(new String[]{"ID", "Product", "Variant", "Price", "Stock", "Unit"}, 0);
        JTable topInventoryTable = new JTable(topInventoryModel);
        topInventoryTable.setFont(new Font("Arial", Font.PLAIN, 11));
        topInventoryTable.getTableHeader().setFont(new Font("Arial", Font.BOLD, 11));
        topInventoryTable.setRowHeight(22);
        
        refreshTopInventoryTable(topInventoryModel);
        
        JScrollPane topScroll = new JScrollPane(topInventoryTable);
        inventoryPanel.add(topScroll, BorderLayout.CENTER);
        
        // Add Product Form Panel (Bottom)
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBorder(BorderFactory.createTitledBorder("Add New Product Form"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        
        JTextField id = new JTextField(15);
        JTextField name = new JTextField(15);
        JTextField variant = new JTextField(10);
        JTextField price = new JTextField(15);
        JTextField stock = new JTextField(15);
        JComboBox<String> unit = new JComboBox<>(new String[]{"kg", "pcs", "liters", "bottle", "can", "pack"});
        JButton add = new JButton("Add Product");
        JButton clear = new JButton("Clear");
        JButton refreshTable = new JButton("Refresh Table");
        
        styleButton(add, new Color(46, 139, 87));
        styleButton(clear, new Color(128, 128, 128));
        styleButton(refreshTable, new Color(52, 152, 219));
        
        int row = 0;
        gbc.gridx = 0; gbc.gridy = row; gbc.anchor = GridBagConstraints.EAST;
        formPanel.add(new JLabel("Product ID:"), gbc);
        gbc.gridx = 1; gbc.anchor = GridBagConstraints.WEST;
        formPanel.add(id, gbc);
        
        row++;
        gbc.gridx = 0; gbc.gridy = row;
        formPanel.add(new JLabel("Product Name:"), gbc);
        gbc.gridx = 1;
        formPanel.add(name, gbc);
        
        row++;
        gbc.gridx = 0; gbc.gridy = row;
        formPanel.add(new JLabel("Variant (optional):"), gbc);
        gbc.gridx = 1;
        formPanel.add(variant, gbc);
        
        row++;
        gbc.gridx = 0; gbc.gridy = row;
        formPanel.add(new JLabel("Price (ETB):"), gbc);
        gbc.gridx = 1;
        formPanel.add(price, gbc);
        
        row++;
        gbc.gridx = 0; gbc.gridy = row;
        formPanel.add(new JLabel("Initial Stock:"), gbc);
        gbc.gridx = 1;
        formPanel.add(stock, gbc);
        
        row++;
        gbc.gridx = 0; gbc.gridy = row;
        formPanel.add(new JLabel("Unit:"), gbc);
        gbc.gridx = 1;
        formPanel.add(unit, gbc);
        
        row++;
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        buttonPanel.add(add);
        buttonPanel.add(clear);
        buttonPanel.add(refreshTable);
        gbc.gridx = 0; gbc.gridy = row; gbc.gridwidth = 2;
        formPanel.add(buttonPanel, gbc);
        
        splitPane.setTopComponent(inventoryPanel);
        splitPane.setBottomComponent(formPanel);
        panel.add(splitPane, BorderLayout.CENTER);
        
        // DEFINE THE CLEAR FORM RUNNABLE HERE
        Runnable clearForm = () -> {
            id.setText("");
            name.setText("");
            variant.setText("");
            price.setText("");
            stock.setText("");
            unit.setSelectedIndex(0);
        };
        
        // Refresh table action
        refreshTable.addActionListener(e -> refreshTopInventoryTable(topInventoryModel));
        
        // Clear button action
        clear.addActionListener(e -> clearForm.run());
        
        add.addActionListener(e->{
            try {
                String productName = name.getText().trim();
                String productVariant = variant.getText().trim();
                int newId = Integer.parseInt(id.getText());
                double newPrice = Double.parseDouble(price.getText());
                int newStock = Integer.parseInt(stock.getText());
                String newUnit = (String)unit.getSelectedItem();
                
                // Validate inputs
                if(productName.isEmpty()) {
                    JOptionPane.showMessageDialog(panel, "Product Name cannot be empty!", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                
                if(newId <= 0) {
                    JOptionPane.showMessageDialog(panel, "Product ID must be a positive number!", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                
                if(newPrice <= 0) {
                    JOptionPane.showMessageDialog(panel, "Price must be greater than 0!", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                
                if(newStock < 0) {
                    JOptionPane.showMessageDialog(panel, "Stock cannot be negative!", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                
                // Check for duplicate ID
                for(Product p : inventory) {
                    if(p.id == newId) {
                        JOptionPane.showMessageDialog(panel, 
                            "Product ID already exists!\nID: " + newId + " belongs to: " + p.getFullName(), 
                            "Duplicate ID Error", JOptionPane.ERROR_MESSAGE);
                        return;
                    }
                }
                
                // SMART DUPLICATE CHECKER
                Product existingProduct = null;
                boolean isDuplicate = false;
                
                for(Product p : inventory) {
                    if(p.name.equalsIgnoreCase(productName)) {
                        // Case 1: New product has NO variant
                        if(productVariant.isEmpty()) {
                            // If existing product has NO variant OR any variant, block it
                            existingProduct = p;
                            isDuplicate = true;
                            break;
                        } 
                        // Case 2: New product HAS a variant
                        else {
                            // Only block if existing product has the EXACT SAME variant
                            String existingVariant = (p.variant == null || p.variant.isEmpty()) ? "" : p.variant;
                            if(existingVariant.equalsIgnoreCase(productVariant)) {
                                existingProduct = p;
                                isDuplicate = true;
                                break;
                            }
                        }
                    }
                }
                
                if(isDuplicate) {
                    StringBuilder message = new StringBuilder();
                    message.append("❌ DUPLICATE PRODUCT DETECTED!\n\n");
                    message.append("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n");
                    message.append("Existing Product:\n");
                    message.append("  • Name: ").append(existingProduct.getFullName()).append("\n");
                    message.append("  • ID: ").append(existingProduct.id).append("\n");
                    message.append("  • Price: ").append(existingProduct.price).append(" ETB\n");
                    message.append("  • Stock: ").append(existingProduct.stock).append(" ").append(existingProduct.unit).append("\n");
                    message.append("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n");
                    message.append("Your Attempt:\n");
                    message.append("  • Name: ").append(productName);
                    if(!productVariant.isEmpty()) message.append(" (").append(productVariant).append(")");
                    message.append("\n");
                    message.append("  • Price: ").append(newPrice).append(" ETB\n\n");
                    
                    // Show other variants if they exist
                    boolean hasOtherVariants = false;
                    StringBuilder variants = new StringBuilder();
                    for(Product p : inventory) {
                        if(p.name.equalsIgnoreCase(productName) && p != existingProduct) {
                            hasOtherVariants = true;
                            String variantDisplay = (p.variant == null || p.variant.isEmpty()) ? "Standard" : p.variant;
                            variants.append("     • ").append(variantDisplay).append(" - ").append(p.price).append(" ETB\n");
                        }
                    }
                    
                    if(hasOtherVariants) {
                        message.append("💡 Other available variants:\n");
                        message.append(variants.toString());
                        message.append("\n");
                    }
                    
                    if(productVariant.isEmpty()) {
                        message.append("⚠️  This product name already exists in inventory.\n");
                        message.append("   Products WITHOUT variants must be unique.\n\n");
                    } else {
                        message.append("⚠️  This variant already exists for this product.\n");
                        message.append("   Use a DIFFERENT variant name for different sizes/types.\n\n");
                    }
                    
                    message.append("📌 WHAT YOU CAN DO:\n");
                    message.append("   1. To add more stock → Use EDIT PRODUCT menu\n");
                    message.append("   2. To change price → Edit the existing product\n");
                    message.append("   3. To add a different variant → Use a UNIQUE variant name\n");
                    message.append("      (Example: \"1L\", \"Organic\", \"Premium\", etc.)\n");
                    
                    JOptionPane.showMessageDialog(panel, message.toString(), "Duplicate Product", JOptionPane.WARNING_MESSAGE);
                    return;
                }
                
                // All checks passed, add the product
                Product newProduct = new Product(newId, productName, productVariant, newPrice, newStock, newUnit);
                inventory.add(newProduct);
                refreshInventoryTable();
                refreshTopInventoryTable(topInventoryModel);
                saveInventory();
                
                String successMsg = "✅ Product Added Successfully!\n\n" +
                                   "Product: " + productName + (productVariant.isEmpty() ? "" : " (" + productVariant + ")") + "\n" +
                                   "ID: " + newId + "\n" +
                                   "Price: " + newPrice + " ETB\n" +
                                   "Stock: " + newStock + " " + newUnit;
                
                JOptionPane.showMessageDialog(panel, successMsg, "Success", JOptionPane.INFORMATION_MESSAGE);
                
                // Clear the form using the Runnable
                clearForm.run();
                
            } catch(NumberFormatException ex) {
                JOptionPane.showMessageDialog(panel, "Please enter valid numbers for ID, Price, and Stock!", "Error", JOptionPane.ERROR_MESSAGE);
            } catch(Exception ex) {
                JOptionPane.showMessageDialog(panel, "Error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });
        
        return panel;
    }
    
    static void refreshTopInventoryTable(DefaultTableModel model) {
        model.setRowCount(0);
        for(Product p: inventory){
            model.addRow(new Object[]{p.id, p.name, p.variant.isEmpty() ? "-" : p.variant, p.price, p.stock, p.unit});
        }
    }
    
    static JPanel editProductPanel(){
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Edit/Delete Product"));
        
        // Create split pane
        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        splitPane.setResizeWeight(0.4);
        
        // Top Panel - Product Selection with Dropdown
        JPanel selectionPanel = new JPanel(new GridBagLayout());
        selectionPanel.setBorder(BorderFactory.createTitledBorder("Select Product"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        
        // Create dropdown with all products
        JComboBox<String> productDropdown = new JComboBox<>();
        refreshProductDropdown(productDropdown);
        productDropdown.setPreferredSize(new Dimension(300, 30));
        productDropdown.setFont(new Font("Arial", Font.PLAIN, 12));
        
        JButton loadBtn = new JButton("Load Product");
        JButton refreshDropdown = new JButton("Refresh List");
        styleButton(loadBtn, new Color(52, 152, 219));
        styleButton(refreshDropdown, new Color(52, 152, 219));
        
        gbc.gridx = 0; gbc.gridy = 0;
        selectionPanel.add(new JLabel("Select Product:"), gbc);
        gbc.gridx = 1;
        selectionPanel.add(productDropdown, gbc);
        gbc.gridx = 2;
        selectionPanel.add(loadBtn, gbc);
        gbc.gridx = 3;
        selectionPanel.add(refreshDropdown, gbc);
        
        // Bottom Panel - Edit Form
        JPanel editPanel = new JPanel(new GridBagLayout());
        editPanel.setBorder(BorderFactory.createTitledBorder("Edit Product Details"));
        gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        
        JTextField idField = new JTextField(10);
        idField.setEditable(false);
        idField.setBackground(new Color(240, 240, 240));
        
        JTextField nameField = new JTextField(20);
        JTextField variantField = new JTextField(15);
        JTextField priceField = new JTextField(10);
        JTextField stockField = new JTextField(10);
        JComboBox<String> unitBox = new JComboBox<>(new String[]{"kg", "pcs", "liters", "bottle", "can", "pack"});
        
        // Stock addition section
        JTextField addStockField = new JTextField(10);
        JButton addStockBtn = new JButton("+ Add to Stock");
        styleButton(addStockBtn, new Color(46, 139, 87));
        
        JButton updateBtn = new JButton("Update Product");
        JButton deleteBtn = new JButton("Delete Product");
        JButton clearBtn = new JButton("Clear Form");
        
        styleButton(updateBtn, new Color(52, 152, 219));
        styleButton(deleteBtn, new Color(231, 76, 60));
        styleButton(clearBtn, new Color(128, 128, 128));
        
        // Stock info label
        JLabel stockInfoLabel = new JLabel("Current Stock: --");
        stockInfoLabel.setFont(new Font("Arial", Font.BOLD, 12));
        stockInfoLabel.setForeground(new Color(0, 100, 0));
        
        int row = 0;
        gbc.gridx = 0; gbc.gridy = row; gbc.anchor = GridBagConstraints.EAST;
        editPanel.add(new JLabel("ID:"), gbc);
        gbc.gridx = 1; gbc.anchor = GridBagConstraints.WEST;
        editPanel.add(idField, gbc);
        
        row++;
        gbc.gridx = 0; gbc.gridy = row;
        editPanel.add(new JLabel("Product Name:"), gbc);
        gbc.gridx = 1;
        editPanel.add(nameField, gbc);
        
        row++;
        gbc.gridx = 0; gbc.gridy = row;
        editPanel.add(new JLabel("Variant:"), gbc);
        gbc.gridx = 1;
        editPanel.add(variantField, gbc);
        
        row++;
        gbc.gridx = 0; gbc.gridy = row;
        editPanel.add(new JLabel("Price (ETB):"), gbc);
        gbc.gridx = 1;
        editPanel.add(priceField, gbc);
        
        row++;
        gbc.gridx = 0; gbc.gridy = row;
        editPanel.add(new JLabel("Stock:"), gbc);
        gbc.gridx = 1;
        JPanel stockPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        stockPanel.add(stockField);
        stockPanel.add(addStockField);
        stockPanel.add(addStockBtn);
        editPanel.add(stockPanel, gbc);
        
        row++;
        gbc.gridx = 0; gbc.gridy = row;
        editPanel.add(new JLabel("Unit:"), gbc);
        gbc.gridx = 1;
        editPanel.add(unitBox, gbc);
        
        row++;
        gbc.gridx = 0; gbc.gridy = row;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        editPanel.add(stockInfoLabel, gbc);
        
        row++;
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        btnPanel.add(updateBtn);
        btnPanel.add(deleteBtn);
        btnPanel.add(clearBtn);
        gbc.gridx = 0; gbc.gridy = row; gbc.gridwidth = 2;
        editPanel.add(btnPanel, gbc);
        
        splitPane.setTopComponent(selectionPanel);
        splitPane.setBottomComponent(editPanel);
        panel.add(splitPane, BorderLayout.CENTER);
        
        // Load product from dropdown
        loadBtn.addActionListener(e -> {
            String selected = (String) productDropdown.getSelectedItem();
            if(selected != null) {
                // Parse product ID from dropdown
                int id = Integer.parseInt(selected.split(" - ")[0]);
                Product found = null;
                for(Product p : inventory) {
                    if(p.id == id) {
                        found = p;
                        break;
                    }
                }
                
                if(found != null) {
                    idField.setText(String.valueOf(found.id));
                    nameField.setText(found.name);
                    variantField.setText(found.variant);
                    priceField.setText(String.valueOf(found.price));
                    stockField.setText(String.valueOf(found.stock));
                    unitBox.setSelectedItem(found.unit);
                    addStockField.setText("");
                    stockInfoLabel.setText("Current Stock: " + found.stock + " " + found.unit);
                    stockInfoLabel.setForeground(found.stock < 10 ? Color.RED : new Color(0, 100, 0));
                }
            }
        });
        
        // Refresh dropdown
        refreshDropdown.addActionListener(e -> refreshProductDropdown(productDropdown));
        
        // Add to Stock button
        addStockBtn.addActionListener(e -> {
            try {
                int addQuantity = Integer.parseInt(addStockField.getText());
                if(addQuantity <= 0) {
                    JOptionPane.showMessageDialog(panel, "Please enter a positive number!", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                
                int currentStock = Integer.parseInt(stockField.getText());
                int newStock = currentStock + addQuantity;
                stockField.setText(String.valueOf(newStock));
                stockInfoLabel.setText("Current Stock: " + newStock + " " + unitBox.getSelectedItem());
                stockInfoLabel.setForeground(new Color(0, 100, 0));
                addStockField.setText("");
                
                JOptionPane.showMessageDialog(panel, 
                    "Added " + addQuantity + " units to stock!\n" +
                    "Previous Stock: " + currentStock + "\n" +
                    "New Stock: " + newStock, 
                    "Stock Updated", JOptionPane.INFORMATION_MESSAGE);
                    
            } catch(NumberFormatException ex) {
                JOptionPane.showMessageDialog(panel, "Please enter a valid number!", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });
        
        // Update button
         updateBtn.addActionListener(e -> {
            try {
                int id = Integer.parseInt(idField.getText());
                String newName = nameField.getText().trim();
                String newVariant = variantField.getText().trim();
                double newPrice = Double.parseDouble(priceField.getText());
                int newStock = Integer.parseInt(stockField.getText());
                String newUnit = (String)unitBox.getSelectedItem();
                
                // Check for duplicate name/variant with other products
                for(Product p : inventory) {
                    if(p.id != id) { // Skip the current product being edited
                        if(p.name.equalsIgnoreCase(newName)) {
                            if((newVariant.isEmpty() && p.variant.isEmpty()) || 
                               (newVariant.equalsIgnoreCase(p.variant))) {
                                JOptionPane.showMessageDialog(panel, 
                                    "Another product with this name and variant already exists!\n" +
                                    "Existing: " + p.getFullName() + " (ID: " + p.id + ")\n" +
                                    "Please use a different name or variant.", 
                                    "Duplicate Product Error", 
                                    JOptionPane.WARNING_MESSAGE);
                                return;
                            }
                        }
                    }
                }
                
                // Update the product
                for(Product p : inventory) {
                    if(p.id == id) {
                        p.name = newName;
                        p.variant = newVariant;
                        p.price = newPrice;
                        p.stock = newStock;
                        p.unit = newUnit;
                        refreshInventoryTable();
                        saveInventory();
                        refreshProductDropdown(productDropdown);
                        JOptionPane.showMessageDialog(panel, 
                            "Product Updated Successfully!\n" +
                            "Product: " + p.getFullName() + "\n" +
                            "New Price: " + p.price + " ETB\n" +
                            "New Stock: " + p.stock + " " + p.unit, 
                            "Success", 
                            JOptionPane.INFORMATION_MESSAGE);
                        break;
                    }
                }
            } catch(Exception ex) {
                JOptionPane.showMessageDialog(panel, "Please enter valid data!\n" + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });
        
        // Delete button
        deleteBtn.addActionListener(e -> {
            int confirm = JOptionPane.showConfirmDialog(panel, 
                "Are you sure you want to delete this product?\n" +
                "Product: " + nameField.getText() + "\n" +
                "This action cannot be undone!", 
                "Confirm Delete", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
            if(confirm == JOptionPane.YES_OPTION) {
                try {
                    int id = Integer.parseInt(idField.getText());
                    inventory.removeIf(p -> p.id == id);
                    refreshInventoryTable();
                    refreshProductDropdown(productDropdown);
                    saveInventory();
                    clearBtn.doClick();
                    JOptionPane.showMessageDialog(panel, "Product Deleted Successfully!");
                } catch(Exception ex) {
                    JOptionPane.showMessageDialog(panel, "Error deleting product!", "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });
        
        // Clear button
        clearBtn.addActionListener(e -> {
            idField.setText("");
            nameField.setText("");
            variantField.setText("");
            priceField.setText("");
            stockField.setText("");
            addStockField.setText("");
            unitBox.setSelectedIndex(0);
            stockInfoLabel.setText("Current Stock: --");
            productDropdown.setSelectedIndex(0);
        });
        
        return panel;
    }
    
    // Helper method to refresh product dropdown
    static void refreshProductDropdown(JComboBox<String> dropdown) {
        dropdown.removeAllItems();
        for(Product p : inventory) {
            dropdown.addItem(p.id + " - " + p.getFullName() + " | Stock: " + p.stock + " " + p.unit);
        }
    }
    
    // Check if product exists (by name and variant)
    static boolean productExists(String name, String variant) {
        for(Product p : inventory) {
            if(p.name.equalsIgnoreCase(name)) {
                if((variant.isEmpty() && p.variant.isEmpty()) || 
                   (variant.equalsIgnoreCase(p.variant))) {
                    return true;
                }
            }
        }
        return false;
    }
    
    // Get existing product if it exists
    static Product getExistingProduct(String name, String variant) {
        for(Product p : inventory) {
            if(p.name.equalsIgnoreCase(name)) {
                if((variant.isEmpty() && p.variant.isEmpty()) || 
                   (variant.equalsIgnoreCase(p.variant))) {
                    return p;
                }
            }
        }
        return null;
    }
    static JPanel salesHistoryPanel(){
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Sales History"));
        
        DefaultTableModel historyModel = new DefaultTableModel(new String[]{"Receipt No","Date","Time","Cashier","Items","Subtotal","Tax","Total"},0);
        JTable historyTable = new JTable(historyModel);
        historyTable.setFont(new Font("Arial", Font.PLAIN, 12));
        historyTable.getTableHeader().setFont(new Font("Arial", Font.BOLD, 12));
        
        JScrollPane scrollPane = new JScrollPane(historyTable);
        panel.add(scrollPane, BorderLayout.CENTER);
        
        JPanel bottomPanel = new JPanel(new FlowLayout());
        JButton refreshBtn = new JButton("Refresh");
        JButton viewDetailsBtn = new JButton("View Details");
        JButton exportBtn = new JButton("Export to CSV");
        
        refreshBtn.addActionListener(e -> refreshSalesHistoryTable(historyModel));
        viewDetailsBtn.addActionListener(e -> {
            int selectedRow = historyTable.getSelectedRow();
            if(selectedRow >= 0) {
                int receiptNo = (int) historyModel.getValueAt(selectedRow, 0);
                showSaleDetails(receiptNo);
            } else {
                JOptionPane.showMessageDialog(panel, "Please select a sale to view details!", "No Selection", JOptionPane.WARNING_MESSAGE);
            }
        });
        
        exportBtn.addActionListener(e -> exportSalesToCSV());
        
        bottomPanel.add(refreshBtn);
        bottomPanel.add(viewDetailsBtn);
        bottomPanel.add(exportBtn);
        panel.add(bottomPanel, BorderLayout.SOUTH);
        
        refreshSalesHistoryTable(historyModel);
        
        return panel;
    }
    
    static void showSaleDetails(int receiptNo) {
        for(Sale sale : salesHistory) {
            if(sale.receiptNo == receiptNo) {
                StringBuilder details = new StringBuilder();
                details.append("╔══════════════════════════════════════════════════╗\n");
                details.append("║              SALE DETAILS                         ║\n");
                details.append("╚══════════════════════════════════════════════════╝\n\n");
                details.append("Receipt No: ").append(sale.receiptNo).append("\n");
                details.append("Date: ").append(sale.date).append("\n");
                details.append("Time: ").append(sale.time).append("\n");
                details.append("Cashier: ").append(sale.cashier).append("\n\n");
                details.append("Items Sold:\n");
                details.append("────────────────────────────────────────────────────\n");
                for(SaleItem item : sale.items) {
                    String itemName = item.variant.isEmpty() ? item.name : item.name + " (" + item.variant + ")";
                    details.append(String.format("%s x%.0f @ %.2f = %.2f\n", itemName, item.qty, item.price, item.total));
                }
                details.append("────────────────────────────────────────────────────\n");
                details.append(String.format("Subtotal: %.2f\n", sale.subtotal));
                details.append(String.format("Tax (15%%): %.2f\n", sale.tax));
                details.append(String.format("GRAND TOTAL: %.2f\n", sale.grandTotal));
                
                JTextArea textArea = new JTextArea(details.toString());
                textArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
                JScrollPane scrollPane = new JScrollPane(textArea);
                scrollPane.setPreferredSize(new Dimension(500, 400));
                JOptionPane.showMessageDialog(null, scrollPane, "Sale Details - Receipt #" + receiptNo, JOptionPane.INFORMATION_MESSAGE);
                break;
            }
        }
    }
    
    static void exportSalesToCSV() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setSelectedFile(new File("sales_history_" + new SimpleDateFormat("yyyyMMdd").format(new Date()) + ".csv"));
        int result = fileChooser.showSaveDialog(null);
        
        if(result == JFileChooser.APPROVE_OPTION) {
            try (PrintWriter writer = new PrintWriter(fileChooser.getSelectedFile())) {
                writer.println("Receipt No,Date,Time,Cashier,Subtotal,Tax,Grand Total");
                for(Sale sale : salesHistory) {
                    writer.printf("%d,%s,%s,%s,%.2f,%.2f,%.2f\n", 
                        sale.receiptNo, sale.date, sale.time, sale.cashier, 
                        sale.subtotal, sale.tax, sale.grandTotal);
                }
                JOptionPane.showMessageDialog(null, "Sales exported successfully!", "Export Complete", JOptionPane.INFORMATION_MESSAGE);
            } catch(IOException e) {
                JOptionPane.showMessageDialog(null, "Error exporting sales: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    
    static void refreshSalesHistoryTable(DefaultTableModel model) {
        model.setRowCount(0);
        for(Sale sale : salesHistory) {
            int itemCount = sale.items.size();
            model.addRow(new Object[]{
                sale.receiptNo, sale.date, sale.time, sale.cashier, itemCount,
                String.format("%.2f", sale.subtotal),
                String.format("%.2f", sale.tax),
                String.format("%.2f", sale.grandTotal)
            });
        }
    }
    
    static void refreshSalesHistory() {
        // Refresh method for sales history panel
    }
    
    static JPanel reportsPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Sales Reports"));
        
        JTextArea reportArea = new JTextArea();
        reportArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        reportArea.setEditable(false);
        
        JButton generateReport = new JButton("Generate Report");
        JButton exportReport = new JButton("Export Report");
        
        generateReport.addActionListener(e -> {
            StringBuilder report = new StringBuilder();
            report.append("╔══════════════════════════════════════════════════════════╗\n");
            report.append("║                    SALES REPORT                          ║\n");
            report.append("╚══════════════════════════════════════════════════════════╝\n\n");
            report.append("Generated: ").append(new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(new Date())).append("\n\n");
            
            double totalSales = 0;
            int totalTransactions = salesHistory.size();
            
            report.append("SUMMARY\n");
            report.append("────────────────────────────────────────────────────────────\n");
            report.append(String.format("Total Transactions: %d\n", totalTransactions));
            
            for(Sale sale : salesHistory) {
                totalSales += sale.grandTotal;
            }
            report.append(String.format("Total Sales Revenue: %.2f ETB\n", totalSales));
            report.append(String.format("Average Transaction Value: %.2f ETB\n", totalTransactions > 0 ? totalSales / totalTransactions : 0));
            
            report.append("\n\nDAILY BREAKDOWN\n");
            report.append("────────────────────────────────────────────────────────────\n");
            Map<String, Double> dailySales = new HashMap<>();
            for(Sale sale : salesHistory) {
                dailySales.put(sale.date, dailySales.getOrDefault(sale.date, 0.0) + sale.grandTotal);
            }
            for(Map.Entry<String, Double> entry : dailySales.entrySet()) {
                report.append(String.format("%s: %.2f ETB\n", entry.getKey(), entry.getValue()));
            }
            
            report.append("\n\nTOP SELLING PRODUCTS\n");
            report.append("────────────────────────────────────────────────────────────\n");
            Map<String, Double> productSales = new HashMap<>();
            for(Sale sale : salesHistory) {
                for(SaleItem item : sale.items) {
                    String key = item.variant.isEmpty() ? item.name : item.name + " (" + item.variant + ")";
                    productSales.put(key, productSales.getOrDefault(key, 0.0) + item.qty);
                }
            }
            productSales.entrySet().stream()
                .sorted(Map.Entry.<String, Double>comparingByValue().reversed())
                .limit(10)
                .forEach(entry -> report.append(String.format("%s: %.0f units sold\n", entry.getKey(), entry.getValue())));
            
            reportArea.setText(report.toString());
        });
        
        exportReport.addActionListener(e -> {
            String report = reportArea.getText();
            if(report.isEmpty()) {
                JOptionPane.showMessageDialog(panel, "Please generate a report first!", "No Report", JOptionPane.WARNING_MESSAGE);
                return;
            }
            
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setSelectedFile(new File("sales_report_" + new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date()) + ".txt"));
            int result = fileChooser.showSaveDialog(null);
            
            if(result == JFileChooser.APPROVE_OPTION) {
                try (PrintWriter writer = new PrintWriter(fileChooser.getSelectedFile())) {
                    writer.print(report);
                    JOptionPane.showMessageDialog(panel, "Report exported successfully!", "Export Complete", JOptionPane.INFORMATION_MESSAGE);
                } catch(IOException ex) {
                    JOptionPane.showMessageDialog(panel, "Error exporting report: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });
        
        JPanel topPanel = new JPanel();
        topPanel.add(generateReport);
        topPanel.add(exportReport);
        panel.add(topPanel, BorderLayout.NORTH);
        panel.add(new JScrollPane(reportArea), BorderLayout.CENTER);
        
        return panel;
        }
    
    static JPanel userManagementPanel(){
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder("User Management"));
        
        // Create table with login information columns
        DefaultTableModel userModel = new DefaultTableModel(new String[]{
            "Username", "Role", "Status", "Login Time", "Logout Time", "Last Login Date"
        }, 0);
        JTable userTable = new JTable(userModel);
        userTable.setFont(new Font("Arial", Font.PLAIN, 12));
        userTable.getTableHeader().setFont(new Font("Arial", Font.BOLD, 12));
        userTable.setRowHeight(25);
        
        refreshUserTableWithLoginInfo(userModel);
        
        JScrollPane scrollPane = new JScrollPane(userTable);
        panel.add(scrollPane, BorderLayout.CENTER);
        
        // Add/Delete Users Panel at bottom
        JPanel bottomPanel = new JPanel(new GridBagLayout());
        bottomPanel.setBorder(BorderFactory.createTitledBorder("Manage Users"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        
        JTextField newUsername = new JTextField(15);
        JPasswordField newPassword = new JPasswordField(15);
        JComboBox<String> roleBox = new JComboBox<>(new String[]{"cashier", "admin"});
        JButton addUserBtn = new JButton("Add User");
        styleButton(addUserBtn, new Color(46, 139, 87));
        
        JComboBox<String> userToDelete = new JComboBox<>();
        for(User u : users) {
            if(!u.username.equals("admin")) {
                userToDelete.addItem(u.username);
            }
        }
        JButton deleteUserBtn = new JButton("Delete User");
        styleButton(deleteUserBtn, new Color(220, 53, 69));
        
        JButton refreshBtn = new JButton("Refresh");
        styleButton(refreshBtn, new Color(52, 152, 219));
        
        gbc.gridx = 0; gbc.gridy = 0;
        bottomPanel.add(new JLabel("New Username:"), gbc);
        gbc.gridx = 1;
        bottomPanel.add(newUsername, gbc);
        
        gbc.gridx = 0; gbc.gridy = 1;
        bottomPanel.add(new JLabel("Password:"), gbc);
        gbc.gridx = 1;
        bottomPanel.add(newPassword, gbc);
        
        gbc.gridx = 0; gbc.gridy = 2;
        bottomPanel.add(new JLabel("Role:"), gbc);
        gbc.gridx = 1;
        bottomPanel.add(roleBox, gbc);
        
        gbc.gridx = 1; gbc.gridy = 3;
        bottomPanel.add(addUserBtn, gbc);
        
        gbc.gridx = 0; gbc.gridy = 4;
        bottomPanel.add(new JLabel("Delete User:"), gbc);
        gbc.gridx = 1;
        bottomPanel.add(userToDelete, gbc);
        
        gbc.gridx = 0; gbc.gridy = 5;
        bottomPanel.add(refreshBtn, gbc);
        gbc.gridx = 1;
        bottomPanel.add(deleteUserBtn, gbc);
        
        panel.add(bottomPanel, BorderLayout.SOUTH);
        
        // Add User Action
        addUserBtn.addActionListener(e->{
            String username = newUsername.getText().trim();
            String password = new String(newPassword.getPassword());
            String role = (String)roleBox.getSelectedItem();
            
            if(username.isEmpty() || password.isEmpty()) {
                JOptionPane.showMessageDialog(panel, "Username and password cannot be empty!", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            for(User u : users) {
                if(u.username.equals(username)) {
                    JOptionPane.showMessageDialog(panel, "Username already exists!", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
            }
            
            User newUser = new User(username, password, role);
            newUser.isLoggedIn = false;
            newUser.loginTime = "";
            newUser.logoutTime = "";
            newUser.lastLoginDate = "";
            users.add(newUser);
            saveUsers();
            refreshUserTableWithLoginInfo(userModel);
            userToDelete.addItem(username);
            newUsername.setText("");
            newPassword.setText("");
            JOptionPane.showMessageDialog(panel, "User added successfully!");
        });
        
        // Delete User Action
        deleteUserBtn.addActionListener(e->{
            String selectedUser = (String)userToDelete.getSelectedItem();
            if(selectedUser != null) {
                int confirm = JOptionPane.showConfirmDialog(panel, "Are you sure you want to delete user: " + selectedUser + "?", 
                                                           "Confirm Delete", JOptionPane.YES_NO_OPTION);
                if(confirm == JOptionPane.YES_OPTION) {
                    users.removeIf(u -> u.username.equals(selectedUser));
                    saveUsers();
                    refreshUserTableWithLoginInfo(userModel);
                    userToDelete.removeAllItems();
                    for(User u : users) {
                        if(!u.username.equals("admin")) {
                            userToDelete.addItem(u.username);
                        }
                    }
                    JOptionPane.showMessageDialog(panel, "User deleted successfully!");
                }
            }
        });
        
        // Refresh Action
        refreshBtn.addActionListener(e -> refreshUserTableWithLoginInfo(userModel));
        
        return panel;
    }
    
        // Helper method to refresh user table with login information
    static void refreshUserTableWithLoginInfo(DefaultTableModel model) {
        model.setRowCount(0);
        for(User u : users) {
            String status = u.isLoggedIn ? "● Logged In" : "○ Logged Out";
            
            // Add null checks for all string fields
            String loginTime = (u.loginTime != null && !u.loginTime.isEmpty()) ? u.loginTime : "---";
            String logoutTime = (u.logoutTime != null && !u.logoutTime.isEmpty()) ? u.logoutTime : "---";
            String lastLogin = (u.lastLoginDate != null && !u.lastLoginDate.isEmpty()) ? u.lastLoginDate : "---";
            
            model.addRow(new Object[]{
                u.username, 
                u.role, 
                status, 
                loginTime, 
                logoutTime, 
                lastLogin
            });
        }
    }
    
    static JPanel cashierPanel(){
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(new Color(240, 248, 255));
        
        // Create a container for the top sections (header + product selection)
        JPanel northContainer = new JPanel(new BorderLayout());
        
        // TOP PANEL - Header with Cashier Info and Buttons
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBackground(new Color(0, 102, 204));
        topPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // Left side - Title
        JLabel title = new JLabel("CASHIER - POINT OF SALE", SwingConstants.LEFT);
        title.setFont(new Font("Arial", Font.BOLD, 20));
        title.setForeground(Color.WHITE);
        topPanel.add(title, BorderLayout.WEST);
        
        // Right side - All buttons
        JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        rightPanel.setBackground(new Color(0, 102, 204));
        
        JLabel cashierLabel = new JLabel("Cashier: " + currentCashier);
        cashierLabel.setForeground(Color.WHITE);
        cashierLabel.setFont(new Font("Arial", Font.BOLD, 12));
        
        JButton returnBtn = new JButton("↩️ Return Item");
        JButton changePasswordBtn = new JButton("🔑 Change Password");
        JButton logoutBtn = new JButton("🚪 LOGOUT");
        
        // Style buttons
        styleButton(returnBtn, new Color(255, 140, 0));
        styleButton(changePasswordBtn, new Color(255, 193, 7));
        styleButton(logoutBtn, new Color(220, 53, 69));
        
        // Set button sizes
        returnBtn.setPreferredSize(new Dimension(130, 35));
        changePasswordBtn.setPreferredSize(new Dimension(160, 35));
        logoutBtn.setPreferredSize(new Dimension(120, 35));
        
        // Set fonts to bold for better visibility
        returnBtn.setFont(new Font("Arial", Font.BOLD, 12));
        changePasswordBtn.setFont(new Font("Arial", Font.BOLD, 12));
        logoutBtn.setFont(new Font("Arial", Font.BOLD, 12));
        
        rightPanel.add(cashierLabel);
        rightPanel.add(Box.createRigidArea(new Dimension(10, 0))); // Spacing
        rightPanel.add(returnBtn);
        rightPanel.add(changePasswordBtn);
        rightPanel.add(logoutBtn);
        
        topPanel.add(rightPanel, BorderLayout.EAST);
        
        // PRODUCT SELECTION PANEL
        JPanel selectionPanel = new JPanel(new FlowLayout());
        selectionPanel.setBorder(BorderFactory.createTitledBorder("Add Item to Cart"));
        selectionPanel.setBackground(new Color(240, 248, 255));
        
        JComboBox<String> productCombo = new JComboBox<>();
        JTextField qty = new JTextField(5);
        JButton add = new JButton("ADD ITEM");
        JButton clearCart = new JButton("CLEAR CART");
        JButton checkout = new JButton("CHECKOUT");
        
        styleButton(add, new Color(46, 139, 87));
        styleButton(clearCart, new Color(255, 140, 0));
        styleButton(checkout, new Color(0, 102, 204));
        
        refreshProductCombo(productCombo);
        
        selectionPanel.add(new JLabel("Product:"));
        selectionPanel.add(productCombo);
        selectionPanel.add(new JLabel("Quantity:"));
        selectionPanel.add(qty);
        selectionPanel.add(add);
        selectionPanel.add(clearCart);
        selectionPanel.add(checkout);
        
        // Add both top sections to the north container
        northContainer.add(topPanel, BorderLayout.NORTH);
        northContainer.add(selectionPanel, BorderLayout.CENTER);
        
        // CART TABLE
        cartModel = new DefaultTableModel(new String[]{"Product","Qty","Unit Price","Total"},0);
        JTable table = new JTable(cartModel);
        table.setFont(new Font("Arial", Font.PLAIN, 12));
        table.getTableHeader().setFont(new Font("Arial", Font.BOLD, 12));
        table.setRowHeight(25);
        
        // RECEIPT PANEL with Print and Save Buttons
        JPanel receiptPanel = new JPanel(new BorderLayout());
        receiptArea = new JTextArea();
        receiptArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        receiptArea.setBackground(new Color(255, 255, 240));
        
        JPanel receiptButtonPanel = new JPanel(new FlowLayout());
        JButton printReceiptBtn = new JButton("🖨️ Print Receipt");
        JButton saveReceiptBtn = new JButton("💾 Save Receipt");
        styleButton(printReceiptBtn, new Color(0, 102, 204));
        styleButton(saveReceiptBtn, new Color(46, 139, 87));
        receiptButtonPanel.add(printReceiptBtn);
        receiptButtonPanel.add(saveReceiptBtn);
        
        JLabel statusLabel = new JLabel(" Ready for next item...");
        statusLabel.setFont(new Font("Arial", Font.PLAIN, 11));
        statusLabel.setForeground(new Color(100, 100, 100));
        statusLabel.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        statusLabel.setBackground(new Color(240, 248, 255));
        statusLabel.setOpaque(true);
        
        receiptPanel.add(new JScrollPane(receiptArea), BorderLayout.CENTER);
        receiptPanel.add(receiptButtonPanel, BorderLayout.SOUTH);
        
        // SPLIT PANE - Cart on left, Receipt on right
        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, new JScrollPane(table), receiptPanel);
        split.setDividerLocation(550);
        
        // Add all components to main panel
        panel.add(northContainer, BorderLayout.NORTH);
        panel.add(split, BorderLayout.CENTER);
        
        // ==================== BUTTON ACTIONS ====================
        
        // LOGOUT BUTTON ACTION - Simple logout without questions
         logoutBtn.addActionListener(e -> {
            // Record logout time for cashier
            for(User u : users) {
                if(u.username.equals(currentCashier)) {
                    u.isLoggedIn = false;
                    if(u.logoutTime == null) u.logoutTime = "";
                    u.logoutTime = new SimpleDateFormat("HH:mm:ss").format(new Date());
                    saveUsers();
                    break;
                }
            }
            currentCashier = "";
            cartModel.setRowCount(0);
            total = 0;
            updateReceipt();
            layout.show(mainPanel, "start");
        });
        
        // RETURN ITEM ACTION
        returnBtn.addActionListener(e -> {
            int selectedRow = table.getSelectedRow();
            if(selectedRow >= 0) {
                int confirm = JOptionPane.showConfirmDialog(panel, 
                    "Return item: " + cartModel.getValueAt(selectedRow, 0) + "?\n" +
                    "Quantity: " + cartModel.getValueAt(selectedRow, 1) + "\n" +
                    "Total: " + cartModel.getValueAt(selectedRow, 3) + " ETB", 
                    "Confirm Return", JOptionPane.YES_NO_OPTION);
                
                if(confirm == JOptionPane.YES_OPTION) {
                    double itemTotal = Double.parseDouble(cartModel.getValueAt(selectedRow, 3).toString());
                    cartModel.removeRow(selectedRow);
                    total -= itemTotal;
                    updateReceipt();
                    
                    JOptionPane.showMessageDialog(panel, 
                        "Item returned successfully!\n" +
                        "Amount refunded: " + String.format("%.2f", itemTotal) + " ETB", 
                        "Return Complete", JOptionPane.INFORMATION_MESSAGE);
                }
            } else {
                JOptionPane.showMessageDialog(panel, "Please select an item to return!", "No Selection", JOptionPane.WARNING_MESSAGE);
            }
        });
        
        // CHANGE PASSWORD ACTION
        changePasswordBtn.addActionListener(e -> {
            JDialog passwordDialog = new JDialog(frame, "Change Password", true);
            passwordDialog.setLayout(new GridBagLayout());
            passwordDialog.setSize(400, 250);
            passwordDialog.setLocationRelativeTo(frame);
            
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.insets = new Insets(10, 10, 10, 10);
            
            JPasswordField oldPass = new JPasswordField(15);
            JPasswordField newPass = new JPasswordField(15);
            JPasswordField confirmPass = new JPasswordField(15);
            JButton updateBtn = new JButton("Update Password");
            JButton cancelBtn = new JButton("Cancel");
            
            int row = 0;
            gbc.gridx = 0; gbc.gridy = row; gbc.anchor = GridBagConstraints.EAST;
            passwordDialog.add(new JLabel("Old Password:"), gbc);
            gbc.gridx = 1; gbc.anchor = GridBagConstraints.WEST;
            passwordDialog.add(oldPass, gbc);
            
            row++;
            gbc.gridx = 0; gbc.gridy = row;
            passwordDialog.add(new JLabel("New Password:"), gbc);
            gbc.gridx = 1;
            passwordDialog.add(newPass, gbc);
            
            row++;
            gbc.gridx = 0; gbc.gridy = row;
            passwordDialog.add(new JLabel("Confirm Password:"), gbc);
            gbc.gridx = 1;
            passwordDialog.add(confirmPass, gbc);
            
            row++;
            JPanel btnPanel = new JPanel(new FlowLayout());
            btnPanel.add(updateBtn);
            btnPanel.add(cancelBtn);
            gbc.gridx = 0; gbc.gridy = row; gbc.gridwidth = 2;
            passwordDialog.add(btnPanel, gbc);
            
            updateBtn.addActionListener(ev -> {
                String oldPassword = new String(oldPass.getPassword());
                String newPassword = new String(newPass.getPassword());
                String confirmPassword = new String(confirmPass.getPassword());
                
                for(User u : users) {
                    if(u.username.equals(currentCashier)) {
                        if(!u.password.equals(oldPassword)) {
                            JOptionPane.showMessageDialog(passwordDialog, "Old password is incorrect!", "Error", JOptionPane.ERROR_MESSAGE);
                            return;
                        }
                        if(newPassword.isEmpty()) {
                            JOptionPane.showMessageDialog(passwordDialog, "New password cannot be empty!", "Error", JOptionPane.ERROR_MESSAGE);
                            return;
                        }
                        if(!newPassword.equals(confirmPassword)) {
                            JOptionPane.showMessageDialog(passwordDialog, "New passwords do not match!", "Error", JOptionPane.ERROR_MESSAGE);
                            return;
                        }
                        u.password = newPassword;
                        JOptionPane.showMessageDialog(passwordDialog, "Password changed successfully!");
                        passwordDialog.dispose();
                        break;
                    }
                }
            });
            
            cancelBtn.addActionListener(ev -> passwordDialog.dispose());
            passwordDialog.setVisible(true);
        });
        
        // ADD ITEM ACTION - WITH PROPER STOCK VALIDATION
         add.addActionListener(e -> {
            String selectedProduct = (String)productCombo.getSelectedItem();
            if(selectedProduct == null) {
                JOptionPane.showMessageDialog(panel, "No product selected!", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            // Parse the selected product string
            String[] parts = selectedProduct.split(" \\| ");
            String productFullName = parts[0];
            
            // Find the product in inventory
            Product foundProduct = null;
            for(Product p: inventory){
                if(p.getFullName().equals(productFullName)) {
                    foundProduct = p;
                    break;
                }
            }
            
            if(foundProduct != null){
                try {
                    double qtyToAdd = Double.parseDouble(qty.getText());
                    if(qtyToAdd <= 0) {
                        JOptionPane.showMessageDialog(panel, "Quantity must be greater than 0!", "Error", JOptionPane.ERROR_MESSAGE);
                        return;
                    }
                    
                    // Calculate how many of this product are already in the cart
                    double currentQtyInCart = 0;
                    for(int i = 0; i < cartModel.getRowCount(); i++) {
                        String cartProduct = cartModel.getValueAt(i, 0).toString();
                        if(cartProduct.equals(foundProduct.getFullName())) {
                            currentQtyInCart = Double.parseDouble(cartModel.getValueAt(i, 1).toString());
                            break;
                        }
                    }
                    
                    // Check if total quantity exceeds available stock
                    double totalQtyAfterAdd = currentQtyInCart + qtyToAdd;
                    if(totalQtyAfterAdd > foundProduct.stock) {
                        JOptionPane.showMessageDialog(panel, 
                            "Insufficient stock!\n" +
                            "Available: " + foundProduct.stock + " " + foundProduct.unit + "\n" +
                            "Already in cart: " + currentQtyInCart + "\n" +
                            "Requested: " + qtyToAdd, 
                            "Stock Error", JOptionPane.WARNING_MESSAGE);
                        return;
                    }
                    
                    // Check if product already exists in cart
                    boolean productExists = false;
                    for(int i = 0; i < cartModel.getRowCount(); i++) {
                        String cartProduct = cartModel.getValueAt(i, 0).toString();
                        if(cartProduct.equals(foundProduct.getFullName())) {
                            // Update existing row
                            double newQty = currentQtyInCart + qtyToAdd;
                            double newTotal = newQty * foundProduct.price;
                            cartModel.setValueAt(newQty, i, 1);
                            cartModel.setValueAt(newTotal, i, 3);
                            productExists = true;
                            break;
                        }
                    }
                    
                    if(!productExists) {
                        // Add new row
                        double rowTotal = qtyToAdd * foundProduct.price;
                        cartModel.addRow(new Object[]{foundProduct.getFullName(), qtyToAdd, foundProduct.price, rowTotal});
                    }
                    
                    // Update total and receipt
                    updateTotal();
                    updateReceipt();
                    qty.setText("");
                    
                    // REFRESH THE DROPDOWN TO SHOW UPDATED STOCK
                    refreshProductCombo(productCombo);
                    
                    // Update status label if it exists
                    if(statusLabel != null) {
                        double newTotalQty = productExists ? 
                            Double.parseDouble(cartModel.getValueAt(findProductRow(foundProduct.getFullName()), 1).toString()) : qtyToAdd;
                        double remainingStock = foundProduct.stock - newTotalQty;
                        statusLabel.setText("✓ Added " + qtyToAdd + " " + foundProduct.getFullName() + 
                                           " | Remaining: " + remainingStock + " " + foundProduct.unit);
                        statusLabel.setForeground(new Color(0, 100, 0));
                        
                        // Reset status after 3 seconds
                        Timer timer = new Timer(3000, ev -> {
                            statusLabel.setText(" Ready for next item...");
                            statusLabel.setForeground(new Color(100, 100, 100));
                        });
                        timer.setRepeats(false);
                        timer.start();
                    }
                    
                } catch(NumberFormatException ex) {
                    JOptionPane.showMessageDialog(panel, "Please enter a valid quantity!", "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });
        // CLEAR CART ACTION
        clearCart.addActionListener(e -> {
            if(cartModel.getRowCount() > 0) {
                int confirm = JOptionPane.showConfirmDialog(panel, 
                    "Clear entire cart?", 
                    "Confirm Clear", JOptionPane.YES_NO_OPTION);
                if(confirm == JOptionPane.YES_OPTION) {
                    cartModel.setRowCount(0);
                    total = 0;
                    updateReceipt();
                }
            }
        });
        
        // CHECKOUT ACTION
        checkout.addActionListener(e -> {
            if(cartModel.getRowCount() == 0) {
                JOptionPane.showMessageDialog(panel, "Cart is empty!", "Checkout Error", JOptionPane.WARNING_MESSAGE);
                return;
            }
            
            double subtotal = 0;
            for(int i = 0; i < cartModel.getRowCount(); i++) {
                subtotal += Double.parseDouble(cartModel.getValueAt(i, 3).toString());
            }
            double tax = subtotal * 0.15;
            double grandTotal = subtotal + tax;
            
            JDialog paymentDialog = new JDialog(frame, "Payment", true);
            paymentDialog.setLayout(new GridBagLayout());
            paymentDialog.setSize(450, 350);
            paymentDialog.setLocationRelativeTo(frame);
            
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.insets = new Insets(10, 10, 10, 10);
            
            JLabel totalLabel = new JLabel("Total Amount: " + String.format("%.2f", grandTotal) + " ETB");
            totalLabel.setFont(new Font("Arial", Font.BOLD, 18));
            totalLabel.setForeground(new Color(0, 102, 204));
            
            JTextField amountPaid = new JTextField(15);
            amountPaid.setFont(new Font("Arial", Font.PLAIN, 14));
            JLabel changeLabel = new JLabel("Change: 0.00 ETB");
            changeLabel.setFont(new Font("Arial", Font.BOLD, 14));
            
            JButton processBtn = new JButton("Process Payment");
            JButton cancelBtn = new JButton("Cancel");
            styleButton(processBtn, new Color(46, 139, 87));
            styleButton(cancelBtn, new Color(128, 128, 128));
            
            amountPaid.addKeyListener(new KeyAdapter() {
                public void keyReleased(KeyEvent ev) {
                    try {
                        double paid = Double.parseDouble(amountPaid.getText());
                        double change = paid - grandTotal;
                        if(change >= 0) {
                            changeLabel.setText("Change: " + String.format("%.2f", change) + " ETB");
                            changeLabel.setForeground(new Color(0, 100, 0));
                        } else {
                            changeLabel.setText("Short: " + String.format("%.2f", Math.abs(change)) + " ETB");
                            changeLabel.setForeground(Color.RED);
                        }
                    } catch(NumberFormatException ex) {
                        changeLabel.setText("Change: 0.00 ETB");
                        changeLabel.setForeground(Color.BLACK);
                    }
                }
            });
            
            gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
            paymentDialog.add(totalLabel, gbc);
            
            gbc.gridy = 1; gbc.gridwidth = 1;
            gbc.anchor = GridBagConstraints.EAST;
            paymentDialog.add(new JLabel("Amount Paid:"), gbc);
            gbc.gridx = 1;
            gbc.anchor = GridBagConstraints.WEST;
            paymentDialog.add(amountPaid, gbc);
            
            gbc.gridy = 2; gbc.gridx = 0; gbc.gridwidth = 2;
            gbc.anchor = GridBagConstraints.CENTER;
            paymentDialog.add(changeLabel, gbc);
            
            gbc.gridy = 3;
            JPanel btnPanel = new JPanel(new FlowLayout());
            btnPanel.add(processBtn);
            btnPanel.add(cancelBtn);
            paymentDialog.add(btnPanel, gbc);
            
            processBtn.addActionListener(ev -> {
                try {
                    double paid = Double.parseDouble(amountPaid.getText());
                    if(paid >= grandTotal) {
                        double change = paid - grandTotal;
                       
                        // Update stock and show changes
                        StringBuilder stockUpdateMsg = new StringBuilder();
                        stockUpdateMsg.append("📦 Stock Updated:\n");
                        stockUpdateMsg.append("─────────────────────\n");
                        stockUpdateMsg.append(String.format("%-20s %10s → %s\n", "Product", "Before", "After"));
                        stockUpdateMsg.append("─────────────────────\n");

                        for(int i = 0; i < cartModel.getRowCount(); i++) {
                            String productName = cartModel.getValueAt(i, 0).toString();
                            double qtySold = Double.parseDouble(cartModel.getValueAt(i, 1).toString());
        
                            for(Product p : inventory) {
                                if(p.getFullName().equals(productName)) {
                                    int oldStock = p.stock;
                                    p.stock -= qtySold;
                                    stockUpdateMsg.append(String.format("%-20s %10d → %-5d %s\n", 
                                    productName.length() > 20 ? productName.substring(0, 17) + "..." : productName, oldStock, p.stock, p.unit));
                                    break;
                                }
                            }
                        }
                        
                        // Save sale to history
                        currentReceiptNo++;
                        String date = new SimpleDateFormat("dd/MM/yyyy").format(new Date());
                        String time = new SimpleDateFormat("HH:mm:ss").format(new Date());
                        Sale sale = new Sale(currentReceiptNo, date, time, currentCashier);
                        
                        double saleSubtotal = 0;
                        for(int i = 0; i < cartModel.getRowCount(); i++) {
                            String name = cartModel.getValueAt(i, 0).toString();
                            double qtySold = Double.parseDouble(cartModel.getValueAt(i, 1).toString());
                            double price = Double.parseDouble(cartModel.getValueAt(i, 2).toString());
                            double totalPrice = Double.parseDouble(cartModel.getValueAt(i, 3).toString());
                            
                            String productName = name;
                            String variant = "";
                            if(name.contains("(")) {
                                productName = name.substring(0, name.indexOf("(")).trim();
                                variant = name.substring(name.indexOf("(") + 1, name.indexOf(")"));
                            }
                            
                            sale.items.add(new SaleItem(productName, variant, qtySold, price, totalPrice));
                            saleSubtotal += totalPrice;
                        }
                        
                        sale.subtotal = saleSubtotal;
                        sale.tax = saleSubtotal * 0.15;
                        sale.grandTotal = saleSubtotal + sale.tax;
                        salesHistory.add(sale);
                        
                        saveSalesHistory();
                        saveReceiptCounter();
            
                        updateReceiptWithPayment(paid, change);
                        paymentDialog.dispose();
                        
                        updateReceiptWithPayment(paid, change);
                        paymentDialog.dispose();
                        
                        int printOption = JOptionPane.showConfirmDialog(panel, 
                            "Transaction Completed!\nReceipt: " + String.format("%06d", currentReceiptNo) + 
                            "\nChange: " + String.format("%.2f", change) + " ETB\n\nPrint receipt?", 
                            "Success", JOptionPane.YES_NO_OPTION);
                        if(printOption == JOptionPane.YES_OPTION) {
                            printReceipt();
                        }
                        
                        int saveOption = JOptionPane.showConfirmDialog(panel, 
                            "Save receipt to file?", "Save Receipt", JOptionPane.YES_NO_OPTION);
                        if(saveOption == JOptionPane.YES_OPTION) {
                            saveReceiptToFile();
                        }
                        
                        cartModel.setRowCount(0);
                        total = 0;
                        refreshProductCombo(productCombo);
                        refreshInventoryTable();
                        updateReceipt();
                        
                    } else {
                        JOptionPane.showMessageDialog(paymentDialog, 
                            "Insufficient payment!\nNeed " + String.format("%.2f", grandTotal - paid) + " ETB more.", 
                            "Error", JOptionPane.ERROR_MESSAGE);
                    }
                } catch(NumberFormatException ex) {
                    JOptionPane.showMessageDialog(paymentDialog, "Please enter a valid amount!", "Error", JOptionPane.ERROR_MESSAGE);
                }
            });
            
            cancelBtn.addActionListener(ev -> paymentDialog.dispose());
            paymentDialog.setVisible(true);
        });
        
        // PRINT RECEIPT ACTION
        printReceiptBtn.addActionListener(e -> printReceipt());
        
        // SAVE RECEIPT ACTION
        saveReceiptBtn.addActionListener(e -> saveReceiptToFile());
        
        return panel;
    }
    
    static void printReceipt() {
        try {
            String receipt = receiptArea.getText();
            if(receipt.isEmpty()) {
                JOptionPane.showMessageDialog(null, "No receipt to print!", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            // Create a printable version
            JTextArea printArea = new JTextArea(receipt);
            printArea.setFont(new Font("Monospaced", Font.PLAIN, 10));
            printArea.setSize(400, 600);
            
            int option = JOptionPane.showConfirmDialog(null, 
                new JScrollPane(printArea), 
                "Print Receipt - Preview", 
                JOptionPane.OK_CANCEL_OPTION, 
                JOptionPane.PLAIN_MESSAGE);
                
            if(option == JOptionPane.OK_OPTION) {
                try {
                    // Print the receipt
                    MessageFormat header = new MessageFormat("STEM STORE - RECEIPT");
                    MessageFormat footer = new MessageFormat("Page {0,number,integer}");
                    printArea.print(header, footer, true, null, null, false);
                    JOptionPane.showMessageDialog(null, "Receipt sent to printer!", "Print", JOptionPane.INFORMATION_MESSAGE);
                } catch(Exception ex) {
                    JOptionPane.showMessageDialog(null, "Error printing: " + ex.getMessage(), "Print Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        } catch(Exception ex) {
            JOptionPane.showMessageDialog(null, "Error preparing receipt for print: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    static void saveReceiptToFile() {
        String receipt = receiptArea.getText();
        if(receipt.isEmpty()) {
            JOptionPane.showMessageDialog(null, "No receipt to save!", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setSelectedFile(new File("receipt_" + new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date()) + ".txt"));
        int result = fileChooser.showSaveDialog(null);
        
        if(result == JFileChooser.APPROVE_OPTION) {
            try (PrintWriter writer = new PrintWriter(fileChooser.getSelectedFile())) {
                writer.print(receipt);
                JOptionPane.showMessageDialog(null, "Receipt saved successfully!", "Save Complete", JOptionPane.INFORMATION_MESSAGE);
            } catch(IOException e) {
                JOptionPane.showMessageDialog(null, "Error saving receipt: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    
    static void updateReceiptWithPayment(double amountPaid, double change) {
        String currentReceipt = receiptArea.getText();
        StringBuilder sb = new StringBuilder(currentReceipt);
        
        // Remove any existing payment info
        int paymentIndex = currentReceipt.lastIndexOf("Amount Paid");
        if(paymentIndex > 0) {
            sb = new StringBuilder(currentReceipt.substring(0, paymentIndex));
        } else {
            sb = new StringBuilder(currentReceipt);
        }
        
        sb.append("──────────────────────────────────────────────────\n");
        sb.append(String.format("%-25s %22.2f\n", "Amount Paid:", amountPaid));
        sb.append(String.format("%-25s %22.2f\n", "Change:", change));
        sb.append("──────────────────────────────────────────────────\n");
        sb.append("             Payment Completed!\n");
        sb.append("               Thank You!\n\n");
        sb.append("                  [ STORE STAMP ]\n");
        receiptArea.setText(sb.toString());
    }
    
    // Helper method to find product row in cart
    static int findProductRow(String productName) {
        for(int i = 0; i < cartModel.getRowCount(); i++) {
            if(cartModel.getValueAt(i, 0).toString().equals(productName)) {
                return i;
            }
        }
        return -1;
    }
    
    // Update total amount
    static void updateTotal() {
        total = 0;
        for(int i = 0; i < cartModel.getRowCount(); i++) {
            total += Double.parseDouble(cartModel.getValueAt(i, 3).toString());
        }
    }
    
    static void refreshProductCombo(JComboBox<String> combo) {
        combo.removeAllItems();
        for(Product p: inventory) {
            combo.addItem(p.getFullName() + " | " + p.price + " ETB | Stock: " + p.stock);
        }
    }
    
    static void updateReceipt(){
        StringBuilder sb = new StringBuilder();
        double subtotal = 0;
        
        sb.append("╔═══════════════════════════════════════════════════╗\n");
        sb.append("║                STEM STORE                 ║\n");
        sb.append("║           Addis Ababa, Ethiopia           ║\n");
        sb.append("╚═══════════════════════════════════════════════════╝\n\n");
        
        String date = new SimpleDateFormat("dd/MM/yyyy").format(new Date());
        String time = new SimpleDateFormat("HH:mm:ss").format(new Date());
        
        sb.append(String.format("%-15s: %s\n", "Receipt No", String.format("%06d", currentReceiptNo + 1)));
        sb.append(String.format("%-15s: %s\n", "Date", date));
        sb.append(String.format("%-15s: %s\n", "Time", time));
        sb.append(String.format("%-15s: %s\n", "Cashier", currentCashier.isEmpty() ? "teller1" : currentCashier));
        sb.append("\n");
        
        sb.append("───────────────────────────────────────────────────\n");
        sb.append(String.format("%-10s %8s %10s %12s\n", "Item", "Qty", "Price", "Total"));
        sb.append("───────────────────────────────────────────────────\n");
        
        for(int i=0;i<cartModel.getRowCount();i++){
            String item = cartModel.getValueAt(i,0).toString();
            String qty = cartModel.getValueAt(i,1).toString();
            double price = Double.parseDouble(cartModel.getValueAt(i,2).toString());
            double rowTotal = Double.parseDouble(cartModel.getValueAt(i,3).toString());
            
            subtotal += rowTotal;
            
            String displayItem = item;
            if(item.length() > 27) displayItem = item.substring(0, 24) + "...";
            
            sb.append(String.format("%-10s %8s %10.2f %12.2f\n", displayItem, qty, price, rowTotal));
        }
        
        double tax = subtotal * 0.15;
        double grandTotal = subtotal + tax;
        
        sb.append("───────────────────────────────────────────────────\n");
        sb.append(String.format("%30s %12.2f\n", "Subtotal:", subtotal));
        sb.append(String.format("%30s %12.2f\n", "Tax (15%):", tax));
        sb.append("───────────────────────────────────────────────────\n");
        sb.append(String.format("%30s %12.2f\n", "TOTAL:", grandTotal));
        sb.append("───────────────────────────────────────────────────\n\n");
        sb.append("              Thank You For Shopping!\n");
        sb.append("                Please Come Again!\n\n");
        sb.append("                 [ STORE STAMP ]\n");
        
        receiptArea.setText(sb.toString());
    }
}