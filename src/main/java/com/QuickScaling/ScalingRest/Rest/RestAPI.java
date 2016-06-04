package com.QuickScaling.ScalingRest.Rest;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.http.HttpServer;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.StaticHandler;

public class RestAPI extends AbstractVerticle{
	
	
	
	@Override
	public void start(Future<Void> startFuture) {	
		HttpServer server = vertx.createHttpServer();
		
		int port = 8080;
		
		if(config().getJsonObject("RestConf") != null) {
			port = config().getJsonObject("RestConf").getInteger("port");
		}

		Router router = Router.router(vertx);
		
		router.route("/*").handler(routingContext->{
			routingContext.response().putHeader("Access-Control-Allow-Origin", "*");
			routingContext.next();
		});
		
		router.get("/GetByTimestamp/:TimeStamp").handler(routingContext->{
			String TimeStamp = routingContext.request().getParam("TimeStamp");
			JsonObject QueryObject = new JsonObject();
			QueryObject.put("TimeStamp", TimeStamp);
			
			vertx.eventBus().send("GET_DATA_BY_HOST_NAME", QueryObject, res -> {
				if(res.succeeded()) {
					routingContext.response().end(res.result().body().toString());
				} else {
					routingContext.response().end("Error");
				}
				
			});
		});
		
		router.get("/GetLatestData").handler(routingContext-> {
			String HostName = "";
			//int Count = Integer.parseInt(routingContext.request().getParam("count"));
			
			JsonObject QueryObject = new JsonObject();
			QueryObject.put("Host", HostName);
			//QueryObject.put("Count", Count);
			
			vertx.eventBus().send("GET_LATEST_DATA", QueryObject, res -> {
				if(res.succeeded()) {
					routingContext.response().end(res.result().body().toString());
				} else {
					routingContext.response().end("Error");
				}
				
			});
		});
		
		router.get("/GetLatestData/:count").handler(routingContext-> {
			int Count = Integer.parseInt(routingContext.request().getParam("count"));
			
			JsonObject QueryObject = new JsonObject();
			QueryObject.put("Min", Count);
			
			vertx.eventBus().send("GET_LATEST_DATA_MIN", QueryObject, res -> {
				if(res.succeeded()) {
					routingContext.response().end(res.result().body().toString());
				} else {
					routingContext.response().end("Error");
				}
				
			});
		});
		
		router.get("/GetAllWebsites").handler(routingContext-> {
			vertx.eventBus().send("GET_ALL_WEBSITES", new JsonObject(), res -> {
				if(res.succeeded()) {
					routingContext.response().end(res.result().body().toString());
				} else {
					routingContext.response().end("Error");
				}
				
			});
		});
		
		
		
		router.route("/*").handler(StaticHandler.create("webroot/public/").setFilesReadOnly(false));
		
		server.websocketHandler(handler-> {
			int WebsocketPeriodicSend = 30000;
			
			if(config().getJsonObject("RestConf") != null) {
				WebsocketPeriodicSend = config().getJsonObject("RestConf").getInteger("WebsocketPeriodicSend");
			}
			
			long PeriodicId = vertx.setPeriodic(WebsocketPeriodicSend, periodic->{

					JsonObject QueryObject = new JsonObject();
					QueryObject.put("Min", 1);
					
					vertx.eventBus().send("GET_LATEST_DATA_MIN", QueryObject, res -> {
						if(res.succeeded()) {
							handler.writeFinalTextFrame(res.result().body().toString());
						} else {
							
						}
						
					});					
			});
			
			handler.closeHandler(res->{
				vertx.cancelTimer(PeriodicId);
			});
			
		});
		
		server.requestHandler(router::accept).listen(port);
		
		startFuture.complete();
	}
}
