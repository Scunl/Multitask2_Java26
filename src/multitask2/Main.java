package multitask2;

import java.awt.Rectangle;
import java.util.function.Function;

import com.github.forax.zen.Application;
import com.github.forax.zen.ApplicationContext;
import com.github.forax.zen.Event;
import com.github.forax.zen.PointerEvent;

import multitask2.controller.Controller;
import multitask2.model.Game;
import multitask2.model.Model;
import multitask2.model.games.Balance;
import multitask2.model.games.Avoid;
import multitask2.model.games.Fly;
import multitask2.view.GeneralView;
import multitask2.view.Viewers;
import multitask2.view.games.AvoidView;
import multitask2.view.games.BalanceView;
import multitask2.view.games.FlyView;
import multitask2.view.games.GameView;

/**
 * Point d'entrée de l'application de mini-jeux multitâches.
 *
 * <p>
 * Cette classe crée l'interface graphique, le modèle partagé par les mini-jeux
 * et le contrôleur qui orchestre les mises à jour et le rendu. Elle enregistre
 * les mini-jeux avec leur vue respective.
 * </p>
 */
public final class Main {

	/**
	 * Constructeur privé afin d'empêcher l'instanciation de cette classe
	 * utilitaire.
	 */
	private Main() {
		super();
	}

	private enum Action {
		START, EXIT, NONE
	};

	private enum Type {
		HOME, GAME_OVER
	};

	/**
	 * Lance la boucle de l'application graphique.
	 *
	 * @param args arguments de la ligne de commande ; ils ne sont pas utilisés
	 *             actuellement
	 */
	public static void main(String[] args) {
		Application.run(GeneralView.BACKGROUND, Main::home_GameOver);
	}

	private static void renderMenu(ApplicationContext context, GeneralView view, Type menu) {
		var screen = context.getScreenInfo();

		context.renderFrame(graphics -> { // Affichage du menu
			graphics.setColor(GeneralView.BACKGROUND);
			graphics.fillRect(0, 0, screen.width(), screen.height());

			switch (menu) {
			case Type.HOME -> view.drawHome(graphics);
			case Type.GAME_OVER -> view.drawGameOver(graphics);
			}
		});
	}

	/**
	 * Menu principal du jeu
	 * 
	 * @param context contexte graphique fourni par la bibliothèque Zen
	 */
	private static void home_GameOver(ApplicationContext context) {
		var viewers = new Viewers();
		var view = new GeneralView(context, viewers);
		var screen = context.getScreenInfo();
		Type menu = Type.HOME;
		while (true) {
		  renderMenu(context, view, menu);
			var action = pollEvents(context, screen.width(), screen.height(), menu);
			if (action == Action.START) {
				menu = game(context) ? Type.HOME : Type.GAME_OVER;
			}
			if (action == Action.EXIT) {
				context.dispose();
				return;
			}
		}
	}

	private static Action pollEvents(ApplicationContext context, int w, int h, Type menu) {
		Event event;
		while ((event = context.pollEvent()) != null) {
			var action = switch (menu) {
				case HOME -> {
					yield handleHomeClick(event, w, h);
				}
				case GAME_OVER -> {
					yield handleGameOverClick(event, w, h, menu);
				}
			};
			if (action != Action.NONE)
				return action;
		}
		return Action.NONE;
	}

	private static Action handleHomeClick(Event e, int w, int h) {
		Rectangle start = new Rectangle(w / 2 - 50, h / 2 - 30, 100, 40);
		Rectangle exit = new Rectangle(w / 2 - 50, h / 2 + 30, 100, 40);
		switch (e) {
			case PointerEvent pe -> {
				int x = pe.location().x();
				int y = pe.location().y();
	
				if (start.contains(x, y))
					return Action.START;
				if (exit.contains(x, y))
					return Action.EXIT;
			}
			default -> {}
		};
		return Action.NONE;
	}

	private static Action handleGameOverClick(Event e, int w, int h, Type menu) {
		switch (e) {
			case PointerEvent pe -> {
				int x = pe.location().x();
				int y = pe.location().y();
	
				if (x > w - 300 && y > h - 100) {
					menu = Type.HOME;
					return Action.NONE;
				}
				if (x < 200 && y > h - 100)
					return Action.EXIT;
			}
			default -> {}
		};
		return Action.NONE;
	}

	/**
	 * Construit les composants de la partie et exécute le contrôleur jusqu'à la fin
	 * du jeu. <br>
	 * Le contexte graphique est libéré même si l'exécution du contrôleur se termine
	 * à la suite d'une exception.
	 *
	 * @param context contexte graphique fourni par la bibliothèque Zen
	 */
	private static boolean game(ApplicationContext context) {
		var model = new Model();
		var viewers = new Viewers();
		addGame(model, viewers, new Balance(), 0, BalanceView::new);
		addGame(model, viewers, new Avoid(), 5, AvoidView::new);
		addGame(model, viewers, new Fly(), 0, FlyView::new);

		var view = new GeneralView(context, viewers);
		var controller = new Controller(model, view, context);

		return controller.run();
	}

	/**
	 * Enregistre un mini-jeu dans le modèle et associe à cette instance sa vue
	 * graphique.
	 *
	 * @param <G>     type concret du mini-jeu
	 * @param model   modèle auquel ajouter le mini-jeu
	 * @param viewers registre dans lequel enregistrer sa vue
	 * @param game    instance du mini-jeu à enregistrer
	 * @param time    temps, en secondes depuis le début de la partie, auquel
	 *                activer le mini-jeu
	 * @param factory fonction construisant une vue à partir de l'instance du
	 *                mini-jeu
	 */
	private static <G extends Game> void addGame(Model model, Viewers viewers, G game, int time,
			Function<G, GameView> factory) {
		model.addGame(game, time);
		viewers.addGame(game, factory.apply(game));
	}
}
