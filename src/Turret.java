import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.geom.AffineTransform;

import javax.swing.ImageIcon;


public class Turret extends Unit {
	
	static final int DEFAULT_TIME_TO_FIRE = 102;
	static final int WIDTH = 22;
	static final int HEIGHT = 22;
	static final int HALF_WIDTH = WIDTH/2;
	static final int HALF_HEIGHT = HEIGHT/2;
	static final int SIGHT_RANGE_SHORT = 200;
	static final int SIGHT_RANGE_LONG = 240;
	
	static final int DESPL_BALL_X = HALF_WIDTH - StraightBall.DIAMETER/2;
	static final int DESPL_BALL_Y = HALF_HEIGHT - StraightBall.DIAMETER/2;
	static final double MAX_TURN_ANGLE = Math.PI/90;
	static final double MIN_AIM_ANGLE = Math.PI/270;
	
	static final int ID_BASE = 2000;
	
	Unit owner;
	GameObject target;
	int timeToFire;
	int maxTimeToFire;
	int adjX;
	int adjY;
	double angle;
	double lastAngle;
	double maxTurnAngle;
	double dist;
	double sight;
	
	Image imgTurret = (new ImageIcon("src/turret.png")).getImage().getScaledInstance(WIDTH, HEIGHT, Image.SCALE_FAST);
	
	public Turret(Unit owner) {
		super(owner.game, owner.x, owner.y, WIDTH, HEIGHT, ID_BASE + owner.id, owner.team);
		this.owner = owner;
		adjX = (this.owner.width - WIDTH)/2 - 1;
		adjY = (this.owner.height - HEIGHT)/2 - 1;
		timeToFire = 0;
		maxTimeToFire = DEFAULT_TIME_TO_FIRE;
		angle = MINUS_HALF_PI;
		lastAngle = angle;
		maxTurnAngle = MAX_TURN_ANGLE;
		dist = Double.POSITIVE_INFINITY;
		sight = SIGHT_RANGE_LONG;
	}
	
	public Turret(Game game, double x, double y, int team) {
		super(game, x, y, WIDTH, HEIGHT, ID_BASE, team);
		owner = this;
		timeToFire = 0;
		maxTimeToFire = DEFAULT_TIME_TO_FIRE;
		angle = MINUS_HALF_PI;
		lastAngle = angle;
		maxTurnAngle = MAX_TURN_ANGLE;
		dist = Double.POSITIVE_INFINITY;
		sight = SIGHT_RANGE_SHORT;
	}
	
	public void move() {
		x = owner.x + adjX;
		y = owner.y + adjY;
		updateTargetPos();
		target = getTarget();
		
		if(target != null) {
			dist = owner.distTo(target);
			if(dist <= sight) {
				angle = owner.angleTo(target);
				lastAngle = newAngle(lastAngle, angle, maxTurnAngle);
				target.setFlagOn(Tank.TARGETED_BY_TURRET);
			}
		} else {
			dist = Double.POSITIVE_INFINITY;
		}
		
		if(timeToFire == maxTimeToFire) {
			if(dist <= sight && Math.abs(lastAngle-angle) < MIN_AIM_ANGLE) {
				//System.out.println("Firing");
				fire();
				timeToFire -= maxTimeToFire;
			}
		} else timeToFire++;
	}
	
	public GameObject getTarget() {
		return mostClosestTarget();
	}
	
	public GameObject mostClosestTarget() {
		return (owner.selectTargets(1, new TankComparator(TankComparator.CLOSEST)).poll());
	}
	
	public void fire() {
		game.objectsToInsert.add(new StraightBall(game, x + DESPL_BALL_X, y + DESPL_BALL_Y, lastAngle, owner));
		game.audioPlayer.playSound(AudioPlayer.BALL_FIRE);
	}
	
	public void paint(Graphics2D g) {
		AffineTransform backup = g.getTransform();
		g.rotate(lastAngle, x + HALF_WIDTH, y + HALF_HEIGHT);
		g.drawImage(imgTurret, (int)x, (int)y, null);
		g.setTransform(backup);
	}
}
