package util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.ExecuteException;
import org.apache.commons.exec.ExecuteWatchdog;
import org.apache.commons.exec.PumpStreamHandler;

import bean.ProcessResult;

public class UtilZ {
	static SimpleDateFormat dateFmt = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	static final org.nutz.log.Log logger = org.nutz.log.Logs.get();
	
	public static String getNextDay() {
		String nextDay = "";
		Calendar cal = Calendar.getInstance(); 
		Date date = new Date(); 
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd"); 
        
        cal.setTime(date); 
        cal.add(Calendar.DATE, 1); 
        //System.out.println("下一天的时间是：" + sdf.format(cal.getTime())); 
        nextDay = sdf.format(cal.getTime());
        return nextDay;
	}
	
	public static String toLongStr(Date date) {
		return dateFmt.format(date);
	}
	
	public static void log(String msg) {
		logger.info(msg);
	}
	
	public static ProcessResult runCmd(CommandLine cmdLine, int expireSec) {
		ProcessResult processResult = new ProcessResult();
		
		int exit[] = {0,1};
		
		ExecuteWatchdog watchdog = new ExecuteWatchdog(expireSec * 1000);//设置超时时间
		
		DefaultExecutor exec = new DefaultExecutor();
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();  
        ByteArrayOutputStream errorStream = new ByteArrayOutputStream(); 
        PumpStreamHandler streamHandler = new PumpStreamHandler(outputStream, errorStream);
		
		try {
			exec.setStreamHandler(streamHandler);
			exec.setWatchdog(watchdog);
			exec.setExitValues(exit);

			processResult.setExitValue(exec.execute(cmdLine));
			processResult.setResult(outputStream.toString("GB18030").trim());
			processResult.setError(errorStream.toString("GB18030").trim());
		} catch (ExecuteException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return processResult;
	}
}
