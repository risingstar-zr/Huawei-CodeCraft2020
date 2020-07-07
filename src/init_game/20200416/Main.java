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
	//static Set<List<Integer>> allNotRepCircles = new HashSet<>();
	static List<HashMap<Integer, List<Integer>>> path2;
	static String[] nodeLF = null;
	static String[] nodeRF = null;
	static int[] reachable;
	static int[] isReachable;
	static List<List<Integer>> allNotRepCirclesList = null;
	static List<List<Integer>> AllG = null;
	public static void main(String[] args) throws NumberFormatException, IOException {
		long start = System.currentTimeMillis();
		List<List<Integer>> G = null;
		boolean[] visi = null;
		BufferedReader reader = null;
		int[] nodeArr = null;

		String line = "";
		String fileNames = "/data/test_data.txt"; 
		String output = "/projects/student/result.txt";
		
		 
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
		AllG= new ArrayList<>(nodeSet.size());
		outDegress = new int[nodeSet.size()];
		visi = new boolean[nodeSet.size()];
		nodeArr = new int[nodeSet.size()];
		nodeLF = new String[nodeSet.size()];
		nodeRF= new String[nodeSet.size()];
		reachable = new int[nodeSet.size()];
		isReachable = new int[nodeSet.size()];
		Collections.sort(nodeList);

		for (int i = 0; i < nodeArr.length; i++) {
			nodeArr[i] = nodeList.get(i);
			nodeLF[i] = nodeList.get(i) + "\n";
			nodeRF[i] = nodeList.get(i) + ",";
			idMap.put(nodeList.get(i), i);
			G.add(new ArrayList<>());
			InG.add(new ArrayList<>());
			AllG.add(new ArrayList<>());
		}

		for (int i = 0; i < inputs.size(); i += 2) {
			int u = (int) idMap.get(inputs.get(i));
			int v = (int) idMap.get(inputs.get(i + 1));
			G.get(u).add(v);
			InG.get(v).add(u);
			AllG.get(u).add(v);
			AllG.get(v).add(u);
			outDegress[u] = outDegress[u] + 1;
		}

		topoSort(G, nodeArr, visi);
		long p2Time = System.currentTimeMillis();
		creatPath2(G, nodeArr.length);
		trace = new ArrayList<>();
		
		allNotRepCirclesList = new ArrayList<List<Integer>>();
		long dfsTime = System.currentTimeMillis();
		
		for (int i = 0; i < nodeArr.length; i++) {
			if (!G.get(i).isEmpty()) {
				//System.out.println("key->"+i);
				for (int reach = i + 1; reach < nodeArr.length; reach++) {
					reachable[reach] = -1;
					isReachable[reach] = -1;
				}

				List<Integer> headList = InG.get(i);
			
				for (int headNextNode : headList) {
					if (headNextNode > i) {
						List<Integer> headNextList = InG.get(headNextNode);
						for (int j = 0; j < headNextList.size(); j++) {
							reachable[headNextList.get(j)] = 0;
						}
					}

				}
				
				cutNode(i);
				decDfs(i, i, 1, G, visi);
			}

		}
		long dfsebd = System.currentTimeMillis();
		System.out.println("dfs Time(s): " + (dfsebd - dfsTime) * 1.0 / 1000 + "s");
		Collections.sort(allNotRepCirclesList, new DicComparator());
		long wtTime = System.currentTimeMillis();
		savePredictResult(output, allNotRepCirclesList, nodeArr);
		long endPro = System.currentTimeMillis();
		System.out.println("All Time(s): " + (endPro - start) * 1.0 / 1000 + "s");
		System.out.println("wt Time(s): " + (endPro - wtTime) * 1.0 / 1000 + "s");
		System.err.println("p2-Time->"+(dfsTime-p2Time));
		
		System.out.println("size->" + allNotRepCirclesList.size());

	}
	public static void cutNode(int current) {
		isReachable[current] = 0;
		List<Integer> list = AllG.get(current);
		for(int second:list) {
			if (second>current) {
				isReachable[second] = 0;
				List<Integer> secondList = AllG.get(second);
				for(int third:secondList) {
					if (third>current) {
						isReachable[third] = 0;
						List<Integer> thirdList = AllG.get(third);
						for(int forth:thirdList){
							if (forth>current) {
								isReachable[forth] = 0;
							}
							
						}
					}
				}
			}
		}
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
					allNotRepCirclesList.add(circle);

				}
			}
		}

		else if (depth < 6) {
			int it = lowerBound(list, 0, list.size(), head);
			for (int i = it, len = list.size(); i < len; i++) {
				int gcur = list.get(i);
				if (gcur == head && depth >= 3 && depth < 6) {
					circle = new ArrayList<>();
					circle.addAll(trace);
					allNotRepCirclesList.add(circle);
				}
				if (isReachable[gcur]==0&&gcur > head && depth < 6 && !visi[gcur]) {
					decDfs(head, gcur, depth + 1, G, visi);
				}

			}
		
			if (reachable[current] != -1 && (depth == 5)) {
				List<Integer> path2List = path2.get(current).get(head);
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

		}

		visi[current] = false;
		trace.remove(trace.size() - 1);
	}
	
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
	
		for (int i = 0; i < nodeArr.length; i++) {
			if (outDegress[i] == 0) {
				G.get(i).clear();
			} else {
				Collections.sort(G.get(i));
			}

		}

	}

	public static int lowerBound(List<Integer> nums, int l, int r, int target) {
		while (l < r) {
			int m = (l + r) / 2;
			if (nums.get(m) >= target)
				r = m;
			else
				l = m + 1;
		}
		return l;

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
					if (j != i && k > j) {
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

	private static void savePredictResult(String predictFileName, List<List<Integer>> allNotRepCirclesList,
			int[] nodeArr) {
		try {
			BufferedWriter out = new BufferedWriter(new FileWriter(predictFileName));
			out.write(allNotRepCirclesList.size() + "\n");
			for (int i = 0,len = allNotRepCirclesList.size(); i < len; i++) {
				for (int j = 0,len_1 = allNotRepCirclesList.get(i).size(); j < len_1; j++) {
					if (j != len_1 - 1) {
						out.write(nodeRF[allNotRepCirclesList.get(i).get(j)]);
					} else {
						out.write(nodeLF[allNotRepCirclesList.get(i).get(j)]);
					}
				}
			}
			out.close();
		} catch (IOException exception) {
			System.err.println(exception.getMessage());
		}
/*		int length = allNotRepCirclesList.size();
		StringBuilder stringBuilder = new StringBuilder("");
		stringBuilder.append(length + "\n");
		for (int i = 0; i < length; i++) {
			for (int j = 0; j < allNotRepCirclesList.get(i).size(); j++) {
				if (j != allNotRepCirclesList.get(i).size() - 1) {
					stringBuilder.append(nodeRF[allNotRepCirclesList.get(i).get(j)]);
				} else {
					stringBuilder.append(nodeLF[allNotRepCirclesList.get(i).get(j)]);
				}
			}
		}
		BufferedWriter out;
		try {
			out = new BufferedWriter(new FileWriter(predictFileName));
			out.write(stringBuilder.toString());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}*/
		
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
