import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;

public class Main {
	static int[] outDegress = null;
	static ArrayList<ArrayList<ArrayList<Double>>> outG = null;
	static List<Double> trace;
	static List<Double> weightTrace;
	static String[] nodeLF = null;
	static String[] nodeRF = null;
	static int[] reachable;
	static Map<Double, ArrayList<Double>> mas = null;
	static Map<Double, Double> weightMap = null;
	static StringBuilder[] sb = new StringBuilder[9];
	static int circleCount = 0;
	static Map<Double, ArrayList<ArrayList<Double>>> outList;
	static Map<Double, ArrayList<ArrayList<Double>>> inputList;
	static Map<Double, Integer> outputValue;
	static Map<Double, Integer> inputValue;
	public static void main(String[] args) throws NumberFormatException, IOException {
		long start = System.currentTimeMillis();
		BufferedReader reader = null;
		String line = "";
		String fileNames = "/data/test_data.txt"; 
		String output = "/projects/student/result.txt";
		outList = new HashMap<Double, ArrayList<ArrayList<Double>>>();
		inputList = new HashMap<Double, ArrayList<ArrayList<Double>>>();
		outputValue = new HashMap<>() ; //出度个数
		inputValue = new HashMap<>() ; //入度个数
		try {
			reader = new BufferedReader(new FileReader(fileNames));
			while ((line = reader.readLine()) != null) {
				String item[] = line.split(",");
				insertEdge(Double.parseDouble(item[0]),Double.parseDouble(item[1]),Double.parseDouble(item[2]));
			}
		} catch (FileNotFoundException exception) {
			System.err.println(fileNames + " File Not Found");
		}finally {
			if (reader != null) {
				try {
					reader.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		Iterator<Double> iter = outputValue.keySet().iterator();
		TreeSet<Double> nodeSet = new TreeSet<>();
		int n = 0;
		//筛选既有出度列表也有入度列表的结点
		while(iter.hasNext()){
			Double tempKey= iter.next();
			Integer tempValue = inputValue.get(tempKey);
			if(tempValue == null) {
		        iter.remove();
			}else {
				nodeSet.add(tempKey);
				n++;
			}
		}
		boolean[] visi = new boolean[n];
		int[] inDegress = new int[n];
		ArrayList<ArrayList<ArrayList<Double>>> InG = new ArrayList<ArrayList<ArrayList<Double>>>(n);
		outDegress = new int[n];
		outG = new ArrayList<ArrayList<ArrayList<Double>>>(n);
//		outWeight = new int[n][out_max];
		//idMap 真实值-对应ID
		HashMap<Double, Integer> idMap = new HashMap<Double, Integer>();
		double[] nodeArr = new double[n];
		nodeLF = new String[n];
		nodeRF = new String[n];
		reachable = new int[n];
		sb[3] = new StringBuilder(3*3*500000);
	    sb[4] = new StringBuilder(4*4*500000);
	    sb[5] = new StringBuilder(5*5*1000000);
	    sb[6] = new StringBuilder(6*6*2000000);
	    sb[7] = new StringBuilder(7*7*3000000);
	    sb[8] = new StringBuilder(8*8*3000000);
		int count_i=0;
		for(iter = nodeSet.iterator(); iter.hasNext(); ) {
			double element =  iter.next();
			idMap.put(element, count_i);
			nodeLF[count_i] = Integer.toString((int)element) + "\n";
			nodeRF[count_i] = Integer.toString((int)element)+ ",";
			nodeArr[count_i++] = element;
		}
		ArrayList<ArrayList<Double>> al;
		ArrayList<Double> al_1;
		ArrayList<Double> al_2;
		ArrayList<ArrayList<Double>> bl;
		ArrayList<Double> bl_1;
		ArrayList<Double> bl_2;
		for (int i = 0; i < n; i++) {
			double element = nodeArr[i];
			int outValue_count =0;
			int inValue_count =0;
			ArrayList<Double> mList = outList.get(element).get(0);
			ArrayList<Double> mWeightList = outList.get(element).get(1);
			al = new ArrayList<ArrayList<Double>>();
			al_1 = new ArrayList<Double>();
			al_2 = new ArrayList<Double>();
			for (int j = 0,len_m = mList.size(); j < len_m; j++) {
				double outElement = mList.get(j);
				Integer outElement_index = idMap.get(outElement);
				if(outElement_index != null) {//判断是否有对应的ID值
					al_1.add((double)outElement_index);
					al_2.add(mWeightList.get(j));
					outValue_count++;
				}
	        }
			outDegress[i] = outValue_count;
			//排序 
			sortArray(al_1,al_2,outValue_count);
			al.add(al_1);
			al.add(al_2);
			outG.add(al);
			
			ArrayList<Double> inList = inputList.get(element).get(0);
			ArrayList<Double> inWeightList = inputList.get(element).get(1);
			bl = new ArrayList<ArrayList<Double>>();
			bl_1 = new ArrayList<Double>();
			bl_2 = new ArrayList<Double>();
			for (int k = 0,len_in = inList.size(); k < len_in; k++) {
				double inElement = inList.get(k);
				Integer inElement_index = idMap.get(inElement);
				if(inElement_index != null) {
					bl_1.add((double)inElement_index);
					bl_2.add(inWeightList.get(k));
					inValue_count++;
				}
	        }
			inDegress[i] = inValue_count;
			//排序
			sortArray(bl_1,bl_2,inValue_count);
			bl.add(bl_1);
			bl.add(bl_2);
			InG.add(bl);
		}
		trace = new ArrayList<>();
		weightTrace = new ArrayList<>();
		long dfs1 = System.currentTimeMillis();
		
		for (int i = 0; i < n; i++) {
			for (int reach = i+1; reach < n; reach++) {
				reachable[reach] = -1;
			}
			mas = new HashMap<Double, ArrayList<Double>>();
			weightMap = new HashMap<Double, Double>();
			ArrayList<Double> firstList = InG.get(i).get(0);
			ArrayList<Double> firstWeightList = InG.get(i).get(1);
			for(int j=0,len_1 = inDegress[i];j<len_1;j++) {
				//倒数第一个节点
				double firstNode = firstList.get(j);
				//倒数第一个节点权重
				double firstWeight = firstWeightList.get(j);
				if (firstNode > i) {
					ArrayList<Double> secondList = InG.get((int)firstNode).get(0);
					ArrayList<Double> secondWeightList = InG.get((int)firstNode).get(1);
					
					for (int k = 0,len_2 = inDegress[(int)firstNode]; k<len_2;k++) {
						//倒数第二个节点
						double secondNode = secondList.get(k);
						//倒数第二个节点权重
						double secondWeight = secondWeightList.get(k);
						
						double tt = (double)firstWeight/secondWeight;
						if(secondNode > i && tt>=0.2 && tt<= 3) {
							reachable[ (new Double(secondNode)).intValue()] = 1;
							if (mas.get(secondNode)==null) {
								ArrayList<Double> result = new ArrayList<Double>();
								mas.put(secondNode, result);
							}
							mas.get(secondNode).add(firstNode);
							mas.get(secondNode).add(secondWeight);
						}
					}
				}
				weightMap.put(firstNode, firstWeight);
			 }
			 decDfs((double)i, (double)i, 1, visi);
		}
		System.out.println("init Time(s): " + (dfs1 - start) * 1.0 / 1000 + "s");
		long dfs2 = System.currentTimeMillis();
		System.out.println("dfs Time(s): " + (dfs2 - dfs1) * 1.0 / 1000 + "s");
		write(output);
		long endPro = System.currentTimeMillis();
		System.out.println("All Time(s): " + (endPro - start) * 1.0 / 1000 + "s");
		System.out.println("size->" + circleCount);

	}
	
	
	
	public static void decDfs(double head, double current, int depth, boolean[] visi) {
		int current_T = (new Double(current)).intValue();
		visi[current_T] = true;
		trace.add(current);
		//找长度7的环
		if (reachable[current_T] == 1 && depth == 7) {
			List<Double> path2List =  mas.get(current);
			for (int i = 0,len = path2List.size(); i < len;i=i+2) {
				//A-B-C-D-E-F-{}
				//last_weight=E-F
				double EF_Weight = weightTrace.get(weightTrace.size() - 1);
				double lastNode =  path2List.get(i);
				double FG_Weight =  path2List.get(i+1);
				
				double t_1 = (double)FG_Weight/EF_Weight;

				// 比较(A-B)/(G-A)
				double GA_Weight = weightMap.get(lastNode);
				double AB_Weight = weightTrace.get(0);
				double t_2 = (double)AB_Weight/GA_Weight;

				if (!visi[(int) lastNode] && t_1 >=0.2 && t_1 <=3 && t_2 >=0.2 && t_2 <=3) {
					for(int key = 0,len_1 = trace.size();key<len_1;key++) {
//						sb[7].append(nodeRF[(int)trace.get(key)]);
						sb[8].append(nodeRF[new Double(trace.get(key)).intValue()]);

					}
					sb[8].append(nodeLF[(int)lastNode]);
					circleCount++;
	
				}
			}
		}

		if (depth < 7) {
			ArrayList<Double> list = outG.get(current_T).get(0);
			for (int i = 0,len = outDegress[current_T]; i < len; i++) {
				double gcur = list.get(i);
				double g_weight = outG.get(current_T).get(1).get(i);
				if(depth == 1 && gcur > head && !visi[(int)gcur]) {
					weightTrace.add(g_weight);
					decDfs(head, gcur, depth + 1, visi);
					weightTrace.remove(weightTrace.size() - 1);
				}
				if(depth>=2 && gcur > head && !visi[(int)gcur]) {
					double last_weight = weightTrace.get(weightTrace.size() - 1);
					double tt = (double)g_weight/last_weight;
					if(tt >=0.2 && tt <=3) {
						weightTrace.add(g_weight);
						decDfs(head, gcur, depth + 1, visi);
						weightTrace.remove(weightTrace.size() - 1);
					}
				//找长度为3,4,5的环
				}else if(gcur == head && depth >= 3 && depth < 7) {
					double last_weight = weightTrace.get(weightTrace.size() - 1);
					double first_weight = weightTrace.get(0);
					//A-B-C-A
					// (C-A)/(B-C) 
					double last_t = (double)g_weight/last_weight;

					// (A-B)/(C-A)
					double first_t = (double)first_weight/g_weight;

					if(last_t >=0.2 && last_t <=3 && first_t >=0.2 && first_t <=3) {
						int length = trace.size()-1;
						for(int key = 0;key < length;key++) {
							sb[depth].append(nodeRF[new Double(trace.get(key)).intValue()]);
//							sb[depth].append(nodeRF[trace.get(key)]);
						}
						sb[depth].append(nodeLF[new Double(trace.get(length)).intValue()]);
						circleCount++;
					}
				}
			}
		
			if (reachable[current_T] == 1 && (depth == 6)) {
				List<Double> path2List = mas.get(current);
				for (int i1 = 0,len = path2List.size(); i1 < len; i1=i1+2) {
					//A-B-C-D-E-{}
					//last_weight=D-E
					double DE_Weight = weightTrace.get(weightTrace.size() - 1);
					double lastNode =  path2List.get(i1);
					double EF_Weight =  path2List.get(i1+1);
					double t_1 = (double)EF_Weight/DE_Weight;

					// 比较(A-B)/(F-A)
					double FA_Weight = weightMap.get(lastNode);
					double AB_Weight = weightTrace.get(0);
					double t_2 = (double)AB_Weight/FA_Weight;

					if (!visi[(int)lastNode] && t_1 >=0.2 && t_1 <=3 && t_2 >=0.2 && t_2 <=3) {
						for(int key = 0,len_1 = trace.size();key<len_1;key++) {
							sb[7].append(nodeRF[new Double(trace.get(key)).intValue()]);
						}
						sb[7].append(nodeLF[(int)lastNode]);
						circleCount++;
					}
				}
			}

		}

		visi[current_T] = false;
		trace.remove(trace.size() - 1);
		
	}
	
	/**冒泡排序
	 * 
	 */
	public static void sortArray(ArrayList<Double> list,ArrayList<Double> weight,int length) {
	    for (int j = 0; j < length-1; j++) {  // 控制遍历的次数
            for (int i = 0; i < length-1; i++) {  // 找出相邻最新元素，交换位置
                if (list.get(i) > list.get(i + 1)) {
                    double temp = list.get(i);
                    double weightTemp = weight.get(i);
                    list.set(i, list.get(i+1));
                    list.set(i+1, temp);
                    weight.set(i, weight.get(i+1));
                    weight.set(i+1, weightTemp);
                }
            }
        }
	}
	
	public static void insertEdge(double a,double b,double c){
		//出度
		if(outList.containsKey(a)) {
			ArrayList<ArrayList<Double>> result = outList.get(a);
			result.get(0).add(b);
			result.get(1).add(c);
			int value = outputValue.get(a).intValue() + 1;
			outputValue.put(a, value);
		}else {
			ArrayList<Double> outListTemp = new ArrayList<>();
			ArrayList<Double> outListWeight = new ArrayList<>();
			ArrayList<ArrayList<Double>> result = new ArrayList<ArrayList<Double>>();
			outListTemp.add(b);
			outListWeight.add(c);
			result.add(outListTemp);
			result.add(outListWeight);
			outList.put(a, result);
			outputValue.put(a, 1);
		}
		//入度
		if(inputList.containsKey(b)) {
			ArrayList<ArrayList<Double>> result = inputList.get(b);
			result.get(0).add(a);
			result.get(1).add(c);
			int value = inputValue.get(b).intValue() + 1;
			inputValue.put(b, value);
		}else {
			ArrayList<Double> inListTemp = new ArrayList<>();
			ArrayList<Double> inListWeight = new ArrayList<>();
			ArrayList<ArrayList<Double>> result = new ArrayList<ArrayList<Double>>();
			inListTemp.add(a);
			inListWeight.add(c);
			result.add(inListTemp);
			result.add(inListWeight);
			inputList.put(b, result);
			inputValue.put(b, 1);
		}
	}
	 public static void write(String predictFileName){
        File file = new File(predictFileName);
        PrintWriter writer = null;
        try{
	        writer = new PrintWriter(new FileWriter(file));
            writer.print(Integer.toString(circleCount) + "\n");
			for (int i = 3; i < sb.length; i++) {
				writer.print(sb[i].toString());
			}
        }
        catch (IOException e){
            
            throw new RuntimeException();
        }finally {
        	writer.close();
		}
    }

}
