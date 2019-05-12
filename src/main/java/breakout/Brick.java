/*
 * This is part of a simple breakout clone.
 * Brick.java is the Model for a game brick; myHealth is brick health
 * @author: Mickey Kim
 */
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

public class Brick extends Rectangle {
   private final double width, height;
   private double leftBounds, rightBounds, upperBounds, lowerBounds;
   private double arcWidth = 5;
   private double arcHeight = 5;
   private int myHealth;

   public Brick(double x, double y, Color color, double width, double height, int health) {
      this.width = width;
      this.height = height;
      leftBounds = x;
      rightBounds = x + width;
      upperBounds = y;
      lowerBounds = y + height;
      myHealth = health;
      setX(x);
      setY(y);
      setWidth(width);
      setHeight(height);
      setArcWidth(arcWidth);
      setArcHeight(arcHeight);
      setFill(color);
   }

   // Getters for brick sides (bounds)
   double getXLeft() {
      double value = leftBounds;
      return value;
   }

   double getXRight() {
      double value = rightBounds;
      return value;
   }
   
   double getYUpper() {
      double value = upperBounds;
      return value;
   }

   double getYLower() {
      double value = lowerBounds;
      return value;
   }
}
