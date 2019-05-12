/*
 * This is a simple breakout clone.
 * Breakout.java is the Controller and View (JavaFX) for the game
 * @author: Mickey Kim
 */

import java.io.File;
import java.net.URL;
import java.util.*;
import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Dimension2D;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.*;
import javafx.scene.*;
import javafx.scene.image.*;
import javafx.scene.media.AudioClip;
import javafx.animation.Timeline;
import javafx.animation.KeyFrame;
import javafx.util.Duration;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;

public class Breakout extends Application {
   private final int FPS60MS  = 16;
   private final int WIDTH    = 600;
   private final int HEIGHT   = 900;
   private final int PWIDTH   = 100;
   private final int PHEIGHT  = 15;
   private final int BWIDTH   = 70;
   private final int BHEIGHT  = 20;
   private final int BRICK_XOFFSET = 10;
   private final int BRICK_YOFFSET = 70;
   private final int BRICK_XGAP    = 85;
   private final int BRICK_YGAP    = 30;
   private final double BALL_RADIUS  = 10;
   private final double BALL_SPEED   = 6;
   private final double PADDLE_SPEED = 8;
   private final double PADDLE_XMOD  = 0.8;
   private boolean roundHasStarted = false;
   private Color brickColors[] = {Color.RED, Color.ORANGE, 
                                  Color.YELLOW, Color.GREEN, Color.BLUE, 
                                  Color.MAGENTA, Color.PURPLE};
   // Free sounds downloaded from : https://www.noiseforfun.com
   // Win sound from : https://archive.org/details/FF7ACVictoryFanfareRingtoneperfectedMp3
   private final String paddleHit = "sound/paddle_hit.wav";
   private final String brickHit = "sound/brick_hit.wav";
   private final String died = "sound/died.wav";
   private final String gameover = "sound/gameover.wav";
   private final String gamewin = "sound/win.wav";

   private List<Brick> brickList = new ArrayList<Brick>();
   private Ball gameBall;
   private Paddle gamePaddle;
   private Group root;

   @Override
   public void start(Stage stage) throws Exception {
      stage.setTitle("Breakout!");
      Image photo = new Image("bg_stars.png"); 
      ImageView view = new ImageView(photo);
      root = new Group(view);
      Scene scene = new Scene(root, WIDTH, HEIGHT);
      createKeyHandler(root);
      resetBallPaddleAndDestroyOld(root, false);
      spawnNewBricks(root);
      stage.setScene(scene);
      stage.show();

      // Main Game Loop
      Timeline gameLoop = new Timeline();
      gameLoop.setCycleCount(Timeline.INDEFINITE);
      KeyFrame kf = new KeyFrame(Duration.millis(FPS60MS), (this::gameEvents));
      gameLoop.getKeyFrames().add(kf);
      gameLoop.play();
   }

   private void gameEvents(ActionEvent e) {
      if (roundHasStarted) {
         gameBall.update();
      } else {
         gameBall.tether(gamePaddle.getMidX());
      }
      gamePaddle.update();
      checkBrickCollisions();
      checkPaddleCollision();
      if (gameBall.isDead()) {
         resetBallPaddleAndDestroyOld(root, true);
         playSound("died");
      }
      if (brickList.isEmpty()) {
         playSound("win");
         resetBallPaddleAndDestroyOld(root, true);
         spawnNewBricks(root);
      }
   }

   private void spawnNewBricks(Group root) {
      if (!brickList.isEmpty()) {
         for (Brick currBrick : brickList) {
            destroyBrick(currBrick);
         }
      }
      createBricks(root);
   }

   private void resetBallPaddleAndDestroyOld(Group root, boolean destroyOld) {
      if (destroyOld) {
         root.getChildren().remove(gameBall);
         root.getChildren().remove(gamePaddle);
      }
      createPaddle(root);
      createNewBall(root);
   }

   private void checkBrickCollisions() {
      for (Brick currBrick : brickList) {
         if (currBrick.getBoundsInLocal().intersects(gameBall.getBoundsInLocal())) {
            if (gameBall.getY() < currBrick.getYUpper()) {
               gameBall.hitBrickTop();
            }
            if (gameBall.getY() > currBrick.getYLower()) {
               gameBall.hitBrickBottom();
            }
            if (gameBall.getX() < currBrick.getXLeft()) {
               gameBall.hitBrickLeft();
            }
            if (gameBall.getX() > currBrick.getXRight()) {
               gameBall.hitBrickRight();
            }
            destroyBrick(currBrick);
            playSound("brick");
         }
      }
   }

   // This method is necessary to avoid ConcurrentModificationException errors
   // Solution adapted from : https://stackoverflow.com/q/16125311
   private void destroyBrick(Brick brick) {
      Platform.runLater(new Runnable() {
         @Override
         public void run() {
            brickList.remove(brick);
            root.getChildren().remove(brick);
         }
      });
   }

   // Ball movement velocities adapted from : https://gamedev.stackexchange.com/a/21048
   private void checkPaddleCollision() {
      if (gameBall.getBoundsInLocal().intersects(gamePaddle.getBoundsInLocal())) {
         gameBall.hitPaddle();
         double speedX = gameBall.getVelX();
         double speedY = gameBall.getVelY();
         double posX = (gameBall.getX() - gamePaddle.getMidX()) / (PWIDTH / 2);
         double speedXY = Math.sqrt(speedX * speedX + speedY * speedY);
         speedX = speedXY * posX * PADDLE_XMOD;
         gameBall.setVelX(speedX);
         gameBall.setVelY(Math.sqrt(speedXY * speedXY - speedX * speedX) *
                                    (speedY > 0 ? -1 : 1));
         playSound("paddle");
      }
   }

   // Create KeyEvent caller attached to an invisible rectangle object
   private void createKeyHandler(Group root) {
      final Rectangle keyboardNode = new Rectangle();
      keyboardNode.setFocusTraversable(true);
      keyboardNode.requestFocus();
      keyboardNode.setOnKeyPressed(this::keyPressed);
      keyboardNode.setOnKeyReleased(this::keyReleased);
      root.getChildren().add(keyboardNode);
   }

   private void createNewBall(Group root) {
      gameBall = new Ball(new Dimension2D(WIDTH, HEIGHT), BALL_RADIUS);
      gameBall.setVelX(BALL_SPEED);
      gameBall.setVelY(-BALL_SPEED);
      root.getChildren().add(gameBall);
   }

   private void createPaddle(Group root) {
      roundHasStarted = false;
      gamePaddle = new Paddle(new Dimension2D(WIDTH, HEIGHT), PWIDTH, PHEIGHT);
      root.getChildren().add(gamePaddle);
   }

   private void createBricks(Group root) {
      int xPos = BRICK_XOFFSET;
      int yPos = BRICK_YOFFSET;
      int colorIndex = brickColors.length - 1;
      for (int i = 0; i < brickColors.length; i++) {
         for (int j = 0; j < brickColors.length; j++) {
            Brick gameBrick = new Brick(xPos, yPos, brickColors[colorIndex], BWIDTH, BHEIGHT, 1);
            xPos += BRICK_XGAP;
            root.getChildren().add(gameBrick);
            brickList.add(gameBrick);
         }
         colorIndex--;
         xPos = BRICK_XOFFSET;
         yPos += BRICK_YGAP;
      }
   }

   private void keyPressed(KeyEvent key) {
      if (key.getCode() == KeyCode.SPACE && roundHasStarted == false) {
         playSound("paddle");
         roundHasStarted = true;
      }
      if (key.getCode() == KeyCode.RIGHT || key.getCode() == KeyCode.D) {
         gamePaddle.setVelX(PADDLE_SPEED);
      } else if (key.getCode() == KeyCode.LEFT || key.getCode() == KeyCode.A) {
         gamePaddle.setVelX(-PADDLE_SPEED);
      }
   }

   // Need to fix jerkiness on change of directions while touching both left & right
   private void keyReleased(KeyEvent key) {
      if (key.getCode() == KeyCode.RIGHT || key.getCode() == KeyCode.LEFT ||
          key.getCode() == KeyCode.D || key.getCode() == KeyCode.A) {
            gamePaddle.setVelX(0);
      }
   }

   private void playSound(String select) {
      URL path = null;
      AudioClip ac;
      switch (select) {
         case "paddle":
            path = getClass().getResource(paddleHit);
            break;
         case "brick":
            path = getClass().getResource(brickHit);
            break;
         case "died":
            path = getClass().getResource(died);
            break;
         case "over":
            path = getClass().getResource(gameover);
            break;
         case "win":
            path = getClass().getResource(gamewin);
            break;
         default:
      }
      ac = new AudioClip(path.toString());
      ac.play();
   }

   public static void main(String[] args) {
      launch(args);
   }
}
