// (C) 2012 uchicom
package com.uchicom.plate.util;

/** @author Uchiyama Shigeki */
public class Base64 {

  public static String encode(byte[] bytes) {
    int amari = bytes.length % 3;
    int bufferSize = bytes.length * 4 / 3;
    if (amari > 0) {
      bufferSize += 4;
    }
    StringBuffer strBuff = new StringBuffer(bufferSize);
    for (int i = 0; i < bytes.length - amari; i += 3) {
      // 1  11111100 00000000 00000000
      strBuff.append(getBase64Char((bytes[i] >> 2) & 0x3F));
      // 2  00000011 11110000 00000000
      strBuff.append(getBase64Char(((bytes[i] << 4) & 0x30) | ((bytes[i + 1] >> 4) & 0x0F)));
      // 3  00000000 00001111 11000000
      strBuff.append(getBase64Char(((bytes[i + 1] << 2) & 0x3C) | ((bytes[i + 2] >> 6) & 0x03)));
      // 4  00000000 00000000 00111111
      strBuff.append(getBase64Char(bytes[i + 2] & 0x3F));
    }

    int index = bytes.length - amari;
    switch (amari) {
      case 1:
        // 1  11111100
        strBuff.append(getBase64Char((bytes[index] >> 2) & 0x3F));
        // 2  00000011 0000
        strBuff.append(getBase64Char((bytes[index] << 4) & 0x30));
        strBuff.append("==");
        break;
      case 2:
        // 1  11111100 00000000 00000000
        strBuff.append(getBase64Char((bytes[index] >> 2) & 0x3F));
        // 2  00000011 11110000 00000000
        strBuff.append(
            getBase64Char(((bytes[index] << 4) & 0x30) | ((bytes[index + 1] >> 4) & 0x0F)));
        // 3  00000000 00001111 11000000
        strBuff.append(getBase64Char((bytes[index + 1] << 2) & 0x3C));
        strBuff.append('=');
        break;
      default:
        // 0は処理が終わってるので、何もしない。
    }

    // 文字に変換
    return strBuff.toString();
  }

  public static byte[] decode(String text) {
    char[] chs = text.toCharArray();
    int maxLength = text.length();
    if (text.endsWith("=")) {
      maxLength -= 4;
    }
    byte[] bytes = null;
    // あまりを先に処理する。
    if (maxLength != text.length()) {
      if (text.endsWith("==")) {
        // "=="1byte
        bytes = new byte[maxLength / 4 * 3 + 1];
        int b1 = getBase64Code(chs[chs.length - 4]);
        int b2 = getBase64Code(chs[chs.length - 3]);
        // 1 00111111 00110000
        bytes[bytes.length - 1] = new Integer(((b1 << 2) & 0xFC) | ((b2 >> 4) & 0x03)).byteValue();
      } else {
        // "="2byte
        bytes = new byte[maxLength / 4 * 3 + 2];
        int b1 = getBase64Code(chs[chs.length - 4]);
        int b2 = getBase64Code(chs[chs.length - 3]);
        int b3 = getBase64Code(chs[chs.length - 2]);
        // 1 00111111 00110000 00000000
        bytes[bytes.length - 2] = new Integer(((b1 << 2) & 0xFC) | ((b2 >> 4) & 0x03)).byteValue();

        // 2 00000000 00001111 00111100
        bytes[bytes.length - 1] = new Integer(((b2 << 4) & 0xF0) | ((b3 >> 2) & 0x0F)).byteValue();
      }
    } else {
      bytes = new byte[maxLength / 4 * 3];
    }
    int index = 0;
    for (int i = 0; i < maxLength; i += 4) {
      int b1 = getBase64Code(chs[i]);
      int b2 = getBase64Code(chs[i + 1]);
      int b3 = getBase64Code(chs[i + 2]);
      int b4 = getBase64Code(chs[i + 3]);
      // 1 00111111 00110000 00000000 00000000
      bytes[index] = new Integer(((b1 << 2) & 0xFC) | ((b2 >> 4) & 0x03)).byteValue();
      // 2 00000000 00001111 00111100 00000000
      bytes[index + 1] = new Integer(((b2 << 4) & 0xF0) | ((b3 >> 2) & 0x0F)).byteValue();
      // 3 00000000 00000000 00000011 00111111
      bytes[index + 2] = new Integer(((b3 << 6) & 0xC0) | (b4 & 0x3F)).byteValue();
      index += 3;
    }
    return bytes;
  }

  public static char getBase64Char(int value) {
    char ch = 0;
    if (value < 26) {
      // A-Z
      ch = (char) ((int) 'A' + value);
    } else if (value < 52) {
      // a-z
      ch = (char) ((int) 'a' + value - 26);
    } else if (value < 62) {
      // 0-9
      ch = (char) ((int) '0' + value - 52);
    } else if (value == 62) {
      ch = '+';
    } else if (value == 63) {
      ch = '/';
    } else {
      throw new IllegalArgumentException();
    }
    return ch;
  }

  public static int getBase64Code(char ch) {
    int value = 0;
    if (ch >= 'a') {
      value = ch - 'a' + 26;
    } else if (ch >= 'A') {
      value = ch - 'A';
    } else if (ch >= '0') {
      value = ch - '0' + 52;
    } else if (ch == '+') {
      value = 62;
    } else if (ch == '/') {
      value = 63;
    } else {
      throw new IllegalArgumentException();
    }
    return value;
  }
}
