import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Stroke;


public class AITank extends Tank {
	
	static final int BASIC = 1;
	static final int ROLLER = 2;
	static final int SNIPER = 3;
	static final int BIG = 4;
	static final int TURRET = 5;
	
	static final int POWERUP_COLOR = 5;
	
	static final int[] SCORES = {0,100,200,300,400};
	
	static final int MAX_BULLETS = 2;
	static final int MAX_HIT_POINTS = 4;
	static final int MAX_POWERUP_HIT_POINTS = 3;
	
	static final int MAX_TIME_TO_FIRE = 200;
	static final int MAX_TIME_TO_TURN = 250;
	static final int MAX_TIME_TO_TURN_ON_BLOCK = 50;
	
	static final double VERY_SLOW_SPEED = 0.5;
	static final double SLOW_SPEED = 0.65;
	static final double FAST_SPEED = 1.4;
	
	static final int RANDOM_TYPE = 0;
	static final int RANDOM_HIT_POINTS = 0;
	
	static final double MIN_ALLIG_TO_TARGET = 15;
	static final double MIN_DIST_TO_TARGET = 180;
	static final double MIN_DIST_TO_EAGLE_TARGET = 60;
	
	static final double TURN_FACTOR = 16.0;
	
	static final int MAX_TIME_TO_SHOW_VALUE = 30;
	static final int TIME_WALK = 16;
	
	// Flags for AITank class
	static final int SHOW_VALUE = 20;
	static final int SCOREABLE = 21;
	static final int CAN_TURN = 22;
	static final int BLOCKED = 23;
	static final int INVERSE_PREF = 24;
	
	// Timers for AITank class
	static final int TIMER_TURN = 12;
	static final int TIMER_SHOW_VALUE = 13;
	
	int powerUpHitPoints;
	int minTimeToTurn;
	int[] pos;
	
	boolean xFirst = false;
	
	Color teamColor;
	String teamString;
	
	Image[][][] imgTank;
	Image imgValue;
	
	public AITank(Game game, int x, int y, int initDir, int id, int team, int type, int hitPoints) {
		super(game, x, y, initDir, id, team);
		setType(type);
		setHitPoints(hitPoints);
		setTeam();
		sightRange = AI_SIGHT_RANGE;
		eagleSightRange = AI_EAGLE_SIGHT_RANGE;
		numBullets = 0;
		score = 0;
		waitTimeToFire = MAX_TIME_TO_FIRE;
		timeToLookNewTarget = (int)(TURN_FACTOR/speed);
		setFlagOff(VISIBLE);
		if(Game.randomBoolean(0.5)) setFlagOn(INVERSE_PREF);
		timerManager.scheduleTimedEvent(TIMER_SPAN, DEFAULT_SPAN_TIME);
	}
	
	public AITank(Game game, int x, int y, int initDir, int id, int team, int type, int hitPoints, int[] pos) {
		this(game, x, y, initDir, id, team, type, hitPoints);
		this.pos = pos;
	}
	
	@Override
	public void triggerInSpecificTimer(int timerId) {
		switch(timerId) {
		case TIMER_TURN:
			setFlagOff(CAN_TURN);
			break;
		case TIMER_SHOW_VALUE:
			setFlagOn(SHOW_VALUE);
			break;
		}
	}
	
	@Override
	public void triggerOutSpecificTimer(int timerId) {
		switch(timerId) {
		case TIMER_TURN:
			setFlagOn(CAN_TURN);
			break;
		case TIMER_SHOW_VALUE:
			finishRemove();
		}
	}
	
	@Override
	public void move() {
		timerManager.processEvents();
		
		if(!isFlagOn(SPANNING) && !isFlagOn(REMOVING)) {
			if(isFlagOn(SLOW)) toggleFlag(CAN_MOVE);
			if(isFlagOn(CAN_MOVE)) {
				lastMove = getNextMove();
				if(isFlagOn(WALKING)) {
					double newPos = game.terrainManager.nextMove(x, y, lastMove, speed, team);
					if(lastMove == LEFT || lastMove == RIGHT) {
						y = round(y,20);
						if(newPos != x) x = newPos;
						else blocked();
					} else {
						x = round(x,20);
						if(newPos != y) y = newPos;
						else blocked();
					}
					
					updateTargetPos();
					walk();
				}
				
				if(isFlagOn(CAN_FIRE) && numBullets < maxBullets) fire();
				moveTurret();
			}
		}
		if(isFlagOn(TARGETED_BY_TURRET)) {
			setFlagOn(PAINT_TARGETED_BY_TURRET);
			setFlagOff(TARGETED_BY_TURRET);
		}
		else setFlagOff(PAINT_TARGETED_BY_TURRET);
	}
	
	@Override
	public void walk() {
		walk = (++walk) % TIME_WALK;
	}
	
	public int getNextMove() {
		int next = lastMove;
		if(target != null){
			if(!isFlagOn(HAS_A_TARGET)) {
				//System.out.println("New target detected!");
				timerManager.remainingTimeForEvent[TIMER_TURN] = minTimeToTurn;
				setFlagOn(HAS_A_TARGET);
			}
		} else {
			//System.out.println("No target");
			setFlagOff(HAS_A_TARGET);
			setFlagOff(INVERSE_PREF);
		}
			
		if(isFlagOn(CAN_TURN)) {
			//System.out.println("Flag AITank can turn");
			setFlagOn(WALKING);
			if(isFlagOn(HAS_A_TARGET)) {
				//System.out.println("I have a target and will pursuit it");
				double dx = x - target.x;
				double dy = y - target.y;
				
				if(isFlagOn(BLOCKED)) {
					toggleFlag(INVERSE_PREF);
					setFlagOff(BLOCKED);
				}
				
				next = getOptimalMove(dx, dy);
				
				if(isFlagOn(WALKING)) timerManager.scheduleTimedEvent(TIMER_TURN, minTimeToTurn);
				
			} else {
				Eagle eagle = closestEagle();
				if(game.isFlagOn(Game.EAGLE) && team == Unit.ENEMY_TEAM && eagle != null) {
					next = towardsEagleMove(eagle);
				} else next = randomDir();
				timerManager.scheduleTimedEvent(TIMER_TURN, (int)(Math.random()*MAX_TIME_TO_TURN + minTimeToTurn));
			}
		}
		return next;
	}
	
	public int getOptimalMove(double dx, double dy) {
		int next = 0;
		double adx = Math.abs(dx);
		double ady = Math.abs(dy);
		if((isFlagOn(INVERSE_PREF)/* ? adx <= ady : adx > ady*/)) {
			if(ady < MIN_ALLIG_TO_TARGET) {
				if(dx > 0.0) next = LEFT;
				else next = RIGHT;
				
				if(adx <= (target instanceof Eagle ? MIN_DIST_TO_EAGLE_TARGET : MIN_DIST_TO_TARGET) && lastMove == next) setFlagOff(WALKING);
			} else {
				if(dy > 0.0) next = UP;
				else next = DOWN;
			}
		} else {
			if(adx < MIN_ALLIG_TO_TARGET) {
				if(dy > 0.0) next = UP;
				else next = DOWN;
				
				if(ady <= (target instanceof Eagle ? MIN_DIST_TO_EAGLE_TARGET : MIN_DIST_TO_TARGET) && lastMove == next) setFlagOff(WALKING);
			} else {
				if(dx > 0.0) next = LEFT;
				else next = RIGHT;
			}
		}
		return next;
	}
	
	public int towardsEagleMove(Eagle eagle) {
		double chanceLeft = 0.25;
		double chanceRight = 0.25;
		double chanceUp = 0.25;
		//double chanceDown = 0.25;
		double dx = x - eagle.x;
		double dy = y - eagle.y;
		if(dx == 0.0) {
			chanceUp += 0.1;
			//chanceDown += 0.10;
			chanceLeft -= 0.1;
			chanceRight -= 0.1;
		} else if(dx > 0.0) {
			chanceLeft += 0.1;
			chanceRight -= 0.1;
		} else {
			chanceRight += 0.1;
			chanceLeft -= 0.1;
		}
		
		if(dy == 0.0) {
			chanceLeft += 0.1;
			chanceRight += 0.1;
			chanceUp -= 0.1;
			//chanceDown -= 0.1;
		} else if(dy > 0.0) {
			chanceUp += 0.1;
			//chanceDown -= 0.1;
		} else {
			//chanceDown += 0.1;
			chanceUp -= 0.1;
		}
		
		double rand = Math.random();
		rand -= chanceLeft;
		if(rand < 0.0) return LEFT;
		else {
			rand -= chanceRight;
			if(rand < 0.0) return RIGHT;
			else {
				rand -= chanceUp;
				if(rand < 0.0) return UP;
				else return DOWN;
			}
		}
	}
	
	@Override
	public void finishSpan() {
		setFlagOff(SPANNING);
		setFlagOn(HITTABLE);
		setFlagOn(VULNERABLE);
		if(!game.teams[team].isFlagOn(TeamManager.FREEZED)) setFlagOn(CAN_MOVE);
		if(game.teams[team].isFlagOn(TeamManager.SLOW_DOWN)) slow(true);
		if(isFlagOn(CAN_FIRE)) timerManager.scheduleTimedEvent(TIMER_FIRE, getWaitTimeToFire());
		timerManager.scheduleTimedEvent(TIMER_TURN, 1);
	}
	
	@Override
	public void finishRemove() {
		if(!isFlagOn(SCOREABLE)) game.objectsToRemove.add(this);
		else {
			timerManager.scheduleTimedEvent(TIMER_SHOW_VALUE, MAX_TIME_TO_SHOW_VALUE);
			setFlagOff(SCOREABLE);
		}
	}
	
	@Override
	public int getWaitTimeToFire() {
		return (int)(Math.random()*waitTimeToFire);
	}
	
	public void setType(int t) {
		if(t == RANDOM_TYPE) rank = (int)(Math.random()*3.9)+1;
		else rank = t;
		
		imgTank = game.enemyManager.img[rank];
		imgValue = game.enemyManager.imgValue[rank];
		setSpeed();
		setMaxBullets();
	}
	
	public void setSpeed() {
		if(rank == AITank.ROLLER) speed = FAST_SPEED;
		else if(rank == AITank.TURRET) speed = VERY_SLOW_SPEED;
		else speed = SLOW_SPEED;
		defaultSpeed = speed;
		minTimeToTurn = (int)(TURN_FACTOR/speed);
	}
	
	public void setTeam() {
		teamString = team == Unit.ALLY_TEAM ? "ALLY" : "ENEMY";
		teamColor = team == Unit.ALLY_TEAM ? Color.GREEN : Color.RED;
	}
	
	public void setMaxBullets() {
		maxBullets = rank > 3 ? 2 : 1;
	}
	
	public void setHitPoints(int op) {
		if(op == RANDOM_HIT_POINTS) {
			hitPoints = randomHitPoints();
			if(Game.randomBoolean(0.5) && hitPoints == MAX_HIT_POINTS && team != Unit.ALLY_TEAM) powerUpHitPoints = (int)(Math.random()*MAX_POWERUP_HIT_POINTS) + 1;
			else powerUpHitPoints = 0;
		}
		else {
			int dif = op - MAX_HIT_POINTS;
			hitPoints = Math.min(op, MAX_HIT_POINTS);
			powerUpHitPoints = (dif > 0 ? Math.min(dif, MAX_POWERUP_HIT_POINTS) : 0);
		}
	}
	
	@Override
	public void blocked() {
		if(game.terrainManager.impassableBlocking && isFlagOn(HAS_A_TARGET)) {
			setFlagOn(BLOCKED);
		}
		timerManager.remainingTimeForEvent[TIMER_TURN] = Math.min(timerManager.remainingTimeForEvent[TIMER_TURN], MAX_TIME_TO_TURN_ON_BLOCK);
	}
	
	@Override
	public void slow(boolean b) {
		if(b) {
			setFlagOn(SLOW);
			minTimeToTurn *= 1.5;
		} else {
			setFlagOff(Tank.SLOW);
			setFlagOn(Tank.CAN_MOVE);
			setSpeed();
		}
	}
	
	public int randomHitPoints() {
		return (int)(Math.random()*MAX_HIT_POINTS)+1;
	}
	
	@Override
	public void hitLetal(Projectile projectile, Unit owner) {
		powerUpHitPoints = Math.min(powerUpHitPoints, 1);
		hitPoints = 0;
		hitted(projectile, owner);
	}
	
	@Override
	public int hitted(Projectile projectile, Unit owner) {
		if(isFlagOn(VULNERABLE)) {
			if(powerUpHitPoints > 0) {
				if(owner != null && owner.team == Unit.PLAYER_TEAM) game.spanPow();
				powerUpHitPoints--;
			} else hitPoints = Math.max(hitPoints-1, 0);
			
			if(Math.max(hitPoints, powerUpHitPoints) < 1) {
				if(owner != null && !(team == Unit.ALLY_TEAM && owner.team == Unit.PLAYER_TEAM)) {
					owner.score();
					if(owner.team == Unit.PLAYER_TEAM) setFlagOn(SCOREABLE);
				}
				destroy();
				game.audioPlayer.playSound(AudioPlayer.ENEMY_DIE);
			} else game.audioPlayer.playSound(AudioPlayer.TANK_HIT);
		}
		return 0;
	}
	
	@Override
	public void upgrade() {
		hitPoints = Math.max(hitPoints + 1, MAX_HIT_POINTS);
	}
	
	@Override
	public boolean maxUpgrade() {
		if(!isFlagOn(REMOVING)) {
			hitPoints = MAX_HIT_POINTS;
			return true;
		} else return false;
	}
	
	public void paint(Graphics2D g) {
		/*if(game.visibilityEnabled) {
			if(isFlagOn(VISIBLE) || game.isFlagOn(Game.FULL_VISIBILITY)) visibility = Math.min(visibility + ALPHA_DELTA, 1.0);
			else visibility = Math.max(visibility - ALPHA_DELTA, 0.0);
		}*/
			
		//if(visibility > 0.0) {
			g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, (float)visibility));
			
			if(isFlagOn(SPANNING)) g.drawImage(span[spanOrder[timerManager.remainingTimeForEvent[TIMER_SPAN]/7]], (int)x, (int)y, null);
			else if(isFlagOn(REMOVING)) {
				if(!isFlagOn(SHOW_VALUE)) g.drawImage(rem[remOrder[timerManager.remainingTimeForEvent[TIMER_REMOVE]/10]], (int)x-20, (int)y-20, null);
			}
			else if(!isFlagOn(SHOW_VALUE)) {
				Image imgToDraw = null;
				if(powerUpHitPoints > 0) {
					if(game.isFlagOn(Game.BLINK_STATE)) imgToDraw = imgTank[POWERUP_COLOR][lastMove][walk/8];
					else imgToDraw = imgTank[1][lastMove][walk/8];
				} else imgToDraw = imgTank[hitPoints][lastMove][walk/8];
				
				if(game.teams[team].isFlagOn(TeamManager.SLOW_DOWN) && isFlagOn(WALKING)) game.drawSlowEffect(g, imgToDraw, (int)x, (int)y, lastMove);
				g.drawImage(imgToDraw, (int)x, (int)y, null);
				
				g.setColor(teamColor);
				g.drawString(teamString, (int)x, (int)y);
				
				if(target != null && team == Unit.ENEMY_TEAM) {
					if(target instanceof Eagle) {
						Stroke backup = g.getStroke();
						g.setStroke(new BasicStroke(3, BasicStroke.CAP_ROUND, BasicStroke.JOIN_MITER));
						g.drawLine((int)xTarget, (int)yTarget, (int)target.xTarget, (int)target.yTarget);
						g.setStroke(backup);
					} else if(target instanceof PlayerTank)
						g.drawLine((int)xTarget, (int)yTarget, (int)target.xTarget, (int)target.yTarget);
				}
				
				if(turret != null) turret.paint(g);
				if(!isFlagOn(VULNERABLE)) g.drawImage(vul[(timerManager.remainingTimeForEvent[TIMER_INVULNERABLE]%10)/5], (int)x, (int)y, null);
			}
		//}
		
		if(isFlagOn(SHOW_VALUE)) g.drawImage(imgValue, (int)x, (int)y, null);
		g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f));
		
		if(isFlagOn(TARGETED_BY_HOMING)) g.drawImage(imgHomingTargeted, (int)x, (int)y, null);
		if(isFlagOn(PAINT_TARGETED_BY_TURRET)) g.drawImage(imgTurretTargeted, (int)x, (int)y, null);
	}
	
}
