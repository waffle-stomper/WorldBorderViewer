package wafflestomper.worldborderviewer;

import java.util.HashMap;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.opengl.GL11;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.VertexBuffer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.launchwrapper.Launch;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import wafflestomper.wafflecore.WaffleCore;
import wafflestomper.wafflecore.WorldInfoEvent;

@Mod(modid = WorldBorderViewer.MODID, version = WorldBorderViewer.VERSION, name = WorldBorderViewer.NAME, 
		dependencies = "required-after:WaffleCore",
		updateJSON = "https://raw.githubusercontent.com/waffle-stomper/WorldBorderViewer/master/update.json")
public class WorldBorderViewer
{
	
    public static final String MODID = "WorldBorderViewer";
    public static final String VERSION = "0.3.2";
    public static final String NAME = "World Border Viewer";
    
    public static final int viewRadiusBlocks = 5*16; //Used for quick check
    private Minecraft mc = Minecraft.getMinecraft();
    private WBConfigManager config = new WBConfigManager();
    private static final Logger logger = LogManager.getLogger("WorldBorderViewer");
    private static final WaffleCore wafflecore = WaffleCore.INSTANCE;
    
	private int connectWait = 10;
	private boolean connected = false;
	boolean devEnv = false;
	private long lastMessage = 0;
	
	private double defaultWorldRadius = 1110d;
	private double defaultPortalRadius = 1100d;
	private int circleSegments = 4000;
	private double defaultHalfPortalAngle = 30d;
	private double renderDistance = 64d;
	private boolean enableDebug = false;
	
	private static String currentServerName;
	private static String currentWorldName;
	
	private ShardConfig currentShard;
	
	
    public WorldBorderViewer(){
    	FMLCommonHandler.instance().bus().register(this);
		MinecraftForge.EVENT_BUS.register(this);
		// Detect development environment
		this.devEnv = (Boolean)Launch.blackboard.get("fml.deobfuscatedEnvironment");
    }
    
    
    @EventHandler
    public void preInit(FMLPreInitializationEvent event) {
    	this.config.init(event);
    	this.defaultWorldRadius = this.config.getDefaultWorldRadius();
		this.defaultPortalRadius = this.config.getDefaultPortalRadius();
		this.circleSegments = this.config.getCircleSegments();
		this.defaultHalfPortalAngle = this.config.getDefaultHalfPortalAngle();
		this.renderDistance = this.config.getRenderDistance();
		this.enableDebug = this.config.getEnableDebug();
    }
    
    
    /**
     * Server/world info messages (mana from heaven)
     */
    @SubscribeEvent
    public void worldInfoReceived(WorldInfoEvent event){
    	logger.debug("Received WorldInfoEvent - " + event.worldID + "@" + event.dirtyServerAddress);
    	currentServerName = event.cleanServerAddress;
    	currentWorldName = event.worldID;
    }
    
    
    /**
     * Rate Limited Debug Message
     */
    private static final long rldmInterval = 5000;
    HashMap<String, Long> rldmLastTimes = new HashMap<String, Long>();
    public void rldm(String message){
    	if (this.rldmLastTimes.containsKey(message)){
    		long rldmLast = rldmLastTimes.get(message);
    		if (System.currentTimeMillis()-rldmLast < rldmInterval){ return; }
    	}
    	this.rldmLastTimes.put(message, System.currentTimeMillis());
    	logger.warn("RLDM: " + message);
    }
    
    
    /**
     * Check to make sure that both ends fall within the set of loaded chunks
     */
    public boolean shouldDrawLineSeg(double x1, double z1, double x2, double z2, double y){
    	BlockPos b1 = new BlockPos(x1, y, z1);
    	BlockPos b2 = new BlockPos(x2, y, z2);
    	return(this.mc.theWorld.isBlockLoaded(b1, false)&&this.mc.theWorld.isBlockLoaded(b2, false));
    }
    
    
    public boolean isAngleInPortal(double theta, double halfPortalAngle){
    	if (this.currentShard != null){
			for (PortalConfig p : this.currentShard.portals){
				// Check if theta lies within angleFrom and angleTo
				if (Double.compare(theta, p.angleFrom) >= 0 && Double.compare(theta, p.angleTo) <=0){
					return true;
				}
				// Special case where the portal passes through 0 degrees (thus angleFrom is higher numerically than angleTo)
				if (Double.compare(p.angleFrom, p.angleTo) > 0){
					if (Double.compare(theta, p.angleFrom) >= 0){
						return true;
					}
					else if (Double.compare(theta, p.angleTo) <= 0){
						return true;
					}
				}
			}
			return false;
    	}
    	
    	// If the current server or shard can't be found in the config, we fall back on the default values    	
    	if (theta > 360d-halfPortalAngle || theta < halfPortalAngle){
    		return true;
    	}
    	if (theta > 90-halfPortalAngle && theta < 90+halfPortalAngle){
    		return true;
    	}
    	if (theta > 180-halfPortalAngle && theta < 180+halfPortalAngle){
    		return true;
    	}
    	if (theta > 270-halfPortalAngle && theta < 270+halfPortalAngle){
    		return true;
    	}
    	return false;
    }
    
    
    /**
     * Debugging text renderer
     */
    @SubscribeEvent
    public void renderGuiText(RenderGameOverlayEvent event){
    	if (this.enableDebug == false){
    		return;
    	}
    	if (event.getType() == RenderGameOverlayEvent.ElementType.TEXT){
    		EntityPlayerSP player = this.mc.thePlayer;
    		if (player == null){
    			return;
    		}
    		double distFromCenter = player.getDistance(0, player.posY, 0);
    		double playerAngle = Math.toDegrees(Math.PI - Math.atan2(player.posX, player.posZ));
    		String angleText = String.valueOf(playerAngle);
    		String centerText = String.valueOf(distFromCenter);
    		int maxWidth = angleText.lastIndexOf('.')+2;
    		if (maxWidth < angleText.length()-1){
    			angleText = angleText.substring(0, maxWidth);
    		}
    		maxWidth = centerText.lastIndexOf('.') + 2;
    		if (maxWidth < centerText.length()-1){
    			centerText = centerText.substring(0, maxWidth);
    		}
    		String displayString = angleText + " deg        " + centerText + " m";
    		if (currentWorldName != null){
    			displayString += "        " + currentWorldName.substring(0,10) + "...";
    		}
    		else{
    			displayString += "no_world_name";
    		}
    		int textWidth = this.mc.fontRendererObj.getStringWidth(displayString);
    		ScaledResolution scaledResolution = new ScaledResolution(this.mc);
      		int xPos = scaledResolution.getScaledWidth()/2 - textWidth/2;
    		this.mc.fontRendererObj.drawString(displayString, xPos, 5, 0xFFFF0000);
    	}
    }
    
    
    private void drawSquare(double x1, double y1, double z1, double x2, double y2, double z2, float red, float green, float blue,
    		double playerX, double playerY, double playerZ){
    	
    	Tessellator tessellator = Tessellator.getInstance();
    	VertexBuffer vertexBuffer = tessellator.getBuffer();
        
    	// New 'pane' (pain) code
		GlStateManager.pushMatrix();
        GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
        GlStateManager.disableTexture2D();
		GlStateManager.depthMask(false);
        GlStateManager.color(red, green, blue, 0.2F);
        vertexBuffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);
        GlStateManager.translate(-playerX, -playerY, -playerZ);
        GlStateManager.scale(1.0D, 1.0D, 1.0D);
        float f3 = (float)(Minecraft.getSystemTime() % 3000L) / 3000.0F;
        float f7 = 0.0F;
        
        // clockwise (panes seen from 'inside')
        vertexBuffer.pos(x1, y1, z1).tex((double)(f3 + f7), (double)(f3 + 0.0F)).endVertex(); //Origin
        vertexBuffer.pos(x1, y2, z1).tex((double)(f3 + f7), (double)(f3 + 0.0F)).endVertex(); //Up
        vertexBuffer.pos(x2,  y2,  z2).tex((double)(f3 + f7), (double)(f3 + 0.0F)).endVertex(); //Up and out
        vertexBuffer.pos(x2, y1, z2).tex((double)(f3 + f7), (double)(f3 + 0.0F)).endVertex(); //Out
        
        // counter clockwise (panes seen from 'outside')
        vertexBuffer.pos(x1, y1, z1).tex((double)(f3 + f7), (double)(f3 + 0.0F)).endVertex(); //Origin
        vertexBuffer.pos(x2, y1, z2).tex((double)(f3 + f7), (double)(f3 + 0.0F)).endVertex(); //Out
        vertexBuffer.pos(x2,  y2,  z2).tex((double)(f3 + f7), (double)(f3 + 0.0F)).endVertex(); //Up and out
        vertexBuffer.pos(x1, y2, z1).tex((double)(f3 + f7), (double)(f3 + 0.0F)).endVertex(); //Up
        
        tessellator.draw();
        GlStateManager.disableBlend();
        GlStateManager.enableTexture2D();
        GlStateManager.depthMask(true);
        GlStateManager.popMatrix();
    }
    
    
    /**
     * Finds the portal angle inside the two angles supplied
     */
    private double getInternalPortalAngle(double startAng, double endAng){
    	if (this.currentShard != null){
			for (PortalConfig p : this.currentShard.portals){
				if (Double.compare(startAng, p.angleFrom) <= 0 && Double.compare(endAng, p.angleFrom) >= 0){
					return(p.angleFrom);
				}
				if (Double.compare(startAng, p.angleTo) <= 0 && Double.compare(endAng, p.angleTo) >= 0){
					return(p.angleTo);
				}
			}
		}
    	
    	// If we can't find a matching shard config, we fall back on the default behavior
    	for (int cardinal=0; cardinal<=360; cardinal+=90){
    		double rightPortal = cardinal+this.defaultHalfPortalAngle;
    		if (rightPortal >= startAng && rightPortal <= endAng){
    			return(rightPortal);
    		}
    		double leftPortal = cardinal-this.defaultHalfPortalAngle;
    		if (leftPortal < 0){ leftPortal+= 360; }
    		if (leftPortal >= startAng && leftPortal <= endAng){
    			return(leftPortal);
    		}
    	}
    	return(-1.0D);
    }
    
    
    /**
     * Determines whether we're working on the left or right portal side (when viewed from the center)
     */
    private boolean isLeftSide(double angle){
    	if (this.currentShard != null){
			for (PortalConfig p : this.currentShard.portals){
				if (Double.compare(angle, p.angleFrom) == 0){
					return true;
				}
				else if (Double.compare(angle, p.angleTo) == 0){
					return false;
				}
			}
		}

    	angle = angle % 90;
    	if (angle > 45){
    		return(true);
    	}
    	return(false);
    }
    
    
    @SubscribeEvent
    public void rwle(RenderWorldLastEvent event){
    	
    	double worldRadius = this.defaultWorldRadius;
    	double portalRadius = this.defaultPortalRadius;
    	
    	this.currentShard = null;
    	if (currentServerName != null && currentWorldName != null){
	    	if (this.config.shardsByServer.containsKey(currentServerName)){
	    		HashMap<String, ShardConfig> currShards = this.config.shardsByServer.get(currentServerName);
	    		if (currShards.containsKey(currentWorldName)){
	    			this.currentShard = currShards.get(currentWorldName);
	    			worldRadius = this.currentShard.worldRadius;
	    			portalRadius = this.currentShard.portalRadius;
	    		}
	    	}
    	}
    	
    	EntityPlayerSP thePlayer = this.mc.thePlayer;
    	double playerX = thePlayer.lastTickPosX + (thePlayer.posX - thePlayer.lastTickPosX) * event.getPartialTicks();
        double playerY = thePlayer.lastTickPosY + (thePlayer.posY - thePlayer.lastTickPosY) * event.getPartialTicks();
        double playerZ = thePlayer.lastTickPosZ + (thePlayer.posZ - thePlayer.lastTickPosZ) * event.getPartialTicks();
        
    	double segmentAngle = 360.0d/circleSegments;
    	double minY = playerY-20;
    	double maxY = playerY+20;
    	
    	int nextPoint = 1;
    	double worldX1 = 0;
    	double worldZ1 = 0;
    	double worldX2 = 0;
    	double worldZ2 = 0;
    	double portalX1 = 0;
    	double portalZ1 = 0;
    	double portalX2 = 0;
    	double portalZ2 = 0;
    	
    	for (int currPoint=0; //currPoint <=10; currPoint++){ 
    						  currPoint<circleSegments; currPoint++){
    		nextPoint = currPoint+1;
    		if (nextPoint == circleSegments){
    			nextPoint = 0;
    		}
    		double currTheta = Math.toRadians((540d-currPoint*segmentAngle)%360d);
    		double nextTheta = Math.toRadians((540d-nextPoint*segmentAngle)%360d);
    		worldX1 = worldRadius * Math.sin(currTheta);
    		worldZ1 = worldRadius * Math.cos(currTheta);
    		worldX2 = worldRadius * Math.sin(nextTheta);
    		worldZ2 = worldRadius * Math.cos(nextTheta);
    		
    		portalX1 = portalRadius * Math.sin(currTheta);
    		portalZ1 = portalRadius * Math.cos(currTheta);
    		portalX2 = portalRadius * Math.sin(nextTheta);
    		portalZ2 = portalRadius * Math.cos(nextTheta);
    		
    		// World border segment
    		if (!isAngleInPortal(currPoint*segmentAngle, defaultHalfPortalAngle) && !isAngleInPortal(nextPoint*segmentAngle, defaultHalfPortalAngle)){
	    		if (this.mc.thePlayer.getDistance(worldX1, this.mc.thePlayer.posY, worldZ1) <= renderDistance &&
	    				this.mc.thePlayer.getDistance(worldX2,  this.mc.thePlayer.posY, worldZ2) <= renderDistance){
	        		// Both points are within render distance. Render the square
	    			drawSquare(worldX1, minY, worldZ1, worldX2, maxY, worldZ2, 1.0F, 0, 0, playerX, playerY, playerZ);
	        	}
    		}
    		
    		// Portal segment
    		if (isAngleInPortal(currPoint*segmentAngle, defaultHalfPortalAngle) && isAngleInPortal(nextPoint*segmentAngle, defaultHalfPortalAngle)){
	    		if (this.mc.thePlayer.getDistance(portalX1, this.mc.thePlayer.posY, portalZ1) <= renderDistance &&
	    				this.mc.thePlayer.getDistance(portalX2,  this.mc.thePlayer.posY, portalZ2) <= renderDistance){
	        		// Both points are within render distance. Render the square
	    			drawSquare(portalX1, minY, portalZ1, portalX2, maxY, portalZ2, 0, 1.0F, 0, playerX, playerY, playerZ);
	        	}
    		}
    		
    		
    		// Linking Z shape (radiating from the center of the map outward with two small legs to join up to the other borders)
    		if (isAngleInPortal(currPoint*segmentAngle, defaultHalfPortalAngle) != isAngleInPortal(nextPoint*segmentAngle, defaultHalfPortalAngle)){
    			if (this.mc.thePlayer.getDistance(worldX1,  this.mc.thePlayer.posY, worldZ1) <= renderDistance &&
    					this.mc.thePlayer.getDistance(worldX2,  this.mc.thePlayer.posY, worldZ2) <= renderDistance &&
    					this.mc.thePlayer.getDistance(portalX1, this.mc.thePlayer.posY, portalZ1) <= renderDistance &&
    					this.mc.thePlayer.getDistance(portalX2, this.mc.thePlayer.posY, portalZ2) <= renderDistance){	
    				
    				// get the endpoints of the radiating line
    				double sideAngle = getInternalPortalAngle(currPoint*segmentAngle, nextPoint*segmentAngle);
    				
    				if (sideAngle < 0){
    					continue;
    				}
    				
    				double sideTheta = Math.toRadians((540d-sideAngle)%360d);
    				double sideXp = portalRadius * Math.sin(sideTheta);
    	    		double sideZp = portalRadius * Math.cos(sideTheta);
    	    		double sideXw = worldRadius * Math.sin(sideTheta);
    	    		double sideZw = worldRadius * Math.cos(sideTheta);
    	    		
    			    // link the world border to the portal side
    	    		if(isLeftSide(sideAngle)){
    	    			drawSquare(worldX1, minY, worldZ1, sideXw, maxY, sideZw, 1.0F, 0, 0, playerX, playerY, playerZ);
    	    		}
    	    		else{
    	    			drawSquare(sideXw, minY, sideZw, worldX2, maxY, worldZ2, 1.0F, 0, 0, playerX, playerY, playerZ);
    	    		}
    	    		
    	    		// draw the portal side
    			    drawSquare(sideXp, minY, sideZp, sideXw, maxY, sideZw, 0, 1.0F, 0, playerX, playerY, playerZ);
    			    
    			    // link the portal to the side
    			    if(isLeftSide(sideAngle)){
    	    			drawSquare(sideXp, minY, sideZp, portalX2, maxY, portalZ2, 0, 1.0F, 0, playerX, playerY, playerZ);
    	    		}
    	    		else{
    			    	drawSquare(portalX1, minY, portalZ1, sideXp, maxY, sideZp, 0, 1.0F, 0, playerX, playerY, playerZ);
    	    		}
    			 }
    		}
    	}    
    }
    
    
    public WorldBorderViewer getinstance(){
    	return this;
    }
}


