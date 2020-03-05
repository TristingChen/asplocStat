package run;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.LinkedHashMap;
import java.util.List;

import org.apache.commons.exec.CommandLine;
import org.nutz.lang.Files;
import org.nutz.lang.Stopwatch;
import org.wickedsource.diffparser.api.DiffParser;
import org.wickedsource.diffparser.api.UnifiedDiffParser;
import org.wickedsource.diffparser.api.model.Diff;
import org.wickedsource.diffparser.api.model.Hunk;
import org.wickedsource.diffparser.api.model.Line;

import bean.DbLocReleaseJob;
import bean.LocResult;
import bean.ProcessResult;
import util.DbUtil;
import util.UtilZ;

public class SvnReleaseLoc {
	
	static DiffParser diffParser = new UnifiedDiffParser();
	
	public static String getDiffFile(DbLocReleaseJob job) {
		int jobId = job.getJobId();
		int projectId = job.getProjectId();
		String scmUri = job.getScmUri().replace(":8443:8443", ":8443").replace("211.139.191.202", "10.1.3.243");
		
		String releaseOld = job.getReleaseOld();
		String releaseNew = job.getReleaseNew();
		
		//2&3 调用SVN程序下载旧版本SVN库：SvnUri+ ReleaseOld / ReleaseNew
		
		String workDir = "diff/" + jobId + "/";
		Files.createDirIfNoExists(workDir);

		//4 调用diff进行两个版本比较：diff -r -x ".svn" -u OLD NEW > diff@PrjId-Id.txt
		String diffFileName =  "diff@" +projectId + "-" + jobId + ".txt";

		CommandLine cmdLine = null;
		
		//./release_diff.sh "diff/1670/" "https://10.1.3.243:8443/svn/ACC_PROJECT/trunk/ppm/" "TEST_1.0.0" "TEST_1.0.1" "diff@1670-1.txt"
		cmdLine = new CommandLine("./release_diff.sh");
		cmdLine.addArgument(workDir);
		cmdLine.addArgument(scmUri);
		cmdLine.addArgument(releaseOld);
		cmdLine.addArgument(releaseNew);
		cmdLine.addArgument(diffFileName);

		UtilZ.log("RunCmd: " + cmdLine.toString());
		
		Stopwatch sw1 = Stopwatch.begin();
		
		ProcessResult processResult = UtilZ.runCmd(cmdLine, 600);//超时600秒，10分钟
		
		sw1.stop();
		UtilZ.log("RunOk: " + processResult.getExitValue() + ", Duration: "+ sw1.getDuration()/1000 +" sec");
		
		return "diff/" + diffFileName;
	}
	
	
	public static LinkedHashMap<String, LocResult> getSvnDiffFromFile(String diffFile) {
		LinkedHashMap<String, LocResult> diffZ = new LinkedHashMap<String, LocResult>();
		
		String path = null;
		StringBuilder data = new StringBuilder();
        
        File file = new File(diffFile);
        
        try (BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(file), "GB2312"))) {
            String line;
            boolean isSkipThisFile = false;
            boolean isLineBegin = false;
            while ((line = br.readLine()) != null) {
            	if(line.startsWith("diff -r -x .svn -u")) {
    				if(path == null) {
    					//新的开始
    					path = line;
    					//path，再处理

    					isSkipThisFile = DbUtil.checkIsSkipFile(path);
    					if(isSkipThisFile) {
    						path = null;
    					}
    				} else if(path.equals(line)) {
    					;//还是这个文件，不用处理
    				} else {
    					//不同的文件了，需要进行处理...
    					LocResult locResult = SvnReleaseLoc.getLocFromDiff(data.toString());
    					diffZ.put(path, locResult);
    					
    					path = line;
    					data = new StringBuilder();
    					isLineBegin = false;
    					
    					isSkipThisFile = DbUtil.checkIsSkipFile(path);
    					if(isSkipThisFile) {
    						path = null;
    					}
    				}
    			}
            	if(!isSkipThisFile)
            		data.append(line + "\r\n");
            }
            //处理完了
    		if(path != null) {
    			LocResult locResult = SvnReleaseLoc.getLocFromDiff(data.toString());
    			diffZ.put(path, locResult);
    		}
        } catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
        
		return diffZ;
	}
	
	private static LocResult getLocFromDiff(String str) {
		//System.out.println(str);
		
		LocResult loc = new LocResult();
		
		if(str.indexOf("mime-type = application") > 0) {
			//loc.setFileType("bin");
			return loc;
		}
		
		List<Diff> diffs = diffParser.parse(str.getBytes());

		//System.out.println(diffs.size());
		
//		int allAdd = 0;
//		int allDel = 0;
		for(Diff diff : diffs) {
			//if(diff.getFromFileName().toLowerCase().endsWith(".class"))
			//	continue;

//			System.out.println(diff.getFromFileName());
//			System.out.println(diff.getToFileName());

			int add =0;
			int del = 0;
			for(Hunk hunk : diff.getHunks()) {
				List<Line> lines = hunk.getLines();
				for(Line line : lines) {
					if(line.getLineType().equals(Line.LineType.NEUTRAL))
						continue;
					
					if(line.getContent().trim().length() == 0)
						continue;
					
					if(line.getContent().trim().equals("//"))
						continue;
					
					if(line.getLineType().equals(Line.LineType.FROM))
						del++;
					else if(line.getLineType().equals(Line.LineType.TO))
						add++;
					
					//System.out.println(line.getContent());
					//System.out.println(line.getLineType());
				}
			}
			
			String file = diff.getFromFileName();
			
			file = file.substring(file.indexOf("/") + 1);
			//file = file.substring(0, file.indexOf("    201"));
			
			
			//System.out.println(file+ "\tadd\t" + add +"\tdel\t" +del);
			
			loc.setFileName(file);
			loc.setFileType(Files.getSuffixName(file));
			
			loc.setLineAdd(add);
			loc.setLineDel(del);
			
//			allAdd += add;
//			allDel += del;
		}
//	
//		System.out.println("all\tadd: " + allAdd +", del: " +allDel);
		
		return loc;
	}
}
