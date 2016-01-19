import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Stroke;
import java.awt.event.KeyEvent;
import java.awt.font.TextAttribute;
import java.text.AttributedString;
import java.util.ConcurrentModificationException;
import java.util.Deque;
import java.util.LinkedList;
import java.util.NoSuchElementException;


public class PlayerTank extends Tank {
	
	static final int PLAYER_ONE = 0;
	static final int PLAYER_TWO = 1;
	static final int PLAYER_THREE = 2;
	static final int PLAYER_FOUR = 3;
	
	static final int MIN_PLAYER = PLAYER_ONE;
	static final int MAX_PLAYER = PLAYER_FOUR;
	
	static final int LEVEL_1 = 1;
	static final int LEVEL_2 = 2;
	static final int LEVEL_3 = 3;
	static final int LEVEL_4 = 4;
	
	static final int MIN_BULLET = 1;
	static final int MAX_BULLET = 2;
	static final int MIN_WAIT_TIME_TO_FIRE = 1;
	
	static final double PLAYER_SPEED = 1.15;
	static final int INIT_LIVES = 40;
	static final int DEFAULT_RANK = LEVEL_2;
	
	static final int TIME_INVULNERABLE_AFTER_SPAN = 300;
	static final int TIME_TO_RESET_COMBO = 500;
	static final int TIME_TO_SPAN_COMBO = 30;
	static final int MAX_COMBO_FONT_SIZE = 20;
	static final int MIN_COMBO_FONT_SIZE = 14;
	static final int DIF_FONT = MAX_COMBO_FONT_SIZE - MIN_COMBO_FONT_SIZE;
	static final int TIME_TO_START_VANISH_COMBO = 100;
	static final int MIN_COMBO_TO_SHOW = 3;
	static final int TIME_RELEASE_SMOKE = 25;
	
	static final int TIME_TO_SHOW_COMBO_BONUS = 100;
	static final int[] DEFAULT_TIME_TO_FIRE_LASER = {0,50,40,30,20};
	static final int TIME_TO_FIRE_MINIGUN = 9;
	
	// Flags for PlayerTank class
	static final int ACTIVE = 20;
	static final int COMBO = 21;
	static final int SHOW_COMBO_BONUS = 22;
	static final int CAN_FIRE_ONCE = 23;
	static final int LOW_HEALTH = 24;
	
	// Timers for PlayerTank class
	static final int TIMER_COMBO = 12;
	static final int TIMER_SHOW_COMBO_BONUS = 13;
	static final int TIMER_SMOKE = 14;
	
	// Control IDs
	static final int GET_LIVES = 0;
	static final int FIRE_ONCE = 5;
	static final int FIRE_RAPID = 6;
	static final int WARP = 7;
	
	static final int[][] CONTROLS = {
		// Player One
		{KeyEvent.VK_M,	// Get Lives
		KeyEvent.VK_D,	// Right
		KeyEvent.VK_W,	// Up
		KeyEvent.VK_S,	// Down
		KeyEvent.VK_A,	// Left
		KeyEvent.VK_N,	// Fire once
		KeyEvent.VK_M,	// Fire rapid
		KeyEvent.VK_B	// Warp
		},
		// Player Two
		{KeyEvent.VK_NUMPAD2,	// Get Lives
		KeyEvent.VK_RIGHT,	// Right
		KeyEvent.VK_UP,	// Up
		KeyEvent.VK_DOWN,	// Down
		KeyEvent.VK_LEFT,	// Left
		KeyEvent.VK_NUMPAD1,	// Fire once
		KeyEvent.VK_NUMPAD2,	// Fire rapid
		KeyEvent.VK_NUMPAD3		// Warp
		},
		// Player Three
		{KeyEvent.VK_C,	// Get Lives
		KeyEvent.VK_J,	// Right
		KeyEvent.VK_Y,	// Up
		KeyEvent.VK_H,	// Down
		KeyEvent.VK_G,	// Left
		KeyEvent.VK_V,	// Fire once
		KeyEvent.VK_C,	// Fire rapid
		KeyEvent.VK_X	// Warp
		},
		// Player Four (undefined)
		null
	};
	
	static final Color[] COLORS = {
		Color.YELLOW,
		new Color(0, 130, 40),
		Color.BLUE,
		Color.ORANGE
	};
	
	static final int DEFAULT_MAX_HIT_POINTS = 3;
	static final int HEALTH_BAR_SHIFT_X = 0;
	static final int HEALTH_BAR_SHIFT_Y = -6;
	static final int HEALTH_BAR_THICKNESS = 4;
	
	static final int WEAPON_BAR_SHIFT_X = 0;
	static final int WEAPON_BAR_SHIFT_Y = -3;
	static final int WEAPON_BAR_THICKNESS = 4;
	
	int[] controls;
	Location loc;
	int maxHitPoints;
	int lives;
	int combo;
	int comboBonusCounter;
	int keyMask = 0;
	Deque<Integer> nextMove;
	WarpPad warpPad;
	
	Bar healthBar;
	LinkedList<Smoke> smokes;
	
	AttributedString actualCombo;
	Image[][][] imgTank;
	
	public PlayerTank(Game game, Slot slot, int id) {
		super(game, slot.x, slot.y, slot.defaultDir, id, Unit.PLAYER_TEAM);
		imgTank = PlayerTeamManager.IMG_PLAYER_TANK[id];
		loc = slot;
		controls = CONTROLS[id];
		nextMove = new LinkedList<Integer>();
		defaultSpeed = speed = PLAYER_SPEED;
		setFlagOn(CAN_PICKUP_POW);
		setFlagOn(CAN_FIRE_ONCE);
		setFlagOn(VISIBLE);
		lives = INIT_LIVES;
		score = 0;
		combo = 0;
		comboBonusCounter = 0;
		waitTimeToFire = MIN_WAIT_TIME_TO_FIRE;
		setMaxBullets();
		
		smokes = new LinkedList<Smoke>();
		healthBar = new Bar(this, HEALTH_BAR_SHIFT_X, HEALTH_BAR_SHIFT_Y, WIDTH-3, HEALTH_BAR_THICKNESS, hitPoints, DEFAULT_MAX_HIT_POINTS, null, true, Bar.TYPE_HEALTH);
		weaponBar = new Bar(this, WEAPON_BAR_SHIFT_X, WEAPON_BAR_SHIFT_Y, WIDTH-3, WEAPON_BAR_THICKNESS, weaponPower, 1, Color.WHITE, true, Bar.TYPE_POWER);
		respanDefault();
	}
	
	@Override
	public void triggerInSpecificTimer(int timerId) {
		switch(timerId) {
		case TIMER_COMBO:
			setFlagOn(COMBO);
			break;
		case TIMER_SHOW_COMBO_BONUS:
			setFlagOn(SHOW_COMBO_BONUS);
			break;
		}
	}
	
	@Override
	public void triggerOutSpecificTimer(int timerId) {
		switch(timerId) {
		case TIMER_COMBO:
			resetCombo();
			break;
		case TIMER_SHOW_COMBO_BONUS:
			setFlagOff(SHOW_COMBO_BONUS);
			break;
		case TIMER_SMOKE:
			if(isFlagOn(LOW_HEALTH) && isFlagOn(ACTIVE)) {
				releaseSmoke();
				timerManager.scheduleTimedEvent(TIMER_SMOKE, TIME_RELEASE_SMOKE);
			}
		}
	}
	
	@Override
	public void move() {
		timerManager.processEvents();
		
		if(game.isFlagOn(Game.GAME_OVER)) {
			setFlagOff(FIRING);
			nextMove.clear();
		}
		
		warpPad = null;
		
		if(isFlagOn(ACTIVE)) {
			if(!isFlagOn(SPANNING) && !isFlagOn(REMOVING)) {
				setFlagOff(TARGETED);
				if(isFlagOn(CAN_MOVE)) {
					if(!nextMove.isEmpty()) {
						try{
							lastMove = nextMove.getLast();
							double nextPos = game.terrainManager.nextMove(x, y, lastMove, speed, team);
							if(lastMove == LEFT || lastMove == RIGHT) {
								y = round(y,20);
								x = nextPos;
							} else {
								x = round(x,20);
								y = nextPos;
							}
							
							updateTargetPos();
							walk();
							setFlagOn(WALKING);
						} catch (NoSuchElementException nsee) {
							
						} catch (NullPointerException npe) {
							
						}
					} else {
						setFlagOff(WALKING);
					}
				}
				
				if(isFlagOn(FIRING) && isFlagOn(CAN_FIRE) && numBullets < maxBullets) fire();
				moveTurret();
			}
		} else {
			respanDefault();
		}
		
		if(!smokes.isEmpty()) {
			LinkedList<Smoke> aliveSmokes = new LinkedList<Smoke>();
			for(Smoke s : smokes) {
				if(s.active) {
					s.move();
					aliveSmokes.add(s);
				}
			}
			smokes = aliveSmokes;
		}
	}
	
	public void keyPressed(KeyEvent e) {
		if(!game.isFlagOn(Game.GAME_OVER)) {
			int key = e.getKeyCode();
			if(isFlagOn(ACTIVE)) {
				if(key == controls[RIGHT]) setNextMove(RIGHT);
				else if(key == controls[UP]) setNextMove(UP);
				else if(key == controls[DOWN]) setNextMove(DOWN);
				else if(key == controls[LEFT]) setNextMove(LEFT);
				else if(key == controls[FIRE_ONCE]) {
					if(isFlagOn(CAN_FIRE_ONCE) && (weapon == LASER || numBullets < maxBullets)) {
						fire();
						setFlagOff(CAN_FIRE_ONCE);
					}
				} else if(key == controls[FIRE_RAPID]) setFlagOn(FIRING);
				else if(key == controls[WARP]) if(warpPad != null && isFlagOn(CAN_MOVE)) warpPad.warp(this);
			} else if(key == CONTROLS[id][GET_LIVES]) getLives();
		}
	}
	
	public void keyReleased(KeyEvent e) {
		if(isFlagOn(ACTIVE)) {
			int key = e.getKeyCode();
			if(key == controls[RIGHT]) deleteMove(RIGHT);
			else if(key == controls[UP]) deleteMove(UP);
			else if(key == controls[DOWN]) deleteMove(DOWN);
			else if(key == controls[LEFT]) deleteMove(LEFT);
			else if(key == controls[FIRE_ONCE]) setFlagOn(CAN_FIRE_ONCE);
			else if(key == controls[FIRE_RAPID]) setFlagOff(FIRING);
		}
	}
	
	@Override
	public void walk() {
		walk = (walk+1)%4;
	}
	
	public void setNextMove(int dir) {
		if((keyMask & (1 << dir)) == 0) {
			keyMask |= (1 << dir);
			nextMove.add(dir);
		}
	}
	
	public void deleteMove(int dir) {
		if((keyMask & (1 << dir)) != 0) {
			keyMask &= ~(1 << dir);
			nextMove.remove(dir);
		}
	}
	
	@Override
	public void finishSpan() {
		setFlagOff(SPANNING);
		timerManager.scheduleTimedEvent(TIMER_INVULNERABLE, TIME_INVULNERABLE_AFTER_SPAN);
		setFlagOn(HITTABLE);
		setFlagOn(CAN_MOVE);
	}
	
	@Override
	public int getWaitTimeToFire() {
		if(weapon == BULLET) return waitTimeToFire;
		else if(weapon == LASER) return DEFAULT_TIME_TO_FIRE_LASER[rank];
		else if(weapon == MG) return TIME_TO_FIRE_MINIGUN;
		return 0;
	}
	
	@Override
	public void upgrade() {
		rank = Math.min(rank+1,LEVEL_4);
		setRank();
	}
	
	@Override
	public boolean maxUpgrade() {
		if(!isFlagOn(REMOVING)) {
			rank = LEVEL_4;
			setRank();
			return true;
		} else return false;
	}
	
	public void downgrade() {
		rank--;
		setRank();
	}
	
	public void setRank() {
		//setHitPoints();
		setMaxBullets();
	}
	
	public void setHitPoints() {
		if(rank > 3) hitPoints = 2;
		else hitPoints = 1;
	}
	
	public void setMaxBullets() {
		maxBullets = (rank > LEVEL_2 ? 2 : 1);
	}
	
	public void getLives() {
		lives += game.playerManager.liveAvailable();
		respanDefault();
	}
	
	public void extraLive() {
		lives++;
		if(game.isFlagOn(Game.EAGLE)) for(Eagle e : game.eagles) e.heal();
	}
	
	@Override
	public void incHealthCap() {
		maxHitPoints++;
		healthBar.setMaxHitPoints(maxHitPoints, hitPoints = maxHitPoints);
	}
	
	@Override
	public int hitted(Projectile projectile, Unit owner) {
		if(isFlagOn(VULNERABLE)) {
			if(isFlagOn(CAN_PASS_WATER))
				setFlagOff(CAN_PASS_WATER);
			else if(hitPoints == 1 && turret != null) {
				turret = null;
				game.audioPlayer.playSound(AudioPlayer.TANK_HIT);
			} else {
				hitPoints--;
				if(hitPoints < 1) {
					if(rank == LEVEL_4) {
						downgrade();
						hitPoints = maxHitPoints;
						game.audioPlayer.playSound(AudioPlayer.TANK_HIT);
					} else {
						destroy();
						resetCombo();
						game.audioPlayer.playSound(AudioPlayer.PLAYER_DIE);
						game.audioPlayer.stopSound(AudioPlayer.PLAYER_MOVE);
						if(owner != null) owner.score();
					}
				} else {
					game.audioPlayer.playSound(AudioPlayer.TANK_HIT);
				}
				healthBar.update(hitPoints);
			}
			
		}
		return 0;
	}
	
	@Override
	public void score() {
		score++;
		combo++;
		comboBonusCounter++;
		timerManager.scheduleTimedEvent(TIMER_COMBO, TIME_TO_RESET_COMBO);
		if(comboBonusCounter % 10 == 0 && isFlagOn(ACTIVE)) {
			extraLive();
			game.audioPlayer.playSound(AudioPlayer.STOCK);
			comboBonusCounter = 0;
			timerManager.scheduleTimedEvent(TIMER_SHOW_COMBO_BONUS, TIME_TO_SHOW_COMBO_BONUS);
		}
	}
	
	public void resetCombo() {
		setFlagOff(COMBO);
		combo = 0;
		comboBonusCounter = 0;
	}
	
	public void finishRemove() {
		setFlagOff(REMOVING);
		respanDefault();
	}
	
	public void fullHealth() {
		if(isFlagOn(HITTABLE)) {
			hitPoints = maxHitPoints;
			healthBar.update(hitPoints);
		}
	}
	
	@Override
	public void lowHealth(boolean b) {
		if(b) {
			setFlagOn(LOW_HEALTH);
			timerManager.scheduleTimedEvent(TIMER_SMOKE, TIME_RELEASE_SMOKE);
		}
		else setFlagOff(LOW_HEALTH);
	}
	
	public void releaseSmoke() {
		smokes.add(new Smoke(this, (int)x, (int)y));
	}
	
	public void respanDefault() {
		if(lives > 0) {
			lives--;
			setFlagOn(ACTIVE);
			setFlagOff(REMOVING);
			setFlagOn(SPANNING);
			x = loc.x;
			y = loc.y;
			xTarget = x + halfWidth;
			yTarget = y + halfHeight;
			lastMove = UP;
			rank = DEFAULT_RANK;
			maxHitPoints = DEFAULT_MAX_HIT_POINTS;
			hitPoints = maxHitPoints;
			healthBar.setMaxHitPoints(maxHitPoints, hitPoints);
			changeWeapon(BULLET);
			setRank();
			turret = null;
			timerManager.endTimer(TIMER_FAST);
			timerManager.scheduleTimedEvent(TIMER_SPAN, DEFAULT_SPAN_TIME);
		} else {
			setFlagOff(ACTIVE);
		}
	}
	
	public void paint(Graphics2D g) {
		if(isFlagOn(ACTIVE)) {
			if(isFlagOn(SPANNING)) g.drawImage(span[spanOrder[timerManager.remainingTimeForEvent[TIMER_SPAN]/7]], (int)x, (int)y, null);
			else if(isFlagOn(REMOVING)) g.drawImage(rem[remOrder[timerManager.remainingTimeForEvent[TIMER_REMOVE]/10]], (int)x-20, (int)y-20, null);
			else {
				if(isFlagOn(GHOST)) g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.6f));
				g.drawImage(imgTank[rank][lastMove][walk/2], (int)x, (int)y, null);
				g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f));
				
				if(isFlagOn(CAN_PASS_WATER)) {
					Stroke backup = g.getStroke();
					g.setStroke(new BasicStroke(2, BasicStroke.CAP_ROUND, BasicStroke.JOIN_MITER));
					g.setColor(COLORS[id]);
					g.drawRoundRect((int)x+1, (int)y+1, WIDTH-2, HEIGHT-2, 5, 5);
					g.setStroke(backup);
				}
				
				if(isFlagOn(HAS_BOUNCING_SHIELD)) {
					Stroke backup = g.getStroke();
					g.setStroke(new BasicStroke(2, BasicStroke.CAP_ROUND, BasicStroke.JOIN_MITER));
					g.setColor(Color.BLUE);
					g.drawRoundRect((int)x-1, (int)y-1, WIDTH+1, HEIGHT+1, 5, 5);
					g.setStroke(backup);
				}
				
				try {
					for(Smoke s : smokes) s.paint(g);
				} catch (ConcurrentModificationException cme) {
					
				}
				
				if(!isFlagOn(VULNERABLE)) g.drawImage(vul[(timerManager.remainingTimeForEvent[TIMER_INVULNERABLE]%10)/5], (int)x, (int)y, null);
				
				if(turret != null) turret.paint(g);
				
				if(isFlagOn(TARGETED))
					g.drawImage(imgTargeted, (int)x, (int)y, null);
				/*
				if(targetedBy > 0) {
					g.drawImage(imgTargeted, (int)x, (int)y, null);
					if(targetedBy > 1) {
						numTargets = new AttributedString(Integer.toString(targetedBy));
						numTargets.addAttribute(TextAttribute.FONT, fontLarge);
						numTargets.addAttribute(TextAttribute.FOREGROUND, Color.RED);
						g.drawString(numTargets.getIterator(), (int)x+WIDTH, (int)y+25);
					}
				}*/
				healthBar.paint(g);
				if(weapon != BULLET) weaponBar.paint(g);
				
				if(isFlagOn(COMBO) && combo >= MIN_COMBO_TO_SHOW) {
					actualCombo = new AttributedString("Combo:" + combo);
					actualCombo.addAttribute(TextAttribute.FONT, fontSmall);
					actualCombo.addAttribute(TextAttribute.FOREGROUND, Color.MAGENTA);
					double currentTime = TIME_TO_RESET_COMBO - timerManager.remainingTimeForEvent[TIMER_COMBO];
					if(currentTime < TIME_TO_SPAN_COMBO)
						actualCombo.addAttribute(TextAttribute.FONT, new Font("Consolas", Font.BOLD, (int)(MAX_COMBO_FONT_SIZE - (DIF_FONT*(currentTime/TIME_TO_SPAN_COMBO)))));
					if(timerManager.remainingTimeForEvent[TIMER_COMBO] < TIME_TO_START_VANISH_COMBO)
						g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, (float)(timerManager.remainingTimeForEvent[TIMER_COMBO]/100.0)));
					g.drawString(actualCombo.getIterator(), (int)x-10, (int)y-6);
					g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f));
				}
				
				if(isFlagOn(SHOW_COMBO_BONUS)) {
					g.setColor(Color.WHITE);
					g.drawString("1UP!", (int)x+8, (int)y-12);
				}
			}
		}
	}
}
