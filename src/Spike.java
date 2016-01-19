import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.ArrayList;

import javax.swing.JFrame;
import javax.swing.JPanel;


public class Spike extends JFrame {
	
	static final int W = 400;
	static final int H = 400;
	
	GraphicsDevice gd;
	boolean inFullScreen = false;
	
	public static void main(String[] args) throws InterruptedException {
		Spike f = new Spike();
		f.setUndecorated(true);
		f.setLocationRelativeTo(null);
		f.setSize(Spike.W, Spike.H);
		
		//f.setOpacity(0.4f);
		f.setVisible(true);
		
		f.addKeyListener(new KeyListener() {
			
			@Override
			public void keyTyped(KeyEvent e) {
				
			}
			
			@Override
			public void keyReleased(KeyEvent e) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void keyPressed(KeyEvent e) {
				//if(e.getKeyCode() == KeyEvent.VK_UP) f.setLocation(f.getX(), f.getY() - 1);
				//if(e.getKeyCode() == KeyEvent.VK_DOWN) f.setLocation(f.getX(), f.getY() + 1);
				//if(e.getKeyCode() == KeyEvent.VK_LEFT) f.setLocation(f.getX() - 1, f.getY());
				//if(e.getKeyCode() == KeyEvent.VK_RIGHT) f.setLocation(f.getX() + 1, f.getY());
				if(e.getKeyCode() == KeyEvent.VK_F) f.toggleFullScreenMode();
				if(e.getKeyCode() == KeyEvent.VK_Q) System.exit(0);
			}
		});
		
		while(true) {
			f.repaint();
			Thread.sleep(10);
		}
	}
	
	public Spike() {
		GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
		GraphicsDevice[] gds = ge.getScreenDevices();
		/*for(GraphicsDevice g : gds) {
			System.out.println(g.toString());
			System.out.println(g.isFullScreenSupported());
		}*/
		gd = ge.getDefaultScreenDevice();
		
		/*if(!gd.isWindowTranslucencySupported(GraphicsDevice.WindowTranslucency.TRANSLUCENT)) {
			System.err.println("Translucency not supported");
			System.exit(0);
		}*/
	}
	
	public void toggleFullScreenMode() {
		try {
			if(inFullScreen) inFullScreen = false;
			else {
				gd.setFullScreenWindow(this);
				inFullScreen = true;
			}
		} catch(Exception e) {
			e.printStackTrace();
		} finally {
			if(!inFullScreen) gd.setFullScreenWindow(null);
		}
	}
	
	public void paint(Graphics g) {
		super.paint(g);
		Graphics2D g2d = (Graphics2D)g;
		//g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.1f));
		g2d.setColor(Color.WHITE);
		g2d.fillRect(0, 0, W, H);
		g2d.setColor(Color.BLACK);
		g2d.drawString("HELLO WORLD!", 10, 10);
	}
}
