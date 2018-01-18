//Created by Glael
//license: see bottom of document

import java.io.File;
import java.io.IOException;
import java.awt.image.BufferedImage;
import java.util.Random;
import java.util.Vector;
import javax.imageio.ImageIO;
import static java.lang.Math.*;

public class ImageProcessor {
    public static void main(String args[])throws IOException{

        ///////////////////config//////////////////////////////////////////////////////////////////////////////////////////////////////////
        String fileName = "ohno.jpg"; //the file to recreate
        String folder = "results"; //the folder where the resulting images should be placed
        int outputImageEveryXLines = 1000; //how often an image should be created. (every x lines drawn) (the lower, the slower)
        int continueForXLines = 250000; //how many lines should be drawn in total
        double accuracy = 1.6; //how precise the algorithm should be (new is x times as good as old) (ALWAYS MORE THAN ONE)
        ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
        //to make a webm (or other video) from the images, open the terminal in the folder chosen above, and use the following command:
        //
        //ffmpeg -i %11d.png -framerate 10 -c:v vp9 output.webm
        //
        //this will take a while. if it doesn't work, install ffmpeg first (google is your friend)
        ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////



        BufferedImage originalImage = null;
        BufferedImage newImage = null;
        File f = null;
        try{
            f = new File(fileName);
            originalImage = ImageIO.read(f);
            newImage = ImageIO.read(f);
        }catch(IOException e){
            System.out.println("Error: "+e);
        }
        fillImageWithColour(newImage, 255, 0, 0, 0);
        int width = originalImage.getWidth();
        int height = originalImage.getHeight();
        Random rand = new Random(System.currentTimeMillis());
        boolean repeat = false;
        for (int iteration = 0; iteration <= continueForXLines; ++iteration) {
            if (tryToDrawLine(originalImage, newImage, iteration, repeat, folder, outputImageEveryXLines, accuracy, width, height, rand)) {
                repeat = false;
            }
            else {
                iteration--;
                repeat = true;
            }
        }
    }


    /**
     * Try to draw a line. if the line is accepted, draw it, if not reject. return true or false based on the result of the test
     * @param originalImage the image that should be recreated
     * @param newImage the new image, that is currently being drawn
     * @param iteration the amount of lines that have already been drawn
     * @param repeat true if the previous line was rejected (don't output the image again)
     * @param folder the folder where images should be placed
     * @param outputImageEveryXLines how often an image should be printed, in relation to the iteration
     * @param accuracy how much "better" the new image should be (multiplier)
     * @param width the width of the image
     * @param height the height of the image
     * @param rand a random number generator
     * @return a boolean to indicate whether a line was drawn
     */
    private static boolean tryToDrawLine(BufferedImage originalImage, BufferedImage newImage, int iteration, boolean repeat, String folder, int outputImageEveryXLines, double accuracy, int width, int height, Random rand) {
        if (iteration % outputImageEveryXLines == 0 && !(repeat)) {
            System.out.println(iteration);
            printFile(folder, iteration, newImage, outputImageEveryXLines);
        }
        int ax = rand.nextInt(width);
        int ay = rand.nextInt(height);
        int bx = rand.nextInt(100) -50 + ax;
        int by = rand.nextInt(100) -50 + ay;
        while (ax == bx && ay == by || bx < 0 || by < 0 || bx > width || by > height) {
            bx = rand.nextInt(100) -50 + ax;
            by = rand.nextInt(100) -50 + ay;
        }
        int randomColour = originalImage.getRGB(rand.nextInt(width), rand.nextInt(height));
        int originalImageAverage = calcAverageDiffBetweenColourOfLineAndCompareColour(originalImage, randomColour, ax, ay, bx, by);
        int newImageAverage = calcAverageDiffBetweenColourOfLineAndCompareColour(newImage, randomColour, ax, ay, bx, by);
        if (abs(newImageAverage) > accuracy*abs(originalImageAverage)) {
            drawLine(newImage, randomColour, ax, ay, bx, by);
            return true;
        }
        else {
            return false;
        }
    }


    /**
     * Draw a line on the image in the given colour
     * @param image the image to draw on
     * @param colour a the colour to draw in
     * @param ax x-coord of point a
     * @param ay y-coord of point a
     * @param bx x-coord of point b
     * @param by y-coord of point b
     */
    private static void drawLine(BufferedImage image, int colour, int ax, int ay, int bx, int by) {
        int dx = bx - ax;
        int dy = by - ay;
        if (abs(dx) > abs(dy)) {
            int maxX = max(ax, bx);
            int minX = min(ax, bx);
            for (int x = minX; x < maxX; ++x) {
                int y = ay + dy * (x - minX) / abs(dx);
                image.setRGB(x, y, colour);
            }
        }
        else {
            int maxY = max(ay, by);
            int minY = min(ay, by);
            for (int y = minY; y < maxY; ++y) {
                int x = ax + dx * (y - minY) / abs(dy);
                image.setRGB(x, y, colour);
            }
        }
    }


    /**
     * Output the given image to the given folder, with a filename based on the current iteration
     * @param folder the folder to output the image to
     * @param iteration the current iteration of the algorithm
     * @param image the image to output
     * @param outputImageEveryXLines how often the image is printed, in relation to the iteration
     */
    private static void printFile(String folder, int iteration, BufferedImage image, int outputImageEveryXLines) {
        try{
            String filename = ("00000000000".substring((new Integer(iteration/outputImageEveryXLines)).toString().length()) + (int)(iteration/outputImageEveryXLines));
            File f = new File((folder + "/" + filename +".png"));
            ImageIO.write(image, "png", f);
        }catch(IOException e){
            System.out.println("Error: "+e);
        }
    }

    /**
     * walk over every pixel, and fill with the given colour
     * @param image the image to fill
     * @param a gamma-component of the colour
     * @param r red-component of the colour
     * @param g green-component of the colour
     * @param b blue-component of the colour
     */
    private static void fillImageWithColour(BufferedImage image, int a, int r, int g, int b) {
        int p = (a<<24) | (r<<16) | (g<<8) | b;

        int width = image.getWidth();
        int height = image.getHeight();
        for (int x = 0; x < width; ++x) {
            for (int y = 0; y < height; y++) {
                image.setRGB(x, y, p);
            }
        }
    }


    /**
     * Walk over line, and calculate difference between pixel colour and compareColour. average those differences.
     * @param image the image to get color values from
     * @param compareColour the color to compare to
     * @param ax x-coord of point a
     * @param ay y-coord of point a
     * @param bx x-coord of point b
     * @param by y-coord of point b
     * @return the average difference between the colour of the line, and compareColour
     */
    private static int calcAverageDiffBetweenColourOfLineAndCompareColour(BufferedImage image, int compareColour, int ax, int ay, int bx, int by) {


        Vector<Integer> pixelList = new Vector<>();

        int dx = bx - ax;
        int dy = by - ay;
        if (abs(dx) > abs(dy)) {
            int maxX = max(ax, bx);
            int minX = min(ax, bx);
            for (int x = minX; x < maxX; ++x) {
                int y = ay + dy * (x - minX) / abs(dx);
                try {
                    pixelList.add(image.getRGB(x,y));
                }
                catch (ArrayIndexOutOfBoundsException e) {
                    System.err.println(e + "\n x =" + x + ", y =" + y + " " + ax + " " + ay + " " + bx + " " + by);
                }
            }
        }
        else {
            int maxY = max(ay, by);
            int minY = min(ay, by);
            for (int y = minY; y < maxY; ++y) {
                int x = ax + dx * (y - minY) / abs(dy);
                try {
                    pixelList.add(image.getRGB(x,y));
                }
                catch (ArrayIndexOutOfBoundsException e) {
                    System.err.println(e + "\n x =" + x + ", y =" + y + " " + ax + " " + ay + " " + bx + " " + by);
                }
            }
        }
        int rC = (compareColour>>16) & 0xff;
        int gC = (compareColour>>8) & 0xff;
        int bC = compareColour & 0xff;

        int a = 255;
        long sum = 0;
        for (int i:pixelList) {
            int rA = (i>>16) & 0xff;
            int gA = (i>>8) & 0xff;
            int bA = i & 0xff;

            sum += (int)sqrt((rA-rC)*(rA-rC)+(gA-gC)*(gA-gC)+(bA-bC)*(bA-bC));
        }
        return (int)sum / pixelList.size();
    }
}





/*
* Version 2, December 2004
*
* Everyone is permitted to copy and distribute verbatim or modified
* copies of this license document, and changing it is allowed as long
* as the name is changed.
*
*           DO WHAT THE FUCK YOU WANT TO PUBLIC LICENSE
*  TERMS AND CONDITIONS FOR COPYING, DISTRIBUTION AND MODIFICATION
*
*    0. You just DO WHAT THE FUCK YOU WANT TO.
*/