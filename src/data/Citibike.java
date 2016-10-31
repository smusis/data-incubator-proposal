package data;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.concurrent.TimeUnit;

public class Citibike {
	public static void main(String[] args) throws Exception {
		calculateValues();//Median Trip Duration
	}

	public static void calculateValues() throws IOException, ParseException{
		final File folder = new File("Data");
		final List<File> fileList = Arrays.asList(folder.listFiles());
		ArrayList<Double> tripDurationList = new ArrayList<Double>();
		TreeMap<String,HashSet<String>> visited = new TreeMap<String,HashSet<String>>();
		ArrayList<Double> avgGreatCircle=new ArrayList<>();
		TreeMap<String,ArrayList<Double>> monthDuration = new TreeMap<String,ArrayList<Double>>();
		TreeMap<String,TreeMap<Double,Double>> stationUsageHour = new TreeMap<String,TreeMap<Double,Double>>();
		TreeMap<String,Double> overallStationUsageHour = new TreeMap<String,Double>();
		TreeMap<Double,Double> overallSystemUsage = new TreeMap<Double,Double>();
		TreeMap<String,String> bikeEndStation = new TreeMap<String,String>();
		TreeMap<String,Double> bikeChangeCount=new TreeMap<String,Double>();

		int allRides=0;
		int countExceeds=0;//Count the no. of rides that exceed time limit (Customer, Subscriber)
		int sameStation=0;
		for(int i=0;i<fileList.size();i++)
		{
			String line="";
			final BufferedReader br = new BufferedReader(new FileReader(fileList.get(i)));
			line= br.readLine();
			while ((line= br.readLine())!= null ) {
				String split[]=line.split(",");				

				double tripDuration=Double.parseDouble(split[0].replaceAll("\"",""));

				//Trip Duration
				tripDurationList.add(Double.parseDouble(split[0].replaceAll("\"","")));

				//Start and End Station
				allRides++;
				String startStation=split[3].replaceAll("\"","").trim();
				String endStation=split[7].replaceAll("\"","").trim();

				if(startStation.equals(endStation)){
					sameStation++;
				}

				//Bikes Visited
				String bikeId=split[11].replaceAll("\"","").trim();
				if(visited.containsKey(bikeId)){
					HashSet<String> temp=visited.get(bikeId);
					temp.add(startStation);
					temp.add(endStation);
					visited.put(bikeId, temp);
				}
				else{
					HashSet<String> temp=new HashSet<>();
					temp.add(startStation);
					temp.add(endStation);
					visited.put(bikeId, temp);
				}

				//Great-Circle Distance

				if(!startStation.equals(endStation)){
					Double lat1=Double.parseDouble(split[5].replaceAll("\"","").trim());
					Double lon1=Double.parseDouble(split[6].replaceAll("\"","").trim());
					Double lat2=Double.parseDouble(split[9].replaceAll("\"","").trim());
					Double lon2=Double.parseDouble(split[10].replaceAll("\"","").trim());

					Double distance=greatCircle(lat1, lon1, lat2, lon2);
					if(!Double.isNaN(distance)){
						avgGreatCircle.add(distance);
					}
				}

				//Average Duration
				String startDate=split[1].replaceAll("\"","").trim();
				//String endDate=split[2].replaceAll("\"","").trim();
				timeDifference(startDate, monthDuration, tripDuration);

				//Largest Ratio
				calculateUsage(startDate, stationUsageHour, overallSystemUsage, startStation,
						overallStationUsageHour);

				//Customers&Subscribers
				String userType=split[12].replaceAll("\"","");
				countExceeds=customersSubscribers(userType, countExceeds,tripDuration);
				
				//Different Station
				differentStation(startStation, endStation, bikeId, bikeEndStation, bikeChangeCount);
			}
			//break;
		}
		//System.out.println(tripDurationList.size());
		Collections.sort(tripDurationList);

		ArrayList<Double> noStationsVisited = new ArrayList<Double>();
		for(Entry<String, HashSet<String>> mapEntry:visited.entrySet()){
			noStationsVisited.add((double) mapEntry.getValue().size());
		}

		//Minimum and Maximum Duration
		Double minDuration=0.0,maxDuration=0.0;
		for(Entry<String, ArrayList<Double>> mapEntry:monthDuration.entrySet()){
			Double average=mean(mapEntry.getValue());			

			if(average>maxDuration){
				maxDuration=average;
			}
			if(average<minDuration || minDuration==0.0){
				minDuration=average;
			}
		}

		//Largest Ratio
		double ratio=largestRatio(stationUsageHour, overallSystemUsage,allRides,overallStationUsageHour);

		//Different Station
		ArrayList<Double> changeCount=new ArrayList<Double>();
		for(Entry<String, Double> mapEntry:bikeChangeCount.entrySet()){
			changeCount.add(mapEntry.getValue());
		}
		
		DecimalFormat df = new DecimalFormat();
		df.setMaximumFractionDigits(10);
		System.out.println("Median Trip Duration "+df.format(median(tripDurationList)));
		System.out.println("Fraction Rides Same Start & End "+df.format((float)sameStation/(float)allRides));
		System.out.println("Standard deviation of the number of stations visited "+df.format(Math.sqrt(variance(noStationsVisited))));
		System.out.println("Average length of a trip (in km) "+df.format(mean(avgGreatCircle)));
		System.out.println("Difference b/w  the longest and shortest average durations "+df.format((maxDuration-minDuration)));
		System.out.println("Largest ratio of station hourly usage fraction to system hourly usage fraction "+df.format(ratio));
		System.out.println("Fraction of rides exceed their corresponding time limit "+df.format((double)countExceeds/(double)allRides));
		System.out.println("Average number of times a bike is moved "+df.format(mean(changeCount)));
	}

	//Median of Arraylist
	public static double median(ArrayList<Double> tripDurationList) {
		int middle = tripDurationList.size()/2;
		if (tripDurationList.size()%2 == 1) {
			return tripDurationList.get(middle);
		} else {
			return (tripDurationList.get(middle-1) + tripDurationList.get(middle)) / 2.0;
		}
	}

	//Mean of Arraylist
	public static double mean(ArrayList<Double> noStationsVisited) {
		double sum=0.0;
		for(Double fStations:noStationsVisited){
			sum=sum+fStations;
		}
		return (sum/noStationsVisited.size());
	}

	public static double variance(ArrayList<Double> noStationsVisited) {
		double mean=mean(noStationsVisited);
		double temp=0.0;
		for(Double fStationsVar:noStationsVisited){
			temp=temp+(fStationsVar-mean)*(fStationsVar-mean);
		}
		return (temp/noStationsVisited.size());
	}

	//Code logic from http://introcs.cs.princeton.edu/java/12types/GreatCircle.java.html
	public static double greatCircle(Double lat1, Double lon1, Double lat2, Double lon2) { 
		double a1 = Math.toRadians(lat1);
		double b1 = Math.toRadians(lon1);
		double a2 = Math.toRadians(lat2);
		double b2 = Math.toRadians(lon2);

		double angle = Math.acos(Math.sin(a1) * Math.sin(a2)
				+ Math.cos(a1) * Math.cos(a2) * Math.cos(b1 - b2));


		// convert to degrees (*60) then km (*1.852)
		double distance = Math.toDegrees(angle) * 60 * 1.852;

		return distance;
	}

	public static void timeDifference(String startDate, TreeMap<String,ArrayList<Double>> monthDuration,
			double tripDuration) throws ParseException{
		String split[]=startDate.split(":");

		Date stdate = null;
		if(split.length>2){
			stdate = new SimpleDateFormat("M/d/yyyy H:mm:ss").parse(startDate);
		}
		else{
			stdate = new SimpleDateFormat("M/d/yyyy H:mm").parse(startDate);		
		}

		double diffSeconds=(tripDuration);

		String monthName=getMonth(stdate.getMonth());

		if(monthDuration.containsKey(monthName)){
			ArrayList<Double> temp=monthDuration.get(monthName);
			temp.add(diffSeconds);
			monthDuration.put(monthName,temp);
		}
		else{
			ArrayList<Double> temp=new ArrayList<Double>();
			temp.add(diffSeconds);
			monthDuration.put(monthName,temp);
		}
	}

	public static String getMonth(int month){
		String monthName="";
		switch((month+1)){
		case 1: monthName="January";break;
		case 2: monthName="February";break;
		case 3: monthName="March";break;
		case 4: monthName="April";break;
		case 5: monthName="May";break;
		case 6: monthName="June";break;
		case 7: monthName="July";break;
		case 8: monthName="August";break;
		case 9: monthName="September";break;
		case 10: monthName="October";break;
		case 11: monthName="November";break;
		case 12: monthName="December";break;
		}

		return monthName;
	}

	//Calculate each station usage and overall system usage
	public static void calculateUsage(String startDate,
			TreeMap<String,TreeMap<Double,Double>> stationUsage,
			TreeMap<Double,Double> overallSystemUsage, String startStation,
			TreeMap<String,Double> overallStationUsageHour) throws ParseException{
		String split[]=startDate.split(":");

		Date stdate = null;
		if(split.length>2){
			stdate = new SimpleDateFormat("M/d/yyyy H:mm:ss").parse(startDate);
		}
		else{
			stdate = new SimpleDateFormat("M/d/yyyy H:mm").parse(startDate);		
		}

		double hour =  stdate.getHours();
		//System.out.println(startDate);
		//System.out.println("Hour "+hour);

		if(stationUsage.containsKey(startStation)){
			TreeMap<Double,Double> temp=stationUsage.get(startStation);
			if(temp.containsKey(hour)){
				temp.put(hour,temp.get(hour)+1.0);
			}
			else{
				temp.put(hour,1.0);
			}
			stationUsage.put(startStation,temp);
			overallStationUsageHour.put(startStation,overallStationUsageHour.get(startStation)+hour);
		}
		else{
			TreeMap<Double,Double> temp=new TreeMap<Double,Double>();
			temp.put(hour,1.0);
			stationUsage.put(startStation,temp);
			overallStationUsageHour.put(startStation,hour);
		}

		if(overallSystemUsage.containsKey(hour)){
			overallSystemUsage.put(hour,overallSystemUsage.get(hour)+1.0);
		}
		else{
			overallSystemUsage.put(hour,1.0);
		}
	}

	public static double largestRatio(TreeMap<String,TreeMap<Double,Double>> stationUsage,
			TreeMap<Double,Double> overallSystemUsage, int allRides,
			TreeMap<String,Double> overallStationUsageHour){

		double ratio=0.0;

		TreeMap<Double,Double> tempOverallSystemUsage=new TreeMap<Double,Double>();
		for(Entry<Double, Double> mapEntry:overallSystemUsage.entrySet()){
			tempOverallSystemUsage.put(mapEntry.getKey(), (mapEntry.getValue()/(double)allRides));
		}

		for(Entry<String,TreeMap<Double,Double>> mapEntry:stationUsage.entrySet()){
			String stationId=mapEntry.getKey();
			for(Entry<Double,Double> inmapEntry:mapEntry.getValue().entrySet()){
				double tempHour=inmapEntry.getKey();
				double stHourUsage=(inmapEntry.getValue()/overallStationUsageHour.get(stationId));

				double tempRatio=(stHourUsage/tempOverallSystemUsage.get(tempHour));

				if(tempHour>ratio){
					ratio=tempRatio;
				}
			}
		}

		return ratio;
	}

	//Check if the ride exceeds time limit for different users
	public static int customersSubscribers(String userType,
			int countExceeds, double tripDuration) throws ParseException{

		double diffMinutes=(tripDuration/60);

		//System.out.println(userType);
		//System.out.println(diffMinutes);
		//System.out.println(countExceeds);
		if(userType.equals("Customer") && diffMinutes>30.0){
			countExceeds++;
		}
		if(userType.equals("Subscriber") && diffMinutes>45.0){
			countExceeds++;
		}

		return countExceeds;
	}
	
	//Check if the bike is moved to a different station w.r.t to previous ride
	public static void differentStation(String startStation, String endStation, String bikeID,
			TreeMap<String,String> bikeEndStation, TreeMap<String,Double> bikeChangeCount){
		
		boolean change=false;
		if(bikeEndStation.containsKey(bikeID)){
			String lastStation=bikeEndStation.get(bikeID);
			if(!lastStation.equals(startStation)){
				change=true;
			}
			bikeEndStation.put(bikeID, endStation);
		}
		else{
			bikeEndStation.put(bikeID, endStation);
		}
		
		if(change){
			if(bikeChangeCount.containsKey(bikeID)){
				bikeChangeCount.put(bikeID, bikeChangeCount.get(bikeID)+1.0);
			}
			else{
				bikeChangeCount.put(bikeID, 1.0);
			}
		}
		else{
			if(!bikeChangeCount.containsKey(bikeID)){
				bikeChangeCount.put(bikeID, 0.0);
			}
		}
	}
}