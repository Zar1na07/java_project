package org.example;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.List;

public class LibraryApp extends Application {

    private static final String SAVE_FILE = "library_data.ser";
    private static final SimpleDateFormat SDF = new SimpleDateFormat("yyyy-MM-dd");

    // ── Shared data ──────────────────────────────────────────────────────────
    private static Library library;
    private static Librarian adminLibrarian;

    static {
        library = loadLibrary();
        if (library == null) {
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
    private Stage primaryStage;

    @Override
    public void start(Stage stage) {
        this.primaryStage = stage;
        stage.setTitle("Library Management System");
        stage.setOnCloseRequest(e -> { saveLibrary(); Platform.exit(); });
        showLoginScreen();
        stage.show();
    }

    // ════════════════════════════════════════════════════════════════════════
    // SCREENS
    // ════════════════════════════════════════════════════════════════════════
    private void showLoginScreen() {
        primaryStage.setScene(new Scene(buildLoginPane(), 980, 660));
    }

    private void showMainScreen() {
        primaryStage.setScene(new Scene(buildMainPane(), 980, 660));
    }

    // ════════════════════════════════════════════════════════════════════════
    // LOGIN PANEL
    // ════════════════════════════════════════════════════════════════════════
    private Pane buildLoginPane() {
        GridPane form = new GridPane();
        form.setHgap(10); form.setVgap(10);
        form.setPadding(new Insets(20));
        form.setAlignment(Pos.CENTER);

        ComboBox<String> roleBox = new ComboBox<>(FXCollections.observableArrayList("Member", "Librarian"));
        roleBox.setValue("Member");
        TextField emailField = new TextField();
        PasswordField passField = new PasswordField();
        Button btnLogin = new Button("Login");
        btnLogin.setMaxWidth(Double.MAX_VALUE);
        Label statusLabel = new Label();
        statusLabel.setStyle("-fx-text-fill: red;");

        form.add(new Label("Role:"),     0, 0); form.add(roleBox,     1, 0);
        form.add(new Label("Email:"),    0, 1); form.add(emailField,  1, 1);
        form.add(new Label("Password:"), 0, 2); form.add(passField,   1, 2);
        GridPane.setColumnSpan(btnLogin, 2);
        form.add(btnLogin,    0, 3);
        GridPane.setColumnSpan(statusLabel, 2);
        form.add(statusLabel, 0, 4);

        form.setStyle("-fx-border-color: #aaa; -fx-border-radius: 6; -fx-background-color: white; -fx-background-radius: 6;");
        form.setMaxSize(320, 240);

        btnLogin.setOnAction(e -> {
            String role  = roleBox.getValue();
            String email = emailField.getText().trim();
            String pass  = passField.getText();

            if ("Librarian".equals(role)) {
                Account found = library.getLibrarians().stream()
                        .filter(l -> l.getEmail().equalsIgnoreCase(email) && l.password.equals(pass))
                        .findFirst().orElse(null);
                if (found != null) doLogin(found);
                else statusLabel.setText("Invalid librarian credentials.");
            } else {
                Account found = library.getMembers().stream()
                        .filter(m -> m.getEmail().equalsIgnoreCase(email) && m.password.equals(pass))
                        .findFirst().orElse(null);
                if (found != null) {
                    if (found.status == AccountStatus.BLACKLISTED) statusLabel.setText("Account is blacklisted.");
                    else doLogin(found);
                } else statusLabel.setText("Invalid member credentials.");
            }
        });

        StackPane root = new StackPane(form);
        root.setStyle("-fx-background-color: #f0f0f0;");
        return root;
    }

    private void doLogin(Account user) {
        currentUser = user;
        Notification.send("Logged in: " + user.getName() + " (" + (user instanceof Librarian ? "Librarian" : "Member") + ")");
        showMainScreen();
    }

    // ════════════════════════════════════════════════════════════════════════
    // MAIN PANEL (role-aware)
    // ════════════════════════════════════════════════════════════════════════
    private Pane buildMainPane() {
        boolean isLibrarian = currentUser instanceof Librarian;

        TabPane tabs = new TabPane();
        tabs.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
        tabs.getTabs().add(new Tab("Books",           buildBooksTab(isLibrarian)));
        tabs.getTabs().add(new Tab("Checkout/Return", buildCheckoutTab()));
        tabs.getTabs().add(new Tab("Reservations",    buildReservationsTab()));
        tabs.getTabs().add(new Tab("Notifications",   buildNotificationsTab()));
        if (isLibrarian) tabs.getTabs().add(new Tab("Members",    buildMembersTab()));
        else             tabs.getTabs().add(new Tab("My Account", buildMyAccountTab((Member) currentUser)));

        String role = isLibrarian ? "Librarian" : "Member";
        Label userLabel = new Label("Logged in as: " + currentUser.getName() + "  [" + role + "]");
        userLabel.setStyle("-fx-font-weight: bold;");
        Button btnLogout = new Button("Logout");
        btnLogout.setOnAction(e -> {
            saveLibrary();
            currentUser = null;
            showLoginScreen();
        });

        HBox topBar = new HBox(10, userLabel, new Region(), btnLogout);
        HBox.setHgrow(topBar.getChildren().get(1), Priority.ALWAYS);
        topBar.setAlignment(Pos.CENTER_LEFT);
        topBar.setPadding(new Insets(6, 10, 6, 10));
        topBar.setStyle("-fx-background-color: #e8e8e8; -fx-border-color: #ccc; -fx-border-width: 0 0 1 0;");

        BorderPane root = new BorderPane();
        root.setTop(topBar);
        root.setCenter(tabs);
        return root;
    }

    // ════════════════════════════════════════════════════════════════════════
    // BOOKS TAB
    // ════════════════════════════════════════════════════════════════════════
    private Pane buildBooksTab(boolean isLibrarian) {
        // Table
        TableView<BookItem> table = new TableView<>();
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        TableColumn<BookItem, String> colBarcode = col("Barcode",  b -> b.barcode);
        TableColumn<BookItem, String> colTitle   = col("Title",    b -> b.title);
        TableColumn<BookItem, String> colAuthor  = col("Author",   b -> b.getAuthorsString());
        TableColumn<BookItem, String> colSubject = col("Subject",  b -> b.subject);
        TableColumn<BookItem, String> colISBN    = col("ISBN",     b -> b.ISBN);
        TableColumn<BookItem, String> colFormat  = col("Format",   b -> b.format.name());
        TableColumn<BookItem, String> colStatus  = col("Status",   b -> b.status.name());
        TableColumn<BookItem, String> colPrice   = col("Price",    b -> String.format("$%.2f", b.price));
        table.getColumns().addAll(colBarcode, colTitle, colAuthor, colSubject, colISBN, colFormat, colStatus, colPrice);
        refreshBooksTable(table, library.getCatalog().getAllBooks());

        // Search bar
        TextField searchField = new TextField();
        searchField.setPromptText("Search...");
        ComboBox<String> searchType = new ComboBox<>(FXCollections.observableArrayList("Title", "Author", "Subject", "ISBN"));
        searchType.setValue("Title");
        Button btnSearch = new Button("Search");
        Button btnClear  = new Button("Clear");

        btnSearch.setOnAction(e -> {
            String q = searchField.getText().trim();
            if (q.isEmpty()) { refreshBooksTable(table, library.getCatalog().getAllBooks()); return; }
            List<BookItem> results;
            switch (searchType.getValue()) {
                case "Author":  results = library.getCatalog().searchByAuthor(q);  break;
                case "Subject": results = library.getCatalog().searchBySubject(q); break;
                case "ISBN":    results = library.getCatalog().searchByISBN(q);    break;
                default:        results = library.getCatalog().searchByTitle(q);   break;
            }
            refreshBooksTable(table, results);
        });
        btnClear.setOnAction(e -> { searchField.clear(); refreshBooksTable(table, library.getCatalog().getAllBooks()); });

        HBox searchBar = new HBox(6, new Label("Search:"), searchField, new Label("by"), searchType, btnSearch, btnClear);
        searchBar.setAlignment(Pos.CENTER_LEFT);

        VBox top = new VBox(4, searchBar);

        if (isLibrarian) {
            Button btnAdd    = new Button("Add Book");
            Button btnRemove = new Button("Remove Book");
            btnAdd.setOnAction(e -> { showAddBookDialog(); refreshBooksTable(table, library.getCatalog().getAllBooks()); });
            btnRemove.setOnAction(e -> {
                BookItem sel = table.getSelectionModel().getSelectedItem();
                if (sel == null) { alert("Select a book first."); return; }
                library.removeBookItem(sel);
                Notification.send("Book removed: " + sel.title);
                refreshBooksTable(table, library.getCatalog().getAllBooks());
            });
            HBox btnBar = new HBox(6, btnAdd, btnRemove);
            top.getChildren().add(btnBar);
        }

        top.setPadding(new Insets(8));
        BorderPane pane = new BorderPane();
        pane.setTop(top);
        pane.setCenter(new ScrollPane(table) {{ setFitToWidth(true); setFitToHeight(true); }});
        return pane;
    }

    private void refreshBooksTable(TableView<BookItem> table, List<BookItem> books) {
        table.setItems(FXCollections.observableArrayList(books));
    }

    private void showAddBookDialog() {
        Dialog<ButtonType> dlg = new Dialog<>();
        dlg.setTitle("Add Book");
        dlg.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        TextField fISBN = new TextField(), fTitle = new TextField(), fAuthor = new TextField();
        TextField fSubject = new TextField(), fPublisher = new TextField();
        TextField fPages = new TextField("0"), fPrice = new TextField("0.00");
        ComboBox<BookFormat> fFormat = new ComboBox<>(FXCollections.observableArrayList(BookFormat.values()));
        fFormat.setValue(BookFormat.PAPERBACK);

        GridPane g = new GridPane(); g.setHgap(8); g.setVgap(6); g.setPadding(new Insets(10));
        g.addRow(0, new Label("ISBN:"),      fISBN);
        g.addRow(1, new Label("Title:"),     fTitle);
        g.addRow(2, new Label("Author:"),    fAuthor);
        g.addRow(3, new Label("Subject:"),   fSubject);
        g.addRow(4, new Label("Publisher:"), fPublisher);
        g.addRow(5, new Label("Pages:"),     fPages);
        g.addRow(6, new Label("Price ($):"), fPrice);
        g.addRow(7, new Label("Format:"),    fFormat);
        dlg.getDialogPane().setContent(g);

        dlg.showAndWait().ifPresent(bt -> {
            if (bt != ButtonType.OK) return;
            try {
                String title = fTitle.getText().trim(), author = fAuthor.getText().trim();
                if (title.isEmpty() || author.isEmpty()) { alert("Title and Author are required."); return; }
                BookItem item = new BookItem(fISBN.getText().trim(), title, fSubject.getText().trim(),
                        fPublisher.getText().trim(), "English", Integer.parseInt(fPages.getText().trim()),
                        author, Double.parseDouble(fPrice.getText().trim()), fFormat.getValue(), false);
                library.addBookItem(item);
                Notification.send("Book added: " + title + " by " + author);
            } catch (NumberFormatException ex) { alert("Invalid number for Pages or Price."); }
        });
    }

    // ════════════════════════════════════════════════════════════════════════
    // MEMBERS TAB (Librarian only)
    // ════════════════════════════════════════════════════════════════════════
    private Pane buildMembersTab() {
        TableView<Member> table = new TableView<>();
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        table.getColumns().addAll(
                col("ID",        m -> m.getId()),
                col("Name",      m -> m.getName()),
                col("Email",     m -> m.getEmail()),
                col("Phone",     m -> m.getPhone()),
                col("Status",    m -> m.getStatus().name()),
                col("Books Out", m -> String.valueOf(m.getTotalCheckedOutBooks()))
        );
        refreshMembersTable(table);

        Button btnAdd     = new Button("Add Member");
        Button btnBlock   = new Button("Block");
        Button btnUnblock = new Button("Unblock");

        btnAdd.setOnAction(e -> { showAddMemberDialog(); refreshMembersTable(table); });
        btnBlock.setOnAction(e -> {
            Member m = table.getSelectionModel().getSelectedItem();
            if (m == null) { alert("Select a member first."); return; }
            adminLibrarian.blockMember(m); Notification.send("Member blocked: " + m.getName()); refreshMembersTable(table);
        });
        btnUnblock.setOnAction(e -> {
            Member m = table.getSelectionModel().getSelectedItem();
            if (m == null) { alert("Select a member first."); return; }
            adminLibrarian.unblockMember(m); Notification.send("Member unblocked: " + m.getName()); refreshMembersTable(table);
        });

        HBox btnBar = new HBox(6, btnAdd, btnBlock, btnUnblock);
        btnBar.setPadding(new Insets(8));
        BorderPane pane = new BorderPane();
        pane.setTop(btnBar);
        pane.setCenter(table);
        return pane;
    }

    private void refreshMembersTable(TableView<Member> table) {
        table.setItems(FXCollections.observableArrayList(library.getMembers()));
    }

    private void showAddMemberDialog() {
        Dialog<ButtonType> dlg = new Dialog<>();
        dlg.setTitle("Add Member");
        dlg.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        TextField fName = new TextField(), fEmail = new TextField(), fPhone = new TextField();
        PasswordField fPass = new PasswordField();
        GridPane g = new GridPane(); g.setHgap(8); g.setVgap(6); g.setPadding(new Insets(10));
        g.addRow(0, new Label("Name:"),     fName);
        g.addRow(1, new Label("Email:"),    fEmail);
        g.addRow(2, new Label("Phone:"),    fPhone);
        g.addRow(3, new Label("Password:"), fPass);
        dlg.getDialogPane().setContent(g);

        dlg.showAndWait().ifPresent(bt -> {
            if (bt != ButtonType.OK) return;
            String name = fName.getText().trim();
            if (name.isEmpty()) { alert("Name is required."); return; }
            library.addMember(new Member(name, fEmail.getText().trim(), fPhone.getText().trim(), fPass.getText()));
            Notification.send("Member added: " + name);
        });
    }

    // ════════════════════════════════════════════════════════════════════════
    // MY ACCOUNT TAB (Member only)
    // ════════════════════════════════════════════════════════════════════════
    private Pane buildMyAccountTab(Member member) {
        TextArea info = new TextArea(
                "Name:              " + member.getName() + "\n" +
                        "Email:             " + member.getEmail() + "\n" +
                        "Phone:             " + member.getPhone() + "\n" +
                        "Member ID:         " + member.getId() + "\n" +
                        "Card Number:       " + member.libraryCard.cardNumber + "\n" +
                        "Card Barcode:      " + member.libraryCard.barcode + "\n" +
                        "Member Since:      " + SDF.format(member.dateOfMembership) + "\n" +
                        "Status:            " + member.getStatus().name() + "\n" +
                        "Books Checked Out: " + member.getTotalCheckedOutBooks() + " / " + Member.MAX_BOOKS_CHECKOUT
        );
        info.setEditable(false);
        info.setStyle("-fx-font-family: monospace; -fx-font-size: 13;");
        BorderPane pane = new BorderPane(info);
        pane.setPadding(new Insets(12));
        return pane;
    }

    // ════════════════════════════════════════════════════════════════════════
    // CHECKOUT / RETURN TAB
    // ════════════════════════════════════════════════════════════════════════
    private Pane buildCheckoutTab() {
        boolean isLibrarian = currentUser instanceof Librarian;

        // ── Checkout ──
        ComboBox<String> coMemberBox  = new ComboBox<>();
        ComboBox<String> coBarcodeBox = new ComboBox<>();
        Button btnCheckout = new Button("Checkout");

        if (isLibrarian) populateMemberCombo(coMemberBox);
        else { coMemberBox.getItems().add(currentUser.getId() + " - " + currentUser.getName()); coMemberBox.setDisable(true); }
        populateAvailableBooksCombo(coBarcodeBox);

        GridPane coGrid = new GridPane(); coGrid.setHgap(10); coGrid.setVgap(10); coGrid.setPadding(new Insets(10));
        coGrid.addRow(0, new Label("Member:"), coMemberBox);
        coGrid.addRow(1, new Label("Book:"),   coBarcodeBox);
        GridPane.setColumnSpan(btnCheckout, 2); coGrid.add(btnCheckout, 0, 2);

        TitledPane coPane = new TitledPane("Checkout Book", coGrid);
        coPane.setCollapsible(false);

        btnCheckout.setOnAction(e -> {
            String sel = coMemberBox.getValue(), bar = coBarcodeBox.getValue();
            if (sel == null || bar == null) { alert("Select member and book."); return; }
            Member member = isLibrarian ? findMemberById(sel.split(" ")[0]) : (Member) currentUser;
            BookItem book = findBookByBarcode(bar.split(" ")[0]);
            if (member == null || book == null) { alert("Not found."); return; }
            if (member.getStatus() == AccountStatus.BLACKLISTED) { alert("Member is blacklisted."); return; }
            if (member.checkoutBook(book)) {
                library.recordLending(member.activeLoans.get(member.activeLoans.size() - 1));
                Notification.send(member.getName() + " checked out: " + book.title);
                alert("Checkout successful!\nDue: " + SDF.format(book.dueDate));
                populateAvailableBooksCombo(coBarcodeBox);
            } else alert("Checkout failed. Unavailable or limit reached.");
        });

        // ── Return ──
        ComboBox<String> retMemberBox  = new ComboBox<>();
        ComboBox<String> retBarcodeBox = new ComboBox<>();
        Button btnReturn = new Button("Return");

        if (isLibrarian) {
            populateMemberCombo(retMemberBox);
            retMemberBox.setOnAction(e -> {
                String s = retMemberBox.getValue();
                if (s != null) populateLoanedBooksCombo(retBarcodeBox, findMemberById(s.split(" ")[0]));
            });
            if (!retMemberBox.getItems().isEmpty()) {
                retMemberBox.setValue(retMemberBox.getItems().get(0));
                populateLoanedBooksCombo(retBarcodeBox, findMemberById(retMemberBox.getValue().split(" ")[0]));
            }
        } else {
            retMemberBox.getItems().add(currentUser.getId() + " - " + currentUser.getName());
            retMemberBox.setDisable(true);
            populateLoanedBooksCombo(retBarcodeBox, (Member) currentUser);
        }

        GridPane retGrid = new GridPane(); retGrid.setHgap(10); retGrid.setVgap(10); retGrid.setPadding(new Insets(10));
        retGrid.addRow(0, new Label("Member:"), retMemberBox);
        retGrid.addRow(1, new Label("Book:"),   retBarcodeBox);
        GridPane.setColumnSpan(btnReturn, 2); retGrid.add(btnReturn, 0, 2);

        TitledPane retPane = new TitledPane("Return Book", retGrid);
        retPane.setCollapsible(false);

        btnReturn.setOnAction(e -> {
            String sel = retMemberBox.getValue(), bar = retBarcodeBox.getValue();
            if (sel == null || bar == null) { alert("Select member and book."); return; }
            Member member = isLibrarian ? findMemberById(sel.split(" ")[0]) : (Member) currentUser;
            BookItem book = findBookByBarcode(bar.split(" ")[0]);
            if (member == null || book == null) { alert("Not found."); return; }
            BookLending lending = member.returnBook(book);
            if (lending != null) {
                double fine = new Fine(lending).getAmount();
                String msg = "Book returned: " + book.title;
                if (fine > 0) msg += String.format("\nOverdue fine: $%.2f", fine);
                Notification.send(member.getName() + " returned: " + book.title + (fine > 0 ? " | Fine: $" + String.format("%.2f", fine) : ""));
                alert(msg);
                populateLoanedBooksCombo(retBarcodeBox, member);
                populateAvailableBooksCombo(coBarcodeBox);
            } else alert("Return failed.");
        });

        HBox layout = new HBox(12, coPane, retPane);
        layout.setPadding(new Insets(12));
        HBox.setHgrow(coPane,  Priority.ALWAYS);
        HBox.setHgrow(retPane, Priority.ALWAYS);
        return layout;
    }

    private void populateMemberCombo(ComboBox<String> combo) {
        combo.getItems().clear();
        for (Member m : library.getMembers()) combo.getItems().add(m.getId() + " - " + m.getName());
        if (!combo.getItems().isEmpty()) combo.setValue(combo.getItems().get(0));
    }

    private void populateAvailableBooksCombo(ComboBox<String> combo) {
        combo.getItems().clear();
        for (BookItem b : library.getCatalog().getAllBooks())
            if (b.status == BookStatus.AVAILABLE) combo.getItems().add(b.barcode + " - " + b.title);
        if (!combo.getItems().isEmpty()) combo.setValue(combo.getItems().get(0));
    }

    private void populateLoanedBooksCombo(ComboBox<String> combo, Member member) {
        combo.getItems().clear();
        if (member == null) return;
        for (BookLending l : member.activeLoans) combo.getItems().add(l.book.barcode + " - " + l.book.title);
        if (!combo.getItems().isEmpty()) combo.setValue(combo.getItems().get(0));
    }

    // ════════════════════════════════════════════════════════════════════════
    // RESERVATIONS TAB
    // ════════════════════════════════════════════════════════════════════════
    private Pane buildReservationsTab() {
        boolean isLibrarian = currentUser instanceof Librarian;

        TableView<BookReservation> table = new TableView<>();
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        TableColumn<BookReservation, String> colMember = new TableColumn<>("Member");
        colMember.setCellValueFactory(cd -> new SimpleStringProperty(cd.getValue().member.getName()));
        TableColumn<BookReservation, String> colBook = new TableColumn<>("Book Title");
        colBook.setCellValueFactory(cd -> new SimpleStringProperty(cd.getValue().book.title));
        TableColumn<BookReservation, String> colDate = new TableColumn<>("Date");
        colDate.setCellValueFactory(cd -> new SimpleStringProperty(SDF.format(cd.getValue().creationDate)));
        TableColumn<BookReservation, String> colStatus = new TableColumn<>("Status");
        colStatus.setCellValueFactory(cd -> new SimpleStringProperty(cd.getValue().status.name()));
        table.getColumns().addAll(colMember, colBook, colDate, colStatus);
        refreshReservationsTable(table, isLibrarian);

        Button btnReserve = new Button("Reserve Book");
        Button btnCancel  = new Button("Cancel Reservation");

        btnReserve.setOnAction(e -> { showReserveDialog(isLibrarian); refreshReservationsTable(table, isLibrarian); });
        btnCancel.setOnAction(e -> {
            BookReservation sel = table.getSelectionModel().getSelectedItem();
            if (sel == null) { alert("Select a reservation first."); return; }
            sel.cancelReservation();
            sel.book.status = BookStatus.AVAILABLE;
            Notification.send("Reservation cancelled: " + sel.book.title + " for " + sel.member.getName());
            refreshReservationsTable(table, isLibrarian);
        });

        HBox btnBar = new HBox(6, btnReserve, btnCancel);
        btnBar.setPadding(new Insets(8));
        BorderPane pane = new BorderPane();
        pane.setTop(btnBar);
        pane.setCenter(table);
        return pane;
    }

    private void refreshReservationsTable(TableView<BookReservation> table, boolean isLibrarian) {
        ObservableList<BookReservation> items = FXCollections.observableArrayList();
        for (Member m : library.getMembers()) {
            if (!isLibrarian && m != currentUser) continue;
            for (BookReservation r : m.activeReservations)
                if (r.status == ReservationStatus.WAITING || r.status == ReservationStatus.PENDING)
                    items.add(r);
        }
        table.setItems(items);
    }

    private void showReserveDialog(boolean isLibrarian) {
        ComboBox<String> memberBox = new ComboBox<>();
        ComboBox<String> bookBox   = new ComboBox<>();

        if (isLibrarian) populateMemberCombo(memberBox);
        else { memberBox.getItems().add(currentUser.getId() + " - " + currentUser.getName()); memberBox.setDisable(true); }

        for (BookItem b : library.getCatalog().getAllBooks())
            if (b.status == BookStatus.LOANED) bookBox.getItems().add(b.barcode + " - " + b.title);

        if (bookBox.getItems().isEmpty()) { alert("No loaned books to reserve."); return; }
        bookBox.setValue(bookBox.getItems().get(0));

        Dialog<ButtonType> dlg = new Dialog<>();
        dlg.setTitle("Reserve Book");
        dlg.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        GridPane g = new GridPane(); g.setHgap(8); g.setVgap(6); g.setPadding(new Insets(10));
        g.addRow(0, new Label("Member:"), memberBox);
        g.addRow(1, new Label("Book:"),   bookBox);
        dlg.getDialogPane().setContent(g);

        dlg.showAndWait().ifPresent(bt -> {
            if (bt != ButtonType.OK) return;
            String sel = memberBox.getValue(), bar = bookBox.getValue();
            if (sel == null || bar == null) return;
            Member member = isLibrarian ? findMemberById(sel.split(" ")[0]) : (Member) currentUser;
            BookItem book = findBookByBarcode(bar.split(" ")[0]);
            if (member == null || book == null) return;
            if (member.reserveBook(book)) {
                Notification.send(member.getName() + " reserved: " + book.title);
                alert("Reservation placed for: " + book.title);
            } else alert("Reservation failed. Book may be available for checkout.");
        });
    }

    // ════════════════════════════════════════════════════════════════════════
    // NOTIFICATIONS TAB
    // ════════════════════════════════════════════════════════════════════════
    private Pane buildNotificationsTab() {
        TextArea area = new TextArea();
        area.setEditable(false);
        area.setStyle("-fx-font-family: monospace; -fx-font-size: 12;");
        refreshNotifications(area);
        Button btnRefresh = new Button("Refresh");
        btnRefresh.setOnAction(e -> refreshNotifications(area));
        VBox pane = new VBox(6, btnRefresh, area);
        VBox.setVgrow(area, Priority.ALWAYS);
        pane.setPadding(new Insets(8));
        return pane;
    }

    private void refreshNotifications(TextArea area) {
        StringBuilder sb = new StringBuilder();
        for (String entry : Notification.getLog()) sb.append(entry).append("\n");
        area.setText(sb.toString());
        area.positionCaret(area.getLength());
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

    /** Generic string column factory for TableView. */
    private <T> TableColumn<T, String> col(String title, java.util.function.Function<T, String> extractor) {
        TableColumn<T, String> c = new TableColumn<>(title);
        c.setCellValueFactory(cd -> new SimpleStringProperty(extractor.apply(cd.getValue())));
        return c;
    }

    private void alert(String msg) {
        new Alert(Alert.AlertType.INFORMATION, msg, ButtonType.OK).showAndWait();
    }

    public static void main(String[] args) { launch(args); }
}

