package run;

import java.util.List;

import bean.DbLocData;
import bean.ResultBean;
import bean.SvnLogEntry;
import util.DbUtil;
import util.SvnLocResult;
import util.SvnLog;
import util.UtilZ;

public class SvnLoc {
	private static ResultBean<java.io.Serializable> resultBean = new ResultBean<>();

	public static ResultBean getLoc(int projectId, String url) {
		
		//2、 从数据库读取该项目ID之前的日期、versionID等
		String[] lastVersion = DbUtil.getLastVersion(projectId);
		int lastSvnVerId = Integer.parseInt(lastVersion[0]);
		String lastDate = lastVersion[1];

		UtilZ.log("LastVer: " + lastSvnVerId + "@"+ lastDate);
		
		// 3、 根据之前日期，以及当前日期，通过svn log，获得xml，如果时间间隔长，以月为间隔，最早时间2016-8-1
		String nowDate = UtilZ.getNextDay();

		// 通过svn log，获得xml，比如"data\\2016-08-01_2016-09-03.xml";//
		ResultBean logFileNameResultBean = SvnLog.getLogFromSVNServer(projectId, lastSvnVerId, lastDate, nowDate, url);
		if(logFileNameResultBean.getStatus() == -1){
			//svn_log脚本执行失败 进行locpathlog数据存入
			DbUtil.saveLocPathLog(projectId,logFileNameResultBean);
			return logFileNameResultBean;
		}
		String logFileName = (String)logFileNameResultBean.getData();
		//String logFileName = "data/11/2016-08-01_2016-09-08.xml";

		ResultBean svnEntryZResultBean = SvnLog.getSvnLogFromFile(logFileName);
		if(svnEntryZResultBean.getStatus() == -1){
			DbUtil.saveLocPathLog(projectId,svnEntryZResultBean);
			return logFileNameResultBean;
		}
		List<SvnLogEntry> svnEntryZ = (List<SvnLogEntry>)svnEntryZResultBean.getData();
		int count = 0;
		int total = 0;
		for (SvnLogEntry entry : svnEntryZ) {
			int versionId = Integer.parseInt(entry.getVersion());
			
			if(versionId <= lastSvnVerId) {
				UtilZ.log("Skip: r" + versionId  + "@" + projectId + " " + entry.getAuthor()
								+ " " + UtilZ.toLongStr(entry.getDate()) );
				continue;
			}
			else {
				count++;
				UtilZ.log("Check: r" + versionId  + "@" + projectId + " " + entry.getAuthor()
								+ " " + UtilZ.toLongStr(entry.getDate()) );
				
				List<DbLocData> dataZ = SvnLocResult.runLocOneEntry(projectId, url, entry);
		
				int vCount = 0;
				// 写入svnLOC数据表
				for (DbLocData data : dataZ) {
						UtilZ.log("LocData: r" + data.getVersionId() + "\t" + data.getUser()
									+ "\t" + UtilZ.toLongStr(data.getLocTime()) + "\t" + data.getAction()
									+ "\t" + data.getLocPath() + "\t" + data.getLineAdd() + "\t" + data.getLineDel());
						
						vCount += data.getLineAdd();
						//data.setProjectId(projectId);
						
						DbUtil.saveLocData2Db(data);
						
						total++;
				}
				
				int entrySize = entry.getFileZ().size();
				
				UtilZ.log("Result: r" + versionId  + "@" + projectId + "\tTotal: " +entrySize + "\t"
							+ "OkLoc: " + dataZ.size() + "\tNoLoc: " + (entrySize - dataZ.size()) + "\tLoc: " + vCount);
				
				if(vCount > 5000) {
					UtilZ.log("disAbleOver5000Loc:  r" + versionId  + "@" + projectId);
					
					DbUtil.disAbleByVersion(projectId, versionId+"");
				}
			}
		}
		//插入locpathlog数据为成功

		UtilZ.log("Finish: " + projectId + ", rAll: " + count + ", locAll: " + total);
		return resultBean.resultOnly("");
	}
}
