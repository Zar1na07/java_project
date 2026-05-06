

package org.example;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.TextAlignment;
import javafx.stage.Stage;

import java.io.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.function.Function;

/*
 * GROUP GUI RESPONSIBILITIES
 *
 * PERSON 1: DURDONA
 * Backend: Book, Catalog, Address, BookFormat, BookStatus
 * GUI: Books/Catalog page.
 *
 * PERSON 2: BONU
 * Backend: BookItem, Library, Rack
 * GUI: Library data, save/load, seed data, Dashboard.
 *
 * PERSON 3: MARJONA
 * Backend: Notification, EmailNotification, PostalNotification, Librarian, Member
 * GUI: Members page, Notifications page, shared UI helper methods.
 *
 * PERSON 4: FERUZA
 * Backend: BarcodeReader, LibraryCard, Account, AccountStatus
 * GUI: Login, Register, My Account page.
 *
 * PERSON 5: ZARINA
 * Backend: BookReservation, BookLending, Person, ReservationStatus
 * GUI: Checkout, Return, Renew, Reservations.
 *
 * PERSON 6: RAMIZA
 * Backend: Fine, FineTransaction, CreditTransaction, CheckTransaction, Cash/CardTransaction
 * GUI: Navigation layout, sidebar, Fines page.
 */


/**
 * Main JavaFX application for the Library Management System.
 *
 * This file connects the backend classes with GUI pages.
 * It is divided into clear sections by group member names.
 */
public class LibraryApp extends Application {

    // File used for saving/loading serialized library data.
    private static final String SAVE_FILE = "library_data.ser";
    private static final String READABLE_FILE = "library_data.txt";
    private static final SimpleDateFormat SDF = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private static final SimpleDateFormat DATE_ONLY = new SimpleDateFormat("yyyy-MM-dd");

    // Color palette used for the minimalist JavaFX design.
    private static final String C_NAVY = "#0F172A";
    private static final String C_SIDEBAR = "#1E293B";
    private static final String C_SIDEBAR_HOVER = "#334155";
    private static final String C_BLUE = "#2563EB";
    private static final String C_TEAL = "#0D9488";
    private static final String C_GREEN = "#16A34A";
    private static final String C_RED = "#DC2626";
    private static final String C_ORANGE = "#EA580C";
    private static final String C_PURPLE = "#7C3AED";
    private static final String C_BG = "#F4F7FB";
    private static final String C_CARD = "#FFFFFF";
    private static final String C_BORDER = "#D8DEE9";
    private static final String C_MUTED = "#64748B";
    private static final String C_TEXT = "#0F172A";






    // =========================================================================
    // PERSON 2: BONU
    // Classes: BookItem, Library, Rack
    // GUI: shared Library object, sample data, save/load functions.
    // =========================================================================
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
            library.addMember(new Member("Bob Jones", "bob@example.com", "555-2222", "pass2"));
        }
        Notification.send("Library system started.");
    }

    private Account currentUser = null;
    private Stage primaryStage;
    private StackPane contentArea;
    private final List<Button> sideButtons = new ArrayList<>();

    @Override
    public void start(Stage stage) {
        this.primaryStage = stage;
        stage.setTitle("City Library Management System");
        stage.setMinWidth(1000);
        stage.setMinHeight(650);
        stage.setOnCloseRequest(e -> { saveLibrary(); Platform.exit(); });
        showAuthScreen();
        stage.show();
    }

    /** Loads saved library data from the binary save file. */
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


    /** Saves the project data and exports a readable text snapshot. */
    private static void saveLibrary() {
        try (ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(SAVE_FILE))) {
            out.writeObject(library);
        } catch (Exception e) {
            System.out.println("Save failed: " + e.getMessage());
            return;
        }

        try (PrintWriter pw = new PrintWriter(new FileWriter(READABLE_FILE))) {
            pw.println("=".repeat(70));
            pw.println("  CITY LIBRARY — SNAPSHOT   " + SDF.format(new Date()));
            pw.println("=".repeat(70));
            pw.println("\n--- LIBRARIANS ---");
            for (Librarian l : library.getLibrarians()) pw.printf("  %-10s  %-22s  %s%n", l.getId(), l.getName(), l.getEmail());
            pw.println("\n--- MEMBERS ---");
            for (Member m : library.getMembers()) pw.printf("  %-10s  %-22s  %-28s  %-12s  Books: %d/%d%n", m.getId(), m.getName(), m.getEmail(), m.status.name(), m.totalBooksCheckedOut, Member.MAX_BOOKS_CHECKOUT);
            pw.println("\n--- BOOKS ---");
            for (BookItem b : library.getCatalog().getAllBooks()) pw.printf("  %-14s  %-10s  %-38s  %s%n", b.barcode, b.status.name(), b.title, b.getAuthorsString());
            pw.println("\n--- ACTIVE LENDINGS ---");
            for (BookLending l : library.getAllLendings()) {
                if (l.returnDate != null) continue;
                double fine = l.calculateFine();
                pw.printf("  %-22s  %-38s  Due: %s%s%n", l.member.getName(), l.book.title, SDF.format(l.dueDate), fine > 0 ? String.format("  FINE: $%.2f", fine) : "");
            }
            pw.println("\n--- RESERVATIONS ---");
            for (Member m : library.getMembers()) for (BookReservation r : m.activeReservations) pw.printf("  %-22s  %-38s  %s%n", m.getName(), r.book.title, r.status.name());
            pw.println("\n" + "=".repeat(70));
        } catch (Exception e) {
            System.out.println("Readable export failed: " + e.getMessage());
        }
    }
    // ============================  BONU  CONTINUES LATER ============================


    // =========================================================================
    // PERSON 4: FERUZA
    // Classes: BarcodeReader, LibraryCard, Account, AccountStatus
    // GUI: Login/Register screen and authentication logic.
    // =========================================================================

    /** Builds the authentication screen with Login and Register tabs. */
    private void showAuthScreen() {
        StackPane brand = new StackPane();
        brand.setPrefWidth(320);
        brand.setMinWidth(280);

        Rectangle gradient = new Rectangle();
        gradient.widthProperty().bind(brand.widthProperty());
        gradient.heightProperty().bind(brand.heightProperty());
        gradient.setFill(new LinearGradient(0, 0, 0, 1, true, CycleMethod.NO_CYCLE,
                new Stop(0, Color.web("#0F172A")), new Stop(1, Color.web("#1E293B"))));

        VBox brandContent = new VBox(16);
        brandContent.setAlignment(Pos.CENTER);
        brandContent.setPadding(new Insets(40, 32, 40, 32));
        Label icon = new Label("📚");
        icon.setFont(Font.font(68));
        Label title = new Label("City Library");
        title.setFont(Font.font("System", FontWeight.BOLD, 31));
        title.setTextFill(Color.WHITE);
        Label sub = new Label("Management System");
        sub.setFont(Font.font("System", FontWeight.NORMAL, 14));
        sub.setTextFill(Color.web("#CBD5E1"));
        Separator sep = new Separator();
        sep.setMaxWidth(170);
        sep.setStyle("-fx-background-color: #475569;");
        Label quote = new Label("\"A reader lives a thousand lives\nbefore he dies.\"");
        quote.setFont(Font.font("System", FontWeight.NORMAL, 12));
        quote.setTextFill(Color.web("#94A3B8"));
        quote.setTextAlignment(TextAlignment.CENTER);
        quote.setWrapText(true);
        HBox stats = new HBox(10, statPill("📖 " + library.getCatalog().getAllBooks().size() + " Books"), statPill("👥 " + library.getMembers().size() + " Members"));
        stats.setAlignment(Pos.CENTER);
        brandContent.getChildren().addAll(icon, title, sub, sep, quote, stats);
        brand.getChildren().addAll(gradient, brandContent);

        TabPane authTabs = new TabPane();
        authTabs.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
        authTabs.setStyle("-fx-background-color: " + C_BG + ";-fx-tab-min-width: 135px;-fx-tab-min-height: 42px;-fx-focus-color: transparent;-fx-faint-focus-color: transparent;");
        authTabs.getTabs().addAll(new Tab("Login", buildLoginForm()), new Tab("Register", buildRegisterForm()));

        HBox root = new HBox(brand, authTabs);
        HBox.setHgrow(authTabs, Priority.ALWAYS);
        primaryStage.setScene(new Scene(root, 930, 590));
    }

    private Label statPill(String text) {
        Label label = new Label(text);
        label.setFont(Font.font("System", FontWeight.BOLD, 11));
        label.setTextFill(Color.web("#CBD5E1"));
        label.setStyle("-fx-background-color: rgba(255,255,255,0.10); -fx-background-radius: 20; -fx-padding: 6 12;");
        return label;
    }

    /** Creates login form and checks credentials for Member or Librarian. */
    private ScrollPane buildLoginForm() {
        VBox outer = new VBox();
        outer.setAlignment(Pos.CENTER);
        outer.setStyle("-fx-background-color: " + C_BG + ";");
        VBox card = new VBox(16);
        card.setPadding(new Insets(38, 46, 38, 46));
        card.setMaxWidth(430);
        card.setStyle(cardStyle());
        Label heading = pageTitle("Welcome Back");
        Label sub = smallText("Sign in to continue");
        ComboBox<String> roleBox = fxCombo("Member", "Librarian");
        TextField email = fxField("Email address");
        PasswordField password = fxPass("Password");
        Button login = btn("Login", C_BLUE);
        login.setMaxWidth(Double.MAX_VALUE);
        Label status = new Label();
        status.setWrapText(true);
        Label demo = smallText("Demo: admin@library.com / admin\nalice@example.com / pass1");
        demo.setTextAlignment(TextAlignment.CENTER);
        card.getChildren().addAll(heading, sub, new Separator(), fieldGroup("Role", roleBox), fieldGroup("Email", email), fieldGroup("Password", password), login, status, demo);

        login.setOnAction(e -> {
            String role = roleBox.getValue();
            String em = email.getText().trim();
            String pw = password.getText();
            if (em.isEmpty() || pw.isEmpty()) { errLabel(status, "Please fill in all fields."); return; }
            if ("Librarian".equals(role)) {
                Account found = library.getLibrarians().stream().filter(l -> l.getEmail().equalsIgnoreCase(em) && l.password.equals(pw)).findFirst().orElse(null);
                if (found != null) doLogin(found); else errLabel(status, "Invalid librarian credentials.");
            } else {
                Account found = library.getMembers().stream().filter(m -> m.getEmail().equalsIgnoreCase(em) && m.password.equals(pw)).findFirst().orElse(null);
                if (found != null) { if (found.status == AccountStatus.BLACKLISTED) errLabel(status, "Account suspended."); else doLogin(found); }
                else errLabel(status, "Invalid member credentials.");
            }
        });

        StackPane wrap = new StackPane(card);
        wrap.setPadding(new Insets(30));
        outer.getChildren().add(wrap);
        ScrollPane scroll = new ScrollPane(outer);
        scroll.setFitToWidth(true);
        scroll.setFitToHeight(true);
        scroll.setStyle("-fx-background-color: " + C_BG + "; -fx-background: " + C_BG + ";");
        return scroll;
    }

    /** Creates registration form for new Member or Librarian accounts. */
    private ScrollPane buildRegisterForm() {
        VBox outer = new VBox();
        outer.setAlignment(Pos.CENTER);
        outer.setStyle("-fx-background-color: " + C_BG + ";");
        VBox card = new VBox(14);
        card.setPadding(new Insets(34, 46, 34, 46));
        card.setMaxWidth(430);
        card.setStyle(cardStyle());
        Label heading = pageTitle("Create Account");
        Label sub = smallText("Register as a member or librarian");
        ComboBox<String> roleBox = fxCombo("Member", "Librarian");
        TextField name = fxField("Full name");
        TextField email = fxField("Email address");
        TextField phone = fxField("Phone number");
        PasswordField password = fxPass("Password");
        PasswordField confirm = fxPass("Confirm password");
        Button create = btn("Create Account", C_TEAL);
        create.setMaxWidth(Double.MAX_VALUE);
        Label status = new Label();
        status.setWrapText(true);
        card.getChildren().addAll(heading, sub, new Separator(), fieldGroup("Role", roleBox), fieldGroup("Full Name", name), fieldGroup("Email", email), fieldGroup("Phone", phone), fieldGroup("Password", password), fieldGroup("Confirm Password", confirm), create, status);

        create.setOnAction(e -> {
            String role = roleBox.getValue(), nm = name.getText().trim(), em = email.getText().trim(), ph = phone.getText().trim(), pw = password.getText(), cf = confirm.getText();
            if (nm.isEmpty() || em.isEmpty() || pw.isEmpty()) { errLabel(status, "Name, email and password are required."); return; }
            if (!pw.equals(cf)) { errLabel(status, "Passwords do not match."); return; }
            boolean taken = library.getMembers().stream().anyMatch(m -> m.getEmail().equalsIgnoreCase(em)) || library.getLibrarians().stream().anyMatch(l -> l.getEmail().equalsIgnoreCase(em));
            if (taken) { errLabel(status, "An account with that email already exists."); return; }
            if ("Librarian".equals(role)) library.addLibrarian(new Librarian(nm, em, ph, pw)); else library.addMember(new Member(nm, em, ph, pw));
            saveLibrary();
            Notification.send("New " + role + " registered: " + nm);
            okLabel(status, "Account created. You can now log in.");
            name.clear(); email.clear(); phone.clear(); password.clear(); confirm.clear();
        });

        StackPane wrap = new StackPane(card);
        wrap.setPadding(new Insets(30));
        outer.getChildren().add(wrap);
        ScrollPane scroll = new ScrollPane(outer);
        scroll.setFitToWidth(true);
        scroll.setFitToHeight(true);
        scroll.setStyle("-fx-background-color: " + C_BG + "; -fx-background: " + C_BG + ";");
        return scroll;
    }

    /** Saves the current user and opens the main application screen. */
    private void doLogin(Account user) {
        currentUser = user;
        Notification.send("Login: " + user.getName() + " [" + (user instanceof Librarian ? "Librarian" : "Member") + "]");
        showMainScreen();
    }
    // ========================= FERUZA CONTINUES LATER =========================



    // =========================================================================
    // PERSON 6: RAMIZA
    // Classes: Fine, FineTransaction, CreditTransaction, CheckTransaction, Cash/CardTransaction
    // GUI: Main navigation, top bar, sidebar, active page switching.
    // =========================================================================
    /** Creates the main layout: top bar, sidebar, and content area. */
    private void showMainScreen() {
        boolean isLib = currentUser instanceof Librarian;
        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: " + C_BG + ";");
        HBox topBar = buildTopBar(isLib);
        VBox sidebar = buildSidebar(isLib);
        contentArea = new StackPane();
        contentArea.setStyle("-fx-background-color: " + C_BG + ";");
        root.setTop(topBar);
        root.setLeft(sidebar);
        root.setCenter(contentArea);
        primaryStage.setScene(new Scene(root, 1180, 740));
        setPage(buildDashboardPage(isLib));
        setActiveButton(sideButtons.get(0));
    }



    /** Builds the top bar with title, user role and logout button. */
    private HBox buildTopBar(boolean isLib) {
        Label title = new Label("📚 Library Management System");
        title.setFont(Font.font("System", FontWeight.BOLD, 20));
        title.setTextFill(Color.WHITE);
        Label user = new Label((isLib ? "Librarian: " : "Member: ") + currentUser.getName());
        user.setFont(Font.font("System", FontWeight.BOLD, 13));
        user.setTextFill(Color.web("#CBD5E1"));
        Button logout = btn("Logout", C_RED);
        logout.setMinWidth(100);
        logout.setOnAction(e -> { saveLibrary(); currentUser = null; showAuthScreen(); });
        HBox bar = new HBox(16, title, new Region(), user, logout);
        HBox.setHgrow(bar.getChildren().get(1), Priority.ALWAYS);
        bar.setAlignment(Pos.CENTER_LEFT);
        bar.setPadding(new Insets(14, 18, 14, 18));
        bar.setStyle("-fx-background-color: " + C_NAVY + ";");
        return bar;
    }

    /** Builds the sidebar navigation menu. */
    private VBox buildSidebar(boolean isLib) {
        sideButtons.clear();
        VBox sidebar = new VBox(8);
        sidebar.setPrefWidth(230);
        sidebar.setPadding(new Insets(22, 14, 14, 14));
        sidebar.setStyle("-fx-background-color: " + C_SIDEBAR + ";");
        Label menu = new Label("MENU");
        menu.setFont(Font.font("System", FontWeight.BOLD, 11));
        menu.setTextFill(Color.web("#94A3B8"));
        menu.setPadding(new Insets(0, 0, 8, 8));
        Button dashboard = sideBtn("Dashboard");
        Button books = sideBtn("Books");
        Button checkout = sideBtn("Checkout / Return");
        Button renew = sideBtn("Renew");
        Button reservations = sideBtn("Reservations");
        Button fines = sideBtn("Fines");
        Button notifications = sideBtn("Notifications");
        Button account = sideBtn(isLib ? "Members" : "My Account");
        dashboard.setOnAction(e -> { setPage(buildDashboardPage(isLib)); setActiveButton(dashboard); });
        books.setOnAction(e -> { setPage(buildBooksPage(isLib)); setActiveButton(books); });
        checkout.setOnAction(e -> { setPage(buildCheckoutPage()); setActiveButton(checkout); });
        renew.setOnAction(e -> { setPage(buildRenewPage()); setActiveButton(renew); });
        reservations.setOnAction(e -> { setPage(buildReservationsPage()); setActiveButton(reservations); });
        fines.setOnAction(e -> { setPage(buildFinesPage()); setActiveButton(fines); });
        notifications.setOnAction(e -> { setPage(buildNotificationsPage()); setActiveButton(notifications); });
        account.setOnAction(e -> { setPage(isLib ? buildMembersPage() : buildMyAccountPage((Member) currentUser)); setActiveButton(account); });
        sideButtons.addAll(Arrays.asList(dashboard, books, checkout, renew, reservations, fines, notifications, account));
        sidebar.getChildren().add(menu);
        sidebar.getChildren().addAll(sideButtons);
        Region space = new Region();
        sidebar.getChildren().add(space);
        VBox.setVgrow(space, Priority.ALWAYS);
        Label version = new Label("City Library v1.0");
        version.setTextFill(Color.web("#64748B"));
        version.setFont(Font.font(11));
        version.setPadding(new Insets(8, 0, 0, 8));
        sidebar.getChildren().add(version);
        return sidebar;
    }

    /** Changes the page displayed in the center content area. */
    private void setPage(Node page) {
        contentArea.getChildren().clear();
        contentArea.getChildren().add(page);
    }

    /** Highlights the currently selected sidebar button. */
    private void setActiveButton(Button active) {
        for (Button b : sideButtons) { b.setStyle(sideButtonStyle(false)); b.setTextFill(Color.web("#CBD5E1")); }
        active.setStyle(sideButtonStyle(true));
        active.setTextFill(Color.WHITE);
    }

    /** Creates one styled sidebar button. */
    private Button sideBtn(String text) {
        Button b = new Button(text);
        b.setMaxWidth(Double.MAX_VALUE);
        b.setAlignment(Pos.CENTER_LEFT);
        b.setFont(Font.font("System", FontWeight.BOLD, 14));
        b.setTextFill(Color.web("#CBD5E1"));
        b.setCursor(Cursor.HAND);
        b.setStyle(sideButtonStyle(false));
        b.setOnMouseEntered(e -> { if (!b.getTextFill().equals(Color.WHITE)) b.setStyle(sideButtonHoverStyle()); });
        b.setOnMouseExited(e -> { if (!b.getTextFill().equals(Color.WHITE)) b.setStyle(sideButtonStyle(false)); });
        return b;
    }

    private String sideButtonStyle(boolean selected) {
        if (selected) return "-fx-background-color: " + C_BLUE + ";-fx-background-radius: 10;-fx-padding: 13 16;-fx-effect: dropshadow(gaussian, rgba(37,99,235,0.35), 10, 0, 0, 3);";
        return "-fx-background-color: transparent;-fx-background-radius: 10;-fx-padding: 13 16;";
    }

    private String sideButtonHoverStyle() { return "-fx-background-color: " + C_SIDEBAR_HOVER + ";-fx-background-radius: 10;-fx-padding: 13 16;"; }

    // ========================= RAMIZA CONTINUES LATER =========================






    // =========================================================================
    // PERSON 2: BONU CONTINUED
    // GUI: Dashboard statistics cards.
    // =========================================================================
    /** Builds the dashboard page with library statistics. */
    private ScrollPane buildDashboardPage(boolean isLib) {
        VBox page = pageShell("Dashboard", "Overview of library activity and system status");
        long available = library.getCatalog().getAllBooks().stream().filter(b -> b.status == BookStatus.AVAILABLE).count();
        long loaned = library.getCatalog().getAllBooks().stream().filter(b -> b.status == BookStatus.LOANED).count();
        long overdue = library.getAllLendings().stream().filter(l -> l.returnDate == null && l.isOverdue()).count();
        double fines = visibleLendings(isLib).stream().mapToDouble(BookLending::calculateFine).sum();
        HBox cards1 = new HBox(16, dashboardCard("Total Books", String.valueOf(library.getCatalog().getAllBooks().size()), C_BLUE), dashboardCard("Available", String.valueOf(available), C_GREEN), dashboardCard("Loaned", String.valueOf(loaned), C_ORANGE), dashboardCard("Members", String.valueOf(library.getMembers().size()), C_PURPLE));
        HBox cards2 = new HBox(16, dashboardCard("Overdue", String.valueOf(overdue), overdue > 0 ? C_RED : C_GREEN), dashboardCard("Fine Total", String.format("$%.2f", fines), fines > 0 ? C_RED : C_GREEN), dashboardCard("Current User", currentUser.getName(), C_TEAL), dashboardCard("Role", isLib ? "Librarian" : "Member", C_BLUE));
        VBox quick = cardBox();
        quick.getChildren().addAll(sectionTitle("Quick Notes"), smallText("Use the sidebar to manage books, loans, reservations, fines and accounts."), smallText("Data is saved into library_data.ser and exported as readable library_data.txt on exit."));
        page.getChildren().addAll(cards1, cards2, quick);
        return wrapPage(page);
    }

    private VBox dashboardCard(String title, String value, String color) {
        Label v = new Label(value);
        v.setFont(Font.font("System", FontWeight.BOLD, 27));
        v.setTextFill(Color.web(color));
        v.setWrapText(true);
        Label t = new Label(title);
        t.setFont(Font.font("System", FontWeight.BOLD, 13));
        t.setTextFill(Color.web(C_MUTED));
        VBox card = new VBox(8, v, t);
        card.setPadding(new Insets(22));
        card.setPrefWidth(210);
        card.setMinHeight(110);
        card.setStyle(cardStyle());
        HBox.setHgrow(card, Priority.ALWAYS);
        return card;
    }

    // ============================ END BONU ============================



    // =========================================================================
    // PERSON 1: DURDONA
    // Classes: Book, Catalog, Address, BookFormat, BookStatus
    // GUI: Books/Catalog page, book search, add/delete book, status badges.
    // =========================================================================
    /** Builds the books/catalog page and search controls. */
    private ScrollPane buildBooksPage(boolean isLib) {
        VBox page = pageShell("Book Management", "Search and manage the library catalog");
        TableView<BookItem> table = styledTable();
        table.getColumns().addAll(col("Barcode", b -> b.barcode), col("Title", b -> b.title), col("Author", b -> b.getAuthorsString()), col("Subject", b -> b.subject), col("ISBN", b -> b.ISBN), col("Published", b -> DATE_ONLY.format(b.publicationDate)), col("Format", b -> b.format.name()), statusCol(), col("Price", b -> String.format("$%.2f", b.price)));
        refreshBooksTable(table, library.getCatalog().getAllBooks());
        TextField searchField = fxField("Search by title, author, subject, ISBN or date...");
        ComboBox<String> searchType = fxCombo("Title", "Author", "Subject", "ISBN", "Publication Date");
        Button search = btn("Search", C_BLUE);
        Button clear = btn("Clear", C_MUTED);
        search.setOnAction(e -> {
            String q = searchField.getText().trim();
            if (q.isEmpty()) { refreshBooksTable(table, library.getCatalog().getAllBooks()); return; }
            List<BookItem> results;
            switch (searchType.getValue()) {
                case "Author": results = library.getCatalog().searchByAuthor(q); break;
                case "Subject": results = library.getCatalog().searchBySubject(q); break;
                case "ISBN": results = library.getCatalog().searchByISBN(q); break;
                case "Publication Date":
                    try { results = library.getCatalog().searchByPublicationDate(DATE_ONLY.parse(q)); }
                    catch (ParseException ex) { alert("Use date format: yyyy-MM-dd"); return; }
                    break;
                default: results = library.getCatalog().searchByTitle(q); break;
            }
            refreshBooksTable(table, results);
        });
        clear.setOnAction(e -> { searchField.clear(); refreshBooksTable(table, library.getCatalog().getAllBooks()); });
        HBox searchBar = new HBox(10, label("Search:"), searchField, label("by"), searchType, search, clear);
        searchBar.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(searchField, Priority.ALWAYS);
        HBox actions = new HBox(10);
        actions.setAlignment(Pos.CENTER_RIGHT);
        if (isLib) {
            Button add = btn("+ Add Book", C_GREEN);
            Button remove = btn("Delete", C_RED);
            add.setOnAction(e -> { showAddBookDialog(); refreshBooksTable(table, library.getCatalog().getAllBooks()); });
            remove.setOnAction(e -> {
                BookItem selected = table.getSelectionModel().getSelectedItem();
                if (selected == null) { alert("Select a book first."); return; }
                if (selected.status == BookStatus.LOANED) { alert("Cannot remove a loaned book."); return; }
                library.removeBookItem(selected);
                Notification.send("Book removed: " + selected.title);
                refreshBooksTable(table, library.getCatalog().getAllBooks());
            });
            actions.getChildren().addAll(add, remove);
        }
        BorderPane top = new BorderPane();
        top.setLeft(searchBar);
        top.setRight(actions);
        VBox card = cardBox(top, table);
        VBox.setVgrow(table, Priority.ALWAYS);
        page.getChildren().add(card);
        return wrapPage(page);
    }


    /** Reloads book data into the table. */
    private void refreshBooksTable(TableView<BookItem> table, List<BookItem> books) { table.setItems(FXCollections.observableArrayList(books)); }

    /** Creates colored status badges for book availability. */
    private TableColumn<BookItem, String> statusCol() {
        TableColumn<BookItem, String> column = new TableColumn<>("Status");
        column.setCellValueFactory(cd -> new SimpleStringProperty(cd.getValue().status.name()));
        column.setCellFactory(col -> new TableCell<BookItem, String>() {
            @Override protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) { setGraphic(null); setText(null); return; }
                Label badge = new Label(item);
                badge.setFont(Font.font("System", FontWeight.BOLD, 11));
                badge.setTextFill(Color.WHITE);
                badge.setPadding(new Insets(3, 9, 3, 9));
                String bg;
                switch (item) { case "AVAILABLE": bg = C_GREEN; break; case "LOANED": bg = C_RED; break; case "RESERVED": bg = C_ORANGE; break; default: bg = C_MUTED; break; }
                badge.setStyle("-fx-background-color: " + bg + "; -fx-background-radius: 20;");
                setGraphic(badge); setText(null);
            }
        });
        return column;
    }

    /** Opens dialog for adding a new book copy. */
    private void showAddBookDialog() {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Add New Book");
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        dialog.getDialogPane().setStyle("-fx-background-color: " + C_BG + ";");
        TextField isbn = fxField("ISBN"), title = fxField("Title"), author = fxField("Author"), subject = fxField("Subject"), publisher = fxField("Publisher"), pages = fxField("0"), price = fxField("0.00");
        ComboBox<BookFormat> format = new ComboBox<>(FXCollections.observableArrayList(BookFormat.values()));
        format.setValue(BookFormat.PAPERBACK);
        format.setMaxWidth(Double.MAX_VALUE);
        GridPane grid = new GridPane();
        grid.setHgap(10); grid.setVgap(9); grid.setPadding(new Insets(16));
        grid.addRow(0, bold("ISBN:"), isbn); grid.addRow(1, bold("Title:"), title); grid.addRow(2, bold("Author:"), author); grid.addRow(3, bold("Subject:"), subject); grid.addRow(4, bold("Publisher:"), publisher); grid.addRow(5, bold("Pages:"), pages); grid.addRow(6, bold("Price:"), price); grid.addRow(7, bold("Format:"), format);
        dialog.getDialogPane().setContent(grid);
        dialog.showAndWait().ifPresent(bt -> {
            if (bt != ButtonType.OK) return;
            try {
                String t = title.getText().trim(), a = author.getText().trim();
                if (t.isEmpty() || a.isEmpty()) { alert("Title and Author are required."); return; }
                library.addBookItem(new BookItem(isbn.getText().trim(), t, subject.getText().trim(), publisher.getText().trim(), "English", Integer.parseInt(pages.getText().trim()), a, Double.parseDouble(price.getText().trim()), format.getValue(), false));
                Notification.send("Book added: " + t);
            } catch (NumberFormatException ex) { alert("Invalid number for Pages or Price."); }
        });
    }
    // ============================ END DURDONA ============================





    // =========================================================================
    // PERSON 5: ZARINA
    // Classes: BookReservation, BookLending, Person, ReservationStatus
    // GUI: Checkout, Return, Renew, Reservations.
    // =========================================================================
    /** Builds checkout and return forms. */
    private ScrollPane buildCheckoutPage() {
        boolean isLib = currentUser instanceof Librarian;
        VBox page = pageShell("Checkout / Return", "Issue and return books with automatic due dates and fines");
        ComboBox<String> coMember = new ComboBox<>(), coBook = new ComboBox<>(); coMember.setMaxWidth(Double.MAX_VALUE); coBook.setMaxWidth(Double.MAX_VALUE);
        Button checkout = btn("Checkout Book", C_GREEN); checkout.setMaxWidth(Double.MAX_VALUE); Label coStatus = new Label(); coStatus.setWrapText(true);
        if (isLib) populateMemberCombo(coMember); else { coMember.getItems().add(currentUser.getId() + " — " + currentUser.getName()); coMember.setValue(coMember.getItems().get(0)); coMember.setDisable(true); }
        populateAvailableBooksCombo(coBook);
        VBox checkoutCard = infoCard("Checkout Book", fieldGroup("Member", coMember), fieldGroup("Book", coBook), checkout, coStatus);
        checkout.setOnAction(e -> {
            String selectedMember = coMember.getValue(), selectedBook = coBook.getValue();
            if (selectedMember == null || selectedBook == null) { errLabel(coStatus, "Select member and book."); return; }
            Member member = isLib ? findMemberById(selectedMember.split(" ")[0]) : (Member) currentUser;
            BookItem book = findBookByBarcode(selectedBook.split(" ")[0]);
            if (member == null || book == null) { errLabel(coStatus, "Not found."); return; }
            if (member.getStatus() == AccountStatus.BLACKLISTED) { errLabel(coStatus, "Member account is suspended."); return; }
            if (member.checkoutBook(book)) { library.recordLending(member.activeLoans.get(member.activeLoans.size() - 1)); Notification.send(member.getName() + " checked out: " + book.title); okLabel(coStatus, "Checked out. Due: " + SDF.format(book.dueDate)); populateAvailableBooksCombo(coBook); }
            else errLabel(coStatus, "Checkout failed. Book unavailable or limit reached.");
        });
        ComboBox<String> retMember = new ComboBox<>(), retBook = new ComboBox<>(); retMember.setMaxWidth(Double.MAX_VALUE); retBook.setMaxWidth(Double.MAX_VALUE);
        Button returnBtn = btn("Return Book", C_ORANGE); returnBtn.setMaxWidth(Double.MAX_VALUE); Label retStatus = new Label(); retStatus.setWrapText(true);
        if (isLib) {
            populateMemberCombo(retMember);
            retMember.setOnAction(e -> { String s = retMember.getValue(); if (s != null) populateLoanedBooksCombo(retBook, findMemberById(s.split(" ")[0])); });
            if (!retMember.getItems().isEmpty()) { retMember.setValue(retMember.getItems().get(0)); populateLoanedBooksCombo(retBook, findMemberById(retMember.getValue().split(" ")[0])); }
        } else { retMember.getItems().add(currentUser.getId() + " — " + currentUser.getName()); retMember.setValue(retMember.getItems().get(0)); retMember.setDisable(true); populateLoanedBooksCombo(retBook, (Member) currentUser); }
        VBox returnCard = infoCard("Return Book", fieldGroup("Member", retMember), fieldGroup("Book", retBook), returnBtn, retStatus);
        returnBtn.setOnAction(e -> {
            String selectedMember = retMember.getValue(), selectedBook = retBook.getValue();
            if (selectedMember == null || selectedBook == null) { errLabel(retStatus, "Select member and book."); return; }
            Member member = isLib ? findMemberById(selectedMember.split(" ")[0]) : (Member) currentUser;
            BookItem book = findBookByBarcode(selectedBook.split(" ")[0]);
            if (member == null || book == null) { errLabel(retStatus, "Not found."); return; }
            BookLending lending = member.returnBook(book);
            if (lending != null) {
                fulfillNextReservation(book);
                double fine = lending.calculateFine();
                if (fine > 0) errLabel(retStatus, String.format("Returned. Overdue fine: $%.2f", fine)); else okLabel(retStatus, "Book returned successfully.");
                Notification.send(member.getName() + " returned: " + book.title + (fine > 0 ? " | Fine: $" + String.format("%.2f", fine) : ""));
                populateLoanedBooksCombo(retBook, member); populateAvailableBooksCombo(coBook);
            } else errLabel(retStatus, "Return failed.");
        });
        HBox layout = new HBox(18, checkoutCard, returnCard); HBox.setHgrow(checkoutCard, Priority.ALWAYS); HBox.setHgrow(returnCard, Priority.ALWAYS);
        page.getChildren().add(layout); return wrapPage(page);
    }

    /** Builds the renew page for borrowed books. */
    private ScrollPane buildRenewPage() {
        VBox page = pageShell("Renew Book", "Extend due date if no other member has reserved the book");
        Member member = currentUser instanceof Member ? (Member) currentUser : null;
        if (member == null) { page.getChildren().add(cardBox(sectionTitle("Renew is available for members only."))); return wrapPage(page); }
        ComboBox<String> bookBox = new ComboBox<>(); bookBox.setMaxWidth(Double.MAX_VALUE);
        for (BookLending lending : member.activeLoans) bookBox.getItems().add(lending.book.barcode + " — " + lending.book.title);
        if (!bookBox.getItems().isEmpty()) bookBox.setValue(bookBox.getItems().get(0));
        Button renew = btn("Renew Book", C_PURPLE); renew.setMaxWidth(Double.MAX_VALUE); Label status = new Label(); status.setWrapText(true);
        renew.setOnAction(e -> {
            String selected = bookBox.getValue(); if (selected == null) { errLabel(status, "You have no active books to renew."); return; }
            BookItem book = findBookByBarcode(selected.split(" ")[0]); if (book == null) { errLabel(status, "Book not found."); return; }
            boolean success = member.renewBook(book, library);
            if (success) { Notification.send(member.getName() + " renewed: " + book.title); okLabel(status, "Book renewed. New due date: " + SDF.format(book.dueDate)); }
            else errLabel(status, "Renew failed. Another member may have reserved this book.");
        });
        VBox card = infoCard("Renew Your Borrowed Book", fieldGroup("Book", bookBox), renew, status);
        page.getChildren().add(card); return wrapPage(page);
    }

    /** When a book is returned, assigns it to the oldest waiting reservation. */
    private void fulfillNextReservation(BookItem book) {
        BookReservation oldest = null;

        for (Member member : library.getMembers()) {
            for (BookReservation reservation : member.activeReservations) {
                if (reservation.book == book && reservation.status == ReservationStatus.WAITING) {
                    if (oldest == null || reservation.creationDate.before(oldest.creationDate)) {
                        oldest = reservation;
                    }
                }
            }
        }

        if (oldest != null) {
            oldest.markReadyForPickup();
            book.status = BookStatus.RESERVED;

            Notification.send(
                    "Reservation ready for pickup: "
                            + oldest.member.getName()
                            + " — "
                            + book.title
            );
        } else {
            book.status = BookStatus.AVAILABLE;
        }
    }

    private void populateMemberCombo(ComboBox<String> combo) {
        combo.getItems().clear(); for (Member m : library.getMembers()) combo.getItems().add(m.getId() + " — " + m.getName());
        if (!combo.getItems().isEmpty()) combo.setValue(combo.getItems().get(0)); }
    private void populateAvailableBooksCombo(ComboBox<String> combo) {
        combo.getItems().clear();

        for (BookItem book : library.getCatalog().getAllBooks()) {

            // Normal available books
            if (book.status == BookStatus.AVAILABLE) {
                combo.getItems().add(book.barcode + " — " + book.title);
            }

            // Reserved books ONLY for the member who reserved them
            else if (book.status == BookStatus.RESERVED && currentUser instanceof Member member) {

                boolean reservedByCurrentUser = member.activeReservations.stream()
                        .anyMatch(r ->
                                r.book == book &&
                                        r.status == ReservationStatus.READY_FOR_PICKUP
                        );

                if (reservedByCurrentUser) {
                    combo.getItems().add(book.barcode + " — " + book.title + " (Reserved for you)");
                }
            }
        }

        if (!combo.getItems().isEmpty()) {
            combo.setValue(combo.getItems().get(0));
        }
    }    private void populateLoanedBooksCombo(ComboBox<String> combo, Member member) { combo.getItems().clear(); if (member == null) return; for (BookLending lending : member.activeLoans) combo.getItems().add(lending.book.barcode + " — " + lending.book.title); if (!combo.getItems().isEmpty()) combo.setValue(combo.getItems().get(0)); }



    /** Builds the reservation table and reserve/cancel controls. */
    private ScrollPane buildReservationsPage() {
        boolean isLib = currentUser instanceof Librarian;
        VBox page = pageShell("Reservations", "Reserve unavailable books and manage waiting requests");
        TableView<BookReservation> table = styledTable();
        TableColumn<BookReservation, String> colMember = new TableColumn<>("Member"); colMember.setCellValueFactory(cd -> new SimpleStringProperty(cd.getValue().member.getName()));
        TableColumn<BookReservation, String> colBook = new TableColumn<>("Book Title"); colBook.setCellValueFactory(cd -> new SimpleStringProperty(cd.getValue().book.title));
        TableColumn<BookReservation, String> colBookStatus = new TableColumn<>("Book Status"); colBookStatus.setCellValueFactory(cd -> new SimpleStringProperty(cd.getValue().book.status.name()));
        TableColumn<BookReservation, String> colDate = new TableColumn<>("Reserved On"); colDate.setCellValueFactory(cd -> new SimpleStringProperty(SDF.format(cd.getValue().creationDate)));
        TableColumn<BookReservation, String> colStatus = new TableColumn<>("Reservation"); colStatus.setCellValueFactory(cd -> new SimpleStringProperty(cd.getValue().status.name()));
        table.getColumns().addAll(colMember, colBook, colBookStatus, colDate, colStatus); refreshReservationsTable(table, isLib);
        Button reserve = btn("+ Reserve Book", C_BLUE), cancel = btn("Cancel Reservation", C_RED), refresh = btn("Refresh", C_MUTED);
        reserve.setOnAction(e -> { showReserveDialog(isLib); refreshReservationsTable(table, isLib); });
        cancel.setOnAction(e -> {
            BookReservation selected = table.getSelectionModel().getSelectedItem();
            if (selected == null) { alert("Select a reservation first."); return; }
            if (!isLib && selected.member != currentUser) { alert("You can only cancel your own reservations."); return; }
            if (selected.status == ReservationStatus.COMPLETED) { alert("This reservation is already completed."); return; }
            selected.cancelReservation(); Notification.send("Reservation cancelled: " + selected.book.title); refreshReservationsTable(table, isLib);
        });
        refresh.setOnAction(e -> refreshReservationsTable(table, isLib));
        HBox actions = new HBox(10, reserve, cancel, refresh); VBox card = cardBox(actions, table); VBox.setVgrow(table, Priority.ALWAYS); page.getChildren().add(card); return wrapPage(page);
    }

    /** Reloads visible reservation records based on user role. */
    private void refreshReservationsTable(TableView<BookReservation> table, boolean isLib) {
        ObservableList<BookReservation> items = FXCollections.observableArrayList();
        for (Member m : library.getMembers()) { if (!isLib && m != currentUser) continue; for (BookReservation r : m.activeReservations) if (r.status == ReservationStatus.WAITING ||
            r.status == ReservationStatus.READY_FOR_PICKUP ||
            r.status == ReservationStatus.COMPLETED) items.add(r); }
        table.setItems(items);
    }



    /** Opens the reservation dialog and prevents duplicate reservations. */
    private void showReserveDialog(boolean isLib) {
        List<BookItem> loanedBooks = new ArrayList<>();
        for (BookItem b : library.getCatalog().getAllBooks()) if (b.status == BookStatus.LOANED) loanedBooks.add(b);
        if (loanedBooks.isEmpty()) { alert("No loaned books available to reserve."); return; }
        ComboBox<String> memberBox = new ComboBox<>(), bookBox = new ComboBox<>(); memberBox.setMaxWidth(Double.MAX_VALUE); bookBox.setMaxWidth(Double.MAX_VALUE);
        if (isLib) populateMemberCombo(memberBox); else { memberBox.getItems().add(currentUser.getId() + " — " + currentUser.getName()); memberBox.setValue(memberBox.getItems().get(0)); memberBox.setDisable(true); }
        for (BookItem b : loanedBooks) bookBox.getItems().add(b.barcode + " — " + b.title); bookBox.setValue(bookBox.getItems().get(0));
        Dialog<ButtonType> dialog = new Dialog<>(); dialog.setTitle("Reserve a Book"); dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL); dialog.getDialogPane().setStyle("-fx-background-color: " + C_BG + ";");
        GridPane grid = new GridPane(); grid.setHgap(10); grid.setVgap(10); grid.setPadding(new Insets(16)); grid.addRow(0, bold("Member:"), memberBox); grid.addRow(1, bold("Book:"), bookBox); dialog.getDialogPane().setContent(grid);
        dialog.showAndWait().ifPresent(bt -> {
            if (bt != ButtonType.OK) return;
            String selectedMember = memberBox.getValue(), selectedBook = bookBox.getValue(); if (selectedMember == null || selectedBook == null) return;
            Member member = isLib ? findMemberById(selectedMember.split(" ")[0]) : (Member) currentUser; BookItem book = findBookByBarcode(selectedBook.split(" ")[0]); if (member == null || book == null) return;
            boolean duplicate = member.activeReservations.stream().anyMatch(r -> r.book == book && r.status == ReservationStatus.WAITING);
            if (duplicate) { alert("You already have a reservation for this book."); return; }
            if (member.reserveBook(book)) { Notification.send(member.getName() + " reserved: " + book.title); alert("Reservation placed for: " + book.title); } else alert("Reservation failed.");
        });
    }
    // ============================ END ZARINA ============================




    // =========================================================================
    // PERSON 6: RAMIZA CONTINUED
    // GUI: Fines page and fine display.
    // =========================================================================
    /** Builds the fines table and total outstanding fine label. */
    private ScrollPane buildFinesPage() {
        boolean isLib = currentUser instanceof Librarian; VBox page = pageShell("Fines", "View overdue penalties and fine totals");
        TableView<BookLending> table = styledTable(); table.getColumns().addAll(col("Member", l -> l.member.getName()), col("Book", l -> l.book.title), col("Due Date", l -> SDF.format(l.dueDate)), col("Returned", l -> l.returnDate != null ? SDF.format(l.returnDate) : "Not returned"), col("Days Late", l -> String.valueOf(daysLate(l))), fineAmountCol());
        refreshFinesTable(table, isLib); Label total = new Label(); total.setFont(Font.font("System", FontWeight.BOLD, 14)); updateFineTotal(total, isLib);
        Button refresh = btn("Refresh", C_BLUE); refresh.setOnAction(e -> { refreshFinesTable(table, isLib); updateFineTotal(total, isLib); });
        HBox top = new HBox(10, refresh, new Region(), total); top.setAlignment(Pos.CENTER_LEFT); HBox.setHgrow(top.getChildren().get(1), Priority.ALWAYS);
        VBox card = cardBox(top, table); VBox.setVgrow(table, Priority.ALWAYS); page.getChildren().add(card); return wrapPage(page);
    }

    /** Calculates how many days a book is late. */
    private long daysLate(BookLending lending) { Date check = lending.returnDate != null ? lending.returnDate : new Date(); if (!check.after(lending.dueDate)) return 0; long diff = check.getTime() - lending.dueDate.getTime(); long days = diff / (24 * 60 * 60 * 1000); return days == 0 ? 1 : days; }

    /** Creates the fine amount table column. */
    private TableColumn<BookLending, String> fineAmountCol() {
        TableColumn<BookLending, String> column = new TableColumn<>("Fine");
        column.setCellValueFactory(cd -> { double fine = cd.getValue().calculateFine(); return new SimpleStringProperty(fine > 0 ? String.format("$%.2f", fine) : "—"); });
        column.setCellFactory(col -> new TableCell<BookLending, String>() { @Override protected void updateItem(String item, boolean empty) { super.updateItem(item, empty); if (empty || item == null) { setText(null); setStyle(""); return; } setText(item); if (!"—".equals(item)) { setFont(Font.font("System", FontWeight.BOLD, 13)); setTextFill(Color.web(C_RED)); } else { setFont(Font.font(13)); setTextFill(Color.web(C_MUTED)); } } });
        return column;
    }

    private void refreshFinesTable(TableView<BookLending> table, boolean isLib) { List<BookLending> rows = new ArrayList<>(); for (BookLending l : library.getAllLendings()) { if (!isLib && l.member != currentUser) continue; if (l.calculateFine() > 0) rows.add(l); } table.setItems(FXCollections.observableArrayList(rows)); }
    private void updateFineTotal(Label label, boolean isLib) { double total = 0; for (BookLending l : library.getAllLendings()) { if (!isLib && l.member != currentUser) continue; total += l.calculateFine(); } label.setText(String.format("Outstanding fines: $%.2f", total)); label.setTextFill(total > 0 ? Color.web(C_RED) : Color.web(C_GREEN)); }
    // ============================ END RAMIZA ============================



    // =========================================================================
    // PERSON 3: MARJONA
    // Classes: Notification, EmailNotification, PostalNotification, Librarian, Member
    // GUI: Notifications page and Member management page.
    // =========================================================================
    /** Builds notification/activity log page. */
    private ScrollPane buildNotificationsPage() {
        VBox page = pageShell("Notifications", "System activity and action history");
        TextArea area = new TextArea(); area.setEditable(false); area.setStyle("-fx-font-family: Consolas; -fx-font-size: 12px; -fx-background-color: white;"); refreshNotifications(area);
        Button refresh = btn("Refresh", C_BLUE), clear = btn("Clear", C_RED); refresh.setOnAction(e -> refreshNotifications(area)); clear.setOnAction(e -> { Notification.log.clear(); area.clear(); });
        HBox buttons = new HBox(10, refresh, clear); VBox card = cardBox(buttons, area); VBox.setVgrow(area, Priority.ALWAYS); page.getChildren().add(card); return wrapPage(page);
    }

    /** Reloads notification messages from the Notification log. */
    private void refreshNotifications(TextArea area) { StringBuilder sb = new StringBuilder(); List<String> log = Notification.getLog(); for (int i = log.size() - 1; i >= 0; i--) sb.append(log.get(i)).append("\n"); area.setText(sb.toString()); }

    /** Builds member management page for librarian. */
    private ScrollPane buildMembersPage() {
        VBox page = pageShell("Members", "Manage member accounts and account status");
        TableView<Member> table = styledTable(); table.getColumns().addAll(col("ID", m -> m.getId()), col("Name", m -> m.getName()), col("Email", m -> m.getEmail()), col("Phone", m -> m.getPhone()), memberStatusCol(), col("Books Out", m -> m.getTotalCheckedOutBooks() + "/" + Member.MAX_BOOKS_CHECKOUT)); refreshMembersTable(table);
        Button block = btn("Block", C_ORANGE), unblock = btn("Unblock", C_GREEN), delete = btn("Delete", C_RED);
        block.setOnAction(e -> { Member member = table.getSelectionModel().getSelectedItem(); if (member == null) { alert("Select a member first."); return; } if (member.totalBooksCheckedOut > 0) { alert("Member has active loans."); return; } member.status = AccountStatus.BLACKLISTED; Notification.send("Member blocked: " + member.getName()); refreshMembersTable(table); });
        unblock.setOnAction(e -> { Member member = table.getSelectionModel().getSelectedItem(); if (member == null) { alert("Select a member first."); return; } member.status = AccountStatus.ACTIVE; Notification.send("Member unblocked: " + member.getName()); refreshMembersTable(table); });
        delete.setOnAction(e -> { Member member = table.getSelectionModel().getSelectedItem(); if (member == null) { alert("Select a member first."); return; } if (member.totalBooksCheckedOut > 0) { alert("Member has active loans. Cannot delete."); return; } library.removeMember(member); Notification.send("Member deleted: " + member.getName()); refreshMembersTable(table); });
        HBox actions = new HBox(10, block, unblock, delete); VBox card = cardBox(actions, table); VBox.setVgrow(table, Priority.ALWAYS); page.getChildren().add(card); return wrapPage(page);
    }

    /** Creates colored account status badges for members. */
    private TableColumn<Member, String> memberStatusCol() {
        TableColumn<Member, String> column = new TableColumn<>("Status"); column.setCellValueFactory(cd -> new SimpleStringProperty(cd.getValue().getStatus().name()));
        column.setCellFactory(col -> new TableCell<Member, String>() { @Override protected void updateItem(String item, boolean empty) { super.updateItem(item, empty); if (empty || item == null) { setGraphic(null); setText(null); return; } Label badge = new Label(item); badge.setFont(Font.font("System", FontWeight.BOLD, 11)); badge.setTextFill(Color.WHITE); badge.setPadding(new Insets(3, 9, 3, 9)); String bg = "ACTIVE".equals(item) ? C_GREEN : C_RED; badge.setStyle("-fx-background-color: " + bg + "; -fx-background-radius: 20;"); setGraphic(badge); setText(null); } });
        return column;
    }


    private void refreshMembersTable(TableView<Member> table) {
        table.setItems(FXCollections.observableArrayList(library.getMembers()));
    }
    // ========================= MARJONA CONTINUES LATER =========================


    // =========================================================================
    // PERSON 4: FERUZA CONTINUED
    // GUI: My Account page and helper find methods.
    // =========================================================================
    /** Builds member profile page with library card and active loans. */
    private ScrollPane buildMyAccountPage(Member member) {
        VBox page = pageShell("My Account", "Library card, membership details and active loans");
        VBox profile = cardBox(); profile.setMaxWidth(420); profile.setAlignment(Pos.TOP_CENTER);
        Label avatar = new Label("👤"); avatar.setFont(Font.font(46)); Label name = new Label(member.getName()); name.setFont(Font.font("System", FontWeight.BOLD, 21)); name.setTextFill(Color.web(C_TEXT));
        GridPane info = new GridPane(); info.setHgap(16); info.setVgap(9); int r = 0;
        info.addRow(r++, bold("Email:"), label(member.getEmail())); info.addRow(r++, bold("Phone:"), label(member.getPhone())); info.addRow(r++, bold("Member ID:"), label(member.getId())); info.addRow(r++, bold("Card No:"), label(member.libraryCard.cardNumber)); info.addRow(r++, bold("Card Barcode:"), label(member.libraryCard.barcode)); info.addRow(r++, bold("Member Since:"), label(SDF.format(member.dateOfMembership))); info.addRow(r++, bold("Status:"), label(member.getStatus().name())); info.addRow(r, bold("Books Out:"), label(member.getTotalCheckedOutBooks() + " / " + Member.MAX_BOOKS_CHECKOUT));
        profile.getChildren().addAll(avatar, name, new Separator(), info);
        TableView<BookLending> loans = styledTable(); loans.setPrefHeight(260); loans.getColumns().addAll(col("Title", l -> l.book.title), col("Barcode", l -> l.book.barcode), col("Due Date", l -> SDF.format(l.dueDate)), col("Overdue", l -> l.isOverdue() ? "YES" : "No"), col("Fine", l -> l.calculateFine() > 0 ? String.format("$%.2f", l.calculateFine()) : "—")); loans.setItems(FXCollections.observableArrayList(member.activeLoans));
        VBox loansCard = cardBox(sectionTitle("Active Loans"), loans); VBox.setVgrow(loans, Priority.ALWAYS); HBox layout = new HBox(18, profile, loansCard); HBox.setHgrow(loansCard, Priority.ALWAYS); page.getChildren().add(layout); return wrapPage(page);
    }

    private BookItem findBookByBarcode(String barcode) { return library.getCatalog().getAllBooks().stream().filter(b -> b.barcode.equals(barcode)).findFirst().orElse(null); }
    private Member findMemberById(String id) { return library.getMembers().stream().filter(m -> m.getId().equals(id)).findFirst().orElse(null); }
    private List<BookLending> visibleLendings(boolean isLib) { List<BookLending> result = new ArrayList<>(); for (BookLending l : library.getAllLendings()) { if (!isLib && l.member != currentUser) continue; result.add(l); } return result; }
    // ============================ END FERUZA ============================





    // =========================================================================
    // PERSON 3: MARJONA CONTINUED
    // GUI: Shared UI helper methods used by all pages.
    // =========================================================================
    private VBox pageShell(String title, String subtitle)
    { VBox page = new VBox(18);
        page.setPadding(new Insets(28));
        page.setStyle("-fx-background-color: " + C_BG + ";");
        Label h = new Label(title); h.setFont(Font.font("System", FontWeight.BOLD, 30));
        h.setTextFill(Color.web(C_TEXT)); Label s = new Label(subtitle);
        s.setFont(Font.font(14));
        s.setTextFill(Color.web(C_MUTED));
        page.getChildren().addAll(h, s); return page; }
    private ScrollPane wrapPage(VBox page) {
        ScrollPane scroll = new ScrollPane(page);
        scroll.setFitToWidth(true);
        scroll.setFitToHeight(true);
        scroll.setStyle("-fx-background-color: " + C_BG + "; -fx-background: " + C_BG + ";");
        return scroll;
    }
    private VBox cardBox(Node... nodes) {
        VBox box = new VBox(16);
        box.setPadding(new Insets(20));
        box.setStyle(cardStyle());
        box.getChildren().addAll(nodes);
        return box;
    }
    private VBox infoCard(String title, Node... nodes) {
        Label label = sectionTitle(title);
        VBox box = new VBox(14, label, new Separator());
        box.getChildren().addAll(nodes);
        box.setPadding(new Insets(22));
        box.setStyle(cardStyle()); return box;
    }
    private Label sectionTitle(String text) {
        Label label = new Label(text);
        label.setFont(Font.font("System", FontWeight.BOLD, 16));
        label.setTextFill(Color.web(C_TEXT)); return label; }
    private Label pageTitle(String text) {
        Label label = new Label(text);
        label.setFont(Font.font("System", FontWeight.BOLD, 24));
        label.setTextFill(Color.web(C_TEXT)); return label; }
    private Label smallText(String text) {
        Label label = new Label(text);
        label.setFont(Font.font(13)); label.setTextFill(Color.web(C_MUTED)); label.setWrapText(true); return label; }
    private <T> TableColumn<T, String> col(String title, Function<T, String> extractor) { TableColumn<T, String> column = new TableColumn<>(title); column.setCellValueFactory(cd -> new SimpleStringProperty(extractor.apply(cd.getValue()))); return column; }
    private <T> TableView<T> styledTable() { TableView<T> table = new TableView<>(); table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY); table.setStyle("-fx-font-size: 13px;-fx-background-color: white;-fx-background-radius: 12;-fx-border-color: " + C_BORDER + ";-fx-border-radius: 12;"); table.setPlaceholder(new Label("No data to display.")); return table; }
    private TextField fxField(String prompt) { TextField field = new TextField(); field.setPromptText(prompt); field.setStyle("-fx-background-color: white;-fx-background-radius: 10;-fx-border-radius: 10;-fx-border-color: " + C_BORDER + ";-fx-padding: 10 12;-fx-font-size: 13;"); return field; }
    private PasswordField fxPass(String prompt) { PasswordField field = new PasswordField(); field.setPromptText(prompt); field.setStyle("-fx-background-color: white;-fx-background-radius: 10;-fx-border-radius: 10;-fx-border-color: " + C_BORDER + ";-fx-padding: 10 12;-fx-font-size: 13;"); return field; }
    @SafeVarargs private <T> ComboBox<T> fxCombo(T... items) { ComboBox<T> combo = new ComboBox<>(FXCollections.observableArrayList(items)); combo.setValue(items[0]); combo.setMaxWidth(Double.MAX_VALUE); combo.setStyle("-fx-background-color: white;-fx-background-radius: 10;-fx-border-radius: 10;-fx-border-color: " + C_BORDER + ";-fx-padding: 4 8;-fx-font-size: 13;"); return combo; }
    private Button btn(String text, String color) { Button button = new Button(text); button.setFont(Font.font("System", FontWeight.BOLD, 13)); button.setTextFill(Color.WHITE); button.setMinHeight(40); button.setPrefHeight(40); button.setMinWidth(108); button.setCursor(Cursor.HAND); String normal = buttonStyle(color, 0, 8, 2), hover = buttonStyle("derive(" + color + ", -10%)", 0, 12, 3), pressed = buttonStyle("derive(" + color + ", -18%)", 0, 5, 1); button.setStyle(normal); button.setOnMouseEntered(e -> button.setStyle(hover)); button.setOnMouseExited(e -> button.setStyle(normal)); button.setOnMousePressed(e -> button.setStyle(pressed)); button.setOnMouseReleased(e -> button.setStyle(hover)); return button; }
    private String buttonStyle(String color, int spread, int radius, int y) { return "-fx-background-color: " + color + ";-fx-background-radius: 10;-fx-border-radius: 10;-fx-padding: 9 16;-fx-effect: dropshadow(gaussian, rgba(15,23,42,0.18), " + radius + ", " + spread + ", 0, " + y + ");"; }
    private Label bold(String text) { Label label = new Label(text); label.setFont(Font.font("System", FontWeight.BOLD, 13)); label.setTextFill(Color.web(C_TEXT)); return label; }
    private Label label(String text) { Label label = new Label(text); label.setFont(Font.font(13)); label.setTextFill(Color.web(C_TEXT)); return label; }
    private VBox fieldGroup(String labelText, Node control) { Label label = new Label(labelText); label.setFont(Font.font("System", FontWeight.BOLD, 12)); label.setTextFill(Color.web(C_MUTED)); VBox group = new VBox(5, label, control); VBox.setVgrow(control, Priority.NEVER); return group; }
    private String cardStyle() { return "-fx-background-color: " + C_CARD + ";-fx-background-radius: 16;-fx-effect: dropshadow(gaussian, rgba(15,23,42,0.10), 14, 0, 0, 4);"; }
    private void errLabel(Label label, String message) { label.setText(message); label.setTextFill(Color.web(C_RED)); }
    private void okLabel(Label label, String message) { label.setText(message); label.setTextFill(Color.web(C_GREEN)); }
    private void alert(String message) { Alert alert = new Alert(Alert.AlertType.INFORMATION, message, ButtonType.OK); alert.setHeaderText(null); alert.getDialogPane().setStyle("-fx-background-color: " + C_BG + ";"); alert.showAndWait(); }
    // ============================ END MARJONA ============================
    public static void main(String[] args) {
        launch(args);
    }
}
