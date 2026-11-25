package mn.edu.must.sqat;

public class CourseRegistrationService {

    private final StudentRepository studentRepository;
    private final EmailSender emailSender;

    public CourseRegistrationService(
            StudentRepository studentRepository,
            EmailSender emailSender) {
        this.studentRepository = studentRepository;
        this.emailSender = emailSender;
    }

    public String register(String studentId, String courseId) {

        if (studentId == null || courseId == null) {
            throw new IllegalArgumentException(
                    "Student ID and Course ID cannot be null");
        }

        Student student = studentRepository.findById(studentId);

        if (student == null) {
            return "ERROR_NO_STUDENT";
        }

        if (!student.isActive()) {
            return "ERROR_INACTIVE_STUDENT";
        }

        if (!hasPrerequisites(studentId, courseId)) {
            return "ERROR_PREREQUISITES";
        }

        studentRepository.saveRegistration(studentId, courseId);

        try {
            emailSender.sendEmail(
                    student.getStudentId(),
                    "Course Registration",
                    "You have successfully registered for " + courseId
            );
        } catch (Exception e) {
            // Email алдаа бүртгэлийг цуцлахгүй
        }

        return "OK";
    }

    private boolean hasPrerequisites(String studentId, String courseId) {
        return studentRepository.hasPrerequisites(studentId, courseId);
    }
}
