package util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedHashMap;

import org.apache.commons.exec.CommandLine;
import org.nutz.lang.Files;
import org.nutz.lang.Stopwatch;

import bean.SvnLogEntry;
import bean.SvnLogFile;
import bean.ProcessResult;

public class GitLog {
	static SimpleDateFormat dateFmt   = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss z");
	
	public static String getGitNumFromServer(int prjId, String lastDate, String nowDate, String url) {
		String localPath = getLocalPath(url);
		
		if(!Files.isDirectory(new File("git/" + localPath))) {
			//���Ŀ¼�����ڣ�����Ҫclone
			runGitClone(url, localPath);
		}
		String dir = "data/" + prjId + "/";
		Files.createDirIfNoExists(dir);
		
		String logFileName =  dir + lastDate + "_" +nowDate + ".txt";
		
		if(prjId == 421) {
			//421����crontab���ɣ��ļ�����ͬ
			SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
			String today = formatter.format(new Date());
			logFileName =  dir + today + ".txt";
		} else {
			runProcess("./git_log_num.sh", lastDate, nowDate, localPath, logFileName);
		}
		
		 return logFileName;
	}

	private static void runGitClone(String url, String localPath) {
		String path = "git/"+ localPath;
		//System.out.println("git clone " + path);
		int inx = path.lastIndexOf("/");
		if(inx >=0) {
			path = path.substring(0, inx);
		}
		
		Files.createDirIfNoExists(path);
		//System.out.println("git clone " + url + "\t" + path);
		
		runProcess("./git_clone.sh", path, url, "", "");
	}
	
	public static String getGitActionFromServer(int prjId, String lastDate, String nowDate, String url) {
		String localPath = getLocalPath(url);
		
		String dir = "data/" + prjId + "/";
		Files.createDirIfNoExists(dir);
		
		String logFileName =  dir + lastDate + "_" +nowDate + "-amd.txt";

		if(prjId == 421) {
			//421����crontab���ɣ��ļ�����ͬ
			SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
			String today = formatter.format(new Date());
			logFileName =  dir + today + "-amd.txt";
		} else {
			runProcess("./git_log_action.sh", lastDate, nowDate, localPath, logFileName);
		}
		 return logFileName;
	}
	
	public static String getLocalPath(String url) {
		
		if(url.startsWith("git") && url.endsWith(".git")) {
			url = url.toLowerCase().replace(".git", "");
		}
		
		String localpath = "";
		if(url.toLowerCase().startsWith("http://") || url.toLowerCase().startsWith("https://")) {
			// http://10.12.3.97:80/shop/workbench.git
			localpath = url.toLowerCase().substring(0, url.lastIndexOf("/"))
								.replace("http://", "").replace("https://", "")
								.replace(":", "_").replace("/", "_").replace(".git", "");
			localpath = localpath + "/" + url.toLowerCase().substring(url.lastIndexOf("/")+1).replace(".git", "");
		} else if(url.startsWith("git") && url.endsWith(".git")) {
			localpath = url.toLowerCase().replace(".git", "");
		}
		else {
			localpath = url.substring(url.indexOf(":/") + 2);
		}
		
		if(localpath.startsWith("it"))
			localpath = "g" + localpath;
		
		return localpath;
	}
	
	public static void runProcess(String cmd, String lastDate, String nowDate, String url, String logFileName) {
		CommandLine cmdLine = new CommandLine(cmd);

		cmdLine.addArgument(lastDate);
		cmdLine.addArgument(nowDate);
		cmdLine.addArgument(url);
		cmdLine.addArgument(logFileName);
		
		UtilZ.log("RunCmd: " + cmdLine.toString());
		
		Stopwatch sw = Stopwatch.begin();
		
		ProcessResult processResult = UtilZ.runCmd(cmdLine, 10 * 60);//��ʱ60�룬1����
		
		sw.stop();
		//判断是否有执行错误的信息
		if(processResult.getStatusCode() == -1){
			//异常抛出的错误
			UtilZ.log("RunError: " + processResult.getError() + ", Duration: "+ sw.getDuration() +" ms");
		}else {
			UtilZ.log("RunOk: " + processResult.getExitValue() + ", Duration: "+ sw.getDuration() +" ms");
		}
	}
	
	public static LinkedHashMap<String, SvnLogEntry> getGitLogFromFile(String logFile) {
		LinkedHashMap<String, SvnLogEntry> entryZ = new LinkedHashMap<String, SvnLogEntry>();
	    
		File file = new File(logFile);
        
        try (BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(file), "GB18030"))) {
            String line;
            SvnLogEntry entry = new SvnLogEntry();
            LinkedHashMap<String, SvnLogFile> fileZ = new LinkedHashMap<String, SvnLogFile>();
            
            while ((line = br.readLine()) != null) {
            	if(line.trim().length() == 0)
            		continue;
            	if(line.startsWith("-\t"))
            		continue;
            	if(line.startsWith("#branch#")) {
            		UtilZ.log("Branch: " + line.substring("#branch#".length()));
            		continue;
            	}
            	
            	String[] words = line.trim().split("\\|");
            	if(words.length == 3) {
            		//��һ��
            		if(fileZ.size() > 0) {//�����ݣ���ô�����ǻ��ύID��
            			entry.setFileZ(fileZ);
            			entryZ.put(entry.getVersion(), entry);

            			entry =  new SvnLogEntry();
            			fileZ = new LinkedHashMap<String, SvnLogFile>();
            		}
            		
            		entry.setVersion(words[0].trim());
            		entry.setAuthor(words[1].trim());
            		try {
						entry.setDate(dateFmt.parse(words[2].trim()));
					} catch (ParseException e) {
						e.printStackTrace();
					}
            	} else {
            		words = line.trim().split("\t");
            		
            		if(words.length == 3) {
            			//1	1	src/ngx_http_lua_shdict.c

	            		SvnLogFile gFile = new SvnLogFile();
	            		
	            		//System.out.println(line);
	            		
	            		gFile.setLineAdd(Integer.parseInt(words[0].trim()));
	            		gFile.setLineDel(Integer.parseInt(words[1].trim()));
	            		gFile.setPath(words[2].trim());
	            		
	            		fileZ.put(gFile.getPath(), gFile); 
            		} else if(words.length == 2) {
            			//M	src/ngx_http_lua_ssl_session_fetchby.h
            			
	            		SvnLogFile gFile = new SvnLogFile();
	            		
	            		gFile.setAction(words[0].trim());
	            		gFile.setPath(words[1].trim());
	            		
	            		fileZ.put(gFile.getPath(), gFile); 
            		}
            	}
            }
            
            //��β
            if(fileZ.size() > 0) {//�����ݣ���ô�����ǻ��ύID��
    			entry.setFileZ(fileZ);
    			entryZ.put(entry.getVersion(), entry);
            }
        } catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	    
	    return entryZ;
	}
}
