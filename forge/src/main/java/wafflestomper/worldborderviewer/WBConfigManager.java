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
	private static final double NNW = 335.5;
	
	public WBConfigManager(){
	}
	
	
	/*
	// Deprecated I guess
	public void loadShardConfig(File shardConfigFile){
		Type SHARDCONFIG_TYPE = new TypeToken<List<ShardConfig>>() {}.getType();
		Gson gson = new Gson();
		JsonReader reader;
		try {
			reader = new JsonReader(new FileReader(shardConfigFile));
		} catch (FileNotFoundException e) {
			this.shardConfigNotAvailable = true;
			return;
		}
		List<ShardConfig> data;;
		try{
			data = gson.fromJson(reader, SHARDCONFIG_TYPE);
		}
		catch (IllegalStateException e){
			// The most likely cause of this is that the file isn't an array (i.e. bounded by square brackets)
			logger.error(e);
			this.shardConfigNotAvailable = true;
			return;
		}
		for (ShardConfig s : data){
			System.out.println(s.serverName);
		}
	}
	*/
	
	
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
		ShardConfig uuidWorld = new ShardConfig("localhost", "70d03911-8dbe-4c79-9ec0-516359935739", "UUID World", 20, 30);
		uuidWorld.addPortal("N", 355, 5);
		//uuidWorld.addPortal("E", ENE, ESE);
		//uuidWorld.addPortal("S", SSE, SSW);
		//uuidWorld.addPortal("W", WSW, WNW);
		uuidWorld.addPortal("Rokko Steppe", 120, 180);
		devWorlds.put(uuidWorld.worldName, uuidWorld);
		this.shardsByServer.put("localhost", devWorlds);
		this.shardsByServer.put("192.168.1.102", devWorlds);
		
		////////////////////////////////////////  Civcraft shards ///////////////////////////////////////////////
		HashMap<String, ShardConfig> civcraftShards = new HashMap<String, ShardConfig>();
		
	    // Naunet
		ShardConfig naunet = new ShardConfig("mc.civcraft.co", "NO WORLD NAME YET n", "Naunet", 9999, 9999); //TODO: Find these values
		naunet.addPortal("Tjikko", SSW, WSW); //TODO: Check these
		civcraftShards.put(naunet.worldName, naunet);
		
		// Tjikko
		ShardConfig tjikko = new ShardConfig("mc.civcraft.co", "44f4b133-a646-461a-a14a-5fd8c8dbc59c", "Tjikko", 1500d, 1510d);
		tjikko.addPortal("Naunet", NNE, ENE); //TODO: Check these
		tjikko.addPortal("Ulca Felya", WSW, WNW); //TODO: Check these
		tjikko.addPortal("Rokko Steppe", 120, 180);
		civcraftShards.put(tjikko.worldName, tjikko);
		
		// Rokko Steppe
		ShardConfig rokko = new ShardConfig("mc.civcraft.co", "a72e4777-ad62-4e3b-a4e0-8cf2d15147ea", "Rokko Steppe", 2000d, 2010d);
		rokko.addPortal("Tjikko", WNW, NNW);  //TODO: Check these
		rokko.addPortal("Volans", SSW, WSW); //TODO: Check these
		civcraftShards.put(rokko.worldName, rokko);
		
		// Volans
		ShardConfig volans = new ShardConfig("mc.civcraft.co", "b25abb31-fd1e-499d-a5b5-510f9d2ec501", "Volans", 1000d, 1010d);
		volans.addPortal("Rokko Steppe", NNE, ENE); //TODO: Check these
		volans.addPortal("Drakontas", WNW, NNW); //TODO: Check these
		civcraftShards.put(volans.worldName, volans);
		
		// Drakontas
		ShardConfig drakontas = new ShardConfig("mc.civcraft.co", "NO WORLD NAME YET d", "Drakontas", 9999, 9999); // TODO: Find these values
		drakontas.addPortal("Volans", ESE, SSE); //TODO: Check these
		drakontas.addPortal("Isolde", SSW, WSW); //TODO: Check these
		civcraftShards.put(drakontas.worldName, drakontas);
		
		// Isolde
		ShardConfig isolde = new ShardConfig("mc.civcraft.co", "NO WORLD NAME YET i", "Isolde", 9999, 9999); // TODO: Find these values
		isolde.addPortal("Drakontas", NNE, ENE); //TODO: Check these
		isolde.addPortal("Padzahr", WNW, NNW); //TODO: Check these
		isolde.addPortal("Ulca Felya", NNW, NNE); //TODO: Check these
		civcraftShards.put(isolde.worldName, isolde);
		
		// Padzahr
		ShardConfig padzahr = new ShardConfig("mc.civcraft.co", "NO WORLD NAME YET p", "Padzahr", 9999, 9999);  // TODO: Find these values
		padzahr.addPortal("Isolde", ESE, SSE); //TODO: Check these
		padzahr.addPortal("Eilon", NNW, NNE); //TODO: Check these
		civcraftShards.put(padzahr.worldName, padzahr);
		
		// Eilon
		ShardConfig eilon = new ShardConfig("mc.civcraft.co", "NO WORLD NAME YET e", "Eilon", 9999, 9999); // TODO: Find these values
		eilon.addPortal("Padzahr", SSE, SSW); //TODO: Check these
		eilon.addPortal("Abydos", NNW, NNE); //TODO: Check these
		civcraftShards.put(eilon.worldName, eilon);
		
		// Abydos
		ShardConfig abydos = new ShardConfig("mc.civcraft.co", "NO WORLD NAME YET a", "Abydos", 9999, 9999); // TODO: Find these values
		abydos.addPortal("Eilon", SSE, SSW); //TODO: Check these
		abydos.addPortal("Sheol", NNE, ENE); //TODO: Check these
		civcraftShards.put(abydos.worldName, abydos);
		
		// Sheol
		ShardConfig sheol = new ShardConfig("mc.civcraft.co", "NO WORLD NAME YET s", "Sheol", 9999, 9999); // TODO: Find these values
		sheol.addPortal("Abydos", SSW, WSW); //TODO: Check these
		sheol.addPortal("Tigrillo", ESE, SSE); //TODO: Check these
		civcraftShards.put(sheol.worldName, sheol);
		
		// Tigrillo
		ShardConfig tigrillo = new ShardConfig("mc.civcraft.co", "NO WORLD NAME YET t", "Tigrillo", 9999, 9999); // TODO: Find these values
		tigrillo.addPortal("Sheol", NNE, ENE); //TODO: Check these
		tigrillo.addPortal("Ulca Felya", SSE, SSW); //TODO: Check these
		civcraftShards.put(tigrillo.worldName, tigrillo);
		
		// Ulca Felya
		ShardConfig ulca = new ShardConfig("mc.civcraft.co", "NO WORLD NAME YET u", "Ulca Felya", 9999, 9999); // TODO: Find these values
		ulca.addPortal("Tigrillo", NNW, NNE); //TODO: Check these
		ulca.addPortal("Eilon", WSW, WNW); //TODO: Check these
		ulca.addPortal("Tjikko", ENE, ESE); //TODO: Check these
		ulca.addPortal("Drakontas", SSE, SSW); //TODO: Check these
		civcraftShards.put(ulca.worldName, ulca);
		
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
