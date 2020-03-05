package bean;

import java.util.Date;
import java.util.HashMap;
import lombok.Data;

@Data
public class SvnLogEntry {
	private String version;
	private String author;
	private Date date;
	private HashMap<String, SvnLogFile> fileZ;
	
}
