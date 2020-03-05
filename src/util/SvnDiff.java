package util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;

import org.apache.commons.exec.CommandLine;
import org.nutz.lang.Files;
import org.nutz.lang.Stopwatch;
import org.wickedsource.diffparser.api.DiffParser;
import org.wickedsource.diffparser.api.UnifiedDiffParser;
import org.wickedsource.diffparser.api.model.Diff;
import org.wickedsource.diffparser.api.model.Hunk;
import org.wickedsource.diffparser.api.model.Line;

import bean.LocResult;
import bean.ProcessResult;
import bean.SvnLogFile;

public class SvnDiff {
	static DiffParser diffParser = new UnifiedDiffParser();
	
	public static String getDiffFromSVNServer(int prjId, String version, String url) {
		String dir = "data/" + prjId + "/";
		Files.createDirIfNoExists(dir);
		
		String diffFileName =  dir + version + ".txt";
		
		/*
		File diffFile = Files.findFile(diffFileName);
		if(diffFile !=null && diffFile.exists() && diffFile.isFile()) {
			if(diffFile.length() > 0) {
				//UtilZ.log("ExistFile\t" + diffFileName);
				return diffFileName;
			}
		}
		*/
		
		
		url = url.replaceAll(" ", "%20");
		//替换url中的空格
		
		CommandLine cmdLine = new CommandLine("./svn_diff.sh");

		cmdLine.addArgument("" + version);
		cmdLine.addArgument(url);
		cmdLine.addArgument(diffFileName);
		
		UtilZ.log("RunCmd: " + cmdLine.toString());
		
		Stopwatch sw = Stopwatch.begin();
		
		ProcessResult processResult = UtilZ.runCmd(cmdLine, 30 * 60);//超时30分钟
		
		sw.stop();
		UtilZ.log("RunOk: " + processResult.getExitValue() + ", Duration: "+ sw.getDuration() +" ms");
		
		 return diffFileName;
	}
	
	public static LinkedHashMap<String, LocResult> getSvnDiffFromFile(String diffFile) {
		LinkedHashMap<String, LocResult> diffZ = new LinkedHashMap<String, LocResult>();
		
		String path = null;
		StringBuilder data = new StringBuilder();
        
        File file = new File(diffFile);
        
        try (BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(file), "UTF-8"))) {
            String line;
            boolean isSkipThisFile = false;
            while ((line = br.readLine()) != null) {
            	if(line.startsWith("Index: ")) {
    				if(path == null) {
    					//新的开始
    					path = line;

    					isSkipThisFile = DbUtil.checkIsSkipFile(path);
    					if(isSkipThisFile) {
    						path = null;
    					}
    				} else if(path.equals(line)) {
    					;//还是这个文件，不用处理
    				} else {
    					//不同的文件了，需要进行处理...
    					LocResult locResult = getLocFromDiff(data.toString());
    					diffZ.put(path.substring(7), locResult);
    					
    					path = line;
    					data = new StringBuilder();
    					
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
    			LocResult locResult = getLocFromDiff(data.toString());
    			diffZ.put(path.substring(7), locResult);
    		}
        } catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
        
		return diffZ;
	}
	
	
	
	public static int getPathMatchIndex(Set<String> keyZ, HashMap<String, SvnLogFile> pathZ) {
		LinkedHashMap<Integer, Integer> indexZ = new LinkedHashMap<Integer, Integer>();
		
		int index = -1;
		
		//diff的数量比log里面的path少，而且必定是短（或者相等），因此，从diff比较log
		for(String diffPath : keyZ) {
			//System.out.println("diffPath\t" + diffPath);
			for(String p: pathZ.keySet()) {
				//SvnLogFile path = pathZ.get(p);
				index = p.indexOf(diffPath);
				if(index >=0) {
					//System.out.println(index + "\t" + p + "\t" + diffPath);

					if(!indexZ.containsKey(index))
						indexZ.put(index, 1);
					else {
						int ct = indexZ.get(index);
						indexZ.put(index, ct+1);
					}
						
					break;
				}
			}
		}
		
		//System.out.println(index);
		
		if(indexZ.size() == 0) //没有
			return -1;
		
		//存在，则要进行比较了
		int okIndex = 0;
		int okCt = 0;
		for(Integer inx : indexZ.keySet()) {
			int ct = indexZ.get(inx);
			if(ct > okCt) {//取最大的
				okIndex = inx;
				okCt = ct;
			}
		}
		
		return okIndex;
	}
	
	public static LocResult getLocFromDiff(String str) {
		LocResult loc = new LocResult();
		
		if(str.indexOf("mime-type = application") > 0) {
			loc.setFileType("bin");
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
			
			//System.out.println("add: " + add +", del: " +del);
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