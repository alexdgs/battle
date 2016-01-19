import java.awt.Graphics2D;
import java.awt.Rectangle;


public interface Hittable {
	
	public int hitted(Projectile projectile, Unit owner);
	
	public void hitLetal(Projectile projectile, Unit owner);

	public Rectangle getBounds();

	public void paint(Graphics2D g);
	
}
