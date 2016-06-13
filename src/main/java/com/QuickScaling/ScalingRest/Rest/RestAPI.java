package com.QuickScaling.ScalingRest.Rest;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.ServerWebSocket;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.StaticHandler;

public class RestAPI extends AbstractVerticle{
	Logger logger = LoggerFactory.getLogger(RestAPI.class);
	HashMap<String, Object> WebsocketPeriodicID = new HashMap<String, Object>();
	
	SimpleDateFormat Mongoformat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
	
	private long startPeriodicForWebsocket(int WebsocketPeriodicSend, ServerWebSocket request) {
		
		return startPeriodicForWebsocket(WebsocketPeriodicSend,request, null);
	}
	
	private long startPeriodicForWebsocket(int WebsocketPeriodicSend, ServerWebSocket request, Calendar FromDate) {		
		return vertx.setPeriodic(WebsocketPeriodicSend, periodic->{

				JsonObject QueryObject = new JsonObject();
				QueryObject.put("Min", 2);
				
				if(FromDate != null) {
					QueryObject.put("StartDate", Mongoformat.format(FromDate.getTime()));
				}
				
				vertx.eventBus().send("GET_LATEST_DATA_MIN", QueryObject, res -> {
					if(res.succeeded()) {
						request.writeFinalTextFrame(res.result().body().toString());
						
						if(FromDate != null) {
							FromDate.add(Calendar.MINUTE, 2);
						}
					} else {
						
					}
					
				});					
		});
	}
	
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

		HttpServer serverWebsocket = vertx.createHttpServer();
		
		serverWebsocket.websocketHandler(request-> {
			int WebsocketPeriodicSend = 30000;
			logger.info("Open " + request.textHandlerID());
			//if(config().getJsonObject("RestConf") != null) {
			//	WebsocketPeriodicSend = config().getJsonObject("RestConf").getInteger("WebsocketPeriodicSend");
			//}
			request.exceptionHandler(handler->{
				logger.error(handler.getMessage());
			});

			request.frameHandler(res -> {
				JsonObject Query;
				try {Query = new JsonObject(res.textData());} 
				catch (Exception e) {
					request.writeFinalTextFrame(new JsonObject().put("Error","Unable To parse data to json").toString());
					return; 
				};
				
				logger.info("Check StartDate and Min");
				logger.info(Query.getString("StartDate") + " : " + Query.getInteger("Min"));
				 if(Query.getString("StartDate") != null && Query.getInteger("Min") != null) {
					
					try {
						Date StartDate = Mongoformat.parse(Query.getString("StartDate"));
						
						logger.info("Cancel Timer: " + WebsocketPeriodicID.get(request.textHandlerID()));
						vertx.cancelTimer((long) WebsocketPeriodicID.get(request.textHandlerID()));
						
						JsonObject QueryObject = new JsonObject();
						QueryObject.put("Min", Query.getInteger("Min"));
						QueryObject.put("StartDate", Query.getString("StartDate"));
						
						vertx.eventBus().send("GET_LATEST_DATA_MIN", QueryObject, ebRes -> {
							if(ebRes.succeeded()) {
								logger.info("Recive From Mongo");
								request.writeFinalTextFrame(ebRes.result().body().toString());
								logger.info("Send To WebSocket");
								
								Calendar DateAddMin = Calendar.getInstance();
								DateAddMin.setTime(StartDate);
								DateAddMin.add(Calendar.MINUTE, Query.getInteger("Min"));
								
								long newPeriodicId = startPeriodicForWebsocket(10000, request, DateAddMin);
								WebsocketPeriodicID.put(request.textHandlerID(), newPeriodicId);
							} else {
								
							}
						});
						
						
						
					} catch (Exception e) {
						request.writeFinalTextFrame(new JsonObject().put("Error", "Unable to format StartDate in pattern yyyy-MM-dd'T'HH:mm:ss'Z'").toString());
					}
				}
			});
			
			request.closeHandler(res->{
				logger.info("Close " + request.textHandlerID());
				vertx.cancelTimer((long) WebsocketPeriodicID.get(request.textHandlerID()));
			});
			
			long PeriodicId = startPeriodicForWebsocket(WebsocketPeriodicSend, request);
			WebsocketPeriodicID.put(request.textHandlerID(), PeriodicId);
			
		});
		serverWebsocket.listen(8089);
		
		server.requestHandler(router::accept);
		
		server.listen(port);
		
		startFuture.complete();
	}
	
	
}
