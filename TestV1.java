import java.io.*;
import java.util.*;

public class TestV1{
	public static void main(String[] args){

		long startTime = System.currentTimeMillis();
 
		int endTime = 1000;
		int s = 1;
		int noOfClasses = 3;

		double[] arrivalRate = new double[noOfClasses+1];
		arrivalRate[1] = 2.6;
		arrivalRate[2] = 2.8;
		arrivalRate[3] = 7;

		double[] serviceRate = new double[noOfClasses+1];
		serviceRate[1] = 3;
		serviceRate[2] = 3;
		serviceRate[3] = 5;

		double[] Reward = new double[noOfClasses+1];
		Reward[1] = 16;
		Reward[2] = 12;
		Reward[3] = 12;

		double[] price = new double[noOfClasses+1];
		price[1] = 0;
		price[2] = 0;
		price[3] = 3;

		double[] Cost = new double[noOfClasses+1];
		Cost[1] = 7;
		Cost[2] = 5;
		Cost[3] = 4;

		double Wage = 3;

		int noOfBeta = 35;
		double[] beta = new double[noOfBeta+1];
		beta[1] = 0.05;
		beta[2] = 0.1;
		beta[3] = 0.15;
		beta[4] = 0.2;
		beta[5] = 0.25;
		beta[6] = 0.3;
		beta[7] = 0.35;
		beta[8] = 0.4;
		beta[9] = 0.45;
		beta[10] = 0.5;
		beta[11] = 0.55;
		beta[12] = 0.6;
		beta[13] = 0.65;
		beta[14] = 0.7;
		beta[15] = 0.75;
		beta[16] = 0.8;
		beta[17] = 0.85;
		beta[18] = 0.9;
		beta[19] = 0.95;
		beta[20] = 1;
		beta[21] = 2;
		beta[22] = 3;
		beta[23] = 4;
		beta[24] = 5;
		beta[25] = 6;
		beta[26] = 7;
		beta[27] = 8;
		beta[28] = 9;
		beta[29] = 10;
		beta[30] = 12;
		beta[31] = 14;
		beta[32] = 16;
		beta[33] = 18;
		beta[34] = 20;
		beta[35] = 30;

		try{
			PrintStream out = new PrintStream(new FileOutputStream("output.txt"));
	        System.setOut(out);
		}catch(Exception e){}

		int j = 30;

		MM1 queueMM1 = new MM1(endTime, arrivalRate[1], serviceRate[1]);
		queueMM1.EventRun();
		queueMM1.PrintStats();
		
		MMs queueMMs = new MMs(endTime, s, arrivalRate[1], serviceRate[1]);
		queueMMs.EventRun();
		queueMMs.PrintStats();
		
		MM1fullRationality queueMM1fR = new MM1fullRationality(endTime, arrivalRate[1], serviceRate[1], Reward[1], price[1], Cost[1]);
		queueMM1fR.EventRun();
		queueMM1fR.PrintStats();

		MM1BoundedRationality queueMM1B = new MM1BoundedRationality(endTime, arrivalRate[1], serviceRate[1], Reward[1], price[1], Cost[1], beta[j]);
		queueMM1B.EventRun();
		queueMM1B.PrintStats();

		PriorityQueue queue = new PriorityQueue(endTime, s, noOfClasses, arrivalRate, serviceRate);
		queue.EventRun();
		queue.PrintStats();
		
		PriorityQueueFullRationality1 queue1 = new PriorityQueueFullRationality1(endTime, s, noOfClasses, arrivalRate, serviceRate, Reward, price, Cost, Wage);
		queue1.EventRun();
		queue1.PrintStats();

		PriorityQueueFullRationality2 queue2 = new PriorityQueueFullRationality2(endTime, s, noOfClasses, arrivalRate, serviceRate, Reward, price,Cost);
		queue2.EventRun();
		queue2.PrintStats();
		
		PriorityQueueFullRationality3 queue3 = new PriorityQueueFullRationality3(endTime, s, noOfClasses, arrivalRate, serviceRate, Reward, price,Cost);
		queue3.EventRun();
		queue3.PrintStats();

		PriorityQueueFullRationality3R queue3R = new PriorityQueueFullRationality3R(endTime, s, noOfClasses, arrivalRate, serviceRate, Reward, price,Cost);
		queue3R.EventRun();
		queue3R.PrintStats();
		
		PriorityQueueBoundedRationality1 queueb1 = new PriorityQueueBoundedRationality1(endTime, s, noOfClasses, arrivalRate, serviceRate, Reward, price, Cost, Wage, beta[j]);
		queueb1.EventRun();
		queueb1.PrintStats();
		
		PriorityQueueBoundedRationality2 queueb2 = new PriorityQueueBoundedRationality2(endTime, s, noOfClasses, arrivalRate, serviceRate, Reward, price, Cost, beta[j]);
		queueb2.EventRun();
		queueb2.PrintStats();

		PriorityQueueBoundedRationality3 queueb3 = new PriorityQueueBoundedRationality3(endTime, s, noOfClasses, arrivalRate, serviceRate, Reward, price, Cost, beta[j]);
		queueb3.EventRun();
		queueb3.PrintStats();

		PriorityQueueBoundedRationality3R queueb3R = new PriorityQueueBoundedRationality3R(endTime, s, noOfClasses, arrivalRate, serviceRate, Reward, price, Cost, beta[j]);
		queueb3R.EventRun();
		queueb3R.PrintStats();
		
		
		long stopTime = System.currentTimeMillis();
	    long elapsedTime = stopTime - startTime;
	    System.out.println("elapsed time is: " + elapsedTime);
	}
}