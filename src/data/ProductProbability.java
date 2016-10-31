package data;

import java.util.ArrayList;
import java.util.Map.Entry;
import java.util.TreeMap;

public class ProductProbability {
	static ArrayList<String> allCombinations=new ArrayList<String>();
	public static void main(String args[]) throws Exception
	{
		int dice=8;
		int sum=24;
		ArrayList<Integer> list = new ArrayList<Integer>();

		diceSum(dice,sum,list,0);
		expectedProduct(dice,sum);
	}

	public static void diceSum(int dice, int n, ArrayList<Integer> selected, int sum) {
		if (dice == 0) {
			//Add it to list after all dice values are selected
			if (sum == n) {
				allCombinations.add(selected.toString());
			}
		} 
		else if (sum < n && sum + 6 * dice >= n){
			for (int i = 1; i <= 6; i++) {
				selected.add(i);
				sum = sum+i;

				diceSum(dice - 1, n, selected, sum);
				selected.remove(selected.size() - 1);
				sum = sum-i;
			}
		}

	}

	public static void expectedProduct(int dice, double sum){
		TreeMap<Double, Double> productCount=new TreeMap<Double, Double>();
		for(int i=0;i<allCombinations.size();i++){
			String split[]= allCombinations.get(i).replace("[", "").replace("]", "").split(",");
			double product=1.0;
			double sumVals=0.0;
			for(int j=0; j<split.length;j++){
				product=product*Double.parseDouble(split[j]);
				sumVals=sumVals+Double.parseDouble(split[j]);
			}

			if(sumVals==sum){
				if(productCount.containsKey(product)){
					productCount.put(product,productCount.get(product)+1.0);
				}
				else{
					productCount.put(product,1.0);
				}
			}
		}

		double expectedValue=0;
		double variance=0;
		for(Entry<Double, Double> mapEntry:productCount.entrySet()){
			expectedValue=expectedValue+(mapEntry.getKey()*(mapEntry.getValue()/(allCombinations.size())));
			variance=variance+((mapEntry.getKey()*mapEntry.getKey())*(mapEntry.getValue()/(allCombinations.size())));
		}

		variance=variance-(expectedValue*expectedValue);
		System.out.println("Expected Value "+expectedValue);
		//System.out.println(variance);
		System.out.println("Standard Deviation "+Math.sqrt(variance));
	}
}

