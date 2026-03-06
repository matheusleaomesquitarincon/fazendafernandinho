import java.awt.Color;

public class Item {
    String id;
    String name;
    String type;
    Color color; 
    Color detailColor; 
    int price;

    public Item(String id, String name, String type, Color color, Color detailColor, int price) {
        this.id = id;
        this.name = name;
        this.type = type;
        this.color = color;
        this.detailColor = detailColor;
        this.price = price;
    }
}