import java.util.Random;

//This generates the arrival times given some distribution with parameter p (in the case of poisson the parameter is the arrival rate).
//Note - this does not generate the inter-arrival times, but the arrival times as a whole.
//the 'time' here is the global time, which advances everytime an arrival occurs.
public class ArrivalProcess{
	double parameter;
	double time;

	public ArrivalProcess(double p, double t){
		parameter = p;
		time = t;
	}

	public double nextArrival(){
		Random randNumber = new Random();
		double u = randNumber.nextDouble();
		time = time - Math.log(u)/parameter;
		return (time);
	}
}