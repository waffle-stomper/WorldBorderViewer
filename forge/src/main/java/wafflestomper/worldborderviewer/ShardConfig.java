package wafflestomper.worldborderviewer;

import java.util.ArrayList;

public class ShardConfig {
	
	public String serverName;
	public String worldName;
	public String friendlyWorldName;
	public double portalRadius;
	public double worldRadius;
	public ArrayList<PortalConfig> portals;
	
	public ShardConfig(String _serverName, String _worldName, String _friendlyWorldName, double _portalRadius, double _worldRadius){
		this.serverName = _serverName;
		this.worldName = _worldName;
		this.friendlyWorldName = _friendlyWorldName;
		this.portalRadius = _portalRadius;
		this.worldRadius = _worldRadius;
		this.portals = new ArrayList<PortalConfig>();
	}
	
	public void addPortal(String name, double fromAngle, double toAngle){
		this.portals.add(new PortalConfig(name, fromAngle, toAngle));
	}
}
