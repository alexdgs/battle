import java.awt.Image;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import javax.swing.ImageIcon;


public class PlayerTeamManager extends TeamManager {
	
	static final int NUM_PLAYERS = 0; // 0 stands for 1 player, 1 for 2 players and so on. Max: 3 (4 players)
	static final int MIN_ARRAY_SIZE = PlayerTank.MAX_PLAYER + 1;
	
	PlayerTank[] players;
	boolean playingWalkSound;
	int walkingTanks = 0;
	
	static final Image[][][][] IMG_PLAYER_TANK = new Image[5][5][5][2];
	
	public PlayerTeamManager(Game game, int team) {
		super(game, team);
		
		for(int player = PlayerTank.PLAYER_ONE; player <= PlayerTank.PLAYER_FOUR; player++) {
			for(int level = PlayerTank.LEVEL_1; level <= PlayerTank.LEVEL_4; level++) {
				for(int dir = Game.RIGHT; dir <= Game.LEFT; dir++) {
					for(int step = 1; step <= 2; step++) {
						IMG_PLAYER_TANK[player][level][dir][step-1] = (new ImageIcon("src/P" + (player+1) + "_L"  + level + "_" + game.getCardinal(dir) + step + ".png")).getImage().getScaledInstance(Tank.WIDTH, Tank.HEIGHT, Image.SCALE_DEFAULT);
					}
				}
			}
		}
		playingWalkSound = false;
	}
	
	public void setUpPlayers() {
		KeyListener[] keyListeners = game.getListeners(KeyListener.class);
		if(keyListeners.length > 0) {
			game.removeKeyListener(keyListeners[0]);
		}
		
		players = new PlayerTank[MIN_ARRAY_SIZE];
		Slot[] slots = game.mapReader.getSlots(team);
		
		for(int i = PlayerTank.PLAYER_ONE; i <= NUM_PLAYERS; i++) {
			PlayerTank player = new PlayerTank(game, slots[i], i);
			game.objectsToInsert.add(player);
			tanks.add(player);
			players[i] = player;
		}
		
		game.addKeyListener(new KeyListener() {
			@Override
			public void keyTyped(KeyEvent e) {
			}
			
			@Override
			public void keyPressed(KeyEvent e) {
				if(e.getKeyCode() == KeyEvent.VK_P) {
					game.pause(!game.isFlagOn(Game.PAUSE));
				} else if(e.getKeyCode() == KeyEvent.VK_R) {
					game.setGame();
				} else if(e.getKeyCode() == KeyEvent.VK_F) {
					game.changeFullScreenMode();
				} else if(e.getKeyCode() == KeyEvent.VK_Q) {
					game.quit(); 
				} else {
					if(players[PlayerTank.PLAYER_ONE] != null) players[PlayerTank.PLAYER_ONE].keyPressed(e);
					if(players[PlayerTank.PLAYER_TWO] != null) players[PlayerTank.PLAYER_TWO].keyPressed(e);
					if(players[PlayerTank.PLAYER_THREE] != null) players[PlayerTank.PLAYER_THREE].keyPressed(e);
					if(players[PlayerTank.PLAYER_FOUR] != null) players[PlayerTank.PLAYER_FOUR].keyPressed(e);
				}
			}

			@Override
			public void keyReleased(KeyEvent e) {
				if(players[PlayerTank.PLAYER_ONE] != null) players[PlayerTank.PLAYER_ONE].keyReleased(e);
				if(players[PlayerTank.PLAYER_TWO] != null) players[PlayerTank.PLAYER_TWO].keyReleased(e);
				if(players[PlayerTank.PLAYER_THREE] != null) players[PlayerTank.PLAYER_THREE].keyReleased(e);
				if(players[PlayerTank.PLAYER_FOUR] != null) players[PlayerTank.PLAYER_FOUR].keyReleased(e);
			}
		});
	}
	
	/*@Override
	public void eventIn(int eventID) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void eventOut(int eventID) {
		// TODO Auto-generated method stub
		
	}*/
	
	@Override
	public void step() {
		boolean areActivePlayers = false;
		walkingTanks = 0;
		for(Unit t : tanks) {
			if(((PlayerTank)t).isFlagOn(PlayerTank.ACTIVE)) {
				areActivePlayers = true;
				break;
			}
		}
		if(areActivePlayers) {
			for(Tank t : tanks) {
				t.move();
				if(((PlayerTank)t).isFlagOn(Tank.WALKING)) walkingTanks++;
			}
		} else if(!game.isFlagOn(Game.WIN) && !game.isFlagOn(Game.GAME_OVER)) {
			game.endGame(Game.GAME_OVER);
		}
		
		if(walkingTanks > 0) {
			playingWalkSound = true;
			game.audioPlayer.loopSound(AudioPlayer.PLAYER_MOVE);
			
		} else {
			playingWalkSound = false;
			game.audioPlayer.stopSound(AudioPlayer.PLAYER_MOVE);
		}
	}
	
	public int liveAvailable() {
		PlayerTank max = null;
		int maxLives = 0;
		for(Unit t : tanks) {
			PlayerTank p = (PlayerTank) t;
			if(p.isFlagOn(PlayerTank.ACTIVE) && p.lives > 0 && p.lives > maxLives) {
				max = p;
				maxLives = p.lives;
			}
		}
		
		if(max != null) {
			max.lives--;
			return 1;
		} else return 0;
	}
	
	@Override
	public void healAll() {
		for(PlayerTank p : players) if(p != null) p.fullHealth();
	}
}
