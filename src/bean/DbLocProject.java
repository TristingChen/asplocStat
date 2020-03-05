package bean;

import org.nutz.dao.entity.annotation.*;

import lombok.Data;

/**
* 
*/
@Data
@Table("zt_aspire_locPath")
public class DbLocProject {

	/**
	 * 
	 */
	@Column("bizunit")
	private Integer bizunit;
	/**
	 * 
	 */
	@Column("build")
	private Integer build;
	/**
	 * 
	 */
	@Column("scmPath")
	private String scmPath;
	/**
	 * 
	 */
	@Column("createtime")
	private java.util.Date createtime;
	/**
	 * 
	 */
	@Column("updatetime")
	private java.util.Date updatetime;
}