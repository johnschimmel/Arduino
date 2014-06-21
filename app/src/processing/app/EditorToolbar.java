/* -*- mode: java; c-basic-offset: 2; indent-tabs-mode: nil -*- */

/*
  Part of the Processing project - http://processing.org

  Copyright (c) 2004-09 Ben Fry and Casey Reas
  Copyright (c) 2001-04 Massachusetts Institute of Technology

  This program is free software; you can redistribute it and/or modify
  it under the terms of the GNU General Public License as published by
  the Free Software Foundation; either version 2 of the License, or
  (at your option) any later version.

  This program is distributed in the hope that it will be useful,
  but WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  GNU General Public License for more details.

  You should have received a copy of the GNU General Public License
  along with this program; if not, write to the Free Software Foundation,
  Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
*/

package processing.app;
import static processing.app.I18n._;

import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.util.Arrays;

import javax.swing.*;
import javax.swing.event.*;
import javax.imageio.ImageIO;

/**
 * run/stop/etc buttons for the ide
 */
public class EditorToolbar extends JComponent implements MouseInputListener, KeyListener {

  /** Rollover titles for each button. */
  static final String title[] = {
    _("Verify"), _("Upload"), _("New"), _("Open"), _("Save"), _("Serial Monitor")
  };

  /** Titles for each button when the shift key is pressed. */ 
  static final String titleShift[] = {
    _("Verify"), _("Upload Using Programmer"), _("New Editor Window"), _("Open in Another Window"), _("Save"), _("Serial Monitor")
  };

  static final int BUTTON_COUNT  = title.length;
  /** Width of each toolbar button. */
  static final int BUTTON_WIDTH  = 27;
  /** Height of each toolbar button. */
  static final int BUTTON_HEIGHT = 32;
  /** The amount of space between groups of buttons on the toolbar. */
  static final int BUTTON_GAP    = 5;
  /** Size of the button image being chopped up. */
  static final int BUTTON_IMAGE_SIZE = 33;


  static final int RUN      = 0;
  static final int EXPORT   = 1;

  static final int NEW      = 2;
  static final int OPEN     = 3;
  static final int SAVE     = 4;

  static final int SERIAL   = 5;

  static final int INACTIVE = 0;
  static final int ROLLOVER = 1;
  static final int ACTIVE   = 2;

  Editor editor;

  Image offscreen;
  int width, height;

  Color bgcolor;

  static Image[][] buttonImages = new Image[BUTTON_COUNT][3];
  int currentRollover;

  JPopupMenu popup;
  JMenu menu;

  int buttonCount;
  int[] state = new int[BUTTON_COUNT];
  JButton[] stateButtons = new JButton[BUTTON_COUNT];
  Image[] stateImage;
  int which[]; // mapping indices to implementation

  int x1[], x2[];
  int y1, y2;

  Font statusFont;
  Color statusColor;

  boolean shiftPressed;

  public EditorToolbar(Editor editor, JMenu menu) {

    this.editor = editor;
    this.menu = menu;
    
    buttonCount = 0;
    which = new int[BUTTON_COUNT];

    //which[buttonCount++] = NOTHING;
    which[buttonCount++] = RUN;
    which[buttonCount++] = EXPORT;
    which[buttonCount++] = NEW;
    which[buttonCount++] = OPEN;
    which[buttonCount++] = SAVE;
    which[buttonCount++] = SERIAL;

    currentRollover = -1;

    bgcolor = Theme.getColor("buttons.bgcolor");
    statusFont = Theme.getFont("buttons.status.font");
    statusColor = Theme.getColor("buttons.status.color");

    addMouseListener(this);
    addMouseMotionListener(this);
  }

  
  public JPanel prepareToolbarButtons() {
 
	Image allButtons = Base.getThemeImage("buttons.gif", this);
	 
	final JPanel toolbarButtonPanel = new JPanel();
	toolbarButtonPanel.setBackground(Color.RED);
	toolbarButtonPanel.setLayout(new GridLayout(1, BUTTON_COUNT,5,1));
	 
	for(int i=0; i < BUTTON_COUNT; i++){
		
		JButton tmpButton = new JButton();
		tmpButton.getAccessibleContext().setAccessibleName(title[i]);
		tmpButton.getAccessibleContext().setAccessibleDescription(titleShift[i]);
		tmpButton.setContentAreaFilled(false);
		tmpButton.setBorderPainted(false);
		tmpButton.setFocusPainted(false);
		tmpButton.setPreferredSize(new Dimension(BUTTON_WIDTH, BUTTON_HEIGHT));
		
		// default icon
		BufferedImage bufferedImage1 = new BufferedImage(BUTTON_WIDTH, BUTTON_HEIGHT, BufferedImage.TYPE_INT_RGB);
		bufferedImage1.getGraphics().drawImage(allButtons, -(i*BUTTON_IMAGE_SIZE) - 3,  (-2+INACTIVE)*BUTTON_IMAGE_SIZE, null);
		ImageIcon startIcon = new ImageIcon(bufferedImage1, titleShift[i]);
		tmpButton.setIcon(startIcon);

		// rollover icon
		BufferedImage bufferedImage2 = new BufferedImage(BUTTON_WIDTH, BUTTON_HEIGHT, BufferedImage.TYPE_INT_RGB);
		bufferedImage2.getGraphics().drawImage(allButtons, -(i*BUTTON_IMAGE_SIZE) - 3,  (-2+ROLLOVER)*BUTTON_IMAGE_SIZE, null);
		ImageIcon rollOverIcon = new ImageIcon(bufferedImage2, titleShift[i]);
		tmpButton.setRolloverIcon(rollOverIcon);
		
		// button actionListener
		tmpButton.addMouseListener(new MouseAdapter() {
            
			@Override
            public void mousePressed(MouseEvent e) {
                super.mousePressed(e);
	        	JButton actionBtn = (JButton) e.getSource();
	        	System.out.println(actionBtn.getAccessibleContext().getAccessibleName());
	        	
	        	int actionIndexNum = (int) Arrays.asList(title).indexOf(actionBtn.getAccessibleContext().getAccessibleName());
     	
	        	if (!isEnabled())
	      	      return;

	        	final int x = e.getX();
	        	final int y = e.getY();
	      	  
	        	switch (actionIndexNum) {
	            case RUN:
	              editor.handleRun(false);
	              break;

	            case OPEN:
	              popup = menu.getPopupMenu();
	              popup.setVisible(true);
	              popup.show(toolbarButtonPanel, x+actionBtn.getX(),y);
	              
	              break;

	            case NEW:
	              if (shiftPressed) {
	                editor.base.handleNew();
	              } else {
	            	editor.base.handleNewReplace();
	              }
	              break;

	            case SAVE:
	              editor.handleSave(false);
	              break;

	            case EXPORT:
	              editor.handleExport(e.isShiftDown());
	              break;

	            case SERIAL:
	              editor.handleSerial();
	              break;
	            }
	        }
	    });
		toolbarButtonPanel.add(tmpButton);
	}
	
	return toolbarButtonPanel;
	
  }
  


  public void mouseMoved(MouseEvent e) { }


  public void mouseDragged(MouseEvent e) { }


  public void handleMouse(MouseEvent e) {}


  private void setState(int slot, int newState, boolean updateAfter) {}


  public void mouseEntered(MouseEvent e) { }


  public void mouseExited(MouseEvent e) {
	  if ((popup != null) && popup.isVisible()) return;
  }

  int wasDown = -1;


  public void mouseClicked(MouseEvent e) { }


  public void mouseReleased(MouseEvent e) { }


  /**
   * Set a particular button to be active.
   */
  public void activate(int what) {}


  /**
   * Set a particular button to be active.
   */
  public void deactivate(int what) { }


  public Dimension getPreferredSize() {
    return getMinimumSize();
  }


  public Dimension getMinimumSize() {
    return new Dimension((BUTTON_COUNT + 1)*BUTTON_WIDTH, BUTTON_HEIGHT);
  }


  public Dimension getMaximumSize() {
    return new Dimension(3000, BUTTON_HEIGHT);
  }


  public void keyPressed(KeyEvent e) {
    if (e.getKeyCode() == KeyEvent.VK_SHIFT) {
      shiftPressed = true;
      repaint();
}
  }


  public void keyReleased(KeyEvent e) {
    if (e.getKeyCode() == KeyEvent.VK_SHIFT) {
      shiftPressed = false;
      repaint();
    }
  }


  public void keyTyped(KeyEvent e) { }
}
