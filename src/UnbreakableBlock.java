import java.awt.Image;

import javax.swing.ImageIcon;


public class UnbreakableBlock extends Metal {
	
	static final Image IMG_UNBREAKABLE_BLOCK = (new ImageIcon("src/unbreakable_metal.png")).getImage().getScaledInstance(BLOCK_CHUNK, BLOCK_CHUNK, Image.SCALE_DEFAULT);

	public UnbreakableBlock(Game game, int x, int y) {
		super(game, x, y);
		imgMetal = IMG_UNBREAKABLE_BLOCK;
	}
	
	public int hitted(Projectile projectile, Unit owner) {
		return 1;
	}
	
	public void hitLetal(Projectile projectile, Unit owner) {
		hitted(projectile, owner);
	}
}
