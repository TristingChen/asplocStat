//package test;
//import java.util.HashMap;
//import java.util.List;
//
//import bean.DbLocProject;
//import bean.SvnLogEntry;
//import bean.SvnLogFile;
//import util.UtilZ;
//import util.DbUtil;
//import util.GitLog;
//
//public class TestGitLoc {
//	public static final org.nutz.log.Log log = org.nutz.log.Logs.get();
//
//	public static void main(String[] args) {
//		DbUtil.initDb();
//
//		// TODO 1銆� 浠庢暟鎹簱鑾峰彇椤圭洰ID銆乁RL绛�
//		List<DbLocProject> projectZ = DbUtil.getProjectZfromDb();
//
//		// TODO 2銆� 浠庢暟鎹簱璇诲彇璇ラ」鐩甀D涔嬪墠鐨勬棩鏈熴�乿ersionID绛�
//		int projectId = 2;
//		String url = "https://10.1.3.243:8443/svn/ASPIREPLUS_PROJECT";
//
//		// htxl
//		projectId = 7;
//		url = "https://github.com/openresty/lua-nginx-module.git";
//		url = "d:/lua";
//		url = "git/hf.weili.cmpower.cn";
//
//		String[] lastVersion = DbUtil.getLastVersion(projectId);
//		String lastDate = lastVersion[1];
//
//		// TODO 3銆� 鏍规嵁涔嬪墠鏃ユ湡锛屼互鍙婂綋鍓嶆棩鏈燂紝閫氳繃svn log锛岃幏寰梮ml锛屽鏋滄椂闂撮棿闅旈暱锛屼互鏈堜负闂撮殧锛屾渶鏃╂椂闂�2016-8-1
//		String nowDate = UtilZ.getNextDay();
//		// prjId = 4;
//		// url = "https://10.1.3.243:8443/svn/ASP-MALL/";
//
//		// 閫氳繃svn log锛岃幏寰梮ml锛屾瘮濡�"data\\2016-08-01_2016-09-03.xml";//
//
//		String logFileName = GitLog.getGitNumFromServer(projectId, lastDate, nowDate, url);
//		HashMap<String, SvnLogEntry> entryZ = GitLog.getGitLogFromFile(logFileName);
//
//		// 鐢熸垚amd鐨刟ctionMap
//		String actFileName = GitLog.getGitActionFromServer(projectId, lastDate, nowDate, url);
//		HashMap<String, SvnLogEntry> actionZ = GitLog.getGitLogFromFile(actFileName);
//
//		for (String version : entryZ.keySet()) {
//			SvnLogEntry entry = entryZ.get(version);
//			SvnLogEntry action = actionZ.get(version);
//
//			UtilZ.log(version + "\t" + entry.getAuthor() + "\t" + UtilZ.toLongStr(entry.getDate()));
//
//			// 鑾峰彇loc鐨刴ap
//			HashMap<String, SvnLogFile> entryFileZ = entry.getFileZ();
//			// 鑾峰彇amd鐨刴ap
//			HashMap<String, SvnLogFile> actionFileZ = action.getFileZ();
//
//			for (String key : entryFileZ.keySet()) {
//				SvnLogFile gFile = entryFileZ.get(key);
//				SvnLogFile aFile = actionFileZ.get(key);
//
//				boolean isSkipThisFile  = DbUtil.checkIsSkipFile(gFile.getPath());
//				if(isSkipThisFile) {
//					UtilZ.log(gFile.getPath() + "\t" + aFile.getAction() + "\t"
//															+ gFile.getLineAdd() + "\t" + gFile.getLineDel() + "\tIgnored!");
//					continue;
//				}
//
//				UtilZ.log(gFile.getPath() + "\t" + aFile.getAction() + "\t" + gFile.getLineAdd() + "\t"
//						+ gFile.getLineDel());
//			}
//			UtilZ.log("-----------");
//		}
//
//		DbUtil.closeDb();
//	}
//}