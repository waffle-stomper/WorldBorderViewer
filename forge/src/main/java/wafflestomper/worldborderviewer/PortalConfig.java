package wafflestomper.worldborderviewer;

public class PortalConfig {
	
	public String name;
	public double angleFrom;
	public double angleTo;
	
	
	public PortalConfig(String _name, double _angleFrom, double _angleTo){
		this.name = _name;
		this.angleFrom = rotate180(_angleFrom);
		this.angleTo = rotate180(_angleTo);
	}
	
	
	/**
	 * This is a hacky fix until I get around to fixing the rendering code so that 0 degrees is north
	 */
	private double rotate180(double inAngle){
		return((inAngle+180d)%360);
	}
}
