import java.util.*;

public class TimetableGenerator {
    private static final String[] DAYS =
        {"Monday","Tuesday","Wednesday","Thursday","Friday","Saturday"};
    private static final int DAYS_COUNT = DAYS.length;
    private static final int PERIODS = 7;

    static class Course {
        String name;
        int tf;
        Course(String n, int t) { name = n; tf = t; }
    }

    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        Random rnd = new Random();

        // 1) Read class info
        System.out.print("Department: ");
        String dept = sc.nextLine();
        System.out.print("Year: ");
        int year = sc.nextInt();
        sc.nextLine();  // consume newline
        System.out.print("Section: ");
        String section = sc.nextLine();

        // 2) Read courses
        System.out.print("Number of courses: ");
        int n = sc.nextInt();
        List<Course> dailyClasses = new ArrayList<>();
        List<Course> labs = new ArrayList<>();
        for (int i = 1; i <= n; i++) {
            System.out.printf("Course %d name: ", i);
            String name = sc.next();
            System.out.print("Timeframe: ");
            int tf = sc.nextInt();
            Course c = new Course(name, tf);
            if (tf == 1) dailyClasses.add(c);
            else          labs.add(c);
        }

        // 3) Initialize empty timetable
        String[][] tt = new String[DAYS_COUNT][PERIODS];
        for (int d = 0; d < DAYS_COUNT; d++) {
            Arrays.fill(tt[d], "—");
        }

        // 4) Place lab blocks (one lab per day, random half)
        boolean[] hasLab = new boolean[DAYS_COUNT];
        int rot = 0;
        for (Course lab : labs) {
            boolean placed = false;
            for (int di = 0; di < DAYS_COUNT && !placed; di++) {
                int day = (rot + di) % DAYS_COUNT;
                if (hasLab[day]) continue;

                // decide half first: true=first half, false=second
                boolean firstHalf = rnd.nextBoolean();
                placed = placeBlock(tt, day, lab, firstHalf);
                if (!placed) {
                    // try the other half
                    placed = placeBlock(tt, day, lab, !firstHalf);
                }
                if (placed) {
                    hasLab[day] = true;
                }
            }
            rot = (rot + 1) % DAYS_COUNT;
        }

        // 5) Place daily classes into shuffled free slots
        for (int day = 0; day < DAYS_COUNT; day++) {
            // gather free period indices
            List<Integer> freeSlots = new ArrayList<>();
            for (int p = 0; p < PERIODS; p++) {
                if (tt[day][p].equals("—")) {
                    freeSlots.add(p);
                }
            }
            Collections.shuffle(freeSlots, rnd);
            Collections.shuffle(dailyClasses, rnd);

            // assign each daily course once
            for (int i = 0; i < dailyClasses.size() && i < freeSlots.size(); i++) {
                int slot = freeSlots.get(i);
                tt[day][slot] = dailyClasses.get(i).name;
            }
        }

        // 6) Print timetable
        System.out.print("Days/Period ");
        for (int p = 1; p <= PERIODS; p++) {
            System.out.printf("| Period%-2d ", p);
        }
        System.out.println();
        for (int d = 0; d < DAYS_COUNT; d++) {
            System.out.printf("%-10s", DAYS[d]);
            for (int p = 0; p < PERIODS; p++) {
                System.out.printf("| %-9s", tt[d][p]);
            }
            System.out.println();
        }
    }

    /**
     * Attempts to place a lab block of length lab.tf on the given day
     * within the specified half. Returns true if placed.
     */
    private static boolean placeBlock(
            String[][] tt, int day, Course lab, boolean firstHalf) {
        int halfSplit = PERIODS / 2;  // 7/2 = 3 → periods 0-2 vs 3-6
        int startMin = firstHalf ? 0 : halfSplit;
        int startMax = firstHalf
            ? halfSplit - lab.tf
            : PERIODS - lab.tf;
        if (startMax < startMin) {
            return false;  // no valid start in that half
        }

        // shuffle possible starts
        List<Integer> starts = new ArrayList<>();
        for (int p = startMin; p <= startMax; p++) {
            starts.add(p);
        }
        Collections.shuffle(starts);

        for (int p : starts) {
            boolean ok = true;
            for (int k = 0; k < lab.tf; k++) {
                if (!tt[day][p + k].equals("—")) {
                    ok = false;
                    break;
                }
            }
            if (ok) {
                for (int k = 0; k < lab.tf; k++) {
                    tt[day][p + k] = lab.name;
                }
                return true;
            }
        }
        return false;
    }
}