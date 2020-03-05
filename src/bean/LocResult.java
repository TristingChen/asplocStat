package bean;

import lombok.Data;

@Data
public class LocResult {
	private String fileType = "";
	private int lineAdd;
	private int lineDel;
	private String fileName = "";
}
