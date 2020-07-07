import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
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
		SerachCricleThread r2 = new SerachCricleThread(gs, sampleNum/15, sampleNum/7);
		SerachCricleThread r3 = new SerachCricleThread(gs, sampleNum/7, sampleNum);
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
		gs.writeNIO(result);
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
	Map<Integer, List<Integer>> mas  = null;
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
			mas = new HashMap<Integer, List<Integer>>();
			int[] headList = gs.InG[i];
			for(int j=0,len_1 = gs.inDegress[i];j<len_1;j++) {
				int headNextNode = headList[j];
				if (headNextNode > i) {
					int[] headNextList = gs.InG[headNextNode];
					for (int k = 0,len_2 = gs.inDegress[headNextNode]; k<len_2;k++) {
						int temp = headNextList[k];
						if(temp > i) {
							reachable[temp] = 0;
							if (mas.get(temp)==null) {
								mas.put(temp, new ArrayList<Integer>());
							}
							mas.get(temp).add(headNextNode);
						}
					}
				}
			}
			gs.decDfs(i, i, 1, visi, trace, sb, reachable,mas,circleCount);
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
			Arrays.sort(outG[i],0,outDegress[i]);

			
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
			Arrays.sort(InG[i],0,inDegress[i]);
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
	
	public void decDfs(int head, int current, int depth, boolean[] visi, List<Integer> trace, StringBuilder[] sb, int[] reachable,Map<Integer, List<Integer>> mas,int[] countArr) {
		visi[current] = true;
		trace.add(current);
		if (reachable[current] == 0 && depth == 6) {
			List<Integer> path2List =  mas.get(current);
			for (int i = 0; i < path2List.size(); i++) {
				int lastNode = (int) path2List.get(i);
				if (!visi[lastNode]) {	
					for(int key = 0;key<trace.size();key++) {
						sb[7].append(nodeRF[trace.get(key)]);
					}
					sb[7].append(nodeLF[lastNode]);
					countArr[0] = countArr[0]+1;
				}
			}
		}
		if (depth < 6) {
			int[] list = outG[current];
			for (int i = 0,len = outDegress[current]; i < len; i++) {
				int gcur = list[i];
				if( gcur > head && !visi[gcur]) {
					decDfs(head, gcur, depth + 1, visi, trace, sb, reachable,mas,countArr);
				//找长度为3,4,5的环
				}else if(gcur == head && depth >= 3 && depth < 6) {
					int length = trace.size()-1;
					for(int key = 0;key < length;key++) {
						sb[depth].append(nodeRF[trace.get(key)]);
					}
					sb[depth].append(nodeLF[trace.get(length)]);
					countArr[0] = countArr[0]+1;
				}
			}
			
			if (reachable[current] == 0 && (depth == 5)) {
				List<Integer> path2List = mas.get(current);
				for (int i1 = 0; i1 < path2List.size(); i1++) {
					int lastNode = (int) path2List.get(i1);
					if (!visi[lastNode]) {
						for(int key = 0;key<trace.size();key++) {
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
	
	public void writeNIO(StringBuilder result) {
		FileOutputStream fos = null;
		try {
			fos = new FileOutputStream(new File(output));
			FileChannel channel = fos.getChannel();
			ByteBuffer src = Charset.forName("utf8").encode(result.toString());
			// 字节缓冲的容量和limit会随着数据长度变化，不是固定不变的
			System.out.println("初始化容量和limit：" + src.capacity() + ","+ src.limit());
			int length = 0;
 
			while ((length = channel.write(src)) != 0) {
				/*
				 * 注意，这里不需要clear，将缓冲中的数据写入到通道中后 第二次接着上一次的顺序往下读
				 */
				System.out.println("写入长度:" + length);
			}
 
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (fos != null) {
				try {
					fos.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

}
