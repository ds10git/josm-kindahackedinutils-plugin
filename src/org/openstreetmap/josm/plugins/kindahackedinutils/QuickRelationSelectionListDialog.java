// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.kindahackedinutils;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.Color;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import javax.swing.DefaultListModel;
import javax.swing.JComponent;
import javax.swing.JList;
import javax.swing.KeyStroke;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;

import org.openstreetmap.josm.actions.HistoryInfoAction;
import org.openstreetmap.josm.actions.relation.EditRelationAction;
import org.openstreetmap.josm.actions.relation.SelectRelationAction;
import org.openstreetmap.josm.data.osm.DataSelectionListener;
import org.openstreetmap.josm.data.osm.DefaultNameFormatter;
import org.openstreetmap.josm.data.osm.INode;
import org.openstreetmap.josm.data.osm.IPrimitive;
import org.openstreetmap.josm.data.osm.IRelation;
import org.openstreetmap.josm.data.osm.IWay;
import org.openstreetmap.josm.data.osm.OsmPrimitive;
import org.openstreetmap.josm.data.osm.event.AbstractDatasetChangedEvent;
import org.openstreetmap.josm.data.osm.event.DataChangedEvent;
import org.openstreetmap.josm.data.osm.event.DataSetListener;
import org.openstreetmap.josm.data.osm.event.NodeMovedEvent;
import org.openstreetmap.josm.data.osm.event.PrimitivesAddedEvent;
import org.openstreetmap.josm.data.osm.event.PrimitivesRemovedEvent;
import org.openstreetmap.josm.data.osm.event.RelationMembersChangedEvent;
import org.openstreetmap.josm.data.osm.event.SelectionEventManager;
import org.openstreetmap.josm.data.osm.event.TagsChangedEvent;
import org.openstreetmap.josm.data.osm.event.WayNodesChangedEvent;
import org.openstreetmap.josm.data.osm.visitor.PrimitiveVisitor;
import org.openstreetmap.josm.data.preferences.NamedColorProperty;
import org.openstreetmap.josm.gui.MainApplication;
import org.openstreetmap.josm.gui.PrimitiveRenderer;
import org.openstreetmap.josm.gui.SideButton;
import org.openstreetmap.josm.gui.dialogs.ToggleDialog;
import org.openstreetmap.josm.gui.layer.MainLayerManager.ActiveLayerChangeEvent;
import org.openstreetmap.josm.gui.layer.MainLayerManager.ActiveLayerChangeListener;
import org.openstreetmap.josm.tools.InputMapUtils;
import org.openstreetmap.josm.tools.Shortcut;

public class QuickRelationSelectionListDialog extends ToggleDialog implements DataSelectionListener, DataSetListener, ActiveLayerChangeListener {
  private static final int TIMEOFF_CLICKING = 220;
  
  private final JList<IRelation<?>> relationList;
  private final DefaultListModel<IRelation<?>> listModel;
  
  /** the edit action */
  private final EditRelationAction edit = new EditRelationAction();
  /** the select relation action */
  private final SelectRelationAction select = new SelectRelationAction(false);
  
  private SideButton editBtn = new SideButton(edit, false);
  private SideButton selectBtn = new SideButton(select, false);
  private final AtomicInteger lastIndex = new AtomicInteger(-1);
  
  public QuickRelationSelectionListDialog() {
    super(tr("Quick Relation Selection List"), "relations", tr("Quickly select relations for which selected object is a memember of."),
        Shortcut.registerShortcut("QuickRelationSelectionListDialog.listOpen", tr("Window: {0}", tr("Quick Relation Selection List")),
            KeyEvent.VK_R, Shortcut.ALT_CTRL_SHIFT), 150, true);
    
    relationList = new JList<>(listModel = new DefaultListModel<>());
    relationList.setCellRenderer(new QuickSelectionCellRenderer(lastIndex));
    relationList.addListSelectionListener(e -> {
      if(!e.getValueIsAdjusting()) {
        final Set<IRelation<?>> selected = Collections.singleton(relationList.getSelectedValue());
        
        SwingUtilities.invokeLater(() -> {
          edit.setPrimitives(selected);
          select.setPrimitives(selected);  
        });
        
        clearHighlighting();
        
        lastIndex.set(relationList.getSelectedIndex());
        
        highlightCurrentRelation();
        
        updateBtnEnabledState();
      }
    });
    relationList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    relationList.addMouseMotionListener(new MouseMotionListener() {
      
      @Override
      public void mouseMoved(MouseEvent e) {
        clearHighlighting();
        
        int index = relationList.locationToIndex(e.getPoint());
        
        if(index != -1 && lastIndex.get() < listModel.getSize() && !relationList.getCellBounds(index, index).contains(e.getPoint())) {
          index = relationList.getSelectedIndex();
        }
        
        lastIndex.set(index);
        
        highlightCurrentRelation();
        
        relationList.repaint();
      }
      
      @Override
      public void mouseDragged(MouseEvent e) {}
    });
    relationList.addMouseListener(new MouseListener() {
      private Thread clickCountThread;
      private int clickCount;
      private long lastClick;
      
      @Override
      public void mouseReleased(MouseEvent e) {}
      
      @Override
      public void mousePressed(MouseEvent e) {}
      
      @Override
      public void mouseExited(MouseEvent e) {
        clearHighlighting();
        
        lastIndex.set(relationList.getSelectedIndex());
        
        highlightCurrentRelation();
        
        relationList.repaint();
      }
      
      @Override
      public void mouseEntered(MouseEvent e) {
        highlightCurrentRelation();
        
        relationList.repaint();
      }
      
      @Override
      public synchronized void mouseClicked(MouseEvent e) {
        if(SwingUtilities.isLeftMouseButton(e) && ((e.getModifiersEx() & MouseEvent.CTRL_DOWN_MASK) != MouseEvent.CTRL_DOWN_MASK)) {
          clickCount = e.getClickCount();
          lastClick = System.currentTimeMillis();
          
          if(clickCountThread == null || !clickCountThread.isAlive()) {
            clickCountThread = new Thread() {
              public void run() {
                while((System.currentTimeMillis() - lastClick) < TIMEOFF_CLICKING) {
                  try {
                    Thread.sleep(50);
                  } catch (InterruptedException e) {
                    // Ignore
                  }
                }
                
                SwingUtilities.invokeLater(() -> {
                  int index = relationList.locationToIndex(e.getPoint());
                  
                  if(index != -1) {
                    if(clickCount == 1) {
                      MainApplication.getLayerManager().getActiveDataSet().setSelected(listModel.getElementAt(index).getPrimitiveId());
                    }
                    else if(clickCount >= 3) {
                      edit.actionPerformed(new ActionEvent(editBtn, 0, "edit"));
                    }
                  }  
                });
              };
            };
            clickCountThread.start();
          }
        }
      }
    });
    
    createLayout(relationList, true, Arrays.asList(        
        editBtn,
        selectBtn
     ));

    // Copied from org.openstreetmap.josm.gui.dialogs.RelationListDialog
    InputMapUtils.unassignCtrlShiftUpDown(relationList, JComponent.WHEN_FOCUSED);
    
    // Select relation on Enter
    InputMapUtils.addEnterAction(relationList, select);
    
    // Edit relation on Ctrl-Enter
    relationList.getActionMap().put("edit", edit);
    relationList.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, KeyEvent.CTRL_DOWN_MASK), "edit");
    
    HistoryInfoAction historyAction = MainApplication.getMenu().historyinfo;
    relationList.getActionMap().put("historyAction", historyAction);
    relationList.getInputMap().put(historyAction.getShortcut().getKeyStroke(), "historyAction");
    
    updateBtnEnabledState();
  }
  
  private void clearHighlighting() {
    if(lastIndex.get() != -1 && lastIndex.get() < listModel.getSize()) {
      for(IPrimitive p : listModel.getElementAt(lastIndex.get()).getMemberPrimitivesList()) {
        p.setHighlighted(false);
      }
    }
  }
  
  private void highlightCurrentRelation() {
    if(lastIndex.get() != -1 && lastIndex.get() < listModel.getSize()) {
      for(IPrimitive p : listModel.getElementAt(lastIndex.get()).getMemberPrimitivesList()) {
        p.setHighlighted(true);
      }
    }
  }
  
  private void updateBtnEnabledState() {
    editBtn.setEnabled(relationList.getSelectedIndex() != -1);
    selectBtn.setEnabled(relationList.getSelectedIndex() != -1);
  }
  
  @Override
  public void showNotify() {
    SelectionEventManager.getInstance().addSelectionListenerForEdt(this);
    MainApplication.getLayerManager().addActiveLayerChangeListener(this);
    updateBtnEnabledState();
  }
  
  @Override
  public synchronized void hideNotify() {
    SelectionEventManager.getInstance().removeSelectionListener(this);
    MainApplication.getLayerManager().removeActiveLayerChangeListener(this);
    
    if(MainApplication.getLayerManager().getActiveDataSet() != null) {
      MainApplication.getLayerManager().getActiveDataSet().removeDataSetListener(this);
    }
  }
  
  @Override
  public void activeOrEditLayerChanged(ActiveLayerChangeEvent e) {
    listModel.clear();
    
    if(e.getPreviousDataSet() != null) {
      e.getPreviousDataSet().removeDataSetListener(this);
    }
    
    if(MainApplication.getLayerManager().getActiveDataSet() != null) {
      MainApplication.getLayerManager().getActiveDataSet().addDataSetListener(this);
    }
  }

  @Override
  public void selectionChanged(SelectionChangeEvent event) {
    clearHighlighting();
    listModel.clear();
    ArrayList<IRelation<?>> list = new ArrayList<>();
    
    if(MainApplication.getLayerManager().getEditDataSet() != null) { 
      for(OsmPrimitive p : MainApplication.getLayerManager().getEditDataSet().getAllSelected()) {
        p.visitReferrers(new PrimitiveVisitor() {
          @Override
          public void visit(IRelation<?> r) {
            if(!list.contains(r)) {
              list.add(r);
            }
          }
          
          @Override
          public void visit(IWay<?> w) {}
          
          @Override
          public void visit(INode n) {}
        });
      }
    }
    
    if(!list.isEmpty()) {
      list.sort(DefaultNameFormatter.getInstance().getRelationComparator());
      
      listModel.addAll(list);
    }
    
    edit.setPrimitives(Collections.emptyList());
    select.setPrimitives(Collections.emptyList());
    updateBtnEnabledState();
  }
  
  private static final class QuickSelectionCellRenderer extends PrimitiveRenderer {
    private NamedColorProperty color = new NamedColorProperty(tr("Quick Relation Selection mouse pointing highlighting color"), new Color(0x99,0xFF,0x99));
    private AtomicInteger lastIndex;
    
    QuickSelectionCellRenderer(AtomicInteger lastIndex) {
      this.lastIndex = lastIndex;
    }
    
    @Override
    public Component getListCellRendererComponent(JList<? extends IPrimitive> list, IPrimitive value, int index,
        boolean isSelected, boolean cellHasFocus) {
      Component c = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
      
      if(lastIndex.get() == index && list.getSelectedIndex() != index) {
        c.setBackground(color.get());
      }
      else if(!isSelected) {
        c.setBackground(list.getBackground());
      }
      else {
        c.setBackground(list.getSelectionBackground());
      }
      
      return c;
    }
    
    @Override
    protected String getComponentToolTipText(IPrimitive value) {
        // Don't show the default tooltip in the relation list
        return null;
    }
  }

  @Override
  public void primitivesAdded(PrimitivesAddedEvent event) {}
  @Override
  public void primitivesRemoved(PrimitivesRemovedEvent event) {}

  @Override
  public void tagsChanged(TagsChangedEvent event) {}

  @Override
  public void nodeMoved(NodeMovedEvent event) {}

  @Override
  public void wayNodesChanged(WayNodesChangedEvent event) {}

  @Override
  public void relationMembersChanged(RelationMembersChangedEvent event) {
    selectionChanged(null);
  }

  @Override
  public void otherDatasetChange(AbstractDatasetChangedEvent event) {}

  @Override
  public void dataChanged(DataChangedEvent event) {}
}
