package se.kth.ag2411.mapalgebra;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;

public class MapPanel extends JPanel {
    public BufferedImage image;
    public int scale;

    // CONSTRUCTORS
    // need to initialize both the attributes inherited from JPanel
    // and those added to this class.
    public MapPanel(BufferedImage image, int scale) {
        super(); // initializes the attributes inherited from JPanel.
        this.image = image;
        this.scale = scale;
        //setPreferredSize(new Dimension(image.getWidth()*scale, image.getHeight()*scale));
    }

    // OTHER METHODS
    // including methods inherited from JPanel, one of which is modified as follows:
    @Override
    protected void paintComponent(Graphics g) {
        // First, do what JPanel would normally do.
        super.paintComponent(g);
        // Then, draw the image at the specified scale.
        g.drawImage(image,0, 0, image.getWidth()*scale, image.getHeight()*scale, this);
    }
}
