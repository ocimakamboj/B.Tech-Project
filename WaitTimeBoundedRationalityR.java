import java.util.Random;

public class WaitTimeBoundedRationalityR{
	double time;
	int[] nSystem; //System State; that is number of customers presently in the system, (nT,n1,n2, ... , nk) corresponds to -
				   // nT total number of people in the system, n1 is the number of customers belonging to k-class in the system
	int[] nInService; //(n1,n2, ... , nk) - corresponds to how many people of each class are currently in service;
					  //(0 doesn't mean anything)
	int[] inService;//1-dimensional array that stores the state of the system w.r.t the servers
					 //(inService[i] corresponds to = B where B is the class of the customer currently in service with server-i
	double[] nextArrivalTime;	//gives the time of next arrival, nextArrivalTime[i] gives the arrival time of the k-class customer
								//(0 doesn't mean anything)
	double[] nextServiceComp; //gives the service completion time; nextServiceComp[i] gives the service completion time of the customer
							  //being served by server i (0 doesn't mean anything)
	double endTime; //time after which no entry is allowed
	ArrivalProcess[] randAP; //Arrival process variable to calculate the next arrival time; randAP[i] gives the next arrival of i-class 
							 //customer, 0 doesn't mean anything
	ServiceTime[] randST; //Service time variable to generate service times; randST[i] gives the service time of i-class customer
	int noOfServers;
	int noOfClasses;
	double[] R; //array specifying rewards of each class
	double[] p; //array specifying prices of each class
	double[] C; //array specifying waiting costs for each class
	double[] serviceRates; //collection of service rates of all the classes
	double[] arrivalRates; //collection of arrival rates of all the classes
	double beta;

	public WaitTimeBoundedRationalityR(double t, int s, int k, double[] ArrivalParameter, double[] ServiceParameter, double[] Reward, double[] price, double[] Cost, int[] NInSystem, int[] NInService, int[] InService,int index, double b){
		int capacity = 20000;
		time = 0;
		nSystem = new int[k+1];
		for(int i=0; i <= k; i++){
			if(i<=index){
				nSystem[i] = NInSystem[i];
			}else{
				nSystem[i] = NInService[i];
				nSystem[0] = nSystem[0] - (NInSystem[i] - NInService[i]);
			}
		}
		nInService = new int[k+1];
		for(int i=0; i <= k; i++){
			nInService[i] = NInService[i];
		}
		inService = new int[s+1];
		for(int i=0; i <= s; i++){
			inService[i] = InService[i];
		}
		randAP = new ArrivalProcess[k+1];
		arrivalRates = new double[k+1]; 
		for(int i=0; i<=k; i++){
			if(i < index){
				randAP[i] = new ArrivalProcess(ArrivalParameter[i], time);
				arrivalRates[i] = ArrivalParameter[i];
			}else{
				randAP[i] = new ArrivalProcess(0, time);
				arrivalRates[i] = 0;
			}
		}
		randST = new ServiceTime[k+1];
		serviceRates = new double[k+1];
		for(int i=0; i<=k; i++){
				randST[i] = new ServiceTime(ServiceParameter[i]);
				serviceRates[i] = ServiceParameter[i]; 
		}
		nextArrivalTime = new double[k+1];
		for(int i=1; i<=k; i++){
			nextArrivalTime[i] = randAP[i].nextArrival();
		}
		nextServiceComp = new double[s+1];
		for(int i=1 ; i<=s ; i++){
			nextServiceComp[i] = randST[inService[i]].nextService();
		}
		endTime = t;
		noOfServers = s;
		noOfClasses = k;
		R = new double[k+1];
		for(int i=0; i<=k; i++){
			R[i] = Reward[i];
		}
		p = new double[k+1];
		for(int i=0; i<=k; i++){
			p[i] = price[i];
		}
		C = new double[k+1];
		for(int i=0; i<=k; i++){
			C[i] = Cost[i];
		}
		beta = b;
	}

	//this function takes as input all the next arrival time and all the service completion times, and finds out the minimum of them
	//it returns (0,i) if the next arrival time of class-i is the minimum, 
	//and returns (1,i) if the service completion time at server-i is minimum
	public int[] min(double[] q1, double[] q2){
		int[] answer = new int[2];
		double minArrival = q1[1];
		int minArrivalIndex = 1;
		for (int i=1; i<=noOfClasses; i++){
			if (q1[i] < minArrival){
				minArrival = q1[i];
				minArrivalIndex = i;
			}
		}
		double minDeparture = q2[1];
		int minDepartureIndex = 1;
		for(int i = 1; i<noOfServers+1 ; i++){
			if (q2[i] < minDeparture){
				minDeparture = q2[i];
				minDepartureIndex = i;
			}
		}
		if(minArrival <= minDeparture && minArrival <= endTime){
			answer[0] = 0;
			answer[1] = minArrivalIndex;
		}else{
			answer[0] = 1;
			answer[1] = minDepartureIndex;
		}
		return(answer);
	}

	//this methods gives the customer in waiting with the highest priority class
	//it takes as input the system state, i.e. present number of customers in each line; the number of customers of every class being served
	//returns 0 if there are no customers waiting
	public int priorityCustomer(int[] q1, int[] q2){
		int answer = 0;
		if(q1[0]<noOfServers){
			answer = 0;
		}else{
			for (int i=1; i<=noOfClasses; i++){
				if ((q1[i] - q2[i])> 0){
					answer = i;
					break;
				} 
			}
		}
		return(answer);
	}

	public double calculateWaitTime(){
		double serviceTime;
		double utility;
		double waitTime;
		Random varR = new Random();
		while(true){
			if(nSystem[0] < noOfServers){
				return(time);
			}
			else{
				int[] nextEvent = min(nextArrivalTime,nextServiceComp); //this tells whether departure or arrival is minimum
																		//and tells the index of the server of class respectively
				int index = nextEvent[1];
				if(nextEvent[0] == 0 && nextArrivalTime[index] <= endTime){
					/*
					if(index == 1){
						waitTime = (nSystem[1])/(noOfServers*serviceRates[1]) + 1/serviceRates[1];
					}else{*/
						WaitTimeBoundedRationalityR var = new WaitTimeBoundedRationalityR(endTime - nextArrivalTime[index], noOfServers, noOfClasses, arrivalRates, serviceRates, R, p, C, nSystem, nInService, inService,index, beta);
						waitTime = var.calculateWaitTime();
						waitTime = waitTime + 1/serviceRates[index];
					//}
					utility = R[index] - p[index] - C[index]*waitTime;
					double prob = Math.exp(utility/beta)/(1 + Math.exp(utility/beta));
					if(varR.nextDouble() > prob){
						time = nextArrivalTime[index];
						nextArrivalTime[index] = randAP[index].nextArrival();
					}else{
						time = nextArrivalTime[index];
						nextArrivalTime[index] = randAP[index].nextArrival();

						//resetting the system state, number in system increases by 1, and the customer goes to the first server that is idle
						//if no server is idle, customer joins the queue
						for(int i = 1; i <= noOfServers; i++){
							if(inService[i] == 0){
								nSystem[0] = nSystem[0] + 1;
								nSystem[index] = nSystem[index] + 1;
								nInService[index] = nInService[index] + 1;
								inService[i] = index;
							
								serviceTime = randST[index].nextService();
								nextServiceComp[i] = time + serviceTime;
								break;
							}
							if(i == noOfServers && inService[i] != 0){
								nSystem[0] = nSystem[0] + 1;
								nSystem[index] = nSystem[index] + 1;
							}
						}
					}
				}

				else if(nextEvent[0] == 1 && nextServiceComp[index] <= endTime){
					int customerClass = inService[index];
					time = nextServiceComp[index];

					nSystem[0] = nSystem[0] - 1;
					nSystem[customerClass] = nSystem[customerClass] - 1;
					nInService[customerClass] = nInService[customerClass] - 1;
					if(nSystem[0] < noOfServers){
						inService[index] = 0;
						nextServiceComp[index] = Double.POSITIVE_INFINITY;
					}else{
						int newCustomerClass = priorityCustomer(nSystem,nInService);
						nInService[newCustomerClass] = nInService[newCustomerClass] + 1;
						inService[index] = newCustomerClass;
						serviceTime = randST[newCustomerClass].nextService();
						nextServiceComp[index] = time + serviceTime;
					}
				}

				else{
					int customerClass = inService[index];
					time = nextServiceComp[index];

					nSystem[0] = nSystem[0] - 1;
					nSystem[customerClass] = nSystem[customerClass] - 1;
					nInService[customerClass] = nInService[customerClass] - 1;
					if(nSystem[0] < noOfServers){						
						inService[index] = 0;
						nextServiceComp[index] = Double.POSITIVE_INFINITY;
					}else{
						int newCustomerClass = priorityCustomer(nSystem,nInService);
						nInService[newCustomerClass] = nInService[newCustomerClass] + 1;
						inService[index] = newCustomerClass;
						serviceTime = randST[newCustomerClass].nextService();
						nextServiceComp[index] = time + serviceTime;
					}
				}
				/*for(int j=1; j<=noOfClasses; j++){
					System.out.print(nArrivals[j] + " Arrivals, " + nDepartures[j] + " Departures, " + nDepartIndex[j] + " DepartureIndex, ");
					System.out.print(nSystem[j] + " in System, " + nInService[j] + " in Service, ");
					System.out.print(nBalking[j] + " balking, ");
					System.out.println();
					for (int l=1; l<=nBalking[j]; l++){
						System.out.print(nBalk[j][l] + ", ");
					}
					System.out.println();
				}
				for(int k=1; k<=noOfServers; k++){
					System.out.println(inService[1][k] + " class, " + inService[0][k] + " number");
				}
				System.out.println();*/
			}
		}
	}
}