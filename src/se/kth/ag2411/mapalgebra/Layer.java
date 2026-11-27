
package se.kth.ag2411.mapalgebra;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.text.DecimalFormat;
import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;
import java.awt.Color;
import java.util.*;
import java.io.IOException;

public class Layer {

    // Attributes
    public String name = null;
    public int nRows;
    public int nCols;
    public double[] origin = new double[2];
    public double resolution;
    public double[] values;
    public double nullValue;

    private final DecimalFormat format = new DecimalFormat("0.##########");

    public Layer(String layerName, String filepath) {
        name = layerName;

        File file = new File(filepath);

        try (Scanner scanner = new Scanner(file)) {

            // Read ASCII raster header
            scanner.next();
            nCols = scanner.nextInt(); scanner.next();
            nRows = scanner.nextInt(); scanner.next();
            origin[0] = scanner.nextDouble(); scanner.next();
            origin[1] = scanner.nextDouble(); scanner.next();
            resolution = scanner.nextDouble(); scanner.next();
            nullValue = scanner.nextDouble();

            // Validate dimensions
            if (nRows <= 0 || nCols <= 0) {
                throw new IllegalArgumentException("Invalid raster dimensions.");
            }

            // Read raster values
            values = new double[nRows*nCols];

            for (int i = 0; i < nRows*nCols; i++) {
                if (!scanner.hasNextDouble()) {
                    throw new IllegalArgumentException("Not enough raster values in file.");
                }
                values[i] = scanner.nextDouble();
            }

        } catch (FileNotFoundException e) {
            throw new IllegalArgumentException("File not found: " + filepath, e);
        }
    }

    public Layer(String name, int nRows, int nCols, double[] origin, double resolution, double nullValue) {
        this.name = name;
        this.nRows = nRows;
        this.nCols = nCols;
        this.origin = origin;
        this.resolution = resolution;
        this.values = new double[nRows*nCols];
        this.nullValue = nullValue;
    }

    public void print() {
        System.out.println("ncols         " + nCols);
        System.out.println("nrows         " + nRows);
        System.out.println("xllcorner     " + format.format(origin[0]));
        System.out.println("yllcorner     " + format.format(origin[1]));
        System.out.println("cellsize      " + format.format(resolution));
        System.out.println("NODATA_value  " + format.format(nullValue));

        for (int i = 0; i < nRows*nCols; i++) {
            System.out.print(format.format(values[i]) + " ");
            if ((i+1) % nCols == 0) {
                System.out.println();
            }
        }
    }

    public void save(String outputFileName) {
        if (!outputFileName.endsWith(".asc")) {
            outputFileName += ".asc";
        }

        try (PrintWriter writer = new PrintWriter(outputFileName)) {
            writer.println("ncols         " + nCols);
            writer.println("nrows         " + nRows);
            writer.println("xllcorner     " + format.format(origin[0]));
            writer.println("yllcorner     " + format.format(origin[1]));
            writer.println("cellsize      " + format.format(resolution));
            writer.println("NODATA_value  " + format.format(nullValue));

            for (int i = 0; i < nRows*nCols; i++) {
                writer.print(format.format(values[i]) + " ");
                if ((i+1) % nCols == 0) {
                    writer.println();
                }
            }

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    // Convert raster values into a grayscale image
    public BufferedImage toImage() {

        BufferedImage image = new BufferedImage(nCols, nRows, BufferedImage.TYPE_INT_RGB);

        WritableRaster raster = image.getRaster();

        double min = getMin();

        double max = getMax();
        for (int i = 0; i < nRows*nCols; i++) {
            int value;
            int[] color;

            if (values[i] == nullValue) {
                color = new int[] {100, 160, 120};
            } else if (max == min) {
                value = 128;
                color = new int[] {value, value, value};
            } else {
                value = (int) (255 * (max - values[i]) / (max - min));
                color = new int[] {value, value, value};
            }

            raster.setPixel(i%nCols, i/nCols, color);
        }
        return image;
    }

    // Convert selected values into a grayscale image
    public BufferedImage toImage(double[] valuesOfInterest) {
        int[][] colors = new int[valuesOfInterest.length][3];

        for(int i=0; i < valuesOfInterest.length; i++) {
            colors[i] = (generateRandomRGBColor());
        }

        // TODO actually random colors


        BufferedImage image = new BufferedImage(
                nCols, nRows, BufferedImage.TYPE_INT_RGB
        );

        WritableRaster raster = image.getRaster();

        int[] defaultColor = {255, 255, 255};
        int[] nullColor = {0, 0, 0};
        for (int i = 0; i < nRows*nCols; i++) {
            int[] color = defaultColor;
            System.out.println(values[i]);
            if (values[i] != nullValue) {
                for (int n = 0; n < valuesOfInterest.length; n++) {
                    if (values[i] == valuesOfInterest[n]) {
                        color = colors[n];
                        break;
                    }
                }
            } else {
                color = nullColor;
            }
            raster.setPixel(i/nCols, i%nCols, color);
        }
        return image;
    }

    public Layer localSum(Layer that, String resultName) {
        Layer result = new Layer(resultName, nRows, nCols, origin, resolution, nullValue);
        for (int i = 0; i < nRows*nCols; i++) {
            if(this.values[i] != nullValue && that.values[i] != nullValue) {
                result.values[i] = this.values[i] + that.values[i];
            } else {
                result.values[i] = nullValue;
            }
        }
        return result;
    }

    public Layer focalVariety(int radius, boolean isSquare, String resultName) {
        Layer result = new Layer(resultName, nRows, nCols, origin, resolution, nullValue);
        for (int i = 0; i < nRows*nCols; i++) {
            int[] neighbors = buildNeighborhood(i, radius, isSquare);
            Set<String> v = new HashSet<>();
            for(int n: neighbors) {
                v.add(String.valueOf(this.values[n]));
            }
            result.values[i] = v.size();
        }
        return result;
    }

    public Layer zonalMinimum(Layer zones, String resultName) {
        Layer result = new Layer(resultName, nRows, nCols, origin, resolution, nullValue);
        HashMap<Integer, Double> zoneMin = new HashMap<>();

        for (int i = 0; i < nRows*nCols; i++) {

            if (zones.values[i] != zones.nullValue && values[i] != nullValue) {
                int zone = (int) zones.values[i];
                double value = values[i];
                zoneMin.merge(zone, value, Math::min);
            }
        }

        for (int i = 0; i < nRows*nCols; i++) {
            if (zones.values[i] != zones.nullValue) {
                int zone = (int) zones.values[i];
                result.values[i] = zoneMin.getOrDefault(zone, nullValue);
            }
        }

        return result;
    }

    private int[] buildNeighborhood(int i, int radius, boolean isSquare){
        ArrayList<Integer> neighbors = new ArrayList<Integer>();
        Integer neighborObj;

        int ix = i/nCols; int iy = i%nCols;
        for (int j = 0; j<nRows*nCols; j++) {
            int jx = j/nCols; int jy = j%nCols;
            if(Math.abs(ix-jx) <= radius && Math.abs(iy-jy) <= radius ) {
                if (isSquare || Math.sqrt((ix - jx) ^ 2 + (iy - jy) ^ 2) <= radius + 0.5) {
                    neighbors.add(Integer.valueOf(j));
                }
            }
        }
        return neighbors.stream().mapToInt(Integer::intValue).toArray();
    }

    private double getMin() {
        double currentBest = Double.POSITIVE_INFINITY;
        for (double value : values) {
            if (value != nullValue && value < currentBest) {
                currentBest = value;
            }
        }
        return currentBest;
    }

    private double getMax() {
        double currentBest = Double.NEGATIVE_INFINITY;
        for (double value : values) {
            if (value != nullValue && value > currentBest) {
                currentBest = value;
            }
        }
        return currentBest;
    }

    private int[] generateRandomRGBColor() {
        Random random = new Random();

        float hue = random.nextFloat() * 360f;
        float saturation = 0.4f + random.nextFloat() * 0.35f; // 50–80%
        float lightness = 0.4f + random.nextFloat() * 0.3f; // 40–60%

        return hsl2Rgb(hue, saturation, lightness);
    }

    private int[] hsl2Rgb(float h, float s, float l) {
        float c = (1 - Math.abs(2 * l - 1)) * s;
        float x = c * (1 - Math.abs((h / 60f) % 2 - 1));
        float m = l - c / 2;

        float r = 0, g = 0, b = 0;

        if (h < 60) {
            r = c; g = x;
        } else if (h < 120) {
            r = x; g = c;
        } else if (h < 180) {
            g = c; b = x;
        } else if (h < 240) {
            g = x; b = c;
        } else if (h < 300) {
            r = x; b = c;
        } else {
            r = c; b = x;
        }

        return new int[] {Math.round((r + m) * 255), Math.round((g + m) * 255), Math.round((b + m) * 255)};
    }
}
