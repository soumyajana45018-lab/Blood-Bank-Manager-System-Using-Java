
import java.util.*;
import java.io.*;
import java.time.*;

public class BloodBankManager {

    static Scanner sc = new Scanner(System.in);

    static HashMap<String, Donor> donors = new HashMap<>();
    static LinkedList<Donation> donations = new LinkedList<>();
    static ArrayList<BloodUnit> units = new ArrayList<>();

    static int donorId = 101, unitId = 101;
    static final String FILE = "blood_bank.txt";

    // Donor Class
    static class Donor {
        String id, name, group;

        Donor(String id, String name, String group) {
            this.id = id;
            this.name = name;
            this.group = group;
        }
    }

    // Blood Unit Class
    static class BloodUnit {
        String id, group, donor;
        LocalDate expiry;
        boolean issued;

        BloodUnit(String id, String group, LocalDate expiry,
                  String donor, boolean issued) {
            this.id = id;
            this.group = group;
            this.expiry = expiry;
            this.donor = donor;
            this.issued = issued;
        }
    }

    // Donation Class
    static class Donation {
        String donor, unit;
        LocalDate date;

        Donation(String donor, String unit, LocalDate date) {
            this.donor = donor;
            this.unit = unit;
            this.date = date;
        }
    }

    // Validate Blood Group
    static boolean validGroup(String g) {
        return Arrays.asList("A+", "A-", "B+", "B-",
                "AB+", "AB-", "O+", "O-").contains(g);
    }

    // Register Donor
    static void registerDonor() {
        System.out.print("Enter name: ");
        String name = sc.nextLine().trim();

        System.out.print("Enter blood group: ");
        String group = sc.nextLine().trim().toUpperCase();

        if (name.isEmpty() || name.contains("|") || !validGroup(group)) {
            System.out.println("Invalid input!");
            return;
        }

        String id = "D" + donorId++;
        donors.put(id, new Donor(id, name, group));

        System.out.println("Donor registered: " +
                id + " | " + name + " | " + group);
    }

    // Search Donor
    static void searchDonor() {
        System.out.print("Enter donor ID: ");
        String id = sc.nextLine().trim().toUpperCase();

        Donor d = donors.get(id);

        if (d == null)
            System.out.println("Donor " + id + " not found.");
        else
            System.out.println(d.id + " | " + d.name + " | " + d.group);
    }

    // Add Blood Unit
    static void addUnit() {
        System.out.print("Enter donor ID: ");
        String id = sc.nextLine().trim().toUpperCase();

        Donor d = donors.get(id);

        if (d == null) {
            System.out.println("Donor not found!");
            return;
        }

        System.out.print("Enter expiry date (YYYY-MM-DD): ");

        try {
            LocalDate date = LocalDate.parse(sc.nextLine().trim());

            if (!date.isAfter(LocalDate.now())) {
                System.out.println("Invalid expiry date!");
                return;
            }

            String uid = "U" + unitId++;

            units.add(new BloodUnit(uid, d.group, date, id, false));
            donations.add(new Donation(id, uid, LocalDate.now()));

            System.out.println("Blood unit added: " + uid);

        } catch (Exception e) {
            System.out.println("Invalid date!");
        }
    }

    // Sort By Expiry Date (Bubble Sort)
    static void sortUnits() {
        for (int i = 0; i < units.size() - 1; i++) {
            for (int j = 0; j < units.size() - i - 1; j++) {
                if (units.get(j).expiry.isAfter(units.get(j + 1).expiry)) {
                    Collections.swap(units, j, j + 1);
                }
            }
        }
    }

    // Binary Search By Unit ID
    static BloodUnit binarySearch(String id) {
        ArrayList<BloodUnit> list = new ArrayList<>(units);

        list.sort(Comparator.comparing(u -> u.id));

        int low = 0, high = list.size() - 1;

        while (low <= high) {
            int mid = (low + high) / 2;
            int cmp = list.get(mid).id.compareTo(id);

            if (cmp == 0) return list.get(mid);
            if (cmp < 0) low = mid + 1;
            else high = mid - 1;
        }

        return null;
    }

    // Search Blood Unit
    static void searchUnit() {
        System.out.print("Enter unit ID: ");
        String id = sc.nextLine().trim().toUpperCase();

        BloodUnit u = binarySearch(id);

        if (u == null)
            System.out.println("Unit not found!");
        else
            System.out.println(u.id + " | " + u.group + " | " +
                    u.expiry + " | " + (u.issued ? "Issued" : "Available"));
    }

    // Search Available Units
    static void availableUnits() {
        System.out.print("Enter blood group: ");
        String g = sc.nextLine().trim().toUpperCase();

        if (!validGroup(g)) {
            System.out.println("Invalid blood group!");
            return;
        }

        int count = 0;

        for (BloodUnit u : units) {
            if (u.group.equals(g) && !u.issued &&
                    u.expiry.isAfter(LocalDate.now()))
                count++;
        }

        System.out.println("Available " + g + " units: " + count);
    }

    // Issue Earliest Expiring Valid Unit
    static void issueUnit() {
        System.out.print("Enter blood group: ");
        String g = sc.nextLine().trim().toUpperCase();

        if (!validGroup(g)) {
            System.out.println("Invalid blood group!");
            return;
        }

        sortUnits();

        for (BloodUnit u : units) {
            if (u.group.equals(g) && !u.issued &&
                    u.expiry.isAfter(LocalDate.now())) {

                u.issued = true;
                System.out.println("Issued unit: " + u.id);
                return;
            }
        }

        System.out.println("No valid " + g + " unit available.");
    }

    // Display Blood Units
    static void displayUnits() {
        sortUnits();

        for (BloodUnit u : units) {
            System.out.println(u.id + " | " + u.group + " | " +
                    u.expiry + " | " + (u.issued ? "Issued" : "Available"));
        }
    }

    // Display Donation Records
    static void displayDonations() {
        for (Donation d : donations) {
            System.out.println(d.donor + " | " + d.unit + " | " + d.date);
        }
    }

    // Save Data
    static void saveData() {
        try (PrintWriter pw = new PrintWriter(new FileWriter(FILE))) {

            for (Donor d : donors.values())
                pw.println("D|" + d.id + "|" + d.name + "|" + d.group);

            for (BloodUnit u : units)
                pw.println("U|" + u.id + "|" + u.group + "|" +
                        u.expiry + "|" + u.donor + "|" + u.issued);

            for (Donation d : donations)
                pw.println("R|" + d.donor + "|" + d.unit + "|" + d.date);

            System.out.println("Data saved successfully!");

        } catch (IOException e) {
            System.out.println("File error: " + e.getMessage());
        }
    }

    // Load Data
    static void loadData() {
        try (BufferedReader br = new BufferedReader(new FileReader(FILE))) {
            String line;

            while ((line = br.readLine()) != null) {
                String[] a = line.split("\\|");

                try {
                    switch (a[0]) {
                        case "D":
                            Donor d = new Donor(a[1], a[2], a[3]);
                            donors.put(d.id, d);
                            donorId = Math.max(donorId,
                                    Integer.parseInt(d.id.substring(1)) + 1);
                            break;

                        case "U":
                            BloodUnit u = new BloodUnit(a[1], a[2],
                                    LocalDate.parse(a[3]), a[4],
                                    Boolean.parseBoolean(a[5]));
                            units.add(u);
                            unitId = Math.max(unitId,
                                    Integer.parseInt(u.id.substring(1)) + 1);
                            break;

                        case "R":
                            donations.add(new Donation(a[1], a[2],
                                    LocalDate.parse(a[3])));
                            break;
                    }
                } catch (RuntimeException e) {
                    System.out.println("Skipping invalid record.");
                }
            }

            System.out.println("Data loaded successfully!");

        } catch (FileNotFoundException e) {
            System.out.println("New database created.");
        } catch (IOException e) {
            System.out.println("File loading error!");
        }
    }

    // Main Method
    public static void main(String[] args) {
        loadData();

        while (true) {
            System.out.println("\n===== BLOOD BANK MANAGER =====");
            System.out.println("1. Register Donor");
            System.out.println("2. Search Donor");
            System.out.println("3. Add Blood Unit");
            System.out.println("4. Search Blood Unit");
            System.out.println("5. Available Units");
            System.out.println("6. Issue Blood Unit");
            System.out.println("7. Display Blood Units");
            System.out.println("8. Display Donations");
            System.out.println("9. Save Data");
            System.out.println("0. Exit");

            System.out.print("Enter choice: ");
            String ch = sc.nextLine().trim();

            switch (ch) {
                case "1": registerDonor(); break;
                case "2": searchDonor(); break;
                case "3": addUnit(); break;
                case "4": searchUnit(); break;
                case "5": availableUnits(); break;
                case "6": issueUnit(); break;
                case "7": displayUnits(); break;
                case "8": displayDonations(); break;
                case "9": saveData(); break;
                case "0":
                    saveData();
                    System.out.println("Thank you!");
                    sc.close();
                    return;
                default:
                    System.out.println("Invalid choice!");
            }
        }
    }
}
