package com.hegde.girish.route.challenge.server;

import com.hegde.girish.route.challenge.model.RouteDetails;
import com.hegde.girish.route.challenge.service.FileWatchThread;
import com.hegde.girish.route.challenge.service.RouteSearchService;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.Message;
import io.vertx.core.http.HttpServer;
import io.vertx.core.json.Json;
import io.vertx.ext.web.Router;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * Created by ghegde on 5/6/17.
 */
public class ApplicationServer extends AbstractVerticle{
    public static final String APPLICATION_SHUT_DOWN = "Application shut down";
    private static Logger LOGGER = LoggerFactory.getLogger(ApplicationServer.class);
    private static int port = 8088;

    @Override
    public void start() throws Exception {
        LOGGER.debug("Application Starting");
        //deploy the verticles
        vertx.deployVerticle(RouteSearchVerticle.class.getCanonicalName());
        LOGGER.debug("Verticles deployed");

        Router router = Router.router(vertx);

        //add router for statistics
        router.get("/api/direct").handler(routingContext -> {
            String dep_sid = routingContext.request().getParam("dep_sid");
            String arr_sid = routingContext.request().getParam("arr_sid");
            RouteDetails request;
            try {
                request = new RouteDetails(Integer.parseInt(dep_sid), Integer.parseInt(arr_sid), false);
            }catch(NumberFormatException e){
                LOGGER.warn("Invalid input");
                routingContext.response().setStatusCode(400).end();
                return;
            }
            vertx.eventBus().send(RouteSearchVerticle.SEARCH_VERTICLE, Json.encode(request), res->{
                Message<Object> result = res.result();
                if (res.failed() || result == null) {
                    routingContext.response().setStatusCode(500);
                }else{
                    routingContext.response().setStatusCode(200);
                    routingContext.response().end(result.body().toString());
                }
            });
        });

        //start http server
        HttpServer server  = vertx.createHttpServer();
        server.requestHandler(router::accept);
        server.listen(port);
        LOGGER.info("Application Started");
    }

    public static void main(String[] args) {
        //read file path
        if(args.length < 1){
            LOGGER.error("Please provide data file path as program argument");
            LOGGER.error(APPLICATION_SHUT_DOWN);
            return;
        }

        try {
            RouteSearchService.getInstance().updateRouteTable(args[0]);
            Thread watchThread = new Thread(new FileWatchThread());
            watchThread.start();
        } catch (IOException e) {
            LOGGER.error("Exception while opening data file", e);
            LOGGER.error(APPLICATION_SHUT_DOWN);
            return;
        } catch (RuntimeException e) {
            LOGGER.error("Data file format is invalid", e);
            LOGGER.error(APPLICATION_SHUT_DOWN);
            return;
        }

        //start vertx and deploy first verticle
        Vertx vertx = Vertx.vertx();
        vertx.deployVerticle(ApplicationServer.class.getCanonicalName());
    }

}
