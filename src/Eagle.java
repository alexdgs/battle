import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Stroke;
import java.util.ArrayList;

import javax.swing.ImageIcon;


public class Eagle extends Unit implements Hittable {
	
	static final int WIDTH = 40;
	static final int HEIGHT = 40;
	static final int REMOVE_TIME = 30;
	static final int MAX_HIT_POINTS = 40;
	
	static final int MAX_PROTECTORS = PowerUp.NUM_PROTECTORS;
	
	static final int HALF_WIDTH = WIDTH/2;
	static final int HALF_HEIGHT = HEIGHT/2;
	
	static final double MAX_DIST_TO_CLOSEST_PLAYER = 240;
	static final int TIME_HIGHLIGHT = 110;
	static final int TIME_GAP_HIGHLIGHT = 220;
	static final double MAX_HIGHLIGHT_CIRCLE_DIAMETER = 280;
	static final int TIME_SHOW_LOST_MESSAGE = 300;
	static final int NUM_PROTECTORS = 3;
	static final int INC_REBUILD_TIME = 12000;
	static final int TIME_DISABLE_EXTRA_SLOT = 1000;
	
	int maxRebuildTime = INC_REBUILD_TIME;
	
	int timeRemoving;
	float timeHighlight;
	int hitPoints;
	
	boolean hasTurrets;
	boolean isDestroyed;
	boolean targeted;
	boolean rebuilding;
	
	int timeGap = 0;
	int timeRebuild = 0;
	int numProtectors;
	boolean protector;
	
	static final int BAR_WIDTH = 4;
	static final int BAR_SHIFT_X = 0;
	static final int BAR_SHIFT_Y = 0;
	
	static final int PROTECTOR_BAR_WIDTH = 4;
	static final int PROTECTOR_BAR_SHIFT_X = 0;
	static final int PROTECTOR_BAR_SHIFT_Y = BAR_WIDTH-1;
	static final Color PROTECTOR_BAR_COLOR = Color.LIGHT_GRAY;
	
	static final int REBUILD_BAR_WIDTH = 4;
	static final int REBUILD_BAR_SHIFT_X = 0;
	static final int REBUILD_BAR_SHIFT_Y = HEIGHT-WIDTH;
	static final Color REBUILD_BAR_COLOR = Color.DARK_GRAY;
	
	Bar bar;
	Bar protectorBar;
	Bar rebuildBar;
	
	Color highlightColor;
	int highlightCircleDiameter = 0;
	int highlightCircleRadius = 0;
	
	int[] remOrder = {0,1,0,0};
	
	ArrayList<Turret> guardians;
	
	Image imgEagle = (new ImageIcon("src/eagle.png")).getImage().getScaledInstance(WIDTH, HEIGHT, Image.SCALE_DEFAULT);
	Image imgTargeted = (new ImageIcon("src/target.png")).getImage().getScaledInstance(WIDTH, HEIGHT, Image.SCALE_DEFAULT);
	Image[] rem = {
			(new ImageIcon("src/die1.png")).getImage().getScaledInstance(WIDTH*2, HEIGHT*2, Image.SCALE_DEFAULT),
			(new ImageIcon("src/die2.png")).getImage().getScaledInstance(WIDTH*2, HEIGHT*2, Image.SCALE_DEFAULT)
	};
	
	Image imgEagleDestroyed = (new ImageIcon("src/eagle_destroyed.png")).getImage().getScaledInstance(WIDTH, HEIGHT, Image.SCALE_DEFAULT);
	
	public Eagle(Game game, int x, int y, int id) {
		super(game, x, y, WIDTH, HEIGHT, id, Unit.ALLY_TEAM);
		timeRemoving = 0;
		setFlagOn(HITTABLE);
		setFlagOn(ALLY_INMUNE);
		isDestroyed = false;
		hasTurrets = false;
		hitPoints = MAX_HIT_POINTS;
		protector = false;
		numProtectors = 0;
		timeHighlight = 0;
		guardians = new ArrayList<Turret>();
		
		bar = new Bar(this, BAR_SHIFT_X, BAR_SHIFT_Y, WIDTH, BAR_WIDTH, hitPoints, MAX_HIT_POINTS, null, true, Bar.TYPE_HEALTH);
		bar.update(hitPoints);
		
		protectorBar = new Bar(this, PROTECTOR_BAR_SHIFT_X, PROTECTOR_BAR_SHIFT_Y, WIDTH, PROTECTOR_BAR_WIDTH, numProtectors, MAX_PROTECTORS, PROTECTOR_BAR_COLOR, true, Bar.TYPE_POWER);
		protectorBar.update(hitPoints);
		
		rebuildBar = new Bar(this, REBUILD_BAR_SHIFT_X, REBUILD_BAR_SHIFT_Y, WIDTH, REBUILD_BAR_WIDTH, 0, maxRebuildTime, REBUILD_BAR_COLOR, false, Bar.TYPE_POWER);
	}
	
	public void step() {
		targeted = false;
		if(isFlagOn(REMOVING)) {
			timeRemoving++;
			if(timeRemoving == REMOVE_TIME) {
				setFlagOff(REMOVING);
				isDestroyed = true;
				hasTurrets = false;
				numProtectors = 0;
				game.terrainManager.removeEagle((int)x, (int)y);
				rebuilding = true;
			}
		} else if(rebuilding && !game.isFlagOn(Game.GAME_OVER)) {
			rebuildBar.update(++timeRebuild);
			if(maxRebuildTime - timeRebuild == TIME_DISABLE_EXTRA_SLOT) game.enemyManager.setExtraSlotState(id, false);
			if(timeRebuild == maxRebuildTime) {
				rebuild();
				rebuilding = false;
				game.allyManager.setTargetState(id, true);
				rebuildBar.setMaxHitPoints(maxRebuildTime *= 2, timeRebuild = 0);
				highlightColor = Color.GREEN;
				timeHighlight = TIME_HIGHLIGHT;
			}
		}
		
		if(!guardians.isEmpty()) for(Turret t : guardians) t.move();
		
		if(timeHighlight > 0) {
			highlightCircleDiameter = (int)((timeHighlight/TIME_HIGHLIGHT) * MAX_HIGHLIGHT_CIRCLE_DIAMETER);
			highlightCircleRadius = (int)(highlightCircleDiameter / 2.0);
			timeHighlight--;
		}
		
		if(timeGap > 0) timeGap--;
	}
	
	public void rebuild() {
		timeRemoving = 0;
		isDestroyed = false;
		setFlagOn(HITTABLE);
		bar.update(hitPoints = MAX_HIT_POINTS / 2);
		game.terrainManager.registerEagle((int)x, (int)y);
		game.coverEagle(this, TerrainManager.BRICK);
		game.audioPlayer.playSound(AudioPlayer.BONUS);
	}
	
	public void heal() {
		if(isFlagOn(HITTABLE)) {
			hitPoints = Math.min(hitPoints+1, MAX_HIT_POINTS);
			bar.update(hitPoints);
		}
	}
	
	public void fullHealth() {
		if(isFlagOn(HITTABLE)) {
			hitPoints = MAX_HIT_POINTS;
			bar.update(hitPoints);
		}
	}
	
	public int hitted(Projectile projectile, Unit owner) {
		if(isFlagOn(HITTABLE)) {
			hitPoints--;
			if(hitPoints < 1) {
				//timeRemoving++;
				setFlagOff(HITTABLE);
				setFlagOn(REMOVING);
				game.audioPlayer.playSound(AudioPlayer.PLAYER_DIE);
				game.eventManager.scheduleTimedEvent(Game.EVENT_EAGLE_LOST, TIME_SHOW_LOST_MESSAGE);
				guardians.clear();
				setProtector(0);
				game.allyManager.setTargetState(id, false);
				game.enemyManager.setExtraSlotState(id, true);
				
				if(distToClosestPlayer() > MAX_DIST_TO_CLOSEST_PLAYER) {
					highlightColor = Color.ORANGE;
					timeHighlight = TIME_HIGHLIGHT;
				}
			} else {
				if(timeGap == 0) {
					if(distToClosestPlayer() > MAX_DIST_TO_CLOSEST_PLAYER) {
						highlightColor = Color.RED;
						timeHighlight = TIME_HIGHLIGHT;
						timeGap = TIME_GAP_HIGHLIGHT;
					}
				}
				//game.eventManager.scheduleTimedEvent(Game.EVENT_EAGLE_UNDER_ATTACK, TIME_SHOW_UNDER_ATTACK_MESSAGE);
				game.audioPlayer.playSound(AudioPlayer.TANK_HIT);
			}
			bar.update(hitPoints);
		}
		return 0;
	}
	
	public double distToClosestPlayer() {
		double distToClosestPlayer = Double.POSITIVE_INFINITY;
		for(Tank t : game.playerManager.tanks) {
			distToClosestPlayer = Math.min(distToClosestPlayer, distTo(t));
		}
		return distToClosestPlayer;
	}
	
	public void hitLetal(Projectile projectile, Unit owner) {
		hitted(projectile, owner);
	}
	
	public void setProtector(int num) {
		if(isFlagOn(HITTABLE)) {
			numProtectors = num;
			if(!protector) {
				for(TeamManager tm : game.teams[team].enemyTeams) {
					for(Tank t : tm.tanks) {
						if(t.target != null && t.target instanceof Eagle) fireProtectors(t);
					}
				}
			}
			protectorBar.update(numProtectors);
			checkProtectorState();
		}
	}
	
	public void checkProtectorState() {
		if(numProtectors == 0) protector = false;
		else protector = true;
	}
	
	public void enemySight(Tank t) {
		if(protector) {
			fireProtectors(t);
		}
	}
	
	public void fireProtectors(Tank t) {
		int num = Math.min(numProtectors, t.hitPoints + 1);
		for(int i = 0; i < num; i++) {
			game.objectsToInsert.add(new EagleProtector(game, xTarget - EagleProtector.HALF_WIDTH, yTarget - EagleProtector.HALF_HEIGHT, randomAngle(), this, t));
		}
		numProtectors -= num;
		protectorBar.update(numProtectors);
		checkProtectorState();
	}
	
	public void paint(Graphics2D g) {
		if(timeHighlight > 0) {
			g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, timeHighlight/TIME_HIGHLIGHT));
			Stroke backup = g.getStroke();
			g.setStroke(new BasicStroke(8, BasicStroke.CAP_ROUND, BasicStroke.JOIN_MITER));
			g.setColor(highlightColor);
			g.drawOval((int)xTarget - highlightCircleRadius, (int)yTarget - highlightCircleRadius, highlightCircleDiameter, highlightCircleDiameter);
			g.setStroke(backup);
			g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f));
		}
		
		if(isFlagOn(REMOVING)) g.drawImage(rem[remOrder[timeRemoving/10]], (int)x-20, (int)y-20, null);
		else if(isDestroyed) {
			g.drawImage(imgEagleDestroyed, (int)x, (int)y, null);
			if(rebuilding) rebuildBar.paint(g);
		}
		else {
			g.drawImage(imgEagle, (int)x, (int)y, null);
			if(targeted) g.drawImage(imgTargeted, (int)x, (int)y, null);
			if(protector) {
				protectorBar.paint(g);
			}
			bar.paint(g);
		}
	}
}
