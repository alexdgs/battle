import java.awt.Color;
import java.awt.Graphics2D;
import java.util.ArrayList;


public class Minimap {
	
	static final int X = Game.MAX_X + 20;
	static final int Y = UI.MAX_Y + 20;
	static final int MAX_WIDTH = UI.MAX_X - X;
	static final int MAX_HEIGHT = Game.HEIGHT - (Y + 45);
	static final int MIN_DIMENSION = Math.min(MAX_WIDTH, MAX_HEIGHT);
	
	static final double SCALE_FACTOR = Math.max(Game.PLAYABLE_X, Game.PLAYABLE_Y)/(double)MIN_DIMENSION;
	
	static final int WIDTH = (int)(Game.PLAYABLE_X/SCALE_FACTOR)-1;
	static final int HEIGHT = (int)(Game.PLAYABLE_Y/SCALE_FACTOR)-1;
	static final double MAX_SCALED_DIMENSION = Math.max(WIDTH, HEIGHT);
	static final int START_X = X + (MAX_WIDTH - WIDTH)/2;
	static final int START_Y = Y + (MAX_HEIGHT - HEIGHT)/2;
	static final int TANK_WIDTH = (int)(Tank.WIDTH/SCALE_FACTOR);
	static final int TANK_HEIGHT = (int)(Tank.HEIGHT/SCALE_FACTOR);
	
	static final int BG_MIN_X = START_X + (int)(Game.MIN_X/SCALE_FACTOR);
	static final int BG_MIN_Y = START_Y + (int)(Game.MIN_Y/SCALE_FACTOR);
	
	static final int TIME_TO_UPDATE_ELEMENTS = 20;
	int timeToUpdateElements;
	
	ArrayList<Tank> tanks;
	
	Game game;
	
	public Minimap(Game game) {
		this.game = game;
		timeToUpdateElements = 0;
		
		//System.out.println(SCALE_FACTOR);
	}
	
	public void buildMap() {
		if(timeToUpdateElements == TIME_TO_UPDATE_ELEMENTS) {
			tanks = new ArrayList<Tank>();
			tanks.addAll(game.allyManager.tanks);
			tanks.addAll(game.enemyManager.tanks);
			tanks.addAll(game.playerManager.tanks);
			timeToUpdateElements = 0;
		} else timeToUpdateElements++;
	}
	
	public void paint(Graphics2D g) {
		g.setColor(Color.BLACK);
		g.fillRect(BG_MIN_X, BG_MIN_Y, WIDTH, HEIGHT);
		if(tanks != null && tanks.size() > 0) {
			for(Unit t : tanks) {
				if(t instanceof PlayerTank) {
					PlayerTank p = (PlayerTank)t;
					if(p.isFlagOn(PlayerTank.ACTIVE)) {
						g.setColor(PlayerTank.COLORS[p.id]);
						g.fillRect((int)(t.x/SCALE_FACTOR) + START_X, (int)(t.y/SCALE_FACTOR) + START_Y, TANK_WIDTH, TANK_HEIGHT);
					}
				} else {
					//if(t.isFlagOn(Tank.VISIBLE) || game.isFlagOn(Game.FULL_VISIBILITY)) {
						if(t.isFlagOn(Tank.REMOVING)) {
							g.setColor(Color.WHITE);
						} else if(t.team == Unit.ALLY_TEAM) {
							g.setColor(Color.GREEN);
						} else {
							g.setColor(Color.RED);
						}
						g.fillRect((int)(t.x/SCALE_FACTOR) + START_X, (int)(t.y/SCALE_FACTOR) + START_Y, TANK_WIDTH, TANK_HEIGHT);
					//}
				}
			}
		}
		if(game.isFlagOn(Game.EAGLE)) {
			for(Eagle e : game.eagles) {
				if(!e.isDestroyed) {
					if(e.isFlagOn(Eagle.REMOVING)) {
						g.setColor(Color.WHITE);
					} else g.setColor(Color.GRAY);
				} else g.setColor(Color.DARK_GRAY);
				g.fillRect((int)(e.x/SCALE_FACTOR) + START_X, (int)(e.y/SCALE_FACTOR) + START_Y, (int)(Eagle.WIDTH/SCALE_FACTOR), (int)(Eagle.HEIGHT/SCALE_FACTOR));
				g.setColor(Color.BLACK);
				g.drawRect((int)(e.x/SCALE_FACTOR) + START_X, (int)(e.y/SCALE_FACTOR) + START_Y, (int)(Eagle.WIDTH/SCALE_FACTOR) - 1, (int)(Eagle.HEIGHT/SCALE_FACTOR) - 1);
			}
		}
		
		if(game.pow != null) {
			g.setColor(Color.LIGHT_GRAY);
			if(game.isFlagOn(Game.BLINK_STATE)) g.fillRect((int)(game.pow.x/SCALE_FACTOR) + START_X, (int)(game.pow.y/SCALE_FACTOR) + START_Y, (int)(PowerUp.WIDTH/SCALE_FACTOR), (int)(PowerUp.HEIGHT/SCALE_FACTOR));
		}
	}
}
