// (C) 2012 uchicom
package com.uchicom.plate.util;

import java.nio.charset.StandardCharsets;

/**
 * 暗号化クラス キーを元にテキストの暗号化を行う
 *
 * @author Uchiyama Shigeki
 */
public class Crypt {

  public static byte[] encrypt3(String key, String text) {
    byte[] keyBytes = key.getBytes(StandardCharsets.US_ASCII);
    byte[] textBytes = text.getBytes(StandardCharsets.US_ASCII);
    for (byte keyByte : keyBytes) {
      enc((keyByte >> 4) & 0xF, textBytes);
      enc(keyByte & 0xF, textBytes);
    }

    return textBytes;
  }

  public static void enc(int prog, byte[] bytes) {
    int right = 0;
    if ((prog & 0x8) == 0) {
      // 1bit右循環シフト
      right = 1;
    } else {
      // 2bit右循環シフト
      right = 2;
    }
    byte temp = bytes[bytes.length - 1];
    for (int i = 0; i < bytes.length; i++) {
      int value = ((bytes[i] & 0xFF) >> right);
      value = value | ((temp << (8 - right)) & 0xFF);
      temp = bytes[i];
      bytes[i] = Integer.valueOf(value).byteValue();
    }
    int bit = 1 << ((prog & 0x7) + 1);
    for (int i = 0; i < bytes.length; i++) {
      int value = bytes[i] & bit;
      if (value == 0) {
        bytes[i] = Integer.valueOf(bytes[i] | bit).byteValue();
      } else {
        bytes[i] = Integer.valueOf(bytes[i] - bit).byteValue();
      }
    }
  }
}
