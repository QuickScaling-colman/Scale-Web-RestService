package com.QuickScaling.ScalingRest.Main;

import com.QuickScaling.ScalingRest.DB.MongoRest;
import com.QuickScaling.ScalingRest.Rest.RestAPI;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;

public class MainVerticle extends AbstractVerticle
{
	private Logger logger = LoggerFactory.getLogger(MainVerticle.class);
	
	@Override
	public void start(Future<Void> startFuture) {	
		DeploymentOptions optionsMongo = new DeploymentOptions();
		optionsMongo.setWorker(true);
		
		String envMongo = System.getProperty("QuickScaling.MongoConfig");
		
		if(envMongo != null && envMongo != "") {
			JsonObject mongoConfig = new JsonObject();
			mongoConfig.put("DB", new JsonObject(envMongo));
			optionsMongo.setConfig(mongoConfig);
			logger.info("Load mongo config from environment variable");
		} else if (config().getJsonObject("DB") != null){
			JsonObject mongoConfig = new JsonObject();
			mongoConfig.put("DB", config().getJsonObject("DB"));
			optionsMongo.setConfig(mongoConfig);
			logger.info("Load mongo config from configuration file");
		} else {
			startFuture.fail("No mongoDB configuration");
		}
		
		vertx.deployVerticle(new MongoRest(),optionsMongo,res-> {
			if(res.succeeded()) { 
				DeploymentOptions optionsRest = new DeploymentOptions();
				optionsMongo.setWorker(true);
				
				String envRestPort = System.getProperty("QuickScaling.RestPort");
				
				if(envRestPort != null && envRestPort != "") {
					JsonObject mongoConfig = new JsonObject();
					mongoConfig.put("RestConf", new JsonObject(envMongo));
					optionsRest.setConfig(mongoConfig);
					logger.info("Load Rest port config from environment variable");
				} else if (config().getJsonObject("RestConf") != null){
					JsonObject mongoConfig = new JsonObject();
					mongoConfig.put("RestConf", config().getJsonObject("RestConf"));
					optionsRest.setConfig(mongoConfig);
					logger.info("Load Rest port config from configuration file");
				} else {
					startFuture.fail("No Rest port configuration");
				}
				
				vertx.deployVerticle(new RestAPI(),optionsRest);
			}
		});
	}
}
