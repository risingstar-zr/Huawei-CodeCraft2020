import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Main {

	public static int threadNum = 3;

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
		SerachCricleThread r1 = new SerachCricleThread(gs, 0, sampleNum/15);
		SerachCricleThread r2 = new SerachCricleThread(gs, sampleNum/15, sampleNum/8);
		SerachCricleThread r3 = new SerachCricleThread(gs, sampleNum/8, sampleNum);
		exe.execute(r1);
		exe.execute(r2);
		exe.execute(r3);
		exe.shutdown();
		while (true) {
			if (exe.isTerminated()) {
				break;
			}
		}
		StringBuilder result = new StringBuilder(7*7*2800000);
		StringBuilder[] result_r1  = r1.getList();
		StringBuilder[] result_r2  = r2.getList();
		StringBuilder[] result_r3  = r3.getList();
		int[] circleCount_r1 = r1.getCount();
		int[] circleCount_r2 = r2.getCount();
		int[] circleCount_r3 = r3.getCount();
		int circleCount =circleCount_r1[0]+ circleCount_r2[0]+circleCount_r3[0];
        result.append(Integer.toString(circleCount) + "\n");

		for (int i = 3; i < 8; i++) {
			result.append(result_r1[i].toString());
			result.append(result_r2[i].toString());
			result.append(result_r3[i].toString());
		}
		long wrtime = System.currentTimeMillis();
		gs.savePredictResult(result);
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
	StringBuilder[] sb = new StringBuilder[8];
	int[] circleCount = {0};
	int[] reachable;
	Map<Integer, ArrayList<Integer>> mas = null;
    Map<Integer, Integer> weightMap = null;

	public SerachCricleThread(GraghSerach gs, int start, int end) {
		this.gs = gs;
		this.start = start;
		this.end = end;
	}

	public StringBuilder[] getList() {
		return sb;
	}
	
	public int[] getCount() {
		return circleCount;
	}
	@Override
	public void run() {
		List<Integer> trace = new ArrayList<>();
		List<Integer> weightTrace = new ArrayList<>();

		int n = gs.n;
		visi = new boolean[n];
		reachable = new int[n];
		sb[3] = new StringBuilder(3*3*200000);
        sb[4] = new StringBuilder(4*4*200000);
        sb[5] = new StringBuilder(5*5*500000);
        sb[6] = new StringBuilder(6*6*500000);
        sb[7] = new StringBuilder(7*7*1000000);
		
		for (int i = start; i < end; i++) {
			for (int reach = i+1; reach < n; reach++) {
				reachable[reach] = -1;
			}
			mas = new HashMap<Integer, ArrayList<Integer>>();
			weightMap = new HashMap<Integer, Integer>();

			ArrayList<Integer> firstList = gs.InG.get(i).get(0);
			ArrayList<Integer> firstWeightList = gs.InG.get(i).get(1);
			for(int j=0,len_1 = gs.inDegress[i];j<len_1;j++) {
				//倒数第一个节点
				int firstNode = firstList.get(j);
				//倒数第一个节点权重
				int firstWeight = firstWeightList.get(j);
				if (firstNode > i) {
					ArrayList<Integer> secondList = gs.InG.get(firstNode).get(0);
					ArrayList<Integer> secondWeightList = gs.InG.get(firstNode).get(1);
					
					for (int k = 0,len_2 = gs.inDegress[firstNode]; k<len_2;k++) {
						//倒数第二个节点
						int secondNode = secondList.get(k);
						//倒数第二个节点权重
						int secondWeight = secondWeightList.get(k);
						
						double tt = (double)firstWeight/secondWeight;
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
			gs.decDfs(i, i, 1, visi, trace, weightTrace,sb, reachable,mas,weightMap,circleCount);

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
	ArrayList<ArrayList<ArrayList<Integer>>> outG = null;
	ArrayList<ArrayList<ArrayList<Integer>>> InG = null;

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
		InG = new ArrayList<ArrayList<ArrayList<Integer>>>(n);
		inDegress = new int[n];
		//大佬测试了最大的出度为不大于50
		outDegress = new int[n];
		outG = new ArrayList<ArrayList<ArrayList<Integer>>>(n);
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
		ArrayList<ArrayList<Integer>> al;
		ArrayList<Integer> al_1;
		ArrayList<Integer> al_2;
		ArrayList<ArrayList<Integer>> bl;
		ArrayList<Integer> bl_1;
		ArrayList<Integer> bl_2;
		for (int i = 0; i < n; i++) {
			int element = nodeArr[i];
			int outValue_count =0;
			int inValue_count =0;
			ArrayList<Integer> mList = outList.get(element).get(0);
			ArrayList<Integer> mWeightList = outList.get(element).get(1);
			al = new ArrayList<ArrayList<Integer>>();
			al_1 = new ArrayList<Integer>();
			al_2 = new ArrayList<Integer>();
			for (int j = 0,len_m = mList.size(); j < len_m; j++) {
				int outElement = mList.get(j);
				Integer outElement_index = idMap.get(outElement);
				if(outElement_index != null) {//判断出度map中是否有当前key
					al_1.add(outElement_index);
					al_2.add(mWeightList.get(j));
					outValue_count++;
				}
	        }
			outDegress[i] = outValue_count;
			sortArray(al_1,al_2,outValue_count);
			al.add(al_1);
			al.add(al_2);
			outG.add(al);
			
			ArrayList<Integer> inList = inputList.get(element).get(0);
			ArrayList<Integer> inWeightList = inputList.get(element).get(1);
			bl = new ArrayList<ArrayList<Integer>>();
			bl_1 = new ArrayList<Integer>();
			bl_2 = new ArrayList<Integer>();
			for (int k = 0,len_in = inList.size(); k < len_in; k++) {
				int inElement = inList.get(k);
				Integer inElement_index = idMap.get(inElement);
				if(inElement_index != null) {//判断出度map中是否有当前key
					bl_1.add(inElement_index);
					bl_2.add(inWeightList.get(k));
					inValue_count++;
				}
	        }
			inDegress[i] = inValue_count;
			//排序
			sortArray(bl_1,bl_2,inValue_count);
			bl.add(bl_1);
			bl.add(bl_2);
			InG.add(bl);
		}
	}
	public void insertEdge(int a,int b,int c,Map<Integer, ArrayList<ArrayList<Integer>>> outList,Map<Integer, ArrayList<ArrayList<Integer>>> inputList,Map<Integer, Integer> outputValue,Map<Integer, Integer> inputValue){
		//出度
		if(outList.containsKey(a)) {
			ArrayList<ArrayList<Integer>> result = outList.get(a);
			result.get(0).add(b);
			result.get(1).add(c);
			int value = outputValue.get(a).intValue() + 1;
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
	
	/**冒泡排序
	 * 
	 */
	public void sortArray(ArrayList<Integer> list,ArrayList<Integer> weight,int length) {
	    for (int j = 0; j < length-1; j++) {  // 控制遍历的次数
            for (int i = 0; i < length-1; i++) {  // 找出相邻最新元素，交换位置
                if (list.get(i) > list.get(i + 1)) {
                    int temp = list.get(i);
                	int weightTemp = weight.get(i);
                    list.set(i, list.get(i+1));
                    list.set(i+1, temp);
                    weight.set(i, weight.get(i+1));
                    weight.set(i+1, weightTemp);
                }
            }
        }
	}
	public void decDfs(int head, int current, int depth, boolean[] visi, List<Integer> trace,List<Integer> weightTrace, StringBuilder[] sb, int[] reachable,Map<Integer, ArrayList<Integer>> mas,Map<Integer, Integer> weightMap,int[] countArr) {
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
				
				double t_1 = (double)FG_Weight/EF_Weight;

				// 比较(A-B)/(G-A)
				int GA_Weight = weightMap.get(lastNode);
				int AB_Weight = weightTrace.get(0);
				double t_2 = (double)AB_Weight/GA_Weight;

				if (!visi[lastNode] && t_1 >=0.2 && t_1 <=3 && t_2 >=0.2 && t_2 <=3) {
					for(int key = 0,len_1 = trace.size();key<len_1;key++) {
						sb[7].append(nodeRF[trace.get(key)]);
					}
					sb[7].append(nodeLF[lastNode]);
					countArr[0] = countArr[0]+1;
				}
			}
		}

		if (depth < 6) {
			ArrayList<Integer> list = outG.get(current).get(0);
			for (int i = 0,len = outDegress[current]; i < len; i++) {
				int gcur = list.get(i);
				int g_weight = outG.get(current).get(1).get(i);
				if(depth == 1 && gcur > head && !visi[gcur]) {
					weightTrace.add(g_weight);
					decDfs(head, gcur, depth + 1, visi, trace,weightTrace, sb, reachable,mas,weightMap,countArr);
					weightTrace.remove(weightTrace.size() - 1);
				}
				if(depth>=2 && gcur > head && !visi[gcur]) {
					int last_weight = weightTrace.get(weightTrace.size() - 1);
					double tt = (double)g_weight/last_weight;
					if(tt >=0.2 && tt <=3) {
						weightTrace.add(g_weight);
						decDfs(head, gcur, depth + 1, visi, trace,weightTrace, sb, reachable,mas,weightMap,countArr);
						weightTrace.remove(weightTrace.size() - 1);
					}
				//找长度为3,4,5的环
				}else if(gcur == head && depth >= 3 && depth < 6) {
					int last_weight = weightTrace.get(weightTrace.size() - 1);
					int first_weight = weightTrace.get(0);
					//A-B-C-A
					// (C-A)/(B-C) 
					double last_t = (double)g_weight/last_weight;

					// (A-B)/(C-A)
					double first_t = (double)first_weight/g_weight;

					if(last_t >=0.2 && last_t <=3 && first_t >=0.2 && first_t <=3) {
						int length = trace.size()-1;
						for(int key = 0;key < length;key++) {
							sb[depth].append(nodeRF[trace.get(key)]);
						}
						sb[depth].append(nodeLF[trace.get(length)]);
						countArr[0] = countArr[0]+1;
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
					double t_1 = (double)EF_Weight/DE_Weight;

					// 比较(A-B)/(F-A)
					int FA_Weight = weightMap.get(lastNode);
					int AB_Weight = weightTrace.get(0);
					double t_2 = (double)AB_Weight/FA_Weight;

					if (!visi[lastNode] && t_1 >=0.2 && t_1 <=3 && t_2 >=0.2 && t_2 <=3) {
						for(int key = 0,len_1 = trace.size();key<len_1;key++) {
							sb[6].append(nodeRF[trace.get(key)]);
						}
						sb[6].append(nodeLF[lastNode]);
						countArr[0] = countArr[0]+1;
					}
				}
			}

		}

		visi[current] = false;
		trace.remove(trace.size() - 1);
		
	}

	public void savePredictResult(StringBuilder result) {
		File file = new File(output);
        try(PrintWriter writer = new PrintWriter(new FileWriter(file))){
            writer.print(result.toString());
        }
        catch (IOException e){
            throw new RuntimeException();
        }
	}

}
