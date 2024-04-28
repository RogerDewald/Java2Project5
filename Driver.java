import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.HeadlessException;
import java.awt.Image;
import java.awt.Insets;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;

import javax.imageio.ImageIO;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.Timer;

import org.openstreetmap.gui.jmapviewer.Coordinate;
import org.openstreetmap.gui.jmapviewer.JMapViewer;
import org.openstreetmap.gui.jmapviewer.MapPolygonImpl;
import org.openstreetmap.gui.jmapviewer.OsmTileLoader;
import org.openstreetmap.gui.jmapviewer.tilesources.OsmTileSource;

public class Driver {
	
	// Declare class data
	private static JFrame mapFrame = null;
	private static GridBagConstraints layout = null;
	private static JPanel pickPanel = null;
	private static JCheckBox addStops = null;
	private static JButton playButton = null;
	private static JComboBox<String> animationTime = null;
	private static JMapViewer mapViewer;
	private static boolean isClicked = false;
	private static Integer time;
	private static boolean includeStops = false;
	private static ArrayList<TripPoint> tripPointList;

    public static void main(String[] args) throws FileNotFoundException, IOException, HeadlessException {

    	// Read file and call stop detection
    	
    	TripPoint.readFile("triplog.csv");
    	TripPoint.h2StopDetectionSimplified();
    	tripPointList = TripPoint.getMovingTrip();
    	
    	
    	// Set up frame, include your name in the title
    	
    	mapFrame = new JFrame("Map Window");
    	mapFrame.setTitle("Project 5 - Daniel Dewald");
    	mapFrame.setSize(1920,1080);
    	mapFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    	mapFrame.setLayout(new GridBagLayout());
    	layout = new GridBagConstraints();
        
        // Set up Panel for input selections
    	pickPanel = new JPanel();
    	
    	
        // Play Button
        playButton = new JButton("Play");
        
    	
        // CheckBox to enable/disable stops
        addStops = new JCheckBox("IncludeStops");
        
    	
        // ComboBox to pick animation time
        animationTime = new JComboBox<String>();
        animationTime.addItem("Animation Time");
        animationTime.addItem("15");
        animationTime.addItem("30");
        animationTime.addItem("60");
        animationTime.addItem("90");
        
        // Add all to top panel
        mapFrame.add(pickPanel, layout.gridx++);
        mapFrame.add(playButton, layout.gridx++);
        mapFrame.add(addStops, layout.gridx++);
        mapFrame.add(animationTime, layout.gridx++);
        
        // Set up mapViewer
        mapViewer = new JMapViewer();
        OsmTileLoader tileLoader = new OsmTileLoader(mapViewer);
        mapViewer.setTileLoader(tileLoader);
        mapViewer.setTileSource(new OsmTileSource.TransportMap());
        //JPanel mapPanel = new JPanel(new GridBagLayout());
        //mapPanel.add(mapViewer, layout);
        mapFrame.add(mapViewer, layout.gridy++);
        
        
        // Add listeners for GUI components
        playButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				
						try {
							animateTrip(time);
						} catch (IOException e1) {
							// TODO Auto-generated catch block
							e1.printStackTrace();
						}
					
			}
            
        });
        addStops.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				tripPointList = TripPoint.getMovingTrip();				
			}
            
        });
        animationTime.addActionListener(new ActionListener() {
        	
        	
			@Override
			public void actionPerformed(ActionEvent e) {
				
					time = Integer.valueOf((String)animationTime.getSelectedItem());
			}  
        });

        // Set the map center and zoom level
        Coordinate defaultCoord = new Coordinate(35.211, -110.4389);
        mapViewer.setDisplayPosition(defaultCoord, 5);
        mapViewer.repaint();
        mapViewer.setPreferredSize(new Dimension(900,900));
        mapFrame.pack();
        mapFrame.setVisible(true);

    

    
    // Animate the trip based on selections from the GUI components
    
}
    public static void animateTrip(Integer time) throws IOException {
        Image racoon = ImageIO.read(new File("raccoon.png"));
        int delay = 1000 * time / tripPointList.size();
        TripPoint startPoint = tripPointList.get(0);
    	Coordinate racPos = new Coordinate(startPoint.getLat(), startPoint.getLon());
    	IconMarker racIcon = new IconMarker(racPos, racoon);
        for (int i = 1; i < tripPointList.size(); i++) {
        	final int index = i;
        	mapViewer.addMapMarker(racIcon);
        	Timer timer = new Timer(delay * i, new ActionListener() {
        		@Override
        		public void actionPerformed(ActionEvent e) {
        			if (index >= tripPointList.size()) {
                        // Stop the timer when we've reached the end of the trip
                        ((Timer) e.getSource()).stop();
                        return;
                    }
        			racIcon.setLat(tripPointList.get(index).getLat());
        			racIcon.setLon(tripPointList.get(index).getLon());
        			mapViewer.repaint();
                    TripPoint previousPoint = tripPointList.get(index - 1);
                    Coordinate previousCoord = new Coordinate(previousPoint.getLat(), previousPoint.getLon());
                    ArrayList<Coordinate> redLinePos = new ArrayList<>();
                    redLinePos.add(previousCoord);
                    Coordinate nowCoord = new Coordinate(tripPointList.get(index).getLat(),tripPointList.get(index).getLon() );
                    redLinePos.add(nowCoord);
                    redLinePos.add(previousCoord); 
                    
                    MapPolygonImpl lineSegment = new MapPolygonImpl(redLinePos);
                    lineSegment.setColor(Color.RED);
                    mapViewer.addMapPolygon(lineSegment);
        		}
        	});
        	timer.setRepeats(false);
        	timer.start();
        	}
    }
}