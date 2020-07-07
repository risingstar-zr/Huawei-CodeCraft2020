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
import java.util.Iterator;
import java.util.LinkedList;
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
		int sampleNum = gs.n;

		ExecutorService exe = Executors.newFixedThreadPool(threadNum);
		SerachCricleThread r1 = new SerachCricleThread(gs, 0, sampleNum/36);
		SerachCricleThread r2 = new SerachCricleThread(gs, sampleNum/36, sampleNum/11);
		SerachCricleThread r3 = new SerachCricleThread(gs, sampleNum/11, sampleNum/6);
		SerachCricleThread r4 = new SerachCricleThread(gs, sampleNum/6, sampleNum);

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
		LinkedList<Integer> trace = new LinkedList<>();
		int n = gs.n;
		visi = new boolean[n];
		reachable = new int[n];
		
		for (int i = start; i < end; i++) {
			for (int reach = i+1; reach < n; reach++) {
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

class GraghSerach {
	Set<Integer> nodeSet = new HashSet<>(150000);
	HashMap<Integer, Integer> idMap = new HashMap<Integer, Integer>();
	int[] outDegress = null;
	int[] inDegress = null;
	List<Integer> trace;
	String fileNames = "/data/test_data.txt"; 
	String output = "/projects/student/result.txt";
	int[][] outG = null;
	int[][] InG = null;
	int out_max = 50;
	int in_max = 50;
	//有效节点的总个数
	int n = 0;
	boolean[] visi = null;
	BufferedReader reader = null;
	int[] nodeArr = null;
	String line = "";
	List<Integer> nodeList = new ArrayList<Integer>();
	int[] reachable;
	String[] nodeLF = null;
	String[] nodeRF = null;
	public GraghSerach() {

	}

	public void initGragh() throws NumberFormatException, IOException {
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
		//大佬测试了最大的入度为不大于255
		InG = new int[n][in_max];
		inDegress = new int[n];
		//大佬测试了最大的出度为不大于50
		outG = new int[n][out_max];
		outDegress = new int[n];
		//定义有效的结点数组
		nodeArr = new int[n];
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
	}
	public void insertEdge(int a,int b,Map<Integer, ArrayList<Integer>> outList,Map<Integer, ArrayList<Integer>> inputList,Map<Integer, Integer> outputValue,Map<Integer, Integer> inputValue){
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
	
	public void decDfs(int head, int current, int depth, boolean[] visi, LinkedList<Integer> trace, List<List<Integer>> allNotRepCirclesList, int[] reachable,Map<Integer, List<Integer>> mas ) {
		visi[current] = true;
		trace.add(current);
		LinkedList<Integer> circle = null;
		int[] arr = outG[current];
		if (reachable[current] != -1 && depth == 6) {
			List<Integer> path2List =  mas.get(current);
			for (int i = 0; i < path2List.size(); i++) {
				int lastNode = (int) path2List.get(i);
				if (!visi[lastNode]) {
					circle = new LinkedList<>();
					circle.addAll(trace);
					circle.add(lastNode);
					allNotRepCirclesList.add(circle);

				}
			}
		}

		else if (depth < 6) {
			for (int i = 0,len = outDegress[current]; i < len; i++) {
				int gcur = arr[i];
				if (gcur == head && depth >= 3 && depth < 6) {
					circle = new LinkedList<>();
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
						circle = new LinkedList<>();
						circle.addAll(trace);
						circle.add(lastNode);
						allNotRepCirclesList.add(circle);
					}

				}
			}

		}

		visi[current] = false;
		trace.removeLast();
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