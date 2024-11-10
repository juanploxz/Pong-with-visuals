import javax.swing.*;
import javax.swing.plaf.basic.BasicSliderUI;
import java.awt.*;
import java.awt.event.*;
import javax.sound.sampled.*;
import java.io.File;
import java.io.IOException;

public class Poronga extends JPanel implements ActionListener {
    public Timer timer;
    private static final int DELAY = 10; // delay in milliseconds
    private int panelWidth;
    private int panelHeight;
    private int ballX = 100, ballY = 100;
    private int ballXDir = 6, ballYDir = 6;
    private int paddle1Y = 100, paddle2Y = 100;
    private int paddleSpeed = 8; // Increased paddle speed
    private boolean wPressed = false, sPressed = false, upPressed = false, downPressed = false;
    private Image paddle1Image, paddle2Image, backgroundImage, mainMenuBackgroundImage;
    private static final int PADDLE_WIDTH = 30; // Increased paddle width
    private static final int PADDLE_HEIGHT = 150; // Increased paddle height
    private Clip clip, backgroundMusicClip, pauseMusicClip, resetClip;
    private JPanel pausePanel, mainMenuPanel;
    private FloatControl backgroundMusicVolumeControl;

    public Poronga(int width, int height) {
        this.panelWidth = width;
        this.panelHeight = height;
        setPreferredSize(new Dimension(panelWidth, panelHeight));
        setLayout(null); // Use null layout to position panels
        timer = new Timer(DELAY, this);
        setFocusable(true);
        addKeyListener(new KeyAdapter() {
            public void keyPressed(KeyEvent e) {
                int key = e.getKeyCode();
                if (key == KeyEvent.VK_W) {
                    wPressed = true;
                }
                if (key == KeyEvent.VK_S) {
                    sPressed = true;
                }
                if (key == KeyEvent.VK_UP) {
                    upPressed = true;
                }
                if (key == KeyEvent.VK_DOWN) {
                    downPressed = true;
                }
                if (key == KeyEvent.VK_ESCAPE) {
                    timer.stop();
                    backgroundMusicClip.stop();
                    playPauseMusic();
                    showPausePanel();
                }
            }

            public void keyReleased(KeyEvent e) {
                int key = e.getKeyCode();
                if (key == KeyEvent.VK_W) {
                    wPressed = false;
                }
                if (key == KeyEvent.VK_S) {
                    sPressed = false;
                }
                if (key == KeyEvent.VK_UP) {
                    upPressed = false;
                }
                if (key == KeyEvent.VK_DOWN) {
                    downPressed = false;
                }
            }
        });

        // Load and scale paddle images
        paddle1Image = new ImageIcon("paddle1.png").getImage().getScaledInstance(PADDLE_WIDTH, PADDLE_HEIGHT, Image.SCALE_SMOOTH);
        paddle2Image = new ImageIcon("paddle2.png").getImage().getScaledInstance(PADDLE_WIDTH, PADDLE_HEIGHT, Image.SCALE_SMOOTH);
        
        // Load background image
        backgroundImage = new ImageIcon("background.png").getImage().getScaledInstance(panelWidth, panelHeight, Image.SCALE_SMOOTH);

        // Load main menu background image
        mainMenuBackgroundImage = new ImageIcon("main_menu_background.png").getImage().getScaledInstance(panelWidth, panelHeight, Image.SCALE_SMOOTH);

        // Load sound
        try {
            AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(new File("bounce.wav").getAbsoluteFile());
            clip = AudioSystem.getClip();
            clip.open(audioInputStream);
            // Set volume to a lower level
            FloatControl volume = (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);
            volume.setValue(-10.0f); // Reduce volume by 10 decibels
        } catch (UnsupportedAudioFileException | IOException | LineUnavailableException e) {
            e.printStackTrace();
        }

        // Load background music
        try {
            AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(new File("background_music.wav").getAbsoluteFile());
            backgroundMusicClip = AudioSystem.getClip();
            backgroundMusicClip.open(audioInputStream);
            backgroundMusicVolumeControl = (FloatControl) backgroundMusicClip.getControl(FloatControl.Type.MASTER_GAIN);
            backgroundMusicClip.loop(Clip.LOOP_CONTINUOUSLY); // Play music in loop
        } catch (UnsupportedAudioFileException | IOException | LineUnavailableException e) {
            e.printStackTrace();
        }

        // Load pause music
        try {
            AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(new File("pause_music.wav").getAbsoluteFile());
            pauseMusicClip = AudioSystem.getClip();
            pauseMusicClip.open(audioInputStream);
        } catch (UnsupportedAudioFileException | IOException | LineUnavailableException e) {
            e.printStackTrace();
        }

        // Load reset sound
        try {
            AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(new File("reset.wav").getAbsoluteFile());
            resetClip = AudioSystem.getClip();
            resetClip.open(audioInputStream);
        } catch (UnsupportedAudioFileException | IOException | LineUnavailableException e) {
            e.printStackTrace();
        }

        // Create pause panel
        pausePanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.5f)); // Set transparency to 50%
                g2d.setColor(Color.BLACK);
                g2d.fillRect(0, 0, getWidth(), getHeight());
                g2d.dispose();
            }
        };
        pausePanel.setSize(panelWidth, panelHeight);
        pausePanel.setLayout(new GridLayout(6, 1)); // Adjusted to 6 rows
        pausePanel.setOpaque(false); // Make the panel itself transparent

        JLabel pauseLabel = new JLabel("Game Paused", SwingConstants.CENTER);
        pauseLabel.setForeground(Color.WHITE);
        pauseLabel.setFont(new Font("Arial", Font.BOLD, 36)); // Set font size to 36
        pauseLabel.setOpaque(false); // Make the label transparent

        JButton continueButton = createSemiTransparentButton("Continue");
        continueButton.addActionListener(e -> {
            timer.start();
            backgroundMusicClip.loop(Clip.LOOP_CONTINUOUSLY); // Resume background music
            stopPauseMusic();
            hidePausePanel();
        });

        JButton mainMenuButton = createSemiTransparentButton("Main Menu");
        mainMenuButton.addActionListener(e -> {
            hidePausePanel();
            showMainMenuPanel();
        });

        JButton exitButton = createSemiTransparentButton("Exit");
        exitButton.addActionListener(e -> System.exit(0));

        // Create volume slider and label
        JLabel volumeLabel = new JLabel("Le vas a bajar a la música? No soporto", SwingConstants.CENTER);
        volumeLabel.setForeground(Color.WHITE);
        volumeLabel.setFont(new Font("Arial", Font.PLAIN, 24)); // Set font size to 14
        volumeLabel.setOpaque(false); // Make the label transparent

        JSlider volumeSlider = new JSlider(JSlider.HORIZONTAL, (int) backgroundMusicVolumeControl.getMinimum(), (int) backgroundMusicVolumeControl.getMaximum(), (int) backgroundMusicVolumeControl.getValue());
        volumeSlider.setPreferredSize(new Dimension(200, 20)); // Set preferred size
        volumeSlider.setMaximumSize(new Dimension(200, 20)); // Set maximum size
        volumeSlider.setOpaque(false); // Make the slider transparent
        volumeSlider.addChangeListener(e -> {
            backgroundMusicVolumeControl.setValue(volumeSlider.getValue());
        });

        // Customize the slider with images
        volumeSlider.setUI(new CustomSliderUI(volumeSlider));

        // Create a panel to hold the volume label and slider
        JPanel volumePanel = new JPanel();
        volumePanel.setLayout(new BoxLayout(volumePanel, BoxLayout.Y_AXIS));
        volumePanel.setOpaque(false); // Make the panel transparent
        volumeLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        volumeSlider.setAlignmentX(Component.CENTER_ALIGNMENT);
        volumePanel.add(volumeLabel);
        volumePanel.add(volumeSlider);

        pausePanel.add(pauseLabel);
        pausePanel.add(continueButton);
        pausePanel.add(mainMenuButton); // Add the new button
        pausePanel.add(exitButton);
        pausePanel.add(volumePanel); // Add the volume panel
        pausePanel.setVisible(false); // Initially hidden
        add(pausePanel);

        // Create main menu panel
        mainMenuPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                g.drawImage(mainMenuBackgroundImage, 0, 0, getWidth(), getHeight(), this);
            }
        };
        mainMenuPanel.setSize(panelWidth, panelHeight);
        mainMenuPanel.setLayout(new GridLayout(3, 1));

        JLabel titleLabel = new JLabel("Main Menu", SwingConstants.CENTER);
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 36)); // Set font size to 36

        JButton startButton = createSemiTransparentButton("Start Game");
        startButton.addActionListener(e -> {
            timer.start();
            backgroundMusicClip.loop(Clip.LOOP_CONTINUOUSLY); // Start background music
            hideMainMenuPanel();
        });

        JButton exitMainButton = createSemiTransparentButton("Exit");
        exitMainButton.addActionListener(e -> System.exit(0));

        mainMenuPanel.add(titleLabel);
        mainMenuPanel.add(startButton);
        mainMenuPanel.add(exitMainButton);
        add(mainMenuPanel);
    }

    private JButton createSemiTransparentButton(String text) {
        JButton button = new JButton(text);
        button.setBackground(new Color(0, 0, 0, 150)); // Semi-transparent black
        button.setForeground(Color.WHITE);
        button.setOpaque(false);
        button.setContentAreaFilled(false);
        button.setBorderPainted(false);
        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                button.setBackground(new Color(0, 0, 0, 200)); // Slightly less transparent on hover
            }

            @Override
            public void mouseExited(MouseEvent e) {
                button.setBackground(new Color(0, 0, 0, 150)); // Original transparency
            }
        });
        return button;
    }

    private void playSound() {
        if (clip != null) {
            clip.setFramePosition(0); // Rewind to the beginning
            clip.start();
        }
    }

    private void playResetSound() {
        if (resetClip != null) {
            resetClip.setFramePosition(0); // Rewind to the beginning
            resetClip.start();
        }
    }

    private void playPauseMusic() {
        if (pauseMusicClip != null) {
            pauseMusicClip.setFramePosition(0); // Rewind to the beginning
            pauseMusicClip.loop(Clip.LOOP_CONTINUOUSLY); // Play music in loop
            System.out.println("Pause music started");
        } else {
            System.out.println("Pause music clip is null");
        }
    }

    private void stopPauseMusic() {
        if (pauseMusicClip != null) {
            pauseMusicClip.stop();
            System.out.println("Pause music stopped");
        } else {
            System.out.println("Pause music clip is null");
        }
    }

    private void showPausePanel() {
        pausePanel.setVisible(true);
    }

    private void hidePausePanel() {
        pausePanel.setVisible(false);
    }

    private void showMainMenuPanel() {
        mainMenuPanel.setVisible(true);
    }

    private void hideMainMenuPanel() {
        mainMenuPanel.setVisible(false);
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        // draw the background
        g.drawImage(backgroundImage, 0, 0, this);
        // draw the ball
        g.setColor(Color.WHITE);
        g.fillOval(ballX, ballY, 20, 20);
        // draw the paddles
        g.drawImage(paddle1Image, 20, paddle1Y, this);
        g.drawImage(paddle2Image, panelWidth - 40, paddle2Y, this);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (wPressed && paddle1Y > 0) {
            paddle1Y -= paddleSpeed;
        }
        if (sPressed && paddle1Y < panelHeight - PADDLE_HEIGHT) {
            paddle1Y += paddleSpeed;
        }
        if (upPressed && paddle2Y > 0) {
            paddle2Y -= paddleSpeed;
        }
        if (downPressed && paddle2Y < panelHeight - PADDLE_HEIGHT) {
            paddle2Y += paddleSpeed;
        }
        ballX += ballXDir;
        ballY += ballYDir;

        // Ball collision with top and bottom
        if (ballY <= 0 || ballY >= panelHeight - 20) {
            ballYDir = -ballYDir;
        }

        // Ball collision with left paddle
        if (ballX <= 40 && ballY >= paddle1Y && ballY <= paddle1Y + PADDLE_HEIGHT) {
            ballXDir = -ballXDir;
            playSound();
        }

        // Ball collision with right paddle
        if (ballX >= panelWidth - 60 && ballY >= paddle2Y && ballY <= paddle2Y + PADDLE_HEIGHT) {
            ballXDir = -ballXDir;
            playSound();
        }

        // Ball collision with left and right walls
        if (ballX <= 0 || ballX >= panelWidth - 20) {
            ballX = 100;
            ballY = 100;
            ballXDir = 6;
            ballYDir = 6;
            playResetSound(); // Play reset sound
        }

        repaint();
    }

    public static void main(String[] args) {
        // Get screen size
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        int screenWidth = screenSize.width;
        int screenHeight = screenSize.height;

        JFrame frame = new JFrame("Poronga");
        Poronga game = new Poronga(screenWidth, screenHeight);
        frame.add(game);
        frame.setSize(screenWidth, screenHeight);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);
    }

    // Custom slider UI class
    private static class CustomSliderUI extends BasicSliderUI {
        private final Image thumbImage;
        private final Image trackImage;

        public CustomSliderUI(JSlider slider) {
            super(slider);
            thumbImage = new ImageIcon("thumb.png").getImage();
            trackImage = new ImageIcon("track.png").getImage();
        }

        @Override
        public void paintThumb(Graphics g) {
            Graphics2D g2d = (Graphics2D) g.create();
            g2d.drawImage(thumbImage, thumbRect.x, thumbRect.y, thumbRect.width, thumbRect.height, null);
            g2d.dispose();
        }

        @Override
        public void paintTrack(Graphics g) {
            Graphics2D g2d = (Graphics2D) g.create();
            g2d.drawImage(trackImage, trackRect.x, trackRect.y, trackRect.width, trackRect.height, null);
            g2d.dispose();
        }
    }
}