import java.time.LocalDate;

public class Main {
    public static void main (String[] args) {
        XDateRange<LocalDate> tree = new XDateRange<>();
        tree.insert(LocalDate.parse("2019-01-01"), LocalDate.parse("2019-01-03"));
        tree.insert(LocalDate.parse("2019-01-01"), LocalDate.parse("2019-01-06"));
        tree.insert(LocalDate.parse("2019-01-02"), LocalDate.parse("2019-01-04"));
        tree.insert(LocalDate.parse("2019-01-02"), LocalDate.parse("2019-01-03"));
        tree.insert(LocalDate.parse("2019-01-05"), LocalDate.parse("2019-01-09"));
        tree.insert(LocalDate.parse("2019-01-06"), LocalDate.parse("2019-01-10"));
        tree.insert(LocalDate.parse("2019-01-07"), LocalDate.parse("2019-01-08"));

        System.out.println("Tree size: " + tree.size());

        LocalDate from = LocalDate.parse("2019-01-03");
        LocalDate to = LocalDate.parse("2019-01-07");

        System.out.println();
        System.out.println("Range: [" + from + " -> " + to + "]");
        System.out.println("Active tickets in range: ");
        tree.printTickets(from, to);
        System.out.println();

        System.out.println("MaxOverlaps: " + tree.getMaxOverlappingIntervals(from, to));
    }
}