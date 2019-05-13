/*
 * This is part of a simple breakout clone.
 * Ball.java is the Model for the game ball
 * @author: Mickey Kim
 */
import javafx.geometry.Dimension2D;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import java.lang.Math;

public class Ball extends Circle {
   private final int BALLINIT_X = 51;
   private double height, width, ballRadius;
   private double ballX, ballY, velX, velY;
   private volatile boolean ballHitPaddle;
   private volatile boolean ballHitBrickTop;
   private volatile boolean ballHitBrickBottom;
   private volatile boolean ballHitBrickLeft;
   private volatile boolean ballHitBrickRight;
   private volatile boolean ballDead = false;

   public Ball(Dimension2D playfieldDims, double ballRad) {
      height = playfieldDims.getHeight();
      width = playfieldDims.getWidth();
      ballX = width/2;
      ballY = height - BALLINIT_X;
      ballRadius = ballRad;
      setCenterX(ballX);
      setCenterY(ballY);
      setRadius(ballRadius);
      setFill(Color.WHITE);
   }

   // Gameloop methods:
   void update() {
      ballX += velX;
      ballY += velY;
      handleObjectCollisions();
      handleWallCollisions();
      setCenterX(ballX);
      setCenterY(ballY);
   }

   void tether(double paddleLoc) {
      ballX = paddleLoc;
      setCenterX(ballX);
   }

   boolean isDead() {
      boolean ballStatus = ballDead;
      return ballStatus;
   }

   // Getters and Setters
   double getX() {
      double xValue = ballX;
      return xValue;
   }

   double getY() {
      double yValue = ballY;
      return yValue;
   }

   double getVelX() {
      double xVelValue = velX;
      return xVelValue;
   }

   double getVelY() {
      double yVelValue = velY;
      return yVelValue;
   }

   void setVelX(double gameSpeed) {
      velX = gameSpeed;
   }

   void setVelY(double gameSpeed) {
      velY = gameSpeed;
   }

   // Collision setters
   void hitPaddle() {
      ballHitPaddle = true;
   }

   void hitBrickTop() {
      ballHitBrickTop = true;
   }

   void hitBrickBottom() {
      ballHitBrickBottom = true;
   }

   void hitBrickLeft() {
      ballHitBrickLeft = true;
   }

   void hitBrickRight() {
      ballHitBrickRight = true;
   }

   // Collision Detection Handling (ball bouncing)
   private void handleObjectCollisions() {
      if (ballHitPaddle) {
         velY = -Math.abs(velY);
         ballHitPaddle = false;
      } else if (ballHitBrickTop){
         velY = -Math.abs(velY);
         ballHitBrickTop = false;
      } else if (ballHitBrickBottom){
         velY = Math.abs(velY);
         ballHitBrickBottom = false;
      } else if (ballHitBrickLeft) {
         velX = -Math.abs(velX);
         ballHitBrickLeft = false;
      } else if (ballHitBrickRight){
         velX = Math.abs(velX);
         ballHitBrickRight = false;
      }
   }

   private void handleWallCollisions() {
      if (ballX + ballRadius >= width) {
         ballX = width - ballRadius;
         velX *= -1;
      } else if (ballX - ballRadius < 0) {
         ballX = 0 + ballRadius;
         velX *= -1;
      } else if (ballY - ballRadius < 0) {
         ballY = 0 + ballRadius;
         velY *= -1;
      } else if (ballY + ballRadius >= height) {
         ballY = height - ballRadius;
         velY = 0;
         velX = 0;
         ballDead = true;
      }
   }
}
