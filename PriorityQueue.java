 /*
An implementation of a Priority Queue with 'k' number of classes and 's' servers
Each server can serve a customer of any class
Class-1 has higher priority than class-2, Class-2 has higher priority than class-3
meaning, a customer of class-1 will be served before the customer of class-2
Within classes, FCFS (first come first served) is followed

Inputs Required : t - time for which entry to the queue is allowed
				  s - no of servers
				  k - no of classes
				  ArrivalParameter - array containing the arrival rates of each class
				  ServiceParameter - array containing the service rates of each class

Methods : EventRun(): to start the simulation
		  AvgWaitTime() : an array containing the average waiting time of the customers of each class
		  AvgQueueLength() : an array containing the average Queue Length of the customers of each class
		  AvgNoCustomers() : the overall average number of customers in the system
*/

 public class PriorityQueue{
	double globalT; //current time
	int[] nArrivals; //total number of arrivals; nArrivals[i] gives the total number of arrivals of priority-k class
					 //(0 doesn't mean anything)
	int[] nDepartures; //total number of departures; nDepartures[i] gives the total number of Departures of priority-k class
					   //(0 doesn't mean anything)
	int[] nServed; //total number of customers served by server i; nServed[i] gives the number of customers served by server-i
				   //(0 doesn't mean anything)
	int[] nSystem; //System State; that is number of customers presently in the system, (nT,n1,n2, ... , nk) corresponds to -
				   // nT total number of people in the system, n1 is the number of customers belonging to k-class in the system
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
							//this records the ordered pair whenever an event occurs, thus (2,1,4) means queue length
							//became 1 at time t=4 for class-2; SystemState[0][i][j] doesn't mean anything
	int nAllClassesTotal; //total number of arrivals and departures combined for all the classes
	double[][] totalSystemState; //System State for all classes combined
	int noOfServers;
	int noOfClasses;

	public PriorityQueue(double t, int s, int k, double[] ArrivalParameter, double[] ServiceParameter){
		int capacity = 20000;
		globalT = 0;
		nArrivals = new int[k+1];
		nDepartures = new int[k+1];
		nServed = new int[s+1];
		nSystem = new int[k+1];
		nInService = new int[k+1];
		inService = new int[2][s+1];
		ArrivalTimes = new double[k+1][capacity];
		DepartureTimes = new double[k+1][capacity];
		randAP = new ArrivalProcess[k+1];
		for(int i=0; i<=k; i++){
			randAP[i] = new ArrivalProcess(ArrivalParameter[i], globalT);
		}
		randST = new ServiceTime[k+1];
		for(int i=0; i<=k; i++){
			randST[i] = new ServiceTime(ServiceParameter[i]);
		}
		nextArrivalTime = new double[k+1];
		for(int i=1; i<=k; i++){
			nextArrivalTime[i] = randAP[i].nextArrival();
		}
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
	//and the number of departures from each class returns (0,0) if there are no customers waiting
	public int[] priorityCustomer(int[] q1, int[] q2, int[] q3){
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
			int value = q2[answer[0]] + q3[answer[0]] + 1;
			answer[1] = value;
		}
		return(answer);
	}

	public void EventRun(){
		double serviceTime;
		while(true){
			int[] nextEvent = min(nextArrivalTime,nextServiceComp); //this tells whether departure or arrival is minimum
																	//and tells the index of the server or class respectively
			int index = nextEvent[1];
			if(nextEvent[0] == 0 && nextArrivalTime[index] <= endTime){
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

			else if(nextEvent[0] == 1 && nextServiceComp[index] <= endTime){
				int customerClass = inService[1][index];
				int customerNumber = inService[0][index];
				globalT = nextServiceComp[index];
				nServed[index] = nServed[index] + 1;
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
					int[] newCustomer = priorityCustomer(nSystem,nInService,nDepartures);
					int newCustomerClass = newCustomer[0];
					int newCustomerNumber = newCustomer[1];
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
						int[] newCustomer = priorityCustomer(nSystem,nInService,nDepartures);
						int newCustomerClass = newCustomer[0];
						int newCustomerNumber = newCustomer[1];
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
			//PrintState();
		}
	}

	public double[] AvgWaitTime(){
		double[] answer = new double[noOfClasses+1];
		for(int j=1; j<=noOfClasses; j++){
			for(int i=1; i<=nArrivals[j]; i++){
				answer[j] = answer[j] + (DepartureTimes[j][i] - ArrivalTimes[j][i])/nArrivals[j];
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

	public void PrintStats(){
		System.out.println("for PriorityQueue");
		for(int i = 1; i<=noOfClasses ; i++){ 
			System.out.print("Total number of people that come of class " + i +" are : ");
			System.out.println(nArrivals[i]);
		}
		for(int i = 1; i<=noOfClasses ; i++){
			System.out.print("Average waiting time per customer of class " + i +" : ");
			System.out.println(AvgWaitTime()[i]);
		}
		for(int i = 1; i<=noOfClasses ; i++){
			System.out.print("Average Queue Length of class " + i + " : ");
			System.out.println(AvgQueueLength()[i]);
		}

		System.out.print("Average number of customers in the system : ");
		System.out.println(AvgNoCustomers());
		
		System.out.print("Time past the end time the last customer departs: ");
		System.out.println(Tp);
		System.out.println();
	}

	public void PrintState(){
		System.out.print("n in System: ");
		for(int i =0; i<=noOfClasses; i++){
			System.out.print(nSystem[i] + ", ");
		}
		System.out.println();
		System.out.print("no of Arrivals: ");
		for(int i =0; i<=noOfClasses; i++){
			System.out.print(nArrivals[i] + ", ");
		}
		System.out.println();
		System.out.print("n Served: ");
		for(int i =0; i<=noOfServers; i++){
			System.out.print(nServed[i] + ", ");
		}
		System.out.println();
		System.out.print("combined arrivals and departures: ");
		for(int i =0; i<=noOfClasses; i++){
			System.out.print(nTotal[i] + ", ");
		}
		System.out.println();
		System.out.println();
		System.out.println("Arrival Times");
		for(int j=0; j<=noOfClasses; j++){
			for(int i=0; i<=nArrivals[j]; i++){
				System.out.print(ArrivalTimes[j][i] + ", ");
			}
			System.out.println();
		}
		System.out.println();
		System.out.println("departure Times");
		for(int j=0; j<=noOfClasses; j++){
			for(int i=0; i<=nArrivals[j]; i++){
				System.out.print(DepartureTimes[j][i] + ", ");
			}
			System.out.println();
		}
		System.out.println();
		System.out.println("system state");
		for(int j=0; j<=noOfClasses; j++){
			System.out.println("for class" + j);
			for(int i=0; i<=nTotal[j]; i++){
				System.out.print((int)SystemState[j][0][i] + ":" + SystemState[j][1][i] + ", ");
			}
			System.out.println();
		}
		System.out.println("one end");
		System.out.println();
		System.out.println();
	}
}

