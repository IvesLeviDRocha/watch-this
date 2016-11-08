package com.weebly.niseishun.watchthis.controllers;

import java.util.concurrent.atomic.AtomicInteger;

public class RequestManager {

  private static int requestLimit = 6;
  private static AtomicInteger requests;
  private static long refreshTime = 400;

  public static void init() {
    requests = new AtomicInteger(0);
    Thread refresher = new Thread(new Runnable() {
      public void run() {
        while (true) {
          boolean slept = false;
          while (!slept) {
            try {
              Thread.sleep(refreshTime);
              slept = true;
            } catch (InterruptedException e) {
              // did not sleep, try again
            }
          }
          if (requests.get() > 0) {
            requests.decrementAndGet();
          }
        }
      }
    });
    refresher.start();
  }

  public static boolean requestPermission() {
    boolean timeout = false;
    long tries = 0;
    while (!timeout) {
      if (requests.get() < requestLimit) {
        requests.incrementAndGet();
        return true;
      } else {
        if (tries > 100) {
          // exception?
          timeout = true;
          break;
        }
        boolean slept = false;
        while (!slept) {
          try {
            Thread.sleep(200);
            slept = true;
          } catch (InterruptedException e) {
            // did not sleep, try again
          }
        }
        tries++;
      }
    }
    return false;
  }


}
