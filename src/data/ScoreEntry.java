package data;

/**
 * ScoreEntry
 * ----------
 * Immutable data record for one high-score row.
 * Fields match the CSV columns: name, score, date, time.
 */
public record ScoreEntry(String name, int score, String date, String time) {}
