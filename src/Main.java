import java.io.FileNotFoundException;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.util.Scanner;

public class Main {

	public static void main(String[] args) throws FileNotFoundException {

		PrintStream o = new PrintStream(new File("Schedule.txt"));
		System.setOut(o);
		
		Genetic gen = new Genetic();
		gen.readFile();
		Chromosome x = gen.geneticAlgorithm(10, 1, 50, 10000);
		gen.printSchedule(x);



	}
}