import java.awt.Graphics2D;
import java.awt.Image;

import javax.swing.ImageIcon;


public class Brick extends GameObject implements Hittable {
	
	int tile;
	
	int x2;
	int y2;
	
	static final int BLOCK_CHUNK = 10;
	
	Image[] imgBrick = {(new ImageIcon("src/brick1.png")).getImage().getScaledInstance(BLOCK_CHUNK, BLOCK_CHUNK, Image.SCALE_DEFAULT), 
			(new ImageIcon("src/brick2.png")).getImage().getScaledInstance(BLOCK_CHUNK, BLOCK_CHUNK, Image.SCALE_DEFAULT)
	};
	
	public Brick(Game game, int x, int y) {
		super(game,x,y,2*BLOCK_CHUNK,2*BLOCK_CHUNK);
		x2 = x + BLOCK_CHUNK;
		y2 = y + BLOCK_CHUNK;
		getTile();
		setFlagOn(HITTABLE);
	}
	
	public Brick(Game game, int x, int y, boolean b) {
		this(game,x,y);
		if(b) setFlagOn(ALLY_INMUNE);
	}

	@Override
	public synchronized int hitted(Projectile projectile, Unit owner) {
		double damage;
		double dir;
		if(projectile != null) {
			dir = projectile.dir;
			if(projectile.isFlagOn(Bullet.STRENGTH)) damage = BLOCK_CHUNK * 2;
			else damage = BLOCK_CHUNK;
		} else {
			dir = UP;
			damage = BLOCK_CHUNK;
		}
		
		if(dir == LEFT || dir == RIGHT) {
			width -= damage;
			if(width == 0) game.terrainManager.remove((int)x, (int)y);//game.objectsToRemove.add(this);
			else if(dir == RIGHT) x += damage;
		} else {
			height -= damage;
			if(height == 0) game.terrainManager.remove((int)x, (int)y);
			else if(dir == DOWN) y += damage;
		}
		//game.audioPlayer.playSound(AudioPlayer.BRICK_HIT);
		getTile();
		return 0;
	}
	
	public void hitLetal(Projectile projectile, Unit owner) {
		hitted(projectile, owner);
	}
	
	public void getTile() {
		if(x%20 == 0 && y%20 == 0) tile = 0;
		else tile = 1;
	}
	
	public void paint(Graphics2D g) {
		g.drawImage(imgBrick[tile], (int)x, (int)y, null);
		if(width > BLOCK_CHUNK) g.drawImage(imgBrick[(tile+1)%2], (int)x2, (int)y, null);
		if(height > BLOCK_CHUNK) {
			g.drawImage(imgBrick[(tile+1)%2], (int)x, (int)y2, null);
			if(width > BLOCK_CHUNK) g.drawImage(imgBrick[tile], (int)x2, (int)y2, null);
		}
		//g.draw
		//g.drawImage
		//g.setColor(Color.ORANGE);
		//g.fillRect((int)x, (int)y, width, height);
	}
}
