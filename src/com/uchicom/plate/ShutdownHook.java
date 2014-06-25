/**
 * (c) 2013 uchicom
 */
package com.uchicom.plate;

/**
 * シャットダウンフックスレッド.
 * @author uchicom: Shigeki Uchiyama
 *
 */
public class ShutdownHook extends Thread {

    /** 一石 */
    private Main plate;
    
    /**
     * 
     * @param isskei
     */
    public ShutdownHook(Main plate) {
        this.plate = plate;
    }
   
    @Override
    public void run() {
        plate.shutdown();
    }
}
