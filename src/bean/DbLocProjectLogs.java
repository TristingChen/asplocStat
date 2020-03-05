package bean;

import org.nutz.dao.entity.annotation.*;

import lombok.Data;

/**
 *
 */
@Data
@Table("zt_aspire_locPathLogs")
public class DbLocProjectLogs {
    /**
     *
     */
    @Column("build")
    private Integer build;
    /**
     *
     */
    @Column("status")
    private Integer status = 0;
    /**
     *
     */
    @Column("msg")
    private String msg;
}