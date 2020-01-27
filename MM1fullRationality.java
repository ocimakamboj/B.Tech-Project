/*
An implementation of Basic M/M/1 Queue following FCFS (first come first serve).
In this system, the customers have the option to balk after observing the queue length.
let
R - Reward on completion of Service
p - price paid to join the queue
C - waiting cost per unit time
mu - Average service rate
n - no. of customers presently in the system

Upon arrival, the (n+1)st customer will have expected Utility, U = 0, if he balks
and expected Utility, U = R-p-C*(n+1)/mu if he joins

Assuming full rationality, the customer joins only if U > 0

Inputs Required : t - time for which entry to the queue is allowed
				  ArrivalParameter - arrival rate
				  ServiceParameter - service rate
				  Reward - value of Reward
				  price - value of price
				  Cost - waiting cost per unit time

Methods : EventRun(): to start the simulation
		  AvgWaitTime() : to compute the average waiting time of the customer
		  AvgQueueLength() : to compute the average Queue Length
*/

public class MM1fullRationality{
	double globalT; //current time
	int nArrivals; //total number of arrivals
	int nDepartures; //no of people who depart after availing the service (excludes the numbers who balk)
	int nDepartIndex; //keeps track of the index of the customer who will depart
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
	int nBalking; //the number of customers balking
	int[] nBalk; //array of the customers who balk
	double R;
	double p;
	double C;
	double serviceRate;

	//to initialise the queue, the inputs required - time for which we want to allow entry t, the arrival rate ArrivalParameter, service rate
	//ServiceParameter, the 'Reward' the customer will get on the completion of the service, the 'price' he'll pay for joining the Queue, and the 
	//'Cost' incurred per unit time while staying in the queue
	public MM1fullRationality(double t, double ArrivalParameter, double ServiceParameter, double Reward, double price, double Cost){	
		globalT = 0;
		nArrivals = 0;
		nDepartures = 0;
		nDepartIndex = 0;
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
		nBalking = 0;
		nBalk = new int[10000];
		R = Reward;
		p = price;
		C = Cost;
		serviceRate = ServiceParameter;
	}

	//this is the main function block where the queue runs
	public void EventRun(){	
		double serviceTime;
		double utility;
		int indexB = 1;;
		while(true){
			if (nextArrivalTime<=nextDepartureTime && nextArrivalTime<=endTime){
				utility = R - p - C*(nSystem+1)/serviceRate;
				if (utility < 0){
					globalT = nextArrivalTime;
					nArrivals = nArrivals + 1;
					ArrivalTimes[nArrivals] = globalT;
					DepartureTimes[nArrivals] = globalT;
					nextArrivalTime = randAP.nextArrival();
					nBalking =nBalking + 1;
					nBalk[nBalking] = nArrivals;
				}else{
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
			}

			else if (nextDepartureTime<nextArrivalTime && nextDepartureTime<=endTime){
				globalT = nextDepartureTime;
				nSystem = nSystem - 1;
				nDepartIndex = nDepartIndex + 1;
				nDepartures = nDepartures + 1;
				if(nSystem == 0){
					nextDepartureTime = Double.POSITIVE_INFINITY;
				}else{
					serviceTime = randST.nextService();
					nextDepartureTime = globalT + serviceTime;
				}
				while(nDepartIndex == nBalk[indexB] && indexB<=nBalking){
					nDepartIndex = nDepartIndex + 1;
					indexB = indexB + 1;
				}

				DepartureTimes[nDepartIndex] = globalT;
				nTotal = nTotal + 1;
				SystemState[0][nTotal] = nSystem;
				SystemState[1][nTotal] = globalT;
			}

			else if (nextArrivalTime > endTime && nextDepartureTime > endTime && nSystem > 0){
				globalT = nextDepartureTime;
				nSystem = nSystem - 1;
				nDepartIndex = nDepartIndex + 1;
				nDepartures = nDepartures + 1;
				if (nSystem > 0){
					serviceTime = randST.nextService();
					nextDepartureTime = globalT + serviceTime;
				}
				while(nDepartIndex == nBalk[indexB] && indexB<=nBalking){
					nDepartIndex = nDepartIndex + 1;
					indexB = indexB + 1;
				}
				DepartureTimes[nDepartIndex] = globalT;
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
		System.out.println("for MM1fullRationality");
		System.out.println("Total number of people that come are : " + nArrivals);
		System.out.println("Total number of people that depart are : " + nDepartures);
		System.out.println("Total number of people that balk are : " + nBalking);
		System.out.println("Average waiting time per customer: " + AvgWaitTime());
		System.out.println("Average Queue Length: " + AvgQueueLength());
		System.out.println();
	}

	public void PrintState(){
		System.out.println(nArrivals);
		System.out.println(nSystem);
		for(int i = 0; i<=nArrivals; i++){
			System.out.print(ArrivalTimes[i] + ", ");
		}
		System.out.println();
		for(int i = 0; i<=nArrivals; i++){
			System.out.print(DepartureTimes[i] + ", ");
		}
		System.out.println();
		for(int i=0; i<=nBalking; i++){
			System.out.print(nBalk[i] + ", ");
		}
		System.out.println();
		for(int i=0; i<=nTotal; i++){
			System.out.print((int)SystemState[0][i] + "--" + SystemState[1][i] + ", ");
		}
		System.out.println();
		System.out.println();
	}
}