/*
This is a simple class for representing the teacher , class and lesson
that occupy a time interval in a schedule
 */


public class Lesson {
    private int LessonId;
    private int ClassId;
    private int TeacherId;

    public Lesson(int LessonId, int TeacherId){
        this.LessonId = LessonId;
        this.TeacherId = TeacherId;
    }

    public Lesson(){}

    public int getLessonId(){
        return this.LessonId;
    }

    public int getTeacherId(){
        return this.TeacherId;
    }

    public int[] get(){
        int[] temp = {this.LessonId,this.ClassId,this.TeacherId};
        return temp;
    }

    public void setLessonId(int lessonId){
        this.LessonId = lessonId;
    }

    public void setTeacherId(int teacherId){
        this.TeacherId = teacherId;
    }

    public void set(int[] attributes){
        if(attributes.length>2){
            System.err.println("Lesson attributes cannot be more than 3");
            return;
        }
        this.LessonId = attributes[0];
        this.TeacherId = attributes[1];
    }

}
