package ui;
import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.util.prefs.Preferences;

/*
  OptionsScreen
  Modal dialog for game settings:
 */
public class OptionsScreen extends JDialog {

    // Preference keys
    public static final String PREF_DIFFICULTY   = "difficulty";
    public static final String PREF_SOUND        = "sound";
    public static final String PREF_MUSIC_VOL    = "musicVolume";
    public static final String PREF_PLAYER_NAME  = "playerName";

    public static final String DIFF_EASY   = "Easy";
    public static final String DIFF_NORMAL = "Normal";
    public static final String DIFF_HARD   = "Hard";

    // Persistence
    private static final Preferences PREFS =
        Preferences.userNodeForPackage(OptionsScreen.class);

    // Colours
    private static final Color BG       = new Color(12, 12, 12);
    private static final Color SECT_BG  = new Color(25, 10, 0);
    private static final Color ACCENT   = new Color(255, 140, 0);
    private static final Color FG       = new Color(220, 200, 160);
    private static final Color BORDER   = new Color(100, 40, 0);

    // Controls
    private JComboBox<String> diffCombo;
    private JCheckBox         soundCheck;
    private JSlider           musicSlider;
    private JTextField        nameField;

    public OptionsScreen(Frame owner) {
        super(owner, "⚙  Options", true);
        setBackground(BG);
        getContentPane().setBackground(BG);
        setLayout(new BorderLayout(0, 0));

        add(buildHeader(),  BorderLayout.NORTH);
        add(buildForm(),    BorderLayout.CENTER);
        add(buildButtons(), BorderLayout.SOUTH);

        setSize(460, 420);
        setLocationRelativeTo(owner);
        setResizable(false);
    }

    // Header

    private JPanel buildHeader() {
        JPanel p = new JPanel();
        p.setBackground(new Color(60, 20, 0));
        p.setBorder(BorderFactory.createEmptyBorder(12, 0, 12, 0));
        JLabel lbl = new JLabel("⚙  GAME OPTIONS");
        lbl.setFont(new Font("Monospaced", Font.BOLD, 18));
        lbl.setForeground(ACCENT);
        p.add(lbl);
        return p;
    }

    // Form

    private JPanel buildForm() {
        JPanel form = new JPanel();
        form.setBackground(BG);
        form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));
        form.setBorder(BorderFactory.createEmptyBorder(16, 24, 8, 24));

        // Player name
        form.add(sectionLabel("Player Name"));
        nameField = new JTextField(PREFS.get(PREF_PLAYER_NAME, "Player1"), 20);
        styleTextField(nameField);
        nameField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 32));
        form.add(nameField);
        form.add(Box.createVerticalStrut(14));

        // Difficulty
        form.add(sectionLabel("Difficulty"));
        diffCombo = new JComboBox<>(new String[]{DIFF_EASY, DIFF_NORMAL, DIFF_HARD});
        diffCombo.setSelectedItem(PREFS.get(PREF_DIFFICULTY, DIFF_NORMAL));
        styleCombo(diffCombo);
        diffCombo.setMaximumSize(new Dimension(Integer.MAX_VALUE, 32));
        form.add(diffCombo);

        JTextArea diffHint = hintArea(
            "Easy: slower enemies, more lives  |  " +
            "Normal: classic mode  |  " +
            "Hard: faster, tougher enemies");
        form.add(diffHint);
        form.add(Box.createVerticalStrut(14));

        // Sound effects
        form.add(sectionLabel("Audio"));
        soundCheck = new JCheckBox("Enable sound effects",
                                   PREFS.getBoolean(PREF_SOUND, true));
        soundCheck.setFont(new Font("Monospaced", Font.PLAIN, 13));
        soundCheck.setForeground(FG);
        soundCheck.setBackground(BG);
        soundCheck.setAlignmentX(Component.LEFT_ALIGNMENT);
        form.add(soundCheck);
        form.add(Box.createVerticalStrut(6));

        // Music volume
        JLabel volLbl = new JLabel("Music Volume");
        volLbl.setFont(new Font("Monospaced", Font.PLAIN, 12));
        volLbl.setForeground(new Color(160, 140, 100));
        volLbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        form.add(volLbl);

        musicSlider = new JSlider(0, 100, PREFS.getInt(PREF_MUSIC_VOL, 70));
        musicSlider.setBackground(BG);
        musicSlider.setForeground(ACCENT);
        musicSlider.setPaintTicks(true);
        musicSlider.setMajorTickSpacing(25);
        musicSlider.setPaintLabels(true);
        musicSlider.setAlignmentX(Component.LEFT_ALIGNMENT);
        form.add(musicSlider);
        form.add(Box.createVerticalStrut(14));

        // Controls reminder
        form.add(sectionLabel("Controls"));
        form.add(hintArea(
            "Move: W A S D  or  Arrow keys\n" +
            "Fire: SPACE\n" +
            "Pause/Resume: P  or  ESC"));

        return form;
    }

    // Buttons

    private JPanel buildButtons() {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.CENTER, 14, 10));
        p.setBackground(BG);
        p.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, BORDER));

        JButton save = styledButton("Save");
        save.addActionListener(e -> {
            PREFS.put(PREF_PLAYER_NAME,  nameField.getText().trim());
            PREFS.put(PREF_DIFFICULTY,   (String) diffCombo.getSelectedItem());
            PREFS.putBoolean(PREF_SOUND, soundCheck.isSelected());
            PREFS.putInt(PREF_MUSIC_VOL, musicSlider.getValue());
            JOptionPane.showMessageDialog(this,
                "Settings saved!", "Options", JOptionPane.INFORMATION_MESSAGE);
            dispose();
        });

        JButton cancel = styledButton("Cancel");
        cancel.addActionListener(e -> dispose());

        JButton defaults = styledButton("Defaults");
        defaults.addActionListener(e -> {
            nameField.setText("Player1");
            diffCombo.setSelectedItem(DIFF_NORMAL);
            soundCheck.setSelected(true);
            musicSlider.setValue(70);
        });

        p.add(save);
        p.add(cancel);
        p.add(defaults);
        return p;
    }

    // Static helpers

    public static String getSavedDifficulty() {
        return PREFS.get(PREF_DIFFICULTY, DIFF_NORMAL);
    }

    public static boolean isSoundEnabled() {
        return PREFS.getBoolean(PREF_SOUND, true);
    }

    public static int getMusicVolume() {
        return PREFS.getInt(PREF_MUSIC_VOL, 70);
    }

    public static String getPlayerName() {
        return PREFS.get(PREF_PLAYER_NAME, "Player1");
    }

    // Styling helpers

    private JLabel sectionLabel(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(new Font("Monospaced", Font.BOLD, 13));
        lbl.setForeground(ACCENT);
        lbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        lbl.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, BORDER));
        return lbl;
    }

    private JTextArea hintArea(String text) {
        JTextArea ta = new JTextArea(text);
        ta.setFont(new Font("Monospaced", Font.PLAIN, 11));
        ta.setForeground(new Color(130, 120, 90));
        ta.setBackground(BG);
        ta.setEditable(false);
        ta.setOpaque(false);
        ta.setAlignmentX(Component.LEFT_ALIGNMENT);
        ta.setBorder(BorderFactory.createEmptyBorder(2, 0, 4, 0));
        return ta;
    }

    private void styleTextField(JTextField tf) {
        tf.setFont(new Font("Monospaced", Font.PLAIN, 13));
        tf.setForeground(FG);
        tf.setBackground(SECT_BG);
        tf.setCaretColor(ACCENT);
        tf.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER, 1),
            BorderFactory.createEmptyBorder(4, 6, 4, 6)));
        tf.setAlignmentX(Component.LEFT_ALIGNMENT);
    }

    private void styleCombo(JComboBox<String> cb) {
        cb.setFont(new Font("Monospaced", Font.PLAIN, 13));
        cb.setForeground(FG);
        cb.setBackground(SECT_BG);
        cb.setAlignmentX(Component.LEFT_ALIGNMENT);
    }

    private JButton styledButton(String text) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Monospaced", Font.BOLD, 13));
        btn.setForeground(ACCENT);
        btn.setBackground(new Color(50, 15, 0));
        btn.setFocusPainted(false);
        btn.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER, 1),
            BorderFactory.createEmptyBorder(6, 20, 6, 20)));
        return btn;
    }
}