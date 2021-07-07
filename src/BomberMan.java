import javax.swing.*;
import java.awt.*;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.Random;
class Bomb {
    int x, y;
    boolean exploded;
    int countToExplode, intervalToExplode = 4;
}

public class BomberMan extends JPanel implements Runnable, KeyListener {

    boolean isRunning;
    Thread thread;
    BufferedImage view, concreteTile, blockTile, player;

    Bomb bomb;
    int[][] scene;
    int playerX, playerY;
    int tileSize = 16, rows = 13, columns = 15;
    int speed = 4;
    boolean right, left, up, down;
    boolean moving;
    int framePlayer = 0, intervalPlayer = 5, indexAnimPlayer = 0;
    BufferedImage[] playerAnimUp, playerAnimDown, playerAnimRight, playerAnimLeft;
    int frameBomb = 0, intervalBomb = 7, indexAnimBomb = 0;
    BufferedImage[] bombAnim;
    BufferedImage[] fontExplosion, rightExplosion, leftExplosion, upExplosion, downExplosion;
    int frameExplosion = 0, intervalExplosion = 3, indexAnimExplosion = 0;
    BufferedImage[] concreteExploding;
    int frameConcreteExploding = 0, intevalConcreteExploding = 4, indexConcreteExploding = 0;
    boolean concreteAnim = false;
    int bombX, bombY;

    final int SCALE = 3;
    final int WIDTH = (tileSize * SCALE) * columns;
    final int HEIGHT = (tileSize * SCALE) * rows;

    public BomberMan() {
        setPreferredSize(new Dimension(WIDTH, HEIGHT));
        addKeyListener(this);
    }

    public static void main(String[] args) {
        JFrame w = new JFrame("Bomberman");
        w.setResizable(false);
        w.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        w.add(new BomberMan());
        w.pack();
        w.setLocationRelativeTo(null);
        w.setVisible(true);
    }

    @Override
    public void addNotify() {
        super.addNotify();
        if (thread == null) {
            thread = new Thread(this);
            isRunning = true;
            thread.start();
        }
    }

    public boolean isFree(int nextX, int nextY) {
        int size = SCALE * tileSize;

        int nextX_1 = nextX / size;
        int nextY_1 = nextY / size;

        int nextX_2 = (nextX + size - 1) / size;
        int nextY_2 = nextY / size;

        int nextX_3 = nextX / size;
        int nextY_3 = (nextY + size - 1) / size;

        int nextX_4 = (nextX + size - 1) / size;
        int nextY_4 = (nextY + size - 1) / size;

        return !((scene[nextY_1][nextX_1] == 1 || scene[nextY_1][nextX_1] == 2) ||
                (scene[nextY_2][nextX_2] == 1 || scene[nextY_2][nextX_2] == 2) ||
                (scene[nextY_3][nextX_3] == 1 || scene[nextY_3][nextX_3] == 2) ||
                (scene[nextY_4][nextX_4] == 1 || scene[nextY_4][nextX_4] == 2));
    }

    public void start() {
        try {
            view = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_RGB);

            BufferedImage spriteSheet = ImageIO.read(getClass().getResource("/sheets.png"));

            concreteTile = spriteSheet.getSubimage(4 * tileSize, 3 * tileSize, tileSize, tileSize);
            blockTile = spriteSheet.getSubimage(3 * tileSize, 3 * tileSize, tileSize, tileSize);
            player = spriteSheet.getSubimage(4 * tileSize, 0, tileSize, tileSize);

            playerAnimUp = new BufferedImage[3];
            playerAnimDown = new BufferedImage[3];
            playerAnimRight = new BufferedImage[3];
            playerAnimLeft = new BufferedImage[3];
            bombAnim = new BufferedImage[3];
            fontExplosion = new BufferedImage[4];
            rightExplosion = new BufferedImage[4];
            leftExplosion = new BufferedImage[4];
            upExplosion = new BufferedImage[4];
            downExplosion = new BufferedImage[4];
            concreteExploding = new BufferedImage[6];

            for (int i = 0; i < 6; i++) {
                concreteExploding[i] = spriteSheet.getSubimage((i + 5) * tileSize, 3 * tileSize, tileSize, tileSize);
            }

            fontExplosion[0] = spriteSheet.getSubimage(2 * tileSize, 6 * tileSize, tileSize, tileSize);
            fontExplosion[1] = spriteSheet.getSubimage(7 * tileSize, 6 * tileSize, tileSize, tileSize);
            fontExplosion[2] = spriteSheet.getSubimage(2 * tileSize, 11 * tileSize, tileSize, tileSize);
            fontExplosion[3] = spriteSheet.getSubimage(7 * tileSize, 11 * tileSize, tileSize, tileSize);

            rightExplosion[0] = spriteSheet.getSubimage(4 * tileSize, 6 * tileSize, tileSize, tileSize);
            rightExplosion[1] = spriteSheet.getSubimage(9 * tileSize, 6 * tileSize, tileSize, tileSize);
            rightExplosion[2] = spriteSheet.getSubimage(4 * tileSize, 11 * tileSize, tileSize, tileSize);
            rightExplosion[3] = spriteSheet.getSubimage(9 * tileSize, 11 * tileSize, tileSize, tileSize);

            leftExplosion[0] = spriteSheet.getSubimage(0, 6 * tileSize, tileSize, tileSize);
            leftExplosion[1] = spriteSheet.getSubimage(5 * tileSize, 6 * tileSize, tileSize, tileSize);
            leftExplosion[2] = spriteSheet.getSubimage(0, 11 * tileSize, tileSize, tileSize);
            leftExplosion[3] = spriteSheet.getSubimage(5 * tileSize, 11 * tileSize, tileSize, tileSize);

            upExplosion[0] = spriteSheet.getSubimage(2 * tileSize, 4 * tileSize, tileSize, tileSize);
            upExplosion[1] = spriteSheet.getSubimage(7 * tileSize, 4 * tileSize, tileSize, tileSize);
            upExplosion[2] = spriteSheet.getSubimage(2 * tileSize, 9 * tileSize, tileSize, tileSize);
            upExplosion[3] = spriteSheet.getSubimage(7 * tileSize, 9 * tileSize, tileSize, tileSize);

            downExplosion[0] = spriteSheet.getSubimage(2 * tileSize, 8 * tileSize, tileSize, tileSize);
            downExplosion[1] = spriteSheet.getSubimage(7 * tileSize, 8 * tileSize, tileSize, tileSize);
            downExplosion[2] = spriteSheet.getSubimage(2 * tileSize, 13 * tileSize, tileSize, tileSize);
            downExplosion[3] = spriteSheet.getSubimage(7 * tileSize, 13 * tileSize, tileSize, tileSize);

            for (int i = 0; i < 3; i++) {
                playerAnimLeft[i] = spriteSheet.getSubimage(i * tileSize, 0, tileSize, tileSize);
                playerAnimRight[i] = spriteSheet.getSubimage(i * tileSize, tileSize, tileSize, tileSize);
                playerAnimDown[i] = spriteSheet.getSubimage((i + 3) * tileSize, 0, tileSize, tileSize);
                playerAnimUp[i] = spriteSheet.getSubimage((i + 3) * tileSize, tileSize, tileSize, tileSize);
                bombAnim[i] = spriteSheet.getSubimage(i * tileSize, 3 * tileSize, tileSize, tileSize);
            }

            scene = new int[][]{
                    {1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1},
                    {1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1},
                    {1, 0, 1, 0, 1, 0, 1, 0, 1, 0, 1, 0, 1, 0, 1},
                    {1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1},
                    {1, 0, 1, 0, 1, 0, 1, 0, 1, 0, 1, 0, 1, 0, 1},
                    {1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1},
                    {1, 0, 1, 0, 1, 0, 1, 0, 1, 0, 1, 0, 1, 0, 1},
                    {1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1},
                    {1, 0, 1, 0, 1, 0, 1, 0, 1, 0, 1, 0, 1, 0, 1},
                    {1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1},
                    {1, 0, 1, 0, 1, 0, 1, 0, 1, 0, 1, 0, 1, 0, 1},
                    {1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1},
                    {1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1}
            };

            for (int i = 0; i < rows; i++) {
                for (int j = 0; j < columns; j++) {
                    if (scene[i][j] == 0) {
                        if (new Random().nextInt(10) < 5) {
                            scene[i][j] = 2;
                        }
                    }
                }
            }
            scene[1][1] = 0;
            scene[2][1] = 0;
            scene[1][2] = 0;

            playerX = (tileSize * SCALE);
            playerY = (tileSize * SCALE);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void update() {
        moving = false;
        if (right && isFree(playerX + speed, playerY)) {
            playerX += speed;
            moving = true;
        }
        if (left && isFree(playerX - speed, playerY)) {
            playerX -= speed;
            moving = true;
        }
        if (up && isFree(playerX, playerY - speed)) {
            playerY -= speed;
            moving = true;
        }
        if (down && isFree(playerX, playerY + speed)) {
            playerY += speed;
            moving = true;
        }

        if (bomb != null) {
            frameBomb++;
            if (frameBomb == intervalBomb) {
                frameBomb = 0;
                indexAnimBomb++;
                if (indexAnimBomb > 2) {
                    indexAnimBomb = 0;
                    bomb.countToExplode++;
                }
                if (bomb.countToExplode >= bomb.intervalToExplode) {
                    concreteAnim = true;
                    bombX = bomb.x;
                    bombY = bomb.y;
                    bomb.exploded = true;
                    if (scene[bomb.y + 1][bomb.x] == 2) {
                        scene[bomb.y + 1][bomb.x] = -1;
                    }
                    if (scene[bomb.y - 1][bomb.x] == 2) {
                        scene[bomb.y - 1][bomb.x] = -1;
                    }
                    if (scene[bomb.y][bomb.x + 1] == 2) {
                        scene[bomb.y][bomb.x + 1] = -1;
                    }
                    if (scene[bomb.y][bomb.x - 1] == 2) {
                        scene[bomb.y][bomb.x - 1] = -1;
                    }
                }
            }

            if(bomb.exploded) {
                frameExplosion++;
                if (frameExplosion == intervalExplosion) {
                    frameExplosion = 0;
                    indexAnimExplosion++;
                    if (indexAnimExplosion == 4) {
                        indexAnimExplosion = 0;
                        scene[bomb.y][bomb.x] = 0;
                        bomb = null;
                    }
                }
            }
        }

        if (concreteAnim) {
            frameConcreteExploding++;
            if (frameConcreteExploding == intevalConcreteExploding) {
                frameConcreteExploding = 0;
                indexConcreteExploding++;
                if (indexConcreteExploding == 5) {
                    indexConcreteExploding = 0;
                    if (scene[bombY + 1][bombX] == -1) {
                        scene[bombY + 1][bombX] = 0;
                    }
                    if (scene[bombY - 1][bombX] == -1) {
                        scene[bombY - 1][bombX] = 0;
                    }
                    if (scene[bombY][bombX + 1] == -1) {
                        scene[bombY][bombX + 1] = 0;
                    }
                    if (scene[bombY][bombX - 1] == -1) {
                        scene[bombY][bombX - 1] = 0;
                    }
                    concreteAnim = false;
                }
            }
        }

        if (moving) {
            framePlayer++;
            if (framePlayer > intervalPlayer) {
                framePlayer = 0;
                indexAnimPlayer++;
                if (indexAnimPlayer > 2) {
                    indexAnimPlayer = 0;
                }
            }

            if (right) {
                player = playerAnimRight[indexAnimPlayer];
            } else if (left) {
                player = playerAnimLeft[indexAnimPlayer];
            } else if (up) {
                player = playerAnimUp[indexAnimPlayer];
            } else if (down) {
                player = playerAnimDown[indexAnimPlayer];
            }
        } else {
            player = playerAnimDown[1];
        }
    }

    public void draw() {
        Graphics2D g2 = (Graphics2D) view.getGraphics();
        g2.setColor(new Color(56, 135, 0));
        g2.fillRect(0, 0, WIDTH, HEIGHT);

        int size = tileSize * SCALE;
        for (int i = 0; i < columns; i++) {
            for (int j = 0; j < rows; j++) {
                if (scene[j][i] == 1) {
                    g2.drawImage(blockTile, i * size, j * size, size, size, null);
                } else if (scene[j][i] == 2) {
                    g2.drawImage(concreteTile, i * size, j * size, size, size, null);
                } else if (scene[j][i] == 3) {
                    if (bomb != null) {
                        if (bomb.exploded) {
                            g2.drawImage(fontExplosion[indexAnimExplosion], bomb.x * size, bomb.y * size, size, size, null);
                            if (scene[bomb.y][bomb.x + 1] == 0) {
                                g2.drawImage(rightExplosion[indexAnimExplosion], (bomb.x + 1) * size, bomb.y * size, size, size, null);
                            }
                            if (scene[bomb.y][bomb.x - 1] == 0) {
                                g2.drawImage(leftExplosion[indexAnimExplosion], (bomb.x - 1) * size, bomb.y * size, size, size, null);
                            }
                            if (scene[bomb.y - 1][bomb.x] == 0) {
                                g2.drawImage(upExplosion[indexAnimExplosion], bomb.x * size, (bomb.y - 1) * size, size, size, null);
                            }
                            if (scene[bomb.y + 1][bomb.x] == 0) {
                                g2.drawImage(downExplosion[indexAnimExplosion], bomb.x * size, (bomb.y + 1) * size, size, size, null);
                            }
                        } else {
                            g2.drawImage(bombAnim[indexAnimBomb], i * size, j * size, size, size, null);
                        }
                    }
                }  else if (scene[j][i] == -1) {
                    g2.drawImage(concreteExploding[indexConcreteExploding], i * size, j * size, size, size, null);
                }
            }
        }

        g2.drawImage(player, playerX, playerY, size, size, null);

        Graphics g = getGraphics();
        g.drawImage(view, 0, 0, WIDTH, HEIGHT, null);
        g.dispose();
    }

    @Override
    public void run() {
        try {
            requestFocus();
            start();
            while (isRunning) {
                update();
                draw();
                Thread.sleep(1000 / 60);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void keyTyped(KeyEvent e) {

    }

    @Override
    public void keyPressed(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_SPACE) {
            if (bomb == null) {
                bomb = new Bomb();
                bomb.x = (playerX + ((SCALE * tileSize) / 2)) / (SCALE * tileSize);
                bomb.y = (playerY + ((SCALE * tileSize) / 2)) / (SCALE * tileSize);
                scene[bomb.y][bomb.x] = 3;
            }
        }
        if (e.getKeyCode() == KeyEvent.VK_RIGHT) {
            right = true;
        }
        if (e.getKeyCode() == KeyEvent.VK_LEFT) {
            left = true;
        }
        if (e.getKeyCode() == KeyEvent.VK_UP) {
            up = true;
        }
        if (e.getKeyCode() == KeyEvent.VK_DOWN) {
            down = true;
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_RIGHT) {
            right = false;
        }
        if (e.getKeyCode() == KeyEvent.VK_LEFT) {
            left = false;
        }
        if (e.getKeyCode() == KeyEvent.VK_UP) {
            up = false;
        }
        if (e.getKeyCode() == KeyEvent.VK_DOWN) {
            down = false;
        }
    }
}