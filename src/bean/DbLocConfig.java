package bean;

import org.nutz.dao.entity.annotation.*;

import lombok.Data;

@Data
@Table("aspireLocConfig")
public class DbLocConfig {

	@Id
	@Column("id")
	private Integer id;
	
	@Column("tag")
	private String tag;
	
	@Column("data")
	private String data;
	
	@Column("isEnable")
	private Integer isEnable = 1;
}