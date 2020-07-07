import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;

public class Main {
	static int out_max = 0;
	static int in_max = 0;
	static int[] outDegress = null;
	static int[][] outG = null;
	static int[][] outWeight = null;
	static List<Integer> trace;
	static List<Integer> weightTrace;
	static String[] nodeLF = null;
	static String[] nodeRF = null;
	static int[] reachable;
	static Map<Integer, ArrayList<Integer>> mas = null;
	static Map<Integer, Integer> weightMap = null;
	static StringBuilder[] sb = new StringBuilder[8];
	static int circleCount = 0;
	public static void main(String[] args) throws NumberFormatException, IOException {
		long start = System.currentTimeMillis();
		BufferedReader reader = null;
		String line = "";
		String fileNames = "/data/test_data.txt"; 
		String output = "/projects/student/result.txt";
		Map<Integer, ArrayList<ArrayList<Integer>>> outList = new HashMap<Integer, ArrayList<ArrayList<Integer>>>();
		Map<Integer, ArrayList<ArrayList<Integer>>> inputList = new HashMap<Integer, ArrayList<ArrayList<Integer>>>();
		Map<Integer, Integer> outputValue = new HashMap<>() ; //出度个数
		Map<Integer, Integer> inputValue = new HashMap<>() ; //入度个数
		try {
			reader = new BufferedReader(new FileReader(fileNames));
			while ((line = reader.readLine()) != null) {
				String item[] = line.split(",");
				insertEdge(Integer.parseInt(item[0]),Integer.parseInt(item[1]),Integer.parseInt(item[2]),outList,inputList,outputValue,inputValue);
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
		Iterator<Integer> iter = outputValue.keySet().iterator();
		TreeSet<Integer> nodeSet = new TreeSet<>();
		int n = 0;
		//筛选既有出度列表也有入度列表的结点
		while(iter.hasNext()){
			int tempKey= iter.next();
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
		int[][] InG = new int[n][in_max];
		int[][] inWeight = new int[n][in_max];
		outDegress = new int[n];
		outG = new int[n][out_max];
		outWeight = new int[n][out_max];
		//idMap 真实值-对应ID
		HashMap<Integer, Integer> idMap = new HashMap<Integer, Integer>();
		int[] nodeArr = new int[n];
		nodeLF = new String[n];
		nodeRF = new String[n];
		reachable = new int[n];
		sb[3] = new StringBuilder(3*3*500000);
	    sb[4] = new StringBuilder(4*4*500000);
	    sb[5] = new StringBuilder(5*5*1000000);
	    sb[6] = new StringBuilder(6*6*2000000);
	    sb[7] = new StringBuilder(7*7*3000000);
		int count_i=0;
		for(iter = nodeSet.iterator(); iter.hasNext(); ) {
			int element = (int) iter.next();
			idMap.put(element, count_i);
			nodeLF[count_i] = Integer.toString(element) + "\n";
			nodeRF[count_i] = Integer.toString(element)+ ",";
			nodeArr[count_i++] = element;
		}
		for (int i = 0; i < n; i++) {
			int element = nodeArr[i];
			int outValue_count =0;
			int inValue_count =0;
			ArrayList<Integer> mList = outList.get(element).get(0);
			ArrayList<Integer> mWeightList = outList.get(element).get(1);
			for (int j = 0,len_m = mList.size(); j < len_m; j++) {
				int outElement = mList.get(j);
				Integer outElement_index = idMap.get(outElement);
				if(outElement_index != null) {//判断是否有对应的ID值
					outG[i][outValue_count] = outElement_index;
					outWeight[i][outValue_count] = mWeightList.get(j);
					outValue_count++;
				}
	        }
			outDegress[i] = outValue_count;
			//排序 
			sortArray(outG[i],outWeight[i],outValue_count);

			ArrayList<Integer> inList = inputList.get(element).get(0);
			ArrayList<Integer> inWeightList = inputList.get(element).get(1);
			for (int k = 0,len_in = inList.size(); k < len_in; k++) {
				int inElement = inList.get(k);
				Integer inElement_index = idMap.get(inElement);
				if(inElement_index != null) {//
					InG[i][inValue_count] = inElement_index;
					inWeight[i][inValue_count] = inWeightList.get(k);
					inValue_count++;
				}
	        }
			inDegress[i] = inValue_count;
			//排序
			sortArray(InG[i],inWeight[i],inValue_count);
		}
		trace = new ArrayList<>();
		weightTrace = new ArrayList<>();
		long dfs1 = System.currentTimeMillis();
		
		for (int i = 0; i < n; i++) {
			for (int reach = i+1; reach < n; reach++) {
				reachable[reach] = -1;
			}
			mas = new HashMap<Integer, ArrayList<Integer>>();
			weightMap = new HashMap<Integer, Integer>();

			int[] firstList = InG[i];
			int[] firstWeightList = inWeight[i];
			for(int j=0,len_1 = inDegress[i];j<len_1;j++) {
				//倒数第一个节点
				int firstNode = firstList[j];
				//倒数第一个节点权重
				int firstWeight = firstWeightList[j];
				if (firstNode > i) {
					int[] secondList = InG[firstNode];
					int[] secondWeightList = inWeight[firstNode];
					
					for (int k = 0,len_2 = inDegress[firstNode]; k<len_2;k++) {
						//倒数第二个节点
						int secondNode = secondList[k];
						//倒数第二个节点权重
						int secondWeight = secondWeightList[k];
						
						float tt = (float)firstWeight/secondWeight;
						if(secondNode > i && tt>=0.2 && tt<= 3) {
							reachable[secondNode] = 1;
							if (mas.get(secondNode)==null) {
								ArrayList<Integer> result = new ArrayList<Integer>();
								mas.put(secondNode, result);
							}
							mas.get(secondNode).add(firstNode);
							mas.get(secondNode).add(secondWeight);
						}
					}
				}
				weightMap.put(firstNode, firstWeight);
			 }
			 decDfs(i, i, 1, visi);
		}
		System.out.println("init Time(s): " + (dfs1 - start) * 1.0 / 1000 + "s");
		long dfs2 = System.currentTimeMillis();
		System.out.println("dfs Time(s): " + (dfs2 - dfs1) * 1.0 / 1000 + "s");
		write(output);
		long endPro = System.currentTimeMillis();
		System.out.println("All Time(s): " + (endPro - start) * 1.0 / 1000 + "s");
		System.out.println("size->" + circleCount);

	}
	
	
	
	public static void decDfs(int head, int current, int depth, boolean[] visi) {
		visi[current] = true;
		trace.add(current);
		//找长度7的环
		if (reachable[current] == 1 && depth == 6) {
			List<Integer> path2List =  mas.get(current);
			for (int i = 0,len = path2List.size(); i < len;i=i+2) {
				//A-B-C-D-E-F-{}
				//last_weight=E-F
				int EF_Weight = weightTrace.get(weightTrace.size() - 1);
				int lastNode = (int) path2List.get(i);
				int FG_Weight = (int) path2List.get(i+1);
				float t_1 = (float)FG_Weight/EF_Weight;
				// 比较(A-B)/(G-A)
				int GA_Weight = weightMap.get(lastNode);
				int AB_Weight = weightTrace.get(0);
				float t_2 = (float)AB_Weight/GA_Weight;

				if (!visi[lastNode] && t_1 >=0.2 && t_1 <=3 && t_2 >=0.2 && t_2 <=3) {
					for(int key = 0,len_1 = trace.size();key<len_1;key++) {
						sb[7].append(nodeRF[trace.get(key)]);
					}
					sb[7].append(nodeLF[lastNode]);
					circleCount++;
	
				}
			}
		}

		if (depth < 6) {
			int[] list = outG[current];
			for (int i = 0,len = outDegress[current]; i < len; i++) {
				int gcur = list[i];
				int g_weight = outWeight[current][i];
				if(depth == 1 && gcur > head && !visi[gcur]) {
					weightTrace.add(g_weight);
					decDfs(head, gcur, depth + 1, visi);
					weightTrace.remove(weightTrace.size() - 1);
				}
				if(depth>=2 && gcur > head && !visi[gcur]) {
					int last_weight = weightTrace.get(weightTrace.size() - 1);
					float tt = (float)g_weight/last_weight;
					if(tt >=0.2 && tt <=3) {
						weightTrace.add(g_weight);
						decDfs(head, gcur, depth + 1, visi);
						weightTrace.remove(weightTrace.size() - 1);
					}
				//找长度为3,4,5的环
				}else if(gcur == head && depth >= 3 && depth < 6) {
					int last_weight = weightTrace.get(weightTrace.size() - 1);
					int first_weight = weightTrace.get(0);
					//A-B-C-A
					// (C-A)/(B-C) 
					float last_t = (float)g_weight/last_weight;
					// (A-B)/(C-A)
					float first_t = (float)first_weight/g_weight;
					if(last_t >=0.2 && last_t <=3 && first_t >=0.2 && first_t <=3) {
						int length = trace.size()-1;
						for(int key = 0;key < length;key++) {
							sb[depth].append(nodeRF[trace.get(key)]);
						}
						sb[depth].append(nodeLF[trace.get(length)]);
						circleCount++;
					}
				}
			}
		
			if (reachable[current] == 1 && (depth == 5)) {
				List<Integer> path2List = mas.get(current);
				for (int i1 = 0,len = path2List.size(); i1 < len; i1=i1+2) {
					//A-B-C-D-E-{}
					//last_weight=D-E
					int DE_Weight = weightTrace.get(weightTrace.size() - 1);
					int lastNode = (int) path2List.get(i1);
					int EF_Weight = (int) path2List.get(i1+1);
					float t_1 = (float)EF_Weight/DE_Weight;
					// 比较(A-B)/(F-A)
					int FA_Weight = weightMap.get(lastNode);
					int AB_Weight = weightTrace.get(0);
					float t_2 = (float)AB_Weight/FA_Weight;
					if (!visi[lastNode] && t_1 >=0.2 && t_1 <=3 && t_2 >=0.2 && t_2 <=3) {
						for(int key = 0,len_1 = trace.size();key<len_1;key++) {
							sb[6].append(nodeRF[trace.get(key)]);
						}
						sb[6].append(nodeLF[lastNode]);
						circleCount++;
					}
				}
			}

		}

		visi[current] = false;
		trace.remove(trace.size() - 1);
		
	}
	
	/**冒泡排序
	 * 
	 */
	public static void sortArray(int[] list,int[] weight,int length) {
	    for (int j = 0; j < length-1; j++) {  // 控制遍历的次数
            for (int i = 0; i < length-1; i++) {  // 找出相邻最新元素，交换位置
                if (list[i] > list[i + 1]) {
                    int temp = list[i];
                	int weightTemp = weight[i];
                    list[i] = list[i + 1];
                    list[i + 1] = temp;
                    weight[i] = weight[i+1];
                    weight[i + 1] = weightTemp;
                }
            }
        }
	}
	
	public static void insertEdge(int a,int b,int c,Map<Integer, ArrayList<ArrayList<Integer>>> outList,Map<Integer, ArrayList<ArrayList<Integer>>> inputList,Map<Integer, Integer> outputValue,Map<Integer, Integer> inputValue){
		//出度
		if(outList.containsKey(a)) {
			ArrayList<ArrayList<Integer>> result = outList.get(a);
			result.get(0).add(b);
			result.get(1).add(c);
			int value = outputValue.get(a).intValue() + 1;
			if(value > out_max) {
				out_max = value;
			}
			outputValue.put(a, value);
		}else {
			ArrayList<Integer> outListTemp = new ArrayList<>();
			ArrayList<Integer> outListWeight = new ArrayList<>();
			ArrayList<ArrayList<Integer>> result = new ArrayList<ArrayList<Integer>>();
			outListTemp.add(b);
			outListWeight.add(c);
			result.add(outListTemp);
			result.add(outListWeight);
			outList.put(a, result);
			outputValue.put(a, 1);
		}
		//入度
		if(inputList.containsKey(b)) {
			ArrayList<ArrayList<Integer>> result = inputList.get(b);
			result.get(0).add(a);
			result.get(1).add(c);
			int value = inputValue.get(b).intValue() + 1;
			if(value > in_max) {
				in_max = value;
			}
			inputValue.put(b, value);
		}else {
			ArrayList<Integer> inListTemp = new ArrayList<>();
			ArrayList<Integer> inListWeight = new ArrayList<>();
			ArrayList<ArrayList<Integer>> result = new ArrayList<ArrayList<Integer>>();
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
