package com.QuickScaling.ScalingRest.Rest;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.http.HttpServer;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;

public class RestAPI extends AbstractVerticle{
	
	@Override
	public void start(Future<Void> startFuture) {	
		HttpServer server = vertx.createHttpServer();
		
		Router router = Router.router(vertx);
		
		router.get("/GetByTimrstamp/:TimeStamp").handler(routingContext->{
			String TimeStamp = routingContext.request().getParam("TimeStamp");
			JsonObject QueryObject = new JsonObject();
			QueryObject.put("TimeStamp", TimeStamp);
			
			vertx.eventBus().send("GET_DATA_BY_HOST_NAME", QueryObject, res -> {
				routingContext.response().end(res.result().body().toString());
			});
		});
		
		server.requestHandler(router::accept).listen(8080);
		
		startFuture.complete();
	}
}
