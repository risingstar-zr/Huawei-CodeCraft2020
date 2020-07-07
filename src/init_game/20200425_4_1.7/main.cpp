#include <stdio.h>
#include <string.h>

#include <algorithm>
#include <cmath>
#include <ctime>
#include <fstream>
#include <iostream>
#include <thread>
#include <unordered_map>
#include <vector>
using namespace std;
#define tm 3
int temps[tm][10];
int temp_pos[tm];
int Graph[280000][50];
int rGraph[280000][50];
int path[tm];
bool avisit[tm][280000];
int rawGraph[280000][50];
int raw_pos[280000];
int adata[280000][2];
int curent_pos[280000];
int reg_curent_pos[280000];
int nod[280000];
int indegree[280000];
int outdegree[280000];
int getfactul[280000];
int getreindexed[280000];
void dfs2(int graph[][50], int curent_pos[], int from, int start, int path, vector<bool> &visit2, int oder);
void decDfs(int head, int current, int depth, vector<int> &reachable, unordered_map<int, vector<int>> &mas, int oder, vector<bool> &visit2);
void sbb(const string &outputFile);
string idsComma[280000];
string idsLine[280000];
vector<vector<char *>> stringbuffers(tm);
char *stringbuffer;
vector<vector<int>> buffersindex(tm);
void concurentdfs(int from, int end, int order, int cnt);

class Fread {
    char *buf, *p1, *p2;
    FILE *fp;
    size_t cache_size;

    inline char next_char() {
        return p1 == p2 && (p2 = (p1 = buf) + fread(buf, 1, cache_size, fp), p1 == p2) ? char(EOF) : *p1++;
    }

   public:
    Fread(FILE *fp, size_t cache_size) {
        this->buf = new char[cache_size];
        this->fp = fp;
        this->cache_size = cache_size;
        p1 = buf;
        p2 = buf;
    }
    ~Fread() {
        delete buf;
        fclose(fp);
    }
    void read(int &x) {
        char c = next_char();
        x = -100;
        for (; c > '9' || c < '0'; c = next_char())
            if (c == char(EOF))
                return;
        for (; c <= '9' && c >= '0'; c = next_char()){
            if(x==-100){
                x = 0;
            }
            x = (x << 3) + (x << 1) + c - 48;
        }
    }
};

int main() {
    //clock_t start = clock();
    for (int i = 0; i < tm; i++) {
        stringbuffers[i].resize(7);
        buffersindex[i].resize(7, 0);
        stringbuffers[i][2] = new char[30 * 500000];
        stringbuffers[i][3] = new char[40 * 500000];
        stringbuffers[i][4] = new char[50 * 1000000];
        stringbuffers[i][5] = new char[60 * 2000000];
        stringbuffers[i][6] = new char[70 * 3000000];
    }
    stringbuffer = new char[7 * 7 * 2800000];
    int cnt = 0, a = -1, pos = 0;
    FILE *read_file = fopen("/data/test_data.txt", "r");
    Fread fr(read_file, 2800000);
    vector<int> drec(280000);
    int c = 0;
    int p = 0;
    while (a != -100) {
        fr.read(a);
        if ((pos % 3) == 0 && a != -100) {
            adata[c][0] = a;
            if (nod[adata[c][0]] == 0) {
                nod[adata[c][0]] = 1;
                //ddrec.insert(adata[c][0]);
                drec[p++] = adata[c][0];
            }
            //cout << c << ",";
        }
        if ((pos % 3) == 1) {
            adata[c][1] = a;
            if (nod[adata[c][1]] == 0) {
                nod[adata[c][1]] = 1;
                drec[p++] = adata[c][1];
            }
            rawGraph[adata[c][0]][raw_pos[adata[c][0]]++] = adata[c][1];
            c++;
        }
        pos++;
    }
    drec.resize(p);
    sort(drec.begin(), drec.end());
    fclose(read_file);
    char *buf = new char[30];
    char *buf1 = new char[30];
    for (auto &i : drec) {
        getreindexed[i] = cnt;
        getfactul[cnt] = i;
        sprintf(buf, "%d,", i);
        idsComma[cnt] = buf;
        sprintf(buf1, "%d\n", i);
        idsLine[cnt] = buf1;
        cnt++;
    }
    delete[] buf;
    delete[] buf1;
    //int indegree[cnt];
    //fill_n(indegree, cnt, 0);
    //int outdegree[cnt];
    //fill_n(outdegree, cnt, 0);  // 填充0
    c = 0;
    for (int i = 0; i < drec.size(); i++) {
        for (int j = 0; j < raw_pos[drec[i]]; j++) {
            int a = getreindexed[drec[i]];
            if (raw_pos[rawGraph[drec[i]][j]] != 0) {
                int b = getreindexed[rawGraph[drec[i]][j]];
                Graph[a][curent_pos[a]++] = b;
                rGraph[b][reg_curent_pos[b]++] = a;
                outdegree[a]++;
                indegree[b]++;
                c++;
            }
        }
    }
    for (int i = 0; i < cnt; i++) {
        if (curent_pos[i] > 1) {
            sort(Graph[i], Graph[i] + curent_pos[i]);
        }
        if (reg_curent_pos[i] > 1) {
            sort(rGraph[i], rGraph[i] + reg_curent_pos[i]);
        }
    }
    //clock_t end = clock();
    //printf("time : %f s\n", ((double)end - start) / CLOCKS_PER_SEC);
    thread thread_k(concurentdfs, 0, cnt / 15, 0, cnt);
    thread thread_p(concurentdfs, cnt / 15, cnt / 8, 1, cnt);
    thread thread_g(concurentdfs, cnt / 8, cnt, 2, cnt);
    thread_k.join();
    thread_p.join();
    thread_g.join();
    //end = clock();
    string tof = "/projects/student/result.txt";
    c = path[0] + path[1] + path[2];
    //printf("time : %f s\n", ((double)end - start) / CLOCKS_PER_SEC);
    //printf("%d\n", c);
    sbb(tof);
    //end = clock();
    //printf("time : %f s\n", ((double)end - start) / CLOCKS_PER_SEC);
    delete[] stringbuffer;
    for (int i = 2; i < 7; i++) {
        for (int j = 0; j < tm; j++) {
            delete[] stringbuffers[j][i];
        }
    }
    return 0;
}
void sbb(const string &outputFile) {
    FILE *fp = fopen(outputFile.c_str(), "w");
    char buf[2048];
    int idx = sprintf(buf, "%d\n", path[0] + path[1] + path[2]);
    buf[idx] = '\0';
    fputs(buf, fp);
    int ipp = 0;
    for (int i = 2; i < 7; i++) {
        for (int j = 0; j < tm; j++) {
            //stringbuffers[j][i];
            ipp += sprintf(stringbuffer + ipp, "%s", stringbuffers[j][i]);
            //printf("%d %d\n", i,j);
            //stringbuffers[j][i][buffersindex[j][i]] = '\0';
            //fputs(stringbuffers[j][i], fp);
        }
    }
    stringbuffer[ipp] = '\0';
    fwrite(stringbuffer, ipp, sizeof(char), fp);
    fclose(fp);
}
void dfs2(int graph[][50], int curent_pos[], int from, int start, int path, vector<int> &visit2, int oder) {
    for (int i = 0; i < curent_pos[from]; i++) {
        int cur = graph[from][i];
        if (cur < start || avisit[oder][cur] == true) {
            continue;
        }
        visit2[cur] = start;
        if (path == 3) {
            continue;
        }
        avisit[oder][cur] = true;
        dfs2(graph, curent_pos, cur, start, path + 1, visit2, oder);
        avisit[oder][cur] = false;
    }
}
void decDfs(int head, int current, int depth, vector<int> &reachable, unordered_map<int, vector<int>> &mas, int oder, vector<int> &visit2) {
    avisit[oder][current] = true;
    //temp[oder].add(current);
    temps[oder][temp_pos[oder]++] = current;
    //temp[oder].push_back(current);
    if (reachable[current] == head && depth == 6) {  //6+1
        //sort(mas[current].begin(), mas[current].end());
        for (int i = 0; i < mas[current].size(); i++) {
            int lastNode = mas[current][i];
            if (!avisit[oder][lastNode]) {
                for (int j = 0; j < temp_pos[oder]; j++) {
                    buffersindex[oder][6] += sprintf(stringbuffers[oder][6] + buffersindex[oder][6], "%s", idsComma[temps[oder][j]].c_str());
                }
                buffersindex[oder][6] += sprintf(stringbuffers[oder][6] + buffersindex[oder][6], "%s", idsLine[mas[current][i]].c_str());
                path[oder]++;
            }
        }
    }

    if (depth < 6) {
        for (int i = 0; i < curent_pos[current]; i++) {
            int gcur = Graph[current][i];
            if (gcur == head && depth >= 3 && depth < 6) {
                path[oder]++;
                for (int j = 0; j < temp_pos[oder] - 1; j++) {
                    buffersindex[oder][temp_pos[oder] - 1] += sprintf(stringbuffers[oder][temp_pos[oder] - 1] + buffersindex[oder][temp_pos[oder] - 1], "%s", idsComma[temps[oder][j]].c_str());
                }
                buffersindex[oder][temp_pos[oder] - 1] += sprintf(stringbuffers[oder][temp_pos[oder] - 1] + buffersindex[oder][temp_pos[oder] - 1], "%s", idsLine[temps[oder][temp_pos[oder] - 1]].c_str());
            }
            if (visit2[gcur] != head) {
                continue;
            }
            if (gcur > head && depth < 6 && !avisit[oder][gcur]) {
                decDfs(head, gcur, depth + 1, reachable, mas, oder, visit2);
            }
        }

        if (reachable[current] == head && (depth == 5)) {
            //sort(mas[current].begin(), mas[current].end());
            for (int i = 0; i < mas[current].size(); i++) {
                int lastNode = mas[current][i];
                if (!avisit[oder][lastNode]) {
                    for (int j = 0; j < temp_pos[oder]; j++) {
                        buffersindex[oder][5] += sprintf(stringbuffers[oder][5] + buffersindex[oder][5], "%s", idsComma[temps[oder][j]].c_str());
                    }
                    buffersindex[oder][5] += sprintf(stringbuffers[oder][5] + buffersindex[oder][5], "%s", idsLine[mas[current][i]].c_str());
                    path[oder]++;
                }
            }
        }
    }

    avisit[oder][current] = false;
    temps[oder][temp_pos[oder]--] = 0;
    //temp[oder].remove(temp_pos[oder] - 1);
}
void concurentdfs(int from, int end, int order, int cnt) {
    vector<int> visit2(cnt, -3);
    vector<int> visit3(cnt, -3);
    for (int i = from; i < end; i++) {
        if (indegree[i] > 0 && outdegree[i] > 0) {
            //vector<bool> visit2(cnt,false);
            dfs2(Graph, curent_pos, i, i, 0, visit2, order);
            dfs2(rGraph, reg_curent_pos, i, i, 0, visit2, order);
            //vector<bool> visit3(cnt, false);
            unordered_map<int, vector<int>> mas;
            for (int j = 0; j < reg_curent_pos[i]; j++) {
                //visit1[rGraph[i][j]] = true;  //可以从reg[i][j]可以到i。
                if (rGraph[i][j] > i) {
                    for (int k = 0; k < reg_curent_pos[rGraph[i][j]]; k++) {
                        if (rGraph[rGraph[i][j]][k] > i) {
                            mas[rGraph[rGraph[i][j]][k]].push_back(rGraph[i][j]);
                            visit3[rGraph[rGraph[i][j]][k]] = i;
                        }
                    }
                }
            }
            decDfs(i, i, 1, visit3, mas, order, visit2);
            avisit[order][i] = true;
        }
    }
}