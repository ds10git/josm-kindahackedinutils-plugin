package org.openstreetmap.josm.plugins.kindahackedinutils;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ItemEvent;

import javax.swing.BorderFactory;
import javax.swing.JCheckBox;
import javax.swing.JPanel;

import org.openstreetmap.josm.gui.preferences.DefaultTabPreferenceSetting;
import org.openstreetmap.josm.gui.preferences.PreferenceTabbedPane;
import org.openstreetmap.josm.tools.GBC;

public class KindaHackedInUtilsPreferences extends DefaultTabPreferenceSetting {
  private JCheckBox directionEnabled;
  private JCheckBox directionObjectSpecific;
  private JCheckBox directionSimple;
  private JCheckBox directionNatural;
  private JCheckBox toFront;
  private JCheckBox splitWays;
  private JCheckBox createNode;
  private JCheckBox detachEnabled;
  
  public KindaHackedInUtilsPreferences() {
    super("detach", "KindaHackedInUtils", tr("Change settings for KindaHackedInUtils plugin."));
    
    directionEnabled = new JCheckBox(tr("Direction from mouse location with keyboard key H"), Conf.isDirectionEnabled());
    directionObjectSpecific = new JCheckBox(tr("Use key {0} for traffic signs on nodes inside a way","traffic_sign:direction"), Conf.isObjectSpecificDirection());
    directionSimple = new JCheckBox(tr("Use {0} as direction for traffic signs on nodes inside a way","forward/backward"), Conf.isSimpleDirection());
    directionNatural = new JCheckBox(tr("Natural direction for traffic signs"), Conf.isNaturalDirection());
    
    splitWays = new JCheckBox(tr("Split ways at mouse location with keyboard key K"), Conf.isSplitWay());
    createNode = new JCheckBox(tr("Create new node at mouse location with keyboard key B"), Conf.isCreateNode());
    detachEnabled = new JCheckBox(tr("Detach nodes from ways and move them with keyboard key Z"), Conf.isDetachEnabled());
    
    toFront = new JCheckBox(tr("Get JOSM to front whenever mouse enters map view or dialog window (might not work depending on OS and window manager)"), Conf.isToFront());
    
    directionObjectSpecific.setEnabled(directionEnabled.isSelected());
    directionSimple.setEnabled(directionEnabled.isSelected() && !directionObjectSpecific.isSelected());
    directionNatural.setEnabled(directionEnabled.isSelected());
    
    directionEnabled.addItemListener(e -> {
      directionObjectSpecific.setEnabled(e.getStateChange() == ItemEvent.SELECTED);
      directionSimple.setEnabled(directionObjectSpecific.isEnabled() && !directionObjectSpecific.isSelected());
      directionNatural.setEnabled(directionObjectSpecific.isEnabled());
    });
    
    directionObjectSpecific.addItemListener(e -> {
      directionSimple.setEnabled(e.getStateChange() == ItemEvent.DESELECTED);
    });
  }
  
  @Override
  public void addGui(PreferenceTabbedPane gui) {
    JPanel p = new JPanel(new GridBagLayout());
    p.setBorder(BorderFactory.createEmptyBorder(5, 20, 5, 5));
    
    p.add(toFront, GBC.std().fill(GBC.HORIZONTAL));
    p.add(detachEnabled, GBC.std(0, 2));
    p.add(createNode, GBC.std(0, 3));
    p.add(splitWays, GBC.std(0, 4));
    p.add(directionEnabled, GBC.std(0, 5));
    
    GridBagConstraints gc = GBC.std(0, 6);
    gc.insets.left = 20;
    
    p.add(directionObjectSpecific, gc);
    gc.gridy = 7;
    p.add(directionSimple, gc);
    gc.gridy = 8;
    p.add(directionNatural, gc);
    
    p.add(GBC.glue(0, 10), GBC.std(0, 9).fill());
    
    createPreferenceTabWithScrollPane(gui, p);
  }

  @Override
  public boolean ok() {
    Conf.setToFront(toFront.isSelected());
    Conf.setDetachEnabled(detachEnabled.isSelected());
    Conf.setCreateNode(createNode.isSelected());
    Conf.setSplitWay(splitWays.isSelected());
    Conf.setDirectionEnabled(directionEnabled.isSelected());
    Conf.setObjectSpecificDirection(directionObjectSpecific.isSelected());
    Conf.setSimpleDirection(directionSimple.isSelected());
    Conf.setNaturalDirection(directionNatural.isSelected());
    
    return false;
  }

}
