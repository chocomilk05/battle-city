package data;
import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.stream.*;

/**
 * ScoreEntry
 * ----------
 * Immutable data record for one high-score row.
 * Fields match the CSV columns: name, score, date, time.
 */
record ScoreEntry(String name, int score, String date, String time) {}

/**
 * ScoreManager
 * ------------
 * Handles all CSV read/write operations for the high-score list.
 *
 * File format  (scores.csv):
 *   name,score,date,time
 *   Alice,12000,2026-06-01,14:32:10
 *   Bob,9500,2026-06-01,15:00:22
 *   ...
 *
 * The file is created automatically in the working directory if it
 * does not yet exist. All methods are static – no instance needed.
 */
public class ScoreManager {

    // ── File location ─────────────────────────────────────────────────────────
    public static final String CSV_FILE = "scores.csv";
    private static final String HEADER  = "name,score,date,time";

    // ── Save a new score ──────────────────────────────────────────────────────

    /**
     * Append a new score entry to the CSV file.
     * The file is created with a header row if it doesn't yet exist.
     *
     * @param entry the score to persist
     */
    public static void save(ScoreEntry entry) {
        try {
            Path path = Path.of(CSV_FILE);
            boolean exists = Files.exists(path);

            try (PrintWriter pw = new PrintWriter(
                    new FileWriter(CSV_FILE, /*append=*/ true))) {
                if (!exists) pw.println(HEADER);
                pw.println(csvLine(entry));
            }
        } catch (IOException ex) {
            System.err.println("ScoreManager: could not write score – " + ex.getMessage());
        }
    }

    // ── Load top-N scores ─────────────────────────────────────────────────────

    /**
     * Load all scores, sort descending by score, return the top {@code n}.
     *
     * @param n maximum number of entries to return
     * @return sorted list (may be shorter than n if fewer entries exist)
     */
    public static List<ScoreEntry> loadTop(int n) {
        List<ScoreEntry> all = loadAll();
        return all.stream()
                  .sorted(Comparator.comparingInt(ScoreEntry::score).reversed())
                  .limit(n)
                  .collect(Collectors.toList());
    }

    /**
     * Load every score entry from the CSV file.
     *
     * @return list of all entries; empty list if file doesn't exist or is corrupt
     */
    public static List<ScoreEntry> loadAll() {
        List<ScoreEntry> list = new ArrayList<>();
        Path path = Path.of(CSV_FILE);
        if (!Files.exists(path)) return list;

        try (BufferedReader br = new BufferedReader(new FileReader(CSV_FILE))) {
            String line;
            boolean first = true;
            while ((line = br.readLine()) != null) {
                if (first) { first = false; continue; } // skip header
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

    // ── Clear all scores ──────────────────────────────────────────────────────

    /**
     * Delete the scores file (used by the HighScoreScreen "Clear All" button).
     */
    public static void clearAll() {
        try {
            Files.deleteIfExists(Path.of(CSV_FILE));
        } catch (IOException ex) {
            System.err.println("ScoreManager: could not clear scores – " + ex.getMessage());
        }
    }

    // ── CSV helpers ───────────────────────────────────────────────────────────

    private static String csvLine(ScoreEntry e) {
        // Sanitise name: remove commas so CSV stays well-formed
        String safeName = e.name().replace(",", " ");
        return String.join(",", safeName, String.valueOf(e.score()), e.date(), e.time());
    }

    /**
     * Parse one CSV line into a ScoreEntry.
     *
     * @return null if the line is malformed
     */
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