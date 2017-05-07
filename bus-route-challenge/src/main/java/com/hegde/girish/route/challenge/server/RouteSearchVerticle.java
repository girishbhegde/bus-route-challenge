package com.hegde.girish.route.challenge.server;

import com.hegde.girish.route.challenge.model.RouteDetails;
import com.hegde.girish.route.challenge.service.RouteSearchService;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.eventbus.MessageConsumer;
import io.vertx.core.json.Json;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by Girish on 03-05-2017.
 */
public class RouteSearchVerticle extends AbstractVerticle{
    private static Logger LOGGER = LoggerFactory.getLogger(RouteSearchVerticle.class);
    public static final String SEARCH_VERTICLE = "SEARCH_VERTICLE";
    private RouteSearchService service = RouteSearchService.getInstance();

    @Override
    public void start(){
        MessageConsumer<String> consumer = vertx.eventBus().consumer(SEARCH_VERTICLE);
        consumer.handler(message -> {
            LOGGER.debug("new search request received");
            message.reply(Json.encode(searchForDirectRoute(Json.decodeValue(message.body(), RouteDetails.class))));
        });
    }


    public RouteDetails searchForDirectRoute(RouteDetails req){
        return service.searchForDirectRoute(req);
    }
}
