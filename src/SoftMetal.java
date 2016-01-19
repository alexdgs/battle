import java.awt.AlphaComposite;
import java.awt.Graphics2D;
import java.awt.Image;

import javax.swing.ImageIcon;


public class SoftMetal extends Metal {
	
	static final int DEFAULT_HIT_POINTS = 5;
	static final float DEFAULT_HIT_POINTS_FLOAT = (float)(DEFAULT_HIT_POINTS - 1);
	
	int hitPoints;
	float alpha;
	
	Image imgSoftMetal = (new ImageIcon("src/soft_metal.png")).getImage().getScaledInstance(BLOCK_CHUNK, BLOCK_CHUNK, Image.SCALE_DEFAULT);
	Image imgSoftMetalDamage = (new ImageIcon("src/soft_metal_damage.png")).getImage().getScaledInstance(BLOCK_CHUNK, BLOCK_CHUNK, Image.SCALE_DEFAULT);
	
	
	public SoftMetal(Game game, int x, int y) {
		super(game, x, y);
		hitPoints = DEFAULT_HIT_POINTS;
		setAlpha();
	}
	
	public SoftMetal(Game game, int x, int y, boolean b) {
		this(game, x, y);
		if(b) setFlagOn(ALLY_INMUNE);
	}
	
	public void setAlpha() {
		alpha = Math.min((DEFAULT_HIT_POINTS - hitPoints)/DEFAULT_HIT_POINTS_FLOAT, 1.0f);
	}
	
	@Override
	public int hitted(Projectile projectile, Unit owner) {
		if(projectile != null && projectile.isFlagOn(Bullet.STRENGTH)) hitPoints = Math.max(hitPoints-2, 0);
		else hitPoints--;
		if(hitPoints < 1) game.terrainManager.remove((int)x, (int)y);
		setAlpha();
		return 0;
	}
	
	@Override
	public void littleHitted(Projectile projectile, Unit owner) {
		hitted(projectile, owner);
	}
	
	@Override
	public void paint(Graphics2D g) {
		g.drawImage(imgSoftMetal, (int)x, (int)y, null);
		g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
		g.drawImage(imgSoftMetalDamage, (int)x, (int)y, null);
		g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f));
		//g.setColor(Color.LIGHT_GRAY);
		//g.fillRect((int)x, (int)y, width, height);
	}
}
