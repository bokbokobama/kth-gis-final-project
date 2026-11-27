package se.kth.ag2411.mapalgebra;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.Objects;

public class Ex03 {
    public static void main(String[] args) {
        String function = args[0];
        String input = args[1];
        String output = null;
        int scale = Integer.parseInt(args[args.length-1]);

        Layer layer = new Layer(args[0], args[1]);
        Layer result = null;
        Layer layer2 = null;

        if(Objects.equals(function, "focalVariety")) {
            // focalVariety .\data\vegetation.asc .\data\fvar_vege.asc 1 false 3
            output = args[2];
            int radius = Integer.parseInt(args[3]);
            boolean isSquare = Boolean.parseBoolean(args[4]);
            result = layer.focalVariety(radius, isSquare, output);
            result.save(output);

        } else {
            String input_2 = args[2];
            output = args[3];

            layer2 = new Layer("layer2", input_2);

            if(Objects.equals(function, "localSum")) {
                // localSum .\data\development.asc .\data\hydrology.asc .\data\lsum_dev_hydro.asc 3
                result = layer.localSum(layer2, output);

            } else if(Objects.equals(function, "zonalMinimum")) {
                // zonalMinimum .\data\elevation.asc .\data\vegetation.asc .\data\zmin_ele_vege.asc 3
                result = layer.zonalMinimum(layer2, output);
            }
        };


        result.save(output);
        result.print();

        JFrame frame = new JFrame("Map");

        // Instantiate a map panel

        MapPanel panel_input; MapPanel panel_result;
        BufferedImage input_img; BufferedImage result_img;

        input_img = layer.toImage();
        result_img = result.toImage();

        JPanel container = new JPanel();
        container.setLayout(new BoxLayout(container, BoxLayout.Y_AXIS));

        panel_input = new MapPanel(input_img, scale);
        panel_input.setPreferredSize(new Dimension(input_img.getWidth()*scale, input_img.getHeight()*scale));
        container.add(panel_input);

        if (layer2 != null) {
            BufferedImage input_img_2 = layer2.toImage();

            MapPanel panel_input_2 = new MapPanel(input_img_2, scale);
            panel_input_2.setPreferredSize(new Dimension(input_img_2.getWidth() * scale, input_img_2.getHeight() * scale));
            container.add(panel_input_2);
        }

        panel_result = new MapPanel(result_img, scale);
        panel_result.setPreferredSize(new Dimension(result_img.getWidth()*scale, result_img.getHeight()*scale));
        container.add(panel_result);

        frame.add(container);
        frame.pack();
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.setVisible(true);
    }
}