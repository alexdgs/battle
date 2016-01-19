import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.geom.AffineTransform;

import javax.swing.ImageIcon;


public class MGTurret extends Turret {
	
	static final int DEFAULT_TIME_TO_FIRE = 10;
	static final int ADD_TIME_GAP_LONG = 40;
	static final int ADD_TIME_GAP_SHORT = 30;
	static final int NUM_BULLETS_TO_GAP = 5;
	
	static final Image imgMGTurret = (new ImageIcon("src/mg_turret.png")).getImage().getScaledInstance(WIDTH, HEIGHT, Image.SCALE_FAST);
	
	int bulletCounter = 0;
	int gapTime;
	
	public MGTurret(Unit owner) {
		super(owner);
		maxTimeToFire = DEFAULT_TIME_TO_FIRE;
		gapTime = ADD_TIME_GAP_LONG;
	}
	
	public MGTurret(Game game, double x, double y, int team) {
		super(game, x, y, team);
		maxTimeToFire = DEFAULT_TIME_TO_FIRE;
		gapTime = ADD_TIME_GAP_SHORT;
	}
	
	@Override
	public void fire() {
		//game.objectsToInsert.add(new ErraticHomingMissile(game, xTarget, yTarget, owner, (Tank)target));
		game.objectsToInsert.add(new Minibullet(game, xTarget - Minibullet.HALF_WIDTH, yTarget - Minibullet.HALF_HEIGHT, sumAngles(lastAngle, randomDev()), owner));
		bulletCounter++;
		if(bulletCounter == NUM_BULLETS_TO_GAP) {
			timeToFire -= gapTime;
			bulletCounter = 0;
		}
		game.audioPlayer.playSound(AudioPlayer.MG_FIRE);
	}
	
	@Override
	public void paint(Graphics2D g) {
		AffineTransform backup = g.getTransform();
		g.rotate(lastAngle, x + HALF_WIDTH, y + HALF_HEIGHT);
		g.drawImage(imgMGTurret, (int)x, (int)y, null);
		g.setTransform(backup);
	}
}
