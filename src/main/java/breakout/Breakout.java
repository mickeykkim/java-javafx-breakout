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
import javafx.geometry.Pos;
import javafx.geometry.HPos;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.*;
import javafx.scene.*;
import javafx.scene.image.*;
import javafx.scene.control.*;
import javafx.scene.layout.*;
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
   private final String paddleHit = "audio/paddle_hit.wav";
   private final String brickHit  = "audio/brick_hit.wav";
   private final String died      = "audio/died.wav";
   private final String gameover  = "audio/gameover.wav";
   private final String gamewin   = "audio/win.wav";
   private final String bgImg     = "image/bg_stars.png";

   private List<Brick> brickList = new ArrayList<Brick>();
   private Ball gameBall;
   private Paddle gamePaddle;
   private StackPane splash = new StackPane();
   private GridPane topText = new GridPane();
   private int playerLives = 3;
   private int currentLevel = 1;
   private int currentScore = 0;
   private Group root;

   private final Label labelStart = new Label(
      "Breakout!\n\nPress Space to start a new game" +
      "\nMove with arrow keys or A and D\nPress ESC to exit game");
   private final Label labelGameover = new Label(
      "Game Over\n\n" + "Your score: " + currentScore +
      "\nPress Space to start a new game" +
      "\nPress ESC to exit game");
   private final Label labelGamewon = new Label(
      "Congratulations You Won!\n" +
      "\nPress Space to continue to level " + currentLevel +
      "\nPress ESC to exit game");
   private static final int LABELPADD = 40;
   private static final int LABELX = 130;
   private static final int LABELY = 400;

   @Override
   public void start(Stage stage) throws Exception {
      stage.setTitle("Breakout!");
      Image photo = new Image(getClass().getResource(bgImg).toURI().toString()); 
      ImageView view = new ImageView(photo);
      root = new Group(view);
      Scene scene = new Scene(root, WIDTH, HEIGHT);

      createKeyHandler(root);
      resetBallPaddleAndDestroyOld(root, false);
      spawnNewBricks(root);
      createSplashScreen(root, labelStart);
      createTopText(root);

      stage.setScene(scene);
      stage.setResizable(false);
      stage.show();

      // Main Game Loop
      Timeline gameLoop = new Timeline();
      gameLoop.setCycleCount(Timeline.INDEFINITE);
      KeyFrame kf = new KeyFrame(Duration.millis(FPS60MS), (this::gameEvents));
      gameLoop.getKeyFrames().add(kf);
      gameLoop.play();
   }

   private void createSplashScreen(Group root, Label text) {
      text.setStyle("-fx-text-fill: white; -fx-font: bold 20 \"serif\"; -fx-padding: 20 20 20 20; -fx-text-alignment: center");
      splash.getChildren().addAll(text);
      splash.setStyle("-fx-background-color: rgba(0, 100, 100, 0.5); -fx-background-radius: 10;");
      splash.setMaxWidth(WIDTH - LABELPADD);
      splash.setMaxHeight(HEIGHT - LABELPADD);
      splash.setTranslateX(LABELX);
      splash.setTranslateY(LABELY);
      root.getChildren().add(splash);
   }

   private void createTopText(Group root) {
      Label lives = new Label("Lives: " + playerLives);
      Label level = new Label("Level: " + currentLevel);
      Label score = new Label("Score: " + currentScore);
      lives.setStyle("-fx-text-fill: white; -fx-font: bold 20 \"serif\"; -fx-padding: 10 10 10 10");
      level.setStyle("-fx-text-fill: white; -fx-font: bold 20 \"serif\"; -fx-padding: 10 10 10 10");
      score.setStyle("-fx-text-fill: white; -fx-font: bold 20 \"serif\"; -fx-padding: 10 10 10 10");
      topText.add(lives, 0, 0);
      topText.add(level, 1, 0);
      topText.add(score, 2, 0);
      topText.getColumnConstraints().add(new ColumnConstraints(WIDTH/3));
      topText.getColumnConstraints().add(new ColumnConstraints(WIDTH/3));
      topText.getColumnConstraints().add(new ColumnConstraints(WIDTH/3));
      topText.setHalignment(lives, HPos.LEFT);
      topText.setHalignment(level, HPos.CENTER);
      topText.setHalignment(score, HPos.RIGHT);
      root.getChildren().add(topText);
   }

   private void updateTopText(Group root) {
      root.getChildren().remove(topText);
      topText.getChildren().clear();
      createTopText(root);
   }

   private void gameEvents(ActionEvent e) {
      if (roundHasStarted) {
         gameBall.update();
      } else {
         gameBall.tether(gamePaddle.getMidX());
      }
      gamePaddle.update();
      checkBrickCollisions();
      checkBrickAndWinStatus();
      checkPaddleCollision();
      checkBallAndLivesStatus();
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
            currentScore += 100;
            updateTopText(root);
         }
      }
   }

   private void checkBrickAndWinStatus() {
      if (brickList.isEmpty()) {
         playSound("win");
         resetBallPaddleAndDestroyOld(root, true);
         spawnNewBricks(root);
         currentLevel++;
         createSplashScreen(root, labelGamewon);
         updateTopText(root);
      }
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

   private void checkBallAndLivesStatus() {
      if (gameBall.isDead()) {
         resetBallPaddleAndDestroyOld(root, true);
         if (--playerLives == 0) {
            playSound("over");
            createSplashScreen(root, labelGameover);
            spawnNewBricks(root);
            playerLives = 3;
            currentScore = 0;
         } else {
            playSound("died");
         }
         updateTopText(root);
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
         root.getChildren().remove(splash);
         splash.getChildren().clear();
         roundHasStarted = true;
      }
      if (key.getCode() == KeyCode.RIGHT || key.getCode() == KeyCode.D) {
         gamePaddle.setVelX(PADDLE_SPEED);
      } else if (key.getCode() == KeyCode.LEFT || key.getCode() == KeyCode.A) {
         gamePaddle.setVelX(-PADDLE_SPEED);
      }
      if (key.getCode() == KeyCode.ESCAPE) {
         System.exit(0);
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
