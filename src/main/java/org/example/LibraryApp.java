package org.example;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;
import javafx.scene.paint.CycleMethod;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.TextAlignment;
import javafx.stage.Stage;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.function.Function;

public class LibraryApp extends Application {

    // ── Files ────────────────────────────────────────────────────────────────
    private static final String SAVE_FILE     = "library_data.ser";
    private static final String READABLE_FILE = "library_data.txt";
    private static final SimpleDateFormat SDF = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    // ── Palette ──────────────────────────────────────────────────────────────
    private static final String C_NAVY    = "#1A252F";
    private static final String C_DARK    = "#2C3E50";
    private static final String C_BLUE    = "#2980B9";
    private static final String C_TEAL    = "#16A085";
    private static final String C_GREEN   = "#27AE60";
    private static final String C_RED     = "#C0392B";
    private static final String C_ORANGE  = "#D35400";
    private static final String C_PURPLE  = "#8E44AD";
    private static final String C_BG      = "#F0F3F4";
    private static final String C_CARD    = "#FFFFFF";
    private static final String C_BORDER  = "#D5D8DC";
    private static final String C_MUTED   = "#85929E";
    private static final String C_TEXT    = "#2C3E50";

    // ── Shared data ──────────────────────────────────────────────────────────
    private static Library library;

    static {
        library = loadLibrary();
        if (library == null) {
            library = new Library("City Library");
            library.addLibrarian(new Librarian("Admin", "admin@library.com", "555-0000", "admin"));
            library.addBookItem(new BookItem("978-0-06-112008-4", "To Kill a Mockingbird",
                    "Fiction", "HarperCollins", "English", 281, "Harper Lee", 12.99, BookFormat.PAPERBACK, false));
            library.addBookItem(new BookItem("978-0-7432-7356-5", "The Great Gatsby",
                    "Classic", "Scribner", "English", 180, "F. Scott Fitzgerald", 9.99, BookFormat.HARDCOVER, false));
            library.addBookItem(new BookItem("978-0-14-028329-7", "1984",
                    "Dystopian", "Penguin Books", "English", 328, "George Orwell", 11.50, BookFormat.EBOOK, false));
            library.addBookItem(new BookItem("978-0-7432-7357-2", "Brave New World",
                    "Dystopian", "Harper Perennial", "English", 311, "Aldous Huxley", 10.99, BookFormat.PAPERBACK, false));
            library.addBookItem(new BookItem("978-0-14-303943-3", "The Catcher in the Rye",
                    "Fiction", "Little, Brown", "English", 277, "J.D. Salinger", 9.50, BookFormat.PAPERBACK, false));
            library.addMember(new Member("Alice Smith", "alice@example.com", "555-1111", "pass1"));
            library.addMember(new Member("Bob Jones",   "bob@example.com",   "555-2222", "pass2"));
        }
        Notification.send("Library system started.");
    }

    private Account currentUser = null;
    private Stage   primaryStage;

    // ════════════════════════════════════════════════════════════════════════
    // ENTRY
    // ════════════════════════════════════════════════════════════════════════
    @Override
    public void start(Stage stage) {
        this.primaryStage = stage;
        stage.setTitle("City Library Management System");
        stage.setMinWidth(860); stage.setMinHeight(560);
        stage.setOnCloseRequest(e -> { saveLibrary(); Platform.exit(); });
        showAuthScreen();
        stage.show();
    }

    // ════════════════════════════════════════════════════════════════════════
    // SAVE / LOAD
    // ════════════════════════════════════════════════════════════════════════
    private static Library loadLibrary() {
        File f = new File(SAVE_FILE);
        if (!f.exists()) return null;
        try (ObjectInputStream in = new ObjectInputStream(new FileInputStream(f))) {
            return (Library) in.readObject();
        } catch (Exception e) {
            System.out.println("Load failed: " + e.getMessage());
            return null;
        }
    }

    private static void saveLibrary() {
        try (ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(SAVE_FILE))) {
            out.writeObject(library);
        } catch (Exception e) { System.out.println("Save failed: " + e.getMessage()); return; }

        try (PrintWriter pw = new PrintWriter(new FileWriter(READABLE_FILE))) {
            pw.println("=".repeat(70));
            pw.println("  CITY LIBRARY — SNAPSHOT   " + SDF.format(new Date()));
            pw.println("=".repeat(70));
            pw.println("\n--- LIBRARIANS ---");
            for (Librarian l : library.getLibrarians())
                pw.printf("  %-10s  %-22s  %s%n", l.getId(), l.getName(), l.getEmail());
            pw.println("\n--- MEMBERS ---");
            for (Member m : library.getMembers())
                pw.printf("  %-10s  %-22s  %-28s  %-12s  Books: %d/%d%n",
                        m.getId(), m.getName(), m.getEmail(), m.status.name(),
                        m.totalBooksCheckedOut, Member.MAX_BOOKS_CHECKOUT);
            pw.println("\n--- BOOKS ---");
            for (BookItem b : library.getCatalog().getAllBooks())
                pw.printf("  %-14s  %-10s  %-38s  %s%n", b.barcode, b.status.name(), b.title, b.getAuthorsString());
            pw.println("\n--- ACTIVE LENDINGS ---");
            for (BookLending l : library.getAllLendings()) {
                if (l.returnDate != null) continue;
                double fine = l.calculateFine();
                pw.printf("  %-22s  %-38s  Due: %s%s%n", l.member.getName(), l.book.title,
                        SDF.format(l.dueDate), fine > 0 ? String.format("  FINE: $%.2f", fine) : "");
            }
            pw.println("\n--- RESERVATIONS ---");
            for (Member m : library.getMembers())
                for (BookReservation r : m.activeReservations)
                    pw.printf("  %-22s  %-38s  %s%n", m.getName(), r.book.title, r.status.name());
            pw.println("\n" + "=".repeat(70));
        } catch (Exception e) { System.out.println("Readable export failed: " + e.getMessage()); }
    }

    // ════════════════════════════════════════════════════════════════════════
    // AUTH SCREEN
    // ════════════════════════════════════════════════════════════════════════
    private void showAuthScreen() {
        // ── Left gradient panel ──
        StackPane brand = new StackPane();
        brand.setPrefWidth(300);
        brand.setMinWidth(260);

        Rectangle gradRect = new Rectangle();
        gradRect.widthProperty().bind(brand.widthProperty());
        gradRect.heightProperty().bind(brand.heightProperty());
        gradRect.setFill(new LinearGradient(0, 0, 0, 1, true, CycleMethod.NO_CYCLE,
                new Stop(0, Color.web("#1A252F")),
                new Stop(1, Color.web("#2C3E50"))));

        VBox brandContent = new VBox(14);
        brandContent.setAlignment(Pos.CENTER);
        brandContent.setPadding(new Insets(40, 30, 40, 30));

        Label icon = new Label("📚");
        icon.setFont(Font.font(72));

        Label title = new Label("City Library");
        title.setFont(Font.font("System", FontWeight.BOLD, 30));
        title.setTextFill(Color.WHITE);

        Label sub = new Label("Management System");
        sub.setFont(Font.font("System", FontWeight.NORMAL, 14));
        sub.setTextFill(Color.web("#AEB6BF"));

        Separator sep = new Separator();
        sep.setStyle("-fx-background-color: #566573;");
        sep.setMaxWidth(160);

        Label quote = new Label("\"A reader lives a thousand lives\nbefore he dies.\"");
        quote.setFont(Font.font("System", FontWeight.NORMAL, 12));
        quote.setTextFill(Color.web("#7F8C8D"));
        quote.setTextAlignment(TextAlignment.CENTER);
        quote.setWrapText(true);

        // Small stat pills
        HBox stats = new HBox(10,
                statPill("📖 " + library.getCatalog().getAllBooks().size() + " Books"),
                statPill("👥 " + library.getMembers().size() + " Members")
        );
        stats.setAlignment(Pos.CENTER);

        brandContent.getChildren().addAll(icon, title, sub, sep, quote, stats);
        brand.getChildren().addAll(gradRect, brandContent);

        // ── Right: tabbed auth ──
        TabPane authTabs = new TabPane();
        authTabs.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
        authTabs.setStyle(
                "-fx-background-color: " + C_BG + ";" +
                        "-fx-tab-min-width: 120px;" +
                        "-fx-tab-min-height: 36px;"
        );
        authTabs.getTabs().addAll(
                new Tab("  🔑  Login  ",    buildLoginForm()),
                new Tab("  ✏  Register  ", buildRegisterForm())
        );

        HBox root = new HBox(brand, authTabs);
        HBox.setHgrow(authTabs, Priority.ALWAYS);
        primaryStage.setScene(new Scene(root, 900, 560));
    }

    private Label statPill(String text) {
        Label l = new Label(text);
        l.setFont(Font.font(11));
        l.setTextFill(Color.web("#AEB6BF"));
        l.setStyle("-fx-background-color: rgba(255,255,255,0.08); -fx-background-radius: 20; -fx-padding: 4 10 4 10;");
        return l;
    }

    // ── Login form ───────────────────────────────────────────────────────────
    private ScrollPane buildLoginForm() {
        VBox outer = new VBox(0);
        outer.setStyle("-fx-background-color: " + C_BG + ";");
        outer.setAlignment(Pos.CENTER);
        outer.setFillWidth(true);

        VBox card = new VBox(16);
        card.setPadding(new Insets(36, 44, 36, 44));
        card.setMaxWidth(420);
        card.setStyle(cardStyle());

        Label heading = new Label("Welcome Back");
        heading.setFont(Font.font("System", FontWeight.BOLD, 24));
        heading.setTextFill(Color.web(C_TEXT));
        Label subLbl = new Label("Sign in to continue");
        subLbl.setFont(Font.font(13));
        subLbl.setTextFill(Color.web(C_MUTED));

        ComboBox<String> roleBox  = fxCombo("Member", "Librarian");
        TextField        email    = fxField("Email address");
        PasswordField    pass     = fxPass("Password");
        Button           btnLogin = btn("Login", C_BLUE);
        btnLogin.setMaxWidth(Double.MAX_VALUE);
        Label status = new Label(); status.setWrapText(true);

        card.getChildren().addAll(
                heading, subLbl, new Separator(),
                fieldGroup("Role",     roleBox),
                fieldGroup("Email",    email),
                fieldGroup("Password", pass),
                btnLogin, status
        );

        btnLogin.setOnAction(e -> {
            String role = roleBox.getValue(), em = email.getText().trim(), pw = pass.getText();
            if (em.isEmpty() || pw.isEmpty()) { errLabel(status, "Please fill in all fields."); return; }
            if ("Librarian".equals(role)) {
                Account found = library.getLibrarians().stream()
                        .filter(l -> l.getEmail().equalsIgnoreCase(em) && l.password.equals(pw))
                        .findFirst().orElse(null);
                if (found != null) doLogin(found);
                else errLabel(status, "Invalid librarian credentials.");
            } else {
                Account found = library.getMembers().stream()
                        .filter(m -> m.getEmail().equalsIgnoreCase(em) && m.password.equals(pw))
                        .findFirst().orElse(null);
                if (found != null) {
                    if (found.status == AccountStatus.BLACKLISTED) errLabel(status, "Account suspended.");
                    else doLogin(found);
                } else errLabel(status, "Invalid member credentials.");
            }
        });

        StackPane wrap = new StackPane(card);
        wrap.setStyle("-fx-background-color: " + C_BG + ";");
        wrap.setPadding(new Insets(30));
        outer.getChildren().add(wrap);
        VBox.setVgrow(wrap, Priority.ALWAYS);

        ScrollPane sp = new ScrollPane(outer);
        sp.setFitToWidth(true); sp.setFitToHeight(true);
        sp.setStyle("-fx-background-color: " + C_BG + "; -fx-background: " + C_BG + ";");
        return sp;
    }

    // ── Register form ────────────────────────────────────────────────────────
    private ScrollPane buildRegisterForm() {
        VBox outer = new VBox(0);
        outer.setStyle("-fx-background-color: " + C_BG + ";");
        outer.setAlignment(Pos.CENTER);

        VBox card = new VBox(14);
        card.setPadding(new Insets(36, 44, 36, 44));
        card.setMaxWidth(420);
        card.setStyle(cardStyle());

        Label heading = new Label("Create Account");
        heading.setFont(Font.font("System", FontWeight.BOLD, 24));
        heading.setTextFill(Color.web(C_TEXT));
        Label subLbl = new Label("Register as Member or Librarian");
        subLbl.setFont(Font.font(13));
        subLbl.setTextFill(Color.web(C_MUTED));

        ComboBox<String> roleBox  = fxCombo("Member", "Librarian");
        TextField        name     = fxField("Full name");
        TextField        email    = fxField("Email address");
        TextField        phone    = fxField("Phone number");
        PasswordField    pass     = fxPass("Password");
        PasswordField    confirm  = fxPass("Confirm password");
        Button           btnReg   = btn("Create Account", C_TEAL);
        btnReg.setMaxWidth(Double.MAX_VALUE);
        Label status = new Label(); status.setWrapText(true);

        card.getChildren().addAll(
                heading, subLbl, new Separator(),
                fieldGroup("Role",             roleBox),
                fieldGroup("Full Name",        name),
                fieldGroup("Email",            email),
                fieldGroup("Phone",            phone),
                fieldGroup("Password",         pass),
                fieldGroup("Confirm Password", confirm),
                btnReg, status
        );

        btnReg.setOnAction(e -> {
            String role = roleBox.getValue(), nm = name.getText().trim(),
                    em = email.getText().trim(), ph = phone.getText().trim(),
                    pw = pass.getText(), cf = confirm.getText();
            if (nm.isEmpty() || em.isEmpty() || pw.isEmpty()) { errLabel(status, "Name, email and password are required."); return; }
            if (!pw.equals(cf)) { errLabel(status, "Passwords do not match."); return; }
            boolean taken = library.getMembers().stream().anyMatch(m -> m.getEmail().equalsIgnoreCase(em))
                    || library.getLibrarians().stream().anyMatch(l -> l.getEmail().equalsIgnoreCase(em));
            if (taken) { errLabel(status, "An account with that email already exists."); return; }
            if ("Librarian".equals(role)) library.addLibrarian(new Librarian(nm, em, ph, pw));
            else                          library.addMember(new Member(nm, em, ph, pw));
            saveLibrary();
            Notification.send("New " + role + " registered: " + nm);
            okLabel(status, "✔  Account created! You can now log in.");
            name.clear(); email.clear(); phone.clear(); pass.clear(); confirm.clear();
        });

        StackPane wrap = new StackPane(card);
        wrap.setStyle("-fx-background-color: " + C_BG + ";");
        wrap.setPadding(new Insets(30));
        outer.getChildren().add(wrap);

        ScrollPane sp = new ScrollPane(outer);
        sp.setFitToWidth(true); sp.setFitToHeight(true);
        sp.setStyle("-fx-background-color: " + C_BG + "; -fx-background: " + C_BG + ";");
        return sp;
    }

    private void doLogin(Account user) {
        currentUser = user;
        Notification.send("Login: " + user.getName() + " [" + (user instanceof Librarian ? "Librarian" : "Member") + "]");
        showMainScreen();
    }

    // ════════════════════════════════════════════════════════════════════════
    // MAIN SCREEN
    // ════════════════════════════════════════════════════════════════════════
    private void showMainScreen() {
        boolean isLib = currentUser instanceof Librarian;

        // ── Top bar ──
        Label nameLabel = new Label("  " + currentUser.getName());
        nameLabel.setFont(Font.font("System", FontWeight.BOLD, 14));
        nameLabel.setTextFill(Color.WHITE);

        Label roleLabel = new Label((isLib ? "  Librarian" : "  Member") + "  ");
        roleLabel.setFont(Font.font(12));
        roleLabel.setTextFill(Color.web("#AEB6BF"));
        roleLabel.setStyle("-fx-background-color: rgba(255,255,255,0.12); -fx-background-radius: 20; -fx-padding: 3 10 3 10;");

        Button btnLogout = btn("⏻  Logout", C_RED);
        btnLogout.setOnAction(e -> { saveLibrary(); currentUser = null; showAuthScreen(); });

        HBox topBar = new HBox(8, new Label("  📚"), nameLabel, roleLabel, new Region(), btnLogout);
        HBox.setHgrow(topBar.getChildren().get(3), Priority.ALWAYS);
        topBar.setAlignment(Pos.CENTER_LEFT);
        topBar.setPadding(new Insets(10, 14, 10, 10));
        topBar.setStyle("-fx-background-color: " + C_NAVY + ";");

        // ── Stats bar (librarian only) ──
        Node statsBar = isLib ? buildStatsBar() : new Region();

        // ── Tabs ──
        TabPane tabs = new TabPane();
        tabs.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
        tabs.setStyle("-fx-tab-min-width: 120px; -fx-tab-min-height: 34px; -fx-background-color: " + C_BG + ";");
        tabs.getTabs().addAll(
                styledTab("📖  Books",           buildBooksTab(isLib)),
                styledTab("🔄  Checkout/Return", buildCheckoutTab()),
                styledTab("📌  Reservations",    buildReservationsTab()),
                styledTab("💰  Fines",           buildFinesTab()),
                styledTab("🔔  Notifications",   buildNotificationsTab())
        );
        if (isLib) tabs.getTabs().add(styledTab("👥  Members", buildMembersTab()));
        else       tabs.getTabs().add(styledTab("👤  My Account", buildMyAccountTab((Member) currentUser)));

        VBox top = new VBox(topBar, statsBar);
        BorderPane root = new BorderPane();
        root.setTop(top);
        root.setCenter(tabs);
        root.setStyle("-fx-background-color: " + C_BG + ";");

        primaryStage.setScene(new Scene(root, 1100, 700));
    }

    private Tab styledTab(String text, Node content) {
        Tab t = new Tab(text, content);
        t.setClosable(false);
        return t;
    }

    private HBox buildStatsBar() {
        long available = library.getCatalog().getAllBooks().stream().filter(b -> b.status == BookStatus.AVAILABLE).count();
        long loaned    = library.getCatalog().getAllBooks().stream().filter(b -> b.status == BookStatus.LOANED).count();
        long overdue   = library.getAllLendings().stream().filter(l -> l.returnDate == null && l.isOverdue()).count();
        double fines   = library.getAllLendings().stream().mapToDouble(BookLending::calculateFine).sum();

        HBox bar = new HBox(12,
                statCard("📚 Total Books",    String.valueOf(library.getCatalog().getAllBooks().size()), C_BLUE),
                statCard("✅ Available",       String.valueOf(available), C_GREEN),
                statCard("📤 Loaned",          String.valueOf(loaned), C_ORANGE),
                statCard("⚠ Overdue",         String.valueOf(overdue), C_RED),
                statCard("👥 Members",         String.valueOf(library.getMembers().size()), C_PURPLE),
                statCard("💰 Fines Due",       String.format("$%.2f", fines), fines > 0 ? C_RED : C_GREEN)
        );
        bar.setPadding(new Insets(10, 14, 10, 14));
        bar.setStyle("-fx-background-color: " + C_DARK + ";");
        return bar;
    }

    private VBox statCard(String label, String value, String color) {
        Label valLbl = new Label(value);
        valLbl.setFont(Font.font("System", FontWeight.BOLD, 18));
        valLbl.setTextFill(Color.web(color));
        Label nameLbl = new Label(label);
        nameLbl.setFont(Font.font(11));
        nameLbl.setTextFill(Color.web("#AEB6BF"));
        VBox card = new VBox(2, valLbl, nameLbl);
        card.setAlignment(Pos.CENTER_LEFT);
        card.setPadding(new Insets(8, 16, 8, 16));
        card.setStyle("-fx-background-color: rgba(255,255,255,0.07); -fx-background-radius: 8;");
        HBox.setHgrow(card, Priority.ALWAYS);
        return card;
    }

    // ════════════════════════════════════════════════════════════════════════
    // BOOKS TAB
    // ════════════════════════════════════════════════════════════════════════
    private Pane buildBooksTab(boolean isLib) {
        TableView<BookItem> table = styledTable();
        table.getColumns().addAll(
                col("Barcode", b -> b.barcode),
                col("Title",   b -> b.title),
                col("Author",  b -> b.getAuthorsString()),
                col("Subject", b -> b.subject),
                col("ISBN",    b -> b.ISBN),
                col("Format",  b -> b.format.name()),
                statusCol(),
                col("Price",   b -> String.format("$%.2f", b.price))
        );
        refreshBooksTable(table, library.getCatalog().getAllBooks());

        TextField searchField = fxField("Search...");
        ComboBox<String> searchType = fxCombo("Title", "Author", "Subject", "ISBN");
        Button btnSearch = btn("Search", C_BLUE);
        Button btnClear  = btn("Clear",  C_MUTED);

        btnSearch.setOnAction(e -> {
            String q = searchField.getText().trim();
            if (q.isEmpty()) { refreshBooksTable(table, library.getCatalog().getAllBooks()); return; }
            List<BookItem> res;
            switch (searchType.getValue()) {
                case "Author":  res = library.getCatalog().searchByAuthor(q);  break;
                case "Subject": res = library.getCatalog().searchBySubject(q); break;
                case "ISBN":    res = library.getCatalog().searchByISBN(q);    break;
                default:        res = library.getCatalog().searchByTitle(q);   break;
            }
            refreshBooksTable(table, res);
        });
        btnClear.setOnAction(e -> { searchField.clear(); refreshBooksTable(table, library.getCatalog().getAllBooks()); });

        HBox searchBar = new HBox(8, label("Search:"), searchField, label("by"), searchType, btnSearch, btnClear);
        searchBar.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(searchField, Priority.ALWAYS);

        HBox toolbar = new HBox(12, searchBar);
        HBox.setHgrow(searchBar, Priority.ALWAYS);
        if (isLib) {
            Button btnAdd = btn("＋ Add Book", C_TEAL);
            Button btnDel = btn("✕ Remove",   C_RED);
            btnAdd.setOnAction(e -> { showAddBookDialog(); refreshBooksTable(table, library.getCatalog().getAllBooks()); });
            btnDel.setOnAction(e -> {
                BookItem sel = table.getSelectionModel().getSelectedItem();
                if (sel == null) { alert("Select a book first."); return; }
                if (sel.status == BookStatus.LOANED) { alert("Cannot remove a loaned book."); return; }
                library.removeBookItem(sel);
                Notification.send("Book removed: " + sel.title);
                refreshBooksTable(table, library.getCatalog().getAllBooks());
            });
            toolbar.getChildren().addAll(btnAdd, btnDel);
        }
        toolbar.setPadding(new Insets(10, 12, 10, 12));
        toolbar.setStyle("-fx-background-color: " + C_CARD + "; -fx-border-color: " + C_BORDER + "; -fx-border-width: 0 0 1 0;");

        BorderPane pane = new BorderPane();
        pane.setTop(toolbar);
        pane.setCenter(table);
        return pane;
    }

    /** Status column with coloured badges */
    @SuppressWarnings("unchecked")
    private TableColumn<BookItem, String> statusCol() {
        TableColumn<BookItem, String> c = new TableColumn<>("Status");
        c.setCellValueFactory(cd -> new SimpleStringProperty(cd.getValue().status.name()));
        c.setCellFactory(col -> new TableCell<BookItem, String>() {
            @Override protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) { setGraphic(null); return; }
                Label badge = new Label(item);
                badge.setFont(Font.font("System", FontWeight.BOLD, 11));
                badge.setTextFill(Color.WHITE);
                badge.setPadding(new Insets(2, 8, 2, 8));
                String bg;
                switch (item) {
                    case "AVAILABLE": bg = C_GREEN;  break;
                    case "LOANED":    bg = C_RED;    break;
                    case "RESERVED":  bg = C_ORANGE; break;
                    default:          bg = C_MUTED;  break;
                }
                badge.setStyle("-fx-background-color: " + bg + "; -fx-background-radius: 20;");
                setGraphic(badge); setText(null);
            }
        });
        return c;
    }

    private void refreshBooksTable(TableView<BookItem> table, List<BookItem> books) {
        table.setItems(FXCollections.observableArrayList(books));
    }

    private void showAddBookDialog() {
        Dialog<ButtonType> dlg = new Dialog<>();
        dlg.setTitle("Add New Book");
        dlg.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        dlg.getDialogPane().setStyle("-fx-background-color: " + C_BG + ";");

        TextField fISBN = fxField("ISBN"), fTitle = fxField("Title"), fAuthor = fxField("Author");
        TextField fSubj = fxField("Subject"), fPub = fxField("Publisher");
        TextField fPages = fxField("0"), fPrice = fxField("0.00");
        ComboBox<BookFormat> fFmt = new ComboBox<>(FXCollections.observableArrayList(BookFormat.values()));
        fFmt.setValue(BookFormat.PAPERBACK);

        GridPane g = new GridPane(); g.setHgap(10); g.setVgap(8); g.setPadding(new Insets(16));
        g.addRow(0, bold("ISBN:"),      fISBN);  g.addRow(1, bold("Title:"),     fTitle);
        g.addRow(2, bold("Author:"),    fAuthor); g.addRow(3, bold("Subject:"),   fSubj);
        g.addRow(4, bold("Publisher:"), fPub);    g.addRow(5, bold("Pages:"),     fPages);
        g.addRow(6, bold("Price ($):"), fPrice);  g.addRow(7, bold("Format:"),    fFmt);
        for (int i = 0; i < g.getChildren().size(); i += 2)
            GridPane.setHgrow(g.getChildren().get(i + 1), Priority.ALWAYS);
        dlg.getDialogPane().setContent(g);

        dlg.showAndWait().ifPresent(bt -> {
            if (bt != ButtonType.OK) return;
            try {
                String t = fTitle.getText().trim(), a = fAuthor.getText().trim();
                if (t.isEmpty() || a.isEmpty()) { alert("Title and Author are required."); return; }
                library.addBookItem(new BookItem(fISBN.getText().trim(), t, fSubj.getText().trim(),
                        fPub.getText().trim(), "English", Integer.parseInt(fPages.getText().trim()),
                        a, Double.parseDouble(fPrice.getText().trim()), fFmt.getValue(), false));
                Notification.send("Book added: " + t);
            } catch (NumberFormatException ex) { alert("Invalid number for Pages or Price."); }
        });
    }

    // ════════════════════════════════════════════════════════════════════════
    // MEMBERS TAB
    // ════════════════════════════════════════════════════════════════════════
    private Pane buildMembersTab() {
        TableView<Member> table = styledTable();
        table.getColumns().addAll(
                col("ID",        m -> m.getId()),
                col("Name",      m -> m.getName()),
                col("Email",     m -> m.getEmail()),
                col("Phone",     m -> m.getPhone()),
                memberStatusCol(),
                col("Books Out", m -> m.getTotalCheckedOutBooks() + "/" + Member.MAX_BOOKS_CHECKOUT)
        );
        refreshMembersTable(table);

        Button btnBlock   = btn("🔒 Block",   C_ORANGE);
        Button btnUnblock = btn("🔓 Unblock", C_GREEN);
        Button btnDelete  = btn("✕ Delete",   C_RED);

        btnBlock.setOnAction(e -> {
            Member m = table.getSelectionModel().getSelectedItem();
            if (m == null) { alert("Select a member first."); return; }
            if (m.totalBooksCheckedOut > 0) { alert("Member has active loans."); return; }
            m.status = AccountStatus.BLACKLISTED;
            Notification.send("Member blocked: " + m.getName()); refreshMembersTable(table);
        });
        btnUnblock.setOnAction(e -> {
            Member m = table.getSelectionModel().getSelectedItem();
            if (m == null) { alert("Select a member first."); return; }
            m.status = AccountStatus.ACTIVE;
            Notification.send("Member unblocked: " + m.getName()); refreshMembersTable(table);
        });
        btnDelete.setOnAction(e -> {
            Member m = table.getSelectionModel().getSelectedItem();
            if (m == null) { alert("Select a member first."); return; }
            if (m.totalBooksCheckedOut > 0) { alert("Member has active loans. Cannot delete."); return; }
            library.removeMember(m);
            Notification.send("Member deleted: " + m.getName()); refreshMembersTable(table);
        });

        HBox toolbar = new HBox(8, btnBlock, btnUnblock, btnDelete);
        toolbar.setPadding(new Insets(10, 12, 10, 12));
        toolbar.setStyle("-fx-background-color: " + C_CARD + "; -fx-border-color: " + C_BORDER + "; -fx-border-width: 0 0 1 0;");

        BorderPane pane = new BorderPane();
        pane.setTop(toolbar);
        pane.setCenter(table);
        return pane;
    }

    @SuppressWarnings("unchecked")
    private TableColumn<Member, String> memberStatusCol() {
        TableColumn<Member, String> c = new TableColumn<>("Status");
        c.setCellValueFactory(cd -> new SimpleStringProperty(cd.getValue().getStatus().name()));
        c.setCellFactory(col -> new TableCell<Member, String>() {
            @Override protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) { setGraphic(null); return; }
                Label badge = new Label(item);
                badge.setFont(Font.font("System", FontWeight.BOLD, 11));
                badge.setTextFill(Color.WHITE);
                badge.setPadding(new Insets(2, 8, 2, 8));
                String bg = "ACTIVE".equals(item) ? C_GREEN : C_RED;
                badge.setStyle("-fx-background-color: " + bg + "; -fx-background-radius: 20;");
                setGraphic(badge); setText(null);
            }
        });
        return c;
    }

    private void refreshMembersTable(TableView<Member> table) {
        table.setItems(FXCollections.observableArrayList(library.getMembers()));
    }

    // ════════════════════════════════════════════════════════════════════════
    // MY ACCOUNT TAB
    // ════════════════════════════════════════════════════════════════════════
    private ScrollPane buildMyAccountTab(Member member) {
        // Profile card
        VBox profile = new VBox(10);
        profile.setPadding(new Insets(20));
        profile.setStyle(cardStyle() + "-fx-max-width: 420;");

        Label avatar = new Label("👤");
        avatar.setFont(Font.font(48));
        Label nameL = new Label(member.getName());
        nameL.setFont(Font.font("System", FontWeight.BOLD, 20));
        nameL.setTextFill(Color.web(C_TEXT));

        profile.setAlignment(Pos.TOP_CENTER);
        profile.getChildren().addAll(avatar, nameL, new Separator());

        GridPane info = new GridPane(); info.setHgap(16); info.setVgap(8);
        int r = 0;
        info.addRow(r++, bold("Email:"),        label(member.getEmail()));
        info.addRow(r++, bold("Phone:"),        label(member.getPhone()));
        info.addRow(r++, bold("Member ID:"),    label(member.getId()));
        info.addRow(r++, bold("Card No:"),      label(member.libraryCard.cardNumber));
        info.addRow(r++, bold("Member Since:"), label(SDF.format(member.dateOfMembership)));
        info.addRow(r++, bold("Status:"),       label(member.getStatus().name()));
        info.addRow(r,   bold("Books Out:"),    label(member.getTotalCheckedOutBooks() + " / " + Member.MAX_BOOKS_CHECKOUT));
        profile.getChildren().add(info);

        // Active loans table
        Label loansTitle = bold("Active Loans");
        loansTitle.setFont(Font.font("System", FontWeight.BOLD, 15));
        loansTitle.setPadding(new Insets(16, 0, 6, 0));

        TableView<BookLending> loansTable = styledTable();
        loansTable.setPrefHeight(200);
        loansTable.getColumns().addAll(
                col("Title",    l -> l.book.title),
                col("Barcode",  l -> l.book.barcode),
                col("Due Date", l -> SDF.format(l.dueDate)),
                col("Overdue",  l -> l.isOverdue() ? "⚠ YES" : "No"),
                col("Fine",     l -> l.calculateFine() > 0 ? String.format("$%.2f", l.calculateFine()) : "—")
        );
        loansTable.setItems(FXCollections.observableArrayList(member.activeLoans));

        VBox right = new VBox(8, loansTitle, loansTable);
        right.setPadding(new Insets(20));
        right.setStyle(cardStyle());
        VBox.setVgrow(loansTable, Priority.ALWAYS);

        HBox layout = new HBox(16, profile, right);
        layout.setPadding(new Insets(16));
        layout.setStyle("-fx-background-color: " + C_BG + ";");
        HBox.setHgrow(right, Priority.ALWAYS);

        ScrollPane sp = new ScrollPane(layout);
        sp.setFitToWidth(true); sp.setFitToHeight(true);
        sp.setStyle("-fx-background-color: " + C_BG + "; -fx-background: " + C_BG + ";");
        return sp;
    }

    // ════════════════════════════════════════════════════════════════════════
    // CHECKOUT / RETURN TAB
    // ════════════════════════════════════════════════════════════════════════
    private Pane buildCheckoutTab() {
        boolean isLib = currentUser instanceof Librarian;

        // ── Checkout card ──
        ComboBox<String> coMember  = new ComboBox<>(); coMember.setMaxWidth(Double.MAX_VALUE);
        ComboBox<String> coBarcode = new ComboBox<>(); coBarcode.setMaxWidth(Double.MAX_VALUE);
        Button btnCo = btn("✔  Checkout Book", C_GREEN); btnCo.setMaxWidth(Double.MAX_VALUE);
        Label coStatus = new Label(); coStatus.setWrapText(true);

        if (isLib) populateMemberCombo(coMember);
        else { coMember.getItems().add(currentUser.getId() + " — " + currentUser.getName()); coMember.setDisable(true); }
        populateAvailableBooksCombo(coBarcode);

        VBox coCard = infoCard("📤  Checkout Book",
                fieldGroup("Member", coMember), fieldGroup("Book", coBarcode), btnCo, coStatus);

        btnCo.setOnAction(e -> {
            String sel = coMember.getValue(), bar = coBarcode.getValue();
            if (sel == null || bar == null) { errLabel(coStatus, "Select member and book."); return; }
            Member member = isLib ? findMemberById(sel.split(" ")[0]) : (Member) currentUser;
            BookItem book = findBookByBarcode(bar.split(" ")[0]);
            if (member == null || book == null) { errLabel(coStatus, "Not found."); return; }
            if (member.getStatus() == AccountStatus.BLACKLISTED) { errLabel(coStatus, "Member account is suspended."); return; }
            if (member.checkoutBook(book)) {
                library.recordLending(member.activeLoans.get(member.activeLoans.size() - 1));
                Notification.send(member.getName() + " checked out: " + book.title);
                okLabel(coStatus, "✔ Checked out! Due: " + SDF.format(book.dueDate));
                populateAvailableBooksCombo(coBarcode);
            } else errLabel(coStatus, "Checkout failed — unavailable or limit reached.");
        });

        // ── Return card ──
        ComboBox<String> retMember  = new ComboBox<>(); retMember.setMaxWidth(Double.MAX_VALUE);
        ComboBox<String> retBarcode = new ComboBox<>(); retBarcode.setMaxWidth(Double.MAX_VALUE);
        Button btnRet = btn("↩  Return Book", C_ORANGE); btnRet.setMaxWidth(Double.MAX_VALUE);
        Label retStatus = new Label(); retStatus.setWrapText(true);

        if (isLib) {
            populateMemberCombo(retMember);
            retMember.setOnAction(e -> {
                String s = retMember.getValue();
                if (s != null) populateLoanedBooksCombo(retBarcode, findMemberById(s.split(" ")[0]));
            });
            if (!retMember.getItems().isEmpty()) {
                retMember.setValue(retMember.getItems().get(0));
                populateLoanedBooksCombo(retBarcode, findMemberById(retMember.getValue().split(" ")[0]));
            }
        } else {
            retMember.getItems().add(currentUser.getId() + " — " + currentUser.getName());
            retMember.setDisable(true);
            populateLoanedBooksCombo(retBarcode, (Member) currentUser);
        }

        VBox retCard = infoCard("📥  Return Book",
                fieldGroup("Member", retMember), fieldGroup("Book", retBarcode), btnRet, retStatus);

        btnRet.setOnAction(e -> {
            String sel = retMember.getValue(), bar = retBarcode.getValue();
            if (sel == null || bar == null) { errLabel(retStatus, "Select member and book."); return; }
            Member member = isLib ? findMemberById(sel.split(" ")[0]) : (Member) currentUser;
            BookItem book = findBookByBarcode(bar.split(" ")[0]);
            if (member == null || book == null) { errLabel(retStatus, "Not found."); return; }
            BookLending lending = member.returnBook(book);
            if (lending != null) {
                fulfillNextReservation(book);
                double fine = lending.calculateFine();
                String msg = fine > 0
                        ? String.format("✔ Returned.  ⚠ Overdue fine: $%.2f", fine)
                        : "✔ Book returned successfully.";
                if (fine > 0) errLabel(retStatus, msg); else okLabel(retStatus, msg);
                Notification.send(member.getName() + " returned: " + book.title
                        + (fine > 0 ? " | Fine: $" + String.format("%.2f", fine) : ""));
                populateLoanedBooksCombo(retBarcode, member);
                populateAvailableBooksCombo(coBarcode);
            } else errLabel(retStatus, "Return failed.");
        });

        HBox layout = new HBox(16, coCard, retCard);
        layout.setPadding(new Insets(16));
        layout.setStyle("-fx-background-color: " + C_BG + ";");
        HBox.setHgrow(coCard,  Priority.ALWAYS);
        HBox.setHgrow(retCard, Priority.ALWAYS);
        return layout;
    }

    private void fulfillNextReservation(BookItem book) {
        BookReservation oldest = null;
        for (Member m : library.getMembers())
            for (BookReservation r : m.activeReservations)
                if (r.book == book && r.status == ReservationStatus.WAITING)
                    if (oldest == null || r.creationDate.before(oldest.creationDate)) oldest = r;
        if (oldest != null) {
            oldest.completeReservation();
            book.status = BookStatus.RESERVED;
            Notification.send("Reservation fulfilled for " + oldest.member.getName() + " — " + book.title);
        }
    }

    private void populateMemberCombo(ComboBox<String> combo) {
        combo.getItems().clear();
        for (Member m : library.getMembers()) combo.getItems().add(m.getId() + " — " + m.getName());
        if (!combo.getItems().isEmpty()) combo.setValue(combo.getItems().get(0));
    }

    private void populateAvailableBooksCombo(ComboBox<String> combo) {
        combo.getItems().clear();
        for (BookItem b : library.getCatalog().getAllBooks())
            if (b.status == BookStatus.AVAILABLE) combo.getItems().add(b.barcode + " — " + b.title);
        if (!combo.getItems().isEmpty()) combo.setValue(combo.getItems().get(0));
    }

    private void populateLoanedBooksCombo(ComboBox<String> combo, Member member) {
        combo.getItems().clear();
        if (member == null) return;
        for (BookLending l : member.activeLoans) combo.getItems().add(l.book.barcode + " — " + l.book.title);
        if (!combo.getItems().isEmpty()) combo.setValue(combo.getItems().get(0));
    }

    // ════════════════════════════════════════════════════════════════════════
    // RESERVATIONS TAB
    // ════════════════════════════════════════════════════════════════════════
    private Pane buildReservationsTab() {
        boolean isLib = currentUser instanceof Librarian;

        TableView<BookReservation> table = styledTable();
        TableColumn<BookReservation, String> colMember = new TableColumn<>("Member");
        colMember.setCellValueFactory(cd -> new SimpleStringProperty(cd.getValue().member.getName()));
        TableColumn<BookReservation, String> colBook = new TableColumn<>("Book Title");
        colBook.setCellValueFactory(cd -> new SimpleStringProperty(cd.getValue().book.title));
        TableColumn<BookReservation, String> colBookStatus = new TableColumn<>("Book Status");
        colBookStatus.setCellValueFactory(cd -> new SimpleStringProperty(cd.getValue().book.status.name()));
        TableColumn<BookReservation, String> colDate = new TableColumn<>("Reserved On");
        colDate.setCellValueFactory(cd -> new SimpleStringProperty(SDF.format(cd.getValue().creationDate)));
        TableColumn<BookReservation, String> colResStatus = new TableColumn<>("Reservation");
        colResStatus.setCellValueFactory(cd -> new SimpleStringProperty(cd.getValue().status.name()));
        table.getColumns().addAll(colMember, colBook, colBookStatus, colDate, colResStatus);
        refreshReservationsTable(table, isLib);

        Button btnReserve = btn("＋ Reserve Book",      C_BLUE);
        Button btnCancel  = btn("✕ Cancel Reservation", C_RED);
        Button btnRefresh = btn("↻ Refresh",            C_MUTED);

        btnReserve.setOnAction(e -> { showReserveDialog(isLib); refreshReservationsTable(table, isLib); });
        btnCancel.setOnAction(e -> {
            BookReservation sel = table.getSelectionModel().getSelectedItem();
            if (sel == null) { alert("Select a reservation first."); return; }
            if (!isLib && sel.member != currentUser) { alert("You can only cancel your own reservations."); return; }
            if (sel.status == ReservationStatus.COMPLETED) { alert("This reservation is already completed."); return; }
            sel.cancelReservation();
            boolean otherWaiting = library.getMembers().stream()
                    .flatMap(m -> m.activeReservations.stream())
                    .anyMatch(r -> r.book == sel.book && r.status == ReservationStatus.WAITING);
            if (!otherWaiting && sel.book.status == BookStatus.RESERVED)
                sel.book.status = BookStatus.LOANED;
            Notification.send("Reservation cancelled: " + sel.book.title);
            refreshReservationsTable(table, isLib);
        });
        btnRefresh.setOnAction(e -> refreshReservationsTable(table, isLib));

        HBox toolbar = new HBox(8, btnReserve, btnCancel, btnRefresh);
        toolbar.setPadding(new Insets(10, 12, 10, 12));
        toolbar.setStyle("-fx-background-color: " + C_CARD + "; -fx-border-color: " + C_BORDER + "; -fx-border-width: 0 0 1 0;");

        BorderPane pane = new BorderPane();
        pane.setTop(toolbar);
        pane.setCenter(table);
        return pane;
    }

    private void refreshReservationsTable(TableView<BookReservation> table, boolean isLib) {
        ObservableList<BookReservation> items = FXCollections.observableArrayList();
        for (Member m : library.getMembers()) {
            if (!isLib && m != currentUser) continue;
            for (BookReservation r : m.activeReservations)
                if (r.status == ReservationStatus.WAITING || r.status == ReservationStatus.COMPLETED)
                    items.add(r);
        }
        table.setItems(items);
    }

    private void showReserveDialog(boolean isLib) {
        List<BookItem> loanedBooks = new ArrayList<>();
        for (BookItem b : library.getCatalog().getAllBooks())
            if (b.status == BookStatus.LOANED) loanedBooks.add(b);
        if (loanedBooks.isEmpty()) { alert("No loaned books available to reserve."); return; }

        ComboBox<String> memberBox = new ComboBox<>(); memberBox.setMaxWidth(Double.MAX_VALUE);
        ComboBox<String> bookBox   = new ComboBox<>(); bookBox.setMaxWidth(Double.MAX_VALUE);

        if (isLib) populateMemberCombo(memberBox);
        else { memberBox.getItems().add(currentUser.getId() + " — " + currentUser.getName()); memberBox.setDisable(true); }
        for (BookItem b : loanedBooks) bookBox.getItems().add(b.barcode + " — " + b.title);
        bookBox.setValue(bookBox.getItems().get(0));

        Dialog<ButtonType> dlg = new Dialog<>();
        dlg.setTitle("Reserve a Book");
        dlg.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        dlg.getDialogPane().setStyle("-fx-background-color: " + C_BG + ";");
        GridPane g = new GridPane(); g.setHgap(10); g.setVgap(10); g.setPadding(new Insets(16));
        g.addRow(0, bold("Member:"), memberBox);
        g.addRow(1, bold("Book:"),   bookBox);
        GridPane.setHgrow(memberBox, Priority.ALWAYS);
        GridPane.setHgrow(bookBox,   Priority.ALWAYS);
        dlg.getDialogPane().setContent(g);

        dlg.showAndWait().ifPresent(bt -> {
            if (bt != ButtonType.OK) return;
            String sel = memberBox.getValue(), bar = bookBox.getValue();
            if (sel == null || bar == null) return;
            Member member = isLib ? findMemberById(sel.split(" ")[0]) : (Member) currentUser;
            BookItem book = findBookByBarcode(bar.split(" ")[0]);
            if (member == null || book == null) return;
            boolean dup = member.activeReservations.stream()
                    .anyMatch(r -> r.book == book && r.status == ReservationStatus.WAITING);
            if (dup) { alert("You already have a reservation for this book."); return; }
            if (member.reserveBook(book)) {
                Notification.send(member.getName() + " reserved: " + book.title);
                alert("✔ Reservation placed for: " + book.title);
            } else alert("Reservation failed.");
        });
    }

    // ════════════════════════════════════════════════════════════════════════
    // FINES TAB
    // ════════════════════════════════════════════════════════════════════════
    private Pane buildFinesTab() {
        boolean isLib = currentUser instanceof Librarian;

        TableView<BookLending> table = styledTable();
        table.getColumns().addAll(
                col("Member",   l -> l.member.getName()),
                col("Book",     l -> l.book.title),
                col("Due Date", l -> SDF.format(l.dueDate)),
                col("Returned", l -> l.returnDate != null ? SDF.format(l.returnDate) : "Not returned"),
                col("Days Late",l -> {
                    if (!l.isOverdue() && (l.returnDate == null || !l.returnDate.after(l.dueDate))) return "0";
                    long ms = (l.returnDate != null ? l.returnDate : new Date()).getTime() - l.dueDate.getTime();
                    return String.valueOf(ms / (24 * 60 * 60 * 1000));
                }),
                fineAmountCol()
        );
        refreshFinesTable(table, isLib);

        Label totalLabel = new Label();
        totalLabel.setFont(Font.font("System", FontWeight.BOLD, 14));
        updateFineTotal(totalLabel, isLib);

        Button btnRefresh = btn("↻ Refresh", C_BLUE);
        btnRefresh.setOnAction(e -> { refreshFinesTable(table, isLib); updateFineTotal(totalLabel, isLib); });

        HBox toolbar = new HBox(10, btnRefresh, new Region(), totalLabel);
        HBox.setHgrow(toolbar.getChildren().get(1), Priority.ALWAYS);
        toolbar.setPadding(new Insets(10, 12, 10, 12));
        toolbar.setAlignment(Pos.CENTER_LEFT);
        toolbar.setStyle("-fx-background-color: " + C_CARD + "; -fx-border-color: " + C_BORDER + "; -fx-border-width: 0 0 1 0;");

        BorderPane pane = new BorderPane();
        pane.setTop(toolbar);
        pane.setCenter(table);
        return pane;
    }

    @SuppressWarnings("unchecked")
    private TableColumn<BookLending, String> fineAmountCol() {
        TableColumn<BookLending, String> c = new TableColumn<>("Fine");
        c.setCellValueFactory(cd -> {
            double f = cd.getValue().calculateFine();
            return new SimpleStringProperty(f > 0 ? String.format("$%.2f", f) : "—");
        });
        c.setCellFactory(col -> new TableCell<BookLending, String>() {
            @Override protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) { setText(null); setStyle(""); return; }
                setText(item);
                if (!"—".equals(item)) {
                    setFont(Font.font("System", FontWeight.BOLD, 13));
                    setTextFill(Color.web(C_RED));
                } else {
                    setFont(Font.font(13));
                    setTextFill(Color.web(C_MUTED));
                }
            }
        });
        return c;
    }

    private void refreshFinesTable(TableView<BookLending> table, boolean isLib) {
        List<BookLending> rows = new ArrayList<>();
        for (BookLending l : library.getAllLendings()) {
            if (!isLib && l.member != currentUser) continue;
            if (l.isOverdue() || (l.returnDate != null && l.returnDate.after(l.dueDate))) rows.add(l);
        }
        table.setItems(FXCollections.observableArrayList(rows));
    }

    private void updateFineTotal(Label label, boolean isLib) {
        double total = 0;
        for (BookLending l : library.getAllLendings()) {
            if (!isLib && l.member != currentUser) continue;
            total += l.calculateFine();
        }
        label.setText(String.format("Outstanding fines: $%.2f", total));
        label.setTextFill(total > 0 ? Color.web(C_RED) : Color.web(C_GREEN));
    }

    // ════════════════════════════════════════════════════════════════════════
    // NOTIFICATIONS TAB
    // ════════════════════════════════════════════════════════════════════════
    private Pane buildNotificationsTab() {
        TextArea area = new TextArea();
        area.setEditable(false);
        area.setStyle("-fx-font-family: monospace; -fx-font-size: 12; -fx-background-color: " + C_CARD + ";");
        refreshNotifications(area);

        Button btnRefresh = btn("↻ Refresh", C_BLUE);
        Button btnClear   = btn("✕ Clear",   C_RED);
        btnRefresh.setOnAction(e -> refreshNotifications(area));
        btnClear.setOnAction(e -> { Notification.log.clear(); area.clear(); });

        HBox toolbar = new HBox(8, btnRefresh, btnClear);
        toolbar.setPadding(new Insets(10, 12, 10, 12));
        toolbar.setStyle("-fx-background-color: " + C_CARD + "; -fx-border-color: " + C_BORDER + "; -fx-border-width: 0 0 1 0;");

        VBox pane = new VBox(0, toolbar, area);
        VBox.setVgrow(area, Priority.ALWAYS);
        return pane;
    }

    private void refreshNotifications(TextArea area) {
        StringBuilder sb = new StringBuilder();
        List<String> log = Notification.getLog();
        for (int i = log.size() - 1; i >= 0; i--) sb.append(log.get(i)).append("\n");
        area.setText(sb.toString());
    }

    // ════════════════════════════════════════════════════════════════════════
    // DATA HELPERS
    // ════════════════════════════════════════════════════════════════════════
    private BookItem findBookByBarcode(String barcode) {
        return library.getCatalog().getAllBooks().stream()
                .filter(b -> b.barcode.equals(barcode)).findFirst().orElse(null);
    }

    private Member findMemberById(String id) {
        return library.getMembers().stream()
                .filter(m -> m.getId().equals(id)).findFirst().orElse(null);
    }

    // ════════════════════════════════════════════════════════════════════════
    // UI FACTORY HELPERS
    // ════════════════════════════════════════════════════════════════════════
    private <T> TableColumn<T, String> col(String title, Function<T, String> fn) {
        TableColumn<T, String> c = new TableColumn<>(title);
        c.setCellValueFactory(cd -> new SimpleStringProperty(fn.apply(cd.getValue())));
        return c;
    }

    @SuppressWarnings("unchecked")
    private <T> TableView<T> styledTable() {
        TableView<T> t = new TableView<>();
        t.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        t.setStyle("-fx-font-size: 13px;");
        t.setPlaceholder(new Label("No data to display."));
        return t;
    }

    private TextField fxField(String prompt) {
        TextField f = new TextField();
        f.setPromptText(prompt);
        f.setStyle("-fx-background-radius: 6; -fx-border-radius: 6; -fx-border-color: " + C_BORDER
                + "; -fx-padding: 6 10 6 10; -fx-font-size: 13;");
        return f;
    }

    private PasswordField fxPass(String prompt) {
        PasswordField f = new PasswordField();
        f.setPromptText(prompt);
        f.setStyle("-fx-background-radius: 6; -fx-border-radius: 6; -fx-border-color: " + C_BORDER
                + "; -fx-padding: 6 10 6 10; -fx-font-size: 13;");
        return f;
    }

    @SafeVarargs
    private <T> ComboBox<T> fxCombo(T... items) {
        ComboBox<T> c = new ComboBox<>(FXCollections.observableArrayList(items));
        c.setValue(items[0]);
        c.setMaxWidth(Double.MAX_VALUE);
        c.setStyle("-fx-background-radius: 6; -fx-border-radius: 6; -fx-font-size: 13;");
        return c;
    }

    private Button btn(String text, String color) {
        Button b = new Button(text);
        b.setStyle("-fx-background-color: " + color + "; -fx-text-fill: white; "
                + "-fx-background-radius: 6; -fx-padding: 7 16 7 16; -fx-font-size: 13; -fx-cursor: hand;");
        return b;
    }

    private Label bold(String text) {
        Label l = new Label(text);
        l.setFont(Font.font("System", FontWeight.BOLD, 13));
        l.setTextFill(Color.web(C_TEXT));
        return l;
    }

    private Label label(String text) {
        Label l = new Label(text);
        l.setFont(Font.font(13));
        l.setTextFill(Color.web(C_TEXT));
        return l;
    }

    private String cardStyle() {
        return "-fx-background-color: " + C_CARD + "; -fx-background-radius: 10; "
                + "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.10), 8, 0, 0, 2);";
    }

    /** Label + control stacked vertically */
    private VBox fieldGroup(String labelText, Node control) {
        Label lbl = new Label(labelText);
        lbl.setFont(Font.font("System", FontWeight.BOLD, 12));
        lbl.setTextFill(Color.web(C_MUTED));
        VBox g = new VBox(4, lbl, control);
        VBox.setVgrow(control, Priority.NEVER);
        return g;
    }

    /** Titled card with drop shadow for checkout/return panels */
    private VBox infoCard(String title, Node... nodes) {
        Label lbl = new Label(title);
        lbl.setFont(Font.font("System", FontWeight.BOLD, 15));
        lbl.setTextFill(Color.web(C_TEXT));
        VBox box = new VBox(14, lbl, new Separator());
        box.getChildren().addAll(nodes);
        box.setPadding(new Insets(20));
        box.setStyle(cardStyle());
        return box;
    }

    private void errLabel(Label l, String msg) {
        l.setText(msg); l.setTextFill(Color.web(C_RED));
    }

    private void okLabel(Label l, String msg) {
        l.setText(msg); l.setTextFill(Color.web(C_GREEN));
    }

    private void alert(String msg) {
        Alert a = new Alert(Alert.AlertType.INFORMATION, msg, ButtonType.OK);
        a.setHeaderText(null);
        a.getDialogPane().setStyle("-fx-background-color: " + C_BG + ";");
        a.showAndWait();
    }

    public static void main(String[] args) { launch(args); }
}


