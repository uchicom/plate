// (C) 2012 uchicom
package com.uchicom.plate.util;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;

/**
 * 暗号化クラス キーを元にテキストの暗号化を行う
 *
 * @author Uchiyama Shigeki
 */
public class Crypt {

  public static String encrypt(String key, String text) {
    int lock = 0;
    for (char ch : key.toCharArray()) {
      lock += ch;
    }
    int crypt = 0;
    StringBuffer strBuff = new StringBuffer();
    for (char ch : text.toCharArray()) {
      crypt = ch + lock;
      crypt = 0x21 + crypt % (0x7E - 0x21);
      strBuff.append((char) crypt);
      lock = lock >> 1;
    }

    return strBuff.toString();
  }

  public static String encrypt2(String key, String text)
      throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException,
          IllegalBlockSizeException, BadPaddingException {
    SecretKeySpec spec = new SecretKeySpec(key.getBytes(), "AES");
    Cipher cipher = Cipher.getInstance("AES");
    cipher.init(Cipher.ENCRYPT_MODE, spec);
    byte[] encrypted = cipher.doFinal(text.getBytes());
    return Base64.encode(encrypted);
  }

  public static String decrypt2(String key, String code)
      throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException,
          IllegalBlockSizeException, BadPaddingException {
    SecretKeySpec spec = new SecretKeySpec(key.getBytes(), "AES");
    Cipher cipher = Cipher.getInstance("AES");
    cipher.init(Cipher.DECRYPT_MODE, spec);
    byte[] encrypted = cipher.doFinal(Base64.decode(code));
    return new String(encrypted);
  }

  public static byte[] encrypt3(String key, String text) {
    byte[] keyBytes = key.getBytes();
    byte[] textBytes = text.getBytes();
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

  public static String decrypt3(String key, byte[] bytes) {
    byte[] keyBytes = key.getBytes();
    for (int i = keyBytes.length - 1; i >= 0; i--) {
      dec(keyBytes[i] & 0xF, bytes);
      dec((keyBytes[i]) >> 4 & 0xF, bytes);
    }

    return new String(bytes);
  }

  public static void dec(int prog, byte[] bytes) {

    // ここより下はencと同じだな。順番が違うな。
    int bit = 1 << ((prog & 0x7) + 1);
    for (int i = 0; i < bytes.length; i++) {
      int value = bytes[i] & bit;
      if (value == 0) {
        bytes[i] = Integer.valueOf(bytes[i] | bit).byteValue();
      } else {
        bytes[i] = Integer.valueOf(bytes[i] - bit).byteValue();
      }
    }

    int left = 0;
    if ((prog & 0x8) == 0) {
      // 1bit左循環シフト
      left = 1;
    } else {
      // 2bit左循環シフト
      left = 2;
    }
    byte temp = bytes[0];
    for (int i = bytes.length - 1; i >= 0; i--) {
      int value = (bytes[i] << left) & 0xFF;
      value = value | ((temp & 0xFF) >> (8 - left));
      temp = bytes[i];
      bytes[i] = Integer.valueOf(value).byteValue();
    }
  }
}
