package com.hegde.girish.route.challenge.service;

/**
 * Created by Girish on 03-05-2017.
 */
public class FileWatchThread implements Runnable {
    @Override
    public void run() {
        RouteSearchService.getInstance().watchForFileUpdates();
    }
}
