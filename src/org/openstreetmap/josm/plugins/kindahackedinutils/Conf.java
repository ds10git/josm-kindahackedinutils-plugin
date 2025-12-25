// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.kindahackedinutils;

import org.openstreetmap.josm.spi.preferences.Config;

public class Conf {
  static final String DIRECTION_ENABLED = "kindahackedinutils.directionEnabled";
  static final String DIRECTION_OBJECT_SPECIFIC = "kindahackedinutils.objectSpecificDirection";
  static final String DIRECTION_SIMPLE = "kindahackedinutils.simpleDirection";
  static final String DIRECTION_NATURAL = "kindahackedinutils.naturalDirection";
  static final String DIRECTION_AUTO_SET = "kindahackedinutils.autoSet";
  static final String DIRECTION_FROM_NODE_FOR_WAYS = "kindahackedinutils.directionFromNodeForWays";
  static final String DIRECTION_SHOW_POPUP = "kindahackedinutils.showPopup";
  static final String DIRECTION_OPPOSITE_SIMPLE = "kindahackedinutils.oppositeSimpleDirection";
  static final String DIRECTION_HELPER_LINE_ENABLED = "kindahackedinutils.directionHelperLineEnabled";
  static final String DIRECTION_HELPER_CONE_ENABLED = "kindahackedinutils.directionHelperConeEnabled";
  static final String DIRECTION_ARROW_FOR_SIMPLE_DIRECTION = "kindahackedinutils.directionArrowForSimpleDirection";
  
  static final String CREATE_NODE = "kindahackedinutils.createNode";
  static final String SPLIT_WAY = "kindahackedinutils.splitWay";
  static final String DETACH_ENABLED = "kindahackedinutils.detachEnabled";
  static final String OPTIMIZE_SPLIT_MULTIPOLYGON_ENABLED = "kindahackedinutils.fixSplitMultipolygon";
  static final String WRAP_AROUND_ENABLED = "kindahackedinutils.wrapAroundEnabled";
  static final String CREATE_AREA_ENABLED = "kindahackedinutils.createAreaEnabled";
  
  static final String TO_FRONT = "kindahackedinutils.toFront";

  static void setValue(String key, boolean value) {
    Config.getPref().putBoolean(key, value);
  }
  
  public static boolean isDirectionEnabled() {
    return Config.getPref().getBoolean(DIRECTION_ENABLED, true);
  }
  
  public static boolean isDirectionHelperLineEnabled() {
    return Config.getPref().getBoolean(DIRECTION_HELPER_LINE_ENABLED, true);
  }
  
  public static boolean isDirectionHelperConeEnabled() {
    return Config.getPref().getBoolean(DIRECTION_HELPER_CONE_ENABLED, true);
  }
  
  public static boolean isDirectionArrowForSimpleDirectionEnabled() {
    return Config.getPref().getBoolean(DIRECTION_ARROW_FOR_SIMPLE_DIRECTION, true);
  }
  
  public static boolean isSimpleDirection() {
    return Config.getPref().getBoolean(DIRECTION_SIMPLE, true);
  }
  
  public static boolean isObjectSpecificDirection() {
    return Config.getPref().getBoolean(DIRECTION_OBJECT_SPECIFIC, false);
  }

  public static boolean isNaturalDirection() {
    return Config.getPref().getBoolean(DIRECTION_NATURAL, true);
  }
  
  public static boolean isToFront() {
    return Config.getPref().getBoolean(TO_FRONT, false);
  }
  
  public static boolean isCreateNode() {
    return Config.getPref().getBoolean(CREATE_NODE, true);
  }
  
  public static boolean isSplitWay() {
    return Config.getPref().getBoolean(SPLIT_WAY, true);
  }
  
  public static boolean isDetachEnabled() {
    return Config.getPref().getBoolean(DETACH_ENABLED, true);
  }
  
  public static boolean isAutoSetEnabled() {
    return Config.getPref().getBoolean(DIRECTION_AUTO_SET, true);
  }
  
  public static boolean isShowPopupEnabled() {
    return Config.getPref().getBoolean(DIRECTION_SHOW_POPUP, true);
  }
  
  public static boolean isOppositeSimpleDirectionEnabled() {
    return Config.getPref().getBoolean(DIRECTION_OPPOSITE_SIMPLE, false);
  }
  
  public static boolean isDirectionFromNodeForWaysEnabled() {
    return Config.getPref().getBoolean(DIRECTION_FROM_NODE_FOR_WAYS, true);
  }
    
  public static boolean isOptimizeSplitMultipolygonEnabled() {
    return Config.getPref().getBoolean(OPTIMIZE_SPLIT_MULTIPOLYGON_ENABLED, true);
  }
    
  public static boolean isWrapAroundEnabled() {
    return Config.getPref().getBoolean(WRAP_AROUND_ENABLED, true);
  }
  
  public static boolean isCreateAreaEnabled() {
    return Config.getPref().getBoolean(CREATE_AREA_ENABLED, true);
  }
}

