import java.awt.Graphics2D;
import java.awt.Rectangle;

public class GameObject {
	
	static final double TWICE_PI = Math.PI*2.0;
	static final double HALF_PI = Math.PI/2.0;
	static final double MINUS_HALF_PI = HALF_PI*-1.0;
	static final double THREE_HALVES_PI = (3.0*Math.PI)/2.0;
	static final int MAX_ANGLE = 360;
	
	// Flags for GameObject class
	static final int HITTABLE = 0;
	static final int REMOVING = 1;
	static final int ALLY_INMUNE = 30;
	static final int DELETE = 31;
	
	// Copy Game direction definition
	static final int RIGHT = Game.RIGHT;
	static final int UP = Game.UP;
	static final int DOWN = Game.DOWN;
	static final int LEFT = Game.LEFT;
	
	Game game;
	int width;
	int height;
	int halfWidth;
	int halfHeight;
	double x;
	double y;
	double xTarget;
	double yTarget;
	
	int flags;
	
	int intersectableWidth;
	int intersectableHeight;
	
	public GameObject(Game game, double x, double y, int w, int h) {
		this.game = game;
		this.x = x;
		this.y = y;
		this.width = w;
		this.height = h;
		halfWidth = width/2;
		halfHeight = height/2;
		xTarget = x + halfWidth;
		yTarget = y + halfHeight;
	}
	
	public boolean isFlagOn(int flag) {
		return (flags & (1 << flag)) != 0;
	}
	
	public void setFlagOn(int flag) {
		flags  |= (1 << flag);
	}
	
	public void setFlagOff(int flag) {
		flags  &= ~(1 << flag);
	}
	
	public void toggleFlag(int flag) {
		flags ^= (1 << flag);
	}
	
	public void updateTargetPos() {
		xTarget = x + halfWidth;
		yTarget = y + halfHeight;
	}
	
	public static int round(double pos, int mod) {
		if(pos % mod >= mod/2) pos += mod - (pos % mod);
		else pos -= (pos % mod);
		return (int)pos;
	}
	
	public double distTo(GameObject go) {
		double dx = go.x-x;
		double dy = go.y-y;
		return Math.sqrt(dx*dx + dy*dy);
	}
	
	public double dist(double x1, double y1, double x2, double y2) {
		double dx = x2-x1;
		double dy = y2-y1;
		return Math.sqrt(dx*dx + dy*dy);
	}
	
	public double angleTo(GameObject go) {
		double dx = (go.xTarget - halfWidth) - x;
		double dy = (go.yTarget - halfHeight) - y;
		return (dx < 0.0 ? Math.atan(dy/dx)+Math.PI : Math.atan(dy/dx));
	}
	
	public double angleTo(double x, double y) {
		double dx = x - this.x;
		double dy = y - this.y;
		return (dx < 0.0 ? Math.atan(dy/dx)+Math.PI : Math.atan(dy/dx));
	}
	
	public double randomAngle() {
		return Math.toRadians(Math.random()*MAX_ANGLE);
	}
	
	public double newAngle(double lastAngle, double targetAngle, double maxTurnAngle) {
		double newAngle = 0.0;
		double difAngle = lastAngle - targetAngle;
		
		if(difAngle >= 0.0) {
			if(difAngle < Math.PI)
				newAngle = lastAngle - Math.min(difAngle, maxTurnAngle);
			else
				newAngle = lastAngle + Math.min(difAngle, maxTurnAngle);
		} else {
			difAngle = Math.abs(difAngle);
			if(difAngle < Math.PI)
				newAngle = lastAngle + Math.min(difAngle, maxTurnAngle);
			else
				newAngle = lastAngle - Math.min(difAngle, maxTurnAngle);
		}
		
		if(newAngle < MINUS_HALF_PI) newAngle += TWICE_PI;
		else if(newAngle > THREE_HALVES_PI) newAngle -= TWICE_PI;
		
		return newAngle;
	}
	
	public double sumAngles(double a, double b) {
		double newAngle = a + b;
		
		if(newAngle < MINUS_HALF_PI) newAngle += TWICE_PI;
		else if(newAngle > THREE_HALVES_PI) newAngle -= TWICE_PI;
		
		return newAngle;
	}
	
	public double cos(double angle) {
		double cos = Math.cos(angle);
		return (Math.abs(cos) < 1e-15 ? 0.0 : cos);
	}
	
	public double sin(double angle) {
		double sin = Math.sin(angle);
		return (Math.abs(sin) < 1e-15 ? 0.0 : sin);
	}
	
	public Rectangle getBounds() {
		return new Rectangle((int)x-1, (int)y-1, width-1, height-1);
	}

	public void paint(Graphics2D g2d) {
		
	}
}
