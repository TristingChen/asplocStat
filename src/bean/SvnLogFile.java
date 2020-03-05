package bean;

import lombok.Data;

@Data
public class SvnLogFile {
	private String path;
	private String action;
	private String kind;
	private String propMods;
	private String textModes;
	
	private int lineAdd;
	private int lineDel;
}
