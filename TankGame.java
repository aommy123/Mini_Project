import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Random;

class TankGame extends JPanel implements ActionListener, KeyListener {
    private Timer timer;
    private Tank player;
    private ArrayList<Bullet> bullets;
    private ArrayList<Enemy> enemies;
    private Random random;
    private int level = 1;
    private int enemySpawnCounter = 0;
    private int score = 0;
    private int highScore = 0;

    public TankGame() {
        this.setPreferredSize(new Dimension(800, 600));
        this.setBackground(Color.BLACK);
        this.setFocusable(true);
        this.addKeyListener(this);

        player = new Tank(400, 500);
        bullets = new ArrayList<Bullet>();
        enemies = new ArrayList<Enemy>();
        random = new Random();

        loadHighScore();

        timer = new Timer(30, this);
        timer.start();
    }

    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        player.draw(g);
        for (Bullet b : bullets) {
            b.draw(g);
        }
        for (Enemy e : enemies) {
            e.draw(g);
        }
        
        g.setColor(Color.WHITE);
        g.drawString("HP: " + player.getHealth(), 10, 20);
        g.drawString("Score: " + score, 10, 40);
        g.drawString("Level: " + level, 10, 60);
        g.drawString("High Score: " + highScore, 10, 80);
    }

    public void actionPerformed(ActionEvent e) {
        player.move();
        for (Bullet b : bullets) {
            b.move();
        }
        for (Enemy enemy : enemies) {
            enemy.move(level); // ปรับความเร็วศัตรูตามเลเวล
        }

        checkCollisions();
        spawnEnemies();
        levelUp();
        repaint();
    }

    private void checkCollisions() {
        Iterator<Bullet> bulletIterator = bullets.iterator();
        while (bulletIterator.hasNext()) {
            Bullet bullet = bulletIterator.next();

            Iterator<Enemy> enemyIterator = enemies.iterator();
            while (enemyIterator.hasNext()) {
                Enemy enemy = enemyIterator.next();
                if (bullet.getBounds().intersects(enemy.getBounds())) {
                    enemyIterator.remove();
                    bulletIterator.remove();
                    score += 100;

                    if (score % 500 == 0) {
                        player.addHealth();
                    }
                    break;
                }
            }
        }

        Iterator<Enemy> enemyIterator = enemies.iterator();
        while (enemyIterator.hasNext()) {
            Enemy enemy = enemyIterator.next();
            if (player.getBounds().intersects(enemy.getBounds())) {
                enemyIterator.remove();
                player.decreaseHealth();
                if (player.getHealth() <= 0) {
                    gameOver();
                }
            }
        }
    }

    private void spawnEnemies() {
        if (++enemySpawnCounter % Math.max(20, (50 - level * 2)) == 0) {
            enemies.add(new Enemy(random.nextInt(750), 50));
        }
    }

    private void levelUp() {
        if (score / 1000 >= level) {
            level++;
        }
    }

    private void gameOver() {
        timer.stop();
        if (score > highScore) {
            highScore = score;
            saveHighScore();
        }
        JOptionPane.showMessageDialog(this, "Game Over!\nScore: " + score + "\nHigh Score: " + highScore, "Game Over", JOptionPane.INFORMATION_MESSAGE);
        System.exit(0);
    }

    private void loadHighScore() {
        try {
            File file = new File("highscore.txt");
            if (file.exists()) {
                BufferedReader reader = new BufferedReader(new FileReader(file));
                highScore = Integer.parseInt(reader.readLine());
                reader.close();
            }
        } catch (Exception e) {
            highScore = 0;
        }
    }

    private void saveHighScore() {
        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter("highscore.txt"));
            writer.write(String.valueOf(highScore));
            writer.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void keyPressed(KeyEvent e) {
        int key = e.getKeyCode();
        if (key == KeyEvent.VK_LEFT) {
            player.setDirection(-1);
        } else if (key == KeyEvent.VK_RIGHT) {
            player.setDirection(1);
        } else if (key == KeyEvent.VK_SPACE) {
            bullets.add(new Bullet(player.getX(), player.getY(), -5));
        }
    }

    public void keyReleased(KeyEvent e) {
        int key = e.getKeyCode();
        if (key == KeyEvent.VK_LEFT || key == KeyEvent.VK_RIGHT) {
            player.setDirection(0);
        }
    }

    public void keyTyped(KeyEvent e) {}

    public static void main(String[] args) {
        JFrame frame = new JFrame("Tank Shooter");
        TankGame game = new TankGame();
        frame.add(game);
        frame.pack();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);
    }
}

class Tank {
    private int x, y, direction, health;
    private final int MAX_HEALTH = 5; // กำหนดพลังชีวิตสูงสุดเป็น 5
    
    public Tank(int x, int y) {
        this.x = x;
        this.y = y;
        this.direction = 0;
        this.health = 3; // เริ่มต้นที่ 3
    }

    public void move() {
        x += direction * 5;
    }

    public void draw(Graphics g) {
        g.setColor(Color.GREEN);
        g.fillRect(x, y, 40, 40);
    }

    public void setDirection(int dir) {
        this.direction = dir;
    }

    public int getX() { return x; }
    public int getY() { return y; }
    
    public int getHealth() { return health; }

    public void addHealth() { 
        if (health < MAX_HEALTH) { // ไม่ให้เกิน 5
            health++; 
        }
    }

    public void decreaseHealth() { 
        health--; 
    }

    public Rectangle getBounds() { 
        return new Rectangle(x, y, 40, 40); 
    }
}

class Bullet {
    private int x, y, speed;
    
    public Bullet(int x, int y, int speed) {
        this.x = x;
        this.y = y;
        this.speed = speed;
    }

    public void move() {
        y += speed;
    }

    public void draw(Graphics g) {
        g.setColor(Color.RED);
        g.fillRect(x, y, 5, 10);
    }

    public Rectangle getBounds() { return new Rectangle(x, y, 5, 10); }
}

class Enemy {
    private int x, y;
    
    public Enemy(int x, int y) {
        this.x = x;
        this.y = y;
    }
    
    public void move(int level) {
        y += Math.min(4, 1 + level / 2); // เพิ่มความเร็วตามเลเวล (สูงสุด 4)
    }
    
    public void draw(Graphics g) {
        g.setColor(Color.RED);
        g.fillRect(x, y, 40, 40);
    }
    
    public Rectangle getBounds() { return new Rectangle(x, y, 40, 40); }
}
