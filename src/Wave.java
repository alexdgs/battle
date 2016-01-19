import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Stroke;
import java.awt.geom.Ellipse2D;
import java.util.ArrayList;


public class Wave extends Projectile implements Moveable {
	
	static final double INIT_DIAMETER = 10.0;
	static final double EXPANSION_SPEED = 5.0;
	static final int THICKNESS = 3;
	static final double X_DESPL = EXPANSION_SPEED/2.0;
	static final double Y_DESPL = EXPANSION_SPEED/2.0;
	static final int MAX_TIME = 450;
	
	double centerX;
	double centerY;
	int time = 0;
	double innerRadius;
	Ellipse2D.Double ellipse;
	ArrayList<Integer> hittedIDs;
	boolean hittable = true;
	static final Color color = new Color(128,128,60);
	
	public Wave(Game game, double x, double y, Unit owner) {
		super(game, x, y, (int)INIT_DIAMETER, (int)INIT_DIAMETER, owner);
		ellipse = new Ellipse2D.Double(x, y, INIT_DIAMETER, INIT_DIAMETER);
		centerX = owner.xTarget;
		centerY = owner.yTarget;
		innerRadius = Math.max(0, INIT_DIAMETER/2.0 - THICKNESS);
		hittedIDs = new ArrayList<Integer>();
	}
	
	public void move() {
		if(time == MAX_TIME) {
			game.objectsToRemove.add(this);
			return;
		} else {
			ellipse.x -= X_DESPL;
			ellipse.y -= Y_DESPL;
			ellipse.width += EXPANSION_SPEED;
			ellipse.height += EXPANSION_SPEED;
			innerRadius = Math.max(0, ellipse.width/2.0 - THICKNESS);
			time++;
		}
		for(TeamManager tm : game.teams[team].enemyTeams) {
			for(Tank p : tm.tanks) {
				if(p.isFlagOn(Tank.HITTABLE) && hit2(p) && dist(centerX, centerY, p.xTarget, p.yTarget) >= innerRadius && !hittedIDs.contains(p.id)) {
					p.hitted(null, owner);
					hittedIDs.add(p.id);
				}
			}
		}
		
	}
	
	public boolean hit2(GameObject go) {
		return ellipse.intersects(go.getBounds());
	}
	
	public void paint(Graphics2D g) {
		Stroke backup = g.getStroke();
		g.setStroke(new BasicStroke(THICKNESS));
		g.setColor(color);
		g.drawOval((int)ellipse.x, (int)ellipse.y, (int)ellipse.width, (int)ellipse.height);
		g.setStroke(backup);
	}
}
