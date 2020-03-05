package run;

import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import org.nutz.lang.Files;

import bean.DbLocData;
import bean.SvnLogEntry;
import bean.SvnLogFile;
import util.DbUtil;
import util.GitLog;
import util.UtilZ;

public class GitLoc {
	static SimpleDateFormat dateFmt2 = new SimpleDateFormat("yyyy-MM-dd");
	
	public static void getLoc(int projectId, String url) {
		
		//深圳IDC无法访问112.33.1.20
		url = url.replace("112.33.1.20", "10.9.20.25");
		
		String[] lastVersion = DbUtil.getLastVersion(projectId);
		String lastVer16 = lastVersion[0];
		String lastDate = lastVersion[1];
		
		if(lastVer16.length() >0 && !lastVer16.equals("0")) 
			lastVer16 = lastVer16.substring(0, 16);

		UtilZ.log("LastVer: " + lastVer16 + "@" + lastDate);

		String nowDate = UtilZ.getNextDay();

		String logFileName = GitLog.getGitNumFromServer(projectId, lastDate, nowDate, url);
		HashMap<String, SvnLogEntry> entryZ = GitLog.getGitLogFromFile(logFileName);

		// 生成amd的actionMap
		String actFileName = GitLog.getGitActionFromServer(projectId, lastDate, nowDate, url);
		HashMap<String, SvnLogEntry> actionZ = GitLog.getGitLogFromFile(actFileName);

		//由于git的version不是数字，而是字符串，因此需要从数据库加载当前项目的所有versionId
		List<String> versionZinDb = DbUtil.loadVersionByProject(projectId);
		
		String[] sortedVersionZ = sortKeyZ(entryZ.keySet());
		//对version进行倒排序，默认是按照时间新->旧，重新排序后为旧->新
		
		int count = 0;
		int total = 0;
		for (String version : sortedVersionZ) {
			SvnLogEntry entry = entryZ.get(version);
			String version16 = version.substring(0,16);
			
			if(versionZinDb.contains(version)){
				//如果是已经存在的verson，则跳过
				UtilZ.log("Skip: g." + version16  + "@" + projectId + " " + entry.getAuthor()
								+ " " + UtilZ.toLongStr(entry.getDate()) );
				continue;
			} else {
				count++;
				UtilZ.log("Check: g." + version16  + "@" + projectId + " " + entry.getAuthor()
								+ " " + UtilZ.toLongStr(entry.getDate()) );
				
				SvnLogEntry action = actionZ.get(version);
				
				//UtilZ.log(version + "\t" + entry.getAuthor() + "\t" + UtilZ.toLongStr(entry.getDate()));
	
				// 获取loc的map
				HashMap<String, SvnLogFile> entryFileZ = entry.getFileZ();
				// 获取amd的map
				HashMap<String, SvnLogFile> actionFileZ = action.getFileZ();
				
				int vCount = 0;
				for (String key : entryFileZ.keySet()) {
					SvnLogFile gFile = entryFileZ.get(key);
					SvnLogFile aFile = actionFileZ.get(key);
	
					boolean isSkipThisFile = DbUtil.checkIsSkipFile(gFile.getPath());
					if (isSkipThisFile) {
//						UtilZ.log(gFile.getPath() + "\t" + aFile.getAction() + "\t" + gFile.getLineAdd() + "\t"
//								+ gFile.getLineDel() + "\tIgnored!");
						continue;
					} else {
//						UtilZ.log(gFile.getPath() + "\t" + aFile.getAction() + "\t" + gFile.getLineAdd() + "\t"
//								+ gFile.getLineDel());
						
						DbLocData data = makeLocData(entry, gFile, aFile.getAction());
						data.setProjectId(projectId);
						UtilZ.log("LocData: g." + version16 + "\t" + data.getUser() + "\t"
									+ UtilZ.toLongStr(data.getLocTime()) + "\t" + data.getAction() + "\t" 
									+ data.getLocPath() + "\t" + data.getLineAdd() + "\t" + data.getLineDel());
						
						vCount += data.getLineAdd();
			
						DbUtil.saveLocData2Db(data);
						
						total++;
					}
				}
				
				UtilZ.log("Result: g." + version16  + "@" + projectId
								+ "\tTotal: " +entry.getFileZ().size() + "\tLoc: " + vCount);
				
				if(vCount > 5000) {
					UtilZ.log("disAbleOver5000Loc:  g." + version16  + "@" + projectId);
					
					DbUtil.disAbleByVersion(projectId, version);
				}
			}
		}
		UtilZ.log("Finish: " + projectId + ", gAll: " + count + ", locAll: " + total);
	}
	
	private static DbLocData makeLocData(SvnLogEntry entry, SvnLogFile loc, String action) {
		DbLocData result = new DbLocData();
		
		result.setVersionId(entry.getVersion());
		result.setUser(entry.getAuthor());
		result.setLocTime(entry.getDate());
		result.setLocDate(dateFmt2.format(entry.getDate()));
		
		result.setLocPath(loc.getPath());
		result.setAction(action);
		result.setFileType(Files.getSuffixName(loc.getPath())); //文件后缀名
		
		result.setLineAdd(loc.getLineAdd());
		result.setLineDel(loc.getLineDel());
		
		return result;
	}
	
	private static String[] sortKeyZ(Set<String> keyZ){
		int n = keyZ.size();
		
		String[] sortedKeyZ = new String[n];
		
		for(String key : keyZ) {
			n--;
			sortedKeyZ[n] = key;
		}
		
		return sortedKeyZ;
	}
}
