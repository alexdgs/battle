import java.awt.Graphics2D;
import java.awt.Image;

import javax.swing.ImageIcon;


public class Metal extends GameObject implements Hittable {
	
	int id;
	
	static final int BLOCK_CHUNK = 20;
	
	Image imgMetal = (new ImageIcon("src/metal.png")).getImage().getScaledInstance(BLOCK_CHUNK, BLOCK_CHUNK, Image.SCALE_DEFAULT);
	
	public Metal(Game game, int x, int y) {
		super(game, x, y, BLOCK_CHUNK, BLOCK_CHUNK);
		setFlagOn(HITTABLE);
	}
	
	public int hitted(Projectile projectile, Unit owner) {
		if(projectile != null && projectile.isFlagOn(Bullet.STRENGTH)) {
			game.terrainManager.remove((int)x, (int)y);
			return 0;
		} else return 1;
	}
	
	public void hitLetal(Projectile projectile, Unit owner) {
		hitted(projectile, owner);
	}
	
	public void littleHitted(Projectile projectile, Unit owner) {
		
	}
	
	@Override
	public void paint(Graphics2D g) {
		g.drawImage(imgMetal, (int)x, (int)y, null);
	}
}
