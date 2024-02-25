import java.io.File;
import java.io.FileNotFoundException;
import java.lang.reflect.Array;
import java.util.Scanner;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Arrays;
import java.util.Random;


public class Genetic {

	//Teachers.get(0).get(i) :
	//first get: a teacher	
	//second get: 	0:  hours per day they are able to teach
	//				1:  hours per week they are able to teach 
	//				2+: lessons they are able to teach
	private ArrayList<ArrayList<Integer>> Teachers = new ArrayList<>();

	//Lessons.get(0).get(i) :
	//first get: a lesson
	//second get:   0,2,4...: class the lesson can be taught on
	//				1,3,5...: the hours each lesson can be taught on each class per week
	private ArrayList<ArrayList<Integer>> Lessons = new ArrayList<>();

	private ArrayList<String> TeacherNames = new ArrayList<>();

	//private ArrayList<Integer> TeacherIDs;

	private ArrayList<String> LessonNames = new ArrayList<>();

	private ArrayList<Chromosome> population = new ArrayList<>(); //The current population of chromosomes

	private ArrayList<Integer> fitnessBounds; // Contains indexes of the chromosomes in the population ArrayList
	// Each chromosome index exists in the ArrayList as many times as its inverted fitness score multiplied by 1000
	// This means that lower in fitness score chromosomes have a greater chance of being picked

	public Genetic()
	{
		this.population = null;
		this.fitnessBounds = null;
	}

	// populationSize: The size of the population in every step
	// mutationPropability: The propability a mutation might occur in a chromosome
	// maximumFitness: The maximum fitness value of the solution we wish to find
	// maximumSteps: The maximum number of steps we will search for a solution
	public Chromosome geneticAlgorithm(int populationSize, double mutationProbability, int maximumFitness, int maximumSteps)
	{
		initializePopulation(populationSize);
		updateFitnessBounds();

		Random r = new Random();
		for(int step=0; step < maximumSteps; step++) {
			ArrayList<Chromosome> newPopulation = new ArrayList<>();

			//We choose two chromosomes from the population
			//Due to how fitnessBounds ArrayList is generated, the propability of
			//selecting a specific chromosome depends on its fitness score
			for(int i=0; i < populationSize-3; i+=2) {
				int xIndex = this.fitnessBounds.get(r.nextInt(this.fitnessBounds.size()));
				Chromosome x = this.population.get(xIndex);
				int yIndex = this.fitnessBounds.get(r.nextInt(this.fitnessBounds.size()));
				while(yIndex == xIndex) yIndex = this.fitnessBounds.get(r.nextInt(this.fitnessBounds.size()));
				Chromosome y = this.population.get(yIndex);
				Chromosome child = this.reproduce(x, y);	//We generate the "child" of the two chromosomes

				if (r.nextDouble() < mutationProbability) mutate(child);	//We might then mutate the child
				newPopulation.add(child);
			}
			this.population = new ArrayList<>(newPopulation);

			//We sort the population so the one with the smallest (=best) fitness is first
			Collections.sort(this.population);
			if(this.population.get(0).getFitness() <= maximumFitness){ //If the best chromosome is acceptable we return it
				System.out.println("Finished after " + step + " steps...");
				return this.population.get(0);
			}

			this.updateFitnessBounds();	//Updating the fitnessBounds arrayList
		}

		System.out.println("Finished after " + maximumSteps + " steps...");
		return this.population.get(0);
	}

	//We initialize the population by creating random chromosomes
	public void initializePopulation(int populationSize)
	{
		this.population = new ArrayList<>();
		for(int i=0; i<populationSize; i++){
			this.population.add(spawnRandomChromosome());
		}
		this.updateFitnessBounds();
	}

	//Updates the arraylist that contains as many indexes of a chromosomes as its fitness.
	public void updateFitnessBounds()
	{
		this.fitnessBounds = new ArrayList<Integer>();
		for (int i=0; i<this.population.size(); i++) {
			double tmp = 1d/this.population.get(i).getFitness();
			tmp = tmp * 100000000;
			for(int j=0; j<tmp; j++) {
				fitnessBounds.add(i);
			}
		}
	}

	//"Reproduces" two chromosomes and generates their "child"
	public Chromosome reproduce(Chromosome x, Chromosome y)
	{
		Random r = new Random();
		//Randomly choose the intersection point
		int [] intersectionPoint = {r.nextInt(8), r.nextInt(4),  r.nextInt(6)};
		Lesson [][][] childGenes = new Lesson[9][5][7];

		//The child has the left side of the x chromosome up to the intersection point...
		for(int i=0; i<intersectionPoint[0]; i++){
			for (int j=0; j<intersectionPoint[1]; j++){
				for (int k=0; k<intersectionPoint[2]; k++){
					if (x.getGenes()[i][j][k] != null) {
						childGenes[i][j][k] = x.getGenes()[i][j][k];
					}
				}
			}
		}

		//...and the right side of the y chromosome after the intersection point
		for(int i=intersectionPoint[0]; i<9; i++){
			for(int j=intersectionPoint[1]; j<5; j++){
				for(int k=intersectionPoint[2]; k<7; k++){
					if (y.getGenes()[i][j][k] != null) {;
						childGenes[i][j][k] = y.getGenes()[i][j][k];
					}
				}
			}
		}

		Chromosome child = new Chromosome(childGenes);
		//Update the fitness
		child.setFitness(child.getFitness() + updateFitness(childGenes));

		return child;
	}

	public Chromosome spawnRandomChromosome(){
		Lesson [][][] genes = new Lesson[9][5][7];	//Array to be filled out (empty spots are null)

		//Available hours per week
		int [] teacherHours = new int[this.Teachers.size()];

		//[lesson][class] = hours
		int [][] lessonHours = new int[this.Lessons.size()][9];

		//Copying the available hours per week
		for(int i = 0; i < teacherHours.length; i++){
			teacherHours[i] = this.Teachers.get(i).get(1);
		}

		for(int i = 0; i < this.Lessons.size(); i++){
			for(int j = 0; j < this.Lessons.get(i).size(); j+=2){
				lessonHours[i][this.Lessons.get(i).get(j)]=this.Lessons.get(i).get(j+1);
			}
		}


		Random r = new Random();

		//Fitting all the lessons one by one into the schedule
		for(int i = 0; i < this.Lessons.size(); i++){
			for(int j = 0; j < 9; j++) {
				//Finding the available teachers and store them in an array
				ArrayList<Integer> availableTeachers = new ArrayList<>();
				for (int k = 0; k < this.Teachers.size(); k++) {
					if (this.Teachers.get(k).lastIndexOf(i) >= 2 && teacherHours[k] > 0) availableTeachers.add(k);
				}

				//While loop in order to schedule as many hours of the lesson possible
				while (lessonHours[i][j] > 0 && !availableTeachers.isEmpty()) {
					boolean availableDays = false;
					//Checking if there are empty spots on the schedule
					for (int k = 0; k < 5; k++) {
						for (int l = 0; l < 7; l++) {
							if (genes[j][k][l] == null) {
								availableDays = true;
								break;
							}
						}
						if (availableDays) break;
					}
					//if there are not any break the while loop
					if (!availableDays){
						break;
					}

					int randomDay = r.nextInt(5);
					int randomHour = r.nextInt(7);
					//Randomize until an unoccupied hour is found
					while (genes[j][randomDay][randomHour] != null) {
						randomDay = r.nextInt(5);
						randomHour = r.nextInt(7);
					}

					int randomTeacher = availableTeachers.get(r.nextInt(availableTeachers.size()));

					//Scheduling
					genes[j][randomDay][randomHour] = new Lesson(i, randomTeacher);
					//Updating the available hours in the arrays
					lessonHours[i][j]--;
					teacherHours[randomTeacher]--;
					if(teacherHours[randomTeacher]==0){
						availableTeachers.remove(availableTeachers.indexOf(randomTeacher));
					}

				}
			}
		}

		//This section calculates the amount of times a teacher works more hours than he is supposed to per day
		//Every conflict found gets multiplied by 7
		int score=0;
		for(int i = 0; i < Teachers.size(); i++){
			for(int j =0; j < 9; j++){
				for(int k = 0; k < 5; k++){
					int counter = Teachers.get(i).get(0);
					for(int l = 0; l < 7; l++){
						if(genes[j][k][l]!=null && genes[j][k][l].getTeacherId()==i)counter--;
					}
					if(counter<0) {
						score = -counter*6;
					}
					else{
						score+=counter*6;
					}
				}
			}
		}

		Chromosome newChromosome = new Chromosome(genes);
		newChromosome.setFitness(newChromosome.getFitness() + score);

		return newChromosome;
	}

	public void mutate(Chromosome child){
		Random r = new Random();
		int randomClass = r.nextInt(9);
		int randomDay = r.nextInt(5);
		int randomHour = r.nextInt(7);
		int randomTeacher = r.nextInt(Teachers.size());
		/*!*/	int randomLesson = r.nextInt((Teachers.get(randomTeacher).size()-2)) + 2;
		Lesson replacementLesson = new Lesson(randomLesson,randomTeacher);

		child.setSpecificGene(randomClass,randomDay,randomHour,replacementLesson);

	}

	public int updateFitness(Lesson[][][] schedule){

		//This section calculates the amount of times a teacher is working more hours that the weekly maximum
		//Every conflict has a multiplier of 8
		int score = 0;
		//for each teacher
		for(int i = 0; i<Teachers.size(); i++){
			//Check the entire schedule
			for(int j = 0; j < 9; j++){
				int counter = Teachers.get(i).get(1);
				for(int k = 0; k < 5; k++){
					for(int l = 0; l < 7; l++){
						//When the variable counter becomes negative the teacher is working more than he is supposed to
						if (schedule[j][k][l] != null) {
							if(schedule[j][k][l].getTeacherId()==i)counter--;
						}
					}
				}
				//Add the amount of extra hours to the counter
				if(counter<0) {
					score += -counter*6;
				}
				else{
					score+=counter*6;
				}
			}
		}

		//This section calculates the amount of times a lesson is taught more times than the weekly maximum
		//Every conflict has a multiplier of 8

		//For each lesson
		for(int i = 0; i < Lessons.size(); i++){
			//And each class that that lesson is being taught to
			for(int j = 0; j < Lessons.get(i).size(); j +=2){
				int counter = Lessons.get(i).get(j+1);
				//Check the schedule
				for(int k = 0; k < 5; k++){
					for(int l = 0; l < 7; l++){
						//When the variable counter becomes negative the lesson is being taught more than it is supposed to
						if (schedule[Lessons.get(i).get(j)][k][l] != null) {
							if(schedule[Lessons.get(i).get(j)][k][l].getLessonId()==i)counter--;
						}
					}
				}
				//Add the amount of extra hours to the counter
				if(counter<0) {
					score += -counter*6;
				}
				else{
					score+=counter*6;
				}
			}
		}

		return score*1;
	}

	public void printSchedule(Chromosome chromosome){
		Lesson [][][] schedule  = chromosome.getGenes();
		System.out.println("--------------Schedule--------------\n\n");
		for(int i = 0; i < 9; i++){
			System.out.println("Class " + (i+1) + "\n");
			System.out.println("        Mon        Tue        Wen        Thu        Fri    \n");
			for(int k = 0; k < 14; k++){
				if(k%2==0){
					System.out.print(k / 2 + 1 + "    ");
					for (int j = 0; j < 5; j++) {
						if (schedule[i][j][(k+1)/2] != null) {
							System.out.print(LessonNames.get(schedule[i][j][(k+1)/2].getLessonId()) + "        ");
						}
					}
					System.out.println();
				}
				else{
					System.out.print("    ");
					for (int j = 0; j < 5; j++) {
						if (schedule[i][j][k/2] != null) {
							System.out.print(TeacherNames.get(schedule[i][j][k/2].getTeacherId()) + "    ");
						}
					}
					System.out.println();
					System.out.println();
				}
			}
		}
	}

	public void readFile() throws FileNotFoundException{

		File lessonsfile = new File("lessons.txt"); 		//Opening the lessons file
		Scanner in = new Scanner(lessonsfile);
		while (in.hasNextLine()) {
			String line = in.nextLine();
			if (line != null) {
				line = line.replaceAll("\\s+", "");
				int firstcomma = line.indexOf(',');
				int secondcomma = firstcomma + 2;
				int thirdcomma = secondcomma + 1;

				//Saving the lesson
				String lesson = line.substring(0, firstcomma);
				LessonNames.add(lesson);

				//Saving the lesson's id
				int id = Integer.parseInt(line.substring(firstcomma + 1, secondcomma));
				ArrayList<Integer> tmp = new ArrayList<>();
				Lessons.add(tmp);

				//Saving the classes
				int index = secondcomma + 1;
				char[] classes = new char[3];
				int i = 0;
				while (line.charAt(index) != ',') {
					classes[i] = line.charAt(index);
					i++;
					index++;
				}

				//Saving the hours
				int[] hours = new int[9];
				i = 0;
				index++;
				while (index < line.length() && Character.isDigit(line.charAt(index))) {
					hours[i] = Character.getNumericValue(line.charAt(index));
					i++;
					index++;
				}
				for (int j = 0; j < 3; j++) {
					if (classes[j] == 'A') {
						for (int k = 0; k < 3; k++) {
							Lessons.get(id).add(k);
							Lessons.get(id).add(hours[k]);
						}
					}
					if (classes[j] == 'B') {
						int l = 0;
						int m = 3;
						if (classes[0] == 'A') l = 3;
						for (int k = l; k < l + 3; k++) {
							Lessons.get(id).add(m);
							Lessons.get(id).add(hours[k]);
							m++;
						}
					}
					if (classes[j] == 'C') {
						int l = 0;
						int m = 6;
						if (classes[0] == 'A' || classes[0] == 'B') {
							l = 3;
						}
						if (classes[1] == 'A' || classes[1] == 'B') {
							l = 6;
						}
						for (int k = l; k < l + 3; k++) {
							Lessons.get(id).add(m);
							Lessons.get(id).add(hours[k]);
							m++;
						}
					}

				}
			}
		}


		File teachersfile = new File("teachers.txt"); 		//Opening the teachers file		
		Scanner in2 = new Scanner(teachersfile);
		while (in2.hasNextLine()) {
			String line = in2.nextLine();
			if (line != null) {
				line = line.replaceAll("\\s+", "");
				int firstcomma = line.indexOf(',');
				int secondcomma = line.indexOf(',', firstcomma + 1);
				int thirdcomma = line.indexOf(',', secondcomma + 1);
				int fourthcomma = line.indexOf(',', thirdcomma + 1);


				//Saving the teacher
				String name = line.substring(0, firstcomma);
				TeacherNames.add(name);

				//Saving the teacher's id
				int tId = Integer.parseInt(line.substring(firstcomma + 1, secondcomma));
				//TeacherIDs.add(tId);
				ArrayList<Integer> tmp = new ArrayList<>();
				Teachers.add(tmp);

				//Saving the lesson ids - part1
				int index = secondcomma + 1;
				List<Integer> lessonId = new ArrayList<>();
				while (line.charAt(index) != ',') {
					lessonId.add(Character.getNumericValue(line.charAt(index)));
					index++;
				}

				//Saving the available daily hours
				int hoursDay = Integer.parseInt(line.substring(thirdcomma + 1, fourthcomma));
				Teachers.get(tId).add(hoursDay);

				//Saving the available weekly hours
				int hoursWeek = Integer.parseInt(line.substring(fourthcomma + 1));
				Teachers.get(tId).add(hoursWeek);

				//Saving the lesson ids - part2
				for (int i = 0; i < lessonId.size(); i++) {
					Teachers.get(tId).add(lessonId.get(i));
				}
			}
		}
	}
}





