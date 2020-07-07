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
	static int[] reachableP5;
	static int[] reachable;
	static List<List<Integer>> allNotRepCirclesList = null;
	static Map<Integer, List<Integer>> mas  = null;
	static Map<Integer, List< List<Integer>>> m3  = null;
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
		boolean[] visi = new boolean[n];
		//大佬测试了最大的出度为不大于50
		int out_max = 50;
		//大佬测试了最大的入度为不大于255
		int in_max = 255;
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
		long dfs1 = System.currentTimeMillis();
		allNotRepCirclesList = new ArrayList<List<Integer>>();
		for (int i = 0; i < n; i++) {
			//i 代表第八个节点
			//System.out.println("key->"+i);
			reachable = new int[n];
			reachableP5 = new int[n];
			mas = new HashMap<Integer, List<Integer>>();
			m3= new HashMap<Integer, List< List<Integer>>>();
			int[] headList = InG[i];
			List<Integer> tempList = null;
			for (int headNextNode : headList) {
				//headNextNode 代表第七个节点
				if (headNextNode > i) {
					int[] headNextList = InG[headNextNode];
					for (int j = 0,len_1 = inDegress[headNextNode]; j < len_1; j++) {
						//temp_six 代表第6个节点
						int temp_six = headNextList[j];
						reachable[temp_six] = 1;
						if (mas.get(temp_six)==null) {
							mas.put(temp_six, new ArrayList<Integer>());
						}
						mas.get(temp_six).add(headNextNode);
						// 第六个节点 大于 第八个节点时：
						if (temp_six > i) {
							int[] m3List = InG[temp_six];
							for(int k :m3List) {
								// k 代表第五个节点
								if(k>i && temp_six != headNextNode) {
									if (m3.get(k)==null) {
										m3.put(k, new ArrayList<List<Integer>>());
									}
									reachableP5[k] = 1;
									tempList = new ArrayList<Integer>();
									tempList.add(temp_six);
									tempList.add(headNextNode);
									m3.get(k).add(tempList);
								}
							}
						}
					}
				}

			}
			decDfs(i, i, 1, visi);
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
	
	
	
	public static void decDfs(int head, int current, int depth, boolean[] visi) {
		visi[current] = true;
		trace.add(current);
		List<Integer> circle = null;
		
		int[] list = outG[current];
		for (int i = 0,len = outDegress[current]; i < len; i++) {
			int gcur = list[i];
			if (gcur == head && depth >= 3 && depth < 5) {
				circle = new ArrayList<>();
				circle.addAll(trace);
				allNotRepCirclesList.add(circle);
			}
			if (gcur > head && !visi[gcur] && depth < 5) {
				decDfs(head, gcur, depth + 1, visi);
			}
		 }
		
		 //查找长度为5 和 6 的环
		 if (reachable[current] == 1 && (depth == 4 || depth == 5)) {
			List<Integer> path2List = mas.get(current);
			for (int i1 = 0; i1 < path2List.size(); i1++) {
				int lastNode = (int) path2List.get(i1);
				if (!visi[lastNode]) {
					circle = new ArrayList<>();
					circle.addAll(trace);
					circle.add(lastNode);
					allNotRepCirclesList.add(circle);
				}
			 }
		 }
		 
		//查找长度为7的环
		if (reachableP5[current] == 1 && depth == 5) {
			List<List<Integer>> path3 = m3.get(current);
			for (List<Integer> path3List: path3) {
				// 第七个节点
				int lNode = (int) path3List.get(1);
				// 第六个节点
				int fNode = (int) path3List.get(0);
				if (!visi[lNode] && !visi[fNode]) {
					circle = new ArrayList<>();
					circle.addAll(trace);
					circle.add(fNode);
					circle.add(lNode);
					allNotRepCirclesList.add(circle);
				}
			}	
		}

		visi[current] = false;
		trace.remove(trace.size() - 1);
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
