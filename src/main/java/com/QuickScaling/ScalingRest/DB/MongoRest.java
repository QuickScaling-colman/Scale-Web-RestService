package com.QuickScaling.ScalingRest.DB;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import com.QuickScaling.ScalingRest.Model.WebsiteData;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.json.JsonArray;
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
			
			this.getLatestData(WebSiteURL, resQuery -> {
				ObjectMapper mapper = new ObjectMapper();
				
				try {
					request.reply(mapper.writeValueAsString(resQuery));
				} catch (Exception e) {
					e.printStackTrace();
				}
			});
			
		});
		
		eb.consumer("GET_LATEST_DATA_MIN", request -> {
			JsonObject jsonResponseTime = new JsonObject(request.body().toString());
			int Minute = jsonResponseTime.getInteger("Min");
			
			this.getLatestDataMinute(Minute, resQuery -> {
				ObjectMapper mapper = new ObjectMapper();
				
				try {
					
					request.reply(resQuery.toString());
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
	
	SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
	//SimpleDateFormat format2 = new SimpleDateFormat("dd/MM/yyyy, HH:mm:ss'Z'");
	
	public void getLatestDataMinute(int minute,Handler<JsonObject> handler) {
		FindOptions OptionResponseTime = new FindOptions();
		JsonObject sort = new JsonObject();
		sort.put("date", -1);

		OptionResponseTime.setSort(sort);
		OptionResponseTime.setLimit(minute);
		
		FindOptions OptionCpuRam = new FindOptions();
		JsonObject sort1 = new JsonObject();
		sort1.put("create_at", -1);

		OptionCpuRam.setSort(sort1);
		OptionCpuRam.setLimit(minute);
		
		FindOptions OptionScale = new FindOptions();
		JsonObject sort2 = new JsonObject();
		sort2.put("date", -1);

		OptionScale.setSort(sort2);
		OptionScale.setLimit(minute);
		 
		_mongoClient.findWithOptions("websitesResponseTime", new JsonObject(),OptionResponseTime, res -> {		
			_mongoClient.findWithOptions("websitesCpuRam", new JsonObject(),OptionCpuRam, res1 -> {
				_mongoClient.findWithOptions("websitesScale", new JsonObject(),OptionScale, res2 -> {
					JsonObject Result = new JsonObject();
					for (JsonObject ResultResponseTime : res.result()) {
						try {
							ResultResponseTime.put("JavaDate", format.parse(ResultResponseTime.getJsonObject("date").getString("$date")).getTime());
						} catch (Exception e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
					
					for (JsonObject ResultCpuRam : res1.result()) {
						try {
							ResultCpuRam.put("JavaDate", new Date().getTime());
						} catch (Exception e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
					
					format.setTimeZone(TimeZone.getTimeZone("Israel"));
					
					for (JsonObject ResultScale : res2.result()) {
						try {
							ResultScale.put("JavaDate", format.parse(ResultScale.getJsonObject("date").getString("$date")).getTime());
						} catch (Exception e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
					Result.put("ResponseTime", new JsonArray(res.result()));
					Result.put("CpuRam", new JsonArray(res1.result()));
					Result.put("Scale", new JsonArray(res2.result()));
					
					handler.handle(Result);
				});
			});
		});
	}
	
	public void GetALlWebsites(Handler<List<JsonObject>> handler) {
		_mongoClient.find("websites", new JsonObject(), res -> { 
			if(res.succeeded()) {
				handler.handle(res.result());
			}
		});
	}
	
	public void getLatestData(String WebSiteURL,Handler<WebsiteData> handler) {
		JsonObject QueryObject = new JsonObject();
		QueryObject.put("website", WebSiteURL);
		FindOptions OptionResponseTime = new FindOptions();
		JsonObject sort = new JsonObject();
		sort.put("date", -1);

		OptionResponseTime.setSort(sort);
		OptionResponseTime.setLimit(1);
		
		FindOptions OptionCpuRam = new FindOptions();
		JsonObject sort1 = new JsonObject();
		sort1.put("create_at", -1);

		OptionCpuRam.setSort(sort1);
		OptionCpuRam.setLimit(1);
		
		FindOptions OptionScale = new FindOptions();
		JsonObject sort2 = new JsonObject();
		sort2.put("date", -1);

		OptionScale.setSort(sort1);
		OptionScale.setLimit(1);
		
		_mongoClient.findWithOptions("websitesResponseTime", QueryObject,OptionResponseTime, res -> {		
			_mongoClient.findWithOptions("websitesCpuRam", new JsonObject(),OptionCpuRam, res1 -> {
				_mongoClient.findWithOptions("websitesScale", new JsonObject(),OptionScale, res2 -> {
					WebsiteData websiteData = new WebsiteData();
					
					if(res1.succeeded() && !res1.result().isEmpty() && res1.result().size() > 0) {
						websiteData.cpu = res1.result().get(1).getDouble("cpu");
						websiteData.ram = res1.result().get(1).getDouble("memory");
						websiteData.MaxCpu = res1.result().get(1).getDouble("cpu_max");
						websiteData.MaxRam = res1.result().get(1).getDouble("memory_max");
					}
					
					if(res.succeeded() && !res.result().isEmpty() && res.result().size() > 0) {
						websiteData.responseTime = res.result().get(1).getDouble("responseTime");
					}
					
					
					if(res2.succeeded() && !res2.result().isEmpty() && res2.result().size() > 0) {
						websiteData.replicas = res2.result().get(1).getInteger("replicas");
					}
					
					websiteData.Timestamp = new Date();
					
					handler.handle(websiteData);
				});
			});
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
