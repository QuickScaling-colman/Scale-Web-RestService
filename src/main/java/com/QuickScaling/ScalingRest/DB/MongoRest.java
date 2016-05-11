package com.QuickScaling.ScalingRest.DB;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.QuickScaling.ScalingRest.Model.WebsiteData;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.mongo.FindOptions;
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
		
		eb.consumer("GET_LATEST_DATA", request -> {
			JsonObject jsonResponseTime = new JsonObject(request.body().toString());
			String WebSiteURL = jsonResponseTime.getString("WebSiteURL");
			int Count = jsonResponseTime.getInteger("Count");
			
			this.getLatestData(WebSiteURL,Count, resQuery -> {
				ObjectMapper mapper = new ObjectMapper();
				
				try {
					request.reply(mapper.writeValueAsString(resQuery));
				} catch (Exception e) {
					e.printStackTrace();
				}
			});
			
		});
		
		eb.consumer("GET_ALL_WEBSITES", request -> {	
			this.GetALlWebsites(resQuery -> {
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
	
	public void GetALlWebsites(Handler<List<JsonObject>> handler) {
		_mongoClient.find("websites", new JsonObject(), res -> { 
			if(res.succeeded()) {
				handler.handle(res.result());
			}
		});
	}
	
	public void getLatestData(String WebSiteURL,int count,Handler<List<WebsiteData>> handler) {
		JsonObject QueryObject = new JsonObject();
		QueryObject.put("website", WebSiteURL);
		FindOptions Option = new FindOptions();
		JsonObject sort = new JsonObject();
		sort.put("date", -1);

		Option.setSort(sort);
		Option.setLimit(count);
		
		_mongoClient.findWithOptions("websitesResponseTime", QueryObject,Option, res -> {
			if(res.succeeded() && !res.result().isEmpty()) { 
				

				
				_mongoClient.findWithOptions("websitesCpuRam", QueryObject,Option, res1 -> {
					List<WebsiteData> websitesData = new ArrayList<>();	
					for (int i = 0; i < count; i++) {
						WebsiteData websiteData = new WebsiteData();
						websiteData.responseTime = res.result().get(i).getInteger("responseTime");
						if(res1.succeeded() && !res1.result().isEmpty()) {
							websiteData.cpu = res1.result().get(i).getInteger("cpu");
							websiteData.ram = res1.result().get(i).getInteger("ram");
						}
						websiteData.Timestamp = new Date(System.currentTimeMillis() - 60000 * i);
						websitesData.add(websiteData);
					}
					
					handler.handle(websitesData);
				});
			}
		});
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
