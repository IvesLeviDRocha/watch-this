package com.weebly.niseishun.watchthis.controllers;

import java.util.concurrent.atomic.AtomicInteger;

public class RequestManager {

  private static int requestLimit = 3;
  private static int browserRequestLimit = 600;
  private static AtomicInteger requests;
  private static AtomicInteger browserRequests;
  private static long refreshTime = 500;
  private static int counterToBrowserRefresh = 2;
  private static int maxCounterToBrowserRefresh = 3;

  public static final int BROWSER = 0;
  public static final int MALAPI = 1;

  public static void init() {
    requests = new AtomicInteger(0);
    browserRequests = new AtomicInteger(0);
    Thread refresher = createRefresherThread();
    refresher.start();
  }

  private static Thread createRefresherThread() {
    Thread refresher = new Thread(new Runnable() {
      public void run() {
        runRefreshCycle();
      }

      private void runRefreshCycle() {
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
          if (browserRequests.get() > 0 && counterToBrowserRefresh <= 0) {
            browserRequests.set(0);
            counterToBrowserRefresh = maxCounterToBrowserRefresh;
          }
          counterToBrowserRefresh--;
        }
      }
    });
    return refresher;
  }

  public static boolean requestPermission(int category) {
    boolean permission;
    switch (category) {
      case BROWSER:
        permission = browserRequest();
        return permission;
      case MALAPI:
        permission = malAPIRequest();
        return permission;
      default:
        return false;

    }

  }

  private static boolean malAPIRequest() {
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
  }

  private static boolean browserRequest() {
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
  }


}
