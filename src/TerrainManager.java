import java.awt.Graphics2D;
import java.awt.Image;
import java.util.Arrays;

import javax.swing.ImageIcon;


public class TerrainManager {
	
	static final int GRID_SIZE = 20;
	
	static final int BRICK = 1;
	static final int METAL = 3;
	static final int WATER = 5;
	static final int EAGLE = 7;
	static final int SOFT_METAL = 9;
	static final int UNBREAKABLE_BLOCK = 11;
	
	static final int EMPTY = 0;
	static final int FOREST = 4;
	static final int ICE = 2;
	static final int DESTROYED_EAGLE = 6;
	//static final int WARP_PAD = 8;
	
	static int[][] terrain;
	static int[][] team;
	static GameObject[][] objects;
	int X;
	int Y;
	
	int waterPos = 0;
	boolean impassableBlocking = false;
	
	Game game;
	
	Image imgForest = (new ImageIcon("src/forest.png")).getImage().getScaledInstance(GRID_SIZE, GRID_SIZE, Image.SCALE_DEFAULT);
	Image[] imgWater = {(new ImageIcon("src/water1.png")).getImage().getScaledInstance(GRID_SIZE, GRID_SIZE, Image.SCALE_DEFAULT),
					(new ImageIcon("src/water2.png")).getImage().getScaledInstance(GRID_SIZE, GRID_SIZE, Image.SCALE_DEFAULT)};
	
	public TerrainManager(Game game) {
		this.game = game;
		X = Game.MAX_X/GRID_SIZE;
		Y = Game.MAX_Y/GRID_SIZE;
		terrain = new int[X][Y];
		team = new int[X][Y];
		objects = new GameObject[X][Y];
	}
	
	public void register(int x, int y, int type, GameObject go) {
		int cellX = x/GRID_SIZE;
		int cellY = y/GRID_SIZE;
		//System.out.println("Element on (" + cellX + "," + cellY + ") registered");
		if(terrain[cellX][cellY] != EMPTY) removeFromCell(cellX, cellY);
		terrain[cellX][cellY] = type;
		objects[cellX][cellY] = go;
	}
	
	public int valueOn(int x, int y) {
		return terrain[x/GRID_SIZE][y/GRID_SIZE];
	}
	
	public void remove(int x, int y) {
		int cellX = x/GRID_SIZE;
		int cellY = y/GRID_SIZE;
		removeFromCell(cellX, cellY);
	}
	
	public void removeFromCell(int cellX, int cellY) {
		terrain[cellX][cellY] = 0;
		if(objects[cellX][cellY] != null) {
			game.objectsToRemove.add(objects[cellX][cellY]);
			objects[cellX][cellY] = null;
		}
	}
	
	public void registerEagle(int x, int y) {
		int cellX = x/GRID_SIZE;
		int cellY = y/GRID_SIZE;
		//System.out.println("Brick on (" + cellX + "," + cellY + ") removed");
		terrain[cellX][cellY] = EAGLE;
		terrain[cellX+1][cellY] = EAGLE;
		terrain[cellX][cellY+1] = EAGLE;
		terrain[cellX+1][cellY+1] = EAGLE;
	}
	
	public void removeEagle(int x, int y) {
		int cellX = x/GRID_SIZE;
		int cellY = y/GRID_SIZE;
		terrain[cellX][cellY] = DESTROYED_EAGLE;
		terrain[cellX+1][cellY] = DESTROYED_EAGLE;
		terrain[cellX][cellY+1] = DESTROYED_EAGLE;
		terrain[cellX+1][cellY+1] = DESTROYED_EAGLE;
	}
	
	public double nextMove(double x, double y, int dir, double speed, int teamID) {
		if(dir == Game.LEFT) {
			double nextX = x - speed;
			if(nextX > Game.MIN_X) {
				int cellX = (int)(nextX / GRID_SIZE);
				int cellY1 = (int)(y/GRID_SIZE);
				int cellY2 = cellY1 + 1;
				if((terrain[cellX][cellY1] % 2 == 0) && (terrain[cellX][cellY2] % 2 == 0) && !blockingTank(cellX, cellY1, teamID) && !blockingTank(cellX, cellY2, teamID)) {
					return nextX;
				} else {
					setImpassableBlocking(terrain[cellX][cellY1], terrain[cellX][cellY2]);
					return (cellX+1)*GRID_SIZE;
				}
			} else return Game.MIN_X;
		} else if(dir == Game.RIGHT) {
			double nextX = x + speed;
			if(nextX < Tank.MAX_X) {
				int cellX = (int)(nextX / GRID_SIZE);
				int cellY1 = (int)(y/GRID_SIZE);
				int cellY2 = cellY1 + 1;
				if((terrain[cellX + 2][cellY1] % 2 == 0) && (terrain[cellX + 2][cellY2] % 2 == 0) && !blockingTank(cellX+2, cellY1, teamID) && !blockingTank(cellX+2, cellY2, teamID)) {
					return nextX;
				} else {
					setImpassableBlocking(terrain[cellX + 2][cellY1], terrain[cellX + 2][cellY2]);
					return cellX*GRID_SIZE;
				}
			} else return Tank.MAX_X;
		} else if(dir == Game.UP) {
			double nextY = y - speed;
			if(nextY > Game.MIN_Y) {
				int cellY = (int)(nextY / GRID_SIZE);
				int cellX1 = (int)(x/GRID_SIZE);
				int cellX2 = cellX1 + 1;
				if((terrain[cellX1][cellY] % 2 == 0) && (terrain[cellX2][cellY] % 2 == 0) && !blockingTank(cellX1, cellY, teamID) && !blockingTank(cellX2, cellY, teamID)) {
					return nextY;
				} else {
					setImpassableBlocking(terrain[cellX1][cellY], terrain[cellX2][cellY]);
					return (cellY+1)*GRID_SIZE;
				}
			} else return Game.MIN_Y;
		} 
		else if(dir == Game.DOWN) {
			double nextY = y + speed;
			if(nextY < Tank.MAX_Y) {
				int cellY = (int)(nextY / GRID_SIZE);
				int cellX1 = (int)(x/GRID_SIZE);
				int cellX2 = cellX1 + 1;
				if((terrain[cellX1][cellY + 2] % 2 == 0) && (terrain[cellX2][cellY + 2] % 2 == 0) && !blockingTank(cellX1, cellY + 2, teamID) && !blockingTank(cellX2, cellY + 2, teamID)) {
					return nextY;
				} else {
					setImpassableBlocking(terrain[cellX1][cellY + 2], terrain[cellX2][cellY + 2]);
					return cellY*GRID_SIZE;
				}
			} else return Tank.MAX_Y;
		}
		return 0;
	}
	
	public boolean blockingTank(int x, int y, int teamID) {
		return (team[x][y] != -1 && team[x][y] != teamID);
	}
	
	public void setImpassableBlocking(int v1, int v2) {
		if(v1 == METAL || v1 == UNBREAKABLE_BLOCK || v1 == EAGLE ||
				v2 == METAL || v2 == UNBREAKABLE_BLOCK || v2 == EAGLE) {
			impassableBlocking = true;
		} else impassableBlocking = false;
	}
	
	public void setTeamsOnTerrain() {
		team = new int[X][Y];
		for(int i = 0; i < X; i++) Arrays.fill(team[i], -1);
		for(TeamManager tm : game.teams) {
			for(Tank t : tm.tanks) {
				if(!t.isFlagOn(Tank.SPANNING) && !t.isFlagOn(Tank.REMOVING)) putTank(t);
			}
		}
	}
	
	public void putTank(Tank t) {
		int cellX = (int)(t.x / GRID_SIZE) + (t.x % GRID_SIZE > 0.0 ? 1 : 0);
		int cellY = (int)(t.y / GRID_SIZE) + (t.y % GRID_SIZE > 0.0 ? 1 : 0);
		team[cellX][cellY] = t.team;
		team[cellX+1][cellY] = t.team;
		team[cellX][cellY+1] = t.team;
		team[cellX+1][cellY+1] = t.team;
	}
	
	public void paintWater(Graphics2D g) {
		if(game.eventManager.remainingTimeForEvent[Game.EVENT_BLINK] == 0)
			waterPos = (waterPos+1)%4;
		int pos = waterPos/2;
		for(int i = 0; i < X; i++) {
			for(int j = 0; j < Y; j++) {
				if(terrain[i][j] == WATER)	
					g.drawImage(imgWater[pos], i*GRID_SIZE, j*GRID_SIZE, null);
			}
		}
	}
	
	public void paintForest(Graphics2D g) {
		for(int i = 0; i < X; i++) {
			for(int j = 0; j < Y; j++) {
				if(terrain[i][j] == FOREST)
					g.drawImage(imgForest, i*GRID_SIZE, j*GRID_SIZE, null);
			}
		}
	}
}
