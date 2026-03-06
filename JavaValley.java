import java.awt.*;
import java.awt.event.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import javax.swing.*;

public class JavaValley extends JPanel implements ActionListener, KeyListener {
    enum GameState { MENU, PLAYING, SLEEPING }
    private GameState currentState = GameState.MENU;

    private final int TILE_SIZE = 48;
    private final int ROWS = 10;
    private final int COLS = 16;
    private final int SKY_HEIGHT = 120;

    private GameHashTable itemRegistry;
    private FarmTile[][] map;
    private Player player;
    
    private Map<String, Integer> storage = new HashMap<>();
    private Rectangle houseBounds; 
    
    private int dailyEarnings = 0;
    private java.util.List<Point> stars = new java.util.ArrayList<>();
    private Random rand = new Random();
    
    private Timer gameLoop;
    private Timer sleepTransitionTimer;
    
    private int day = 1;
    private boolean up, down, left, right;

    // Cores Premium para UI
    private final Color UI_WOOD = new Color(130, 80, 45);
    private final Color UI_WOOD_DARK = new Color(90, 50, 25);
    private final Color SLOT_BG = new Color(160, 110, 70);
    private final Color SLOT_SELECTED = new Color(255, 220, 80);

    public JavaValley() {
        setPreferredSize(new Dimension(COLS * TILE_SIZE, SKY_HEIGHT + (ROWS * TILE_SIZE))); // Altura ajustada
        setBackground(Color.BLACK);
        setFocusable(true);
        addKeyListener(this);

        initGame();

        for(int i=0; i<50; i++) stars.add(new Point(rand.nextInt(800), rand.nextInt(SKY_HEIGHT)));

        gameLoop = new Timer(1000 / 60, this);
        gameLoop.start();

        sleepTransitionTimer = new Timer(4000, e -> wakeUp()); 
        sleepTransitionTimer.setRepeats(false);
    }

    private void initGame() {
        storage.put("Abóbora", 0);
        storage.put("Berry Azul", 0);
        
        itemRegistry = new GameHashTable();
        itemRegistry.put("HOE", new Item("HOE", "Enxada", "TOOL", new Color(139, 69, 19), Color.LIGHT_GRAY, 0));
        itemRegistry.put("WATER", new Item("WATER", "Regador", "TOOL", new Color(70, 150, 220), Color.WHITE, 0));
        itemRegistry.put("SCYTHE", new Item("SCYTHE", "Foice", "TOOL", Color.LIGHT_GRAY, new Color(101, 67, 33), 0));
        itemRegistry.put("FENCE", new Item("FENCE", "Cerca", "BUILDING", new Color(120, 75, 40), new Color(150, 100, 60), 10));
        
        itemRegistry.put("S_PUMPKIN", new Item("S_PUMPKIN", "Sem. Abóbora", "SEED", new Color(220, 180, 120), new Color(150, 100, 50), 20));
        itemRegistry.put("S_BERRY", new Item("S_BERRY", "Sem. Berry", "SEED", new Color(200, 160, 200), new Color(150, 100, 150), 10));
        itemRegistry.put("S_TREE", new Item("S_TREE", "Muda de Árvore", "TREE_SEED", new Color(120, 75, 40), new Color(30, 120, 40), 50));
        
        itemRegistry.put("C_PUMPKIN", new Item("C_PUMPKIN", "Abóbora", "CROP", new Color(255, 140, 0), new Color(0, 100, 0), 60)); 
        itemRegistry.put("C_BERRY", new Item("C_BERRY", "Berry Azul", "CROP", new Color(30, 80, 220), new Color(34, 150, 34), 35));
        itemRegistry.put("C_TREE", new Item("C_TREE", "Carvalho", "TREE", new Color(120, 75, 40), new Color(25, 120, 35), 0));

        map = new FarmTile[COLS][ROWS];
        for (int c = 0; c < COLS; c++) { 
            for (int r = 0; r < ROWS; r++) {
                map[c][r] = new FarmTile(c, r); 
            }
        }
        
        map[0][0].isLake = true; map[1][0].isLake = true;
        map[0][1].isLake = true; map[1][1].isLake = true;
        map[2][0].isLake = true; map[2][1].isLake = true;

        player = new Player(150, SKY_HEIGHT + 100);
        
        player.inventory[0] = itemRegistry.get("HOE");
        player.inventory[1] = itemRegistry.get("WATER");
        player.inventory[2] = itemRegistry.get("SCYTHE");
        player.inventory[3] = itemRegistry.get("FENCE");
        player.inventory[4] = itemRegistry.get("S_TREE");
        player.inventory[5] = itemRegistry.get("S_PUMPKIN");
        player.inventory[6] = itemRegistry.get("S_BERRY");
        player.inventory[7] = null;
        
        int houseW = 3 * TILE_SIZE;
        int houseH = 3 * TILE_SIZE;
        houseBounds = new Rectangle((COLS * TILE_SIZE) - houseW - 10, SKY_HEIGHT + 10, houseW, houseH);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (currentState == GameState.PLAYING) updateGame();
        repaint();
    }

    private void updateGame() {
        int dx = 0, dy = 0;
        if (up) dy--; if (down) dy++; if (left) dx--; if (right) dx++;
        
        int nextX = player.x + dx * player.speed;
        int nextY = player.y + dy * player.speed;
        Rectangle nextBounds = new Rectangle(nextX, nextY, player.width, player.height);
        
        boolean canMoveX = nextX >= 0 && nextX < getWidth() - player.width;
        boolean canMoveY = nextY >= SKY_HEIGHT && nextY < getHeight() - player.height; // Removido limite extra da UI antiga

        if (nextBounds.intersects(houseBounds)) { canMoveX = false; canMoveY = false; }
        
        Point tileP = new Point((nextX + player.width/2)/TILE_SIZE, (nextY + player.height/2 + 10 - SKY_HEIGHT)/TILE_SIZE);
        if (tileP.x >= 0 && tileP.x < COLS && tileP.y >= 0 && tileP.y < ROWS) {
            if (map[tileP.x][tileP.y].isLake) { canMoveX = false; canMoveY = false; }
        }

        if (canMoveX) player.move(dx, 0);
        if (canMoveY) player.move(0, dy);
    }

    private void interact() {
        Item heldItem = player.getHeldItem();

        Point playerCenter = new Point(player.x + player.width/2, player.y + player.height/2);
        Point houseCenter = new Point(houseBounds.x + houseBounds.width/2, houseBounds.y + houseBounds.height/2);
        
        if (playerCenter.distance(houseCenter) < 150 && heldItem != null && heldItem.type.equals("CROP")) {
            String fruitName = heldItem.name;
            if (storage.containsKey(fruitName)) {
                storage.put(fruitName, storage.get(fruitName) + 1);
                player.inventory[player.selectedSlot] = null;
            }
            return; 
        }

        int adjustedPlayerY = player.y - SKY_HEIGHT;
        if (adjustedPlayerY < 0) return;

        Point p = new Point((player.x + player.width/2) / TILE_SIZE, (player.y + player.height/2 + 10 - SKY_HEIGHT) / TILE_SIZE);
        if (p.x < 0 || p.x >= COLS || p.y < 0 || p.y >= ROWS) return;
        Rectangle tileRect = new Rectangle(p.x * TILE_SIZE, p.y * TILE_SIZE + SKY_HEIGHT, TILE_SIZE, TILE_SIZE);
        if (tileRect.intersects(houseBounds)) return;

        FarmTile tile = map[p.x][p.y];

        if (heldItem == null) {
            if (tile.crop != null) {
                boolean ready = (tile.crop.type.equals("TREE") && tile.cropGrowthStage == 5) ||
                                (!tile.crop.type.equals("TREE") && tile.cropGrowthStage == 3);
                if (ready) {
                    player.inventory[player.selectedSlot] = tile.crop; 
                    tile.crop = null; tile.cropGrowthStage = 0;
                }
            }
            return;
        }

        switch (heldItem.type) {
            case "TOOL":
                if (heldItem.name.equals("Enxada")) { 
                    if (!tile.isTilled && tile.crop == null && !tile.hasFence && !tile.isLake) {
                         if(player.useEnergy()) tile.isTilled = true; 
                    }
                } 
                else if (heldItem.name.equals("Regador")) { 
                    boolean isNearWater = tile.isLake;
                    if (!isNearWater) {
                        int[] dx = {0, 0, 1, -1}; int[] dy = {1, -1, 0, 0};
                        for(int i=0; i<4; i++) {
                            int nx = p.x + dx[i]; int ny = p.y + dy[i];
                            if(nx >= 0 && nx < COLS && ny >= 0 && ny < ROWS && map[nx][ny].isLake) {
                                isNearWater = true; break;
                            }
                        }
                    }

                    if (isNearWater) {
                        player.waterCharges = player.MAX_WATER;
                    } else if (tile.isTilled || (tile.crop != null && tile.crop.type.equals("TREE"))) {
                        if (player.waterCharges > 0) {
                            if(player.useEnergy()) { tile.isWatered = true; player.waterCharges--; }
                        }
                    }
                }
                else if (heldItem.name.equals("Foice")) { 
                    if(player.useEnergy()) {
                        if (tile.hasFence) tile.hasFence = false;
                        else if (tile.crop != null) { tile.crop = null; tile.cropGrowthStage = 0; }
                    }
                }
                break;

            case "BUILDING":
                if (!tile.hasFence && tile.crop == null && !tile.isLake) {
                    tile.hasFence = true; tile.isTilled = false;
                }
                break;

            case "SEED":
                if (tile.isTilled && tile.crop == null && !tile.hasFence) {
                    String finalCropId = heldItem.id.replace("S_", "C_");
                    tile.crop = itemRegistry.get(finalCropId);
                    tile.cropGrowthStage = 0;
                    player.inventory[player.selectedSlot] = null; 
                }
                break;

            case "TREE_SEED":
                if (tile.crop == null && !tile.hasFence && !tile.isLake) {
                    tile.crop = itemRegistry.get("C_TREE");
                    tile.cropGrowthStage = 0;
                    player.inventory[player.selectedSlot] = null; 
                }
                break;
        }
    }

    private void startSleep() {
        currentState = GameState.SLEEPING;
        dailyEarnings = 0;
        int qtdPumpkin = storage.get("Abóbora");
        int pricePumpkin = itemRegistry.get("C_PUMPKIN").price;
        dailyEarnings += qtdPumpkin * pricePumpkin;
        storage.put("Abóbora", 0); 

        int qtdBerry = storage.get("Berry Azul");
        int priceBerry = itemRegistry.get("C_BERRY").price;
        dailyEarnings += qtdBerry * priceBerry;
        storage.put("Berry Azul", 0); 

        player.gold += dailyEarnings;
        sleepTransitionTimer.start();
    }

    private void wakeUp() {
        day++;
        player.recoverEnergy(); 
        for (int c = 0; c < COLS; c++) {
            for (int r = 0; r < ROWS; r++) map[c][r].grow();
        }
        currentState = GameState.PLAYING;
        
        if (player.inventory[4] == null) player.inventory[4] = itemRegistry.get("S_TREE");
        if (player.inventory[5] == null) player.inventory[5] = itemRegistry.get("S_PUMPKIN");
        if (player.inventory[6] == null) player.inventory[6] = itemRegistry.get("S_BERRY");
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        if (currentState == GameState.MENU) {
            drawMenu(g2); return;
        }

        drawSky(g2);

        int mapOffsetY = SKY_HEIGHT;
        g2.translate(0, mapOffsetY); 
        for (int r = 0; r < ROWS; r++) { 
            for (int c = 0; c < COLS; c++) { 
                map[c][r].draw(g2, TILE_SIZE); 
            } 
        }
        g2.translate(0, -mapOffsetY); 

        drawHouse(g2);
        player.draw(g2);
        
        // UI Modernizada
        drawHotbar(g2); 
        drawDashboard(g2); 

        if (currentState == GameState.SLEEPING) {
            drawSleepScreen(g2);
        }
    }

    // --- PAINEL STATUS (Top Right) ---
    private void drawDashboard(Graphics2D g2) {
        int dashW = 180;
        int dashH = 90;
        int dashX = getWidth() - dashW - 20;
        int dashY = 15;

        // Fundo Painel (Madeira)
        g2.setColor(new Color(0, 0, 0, 100)); // Sombra
        g2.fillRoundRect(dashX + 4, dashY + 4, dashW, dashH, 15, 15);
        g2.setColor(UI_WOOD);
        g2.fillRoundRect(dashX, dashY, dashW, dashH, 15, 15);
        g2.setColor(UI_WOOD_DARK);
        g2.setStroke(new BasicStroke(4));
        g2.drawRoundRect(dashX, dashY, dashW, dashH, 15, 15);

        // Texto DIA
        g2.setColor(Color.WHITE);
        g2.setFont(new Font("Bookman Old Style", Font.BOLD, 18));
        g2.drawString("Dia: " + day, dashX + 15, dashY + 25);

        // Energia (Barra Suave)
        int barW = 140; int barH = 14;
        int barX = dashX + 15; int barY = dashY + 40;
        g2.setColor(new Color(40, 20, 10)); // Fundo barra
        g2.fillRoundRect(barX, barY, barW, barH, 8, 8);
        
        float energyPercent = (float) player.energy / player.maxEnergy;
        Color eColor = new Color(50, 220, 80);
        if (energyPercent <= 0.2) eColor = new Color(220, 50, 50);
        else if (energyPercent <= 0.5) eColor = new Color(220, 180, 50);
        
        g2.setColor(eColor);
        g2.fillRoundRect(barX + 2, barY + 2, (int)((barW - 4) * energyPercent), barH - 4, 6, 6);
        
        // Brilho na barra
        g2.setColor(new Color(255, 255, 255, 80));
        g2.fillRoundRect(barX + 2, barY + 2, (int)((barW - 4) * energyPercent), barH/2 - 2, 6, 6);

        // Moedas
        g2.setColor(new Color(255, 215, 0)); g2.fillOval(barX, barY + 20, 20, 20); 
        g2.setColor(new Color(218, 165, 32)); g2.drawOval(barX, barY + 20, 20, 20); 
        g2.setColor(new Color(150, 80, 0)); g2.setFont(new Font("Arial", Font.BOLD, 14)); 
        g2.drawString("F", barX + 6, barY + 35); // Moeda Fernandinho

        g2.setColor(Color.WHITE); g2.setFont(new Font("Arial", Font.BOLD, 18));
        g2.drawString(player.gold + " F-Coins", barX + 25, barY + 36);
        
        if (player.energy == 0) {
            g2.setColor(Color.RED); g2.setFont(new Font("Arial", Font.BOLD, 12));
            g2.drawString("EXAUSTO!", dashX + 90, dashY + 23);
        }
    }

    // --- INVENTÁRIO (HOTBAR FLUTUANTE) ---
    private void drawHotbar(Graphics2D g2) {
        int slots = 8;
        int slotSize = 46;
        int gap = 8;
        int padding = 10;
        int hotbarW = (slots * slotSize) + ((slots - 1) * gap) + (padding * 2);
        int hotbarH = slotSize + (padding * 2);
        int startX = (getWidth() - hotbarW) / 2;
        int startY = getHeight() - hotbarH - 10;

        // Fundo Hotbar
        g2.setColor(new Color(0, 0, 0, 100)); // Sombra
        g2.fillRoundRect(startX + 4, startY + 4, hotbarW, hotbarH, 20, 20);
        g2.setColor(UI_WOOD);
        g2.fillRoundRect(startX, startY, hotbarW, hotbarH, 20, 20);
        g2.setColor(UI_WOOD_DARK);
        g2.setStroke(new BasicStroke(4));
        g2.drawRoundRect(startX, startY, hotbarW, hotbarH, 20, 20);

        g2.setFont(new Font("Arial", Font.BOLD, 12));

        for (int i = 0; i < slots; i++) {
            int x = startX + padding + (i * (slotSize + gap)); 
            int y = startY + padding;
            
            // Slot BG
            g2.setColor(SLOT_BG); 
            g2.fillRoundRect(x, y, slotSize, slotSize, 12, 12);
            
            if (i == player.selectedSlot) { 
                g2.setColor(SLOT_SELECTED); 
                g2.setStroke(new BasicStroke(4)); 
            } else { 
                g2.setColor(new Color(0, 0, 0, 50)); // Sombra interna leve
                g2.setStroke(new BasicStroke(2)); 
            }
            g2.drawRoundRect(x, y, slotSize, slotSize, 12, 12);
            
            // Número atalho
            g2.setColor(Color.WHITE); 
            g2.drawString(String.valueOf(i+1), x + 6, y + 16);

            // Item Icon
            Item item = player.inventory[i];
            if (item != null) {
                if (item.id.equals("SCYTHE")) {
                    g2.setColor(item.color); g2.setStroke(new BasicStroke(2));
                    g2.drawArc(x + 10, y + 10, 26, 26, 0, 110);
                    g2.setColor(item.detailColor); g2.fillRect(x + 24, y + 10, 4, 26);
                } else if (item.id.equals("HOE")) {
                    g2.setColor(item.detailColor); g2.fillRect(x + 22, y + 8, 4, 30);
                    g2.setColor(item.color); g2.fillRect(x + 12, y + 8, 14, 6);
                } else if (item.id.equals("WATER")) {
                    g2.setColor(item.color); g2.fillRoundRect(x + 12, y + 16, 20, 18, 6, 6);
                    g2.setColor(item.detailColor); g2.drawArc(x + 12, y + 10, 14, 14, 0, 180);
                    g2.drawLine(x + 28, y + 22, x + 38, y + 14);
                } else if (item.id.equals("FENCE")) {
                    g2.setColor(item.color); g2.fillRect(x + 16, y + 10, 4, 26); g2.fillRect(x + 26, y + 10, 4, 26);
                    g2.setColor(item.detailColor); g2.fillRect(x + 14, y + 16, 16, 4);
                } else if (item.type.equals("TREE_SEED")) {
                    g2.setColor(item.color); g2.fillRect(x + 20, y + 24, 6, 12);
                    g2.setColor(item.detailColor); g2.fillOval(x + 14, y + 8, 18, 18);
                } else {
                    g2.setColor(item.color); g2.fillOval(x + 10, y + 12, 26, 26);
                    if(item.type.equals("CROP")) {
                        g2.setColor(item.detailColor); g2.fillRoundRect(x+21, y+8, 4, 8, 2, 2);
                        g2.setColor(new Color(255,255,255,100)); g2.fillOval(x+14, y+16, 6, 6);
                    }
                }
                
                // Nome do item selecionado (flutuando em cima da hotbar)
                if (i == player.selectedSlot) {
                    String name = item.name;
                    int tw = g2.getFontMetrics().stringWidth(name);
                    g2.setColor(new Color(0,0,0,150));
                    g2.fillRoundRect(startX + (hotbarW/2) - (tw/2) - 10, startY - 30, tw + 20, 24, 10, 10);
                    g2.setColor(Color.WHITE); 
                    g2.drawString(name, startX + (hotbarW/2) - (tw/2), startY - 14);
                }
            }
        }
    }

    private void drawSleepScreen(Graphics2D g2) {
        g2.setColor(new Color(0, 0, 0, 220)); g2.fillRect(0, 0, getWidth(), getHeight());
        int cx = getWidth() / 2; int cy = getHeight() / 2;
        g2.setColor(Color.WHITE);  g2.setFont(new Font("Bookman Old Style", Font.ITALIC, 40));
        String msg = "Fim do Dia " + day;
        int w = g2.getFontMetrics().stringWidth(msg);
        g2.drawString(msg, cx - w/2, cy - 50);

        g2.setFont(new Font("Arial", Font.BOLD, 24)); g2.setColor(Color.YELLOW);
        String earnings = "Vendas da noite: " + dailyEarnings + " F-Coins";
        w = g2.getFontMetrics().stringWidth(earnings);
        g2.drawString(earnings, cx - w/2, cy + 20);
        
        g2.setColor(Color.WHITE); g2.setFont(new Font("Arial", Font.PLAIN, 16));
        g2.drawString("Energia restaurada.", cx - 70, cy + 60);
    }

    private void drawMenu(Graphics2D g2) {
        GradientPaint gp = new GradientPaint(0, 0, new Color(135, 206, 250), 0, getHeight(), new Color(34, 139, 34));
        g2.setPaint(gp); g2.fillRect(0, 0, getWidth(), getHeight());
        
        // Sombra de folhagem no fundo (decorativo)
        g2.setColor(new Color(20, 100, 20, 50));
        g2.fillOval(-50, getHeight()-150, 300, 300);
        g2.fillOval(getWidth()-200, getHeight()-200, 350, 350);

        g2.setColor(Color.WHITE); g2.setFont(new Font("Comic Sans MS", Font.BOLD, 60));
        String title = "Fazenda do Fernandinho";
        int w = g2.getFontMetrics().stringWidth(title);
        g2.setColor(new Color(0, 50, 0, 100)); g2.drawString(title, getWidth()/2 - w/2 + 5, 205);
        g2.setColor(new Color(255, 220, 50)); g2.drawString(title, getWidth()/2 - w/2, 200);
        
        g2.setColor(Color.WHITE); g2.setFont(new Font("Arial", Font.BOLD, 24));
        String sub = "Pressione ENTER para Iniciar";
        int w2 = g2.getFontMetrics().stringWidth(sub);
        
        // Botão estilo Start
        g2.setColor(new Color(0,0,0,100));
        g2.fillRoundRect(getWidth()/2 - w2/2 - 20 + 4, 370 + 4, w2 + 40, 50, 25, 25);
        g2.setColor(UI_WOOD);
        g2.fillRoundRect(getWidth()/2 - w2/2 - 20, 370, w2 + 40, 50, 25, 25);
        g2.setColor(UI_WOOD_DARK);
        g2.setStroke(new BasicStroke(3));
        g2.drawRoundRect(getWidth()/2 - w2/2 - 20, 370, w2 + 40, 50, 25, 25);

        if ((System.currentTimeMillis() / 500) % 2 == 0) {
            g2.setColor(Color.WHITE);
            g2.drawString(sub, getWidth()/2 - w2/2, 405);
        }
    }

    private void drawSky(Graphics2D g2) {
        boolean isNight = (currentState == GameState.SLEEPING);
        if (isNight) {
            GradientPaint night = new GradientPaint(0, 0, new Color(10, 10, 30), 0, SKY_HEIGHT, new Color(40, 20, 60));
            g2.setPaint(night); g2.fillRect(0, 0, getWidth(), SKY_HEIGHT);
            g2.setColor(Color.WHITE); for(Point s : stars) g2.fillOval(s.x, s.y, 2, 2);
            
            // Lua com brilho
            g2.setColor(new Color(240, 240, 255, 100)); g2.fillOval(40, 10, 80, 80);
            g2.setColor(new Color(240, 240, 240)); g2.fillOval(50, 20, 60, 60);
            g2.setColor(new Color(200, 200, 210)); g2.fillOval(60, 35, 15, 15); g2.fillOval(80, 50, 10, 10);
        } else {
            GradientPaint daySky = new GradientPaint(0, 0, new Color(80, 190, 255), 0, SKY_HEIGHT, new Color(170, 220, 255));
            g2.setPaint(daySky); g2.fillRect(0, 0, getWidth(), SKY_HEIGHT);
            
            // Sol com brilho suave
            g2.setColor(new Color(255, 220, 100, 100)); g2.fillOval(getWidth() - 115, 5, 100, 100);
            g2.setColor(new Color(255, 230, 80)); g2.fillOval(getWidth() - 100, 20, 70, 70);
            
            // Nuvens fofinhas
            g2.setColor(new Color(255, 255, 255, 220));
            g2.fillRoundRect(100, 50, 100, 30, 30, 30); g2.fillOval(120, 35, 50, 50);
            g2.fillRoundRect(300, 30, 120, 40, 40, 40); g2.fillOval(330, 15, 60, 60);
        }
    }

    private void drawHouse(Graphics2D g2) {
        int x = houseBounds.x; int y = houseBounds.y; int w = houseBounds.width; int h = houseBounds.height;
        
        // Sombra da Casa
        g2.setColor(new Color(0,0,0,80));
        g2.fillOval(x - 10, y + h - 20, w + 20, 30);

        // Base da Parede
        g2.setColor(new Color(140, 90, 50)); g2.fillRect(x + 10, y + 40, w - 20, h - 40);
        // Textura madeira da parede
        g2.setColor(new Color(110, 70, 35)); 
        g2.setStroke(new BasicStroke(2));
        for(int i = y + 55; i < y + h; i+=15) g2.drawLine(x + 10, i, x + w - 10, i);
        
        // Porta
        g2.setColor(new Color(60, 40, 20)); g2.fillRoundRect(x + w/2 - 15, y + h - 50, 30, 50, 8, 8);
        g2.setColor(new Color(40, 25, 10)); g2.drawRoundRect(x + w/2 - 15, y + h - 50, 30, 50, 8, 8);
        g2.setColor(Color.YELLOW); g2.fillOval(x + w/2 + 6, y + h - 25, 5, 5); // Maçaneta

        // Janelas
        g2.setColor(new Color(135, 206, 235)); // Vidro
        g2.fillRoundRect(x + 20, y + h - 60, 24, 24, 4, 4);
        g2.fillRoundRect(x + w - 44, y + h - 60, 24, 24, 4, 4);
        g2.setColor(new Color(60, 40, 20)); // Esquadria
        g2.drawRoundRect(x + 20, y + h - 60, 24, 24, 4, 4); g2.drawLine(x + 32, y + h - 60, x + 32, y + h - 36);
        g2.drawRoundRect(x + w - 44, y + h - 60, 24, 24, 4, 4); g2.drawLine(x + w - 32, y + h - 60, x + w - 32, y + h - 36);

        // Chaminé com Fumaça
        g2.setColor(new Color(100, 50, 50)); g2.fillRect(x + 20, y + 10, 16, 40);
        g2.setColor(new Color(80, 30, 30)); g2.drawRect(x + 20, y + 10, 16, 40);
        g2.setColor(new Color(200, 200, 200, 150));
        long time = System.currentTimeMillis() / 400;
        g2.fillOval(x + 20 + (int)(Math.sin(time)*5), y - 10, 15, 15);
        g2.fillOval(x + 25 + (int)(Math.sin(time+1)*10), y - 25, 20, 20);

        // Telhado (Gradiente Vermelho Bonito)
        GradientPaint roofPaint = new GradientPaint(x, y, new Color(180, 50, 50), x, y + 40, new Color(120, 20, 20));
        g2.setPaint(roofPaint);
        Polygon roof = new Polygon();
        roof.addPoint(x - 15, y + 45); roof.addPoint(x + w + 15, y + 45); roof.addPoint(x + w / 2, y - 5);
        g2.fillPolygon(roof);
        g2.setColor(new Color(80, 10, 10)); g2.setStroke(new BasicStroke(4, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g2.drawPolygon(roof);

        // Placa do Armazém no Chão (ao lado da porta)
        int infoX = x + w + 5; int infoY = y + h - 50;
        g2.setColor(new Color(0,0,0,80)); g2.fillRoundRect(infoX + 2, infoY + 2, 130, 50, 8, 8); // Sombra
        g2.setColor(new Color(220, 180, 110)); g2.fillRoundRect(infoX, infoY, 130, 50, 8, 8);
        g2.setColor(new Color(120, 75, 40)); g2.drawRoundRect(infoX, infoY, 130, 50, 8, 8);
        
        g2.setFont(new Font("Arial", Font.BOLD, 12)); 
        g2.setColor(new Color(80, 40, 10)); g2.drawString("Armazém:", infoX + 8, infoY + 16);
        
        g2.setFont(new Font("Arial", Font.BOLD, 11)); 
        g2.setColor(new Color(200, 100, 0));
        g2.drawString("Abóboras: " + storage.get("Abóbora"), infoX + 8, infoY + 30);
        g2.setColor(new Color(20, 80, 200)); 
        g2.drawString("Berries: " + storage.get("Berry Azul"), infoX + 8, infoY + 44);
    }

    @Override
    public void keyPressed(KeyEvent e) {
        int code = e.getKeyCode();
        if (currentState == GameState.MENU) { if (code == KeyEvent.VK_ENTER) currentState = GameState.PLAYING; return; }
        if (currentState == GameState.SLEEPING) return;
        
        if (code == KeyEvent.VK_W) up = true; if (code == KeyEvent.VK_S) down = true;
        if (code == KeyEvent.VK_A) left = true; if (code == KeyEvent.VK_D) right = true;
        if (code == KeyEvent.VK_SPACE) interact();
        if (code == KeyEvent.VK_ENTER) startSleep(); 
        if (code >= KeyEvent.VK_1 && code <= KeyEvent.VK_8) player.selectedSlot = code - KeyEvent.VK_1;
    }
    @Override public void keyReleased(KeyEvent e) {
        int code = e.getKeyCode();
        if (code == KeyEvent.VK_W) up = false; if (code == KeyEvent.VK_S) down = false;
        if (code == KeyEvent.VK_A) left = false; if (code == KeyEvent.VK_D) right = false;
    }
    @Override public void keyTyped(KeyEvent e) {}

    public static void main(String[] args) {
        JFrame frame = new JFrame("Fazenda do Fernandinho - Remaster Visual");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setResizable(false);
        frame.add(new JavaValley());
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
}