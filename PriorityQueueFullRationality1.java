/*
An implementation of a Priority Queue with 'k' number of classes and 's' servers
Each server can serve a customer of any class
Class-1 has higher priority than class-2, Class-2 has higher priority than class-3
meaning, a customer of class-1 will be served before the customer of class-2
Within classes, FCFS (first come first served) is followed
In this system, the customers have the option to balk after observing the queue length.
let
R - Reward on completion of Service
p - price paid to join the queue
C - waiting cost per unit time
W - wait time of a customer that he estimated based on the number of customers currently in the system

Upon arrival, the customer will have expected Utility, U = 0, if he balks
and expected Utility, U = R-p-C*W if he joins

Assuming full rationality, the customer joins only if U > 0

Inputs Required : t - time for which entry to the queue is allowed
				  s - no of servers
				  k - no of classes
				  ArrivalParameter - array containing the arrival rates of each class
				  ServiceParameter - array containing the service rates of each class
				  Reward - array containing the values of Reward for each class
				  price - array containing the values of price for each class
				  Cost - array containing the waiting cost per unit time for each class
				  W - wages per unit time of the servers

Methods : EventRun(): to start the simulation
		  AvgWaitTime() : an array containing the average waiting time of the customers of each class
		  AvgQueueLength() : an array containing the average Queue Length of the customers of each class
		  AvgNoCustomers() : the overall average number of customers in the system
		  SocialWelfare() : the social welfare value 
*/

public class PriorityQueueFullRationality1{
	double globalT; //current time
	int[] nArrivals; //total number of arrivals; nArrivals[i] gives the total number of arrivals of priority-k class
					 //(0 doesn't mean anything)
	int[] nDepartures; //total number of departures; nDepartures[i] gives the total number of Departures of priority-k class
					   //(0 doesn't mean anything)
	int[] nDepartIndex; //takes care of the largest index of the customer who departed last from a particular class
	int[] nServed; //total number of customers served by server i; nServed[i] gives the number of customers served by server-i
				   //(0 doesn't mean anything)
	int[] nSystem; //System State; that is number of customers presently in the system, (nT,n1,n2, ... , nk) corresponds to -
				   // nT total number of people in the system, nk is the number of customers belonging to k-class in the system
	int[] nInService; //(n1,n2, ... , nk) - corresponds to how many people of each class are currently in service;
					  //(0 doesn't mean anything)
	int[][] inService;//2-dimensional array that stores the state of the system w.r.t the servers
					 //(inService[0][i],inService[1][i]) corresponds to = (A,B), where B is the class of the customer currently
					 //in service with server-i, and A is its overall number in the line of his class; (A,B) = 0 implies idle server
	double[][] ArrivalTimes; //Array that stores the arrival times of i'th customer belonging to k class at (k,i) 
							 //(0 row and column doesn't mean anything)
	double[][] DepartureTimes; //Array that stores the departure times of i'th customer belonging to k class at (k,i)
							   //(0 row and column doesn't mean anything)
	double[] nextArrivalTime;	//gives the time of next arrival, nextArrivalTime[i] gives the arrival time of the k-class customer
								//(0 doesn't mean anything)
	double[] nextServiceComp; //gives the service completion time; nextServiceComp[i] gives the service completion time of the customer
							  //being served by server i (0 doesn't mean anything)
	double endTime; //time after which no entry is allowed
	double Tp; //time past endTime when the last customer departs
	ArrivalProcess[] randAP; //Arrival process variable to calculate the next arrival time; randAP[i] gives the next arrival of i-class 
							 //customer, 0 doesn't mean anything
	ServiceTime[] randST; //Service time variable to generate service times; randST[i] gives the service time of i-class customer
	int[] nTotal; //total number of arrivals and departures combined; nTotal[i] gives the combined arrivals and departures of k-class
	double[][][] SystemState; //3-dimensional array that stores the system state at each point of time; 
							//(SystemState[k][0][i],SystemState[k][1][i]) corresponds to the ordered pair (k,nSystemk,globalT)
							//this records the ordered pair whenever an event occurs, this (2,1,4) means queue length
							//became 1 at time t=4 for class-k; SystemState[0][i][j] doesn't mean anything
	int nAllClassesTotal; //total number of arrivals and departures combined for all the classes
	double[][] totalSystemState; //System State for all classes combined
	int noOfServers;
	int noOfClasses;
	int[] nBalking; //no of customers of each class who balk (0 doesn't mean anything)
	int[][] nBalk; //double array that stores which customers of a class balk
	double[] R; //array specifying rewards of each class
	double[] p; //array specifying prices of each class
	double[] C; //array specifying waiting costs for each class
	double Wage;
	double[] serviceRates; //collection of service rates of all the classes
	double[] arrivalRates; //collection of arrival rates of all the classes

	public PriorityQueueFullRationality1(double t, int s, int k, double[] ArrivalParameter, double[] ServiceParameter, double[] Reward, double[] price, double[] Cost, double W){
		int capacity = 20000;
		globalT = 0;
		nArrivals = new int[k+1];
		nDepartures = new int[k+1];
		nDepartIndex = new int[k+1];
		nServed = new int[s+1];
		nSystem = new int[k+1];
		nInService = new int[k+1];
		inService = new int[2][s+1];
		ArrivalTimes = new double[k+1][capacity];
		DepartureTimes = new double[k+1][capacity];
		randAP = new ArrivalProcess[k+1];
		arrivalRates = new double[k+1]; 
		for(int i=0; i<=k; i++){
			randAP[i] = new ArrivalProcess(ArrivalParameter[i], globalT); //forming the random variables
			arrivalRates[i] = ArrivalParameter[i];
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
		//the arrival times corresponding the different classes have been generated
		nextServiceComp = new double[s+1];
		for(int i=1 ; i<=s ; i++){
			nextServiceComp[i] = Double.POSITIVE_INFINITY;
		}
		endTime = t;
		nTotal = new int[k+1];
		SystemState = new double[k+1][2][2*capacity];
		nAllClassesTotal = 0;
		totalSystemState = new double[2][4*capacity];
		noOfServers = s;
		noOfClasses = k;
		nBalking = new int[k+1];
		nBalk = new int[k+1][capacity];
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
		Wage = W;
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
	//(i,j) means customer belongs to class i and his overall number in his class is j
	//it takes as input the system state, i.e. present number of customers in each line; the number of customers of every class being served
	//and the index of departures from each class, and the system state w.r.t the servers; returns (0,0) if there are no customers waiting
	//q1 nSystem, q2 nInService, q3 nDepartIndex, q4 inService
	//answer[1] essentially tells me which customer should be the next in line to get served, not the actual customer next in line to get served
	public int[] priorityCustomer(int[] q1, int[] q2, int[] q3, int[][] q4){
		int[] answer = new int[2];
		if(q1[0]<noOfServers){
			answer[0] = 0;
			answer[1] = 0;
		}else{
			for (int i=1; i<=noOfClasses; i++){
				if ((q1[i] - q2[i])> 0){
					answer[0] = i;
					break;
				} 
			}
			int value = 0;

			if(q2[answer[0]] == 0){
				value = q3[answer[0]];
			}else{
				for (int i = 1; i<=noOfServers; i++){
					if(q4[1][i] == answer[0]){
						if(q4[0][i] > value){
							value = q4[0][i];
						}
					}
				}
				if (q3[answer[0]] > value){
					value = q3[answer[0]];
				}
			}
			answer[1] = value + 1;
		}
		return(answer);
	}

	public void EventRun(){
		double serviceTime;
		double utility;
		double waitTime;
		int[] indexB = new int[noOfClasses+1];
		for(int i=1; i<=noOfClasses; i++){
			indexB[i] = 1; //a pointer; indexB[i] used to determine the number of the customer of class-i next in line to get served; it tells which numbers to skip
						   //while determining the identity of the customer getting served. priority Customer tells which customer should be getting served; indexB
						   //helps to determine whether that customer has balked or not; if the priorityCustomer gives the identity of the customer that should be getting
						   //served as x, then indexB points to the indentity that we should check against to determine whether x balked or not
		}
		while(true){
			int[] nextEvent = min(nextArrivalTime,nextServiceComp); //this tells whether departure or arrival is minimum
																	//and tells the index of the server of class respectively
			int index = nextEvent[1];
			if(nextEvent[0] == 0 && nextArrivalTime[index] <= endTime){
				//basing the utility on the number of customers currently in the system
				waitTime = 0;
				for(int i=1; i<=index; i++){
					waitTime = waitTime + nSystem[i]/(noOfServers*serviceRates[i]); //not completely correct
				}
				waitTime = waitTime + 1/serviceRates[index];
				//System.out.println(waitTime);
				utility = R[index] - p[index] - C[index]*waitTime;
				if(utility < 0){
					globalT = nextArrivalTime[index];
					nArrivals[index] = nArrivals[index] + 1;
					nextArrivalTime[index] = randAP[index].nextArrival();
					ArrivalTimes[index][nArrivals[index]] = globalT;
					DepartureTimes[index][nArrivals[index]] = globalT;
					nBalking[index] = nBalking[index] + 1;
					nBalk[index][nBalking[index]] = nArrivals[index];
					
				}else{
					globalT = nextArrivalTime[index];
					nArrivals[index] = nArrivals[index] + 1;
					nextArrivalTime[index] = randAP[index].nextArrival();
					ArrivalTimes[index][nArrivals[index]] = globalT;

					//resetting the system state, number in system increases by 1, and the customer goes to the first server that is idle
					//if no server is idle, customer joins the queue
					for(int i = 1; i <= noOfServers; i++){
						if(inService[0][i] == 0){
							nSystem[0] = nSystem[0] + 1;
							nSystem[index] = nSystem[index] + 1;
							nInService[index] = nInService[index] + 1;
							inService[0][i] = nArrivals[index];
							inService[1][i] = index;
							while(nBalk[index][indexB[index]] < nArrivals[index] && indexB[index] <= nBalking[index]){
								indexB[index] = indexB[index] + 1;
							}
					
							serviceTime = randST[index].nextService();
							nextServiceComp[i] = globalT + serviceTime;
							break;
						}
						if(i == noOfServers && inService[0][i] != 0){
							nSystem[0] = nSystem[0] + 1;
							nSystem[index] = nSystem[index] + 1;
						}
					}

					nTotal[index] = nTotal[index] + 1;
					SystemState[index][0][nTotal[index]] = nSystem[index];
					SystemState[index][1][nTotal[index]] = globalT;
					nAllClassesTotal = nAllClassesTotal + 1;
					totalSystemState[0][nAllClassesTotal] = nSystem[0];
					totalSystemState[1][nAllClassesTotal] = globalT; 
				}
			}

			else if(nextEvent[0] == 1 && nextServiceComp[index] <= endTime){
				int customerClass = inService[1][index];
				int customerNumber = inService[0][index];
				globalT = nextServiceComp[index];
				nServed[index] = nServed[index] + 1;
				if(nDepartIndex[customerClass] < customerNumber){nDepartIndex[customerClass] = customerNumber;}
				nDepartures[customerClass] = nDepartures[customerClass] + 1;
				DepartureTimes[customerClass][customerNumber] = globalT;

				nSystem[0] = nSystem[0] - 1;
				nSystem[customerClass] = nSystem[customerClass] - 1;
				nInService[customerClass] = nInService[customerClass] - 1;
				if(nSystem[0] < noOfServers){
					inService[0][index] = 0;
					inService[1][index] = 0;
					nextServiceComp[index] = Double.POSITIVE_INFINITY;
				}else{
					int[] newCustomer = priorityCustomer(nSystem,nInService,nDepartIndex,inService);
					int newCustomerClass = newCustomer[0];
					int newCustomerNumber = newCustomer[1];
					while(newCustomerNumber == nBalk[newCustomerClass][indexB[newCustomerClass]] && indexB[newCustomerClass] <= nBalking[newCustomerClass]){
						newCustomerNumber = newCustomerNumber + 1;
						indexB[newCustomerClass] = indexB[newCustomerClass] + 1;
					}
					nInService[newCustomerClass] = nInService[newCustomerClass] + 1;
					inService[0][index] = newCustomerNumber;
					inService[1][index] = newCustomerClass;
					serviceTime = randST[newCustomerClass].nextService();
					nextServiceComp[index] = globalT + serviceTime;
				}
				nTotal[customerClass] = nTotal[customerClass] + 1;
				SystemState[customerClass][0][nTotal[customerClass]] = nSystem[customerClass];
				SystemState[customerClass][1][nTotal[customerClass]] = globalT;
				nAllClassesTotal = nAllClassesTotal + 1;
				totalSystemState[0][nAllClassesTotal] = nSystem[0];
				totalSystemState[1][nAllClassesTotal] = globalT; 
			}

			else{
				if(nSystem[0] > 0){
					int customerClass = inService[1][index];
					int customerNumber = inService[0][index];
					globalT = nextServiceComp[index];
					nServed[index] = nServed[index] + 1;
					if(nDepartIndex[customerClass] < customerNumber){nDepartIndex[customerClass] = customerNumber;}
					nDepartures[customerClass] = nDepartures[customerClass] + 1;
					DepartureTimes[customerClass][customerNumber] = globalT;

					nSystem[0] = nSystem[0] - 1;
					nSystem[customerClass] = nSystem[customerClass] - 1;
					nInService[customerClass] = nInService[customerClass] - 1;
					if(nSystem[0] < noOfServers){						
						inService[0][index] = 0;
						inService[1][index] = 0;
						nextServiceComp[index] = Double.POSITIVE_INFINITY;
					}else{
						int[] newCustomer = priorityCustomer(nSystem,nInService,nDepartIndex,inService);
						int newCustomerClass = newCustomer[0];
						int newCustomerNumber = newCustomer[1];
						while(newCustomerNumber == nBalk[newCustomerClass][indexB[newCustomerClass]] && indexB[newCustomerClass] <= nBalking[newCustomerClass]){
							newCustomerNumber = newCustomerNumber + 1;
							indexB[newCustomerClass] = indexB[newCustomerClass] + 1;
						}
						nInService[newCustomerClass] = nInService[newCustomerClass] + 1;
						inService[0][index] = newCustomerNumber;
						inService[1][index] = newCustomerClass;
						serviceTime = randST[newCustomerClass].nextService();
						nextServiceComp[index] = globalT + serviceTime;
					}
					nTotal[customerClass] = nTotal[customerClass] + 1;
					SystemState[customerClass][0][nTotal[customerClass]] = nSystem[customerClass];
					SystemState[customerClass][1][nTotal[customerClass]] = globalT;
					nAllClassesTotal = nAllClassesTotal + 1;
					totalSystemState[0][nAllClassesTotal] = nSystem[0];
					totalSystemState[1][nAllClassesTotal] = globalT; 
				}else{
					Tp = 0;
					double x = globalT - endTime;
					if(x>Tp){
						Tp = x;
					}
					break;
				}
			}
			//break;
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

	public double[] AvgWaitTime(){
		double[] answer = new double[noOfClasses+1];
		for(int j=1; j<=noOfClasses; j++){
			for(int i=1; i<=nArrivals[j]; i++){
				if(nDepartures[j]>0){
					answer[j] = answer[j] + (DepartureTimes[j][i] - ArrivalTimes[j][i])/nDepartures[j];
				}
				else{
					answer[j] = 0;
				}		
			}
		}
		return(answer);
	}

	public double[] AvgQueueLength(){
		double[] length = new double[noOfClasses+1];
		for(int j=1; j<=noOfClasses; j++){
			for(int i=1; i<nTotal[j]; i++){
				length[j] = length[j] + SystemState[j][0][i]*(SystemState[j][1][i+1] - SystemState[j][1][i]);
			}
			length[j] = length[j]/globalT;
		}
		return(length);
	}

	public double AvgNoCustomers(){
		double number = 0;
		for (int i = 0; i<nAllClassesTotal ; i++){
			number = number + totalSystemState[0][i]*(totalSystemState[1][i+1] - totalSystemState[1][i]);
		}
		number = number/globalT;
		return(number);
	}

	public double SocialWelfare(){
		double welfare = 0;
		for (int i = 1; i<=noOfClasses; i++){
			welfare = nDepartures[i]*R[i] - C[i]*AvgWaitTime()[i]*nDepartures[i] + welfare;
		} 
		welfare = welfare/globalT;
		return(welfare);
	}

	public void PrintStats(){
		System.out.println("for PriorityQueueFullRationality1");
		for(int i = 1; i<=noOfClasses ; i++){ 
			System.out.print("Total number of people that come of class " + i +" are : ");
			System.out.println(nArrivals[i]);
		}
		System.out.println();
		for(int i = 1; i<=noOfClasses ; i++){ 
			System.out.print("Total number of people that depart of class " + i +" are : ");
			System.out.println(nDepartures[i]);
		}
		System.out.println();
		for(int i = 1; i<=noOfClasses ; i++){ 
			System.out.print("Total number of people that balk of class " + i +" are : ");
			System.out.println(nBalking[i]);
		}
		System.out.println();
		for(int i = 1; i<=noOfServers ; i++){ 
			System.out.print("Total number of people served by server " + i +" are : ");
			System.out.println(nServed[i]);
		}
		System.out.println();
		for(int i = 1; i<=noOfClasses ; i++){
			System.out.print("Average waiting time per customer of class " + i +" : ");
			System.out.println(AvgWaitTime()[i]);
		}
		System.out.println();
		for(int i = 1; i<=noOfClasses ; i++){
			System.out.print("Average Queue Length of class " + i + " : ");
			System.out.println(AvgQueueLength()[i]);
		}
		System.out.println();

		System.out.print("Average number of customers in the system : ");
		System.out.println(AvgNoCustomers());
		
		System.out.print("Time past the end time the last customer departs: ");
		System.out.println(Tp);
		System.out.println();
	}

	public void PrintState(){
		for(int i =1; i<=noOfClasses; i++){
			System.out.print(nSystem[i] + ", ");
		}
		System.out.println();
		for(int i =1; i<=noOfClasses; i++){
			System.out.print(nArrivals[i] + ", ");
		}
		System.out.println();
		for(int i =1; i<=noOfServers; i++){
			System.out.print(nServed[i] + ", ");
		}
		System.out.println();
		/*for(int i =0; i<=noOfClasses; i++){
			System.out.print(nTotal[i] + ", ");
		}
		System.out.println();*/
		for(int i =1; i<=noOfClasses; i++){
			for(int j=1; j<=nBalking[i]; j++){
				System.out.print(nBalk[i][j] + ", ");
			}
			System.out.println();
		}
		System.out.println();

		for(int j=1; j<=noOfClasses; j++){
			for(int i=1; i<=nArrivals[j]; i++){
				if(ArrivalTimes[j][i] > DepartureTimes[j][i]){
					System.out.println(ArrivalTimes[j][i] + ", " + DepartureTimes[j][i] + "             here");
				}
				else if(ArrivalTimes[j][i] == DepartureTimes[j][i]){
					System.out.println(ArrivalTimes[j][i] + ", " + DepartureTimes[j][i] + "              balk");
				}else{	
					System.out.println(ArrivalTimes[j][i] + ", " + DepartureTimes[j][i]);
				}
			}
			System.out.println();
		}
		System.out.println();
		/*for(int j=0; j<=noOfClasses; j++){
			for(int i=0; i<=nArrivals[j]; i++){
				System.out.print(DepartureTimes[j][i] + ", ");
			}
			System.out.println();
		}
		System.out.println();*/
		/*for(int j=0; j<=noOfClasses; j++){
			System.out.println("for class" + j);
			for(int i=0; i<=nTotal[j]; i++){
				System.out.print((int)SystemState[j][0][i] + ":" + SystemState[j][1][i] + ", ");
			}
			System.out.println();
		}*/
		System.out.println("one end");
	}
}

