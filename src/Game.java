import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.event.WindowEvent;
import java.awt.event.WindowFocusListener;
import java.util.ArrayList;
import java.util.ConcurrentModificationException;
import java.util.Deque;
import java.util.LinkedList;
import java.util.Queue;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JPanel;


@SuppressWarnings("serial")
public class Game extends JPanel implements EventDriven {
	
	static final int WIDTH = 1000;
	static final int HEIGHT = 720;
	
	static final int MIN_X = 20;
	static final int MIN_Y = 20;
	static final int MAX_X = WIDTH-180;
	static final int MAX_Y = HEIGHT-20;
	static final int PLAYABLE_X = MAX_X - MIN_X;
	static final int PLAYABLE_Y = MAX_Y - MIN_Y;
	static final int CENTER_X = (MAX_X-MIN_X)/2;
	static final int CENTER_Y = (MAX_Y-MIN_Y)/2;
	
	static final int X_POS = 0;
	static final int Y_POS = 1;
	static final int INIT_DIR = 2;
	
	static final int RIGHT = 1;
	static final int UP = 2; 
	static final int DOWN = 3;
	static final int LEFT = 4;
	
	static final int EAGLE_WIDTH = 40;
	static final int EAGLE_HEIGHT = 40;
	
	static final int TIME_TO_AUTOMATIC_QUIT = 400;
	static final int BLINK_CHANGE_TIME = 20;
	
	static final int TICKS_FOR_ONE_SECOND = 101;
	
	// Flags for Game
	static final int GAME_OVER = 0;
	static final int WIN = 1;
	static final int PAUSE = 2;
	static final int MAP = 3;
	static final int EAGLE = 4;
	static final int FULL_VISIBILITY = 5;
	static final int VISIBILITY_ENABLED = 6;
	static final int REMOVE_POW = 7;
	static final int SHOW_PAUSE = 8;
	static final int SHOW_MESSAGE = 9;
	static final int SHOW_OPTIONS = 10;
	static final int BLINK_STATE = 11;
	static final int GUARDIANS = 12;
	static final int SHOVEL = 13;
	static final int BLINKING_SHOVEL_STATUS = 14;
	static final int EAGLE_UNDER_ATTACK = 15;
	static final int EAGLE_LOST = 16;
	static final int EXTRA_SLOTS = 17;
	static final int FULL_SCREEN_AVAILABLE = 29;
	static final int FULL_SCREEN = 30;
	static final int DEBUG_MODE = 31;
	
	// Timers for Game
	static final int EVENT_AUTOMATIC_QUIT = 0;
	static final int EVENT_BLINK = 1;
	static final int EVENT_VISIBILITY = 2;
	static final int EVENT_SHOW_MESSAGE = 3;
	static final int EVENT_SHOVEL = 4;
	static final int EVENT_BLINK_SHOVEL = 5;
	static final int EVENT_EAGLE_UNDER_ATTACK = 6;
	static final int EVENT_EAGLE_LOST = 7;
	
	static final int SLOW_EFFECT_DIST = 6;
	
	MapReader mapReader;
	EventManager eventManager;
	TerrainManager terrainManager;
	AudioPlayer audioPlayer;
	PlayerTeamManager playerManager;
	AITeamManager allyManager;
	AITeamManager enemyManager;
	TeamManager[] teams = new TeamManager[3];
	PowerUp pow;
	ArrayList<Eagle> eagles;
	ArrayList<WarpPad> warps;
	ArrayList<EventDriven> timeObservers;
	
	//Minimap minimap;
	UI ui;
	int flags;
	
	Deque<GameObject> objects;
	Deque<Moveable> moveables;
	Deque<Hittable> hittables;
	
	Queue<GameObject> objectsToInsert;	// Stores objects to be inserted between game steps and avoid concurrency problems with main GameObjects array
	Queue<GameObject> objectsToRemove;
	
	boolean visibilityEnabled;
	
	int timeShovel;
	boolean brickedEagle;
	
	int powToSpan;
	int tick;
	int timer;
	
	Image imgPow[] = new Image[PowerUp.MAX_POWER_UP_ID+1];
	Image imgPowPicked = (new ImageIcon("src/powPicked.png")).getImage().getScaledInstance(PowerUp.WIDTH, PowerUp.HEIGHT, Image.SCALE_FAST);
	
	JFrame parentFrame;
	GraphicsDevice gdevice;
	
	public Game(JFrame f) {
		parentFrame = f;
		setFocusable(true);
		setBounds(0,0,WIDTH,HEIGHT);
		setBackground(Color.BLACK);
		setGame();
	}
	
	public void setGame() {
		checkFullScreenAvailability();
		
		eventManager = null;
		eventManager = new EventManager(this);
		terrainManager = new TerrainManager(this);
		if(audioPlayer == null) audioPlayer = new AudioPlayer(this);
		objects = new LinkedList<GameObject>();
		moveables = new LinkedList<Moveable>();
		hittables = new LinkedList<Hittable>();
		objectsToInsert = new LinkedList<GameObject>();
		objectsToRemove = new LinkedList<GameObject>();
		timeObservers = new ArrayList<EventDriven>();
		eagles = new ArrayList<Eagle>();
		warps = new ArrayList<WarpPad>();
		flags = 0;
		
		setFlagOn(MAP);
		setFlagOn(EAGLE);
		setFlagOn(SHOW_PAUSE);
		setFlagOn(GUARDIANS);
		setFlagOn(EXTRA_SLOTS);
		
		if(isFlagOn(MAP)) {
			mapReader = new MapReader(this);
			mapReader.readMapFromFile("src/map2.txt");
			this.warps = mapReader.warps;
		}
		
		playerManager = new PlayerTeamManager(this, Unit.PLAYER_TEAM);
		playerManager.setUpPlayers();
		allyManager = new AITeamManager(this, Unit.ALLY_TEAM, false);
		enemyManager = new AITeamManager(this, Unit.ENEMY_TEAM, true);
		
		playerManager.addAllyTeam(allyManager);
		
		playerManager.addEnemyTeam(enemyManager);
		allyManager.addEnemyTeam(enemyManager);
		enemyManager.addEnemyTeam(allyManager);
		if(mapReader != null && isFlagOn(EAGLE) && isFlagOn(EXTRA_SLOTS)) enemyManager.setExtraSlots(mapReader.eagleLocations);
		enemyManager.addEnemyTeam(playerManager);
		
		teams[Unit.PLAYER_TEAM] = playerManager;
		teams[Unit.ALLY_TEAM] = allyManager;
		teams[Unit.ENEMY_TEAM] = enemyManager;
		
		timeObservers.add(allyManager);
		timeObservers.add(enemyManager);
		
		for(int type = PowerUp.JOKER; type <= PowerUp.MAX_POWER_UP_ID; type++)
			imgPow[type] = (new ImageIcon("src/pow" + type + ".png")).getImage().getScaledInstance(PowerUp.WIDTH, PowerUp.HEIGHT, Image.SCALE_DEFAULT);
		
		eventManager.scheduleTimedEvent(EVENT_BLINK, BLINK_CHANGE_TIME);
		
		timeShovel = 0;
		tick = 1;
		timer = 0;
		brickedEagle = false;
		
		if(pow != null) pow = null;
		//putBricks();
		//putMetals();
		//putForest();
		//putWater();
		
		powToSpan = PowerUp.RANDOM;
		audioPlayer.playSound(AudioPlayer.GAME_START);
		spanPow();
		
	}
	
	public void setUpGraphicalComponents() {
		//minimap = new Minimap(this);
		ui = new UI(this);
	}
	
	public void eventIn(int eventID) {
		switch(eventID) {
		case EVENT_VISIBILITY:
			setFlagOn(FULL_VISIBILITY);
			break;
		case EVENT_SHOW_MESSAGE:
			setFlagOn(SHOW_MESSAGE);
			break;
		case EVENT_SHOVEL:
			for(Eagle e : eagles) coverEagle(e, TerrainManager.METAL);
			setFlagOn(SHOVEL);
			break;
		case EVENT_EAGLE_LOST:
			setFlagOn(EAGLE_LOST);
		}
	}
	
	public void eventOut(int eventID) {
		switch(eventID) {
		case EVENT_VISIBILITY:
			setFlagOff(FULL_VISIBILITY);
			break;
		case EVENT_SHOW_MESSAGE:
			setFlagOn(SHOW_OPTIONS);
			eventManager.scheduleTimedEvent(EVENT_AUTOMATIC_QUIT, TIME_TO_AUTOMATIC_QUIT);
			break;
		case EVENT_BLINK:
			toggleFlag(BLINK_STATE);
			if(isFlagOn(BLINK_STATE)) toggleFlag(SHOW_PAUSE);
			eventManager.scheduleTimedEvent(EVENT_BLINK, BLINK_CHANGE_TIME);
			break;
		case EVENT_SHOVEL:
			if(!isFlagOn(BLINKING_SHOVEL_STATUS)) {
				for(Eagle e : eagles) coverEagle(e, TerrainManager.SOFT_METAL);
			}
			setFlagOff(SHOVEL);
			break;
		case EVENT_BLINK_SHOVEL:
			if(isFlagOn(SHOVEL)) {
				toggleFlag(BLINKING_SHOVEL_STATUS);
				if(isFlagOn(BLINKING_SHOVEL_STATUS)) {
					for(Eagle e : eagles) coverEagle(e, TerrainManager.SOFT_METAL);
				}
				else for(Eagle e : eagles) coverEagle(e, TerrainManager.METAL);
				eventManager.scheduleTimedEvent(EVENT_BLINK_SHOVEL, PowerUp.SHOVEL_BLINK_TIME);
			}
			break;
		case EVENT_EAGLE_LOST:
			setFlagOff(EAGLE_LOST);
			break;
		case EVENT_AUTOMATIC_QUIT:
			quit();
		}
	}
	
	public void eventSecond(int second) { /* Not used in Game for now */ }
	
	/**
	 * Advance the current game one tick.
	 * */
	private void move() {
		eventManager.processEvents();
		if(!isFlagOn(PAUSE)) {
			if(objectsToInsert.size() > 0) insertObjects();
			
			if(isFlagOn(EAGLE)) {
				boolean eagleAlive = false;
				for(Eagle e : eagles) {
					e.step();
					if(!e.isDestroyed) eagleAlive = true;
				}
				if(!eagleAlive && !isFlagOn(GAME_OVER)) endGame(GAME_OVER);
			}
			
			terrainManager.setTeamsOnTerrain();
			for(TeamManager t : teams) t.step();
			
			if(moveables.size() > 0) for(Moveable m : moveables) m.move();
			
			if(objectsToRemove.size() > 0) removeObjects();
			
			for(WarpPad w : warps) w.step();
			
			if(isFlagOn(REMOVE_POW)) {
				pow = null;
				setFlagOff(REMOVE_POW);
			} else if(pow != null) pow.step();
			
			allyManager.setTarget();
			enemyManager.setTarget();
			
			tick++;
			if(tick == TICKS_FOR_ONE_SECOND && !(isFlagOn(GAME_OVER) || isFlagOn(WIN))) {
				timer++;
				tick = 0;
				for(EventDriven e : timeObservers) e.eventSecond(timer);
			}
		}
		
		if(enemyManager.tanks.size() < 1 || isFlagOn(GAME_OVER) || isFlagOn(PAUSE)) {
			audioPlayer.stopSound(AudioPlayer.ENEMY_MOVE);
			if(enemyManager.remaining <= 0 && enemyManager.tanks.size() < 1 && !isFlagOn(GAME_OVER) && !isFlagOn(WIN)) endGame(WIN);
		} else if(enemyManager.tanks.size() > 0 && !isFlagOn(GAME_OVER) && !isFlagOn(PAUSE) && !playerManager.playingWalkSound) {
			audioPlayer.loopSound(AudioPlayer.ENEMY_MOVE);
		}
		
		ui.update();
		//minimap.buildMap();
	}
	
	public void insertObjects() {
		synchronized(this) {
			while(objectsToInsert.size() > 0) {
				GameObject obj = objectsToInsert.poll();
				if(obj instanceof Hittable) {
					hittables.add((Hittable)obj);
					if(obj instanceof Brick)
						terrainManager.register((int)obj.x, (int)obj.y, TerrainManager.BRICK, obj);
					else if(obj instanceof SoftMetal)
						terrainManager.register((int)obj.x, (int)obj.y, TerrainManager.SOFT_METAL, obj);
					else if(obj instanceof Metal)
						terrainManager.register((int)obj.x, (int)obj.y, TerrainManager.METAL, obj);
					else if(obj instanceof Eagle) {
						Eagle e = (Eagle)obj;
						eagles.add(e);
						terrainManager.registerEagle((int)obj.x, (int)obj.y);
						coverEagle(e, TerrainManager.SOFT_METAL);
					}
				}
				if(obj instanceof Moveable) moveables.add((Moveable)obj);
				if(!(obj instanceof PlayerTank)) objects.add(obj);
			}
		}
	}
	
	public void removeObjects() {
		synchronized(this) {
			while(objectsToRemove.size() > 0) {
				GameObject obj = objectsToRemove.poll();
				if(obj instanceof Hittable) {
					hittables.remove((Hittable)obj);
					
					if(obj instanceof Bullet) {
						Bullet b = (Bullet)obj;
						if(b.owner != null && !b.stealed) 
							b.owner.removeBullet();
					}
					if(obj instanceof AITank) {
						AITank ai = (AITank)obj;
						teams[ai.team].tanks.remove(ai);
					}
				}
				if(obj instanceof Moveable) moveables.remove((Moveable)obj);
				if(!(obj instanceof PlayerTank)) objects.remove(obj);
			}
		}
	}
	
	public void spanPow() {
		pow = new PowerUp(this, powToSpan);
		audioPlayer.playSound(AudioPlayer.POW_SPAN);
	}
	
	public void scheduleRemovePow() {
		setFlagOn(REMOVE_POW);
	}
	
	public void activateShovel() {
		eventManager.scheduleTimedEvent(EVENT_SHOVEL, PowerUp.SHOVEL_TIME);
		eventManager.scheduleTimedEvent(EVENT_BLINK_SHOVEL, PowerUp.SHOVEL_TIME_TO_START_BLINKING);
	}
	
	public void putBricks() {
		putBrickArea(Game.MIN_X,Game.MIN_Y,Game.MAX_X-20,Game.MAX_Y-20);
	}
	
	public void putBrickArea(int x, int y, int w, int h) {
		/*for(int i = x; i <= w; i+=120) {
			for(int j = y; j <= h; j+=120) {
				objectsToInsert.add(new Brick(this,i,j));
				objectsToInsert.add(new Brick(this,i+20,j));
				objectsToInsert.add(new Brick(this,i+40,j));
				objectsToInsert.add(new Brick(this,i+60,j));
				
				objectsToInsert.add(new Brick(this,i,j+20));
				objectsToInsert.add(new Brick(this,i+20,j+20));
				objectsToInsert.add(new Brick(this,i+40,j+20));
				objectsToInsert.add(new Brick(this,i+60,j+20));
			}
		}*/
		
		for(int i = x; i <= w; i+=20) {
			for(int j = y; j <= h; j+=20) {
				objectsToInsert.add(new Brick(this,i,j));
			}
		}
	}
	
	public void putForest() {
		putForestArea(Game.MIN_X,Game.MIN_Y+120,Game.MAX_X-20,Game.MAX_Y-100);
	}
	
	public void putForestArea(int x, int y, int w, int h) {
		for(int i = x; i <= w-20; i+=120) {
			for(int j = y; j <= h-20; j+=120) {
				terrainManager.register(i, j, TerrainManager.FOREST, null);
				terrainManager.register(i+20, j, TerrainManager.FOREST, null);
				terrainManager.register(i, j+20, TerrainManager.FOREST, null);
				terrainManager.register(i+20, j+20, TerrainManager.FOREST, null);
			}
		}
	}
	
	public void putWater() {
		putWaterArea(Game.MIN_X,Game.MIN_Y+160,Game.MAX_X-20,Game.MAX_Y-180);
	}
	
	public void putWaterArea(int x, int y, int w, int h) {
		for(int i = x; i <= w; i+=20) {
			for(int j = y; j <= h; j+=20) {
				terrainManager.register(i, j, TerrainManager.WATER, null);
			}
		}
	}
	
	public void putMetals() {
		putMetalArea(Game.MIN_X,Game.MIN_Y+80,Game.MAX_X-20,Game.MAX_Y-60);
	}
	
	public void putMetalArea(int x, int y, int w, int h) {
		for(int i = x; i <= w; i+=240) {
			for(int j = y; j <= h; j+=200) {
				objectsToInsert.add(new Metal(this,i,j));
				objectsToInsert.add(new Metal(this,i+20,j));
				objectsToInsert.add(new Metal(this,i,j+20));
				objectsToInsert.add(new Metal(this,i+20,j+20));
			}
		}
	}
	
	public void coverEagle(Eagle e, int type) {
		if(!e.isFlagOn(Eagle.HITTABLE)) return;
		int x1 = (int) e.x-20;
		int y1 = (int) e.y-20;
		int x2 = (int) (e.x + e.width);
		int y2 = (int) (e.y + e.height);
		
		for(int x = x1; x <= x2; x+=20) {
			for(int y = y1; y<= y2; y += 20) {
				if(x >= MIN_X && x < MAX_X && y >= MIN_Y && y < MAX_Y && terrainManager.valueOn(x, y) != TerrainManager.EAGLE) {
					if(type == TerrainManager.SOFT_METAL) objectsToInsert.add(new SoftMetal(this, x, y, true));
					else if(type == TerrainManager.METAL) objectsToInsert.add(new Metal(this, x, y));
					else if(type == TerrainManager.BRICK) objectsToInsert.add(new Brick(this, x, y, true));
					if(isFlagOn(GUARDIANS) && !e.hasTurrets) {
						if((x == x1 || x == x2) && (y == y1 || y == y2)) {
							Turret t = new Turret(this, x-1, y, Unit.ALLY_TEAM);
							e.guardians.add(t);
							t.lastAngle = t.angleTo(Game.CENTER_X, Game.CENTER_Y);
						}
					}
				}
				
			}
		}
		e.hasTurrets = true;
	}
	
	public String getCardinal(int p) {
		if(p == LEFT) return "W";
		if(p == RIGHT) return "E";
		if(p == UP) return "N";
		if(p == DOWN) return "S";
		return null;
	}
	
	public void pause(boolean b) {
		if(!isFlagOn(GAME_OVER) && !isFlagOn(WIN)) {
			if(b && !isFlagOn(PAUSE)) {
				audioPlayer.suspendAll();
				audioPlayer.playSound(AudioPlayer.PAUSE);
				setFlagOn(PAUSE);
			} else if(!b && isFlagOn(PAUSE)) {
				audioPlayer.resumeAll();
				setFlagOff(PAUSE);
			}
		}
	}
	
	public void endGame(int result) {
		//System.out.println(result);
		if(isFlagOn(DEBUG_MODE)) return;
		setFlagOn(result);
		eventManager.scheduleTimedEvent(EVENT_SHOW_MESSAGE, UI.TIME_TO_CENTER_MESSAGE);
	}
	
	public void checkFullScreenAvailability() {
		GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
		gdevice = ge.getDefaultScreenDevice();
		if(gdevice.isFullScreenSupported()) setFlagOn(FULL_SCREEN_AVAILABLE);
	}
	
	public void changeFullScreenMode() {
		//if(isFlagOn(FULL_SCREEN_AVAILABLE)) {
			try {
				if(isFlagOn(FULL_SCREEN)) setFlagOff(FULL_SCREEN);
				else {
					gdevice.setFullScreenWindow(parentFrame);
					setFlagOn(FULL_SCREEN);
				}
			} catch(Exception e) {
				e.printStackTrace();
			} finally {
				if(!isFlagOn(FULL_SCREEN)) gdevice.setFullScreenWindow(null);
			}
		//} else System.out.println("Full screen not available");
	}
	
	public void quit() {
		System.exit(0);
	}
	
	public boolean isFlagOn(int flag) {
		return (flags & (1 << flag)) != 0;
	}
	
	public void setFlagOn(int flag) {
		flags  |= (1 << flag);
	}
	
	public void setFlagOff(int flag) {
		flags  &= ~(1 << flag);
	}
	
	public void toggleFlag(int flag) {
		flags ^= (1 << flag);
	}
	
	public static boolean randomBoolean(double p) {
		return (Math.random() <= p ? true : false);
	}
	
	@Override
	public void paint(Graphics g) {
		super.paint(g);
		Graphics2D g2d = (Graphics2D) g;
		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
				RenderingHints.VALUE_ANTIALIAS_ON);
		try {
			g2d.setColor(Color.GRAY);
			g2d.fillRect(0, 0, WIDTH, HEIGHT);
			g2d.setColor(Color.BLACK);
			g2d.fillRect(MIN_X, MIN_Y, MAX_X-20, MAX_Y-20);
			
			terrainManager.paintWater(g2d);
			for(WarpPad w : warps) w.paint(g2d);
			
			synchronized(this) {
				for(GameObject obj : objects) {
					obj.paint(g2d);
				}
			}
			
			for(Unit p : playerManager.tanks) p.paint(g2d);
			
			terrainManager.paintForest(g2d);
			for(Eagle e : eagles) for(Turret t : e.guardians) t.paint(g2d);
			if(pow != null) pow.paint(g2d);
			
			ui.paint(g2d);
			//minimap.paint(g2d);
			
		} catch(ConcurrentModificationException cme) {
			cme.printStackTrace();
			//System.out.println("Concurrent Problem: Repaint");
		}
	}
	
	public void drawSlowEffect(Graphics2D g, Image img, int x, int y, int dir) {
		if(img == null) return;
		int x2;
		int y2;
		int y3;
		int x3;
		if(dir == LEFT || dir == RIGHT) {
			y2 = y3 = y;
			if(dir == LEFT) {
				x2 = x + SLOW_EFFECT_DIST;
				x3 = x + SLOW_EFFECT_DIST*2;
			} else {
				x2 = x - SLOW_EFFECT_DIST;
				x3 = x - SLOW_EFFECT_DIST*2;
			}
		} else {
			x2 = x3 = x;
			if(dir == UP) {
				y2 = y + SLOW_EFFECT_DIST;
				y3 = y + SLOW_EFFECT_DIST*2;
			} else {
				y2 = y - SLOW_EFFECT_DIST;
				y3 = y - SLOW_EFFECT_DIST*2;
			}
		}
		
		if(x3 >= MIN_X && x3 <= MAX_X - img.getWidth(null) && y3 >= MIN_Y && y3 <= MAX_Y - img.getHeight(null)) {
			g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.4f));
			g.drawImage(img, (int)x3, (int)y3, null);
		}
		
		if(x2 >= MIN_X && x2 <= MAX_X - img.getWidth(null) && y2 >= MIN_Y && y2 <= MAX_Y - img.getHeight(null)) {
			g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.7f));
			g.drawImage(img, (int)x2, (int)y2, null);
		}
		
		g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f));
	}
	
	public static void main(String[] args) throws InterruptedException {
		JFrame f = new JFrame("My Game");
		Game game = new Game(f);
		game.setUpGraphicalComponents();
		
		f.add(game);
		
		f.addWindowFocusListener(new WindowFocusListener() {
			
			@Override
			public void windowLostFocus(WindowEvent e) {
				game.pause(true);
			}
			
			@Override
			public void windowGainedFocus(WindowEvent e) {
				// TODO Auto-generated method stub
				
			}
		});
		
		f.setSize(Game.WIDTH,Game.HEIGHT);
		f.setUndecorated(true);
		f.setVisible(true);
		f.setLocationRelativeTo(null);
		f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		while(true) {
			game.move();
			game.repaint();
			Thread.sleep(9);
		}
	}
}
