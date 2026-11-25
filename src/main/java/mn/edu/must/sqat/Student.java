package mn.edu.must.sqat;

public class Student {
    private String studentId;
    private String name;
    private boolean active;

    public Student(String studentId, String name, boolean active) {
        this.studentId = studentId;
        this.name = name;
        this.active = active;
    }

    public String getStudentId() {
        return studentId;
    }

    public String getName() {
        return name;
    }

    public boolean isActive() {
        return active;
    }
}
