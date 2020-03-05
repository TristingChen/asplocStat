package bean;

import org.nutz.dao.entity.annotation.*;

import lombok.Data;

@Data
@Table("aspireLocDataByRelease")
public class DbLocReleaseData {

	@Id
	@Column("id")
	private Integer id;
	
	@Column("projectId")
	private Integer projectId;
	
	@Column("JobId")
	private String jobId;
	
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
	
	@Column("addTime")
	private java.util.Date addTime;
	
	@Column("isEnable")
	private Integer isEnable = 1;
}