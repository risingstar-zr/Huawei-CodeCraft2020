import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

public class Main {
	static int[] outDegress = null;
	static int[][] outG = null;
	static List<Integer> trace;
	static String[] nodeLF = null;
	static String[] nodeRF = null;
	static int[] reachable;
	static List<List<Integer>> allNotRepCirclesList = null;
	static Map<Integer, List<Integer>> mas  = null;
	public static void main(String[] args) throws NumberFormatException, IOException {
		long start = System.currentTimeMillis();
		BufferedReader reader = null;
		String line = "";
		String fileNames = "/data/test_data.txt"; 
		String output = "/projects/student/result.txt";
		Map<Integer, ArrayList<Integer>> outList = new HashMap<Integer, ArrayList<Integer>>();//key为结点，value为出度列表）
		Map<Integer, ArrayList<Integer>> inputList = new HashMap<Integer, ArrayList<Integer>>();//key为结点，value为入度列表）
		Map<Integer, Integer> outputValue = new HashMap<>() ; //key为结点，value为出度个数
		Map<Integer, Integer> inputValue = new HashMap<>() ; //key为结点，value为入度个数
		try {
			reader = new BufferedReader(new FileReader(fileNames));
			while ((line = reader.readLine()) != null) {
				String item[] = line.split(",");
				insertEdge(Integer.parseInt(item[0]),Integer.parseInt(item[1]),outList,inputList,outputValue,inputValue);
			}
		} catch (FileNotFoundException exception) {
			System.err.println(fileNames + " File Not Found");
		}
		Iterator<Integer> iter = outputValue.keySet().iterator();
		Set<Integer> nodeSet = new HashSet<>(150000);
		int n = 0;
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
		//大佬测试了最大的出度为不大于50
		int out_max = 50;
		//大佬测试了最大的入度为不大于50
		int in_max = 50;
		int[][] InG = new int[n][in_max];
		int[] inDegress = new int[n];
		outG = new int[n][out_max];
		outDegress = new int[n];
		HashMap<Integer, Integer> idMap = new HashMap<Integer, Integer>();
		//定义有效的结点数组
		int[] nodeArr = new int[n];
		nodeLF = new String[n];
		nodeRF= new String[n];
		reachable = new int[n];
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
			ArrayList<Integer> mList = outList.get(element);
			for (int j = 0,len_m = mList.size(); j < len_m; j++) {
				int outElement = mList.get(j);
				Integer outElement_index = idMap.get(outElement);
				if(outElement_index != null) {//判断出度map中是否有当前key
					outG[i][outValue_count] = outElement_index;
					outValue_count++;
				}
	        }
			outDegress[i] = outValue_count;
			
			ArrayList<Integer> inList = inputList.get(element);
			for (int k = 0,len_in = inList.size(); k < len_in; k++) {
				int inElement = inList.get(k);
				Integer inElement_index = idMap.get(inElement);
				if(inElement_index != null) {//判断出度map中是否有当前key
					InG[i][inValue_count] = inElement_index;
					inValue_count++;
				}
	        }
			inDegress[i] = inValue_count;
		}
		trace = new ArrayList<>();
		boolean[] visi = new boolean[n];
		allNotRepCirclesList = new ArrayList<List<Integer>>();
		long dfs1 = System.currentTimeMillis();
		for (int i = 0; i < n; i++) {
			for (int reach = i+1; reach < n; reach++) {
				reachable[reach] = -1;
			}
			mas = new HashMap<Integer, List<Integer>>();
			//headList 第七层
			int[] headList = InG[i];
			for(int j=0,len_1 = inDegress[i];j<len_1;j++) {
				int headNextNode = headList[j];
				if (headNextNode > i) {
					int[] headNextList = InG[headNextNode];
					for (int k = 0,len_2 = inDegress[headNextNode]; k<len_2;k++) {
						//temp 第六层
						int temp = headNextList[k];
						reachable[temp] = 0;
						if (mas.get(temp)==null) {
							mas.put(temp, new ArrayList<Integer>());
						}
						mas.get(temp).add(headNextNode);
					}
				}
			}
			findCycle(i,visi);
		}
		System.out.println("init Time(s): " + (dfs1 - start) * 1.0 / 1000 + "s");
		long dfs2 = System.currentTimeMillis();
		System.out.println("dfs Time(s): " + (dfs2 - dfs1) * 1.0 / 1000 + "s");
		Collections.sort(allNotRepCirclesList, new DicComparator());
		savePredictResult(output, allNotRepCirclesList);
		long endPro = System.currentTimeMillis();
		System.out.println("All Time(s): " + (endPro - start) * 1.0 / 1000 + "s");
		System.out.println("size->" + allNotRepCirclesList.size());

	}
	
	
	public static void findCycle(int v, boolean[] visi) {
		visi[v] = true;
		trace.add(v);
		List<Integer> temp = null;
		for(int a = 0,len_a = outDegress[v];a < len_a; a++) {//第二个结点，不做处理
			int temp_a = outG[v][a];
			if(temp_a > v) {//2层
				visi[temp_a] = true;
				trace.add(temp_a);
				for(int b=0,len_b = outDegress[temp_a];b < len_b;b++) {//第三个结点
					int temp_b = outG[temp_a][b];
					if(temp_b == v) {//第三个结点与第一个相同，则continue
						continue;
					}
					if(temp_b > v) {//3层
						visi[temp_b] = true;
						trace.add(temp_b);
						for(int c = 0,len_c = outDegress[temp_b];c < len_c;c++) {//第四个结点
							int temp_c = outG[temp_b][c];
							if(temp_c == v) {//第四个结点与第一个相同，则找到长度为3的环，且continue
								temp = new ArrayList<>();
								temp.addAll(trace);
								allNotRepCirclesList.add(temp);
								continue;
							}else if(visi[temp_c]) {//排除temp_c在trace中，但不是在一个位置的情况
								continue;
							}
							if(temp_c > v) {//4层
								visi[temp_c] = true;
								trace.add(temp_c);
								for(int d=0,len_d=outDegress[temp_c];d<len_d;d++) {
									int temp_d = outG[temp_c][d];
									if(temp_d == v) {//第五个结点与第一个相同，则找到长度为4的环，且continue
										temp = new ArrayList<>();
										temp.addAll(trace);
										allNotRepCirclesList.add(temp);
										continue;
									}else if(visi[temp_d]) {//排除temp_d在trace中，但不是在一个位置的情况
										continue;
									}
									if(temp_d > v) {//5层
										visi[temp_d] = true;
										trace.add(temp_d);
										//找长度为6的环
										if (reachable[temp_d] == 0) {
											List<Integer> path2List = mas.get(temp_d);
											for (int i1 = 0,len_i1 = path2List.size(); i1 < len_i1; i1++) {
												int lastNode = (int) path2List.get(i1);
												if (!visi[lastNode]) {
													temp = new ArrayList<>();
													temp.addAll(trace);
													temp.add(lastNode);
													allNotRepCirclesList.add(temp);
												}

											}
										}
										for(int e=0,len_e=outDegress[temp_d];e<len_e;e++) {
											int temp_e = outG[temp_d][e];
											if(temp_e == v) {//第六个结点与第一个相同，则找到长度为5的环，且continue
												temp = new ArrayList<>();
												temp.addAll(trace);
												allNotRepCirclesList.add(temp);
												continue;
											}else if(visi[temp_e]) {//排除temp_e在trace中，但不是在一个位置的情况
												continue;
											}
											if(temp_e > v) {//6层
												//找长度为7的环
												if (reachable[temp_e] == 0) {
													List<Integer> path2List = mas.get(temp_e);
													for (int i1 = 0,len_i1 = path2List.size(); i1 < len_i1; i1++) {
														int lastNode = (int) path2List.get(i1);
														if (!visi[lastNode]) {
															temp = new ArrayList<>();
															temp.addAll(trace);
															temp.add(temp_e);
															temp.add(lastNode);
															allNotRepCirclesList.add(temp);
														}
													}
												}
											}
										}
										visi[temp_d] = false;
										trace.remove(trace.size()-1);
									}
	
								}
								visi[temp_c] = false;
								trace.remove(trace.size()-1);
							}
	
						}
						visi[temp_b] = false;
						trace.remove(trace.size()-1);
					}
					
				}
				visi[temp_a] = false;
				trace.remove(trace.size()-1);
			}
		}
		visi[v] = false;
		trace.remove(trace.size()-1);
	}
	
	public static void insertEdge(int a,int b,Map<Integer, ArrayList<Integer>> outList,Map<Integer, ArrayList<Integer>> inputList,Map<Integer, Integer> outputValue,Map<Integer, Integer> inputValue){
		//出度
		if(outList.containsKey(a)) {
			outList.get(a).add(b);
			int value = outputValue.get(a).intValue() + 1;
			outputValue.put(a, Integer.valueOf(value));
		}else {
			ArrayList<Integer> temp = new ArrayList<>();
			temp.add(b);
			outList.put(a, temp);
			outputValue.put(a, 1);
		}
		//入度
		if(inputList.containsKey(b)) {
			inputList.get(b).add(a);
			inputValue.put(b, ((Integer)inputValue.get(b)).intValue() + 1);
		}else {
			ArrayList<Integer> temp = new ArrayList<>();
			temp.add(a);
			inputList.put(b, temp);
			inputValue.put(b, 1);
		}
	}
	private static void savePredictResult(String predictFileName, List<List<Integer>> allNotRepCirclesList) {
		try {
			BufferedWriter out = new BufferedWriter(new FileWriter(predictFileName));
			int length = allNotRepCirclesList.size();
			int jSize =0;
			out.write(Integer.toString(length) + "\n");
			for (int i = 0; i < length; i++) {
				jSize = allNotRepCirclesList.get(i).size()-1;
				for (int j = 0; j < jSize; j++) {
					
				out.write(nodeRF[allNotRepCirclesList.get(i).get(j)]);
					
				}
				out.write(nodeLF[allNotRepCirclesList.get(i).get(jSize)]);
				
			}
			out.close();
		} catch (IOException exception) {
			System.err.println(exception.getMessage());
		}
	}

	static class DicComparator implements Comparator<List<Integer>> {

		@Override
		public int compare(List<Integer> var1, List<Integer> var2) {
			int len_1 = var1.size();
			int len_2 = var2.size();
			if (len_1 > len_2) {
				return 1;
			}
			if (len_1 == len_2) {
				int index = 0;
				for (int i = 0; i < len_1; i++) {
					if (!var1.get(i).equals(var2.get(i))) {
						index = i;
						break;
					}
				}
				return var1.get(index) - var2.get(index);
			}

			else {
				return -1;
			}
		}
	}
}
