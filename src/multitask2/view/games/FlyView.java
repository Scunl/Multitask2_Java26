
package multitask2.view.games;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.util.Objects;

import multitask2.model.games.Fly;

/**
 * Vue graphique du mini-jeu Fly : dessine le joueur et les obstacles.
 */
public final class FlyView implements GameView {

  /** État du mini-jeu à représenter. */
  private final Fly flyer;

  /**
   * Crée une vue associée au mini-jeu fourni.
   *
   * @param flyer mini-jeu dont l'état doit être représenté
   */
  public FlyView(Fly flyer) {
    Objects.requireNonNull(flyer);
    this.flyer = flyer;
    super();
  }

  /**
   * Dessine le joueur et les obstacles.
   *
   * @param graphics contexte graphique
   * @param area zone disponible pour le dessin
   */
  @Override
  public void render(Graphics2D graphics, Rectangle area) {
    GameView.checkInput(graphics, flyer, area);

    var scaleX = area.width / Fly.getWidth();
    var scaleY = area.height / Fly.getHeight();

    // Dessin du fond
    graphics.setColor(new Color(25, 30, 55));
    graphics.fillRect(area.x, area.y, area.width, area.height);

    // Dessin du joueur
    var playerSize = 5;

    var playerX = area.x + (int) (15 * scaleX);
    var playerY = area.y + area.height
        - (int) (flyer.playerY() * scaleY);

    graphics.setColor(new Color(90, 200, 220));
    graphics.fillOval(
        playerX - (int) (playerSize * scaleX / 2),
        playerY - (int) (playerSize * scaleY / 2),
        (int) (playerSize * scaleX),
        (int) (playerSize * scaleY)
    );

    // Dessin des obstacles
    graphics.setColor(new Color(230, 90, 90));

    for (Fly.Obstacle obstacle : flyer.obstacles()) {

      var obstacleX = area.x + (int) (obstacle.x() * scaleX);

      var obstacleY = area.y + area.height
          - (int) ((obstacle.y() + obstacle.height()) * scaleY);

      var obstacleWidth = (int) (obstacle.width() * scaleX);
      var obstacleHeight = (int) (obstacle.height() * scaleY);

      graphics.fillRect(
          obstacleX,
          obstacleY,
          obstacleWidth,
          obstacleHeight
      );
    }
  }
}
