package it.polimi.ingsw.view.gui.components;

import it.polimi.ingsw.controller.states.CommonState;
import it.polimi.ingsw.controller.states.LobbyState;
import it.polimi.ingsw.enums.GameLevel;
import it.polimi.ingsw.enums.GamePhaseType;
import it.polimi.ingsw.model.player.Player;
import it.polimi.ingsw.util.GameLevelStandards;
import it.polimi.ingsw.util.Util;
import it.polimi.ingsw.view.gui.UIs.AdventureUI;
import it.polimi.ingsw.view.gui.UIs.AssembleUI;
import it.polimi.ingsw.view.gui.helpers.Asset;
import it.polimi.ingsw.view.gui.helpers.AssetHandler;
import javafx.animation.TranslateTransition;
import javafx.geometry.Point2D;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Ellipse;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.Shape;
import javafx.util.Duration;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Represents the main game board component in the GUI.
 * This component displays an elliptical path where player ships are placed and moved.
 * It handles the calculation of equidistant points along the ellipse, drawing the ellipse,
 * and managing the visual representation and animation of ships.
 */
public class BoardComponent extends VBox {

    /**
     * List of 2D points representing the calculated positions for each step on the ellipse.
     */
    private List<Point2D> ellipseStepPositions;
    /**
     * List of {@link Ship} objects currently displayed on the board.
     */
    private final List<Ship> ships = new ArrayList<>();
    /**
     * The horizontal radius of the ellipse.
     */
    private final double ellipseRx;
    /**
     * The vertical radius of the ellipse.
     */
    private final double ellipseRy;
    /**
     * The starting phase to build the positions in the board.
     */
    private final double phi;
    /**
     * The x-coordinate of the center of the ellipse.
     */
    private double ellipseCenterX;
    /**
     * The y-coordinate of the center of the ellipse.
     */
    private double ellipseCenterY;
    /**
     * The total number of discrete steps or positions along the ellipse.
     */
    private final int numberOfEllipseSteps;
    /**
     * The starting position of the first player in this FlightBoard
     */
    private final int inverseDeltaPlayerPos;
    /**
     * The default duration for ship movement animations.
     */
    private Duration defaultMoveDuration = Duration.millis(700);

    /**
     * The JavaFX Ellipse shape used for visually representing the path.
     */
    private Ellipse visualEllipse;
    /**
     * List of Circle markers used to visually indicate each step on the ellipse.
     */
    private final List<Circle> stepMarkers = new ArrayList<>();

    /**
     * The StackPane used to layer the background, ellipse, step markers, and ships.
     */
    private final StackPane boardDisplayPane;

    /**
     * Constructs a BoardComponent with specified ellipse dimensions and number of steps.
     *
     * @param ellipseRx The horizontal radius of the ellipse. Must be non-negative.
     * @param ellipseRy The vertical radius of the ellipse. Must be non-negative.
     * @param numberOfSteps The number of discrete steps along the ellipse. Must be positive.
     * @param inverseDeltaPlayerPos The starting position of the first player on the board.
     * @throws IllegalArgumentException if numberOfSteps is not positive.
     */
    private BoardComponent(double ellipseRx, double ellipseRy, double phi, int numberOfSteps, int inverseDeltaPlayerPos) {
        super();

        this.ellipseRx = Math.abs(ellipseRx);
        this.ellipseRy = Math.abs(ellipseRy);
        this.phi = phi;
        if (numberOfSteps <= 0) {
            throw new IllegalArgumentException("The number of steps must be greater than 0");
        }
        this.numberOfEllipseSteps = numberOfSteps;
        this.inverseDeltaPlayerPos = inverseDeltaPlayerPos;

        boardDisplayPane = new StackPane();

        Button backButton = new Button("Back");
        backButton.setOnMouseClicked(e -> {
            if (CommonState.isCurrentPhase(GamePhaseType.ASSEMBLE)){
                AssembleUI.getInstance().setAssembleLayout(AssembleUI.AssemblePane.PLAYER_BOARD);
            }else{
                AdventureUI.getInstance().setAdventureLayout(AdventureUI.AdventurePane.PLAYER_BOARD);
            }
        });
        this.getChildren().addAll(boardDisplayPane, backButton);

        boardDisplayPane.setMaxHeight(450);
        boardDisplayPane.setMinHeight(450);

        this.setSpacing(200);
        this.setAlignment(Pos.CENTER);
        VBox.setVgrow(boardDisplayPane, Priority.ALWAYS);

        boardDisplayPane.widthProperty().addListener((obs, oldVal, newVal) -> updateEllipseParametersAndPositions());
        boardDisplayPane.heightProperty().addListener((obs, oldVal, newVal) -> updateEllipseParametersAndPositions());

        setBackgroundImage();

        if (this.ellipseStepPositions == null) {
            calculateEllipseStepPositions();
        }
    }

    /**
     * Creates a new BoardComponent based on the specified game level.
     * @param gameLevel the level of the game
     * @return a new BoardComponent
     */
    public static BoardComponent create(GameLevel gameLevel) {
        double phi = 0;
        double rx = 0, ry = 0;
        int steps = 1;
        switch (gameLevel) {
            case TESTFLIGHT, ONE:
                rx = 285;
                ry = 150;
                // first position at (144, 178) from center
                phi = Math.atan2(178 / ry, 144 / rx);
                steps = 18;
                break;
            case TWO:
                rx = 285;
                ry = 160;
                // first position at (205, 191) from center
                phi = Math.atan2(191 / ry, 205 / rx);
                steps = 24;
                break;
        }
        return new BoardComponent(rx, ry, phi, steps,
                GameLevelStandards.getFlightBoardParkingLots(gameLevel).getFirst());
    }

    /**
     * Sets the background image for the board display pane.
     * The image is loaded using {@link AssetHandler} and configured to fit the pane.
     */
    private void setBackgroundImage() {
        Image image = AssetHandler.loadRawImage(Asset.BOARD.toString());
        BackgroundSize backgroundSize = new BackgroundSize(
                100, 100, true, true, true, false);
        BackgroundImage backgroundImage = new BackgroundImage(
                image,
                BackgroundRepeat.NO_REPEAT,
                BackgroundRepeat.NO_REPEAT,
                BackgroundPosition.CENTER,
                backgroundSize
        );
        boardDisplayPane.setBackground(new Background(backgroundImage));
    }

    /**
     * Gets the total number of steps defined for the ellipse.
     *
     * @return The number of ellipse steps.
     */
    public int getNumberOfEllipseSteps() {
        return numberOfEllipseSteps;
    }

    /**
     * Updates the ellipse center coordinates based on the current dimensions of the
     * {@code boardDisplayPane}. It then recalculates step positions, redraws ellipse visuals,
     * and repositions ships. This method is typically called when the pane's size changes.
     */
    private void updateEllipseParametersAndPositions() {
        if (boardDisplayPane.getWidth() > 0 && boardDisplayPane.getHeight() > 0) {
            this.ellipseCenterX = boardDisplayPane.getWidth() / 2 - 6;
            this.ellipseCenterY = boardDisplayPane.getHeight() / 2;
            calculateEllipseStepPositions();
            drawOrUpdateEllipseVisuals();
            repositionShipsToCurrentSteps();
        }
    }

    /**
     * Calculates equidistant points along the perimeter of the ellipse.
     * It handles cases for proper ellipses, as well as degenerate cases like lines or a point.
     * The calculated points are stored in {@code ellipseStepPositions}.
     * This method uses a numerical approximation by sampling points along the ellipse
     * and then interpolating to find equidistant points.
     */
    private void calculateEllipseStepPositions() {
        this.ellipseStepPositions = new ArrayList<>();

        if (numberOfEllipseSteps <= 0) {
            return;
        }

        // Handle degenerate cases (point or line)
        if (ellipseRx < 1e-6 && ellipseRy < 1e-6) { // Ellipse is a point
            Point2D centerPoint = new Point2D(ellipseCenterX, ellipseCenterY);
            for (int i = 0; i < numberOfEllipseSteps; i++) {
                this.ellipseStepPositions.add(centerPoint);
            }
            return;
        } else if (ellipseRx < 1e-6) { // Vertical line
            for (int i = 0; i < numberOfEllipseSteps; i++) {
                double y;
                if (numberOfEllipseSteps == 1) {
                    y = ellipseCenterY;
                } else {
                    y = ellipseCenterY - ellipseRy + (2.0 * ellipseRy * i) / (numberOfEllipseSteps - 1);
                }
                this.ellipseStepPositions.add(new Point2D(ellipseCenterX, y));
            }
            return;
        } else if (ellipseRy < 1e-6) { // Horizontal line
            for (int i = 0; i < numberOfEllipseSteps; i++) {
                double x;
                if (numberOfEllipseSteps == 1) {
                    x = ellipseCenterX;
                } else {
                    x = ellipseCenterX - ellipseRx + (2.0 * ellipseRx * i) / (numberOfEllipseSteps - 1);
                }
                this.ellipseStepPositions.add(new Point2D(x, ellipseCenterY));
            }
            return;
        }

        // For a proper ellipse
        final int SAMPLES = Math.max(4000, numberOfEllipseSteps * 100);
        final double deltaAngle = (2 * Math.PI) / SAMPLES;

        Point2D[] sampledPoints = new Point2D[SAMPLES + 1];
        double[] cumulativeArcLengths = new double[SAMPLES + 1];

        sampledPoints[0] = new Point2D(
                ellipseCenterX + ellipseRx * Math.cos(phi),
                ellipseCenterY - ellipseRy * Math.sin(phi)
        );
        cumulativeArcLengths[0] = 0;

        for (int i = 1; i <= SAMPLES; i++) {
            double angle = phi + i * deltaAngle;
            sampledPoints[i] = new Point2D(
                    ellipseCenterX + ellipseRx * Math.cos(angle),
                    ellipseCenterY - ellipseRy * Math.sin(angle)
            );
            cumulativeArcLengths[i] = cumulativeArcLengths[i-1] + sampledPoints[i-1].distance(sampledPoints[i]);
        }

        double totalPerimeter = cumulativeArcLengths[SAMPLES];

        if (totalPerimeter < 1e-9 && (ellipseRx > 1e-6 || ellipseRy > 1e-6)) {
            // Fallback to equi-angular distribution if perimeter calculation failed
            for (int i = 0; i < numberOfEllipseSteps; i++) {
                double angle = phi + (2 * Math.PI / numberOfEllipseSteps) * i;
                this.ellipseStepPositions.add(new Point2D(
                        ellipseCenterX + ellipseRx * Math.cos(angle),
                        ellipseCenterY - ellipseRy * Math.sin(angle)
                ));
            }
            return;
        }

        this.ellipseStepPositions.add(sampledPoints[0]);
        if (numberOfEllipseSteps == 1) {
            return;
        }

        double targetArcLengthPerStep = totalPerimeter / numberOfEllipseSteps;
        int currentSampleLookupIndex = 0;

        for (int k = 1; k < numberOfEllipseSteps; k++) {
            double desiredTotalLengthFromStart = k * targetArcLengthPerStep;

            while (currentSampleLookupIndex < SAMPLES && cumulativeArcLengths[currentSampleLookupIndex + 1] < desiredTotalLengthFromStart) {
                currentSampleLookupIndex++;
            }

            if (currentSampleLookupIndex >= SAMPLES) {
                this.ellipseStepPositions.add(sampledPoints[SAMPLES]);
                continue;
            }

            Point2D p1 = sampledPoints[currentSampleLookupIndex];
            Point2D p2 = sampledPoints[currentSampleLookupIndex+1];

            double lengthIntoSegment = desiredTotalLengthFromStart - cumulativeArcLengths[currentSampleLookupIndex];
            double segmentActualLength = cumulativeArcLengths[currentSampleLookupIndex+1] - cumulativeArcLengths[currentSampleLookupIndex];

            double ratio = (segmentActualLength < 1e-9) ? 0 : lengthIntoSegment / segmentActualLength;
            ratio = Math.max(0, Math.min(1, ratio));

            double newX = p1.getX() + ratio * (p2.getX() - p1.getX());
            double newY = p1.getY() + ratio * (p2.getY() - p1.getY());
            this.ellipseStepPositions.add(new Point2D(newX, newY));
        }
    }

    /**
     * Draws or updates the visual representation of the ellipse and its step markers.
     * If an ellipse or markers already exist, they are removed and redrawn.
     * The ellipse is only drawn if its radii are positive.
     */
    private void drawOrUpdateEllipseVisuals() {
        if (ellipseStepPositions == null || ellipseStepPositions.isEmpty()) return;

        if (visualEllipse != null) {
            boardDisplayPane.getChildren().remove(visualEllipse);
        }
        for (Circle marker : stepMarkers) {
            boardDisplayPane.getChildren().remove(marker);
        }
        stepMarkers.clear();

        if (ellipseRx > 1e-6 || ellipseRy > 1e-6) {
            visualEllipse = new Ellipse(ellipseCenterX, ellipseCenterY, ellipseRx, ellipseRy);
            visualEllipse.setFill(null);
            // visualEllipse.setStroke(Color.DIMGRAY);
            // visualEllipse.setStrokeWidth(0.7);
            boardDisplayPane.getChildren().add(0, visualEllipse);
        } else {
            visualEllipse = null;
        }

        for (Point2D stepPos : ellipseStepPositions) {
            Circle marker = new Circle(stepPos.getX(), stepPos.getY(), 3.5, Color.DARKSLATEGRAY);
            boardDisplayPane.getChildren().add(1, marker);
            stepMarkers.add(marker);
        }
    }

    /**
     * Repositions all ships currently on the board to their respective current step positions.
     * This is typically used after the ellipse parameters or step positions have been updated.
     * Ships are centered on their target step positions.
     */
    private void repositionShipsToCurrentSteps() {
        if (ellipseStepPositions == null || ellipseStepPositions.isEmpty()) return;

        for (Ship ship : ships) {
            if (ship.getCurrentStepIndex() >= 0 && ship.getCurrentStepIndex() < ellipseStepPositions.size()) {
                Point2D targetPos = ellipseStepPositions.get(ship.getCurrentStepIndex());

                double shapeWidth = ship.getWidth();
                double shapeHeight = ship.getHeight();
                double translateX = targetPos.getX() - shapeWidth / 2;
                double translateY = targetPos.getY() - shapeHeight / 2;

                ship.getShapeView().setTranslateX(translateX);
                ship.getShapeView().setTranslateY(translateY);
            }
        }
    }

    /**
     * Adds a new ship (represented by a {@link Shape}) to the ellipse at a specified initial step.
     * The ship is associated with a {@link Player}.
     *
     * @param shape The JavaFX Shape representing the ship.
     * @param player The player associated with this ship.
     * @return The created {@link Ship} object, or null if ellipse positions are not calculated.
     * @throws IllegalArgumentException if initialStepIndex is invalid.
     */
    private Ship addShapeToEllipse(Shape shape, Player player) {
        if (ellipseStepPositions == null || ellipseStepPositions.isEmpty()) {
            System.err.println("Ellipse positions not calculated. Ship cannot be added. Ensure the component is visible and has dimensions.");
            if (boardDisplayPane.getWidth() > 0 && boardDisplayPane.getHeight() > 0) {
                updateEllipseParametersAndPositions();
            }
            if (ellipseStepPositions == null || ellipseStepPositions.isEmpty()) {
                System.err.println("Failed to calculate ellipse positions even after trying to update.");
                return null;
            }
        }

        int initialStepIndex = Util.getModular(inverseDeltaPlayerPos - player.getPosition(), numberOfEllipseSteps);

        Ship ship = new Ship(shape, initialStepIndex, player);
        Point2D initialPos = ellipseStepPositions.get(initialStepIndex);

        double shapeWidth = ship.getWidth();
        double shapeHeight = ship.getHeight();
        ship.getShapeView().setTranslateX(initialPos.getX() - shapeWidth / 2);
        ship.getShapeView().setTranslateY(initialPos.getY() - shapeHeight / 2);

        this.ships.add(ship);
        boardDisplayPane.getChildren().add(ship.getShapeView());
        return ship;
    }

    /**
     * Moves a specified ship smoothly to a target step index on the ellipse using the default animation duration.
     * The target step index is normalized to be within the bounds of the ellipse steps.
     *
     * @param shipToMove The {@link Ship} to move.
     * @param targetStepIndex The target step index on the ellipse.
     */
    public void moveShapeSmoothly(Ship shipToMove, int targetStepIndex) {
        moveShapeSmoothly(shipToMove, targetStepIndex, defaultMoveDuration);
    }

    /**
     * Moves a specified ship smoothly to a target step index on the ellipse using a specified animation duration.
     * The target step index is normalized to be within the bounds of the ellipse steps.
     *
     * @param shipToMove The {@link Ship} to move.
     * @param targetStepIndex The target step index on the ellipse.
     * @param duration The duration of the movement animation.
     */
    public void moveShapeSmoothly(Ship shipToMove, int targetStepIndex, Duration duration) {
        if (shipToMove == null || !ships.contains(shipToMove)) {
            System.err.println("Ship not found or null.");
            return;
        }
        if (ellipseStepPositions == null || ellipseStepPositions.isEmpty()) {
            System.err.println("Ellipse positions not calculated. Cannot move ship.");
            return;
        }

        int normalizedTargetStepIndex = targetStepIndex % numberOfEllipseSteps;
        if (normalizedTargetStepIndex < 0) {
            normalizedTargetStepIndex += numberOfEllipseSteps;
        }
        if (normalizedTargetStepIndex >= ellipseStepPositions.size()) {
            System.err.println("Target step index " + normalizedTargetStepIndex + " is out of bounds for " + ellipseStepPositions.size() + " positions.");
            return;
        }

        Point2D targetPos = ellipseStepPositions.get(normalizedTargetStepIndex);

        double shapeWidth = shipToMove.getWidth();
        double shapeHeight = shipToMove.getHeight();
        double targetTranslateX = targetPos.getX() - shapeWidth / 2;
        double targetTranslateY = targetPos.getY() - shapeHeight / 2;

        TranslateTransition transition = new TranslateTransition();
        transition.setNode(shipToMove.getShapeView());
        transition.setDuration(duration);
        transition.setToX(targetTranslateX);
        transition.setToY(targetTranslateY);

        final int finalStepIndex = normalizedTargetStepIndex;
        transition.setOnFinished(event -> {
            shipToMove.setCurrentStepIndex(finalStepIndex);
        });

        transition.play();
    }

    /**
     * Sets the default duration for ship movement animations.
     *
     * @param duration The new default animation duration.
     */
    public void setDefaultMoveDuration(Duration duration) {
        this.defaultMoveDuration = duration;
    }

    /**
     * Removes all ships from the board display pane and clears the internal list of ships.
     */
    public void clearAllShapes() {
        for (Ship ship : ships) {
            boardDisplayPane.getChildren().remove(ship.getShapeView());
        }
        ships.clear();
    }

    /**
     * Adds player shapes (ships) to the board based on player data from {@link LobbyState}.
     * Each player gets a rectangular ship colored with their player color.
     * Ships are distributed around the ellipse.
     */
    public void addPlayers() {
        int SHIP_WIDTH = 20;
        int SHIP_HEIGHT = 20;
        /* DEBUG
        for (int i = 0; i < numberOfEllipseSteps; i++) {
            Rectangle r = new Rectangle(SHIP_WIDTH, SHIP_HEIGHT);
            r.setFill(i == 0 ? Color.RED : Color.GRAY);
            addShapeToEllipse(r, i, null);
        }
        */

        clearAllShapes();

        if (LobbyState.getGameData() != null && LobbyState.getGameData().getPlayers() != null) {
            for (Player p : LobbyState.getGameData().getPlayers(player -> Objects.nonNull(player.getPosition()))) { // Lambda corrected
                Rectangle r = new Rectangle(SHIP_WIDTH, SHIP_HEIGHT);
                r.setFill(Paint.valueOf(p.getColor().toString()));
                addShapeToEllipse(r, p);
            }
        } else {
            System.err.println("GameData or Players list is null, cannot add player shapes.");
        }
    }

    /**
     * Gets the StackPane used for displaying the board elements.
     *
     * @return The board display StackPane.
     */
    public StackPane getBoardDisplayPane() {
        return boardDisplayPane;
    }
}