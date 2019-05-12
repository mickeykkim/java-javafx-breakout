/*
 * This is part of a simple breakout clone.
 * Paddle.java is the Model for the player controlled paddle
 * @author: Mickey Kim
 */
import javafx.geometry.Dimension2D;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

public class Paddle extends Rectangle {
   private final int PADDLEINIT_Y = -25;
   private double height, width, paddleWidth, paddleHeight;
   private double arcWidth = 15;
   private double arcHeight = 15;
   private double paddleX;
   private double velX = 0;

   public Paddle(Dimension2D playfieldDims, double pWidth, double pHeight) {
      height = playfieldDims.getHeight();
      width = playfieldDims.getWidth();
      paddleX = width/2 - pWidth/2;
      paddleWidth = pWidth;
      paddleHeight = pHeight;
      setX(paddleX);
      setY(height - pHeight + PADDLEINIT_Y);
      setWidth(pWidth);
      setHeight(pHeight);
      setArcWidth(arcWidth);
      setArcHeight(arcHeight);
      setFill(Color.CYAN);
   }

   // Getters and Setters
   void setVelX(double velocity) {
      velX = velocity;
   }

   double getMidX() {
      double locX = paddleX + paddleWidth/2;
      return locX;
   }

   // Gameloop methods:
   void update() {
      paddleX += velX;
      if (paddleX < 0) {
         paddleX = 0;
      } else if (paddleX + paddleWidth > width) {
         paddleX = width - paddleWidth;
      }
      setX(paddleX);
   }
}
