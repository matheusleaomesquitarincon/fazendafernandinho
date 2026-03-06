import java.awt.*;
import java.util.Random;

public class FarmTile {
    public int x, y;
    public boolean isTilled = false;
    public boolean isWatered = false;
    public boolean hasFence = false;
    public boolean isLake = false;
    
    public Item crop = null;
    public int cropGrowthStage = 0; 

    // Cores mais suaves e vibrantes
    private final Color GRASS_COLOR = new Color(110, 190, 70);
    private final Color GRASS_DETAIL = new Color(90, 160, 50);
    private final Color SOIL_DRY = new Color(150, 90, 50);
    private final Color SOIL_WET = new Color(100, 55, 25);
    private final Color WOOD_DARK = new Color(120, 75, 40);
    private final Color WOOD_LIGHT = new Color(150, 100, 60);
    private final Color WATER_COLOR = new Color(70, 150, 230);
    private final Color WATER_RIPPLE = new Color(180, 220, 255, 150);

    private Random rand = new Random();
    private int textureSeed; 

    public FarmTile(int x, int y) {
        this.x = x;
        this.y = y;
        this.textureSeed = rand.nextInt(1000);
    }

    public void grow() {
        if (crop != null && isWatered) {
            int maxStage = crop.type.equals("TREE") ? 5 : 3;
            if (cropGrowthStage < maxStage) cropGrowthStage++;
            isWatered = false;
        } else if (crop == null && isWatered) {
            isWatered = false;
        }
    }

    public void draw(Graphics g, int tileSize) {
        Graphics2D g2 = (Graphics2D) g;
        // Ativa o Anti-Aliasing para deixar tudo redondinho e bonito
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        int sx = x * tileSize;
        int sy = y * tileSize;
        rand.setSeed(textureSeed);

        // 1. DESENHO DO LAGO
        if (isLake) {
            g2.setColor(WATER_COLOR);
            g2.fillRect(sx, sy, tileSize, tileSize);
            
            // Ondinhas suaves
            g2.setColor(WATER_RIPPLE);
            g2.setStroke(new BasicStroke(2, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            long time = System.currentTimeMillis() / 300;
            if ((time + x + y) % 3 == 0) {
                g2.drawLine(sx + 10, sy + 15, sx + 25, sy + 15);
                g2.drawLine(sx + 20, sy + 35, sx + 35, sy + 35);
            } else if ((time + x + y) % 3 == 1) {
                g2.drawLine(sx + 5, sy + 25, sx + 20, sy + 25);
                g2.drawLine(sx + 30, sy + 15, sx + 40, sy + 15);
            }
            return;
        }

        // 2. SOLO (Grama ou Terra)
        if (isTilled && !hasFence) { 
            // Terra Arada com leve arredondamento
            g2.setColor(GRASS_COLOR); // Fundo verde
            g2.fillRect(sx, sy, tileSize, tileSize);
            g2.setColor(isWatered ? SOIL_WET : SOIL_DRY);
            g2.fillRoundRect(sx + 2, sy + 2, tileSize - 4, tileSize - 4, 8, 8);
            
            // Detalhe de terra remexida
            g2.setColor(isWatered ? SOIL_WET.darker() : SOIL_DRY.darker());
            g2.drawLine(sx + 8, sy + 15, sx + tileSize - 8, sy + 15);
            g2.drawLine(sx + 8, sy + 30, sx + tileSize - 8, sy + 30);
        } else {
            // Grama
            g2.setColor(GRASS_COLOR);
            g2.fillRect(sx, sy, tileSize, tileSize);
            g2.setColor(GRASS_DETAIL);
            for (int i = 0; i < 4; i++) {
                int px = sx + rand.nextInt(tileSize - 8) + 4;
                int py = sy + rand.nextInt(tileSize - 8) + 4;
                // Desenha tufinhos de grama (v invertido)
                g2.drawLine(px, py, px - 3, py - 4);
                g2.drawLine(px, py, px + 3, py - 4);
            }
        }
        
        // Sombra de grade bem suave
        g2.setColor(new Color(0, 0, 0, 15));
        g2.drawRect(sx, sy, tileSize, tileSize);

        // 3. CERCA
        if (hasFence) drawFence(g2, sx, sy, tileSize);

        // 4. PLANTA / ÁRVORE
        if (crop != null) drawCrop(g2, sx, sy, tileSize);
    }

    private void drawFence(Graphics2D g2, int sx, int sy, int tileSize) {
        g2.setColor(WOOD_DARK);
        // Postes com sombra
        g2.fillRoundRect(sx + 4, sy + 6, 10, tileSize - 6, 4, 4);
        g2.fillRoundRect(sx + tileSize - 14, sy + 6, 10, tileSize - 6, 4, 4);
        
        g2.setColor(WOOD_LIGHT);
        g2.fillRect(sx + 5, sy + 7, 6, tileSize - 8);
        g2.fillRect(sx + tileSize - 13, sy + 7, 6, tileSize - 8);

        // Barras horizontais
        g2.setColor(WOOD_DARK);
        g2.fillRect(sx, sy + 18, tileSize, 8);
        g2.fillRect(sx, sy + 34, tileSize, 8);
        g2.setColor(WOOD_LIGHT);
        g2.fillRect(sx, sy + 19, tileSize, 4);
        g2.fillRect(sx, sy + 35, tileSize, 4);
    }

    private void drawCrop(Graphics2D g2, int sx, int sy, int tileSize) {
        int centerX = sx + tileSize / 2;
        int bottomY = sy + tileSize - 5;

        // --- ÁRVORE COM SOMBRAS E LUZES ---
        if (crop.type.equals("TREE")) {
            if (cropGrowthStage < 5) {
                int height = 10 + (cropGrowthStage * 8);
                g2.setColor(WOOD_DARK); 
                g2.fillRoundRect(centerX - 3, bottomY - height, 6, height, 3, 3);
                
                g2.setColor(new Color(20, 120, 30)); // Folhas base
                int leafSize = 16 + cropGrowthStage * 3;
                g2.fillOval(centerX - leafSize/2, bottomY - height - leafSize/2 + 5, leafSize, leafSize);
                // Brilho da folha
                g2.setColor(new Color(40, 160, 50)); 
                g2.fillOval(centerX - leafSize/2 + 2, bottomY - height - leafSize/2 + 5, leafSize - 6, leafSize - 6);
            } else {
                // Sombra da árvore no chão
                g2.setColor(new Color(0, 0, 0, 60));
                g2.fillOval(centerX - 15, bottomY - 10, 30, 15);

                // Tronco
                g2.setColor(WOOD_DARK); 
                g2.fillRect(centerX - 10, bottomY - 60, 20, 60);
                g2.setColor(WOOD_LIGHT);
                g2.fillRect(centerX - 8, bottomY - 60, 8, 60); // Detalhe do tronco
                
                // Copa (3 camadas para dar volume)
                g2.setColor(new Color(15, 90, 20)); // Verde bem escuro (fundo)
                g2.fillOval(centerX - 45, bottomY - 115, 90, 90);
                
                g2.setColor(new Color(25, 120, 35)); // Verde médio
                g2.fillOval(centerX - 40, bottomY - 110, 80, 80);
                g2.fillOval(centerX - 25, bottomY - 130, 50, 50);
                
                g2.setColor(new Color(45, 160, 55)); // Verde claro (luz)
                g2.fillOval(centerX - 30, bottomY - 100, 50, 40);
                g2.fillOval(centerX - 15, bottomY - 120, 30, 30);
            }
            return;
        }

        // --- PLANTAS NORMAIS ---
        // Sombra da planta
        g2.setColor(new Color(0, 0, 0, 40));
        g2.fillOval(centerX - 8, bottomY - 4, 16, 8);

        if (cropGrowthStage < 3) {
            g2.setColor(new Color(40, 140, 30));
            g2.setStroke(new BasicStroke(2, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            if(cropGrowthStage == 0) {
                g2.setColor(new Color(220, 190, 140)); // Sementes mais claras
                g2.fillOval(centerX - 4, bottomY - 4, 4, 4);
                g2.fillOval(centerX + 2, bottomY - 6, 4, 4);
            } else if(cropGrowthStage == 1) {
                g2.drawLine(centerX, bottomY, centerX - 5, bottomY - 10);
                g2.drawLine(centerX, bottomY, centerX + 5, bottomY - 10);
            } else {
                g2.fillRect(centerX - 1, bottomY - 20, 3, 20);
                g2.fillOval(centerX - 12, bottomY - 25, 12, 10);
                g2.fillOval(centerX + 2, bottomY - 22, 12, 10);
            }
        } else {
            // ESTÁGIO MADURO
            if (crop.id.equals("C_PUMPKIN")) {
                int pSize = 28;
                int pX = centerX - pSize/2; int pY = bottomY - pSize - 2;
                
                g2.setColor(new Color(220, 100, 0)); // Borda escurecida
                g2.fillOval(pX, pY, pSize, pSize);
                g2.setColor(new Color(255, 140, 0)); // Corpo
                g2.fillOval(pX+2, pY+2, pSize-4, pSize-4);
                
                // Gomos 3D
                g2.setColor(new Color(255, 170, 40)); 
                g2.setStroke(new BasicStroke(2));
                g2.drawArc(pX + 5, pY + 2, pSize - 10, pSize - 4, 0, 360); 
                g2.drawArc(pX + 11, pY + 2, pSize - 22, pSize - 4, 0, 360);
                
                // Brilho
                g2.setColor(new Color(255, 255, 255, 100));
                g2.fillOval(pX + 6, pY + 4, 6, 8);
                
                // Talo
                g2.setColor(new Color(30, 100, 20)); 
                g2.fillRoundRect(centerX - 2, pY - 6, 4, 8, 2, 2);
                
            } else if (crop.id.equals("C_BERRY")) {
                // Arbusto com volume
                g2.setColor(new Color(20, 100, 20)); 
                g2.fillOval(centerX - 16, bottomY - 32, 32, 32); // Fundo escuro
                g2.setColor(new Color(34, 150, 34)); 
                g2.fillOval(centerX - 12, bottomY - 28, 24, 24); // Frente clara
                
                // Frutinhas brilhantes
                g2.setColor(new Color(20, 20, 180));
                g2.fillOval(centerX - 6, bottomY - 25, 8, 8); 
                g2.fillOval(centerX + 6, bottomY - 20, 8, 8);
                g2.fillOval(centerX - 10, bottomY - 15, 8, 8); 
                g2.fillOval(centerX + 2, bottomY - 10, 8, 8);
                
                g2.setColor(new Color(150, 200, 255)); // Brilho das berries
                g2.fillOval(centerX - 4, bottomY - 24, 3, 3);
                g2.fillOval(centerX + 8, bottomY - 19, 3, 3);
                g2.fillOval(centerX - 8, bottomY - 14, 3, 3);
                g2.fillOval(centerX + 4, bottomY - 9, 3, 3);
            }
        }
    }
}