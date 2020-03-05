package bean;

import org.nutz.dao.entity.annotation.*;

import lombok.Data;

@Data
@Table("aspireLocJobByRelease")
public class DbLocReleaseJob {
	
	@Id
	@Column("id")
	private Integer jobId;

	@Column("projectId")
	private Integer projectId;
	
	@Column("SvnUri")
	private String ScmUri;
	
	@Column("ReleaseOld")
	private String releaseOld;
	
	@Column("ReleaseNew")
	private String releaseNew;
	
	@Column("isDone")
	private Integer isDone;
	
	@Column("FinishTime")
	private java.util.Date finishTime;
	
}