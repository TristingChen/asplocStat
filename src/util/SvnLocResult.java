package util;

import java.text.SimpleDateFormat;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.nutz.lang.Files;

import bean.DbLocData;
import bean.LocResult;
import bean.SvnLogEntry;
import bean.SvnLogFile;

public class SvnLocResult {

	static SimpleDateFormat dateFmt2 = new SimpleDateFormat("yyyy-MM-dd");
	
	public static DbLocData makeLocData(SvnLogEntry entry, SvnLogFile path, LocResult loc) {
		DbLocData result = new DbLocData();
		
		result.setVersionId(entry.getVersion());
		result.setUser(entry.getAuthor());
		result.setLocTime(entry.getDate());
		result.setLocDate(dateFmt2.format(entry.getDate()));
		
		result.setLocPath(path.getPath());
		result.setAction(path.getAction());
		result.setKind(path.getKind());
		result.setPropMods(path.getPropMods());
		result.setFileType(Files.getSuffixName(path.getPath())); //文件后缀名
		
		result.setLineAdd(loc.getLineAdd());
		result.setLineDel(loc.getLineDel());
		
		return result;
	}
	
	public static List<DbLocData> runLocOneEntry(int projectId, String url, SvnLogEntry entry) {
		LinkedList<DbLocData> dataZ = new LinkedList<DbLocData>();
		
		String version = entry.getVersion();
		
		if(entry.getAuthor().equals("admin"))
			return dataZ;
		
//		if(!version.equals("8308"))
//			continue;
		
		//5、	通过svn diff versionId，获取diff文件（某些diff，可能要10分钟。。。恐怖）
		String diffFile = SvnDiff.getDiffFromSVNServer(projectId, version, url);
		//7、	根据version，解析diff文件，生成loc结果集
		Map<String, LocResult> svnDiffZ = SvnDiff.getSvnDiffFromFile(diffFile);
		
		//获取文件路径偏移
		int pathIndex = SvnDiff.getPathMatchIndex(svnDiffZ.keySet(), entry.getFileZ());
//System.out.println("index"+pathIndex);
		for(SvnLogFile path: entry.getFileZ().values()) {
//			if(path.getKind().equals("file")) 
//				continue;
			
			String p = path.getPath();
//			if(pathIndex > p.length()) {
//				System.out.println(p);
//			}
			if(pathIndex > 0 && pathIndex <= p.length()) //修正文件路径
				p = p.substring(pathIndex);
//			if(p.startsWith("/")) {
//				p = p.substring(1);//去掉最前面的/
//				p = p.substring(p.indexOf("/") +1 );
//			}
			//UtilZ.log(p);
			//获取loc结果
			LocResult loc = svnDiffZ.get(p);
				
			if(loc == null) {
				continue;
			} else {
				if(loc.getFileType().toLowerCase().equals("bin"))
					continue;
				
				//生成要写入数据库的结果
				DbLocData result = makeLocData(entry, path, loc);

				if(result != null) {
					result.setProjectId(projectId);
					dataZ.add(result);
				}
			}
		}
		
		return dataZ;
	}
}
