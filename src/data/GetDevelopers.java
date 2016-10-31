package data;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.TreeMap;
import java.util.Map.Entry;

public class GetDevelopers {
	public static void main(String[] args) throws Exception
	{
		getDevelopers();
	}

	// Read log files to find developers for each file
	public static void getDevelopers() throws Exception
	{

		final BufferedReader brProject = new BufferedReader(new FileReader(
				"E:/users/kochharps/Projects/DataIncubator/ReposData.csv")); 
		String line="";
		line= brProject.readLine();
		TreeMap<String, String> projectComp=new TreeMap<>();
		while ((line= brProject.readLine())!= null ) { 
			String split[]=line.split(",");
			projectComp.put(split[1],split[0]);
		}

		ArrayList<String> uniquedevs=new ArrayList<>();

		PrintWriter outNodes=new PrintWriter(new File("E:/users/kochharps/Projects/DataIncubator/NetworkGraphNodes.csv"));

		PrintWriter out=new PrintWriter(new File("E:/users/kochharps/Projects/DataIncubator/NetworkGraphEdges.csv"));

		final File folder = new File("E:/users/kochharps/Projects/DataIncubator/Logs");
		final List<File> fileList = Arrays.asList(folder.listFiles());
		//PrintStream out = new PrintStream(new FileOutputStream("D:/Projects/ICSME 2015/output.txt"));

		int count=0;

		for(int i=0;i<fileList.size();i++)
		{
			final BufferedReader br = new BufferedReader(new FileReader(fileList.get(i))); 
			//System.out.println(fileList.get(i));

			String project[]=fileList.get(i).toString().split("\\\\");
			String projectName=project[project.length-1].replace(",", "").trim().toLowerCase();
			String commit="",author="";
			String dateStr="";
			Date date=null;
			TreeMap<String, HashSet<String>> authorCount=new TreeMap<>();
			System.out.println(projectName);

			if(projectName.equals("")){
				continue;
			}

			while ((line= br.readLine())!= null ) { 
				if(line.startsWith("Author:"))
				{
					//author=line.replace("Author:","").trim();
					String semi[]=line.split("Author:");
					if(semi.length>1){
						String[] splEmail=semi[1].split("<");
						author=splEmail[1].replace(">", "").replace(",", "").trim().toLowerCase();
					}
					//System.out.println(author.trim());
				}



				if(line.startsWith("Date:"))
				{
					dateStr=line.replace("Date:","").trim();
					date = new SimpleDateFormat("EEE MMM dd HH:mm:ss yyyy Z").parse(dateStr);
				}

				if((line.startsWith("commit "))){

					//System.out.println("yes");
					//System.out.println(commit);
					//System.out.println(dateStr);
					if(!commit.equals("") && !dateStr.equals("")){
						//	out.write(author+","+projectName+","+"1"+","+commit+","+author+","+
						//		(date.getMonth()+1)+"/"+date.getDay()+"/"+date.getYear()+"\n");
						out.write(author+","+projectName+","+"1"+"\n");
					}

					if(!uniquedevs.contains(author) && !author.equals("")){
						uniquedevs.add(author);
					}

					if(!uniquedevs.contains(projectName) && !projectName.equals("")){
						uniquedevs.add(projectName);
					}

					commit="";author="";
					dateStr="";

					String comm[]=line.split("commit");
					commit=comm[1].trim();
				}

			}

			count++;
			if(count==75){
				break;
			}
			//break;
		}
		out.close();


		final BufferedReader brEdges = new BufferedReader(new FileReader(
				"E:/users/kochharps/Projects/DataIncubator/NetworkGraphEdges.csv"));
		while ((line= brEdges.readLine())!= null ) { 
			String split[]=line.split(",");
			if(!uniquedevs.contains(split[0])){
				System.out.println(split[0]);
				uniquedevs.add(split[0].trim());
			}
			if(!uniquedevs.contains(split[1])){
				System.out.println(split[1]);
				uniquedevs.add(split[1].trim());
			}
		}
		brEdges.close();

		for(String str:uniquedevs){
			outNodes.write(str+"\n");
		}
		outNodes.close();
	}
}
