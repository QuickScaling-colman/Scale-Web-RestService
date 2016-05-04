package com.QuickScaling.ScalingRest.DB;

import java.util.ArrayList;
import java.util.List;

import com.QuickScaling.ScalingRest.Model.WebsiteData;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.mongo.MongoClient;

public class MongoRest extends AbstractVerticle{
	private MongoClient _mongoClient;
	
	@Override
	public void start(Future<Void> startFuture) {	
		EventBus eb = vertx.eventBus();
		
		_mongoClient = MongoClient.createNonShared(vertx, config().getJsonObject("DB"));
		
		eb.consumer("GET_DATA_BY_HOST_NAME", request -> {
			JsonObject jsonResponseTime = new JsonObject(request.body().toString());
			String WebSiteURL = jsonResponseTime.getString("WebSiteURL");
			this.GetWebsiteData(WebSiteURL, resQuery -> {
				ObjectMapper mapper = new ObjectMapper();
				
				try {
					request.reply(mapper.writeValueAsString(resQuery));
				} catch (Exception e) {
					e.printStackTrace();
				}
			});
			
		});
		
		startFuture.complete();
	}
	
	public void GetWebsiteData(String WebSiteURL,Handler<List<WebsiteData>> handler) {		
		JsonObject QueryObject = new JsonObject();
		QueryObject.put("website", WebSiteURL);
		
		_mongoClient.find("websitesResponseTime", QueryObject, res -> {
			if(res.succeeded()) {
				_mongoClient.find("websitesCpuRam", QueryObject, res1 -> {
					if(res1.succeeded()) {
						List<WebsiteData> arrWebsiteData = new ArrayList<>();
						arrWebsiteData = MixResponseTimeCPURAM(res.result(),res1.result());
						
						handler.handle(arrWebsiteData);
					}
				});
			}
		});
	}
	
	private List<WebsiteData> MixResponseTimeCPURAM(List<JsonObject> ReponseTime,List<JsonObject> CPURam) {
		return null;
	}
}
