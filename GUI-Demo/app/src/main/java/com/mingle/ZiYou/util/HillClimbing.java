package com.mingle.ZiYou.util;

import android.provider.Settings;

import com.baidu.location.Poi;
import com.mingle.ZiYou.bean.Point;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class HillClimbing {

	private int MAX_GEN;// 迭代次数
	private int cityNum; // 城市数量,编码长度
	private double[][] distance; // 距离矩阵
	private int bestT;// 最佳出现代数
	private int[] bestGh;// 最好的路径编码
	private int bestEvaluation;
	private List<Point> pointList = null;
	private Random random;

	public HillClimbing() {

	}

	/**
	 * constructor of GA
	 * 
	 * @param n
	 *            城市数量
	 * @param g
	 *            运行代数
	 * 
	 **/
	public HillClimbing(List<Point> list, int g) {
		pointList = list;
		cityNum = pointList.size();
		MAX_GEN = g;
	}

	// 给编译器一条指令，告诉它对被批注的代码元素内部的某些警告保持静默
	@SuppressWarnings("resource")
	/**
	 * 初始化HillClimbing算法类
	 * @param filename 数据文件名，该文件存储所有城市节点坐标数据
	 * @throws IOException
	 */
	private void init(){
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

		bestGh = new int[cityNum];
		bestEvaluation = Integer.MAX_VALUE;
		bestT = 0;

		random = new Random(System.currentTimeMillis());
	}

	// 初始化编码Ghh
	void initGroup() {
		int i, j;
		bestGh[0] = random.nextInt(65535) % cityNum;
		for (i = 1; i < cityNum;)// 编码长度
		{
			bestGh[i] = random.nextInt(65535) % cityNum;
			for (j = 0; j < i; j++) {
				if (bestGh[i] == bestGh[j]) {
					break;
				}
			}
			if (j == i) {
				i++;
			}
		}
	}

	public int evaluate(int[] chr) {
		int len = 0;
		// 染色体，起始城市,城市1,城市2...城市n
		for (int i = 1; i < cityNum; i++) {
			len += distance[chr[i - 1]][chr[i]];
		}
		// 城市n,起始城市
		len += distance[chr[cityNum - 1]][chr[0]];
		return len;
	}

	// 爬山算法
	public void pashan(int[] Gh, int T) {
		int i, temp, tt = 0;
		int ran1, ran2;
		int e;// 评价新值
		int[] tempGh = new int[cityNum];
		bestEvaluation = evaluate(Gh);

		// 爬山代数T
		for (tt = 0; tt < T; tt++) {
			for (i = 0; i < cityNum; i++) {
				tempGh[i] = Gh[i];
			}
			ran1 = random.nextInt(65535) % cityNum;
			ran2 = random.nextInt(65535) % cityNum;
			while (ran1 == ran2) {
				ran2 = random.nextInt(65535) % cityNum;
			}

			// 两交换法实施邻域操作
			temp = tempGh[ran1];
			tempGh[ran1] = tempGh[ran2];
			tempGh[ran2] = temp;

			e = evaluate(tempGh);// 评价新值

			if (e < bestEvaluation) {
				bestT = tt;
				bestEvaluation = e;
				for (i = 0; i < cityNum; i++) {
					Gh[i] = tempGh[i];
				}
			}
		}

	}

	public List<Point> solve() {
		init();
		initGroup();// 初始化编码
		pashan(bestGh, MAX_GEN);

//		System.out.println("最佳长度出现代数：");
//		System.out.println(bestT);
//		System.out.println("最佳长度");
//		System.out.println(bestEvaluation);
//		System.out.println("最佳路径：");
//		for (int i = 0; i < cityNum; i++) {
//			System.out.print(bestGh[i] + ",");
//			if (i % 10 == 0 && i != 0) {
//				System.out.println();
//			}
//		}
		List<Point> result = new ArrayList<Point>();
		for (int i = 0; i < cityNum; i++) {
			result.add(i,pointList.get(bestGh[i]));
		}
		return result;
	}

	/**
	 * @param args
	 * @throws IOException
	 */
//	public static void getBestRoutine(List<Point> list){
////		System.out.println("Start....");
////		String strbuff;
////		String filename = "/Users/xyk0058/Desktop/data.txt";
////	    BufferedReader data = new BufferedReader(new InputStreamReader(
////	        new FileInputStream(filename)));
////	    ArrayList<Point> list = new ArrayList<Point>();
////	    while ((strbuff = data.readLine()) != null) {
////	    	String[] strcol = strbuff.split(" ");
////	    	Point p = new Point();
////	    	double x = Double.parseDouble(strcol[1]);// x坐标
////	        double y = Double.parseDouble(strcol[2]);// y坐标
////	        p.setPlat(x+"");
////	        p.setPlong(y+"");
////	        list.add(p);
////	    }
//		try{
//			HillClimbing hillClimbing = new HillClimbing(list, 5000);
//			hillClimbing.init();
//			hillClimbing.solve();
//		}catch (Exception e)
//		{
//			System.out.print("wrong wrong wrong");
//		}
//
//	}
}
