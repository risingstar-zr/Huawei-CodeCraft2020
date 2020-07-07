import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

public class Main {
	static Set<Integer> nodeSet = new HashSet<>();
	static List<Integer> inputs = new ArrayList<>();
	static HashMap<Integer, Integer> idMap = new HashMap<Integer, Integer>();

	static int[] outDegress = null;
	static List<List<Integer>> InG = null;
	static List<Integer> trace;
	static List<HashMap<Integer, List<Integer>>> path2;
	static int[] reachable;
	static List<List<Integer>> allCircles = new ArrayList<List<Integer>>();

	public static void main(String[] args) throws NumberFormatException, IOException {
		long start = System.currentTimeMillis();
		List<List<Integer>> G = null;
		boolean[] visi = null;
		BufferedReader reader = null;
		int[] nodeArr = null;

		String line = "";
		// String fileNames = "/data/test_data.txt"; 
		// String output = "/projects/student/result.txt";
		String fileNames = "/root/data/java/28w_2896262/test_data_280w.txt"; 
		String output = "./result0415.txt";
		List<Integer> nodeList = new ArrayList<Integer>();

		try {
			reader = new BufferedReader(new FileReader(fileNames));
			while ((line = reader.readLine()) != null) {
				String item[] = line.split(",");
				int startValue = Integer.parseInt(item[0]);
				int endValue = Integer.parseInt(item[1]);
				nodeSet.add(startValue);
				nodeSet.add(endValue);
				inputs.add(startValue);
				inputs.add(endValue);
			}
		} catch (FileNotFoundException exception) {
			System.err.println(fileNames + " File Not Found");
		}
		for (int key : nodeSet) {
			nodeList.add(key);
		}
		G = new ArrayList<>(nodeSet.size());
		InG = new ArrayList<>(nodeSet.size());
		outDegress = new int[nodeSet.size()];
		visi = new boolean[nodeSet.size()];
		nodeArr = new int[nodeSet.size()];
		reachable = new int[nodeSet.size()];
		Collections.sort(nodeList);

		for (int i = 0; i < nodeArr.length; i++) {
			nodeArr[i] = nodeList.get(i);
			idMap.put(nodeList.get(i), i);
			G.add(new ArrayList<>());
			InG.add(new ArrayList<>());
		}

		for (int i = 0; i < inputs.size(); i += 2) {
			int u = (int) idMap.get(inputs.get(i));
			int v = (int) idMap.get(inputs.get(i + 1));
			G.get(u).add(v);
			InG.get(v).add(u);
			outDegress[u] = outDegress[u] + 1;
		}

		topoSort(G, nodeArr, visi);

		creatPath2(G, nodeArr.length);
		trace = new ArrayList<>();
		long dfs1 = System.currentTimeMillis();
		for (int i = 0; i < nodeArr.length; i++) {
			if (!G.get(i).isEmpty()) {
				System.out.println("key->"+i);
				//reachable数组表示从访问的i节点到reachable数组中是否有可达路径，0表示存在，-1表示不存在。
				//i循环后，对reachable数组重新初始化，由于访问的节点大于head（i）节点，因此要从i+1开始遍历
				for (int reach = i + 1; reach < nodeArr.length; reach++) {
					reachable[reach] = -1;
				}

				List<Integer> headList = InG.get(i);
				//查询i点到访问的j是否可达，j为reachable数组下标，i更新一次，reachable数组更新一次，动态更改
				for (int headNextNode : headList) {
					if (headNextNode > i) {
						List<Integer> headNextList = InG.get(headNextNode);
						for (int j = 0; j < headNextList.size(); j++) {
							reachable[headNextList.get(j)] = 0;
						}
					}

				}
				decDfs(i, i, 1, G, visi);
			}

		}
		long dfs2 = System.currentTimeMillis();
		Collections.sort(allCircles, new DicComparator());
		System.out.println("dfs Time(s): " + (dfs2 - dfs1) * 1.0 / 1000 + "s");
		System.out.println("init Time(s): " + (dfs1 - start) * 1.0 / 1000 + "s");
		long endwri = System.currentTimeMillis();
		savePredictResult(output, nodeArr);
		long endPro = System.currentTimeMillis();
		System.out.println("All Time(s): " + (endPro - start) * 1.0 / 1000 + "s");
		System.out.println("save Time(s): " + (endPro - endwri) * 1.0 / 1000 + "s");
		System.out.println("size->" + allCircles.size());

	}

	public static void decDfs(int head, int current, int depth, List<List<Integer>> G, boolean[] visi) {
		visi[current] = true;
		trace.add(current);
		List<Integer> circle = null;
		List<Integer> list = G.get(current);
		if (reachable[current] != -1 && depth == 6) {
			// 六层
			List<Integer> path2List = path2.get(current).get(head);
			for (int i = 0; i < path2List.size(); i++) {
				int lastNode = (int) path2List.get(i);
				if (!visi[lastNode]) {
					circle = new ArrayList<>();
					circle.addAll(trace);
					circle.add(lastNode);
					allCircles.add(circle);
				}
			}
		}

		else if (depth < 6) {
			//int it = lowerBound(list, 0, list.size(), head);
			for (int i = 0, len = list.size(); i < len; i++) {
				int gcur = list.get(i);
				if (gcur == head && depth >= 3 && depth < 6) {
					circle = new ArrayList<>();
					circle.addAll(trace);
					allCircles.add(circle);
				}
				if (gcur > head && depth < 6 && !visi[gcur]) {
					decDfs(head, gcur, depth + 1, G, visi);
				}

			}
			//在第五层时，第五个节点，使用path2方法，寻找中间节点k，可增加搜索速度（5+1,6+1方案）
			if (reachable[current] != -1 && (depth == 5)) {
				List<Integer> path2List = path2.get(current).get(head);
				for (int i1 = 0,len_1 = path2List.size(); i1 < len_1; i1++) {
					int lastNode = (int) path2List.get(i1);
					if (!visi[lastNode]) {
						circle = new ArrayList<>();
						circle.addAll(trace);
						circle.add(lastNode);
						allCircles.add(circle);
					}
				}
			}

		}

		visi[current] = false;
		trace.remove(trace.size() - 1);
	}
	//拓扑排序（多少有点效果，0.5左右）
	public static void topoSort(List<List<Integer>> G, int[] nodeArr, boolean[] visi) {
		Queue<Integer> queue = new LinkedList<Integer>();
		List<Integer> restoppList = new ArrayList<Integer>();
		for (int key = 0; key < nodeArr.length; key++) {
			if (outDegress[key] == 0) {
				queue.offer(key);
			}
		}
		while (!queue.isEmpty()) {
			int j = queue.poll().intValue();
			visi[j] = true;
			restoppList.add(j);
			ArrayList<Integer> node = (ArrayList<Integer>) InG.get(j);
			if (node != null)
				for (int topo : node) {
					int curIn = outDegress[topo] - 1;
					if (curIn == 0) {
						queue.offer(topo);
					}
					outDegress[topo] = curIn;
				}
		}
		//去掉出度为0的节点的边表节点，用来进行初始判断（空则不访问）
		for (int i = 0; i < nodeArr.length; i++) {
			if (outDegress[i] == 0) {
				G.get(i).clear();
			} /*else {
				Collections.sort(G.get(i));
			}*/

		}

	}

	public static void creatPath2(List<List<Integer>> G, int nodeArr) {
		path2 = new ArrayList<>(nodeArr);
		for (int i = 0; i < nodeArr; i++) {
			List<Integer> gi = G.get(i);
			HashMap<Integer, List<Integer>> giMap = new HashMap<Integer, List<Integer>>();
			path2.add(giMap);
			for (Integer k : gi) {
				List<Integer> gk = G.get(k);
				for (int j : gk) {
					List<Integer> circle = giMap.get(j);
					if (j < i && k > j) {//k为中间节点，j为head节点，根据剪枝规则，k>j构造路径节点的list，一次访问，不需要判断
						if (circle == null) {
							circle = new ArrayList<Integer>();
						}
						circle.add(k);
						giMap.put(j, circle);
					}
				}
			}

		}
	}

	private static void savePredictResult(String predictFileName, int[] nodeArr) {
		try {
			int length = allCircles.size() ;
			StringBuilder stringBuilder = new StringBuilder();
			stringBuilder.append(length);
			stringBuilder.append("\n");
	        FileWriter fw = new FileWriter(predictFileName);
			BufferedWriter bw = new BufferedWriter(fw);
			for (int i = 0; i < length; i++) {
				for (int j = 0,len2 = allCircles.get(i).size(); j < len2; j++) {
					if (j != len2 - 1) {
						stringBuilder.append(nodeArr[allCircles.get(i).get(j)]);
						stringBuilder.append(",");
					} else {
						stringBuilder.append(nodeArr[allCircles.get(i).get(j)]);
						stringBuilder.append("\n");
					}
				}
				bw.write(stringBuilder.toString());
				stringBuilder = new StringBuilder();
			}
			bw.close() ; //关闭流
			fw.close();   //关闭流
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
