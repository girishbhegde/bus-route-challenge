package com.hegde.girish.route.challenge.model;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Created by Girish on 03-05-2017.
 */
public class RouteDetails {
    @JsonProperty("dep_sid")
    int depStationId;

    @JsonProperty("arr_sid")
    int arrivalStationId;

    @JsonProperty("direct_bus_route")
    boolean isDirect_bus_route;

    public RouteDetails(){}

    public RouteDetails(int depStationId, int arrivalStationId, boolean isDirect_bus_route) {
        this.depStationId = depStationId;
        this.arrivalStationId = arrivalStationId;
        this.isDirect_bus_route = isDirect_bus_route;
    }

    public int getDepStationId() {
        return depStationId;
    }

    public void setDepStationId(int depStationId) {
        this.depStationId = depStationId;
    }

    public int getArrivalStationId() {
        return arrivalStationId;
    }

    public void setArrivalStationId(int arrivalStationId) {
        this.arrivalStationId = arrivalStationId;
    }

    public boolean isDirect_bus_route() {
        return isDirect_bus_route;
    }

    public void setDirect_bus_route(boolean direct_bus_route) {
        isDirect_bus_route = direct_bus_route;
    }
}
