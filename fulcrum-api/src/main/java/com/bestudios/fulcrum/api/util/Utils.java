package com.bestudios.fulcrum.api.util;

public class Utils {

  public static int compareVersions(String v1, String v2) {
    int i = 0, j = 0;
    int len1 = v1.length(), len2 = v2.length();

    while (i < len1 || j < len2) {
      int num1 = 0, num2 = 0;

      // Extract numeric part from v1
      while (i < len1 && v1.charAt(i) != '.') {
        num1 = num1 * 10 + (v1.charAt(i) - '0');
        i++;
      }

      // Extract numeric part from v2
      while (j < len2 && v2.charAt(j) != '.') {
        num2 = num2 * 10 + (v2.charAt(j) - '0');
        j++;
      }

      // Compare current parts
      if (num1 > num2) return 1;
      if (num1 < num2) return -1;

      // Skip the dot and move to next part
      i++;
      j++;
    }

    return 0;
  }

}
