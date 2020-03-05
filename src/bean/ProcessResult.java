package bean;

import lombok.Data;

@Data
public class ProcessResult {
	private int exitValue = 0;
	private String result = "";
	private String error = "";
	private int statusCode = 0;
}
