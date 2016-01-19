import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.font.TextAttribute;
import java.text.AttributedCharacterIterator;
import java.text.AttributedString;
import java.util.HashMap;
import java.util.Map;

import javax.swing.ImageIcon;


public class UI {
	
	Game game;
	
	static final int ALLIG_X = Game.MAX_X + 15;
	static final int ALLIG_X2 = ALLIG_X + 35;
	static final int ALLIG_X3 = ALLIG_X + 60;
	
	static final int MAX_X = Game.WIDTH - 30;
	static final int MAX_Y = 400;
	
	static final int PAUSE_X = Game.CENTER_X - 20;
	static final int MESSAGE_X = Game.CENTER_X - 10;
	static final int CENTER_MESSAGE_Y = Game.CENTER_Y - 20;
	static final int INITAL_MESSAGE_Y = Game.MAX_Y + 60;
	static final double SPEED_MESSAGE = 1.5;
	static final int TIME_TO_CENTER_MESSAGE = (int)((INITAL_MESSAGE_Y-CENTER_MESSAGE_Y)/SPEED_MESSAGE);
	
	static final float PAUSE_OPACITY = 0.4f;
	
	static final int[][] Y_PLAYER = {
		{180,300},
		{200,320},
		{220,340},
		{240,360}
	};
	
	static final int OPTION_X = Game.CENTER_X - 85;
	static final int OPTION_Y = Game.CENTER_Y + 40;
	
	static final int EAGLE_LOST_X = Game.CENTER_X - 32;
	static final int EAGLE_LOST_Y = Game.CENTER_Y - 40;
	
	static final int ARRAY_SIZE = PlayerTeamManager.MIN_ARRAY_SIZE;
	static final int TIME_HIGHLIGHT = 40;
	
	static Map<AttributedCharacterIterator.Attribute,Object> attributesSmall;
	static Map<AttributedCharacterIterator.Attribute,Object> attributesMedium;
	
	static final Font smallFont = new Font("Consolas", Font.BOLD,16);
	static final Font mediumFont = new Font("Consolas", Font.BOLD,20);
	
	static final Color gray2 = new Color(100, 100, 100);
	static final Color danger = new Color(170, 20, 20);
	
	static final String REMAINING_ENEMIES = "Remaining Enemies";
	static final String REMAINING_ALLIES = "Remaining Allies";
	static final String LIVES = "Lives";
	static final String SCORES = "Scores";
	static final String TIME = "Time:";
	
	static final String OPTIONS = "R: Restart, Q: Quit";
	static final String EAGLE_LOST = "Eagle lost!";
	
	static AttributedString ATT_REMAINING_ENEMIES;
	static AttributedString ATT_REMAINING_ALLIES;
	static AttributedString ATT_SCORES;
	static AttributedString ATT_LIVES;
	static AttributedString ATT_TIME;
	
	static AttributedString ATT_OPTIONS;
	static AttributedString ATT_EAGLE_LOST;
	
	AttributedString enemies;
	AttributedString allies;
	AttributedString livesP4;
	AttributedString time;
	
	static String P1 = "P1:";
	static String P2 = "P2:";
	static String P3 = "P3:";
	static String P4 = "P4:";
	
	static AttributedString[] ATT_P = {
		new AttributedString(P1),
		new AttributedString(P2),
		new AttributedString(P3),
		new AttributedString(P4)
	};
	
	AttributedString[] scores;
	AttributedString[] lives;
	
	int[] timeHighlightScore;
	
	int[] lastScores;
	int[] lastLives;
	
	double messageY;
	
	Image imgGameOver = (new ImageIcon("src/gameover.png")).getImage().getScaledInstance(62, 32, Image.SCALE_DEFAULT);
	Image imgWellDone = (new ImageIcon("src/welldone.png")).getImage().getScaledInstance(62, 32, Image.SCALE_DEFAULT);
	Image imgPause = (new ImageIcon("src/pause.png")).getImage().getScaledInstance(78, 16, Image.SCALE_DEFAULT);
	
	public UI(Game g) {
		game = g;
		
		attributesSmall = new HashMap<AttributedCharacterIterator.Attribute, Object>();
		
		attributesSmall.put(TextAttribute.FONT, smallFont);
		attributesSmall.put(TextAttribute.FOREGROUND, Color.BLACK);
		
		attributesMedium = new HashMap<AttributedCharacterIterator.Attribute, Object>();
		
		attributesMedium.put(TextAttribute.FONT, mediumFont);
		attributesMedium.put(TextAttribute.FOREGROUND, Color.BLACK);
		
		ATT_REMAINING_ENEMIES = new AttributedString(REMAINING_ENEMIES, attributesSmall);
		ATT_REMAINING_ALLIES = new AttributedString(REMAINING_ALLIES, attributesSmall);
		ATT_SCORES = new AttributedString(SCORES, attributesSmall);
		ATT_LIVES = new AttributedString(LIVES, attributesSmall);
		ATT_TIME = new AttributedString(TIME, attributesMedium);
		
		ATT_OPTIONS = new AttributedString(OPTIONS, attributesMedium);
		ATT_OPTIONS.addAttribute(TextAttribute.FOREGROUND, Color.LIGHT_GRAY);
		ATT_EAGLE_LOST = new AttributedString(EAGLE_LOST, attributesSmall);
		ATT_EAGLE_LOST.addAttribute(TextAttribute.FOREGROUND, Color.ORANGE);
		
		scores = new AttributedString[ARRAY_SIZE];
		lives = new AttributedString[ARRAY_SIZE];
		
		lastScores = new int[ARRAY_SIZE];
		lastLives = new int[ARRAY_SIZE];
		
		for(int i = PlayerTank.MIN_PLAYER; i < ARRAY_SIZE; i++) {
			ATT_P[i].addAttributes(attributesMedium, 0, P1.length());
			lastScores[i] = lastLives[i] = -1;
		}
		
		timeHighlightScore = new int[ARRAY_SIZE];
		messageY = INITAL_MESSAGE_Y;
		
		update();
	}
	
	public void update() {
		enemies = new AttributedString(Integer.toString(game.enemyManager.remaining), attributesMedium);
		allies = new AttributedString(Integer.toString(game.allyManager.remaining), attributesMedium);
		
		if(game.enemyManager.remaining < 1) {
			ATT_REMAINING_ENEMIES.addAttribute(TextAttribute.FOREGROUND, gray2);
			enemies.addAttribute(TextAttribute.FOREGROUND, gray2);
		} else {
			ATT_REMAINING_ENEMIES.addAttribute(TextAttribute.FOREGROUND, Color.BLACK);
			enemies.addAttribute(TextAttribute.FOREGROUND, Color.BLACK);
		}
		
		if(game.allyManager.remaining < 1) {
			ATT_REMAINING_ALLIES.addAttribute(TextAttribute.FOREGROUND, gray2);
			allies.addAttribute(TextAttribute.FOREGROUND, gray2);
		} else {
			ATT_REMAINING_ALLIES.addAttribute(TextAttribute.FOREGROUND, Color.BLACK);
			allies.addAttribute(TextAttribute.FOREGROUND, Color.BLACK);
		}
		
		for(int i = PlayerTank.MIN_PLAYER; i <= PlayerTank.MAX_PLAYER; i++) {
			if(game.playerManager.players[i] != null) {
				PlayerTank pt = game.playerManager.players[i];
				if(lastLives[i] != pt.lives) {
					lastLives[i] = pt.lives;
					lives[i] = new AttributedString(Integer.toString(lastLives[i]), attributesMedium);
				}
					
				if(lastScores[i] != pt.score) {
					lastScores[i] = pt.score;
					timeHighlightScore[i] = TIME_HIGHLIGHT;
					scores[i] = new AttributedString(Integer.toString(lastScores[i]), attributesMedium);
				}
				
				if(timeHighlightScore[i] > 0) timeHighlightScore[i]--;
			}
		}
		
		time = new AttributedString(Integer.toString(game.timer), attributesMedium);
		if(game.isFlagOn(Game.SHOW_MESSAGE)) messageY = Math.max(messageY-SPEED_MESSAGE, CENTER_MESSAGE_Y);
	}
	
	public void paint(Graphics2D g) {
		try {
			g.drawString(ATT_REMAINING_ENEMIES.getIterator(), ALLIG_X, 60);
			g.drawString(enemies.getIterator(), ALLIG_X, 80);
			g.drawString(ATT_REMAINING_ALLIES.getIterator(), ALLIG_X, 100);
			g.drawString(allies.getIterator(), ALLIG_X, 120);
			
			g.drawString(ATT_SCORES.getIterator(), ALLIG_X, 160);
			g.drawString(ATT_LIVES.getIterator(), ALLIG_X, 280);
			
			// draw scores
			for(int i = PlayerTank.MIN_PLAYER; i <= PlayerTank.MAX_PLAYER; i++) {
				if(game.playerManager.players[i] != null) {
					if(timeHighlightScore[i] > 0) {
						ATT_P[i].addAttribute(TextAttribute.FOREGROUND, Color.WHITE);
						scores[i].addAttribute(TextAttribute.FOREGROUND, Color.WHITE);
					} else {
						ATT_P[i].addAttribute(TextAttribute.FOREGROUND, Color.BLACK);
						scores[i].addAttribute(TextAttribute.FOREGROUND, Color.BLACK);
					}
					g.drawString(ATT_P[i].getIterator(), ALLIG_X, Y_PLAYER[i][0]);
					g.drawString(scores[i].getIterator(), ALLIG_X2, Y_PLAYER[i][0]);
					if(timeHighlightScore[i] > 0) {
						ATT_P[i].addAttribute(TextAttribute.FOREGROUND, Color.BLACK);
					}
				}
			}
			
			for(int i = PlayerTank.MIN_PLAYER; i <= PlayerTank.MAX_PLAYER; i++) {
				if(game.playerManager.players[i] != null) {
					if(lastLives[i] < 1) {
						if(game.playerManager.players[i].isFlagOn(PlayerTank.ACTIVE)) {
							ATT_P[i].addAttribute(TextAttribute.FOREGROUND, danger);
							lives[i].addAttribute(TextAttribute.FOREGROUND, danger);
						} else {
							ATT_P[i].addAttribute(TextAttribute.FOREGROUND, gray2);
							lives[i].addAttribute(TextAttribute.FOREGROUND, gray2);
						}
					}
					g.drawString(ATT_P[i].getIterator(), ALLIG_X, Y_PLAYER[i][1]);
					g.drawString(lives[i].getIterator(), ALLIG_X2, Y_PLAYER[i][1]);
				}
			}
			
			g.drawString(ATT_TIME.getIterator(), ALLIG_X, 410);
			g.drawString(time.getIterator(), ALLIG_X3, 410);
			
			if(game.isFlagOn(Game.SHOW_OPTIONS))
				g.drawString(ATT_OPTIONS.getIterator(), OPTION_X, OPTION_Y);
			if(game.isFlagOn(Game.EAGLE_LOST))
				g.drawString(ATT_EAGLE_LOST.getIterator(), EAGLE_LOST_X, EAGLE_LOST_Y);
			
			if(game.isFlagOn(Game.GAME_OVER)) g.drawImage(imgGameOver, MESSAGE_X, (int)messageY, null);
			else if (game.isFlagOn(Game.WIN)) g.drawImage(imgWellDone, MESSAGE_X, (int)messageY, null);
			
			if(game.isFlagOn(Game.PAUSE)) {
				g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, PAUSE_OPACITY));
				g.setColor(Color.BLACK);
				g.fillRect(Game.MIN_X, Game.MIN_Y, Game.PLAYABLE_X, Game.PLAYABLE_Y);
				g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f));
				if(game.isFlagOn(Game.SHOW_PAUSE)) g.drawImage(imgPause, PAUSE_X, CENTER_MESSAGE_Y, null);
			}
			
		} catch(NullPointerException npe) {
			npe.printStackTrace();
		}
	}
}
