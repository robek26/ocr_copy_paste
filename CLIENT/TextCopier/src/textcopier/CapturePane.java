package textcopier;


import java.awt.AWTException;
import java.awt.Color;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Area;
import java.awt.image.BufferedImage;

import java.io.File;

import java.io.IOException;

import java.util.List;

import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.Timer;





/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author zion
 */
public class CapturePane extends JPanel {

        private Rectangle selectionBounds;
        private Point clickPoint;
        
        private int mx,my,w,h;
        
        public boolean WAIT = false;
        
       
        private boolean makeinv = false;
       

        public CapturePane() {
            this.setOpaque(false);
            

            MouseAdapter mouseHandler = new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    if (SwingUtilities.isLeftMouseButton(e) && e.getClickCount() == 2) {
                        System.exit(0);
                    }
                }

                @Override
                public void mousePressed(MouseEvent e) {
                    clickPoint = e.getPoint();
                    selectionBounds = null;
                }

                @Override
                public void mouseReleased(MouseEvent e) {
                    clickPoint = null;
                    // capture the screen
                    
                    screenCapture(mx, my, w, h);
                    
                    selectionBounds = getVirtualBounds();
                    makeinv = true;
                    repaint();
                    
                    
                    

                    
                }

                @Override
                public void mouseDragged(MouseEvent e) {
                    Point dragPoint = e.getPoint();
                    int x = Math.min(clickPoint.x, dragPoint.x);
                    int y = Math.min(clickPoint.y, dragPoint.y);
                    int width = Math.max(clickPoint.x - dragPoint.x, dragPoint.x - clickPoint.x);
                    int height = Math.max(clickPoint.y - dragPoint.y, dragPoint.y - clickPoint.y);
                    mx = x;
                    my = y;
                    w = width;
                    h = height;
                    selectionBounds = new Rectangle(x, y, width, height);
                    
                    repaint();
                }
            };

            addMouseListener(mouseHandler);
            addMouseMotionListener(mouseHandler);
        }
        
        private void sendDataToServer(){
          
                  
            Thread t = new Thread(() -> {
                  
  

                try {
                    String charset = "UTF-8";
                    File ff = new File("");
                    String newpath = ff.getAbsolutePath() + File.separator + "tmpimg/unk.png";
                    File uploadFile1 = new File(newpath);
                    
                    String requestURL = "http://localhost:24000";
                    
                    
                    MultipartUtility multipart = new MultipartUtility(requestURL, charset);
                    
                    multipart.addHeaderField("User-Agent", "mozilla");
                    multipart.addHeaderField("Test-Header", "Header-Value");
                   
                    
                    multipart.addFilePart("fileUpload", uploadFile1);
                    
                    List<String> response = multipart.finish();
                    
                    System.out.println("SERVER REPLIED:");
                    
                    String responsetext = "";
                    
                    for (String line : response) {
                        responsetext += line + "\n";
                    }
                    
                    final String lbl = "<html>Text Copied to clipboard!<br/>Paste to get text.</html>";
                        
                        // copy to clipboard
                        StringSelection selection = new StringSelection(responsetext);
                        Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
                        clipboard.setContents(selection, selection);
                        
                        
                        SwingUtilities.invokeLater(new Runnable() {
                            public void run() {
                                //UI code here
                                
                                
                                JPanel fr = (JPanel)TextCopier.stp.createTransparentWindow2(1,lbl);
                                
                                Timer t = new Timer(3000, (ActionEvent e) -> {
                                    TextCopier.stp.frame.setVisible(false);
                                    for(int i = 0 ; i < fr.getComponentCount();i++){
                                        fr.remove(i);
                                    }
                                    for(int i = 0 ; i < TextCopier.stp.frame.getContentPane().getComponentCount();i++){
                                        TextCopier.stp.frame.getContentPane().remove(i);
                                    }
                                    
                                    
                                    
                                    
                                });
                                t.setRepeats(false);
                                t.start();
                                
                                
                            }
                        });
                } catch (IOException ex) {
                    Logger.getLogger(CapturePane.class.getName()).log(Level.SEVERE, null, ex);
                }
                    
                    
               
            });
            t.start();
        }
        public  Rectangle getVirtualBounds() {
            Rectangle bounds = new Rectangle(0, 0, 0, 0);

            GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
            GraphicsDevice lstGDs[] = ge.getScreenDevices();
            for (GraphicsDevice gd : lstGDs) {
                bounds.add(gd.getDefaultConfiguration().getBounds());
            }
            return bounds;
        }
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2d = (Graphics2D) g.create();
            g2d.setColor(new Color(255, 255, 255, 150));

            Area fill = new Area(new Rectangle(new Point(0, 0), getSize()));
            if (selectionBounds != null) {
                fill.subtract(new Area(selectionBounds));
            }
            g2d.fill(fill);
            if (selectionBounds != null) {
                g2d.setColor(Color.BLACK);
                g2d.draw(selectionBounds);
            }
            g2d.dispose();
            if(makeinv){
                JFrame topFrame = (JFrame) SwingUtilities.getWindowAncestor(CapturePane.this);
                topFrame.setVisible(false);
                selectionBounds = null;
                repaint();
                makeinv = false;
            }
           
        }
        

        private void screenCapture(int x, int y, int width,int height){
            Robot robot;
            try {
                robot = new Robot();

                // The hard part is knowing WHERE to capture the screen shot from
                BufferedImage screenShot = robot.createScreenCapture(new Rectangle(x, y, width, height));
                
                try {
                    // Save your screen shot with its label
                    File ff = new File("");
                    String newpath = ff.getAbsolutePath() + File.separator + "tmpimg/unk.png";
                    ImageIO.write(screenShot, "png", new File(newpath));
       
                    
                    sendDataToServer();
                    
                } catch (IOException ex) {
                    Logger.getLogger(TextCopier.class.getName()).log(Level.SEVERE, null, ex);
                }

            } catch (AWTException ex) {
                Logger.getLogger(TextCopier.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }