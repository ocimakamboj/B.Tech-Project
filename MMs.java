/*
An implementation of Basic M/M/s Queue, that is there are s number of servers, FCFS (first come first serve)

Inputs Required : t - time for which entry to the queue is allowed
				  s - No. of servers
				  ArrivalParameter - arrival rate
				  ServiceParameter - service rate

Methods : EventRun(): to start the simulation
		  AvgWaitTime() : to compute the average waiting time of the customer
		  AvgQueueLength() : to compute the average Queue Length
*/


public class MMs{
	double globalT; //current time
	int nArrivals; //total number of arrivals
	int[] nServed; //total number of customers served by server i; nServed[i] gives the number of customers served by server-i
				   //(0 doesn't mean anything)
	int[] nSystem; //System State; that is number of customers presently in the system, and the respective customers with the servers
				   //(n,i1,i2, ... , is) corresponds to - n the number of people in the system, i1 is the customer with server 1
				   //i2 is the customer with server 2, and so on. If any i is 0, it means that that server is idle
	double[] ArrivalTimes; //Array that stores the arrival times of i'th customer at i'th index (0 index doesn't mean anything)
	double[] DepartureTimes; //Array that stores the departure times of i'th customer at i'th index (0 index doesn't mean anything)
	double nextArrivalTime;	//gives the time of next arrival
	double[] nextServiceComp; //gives the service completion time; nextServiceComp[i] gives the service completion time of the customer
							  //being served by server i (0 doesn't mean anything)
	double endTime; //time after which no entry is allowed
	double Tp; //time past endTime when the last customer departs
	ArrivalProcess randAP; //Arrival process variable to calculate the next arrival time
	ServiceTime randST; //Service time variable to generate service times
	int nTotal; //total number of arrivals and departures combined
	double[][] SystemState; //2-dimensional array that stores the system state at each point of time; 
							//(SystemState[0][i],SystemState[1][i]) corresponds to the ordered pair (nSystem,globalT)
							//this records the ordered pair whenever an event occurs, thus (1,4) means system state
							//became 1 at time t=4 
	int noOfServers;

	//to initialise the queue, the inputs required - time for which we want to allow entry t, number of servers s,
	//the arrival rate ArrivalParameter, and service rate ServiceParameter
	public MMs(double t, int s, double ArrivalParameter, double ServiceParameter){
		globalT = 0;
		nArrivals = 0;
		nServed = new int[s+1];
		nSystem = new int[s+1];
		ArrivalTimes = new double[20000];
		DepartureTimes = new double[20000];
		randAP = new ArrivalProcess(ArrivalParameter, globalT);
		nextArrivalTime = randAP.nextArrival();
		randST = new ServiceTime(ServiceParameter);
		nextServiceComp = new double[s+1];
		for(int i=1 ; i<=s ; i++){
			nextServiceComp[i] = Double.POSITIVE_INFINITY;
		}
		endTime = t;
		nTotal = 0;
		SystemState = new double[2][40000];
		noOfServers = s;
	}

	//this function takes as input the next arrival time and all the service completion times, and finds out the minimum of them
	//it returns 0 if the next arrival time is the minimum, and returns 'i' if the service completion time at server-i is minimum
	public int min(double q1, double[] q2){
		int min = 0;
		double minValue = q1;
		if(q1 > endTime){
			min = 1;
			minValue = q2[1];
		}
		for(int i = 1; i<noOfServers+1 ; i++){
			if (q2[i] < minValue){
				min = i;
				minValue = q2[i];
			}
		}
		return(min);
	}

	public void EventRun(){
		double serviceTime;
		while(true){
			int index = min(nextArrivalTime,nextServiceComp); //this is the server with the minimum service completion time,
															  //0 if arrival time is minimum
			if((index == 0 && nextArrivalTime <= endTime) || (index >0 && nextServiceComp[index] <= endTime)){ 
				if(index == 0){
					globalT = nextArrivalTime;
					nArrivals = nArrivals + 1;
					nextArrivalTime = randAP.nextArrival();
					ArrivalTimes[nArrivals] = globalT;

					//resetting the system state, number in system increases by 1, and the customer goes to the first server that is idle
					//if no server is idle, customer joins the queue
					for(int i = 1; i <= noOfServers; i++){
						if(nSystem[i] == 0){
							nSystem[0] = nSystem[0] + 1;
							nSystem[i] = nArrivals;
							serviceTime = randST.nextService();
							nextServiceComp[i] = globalT + serviceTime;
							break;
						}
						if(i == noOfServers && nSystem[i] != 0){
							nSystem[0] = nSystem[0] + 1;
						}
					}

					nTotal = nTotal + 1;
					SystemState[0][nTotal] = nSystem[0];
					SystemState[1][nTotal] = globalT;
				}

				else{
					globalT = nextServiceComp[index];
					nServed[index] = nServed[index] + 1;
					DepartureTimes[nSystem[index]] = globalT;

					if(nSystem[0] <= noOfServers){
						nSystem[0] = nSystem[0] - 1;
						nSystem[index] = 0;
						nextServiceComp[index] = Double.POSITIVE_INFINITY;
					}else{
						//finding the customer being served with largest index
						int newCustomer = nSystem[1];
						for(int i =2; i<=noOfServers; i++){
							if (newCustomer < nSystem[i]){
								newCustomer = nSystem[i];
							}
						}
						nSystem[0] = nSystem[0] - 1;
						nSystem[index] = newCustomer + 1;
						serviceTime = randST.nextService();
						nextServiceComp[index] = globalT + serviceTime;
					}

					nTotal = nTotal + 1;
					SystemState[0][nTotal] = nSystem[0];
					SystemState[1][nTotal] = globalT;
				}
			}
			else{
				if(nSystem[0] > 0){
					globalT = nextServiceComp[index];
					nServed[index] = nServed[index] + 1;
					DepartureTimes[nSystem[index]] = globalT;

					nSystem[0] = nSystem[0] - 1;
					if(nSystem[0] > 0){
						if(nSystem[0] < noOfServers){
							nSystem[index] = 0;
							nextServiceComp[index] = Double.POSITIVE_INFINITY;
						}else{
							//finding the customer being served with largest index
							int newCustomer = nSystem[1];
							for(int i =2; i<=noOfServers; i++){
								if (newCustomer < nSystem[i]){
									newCustomer = nSystem[i];
								}
							}
							nSystem[index] = newCustomer + 1;
							serviceTime = randST.nextService();
							nextServiceComp[index] = globalT + serviceTime;
						}
					}

					nTotal = nTotal + 1;
					SystemState[0][nTotal] = nSystem[0];
					SystemState[1][nTotal] = globalT;
				}else{
					Tp = 0;
					double x = globalT - endTime;
					if(x>Tp){
						Tp = x;
					}
					break;
				}
			}
		}
	}

	public double AvgWaitTime(){
		double waittime = 0;
		for(int i = 0; i <= nArrivals; i++){
			waittime = (DepartureTimes[i] - ArrivalTimes[i])/nArrivals + waittime;
		}
		return(waittime);
	}

	public double AvgQueueLength(){
		double length = 0;
		for (int i = 0; i<nTotal ; i++){
			length = length + SystemState[0][i]*(SystemState[1][i+1] - SystemState[1][i]);
		}
		length = length/globalT;
		return(length);
	}

	public void PrintStats(){
		System.out.println("for MMs");
		System.out.println("Total number of people that come are : " + nArrivals);
		System.out.println("Average waiting time per customer: " + AvgWaitTime());
		System.out.println("Average Queue Length: " + AvgQueueLength());
		System.out.println();
	}

	public void PrintState(){
		for (int i=0; i<=noOfServers ; i++){
			System.out.print(nServed[i] + ", ");
		}
		System.out.println("");
		for (int i=0; i<=nArrivals ; i++){
			System.out.print(ArrivalTimes[i] + ", ");
		}
		System.out.println("");
		for (int i=0; i<=nArrivals ; i++){
			System.out.print(DepartureTimes[i] + ", ");
		}
		System.out.println("");
		for (int i=0; i<=nTotal ; i++){
			System.out.print(SystemState[0][i] + ":" + SystemState[1][i] + ", ");
		}
		System.out.println("");
	}

}