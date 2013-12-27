package nl.taico.tekkitrestrict.objects;

public class TRDupeSettings {
	public boolean prevent, broadcast, kick, useCommand;
	public String command = "";
	public int triggerAfter;
	
	public TRDupeSettings(){}
	
	public TRDupeSettings(boolean prevent, boolean broadcast, boolean kick, boolean useCommand, int triggerAfter, String command){
		this.prevent = prevent;
		this.broadcast = broadcast;
		this.kick = kick;
		this.useCommand = useCommand;
		this.triggerAfter = triggerAfter;
		this.command = command;
	}

	@Override
	public String toString() {
		return "TRDupeSettings [prevent=" + prevent + ", broadcast=" + broadcast + ", kick=" + kick + ", useCommand=" + useCommand + ", command=" + command
				+ ", triggerAfter=" + triggerAfter + "]";
	}
}
