import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;

import javax.imageio.ImageIO;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.imgcodecs.Imgcodecs;

public class Main {
	
	static String serverName = "roborio-2415-frc.local";
	static int port = 2415;
	
	static Socket client;
	
	public static void main(String[] args) {
		
		Mat image = new Mat();
		
		try{
			client = new Socket(serverName, port);
			BufferedImage img = ImageIO.read(ImageIO.createImageInputStream(client.getInputStream()));
			ImageIO.write(img, "JPG", new File("img.jpg"));
			client.close();
			System.out.println("Image recieved!");
			
			image = Imgcodecs.imread("img.jpg");
			
		}catch(Exception e){
			if(e instanceof UnknownHostException){
				System.out.println("[ERROR]: Couldn't find " + serverName + " on port " + port + "!");
			}
			if(e instanceof IOException){
				e.printStackTrace();
			}
		}
	}

}
