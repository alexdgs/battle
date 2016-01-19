import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Stroke;


public class PowerUp extends GameObject {
	
	static final int WIDTH = 40;
	static final int HEIGHT = 40;
	
	// Flags
	static final int PICKABLE_BY_PLAYER = 1;
	static final int PICKABLE_BY_AI = 2;
	
	static final int JOKER = 0;
	static final int STAR = 1;
	static final int PISTOL = 2;
	static final int SHIELD = 3;
	static final int CLOCK = 4;
	static final int SHOVEL = 5;
	static final int BOMB = 6;
	static final int STOCK = 7;
	static final int WATER = 8;
	static final int BUDDIES = 9;
	static final int SIGHT = 10;
	static final int HOMING = 11;
	static final int SLOW = 12;
	static final int HEALTH = 13;
	static final int CAPTURE = 14;
	static final int FAST = 15;
	static final int BOUNCING_SHIELD = 16;
	static final int LAZY_HOMING = 17;
	static final int LASER = 18;
	static final int GHOST = 19;
	static final int RICOCHET = 20;
	static final int TURRET = 21;
	static final int HOMING_BALL = 22;
	static final int SPRINKLER = 23;
	static final int LASER_TURRET = 24;
	static final int KIRO = 25;
	static final int PROTECTOR = 26;
	static final int MGTURRET = 27;
	static final int ERRATIC_HOMING = 28;
	static final int WAVE = 29;
	static final int INC_MAX_HEALTH = 30;
	static final int MG = 31;
	
	static final int MAX_POWER_UP_ID = MG;
	
	static final int RANDOM = 100;
	
	static final int HALF_TEAM = 0;
	static final int FULL_TEAM = -1;
	
	static final int IDLE_TIME_LIMIT = 90;
	static final float SPAN_HIGHLIGHT_TIME = 45;
	static final double MAX_HIGHLIGHT_CIRCLE_DIAMETER = 200;
	
	static final int INVULNERABILITY_TIME = 1200;
	static final int FREEZE_TIME = 1000;
	static final int REINFORCEMENTS = 6;
	static final int SHOVEL_TIME = 1800;
	static final int SHOVEL_TIME_TO_START_BLINKING = SHOVEL_TIME - 480;
	static final int SHOVEL_BLINK_TIME = 40;
	static final int FULL_VISIBILITY_TIME = 1200;
	static final int NUM_HOMINGS = HALF_TEAM;
	static final int SLOW_TIME = 1200;
	static final int FAST_TIME = 1000;
	static final int BOUNCING_SHIELD_TIME = 1000;
	static final int LASER_POWER = 40;
	static final int GHOST_TIME = 1000;
	static final int NUM_RICOCHETS = 10;
	static final int NUM_PROTECTORS = 30;
	static final int NUM_ERRATIC_HOMINGS = 2;
	static final int MG_POWER = 200;
	
	static final String[] TEXT = {
		null,
		"STAR",
		"PISTOL",
		"SHIELD",
		"STOP",
		"SHOVEL",
		"BOMB",
		"1UP",
		"AQUATIC",
		"FRIENDS",
		"FULL VIEW",
		"HOMINGS",
		"SLOW",
		"HEALTH",
		"CAPTURE",
		"FAST",
		"BOUNCING",
		"LAZY HOMINGS",
		"LASER",
		"GHOST",
		"RICOCHET",
		"TURRET",
		"HOMING BALLS",
		"SPRINKLER",
		"LASER TURRET",
		"KIRO",
		"PROTECTOR",
		"MGTURRET",
		"ERRATIC",
		"WAVE",
		"INC MAX HEALTH",
		"MACHINE GUN"
	};
	
	int type;
	int spanTime = 0;
	int idleTime = 0;
	String text;
	boolean drawText;
	boolean pickable;
	boolean spanning;
	int highlightCircleDiameter;
	int highlightCircleRadius;
	
	Image imgPow;
	
	public PowerUp(Game game, int t) {
		super(game,round(Math.random()*(Game.MAX_X-(20+PowerUp.WIDTH*2)),40)+40,
				round(Math.random()*(Game.MAX_Y-(20+PowerUp.HEIGHT*2)),40)+40,WIDTH,HEIGHT);
		setType(t);
		
		boolean safe = false;
		while(!safe) {
			safe = true;
			for(Unit p : game.playerManager.tanks) {
				if(p.isFlagOn(Tank.CAN_PICKUP_POW) == true && pickUp(p)) {
					x = round(Math.random()*(Game.MAX_X-(20+PowerUp.WIDTH*2)),40)+60;
					y = round(Math.random()*(Game.MAX_Y-(20+PowerUp.HEIGHT*3)),40)+60;
					safe = false;
					break;
				}
			}
		}
		
		pickable = true;
		spanning = true;
	}
	
	public PowerUp(Game game, int x, int y, int t) {
		super(game,x,y,WIDTH,HEIGHT);
		this.type = t;
		pickable = true;
	}
	
	public void setType(int t) {
		drawText = false;
		
		if(t == RANDOM) {
			type = (int)(Math.random()*MAX_POWER_UP_ID + 0.9);
		} else type = t;
		
		imgPow = game.imgPow[type];
		
		if(type == JOKER) {
			type = (int)(Math.random()*(MAX_POWER_UP_ID-1) + 1.9);
			drawText = true;
		}
		
		if(type == SIGHT) {
			type = CLOCK;
			imgPow = game.imgPow[type];
		}
		
		if(type == SHOVEL && !game.isFlagOn(Game.EAGLE)) {
			type = STAR;
			imgPow = game.imgPow[type];
		}
		
		text = TEXT[type];
	}
	
	public void step() {
		if(pickable) {
			for(Tank p : game.playerManager.tanks) {
				if(p.isFlagOn(Tank.CAN_PICKUP_POW) == true && pickUp(p)) {
					pickable = false;
					activate(p);
					break;
				}
			}
		} else {
			idleTime++;
			if(idleTime == IDLE_TIME_LIMIT) {
				game.scheduleRemovePow();
			}
		}
		
		if(spanTime == SPAN_HIGHLIGHT_TIME) spanning = false;
		else {
			highlightCircleDiameter = (int)((1.0 - spanTime/SPAN_HIGHLIGHT_TIME) * MAX_HIGHLIGHT_CIRCLE_DIAMETER);
			highlightCircleRadius = (int)(highlightCircleDiameter / 2.0);
			spanTime++;
		}
	}
	
	public boolean pickUp(Unit p) {
		return p.getBounds().intersects(getBounds());
	}
	
	public void activate(Tank tank) {
		boolean stock = false;
		switch(type) {
		case STAR:
			tank.upgrade();
			break;
		case PISTOL:
			tank.maxUpgrade();
			break;
		case SHIELD:
			tank.timerManager.scheduleTimedEvent(Tank.TIMER_INVULNERABLE, INVULNERABILITY_TIME);
			break;
		case CLOCK:
			game.teams[tank.team].freezeEnemies(FREEZE_TIME);
			break;
		case SHOVEL:
			if(game.isFlagOn(Game.EAGLE)) game.activateShovel();
			break;
		case BOMB:
			game.teams[tank.team].destroyEnemies();
			game.audioPlayer.playSound(AudioPlayer.ENEMY_DIE);
			break;
		case STOCK:
			if(tank instanceof PlayerTank) {
				((PlayerTank)tank).extraLive();
				stock = true;
			}
			break;
		case WATER:
			tank.setFlagOn(Tank.CAN_PASS_WATER);
			break;
		case BUDDIES:
			game.teams[tank.team].orderReinforcements(REINFORCEMENTS);
			break;
		case SIGHT:
			game.eventManager.scheduleTimedEvent(Game.FULL_VISIBILITY, FULL_VISIBILITY_TIME);
			break;
		case HOMING:
			tank.fireHomings(NUM_HOMINGS);
			break;
		case SLOW:
			game.teams[tank.team].slowEnemies(SLOW_TIME);
			break;
		case HEALTH:
			game.teams[tank.team].fullHealth();
			break;
		case CAPTURE:
			tank.captureEnemies(HALF_TEAM);
			break;
		case FAST:
			tank.timerManager.scheduleTimedEvent(Tank.TIMER_FAST, FAST_TIME);
			break;
		case BOUNCING_SHIELD:
			tank.timerManager.scheduleTimedEvent(Tank.TIMER_BOUNCING_SHIELD, BOUNCING_SHIELD_TIME);
			break;
		case LAZY_HOMING:
			tank.fireLazyHomings(HALF_TEAM);
			break;
		case LASER:
			tank.changeWeapon(Tank.LASER);
			break;
		case GHOST:
			tank.timerManager.scheduleTimedEvent(Tank.TIMER_GHOST, GHOST_TIME);
			break;
		case RICOCHET:
			tank.fireBouncingBalls(NUM_RICOCHETS);
			break;
		case TURRET:
			tank.putTurret();
			break;
		case HOMING_BALL:
			tank.fireHomingBall(HALF_TEAM);
			break;
		case SPRINKLER:
			tank.fireSprinklers(HALF_TEAM);
			break;
		case LASER_TURRET:
			tank.putLaserTurret();
			break;
		case KIRO:
			tank.fireKiro();
			break;
		case PROTECTOR:
			if(game.isFlagOn(Game.EAGLE)) for(Eagle e : game.eagles) e.setProtector(NUM_PROTECTORS);
			break;
		case MGTURRET:
			tank.putMGTurret();
			break;
		case ERRATIC_HOMING:
			tank.fireErraticHomings(FULL_TEAM, NUM_ERRATIC_HOMINGS);
			break;
		case WAVE:
			tank.fireWave();
			break;
		case INC_MAX_HEALTH:
			tank.incHealthCap();
			break;
		case MG:
			tank.changeWeapon(Tank.MG);
			break;
		default:
			System.out.println("UNKNOWN POWER");
		}
		
		if(stock) game.audioPlayer.playSound(AudioPlayer.STOCK);
		else game.audioPlayer.playSound(AudioPlayer.POW_PICKUP);
	}
	
	public void paint(Graphics2D g) {
		if(spanning) {
			g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f - spanTime/SPAN_HIGHLIGHT_TIME));
			Stroke backup = g.getStroke();
			g.setStroke(new BasicStroke(6, BasicStroke.CAP_ROUND, BasicStroke.JOIN_MITER));
			g.setColor(Color.WHITE);
			g.drawOval((int)xTarget - highlightCircleRadius, (int)yTarget - highlightCircleRadius, highlightCircleDiameter, highlightCircleDiameter);
			g.setStroke(backup);
			g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f));
		}
		
		if(pickable) {
			if(game.isFlagOn(Game.BLINK_STATE))
				g.drawImage(imgPow, (int)x, (int)y, null);
		} else {
			g.drawImage(game.imgPowPicked, (int)x, (int)y, null);
			if(drawText) {
				g.setColor(Color.WHITE);
				g.drawString(text, (int)x, (int)y);
			}
		}
	}
}
