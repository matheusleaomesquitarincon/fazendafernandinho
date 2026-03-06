import java.awt.*;

public class Player {
    public int x, y;
    public int speed = 5; // Um pouco mais rápido para melhor jogabilidade
    public int width = 32, height = 36;
    public int selectedSlot = 0;
    
    // VARIÁVEIS DE RPG
    public int maxEnergy = 100;
    public int energy = 100;
    public int gold = 0;
    
    // Cargas do Regador
    public int waterCharges = 3; 
    public final int MAX_WATER = 3;
    
    public Item[] inventory = new Item[8];
    
    // Variável para animação
    private int walkAnim = 0;
    
    public Item getHeldItem() {
        if (selectedSlot < 0 || selectedSlot >= inventory.length) return null;
        return inventory[selectedSlot];
    }

    public Player(int startX, int startY) {
        this.x = startX;
        this.y = startY;
    }

    public void move(int dx, int dy) {
        x += dx * speed;
        y += dy * speed;
        if (dx != 0 || dy != 0) {
            walkAnim++; // Incrementa a animação ao andar
        } else {
            walkAnim = 0; // Para a animação ao parar
        }
    }

    public boolean useEnergy() {
        if (energy > 0) { energy -= 5; return true; }
        return false;
    }
    public void recoverEnergy() { energy = maxEnergy; }

    public void draw(Graphics g) {
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        // Cálculo de oscilação (Swing) para braços e pernas
        int swing = (int)(Math.sin(walkAnim * 0.4) * 6);
        
        // Sombra Dinâmica
        g2.setColor(new Color(0,0,0,50)); 
        g2.fillOval(x+4 - (swing/2), y+height-4, width-8 + Math.abs(swing), 8);

        // Pernas (Animadas)
        g2.setColor(new Color(40, 40, 60)); // Calça Escura
        g2.fillRoundRect(x+10, y+height-14, 6, 14 - swing, 3, 3); // Perna Esq
        g2.fillRoundRect(x+width-16, y+height-14, 6, 14 + swing, 3, 3); // Perna Dir

        // Braço Traseiro (Animado)
        g2.setColor(new Color(30, 80, 200)); 
        g2.fillRoundRect(x+4, y+12, 6, 12 + swing, 3, 3);

        // Corpo (Camisa)
        g2.setColor(new Color(50, 100, 255)); 
        g2.fillRoundRect(x+8, y+10, width-16, height-18, 6, 6);
        
        // Logo Java (Mais nítida)
        g2.setColor(new Color(255, 140, 0)); 
        g2.fillArc(x+12, y+16, 8, 8, 180, 180);
        g2.setColor(Color.RED);
        g2.drawLine(x+14, y+14, x+14, y+16);
        g2.drawLine(x+17, y+13, x+17, y+16);

        // Cabeça
        g2.setColor(new Color(255, 210, 170)); // Pele mais viva
        g2.fillOval(x+4, y-6, 24, 24);
        
        // Rosto Kawaii/Fofo
        g2.setColor(new Color(50, 30, 10)); // Marrom escuro para rosto e cabelo
        g2.fillOval(x + 10, y + 2, 4, 5); // Olho Esq
        g2.fillOval(x + 19, y + 2, 4, 5); // Olho Dir
        
        // Brilho nos olhos
        g2.setColor(Color.WHITE);
        g2.fillOval(x + 11, y + 3, 2, 2);
        g2.fillOval(x + 20, y + 3, 2, 2);
        
        // Sorrisinho
        g2.setColor(new Color(50, 30, 10));
        g2.drawArc(x + 13, y + 6, 6, 4, 0, -180);

        // Cabelo Estiloso
        g2.fillArc(x+4, y-6, 24, 16, 0, 180);
        g2.fillPolygon(new int[]{x+4, x+10, x+6}, new int[]{y+2, y-2, y+6}, 3); // Franja

        // Braço Frontal (Animado)
        g2.setColor(new Color(60, 110, 255)); 
        g2.fillRoundRect(x+width-10, y+12, 6, 12 - swing, 3, 3);

        // Item na mão
        Item held = getHeldItem();
        if (held != null) drawHeldItem(g2, held, swing);
    }

    private void drawHeldItem(Graphics2D g2, Item item, int swing) {
        // A mão frontal segura o item, então o item acompanha o balanço do braço frontal
        int handX = x + width - 8;
        int handY = y + 18 - swing; 
        
        g2.setStroke(new BasicStroke(2, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        
        if (item.id.equals("HOE")) {
            g2.setColor(new Color(101, 67, 33)); // Cabo
            g2.drawLine(handX - 8, handY + 8, handX + 8, handY - 8);
            g2.setColor(new Color(180, 180, 180)); // Lâmina com reflexo
            g2.drawLine(handX + 8, handY - 8, handX + 4, handY - 12);
            g2.drawLine(handX + 4, handY - 12, handX, handY - 4);
        } else if (item.id.equals("WATER")) {
            handY -= 5;
            g2.setColor(item.color); 
            g2.fillRoundRect(handX - 6, handY - 6, 12, 12, 4, 4);
            g2.setColor(item.detailColor); 
            g2.drawArc(handX - 4, handY - 10, 8, 8, 0, 180);
            g2.drawLine(handX + 6, handY, handX + 12, handY - 4);
            g2.setColor(new Color(200, 230, 255)); 
            g2.fillOval(handX + 10, handY - 6, 4, 4);

            // Mini Barra de Água (Mais suave)
            g2.setColor(new Color(0,0,0,150)); 
            g2.fillRoundRect(handX - 10, handY - 16, 20, 4, 2, 2);
            g2.setColor(new Color(50, 200, 255)); 
            int fill = (int)((20.0 * waterCharges) / MAX_WATER);
            g2.fillRoundRect(handX - 10, handY - 16, fill, 4, 2, 2);

        } else if (item.id.equals("SCYTHE")) {
            g2.setColor(new Color(101, 67, 33)); // Cabo
            g2.drawLine(handX - 6, handY + 6, handX + 6, handY - 6);
            g2.setColor(new Color(200, 200, 200)); // Lâmina
            g2.drawArc(handX - 4, handY - 16, 16, 16, 0, 120);
        } else if (item.id.equals("FENCE")) { 
            g2.setColor(new Color(101, 67, 33)); 
            g2.fillRect(handX - 4, handY - 8, 3, 16); 
            g2.fillRect(handX + 2, handY - 8, 3, 16);
            g2.setColor(new Color(150, 100, 60)); 
            g2.fillRect(handX - 6, handY - 2, 14, 3);
        } else if (item.type.equals("TREE_SEED")) {
            g2.setColor(new Color(101, 67, 33)); 
            g2.fillRect(handX - 1, handY, 3, 8);
            g2.setColor(new Color(30, 120, 40)); 
            g2.fillOval(handX - 6, handY - 10, 12, 12);
            g2.setColor(new Color(50, 160, 60));
            g2.fillOval(handX - 4, handY - 8, 6, 6);
        } else {
            // Segurando Fruta/Semente Padrão (com sombra na mão)
            g2.setColor(new Color(0,0,0,50));
            g2.fillOval(handX - 5, handY - 1, 12, 12);
            
            g2.setColor(item.color); 
            g2.fillOval(handX - 6, handY - 6, 12, 12);
            if(item.type.equals("CROP") || item.type.equals("SEED")) {
                 g2.setColor(item.detailColor); 
                 g2.fillRoundRect(handX - 2, handY - 8, 4, 6, 2, 2);
                 // Brilho na fruta
                 g2.setColor(new Color(255,255,255,100));
                 g2.fillOval(handX - 3, handY - 4, 4, 4);
            }
        }
    }
    
    public Point getTilePosition(int tileSize) {
        int centerX = x + width / 2;
        int centerY = y + height / 2 + 10;
        return new Point(centerX / tileSize, centerY / tileSize);
    }
}