import java.awt.image.BufferedImage;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Iterator;

import javax.imageio.ImageIO;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

public class Main {
	
	static String serverName = "roborio-2415-frc.local";
	static int port = 2415;
	
	static Socket client;
	
	public static final Scalar BLACK = new Scalar(0,0,0),
		LOWER_BOUNDS = new Scalar(58,0,109),
		UPPER_BOUNDS = new Scalar(93,255,240);
	
	public static final double SCREEN_CENTER_X = 160;
	public static final double SCREEN_CENTER_Y = 120;
	
	public static double error;
	
	public static void main(String[] args) {
		
		Mat matOriginal = new Mat();
		Mat matHSV = new Mat();
		Mat matThresh = new Mat();
		Mat clusters = new Mat();
		Mat matHeirarchy = new Mat();
		Mat image = new Mat();
		
		try{
			client = new Socket(serverName, port);
			BufferedImage img = ImageIO.read(ImageIO.createImageInputStream(client.getInputStream()));
			ImageIO.write(img, "JPG", new File("img.jpg"));
			client.close();
			System.out.println("Image recieved!");
			
			matOriginal = Imgcodecs.imread("img.jpg");
			
			ArrayList<MatOfPoint> contours = new ArrayList<MatOfPoint>();	
			Imgproc.cvtColor(matOriginal,matHSV,Imgproc.COLOR_BGR2HSV);			
			Core.inRange(matHSV, LOWER_BOUNDS, UPPER_BOUNDS, matThresh);
			Imgproc.findContours(matThresh, contours, matHeirarchy, Imgproc.RETR_EXTERNAL, 
				Imgproc.CHAIN_APPROX_SIMPLE);
			
			for (Iterator<MatOfPoint> iterator = contours.iterator(); iterator.hasNext();) {
				MatOfPoint matOfPoint = (MatOfPoint) iterator.next();
				Rect rec = Imgproc.boundingRect(matOfPoint);
					if(rec.height < 25 || rec.width < 25){
						iterator.remove();
					continue;
					}
					float aspect = (float)rec.width/(float)rec.height;
					if(aspect < 1.0)
						iterator.remove();
				}
				for(MatOfPoint mop : contours){
					Rect rec = Imgproc.boundingRect(mop);
					Imgproc.rectangle(matOriginal, rec.br(), rec.tl(), BLACK);
			}
//				if there is only 1 target, then we have found the target we want
			if(contours.size() == 1){
				Rect rec = Imgproc.boundingRect(contours.get(0));
				
				double goalCenterX = rec.br().x - (rec.width/2);
				double goalCenterY = rec.tl().y - (rec.height/2);
				error = Math.sqrt((Math.pow(goalCenterX,2)-Math.pow(SCREEN_CENTER_X,2)) + 
						(Math.pow(goalCenterY,2)-Math.pow(SCREEN_CENTER_Y,2)));
			}
			
			DataOutputStream out = new DataOutputStream(client.getOutputStream());
			out.writeDouble(error);
			client.close();
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
