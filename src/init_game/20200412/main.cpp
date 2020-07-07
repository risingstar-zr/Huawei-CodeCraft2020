#include<unordered_map>
#include<stdio.h>
#include<stdlib.h>
#include<time.h>
#include<vector>
#include<iterator>
#include<algorithm>
#include<pthread.h>

int threadArray[4] = { 0, 1, 2, 3 };
using namespace std;
vector<vector<vector<int>>> resArray(4);
static vector<vector<int>> result;//save result
static vector<vector<int>> outGraph;//出度图
static vector<vector<int>> inGraph;//出度图
static vector<int> node;//保存节点
static vector<int> allNode;//初始化保存uv
//static vector<vector<int>> visit;//保存是否访问
vector<int> visit;
static vector<int> inValue;//出度数结构
static vector<int> outValue;//入度数结构
static unordered_map<int, int> idMap;//映射id
static int countId;

static vector<vector<int>> keyNum(4);
/*
	save file
*/
static void savePredictResult(string predictFileName) {
	FILE* file = fopen(predictFileName.c_str(), "w");
	char dataBuf[1024];
	int id_buf = sprintf(dataBuf, "%d\n", result.size());
	dataBuf[id_buf] = '\0';
	fputs(dataBuf, file);
	for (int i = 0; i < result.size(); i++) {
		id_buf = 0;
		for (int j = 0; j < result[i].size(); j++) {
			if (j != result[i].size() - 1) {
				id_buf += sprintf(dataBuf + id_buf, "%d,", result[i][j]);
			}
			else {
				id_buf += sprintf(dataBuf + id_buf, "%d\n", result[i][j]);
			}
		}
		dataBuf[id_buf] = '\0';
		fputs(dataBuf, file);
		// out.write(result.get(i) + "\n");
	}
	fclose(file);
}

/*
	read file
*/
static void readFile(string predictFileName)
{
	FILE* file_buf;
	if ((file_buf = fopen(predictFileName.c_str(), "r")) == NULL)
	{
		printf("Open file error!\n");
		return;
	}
	int id1, id2, value;
	while (fscanf(file_buf, "%d,%d,%d", &id1, &id2, &value) != EOF){
		allNode.push_back(id1);
		allNode.push_back(id2);
	}
	fclose(file_buf);
}

/*
	result sort
*/
bool compare(vector<int> var1, vector<int> var2){
	if (var1.size() < var2.size()){
		return true;
	}
	if (var1.size() == var2.size()){
		for (int i = 0; i < var1.size(); i++){
			if (var1[i] != var2[i]){
				return var1[i] < var2[i];
			}
		}
		return true;
	}
	else{
		return false;
	}
}

/*
	data init
*/
static void initData()
{
	vector<int> getAllNode = allNode;
	sort(getAllNode.begin(), getAllNode.end());
	getAllNode.erase(unique(getAllNode.begin(), getAllNode.end()), getAllNode.end());
	node = getAllNode;//get all node and keep node sort
	countId = 0;
	for (int i:getAllNode)
	{
		idMap[i] = countId;
		keyNum[countId % 4].push_back(countId);
		countId++;
	}
	outGraph = vector<vector<int>>(countId);
	inGraph = vector<vector<int>>(countId);
	inValue = vector<int>(countId, 0);
	outValue = vector<int>(countId, 0);
	int allSize = allNode.size();
	for (int i = 0; i < allSize; i=i+2)
	{
		int id1Map = idMap[allNode[i]];
		int id2Map = idMap[allNode[i + 1]];
		outGraph[id1Map].push_back(id2Map);
		inGraph[id2Map].push_back(id1Map);
		inValue[id2Map]++;
		outValue[id1Map]++;
	}
}

/*
	find cycle[3,7]
*/
static void findCycle_D(vector<int> way,int headId){
	int idxMap = idMap[way.back()];
	visit[idxMap] = 1;
	for (int x:outGraph[idxMap])
	{
		if (node[x] == way[0] && way.size() >= 3 && way.size() < 6)
		{
			result.push_back(way);
		}
		/*
		way<6
		this node isn`t in way
		x>way[0]
		*/
		if (way.size() < 5 && !visit[x] && node[x] > way[0])
		{
			vector<int> temp = way;
			temp.push_back(node[x]);
			findCycle_D(temp, headId);
		}
		/*
			find level 6&7
		*/
		if (way.size() == 5 && !visit[x] && node[x] > way[0])
		{
			/*-----------find level 6----------------*/
			//x is in head inList?
			vector<int> headInList = inGraph[headId];
			sort(headInList.begin(), headInList.end());
			auto loc = lower_bound(headInList.begin(), headInList.end(), x);
			if (loc != headInList.end() && *loc == x &&!visit[*loc])
			{
				vector<int> temp = way;
				temp.push_back(node[x]);
				result.push_back(temp);
			}
			/*-----------find level 7---------------*/
			//x outList intersection with head inList
			vector<int> out = outGraph[x];
			sort(out.begin(), out.end());
			auto loc2 = upper_bound(out.begin(), out.end(), headId);
			loc = upper_bound(headInList.begin(), headInList.end(), headId);
			vector<int> inter;
			set_intersection(loc2, out.end(), loc, headInList.end(), back_inserter(inter));
			for (int i = 0; i < inter.size(); i++)
			{
				if (!visit[inter[i]])
				{
					vector<int> temp = way;
					temp.push_back(node[x]);
					temp.push_back(node[inter[i]]);
					result.push_back(temp);
				}
			}
			
		}
	}
	visit[idxMap] = 0;
}

//void* pthreadDue(void* numIdx)
//{
//	int num = *(int*)numIdx;
//	for (int i = 0; i < keyNum[num].size(); i++)
//	{
//		//visit[num] = vector<int>(countId, 0);
//		int key = keyNum[num][i];
//		vector<int> way;
//		way.push_back(keyNum[num][i]);
//		findCycle_D(way, keyNum[num][i], num);
//	}
//	return nullptr;
//}

int main()
{
	//Online Set
	string fileName = "/data/test_data.txt";
	string output = "/projects/student/result.txt";
	//Linux Set
	//string fileName = "test_data2.txt";
	//string output = "projects/result.txt";
	//Windows Set
	//string fileName = "D://huaweiOnline//test_data.txt";
	//string output = "D://huaweiOnline//projects//result.txt";
	readFile(fileName);
	initData();
	//vector<int> temp = vector<int>(countId, 0);
	//visit = vector<vector<int>>(4, temp);
	/*pthread_t pid[4];
	for (int i = 0; i < 4; i++)
	{
		int * pthreadNum = threadArray + i;
		if (pthread_create(&pid[i], NULL, pthreadDue, pthreadNum))
			perror("Pthread Create Fails");
	}
	for (int i = 0; i < 4; i++)
	{
		if (pthread_join(pid[i], NULL))
			perror("Pthread Join Fails");
	}*/
	for (int i = 0; i < countId; i++)
	{
		visit = vector<int>(countId, 0);
		vector<int> way;
		way.push_back(node[i]);
		findCycle_D(way, i);
	}
	/*for (int i = 0; i < 4; i++)
	{
		result.insert(result.end(), resArray[i].begin(), resArray[i].end());
	}*/
	sort(result.begin(), result.end(), compare);
	savePredictResult(output);
	return 0;
}