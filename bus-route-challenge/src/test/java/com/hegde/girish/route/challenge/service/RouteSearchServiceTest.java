package com.hegde.girish.route.challenge.service;

import com.hegde.girish.route.challenge.model.RouteDetails;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Created by ghegde on 5/6/17.
 */
public class RouteSearchServiceTest {
    RouteSearchService testClass;

    @Before
    public void init(){
        testClass = RouteSearchService.getInstance();
    }

    @Test
    public void testSearchForDirectRoute(){
        RouteDetails route = new RouteDetails(1,2,false);
        Assert.assertFalse(testClass.searchForDirectRoute(route).isDirect_bus_route());
    }
}
