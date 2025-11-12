// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.kindahackedinutils;

import static org.openstreetmap.josm.tools.I18n.marktr;
import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.AWTEvent;
import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.event.AWTEventListener;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionAdapter;
import java.awt.event.MouseMotionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.geom.Area;
import java.awt.geom.Path2D;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.swing.AbstractButton;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.DefaultListSelectionModel;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeSelectionModel;

import org.openstreetmap.josm.actions.AutoScaleAction;
import org.openstreetmap.josm.actions.AutoScaleAction.AutoScaleMode;
import org.openstreetmap.josm.actions.JosmAction;
import org.openstreetmap.josm.actions.PurgeAction;
import org.openstreetmap.josm.actions.mapmode.SplitMode;
import org.openstreetmap.josm.command.AddCommand;
import org.openstreetmap.josm.command.ChangeCommand;
import org.openstreetmap.josm.command.ChangeMembersCommand;
import org.openstreetmap.josm.command.ChangeNodesCommand;
import org.openstreetmap.josm.command.ChangePropertyCommand;
import org.openstreetmap.josm.command.Command;
import org.openstreetmap.josm.command.DeleteCommand;
import org.openstreetmap.josm.command.PseudoCommand;
import org.openstreetmap.josm.command.RemoveNodesCommand;
import org.openstreetmap.josm.command.SequenceCommand;
import org.openstreetmap.josm.data.Bounds;
import org.openstreetmap.josm.data.UndoRedoHandler;
import org.openstreetmap.josm.data.UndoRedoHandler.CommandQueueListener;
import org.openstreetmap.josm.data.coor.EastNorth;
import org.openstreetmap.josm.data.osm.DataSelectionListener;
import org.openstreetmap.josm.data.osm.DataSet;
import org.openstreetmap.josm.data.osm.Node;
import org.openstreetmap.josm.data.osm.OsmData;
import org.openstreetmap.josm.data.osm.OsmDataManager;
import org.openstreetmap.josm.data.osm.OsmPrimitive;
import org.openstreetmap.josm.data.osm.OsmPrimitiveType;
import org.openstreetmap.josm.data.osm.Relation;
import org.openstreetmap.josm.data.osm.RelationMember;
import org.openstreetmap.josm.data.osm.Tag;
import org.openstreetmap.josm.data.osm.TagMap;
import org.openstreetmap.josm.data.osm.Way;
import org.openstreetmap.josm.data.osm.WaySegment;
import org.openstreetmap.josm.data.osm.event.AbstractDatasetChangedEvent;
import org.openstreetmap.josm.data.osm.event.DataChangedEvent;
import org.openstreetmap.josm.data.osm.event.DataSetListener;
import org.openstreetmap.josm.data.osm.event.NodeMovedEvent;
import org.openstreetmap.josm.data.osm.event.PrimitivesAddedEvent;
import org.openstreetmap.josm.data.osm.event.PrimitivesRemovedEvent;
import org.openstreetmap.josm.data.osm.event.RelationMembersChangedEvent;
import org.openstreetmap.josm.data.osm.event.TagsChangedEvent;
import org.openstreetmap.josm.data.osm.event.WayNodesChangedEvent;
import org.openstreetmap.josm.data.osm.visitor.OsmPrimitiveVisitor;
import org.openstreetmap.josm.data.osm.visitor.paint.ArrowPaintHelper;
import org.openstreetmap.josm.data.osm.visitor.paint.PaintColors;
import org.openstreetmap.josm.data.preferences.AbstractProperty;
import org.openstreetmap.josm.data.preferences.CachingProperty;
import org.openstreetmap.josm.data.preferences.NamedColorProperty;
import org.openstreetmap.josm.data.preferences.StrokeProperty;
import org.openstreetmap.josm.gui.ExtendedDialog;
import org.openstreetmap.josm.gui.IconToggleButton;
import org.openstreetmap.josm.gui.MainApplication;
import org.openstreetmap.josm.gui.MainMenu;
import org.openstreetmap.josm.gui.MapFrame;
import org.openstreetmap.josm.gui.MapView;
import org.openstreetmap.josm.gui.MapViewState.MapViewPoint;
import org.openstreetmap.josm.gui.PrimitiveRenderer;
import org.openstreetmap.josm.gui.datatransfer.ClipboardUtils;
import org.openstreetmap.josm.gui.dialogs.CommandListMutableTreeNode;
import org.openstreetmap.josm.gui.draw.MapPath2D;
import org.openstreetmap.josm.gui.layer.MainLayerManager.ActiveLayerChangeEvent;
import org.openstreetmap.josm.gui.layer.MainLayerManager.ActiveLayerChangeListener;
import org.openstreetmap.josm.gui.layer.MapViewPaintable;
import org.openstreetmap.josm.gui.layer.OsmDataLayer;
import org.openstreetmap.josm.gui.preferences.PreferenceSetting;
import org.openstreetmap.josm.gui.tagging.presets.TaggingPreset;
import org.openstreetmap.josm.gui.tagging.presets.TaggingPresetType;
import org.openstreetmap.josm.gui.tagging.presets.TaggingPresets;
import org.openstreetmap.josm.gui.util.GuiHelper;
import org.openstreetmap.josm.gui.util.KeyPressReleaseListener;
import org.openstreetmap.josm.gui.util.WindowGeometry;
import org.openstreetmap.josm.gui.widgets.ImageLabel;
import org.openstreetmap.josm.plugins.Plugin;
import org.openstreetmap.josm.plugins.PluginInformation;
import org.openstreetmap.josm.spi.preferences.Config;
import org.openstreetmap.josm.tools.Geometry;
import org.openstreetmap.josm.tools.ImageProvider;
import org.openstreetmap.josm.tools.ImageProvider.ImageSizes;
import org.openstreetmap.josm.tools.InputMapUtils;
import org.openstreetmap.josm.tools.Shortcut;
import org.openstreetmap.josm.tools.SubclassFilteredCollection;
import org.openstreetmap.josm.tools.Utils;

/**
 * Collection of utilities
 */
public class KindaHackedInUtilsPlugin extends Plugin {
  private static final String ACTION_NAME_DIRECTION_ALTERNATIVE = "SHIFT+ALT+H_ALTERNATIVE";
  private static final String ACTION_NAME_ADD_TO_RELATION_ALTERNATIVE = "CTRL+SHIFT+R_ALTERNATIVE";
  private static final AbstractProperty<Color> RUBBER_LINE_COLOR = PaintColors.SELECTED.getProperty().getChildColor(marktr("helper line"));
  private static final CachingProperty<BasicStroke> RUBBER_LINE_STROKE = new StrokeProperty("draw.stroke.helper-line", "3").cached();
  private NamedColorProperty coneColor = new NamedColorProperty(tr("KindaHackedInUtils: Direction cone color"), new Color(0xFF,0,0,0x78));
  
  private static final LinkedList<String> TURN_RESTRICTION = 
      Stream.of(
          "no_right_turn",
          "no_left_turn",
          "no_straight_on",
          "no_u_turn",
          "only_right_turn",
          "only_left_turn",
          "only_straight_on"
      ).collect(Collectors.toCollection(LinkedList::new));
  
  private DataSet editDataSet;
  private OsmData<?, ?, ?, ?> activeData;
  
  private static KindaHackedInUtilsPlugin instance;
  
  private QuickRelationSelectionListDialog dialog;
  
  private final ImageIcon arrow;
  private final DataSetListener listener;
  private final DataSelectionListener selectionListener;
  private final AngleAction angleAction;
  private final AddToRelationAction addToRelation;
  private final Shortcut angleDegreeShortcut;
  private final Shortcut drawNodeShortcut;
  private final Shortcut addToRelationShortcut;
  private Shortcut splitWayShortcut;
  private SplitMode splitMode;
  private PurgeAction purgeAction;
  private CommandQueueListener commandListener;
  
  private ChangeListener cl = new ChangeListener() {
    private OsmPrimitveMenuItem lastItem;
    
    @Override
    public void stateChanged(ChangeEvent e) {
      if(e.getSource() instanceof OsmPrimitveMenuItem) {
        if(Objects.equals(lastItem, e.getSource())) {
          lastItem.setHighlighted(false);
          lastItem = null;
        }
        else {
          lastItem = (OsmPrimitveMenuItem)e.getSource();
          lastItem.setHighlighted(true);
        }
      }
      else {
        lastItem.setHighlighted(false);
        lastItem = null;
      }
    }
  };
  
  public KindaHackedInUtilsPlugin(PluginInformation info) {
    super(info);
    instance = this;
    drawNodeShortcut = Shortcut.registerShortcut("kindahackedinutils.drawNodeAtMouse", tr("Draw node at mouse location"), KeyEvent.VK_B, Shortcut.DIRECT);
    angleDegreeShortcut = Shortcut.registerShortcut("kindahackedinutils.angleDegree", tr("Get heading in degrees"), KeyEvent.VK_H, Shortcut.ALT_SHIFT);
    addToRelationShortcut = Shortcut.registerShortcut("kindahackedinutils.addToRelationRegardless", tr("Adds selected objects to selected relation regardless if they already are members of the relation"), KeyEvent.VK_R, Shortcut.CTRL_SHIFT);
    
    angleAction = new AngleAction();
    addToRelation = new AddToRelationAction();
    
    DetachAction detachAction = new DetachAction();
    TurnRestrictionAction turnRestrictionAction = new TurnRestrictionAction();
    UndoRedoSelectedAction undo = new UndoRedoSelectedAction();
    
    arrow = ImageProvider.get("N", ImageSizes.POPUPMENU);
    listener = new DataSetListener() {
      private Thread waitForEventEnd;
      private TagsChangedEvent lastEvent;
      private TagsChangedEvent previouseEvent;
      
      private Way lastChanged;
      private long lastChangedEvent;
      private Thread waitForWayEventEnd;
      
      @Override
      public void tagsChanged(TagsChangedEvent event) {
        if(Conf.isDirectionEnabled() && event.getPrimitives().size() == 1) {
          lastEvent = event;
          
          if(waitForEventEnd == null || !waitForEventEnd.isAlive()) {
            waitForEventEnd = new Thread() {
              public void run() {
                try {
                  Thread.sleep(50);
                } catch (InterruptedException e) {
                  // ignore
                }
                
                while(!Objects.equals(lastEvent, previouseEvent)) {
                  try {
                    Thread.sleep(100);
                  } catch (InterruptedException e) {
                    // ignore
                  }
                  previouseEvent = lastEvent;
                }

                TagsChangedEvent use = lastEvent;
                
                lastEvent = null;
                previouseEvent = null;
                
                if(use != null) {
                  if(use.getPrimitive() instanceof Node) {
                    SwingUtilities.invokeLater(() -> changeDirectionForTrafficSign(use.getPrimitives(), false, null, Conf.isObjectSpecificDirection(), Conf.isAutoSetEnabled()));
                  }
                  else if(use.getPrimitive() instanceof Way) {
                    Way w = (Way)use.getPrimitive();
                    for(int i = 0; i < w.getNodesCount(); i++) {
                      final Node n = w.getNode(i);
                      
                      if(isSpecialDirectionNode(n) && n.referrers(Way.class).count() > 1) {
                        SwingUtilities.invokeLater(() -> changeDirectionForTrafficSign(Collections.singleton(n), false, null, Conf.isObjectSpecificDirection(), Conf.isAutoSetEnabled()));
                      }
                    }
                  }
                }
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
      public synchronized void primitivesAdded(final PrimitivesAddedEvent event) {
        if(Conf.isDirectionEnabled() && (waitForWayEventEnd == null || !waitForWayEventEnd.isAlive())) {
          waitForEventEnd = new Thread() {
            public void run() {
              try {
                Thread.sleep(100);
              } catch (InterruptedException e) {
                // ignore
              }
              
              Way toUse = lastChanged;
              
              if(toUse != null && toUse.getNodesCount() > 0 && System.currentTimeMillis() - lastChangedEvent < 500 && event.getPrimitives().size() == 1) {
                Optional<? extends OsmPrimitive> test = event.getPrimitives().stream().filter(w->w instanceof Way).findFirst();
                
                if(test.isPresent()) {
                  Way newWay = (Way)test.get();
                  
                  final HashSet<Node> changed = new HashSet<>();
                  
                  if(newWay.getNodesCount() > 0) {
                    changed.add(newWay.getNode(0));
                  }
                  if(newWay.getNodesCount() > 1) {
                    changed.add(newWay.getNode(newWay.getNodesCount()-1));
                  }
                  
                  if(!changed.isEmpty()) {
                    SwingUtilities.invokeLater(() -> {
                      for(Node n : changed) {
                        changeDirectionForTrafficSign(Collections.singleton(n), false, null, Conf.isObjectSpecificDirection(), Conf.isAutoSetEnabled());
                      }
                    });
                  }
                }
              }
              
              lastChangedEvent = 0;
              lastChanged = null;
            }
          };
          waitForEventEnd.start();
        }
      }
      
      @Override
      public void otherDatasetChange(AbstractDatasetChangedEvent event) {}
      
      @Override
      public void nodeMoved(NodeMovedEvent event) {}
      
      @Override
      public void dataChanged(DataChangedEvent event) {}              
      
      @Override
      public void wayNodesChanged(WayNodesChangedEvent event) {
        if(event.getPrimitives().size() == 1) {
          lastChanged = event.getChangedWay();
          lastChangedEvent = System.currentTimeMillis();
        }
      }
    };
    
    selectionListener = e -> {
      if(e.getSelection().size() == 1) {
        changeDirectionForTrafficSign(MainApplication.getLayerManager().getActiveDataSet().getSelected(), false, null, Conf.isObjectSpecificDirection(), Conf.isAutoSetEnabled());
      }
    };
    
    JMenu toolsMenu = MainApplication.getMenu().moreToolsMenu;
    MainMenu.add(toolsMenu, detachAction);
    MainMenu.add(toolsMenu, turnRestrictionAction);
    MainMenu.add(toolsMenu, undo);
    MainMenu.add(toolsMenu, addToRelation);
    
    commandListener = (e,x) -> {
      undo.updateEnabledState();
    };
        
    UndoRedoHandler.getInstance().addCommandQueueListener(commandListener);
  }
  
  @Override
  public PreferenceSetting getPreferenceSetting() {
    return new KindaHackedInUtilsPreferences();
  }
  
  boolean hasSplitMode() {
    return splitMode != null;
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
    if(splitMode == null) {
      for(IconToggleButton b : MainApplication.getMap().allMapModeButtons) {
        if(b.getAction() instanceof SplitMode) {
          splitMode = (SplitMode)b.getAction();
          break;
        }
      }
      
      if(splitMode != null) {
        splitWayShortcut = Shortcut.registerShortcut("kindahackedinutils.splitWay", tr("Split way at mouse location"), KeyEvent.VK_K, Shortcut.DIRECT);
      }
    }
    
    if(purgeAction == null) {
      JMenu edit = MainApplication.getMenu().editMenu;
      
      for(int i = 0; i < edit.getItemCount(); i++) {
        JMenuItem item = edit.getItem(i);
        
        if(item != null && item.getAction() instanceof PurgeAction) {
          purgeAction = (PurgeAction)item.getAction();
          break;
        }
      }
    }
    
    if (oldFrame == null && newFrame != null) {
      dialog = new QuickRelationSelectionListDialog();
      newFrame.addToggleDialog(dialog);
    } else if (oldFrame != null && newFrame == null) {
      dialog = null;
    }
    
    if(oldFrame != null) {
      oldFrame.getActionMap().remove("kindahackedinutils.addHeading");
      
      MapFrame map = MainApplication.getMap();
      
      if(map != null) {
        map.keyDetector.removeKeyListener(angleAction);
      }
    }   
    if(newFrame != null) {
      MapFrame map = MainApplication.getMap();
      map.keyDetector.addKeyListener(angleAction);
      
      newFrame.getActionMap().put("kindahackedinutils.addHeading", angleAction);
      
      MainApplication.getLayerManager().addActiveLayerChangeListener(new ActiveLayerChangeListener() {
        @Override
        public void activeOrEditLayerChanged(ActiveLayerChangeEvent e) {
          if(e.getPreviousDataSet() != null) {
            if(editDataSet != null) {
              editDataSet.removeDataSetListener(listener);
              editDataSet = null;
            }
            if(activeData != null) {
              activeData.removeSelectionListener(selectionListener);
              activeData = null;
            }
          }
          
          if(MainApplication.getLayerManager().getEditDataSet() != null) {
            if(editDataSet == null || editDataSet != MainApplication.getLayerManager().getEditDataSet()) {
              editDataSet = MainApplication.getLayerManager().getEditDataSet();
              editDataSet.addDataSetListener(listener);
              
              if(activeData == null || activeData != MainApplication.getLayerManager().getActiveData()) {
                activeData = MainApplication.getLayerManager().getActiveData();
                activeData.addSelectionListener(selectionListener);
              }
            }
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
            angleAction.actionPerformed(new ActionEvent(MainApplication.getMap().mapView, 0, ACTION_NAME_DIRECTION_ALTERNATIVE));
          }
          else if(drawNodeShortcut.isEvent(e)) {
            if(Conf.isCreateNode()) {
              MainApplication.getMenu().unselectAll.actionPerformed(null);
  
              Point b = MainApplication.getMap().mapView.getMousePosition(true);
              
              if(b != null) {
                MainApplication.getMap().mapModeDraw.mouseReleased(new MouseEvent(MainApplication.getMap(), 0, System.currentTimeMillis(), 0, b.x, b.y, 1, false, MouseEvent.BUTTON1));
                MainApplication.getMap().mapModeDraw.mouseReleased(new MouseEvent(MainApplication.getMap(), 0, System.currentTimeMillis(), 0, b.x, b.y, 2, false, MouseEvent.BUTTON1));
              }
            }
          }
          else if(addToRelationShortcut.isEvent(e)) {
            addToRelation.actionPerformed(new ActionEvent(MainApplication.getMap().mapView, 0, ACTION_NAME_ADD_TO_RELATION_ALTERNATIVE));
          }
          else if(splitWayShortcut != null && splitWayShortcut.isEvent(e)) {
            if(Conf.isSplitWay() && splitMode != null) {
              MainApplication.getMenu().unselectAll.actionPerformed(null);
  
              Point b = MainApplication.getMap().mapView.getMousePosition(true);
              
              if(b != null) {
                splitMode.putValue("active", Boolean.TRUE);
                splitMode.mousePressed(new MouseEvent(MainApplication.getMap(), 0, System.currentTimeMillis(), 0, b.x, b.y, 1, false, MouseEvent.BUTTON1));
                splitMode.putValue("active", Boolean.FALSE);
              }
            }
          }
        }
      });
  	}
  }
  
  private synchronized boolean changeDirectionForTrafficSign(Collection<? extends OsmPrimitive> list, boolean ignoreExistingValue, Way wayPointedAt, final boolean objectSpecificDirection, boolean autoSet) {
    if(list.size() == 1 && list.stream().findFirst().get() instanceof Node) {
      final Node n = (Node)list.stream().findFirst().get();
      Way[] ways = n.referrers(Way.class).toArray(Way[]::new);
      
      if(isSpecialDirectionNode(n) && ((ignoreExistingValue && wayPointedAt != null) || !n.hasKey("direction") || Objects.equals(n.get("direction"),"forward") || Objects.equals(n.get("direction"),"backward"))) {
        LinkedList<Way> highways = new LinkedList<>();
        
        for(Way o : ways) {
          if(o.hasKey("highway") && (o.isFirstLastNode(n) || ways.length > 1)) {
            highways.add(o);
          }
        }
        
        JPopupMenu menu = new JPopupMenu();
        
        String direction = n.hasKey("direction") ? n.get("direction") : n.hasKey("traffic_sign:direction") ? n.get("traffic_sign:direction") : null;
        
        if(!highways.isEmpty()) {
          ActionListener a = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
              if(e.getSource() instanceof OsmPrimitveMenuItem) {
                final HashSet<Command> cmds = new HashSet<>(2);
                String key = "direction";
                
                if(Conf.isObjectSpecificDirection()) {
                  key = "traffic_sign:direction";
                }
                
                cmds.add(new ChangePropertyCommand(n, key, ((OsmPrimitveMenuItem)e.getSource()).getText()));
                DirectionKeyCommands.addRemoveCommandsToList(key, n, cmds);
                
                UndoRedoHandler.getInstance().add(new SequenceCommand("Change direction value", cmds));
                
                ((OsmPrimitveMenuItem)e.getSource()).setHighlighted(false);
                
                 if(wayPointedAt != null) {
                    SwingUtilities.invokeLater(() -> wayPointedAt.setHighlighted(true));
                 }
                
                MainApplication.getLayerManager().getActiveData().clearHighlightedWaySegments();
              }
            }
          };
          
          boolean singleWay =  (highways.size() == 1);
          boolean sameDirection = false;
          
          String lastNodePos = null;
          WaySegment segmentPointed = null;
          
          Point p = MainApplication.getMap().mapView.getMousePosition(true);
          
          if(p != null) {
            segmentPointed = MainApplication.getMap().mapView.getNearestWaySegment(p, o -> o.hasKey("highway"));
          }

          for(Way w : highways) {
            int d = -1;
            String nodePos = null;
            
            if(n == w.getNode(0)) {
              d = calculateDirection(w.getNode(1).getEastNorth(), n.getEastNorth());
              nodePos = "firstNode";
            }
            else if(n == w.getNode(w.getNodesCount()-1)) {
              d = calculateDirection(w.getNode(w.getNodesCount()-2).getEastNorth(), n.getEastNorth());
              nodePos = "lastNode";
            }
            else {
              for(int i = 1; i < w.getNodesCount()-1; i++) {
                if(n == w.getNode(i)) {
                  Node prev = w.getNode(i-1);
                  Node next = w.getNode(i+1);
                  
                  int d1 = calculateDirection(next.getEastNorth(), n.getEastNorth());
                  int d2 = calculateDirection(prev.getEastNorth(), n.getEastNorth());
                  
                  OsmPrimitveMenuItem item = new OsmPrimitveMenuItem(w, d1, arrow, "moddleNode");
                  item.waySegment = new WaySegment(w, i);
                  item.addActionListener(a);
                  item.addChangeListener(cl);
                  menu.add(item);
                  
                  if(autoSet && segmentPointed != null && w.isHighlighted() && segmentPointed.getWay() == w && segmentPointed.getFirstNode() == n) {
                    a.actionPerformed(new ActionEvent(item, 0, null));
                    return true;
                  }
                  
                  item = new OsmPrimitveMenuItem(w, d2, arrow, nodePos);
                  item.waySegment = new WaySegment(w, i-1);
                  item.addActionListener(a);
                  item.addChangeListener(cl);
                  menu.add(item);

                  if(autoSet && segmentPointed != null && w.isHighlighted() && segmentPointed.getWay() == w && segmentPointed.getSecondNode() == n) {
                    a.actionPerformed(new ActionEvent(item, 0, null));
                    return true;
                  }
                }
              }
            }
            
            if(d != -1) {
              if(lastNodePos != null && !Objects.equals(lastNodePos, nodePos)) {
                sameDirection = true;
              }
              
              lastNodePos = nodePos;
              
              OsmPrimitveMenuItem item = new OsmPrimitveMenuItem(w, d, arrow, nodePos);
              item.addActionListener(a);
              item.addChangeListener(cl);
              
              if(autoSet && (Objects.equals(wayPointedAt, w) || singleWay)) {
                a.actionPerformed(new ActionEvent(item, 0, null));
                return true;
              }
              
              menu.add(item);
            }
          }
          
          if(highways.size() == 2 && sameDirection && (Objects.equals(direction, "forward") || Objects.equals(direction, "backward"))) {
            for(int i = 0; i < 2; i++) {
              OsmPrimitveMenuItem item = (OsmPrimitveMenuItem)menu.getComponent(i);
                 
              if(autoSet && (Objects.equals(direction, "forward") && Objects.equals(item.nodePos, "firstNode")) ||
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
        else if(!ignoreExistingValue && ways.length == 1 && ((n.hasKey("traffic_sign") && objectSpecificDirection && !n.hasKey("traffic_sign:direction") && !n.hasTag("highway", "stop", "give_way")) || 
            (!objectSpecificDirection && n.hasKey("traffic_sign") && !n.hasKey("direction")  && !n.hasTag("highway", "stop", "give_way")) ||
            (!n.hasKey("direction") && n.hasTag("highway", "stop", "give_way")))) {
          ActionListener a = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
              if(e.getSource() instanceof JMenuItem) {
                final HashSet<Command> cmds = new HashSet<>(2);
                
                String key = "direction";
                
                if(objectSpecificDirection && n.hasKey("traffic_sign") && !n.hasTag("highway","stop","give_way")) {
                  key = "traffic_sign:direction";
                }
                
                cmds.add(new ChangePropertyCommand(n, key, ((JMenuItem)e.getSource()).getName()));
                DirectionKeyCommands.addRemoveCommandsToList(key, n, cmds);
                
                UndoRedoHandler.getInstance().add(new SequenceCommand("Change direction value", cmds));
                MainApplication.getLayerManager().getActiveData().clearHighlightedWaySegments();
                
                if(wayPointedAt != null) {
                  SwingUtilities.invokeLater(() -> wayPointedAt.setHighlighted(true));
                }
              }
            }
          };
          
          AtomicReference<WaySegment> forwardSegment = new AtomicReference<WaySegment>();
          AtomicReference<WaySegment> backwardSegment = new AtomicReference<WaySegment>();
          
          for(int i = 1; i < ways[0].getNodesCount(); i++) {
            if(ways[0].getNode(i) == n) {
              forwardSegment.set(new WaySegment(ways[0], i));
              backwardSegment.set(new WaySegment(ways[0], i-1));
              break;
            }
          }
            
          final JMenuItem forward = new JMenuItem("↑ " + tr("forward"));
          forward.setName("forward");
          forward.addActionListener(a);
          menu.add(forward);
          
          final JMenuItem backward = new JMenuItem("↓ " + tr("backward"));
          backward.setName("backward");
          backward.addActionListener(a);
          menu.add(backward);
          
          ChangeListener cl = new ChangeListener() {
            JMenuItem lastItem;
            
            @Override
            public void stateChanged(ChangeEvent e) {
              if(e.getSource() instanceof JMenuItem) {
                if(Objects.equals(lastItem, e.getSource())) {
                  MainApplication.getLayerManager().getActiveData().clearHighlightedWaySegments();
                  lastItem = null;
                }
                else if(Objects.equals(e.getSource(), forward)){
                  MainApplication.getLayerManager().getActiveData().setHighlightedWaySegments(Collections.singleton(forwardSegment.get()));
                  lastItem = (JMenuItem)e.getSource();
                }
                else if(Objects.equals(e.getSource(), backward)){
                  MainApplication.getLayerManager().getActiveData().setHighlightedWaySegments(Collections.singleton(backwardSegment.get()));
                  lastItem = (JMenuItem)e.getSource();
                }
              }
              else {
                lastItem = null;
                MainApplication.getLayerManager().getActiveData().clearHighlightedWaySegments();
              }
            }
          };
          
          forward.addChangeListener(cl);
          backward.addChangeListener(cl);
          
          if(Objects.equals("forward", direction)) {
            forward.getActionListeners()[0].actionPerformed(new ActionEvent(forward, 0, null));
            return true;
          }
          else if(Objects.equals("backward", direction)) {
            backward.getActionListeners()[0].actionPerformed(new ActionEvent(backward, 0, null));
            return true;
          }
        }
        
        if(Conf.isShowPopupEnabled() && menu.getComponentCount() > 0) {
          Point p = MainApplication.getMap().mapView.getPoint(n.getEastNorth());
          
          menu.show(MainApplication.getMap().mapView, p.x, p.y);
          return true;
        }
      }
    }
    
    return false;
  }
  
  private static final boolean isSpecialDirectionNode(Node n) {
    return n.hasKey("traffic_sign") ||n.hasTag("highway", "stop", "give_way");
  }
  
  public static final KindaHackedInUtilsPlugin getInstance() {
    return instance;
  }
  
  class AddToRelationAction extends JosmAction {
    public AddToRelationAction() {
      super(tr("Add selected objects to selected relation"), /* ICON() */ "relation-add", tr("Adds selected objects to selected relation"),
          Shortcut.registerShortcut("kindahackedinutils.relation-add", tr("Add selected objects to selected relation"), KeyEvent.VK_R, Shortcut.CTRL), false);
    }
    
    @Override
    public void actionPerformed(ActionEvent e) {
      OsmPrimitive[] selection = MainApplication.getLayerManager().getEditDataSet().getSelected().toArray(OsmPrimitive[]::new);
      
      if(selection.length > 0 && selection[0] instanceof Relation) {
        List<OsmPrimitive> members = ((Relation)selection[0]).getMemberPrimitivesList();
        List<RelationMember> newMembers = ((Relation)selection[0]).getMembers();
        Hashtable<OsmPrimitive, RelationMember> doublets = new Hashtable<>(); 
        
        final Collection<TaggingPreset> presets = TaggingPresets.getMatchingPresets(EnumSet.of(TaggingPresetType.forPrimitive((Relation)selection[0])), ((Relation)selection[0]).getKeys(), false);
        
        for(int i = 1; i < selection.length; i++) {
          final Set<String> roles = findSuggestedRoles(presets, selection[i]);
          final RelationMember member = new RelationMember(roles.size() == 1 ? roles.iterator().next() : "", selection[i]);
          
          if(!members.contains(selection[i])) {
            newMembers.add(member);
          }
          else {
            doublets.put(selection[i], member);
          }
        }
        
        if(!doublets.isEmpty() && !Objects.equals(ACTION_NAME_ADD_TO_RELATION_ALTERNATIVE, e.getActionCommand())) {
          AddToRelationAgainDialog dialog = new AddToRelationAgainDialog(doublets);
          dialog.showDialog();
        }
        
        if(!doublets.isEmpty()) {
          newMembers.addAll(doublets.values());
        }
        
        if(members.size() != newMembers.size()) {
          UndoRedoHandler.getInstance().add(new ChangeMembersCommand((Relation)selection[0], newMembers));
        }
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
        setEnabled(checkSelection(selection));
    }
    
    private boolean checkSelection(Collection<? extends OsmPrimitive> selection) {
      Relation r = null;
      
      OsmPrimitive[] primitives = selection.toArray(OsmPrimitive[]::new);
      
      if(primitives.length > 0 && primitives[0] instanceof Relation) {
        r = (Relation)primitives[0];
      }
      
      return r != null && primitives.length > 1;
    }
    
    // Copied from org.openstreetmap.josm.gui.dialogs.relation.GenericRelationEditor
    private Set<String> findSuggestedRoles(final Collection<TaggingPreset> presets, OsmPrimitive p) {
      return presets.stream()
              .map(preset -> preset.suggestRoleForOsmPrimitive(p))
              .filter(role -> !Utils.isEmpty(role))
              .collect(Collectors.toSet());
  }
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
      HashSet<Node> replaceNodeSet = new HashSet<>();
      
      for(Way w : selectedWays) {
        if(w.isOutsideDownloadArea() && JOptionPane.YES_OPTION != JOptionPane.showConfirmDialog(MainApplication.getMainFrame(), tr("The selected way lies outside the download area.\nThis might result in different detachments.\n\nDo you want to continue anyway?"), tr("Way outside of download area"), JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE)) {
          return;
        }
        
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
                
                if((!way.isClosed() || way.hasKey("building", "highway"))) {
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
                
                if(way.isClosed() && !way.isHighway() || isMultipolygon) {
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
                  
                  int index = way.getIndexForNode(cur);
                  
                  if(checkForReplacement(getNewNode(way.findPreviouseNodeForIndex(index), cur, way, replaceNodeSet, ds, cmds), getNewNode(way.findNextNodeForIndex(index), cur, way, replaceNodeSet, ds, cmds), cur, nodeList, nodePairs)) {
                    continue;
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
          final LinkedList<Node> newNodes = new LinkedList<Node>();
          final HashSet<Node> toRemove = new HashSet<Node>();
          final LinkedList<NodePair> list = newNodesTable.get(way);
          
          for(int i = 0; i < way.getRealNodesCount(); i++) {
            final Node node = way.getNode(i);
            
            if(list != null) {
              AtomicBoolean found = new AtomicBoolean();
              list.forEach(n -> {
                if(n.containsOldNode(node)) {
                  if(!toRemove.contains(node)) {
                    toRemove.add(node);
                  }
                  
                  newNodes.add(n.newNode);
                  found.set(true);
                }
              });
              
              if(!found.get()) {
                newNodes.add(node);
              }
            } 
            else {
              newNodes.add(node);
            }
          }
          
          newNodes.add(newNodes.get(0));
          
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
    
    private Node getNewNode(Node toCheck, Node cur, WayExt way, HashSet<Node> replacements, DataSet ds, List<Command> cmds) {
      Node extNewNode = null;
      
      if(toCheck != null && !toCheck.referrers(Way.class).anyMatch(ww -> (!ww.isClosed() && !ww.referrers(Relation.class).anyMatch(Relation::isMultipolygon)) && ww.containsNode(cur))) {
        Way[] extWays = toCheck.referrers(Way.class).filter(ww -> ww != way.way && (ww.isClosed() || ww.referrers(Relation.class).anyMatch(Relation::isMultipolygon)) && ww.containsNode(cur)).toArray(Way[]::new);
        
        if(extWays.length == 1) {          
          double angle = calculateDirection(toCheck.getEastNorth(), cur.getEastNorth());
          
          EastNorth m = WayExt.calculateMove(angle, 6);
          extNewNode = new Node(cur.getEastNorth().add(m));
          
          boolean found = false;
          
          for(Node n : replacements) {
            if(n.getEastNorth().equals(extNewNode.getEastNorth())) {
              found = true;
              extNewNode = n;
              break;
            }
          }
          
          if(!found) {
            replacements.add(extNewNode);
            cmds.add(new AddCommand(ds, extNewNode));
          }
        }
      }
      
      return extNewNode;
    }
    
    private boolean checkForReplacement(Node prev, Node next, Node cur, LinkedList<Node> nodeList, LinkedList<NodePair> nodePairs) {
      boolean result = false;
      
      if(prev != null) {
        nodeList.add(prev);
        nodePairs.add(new NodePair(cur, prev));
        result = true;
      }
      
      if(next != null) {
        nodeList.add(next);
        nodePairs.add(new NodePair(cur, next));
        result = true;
      }
      
      return result;
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
  
  class TurnRestrictionAction extends JosmAction {
    public TurnRestrictionAction() {
      super(tr("Create turn restriction or replace selected"), /* ICON() */ "presets/vehicle/restriction/turn_restrictions/no_u_turn.svg", tr("Get heading for direction from mouse location"),
          Shortcut.registerShortcut("kindahackedinutils.turnrestriction", tr("Create turn restriction"), KeyEvent.VK_T,
                  Shortcut.SHIFT), false);
    }
    
    @Override
    public void actionPerformed(ActionEvent e) {
      TurnRestrictionDialog t = new TurnRestrictionDialog();
      t.showDialog();
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
        setEnabled(checkSelection(selection));
    }
    
    private boolean checkSelection(Collection<? extends OsmPrimitive> selection) {
      Relation turnRestriction = null;
      boolean continuous = true;
      
      ArrayList<Way> ways = new ArrayList<>();
      
      for (OsmPrimitive p : selection) {
        if (p instanceof Way) {
          Way w = (Way)p;
          
          boolean add = false;
          
          if(!ways.isEmpty() && w.getNodesCount() > 0) {
            if(ways.get(ways.size()-1).isFirstLastNode(w.getNode(0)) ||
                ways.get(ways.size()-1).isFirstLastNode(w.getNode(w.getNodesCount()-1))) {
              add = true;
            }
            else {
              continuous = false;
            }
          }
          
          if(ways.isEmpty() || add) {
            ways.add(w);
          }
        }
        else if(p instanceof Relation && turnRestriction == null) {
          Relation r = (Relation)p;
          
          for(Tag t : p.getKeys().getTags()) {
            if(t.getKey().startsWith("restriction") && TURN_RESTRICTION.contains(t.getValue())) {
              turnRestriction = r;
              break;
            }
          }
        }
      }
      
      if(turnRestriction != null && ways.size() == 1) {
        for(int i = 0; i < turnRestriction.getMembersCount(); i++) {
          if(Objects.equals("from", turnRestriction.getRole(i)) && turnRestriction.getMember(i).getMember() instanceof Way) {
            Way w = turnRestriction.getMember(i).getWay();
            
            if(!ways.get(0).isFirstLastNode(w.getNode(0)) && !ways.get(0).isFirstLastNode(w.getNode(w.getNodesCount()-1))) {
              continuous = false;
            }
          }
        }
      }
      
      return continuous && (ways.size() >= 2 || ((ways.size() == 0 || ways.size() == 1) && turnRestriction != null));
    }
  }
  
  class UndoRedoSelectedAction extends JosmAction {
    public UndoRedoSelectedAction() {
      super(tr("Undo/redo changes on selected object"), /* ICON() */ "unredo.svg", tr("Undo/redo changes on selected object"),
          Shortcut.registerShortcut("kindahackedinutils.undoRedoObject", tr("undo/redo object"), KeyEvent.VK_Z,
                  Shortcut.ALT_CTRL_SHIFT), false);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
      if(MainApplication.getLayerManager().getActiveData().getAllSelected().isEmpty()) {
        String initialValue = "";
        
        if(ClipboardUtils.getClipboardStringContent() != null && ClipboardUtils.getClipboardStringContent().matches("([wnr]{1}|way\\s+|node\\s+|relation\\s+)\\d+")) {
          initialValue = ClipboardUtils.getClipboardStringContent();
        }
        
        String result = JOptionPane.showInputDialog(MainApplication.getMainFrame(), tr("Enter type and id of object (like ''w12345'' or ''way 12345'' for ways,\n''n12345'' or ''node 12345'' for nodes, ''r12345'' or ''relation 12345'' for relations)"), initialValue);
        
        if(result == null) {
          return;
        }
        
        int index = result.indexOf(" ");
        
        if(index == -1) {
          index = 1;
        }
        
        String type = result.substring(0,index).strip();
        String id = result.substring(index).strip();
        
        OsmPrimitiveType pType = null;
        
        if(Objects.equals(type, "w") || Objects.equals(type, "way")) {
          pType = OsmPrimitiveType.WAY;
        }
        else if(Objects.equals(type, "n") || Objects.equals(type, "node")) {
          pType = OsmPrimitiveType.NODE;
        }
        else if(Objects.equals(type, "r") || Objects.equals(type, "relation")) {
          pType = OsmPrimitiveType.RELATION;
        }
        
        if(pType == null) {
          return;
        }
        
        try {
          OsmPrimitive p = OsmDataManager.getInstance().getActiveDataSet().getPrimitiveById(Long.parseLong(id), pType);
          
          if(p == null) {
            return;
          }
          
          OsmDataManager.getInstance().getActiveDataSet().setSelected(p);
        }catch(NumberFormatException nfe) {
          return;
        }
      }
      
      /*
      final LinkedList<PrimitiveId> ids = new LinkedList<>();
      
      for(Object o : MainApplication.getLayerManager().getActiveData().getAllSelected()) {
        if(o instanceof OsmPrimitive) {
          ids.add(((OsmPrimitive)o).getPrimitiveId());
        }
        if(o instanceof Way) {
          Way w = (Way)o;
          
          for(int i = 0; i < w.getRealNodesCount(); i++) {
            OsmDataManager.getInstance().getActiveDataSet().addSelected(w.getNode(i));
            ids.add(w.getNode(i).getPrimitiveId());
            
            w.getNode(i).referrers(Way.class).forEach(x -> {
              if(!ids.contains(x.getPrimitiveId())) {
                ids.add(x.getPrimitiveId());
              }
            });
          }
        }
      }
      
      purgeAction.actionPerformed(new ActionEvent(this, 0, "DUMMY"));
      
      if(!ids.isEmpty() && MainApplication.getLayerManager().getActiveData().getAllSelected().isEmpty()) {
        DownloadPrimitiveAction.processItems(false, ids, false, false);
      }*/
      
      List<Command> undos = UndoRedoHandler.getInstance().getUndoCommands();
      Collection<OsmPrimitive> selected = OsmDataManager.getInstance().getActiveDataSet().getAllSelected();
      HashSet<Node> affectedNodes = new HashSet<>();
      
      for(OsmPrimitive s : selected) {
        if(s instanceof Way) {
          Way w = (Way)s;
          
          if(w.isDeleted()) {
            for(Command undo : undos) {
              if(undo.getParticipatingPrimitives().contains(w) && undo instanceof SequenceCommand) {
                for(PseudoCommand pc : undo.getChildren()) {
                  if(pc instanceof DeleteCommand) {
                    pc.getParticipatingPrimitives().forEach(o -> {
                      if(o instanceof Node) {
                        affectedNodes.add((Node)o);
                      }
                    });
                  }
                }
              }
            }
          }
          
          affectedNodes.addAll(w.getNodes());
        }
      }
      
      LinkedList<Command> matchingUndosList = new LinkedList<Command>();
      
      for(int i = undos.size()-1; i >= 0; i--) {
        Command c = undos.get(i);
        
        Collection<? extends OsmPrimitive> primitives = c.getParticipatingPrimitives();
        
        for(OsmPrimitive p : primitives) {
          if(selected.contains(p) || (p instanceof Node && (affectedNodes.contains(p)))) {
            matchingUndosList.add(c);
            break;
          }
        }
      }
      
      if(matchingUndosList.isEmpty()) {
        JOptionPane.showMessageDialog(MainApplication.getMainFrame(), tr("No undo commands for selected object found."));
      }
      else {
        UndoRedoDialog undo = new UndoRedoDialog(matchingUndosList);
        
        undo.showDialog();
      }
    }
    
    @Override
    protected void updateEnabledState() {
        updateEnabledStateOnCurrentSelection();
    }
    
    @Override
    protected void updateEnabledState(Collection<? extends OsmPrimitive> selection) {
        if (MainApplication.getLayerManager().getEditDataSet() == null || !UndoRedoHandler.getInstance().hasUndoCommands()) {
            setEnabled(false);
            return;
        }
        setEnabled(true);
    }
  }
  
  class AngleAction extends JosmAction implements KeyPressReleaseListener, MapViewPaintable, MouseMotionListener {
    private final ArrowPaintHelper ah = new ArrowPaintHelper(Utils.toRadians(35), 20);
    
    private MouseAdapter mouseAdapter;
    private String pressedAction;
    private EastNorth start;
    private boolean isSimpleDirection;
    private Node trafficSigNode;
    private int angle;
    private String simpleDirection;
    
    public AngleAction() {
      super(tr("Get heading for direction from mouse location"), /* ICON() */ "statusline/heading.svg", tr("Get heading for direction from mouse location"),
          Shortcut.registerShortcut("kindahackedinutils.angle", tr("Get heading"), KeyEvent.VK_H,
                  Shortcut.DIRECT), false);
      mouseAdapter = new MouseAdapter() {
        @Override
        public void mouseExited(MouseEvent e) {
          handleMouseMovement();
        }
      };
    }

    @Override
    public void actionPerformed(ActionEvent e) {
      if(Conf.isDirectionEnabled() && pressedAction == null && start != null) {
        Collection<Node> nodes = OsmDataManager.getInstance().getActiveDataSet().getSelectedNodes();
        Collection<Way> waysSelected = OsmDataManager.getInstance().getActiveDataSet().getSelectedWays();
        Collection<Relation> relationsSelected = OsmDataManager.getInstance().getActiveDataSet().getSelectedRelations();
        ArrayList<Relation> buildingRelationsSelected = new ArrayList<>();
        
        for(Relation r : relationsSelected) {
          if(r.isMultipolygon() && DirectionTagMap.getDirectionKeyForPrimitive(r) != null) {
            buildingRelationsSelected.add(r);
          }
        }
        
        final Point p = MainApplication.getMap().mapView.getMousePosition(true);
        
        if(p != null) {
          int test = calculateDirection(start, MainApplication.getMap().mapView.getEastNorth(p.x, p.y));
          int a = test;
          
          if(nodes.size() == 1) {
            try {
              final boolean objectSpecificDirection = Conf.isObjectSpecificDirection();
              
              Node n = nodes.stream().findFirst().get();
              
              Way pointedTo = MainApplication.getMap().mapView.getNearestWay(p, OsmPrimitive::isSelectable);
              
              if(pointedTo != null && !pointedTo.isHighlighted()) {
                pointedTo = null;
              }
              
              if(!changeDirectionForTrafficSign(Collections.singletonList(n), true, pointedTo, objectSpecificDirection, true)) {
                if(Conf.isNaturalDirection() && (isSpecialDirectionNode(n) || n.hasTag("highway", "traffic_signals") || n.hasTag("railway", "signal"))) {
                  a = inverseDirection(a);
                }
                
                AtomicReference<String> angle = new AtomicReference<String>(getDirectionFromHeading(a));
                
                String key = "direction";
                
                String simpleDirection = null;
                Way[] ways = n.referrers(Way.class).toArray(Way[]::new);
                
                ArrayList<OsmPrimitive> waysToHandleList = new ArrayList<>();
                
                if(Conf.isDirectionFromNodeForWaysEnabled()) {
                  if(DirectionTagMap.getDirectionKeyForPrimitive(n) != null) {
                    waysToHandleList.add(n);
                  }
                  
                  for(Way w : ways) {
                    if(DirectionTagMap.getDirectionKeyForPrimitive(w) != null) {
                      waysToHandleList.add(w);
                    }
                    else {
                      w.visitReferrers(new OsmPrimitiveVisitor() {
                        @Override
                        public void visit(Relation r) {
                          if(r.isMultipolygon() && DirectionTagMap.getDirectionKeyForPrimitive(r) != null) {
                            waysToHandleList.add(r);
                          }
                        }
                        
                        @Override
                        public void visit(Way w) {}
                        
                        @Override
                        public void visit(Node n) {}
                      });
                    }
                  }
                }
                
                int wayIndex = 0;
                
                if((ways.length == 2 && n.hasTag("highway", "traffic_signals"))) {
                  for(int i = 0; i < ways.length; i++) {
                    if(n == ways[i].getNode(0)) {
                      wayIndex = i;
                      break;
                    }
                  }
                }
                
                if(waysToHandleList.isEmpty() && ((ways.length == 1) || (ways.length == 2 && !n.hasKey("traffic_sign") && 
                    n.hasTag("highway", "traffic_signals", "give_way", "stop")))) {
                  simpleDirection = getSimpleDirectionForPointed(MainApplication.getMap().mapView, ways[wayIndex], n, MainApplication.getMap().mapView.getEastNorth(p.x, p.y));
                  int pAngle = getDirectionForPointed(MainApplication.getMap().mapView, n, p);
                  
                  if(pAngle != -1) {
                    a = pAngle;
                    angle.set(getDirectionFromHeading(pAngle));
                  }
                }
                else if(!waysToHandleList.isEmpty()) {
                  if(waysToHandleList.size() > 1) {
                    handleMultiplePrimitives(a, angle.get(), waysToHandleList, MainApplication.getMap().mapView.getPoint(n.getEastNorth()));
                  }
                  else if(!waysToHandleList.isEmpty()) {
                    UndoRedoHandler.getInstance().add(new ChangePropertyCommand(waysToHandleList.get(0), DirectionTagMap.getDirectionKeyForPrimitive(waysToHandleList.get(0)), String.valueOf(test)));
                    OsmDataManager.getInstance().getActiveDataSet().setSelected(waysToHandleList.get(0));
                  }
                  
                  return;
                }
                
                LinkedList<Command> cmdList = new LinkedList<>();
                
                boolean isTrafficSign = n.hasKey("traffic_sign");
                boolean isTrafficSignals = n.hasTag("highway", "traffic_signals");
                boolean isRailwaySignal = n.hasTag("railway", "signal");
                boolean isStopSign = n.hasTag("highway","stop");
                boolean isGiveWaySign = n.hasTag("highway","give_way");
                
                if(simpleDirection != null &&
                    (isTrafficSign || isTrafficSignals || isRailwaySignal || isStopSign || isGiveWaySign) 
                    && !Objects.equals(ACTION_NAME_DIRECTION_ALTERNATIVE, e.getActionCommand())) {
                  if(isTrafficSign && (objectSpecificDirection || 
                      (Conf.isSimpleDirection()))) {
                    if(objectSpecificDirection) {
                      key = "traffic_sign:direction";
                    }
                    
                    angle.set(simpleDirection);
                  }
                  else if(isTrafficSignals) {
                    key = "traffic_signals:direction";
                    angle.set(simpleDirection);
                  }
                  else if(isRailwaySignal) {
                    key = "railway:signal:direction";
                    angle.set(simpleDirection);                      
                  }
                  else if(!n.hasKey("traffic_sign") || isStopSign || isGiveWaySign) {
                    angle.set(simpleDirection);
                  }
                }
                else if(n.hasTag("highway", "street_lamp")) {
                  key = "light:direction";
                }
                else if(n.hasTag("manmade", "surveillance")) {
                  key = "camera:direction";
                }
                else if(n.hasKey("traffic_sign") && Config.getPref().getBoolean("kindahackedinutils.angleInfoNotShown", true)) {
                  JOptionPane.showMessageDialog(MainApplication.getMainFrame(), tr("Note that the direction for a traffic sign is opposite of the direction it''s effect is.\nFor example if a street's direction is in 45° forward and a speed limit sign is placed besides this street in forward direction it is mapped with a direction of 225.\nIf natural direction is enabled for traffic signs you can just point the mouse behind the traffic sign in the direction of the street to get the correct mapping."));
                  Config.getPref().putBoolean("kindahackedinutils.angleInfoNotShown", false);
                }
                
                if(DirectionKeyCommands.isDirection(key)) {
                  DirectionKeyCommands.addRemoveCommandsToList(key, n, cmdList);
                }
                
                cmdList.add(new ChangePropertyCommand(n, key, angle.get()));
                
                UndoRedoHandler.getInstance().add(new SequenceCommand("Change direction", cmdList));
                
                if(pointedTo != null) {
                  final Way toHighlight = pointedTo;
                  SwingUtilities.invokeLater(() -> toHighlight.setHighlighted(true));
                }
              }
            }catch(NumberFormatException nfe) {
              nfe.printStackTrace();
            }
          }
          else if(nodes.isEmpty() && ((waysSelected.size() == 1 && buildingRelationsSelected.size() == 0) || 
              (waysSelected.size() == 0 && buildingRelationsSelected.size() == 1))) {
            ArrayList<OsmPrimitive> buildingsList = new ArrayList<>();
            
            if(buildingRelationsSelected.size() == 1) {
              buildingsList.add(buildingRelationsSelected.get(0));
            }
            else {
              Way w = waysSelected.stream().findFirst().get();
              
              w.visitReferrers(new OsmPrimitiveVisitor() {
                @Override
                public void visit(Relation r) {
                  if(r.isMultipolygon() && DirectionTagMap.getDirectionKeyForPrimitive(r) != null) {
                    buildingsList.add(r);
                  }
                }
                
                @Override
                public void visit(Way w) {}
                @Override
                public void visit(Node n) {}
              });
              
              if(buildingsList.isEmpty() && DirectionTagMap.getDirectionKeyForPrimitive(w) != null) {
                buildingsList.add(w);
              }
            }
            
            if(!buildingsList.isEmpty()) {
              if(buildingsList.size() == 1) {
                OsmPrimitive op = buildingsList.get(0);
                
                ArrayList<Node> nodeList = new ArrayList<>();
                
                if(op instanceof Relation) {
                  for(int i = 0; i < ((Relation)op).getMembersCount(); i++) {
                    if(Objects.equals(((Relation)op).getRole(i), "outer")) {
                      RelationMember m = ((Relation)op).getMember(i);
                      
                      if(m.getType() == OsmPrimitiveType.WAY) {
                        nodeList.addAll(m.getWay().getNodes());
                      }
                      else if(m.getType() == OsmPrimitiveType.NODE) {
                        nodeList.add(m.getNode());
                      }
                    }
                  }
                }
                else {
                  nodeList.addAll(((Way)op).getNodes());
                }
                
                if(!nodeList.isEmpty() && p != null) {
                  UndoRedoHandler.getInstance().add(new ChangePropertyCommand(op, DirectionTagMap.getDirectionKeyForPrimitive(op), getDirectionFromHeading(calculateDirection(Geometry.getCentroid(nodeList), MainApplication.getMap().mapView.getEastNorth(p.x, p.y)))));
                  
                  if(op instanceof Relation) {
                    OsmDataManager.getInstance().getActiveDataSet().setSelected(op);
                  }
                }
              }
              else if(buildingsList.size() > 1 && !waysSelected.isEmpty()) {
                Way w = waysSelected.stream().findFirst().get();
                EastNorth ea = Geometry.getCentroid(w.getNodes());
                
                handleMultiplePrimitives(a, getDirectionFromHeading(a), buildingsList, MainApplication.getMap().mapView.getPoint(ea));
              }
            }
          }
        }
      }
    }
    
    private void handleMultiplePrimitives(int a, String angle, ArrayList<OsmPrimitive> waysToHandleList, Point p1) {
      Collections.sort(waysToHandleList, DirectionTagMap.COMPARATOR);
      
      JPopupMenu menu = new JPopupMenu();
      
      ActionListener b = new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent e) {
          ((OsmPrimitveMenuItem)e.getSource()).changeTags();
        }
      };
      
      String lastTitle = null;
      HashSet<OsmPrimitive> current = new HashSet<>();
      boolean showAll = false;
      
      for(int i = 0; i < waysToHandleList.size(); i++) {
        String name = DirectionTagMap.getNameForPrimitive(waysToHandleList.get(i));
        
        if(lastTitle == null || !Objects.equals(name, lastTitle)) {
          if(!current.isEmpty() && current.size() > 1) {
            @SuppressWarnings("unchecked")
            OsmPrimitveMenuItem item = new OsmPrimitveMenuItem((HashSet<OsmPrimitive>)current.clone(), a, arrow, "");
            
            item.setText(DirectionTagMap.getPluralForSingular(lastTitle));
            item.setName(angle);
            item.addActionListener(b);
            item.addChangeListener(cl);
            
            menu.add(item);
          }
          
          if(lastTitle != null) {
            menu.addSeparator();
            showAll = true;
          }
          
          JLabel label = new JLabel(DirectionTagMap.getDirectionKeyForPrimitive(waysToHandleList.get(i)) + "="+angle+" "+tr("for")+":");
          label.setAlignmentX(JLabel.CENTER_ALIGNMENT);
          menu.add(label);
          
          current.clear();
        }
        
        current.add(waysToHandleList.get(i));
        
        lastTitle = name;
         
        OsmPrimitveMenuItem item = new OsmPrimitveMenuItem(waysToHandleList.get(i), a, arrow, "");
        
        item.setText(name.replace("{0}", String.valueOf(current.size())));
        item.setName(angle);
        item.addActionListener(b);
        item.addChangeListener(cl);
        
        menu.add(item);
      }
      
      if(!current.isEmpty() && current.size() > 1) {
        OsmPrimitveMenuItem item = new OsmPrimitveMenuItem(current, a, arrow, "");
        
        item.setText(DirectionTagMap.getPluralForSingular(lastTitle));
        item.setName(angle);
        item.addActionListener(b);
        item.addChangeListener(cl);
        
        menu.add(item);
      }
      
      if(showAll) {
        menu.addSeparator();
        
        OsmPrimitveMenuItem item = new OsmPrimitveMenuItem(waysToHandleList, a, arrow, "");
        
        item.setText(tr("{0} for all objects", angle));
        item.setName(angle);
        item.addActionListener(b);
        item.addChangeListener(cl);
        
        menu.add(item);
      }
      
      menu.show(MainApplication.getMap().mapView, p1.x, p1.y);
    }
    
    @Override
    public void doKeyPressed(KeyEvent e) {
      angle = -1;
      if((Conf.isDirectionHelperLineEnabled() || Conf.isDirectionHelperConeEnabled()) && (getShortcut().isEvent(e) || angleDegreeShortcut.isEvent(e))) {
        MainApplication.getMap().mapView.addTemporaryLayer(AngleAction.this);
        MainApplication.getMap().mapView.addMouseMotionListener(AngleAction.this);
        MainApplication.getMap().mapView.addMouseListener(mouseAdapter);
        pressedAction = getShortcut().isEvent(e) ? "" : ACTION_NAME_DIRECTION_ALTERNATIVE;
      }
      
      Collection<Node> nodes = OsmDataManager.getInstance().getActiveDataSet().getSelectedNodes();
      Collection<Way> ways = OsmDataManager.getInstance().getActiveDataSet().getSelectedWays();
      Collection<Relation> relations = OsmDataManager.getInstance().getActiveDataSet().getSelectedRelations();
      
      MapView mv = MainApplication.getMap().mapView;
      
      if(OsmDataManager.getInstance().getActiveDataSet().getSelectedNodes().size() == 1) {
        Node n = nodes.stream().findFirst().get();
        start = n.getEastNorth();
        
        if(nodes.stream().anyMatch(n1 -> isSpecialDirectionNode(n1) || n1.hasTag("highway", "traffic_signals") || n1.hasTag("railway", "signal"))) {
          trafficSigNode = n;
        }
        
        isSimpleDirection = pressedAction != null && pressedAction.isBlank() && (trafficSigNode != null && (Conf.isSimpleDirection() || Conf.isObjectSpecificDirection())) && n.referrers(Way.class).filter(w -> isHighOrRailway(w)).count() == 1;
      }
      else if(ways.size() == 1) {
        Way w = ways.stream().findFirst().get();
        List<Relation> relationList = w.referrers(Relation.class).filter(r -> r.isMultipolygon() && DirectionTagMap.getDirectionKeyForPrimitive(r) != null).toList();
        
        if(relationList.size() == 1) {
          setStartForRelation(relationList.get(0), mv);
        }
        else if(relationList.size() > 0 || DirectionTagMap.getDirectionKeyForPrimitive(w) != null) {
          Point p0 = mv.getPoint(Geometry.getCentroid(w.getNodes()));
          start = mv.getEastNorth(p0.x, p0.y);
        }
      }
      else if(relations.size() == 1 && relations.stream().anyMatch(r -> r.isMultipolygon() && DirectionTagMap.getDirectionKeyForPrimitive(r) != null)) {
        setStartForRelation(relations.stream().findFirst().get(), mv);
      }
      
      handleMouseMovement();
    }

    private void setStartForRelation(Relation r, MapView mv) {
      ArrayList<Node> nodeList = new ArrayList<>();
      
      for(RelationMember m : r.getMembers()) {
        if(m.getRole().equals("outer") && m.getMember() instanceof Way) {
          for(Node n :m.getWay().getNodes()) {
            if(!nodeList.contains(n)) {
              nodeList.add(n);
            }
          }
        }
      }
      
      if(!nodeList.isEmpty()) {
        Point p0 = mv.getPoint(Geometry.getCentroid(nodeList));
        start = mv.getEastNorth(p0.x, p0.y);
      }
    }
    
    @Override
    public void doKeyReleased(KeyEvent e) {
      if(pressedAction != null) {
        MainApplication.getMap().mapView.removeTemporaryLayer(AngleAction.this);
        MainApplication.getMap().mapView.removeMouseMotionListener(AngleAction.this);
        MainApplication.getMap().mapView.removeMouseListener(mouseAdapter);
        String action = pressedAction; 
        pressedAction = null;
        
        actionPerformed(new ActionEvent(MainApplication.getMainFrame(), 0, action));
        MainApplication.getMap().statusLine.setHeading(-1);
      }
      
      trafficSigNode = null;
      isSimpleDirection = false;
      start = null;
    }

    @Override
    public void paint(Graphics2D g, MapView mv, Bounds bbox) {
      g.setColor(RUBBER_LINE_COLOR.get());
      g.setStroke(RUBBER_LINE_STROKE.get());
      g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
      Point p1 = mv.getMousePosition(true);
      
      if(start != null && !isSimpleDirection && angle == -1) {
        Point p0 = mv.getPoint(start);
        
        if(p1 != null && (!pressedAction.isBlank() || trafficSigNode == null || angle == -1 || (!Conf.isSimpleDirection() && trafficSigNode.referrers(Way.class).count() == 1) || !trafficSigNode.referrers(Way.class).anyMatch(w -> (w.hasTag("highway") || w.hasTag("railway")) && w.isHighlighted()))) {
          angle = calculateDirection(start, mv.getEastNorth(p1.x, p1.y));
          
          if(trafficSigNode != null && Conf.isNaturalDirection()) {
            angle = inverseDirection(angle);
          }
          
          if(Conf.isDirectionHelperLineEnabled()) {
            g.drawLine(p0.x, p0.y, p1.x, p1.y);
          }
        }
      }
      else if(start != null && trafficSigNode != null && simpleDirection != null && Conf.isDirectionHelperLineEnabled()) {
        Way w = trafficSigNode.referrers(Way.class).findFirst().get();
        
        if(!w.isHighlighted()) {
          int index1 = -1;
          int index2 = -1;
          
          for(int i = 0; i < w.getRealNodesCount(); i++) {
            if(w.getNode(i) == trafficSigNode) {
              if(i != w.getRealNodesCount()-1) {
                index1 = i;
                index2 = i+1;
              }
              else {
                index1 = i-1;
                index2 = i;
              }
              
              break;
            }
          }
          
          if(index1 != -1) {
            int angle = calculateDirection(w.getNode(index1).getEastNorth(), w.getNode(index2).getEastNorth())+90;
            
            if(angle >= 360) {
              angle -= 360;
            }
            
            EastNorth add = WayExt.calculateMove(angle, (int)400*mv.getScale());
            
            Point p0 = mv.getPoint(start.add(add));
            Point p2 = mv.getPoint(start.add(-add.east(), -add.north()));
            
            g.drawLine(p0.x, p0.y, p2.x, p2.y);
          }
        }
      }
        
      if(p1 != null && simpleDirection != null && Conf.isDirectionArrowForSimpleDirectionEnabled()) {
        Point p2 = simpleDirection.equals("forward") ? new Point(p1.x, p1.y - 100) : new Point(p1.x, p1.y + 100);
        
        g.drawLine(p1.x, p1.y, p2.x, p2.y);
        
        MapViewPoint mvp = mv.getState().getPointFor(mv.getEastNorth(p2.x, p2.y));
        MapViewPoint from = mv.getState().getPointFor(mv.getEastNorth(p1.x, p1.y));
        
        MapPath2D path = new MapPath2D();
        ah.paintArrowAt(path, mvp, from);
        g.draw(path);
      }
      
      if(Conf.isDirectionHelperConeEnabled() && angle != -1 && p1 != null && start != null) {
        Point p0 = mv.getPoint(start);
        Path2D p = new Path2D.Double();
        p.moveTo(p0.x, p0.y);
        EastNorth l = mv.getEastNorth(p0.x, p0.y);
        
        int viewAngle = 35;
        
        int angle = inverseDirection(this.angle) + viewAngle;
        
        if(angle >= 360) {
          angle -= 360;
        }
        
        EastNorth add = WayExt.calculateMove(angle, viewAngle*mv.getScale());
        
        angle = inverseDirection(this.angle) - viewAngle;
        
        if(angle < 0) {
          angle += 360;
        }
        
        EastNorth add3 = WayExt.calculateMove(angle, viewAngle*mv.getScale());
        
        
        EastNorth add2 = WayExt.calculateMove(inverseDirection(this.angle), (viewAngle+10)*mv.getScale());
        
        Point c1 = mv.getPoint(l.add(add));
        Point c2 = mv.getPoint(l.add(add3));
        Point cm = mv.getPoint(l.add(add2));
        
        p.lineTo(c1.x, c1.y);
        p.curveTo(c1.x, c1.y, cm.x, cm.y, c2.x, c2.y);
        p.lineTo(p0.x, p0.y);
        
        g.setColor(coneColor.get());
        g.fill(p);
      }
      
      setHeadingStatus();
    }
    
    private void setHeadingStatus() {
      if(simpleDirection == null) {
        String direction = getDirectionFromHeading(angle);
        
        if(direction.matches("\\d+")) {
          MainApplication.getMap().statusLine.setHeading(angle);
        }
        else {
          ((ImageLabel)MainApplication.getMap().statusLine.getComponent(2)).setText(direction);
        }
      }
      else if(simpleDirection != null && MainApplication.getMap().statusLine.getComponentCount() >= 3 && MainApplication.getMap().statusLine.getComponent(2) instanceof ImageLabel) {
        ((ImageLabel)MainApplication.getMap().statusLine.getComponent(2)).setText("<html><div style=\"font-size:85%;\">"+simpleDirection+"</div></html>");
      }
    }

    @Override
    public void mouseDragged(MouseEvent e) {
      handleMouseMovement();
    }

    @Override
    public void mouseMoved(MouseEvent e) {
      handleMouseMovement();
    }
    
    private synchronized void handleMouseMovement() {
      angle = -1;
      simpleDirection = null;
      
      MapView mv = MainApplication.getMap().mapView;
      Point p = mv.getMousePosition(true);
      
      if(p != null) {
        if(!isSimpleDirection && trafficSigNode != null && trafficSigNode.referrers(Way.class).anyMatch(w -> (w.hasTag("highway") || w.hasTag("railway")) && w.isHighlighted())) {
          angle = getDirectionForPointed(mv, trafficSigNode, p);
          
          if(angle != -1) {
            setHeadingStatus();
          }
        }
        else if(isSimpleDirection && trafficSigNode != null) {
          simpleDirection = getSimpleDirectionForPointed(mv, trafficSigNode.referrers(Way.class).findFirst().get(), trafficSigNode, mv.getEastNorth(p.x, p.y));
          Way w = trafficSigNode.referrers(Way.class).findFirst().get();
          
          int index = -1;
          
          for(int i = 0; i < w.getRealNodesCount(); i++) {
            if(w.getNode(i) == trafficSigNode) {
              index = i;
              break;
            }
          }
          
          int index1 = -1;
          int index2 = -1;
          
          if(Objects.equals("forward", simpleDirection)) {
            if(index != w.getRealNodesCount()-1) {
              index1 = index;
              index2 = index+1;
            }
            else {
              index1 = index-1;
              index2 = index;
            }
          }
          else if(simpleDirection != null) {
            if(index != 0)  {
              index1 = index;
              index2 = index-1;
            }
            else {
              index1 = index+1;
              index2 = index;
            }
          }
          
          angle = calculateDirection(w.getNode(index2).getEastNorth(), w.getNode(index1).getEastNorth());
        }
      }
      
      MainApplication.getMap().mapView.repaint();
    }
    
    private boolean isHighOrRailway(OsmPrimitive p) {
      return p instanceof Way && p.hasKey("highway", "railway");
    }
    
    private int getDirectionForPointed(MapView mv, Node n, Point mouseLocation) {
      int angle = -1;
      
      List<Way> ways = n.referrers(Way.class).filter(w -> w.containsNode(n) && isHighOrRailway(w) && w.isHighlighted()).toList();
      
      if(ways.size() == 1 && ways.get(0).getRealNodesCount() > 1) {
        Way w = ways.get(0);
        WaySegment s = mv.getNearestWaySegment(mouseLocation, o -> isHighOrRailway(w) && (o instanceof Way && ((Way)o).containsNode(n)));
        
        if(s != null && s.getWay() == w) {
          int nIndex = -1;
          int sIndex = -1;
          
          for(int i = 0; i < w.getRealNodesCount(); i++) {
            if(w.getNode(i) == n) {
              nIndex = i;
            }
            if(w.getNode(i) == s.getSecondNode()) {
              sIndex = i;
            }
            
            if(nIndex != -1 && sIndex != -1) {
              break;
            }
          }
          
          if(sIndex <= nIndex) {
            angle = calculateDirection(w.getNode(nIndex-1).getEastNorth(), w.getNode(nIndex).getEastNorth());
          }
          else {
            angle = calculateDirection(w.getNode(nIndex+1).getEastNorth(), w.getNode(nIndex).getEastNorth());
          }

          if(!Conf.isNaturalDirection()) {
            angle = inverseDirection(angle);
          }
        }
      }
      
      return angle;
    }
    
    private String getSimpleDirectionForPointed(MapView mv, Way w, Node n, EastNorth mouseLocation) {
      String simpleDirection = null;
      WaySegment s = mv.getNearestWaySegment(mv.getPoint(mouseLocation), o -> isHighOrRailway(o));
      
      if(w.isHighlighted() && w.containsNode(n) && s != null && s.getWay() == w) {
        if(s != null && s.getWay() == w) {
          int nIndex = -1;
          int sIndex = -1;
          
          for(int i = 0; i < w.getRealNodesCount(); i++) {
            if(w.getNode(i) == n) {
              nIndex = i;
            }
            
            if(w.getNode(i) == s.getSecondNode()) {
              sIndex = i;
            }
            
            if(nIndex != -1 && sIndex != -1) {
              break;
            }
          }
          
          if(nIndex != -1 && nIndex != -1) {
            if(sIndex <= nIndex) {
              simpleDirection = !Conf.isNaturalDirection() && Conf.isOppositeSimpleDirectionEnabled() ? "forward" : "backward";
            }
            else {
              simpleDirection = !Conf.isNaturalDirection() && Conf.isOppositeSimpleDirectionEnabled() ? "backward" : "forward";
            }
          }
        }
      }
      else if(w.getRealNodesCount() > 1) {
        for(int i = 0; i < w.getRealNodesCount(); i++) {
          if(w.getNode(i) == n) {
            if(i != w.getRealNodesCount()-1) {  
              simpleDirection = getSimpleDirection(calculateDirection(n.getEastNorth(), w.getNode(i+1).getEastNorth()), calculateDirection(n.getEastNorth(), mouseLocation));
            }
            else {
              simpleDirection = getSimpleDirection(calculateDirection(w.getNode(w.getRealNodesCount()-1).getEastNorth(), n.getEastNorth()), calculateDirection(n.getEastNorth(), mouseLocation));
            }
            
            break;
          }
        }
      }
      
      return simpleDirection;
    }
    
    private int inverseDirection(int angle) {
      angle += 180;
      
      if(angle >= 360) {
        angle -= 360;
      }
      
      return angle;
    }
  }
    
  private static int calculateDirection(EastNorth en1, EastNorth en2) {
    return (int)Math.round(Math.round(Utils.toDegrees(en1.heading(en2))));
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
      return !Conf.isNaturalDirection() && Conf.isOppositeSimpleDirectionEnabled() ? "backward" : "forward";
    }
    else {
      return !Conf.isNaturalDirection() && Conf.isOppositeSimpleDirectionEnabled() ? "forward" : "backward";
    }
  }
  
  private static final class DirectionKeyCommands {
    private static final List<String> DIRECTION_KEYS = Arrays.asList("direction","traffic_sign:direction","traffic_signals:direction");
    
    public static void addRemoveCommandsToList(String key, OsmPrimitive p, Collection<Command> cmdList) {
      for(String dKey : DIRECTION_KEYS) {
        if(!Objects.equals(dKey, key)) {
          cmdList.add(new ChangePropertyCommand(p, dKey, null));
        }
      }
    }
    
    public static boolean isDirection(String key) {
      return key != null && DIRECTION_KEYS.contains(key);
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
      
      for(Way w : ways) {
        boolean inSameMultipoligone = matchingRelations(multis, w.referrers(Relation.class).filter(Relation::isMultipolygon).toArray(Relation[]::new));
          
        if(w != way) {
          Node foundNode = null;
          
          if(w.getNode(w.getNodesCount()-1) == me) {
            foundNode = w.getNode(w.getNodesCount()-2);
          }
          else if(w.getNode(0) == me) {
            foundNode = w.getNode(1);
          }
          
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
      
      Relation[] multis = way.referrers(Relation.class).filter(Relation::isMultipolygon).toArray(Relation[]::new);
      
      if(!way.isClosed() && multis.length == 1) {
        LinkedList<Node> nodeList = new LinkedList<Node>();
        
        for(int i = 0; i < multis[0].getMembersCount(); i++) {
          RelationMember m = multis[0].getMember(i);
          if(m.isWay() && !m.getWay().isIncomplete() && Objects.equals("outer", m.getRole())) {
            nodeList.addAll(m.getWay().getNodes());
          }
        }
        
        area = Geometry.getArea(nodeList);
      }
      
      for(int i = 0; i < way.getNodesCount()-1; i++) {
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
        EastNorth test = way.getNode(i).getEastNorth().add(add.east() >= 1 ? 1 : add.east() <= -1 ? -1 : add.east(), add.north() >= 1 ? 1 : add.north() <= -1 ? -1 : add.north());
        
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

    private static EastNorth calculateMove(double heading, double length) {
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
    
    public boolean isHighway() {
      return way.hasKey("highway");
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
  
  private static final class OsmPrimitveMenuItem extends JMenuItem {
    private Collection<OsmPrimitive> osmPrimitives;
    private WaySegment waySegment;
    private String nodePos;
    
    public OsmPrimitveMenuItem(Collection<OsmPrimitive> ways, int heading, ImageIcon arrow, String nodePos) {
      this.osmPrimitives = ways;
      this.nodePos = nodePos;
      
      setText(getDirectionFromHeading(heading));
      
      if(arrow != null) {
        setIcon(new ArrowIcon(arrow,heading));
      }
    }
    
    public OsmPrimitveMenuItem(OsmPrimitive way, int heading, ImageIcon arrow, String nodePos) {
      this(Collections.singleton(way), heading, arrow, nodePos);
    }
    
    public void changeTags() {
      HashSet<Command> cmdList = new HashSet<>();
      
      for(OsmPrimitive p : osmPrimitives) {
        cmdList.add(new ChangePropertyCommand(p, DirectionTagMap.getDirectionKeyForPrimitive(p), getName()));
      }
      
      UndoRedoHandler.getInstance().add(new SequenceCommand("Change direction keys", cmdList));
      
      setHighlighted(false);
      OsmDataManager.getInstance().getActiveDataSet().setSelected(osmPrimitives);
    }
    
    public void setHighlighted(boolean value) {
      if(waySegment != null) {
        if(value) {
          MainApplication.getLayerManager().getActiveData().setHighlightedWaySegments(Collections.singleton(waySegment));
        }
        else {
          MainApplication.getLayerManager().getActiveData().clearHighlightedWaySegments();
        }
      }
      else {
        osmPrimitives.forEach(p -> {
          if(p instanceof Relation) {
            for(OsmPrimitive op : ((Relation) p).getMemberPrimitives()) {
              if(op instanceof Way || op instanceof Node) {
                op.setHighlighted(value);
              }
            }
          }
          else {
            p.setHighlighted(value);
          }
        });
      }
    }
  }
  
  private static final class TurnRestrictionDialog extends ExtendedDialog {
    private JList<String> restrictionType;
    private ButtonGroup onlyForGroup;
    private ImageSelectionButton[] except;
    private String only;
    private Set<String> exceptValue;
    
    public TurnRestrictionDialog() {
      super(MainApplication.getMainFrame(), tr("Turn restriction type"), new String[] {tr("OK"), tr("Cancel")}, true /* modal */);
      setRememberWindowGeometry(TurnRestrictionDialog.class.getName() + ".geometry",
          WindowGeometry.centerInWindow(MainApplication.getMainFrame(), new Dimension(630, 330)));
      setButtonIcons("ok", "cancel");
      
      restrictionType = new JList<>(TURN_RESTRICTION.toArray(new String[0]));
      restrictionType.setSelectedIndex(restrictionType.getModel().getSize()-1);
      
      Collection<OsmPrimitive> selection = OsmDataManager.getInstance().getActiveDataSet().getAllSelected();
      
      for(OsmPrimitive s : selection) {
        if(s instanceof Relation) {
          String res = s.get("restriction");
          
          if(res == null) {
            for(Tag tag : s.getKeys().getTags()) {
              if(tag.getKey().startsWith("restriction:")) {
                only = tag.getKey().substring(tag.getKey().indexOf(":")+1);
                
                res = tag.getValue();
                break;
              }
            }
          }
          
          if(res != null) {
            restrictionType.setSelectedValue(res, true);
          }
          
          if(s.hasKey("except")) {
            exceptValue = Set.of(s.get("except").split(";",-1));
          }
          
          break;
        }
      }
      
      restrictionType.setCellRenderer(new DefaultListCellRenderer() {
        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
          String v = (String)value;
          
          JLabel l = (JLabel)super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
          l.setText(tr(v));
          ImageIcon i = ImageProvider.get("presets/vehicle/restriction/turn_restrictions", (v.startsWith("no_") && !v.equals("no_u_turn") ? v +"_red" : v) +".svg", ImageSizes.CURSOR);
          
          if(i != null) {
            l.setIcon(i);
          }
          
          return l;
        }
      });
      
      JPanel left = new JPanel();
      left.setLayout(new BoxLayout(left, BoxLayout.Y_AXIS));
      left.add(new JLabel(tr("only for:")));
      
      final String[] values = {"hgv", "caravan", "motorcar", "bus", "agricultural", "motorcycle", "bicycle", "hazmat"};
      final String[] iconNames = {"presets/vehicle/restriction/goods","caravan","presets/vehicle/restriction/motorcar","presets/vehicle/restriction/psv","tractor","presets/vehicle/restriction/motorbike","presets/vehicle/restriction/bicycle","hazmat"};
      final ImageSelectionButton[] onlyFor = new ImageSelectionButton[values.length];
      onlyForGroup = new ButtonGroup();
      
      for(int i = 0; i < values.length; i++) {
        onlyFor[i] = new ImageSelectionButton(iconNames[i], tr(values[i]), values[i]);
        onlyFor[i].setAlignmentX(LEFT_ALIGNMENT);
        onlyForGroup.add(onlyFor[i]);
        left.add(onlyFor[i]);
        
        if(Objects.equals(values[i], only)) {
          onlyFor[i].setSelected(true);
        }
      }
      
      JPanel right = new JPanel();
      right.setLayout(new BoxLayout(right, BoxLayout.Y_AXIS));
      right.add(new JLabel(tr("except:")));
      
      final String[] values2 = {"psv", "bicycle", "hgv", "motorcar", "emergency"};
      final String[] iconNames2 = {"presets/transport/bus", "presets/vehicle/restriction/plain/bicycle", "hgv", "presets/vehicle/restriction/plain/motorcar", "presets/emergency/ambulance_station"};
      except = new ImageSelectionButton[values2.length];
      
      for(int i = 0; i < values2.length; i++) {
        except[i] = new ImageSelectionButton(iconNames2[i], tr(values2[i]), values2[i]);
        except[i].setName(values2[i]);
        except[i].setAlignmentX(LEFT_ALIGNMENT);
        right.add(except[i]);
        
        if(exceptValue != null && exceptValue.contains(values2[i])) {
          except[i].setSelected(true);
        }
      }
      
      JPanel content = new JPanel(new BorderLayout(10,0));
      content.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
      
      content.add(left, BorderLayout.WEST);
      content.add(restrictionType, BorderLayout.CENTER);
      content.add(right, BorderLayout.EAST);
      
      setContent(content);
    }
    
    protected void buttonAction(int j, ActionEvent evt) {
      if (j == 0 && restrictionType.getSelectedValue() != null) { // OK Button
        Collection<OsmPrimitive> selection = OsmDataManager.getInstance().getActiveDataSet().getAllSelected();
        
        Relation turnRestriction = null;
        
        ArrayList<OsmPrimitive> primitives = new ArrayList<>();
        ArrayList<Way> ways = new ArrayList<>();
        ArrayList<Node> nodes = new ArrayList<>();
        
        ArrayList<OsmPrimitive> from = new ArrayList<>();
        ArrayList<OsmPrimitive> via = new ArrayList<>();
        ArrayList<OsmPrimitive> to = new ArrayList<>();
        
        int wayCount = 0;
        int nodeCount = 0;
        
        for(OsmPrimitive s : selection) {
          if(s instanceof Relation) {
            turnRestriction = (Relation)s;
            
            for(int i = turnRestriction.getMembersCount()-1; i >= 0 ; i--) {
              RelationMember m = turnRestriction.getMember(i);
              
              if(Objects.equals("from", m.getRole())) {
                from.add(m.getMember());
              }
              else if(Objects.equals("via", m.getRole())) {
                via.add(m.getMember());
              }
              else if(Objects.equals("to", m.getRole())) {
                to.add(m.getMember());
              }
            }
          }
          else if(s instanceof Way || s instanceof Node) {
            if(s instanceof Way) {
              wayCount++;
              ways.add((Way)s);
            }
            else {
              nodeCount++;
              nodes.add((Node)s);
            }
            primitives.add(s);
          }
        }
        
        if(nodes.isEmpty() && ways.size() == 2) {
          Node nF = ways.get(0).getNode(0);
          Node nL = ways.get(0).getNode(ways.get(0).getNodesCount()-1);
          
          Node oF = ways.get(1).getNode(0);
          Node oL = ways.get(1).getNode(ways.get(1).getNodesCount()-1);
          
          if(nF == oF || nF == oL) {
            nodes.add(nF);
            primitives.add(1, nF);
          }
          else if(nL == oL || nL == oF) {
            nodes.add(nL);
            primitives.add(1, nL);
          }
          
          nodeCount = 1;
        }
        
        if(nodes.isEmpty() && ways.size() == 1 && turnRestriction != null && via.size() == 1 && via.get(0) instanceof Node) {
          nodes.add((Node)via.get(0));
        }
        
        boolean existing = turnRestriction != null;
        Relation replacement = existing ? new Relation(turnRestriction, false, false) : new Relation();
        
        if(turnRestriction != null && primitives.size() < 3) {
          if(wayCount == 1 && nodeCount == 1) {
            via.clear();
            via.addAll(nodes);
            to.clear();
            to.addAll(ways);
          }
          else if(wayCount == 2 && nodeCount == 0) {
            via.clear();
            via.add(ways.get(0));
            to.clear();
            to.add(ways.get(ways.size()-1));
          }
          else if(wayCount == 1) {
            to.clear();
            to.add(ways.get(0));
          }
        }
        
        if(!existing) {
          for(int i = 0; i < primitives.size(); i++) {
            if(i == 0) {
              from.add(primitives.get(0));
            }
            else if(i == primitives.size()-1) {
              to.add(primitives.get(i));
            }
            else {
              via.add(primitives.get(i));
            }
          }
        }
        
        String restriction = "restriction";
        
        if(onlyForGroup.getSelected() != null) {
          restriction += ":" + onlyForGroup.getSelected().getName();
          replacement.put("restriction", null);
        }
        
        if(only != null && !restriction.endsWith((":"+only))) {
          replacement.put("restriction:"+only, null);
        }
        
        replacement.put("type", "restriction");
        replacement.put(restriction, restrictionType.getSelectedValue());
        
        StringBuilder b = new StringBuilder();
        
        for(int i = 0; i < except.length; i++) {
          if(except[i].isSelected()) {
            if(b.length() > 0) {
              b.append(";");
            }
            
            b.append(except[i].getName());
          }
        }
        
        if(b.length() > 0) {
          replacement.put("except", b.toString());
        }
        else if(exceptValue != null) {
          replacement.put("except", null);
        }
        
        for(OsmPrimitive f : from) {
          replacement.addMember(new RelationMember("from", f));
        }
        for(OsmPrimitive v : via) {
          replacement.addMember(new RelationMember("via", v));
        }
        for(OsmPrimitive t : to) {
          replacement.addMember(new RelationMember("to", t));
        }
        
        if(!existing) {        
          UndoRedoHandler.getInstance().add(new AddCommand(OsmDataManager.getInstance().getActiveDataSet(), replacement));
        }
        else {
          UndoRedoHandler.getInstance().add(new ChangeCommand(OsmDataManager.getInstance().getActiveDataSet(), turnRestriction, replacement));
        }
      }
      
      super.buttonAction(j, evt);
    }
  }
  
  private static final class ButtonGroup {
    private AbstractButton selected;
    private ItemListener listener;
    
    public ButtonGroup() {
      listener = e -> {
        if(e.getStateChange() == ItemEvent.SELECTED) {
          if(selected != null) {
            selected.setSelected(false);;
          }
          
          selected = (AbstractButton)e.getItem();
        }
        else if(e.getStateChange() == ItemEvent.DESELECTED) {
          if(Objects.equals(selected, e.getItem())) {
            selected = null;
          }
        }
      };
    }
    
    public void add(Object b) {
      if(b instanceof AbstractButton) {
        ((AbstractButton)b).addItemListener(listener);
      }
      else if(b instanceof ImageSelectionButton) {
        ((ImageSelectionButton) b).addItemListener(listener);
      }
    }
    
    public AbstractButton getSelected() {
      return selected;
    }
  }
  
  // Copied from org.openstreetmap.josm.gui.dialogs.CommandStackDialog
  private static class CommandCellRenderer extends DefaultTreeCellRenderer {
    @Override
    public Component getTreeCellRendererComponent(JTree tree, Object value, boolean sel, boolean expanded, boolean leaf, int row,
            boolean hasFocus) {
        super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);
        DefaultMutableTreeNode v = (DefaultMutableTreeNode) value;
        if (v.getUserObject() instanceof JLabel) {
            JLabel l = (JLabel) v.getUserObject();
            setIcon(l.getIcon());
            setText(l.getText());
        }
        return this;
    }
  }
  
  /*
   * Inspired by org.openstreetmap.josm.gui.dialogs.CommandStackDialog
   */
  private static final class UndoRedoDialog extends ExtendedDialog {
    private final DefaultTreeModel undoTreeModel = new DefaultTreeModel(new DefaultMutableTreeNode());
    private final JTree undoTree = new JTree(undoTreeModel);
    private final DefaultMutableTreeNode undoRoot = new DefaultMutableTreeNode();
    private final LinkedList<Command> matchingUndosList;
    
    public UndoRedoDialog(LinkedList<Command> matchingUndosList) {
      super(MainApplication.getMainFrame(), tr("Select undo/redo command to undo/redo until"), new String[] {tr("Undo"),tr("Redo"),tr("Cancel")}, true, true);
      setRememberWindowGeometry(getClass().getName() + ".geometry",
          WindowGeometry.centerInWindow(MainApplication.getMainFrame(), new Dimension(850, 600)));
      setButtonIcons("undo","redo","cancel");
      
      this.matchingUndosList = matchingUndosList;
      
      for (Command undoCommand : matchingUndosList) {
        undoRoot.add(getNodeForCommand(undoCommand));
      }
      
      undoTreeModel.setRoot(undoRoot);
      undoTree.setRootVisible(false);
      undoTree.addMouseListener(new MouseAdapter() {
        @Override
        public void mouseClicked(MouseEvent e) {
          if(SwingUtilities.isLeftMouseButton(e) && e.getClickCount() == 2 && undoTree.getSelectionPath() != null) {
            PseudoCommand command = ((CommandListMutableTreeNode) undoTree.getSelectionPath().getLastPathComponent()).getCommand();
            
            if (command == null) {
                return;
            }

            DataSet dataSet = MainApplication.getLayerManager().getEditDataSet();
            
            if (dataSet == null) {
              return;
            }
            
            dataSet.setSelected(getAffectedPrimitives(command));
            AutoScaleAction.autoScale(AutoScaleMode.SELECTION);
          }
        }
      });
      undoTree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
      undoTree.setShowsRootHandles(true);
      undoTree.expandRow(0);
      undoTree.setCellRenderer(new CommandCellRenderer());
      
      InputMapUtils.unassignCtrlShiftUpDown(undoTree, JComponent.WHEN_FOCUSED);
      
      undoTree.addTreeSelectionListener(e -> {
        buttons.get(0).setEnabled(e.getNewLeadSelectionPath() != null);
        buttons.get(1).setEnabled(buttons.get(0).isEnabled());
      });
      
      JLabel info = new JLabel(tr("<html>Select the command that you want to be undone/redone, all later commands will also be undone/redone.<br><br>The list is sorted from last at the top to earliest at the bottom.<br>The list will contain all available undo commands for the selected object even if they have already been undone previousely.</html>"));
      
      JPanel content = new JPanel(new BorderLayout(0,5));
      content.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 5));
      content.add(info, BorderLayout.NORTH);
      content.add(new JScrollPane(undoTree), BorderLayout.CENTER);
      
      setContent(content, false);
    }
    
    @Override
    public void setVisible(boolean visible) {
      if(visible) {
        buttons.get(0).setEnabled(undoRoot.getChildCount() == 1);
        buttons.get(1).setEnabled(false);
        setDefaultButton(0);
        
        if(undoRoot.getChildCount() == 1) {
          undoTree.setSelectionRow(0);
        }
      }
      
      super.setVisible(visible);
    }
    
    @Override
    protected void buttonAction(int buttonIndex, ActionEvent evt) {
      if(buttonIndex == 0 || buttonIndex == 1) {
        GuiHelper.runInEDTAndWait(() -> {
          DataSet ds = OsmDataManager.getInstance().getEditDataSet();
          if (ds != null) {
              ds.beginUpdate();
          }
          try {
            CommandListMutableTreeNode node = (CommandListMutableTreeNode) undoTree.getSelectionModel().getSelectionPath().getLastPathComponent();
            
            while(!Objects.equals(node.getParent(), undoRoot)) {
              node = (CommandListMutableTreeNode) node.getParent();
            }
            
            PseudoCommand selected = node.getCommand();
            
            if(buttonIndex == 0) {
              for(Command c : matchingUndosList) {              
                try {
                  c.undoCommand();
                }catch(Throwable e) {
                  //ignore
                }
                
                if(Objects.equals(c, selected)) {
                  break;
                }
              }
            }
            else {
              boolean redo = false;
              
              for(int i = matchingUndosList.size()-1; i >= 0; i--) {
                if(!redo && Objects.equals(matchingUndosList.get(i), selected)) {
                  redo = true;
                }
                
                if(redo) {
                  try {
                    matchingUndosList.get(i).executeCommand();
                  }catch(Throwable e) {
                    //ignore
                  }
                }
              }
            }
          } finally {
              if (ds != null) {
                  ds.endUpdate();
              }
          }
        });
      }
      
      super.buttonAction(buttonIndex, evt);
    }
    
    /**
     * Copied from org.openstreetmap.josm.gui.dialogs.CommandStackDialog
     * Wraps a command in a CommandListMutableTreeNode.
     * Recursively adds child commands.
     * @param c the command
     * @return the resulting node
     */
    private CommandListMutableTreeNode getNodeForCommand(PseudoCommand c) {
        CommandListMutableTreeNode node = new CommandListMutableTreeNode(c);
        if (c.getChildren() != null) {
            List<PseudoCommand> children = new ArrayList<>(c.getChildren());
            for (PseudoCommand child : children) {
                node.add(getNodeForCommand(child));
            }
        }
        return node;
    }
    
    /**
     * Copied from org.openstreetmap.josm.gui.dialogs.CommandStackDialog
     * Return primitives that are affected by some command
     * @param c the command
     * @return collection of affected primitives, only usable ones
     */
    protected static Collection<? extends OsmPrimitive> getAffectedPrimitives(PseudoCommand c) {
        final OsmDataLayer currentLayer = MainApplication.getLayerManager().getEditLayer();
        return new SubclassFilteredCollection<>(
                c.getParticipatingPrimitives(),
                o -> {
                    OsmPrimitive p = currentLayer.data.getPrimitiveById(o);
                    return p != null && p.isUsable();
                }
        );
    }
  }
  
  private static final class AddToRelationAgainDialog extends ExtendedDialog {
    private final JList<OsmPrimitive> primitiveList;
    private final Hashtable<OsmPrimitive, RelationMember> doubletsMemberTable;
    private final DefaultListModel<OsmPrimitive> model;
    
    public AddToRelationAgainDialog(Hashtable<OsmPrimitive, RelationMember> doubletsMemberTable) {
      super(MainApplication.getMainFrame(), tr("Select objects to add to relation again"), new String[] {tr("Add all"),tr("Add selected"),tr("Don''t add again")}, true, true);
      setRememberWindowGeometry(getClass().getName() + ".geometry",
          WindowGeometry.centerInWindow(MainApplication.getMainFrame(), new Dimension(630, 500)));
      setButtonIcons("addall","dialogs/add","cancel");
      
      this.doubletsMemberTable = doubletsMemberTable;
      
      model = new DefaultListModel<>();
      model.addAll(doubletsMemberTable.keySet());
      
      primitiveList = new JList<>(model);
      primitiveList.setCellRenderer(new PrimitiveRenderer());
      primitiveList.setSelectionModel(new SingleClickSelectionModel<OsmPrimitive>(primitiveList));
      primitiveList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
      primitiveList.addListSelectionListener(e -> {
        if(!e.getValueIsAdjusting()) {
          buttons.get(1).setEnabled(primitiveList.getSelectedIndex() != -1);
        }
      });
      
      JLabel info = new JLabel(tr("<html><p>The following objects are already members of the relation.</p><p>Select the objects you want to add again or press <b>{0}</b>.</p><br><p>(The list uses single click and dragging selection.)</p></html>", tr("Add all")));
      
      JPanel content = new JPanel(new BorderLayout(0,5));
      content.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 5));
      content.add(info, BorderLayout.NORTH);
      content.add(new JScrollPane(primitiveList), BorderLayout.CENTER);
      
      setContent(content, false);
      
      addWindowListener(new WindowAdapter() {
        @Override
        public void windowClosing(WindowEvent e) {
          cancel();
        }
      });
    }
    
    @Override
    protected void buttonAction(int buttonIndex, ActionEvent evt) {
      // add all
      if(buttonIndex == 2) {
        cancel();
      }
      else if(buttonIndex == 1) {
        for(int i = 0; i < model.getSize(); i++) {
          if(!primitiveList.isSelectedIndex(i)) {
            doubletsMemberTable.remove(model.get(i));
          }
        }
      }
      
      super.buttonAction(buttonIndex, evt);
    }
    
    @Override
    public void setVisible(boolean visible) {
      if(visible) {
        buttons.get(1).setEnabled(false);
        setDefaultButton(0);
        setCancelButton(2);
      }
      
      super.setVisible(visible);
    }
    
    private void cancel() {
      doubletsMemberTable.clear();
    }
  }
  
  private static final class SingleClickSelectionModel<T> extends DefaultListSelectionModel {
    private boolean mousePressed;
    private int indexPressed;
    private Point lastLocation;
    private int direction;
    private int lastHandledIndex;
    
    private SingleClickSelectionModel(final JList<T> list) {
        MouseListener[] listeners = list.getMouseListeners();
        
        for(MouseListener l : listeners) {
          list.removeMouseListener(l);
        }
      
        list.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
              direction = 0;
              indexPressed = list.locationToIndex(e.getPoint());
              
              if(indexPressed != -1 && !list.getCellBounds(indexPressed, indexPressed).contains(e.getPoint())) {
                indexPressed = -1;
              }
              
              lastHandledIndex = indexPressed;
              
              if(indexPressed != -1) {
                if(list.isSelectedIndex(indexPressed)) {
                  list.removeSelectionInterval(indexPressed, indexPressed);
                }
                else {
                  if (getSelectionMode() == MULTIPLE_INTERVAL_SELECTION) {
                    list.addSelectionInterval(indexPressed, indexPressed);
                  }
                  else {
                    list.setSelectionInterval(indexPressed, indexPressed);
                  }
                }
              }
              
              mousePressed = true;
            }
            
            @Override
            public void mouseReleased(MouseEvent e) {
              mousePressed = false;
              
              lastLocation = null;
            }
        });
        
        list.addMouseMotionListener(new MouseMotionAdapter() {
          @Override
          public void mouseDragged(MouseEvent e) {
            if (getSelectionMode() == MULTIPLE_INTERVAL_SELECTION) {
              int index = list.locationToIndex(e.getPoint());
              
              if(index != -1 && !list.getCellBounds(index, index).contains(e.getPoint())) {
                index = -1;
              }
              
              indexPressed = index;
              
              int lastiDirection = direction;
              
              if(lastLocation != null) {
                if(e.getPoint().y - lastLocation.y > 0) {
                  direction = 1;
                }
                else if(e.getPoint().y - lastLocation.y < 0) {
                  direction = -1;
                }
              }
              
              if(mousePressed && index != -1 && (index != lastHandledIndex || (lastiDirection != 0 && lastiDirection != direction))) {
                lastHandledIndex = index;
                
                if(list.isSelectedIndex(index)) {
                  removeSelectionInterval(index, index);
                }
                else {
                  addSelectionInterval(index, index);
                }
              }
              
              lastLocation = e.getPoint();
            }
          }
        });
    }
    
    @Override
    public void setSelectionInterval(int index0, int index1) {
      if(indexPressed != -1) {
        if (getSelectionMode() == MULTIPLE_INTERVAL_SELECTION) {
            if (!mousePressed ) {
                if (isSelectedIndex(index0)) {
                    super.removeSelectionInterval(index0, index1);
                }
                else {
                    super.addSelectionInterval(index0, index1);
                }
            }
        }
        else {
            super.setSelectionInterval(index0, index1);
        }
      }
    }
  }
  
  private static final class DirectionTagMap {
    static final Comparator<OsmPrimitive> COMPARATOR = new Comparator<OsmPrimitive>() {
      @Override
      public int compare(OsmPrimitive o1, OsmPrimitive o2) {
        String k1 = getDirectionKeyForPrimitive(o1);
        String k2 = getDirectionKeyForPrimitive(o2);
        
        return k1.compareToIgnoreCase(k2);
      }
    };
    
    private static final String[] keys = new String[] {"building","building:part"};
    private static final Map<String, String> tags = Map.ofEntries(Map.entry("generator:source", "solar"));
    
    private static final Map<String, String> keysToSet = Map.ofEntries(Map.entry("generator:source", "direction"),
        Map.entry("building", "roof:direction"), Map.entry("building:part", "roof:direction"));
    
    private static final Map<String, String> names = Map.ofEntries(Map.entry("generator:source", tr("Solar {0}")),
        Map.entry("building", tr("Building {0}")), Map.entry("building:part", tr("Building {0}")),
        Map.entry(tr("Building {0}"),tr("All Buildings")), Map.entry(tr("Solar {0}"),tr("All Solars")));
    
    public static String getPluralForSingular(String singular) {
      return names.get(singular);
    }
    
    public static String getNameForPrimitive(OsmPrimitive p) {
      String result = null;
      
      int i = 0;
      
      do {
        String key = keys[i++];
        
        if(p.hasKey(key)) {
          result = names.get(key);
        }
      }while(result == null && i < keys.length);
      
      if(result == null) {
        Set<Map.Entry<String, String>> matching = tags.entrySet().stream().filter(e -> p.hasTag(e.getKey(), e.getValue())).collect(Collectors.toSet());
        
        if(matching.size() == 1) {
          Map.Entry<String, String> key = matching.stream().findFirst().get();
          
          result = names.get(key.getKey());
        }
      }
      
      return result;
    }
    
    public static String getDirectionKeyForPrimitive(OsmPrimitive p) {
      String result = null;
      
      int i = 0;
      
      do {
        String key = keys[i++];
        
        if(p.hasKey(key)) {
          result = keysToSet.get(key);
        }
      }while(result == null && i < keys.length);
      
      if(result == null) {
        Set<Map.Entry<String, String>> matching = tags.entrySet().stream().filter(e -> p.hasTag(e.getKey(), e.getValue())).collect(Collectors.toSet());
        
        if(matching.size() == 1) {
          Map.Entry<String, String> key = matching.stream().findFirst().get();
          
          result = keysToSet.get(key.getKey());
        }
      }
      
      return result;
    }
  }
  
  public static class ImageSelectionButton extends JPanel {
    private JCheckBox button;
    private JLabel label;
        
    public ImageSelectionButton(String image, String text, String value) {
      this(image, text, value, false);
    }
        
    public ImageSelectionButton(String image, String text, String value, boolean buttonLast) {
      setLayout(new BoxLayout(this, BoxLayout.LINE_AXIS));
      setOpaque(false);
      button = new JCheckBox();
      button.setName(value);
      MouseAdapter m = new MouseAdapter() {
        @Override
        public void mouseClicked(MouseEvent e) {
          if(SwingUtilities.isLeftMouseButton(e)) {
            button.setSelected(!button.isSelected());
          }
        }
      };
      this.addMouseListener(m);
      
      label = new JLabel(ImageProvider.get(image, ImageSizes.LARGEICON));
      label.addMouseListener(m);
      
      if(text != null) {
        label.setText(text);
      }
      
      if(!buttonLast) {
        add(button);
        add(Box.createRigidArea(new Dimension(5,0)));
        add(label);
      }
      else {
        add(label);
        add(Box.createRigidArea(new Dimension(5,0)));
        add(button);
      }
    }
    
    public void addItemListener(ItemListener listener) {
      button.addItemListener(listener);
    }
    
    public boolean isSelected() {
      return button.isSelected();
    }
        
    public void setSelected(boolean isSelected) {
      button.setSelected(isSelected);
    }
    
    public boolean isButton(Object o) {
      return Objects.equals(button, o);
    }
    
    public void setEnabled(boolean value) {
      super.setEnabled(value);
      button.setEnabled(value);
      label.setEnabled(value);
    }
  }
}
