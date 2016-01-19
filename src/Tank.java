import java.awt.Color;
import java.awt.Font;
import java.awt.Image;
import java.text.AttributedString;
import java.util.ArrayList;
import java.util.PriorityQueue;

import javax.swing.ImageIcon;


public abstract class Tank extends Unit implements Hittable, EventDriven {
	
	static final int WIDTH = 40;
	static final int HEIGHT = 40;
	
	static final int MAX_X = Game.MAX_X - WIDTH;
	static final int MAX_Y = Game.MAX_Y - HEIGHT;
	
	static final int DEFAULT_SPAN_TIME = 90;
	static final int DEFAULT_REMOVE_TIME = 30;
	
	static final int RANDOM_DIR = 0;
	
	static final int BULLET = 1;
	static final int LASER = 2;
	static final int MG =3;
	
	static final int PLAYER_SIGHT_RANGE = 200;
	static final int AI_SIGHT_RANGE = 200;
	static final int AI_EAGLE_SIGHT_RANGE = 220;
	static final int DIF = 4;
	
	static final double ALPHA_DELTA = 0.05;
	
	static final int LITTLE_HIT_POINTS_PER_HIT_POINT = 5;
	
	// Flags for Tank class
	static final int CAN_MOVE = 1;
	static final int WALKING = 2;
	static final int CAN_FIRE = 3;
	static final int FIRING = 4;
	static final int VULNERABLE = 5;
	static final int SPANNING = 6;
	static final int REMOVING = 7;
	static final int CAN_PICKUP_POW = 8;
	static final int CAN_PASS_WATER = 9;
	static final int HAS_A_TARGET = 10;
	static final int HAS_TURRET = 11;
	static final int VISIBLE = 12;
	//static final int HAS_LASER = 13;
	static final int GHOST = 14;
	static final int HAS_BOUNCING_SHIELD = 15;
	static final int FAST = 16;
	static final int SLOW = 17;
	static final int CAN_LOOK_FOR_TARGET = 18;
	
	static final int TARGETED = 25;
	static final int TARGETED_BY_HOMING = 26;
	static final int TARGETED_BY_TURRET = 27;
	static final int PAINT_TARGETED_BY_TURRET = 28;
	
	// Timer IDs
	static final int TIMER_SPAN = 1;
	static final int TIMER_REMOVE = 2;
	static final int TIMER_FIRE = 3;
	static final int TIMER_INVULNERABLE = 4;
	//static final int TIMER_LASER = 5;
	static final int TIMER_FAST = 6;
	static final int TIMER_GHOST = 7;
	static final int TIMER_BOUNCING_SHIELD = 8;
	static final int TIMER_LOOK_FOR_TARGET = 9;
	//static final int TIMER_MG = 10;
	
	EventManager timerManager;
	
	double speed;
	double defaultSpeed;
	
	int lastMove;
	int weapon;
	int rank;
	
	int waitTimeToFire;
	int walk;
	int timeToLookNewTarget;
	
	int score;
	int numBullets;
	int maxBullets;
	int hitPoints;
	int littleHitPoints;
	double fireX;
	double fireY;
	int weaponPower = 0;
	
	double visibility;
	double distTotarget;
	double sightRange;
	double eagleSightRange;
	
	int[] spanOrder = {0,1,2,3,2,1,0,1,2,3,2,1,0};
	
	int[] remOrder = {0,1,0,0};
	
	//int targetedBy = 0;
	GameObject target = null;
	Turret turret = null;
	Bar weaponBar;
	
	Image[] span = {
			(new ImageIcon("src/span1.png")).getImage().getScaledInstance(WIDTH, HEIGHT, Image.SCALE_DEFAULT),
			(new ImageIcon("src/span2.png")).getImage().getScaledInstance(WIDTH, HEIGHT, Image.SCALE_DEFAULT),
			(new ImageIcon("src/span3.png")).getImage().getScaledInstance(WIDTH, HEIGHT, Image.SCALE_DEFAULT),
			(new ImageIcon("src/span4.png")).getImage().getScaledInstance(WIDTH, HEIGHT, Image.SCALE_DEFAULT)
	};
	
	Image[] rem = {
			(new ImageIcon("src/die1.png")).getImage().getScaledInstance(WIDTH*2, HEIGHT*2, Image.SCALE_DEFAULT),
			(new ImageIcon("src/die2.png")).getImage().getScaledInstance(WIDTH*2, HEIGHT*2, Image.SCALE_DEFAULT)
	};
	
	Image[] vul = {
			(new ImageIcon("src/shield1.png")).getImage().getScaledInstance(WIDTH, HEIGHT, Image.SCALE_DEFAULT),
			(new ImageIcon("src/shield2.png")).getImage().getScaledInstance(WIDTH, HEIGHT, Image.SCALE_DEFAULT)
	};
	
	Image imgTargeted = (new ImageIcon("src/target.png")).getImage().getScaledInstance(WIDTH, HEIGHT, Image.SCALE_DEFAULT);
	Image imgHomingTargeted = (new ImageIcon("src/homingTarget.png")).getImage().getScaledInstance(WIDTH, HEIGHT, Image.SCALE_DEFAULT);
	Image imgTurretTargeted = (new ImageIcon("src/turretTarget.png")).getImage().getScaledInstance(WIDTH, HEIGHT, Image.SCALE_DEFAULT);
	
	AttributedString numTargets;
	
	static final Font fontLarge = new Font("Consolas", Font.BOLD, 24);
	static final Font fontSmall = new Font("Consolas", Font.BOLD, 14);
	
	public Tank(Game game, int x, int y, int initDir, int id, int team) {
		super(game, x, y, WIDTH, HEIGHT, id, team);
		this.game = game;
		
		timerManager = new EventManager(this);
		
		setFlagOn(CAN_FIRE);
		setFlagOn(CAN_LOOK_FOR_TARGET);
		lastMove = setInitDir(initDir);
		weapon = BULLET;
		walk = 0;
		score = 0;
		littleHitPoints = LITTLE_HIT_POINTS_PER_HIT_POINT;
		//putTurret();
		//targetedBy = 0;
		visibility = this.game.visibilityEnabled ? 0.0 : 1.0;
	}
	
	public void eventIn(int timerId) {
		switch(timerId) {	// Do work based on this timer ID
		case TIMER_SPAN:
			setFlagOn(SPANNING);
			break;
		case TIMER_FIRE:
			setFlagOff(CAN_FIRE);
			break;
		case TIMER_REMOVE:
			setFlagOn(REMOVING);
			break;
		case TIMER_INVULNERABLE:
			setFlagOff(VULNERABLE);
			break;
		case TIMER_FAST:
			if(!isFlagOn(FAST)) speedFast();
			break;
		/*case TIMER_LASER:
			changeWeapon(LASER);
			break;*/
		case TIMER_GHOST:
			setGhost(true);
			break;
		case TIMER_BOUNCING_SHIELD:
			setFlagOn(HAS_BOUNCING_SHIELD);
			break;
		/*case TIMER_MG:
			changeWeapon(MG);
			break;*/
		default:
			triggerInSpecificTimer(timerId);	// This must be a timer defined in subclasses, send to it
		}
	}
	
	public void eventOut(int timerId) {
		switch(timerId) {	// Do work based on this timer ID
		case TIMER_SPAN:
			finishSpan();
			break;
		case TIMER_REMOVE:
			finishRemove();
			break;
		case TIMER_FIRE:
			setFlagOn(CAN_FIRE);
			break;
		case TIMER_INVULNERABLE:
			setFlagOn(VULNERABLE);
			break;
		case TIMER_FAST:
			speedNormal();
			break;
		/*case TIMER_LASER:
			changeWeapon(BULLET);
			break;*/
		case TIMER_GHOST:
			setGhost(false);
			break;
		case TIMER_BOUNCING_SHIELD:
			setFlagOff(HAS_BOUNCING_SHIELD);
			break;
		case TIMER_LOOK_FOR_TARGET:
			setFlagOn(CAN_LOOK_FOR_TARGET);
			break;
		/*case TIMER_MG:
			changeWeapon(BULLET);
			break;*/
		default:
			triggerOutSpecificTimer(timerId);	// This must be a timer defined in subclasses, send to it
		}
	}
	
	public void triggerInSpecificTimer(int timerId) {/*Defined in subclasses*/}
	
	public void triggerOutSpecificTimer(int timerId) {/*Defined in subclasses*/}
	
	public void eventSecond(int second) { /* Not used in Tank for now */ }
	
	public void finishSpan() {/*Defined in subclasses*/}
	
	public void finishRemove() {/*Defined in subclasses*/}
	
	public void walk() {/*Defined in subclasses*/}
	
	public void move() {/*Defined in subclasses*/}
	
	public void moveTurret() {
		if(turret != null) turret.move();
	}
	
	public void blocked() {/*Defined in subclasses*/}
	
	public int setInitDir(int dir) {
		if(dir == RANDOM_DIR) return randomDir();
		else return dir;
	}
	
	public int randomDir() {
		return (int)(Math.random()*4) + 1;
	}
	
	public void hitLetal(Projectile projectile, Unit owner) {
		hitted(projectile, owner);
	}
	
	public void hittedLittle(Projectile projectile, Unit owner) {
		littleHitPoints--;
		if(littleHitPoints == 0) {
			hitted(projectile, owner);
			littleHitPoints = LITTLE_HIT_POINTS_PER_HIT_POINT;
		} else game.audioPlayer.playSound(AudioPlayer.LITTLE_HIT);
	}
	
	public int getWaitTimeToFire() {
		/*Must be defined in subclasses*/
		return 0;
	}
	
	public void fire() {	// Fire using current weapon
		setFireOrigin();	// Set shot origin
		if(weapon == BULLET) fireBullet();
		else {
			if(weapon == LASER) fireLaser();
			else if(weapon == MG) fireMinibullet();
			if(--weaponPower == 0) changeWeapon(BULLET);
			else weaponBar.update(weaponPower);
		}
		timerManager.scheduleTimedEvent(TIMER_FIRE, getWaitTimeToFire());	// Set time to wait until next shot
	}
	
	public void fireBullet() {	// Fire standard weapon
		int w;
		int h;
		if(lastMove == LEFT || lastMove == RIGHT) {
			fireY -= Bullet.HALF_HEIGHT;
			w = Bullet.WIDTH;
			h = Bullet.HEIGHT;
		} else {
			fireX -= Bullet.HALF_HEIGHT;
			w = Bullet.HEIGHT;
			h = Bullet.WIDTH;
		}
		game.objectsToInsert.add(new Bullet(this, id+numBullets, lastMove, fireX, fireY, w, h));
		numBullets++;	// Remember this shot
		if(team == PLAYER_TEAM) game.audioPlayer.playSound(AudioPlayer.PLAYER_FIRE); // Only a PlayerTank generates a sound event
	}
	
	public void fireLaser() {	// Fire Laser weapon
		int w;
		int h;
		double x0;
		double y0;
		if(lastMove == LEFT || lastMove == RIGHT) {
			h = Laser.AMPLITUDE + 2;
			y0 = fireY - Laser.AMPLITUDE + 1;
			if(lastMove == LEFT) {
				w = (int)fireX;
				x0 = Game.MIN_X;
			} else {
				w = Game.MAX_X - (int)fireX;
				x0 = fireX;
			}
			fireY -= 2;
		} else {
			w = Laser.AMPLITUDE + 2;
			x0 = fireX - Laser.AMPLITUDE + 1;
			if(lastMove == UP) {
				h = (int)fireY;
				y0 = Game.MIN_Y;
			} else {
				h = Game.MAX_Y - (int)fireY;
				y0 = fireY;
			}
			fireX -= 2;
		}
		game.objectsToInsert.add(new Laser(game, this, lastMove, x0, y0, w, h, fireX, fireY));
		game.audioPlayer.playSound(AudioPlayer.PLAYER_FIRE);
	}
	
	public void fireMinibullet() {
		double angle = 0.0;
		if(lastMove == LEFT) angle = Math.PI;
		else if(lastMove == DOWN) angle = HALF_PI;
		else if(lastMove == UP) angle = THREE_HALVES_PI;
		game.objectsToInsert.add(new Minibullet(game, xTarget - Minibullet.HALF_WIDTH, yTarget - Minibullet.HALF_HEIGHT, sumAngles(angle, randomDev()), this));
		game.audioPlayer.playSound(AudioPlayer.MG_FIRE);
	}
	
	public void setFireOrigin() {	// Set shot origin, save results in fireX and fireY
		if(lastMove == LEFT || lastMove == RIGHT) {
			if(lastMove == LEFT) fireX = x + DIF;
			else fireX = x + Tank.HEIGHT - DIF;
			fireY = y + halfHeight;
		} else {
			if(lastMove == UP) fireY = y + DIF;
			else fireY = y + Tank.HEIGHT - DIF;
			fireX = x + halfHeight;
		}
	}
	
	public void fireHomings(int n) {	// Fire standard homing missiles, choosing farthest Tanks first
		PriorityQueue<Tank> targets = selectTargets(n, new TankComparator(TankComparator.CLOSEST));
		for(Tank t : targets)
			game.objectsToInsert.add(new HomingMissile(game, xTarget, yTarget, this, t));
		
	}
	
	public void fireLazyHomings(int n) { // Fire lazy homing missiles, choosing strongest Tanks first
		PriorityQueue<Tank> targets = selectTargets(n, new TankComparator(TankComparator.STRONGEST));
		for(Tank t : targets)
			game.objectsToInsert.add(new LazyHoming(game, xTarget, yTarget, this, t));
	}
	
	public void fireErraticHomings(int n, int m) { // Fire lazy homing missiles, choosing strongest Tanks first
		PriorityQueue<Tank> targets = selectTargets(n, new TankComparator(TankComparator.STRONGEST));
		for(Tank t : targets)
			for(int i = 0; i < m; i++) game.objectsToInsert.add(new ErraticHomingMissile(game, xTarget, yTarget, this, t));
	}
	
	public void captureEnemies(int n) {	// Make some enemies allies, choosing closest Tanks first
		PriorityQueue<Tank> targets = selectTargets(n, new TankComparator(TankComparator.CLOSEST));
		for(Unit t : targets) {
			game.teams[t.team].tanks.remove(t);	// Remove Tank t form its team registry
			game.allyManager.insertTank(t);		// Add Tank t to our allies registry
		}
	}
	
	public void fireBouncingBalls(int num) {
		PriorityQueue<Tank> targets = selectTargets(num, new TankComparator(TankComparator.CLOSEST));
		for(Unit t : targets)
			game.objectsToInsert.add(new BouncingBall(game, xTarget, yTarget, angleTo(t), this));
	}
	
	public void fireHomingBall(int num) {
		PriorityQueue<Tank> targets = selectTargets(num, new TankComparator(TankComparator.CLOSEST));
		for(Tank t : targets)
			game.objectsToInsert.add(new BigHomingBall(this.game, xTarget, yTarget, this, t));
	}
	
	public void fireSprinklers(int num) {
		PriorityQueue<Tank> targets = selectTargets(num, new TankComparator(TankComparator.CLOSEST));
		for(Tank t : targets)
			game.objectsToInsert.add(new SprinklerMissile(this.game, xTarget - SprinklerMissile.HALF_HEIGHT, yTarget - SprinklerMissile.HALF_HEIGHT, this, t));
	}
	
	public void fireKiro() {
		Tank t = getClosestTarget();
		if(t != null) game.objectsToInsert.add(new Kiro(this.game, xTarget, yTarget, this, getClosestTarget()));
	}
	
	public void fireWave() {
		game.objectsToInsert.add(new Wave(game, xTarget, yTarget, this));
	}
	
	@Override
	public void removeBullet() {
		numBullets = Math.max(numBullets-1, 0);
	}
	
	public void upgrade() { /* Defined in subclasses */ }

	public boolean maxUpgrade() {
		// Defined in subclasses
		return false;
	}
	
	public void incHealthCap() { /* Defined in subclasses */ }
	
	public void destroy() {
		if(!isFlagOn(SPANNING)) {
			setFlagOff(HITTABLE);
			setFlagOff(CAN_MOVE);
		}
		setFlagOff(TARGETED_BY_HOMING);
		timerManager.scheduleTimedEvent(TIMER_REMOVE, DEFAULT_REMOVE_TIME);
	}
	
	public void setTarget(ArrayList<Tank> tanks) {
		if(!isFlagOn(CAN_LOOK_FOR_TARGET)) return;
		boolean eagleFound = false;
		if(team == Unit.ENEMY_TEAM && game.isFlagOn(Game.EAGLE)) {
			Eagle e = closestEagle();
			if(e != null && distTo(e) <= eagleSightRange) {
				if(target == null || !(target instanceof Eagle)) e.enemySight(this);
				target = e;
				e.targeted = true;
				eagleFound = true;
			}
		}
		
		if(!eagleFound){
			target = null;
			Unit tmp = selectTargets(1, new TankComparator(TankComparator.WEAKEST), sightRange).poll();
			if(tmp != null) {
				//tmp.targetedBy++;
				tmp.setFlagOn(TARGETED);
				target = tmp;
			}
		}
		timerManager.scheduleTimedEvent(TIMER_LOOK_FOR_TARGET, timeToLookNewTarget);
	}
	
	public Eagle closestEagle() {
		Eagle eagle = null;
		double minDist = Double.POSITIVE_INFINITY;
		for(Eagle e : game.eagles) {
			if(e.isFlagOn(Eagle.HITTABLE)) {
				double dist = distTo(e);
				if(dist < minDist) {
					minDist = dist;
					eagle = e;
				}
			}
		}
		return eagle;
	}
	
	public void setVisiblility() {
		setFlagOff(VISIBLE);
		for(Unit t : game.teams[Unit.PLAYER_TEAM].tanks) {
			if(distTo(t) < PLAYER_SIGHT_RANGE && ((PlayerTank)t).isFlagOn(PlayerTank.ACTIVE)) {
				setFlagOn(VISIBLE);
				break;
			}
		}
	}
	
	public void changeWeapon(int w) {
		if(w == LASER) {
			weaponBar.setColor(Color.CYAN);
			weaponBar.setMaxHitPoints(PowerUp.LASER_POWER, weaponPower = PowerUp.LASER_POWER);
		} else if(w == MG) {
			weaponBar.setColor(Color.RED);
			weaponBar.setMaxHitPoints(PowerUp.MG_POWER, weaponPower = PowerUp.MG_POWER);
		}
		weapon = w;
	}
	
	public void speedFast() {
		speed += (speed*0.5);
		setFlagOn(FAST);
	}
	
	public void speedNormal() {
		speed = defaultSpeed;
		setFlagOff(FAST);
	}
	
	public void slow(boolean b) { /* Defined in subclasses */ }
	
	public void setGhost(boolean b) {
		if(b) {
			setFlagOn(GHOST);
			setFlagOff(HITTABLE);
		} else {
			setFlagOff(GHOST);
			setFlagOn(HITTABLE);
		}
	}
	
	public void putTurret() {
		if(turret == null) turret = new Turret(this);
	}
	
	public void putLaserTurret() {
		if(turret == null) turret = new LaserTurret(this);
		else if(turret instanceof LaserTurret) ((LaserTurret)turret).setPower(LaserTurret.INIT_POWER);
	}
	
	public void putMGTurret() {
		if(turret == null) turret = new MGTurret(this);
	}
}
