import java.awt.Color;
import java.awt.Graphics2D;


public class WeaponHoming extends HomingMissile{
	
	static final double SPEED = 0.4;
	
	public WeaponHoming(Game game, double x, double y, HomingMissile homing) {
		super(game,x,y,null,null);
		this.speed = SPEED;
	}
	
	public void paint(Graphics2D g) {
		g.setColor(Color.GREEN);
		g.fillRect((int)x, (int)y, WIDTH, HEIGHT);
	}
}
