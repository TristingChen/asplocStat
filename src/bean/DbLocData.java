package bean;

import org.nutz.dao.entity.annotation.*;

import lombok.Data;

@Data
@Table("aspireLocData")
public class DbLocData {

	@Id
	@Column("id")
	private Integer id;
	
	@Column("projectId")
	private Integer projectId;
	
	@Column("SvnRevisionId")
	private String versionId;
	
	@Column("SvnUser")
	private String user;
	
	@Column("SvnDate")
	private String locDate;
	
	@Column("SvnTime")
	private java.util.Date locTime;
	
	@Column("SvnPath")
	private String locPath;
	
	@Column("SvnFileType")
	private String fileType;
	
	@Column("SvnAction")
	private String action;
	
	@Column("SvnLineAdd")
	private Integer lineAdd;
	
	@Column("SvnLineDel")
	private Integer lineDel;
	
	@Column("SvnKind")
	private String kind = "file";
	
	@Column("SvnTextMods")
	private String textMods = "true";
	
	@Column("SvnPropMods")
	private String propMods = "false";
	
	@Column("addTime")
	private java.util.Date addTime;
	
	@Column("isEnable")
	private Integer isEnable = 1;
}