import java.util.List;

import org.nutz.lang.Stopwatch;

import bean.DbLocProject;
import run.GitLoc;
import run.SvnLoc;
import util.UtilZ;
import util.DbUtil;

public class RunSvnLoc {
	// public static final org.nutz.log.Log log = org.nutz.log.Logs.get();

	public static void main(String[] args) {
		Stopwatch sw = new Stopwatch();
		sw.start();
		
		int runId = 0;
		try{
			runId = Integer.parseInt(args[0]);
		} catch(Exception e){}

		DbUtil.initDb();
		// DbUtil.loadConfigByTag("skipFileType");
		// 1、 从数据库获取项目ID、URL等
		List<DbLocProject> projectZ = DbUtil.getProjectZfromDb();

		UtilZ.log("Total: " + projectZ.size() + " project");

		// DbUtil.loadVersionByProject(272);

		// List<String> b5000 = DbUtil.loadBig5000();
		// for(String s : b5000) {
		// String[] words = s.split("\t");
		// DbUtil.getLast(words[0], words[1], words[2]);
		// int prj = Integer.parseInt(words[0]);
		// DbUtil.disAbleByVersion(prj, words[1]);
		// }
		int i = 0;
		for (DbLocProject project : projectZ) {
			int projectId = project.getBuild();

			//如果runId存在，则只跑单个Id
    		if(runId > 0 && projectId != runId)
				continue;
			
			String url = project.getScmPath().replace(":8443:8443", ":8443").replace("211.139.191.202", "10.1.3.243");

			UtilZ.log("Project(" + (i++) + "/" + projectZ.size() + "): " + projectId + ", Url: " + url);

			if (url.toLowerCase().endsWith(".git") || url.toLowerCase().startsWith("git")  || url.toLowerCase().indexOf("git")>0 
					|| projectId == 991 ) {
					GitLoc.getLoc(projectId, url);
			} else {
				//if(url.indexOf("10.12.3.111:8443") >= 0 )
					SvnLoc.getLoc(projectId, url);
			}
		}

		DbUtil.closeDb();

		sw.stop();
		UtilZ.log("AllDone: " + projectZ.size() + " Project, " + sw.getDuration() / 1000 + " sec.");
	}

}
