package se.kth.ag2411.mapalgebra;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;

public class Ex02 {

    public static void main(String[] args) {

        if (args.length >= 3) {
            String layerName = args[0];
            String layerPath = args[1];
            int scale = Integer.parseInt(args[2]);

            // Instantiate a layer
            Layer layer = new Layer(args[0], args[1]);

            // Create a frame
            JFrame frame = new JFrame("Map");

            // Instantiate a map panel

            MapPanel panel;
            BufferedImage image;
            if (args.length > 3) {
                double[] valuesOfInterest = new double[args.length - 3];
                for (int i = 3; i < args.length; i++) {
                    valuesOfInterest[i - 3] = Double.parseDouble(args[i]);
                }
                //panel = new MapPanel(layer.toImage(valuesOfInterest), Integer.parseInt(args[2]));
                image = layer.toImage(valuesOfInterest);
            } else {
                //panel = new MapPanel(layer.toImage(), Integer.parseInt(args[2]));
                image = layer.toImage();
                }

            panel = new MapPanel(image, Integer.parseInt(args[2]));
            panel.setPreferredSize(new Dimension(image.getWidth()*scale, image.getHeight()*scale));

            frame.add(panel);
            frame.pack();
            frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
            frame.setVisible(true);
        } else{
            System.out.println("Too many or few arguments......");
        }
    }
}