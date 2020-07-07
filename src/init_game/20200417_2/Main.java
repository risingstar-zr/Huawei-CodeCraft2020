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
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class Main {

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
		int sampleNum = gs.nodeArr.length;

		ExecutorService exe = Executors.newFixedThreadPool(threadNum);
		SerachCricleThread r1 = new SerachCricleThread(gs, 0, sampleNum/30);
		SerachCricleThread r2 = new SerachCricleThread(gs, sampleNum/30, sampleNum/10);
		SerachCricleThread r3 = new SerachCricleThread(gs, sampleNum/10, sampleNum/5);
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
		long wrtime = System.currentTimeMillis();
		gs.savePredictResult(circleList);
		long alltime = System.currentTimeMillis();
		System.out.println("All Time(s): " + (alltime - start) * 1.0 / 1000 + "s");
		System.out.println("wr Time(s): " + (alltime - wrtime) * 1.0 / 1000 + "s");
	}
}

class SerachCricleThread implements Runnable {
	GraghSerach gs;
	int start;
	int end;
	boolean visi[];
	List<List<Integer>> allNotRepCirclesList = new ArrayList<>();
	int[] reachable;
	ArrayList<Integer>[] s2;
	Map<Integer, List<Integer>> mas  = null;
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
			if (gs.outDegress[i] > 0) {
				for (int reach = i+1; reach < gs.nodeArr.length; reach++) {
					reachable[reach] = -1;
				}
				mas = new HashMap<Integer, List<Integer>>();
				int[] headList = gs.InG[i];
				for(int j=0,len_1 = gs.inDegress[i];j<len_1;j++) {
					int headNextNode = headList[j];
					if (headNextNode > i) {
						int[] headNextList = gs.InG[headNextNode];
						for (int k = 0,len_2 = gs.inDegress[headNextNode]; k<len_2;k++) {
							int temp = headNextList[k];
							reachable[temp] = 0;
							if (mas.get(temp)==null) {
								mas.put(temp, new ArrayList<Integer>());
							}
							mas.get(temp).add(headNextNode);
						}
					}
				}
				gs.decDfs(i, i, 1, visi, trace, allNotRepCirclesList, reachable,mas);
			}
		}
		
	}

}

class GraghSerach {
	Set<Integer> nodeSet = new HashSet<>(150000);
	List<Integer> inputs = new ArrayList<>(560000);
	HashMap<Integer, Integer> idMap = new HashMap<Integer, Integer>();
	int[] outDegress = null;
	int[] inDegress = null;
	List<Integer> trace;
	String fileNames = "/data/test_data.txt"; 
	String output = "/projects/student/result.txt";
	int[][] outG = null;
	int[][] InG = null;
	int out_max = 50;
	int in_max = 255;
	boolean[] visi = null;
	BufferedReader reader = null;
	int[] nodeArr = null;
	String line = "";
	List<Integer> nodeList = new ArrayList<Integer>();
	int[] reachable;
	ArrayList<Integer>[] s2;
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
		int n = nodeSet.size();
		//大佬测试了最大的入度为不大于255
		InG = new int[n][in_max];
		outDegress = new int[n];
		inDegress = new int[n];
		//大佬测试了最大的出度为不大于50
		outG = new int[n][out_max];
		visi = new boolean[n];
		nodeArr = new int[n];
		nodeLF = new String[n];
		nodeRF= new String[n];
		reachable = new int[n];
		Collections.sort(nodeList);

		for (int i = 0; i < n; i++) {
			nodeArr[i] = nodeList.get(i);
			idMap.put(nodeList.get(i), i);
			nodeLF[i] = Integer.toString(nodeList.get(i)) + "\n";
			nodeRF[i] = Integer.toString(nodeList.get(i))+ ",";
		}
		for (int i = 0,len = inputs.size(); i < len; i += 2) {
			int u = (int) idMap.get(inputs.get(i));
			int v = (int) idMap.get(inputs.get(i + 1));
			//获取当前元素的出度个数
			int uCount = outDegress[u];
			int vCount = inDegress[v];
			outG[u][uCount] = v;
			outDegress[u] = uCount+1;
			InG[v][vCount] = u;
			inDegress[v] = vCount+1;
		}
	}
	
	public void decDfs(int head, int current, int depth, boolean[] visi, List<Integer> trace, List<List<Integer>> allNotRepCirclesList, int[] reachable,Map<Integer, List<Integer>> mas ) {
		visi[current] = true;
		trace.add(current);
		List<Integer> circle = null;
		int[] list = outG[current];
		if (reachable[current] != -1 && depth == 6) {
			List<Integer> path2List =  mas.get(current);
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
			for (int i = 0,len = outDegress[current]; i < len; i++) {
				int gcur = list[i];
				if (gcur == head && depth >= 3 && depth < 6) {
					circle = new ArrayList<>();
					circle.addAll(trace);
					allNotRepCirclesList.add(circle);
				}
				if (gcur > head && depth < 6 && !visi[gcur]) {
					decDfs(head, gcur, depth + 1, visi, trace, allNotRepCirclesList, reachable,mas);
				}

			}

			if (reachable[current] != -1 && (depth == 5)) {
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

		}

		visi[current] = false;
		trace.remove(trace.size() - 1);
	}

	public void savePredictResult(List<List<Integer>> allNotRepCirclesList) {

		try {
			BufferedWriter out = new BufferedWriter(new FileWriter(output));
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