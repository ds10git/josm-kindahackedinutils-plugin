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
  
  static final String TO_FRONT = "kindahackedinutils.toFront";

  public static boolean isDirectionEnabled() {
    return Config.getPref().getBoolean(DIRECTION_ENABLED, true);
  }
  
  static void setDirectionEnabled(boolean value) {
    Config.getPref().putBoolean(DIRECTION_ENABLED, value);
  }
  
  public static boolean isDirectionHelperLineEnabled() {
    return Config.getPref().getBoolean(DIRECTION_HELPER_LINE_ENABLED, true);
  }
  
  static void setDirectionHelperLineEnabled(boolean value) {
    Config.getPref().putBoolean(DIRECTION_HELPER_LINE_ENABLED, value);
  }
  
  public static boolean isDirectionHelperConeEnabled() {
    return Config.getPref().getBoolean(DIRECTION_HELPER_CONE_ENABLED, true);
  }
  
  static void setDirectionHelperConeEnabled(boolean value) {
    Config.getPref().putBoolean(DIRECTION_HELPER_CONE_ENABLED, value);
  }
  
  public static boolean isDirectionArrowForSimpleDirectionEnabled() {
    return Config.getPref().getBoolean(DIRECTION_ARROW_FOR_SIMPLE_DIRECTION, true);
  }
  
  static void setDirectionArrowForSimpleDirection(boolean value) {
    Config.getPref().putBoolean(DIRECTION_ARROW_FOR_SIMPLE_DIRECTION, value);
  }
  
  public static boolean isSimpleDirection() {
    return Config.getPref().getBoolean(DIRECTION_SIMPLE, true);
  }
  
  static void setSimpleDirection(boolean value) {
    Config.getPref().putBoolean(DIRECTION_SIMPLE, value);
  }
  
  public static boolean isObjectSpecificDirection() {
    return Config.getPref().getBoolean(DIRECTION_OBJECT_SPECIFIC, false);
  }

  public static boolean isNaturalDirection() {
    return Config.getPref().getBoolean(DIRECTION_NATURAL, true);
  }
  
  static void setNaturalDirection(boolean value) {
    Config.getPref().putBoolean(DIRECTION_NATURAL, value);
  }
  
  static void setObjectSpecificDirection(boolean value) {
    Config.getPref().putBoolean(DIRECTION_OBJECT_SPECIFIC, value);
  }
  
  public static boolean isToFront() {
    return Config.getPref().getBoolean(TO_FRONT, false);
  }
  
  static void setToFront(boolean value) {
    Config.getPref().putBoolean(TO_FRONT, value);
  }
  
  public static boolean isCreateNode() {
    return Config.getPref().getBoolean(CREATE_NODE, true);
  }
  
  static void setCreateNode(boolean value) {
    Config.getPref().putBoolean(CREATE_NODE, value);
  }
  
  public static boolean isSplitWay() {
    return Config.getPref().getBoolean(SPLIT_WAY, true);
  }
  
  static void setSplitWay(boolean value) {
    Config.getPref().putBoolean(SPLIT_WAY, value);
  }

  public static boolean isDetachEnabled() {
    return Config.getPref().getBoolean(DETACH_ENABLED, true);
  }
  
  static void setDetachEnabled(boolean value) {
    Config.getPref().putBoolean(DETACH_ENABLED, value);
  }
  
  public static boolean isAutoSetEnabled() {
    return Config.getPref().getBoolean(DIRECTION_AUTO_SET, true);
  }
  
  static void setAutoSetEnabled(boolean value) {
    Config.getPref().putBoolean(DIRECTION_AUTO_SET, value);
  }
  
  public static boolean isShowPopupEnabled() {
    return Config.getPref().getBoolean(DIRECTION_SHOW_POPUP, true);
  }
  
  static void setShowPopupEnabled(boolean value) {
    Config.getPref().putBoolean(DIRECTION_SHOW_POPUP, value);
  }
  
  public static boolean isOppositeSimpleDirectionEnabled() {
    return Config.getPref().getBoolean(DIRECTION_OPPOSITE_SIMPLE, false);
  }
  
  static void setDirectionFromNodeForWaysEnabled(boolean value) {
    Config.getPref().putBoolean(DIRECTION_FROM_NODE_FOR_WAYS, value);
  }
  
  public static boolean isDirectionFromNodeForWaysEnabled() {
    return Config.getPref().getBoolean(DIRECTION_FROM_NODE_FOR_WAYS, true);
  }
}

