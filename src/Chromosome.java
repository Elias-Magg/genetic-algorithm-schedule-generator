import java.util.ArrayList;
import java.util.Random;

public class Chromosome implements Comparable<Chromosome> {

    private Lesson[][][] genes;		//[class][days][hours] = lesson

    private int fitness;		//= score.

    /*There will not be a constructor that randomizes the attributes of the object
      simply because there is no way to check if a teacher is not teaching a lesson
      that he is not supposed to or that a class of students is not being taught a lesson
      they should not be being taught.
      The randomization of the schedule will be carried out in the main method and copied over
      using the constructor bellow.
    */

    //Constructor
    public Chromosome(Lesson[][][] genes)
    {
        this.genes = new Lesson[9][5][7];
        for(int i=0; i<9; i++){
            for(int j=0; j<5; j++) {
                for(int k=0; k<7; k++) {
                    this.genes[i][j][k] = genes[i][j][k];
                }
            }
        }
        this.calculateFitness();
    }

    /*-Fitness score = # of conflicts.
      -Different conflicts have different multipliers based on their importance.
	  -The lower the score the closer the chromosome is to the final form of the schedule.    
     */
    public void calculateFitness(){
        int score = 1;

        //This section finds the number of times a teacher is teaching in two different classes at the same time
        int timeParadoxes = 0;
        for(int i=0; i<9; i++) {
            for (int j = 0; j < 5; j++) {
                for (int k = 0; k < 7; k++) {

                    for(int l=i+1; l<9; l++){
                        if(genes[l][j][k]!=null && genes[i][j][k]!=null && genes[i][j][k].getTeacherId()==genes[l][j][k].getTeacherId())timeParadoxes++;
                    }

                }
            }
        }
        score += timeParadoxes*10;

        //This section calculates the amount of unoccupied hours in between occupied ones
        int unoccupiedHours = 0;
        for(int i=0; i<9; i++) {
            for (int j = 0; j < 5; j++) {
                boolean hasLessons = false;
                for (int k = 6; k >= 0; k--) {
                    //check where the last lesson is in the day and count every unoccupied hour before it
                    if (genes[i][j][k] != null) hasLessons = true;
                    if (genes[i][j][k] == null && hasLessons) unoccupiedHours++;
                }
            }
        }
        score += unoccupiedHours*10;

        //This section calculates the amount of times teachers have to work for more than 2 hours
        int overworkingHours = 0;

        //Checks 3 consecutive hours across all the classes' schedules
        for(int i=0; i<9; i++){
            for(int j=0; j<5; j++) {
                for (int k = 0; k < 5; k++) {

                    boolean secondHour = false;
                    for(int l=0; l<9; l++){
                        if(genes[l][j][k+1]!=null && genes[i][j][k]!= null && genes[i][j][k].getTeacherId()==genes[l][j][k+1].getTeacherId())secondHour=true;
                    }
                    for(int l=0; l<9; l++){
                        if(genes[l][j][k+2]!=null && genes[i][j][k]!=null && genes[i][j][k].getTeacherId()==genes[l][j][k+2].getTeacherId() && secondHour)overworkingHours++;
                    }
                }
            }
        }
        score += overworkingHours*2;

		/* -This section counts how many days have a great deviation in terms of lesson hours than the rest of the other days.
		   - To achieve this it is first needed to discover how many lesson hours per day are registered in the schedule
			on average
        */
        int[] averageHours = new int[9];	//per class, weekly
        int[][] hoursPerDay = new int[9][5];
        for(int i=0; i<9; i++) {
            for (int j = 0; j < 5; j++) {
                for (int k = 6; k >= 0; k--) {
                    if (genes[i][j][k] != null) {
                        averageHours[i] += k;
                        hoursPerDay[i][j] = k;
                        break;
                    }
                }
            }
        }

        int overOrUnderScheduledDays = 0;

		//If a day has =>2 or <=2 hours than the average it is considered a conflict
        for(int i=0; i<9; i++){
            for (int j = 0; j < 5; j++) {
                if (hoursPerDay[i][j] >= (averageHours[i]/5) + 2 || hoursPerDay[i][j] <= (averageHours[i]/5) - 2)overOrUnderScheduledDays++;
            }
        }
        score += overOrUnderScheduledDays*5;

        //This section counts how many times a class is scheduled for a lesson more that 4 hours in a day
        int lessonOverSchedule = 0;
        for(int i=0; i<9; i++){
            for(int j=0; j<5; j++) {
                for (int k = 0; k < 4; k++) {
                    int secondHour = -1;
                    int thirdHour = -1;
                    for(int l=k+1; l<7; l++){
                        if(genes[i][j][l]!=null && genes[i][j][k]!=null && genes[i][j][k].getLessonId()==genes[i][j][l].getLessonId() && secondHour==-1 && thirdHour==-1)secondHour=l;
                        if(genes[i][j][l]!=null && genes[i][j][k]!=null && genes[i][j][k].getLessonId()==genes[i][j][l].getLessonId() && secondHour!=-1 && thirdHour==-1)thirdHour=l;
                        if(genes[i][j][l]!=null && genes[i][j][k]!=null && genes[i][j][k].getLessonId()==genes[i][j][l].getLessonId() && secondHour!=-1 && thirdHour!=-1)lessonOverSchedule++;
                    }
                }
            }
        }
        score += lessonOverSchedule*1;

        /*-This section counts the amount of teachers that work a lot more or a lot less than most teachers
		  -To achieve this first the general amount of hours that the teachers work must be discovered
         */
        ArrayList<Integer> teacherHours = new ArrayList<>();
        ArrayList<Integer> teacherIDs = new ArrayList<>();

        for(int i=0; i<9; i++){
            for(int j=0; j<5; j++) {
                for (int k = 0; k < 7; k++) {
                    //If a teacher with an id that does not exist on the list add it and find out how many hours per week
                    //this teacher is working
                    if(genes[i][j][k]!=null && !teacherIDs.contains(genes[i][j][k].getTeacherId())){
                        teacherIDs.add(genes[i][j][k].getTeacherId());
                        int hours = 0;
                        for(int l=i; l<9; l++) {
                            for (int m = j; m < 5; m++) {
                                for (int n = k+1; n < 7; n++) {
                                    if(genes[l][m][n]!=null && genes[i][j][k].getTeacherId()==genes[l][m][n].getTeacherId())hours++;
                                }
                            }
                        }
                        teacherHours.add(hours);
                    }
                }
            }
        }
        //Find out what the average is
        int totalTeacherHours = 0;
        for (int i = 0; i<teacherIDs.size(); i++){
            totalTeacherHours += teacherHours.get(i);
        }
        int averageTeacherHours = totalTeacherHours/teacherIDs.size();
        int overOrUnderWorkedTeachers = 0;

        //Find out how many teachers deviate from that average
        for (int i = 0; i<teacherIDs.size(); i++){
            if(teacherHours.get(i)<=averageTeacherHours - 5 || teacherHours.get(i)>=averageTeacherHours + 5)overOrUnderWorkedTeachers++;
        }
        score += overOrUnderWorkedTeachers*3;

        //Set the fitness
        this.fitness = score;
    }

    public Lesson[][][] getGenes(){
        return this.genes;
    }


    public void setSpecificGene(int classNum ,int dayNum, int hourNum, Lesson replacementLesson){
        genes[classNum][dayNum][hourNum] = replacementLesson;
    }

    public int getFitness()
    {
        return this.fitness;
    }

    public void setGenes(Lesson[][][] genes)
    {
        for(int i=0; i<9; i++){
            for(int j=0; j<5; j++) {
                for (int k = 0; k < 7; k++) {
                    this.genes[i][j][k] = genes[i][j][k];
                }
            }
        }
    }

    public void setFitness(int fitness)
    {
        this.fitness = fitness;
    }
	
	@Override
    //The compareTo function has been overridden so sorting can be done according to fitness scores
	public int compareTo(Chromosome x)
	{
		if(this.fitness > x.fitness) return -1;
		if(this.fitness < x.fitness) return 1;
		return 0;
	}
}
