import java.util.LinkedHashMap;
import java.util.List;

import org.nutz.lang.Stopwatch;

import bean.DbLocReleaseJob;
import bean.LocResult;
import run.SvnReleaseLoc;
import util.DbUtil;
import util.UtilZ;

public class RunReleaseStat {

	public static void main(String[] args) {
		int runId = 0;
		try {
			runId = Integer.parseInt(args[0]);
		} catch (Exception e) {
		}

		Stopwatch sw = new Stopwatch();
		sw.start();

		DbUtil.initDb();

		// 1、 扫描“按版本统计任务”表，读取isDone=0的行，进行后续处理
		List<DbLocReleaseJob> jobZ = DbUtil.getReleaseJobFromDb();

		for (DbLocReleaseJob job : jobZ) {
			int jobId = job.getJobId();

			// 如果runId存在，则只跑单个Id
			if (runId > 0 && jobId != runId)
				continue;

			int projectId = job.getProjectId();

			UtilZ.log("RunJob: " + jobId + "@" + projectId);
			
			String diffFileName = null; 

			diffFileName =	SvnReleaseLoc.getDiffFile(job);

			LinkedHashMap<String, LocResult> dataZ = SvnReleaseLoc.getSvnDiffFromFile(diffFileName);

			int fileCount = 0;
			int lineAdd = 0;
			int lineDel = 0;

			// 写入svnLOC数据表
			for (LocResult result : dataZ.values()) {
				DbUtil.saveLocReleaseData2Db(jobId, projectId, result);

				fileCount++;
				lineAdd += result.getLineAdd();
				lineDel += result.getLineDel();
			}

			// 写入数据库，完成job
			DbUtil.saveLocReleaseJob2Db(job);

			UtilZ.log("DoneJob: " + jobId + "@" + projectId + ", file: " + fileCount + ", add: " + lineAdd + ", del: " + lineDel);
		}

		DbUtil.closeDb();

		sw.stop();
		UtilZ.log("AllDone: " + jobZ.size() + " Job, " + sw.getDuration() / 1000 + " sec.");
	}

}
