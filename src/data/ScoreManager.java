package data;
import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.stream.*;

public class ScoreManager {

    public static final String CSV_FILE = "scores.csv";
    private static final String HEADER  = "name,score,date,time";

    public static void save(ScoreEntry entry) {
        try {
            Path path = Path.of(CSV_FILE);
            boolean exists = Files.exists(path);

            try (PrintWriter pw = new PrintWriter(
                    new FileWriter(CSV_FILE, true))) {
                if (!exists) pw.println(HEADER);
                pw.println(csvLine(entry));
            }
        } catch (IOException ex) {
            System.err.println("ScoreManager: could not write score – " + ex.getMessage());
        }
    }

    public static List<ScoreEntry> loadTop(int n) {
        List<ScoreEntry> all = loadAll();
        return all.stream()
                  .sorted(Comparator.comparingInt(ScoreEntry::score).reversed())
                  .limit(n)
                  .collect(Collectors.toList());
    }

    public static List<ScoreEntry> loadAll() {
        List<ScoreEntry> list = new ArrayList<>();
        Path path = Path.of(CSV_FILE);
        if (!Files.exists(path)) return list;

        try (BufferedReader br = new BufferedReader(new FileReader(CSV_FILE))) {
            String line;
            boolean first = true;
            while ((line = br.readLine()) != null) {
                if (first) { first = false; continue; }
                line = line.trim();
                if (line.isEmpty()) continue;
                ScoreEntry e = parseLine(line);
                if (e != null) list.add(e);
            }
        } catch (IOException ex) {
            System.err.println("ScoreManager: could not read scores – " + ex.getMessage());
        }
        return list;
    }

    public static void clearAll() {
        try {
            Files.deleteIfExists(Path.of(CSV_FILE));
        } catch (IOException ex) {
            System.err.println("ScoreManager: could not clear scores – " + ex.getMessage());
        }
    }

    private static String csvLine(ScoreEntry e) {
        String safeName = e.name().replace(",", " ");
        return String.join(",", safeName, String.valueOf(e.score()), e.date(), e.time());
    }

    private static ScoreEntry parseLine(String line) {
        try {
            String[] parts = line.split(",", 4);
            if (parts.length < 4) return null;
            String name  = parts[0].trim();
            int    score = Integer.parseInt(parts[1].trim());
            String date  = parts[2].trim();
            String time  = parts[3].trim();
            return new ScoreEntry(name, score, date, time);
        } catch (NumberFormatException ex) {
            return null;
        }
    }
}