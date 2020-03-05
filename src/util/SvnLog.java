package util;

import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.TimeZone;

import org.apache.commons.exec.CommandLine;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.nutz.lang.Files;
import org.nutz.lang.Stopwatch;

import bean.SvnLogEntry;
import bean.ProcessResult;
import bean.SvnLogFile;

public class SvnLog {
	static SimpleDateFormat dateFmt   = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
	
	public static String getLogFromSVNServer(int prjId, int lastSvnVerId, String lastDate, String nowDate, String url) {
		String dir = "data/" + prjId + "/";
		Files.createDirIfNoExists(dir);
		
		String logFileName =  dir + lastDate + "_" +nowDate + ".xml";
		
		CommandLine cmdLine = null;
		
		url = url.replaceAll(" ", "%20");
		//替换url中的空格
		
		//if(prjId == 852)
		//	return "data/852/2017-01-01_2017-04-24.xml";
		
		if(url.indexOf("10.12.3.111:8443") >= 0 ) {
		//if(prjId == 852) {
			cmdLine = new CommandLine("./svn_num.sh");
			//cmdLine.addArgument(lastDate);
			//cmdLine.addArgument(nowDate);
			cmdLine.addArgument(url);
			cmdLine.addArgument(logFileName);
		} else {
			cmdLine = new CommandLine("./svn_log.sh");
			cmdLine.addArgument(lastDate);
			cmdLine.addArgument(nowDate);
			cmdLine.addArgument(url);
			cmdLine.addArgument(logFileName);
		}
		
		UtilZ.log("RunCmd: " + cmdLine.toString());
		
		Stopwatch sw = Stopwatch.begin();
		
		ProcessResult processResult = UtilZ.runCmd(cmdLine, 180);//��ʱ60�룬1����
		
		sw.stop();
		if(processResult.getStatusCode() == -1){
			//异常抛出的错误
			UtilZ.log("RunError: " + processResult.getError() + ", Duration: "+ sw.getDuration() +" ms");
		}else {
			UtilZ.log("RunOk: " + processResult.getExitValue() + ", Duration: "+ sw.getDuration() +" ms");
		}

		return logFileName;
	}
	
	@SuppressWarnings("unchecked")
	public static List<SvnLogEntry> getSvnLogFromFile(String file) {
		LinkedList<SvnLogEntry> entryZ = new LinkedList<SvnLogEntry>();
	    
		dateFmt.setTimeZone(TimeZone.getTimeZone("UTC"));
		
		SAXReader sr = new SAXReader();
	    Document document;
		try {
			document = sr.read(new File(file));
		
		    Element root = document.getRootElement();
	
		    List<Element> logList = root.elements();
		    for (Element svnlog : logList) {
		    	SvnLogEntry entry = new SvnLogEntry();
		    	
		    	String version = svnlog.attributeValue("revision");
		    	String author = svnlog.elementText("author");
		    	entry.setVersion(version);
		    	entry.setAuthor(author);
		    	
		    	String date = svnlog.elementText("date");
		    	
		    	if(date == null)
		    		continue;
		    	
		    	//System.out.println(version + "\t" + author + "\t" + date);

				try {
					date = date.substring(0, date.lastIndexOf("."));

					entry.setDate(dateFmt.parse(date));
				} catch (ParseException e) {
					e.printStackTrace();
				}
		    	
		    	HashMap<String, SvnLogFile> pathZ = new LinkedHashMap<String, SvnLogFile>();
		    	
		    	Element paths = svnlog.element("paths");
		    	if(paths == null)
		    		continue;
		    	
		    	List<Element> pathList = paths.elements();
		    	for (Element p : pathList) {
		    		SvnLogFile path = new SvnLogFile();

		    		String msg = p.attributeValue("msg");
		    		if(msg != null) {
		    			if(msg.toLowerCase().startsWith("copyfrom-rev") || msg.toLowerCase().startsWith("3rdlibrary"))
		    				continue;
		    		}
		    		
		    		String copyFrom = p.attributeValue("copyfrom-rev");
		    		if(copyFrom != null) {
		    			//System.out.println("copyfrom-rev@\t" + version + "\t" + author + "\t" + date);
		    			continue;
		    		}
		    		
		    		String action = p.attributeValue("action");
		    		if(action!=null)
		    			action = action.toLowerCase();
		    		
		    		String textModes = p.attributeValue("text-mods");
		    		if(textModes != null) {
		    			textModes = textModes.toLowerCase();
		    		}
		    		else {
		    			textModes = "true";
		    		}
		    		
		    		if(action.equals("a") || action.equals("m")) {
			    		if(textModes.equals("false"))
			    			continue;
		    		} else if(action.equals("d")) {
		    			;//do nothing...
//		    			<path text-mods="false" kind="file" action="D" prop-mods="false">/trunk/AspLocStat/src/RunGitLoc.java</path>
		    		}
//		    		if(p.attributeValue("kind").equals("file"))
//		    			continue;

	//	    		if(p.attributeValue("prop-mods").equals("true"))
	//	    			continue;

		    		
		    		path.setPath(p.getText());
		    		path.setKind(p.attributeValue("kind"));
		    		path.setPropMods(p.attributeValue("prop-mods"));
		    		path.setAction(p.attributeValue("action"));
		    		path.setTextModes(textModes);
		    		
		    		
		    		pathZ.put(path.getPath(), path);
		    	}
		    	
		    	if(pathZ.size() > 0) {
			    	entry.setFileZ(pathZ);
			    	entryZ.addFirst(entry);
		    	}
		    }
		} catch (DocumentException e) {
			//e.printStackTrace();
			UtilZ.log("XMLError: " + file);
		}
	    
	    return entryZ;
	}
}
