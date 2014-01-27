package nl.taico.tekkitrestrict.logging;

import java.util.Locale;

public class TRLogFilterPlus {
	public enum TRFilterMethod{
		STARTS_WITH, ENDS_WITH, CONTAINS, EQUALS;
		public boolean caseSensitive;
		TRFilterMethod(){
			caseSensitive = false;
		}
		TRFilterMethod(boolean caseSensitive){
			this.caseSensitive = caseSensitive;
		}
		
		public boolean isCaseSensitive(){
			return this.caseSensitive;
		}
		public boolean isCS(){
			return this.caseSensitive;
		}
		
		public static TRFilterMethod byName(String name){
			name = name.replace(" ", "").replace("_", "").toUpperCase();
			for (TRFilterMethod m : TRFilterMethod.values()){
				if (name.equalsIgnoreCase(m.name().replace("_", ""))) return m;
			}
			return null;
		}
	}
	
	public static boolean matches(TRFilterMethod method, String input, String filter){
		switch (method){
		case CONTAINS:
			if (method.isCS()) return input.contains(filter);
			else return input.toLowerCase(Locale.ENGLISH).matches(filter.toLowerCase(Locale.ENGLISH));
		case ENDS_WITH:
			if (method.isCS()) return input.endsWith(filter);
			else return input.toLowerCase(Locale.ENGLISH).endsWith(filter.toLowerCase(Locale.ENGLISH));
		case EQUALS:
			if (method.isCS()) return input.equals(filter);
			else return input.equalsIgnoreCase(filter);
		case STARTS_WITH:
			if (method.isCS()) return input.startsWith(filter);
			else return input.toLowerCase(Locale.ENGLISH).startsWith(filter.toLowerCase(Locale.ENGLISH));
		default:
			return false;
		
		}
	}
	
	
}
