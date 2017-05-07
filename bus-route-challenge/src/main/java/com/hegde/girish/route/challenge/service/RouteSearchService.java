package com.hegde.girish.route.challenge.service;

import com.hegde.girish.route.challenge.model.RouteDetails;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.*;
import java.util.*;

import static java.nio.file.StandardWatchEventKinds.ENTRY_MODIFY;

/**
 * Created by Girish on 02-05-2017.
 */
public class RouteSearchService {
    public static final String WHITESPACE = " ";
    public static final String FORWARD_SLASH = "/";
    private static Logger LOGGER = LoggerFactory.getLogger(RouteSearchService.class);
    private static RouteSearchService service = new RouteSearchService();
    private int routeSize = 0;
    private Map<Integer, Set<Integer>> routeMap = new HashMap();
    private String path;

    private RouteSearchService(){}

    public static RouteSearchService getInstance(){
        return service;
    }

    public RouteDetails searchForDirectRoute(RouteDetails req){
        //get the direct station list for dep_sid
        Set<Integer> directStations =  routeMap.get(req.getDepStationId());
        if(directStations!=null && directStations.contains(req.getArrivalStationId())){
            req.setDirect_bus_route(true);
        }
        return req;
    }

    /**
     * This operation reads a file and puts the station data into map.
     * The data is organized such a way that key of map is origin station id and
     * value contains  station ids that can be reached from origin directly
     * @param path
     * @throws IOException
     */
    public void updateRouteTable(String path) throws IOException {
        LOGGER.info("updating route list");
        this.path = path;
        Map<Integer, Set<Integer>> newRouteMap = new HashMap();

        try(BufferedReader br = new BufferedReader(new FileReader(path))){
            routeSize = Integer.parseInt(br.readLine().trim());
            for(int lineNo = 0; lineNo<routeSize ; lineNo++){
                String line = br.readLine();
                if(StringUtils.isBlank(line)){
                    LOGGER.error("Invalid data file");
                    throw new IllegalArgumentException("Invalid data file");
                }

                //read route as a string/line and convert into list
                List<String> stationList = Arrays.asList(StringUtils.substringAfter(line, WHITESPACE).split(WHITESPACE));

                //add each station id as key and value as direct route stations in a list
                //don't add last station id as it has no direct route
                for(int j=0; j<stationList.size() - 1; j++){
                    int originSid = Integer.parseInt(stationList.get(j));
                    Set<Integer> stations = newRouteMap.get(originSid);
                    if(stations == null){
                        stations = new HashSet<>();
                    }

                    for(int k = j+1; k<stationList.size(); k++){
                        stations.add(Integer.parseInt(stationList.get(k)));
                    }
                    newRouteMap.put(originSid, stations);
                }
            }
            routeMap = newRouteMap;
        }catch (RuntimeException e){
            LOGGER.error("Invalid File");
            throw e;
        }
    }


    /**
     * This method keeps watching a file for any modifications and
     * reloads the routeMap for each modification
     * This needs to be run in a separate thread
     */
    public void watchForFileUpdates(){
        try {
            while(true){
                //create a watch
                WatchService watcher = FileSystems.getDefault().newWatchService();
                Path dir = Paths.get(StringUtils.substringBeforeLast(path, FORWARD_SLASH));
                dir.register(watcher, ENTRY_MODIFY);

                    WatchKey key;
                    try {
                        //wait for modification
                        key = watcher.take();
                    } catch (InterruptedException ex) {
                        LOGGER.error(ex.getMessage(), ex);
                        return;
                    }
                    for (WatchEvent<?> event : key.pollEvents()) {
                        WatchEvent.Kind<?> kind = event.kind();

                        WatchEvent<Path> ev = (WatchEvent<Path>) event;
                        Path fileName = ev.context();

                        //if change is modify and its for same file, call update Route method
                        if (kind == ENTRY_MODIFY && fileName.toString().equals(StringUtils.substringAfterLast(path, FORWARD_SLASH).trim())) {
                            updateRouteTable(path);
                            break;
                        }
                    }
            }
        } catch (IOException e) {
            LOGGER.error(e.getMessage(), e);
        }
    }
}
