import java.util.Random;

//this generates the Service time given some distribution with parameter p. This gives the time taken to complete the service,
//without taking into account the global time
public class ServiceTime{
	double parameter;

	public ServiceTime(double p){
		parameter = p;
	}

	public double nextService(){
		Random randNumber = new Random();
		double u = randNumber.nextDouble();
		double time = - (Math.log(u))/parameter;
		return (time);
	}
}