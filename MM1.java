/*
An implementation of Basic M/M/1 Queue following FCFS (first come first serve)

Inputs Required : t - time for which entry to the queue is allowed
				  ArrivalParameter - arrival rate
				  ServiceParameter - service rate

Methods : EventRun(): to start the simulation
		  AvgWaitTime() : to compute the average waiting time of the customer
		  AvgQueueLength() : to compute the average Queue Length
*/

public class MM1{
	public static final String ANSI_GREEN = "\u001B[32m";
	public static final String ANSI_RESET = "\u001B[0m";
	double globalT; //current time
	int nArrivals; //total number of arrivals
	int nDepartures; //total number of departures
	int nSystem; //System State; that is number of customers presently in the system
	double[] ArrivalTimes; //Array that stores the arrival times of i'th customer at i'th index (0 index doesn't mean anything)
	double[] DepartureTimes; //Array that stores the departure times of i'th customer at i'th index (0 index doesn't mean anything)
	double nextArrivalTime;	//gives the time of next arrival
	double nextDepartureTime; //gives the time of next departure
	double endTime; //time after which no entry is allowed
	double Tp; //time past entTime when the last customer departs
	ArrivalProcess randAP; //Arrival process variable to calculate the next arrival time
	ServiceTime randST; //Service time variable to generate service times
	int nTotal; //total number of arrivals and departures combined
	double[][] SystemState; //2-dimensional array that stores the system state at each point of time; 
							//(SystemState[0][i],SystemState[1][i]) corresponds to the ordered pair (nSystem,globalT)
							//this records the ordered pair whenever an event occurs, thus (1,4) means system state
							//became 1 at time t=4 

	//to initialise the queue, the inputs required - time for which we want to allow entry t, the arrival rate ArrivalParameter, and service rate
	//ServiceParameter
	public MM1(double t, double ArrivalParameter, double ServiceParameter){	
		globalT = 0;
		nArrivals = 0;
		nDepartures = 0;
		nSystem = 0;
		ArrivalTimes = new double[10000];
		DepartureTimes = new double[10000];
		randAP = new ArrivalProcess(ArrivalParameter, globalT);
		nextArrivalTime = randAP.nextArrival();
		randST = new ServiceTime(ServiceParameter);
		nextDepartureTime = Double.POSITIVE_INFINITY;
		endTime = t;
		nTotal = 0;
		SystemState = new double[2][20000];
	}

	//this is the main function block where the queue runs
	public void EventRun(){	
		double serviceTime;
		while(true){
			if (nextArrivalTime<=nextDepartureTime && nextArrivalTime<=endTime){
				globalT = nextArrivalTime;
				nArrivals = nArrivals + 1;
				nSystem = nSystem + 1;
				nextArrivalTime = randAP.nextArrival();
				if (nSystem==1){
					serviceTime = randST.nextService();
					nextDepartureTime = globalT + serviceTime;
				}
				ArrivalTimes[nArrivals] = globalT;
				nTotal = nTotal + 1;
				SystemState[0][nTotal] = nSystem;
				SystemState[1][nTotal] = globalT;
			}

			else if (nextDepartureTime<nextArrivalTime && nextDepartureTime<=endTime){
				globalT = nextDepartureTime;
				nSystem = nSystem - 1;
				nDepartures = nDepartures + 1;
				if(nSystem == 0){
					nextDepartureTime = Double.POSITIVE_INFINITY;
				}else{
					serviceTime = randST.nextService();
					nextDepartureTime = globalT + serviceTime;
				}
				DepartureTimes[nDepartures] = globalT;
				nTotal = nTotal + 1;
				SystemState[0][nTotal] = nSystem;
				SystemState[1][nTotal] = globalT;
			}

			else if (nextArrivalTime > endTime && nextDepartureTime > endTime && nSystem > 0){
				globalT = nextDepartureTime;
				nSystem = nSystem - 1;
				nDepartures = nDepartures + 1;
				if (nSystem > 0){
					serviceTime = randST.nextService();
					nextDepartureTime = globalT + serviceTime;
				}
				DepartureTimes[nDepartures] = globalT;
				nTotal = nTotal + 1;
				SystemState[0][nTotal] = nSystem;
				SystemState[1][nTotal] = globalT;
			}

			else if(nextArrivalTime > endTime && nextDepartureTime > endTime && nSystem==0){
				Tp = 0;
				double x = globalT - endTime;
				if(x>Tp){
					Tp = x;
				}
				break;
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
		//System.out.println(ANSI_GREEN + "for MM1" + ANSI_RESET);
		System.out.println("for MM1");
		System.out.println("Total number of people that come are : " + nArrivals);
		System.out.println("Average waiting time per customer: " + AvgWaitTime());
		System.out.println("Average Queue Length: " + AvgQueueLength());
		System.out.println();
	}

	public void PrintState(){
		for (int i = 0; i<=nArrivals; i++){
			System.out.println(ArrivalTimes[i]);
		}

		System.out.println("here starts departure time");
		System.out.println();
		System.out.println();
		System.out.println();
		System.out.println();

		for (int i = 0; i<=nArrivals; i++){
			System.out.println(DepartureTimes[i]);
		}
	}
}