package mn.edu.must.sqat;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CourseRegistrationServiceTest {

    @Mock
    private StudentRepository studentRepository;

    @Mock
    private EmailSender emailSender;

    @InjectMocks
    private CourseRegistrationService service;

    private Student activeStudent;

    @BeforeEach
    void setUp() {
        activeStudent = new Student("S001", "Bat", true);
    }

    @Test
    void A1_activeStudent_registersSuccessfully() {
        when(studentRepository.findById("S001")).thenReturn(activeStudent);
        when(studentRepository.hasPrerequisites("S001", "CS101")).thenReturn(true);

        String result = service.register("S001", "CS101");

        assertEquals("OK", result);
        verify(studentRepository).saveRegistration("S001", "CS101");
        verify(emailSender).sendEmail(
                eq("S001"), eq("Course Registration"), contains("CS101"));
    }

    @Test
    void A2_missingStudent_returnsError() {
        when(studentRepository.findById("S002")).thenReturn(null);

        String result = service.register("S002", "CS101");

        assertEquals("ERROR_NO_STUDENT", result);
        verify(studentRepository, never()).saveRegistration(anyString(), anyString());
        verifyNoInteractions(emailSender);
    }

    @Test
    void A3_inactiveStudent_returnsError() {
        Student inactive = new Student("S003", "Bold", false);
        when(studentRepository.findById("S003")).thenReturn(inactive);

        String result = service.register("S003", "CS101");

        assertEquals("ERROR_INACTIVE_STUDENT", result);
        verifyNoInteractions(emailSender);
        verify(studentRepository, never()).saveRegistration(anyString(), anyString());
    }

    @Test
    void A4_missingPrerequisites_returnsError() {
        when(studentRepository.findById("S001")).thenReturn(activeStudent);
        when(studentRepository.hasPrerequisites("S001", "CS201")).thenReturn(false);

        String result = service.register("S001", "CS201");

        assertEquals("ERROR_PREREQUISITES", result);
        verify(studentRepository, never()).saveRegistration(anyString(), anyString());
        verifyNoInteractions(emailSender);
    }

    @Test
    void A5_emailFailure_doesNotCancelRegistration() {
        when(studentRepository.findById("S001")).thenReturn(activeStudent);
        when(studentRepository.hasPrerequisites("S001", "CS101")).thenReturn(true);
        doThrow(new RuntimeException("Email server error"))
                .when(emailSender)
                .sendEmail(anyString(), anyString(), anyString());

        String result = service.register("S001", "CS101");

        assertEquals("OK", result);
        verify(studentRepository).saveRegistration("S001", "CS101");
    }

    @Test
    void A6_nullInput_throwsException() {
        assertThrows(IllegalArgumentException.class,
                () -> service.register(null, "CS101"));

        assertThrows(IllegalArgumentException.class,
                () -> service.register("S001", null));

        verifyNoInteractions(studentRepository, emailSender);
    }

    @Test
    void A7_emailBodyContainsCourseCode() {
        when(studentRepository.findById("S001")).thenReturn(activeStudent);
        when(studentRepository.hasPrerequisites("S001", "CS101")).thenReturn(true);

        service.register("S001", "CS101");

        ArgumentCaptor<String> bodyCaptor = ArgumentCaptor.forClass(String.class);
        verify(emailSender).sendEmail(
                eq("S001"), eq("Course Registration"), bodyCaptor.capture());

        assertTrue(bodyCaptor.getValue().contains("CS101"));
    }

    @ParameterizedTest
    @CsvSource({
            "S002, CS101, ERROR_NO_STUDENT",
            "S003, CS101, ERROR_INACTIVE_STUDENT",
            "S001, CS201, ERROR_PREREQUISITES"
    })
    void A8_errorScenarios(String studentId, String courseId, String expected) {
        if (studentId.equals("S002")) {
            when(studentRepository.findById(studentId)).thenReturn(null);
        } else if (studentId.equals("S003")) {
            when(studentRepository.findById(studentId))
                    .thenReturn(new Student("S003", "Bold", false));
        } else {
            when(studentRepository.findById(studentId)).thenReturn(activeStudent);
            when(studentRepository.hasPrerequisites(studentId, courseId)).thenReturn(false);
        }

        assertEquals(expected, service.register(studentId, courseId));
        verifyNoInteractions(emailSender);
    }
}
