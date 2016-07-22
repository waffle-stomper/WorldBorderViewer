package wafflestomper.worldborderviewer;

import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

public class WBConfigManager {
	
	private Configuration config;
	
	private double worldRadius = 1110d;
	private double portalRadius = 1100d;
	private int circleSegments = 5000;
	private double halfPortalAngle = 30d;
	private double renderDistance = 4*16;//4 chunks 
	private boolean enableDebug = false;
	
	
	public WBConfigManager(){
	}
	
	
	public void init(FMLPreInitializationEvent event){
	    this.config = new Configuration(event.getSuggestedConfigurationFile());
	    
	    // Load data and/or set defaults
	    this.config.load();
	    this.worldRadius = this.config.get("world", "world_radius", 1110d, "World radius").getDouble(1110d);
	    this.portalRadius = this.config.get("world", "portal_radius", 1100d, "Portal radius").getDouble(1100d);
	    this.halfPortalAngle = this.config.get("world", "shard_portal_angle", 60d, "Angle in degrees representing "+
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
    }
	
	
	public double getWorldRadius(){
		return(this.worldRadius);
	}
	
	
	public double getPortalRadius(){
		return(this.portalRadius);
	}
	
	
	public int getCircleSegments(){
		return(this.circleSegments);
	}
	
	
	public double getHalfPortalAngle(){
		return(this.halfPortalAngle);
	}
	
	
	public double getRenderDistance(){
		return(this.renderDistance);
	}
	
	
	public boolean getEnableDebug(){
		return(this.enableDebug);
	}
}
