// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.kindahackedinutils;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.AWTEvent;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.event.AWTEventListener;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Area;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.SwingUtilities;

import org.openstreetmap.josm.actions.JosmAction;
import org.openstreetmap.josm.command.AddCommand;
import org.openstreetmap.josm.command.ChangeNodesCommand;
import org.openstreetmap.josm.command.ChangePropertyCommand;
import org.openstreetmap.josm.command.Command;
import org.openstreetmap.josm.command.RemoveNodesCommand;
import org.openstreetmap.josm.command.SequenceCommand;
import org.openstreetmap.josm.data.UndoRedoHandler;
import org.openstreetmap.josm.data.coor.EastNorth;
import org.openstreetmap.josm.data.coor.LatLon;
import org.openstreetmap.josm.data.osm.BBox;
import org.openstreetmap.josm.data.osm.DataSet;
import org.openstreetmap.josm.data.osm.Node;
import org.openstreetmap.josm.data.osm.OsmDataManager;
import org.openstreetmap.josm.data.osm.OsmPrimitive;
import org.openstreetmap.josm.data.osm.Relation;
import org.openstreetmap.josm.data.osm.TagMap;
import org.openstreetmap.josm.data.osm.Way;
import org.openstreetmap.josm.data.osm.event.AbstractDatasetChangedEvent;
import org.openstreetmap.josm.data.osm.event.DataChangedEvent;
import org.openstreetmap.josm.data.osm.event.DataSetListener;
import org.openstreetmap.josm.data.osm.event.NodeMovedEvent;
import org.openstreetmap.josm.data.osm.event.PrimitivesAddedEvent;
import org.openstreetmap.josm.data.osm.event.PrimitivesRemovedEvent;
import org.openstreetmap.josm.data.osm.event.RelationMembersChangedEvent;
import org.openstreetmap.josm.data.osm.event.TagsChangedEvent;
import org.openstreetmap.josm.data.osm.event.WayNodesChangedEvent;
import org.openstreetmap.josm.gui.MainApplication;
import org.openstreetmap.josm.gui.MainMenu;
import org.openstreetmap.josm.gui.MapFrame;
import org.openstreetmap.josm.gui.layer.MainLayerManager.ActiveLayerChangeEvent;
import org.openstreetmap.josm.gui.layer.MainLayerManager.ActiveLayerChangeListener;
import org.openstreetmap.josm.gui.preferences.PreferenceSetting;
import org.openstreetmap.josm.gui.util.KeyPressReleaseListener;
import org.openstreetmap.josm.plugins.Plugin;
import org.openstreetmap.josm.plugins.PluginInformation;
import org.openstreetmap.josm.spi.preferences.Config;
import org.openstreetmap.josm.tools.Geometry;
import org.openstreetmap.josm.tools.ImageProvider;
import org.openstreetmap.josm.tools.ImageProvider.ImageSizes;
import org.openstreetmap.josm.tools.Shortcut;
import org.openstreetmap.josm.tools.Utils;

/**
 * Collection of utilities
 */
public class KindaHackedInUtilsPlugin extends Plugin {
  private static final String ACTION_NAME = "SHIFT+ALT+H_ALTERNATIVE";
  
  private static KindaHackedInUtilsPlugin instance;
  private final ImageIcon arrow;
  private final DataSetListener listener;
  private final AngleAction angleAction;
  private final Shortcut angleDegreeShortcut;
  private final Shortcut drawNodeShortcut;
  private final Shortcut splitWayShortcut;

  public KindaHackedInUtilsPlugin(PluginInformation info) {
    super(info);
    instance = this;
    drawNodeShortcut = Shortcut.registerShortcut("kindahackedinutils.drawNodeAtMouse", tr("Draw node at mouse location"), KeyEvent.VK_B, Shortcut.DIRECT);
    splitWayShortcut = Shortcut.registerShortcut("kindahackedinutils.splitWay", tr("Split way at mouse location"), KeyEvent.VK_K, Shortcut.DIRECT);
    angleDegreeShortcut = Shortcut.registerShortcut("kindahackedinutils.angleDegree", tr("Get heading in degrees"), KeyEvent.VK_H, Shortcut.ALT_SHIFT);
    
    angleAction = new AngleAction();
    DetachAction detachAction = new DetachAction();
    arrow = ImageProvider.get("N", ImageSizes.POPUPMENU);
    listener = new DataSetListener() {
      Thread waitForEventEnd;
      TagsChangedEvent lastEvent;
      TagsChangedEvent previouseEvent;
      
      @Override
      public void tagsChanged(TagsChangedEvent event) {
        if(event.getPrimitives().size() == 1) {
          lastEvent = event;
          
          if(waitForEventEnd == null || !waitForEventEnd.isAlive()) {
            waitForEventEnd = new Thread() {
              public void run() {
                try {
                  Thread.sleep(50);
                } catch (InterruptedException e) {
                  // TODO Auto-generated catch block
                  e.printStackTrace();
                }
                
                while(!Objects.equals(lastEvent, previouseEvent)) {
                  try {
                    Thread.sleep(100);
                  } catch (InterruptedException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                  }
                  previouseEvent = lastEvent;
                }

                TagsChangedEvent use = lastEvent;
                
                lastEvent = null;
                previouseEvent = null;

                SwingUtilities.invokeLater(() -> changeDirectionForTrafficSign(use.getPrimitives(), false, null, Conf.isObjectSpecificDirection()));
              };
            };
            waitForEventEnd.start();
          }
        }
      }
      
      @Override
      public void relationMembersChanged(RelationMembersChangedEvent event) {}
      
      @Override
      public void primitivesRemoved(PrimitivesRemovedEvent event) {}
      
      @Override
      public void primitivesAdded(PrimitivesAddedEvent event) {}
      
      @Override
      public void otherDatasetChange(AbstractDatasetChangedEvent event) {}
      
      @Override
      public void nodeMoved(NodeMovedEvent event) {}
      
      @Override
      public void dataChanged(DataChangedEvent event) {}              
      
      @Override
      public void wayNodesChanged(WayNodesChangedEvent event) {}
    };
    
    JMenu toolsMenu = MainApplication.getMenu().moreToolsMenu;
    MainMenu.add(toolsMenu, detachAction);
  }
  
  @Override
  public PreferenceSetting getPreferenceSetting() {
    return new KindaHackedInUtilsPreferences();
  }
  
  private static String getDirectionFromHeading(int test) {
    String angle = String.valueOf(test);
    
    if(test == 0 || test == 360) {
      angle = "N";
    }
    else if(test == 22) {
      angle = "NNE";
    }
    else if(test == 45) {
      angle = "NE";
    }
    else if(test == 90) {
      angle = "E";
    }
    else if(test == 112) {
      angle = "ESE";
    }
    else if(test == 135) {
      angle = "SE";
    }
    else if(test == 157) {
      angle = "SSE";
    }
    else if(test == 180) {
      angle = "S";
    }
    else if(test == 202) {
      angle = "SSW";
    }
    else if(test == 225) {
      angle = "SW";
    }
    else if(test == 247) {
      angle = "WSW";
    }
    else if(test == 270) {
      angle = "W";
    }
    else if(test == 292) {
      angle = "WNW";
    }
    else if(test == 315) {
      angle = "NW";
    }
    else if(test == 337) {
      angle = "NNW";
    }
    
    return angle;
  }
  
  @Override
  public void mapFrameInitialized(MapFrame oldFrame, final MapFrame newFrame) {
    if(oldFrame != null) {
      oldFrame.getActionMap().remove("kindahackedinutils.addHeading");
    }
    if(newFrame != null) {
      newFrame.getActionMap().put("kindahackedinutils.addHeading", angleAction);
      
      MainApplication.getLayerManager().addActiveLayerChangeListener(new ActiveLayerChangeListener() {
        @Override
        public void activeOrEditLayerChanged(ActiveLayerChangeEvent e) {
          if(e.getPreviousDataSet() != null) {
            e.getPreviousDataSet().removeDataSetListener(listener);
          }
          
          if(MainApplication.getLayerManager().getEditDataSet() != null) {
            MainApplication.getLayerManager().getEditDataSet().addDataSetListener(listener);
          }
        }
      });
      
      Toolkit.getDefaultToolkit().addAWTEventListener(new AWTEventListener() {
        public void eventDispatched(AWTEvent event) {
          if(event instanceof MouseEvent) {
            MouseEvent evt = (MouseEvent)event;
            
            if(Conf.isToFront() && evt.getID() == MouseEvent.MOUSE_ENTERED){
              if(evt.getSource() == newFrame.mapView && !MainApplication.getMainFrame().isActive()) {
                MainApplication.getMainFrame().toFront();
                MainApplication.getMainFrame().setAlwaysOnTop(true);
                MainApplication.getMainFrame().setAlwaysOnTop(false);
              }
              else if(evt.getSource() instanceof JComponent) {
                Window parent = SwingUtilities.windowForComponent((JComponent)evt.getSource());
                
                if(parent != null && parent instanceof JDialog && ((JDialog)parent).isModal() && !((JDialog)parent).isActive()) {
                  ((JDialog)parent).toFront();
                  ((JDialog)parent).setAlwaysOnTop(true);
                  ((JDialog)parent).setAlwaysOnTop(false);
                }
              }
            }
          }
        }
      }, AWTEvent.MOUSE_EVENT_MASK);
     
      MainApplication.getMap().keyDetector.addKeyListener(new KeyPressReleaseListener() {
        @Override
        public void doKeyReleased(KeyEvent e) {}
        
        @Override
        public void doKeyPressed(KeyEvent e) {
          if(angleDegreeShortcut.isEvent(e)) {
            angleAction.actionPerformed(new ActionEvent(MainApplication.getMap().mapView, 0, ACTION_NAME));
          }
          else if(drawNodeShortcut.isEvent(e)) {
            if(Conf.isCreateNode()) {
              MainApplication.getMenu().unselectAll.actionPerformed(null);
  
              Point b = MouseInfo.getPointerInfo().getLocation();
              SwingUtilities.convertPointFromScreen(b, MainApplication.getMap());
              
              MainApplication.getMap().mapModeDraw.mouseReleased(new MouseEvent(MainApplication.getMap(), 0, System.currentTimeMillis(), 0, b.x, b.y, 1, false, MouseEvent.BUTTON1));
              MainApplication.getMap().mapModeDraw.mouseReleased(new MouseEvent(MainApplication.getMap(), 0, System.currentTimeMillis(), 0, b.x, b.y, 2, false, MouseEvent.BUTTON1));
            }
          }
          else if(splitWayShortcut.isEvent(e)) {
            if(Conf.isSplitWay()) {
              MainApplication.getMenu().unselectAll.actionPerformed(null);
  
              Point b = MouseInfo.getPointerInfo().getLocation();
              SwingUtilities.convertPointFromScreen(b, MainApplication.getMap());
  
              MainApplication.getMap().mapModeSplit.mousePressed(new MouseEvent(MainApplication.getMap(), 0, System.currentTimeMillis(), 0, b.x, b.y, 1, false, MouseEvent.BUTTON1));
            }
          }
        }
      });
  	}
  }
  
  private synchronized boolean changeDirectionForTrafficSign(List<? extends OsmPrimitive> list, boolean ignoreExistingValue, Way wayPointedAt, final boolean objectSpecificDirection) {
    if(list.size() == 1 && list.get(0) instanceof Node) {
      final Node n = (Node)list.get(0);
      
      if(n.hasKey("traffic_sign") && ((ignoreExistingValue && wayPointedAt != null) || !n.hasKey("direction") || Objects.equals(n.get("direction"),"forward") || Objects.equals(n.get("direction"),"backward"))) {
        Way[] ways = n.referrers(Way.class).toArray(Way[]::new);
        
        LinkedList<Way> highways = new LinkedList<>();
        
        for(Way o : ways) {
          if(o.hasKey("highway") && (o.isFirstLastNode(n))) {
            highways.add(o);
          }
        }
        
        JPopupMenu menu = new JPopupMenu();
        
        if(!highways.isEmpty()) {
          MouseAdapter ma = new MouseAdapter() {
            public void mouseEntered(MouseEvent e) {
              if(e.getComponent() instanceof WayMenuItem) {
                ((WayMenuItem)e.getComponent()).way.setHighlighted(true);
              }
            };
            
            public void mouseExited(MouseEvent e) {
              if(e.getComponent() instanceof WayMenuItem) {
                ((WayMenuItem)e.getComponent()).way.setHighlighted(false);
              }
            };
          };
          
          ActionListener a = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
              if(e.getSource() instanceof WayMenuItem) {
                final HashSet<Command> cmds = new HashSet<>(2);
                
                cmds.add(new ChangePropertyCommand(n, "direction", ((WayMenuItem)e.getSource()).getText()));
                cmds.add(new ChangePropertyCommand(n, "traffic_sign:direction", null));
                
                UndoRedoHandler.getInstance().add(new SequenceCommand("Change direction value", cmds));
                
                ((WayMenuItem)e.getSource()).way.setHighlighted(false);
              }
            }
          };
          
          boolean singleWay =  (highways.size() == 1);
          boolean sameDirection = false;
          
          String lastNodePos = null;
          String direction = n.hasKey("direction") ? n.get("direction") : n.hasKey("traffic_sign:direction") ? n.get("traffic_sgin:direction") : null;
          
          for(Way w : highways) {
            int d = -1;
            String nodePos = null;
            
            if(n == w.getNode(0)) {
              d = (int)Utils.toDegrees(w.getNode(1).getEastNorth().heading(n.getEastNorth()));
              nodePos = "firstNode";
            }
            else if(n == w.getNode(w.getNodesCount()-1)) {
              d = (int)Utils.toDegrees(w.getNode(w.getNodesCount()-2).getEastNorth().heading(n.getEastNorth()));
              nodePos = "lastNode";
            }
            
            if(!Objects.equals(lastNodePos, nodePos)) {
              sameDirection = true;
            }
            
            lastNodePos = nodePos;
            
            WayMenuItem item = new WayMenuItem(w, d, arrow, nodePos);
            item.addActionListener(a);
            item.addMouseListener(ma);
            
            if(Objects.equals(wayPointedAt, w) || singleWay) {
              a.actionPerformed(new ActionEvent(item, 0, null));
              return true;
            }
            
            menu.add(item);
          }
          System.out.println(sameDirection + " "+ highways.size());
          if(highways.size() == 2 && sameDirection && (Objects.equals(direction, "forward") || Objects.equals(direction, "backward"))) {
            for(int i = 0; i < 2; i++) {
              WayMenuItem item = (WayMenuItem)menu.getComponent(i);
              System.out.println(" " + direction + " "+ item.nodePos);    
              if((Objects.equals(direction, "forward") && Objects.equals(item.nodePos, "firstNode")) ||
                  (Objects.equals(direction, "backward") && Objects.equals(item.nodePos, "lastNode"))) {
                item.getActionListeners()[0].actionPerformed(new ActionEvent(item, 0, null));
                return true;
              }
            }
          }
          
          if(ignoreExistingValue) {
            return false;
          }
        }
        else if(!ignoreExistingValue && ways.length == 1 && ((objectSpecificDirection && !n.hasKey("traffic_sign:direction")) || !objectSpecificDirection && !n.hasKey("direction"))) {
          ActionListener a = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
              if(e.getSource() instanceof JMenuItem) {
                final HashSet<Command> cmds = new HashSet<>(2);
                
                if(objectSpecificDirection) {
                  cmds.add(new ChangePropertyCommand(n, "traffic_sign:direction", ((JMenuItem)e.getSource()).getName()));
                  cmds.add(new ChangePropertyCommand(n, "direction", null));
                }
                else {
                  cmds.add(new ChangePropertyCommand(n, "traffic_sign:direction", null));
                  cmds.add(new ChangePropertyCommand(n, "direction", ((JMenuItem)e.getSource()).getName()));
                }
                
                UndoRedoHandler.getInstance().add(new SequenceCommand("Change direction value", cmds));

              }
            }
          };
          
          JMenuItem forward = new JMenuItem("↑ " + tr("forward"));
          forward.setName("forward");
          forward.addActionListener(a);
          menu.add(forward);
          
          JMenuItem backward = new JMenuItem("↓ " + tr("backward"));
          backward.setName("backward");
          backward.addActionListener(a);
          menu.add(backward);
        }
        
        if(menu.getComponentCount() > 0) {
          Point p = MainApplication.getMap().mapView.getPoint(n.getEastNorth());
          
          menu.show(MainApplication.getMap().mapView, p.x, p.y);
          return true;
        }
      }
    }
    
    return false;
  }
  
  public static final KindaHackedInUtilsPlugin getInstance() {
    return instance;
  }
  
  class DetachAction extends JosmAction {
    public DetachAction() {
      super(tr("Detach nodes from ways and move them"), /* ICON() */ "preferences/detach", tr("Detach nodes from ways and move them"),
          Shortcut.registerShortcut("kindahackedinutils.detach", tr("Detatch nodes"), KeyEvent.VK_Z, Shortcut.DIRECT), false);
    }
    
    @Override
    public void actionPerformed(ActionEvent e) {
      final DataSet ds = MainApplication.getLayerManager().getEditDataSet();
      
      Collection<Way> selectedWays = ds.getSelectedWays();
      
      LinkedList<Command> cmds = new LinkedList<>();
      Hashtable<Node,LinkedList<Node>> replacement = new Hashtable<>();
      Hashtable<Way,LinkedList<NodePair>> newNodesTable = new Hashtable<>();
      
      for(Way w : selectedWays) {
        if(w.isClosed()) {
          LinkedList<Command> cmds1 = new LinkedList<>();
          
          LinkedList<Node> moveNodes1 = new LinkedList<>();
          LinkedList<Node> allNodes1 = new LinkedList<>();
          
          HashSet<Node> removeNodes1 = new HashSet<>();
          
          WayExt wayExt = new WayExt(w);
          
          for(int i = 0; i < w.getNodesCount()-1; i++) {
            final Node n = w.getNode(i);
            final AtomicBoolean nodeHandled = new AtomicBoolean();
            
            n.referrers(Way.class).forEach(way -> {
              if(w != way && !nodeHandled.get()) {
                removeNodes1.add(n);
                
                if((!way.isClosed() || way.hasKey("building"))) {
                  allNodes1.add(new Node(n.getEastNorth().add(wayExt.getAddValueForNode(n))));
                  moveNodes1.add(allNodes1.getLast());
                  cmds1.add(new AddCommand(ds, allNodes1.getLast()));
                  
                  nodeHandled.set(true);
                }
              }
            });
            
            if(!nodeHandled.get()) {
              allNodes1.add(n);
              moveNodes1.add(new Node(n.getEastNorth()));
            }
          }
          
          if(!cmds1.isEmpty()) {
            allNodes1.add(allNodes1.getFirst());
            
            cmds1.add(0, new RemoveNodesCommand(w, removeNodes1));
            cmds1.add(new ChangeNodesCommand(w, allNodes1));
            
            UndoRedoHandler.getInstance().add(new SequenceCommand("detach and scale nodes", cmds1));
          }
        }
        else {
          for(int i = 0; i < w.getNodesCount(); i++) {
            final Node cur = w.getNode(i);
            
            Iterator<Way> it = cur.referrers(Way.class).iterator();
            
            while(it.hasNext()) {
              Way w2 = it.next();
              
              if(w != w2) {
                WayExt way = new WayExt(w2); 
                
                boolean isMultipolygon = way.way.referrers(Relation.class).anyMatch(Relation::isMultipolygon);
                
                if(way.isClosed() || isMultipolygon) {
                  LinkedList<NodePair> nodePairs = newNodesTable.get(way.way);
                  
                  if(nodePairs == null) {
                    nodePairs = new LinkedList<>();
                    newNodesTable.put(way.way, nodePairs);
                  }
                  
                  LinkedList<Node> nodeList = replacement.get(cur);
                  
                  if(nodeList == null) {
                    nodeList = new LinkedList<Node>();
                    replacement.put(cur, nodeList);
                  }
                  
                  Node newNode = new Node(cur.getEastNorth().add(way.getAddValueForNode(cur)));
                  boolean found = false;
                  
                  for(Node node : nodeList) {
                    if(newNode.getEastNorth().distance(node.getEastNorth()) <= 1) {
                      found = true;
                      nodePairs.add(new NodePair(cur, node));
                      break;
                    }
                  }
                  
                  if(!found) {
                    cmds.add(new AddCommand(ds, newNode));
                    nodeList.add(newNode);
                    nodePairs.add(new NodePair(cur, newNode));
                  }
                }
              }
            }
          }
        }
      }

      if(!replacement.isEmpty()) {
        for(Way way : newNodesTable.keySet()) {
          LinkedList<Node> newNodes = new LinkedList<Node>();
          HashSet<Node> toRemove = new HashSet<Node>();
          LinkedList<NodePair> list = newNodesTable.get(way);
          
          for(int i = 0; i < way.getNodesCount(); i++) {
            final Node node = way.getNode(i);
            
            if(list != null) {
              Optional<NodePair> pair = list.stream().filter(n -> n.containsOldNode(node)).findFirst();
              
              if(pair.isPresent()) {
                toRemove.add(node);
                newNodes.add(pair.get().newNode);
              }
              else {
                newNodes.add(node);
              }
            } 
            else {
              newNodes.add(node);
            }
          }
          
          if(!toRemove.isEmpty()) {
            cmds.add(0, new RemoveNodesCommand(way, toRemove));
          }
          
          if(!newNodes.isEmpty()) {
            cmds.add(new ChangeNodesCommand(way, newNodes));
          }
        }
      }
      
      if(!cmds.isEmpty()) {
        UndoRedoHandler.getInstance().add(new SequenceCommand("detach and move nodes", cmds));                      
      }
    }
    
    @Override
    protected void updateEnabledState() {
        updateEnabledStateOnCurrentSelection();
    }
    
    @Override
    protected void updateEnabledState(Collection<? extends OsmPrimitive> selection) {
        if (selection == null) {
            setEnabled(false);
            return;
        }
        setEnabled(Conf.isDetachEnabled() && checkSelection(selection));
    }
    
    private boolean checkSelection(Collection<? extends OsmPrimitive> selection) {
      boolean result = false;
      
      for (OsmPrimitive p : selection) {
        if (p instanceof Way) {
            result = true;
            break;
        }
      }
      
      return result;
    }
  }
  
  class AngleAction extends JosmAction {
    public AngleAction() {
      super(tr("Get heading for direction from mouse location"), /* ICON() */ "statusline/heading.svg", tr("Get heading for direction from mouse location"),
          Shortcut.registerShortcut("kindahackedinutils.angle", tr("Get heading"), KeyEvent.VK_H,
                  Shortcut.DIRECT), false);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
      if(Conf.isDirectionEnabled()) {
        Collection<Node> nodes = OsmDataManager.getInstance().getActiveDataSet().getSelectedNodes();
        
        if(nodes.size() == 1) {
          try {
            final boolean objectSpecificDirection = Conf.isObjectSpecificDirection();
            
            nodes.forEach(n -> {
              Point p = MouseInfo.getPointerInfo().getLocation();
              SwingUtilities.convertPointFromScreen(p, MainApplication.getMap().mapView);
              
              Way pointedTo = MainApplication.getMap().mapView.getNearestWay(p, OsmPrimitive::isSelectable);
              
              if(pointedTo != null && !pointedTo.isHighlighted()) {
                pointedTo = null;
              }
              
              if(!changeDirectionForTrafficSign(Collections.singletonList(n), true, pointedTo, objectSpecificDirection)) {
                int test = (int)Math.round(Utils.toDegrees(n.getEastNorth().heading(MainApplication.getMap().mapView.getEastNorth(p.x, p.y))));
                int a = test;
                
                if(Conf.isNaturalDirection() && n.hasKey("traffic_sign")) {
                  a += 180;
                  
                  if(a >= 360) {
                    a -= 360;
                  }
                }
                
                AtomicReference<String> angle = new AtomicReference<String>(getDirectionFromHeading(a));
                
                String key = "direction";
                
                String simpleDirection = null;
                  Way[] ways = n.referrers(Way.class).toArray(Way[]::new);
                  
                  int wayIndex = 0;
                  
                  if((ways.length == 2 && Objects.equals(n.get("highway"), "traffic_signals"))) {
                    for(int i = 0; i < ways.length; i++) {
                      if(n == ways[i].getNode(0)) {
                        wayIndex = i;
                        break;
                      }
                    }
                  }
                  
                  if(ways.length == 1 || (ways.length == 2 && Objects.equals(n.get("highway"), "traffic_signals"))) {
                    Way way = (Way)ways[wayIndex];
                    
                      Node prev = null;
                      Node next = null;
                      Node last = null;
                      
                      for(int i = 0; i < way.getNodesCount(); i++) {
                        if(way.getNode(i).equals(n)) {
                          prev = last;
                          
                          if(i+1 < way.getNodesCount()) {
                            next = way.getNode(i+1);
                          }
                          
                          break;
                        }
                        
                        last = way.getNode(i);
                      }
                      
                      if(pointedTo != null && pointedTo.containsNode(n) && next != null && prev != null) {
                        LatLon mousePoint = MainApplication.getMap().mapView.getLatLon(p.x, p.y);
                        BBox prevBox = n.getBBox();
                        prevBox.add(prev.getBBox());

                        if(prevBox.contains(mousePoint)) {
                          next = null;
                        }
                      }
                      
                      if(next == null) {
                        next = n;
                      }
                      else {
                        prev = n;
                      }
                      
                      if(prev != null) {
                        simpleDirection = getSimpleDirection(Utils.toDegrees(prev.getEastNorth().heading(next.getEastNorth())), test);
                      }
                  }
                  
                  if(simpleDirection != null) {
                    if(n.hasKey("traffic_sign") && (objectSpecificDirection || 
                        (Conf.isSimpleDirection() &&
                            !Objects.equals(ACTION_NAME, e.getActionCommand())))) {
                      if(objectSpecificDirection) {
                        key = "traffic_sign:direction";
                      }
                      
                      angle.set(simpleDirection);
                    }
                    else if(n.hasTag("highway", "traffic_signals") ) {
                      key = "traffic_signals:direction";
                      angle.set(simpleDirection);
                    }
                  }
                  else if(n.hasKey("traffic_sign") && Config.getPref().getBoolean("kindahackedinutils.angleInfoNotShown", true)) {
                    JOptionPane.showMessageDialog(MainApplication.getMainFrame(), tr("Note that the direction for a traffic sign is opposite of the direction it's effect is.\nFor example if a street's direction is in 45° forward and a speed limit sign is placed besides this street in forward direction it is mapped with a direction of 225.\nIf natural direction is enabled for traffic signs you can just point the mouse behind the traffic sign in the direction of the street to get the correct mapping."));
                    Config.getPref().putBoolean("kindahackedinutils.angleInfoNotShown", false);
                  }
                
                  UndoRedoHandler.getInstance().add(new ChangePropertyCommand(n, key, angle.get())); 
                }
            });
          }catch(NumberFormatException nfe) {
            nfe.printStackTrace();
          }
        }
      }
    }
  }
  
  private String getSimpleDirection(double wayHeading, double mouseHeading) {
    double lB = wayHeading - 90;
    double uB = wayHeading + 90;
    
    if(lB < 0) {
      lB += 360;
    }
    if(uB >= 360) {
      uB -= 360;
    }
    
    if(mouseHeading >= lB && mouseHeading <= uB || (lB > uB && (mouseHeading < uB || mouseHeading > lB))) {
      return "forward";
    }
    else {
      return "backward";
    }
  }
  
  private static final class NodePair {
    public Node oldNode;
    public Node newNode;
    
    public NodePair(Node oldNode, Node newNode) {
      this.oldNode = oldNode;
      this.newNode = newNode;
    }
    
    boolean containsOldNode(Node node) {
      return oldNode == node;
    }
  }
  
  private static final class WayExt {
    public Way way;
    public LinkedList<EastNorth> addList;
    
    public WayExt(Way way) {
      this.way = way;
      addList = new LinkedList<>();
      calculateAngles();
    }
    
    public EastNorth getAddValueForNode(Node n) {
      int index = getIndexForNode(n);
      
      if(index >= 0 && index < addList.size()) {
        return addList.get(index);
      }
      
      return new EastNorth(-5, -5);
    }

    private boolean matchingRelations(Relation[] one, Relation[] two) {
      for(Relation o : one) {
        for(Relation t : two) {
          if(o.equals(t)) {
            return true;
          }
        }
      }
      
      return false;
    }
    
    private Node findPreviouseNodeForIndex(int index) {
      if(index == 0) {
        if(way.isClosed()) {
          return way.getNode(way.getNodesCount()-2);
        }
        else {
          return findNodeForIndexFromMatchingWay(index);
        }
      }
      else if(index > 0 && index <= way.getNodesCount()) {
        return way.getNode(index - 1);
      }
      
      return null;
    }
    
    private Node findNodeForIndexFromMatchingWay(int index) {
      Node me = way.getNode(index);
      TagMap meTags = way.getKeys();
      
      Way[] ways = me.referrers(Way.class).toArray(Way[]::new);
      Relation[] multis = way.referrers(Relation.class).filter(Relation::isMultipolygon).toArray(Relation[]::new);
      System.out.println("WAYS LENGTH " + ways.length + " MULTIS LENGTH " + multis.length);
      for(Way w : ways) {
        boolean inSameMultipoligone = matchingRelations(multis, w.referrers(Relation.class).filter(Relation::isMultipolygon).toArray(Relation[]::new));
        System.out.println("  inSameMultipoligone " + inSameMultipoligone);  
        if(w != way) {
          Node foundNode = null;
          
          System.out.println(" ME NODE " + me + "\n LAST NODE " + w.getNode(w.getNodesCount()-1)+"\n FIRST NODE " + w.getNode(0));
          
          if(w.getNode(w.getNodesCount()-1) == me) {
            foundNode = w.getNode(w.getNodesCount()-2);
          }
          else if(w.getNode(0) == me) {
            foundNode = w.getNode(1);
          }
          System.out.println(" FOUND NODE " + foundNode);
          if(foundNode != null) {
            if(inSameMultipoligone) {
              return foundNode;
            }
            else {
              TagMap tags = w.getKeys();
              
              if(meTags.size() == tags.size()) {
                final AtomicBoolean mismatch = new AtomicBoolean(false);
                
                meTags.forEach((meKey, meValue) -> {
                  if(tags.get(meKey) == null || !tags.get(meKey).equals(meValue)) {
                    mismatch.set(true);
                  }
                });
                
                if(!mismatch.get()) {
                  return foundNode;
                }
              }
            }
          }
        }
      }
      
      return null;
    }
    
    private Node findNextNodeForIndex(int index) {
      if(index == way.getNodesCount() -1) {
        if(way.isClosed()) {
          return way.getNode(1);
        }
        else {
          return findNodeForIndexFromMatchingWay(index);
        }
      }
      else if(index >= 0 && index < way.getNodesCount()-1) {
        return way.getNode(index + 1);
      }
      
      return null;
    }
    
    private void calculateAngles() {
      Area area = Geometry.getAreaEastNorth(way);
      
      for(int i = 0; i < way.getNodesCount(); i++) {
        double angle = -1;
        
        double angleMeToPrev = -1;
        double angleMeToNext = -1;
        
        Node prev = findPreviouseNodeForIndex(i);
        Node next = findNextNodeForIndex(i);
        
        if(prev != null && next != null) {
          angleMeToPrev = Utils.toDegrees(way.getNode(i).getEastNorth().heading(prev.getEastNorth()));
          angleMeToNext = Utils.toDegrees(way.getNode(i).getEastNorth().heading(next.getEastNorth()));
        }
        
        if(angleMeToPrev != -1 && angleMeToNext != -1) {
          if(angleMeToNext - 180 > angleMeToPrev) {
            angleMeToPrev += 360;
          }
          else if(angleMeToPrev -180 > angleMeToNext) {
            angleMeToNext += 360;
          }
          
          angle = (angleMeToPrev + angleMeToNext) / 2;
        }
        
        double alpha = -1;
        
        if(angle > angleMeToNext) {
          alpha = angle - angleMeToNext;
        }
        else if(angle < angleMeToNext) {
          alpha = angleMeToNext - angle;
        }
        
        if(angle >= 360) {
          angle -= 360;
        }
        
        double length = 6;
        
        if(alpha != -1) {
          if(alpha > 90) {
            alpha -= 90;
          }
          
          length = length / Math.sin(Utils.toRadians(alpha));
        }
        
        // Too far away, fall back to default
        if(length > 24) {
          length = 6;
        }
        
        EastNorth add = calculateMove(angle, length);
        System.out.println(angleMeToPrev + " | " + angleMeToNext + " " + angle + " | " + alpha + " | " + length + " | " + add);
        EastNorth test = way.getNode(i).getEastNorth().add(add.east() < 0 ? -2 : 2, add.north() < 0 ? -2 : 2);
        EastNorth test2 = way.getNode(i).getEastNorth().add(add.east() < 0 ? 2 : -2, add.north() < 0 ? 2 : -2);
        
        System.out.println(" " + area.contains(test.east(), test.north()) + " " + area.contains(test2.east(), test2.north()));
        
        if(!area.contains(test.east(), test.north())) {
          add = new EastNorth(-add.east(), -add.north());
        }
        
        addList.add(add);
      }
    }
    
    private int getIndexForNode(Node n) {
      int index = -1;
      
      for(int i = 0; i < way.getNodesCount(); i++) {
        if(way.getNode(i) == n) {
          index = i;
          break;
        }
      }
      
      return index;
    }

    private EastNorth calculateMove(double heading, double length) {
      if(heading == 360) {
        heading = 0;
      }
      
      double east = 0;
      double north = 0;
      
      if(heading >= 0 && heading <= 90) {
        double alpha = Utils.toRadians(heading);
        
        east = -Math.sin(alpha) * length;
        north = -Math.cos(alpha) * length;
      }
      else if(heading > 90 && heading <= 180) {
        double alpha = Utils.toRadians(180 - heading);
        
        east = -Math.sin(alpha) * length;
        north = Math.cos(alpha) * length;
      }
      else if(heading > 180 && heading <= 270) {
        double alpha = Utils.toRadians(heading - 180);
        
        east = Math.sin(alpha) * length;
        north = Math.cos(alpha) * length;
      }
      else if(heading > 270 && heading < 360) {
        double alpha = Utils.toRadians(heading - 270);
        
        east = Math.cos(alpha) * length;
        north = -Math.sin(alpha) * length;
      }
      
      return new EastNorth(east, north);
    }
    
    public boolean isClosed() {
      return way.isClosed();
    }
  }
  
  private static final class ArrowIcon implements Icon {
    private ImageIcon arrow;
    private double heading;
    
    public ArrowIcon(ImageIcon i, int heading) {
      arrow = i;
      this.heading = Utils.toRadians(heading);
    }
    
    @Override
    public void paintIcon(Component c, Graphics g, int x, int y) {
      Graphics2D g2 = (Graphics2D)g;
      g2.rotate(heading, x + getIconWidth() / 2, y + getIconHeight() / 2);
      arrow.paintIcon(c, g2, x, y);
      g2.rotate(-heading, x + getIconWidth() / 2, y + getIconHeight() / 2);
    }

    @Override
    public int getIconWidth() {
      return ImageSizes.POPUPMENU.getAdjustedWidth();
    }

    @Override
    public int getIconHeight() {
      return ImageSizes.POPUPMENU.getAdjustedHeight();
    }
  }
  
  private static final class WayMenuItem extends JMenuItem {
    private Way way;
    private String nodePos;
    
    public WayMenuItem(Way way, int heading, ImageIcon arrow, String nodePos) {
      this.way = way;
      this.nodePos = nodePos;
      
      setText(getDirectionFromHeading(heading));
      setIcon(new ArrowIcon(arrow,heading));
    }
  }
}
