package com.hegde.girish.route.challenge.service;

import com.hegde.girish.route.challenge.model.RouteDetails;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.*;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.nio.file.StandardWatchEventKinds.ENTRY_DELETE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_MODIFY;

/**
 * Created by Girish on 02-05-2017.
 */
public class RouteSearchService {
    private static Logger LOGGER = LoggerFactory.getLogger(RouteSearchService.class);
    private static RouteSearchService service = new RouteSearchService();
    private int routeSize = 0;
    private Map<Integer, List<String>> routeList = new HashMap();
    private String path;

    private RouteSearchService(){}

    public static RouteSearchService getInstance(){
        return service;
    }

    public RouteDetails searchForDirectRoute(RouteDetails req){
        for(Map.Entry<Integer, List<String>> route : routeList.entrySet()){
            int depIndex = route.getValue().indexOf(String.valueOf(req.getDepStationId()));
            int arrIndex = route.getValue().indexOf(String.valueOf(req.getArrivalStationId()));
            if(depIndex > -1 && arrIndex > -1 && depIndex < arrIndex){
                req.setDirect_bus_route(true);
                break;
            }
        }
        return req;
    }

    public void updateRouteTable(String path) throws IOException {
        LOGGER.info("updating route list");
        this.path = path;
        Map<Integer, List<String>> newRouteList = new HashMap();
        try(BufferedReader br = new BufferedReader(new FileReader(path))){
            routeSize = Integer.parseInt(br.readLine().trim());
            for(int i = 0; i<routeSize ; i++){
                String line = br.readLine();
                if(StringUtils.isBlank(line)){
                    LOGGER.error("Invalid data file");
                    throw new IllegalArgumentException("Invalid data file");
                }
                int key = Integer.parseInt(StringUtils.substringBefore(line, " "));
                List<String> value = Arrays.asList(StringUtils.substringAfter(line, " ").split(" "));
                newRouteList.put(key, value);
            }
            routeList = newRouteList;
        }catch (RuntimeException e){
            LOGGER.error("Invalid File");
            throw e;
        }
    }


    public void watchForFileUpdates(){
        try {
            while(true){
                WatchService watcher = FileSystems.getDefault().newWatchService();
                Path dir = Paths.get(StringUtils.substringBeforeLast(path, "/"));
                dir.register(watcher, ENTRY_DELETE, ENTRY_MODIFY);

                    WatchKey key;
                    try {
                        key = watcher.take();
                    } catch (InterruptedException ex) {
                        LOGGER.error(ex.getMessage(), ex);
                        return;
                    }
                    for (WatchEvent<?> event : key.pollEvents()) {
                        WatchEvent.Kind<?> kind = event.kind();

                        WatchEvent<Path> ev = (WatchEvent<Path>) event;
                        Path fileName = ev.context();

                        if (kind == ENTRY_MODIFY && fileName.toString().equals(StringUtils.substringAfterLast(path, "/").trim())) {
                            updateRouteTable(path);
                            break;
                        }
                    }
            }
        } catch (IOException e) {
            LOGGER.error(e.getMessage(), e);
            return;
        }
    }
}
