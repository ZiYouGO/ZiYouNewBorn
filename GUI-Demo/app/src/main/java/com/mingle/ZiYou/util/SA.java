package com.mingle.ZiYou.util;

import com.mingle.ZiYou.bean.Point;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Random;

public class SA {

	private int cityNum; // 城市数量，编码长度
	private int N;// 每个温度迭代步长
	private int T;// 降温次数
	private double a;// 降温系数
	private double t0;// 初始温度

	private double[][] distance; // 距离矩阵
	private int bestT;// 最佳出现代数

	private int[] Ghh;// 初始路径编码
	private int GhhEvaluation;
	private int[] bestGh;// 最好的路径编码
	private int bestEvaluation;
	private int[] tempGhh;// 存放临时编码
	private int tempEvaluation;
	private ArrayList<Point> pointList = null;
	private Random random;

	public SA() {

	}

	/**
	 * constructor of GA
	 * 
	 * @param cn
	 *            城市数量
	 * @param t
	 *            降温次数
	 * @param n
	 *            每个温度迭代步长
	 * @param tt
	 *            初始温度
	 * @param aa
	 *            降温系数
	 * 
	 **/
	public SA(int cn, int n, int t, double tt, double aa) {
		cityNum = cn;
		N = n;
		T = t;
		t0 = tt;
		a = aa;
	}

	public SA(ArrayList<Point> list) {
		pointList = list;
		cityNum = pointList.size() + 1;
		N = 1;
		T = 10000;
		a = 0.98f;
		t0 = 2147483647;
	}

	// 给编译器一条指令，告诉它对被批注的代码元素内部的某些警告保持静默
	@SuppressWarnings("resource")
	/**
	 * 初始化Tabu算法类
	 * 
	 * @param filename
	 *            数据文件名，该文件存储所有城市节点坐标数据
	 * @throws IOException
	 */
	private void init() throws IOException {
		// 读取数据
		double[] x;
		double[] y;
		distance = new double[cityNum][cityNum];
		x = new double[cityNum];
		y = new double[cityNum];
		for (int i = 0; i < pointList.size(); i++) {
			x[i] = Double.parseDouble(pointList.get(i).getPlat());// lat
			y[i] = Double.parseDouble(pointList.get(i).getPlong());// long
		}
		// 计算距离矩阵
		// ，针对具体问题，距离计算方法也不一样，此处用的是att48作为案例，它有48个城市，距离计算方法为伪欧氏距离，最优值为10628
		for (int i = 0; i < pointList.size(); i++) {
			Point p1 = pointList.get(i);
			distance[i][i] = 0; // 对角线为0
			for (int j = i + 1; j < pointList.size(); j++) {
				Point p2 = pointList.get(j);
				double rij = DistanceCalculator.getDistance(p1.getPlat(), p1.getPlong(), 
						Double.parseDouble(p2.getPlat()), Double.parseDouble(p2.getPlong()));
				distance[i][j] = rij;
				distance[j][i] = distance[i][j];
			}
		}
		distance[cityNum - 1][cityNum - 1] = 0;
		distance[cityNum - 1][0] = 1;
		distance[0][cityNum - 1] = 1;
		distance[cityNum - 1][cityNum - 2] = 2147483647;
		distance[cityNum - 2][cityNum - 1] = 2147483647;

		Ghh = new int[cityNum];
		bestGh = new int[cityNum];
		bestEvaluation = Integer.MAX_VALUE;
		tempGhh = new int[cityNum];
		tempEvaluation = Integer.MAX_VALUE;
		bestT = 0;
		random = new Random(System.currentTimeMillis());

		System.out.println(cityNum + "," + N + "," + T + "," + a + "," + t0);

	}

	// 初始化编码Ghh
	void initGroup() {
		int i, j;
		Ghh[0] = random.nextInt(65535) % cityNum;
		for (i = 1; i < cityNum;)// 编码长度
		{
			Ghh[i] = random.nextInt(65535) % cityNum;
			for (j = 0; j < i; j++) {
				if (Ghh[i] == Ghh[j]) {
					break;
				}
			}
			if (j == i) {
				i++;
			}
		}
	}

	// 复制编码体，复制编码Gha到Ghb
	public void copyGh(int[] Gha, int[] Ghb) {
		for (int i = 0; i < cityNum; i++) {
			Ghb[i] = Gha[i];
		}
	}

	public int evaluate(int[] chr) {
		// 0123
		int len = 0;
		// 编码，起始城市,城市1,城市2...城市n
		for (int i = 1; i < cityNum; i++) {
			len += distance[chr[i - 1]][chr[i]];
		}
		// 城市n,起始城市
		len += distance[chr[cityNum - 1]][chr[0]];
		return len;
	}

	// 邻域交换，得到邻居
	public void Linju(int[] Gh, int[] tempGh) {
		int i, temp;
		int ran1, ran2;

		for (i = 0; i < cityNum; i++) {
			tempGh[i] = Gh[i];
		}
		ran1 = random.nextInt(65535) % cityNum;
		ran2 = random.nextInt(65535) % cityNum;
		while (ran1 == ran2) {
			ran2 = random.nextInt(65535) % cityNum;
		}
		temp = tempGh[ran1];
		tempGh[ran1] = tempGh[ran2];
		tempGh[ran2] = temp;
	}

	public void solve() {
		// 初始化编码Ghh
		initGroup();
		copyGh(Ghh, bestGh);// 复制当前编码Ghh到最好编码bestGh
		bestEvaluation = evaluate(Ghh);
		GhhEvaluation = bestEvaluation;
		int k = 0;// 降温次数
		int n = 0;// 迭代步数
		double t = t0;
		double r = 0.0f;

		while (k < T || (bestGh[0] != 1 && bestGh[cityNum - 1] != cityNum)) {
			n = 0;
			while (n < N) {
				Linju(Ghh, tempGhh);// 得到当前编码Ghh的邻域编码tempGhh
				tempEvaluation = evaluate(tempGhh);
				if (tempEvaluation < bestEvaluation) {
					copyGh(tempGhh, bestGh);
					bestT = k;
					bestEvaluation = tempEvaluation;
				}
				r = random.nextFloat();
				if (tempEvaluation < GhhEvaluation || Math.exp((GhhEvaluation - tempEvaluation) / t) > r) {
					copyGh(tempGhh, Ghh);
					GhhEvaluation = tempEvaluation;
				}
				n++;
			}
			t = a * t;
			k++;
		}

		System.out.println("最佳长度出现代数：");
		System.out.println(bestT);
		System.out.println("最佳长度");
		System.out.println(bestEvaluation);
		System.out.println("最佳路径：");
		for (int i = 0; i < cityNum; i++) {
			System.out.print(bestGh[i] + ",");
			if (i % 10 == 0 && i != 0) {
				System.out.println();
			}
		}
	}

	/**
	 * @param args
	 * @throws IOException
	 */
	public static void main(String[] args) throws IOException {
		System.out.println("Start....");
		String strbuff;
		String filename = "/Users/xyk0058/Desktop/data.txt";
	    BufferedReader data = new BufferedReader(new InputStreamReader(
	        new FileInputStream(filename)));
	    ArrayList<Point> list = new ArrayList<Point>();
	    while ((strbuff = data.readLine()) != null) {
	    	String[] strcol = strbuff.split(" ");
	    	Point p = new Point();
	    	double x = Double.parseDouble(strcol[1]);// x坐标
	        double y = Double.parseDouble(strcol[2]);// y坐标
	        p.setPlat(x+"");
	        p.setPlong(y+"");
	        list.add(p);
	    }
		SA sa = new SA(list);
		sa.init();
		sa.solve();
	}
}