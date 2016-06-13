package com.QuickScaling.ScalingRest.DB;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

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
import io.vertxconcurrent.CountDownLatch;

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
			String WebSiteURL = jsonResponseTime.getString("Host");
			
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
			String StartDate = jsonResponseTime.getString("StartDate");
			Date dStartDate = null;
			if(StartDate != null) {
				format.setTimeZone(TimeZone.getTimeZone("Israel"));
				try {
					dStartDate = format.parse(StartDate);
				} catch (Exception e) {
					request.reply("Error parse date");
					return;
				}
			}
			
			this.getLatestDataMinute(Minute, dStartDate, resQuery -> {
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
	
	public void getLatestDataMinute(int minute,Date StartDate,Handler<JsonObject> handler) {
		CountDownLatch  latch = new CountDownLatch(3, vertx);
		
		Calendar calStartTime = Calendar.getInstance();
		Calendar calEndTimeTime = Calendar.getInstance();
		
		JsonObject FilterDateIsrael = new JsonObject();
		JsonObject FilterDateUTC = new JsonObject();
		
		if(StartDate != null) {
			calStartTime.setTime(StartDate);
			calEndTimeTime.setTime(StartDate);
			calEndTimeTime.add(Calendar.MINUTE, minute);
		} else {
			calStartTime.add(Calendar.MINUTE, minute * -1);
			
		}
		
		format.setTimeZone(TimeZone.getTimeZone("Israel"));
		FilterDateIsrael.put("$gte", new JsonObject().put("$date", format.format(calStartTime.getTime())));
		FilterDateIsrael.put("$lte", new JsonObject().put("$date", format.format(calEndTimeTime.getTime())));
		
		format.setTimeZone(TimeZone.getTimeZone("UTC"));
		FilterDateUTC.put("$gte", new JsonObject().put("$date", format.format(calStartTime.getTime())));
		FilterDateUTC.put("$lte", new JsonObject().put("$date", format.format(calEndTimeTime.getTime())));
		
		
		// --------------- ResponseTime -----------------
		JsonObject QueryObjectResponseTime = new JsonObject();
		
		QueryObjectResponseTime.put("date", FilterDateIsrael);

		
		FindOptions OptionResponseTime = new FindOptions();
		JsonObject sort = new JsonObject();
		sort.put("date", -1);

		OptionResponseTime.setSort(sort);

		// --------------- CpuRam -----------------
		format.setTimeZone(TimeZone.getTimeZone("UTC"));
		JsonObject QueryObjectCpuRam = new JsonObject();
		
		QueryObjectCpuRam.put("timestamp", FilterDateUTC);

		FindOptions OptionCpuRam = new FindOptions();
		JsonObject sort1 = new JsonObject();
		sort1.put("timestamp", -1);

		OptionCpuRam.setSort(sort1);		
		
		// --------------- Scale -----------------
		format.setTimeZone(TimeZone.getTimeZone("Israel"));
		JsonObject QueryObjectScale = new JsonObject();
		QueryObjectScale.put("date", FilterDateIsrael);
		FindOptions OptionScale = new FindOptions();
		JsonObject sort2 = new JsonObject();
		sort2.put("date", -1);

		OptionScale.setSort(sort2);
		
		// --------- Start Query ----------
		JsonObject Result = new JsonObject();
		
		_mongoClient.findWithOptions("websitesResponseTime", QueryObjectResponseTime,OptionResponseTime, res -> {
			format.setTimeZone(TimeZone.getTimeZone("Israel"));
			JsonArray ArrResult = new JsonArray();
			Calendar CurrentDate = Calendar.getInstance();
			JsonObject CurrentRow = null;
			int Counter = 0;
			for (JsonObject ResultResponseTime : res.result()) {
				try {
					ResultResponseTime.put("JavaDate", format.parse(ResultResponseTime.getJsonObject("date").getString("$date")).getTime());
					
					Calendar rowDate = Calendar.getInstance();
					rowDate.setTime(format.parse(ResultResponseTime.getJsonObject("date").getString("$date")));
					
					if(CurrentRow == null) {
						CurrentRow = ResultResponseTime;
						CurrentDate = rowDate;
						Counter = 1;
					}
					else if(rowDate.get(Calendar.MINUTE) == CurrentDate.get(Calendar.MINUTE)) {
						CurrentRow.put("responseTime", CurrentRow.getDouble("responseTime") + ResultResponseTime.getDouble("responseTime")); 
						Counter++;
					}
					else {
						CurrentRow.put("responseTime", CurrentRow.getDouble("responseTime") / Counter);
						ArrResult.add(CurrentRow);
						CurrentRow = ResultResponseTime;
						CurrentDate = rowDate;
						Counter = 1;
					}
					
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			
			Result.put("ResponseTime", ArrResult);
			latch.countDown();
		});
		
		_mongoClient.findWithOptions("websitesCpuRam", QueryObjectCpuRam,OptionCpuRam, res1 -> {
			format.setTimeZone(TimeZone.getTimeZone("UTC"));
			for (JsonObject ResultCpuRam : res1.result()) {
				try {
					ResultCpuRam.put("JavaDate", format.parse(ResultCpuRam.getJsonObject("timestamp").getString("$date")).getTime());
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			
			Result.put("CpuRam", new JsonArray(res1.result()));
			latch.countDown();
		});
		
		_mongoClient.findWithOptions("websitesScale", QueryObjectScale,OptionScale, res2 -> {
			format.setTimeZone(TimeZone.getTimeZone("Israel"));
			
			for (JsonObject ResultScale : res2.result()) {
				try {
					ResultScale.put("JavaDate", format.parse(ResultScale.getJsonObject("date").getString("$date")).getTime());
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			
			Result.put("Scale", new JsonArray(res2.result()));
			latch.countDown();
		});
			
		latch.await(30,TimeUnit.SECONDS,res -> {
			handler.handle(Result);
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
		WebsiteData websiteData = new WebsiteData();
		CountDownLatch  latch = new CountDownLatch(3, vertx);
		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.MINUTE, -1);
		
		JsonObject QueryObjectCpuRam = new JsonObject();
		format.setTimeZone(TimeZone.getTimeZone("UTC"));
		QueryObjectCpuRam.put("timestamp", new JsonObject().put("$gte", new JsonObject().put("$date", format.format(cal.getTime()))));
		
		FindOptions OptionCpuRam = new FindOptions();
		JsonObject sort1 = new JsonObject();
		sort1.put("timestamp", -1);

		OptionCpuRam.setSort(sort1);
	
		_mongoClient.findWithOptions("websitesCpuRam", QueryObjectCpuRam ,OptionCpuRam, res1 -> {
			
			if(res1.succeeded() && !res1.result().isEmpty() && res1.result().size() > 0) {
				websiteData.ram = res1.result().get(0).getDouble("memory");
				websiteData.MaxCpu = res1.result().get(0).getDouble("cpu_limit");
				websiteData.MaxRam = res1.result().get(0).getDouble("memory_limit");
				
				int Count = 0;
				
				for (JsonObject entry : res1.result()) {
					if(websiteData.MaxCpu == entry.getDouble("cpu_limit")) {
						websiteData.cpu += entry.getDouble("cpu");
						Count++;
					}
					

				}
				
				websiteData.cpu = websiteData.cpu / Count;
			}
			
			latch.countDown();
		});
		
		format.setTimeZone(TimeZone.getTimeZone("Israel"));
		JsonObject QueryObjectResponseTime = new JsonObject();
		QueryObjectResponseTime.put("date", new JsonObject().put("$gte", new JsonObject().put("$date", format.format(cal.getTime()))));
		FindOptions OptionResponseTime = new FindOptions();
		JsonObject sort = new JsonObject();
		sort.put("date", -1);

		OptionResponseTime.setSort(sort);
		
		_mongoClient.findWithOptions("websitesResponseTime", QueryObjectResponseTime, OptionResponseTime, res -> {
			if(res.succeeded() && !res.result().isEmpty() && res.result().size() > 0) {
				Double LastEntry = res.result().get(0).getDouble("responseTime");
				int Count = 0;
				
				for (JsonObject entry : res.result()) {
					if((LastEntry * 2.5) > entry.getDouble("responseTime")) {
						websiteData.responseTime += entry.getDouble("responseTime");
						if(entry.getDouble("responseTime") > LastEntry) {
							LastEntry = entry.getDouble("responseTime");
						}
						Count++;
					}
				}
				
				websiteData.responseTime = websiteData.responseTime / Count;
			}
			latch.countDown();
		});
			
		JsonObject QueryObjectScale = new JsonObject();
		FindOptions OptionScale = new FindOptions();
		JsonObject sort2 = new JsonObject();
		sort2.put("date", -1);

		OptionScale.setSort(sort2);
		OptionScale.setLimit(1);
			
		_mongoClient.findWithOptions("websitesScale", QueryObjectScale,OptionScale, res2 -> {
			if(res2.succeeded() && !res2.result().isEmpty() && res2.result().size() > 0) {
				websiteData.replicas = res2.result().get(0).getInteger("replicas");
			}
			
			latch.countDown();
		});
		
		latch.await(30,TimeUnit.SECONDS,res -> {
			websiteData.Timestamp = new Date();
			handler.handle(websiteData);
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
