package wafflestomper.worldborderviewer;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.lang.reflect.Type;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.MalformedJsonException;

import net.minecraft.client.Minecraft;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

public class WBConfigManager {
	
	private Configuration config;
	
	private double defaultWorldRadius = 1110d;
	private double defaultPortalRadius = 1100d;
	private int circleSegments = 5000;
	private double defaultHalfPortalAngle = 30d;
	private double renderDistance = 4*16;//4 chunks 
	private boolean enableDebug = false;
	private boolean shardConfigNotAvailable = false;
	private static final Logger logger = LogManager.getLogger("WorldBorderViewer");
	public HashMap<String, HashMap<String, ShardConfig>> shardsByServer = new HashMap<String, HashMap<String, ShardConfig>>();
	
	private static final double N = 0d;
	private static final double NNE = 22.5d;
	private static final double NE = 45d;
	private static final double ENE = 67.5d;
	private static final double E = 90d;
	private static final double ESE = 112.5d;
	private static final double SE = 135d;
	private static final double SSE = 157.5d;
	private static final double S = 180d;
	private static final double SSW = 202.5d;
	private static final double SW = 225d;
	private static final double WSW = 247.5d;
	private static final double W = 270d;
	private static final double WNW = 292.5d;
	private static final double NW = 315d;
	private static final double NNW = 337.5;
	
	public WBConfigManager(){
	}
	
	
	private void setUpShardConfig(){
		/*
		 * N:  NNW, NNE
		 * NE: NNE, ENE
		 * E:  ENE, ESE
		 * SE: ESE, SSE
		 * S:  SSE, SSW
		 * SW: SSW, WSW
		 * W:  WSW, WNW
		 * NW: WNW, NNW
		 */
		
		/////////////////////////////////////// Dev server worlds //////////////////////////////////////////////
		HashMap<String, ShardConfig> devWorlds = new HashMap<String, ShardConfig>();
		ShardConfig uuidWorld = new ShardConfig("localhost", "70d03911-8dbe-4c79-9ec0-516359935739", "UUID World", 20, 20);
		uuidWorld.addPortal("N", 0, 360);
		//uuidWorld.addPortal("E", ENE, ESE);
		//uuidWorld.addPortal("S", SSE, SSW);
		//uuidWorld.addPortal("W", WSW, WNW);
		//uuidWorld.addPortal("Rokko Steppe", 120, 180);
		devWorlds.put(uuidWorld.worldName, uuidWorld);
		this.shardsByServer.put("localhost", devWorlds);
		this.shardsByServer.put("192.168.1.102", devWorlds);
		
		////////////////////////////////////////  Civcraft shards ///////////////////////////////////////////////
		HashMap<String, ShardConfig> civcraftShards = new HashMap<String, ShardConfig>();
		
		// Rokko Steppe
		ShardConfig rokko = new ShardConfig("mc.civcraft.co", "a72e4777-ad62-4e3b-a4e0-8cf2d15147ea", "Rokko Steppe", 2000d, 2010d); 
		rokko.addPortal("Volans", 202.5d, 247.5d);
		rokko.addPortal("Tjikko", 292.5d, 337.5d);
		civcraftShards.put(rokko.worldName, rokko);
		
		// Volans
		ShardConfig volans = new ShardConfig("mc.civcraft.co", "b25abb31-fd1e-499d-a5b5-510f9d2ec501", "Volans", 1000d, 1010d);
		volans.addPortal("Rokko Steppe", 22.5d, 67.5d);
		volans.addPortal("Drakontas", 292.5d, 337.5d);
		civcraftShards.put(volans.worldName, volans);
		
		// Drakontas
		ShardConfig drakontas = new ShardConfig("mc.civcraft.co", "a7cbf239-6c11-4146-a715-ef0a9827b4c4", "Drakontas", 1750d, 1760d);
		drakontas.addPortal("Volans", 90d, 150d);
		drakontas.addPortal("Isolde", 210d, 270d);
		drakontas.addPortal("Ulca Felya", 330d, 30d);
		civcraftShards.put(drakontas.worldName, drakontas);		
		
		// Tjikko
		ShardConfig tjikko = new ShardConfig("mc.civcraft.co", "44f4b133-a646-461a-a14a-5fd8c8dbc59c", "Tjikko", 1500d, 1510d);
		tjikko.addPortal("Naunet", 0d, 60d);
		tjikko.addPortal("Rokko Steppe", 120d, 180d);
		tjikko.addPortal("Ulca Felya", 240d, 300d);
		civcraftShards.put(tjikko.worldName, tjikko);				

		// Eilon
		ShardConfig eilon = new ShardConfig("mc.civcraft.co", "a358b10c-7041-40c5-ac5e-db5483a9dfc2", "Eilon", 1500, 1510);
		eilon.addPortal("Ulca Felya", 60d, 120d);
		eilon.addPortal("Padzahr", 180d, 240d);
		eilon.addPortal("Abydos", 300d, 0d);
		civcraftShards.put(eilon.worldName, eilon);		

		// Abydos
		ShardConfig abydos = new ShardConfig("mc.civcraft.co", "182702a7-ea3f-41de-a2d3-c046842d5e74", "Abydos", 2000, 2010);
		abydos.addPortal("Sheol", 22.5d, 67.5d);
		abydos.addPortal("Eilon", 112.5d, 157.5d);
		civcraftShards.put(abydos.worldName, abydos);		
		
		// Padzahr
		ShardConfig padzahr = new ShardConfig("mc.civcraft.co", "7120b7a6-dd21-468c-8cd7-83d96f735589", "Padzahr", 1000, 1010);
		padzahr.addPortal("Isolde", 112.5d, 157.5d);
		padzahr.addPortal("Eilon", 22.5d, 67.5d);
		civcraftShards.put(padzahr.worldName, padzahr);		
		
		// Isolde
		ShardConfig isolde = new ShardConfig("mc.civcraft.co", "197e2c4f-2fd6-464a-8754-53b24d9f7898", "Isolde", 2500, 2510);
		isolde.addPortal("Drakontas", 22.5d, 67.5d);
		isolde.addPortal("Padzahr", 292.5d, 337.5d);
		civcraftShards.put(isolde.worldName, isolde);
		
		// Naunet
		ShardConfig naunet = new ShardConfig("mc.civcraft.co", "de730958-fa83-4e73-ab7f-bfdab8e27960", "Naunet", 1000d, 1010d);
		naunet.addPortal("Tjikko", 202.5d, 247.5d);
		civcraftShards.put(naunet.worldName, naunet);		
		
		// Tigrillo
		ShardConfig tigrillo = new ShardConfig("mc.civcraft.co", "63a68417-f07f-4cb5-a9d8-e5e702565967", "Tigrillo", 1750, 1760);
		tigrillo.addPortal("Sheol", 270d, 330d);
		tigrillo.addPortal("Ulca Felya", 150d, 210d);
		civcraftShards.put(tigrillo.worldName, tigrillo);		
		
		////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
		/////////////////////////////////// Research needed for the shards below/ ///////////////////////////////////////////////////////
		/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

		// I think this is the lobby: 
		// ba0cfdfb-4857-4efb-b516-0f5876d460d3
		
		// Ulca Felya
		ShardConfig ulca = new ShardConfig("mc.civcraft.co", "7f03aa4d-833c-4b0c-9d3b-a65a5c6eada0", "Ulca Felya", 3000, 3010);
		ulca.addPortal("Tigrillo", NNW, NNE); //TODO: Check these
		ulca.addPortal("Eilon", WSW, WNW); //TODO: Check these
		ulca.addPortal("Tjikko", ENE, ESE); //TODO: Check these
		ulca.addPortal("Drakontas", SSE, SSW); //TODO: Check these
		civcraftShards.put(ulca.worldName, ulca);		
		
		// Sheol
		ShardConfig sheol = new ShardConfig("mc.civcraft.co", "fc891b9e-4b20-4c8d-8f97-7436383e8105", "Sheol", 1300, 1310);
		sheol.addPortal("Abydos", SSW, WSW); //TODO: Check these
		sheol.addPortal("Tigrillo", ESE, SSE); //TODO: Check these
		civcraftShards.put(sheol.worldName, sheol);
		
		this.shardsByServer.put("mc.civcraft.co", civcraftShards);
	}
	
	
	public void init(FMLPreInitializationEvent event){
	    this.config = new Configuration(event.getSuggestedConfigurationFile());
	    
	    // Load data and/or set defaults
	    this.config.load();
	    this.config.setCategoryComment("world", "These are the default settings for the world border when a match can't be found in the internal shard config");
	    this.defaultWorldRadius = this.config.get("world", "world_radius", 1110d, "World radius").getDouble(1110d);
	    this.defaultPortalRadius = this.config.get("world", "portal_radius", 1100d, "Portal radius").getDouble(1100d);
	    this.defaultHalfPortalAngle = this.config.get("world", "shard_portal_angle", 60d, "Angle in degrees representing "+
	    									  "the size of the shard portals").getDouble(60d)/2;
	    this.circleSegments = this.config.get("display", "circle_segments", 4000, "Circle segments (a higher "+
	    									  "number will make the perimeter smoother, but could decrease your framerate").getInt(4000);
	    this.renderDistance = this.config.get("display", "render_distance", 64d, "Wall render distance in blocks").getDouble(64);
	    this.enableDebug = this.config.get("display", "enable_debug", false, 
	    								   "Enable debugging messages at the top of the screen").getBoolean(false);
	    if (this.circleSegments < 360){
	    	this.circleSegments = 360;
	    }
	    
	    // Save defaults
	    this.config.save();
	    
	    this.setUpShardConfig();
    }
	
	
	public double getDefaultWorldRadius(){
		return(this.defaultWorldRadius);
	}
	
	
	public double getDefaultPortalRadius(){
		return(this.defaultPortalRadius);
	}
	
	
	public int getCircleSegments(){
		return(this.circleSegments);
	}
	
	
	public double getDefaultHalfPortalAngle(){
		return(this.defaultHalfPortalAngle);
	}
	
	
	public double getRenderDistance(){
		return(this.renderDistance);
	}
	
	
	public boolean getEnableDebug(){
		return(this.enableDebug);
	}
}
