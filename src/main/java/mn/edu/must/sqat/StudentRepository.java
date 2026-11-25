package mn.edu.must.sqat;

public interface StudentRepository {

    Student findById(String studentId);

    boolean hasPrerequisites(String studentId, String courseId);

    void saveRegistration(String studentId, String courseId);
}
