package sample;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Bounds;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.layout.AnchorPane;
import javafx.scene.paint.Color;
import javafx.scene.robot.Robot;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;

import java.net.URL;
import java.util.ArrayList;
import java.util.ResourceBundle;

public class Controller implements Initializable {

	@FXML
	private AnchorPane scene;

	@FXML
	private Circle circle;

	@FXML
	private Rectangle paddle;

	@FXML
	private Rectangle bottomZone;

	@FXML
	private Button startButton;
	
	@FXML
	private Button GameLaunchPageButton;

	private int paddleStartSize = 600;

	Robot robot = new Robot();

	private ArrayList<Rectangle> bricks = new ArrayList<>();

	double deltaX = -1;
	double deltaY = -3;

	// 1 Frame evey 10 millis, which means 100 FPS
	Timeline timeline = new Timeline(new KeyFrame(Duration.millis(10), new EventHandler<ActionEvent>() {
	    @Override
	    public void handle(ActionEvent actionEvent) {
	        movePaddle();
	        checkCollisionPaddle(paddle);
	        circle.setLayoutX(circle.getLayoutX() + deltaX);
	        circle.setLayoutY(circle.getLayoutY() + deltaY);

	        if (!bricks.isEmpty()) {
	            bricks.removeIf(brick -> checkCollisionBrick(brick));
	            if (bricks.isEmpty()) { // Check if all bricks are gone
	                resetAndNavigateToStart(); // Call the method to reset the game and navigate
	            }
	        } else {
	            resetAndNavigateToStart(); // Call the method to reset the game and navigate
	        }

	        checkCollisionScene(scene);
	        checkCollisionBottomZone();
	    }
	}));


	@Override
	public void initialize(URL url, ResourceBundle resourceBundle) {
		paddle.setWidth(paddleStartSize);
		timeline.setCycleCount(Animation.INDEFINITE);
	}

	@FXML
	void startGameButtonAction(ActionEvent event) {
		startButton.setVisible(false);
		startGame();
	}
	@FXML
	void endGameButtonAction(ActionEvent event) {
		GameLaunchPageButton.setVisible(false);
		
	}

	public void startGame() {
		createBricks();
		timeline.play();
	}

	public void checkCollisionScene(Node node) {
		Bounds bounds = node.getBoundsInLocal();
		boolean rightBorder = circle.getLayoutX() >= (bounds.getMaxX() - circle.getRadius());
		boolean leftBorder = circle.getLayoutX() <= (bounds.getMinX() + circle.getRadius());
		boolean bottomBorder = circle.getLayoutY() >= (bounds.getMaxY() - circle.getRadius());
		boolean topBorder = circle.getLayoutY() <= (bounds.getMinY() + circle.getRadius());

		if (rightBorder || leftBorder) {
			deltaX *= -1;
		}
		if (bottomBorder || topBorder) {
			deltaY *= -1;
		}
	}

	public boolean checkCollisionBrick(Rectangle brick) {

		if (circle.getBoundsInParent().intersects(brick.getBoundsInParent())) {
			boolean rightBorder = circle.getLayoutX() >= ((brick.getX() + brick.getWidth()) - circle.getRadius());
			boolean leftBorder = circle.getLayoutX() <= (brick.getX() + circle.getRadius());
			boolean bottomBorder = circle.getLayoutY() >= ((brick.getY() + brick.getHeight()) - circle.getRadius());
			boolean topBorder = circle.getLayoutY() <= (brick.getY() + circle.getRadius());

			if (rightBorder || leftBorder) {
				deltaX *= -1;
			}
			if (bottomBorder || topBorder) {
				deltaY *= -1;
			}

			paddle.setWidth(paddle.getWidth() - (0.10 * paddle.getWidth()));
			scene.getChildren().remove(brick);

			return true;
		}
		return false;
	}

	public void createBricks() {
		double width = 560;
		double height = 200;
		String[] colors = { "RED", "BLUE", "ORGANGE", "GREEN" };
		int spaceCheck = 1;
		int h = 0;
		for (double i = height; i > 0; i = i - 50) {
			for (double j = width; j > 0; j = j - 25) {
				if (spaceCheck % 2 == 0) {
					Rectangle rectangle = new Rectangle(j, i, 30, 30);
					rectangle.setFill(Color.RED);
					h++;
					scene.getChildren().add(rectangle);
					bricks.add(rectangle);
					if (h == 4) {
						h = 0;
					}
				}
				spaceCheck++;

			}
		}
	}

	public void movePaddle() {
		Bounds bounds = scene.localToScreen(scene.getBoundsInLocal());
		double sceneXPos = bounds.getMinX();

		double xPos = robot.getMouseX();
		double paddleWidth = paddle.getWidth();

		if (xPos >= sceneXPos + (paddleWidth / 2) && xPos <= (sceneXPos + scene.getWidth()) - (paddleWidth / 2)) {
			paddle.setLayoutX(xPos - sceneXPos - (paddleWidth / 2));
		} else if (xPos < sceneXPos + (paddleWidth / 2)) {
			paddle.setLayoutX(0);
		} else if (xPos > (sceneXPos + scene.getWidth()) - (paddleWidth / 2)) {
			paddle.setLayoutX(scene.getWidth() - paddleWidth);
		}
	}

	public void checkCollisionPaddle(Rectangle paddle) {

		if (circle.getBoundsInParent().intersects(paddle.getBoundsInParent())) {

			boolean rightBorder = circle
					.getLayoutX() >= ((paddle.getLayoutX() + paddle.getWidth()) - circle.getRadius());
			boolean leftBorder = circle.getLayoutX() <= (paddle.getLayoutX() + circle.getRadius());
			boolean bottomBorder = circle
					.getLayoutY() >= ((paddle.getLayoutY() + paddle.getHeight()) - circle.getRadius());
			boolean topBorder = circle.getLayoutY() <= (paddle.getLayoutY() + circle.getRadius());

			if (rightBorder || leftBorder) {
				deltaX *= -1;
			}
			if (bottomBorder || topBorder) {
				deltaY *= -1;
			}
		}
	}

	public void checkCollisionBottomZone() {
		if (circle.getBoundsInParent().intersects(bottomZone.getBoundsInParent())) {
			timeline.stop();
			bricks.forEach(brick -> scene.getChildren().remove(brick));
			bricks.clear();
			startButton.setVisible(true);

			paddle.setWidth(paddleStartSize);

			deltaX = -1;
			deltaY = -3;

			circle.setLayoutX(300);
			circle.setLayoutY(300);

			System.out.println("Game over!");
		}
	}
	
	public void resetAndNavigateToStart() {
	    timeline.stop(); // Stop the game animation
	    bricks.forEach(brick -> scene.getChildren().remove(brick)); // Remove all bricks
	    bricks.clear(); // Clear the bricks list
	    startButton.setVisible(true); // Show the start button
	    GameLaunchPageButton.setVisible(true); // Show the game launch button if hidden

	    // Reset the ball and paddle positions
	    paddle.setWidth(paddleStartSize);
	    circle.setLayoutX(300);
	    circle.setLayoutY(300);

	    // Reset movement deltas
	    deltaX = -1;
	    deltaY = -3;

	    // Call the navigation method here
	    // navigateToStartScreen(); // Uncomment this line if you have a method to handle screen changes
	    System.out.println("Game reset, navigate to start!");
	}

}

