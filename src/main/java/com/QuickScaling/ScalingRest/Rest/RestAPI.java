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
		
		Router router = Router.router(vertx);
		
		router.route("/*").handler(routingContext->{
			routingContext.response().putHeader("Access-Control-Allow-Origin", "*");
			routingContext.next();
		});
		
		router.get("/GetByTimrstamp/:TimeStamp").handler(routingContext->{
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
		
		router.get("/GetLatestData/:WebSiteURL/:count").handler(routingContext-> {
			String WebSiteURL = routingContext.request().getParam("WebSiteURL");
			int Count = Integer.parseInt(routingContext.request().getParam("count"));
			WebSiteURL = WebSiteURL;
			
			JsonObject QueryObject = new JsonObject();
			QueryObject.put("WebSiteURL", WebSiteURL);
			QueryObject.put("Count", Count);
			
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
		
		server.requestHandler(router::accept).listen(8002);
		
		startFuture.complete();
	}
}
