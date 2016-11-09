package com.weebly.niseishun.watchthis.controllers;

import java.util.concurrent.atomic.AtomicInteger;

public class RequestManager {

  private static int requestLimit = 3;
  private static int browserRequestLimit = 30;
  private static AtomicInteger requests;
  private static AtomicInteger browserRequests;
  private static long refreshTime = 500;

  public static final int BROWSER = 0;
  public static final int MALAPI = 1;

  public static void init() {
    requests = new AtomicInteger(0);
    browserRequests = new AtomicInteger(0);
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
          if (browserRequests.get() > 0) {
            browserRequests.set(0);
          }
        }
      }
    });
    refresher.start();
  }

  public static boolean requestPermission(int category) {
    switch (category) {
      case BROWSER:
        while (true) {
          if (browserRequests.get() < browserRequestLimit) {
            browserRequests.incrementAndGet();
            return true;
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
        }
      case MALAPI:
        while (true) {
          if (requests.get() < requestLimit) {
            requests.incrementAndGet();
            return true;
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
        }
      default:
        return false;

    }

  }


}
