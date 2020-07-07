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
	static int[][] outG = null;
	static List<Integer> trace;
	static String[] nodeLF = null;
	static String[] nodeRF = null;
	static int[] reachable;
	static Map<Integer, List<Integer>> mas  = null;
	static StringBuilder[] sb = new StringBuilder[8];
	static int circleCount = 0;
	public static void main(String[] args) throws NumberFormatException, IOException {
		long start = System.currentTimeMillis();
		BufferedReader reader = null;
		String line = "";
		String fileNames = "/data/test_data.txt"; 
		String output = "/projects/student/result.txt";
		Map<Integer, ArrayList<Integer>> outList = new HashMap<Integer, ArrayList<Integer>>();
		Map<Integer, ArrayList<Integer>> inputList = new HashMap<Integer, ArrayList<Integer>>();
		Map<Integer, Integer> outputValue = new HashMap<>() ; //
		Map<Integer, Integer> inputValue = new HashMap<>() ; //
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
		int out_max = 50;
		int in_max = 50;
		int[][] InG = new int[n][in_max];
		int[] inDegress = new int[n];
		outG = new int[n][out_max];
		outDegress = new int[n];
		HashMap<Integer, Integer> idMap = new HashMap<Integer, Integer>();
		//
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
			ArrayList<Integer> mList = outList.get(element);
			for (int j = 0,len_m = mList.size(); j < len_m; j++) {
				int outElement = mList.get(j);
				Integer outElement_index = idMap.get(outElement);
				if(outElement_index != null) {//
					outG[i][outValue_count] = outElement_index;
					outValue_count++;
				}
	        }
			outDegress[i] = outValue_count;
			Arrays.sort(outG[i],0,outDegress[i]);

			ArrayList<Integer> inList = inputList.get(element);
			for (int k = 0,len_in = inList.size(); k < len_in; k++) {
				int inElement = inList.get(k);
				Integer inElement_index = idMap.get(inElement);
				if(inElement_index != null) {//
					InG[i][inValue_count] = inElement_index;
					inValue_count++;
				}
	        }
			inDegress[i] = inValue_count;
			Arrays.sort(InG[i],0,inDegress[i]);
		}
		trace = new ArrayList<>();
		long dfs1 = System.currentTimeMillis();
		
		for (int i = 0; i < n; i++) {
			for (int reach = i+1; reach < n; reach++) {
				reachable[reach] = -1;
			}
			mas = new HashMap<Integer, List<Integer>>();
			int[] headList = InG[i];
			for(int j=0,len_1 = inDegress[i];j<len_1;j++) {
				//倒数第一个节点
				int headNextNode = headList[j];
				if (headNextNode > i) {
					int[] headNextList = InG[headNextNode];
					for (int k = 0,len_2 = inDegress[headNextNode]; k<len_2;k++) {
						//倒数第二个节点
						int temp = headNextList[k];
						if(temp > i) {
							reachable[temp] = 1;
							if (mas.get(temp)==null) {
								mas.put(temp, new ArrayList<Integer>());
							}
							mas.get(temp).add(headNextNode);
						}
					}
				}
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
		if (reachable[current] == 1 && depth == 6) {
			List<Integer> path2List =  mas.get(current);
			for (int i = 0; i < path2List.size(); i++) {
				int lastNode = (int) path2List.get(i);
				if (!visi[lastNode]) {	
					for(int key = 0;key<trace.size();key++) {
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
				if( gcur > head && !visi[gcur]) {
					decDfs(head, gcur, depth + 1, visi);
				//找长度为3,4,5的环
				}else if(gcur == head && depth >= 3 && depth < 6) {
					int length = trace.size()-1;
					for(int key = 0;key < length;key++) {
						sb[depth].append(nodeRF[trace.get(key)]);
					}
					sb[depth].append(nodeLF[trace.get(length)]);
					circleCount++;
				}
			}
		
			if (reachable[current] == 1 && (depth == 5)) {
				List<Integer> path2List = mas.get(current);
				for (int i1 = 0; i1 < path2List.size(); i1++) {
					int lastNode = (int) path2List.get(i1);
					if (!visi[lastNode]) {
						for(int key = 0;key<trace.size();key++) {
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
	 public static void write(String predictFileName){
	        File file = new File(predictFileName);
	        try(PrintWriter writer = new PrintWriter(new FileWriter(file))){
	            writer.print(Integer.toString(circleCount) + "\n");

				for (int i = 3; i < sb.length; i++) {
					writer.print(sb[i].toString());
				}
				//writer.close();
	        }
	        catch (IOException e){
	            
	            throw new RuntimeException();
	        }
	    }

}
