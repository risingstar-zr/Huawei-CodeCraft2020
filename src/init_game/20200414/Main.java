import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

public class Main {
	//真实值->id
	static HashMap<Integer, Integer> idMap = new HashMap<Integer,Integer>();
	//访问标志
	static boolean[] visi = null;
	static List<Integer> trace;
	static int[] reachable;
	
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
		Set<Integer> nodeSet = new TreeSet<>();
		int n =0;//有效节点的总个数
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
		//定义有效的结点数组
		int[] nodeArr = new int[n];
		int count_i=0;
		for(iter = nodeSet.iterator(); iter.hasNext(); ) {
			int element = (int) iter.next();
			idMap.put(element, count_i);
			nodeArr[count_i++] = element;
		}
		//真实的出度列表、入度列表、出度个数、入度个数
		List<List<Integer>> out_G = new ArrayList<>(n);
		List<List<Integer>> in_G = new ArrayList<>(n);
		int[] outDegress = new int[n];
		int[] inDegress =new int[n];
		boolean[] visi = new boolean[n];
		reachable = new int[n];
	
		for (int i = 0; i < n; i++) {
			int element = nodeArr[i];
			int outValue_count =0;
			int inValue_count =0;
			ArrayList<Integer> mList = outList.get(element);
			
			ArrayList<Integer> tempList = new ArrayList<Integer>();
			for (int j = 0,len_m = mList.size(); j < len_m; j++) {
				int outElement = mList.get(j);
				Integer outElement_index = idMap.get(outElement);
				if(outElement_index != null) {//判断出度map中是否有当前key
					outValue_count++;
					tempList.add(outElement_index);
				}
	        }
			outDegress[i] = outValue_count;
			out_G.add(tempList);
			
			ArrayList<Integer> inList = inputList.get(element);
			tempList = new ArrayList<Integer>();
			for (int k = 0,len_in = inList.size(); k < len_in; k++) {
				int inElement = inList.get(k);
				Integer inElement_index = idMap.get(inElement);
				if(inElement_index != null) {//判断出度map中是否有当前key
					inValue_count++;
					tempList.add(inElement_index);
				}
	        }
			inDegress[i] = inValue_count;
			in_G.add(tempList);
			//访问标志数组添加默认值false
			visi[i] = false;
		}
		long inittime = System.currentTimeMillis();
		List<HashMap<Integer, List<Integer>>> path2 = creatPath2(out_G, n);
		List<List<Integer>> result = new ArrayList<List<Integer>>();
		long dfs1 = System.currentTimeMillis();
		for (int i  = 0; i < n; i++) {
			System.out.println("key->" + i);
			trace = new ArrayList<>();
			//reachable数组表示从访问的i节点到reachable数组中是否有可达路径，0表示存在，-1表示不存在。
			//i循环后，对reachable数组重新初始化，由于访问的节点大于head（i）节点，因此要从i+1开始遍历
			for (int reach = i + 1; reach < n; reach++) {
				reachable[reach] = -1;
			}

			List<Integer> headList = in_G.get(i);
			//查询i点到访问的j是否可达，j为reachable数组下标，i更新一次，reachable数组更新一次，动态更改
			for (int headNextNode : headList) {
				if (headNextNode > i) {
					List<Integer> headNextList = in_G.get(headNextNode);
					for (int j = 0,len = inDegress[headNextNode]; j < len; j++) {
						int value = headNextList.get(j);
						if (value > i) {
							reachable[value] = 0;
						}
					}
				}
			}
			decDfs(i, i, 1, out_G, path2, result, visi);
		}
		long dfs2 = System.currentTimeMillis();
		Collections.sort(result, new DicComparator());	
		long endwri = System.currentTimeMillis();
		savePredictResult(output, result,nodeArr);
		long endPro = System.currentTimeMillis();
		System.out.println("All Time(s): " + (endPro - start) * 1.0 / 1000 + "s");
		System.out.println("save Time(s): " + (endPro - endwri) * 1.0 / 1000 + "s");
		System.out.println("dfs Time(s): " + (dfs2 - dfs1) * 1.0 / 1000 + "s");
		System.out.println("init Time(s): " + (dfs1 - start) * 1.0 / 1000 + "s");
		System.out.println("size->" + result.size());
	}
	public static void decDfs(int head, int current, int depth,List<List<Integer>> G ,List<HashMap<Integer, List<Integer>>> path2, List<List<Integer>> result, boolean[] visi ) {
		visi[current] = true;
		trace.add(current);
		List<Integer> circle = null;
		List<Integer> list = G.get(current);
		//七层
		if (reachable[current] != -1 &&depth == 6) {	
			List<Integer> path2List = path2.get(current).get(head);
				if (path2List!=null) {	
						for (int i = 0,len = path2List.size();i < len; i++) {
							int lastNode  = (int) path2List.get(i);
							if (lastNode > head && !visi[lastNode]) {
								circle = new ArrayList<>();
								trace.add(lastNode);
								circle.addAll(trace);
								result.add(circle);
								visi[lastNode] =  false;
								trace.remove(trace.size() - 1);
							}
						}
				}
		}
		//3-6层
		else if (depth < 6) {
			for (int i = 0,len = list.size(); i < len; i++) {
				int gcur = list.get(i);
				//3-5层
				if (gcur == head && depth >= 3 && depth < 6) {
					circle = new ArrayList<>();
					circle.addAll(trace);
					result.add(circle);
				}
				if (gcur > head && depth < 6 && !visi[gcur]) {
					decDfs(head, gcur, depth + 1, G, path2, result,visi );
				}
			}
			//第六层
			//在第五层时，第五个节点，使用path2方法，寻找中间节点k，可增加搜索速度（5+1,6+1方案）
			if (reachable[current] != -1 && (depth == 5)) {
				List<Integer> path2List = path2.get(current).get(head);
				for (int i1 = 0; i1 < path2List.size(); i1++) {
					int lastNode = (int) path2List.get(i1);
					if (!visi[lastNode]) {
						circle = new ArrayList<>();
						circle.addAll(trace);
						circle.add(lastNode);
						result.add(circle);
					}
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
	public static List creatPath2(List<List<Integer>> G ,int nodeArr ) {
		List<HashMap<Integer, List<Integer>>> path2 = new ArrayList<>(nodeArr);
		//i 代表第6个节点
		for (int i = 0;i<nodeArr;i++) {
			List<Integer> gi = G.get(i);
			HashMap<Integer, List<Integer>> giMap = new HashMap<Integer, List<Integer>>();;
			path2.add(giMap);
			//k 代表第7个节点
			for (Integer k : gi) {
				List<Integer> gk = G.get(k);
					//j 代表第8个节点
					for (int j : gk) {									
							List<Integer> circle = path2.get(i).get(j);
							//k为中间节点，j为head节点，根据剪枝规则，k>j构造路径节点的list，一次访问，不需要判断
							if (j != i && k > j) {
								if (circle==null) {
									circle = new ArrayList<Integer>();
								}
								circle.add(k);
								giMap.put(j, circle);				
							}					
					}		
			}

		}
		return path2;
	}
	private static void savePredictResult(String predictFileName, List<List<Integer>> allNotRepCirclesList,int[] nodeArr ) {
		try {
			BufferedWriter out = new BufferedWriter(new FileWriter(predictFileName));
			out.write(allNotRepCirclesList.size() + "\n");
			for (int i = 0,len_1 = allNotRepCirclesList.size(); i < len_1; i++) {
				for (int j = 0,len_2 = allNotRepCirclesList.get(i).size(); j < len_2; j++) {
					if (j != len_2 - 1) {
						out.write(nodeArr[allNotRepCirclesList.get(i).get(j)] + ",");
					} else {
						out.write(nodeArr[allNotRepCirclesList.get(i).get(j)] + "\n");
					}
				}
			}
			out.close();
		} catch (IOException exception) {
			System.err.println(exception.getMessage());
		}
	}
	static class DicComparator implements Comparator<List<Integer>> {

		@Override
		public int compare(List<Integer> var1, List<Integer> var2) {
			if (var1.size() > var2.size()) {
				return 1;
			}
			if (var1.size() == var2.size()) {
				int index = 0;
				for (int i = 0; i < var1.size(); i++) {
					if (var1.get(i) != var2.get(i)) {
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
