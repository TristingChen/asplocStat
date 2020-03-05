package test;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.nutz.lang.Files;

public class check {

	public static void main(String[] args) {
		List<String> lines = Files.readLines(new File("ck.txt"));
		//System.out.println("prj\tver");
		
		HashMap<String, List<String>> mapz = new HashMap<String, List<String>>();
		
		for(String line : lines) {
			if(line.indexOf("Check") > 0) {
				String[] words = line.replace("\t", " ").split(" ");
				String version = words[6];
				String project = words[8];
				
				if(!mapz.containsKey(version)) {
					List<String> list = new 	ArrayList<String>();
					list.add(project);
					
					mapz.put(version, list);
				} else {
					List<String> list =mapz.get(version);
					
					list.add(project);
					
					mapz.put(version, list);
				}
				//System.out.println(words[8] + "\t" + words[6]);
				
			}
			
			
		}
		
		//System.out.println("n" + mapz.size());
		for(String version : mapz.keySet()) {
			System.out.print(version + "\t");
			
			List<String> list =mapz.get(version);
			
			for(String value : list) {
				System.out.print(value + "\t");
			}
			
			System.out.print("\r\n");
		}
	}

}
