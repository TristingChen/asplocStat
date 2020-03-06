package util;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import bean.*;
import org.apache.commons.dbcp.BasicDataSource;
import org.nutz.dao.Chain;
import org.nutz.dao.Cnd;
import org.nutz.dao.Condition;
import org.nutz.dao.Dao;
import org.nutz.dao.Sqls;
import org.nutz.dao.impl.NutDao;
import org.nutz.dao.sql.Sql;
import org.nutz.dao.sql.SqlCallback;

public class DbUtil {
	static Dao myDao = null;
	static BasicDataSource myDs = null;

	static SimpleDateFormat dateFmt2 = new SimpleDateFormat("yyyy-MM-dd");

	static List<String> skipFileTypeZ = new ArrayList<String>();
	
	public static Dao initDb() {
		if (myDao == null) {
			String sqlDriver = "org.gjt.mm.mysql.Driver";
			String sqlUrl = "jdbc:mysql://10.9.20.211:3306/zentao?useUnicode=true&characterEncoding=utf-8";
			String sqlUser = "zentao";
			String sqlPass = "wf6Q5XbH07kCz2rs";

			myDs = new BasicDataSource();
			myDs.setDriverClassName(sqlDriver);
			myDs.setUrl(sqlUrl);
			myDs.setUsername(sqlUser);
			myDs.setPassword(sqlPass);

			myDao = new NutDao(myDs);
		}
		
		loadConfigByTag("skipFileType");

		return myDao;
	}

	public static void closeDb() {
		if (myDs != null) {
			try {
				myDs.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
	}

	public static String[] getLastVersion(int projectId) {
		String[] result = new String[2];

		List<DbLocData> DbSvnLocDataZ = myDao.query(DbLocData.class, 
				Cnd.where("projectId", "=", projectId).desc("SvnTime"), myDao.createPager(1, 1));

		if (DbSvnLocDataZ.size() > 0) {
			for (DbLocData locData : DbSvnLocDataZ) {
				result[0] = locData.getVersionId();
				result[1] = dateFmt2.format(locData.getLocTime());
			}
		} else {
			result[0] = "0";
			result[1] = "2017-01-01";
		}

		return result;
	}
	
	public static String[] getLast(String projectId, String revisionId, String line) {
		String[] result = new String[2];

		List<DbLocData> DbSvnLocDataZ = myDao.query(DbLocData.class, 
				Cnd.where("projectId", "=", projectId).and("SvnRevisionId", "=", revisionId)
				.desc("addTime"), myDao.createPager(1, 1));

		if (DbSvnLocDataZ.size() > 0) {
			for (DbLocData locData : DbSvnLocDataZ) {
				System.out.println(projectId +"\t" + revisionId+ "\t" + line + "\t"
												+ dateFmt2.format(locData.getLocTime()));
			}
		} 

		return result;
	}

	public static List<DbLocProject> getProjectZfromDb() {
		//HashMap<Integer, String> projectZ = new HashMap<Integer, String>();

		List<DbLocProject> projectZ = myDao.query(DbLocProject.class, Cnd.orderBy().asc("build"));
		
//		for(DbLocProject prj : projectZ) {
//			int projectId = prj.getBuild();
//			String url = prj.getScmPath();
//			
//			System.out.println(projectId + "\t" + url);
//		}
		
		return projectZ;
	}
	
	public static List<DbLocReleaseJob> getReleaseJobFromDb() {
		//HashMap<Integer, String> projectZ = new HashMap<Integer, String>();

		List<DbLocReleaseJob> projectZ = myDao.query(DbLocReleaseJob.class, Cnd.where("isDone", "=", "0").asc("id"));
		
		return projectZ;
	}

	public static void saveLocData2Db(DbLocData data){
		myDao.insert(data);
	}
	
	public static void saveLocReleaseData2Db(int jobId, int projectId, LocResult result){
		
		DbLocReleaseData data = new DbLocReleaseData();
		data.setProjectId(projectId);
		data.setJobId(Integer.toString(jobId));

		data.setLocPath(result.getFileName());
		data.setFileType(result.getFileType());

		data.setLineAdd(result.getLineAdd());
		data.setLineDel(result.getLineDel());

		data.setAction("M");
		
		myDao.insert(data);
	}
	
	public static void saveLocReleaseJob2Db(DbLocReleaseJob job){
		// д�����ݿ⣬���job
		job.setIsDone(1);
		job.setFinishTime(new Date());
		
		myDao.update(job);
	}
	
	public static List<String> loadVersionByProject(int projectId) {
		
		Sql sql = Sqls.create("SELECT SvnRevisionId FROM aspireLocData WHERE projectId = @projectId "
										+ "GROUP BY SvnRevisionId, SvnTime ORDER BY SvnTime");
		 sql.params().set("projectId", projectId);
		 
		sql.setCallback(new SqlCallback() {
			public Object invoke(Connection conn, ResultSet rs, Sql sql) throws SQLException {
				List<String> list = new LinkedList<String>();
				while (rs.next())
					list.add(rs.getString("SvnRevisionId"));
				return list;
			}
		});
		myDao.execute(sql);

		return sql.getList(String.class);
	}
	
	public static List<String> loadBig5000() {
			
			Sql sql = Sqls.create("SELECT projectId, SvnRevisionId, sum(`SvnLineAdd`) as sla FROM `aspireLocData` "
					+ "WHERE isEnable=1 group by projectId, SvnRevisionId having sla>5000 order by sla desc");
			
			sql.setCallback(new SqlCallback() {
				public Object invoke(Connection conn, ResultSet rs, Sql sql) throws SQLException {
					List<String> list = new LinkedList<String>();
					while (rs.next())
						list.add(rs.getString("projectId") +  "\t" + rs.getString("SvnRevisionId") +  "\t" + rs.getString("sla"));
					return list;
				}
			});
			myDao.execute(sql);

			return sql.getList(String.class);
	}
	
	public static List<String> loadConfigByTag(String tag) {
		if(skipFileTypeZ.size() == 0 ) {
			List<DbLocConfig> configZ = myDao.query(DbLocConfig.class, Cnd.where("tag", "=", tag));
			
			for(DbLocConfig cc : configZ) {
				String data = cc.getData().toLowerCase();
				if(!skipFileTypeZ.contains(data))
					skipFileTypeZ.add(data);
			}
		}
		
		UtilZ.log("SkipFileTypeNum: " + skipFileTypeZ.size());
		
		return skipFileTypeZ;
	}
	
	public static boolean checkIsSkipFile(String path) {
		boolean isSkipThisFile = false;
		
		String fpath = path.toLowerCase();
		
		for(String skip : skipFileTypeZ) {
			if(fpath.endsWith(skip)) {
				isSkipThisFile = true;
			}
		}
		
		return isSkipThisFile;
	}
	
	public static void disAbleByVersion(int projectId, String versionId){
		Condition cnd  = Cnd.where("projectId", "=", projectId).and("SvnRevisionId", "=", versionId);
		
		myDao.update(DbLocData.class, Chain.make("isEnable", 0), cnd);
	}

	public static void saveLocPathLog(int build, ResultBean resultBean){
		DbLocProjectLogs dbLocProjectLogs = new DbLocProjectLogs();
		//先进行已有表数据的删除 然后插入
		myDao.clear(dbLocProjectLogs.getClass(),Cnd.where("build","=",build));
		dbLocProjectLogs.setBuild(build);
		dbLocProjectLogs.setMsg(resultBean.getMsg());
		dbLocProjectLogs.setStatus(dbLocProjectLogs.getStatus());
		myDao.insert(dbLocProjectLogs);
	}
}
