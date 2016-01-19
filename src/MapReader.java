import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.StringTokenizer;


public class MapReader {
	
	static final int X = Game.MAX_X / TerrainManager.GRID_SIZE;
	static final int Y = Game.MAX_Y / TerrainManager.GRID_SIZE;
	Game game;
	
	char[][] map;
	ArrayList<WarpPad> warps;
	int eagleCounter = 0;
	ArrayList<Location> eagleLocations;
	Slot[] playerSlots;
	Slot[] allySlots;
	Slot[] enemySlots;
	
	boolean selectiveSpawningInfoAvailable = false;
	ArrayList<Integer>[] selectiveSpawnerAllyInfo;
	ArrayList<Integer>[] selectiveSpawnerEnemyInfo;
	
	public MapReader(Game game) {
		this.game = game;
		warps = new ArrayList<WarpPad>();
		eagleLocations = new ArrayList<Location>();
		playerSlots = new Slot[PlayerTeamManager.MIN_ARRAY_SIZE];
	}
	
	@SuppressWarnings("unchecked")
	public void readMapFromFile(String fileName) {
		map = new char[Y][X];
		try {
			ArrayList<Slot> allySlotsDiscovered = new ArrayList<Slot>();
			ArrayList<Slot> enemySlotsDiscovered = new ArrayList<Slot>();
			BufferedReader br = new BufferedReader(new FileReader(fileName));
			
			for(int j = 0; j < Y-1; j++) {
				String s = br.readLine();
				if(s != null && s.length() > 0) {
					for(int i = 0; i < X-1; i++) {
						if(i < s.length()) map[j][i] = s.charAt(i);
						else break;
					}
				} else break;
			}
			
			for(int j = 0; j < Y-1; j++) {
				for(int i = 0; i < X-1; i++) {
					int x = i * TerrainManager.GRID_SIZE + Game.MIN_X;
					int y = j * TerrainManager.GRID_SIZE + Game.MIN_Y;
					char c = map[j][i];
					switch(c) {
					case 'B':
						game.objectsToInsert.add(new Brick(game, x, y));
						break;
					case 'M':
						game.objectsToInsert.add(new Metal(game, x, y));
						break;
					case 'S':
						game.objectsToInsert.add(new SoftMetal(game, x, y));
						break;
					case 'W':
						game.terrainManager.register(x, y, TerrainManager.WATER, null);
						break;
					case 'F':
						game.terrainManager.register(x, y, TerrainManager.FOREST, null);
						break;
					case 'U':
						game.objectsToInsert.add(new UnbreakableBlock(game, x, y));
						break;
					case 'E':
						if(game.isFlagOn(Game.EAGLE)) {
							game.objectsToInsert.add(new Eagle(game, x, y, eagleCounter++));
							eagleLocations.add(new Location(x, y));
						}
						i++;
						break;
					case 'R':
						warps.add(new WarpPad(game, x, y));
						i++;
						break;
					case 'p':
						int playerId = map[j+1][i] - '0';
						//System.out.println("Found player " + playerId + " start location. Its default dir is " + map[j][i+1]);
						playerSlots[playerId - 1] = new Slot(x, y, charToDir(map[j][i+1]));
						i++;
						break;
					case 'a':
						allySlotsDiscovered.add(new Slot(x, y, charToDir(map[j][i+1])));
						i++;
						break;
					case 'e':
						enemySlotsDiscovered.add(new Slot(x, y, charToDir(map[j][i+1])));
						i++;
						break;
					}
				}
			}
			
			/* Read selective spawner info here */
			String s;
			while(((s = br.readLine()) != null) && !s.equals("")) {
				if(!selectiveSpawningInfoAvailable) {
					selectiveSpawnerAllyInfo = new ArrayList[eagleCounter];
					selectiveSpawnerEnemyInfo = new ArrayList[eagleCounter];
					for(int i = 0; i < eagleCounter; i++) {
						selectiveSpawnerAllyInfo[i] = new ArrayList<Integer>();
						selectiveSpawnerEnemyInfo[i] = new ArrayList<Integer>();
					}
					selectiveSpawningInfoAvailable = true;
				}
				
				int targetId = Integer.parseInt(s);
				
				StringTokenizer st = new StringTokenizer(br.readLine());
				while(st.hasMoreTokens()) {
					selectiveSpawnerAllyInfo[targetId].add(Integer.parseInt(st.nextToken()));
				}
				
				st = new StringTokenizer(br.readLine());
				while(st.hasMoreTokens()) {
					selectiveSpawnerEnemyInfo[targetId].add(Integer.parseInt(st.nextToken()));
				}
			}
			
			br.close();
			
			allySlots = new Slot[allySlotsDiscovered.size()];
			for(int i = 0; i < allySlotsDiscovered.size(); i++) {
				allySlots[i] = allySlotsDiscovered.get(i);
			}
			
			enemySlots = new Slot[enemySlotsDiscovered.size()];
			for(int i = 0; i < enemySlotsDiscovered.size(); i++) {
				enemySlots[i] = enemySlotsDiscovered.get(i);
			}
			
			if(!warps.isEmpty()) {
				warps.add(warps.get(0));
				for(int i = 0; i < warps.size()-1; i++) {
					warps.get(i).setDestiny(warps.get(i+1).x, warps.get(i+1).y);
				}
			}
			
		} catch (IOException ioe) {
			
		}	
	}
	
	public Slot[] getSlots(int team) {
		if(team == Unit.PLAYER_TEAM) return playerSlots;
		else if(team == Unit.ALLY_TEAM) return allySlots;
		else if(team == Unit.ENEMY_TEAM) return enemySlots;
		else return null;
	}
	
	public ArrayList<Integer>[] getSelectiveSpawnerInfo(int team) {
		if(team == Unit.ALLY_TEAM) return selectiveSpawnerAllyInfo;
		else if(team == Unit.ENEMY_TEAM) return selectiveSpawnerEnemyInfo;
		else return null;
	}
	
	public int charToDir(char c) {
		if(c == 'U') return Game.UP;
		if(c == 'D') return Game.DOWN;
		if(c == 'L') return Game.LEFT;
		if(c == 'R') return Game.RIGHT;
		return 0;
	}
}
