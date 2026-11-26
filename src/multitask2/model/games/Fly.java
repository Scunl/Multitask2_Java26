
package multitask2.model.games;

import multitask2.controller.Input;
import multitask2.model.Game;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import com.github.forax.zen.KeyboardEvent.Key;

/**
 * Mini-jeu Fly : le joueur vole en maintenant la touche ESPACE
 * et descend automatiquement lorsqu'il la relâche.
 *
 * Des obstacles de tailles différentes traversent l'écran
 * de droite à gauche. Le joueur doit les éviter.
 */
public final class Fly implements Game {

  /** Largeur de la zone de jeu. */
  public static final double WIDTH = 100;

  /** Hauteur de la zone de jeu. */
  public static final double HEIGHT = 100;

  /** Position horizontale fixe du joueur. */
  private static final double PLAYER_X = 15;

  /** Taille du joueur. */
  private static final double PLAYER_SIZE = 5;

  /** Vitesse verticale du joueur lorsqu'il monte. */
  private static final double FLY_SPEED = 35;

  /** Force de gravité lorsque le joueur ne vole pas. */
  private static final double GRAVITY = 30;

  /** Vitesse initiale des obstacles. */
  private static final double BASE_OBSTACLE_SPEED = 30;

  /** Intervalle initial entre deux obstacles. */
  private static final double BASE_SPAWN_INTERVAL = 2;

  /**
   * Obstacle rectangulaire se déplaçant de droite à gauche.
   *
   * @param x position horizontale
   * @param y position verticale
   * @param width largeur de l'obstacle
   * @param height hauteur de l'obstacle
   */
  public record Obstacle(double x, double y, double width, double height) {

    /**
     * Déplace l'obstacle vers la gauche.
     */
    private Obstacle moveLeft(double dx) {
      return new Obstacle(x - dx, y, width, height);
    }
  }

  /** Position verticale du joueur. */
  private double playerY;

  /** Vitesse verticale du joueur. */
  private double velocityY;

  /** Liste des obstacles présents dans le jeu. */
  private final List<Obstacle> obstacles;

  /** Générateur aléatoire. */
  private final Random random;

  /** Temps restant avant la prochaine apparition. */
  private double spawnTimer;

  /**
   * Initialise le jeu avec le joueur au milieu de l'écran.
   */
  public Fly() {
    playerY = HEIGHT / 2;
    velocityY = 0;

    obstacles = new ArrayList<>();
    random = new Random();

    spawnTimer = 1;
  }

  /**
   * Met à jour le jeu.
   *
   * @param dt durée du pas de simulation en secondes
   * @param input état des entrées clavier
   * @param difficulty difficulté globale
   * @return true si le joueur n'a pas rencontré d'obstacle
   */
  @Override
  public boolean update(double dt, Input input, double difficulty) {
    Game.checkInput(dt, input, difficulty);

    movePlayer(dt, input);
    spawnObstacles(dt, difficulty);
    moveObstacles(dt, difficulty);

    return checkCollision();
  }

  /**
   * Déplace le joueur verticalement.
   * ESPACE maintenue : le joueur monte.
   * ESPACE relâchée : le joueur descend.
   */
  private void movePlayer(double dt, Input input) {

    if (input.isKeyDown(Key.SPACE)) {
      velocityY = FLY_SPEED;
    } else {
      velocityY -= GRAVITY * dt;
    }

    playerY += velocityY * dt;

    // Empêche le joueur de sortir de l'écran.
    playerY = Math.max(
    	    PLAYER_SIZE / 2,
    	    Math.min(HEIGHT - PLAYER_SIZE / 2, playerY)
    	);
  }

  /**
   * Fait apparaître des obstacles à droite de l'écran.
   * Leur position et leur taille sont aléatoires.
   */
  private void spawnObstacles(double dt, double difficulty) {

    double spawnInterval = Math.max(
        0.6,
        BASE_SPAWN_INTERVAL - difficulty * 0.15
    );

    spawnTimer -= dt;

    if (spawnTimer < 0) {

      // Dimensions aléatoires.
      double obstacleWidth = 5 + random.nextDouble() * 10;
      double obstacleHeight = 5 + random.nextDouble() * 20;

      // Position verticale aléatoire.
      double obstacleY = random.nextDouble()
          * (HEIGHT - obstacleHeight);

      obstacles.add(new Obstacle(
          WIDTH,
          obstacleY,
          obstacleWidth,
          obstacleHeight
      ));

      spawnTimer = spawnInterval;
    }
  }

  /**
   * Déplace les obstacles vers la gauche.
   * Leur vitesse augmente avec la difficulté.
   */
  private void moveObstacles(double dt, double difficulty) {

    double obstacleSpeed = BASE_OBSTACLE_SPEED + difficulty * 10;

    obstacles.replaceAll(
        obstacle -> obstacle.moveLeft(obstacleSpeed * dt)
    );

    // Supprime les obstacles sortis de l'écran.
    obstacles.removeIf(
        obstacle -> obstacle.x + obstacle.width < 0
    );
  }

  /**
   * Vérifie les collisions entre le joueur et les obstacles.
   *
   * @return false si une collision est détectée, true sinon
   */
  private boolean checkCollision() {

    return obstacles.stream().noneMatch(obstacle ->

        PLAYER_X + PLAYER_SIZE / 2 >= obstacle.x
        && PLAYER_X - PLAYER_SIZE / 2 <= obstacle.x + obstacle.width

        && playerY + PLAYER_SIZE / 2 >= obstacle.y
        && playerY - PLAYER_SIZE / 2 <= obstacle.y + obstacle.height
    );
  }

  /**
   * Retourne la position verticale du joueur.
   */
  public double playerY() {
    return playerY;
  }

  /**
   * Retourne la liste des obstacles.
   */
  public List<Obstacle> obstacles() {
    return Collections.unmodifiableList(obstacles);
  }
}
