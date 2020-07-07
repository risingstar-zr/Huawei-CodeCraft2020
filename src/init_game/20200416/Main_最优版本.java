package init_game_0416;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Main_最优版本 {

	public static int threadNum = 4;

	public static void main(String[] args) {
		long start = System.currentTimeMillis();
		GraghSerach gs = new GraghSerach();
		try {
			gs.initGragh();
		} catch (NumberFormatException e) {

			e.printStackTrace();
		} catch (IOException e) {

			e.printStackTrace();
		}
		gs.topoSort(gs.G, gs.nodeArr );
		gs.creatPath2(gs.G, gs.nodeArr.length);
		int sampleNum = gs.nodeArr.length;

		ExecutorService exe = Executors.newFixedThreadPool(threadNum);
		SerachCricleThread r1 = new SerachCricleThread(gs, 0, sampleNum/30);
		SerachCricleThread r2 = new SerachCricleThread(gs, sampleNum/30,sampleNum/10);
		SerachCricleThread r3 = new SerachCricleThread(gs,
				sampleNum/10,
				sampleNum/5);
		SerachCricleThread r4 = new SerachCricleThread(gs, sampleNum/5, sampleNum);

		exe.execute(r1);
		exe.execute(r2);
		exe.execute(r3);
		exe.execute(r4);
		exe.shutdown();
		while (true) {
			if (exe.isTerminated()) {
				break;
			}
		}
		List<List<Integer>> circleList = new ArrayList<List<Integer>>();
		circleList.addAll(r1.getList());
		circleList.addAll(r2.getList());
		circleList.addAll(r3.getList());
		circleList.addAll(r4.getList());
		Collections.sort(circleList, new DicComparator());
		
		gs.savePredictResult(circleList);
		long alltime = System.currentTimeMillis();
		System.out.println("All Time(s): " + (alltime - start) * 1.0 / 1000 + "s");
	}
}

class SerachCricleThread implements Runnable {
	GraghSerach gs;
	int start;
	int end;
	boolean visi[];
	List<List<Integer>> allNotRepCirclesList = new ArrayList<>();
	int[] reachable;

	public SerachCricleThread(GraghSerach gs, int start, int end) {
		this.gs = gs;
		this.start = start;
		this.end = end;

	}

	public List<List<Integer>> getList() {
		return allNotRepCirclesList;
	}

	@Override
	public void run() {
		List<Integer> trace = new ArrayList<>();
		visi = new boolean[gs.nodeArr.length];
		reachable = new int[gs.nodeArr.length];
		for (int i = start; i < end; i++) {
			for (int reach = i + 1; reach < gs.nodeArr.length; reach++) {
				reachable[reach] = -1;
			}

			List<Integer> headList = gs.InG.get(i);

			for (int headNextNode : headList) {
				if (headNextNode > i) {
					List<Integer> headNextList = gs.InG.get(headNextNode);
					for (int j = 0; j < headNextList.size(); j++) {
						reachable[headNextList.get(j)] = 0;
					}
				}

			}
			gs.decDfs(i, i, 1, visi, trace, allNotRepCirclesList, reachable);
		}

	}

}

class GraghSerach {
	Set<Integer> nodeSet = new HashSet<>();
	List<Integer> inputs = new ArrayList<>();
	HashMap<Integer, Integer> idMap = new HashMap<Integer, Integer>();
	int[] outDegress = null;
	List<Integer> trace;

	List<HashMap<Integer, List<Integer>>> path2;
	String fileNames = "D:\\java\\codecraft\\CodeCraft_2020\\src\\initial_game_data\\test_data_280w.txt";
	String output = "D:\\java\\codecraft\\CodeCraft_2020\\src\\initial_game_data\\result_0416.txt";
	/*String output = "F://data/test_ansser.txt";
	String fileNames = "F://data/test_data1.txt";*/
//	String fileNames = "/data/test_data.txt"; 
//	String output = "/projects/student/result.txt";
	List<List<Integer>> G = null;
	boolean[] visi = null;
	BufferedReader reader = null;
	int[] nodeArr = null;
	String line = "";
	List<Integer> nodeList = new ArrayList<Integer>();
	int[] reachable;
	List<List<Integer>> InG = null;
	String[] nodeLF = null;
	String[] nodeRF = null;
	public GraghSerach() {

	}

	public void initGragh() throws NumberFormatException, IOException {
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
		nodeLF = new String[nodeSet.size()];
		nodeRF= new String[nodeSet.size()];
		reachable = new int[nodeSet.size()];
		InG = new ArrayList<>(nodeSet.size());
		Collections.sort(nodeList);

		for (int i = 0; i < nodeArr.length; i++) {
			nodeArr[i] = nodeList.get(i);
			idMap.put(nodeList.get(i), i);
			nodeLF[i] = nodeList.get(i) + "\n";
			nodeRF[i] = nodeList.get(i) + ",";
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
	}
	public void topoSort(List<List<Integer>> G, int[] nodeArr) {
		Queue<Integer> queue = new LinkedList<Integer>();
		List<Integer> restoppList = new ArrayList<Integer>();
		for (int key = 0; key < nodeArr.length; key++) {
			if (outDegress[key] == 0) {
				queue.offer(key);
			}
		}
		while (!queue.isEmpty()) {
			int j = queue.poll().intValue();
		
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
	public void decDfs(int head, int current, int depth, boolean[] visi, List<Integer> trace,
		List<List<Integer>> allNotRepCirclesList, int[] reachable) {
		visi[current] = true;
		trace.add(current);
		List<Integer> circle = null;
		List<Integer> list = G.get(current);
		if (reachable[current] != -1 && depth == 6) {
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
			for (int i =it, len = list.size(); i < len; i++) {
				int gcur = list.get(i);
				if (gcur == head && depth >= 3 && depth < 6) {
					circle = new ArrayList<>();
					circle.addAll(trace);
					allNotRepCirclesList.add(circle);
				}
				if (gcur > head && depth < 6 && !visi[gcur]) {
					decDfs(head, gcur, depth + 1, visi, trace, allNotRepCirclesList, reachable);
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

	public int lowerBound(List<Integer> nums, int l, int r, int target) {
		while (l < r) {
			int m = (l + r) / 2;
			if (nums.get(m) >= target)
				r = m;
			else
				l = m + 1;
		}
		return l;

	}

	public void creatPath2(List<List<Integer>> G, int nodeArr) {
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

	public void savePredictResult(List<List<Integer>> allNotRepCirclesList) {

		try {
			BufferedWriter out = new BufferedWriter(new FileWriter(output));
			int length = allNotRepCirclesList.size();
			out.write(length + "\n");
			for (int i = 0; i < length; i++) {
				int len = allNotRepCirclesList.get(i).size();
				for (int j = 0; j < len-1; j++) {
						out.write(nodeRF[allNotRepCirclesList.get(i).get(j)]);
				}
				out.write(nodeLF[allNotRepCirclesList.get(i).get(len-1)]);
			}
			out.close();
		} catch (IOException exception) {
			System.err.println(exception.getMessage());
		}
	}

}

class DicComparator implements Comparator<List<Integer>> {

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