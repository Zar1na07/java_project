package org.example;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.*;
import java.text.SimpleDateFormat;
import java.util.List;

public class LibraryApp extends JFrame {

    private static final String SAVE_FILE = "library_data.ser";
    private static final SimpleDateFormat SDF = new SimpleDateFormat("yyyy-MM-dd");

    // ── Shared data ─────────────────────────────────────────────────────────
    private static Library library;
    private static Librarian adminLibrarian;

    static {
        library = loadLibrary();
        if (library == null) {
            // First run — seed default data
            library = new Library("City Library");
            adminLibrarian = new Librarian("Admin", "admin@library.com", "555-0000", "admin");
            library.addLibrarian(adminLibrarian);

            library.addBookItem(new BookItem("978-0-06-112008-4", "To Kill a Mockingbird",
                    "Fiction", "HarperCollins", "English", 281, "Harper Lee", 12.99, BookFormat.PAPERBACK, false));
            library.addBookItem(new BookItem("978-0-7432-7356-5", "The Great Gatsby",
                    "Classic", "Scribner", "English", 180, "F. Scott Fitzgerald", 9.99, BookFormat.HARDCOVER, false));
            library.addBookItem(new BookItem("978-0-14-028329-7", "1984",
                    "Dystopian", "Penguin Books", "English", 328, "George Orwell", 11.50, BookFormat.EBOOK, false));

            library.addMember(new Member("Alice Smith", "alice@example.com", "555-1111", "pass1"));
            library.addMember(new Member("Bob Jones", "bob@example.com", "555-2222", "pass2"));
        } else {
            // Restore admin reference from loaded data
            adminLibrarian = library.getLibrarians().isEmpty() ? null : library.getLibrarians().get(0);
        }
        Notification.send("Library system started.");
    }

    private static Library loadLibrary() {
        File f = new File(SAVE_FILE);
        if (!f.exists()) return null;
        try (ObjectInputStream in = new ObjectInputStream(new FileInputStream(f))) {
            return (Library) in.readObject();
        } catch (Exception e) {
            System.out.println("Could not load save file, starting fresh. (" + e.getMessage() + ")");
            return null;
        }
    }

    private static void saveLibrary() {
        try (ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(SAVE_FILE))) {
            out.writeObject(library);
            System.out.println("Data saved to " + SAVE_FILE);
        } catch (Exception e) {
            System.out.println("Save failed: " + e.getMessage());
        }
    }

    // ── Current session ──────────────────────────────────────────────────────
    private Account currentUser = null;

    // ── Root panel (CardLayout: login / main) ────────────────────────────────
    private final CardLayout cardLayout = new CardLayout();
    private final JPanel rootPanel = new JPanel(cardLayout);

    public LibraryApp() {
        super("Library Management System");
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent e) {
                saveLibrary();
                dispose();
                System.exit(0);
            }
        });
        setSize(980, 660);
        setLocationRelativeTo(null);

        rootPanel.add(buildLoginPanel(), "LOGIN");
        // Main panel built after login so it knows the role
        add(rootPanel);
        cardLayout.show(rootPanel, "LOGIN");
    }

    // ════════════════════════════════════════════════════════════════════════
    // LOGIN PANEL
    // ════════════════════════════════════════════════════════════════════════
    private JPanel buildLoginPanel() {
        JPanel outer = new JPanel(new GridBagLayout());
        JPanel form  = new JPanel(new GridBagLayout());
        form.setBorder(BorderFactory.createTitledBorder("Library Management System — Login"));
        form.setPreferredSize(new Dimension(340, 220));

        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(8, 8, 8, 8);
        g.fill   = GridBagConstraints.HORIZONTAL;

        String[] roles = {"Member", "Librarian"};
        JComboBox<String> roleBox = new JComboBox<>(roles);
        JTextField emailField     = new JTextField(18);
        JPasswordField passField  = new JPasswordField(18);
        JButton btnLogin          = new JButton("Login");
        JLabel  statusLabel       = new JLabel(" ");
        statusLabel.setForeground(Color.RED);

        g.gridx = 0; g.gridy = 0; form.add(new JLabel("Role:"),     g);
        g.gridx = 1;               form.add(roleBox,                 g);
        g.gridx = 0; g.gridy = 1; form.add(new JLabel("Email:"),    g);
        g.gridx = 1;               form.add(emailField,              g);
        g.gridx = 0; g.gridy = 2; form.add(new JLabel("Password:"), g);
        g.gridx = 1;               form.add(passField,               g);
        g.gridx = 0; g.gridy = 3; g.gridwidth = 2; form.add(btnLogin,     g);
        g.gridy = 4;               form.add(statusLabel,             g);

        btnLogin.addActionListener(e -> {
            String role  = (String) roleBox.getSelectedItem();
            String email = emailField.getText().trim();
            String pass  = new String(passField.getPassword());

            if ("Librarian".equals(role)) {
                Account found = library.getLibrarians().stream()
                        .filter(l -> l.getEmail().equalsIgnoreCase(email) && l.password.equals(pass))
                        .findFirst().orElse(null);
                if (found != null) { doLogin(found); }
                else { statusLabel.setText("Invalid librarian credentials."); }
            } else {
                Account found = library.getMembers().stream()
                        .filter(m -> m.getEmail().equalsIgnoreCase(email) && m.password.equals(pass))
                        .findFirst().orElse(null);
                if (found != null) {
                    if (found.status == AccountStatus.BLACKLISTED) {
                        statusLabel.setText("Account is blacklisted.");
                    } else { doLogin(found); }
                } else { statusLabel.setText("Invalid member credentials."); }
            }
        });

        outer.add(form);
        return outer;
    }

    private void doLogin(Account user) {
        currentUser = user;
        Notification.send("Logged in: " + user.getName() + " (" + (user instanceof Librarian ? "Librarian" : "Member") + ")");
        rootPanel.add(buildMainPanel(), "MAIN");
        cardLayout.show(rootPanel, "MAIN");
    }

    // ════════════════════════════════════════════════════════════════════════
    // MAIN PANEL (role-aware)
    // ════════════════════════════════════════════════════════════════════════
    private JPanel buildMainPanel() {
        boolean isLibrarian = currentUser instanceof Librarian;

        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("Books",           buildBooksTab(isLibrarian));
        tabs.addTab("Checkout/Return", buildCheckoutTab());
        tabs.addTab("Reservations",    buildReservationsTab());
        tabs.addTab("Notifications",   buildNotificationsTab());
        if (isLibrarian) {
            tabs.addTab("Members", buildMembersTab());
        } else {
            tabs.addTab("My Account", buildMyAccountTab((Member) currentUser));
        }

        // Top bar with user info + logout
        JPanel topBar = new JPanel(new BorderLayout());
        String role = isLibrarian ? "Librarian" : "Member";
        JLabel userLabel = new JLabel("  Logged in as: " + currentUser.getName() + "  [" + role + "]");
        userLabel.setFont(userLabel.getFont().deriveFont(Font.BOLD));
        JButton btnLogout = new JButton("Logout");
        btnLogout.addActionListener(e -> {
            saveLibrary();
            currentUser = null;
            rootPanel.remove(rootPanel.getComponent(1));
            cardLayout.show(rootPanel, "LOGIN");
        });
        topBar.add(userLabel,  BorderLayout.WEST);
        topBar.add(btnLogout,  BorderLayout.EAST);
        topBar.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));

        JPanel main = new JPanel(new BorderLayout());
        main.add(topBar, BorderLayout.NORTH);
        main.add(tabs,   BorderLayout.CENTER);
        return main;
    }

    // ════════════════════════════════════════════════════════════════════════
    // BOOKS TAB
    // ════════════════════════════════════════════════════════════════════════
    private JPanel buildBooksTab(boolean isLibrarian) {
        JPanel panel = new JPanel(new BorderLayout(5, 5));
        panel.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

        String[] cols = {"Barcode", "Title", "Author", "Subject", "ISBN", "Format", "Status", "Price"};
        DefaultTableModel model = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable table = new JTable(model);
        refreshBooksTable(model, library.getCatalog().getAllBooks());

        JPanel searchBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        JTextField searchField = new JTextField(18);
        JComboBox<String> searchType = new JComboBox<>(new String[]{"Title", "Author", "Subject", "ISBN"});
        JButton btnSearch = new JButton("Search");
        JButton btnClear  = new JButton("Clear");
        searchBar.add(new JLabel("Search:")); searchBar.add(searchField);
        searchBar.add(new JLabel("by"));      searchBar.add(searchType);
        searchBar.add(btnSearch);             searchBar.add(btnClear);

        btnSearch.addActionListener(e -> {
            String q = searchField.getText().trim();
            if (q.isEmpty()) { refreshBooksTable(model, library.getCatalog().getAllBooks()); return; }
            List<BookItem> results;
            switch ((String) searchType.getSelectedItem()) {
                case "Author":  results = library.getCatalog().searchByAuthor(q);  break;
                case "Subject": results = library.getCatalog().searchBySubject(q); break;
                case "ISBN":    results = library.getCatalog().searchByISBN(q);    break;
                default:        results = library.getCatalog().searchByTitle(q);   break;
            }
            refreshBooksTable(model, results);
        });
        btnClear.addActionListener(e -> { searchField.setText(""); refreshBooksTable(model, library.getCatalog().getAllBooks()); });

        JPanel top = new JPanel(new BorderLayout());
        top.add(searchBar, BorderLayout.NORTH);

        // Librarian-only buttons
        if (isLibrarian) {
            JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
            JButton btnAdd    = new JButton("Add Book");
            JButton btnRemove = new JButton("Remove Book");
            btnPanel.add(btnAdd); btnPanel.add(btnRemove);

            btnAdd.addActionListener(e -> { showAddBookDialog(); refreshBooksTable(model, library.getCatalog().getAllBooks()); });
            btnRemove.addActionListener(e -> {
                int row = table.getSelectedRow();
                if (row < 0) { JOptionPane.showMessageDialog(this, "Select a book first."); return; }
                BookItem found = findBookByBarcode((String) model.getValueAt(row, 0));
                if (found != null) { library.removeBookItem(found); Notification.send("Book removed: " + found.title); refreshBooksTable(model, library.getCatalog().getAllBooks()); }
            });
            top.add(btnPanel, BorderLayout.SOUTH);
        }

        panel.add(top, BorderLayout.NORTH);
        panel.add(new JScrollPane(table), BorderLayout.CENTER);
        return panel;
    }

    private void refreshBooksTable(DefaultTableModel model, List<BookItem> books) {
        model.setRowCount(0);
        for (BookItem b : books)
            model.addRow(new Object[]{ b.barcode, b.title, b.getAuthorsString(), b.subject, b.ISBN, b.format.name(), b.status.name(), String.format("$%.2f", b.price) });
    }

    private void showAddBookDialog() {
        JTextField fISBN = new JTextField(15), fTitle = new JTextField(15), fAuthor = new JTextField(15);
        JTextField fSubject = new JTextField(15), fPublisher = new JTextField(15);
        JTextField fPages = new JTextField("0", 5), fPrice = new JTextField("0.00", 7);
        JComboBox<BookFormat> fFormat = new JComboBox<>(BookFormat.values());

        JPanel form = new JPanel(new GridLayout(0, 2, 4, 4));
        form.add(new JLabel("ISBN:"));      form.add(fISBN);
        form.add(new JLabel("Title:"));     form.add(fTitle);
        form.add(new JLabel("Author:"));    form.add(fAuthor);
        form.add(new JLabel("Subject:"));   form.add(fSubject);
        form.add(new JLabel("Publisher:")); form.add(fPublisher);
        form.add(new JLabel("Pages:"));     form.add(fPages);
        form.add(new JLabel("Price ($):")); form.add(fPrice);
        form.add(new JLabel("Format:"));    form.add(fFormat);

        if (JOptionPane.showConfirmDialog(this, form, "Add Book", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE) != JOptionPane.OK_OPTION) return;
        try {
            String title = fTitle.getText().trim(), author = fAuthor.getText().trim();
            if (title.isEmpty() || author.isEmpty()) { JOptionPane.showMessageDialog(this, "Title and Author are required."); return; }
            BookItem item = new BookItem(fISBN.getText().trim(), title, fSubject.getText().trim(),
                    fPublisher.getText().trim(), "English", Integer.parseInt(fPages.getText().trim()),
                    author, Double.parseDouble(fPrice.getText().trim()), (BookFormat) fFormat.getSelectedItem(), false);
            library.addBookItem(item);
            Notification.send("Book added: " + title + " by " + author);
        } catch (NumberFormatException ex) { JOptionPane.showMessageDialog(this, "Invalid number for Pages or Price."); }
    }

    // ════════════════════════════════════════════════════════════════════════
    // MEMBERS TAB (Librarian only)
    // ════════════════════════════════════════════════════════════════════════
    private JPanel buildMembersTab() {
        JPanel panel = new JPanel(new BorderLayout(5, 5));
        panel.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

        String[] cols = {"ID", "Name", "Email", "Phone", "Status", "Books Out"};
        DefaultTableModel model = new DefaultTableModel(cols, 0) { public boolean isCellEditable(int r, int c) { return false; } };
        JTable table = new JTable(model);
        refreshMembersTable(model);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        JButton btnAdd = new JButton("Add Member"), btnBlock = new JButton("Block"), btnUnblock = new JButton("Unblock");
        btnPanel.add(btnAdd); btnPanel.add(btnBlock); btnPanel.add(btnUnblock);

        btnAdd.addActionListener(e -> { showAddMemberDialog(); refreshMembersTable(model); });
        btnBlock.addActionListener(e -> {
            Member m = getSelectedMember(table, model);
            if (m == null) return;
            adminLibrarian.blockMember(m); Notification.send("Member blocked: " + m.getName()); refreshMembersTable(model);
        });
        btnUnblock.addActionListener(e -> {
            Member m = getSelectedMember(table, model);
            if (m == null) return;
            adminLibrarian.unblockMember(m); Notification.send("Member unblocked: " + m.getName()); refreshMembersTable(model);
        });

        panel.add(btnPanel, BorderLayout.NORTH);
        panel.add(new JScrollPane(table), BorderLayout.CENTER);
        return panel;
    }

    private void refreshMembersTable(DefaultTableModel model) {
        model.setRowCount(0);
        for (Member m : library.getMembers())
            model.addRow(new Object[]{ m.getId(), m.getName(), m.getEmail(), m.getPhone(), m.getStatus().name(), m.getTotalCheckedOutBooks() });
    }

    private void showAddMemberDialog() {
        JTextField fName = new JTextField(15), fEmail = new JTextField(15), fPhone = new JTextField(15);
        JPasswordField fPass = new JPasswordField(15);
        JPanel form = new JPanel(new GridLayout(0, 2, 4, 4));
        form.add(new JLabel("Name:"));     form.add(fName);
        form.add(new JLabel("Email:"));    form.add(fEmail);
        form.add(new JLabel("Phone:"));    form.add(fPhone);
        form.add(new JLabel("Password:")); form.add(fPass);
        if (JOptionPane.showConfirmDialog(this, form, "Add Member", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE) != JOptionPane.OK_OPTION) return;
        String name = fName.getText().trim();
        if (name.isEmpty()) { JOptionPane.showMessageDialog(this, "Name is required."); return; }
        Member member = new Member(name, fEmail.getText().trim(), fPhone.getText().trim(), new String(fPass.getPassword()));
        library.addMember(member);
        Notification.send("Member added: " + name);
    }

    private Member getSelectedMember(JTable table, DefaultTableModel model) {
        int row = table.getSelectedRow();
        if (row < 0) { JOptionPane.showMessageDialog(this, "Select a member first."); return null; }
        String id = (String) model.getValueAt(row, 0);
        return library.getMembers().stream().filter(m -> m.getId().equals(id)).findFirst().orElse(null);
    }

    // ════════════════════════════════════════════════════════════════════════
    // MY ACCOUNT TAB (Member only)
    // ════════════════════════════════════════════════════════════════════════
    private JPanel buildMyAccountTab(Member member) {
        JPanel panel = new JPanel(new BorderLayout(5, 5));
        panel.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

        JTextArea info = new JTextArea();
        info.setEditable(false);
        info.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 13));
        info.setText(
                "Name:            " + member.getName() + "\n" +
                        "Email:           " + member.getEmail() + "\n" +
                        "Phone:           " + member.getPhone() + "\n" +
                        "Member ID:       " + member.getId() + "\n" +
                        "Card Number:     " + member.libraryCard.cardNumber + "\n" +
                        "Card Barcode:    " + member.libraryCard.barcode + "\n" +
                        "Member Since:    " + SDF.format(member.dateOfMembership) + "\n" +
                        "Status:          " + member.getStatus().name() + "\n" +
                        "Books Checked Out: " + member.getTotalCheckedOutBooks() + " / " + Member.MAX_BOOKS_CHECKOUT
        );

        panel.add(new JScrollPane(info), BorderLayout.CENTER);
        return panel;
    }

    // ════════════════════════════════════════════════════════════════════════
    // CHECKOUT / RETURN TAB
    // ════════════════════════════════════════════════════════════════════════
    private JPanel buildCheckoutTab() {
        boolean isLibrarian = currentUser instanceof Librarian;
        JPanel panel = new JPanel(new GridLayout(1, 2, 12, 0));
        panel.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

        // ── Checkout ──
        JPanel coPanel = new JPanel(new GridBagLayout());
        coPanel.setBorder(BorderFactory.createTitledBorder("Checkout Book"));
        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(6,6,6,6); g.fill = GridBagConstraints.HORIZONTAL;

        JComboBox<String> coMemberBox = new JComboBox<>();
        JComboBox<String> coBarcodeBox = new JComboBox<>();
        JButton btnCheckout = new JButton("Checkout");

        if (isLibrarian) { populateMemberCombo(coMemberBox); }
        else { coMemberBox.addItem(currentUser.getId() + " - " + currentUser.getName()); coMemberBox.setEnabled(false); }
        populateAvailableBooksCombo(coBarcodeBox);

        g.gridx=0; g.gridy=0; coPanel.add(new JLabel("Member:"), g);
        g.gridx=1; coPanel.add(coMemberBox, g);
        g.gridx=0; g.gridy=1; coPanel.add(new JLabel("Book:"), g);
        g.gridx=1; coPanel.add(coBarcodeBox, g);
        g.gridx=0; g.gridy=2; g.gridwidth=2; coPanel.add(btnCheckout, g);

        btnCheckout.addActionListener(e -> {
            String sel = (String) coMemberBox.getSelectedItem();
            String bar = (String) coBarcodeBox.getSelectedItem();
            if (sel == null || bar == null) { JOptionPane.showMessageDialog(this, "Select member and book."); return; }
            Member member = isLibrarian ? findMemberById(sel.split(" ")[0]) : (Member) currentUser;
            BookItem book = findBookByBarcode(bar.split(" ")[0]);
            if (member == null || book == null) { JOptionPane.showMessageDialog(this, "Not found."); return; }
            if (member.getStatus() == AccountStatus.BLACKLISTED) { JOptionPane.showMessageDialog(this, "Member is blacklisted."); return; }
            if (member.checkoutBook(book)) {
                library.recordLending(member.activeLoans.get(member.activeLoans.size()-1));
                Notification.send(member.getName() + " checked out: " + book.title);
                JOptionPane.showMessageDialog(this, "Checkout successful!\nDue: " + SDF.format(book.dueDate));
                populateAvailableBooksCombo(coBarcodeBox);
            } else { JOptionPane.showMessageDialog(this, "Checkout failed. Unavailable or limit reached."); }
        });

        // ── Return ──
        JPanel retPanel = new JPanel(new GridBagLayout());
        retPanel.setBorder(BorderFactory.createTitledBorder("Return Book"));
        GridBagConstraints g2 = new GridBagConstraints();
        g2.insets = new Insets(6,6,6,6); g2.fill = GridBagConstraints.HORIZONTAL;

        JComboBox<String> retMemberBox = new JComboBox<>();
        JComboBox<String> retBarcodeBox = new JComboBox<>();
        JButton btnReturn = new JButton("Return");

        if (isLibrarian) {
            populateMemberCombo(retMemberBox);
            retMemberBox.addActionListener(e -> {
                String s = (String) retMemberBox.getSelectedItem();
                if (s != null) populateLoanedBooksCombo(retBarcodeBox, findMemberById(s.split(" ")[0]));
            });
            if (retMemberBox.getItemCount() > 0) {
                String s = (String) retMemberBox.getSelectedItem();
                if (s != null) populateLoanedBooksCombo(retBarcodeBox, findMemberById(s.split(" ")[0]));
            }
        } else {
            retMemberBox.addItem(currentUser.getId() + " - " + currentUser.getName());
            retMemberBox.setEnabled(false);
            populateLoanedBooksCombo(retBarcodeBox, (Member) currentUser);
        }

        g2.gridx=0; g2.gridy=0; retPanel.add(new JLabel("Member:"), g2);
        g2.gridx=1; retPanel.add(retMemberBox, g2);
        g2.gridx=0; g2.gridy=1; retPanel.add(new JLabel("Book:"), g2);
        g2.gridx=1; retPanel.add(retBarcodeBox, g2);
        g2.gridx=0; g2.gridy=2; g2.gridwidth=2; retPanel.add(btnReturn, g2);

        btnReturn.addActionListener(e -> {
            String sel = (String) retMemberBox.getSelectedItem();
            String bar = (String) retBarcodeBox.getSelectedItem();
            if (sel == null || bar == null) { JOptionPane.showMessageDialog(this, "Select member and book."); return; }
            Member member = isLibrarian ? findMemberById(sel.split(" ")[0]) : (Member) currentUser;
            BookItem book = findBookByBarcode(bar.split(" ")[0]);
            if (member == null || book == null) { JOptionPane.showMessageDialog(this, "Not found."); return; }
            BookLending lending = member.returnBook(book);
            if (lending != null) {
                double fine = new Fine(lending).getAmount();
                String msg = "Book returned: " + book.title;
                if (fine > 0) msg += String.format("\nOverdue fine: $%.2f", fine);
                Notification.send(member.getName() + " returned: " + book.title + (fine > 0 ? " | Fine: $" + String.format("%.2f", fine) : ""));
                JOptionPane.showMessageDialog(this, msg);
                populateLoanedBooksCombo(retBarcodeBox, member);
                populateAvailableBooksCombo(coBarcodeBox);
            } else { JOptionPane.showMessageDialog(this, "Return failed."); }
        });

        panel.add(coPanel); panel.add(retPanel);
        return panel;
    }

    private void populateMemberCombo(JComboBox<String> combo) {
        combo.removeAllItems();
        for (Member m : library.getMembers()) combo.addItem(m.getId() + " - " + m.getName());
    }

    private void populateAvailableBooksCombo(JComboBox<String> combo) {
        combo.removeAllItems();
        for (BookItem b : library.getCatalog().getAllBooks())
            if (b.status == BookStatus.AVAILABLE) combo.addItem(b.barcode + " - " + b.title);
    }

    private void populateLoanedBooksCombo(JComboBox<String> combo, Member member) {
        combo.removeAllItems();
        if (member == null) return;
        for (BookLending l : member.activeLoans) combo.addItem(l.book.barcode + " - " + l.book.title);
    }

    // ════════════════════════════════════════════════════════════════════════
    // RESERVATIONS TAB
    // ════════════════════════════════════════════════════════════════════════
    private JPanel buildReservationsTab() {
        boolean isLibrarian = currentUser instanceof Librarian;
        JPanel panel = new JPanel(new BorderLayout(5, 5));
        panel.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

        String[] cols = {"Member", "Book Title", "Date", "Status"};
        DefaultTableModel model = new DefaultTableModel(cols, 0) { public boolean isCellEditable(int r, int c) { return false; } };
        JTable table = new JTable(model);
        refreshReservationsTable(model, isLibrarian);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        JButton btnReserve = new JButton("Reserve Book");
        JButton btnCancel  = new JButton("Cancel Reservation");
        btnPanel.add(btnReserve); btnPanel.add(btnCancel);

        btnReserve.addActionListener(e -> { showReserveDialog(isLibrarian); refreshReservationsTable(model, isLibrarian); });
        btnCancel.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row < 0) { JOptionPane.showMessageDialog(this, "Select a reservation first."); return; }
            String memberName = (String) model.getValueAt(row, 0);
            String bookTitle  = (String) model.getValueAt(row, 1);
            for (Member m : library.getMembers()) {
                if (!isLibrarian && m != currentUser) continue;
                for (BookReservation r : m.activeReservations) {
                    if (m.getName().equals(memberName) && r.book.title.equals(bookTitle) && r.status == ReservationStatus.WAITING) {
                        r.cancelReservation(); r.book.status = BookStatus.AVAILABLE;
                        Notification.send("Reservation cancelled: " + bookTitle + " for " + memberName);
                        refreshReservationsTable(model, isLibrarian); return;
                    }
                }
            }
            JOptionPane.showMessageDialog(this, "Could not cancel reservation.");
        });

        panel.add(btnPanel, BorderLayout.NORTH);
        panel.add(new JScrollPane(table), BorderLayout.CENTER);
        return panel;
    }

    private void refreshReservationsTable(DefaultTableModel model, boolean isLibrarian) {
        model.setRowCount(0);
        for (Member m : library.getMembers()) {
            if (!isLibrarian && m != currentUser) continue;
            for (BookReservation r : m.activeReservations)
                if (r.status == ReservationStatus.WAITING || r.status == ReservationStatus.PENDING)
                    model.addRow(new Object[]{ m.getName(), r.book.title, SDF.format(r.creationDate), r.status.name() });
        }
    }

    private void showReserveDialog(boolean isLibrarian) {
        JComboBox<String> memberBox = new JComboBox<>();
        JComboBox<String> bookBox   = new JComboBox<>();

        if (isLibrarian) { populateMemberCombo(memberBox); }
        else { memberBox.addItem(currentUser.getId() + " - " + currentUser.getName()); memberBox.setEnabled(false); }

        for (BookItem b : library.getCatalog().getAllBooks())
            if (b.status == BookStatus.LOANED) bookBox.addItem(b.barcode + " - " + b.title);

        if (bookBox.getItemCount() == 0) { JOptionPane.showMessageDialog(this, "No loaned books to reserve."); return; }

        JPanel form = new JPanel(new GridLayout(0, 2, 4, 4));
        form.add(new JLabel("Member:")); form.add(memberBox);
        form.add(new JLabel("Book:"));   form.add(bookBox);

        if (JOptionPane.showConfirmDialog(this, form, "Reserve Book", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE) != JOptionPane.OK_OPTION) return;

        String sel = (String) memberBox.getSelectedItem();
        String bar = (String) bookBox.getSelectedItem();
        if (sel == null || bar == null) return;

        Member member = isLibrarian ? findMemberById(sel.split(" ")[0]) : (Member) currentUser;
        BookItem book = findBookByBarcode(bar.split(" ")[0]);
        if (member == null || book == null) return;

        if (member.reserveBook(book)) {
            Notification.send(member.getName() + " reserved: " + book.title);
            JOptionPane.showMessageDialog(this, "Reservation placed for: " + book.title);
        } else { JOptionPane.showMessageDialog(this, "Reservation failed. Book may be available for checkout."); }
    }

    // ════════════════════════════════════════════════════════════════════════
    // NOTIFICATIONS TAB
    // ════════════════════════════════════════════════════════════════════════
    private JPanel buildNotificationsTab() {
        JPanel panel = new JPanel(new BorderLayout(5, 5));
        panel.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        JTextArea area = new JTextArea();
        area.setEditable(false);
        area.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        refreshNotifications(area);
        JButton btnRefresh = new JButton("Refresh");
        btnRefresh.addActionListener(e -> refreshNotifications(area));
        panel.add(btnRefresh, BorderLayout.NORTH);
        panel.add(new JScrollPane(area), BorderLayout.CENTER);
        return panel;
    }

    private void refreshNotifications(JTextArea area) {
        StringBuilder sb = new StringBuilder();
        for (String entry : Notification.getLog()) sb.append(entry).append("\n");
        area.setText(sb.toString());
        area.setCaretPosition(area.getDocument().getLength());
    }

    // ════════════════════════════════════════════════════════════════════════
    // HELPERS
    // ════════════════════════════════════════════════════════════════════════
    private BookItem findBookByBarcode(String barcode) {
        return library.getCatalog().getAllBooks().stream().filter(b -> b.barcode.equals(barcode)).findFirst().orElse(null);
    }

    private Member findMemberById(String id) {
        return library.getMembers().stream().filter(m -> m.getId().equals(id)).findFirst().orElse(null);
    }

    // ════════════════════════════════════════════════════════════════════════
    // MAIN
    // ════════════════════════════════════════════════════════════════════════
    public static void main(String[] args) {
        try {
            for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels())
                if ("Nimbus".equals(info.getName())) { UIManager.setLookAndFeel(info.getClassName()); break; }
        } catch (Exception e) {
            try { UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); } catch (Exception ignored) {}
        }
        SwingUtilities.invokeLater(() -> new LibraryApp().setVisible(true));
    }
}
