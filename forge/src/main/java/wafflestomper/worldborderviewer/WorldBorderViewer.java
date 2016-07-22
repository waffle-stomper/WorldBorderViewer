package wafflestomper.worldborderviewer;

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
import net.minecraftforge.fml.common.gameevent.TickEvent.Phase;
import net.minecraftforge.fml.common.gameevent.TickEvent.RenderTickEvent;

@Mod(modid = WorldBorderViewer.MODID, version = WorldBorderViewer.VERSION, name = WorldBorderViewer.NAME)
public class WorldBorderViewer
{
	
    public static final String MODID = "WorldBorderViewer";
    public static final String VERSION = "0.2.8";
    public static final String NAME = "World Border Viewer";
    
    public static final int viewRadiusBlocks = 5*16; //Used for quick check
    private Minecraft mc = Minecraft.getMinecraft();
    private WBConfigManager config = new WBConfigManager();
    
	private int connectWait = 10;
	private boolean connected = false;
	boolean devEnv = false;
	private long lastMessage = 0;
	
	private double worldRadius = 1110d;
	private double portalRadius = 1100d;
	private int circleSegments = 4000;
	private double halfPortalAngle = 30d;
	private double renderDistance = 64d;
	private boolean enableDebug = false;
	
	
    public WorldBorderViewer(){
    	FMLCommonHandler.instance().bus().register(this);
		MinecraftForge.EVENT_BUS.register(this);
		// Detect development environment
		this.devEnv = (Boolean)Launch.blackboard.get("fml.deobfuscatedEnvironment");
    }
    
    
    @EventHandler
    public void preInit(FMLPreInitializationEvent event) {
    	this.config.init(event);
    	this.worldRadius = this.config.getWorldRadius();
		this.portalRadius = this.config.getPortalRadius();
		this.circleSegments = this.config.getCircleSegments();
		this.halfPortalAngle = this.config.getHalfPortalAngle();
		this.renderDistance = this.config.getRenderDistance();
		this.enableDebug = this.config.getEnableDebug();
    	
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
    		double playerAngle = Math.toDegrees(Math.asin(this.mc.thePlayer.posX/distFromCenter));
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
    	for (int cardinal=0; cardinal<=360; cardinal+=90){
    		double rightPortal = cardinal+this.halfPortalAngle;
    		if (rightPortal >= startAng && rightPortal <= endAng){
    			return(rightPortal);
    		}
    		double leftPortal = cardinal-this.halfPortalAngle;
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
    	angle = angle % 90;
    	if (angle < 45){
    		return(true);
    	}
    	return(false);
    }
    
    
    @SubscribeEvent
    public void rwle(RenderWorldLastEvent event){
    	EntityPlayerSP thePlayer = this.mc.thePlayer;
    	double playerX = thePlayer.lastTickPosX + (thePlayer.posX - thePlayer.lastTickPosX) * event.getPartialTicks();
        double playerY = thePlayer.lastTickPosY + (thePlayer.posY - thePlayer.lastTickPosY) * event.getPartialTicks();
        double playerZ = thePlayer.lastTickPosZ + (thePlayer.posZ - thePlayer.lastTickPosZ) * event.getPartialTicks();
        
    	float segmentAngle = 360.0f/circleSegments;
    	double minY = playerY-5;
    	double maxY = playerY+6.8;
    	
    	int nextPoint = 1;
    	double worldX1 = 0;
    	double worldZ1 = 0;
    	double worldX2 = 0;
    	double worldZ2 = 0;
    	double portalX1 = 0;
    	double portalZ1 = 0;
    	double portalX2 = 0;
    	double portalZ2 = 0;
    	
    	for (int currPoint=0; currPoint<circleSegments; currPoint++){
    		nextPoint = currPoint+1;
    		if (nextPoint == circleSegments){
    			nextPoint = 0;
    		}
    		double currTheta = Math.toRadians(currPoint*segmentAngle);
    		double nextTheta = Math.toRadians(nextPoint*segmentAngle);
    		worldX1 = worldRadius * Math.sin(currTheta);
    		worldZ1 = worldRadius * Math.cos(currTheta);
    		worldX2 = worldRadius * Math.sin(nextTheta);
    		worldZ2 = worldRadius * Math.cos(nextTheta);
    		
    		portalX1 = portalRadius * Math.sin(currTheta);
    		portalZ1 = portalRadius * Math.cos(currTheta);
    		portalX2 = portalRadius * Math.sin(nextTheta);
    		portalZ2 = portalRadius * Math.cos(nextTheta);
    		
    		// World border segment
    		if (!isAngleInPortal(currPoint*segmentAngle, halfPortalAngle) && !isAngleInPortal(nextPoint*segmentAngle, halfPortalAngle)){
	    		if (this.mc.thePlayer.getDistance(worldX1, this.mc.thePlayer.posY, worldZ1) <= renderDistance &&
	    				this.mc.thePlayer.getDistance(worldX2,  this.mc.thePlayer.posY, worldZ2) <= renderDistance){
	        		// Both points are within render distance. Render the square
	    			drawSquare(worldX1, minY, worldZ1, worldX2, maxY, worldZ2, 1.0F, 0, 0, playerX, playerY, playerZ);
	        	}
    		}
    		
    		// Portal segment
    		if (isAngleInPortal(currPoint*segmentAngle, halfPortalAngle) && isAngleInPortal(nextPoint*segmentAngle, halfPortalAngle)){
	    		if (this.mc.thePlayer.getDistance(portalX1, this.mc.thePlayer.posY, portalZ1) <= renderDistance &&
	    				this.mc.thePlayer.getDistance(portalX2,  this.mc.thePlayer.posY, portalZ2) <= renderDistance){
	        		// Both points are within render distance. Render the square
	    			drawSquare(portalX1, minY, portalZ1, portalX2, maxY, portalZ2, 0, 1.0F, 0, playerX, playerY, playerZ);
	        	}
    		}
    		
    		// Linking Z shape (radiating from the center of the map outward with two small legs to join up to the other borders)
    		if (isAngleInPortal(currPoint*segmentAngle, halfPortalAngle) != isAngleInPortal(nextPoint*segmentAngle, halfPortalAngle)){
    			if (this.mc.thePlayer.getDistance(worldX1,  this.mc.thePlayer.posY, worldZ1) <= renderDistance &&
    					this.mc.thePlayer.getDistance(worldX2,  this.mc.thePlayer.posY, worldZ2) <= renderDistance &&
    					this.mc.thePlayer.getDistance(portalX1, this.mc.thePlayer.posY, portalZ1) <= renderDistance &&
    					this.mc.thePlayer.getDistance(portalX2, this.mc.thePlayer.posY, portalZ2) <= renderDistance){	
    				// get the endpoints of the radiating line
    				double sideAngle = getInternalPortalAngle(currPoint*segmentAngle, nextPoint*segmentAngle);
    				if (sideAngle < 0){
    					continue;
    				}
    				double sideTheta = Math.toRadians(sideAngle);
    				double sideXp = portalRadius * Math.sin(sideTheta);
    	    		double sideZp = portalRadius * Math.cos(sideTheta);
    	    		double sideXw = worldRadius * Math.sin(sideTheta);
    	    		double sideZw = worldRadius * Math.cos(sideTheta);
    	    		
    			    // link the world border to the portal side
    	    		if(isLeftSide(sideAngle)){
    	    			drawSquare(sideXw, minY, sideZw, worldX2, maxY, worldZ2, 1.0F, 0, 0, playerX, playerY, playerZ);
    	    		}
    	    		else{
    	    			drawSquare(worldX1, minY, worldZ1, sideXw, maxY, sideZw, 1.0F, 0, 0, playerX, playerY, playerZ);
    	    		}
    	    		
    	    		// draw the portal side
    			    drawSquare(sideXp, minY, sideZp, sideXw, maxY, sideZw, 0, 1.0F, 0, playerX, playerY, playerZ);
    			    
    			    // link the portal to the side
    			    if(isLeftSide(sideAngle)){
    			    	drawSquare(portalX1, minY, portalZ1, sideXp, maxY, sideZp, 0, 1.0F, 0, playerX, playerY, playerZ);
    	    		}
    	    		else{
    	    			drawSquare(sideXp, minY, sideZp, portalX2, maxY, portalZ2, 0, 1.0F, 0, playerX, playerY, playerZ);
    	    		}
    			 }
    		}
    	}    
    }
    
    
    public WorldBorderViewer getinstance(){
    	return this;
    }
}


