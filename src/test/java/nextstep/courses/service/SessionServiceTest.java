package nextstep.courses.service;

import nextstep.courses.domain.image.*;
import nextstep.courses.domain.lecturer.LecturerRepository;
import nextstep.courses.domain.lecturer.LecturerTest;
import nextstep.courses.domain.session.*;
import nextstep.courses.domain.student.Student;
import nextstep.courses.domain.student.StudentRepository;
import nextstep.courses.domain.student.StudentStatus;
import nextstep.payments.domain.Payment;
import nextstep.users.domain.NsUser;
import nextstep.users.domain.NsUserTest;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class SessionServiceTest {
    @InjectMocks
    SessionService sessionService;
    @Mock
    SessionRepository sessionRepository;
    @Mock
    ImageRepository imageRepository;
    @Mock
    StudentRepository studentRepository;
    @Mock
    LecturerRepository lecturerRepository;

    private Session session;
    private Image image;
    private SessionDate sessionDate;
    private SessionCapacity sessionCapacity;
    private String title = "TDD";
    private Money fee = new Money(200_000L);

    @BeforeEach
    void init() {
        image = new Image(new ImageSize(1024), ImageType.GIF, new ImagePixel(300, 200));

        LocalDateTime start = LocalDateTime.of(2024, 10, 10, 10, 10);
        LocalDateTime end = LocalDateTime.of(2024, 10, 10, 10, 11);
        sessionDate = new SessionDate(start, end);
        sessionCapacity = new SessionCapacity(10);

        session = new PaidSession(title, new ArrayList<>(List.of(image)), sessionDate, sessionCapacity, fee);
        session.assignLecturer(LecturerTest.testLecturer);
    }

    @Test
    void createSessionTest() {
        Long courseId = 1L;
        long sessionId = sessionService.create(courseId, session, session.getImages(), LecturerTest.testLecturer);

        verify(sessionRepository).save(session, courseId);
        verify(imageRepository).saveAll(session.getImages(), sessionId);
        verify(lecturerRepository).save(LecturerTest.testLecturer, sessionId);
    }


    @Test
    void registerTest() {
        Long sessionId = 2L;
        NsUser nsUser = NsUserTest.JAVAJIGI;
        Long amount = 200_000L;
        Payment payment = new Payment("1234", sessionId, nsUser.getId(), amount);
        PaidSession paidSession = new PaidSession(sessionId, title, new ArrayList<>(List.of(image)), sessionDate, sessionCapacity, fee);

        Student student = new Student(amount, nsUser.getId());
        Student student2 = new Student(amount, NsUserTest.SANJIGI.getId());

        mockSetup(paidSession, student2);

        paidSession.open();


        PaidSession registerSession = (PaidSession) sessionService.register(sessionId, nsUser, payment);


        assertThat(registerSession.getId()).isEqualTo(paidSession.getId());
        assertThat(registerSession.getStudents()).hasSameElementsAs(List.of(student, student2));
        assertThat(registerSession.getFee().getPrice()).isEqualTo(amount);
    }

    @Test
    void acceptStudentsTest() {
        Long sessionId = 2L;
        NsUser nsUser = NsUserTest.JAVAJIGI;
        Long amount = 200_000L;
        Payment payment = new Payment("1234", sessionId, nsUser.getId(), amount);
        Payment payment2 = new Payment("1235", sessionId, NsUserTest.SANJIGI.getId(), amount);
        PaidSession paidSession = new PaidSession(sessionId, title, new ArrayList<>(List.of(image)), sessionDate, sessionCapacity, fee);
        paidSession.assignLecturer(LecturerTest.testLecturer);

        Student student = new Student(amount, nsUser.getId());
        Student student2 = new Student(amount, NsUserTest.SANJIGI.getId());
        List<Student> students = Arrays.asList(student, student2);

        paidSession.open();
        paidSession.register(Registration.of(sessionId, nsUser, payment));
        paidSession.register(Registration.of(sessionId, NsUserTest.SANJIGI, payment2));

        mockSetup(paidSession, student2);

        Session accept = sessionService.accept(sessionId, LecturerTest.testLecturer, students);

        for (Student elem : accept.getStudents()) {
            Assertions.assertThat(elem.getStatus()).isEqualTo(StudentStatus.ACCEPTED);
        }
    }

    private void mockSetup(PaidSession paidSession, Student student) {
        when(sessionRepository.findById(anyLong())).thenReturn(Optional.of(paidSession));
        when(imageRepository.findAllBySessionId(anyLong())).thenReturn(new ArrayList<>(List.of(image)));
        when(studentRepository.findAllBySessionId(anyLong())).thenReturn(new ArrayList<>(List.of(student)));
        when(lecturerRepository.findBySessionId(anyLong())).thenReturn(Optional.of(LecturerTest.testLecturer));
        when(studentRepository.saveAll(anyList(), anyLong())).thenReturn(new int[]{1});
    }

    @Test
    void rejectStudentsTest() {
        Long sessionId = 2L;
        NsUser nsUser = NsUserTest.JAVAJIGI;
        Long amount = 200_000L;
        Payment payment = new Payment("1234", sessionId, nsUser.getId(), amount);
        Payment payment2 = new Payment("1235", sessionId, NsUserTest.SANJIGI.getId(), amount);
        PaidSession paidSession = new PaidSession(sessionId, title, new ArrayList<>(List.of(image)), sessionDate, sessionCapacity, fee);
        paidSession.assignLecturer(LecturerTest.testLecturer);

        Student student = new Student(amount, nsUser.getId());
        Student student2 = new Student(amount, NsUserTest.SANJIGI.getId());
        List<Student> students = Arrays.asList(student, student2);

        paidSession.open();
        paidSession.register(Registration.of(sessionId, nsUser, payment));
        paidSession.register(Registration.of(sessionId, NsUserTest.SANJIGI, payment2));

        mockSetup(paidSession, student2);

        Session accept = sessionService.reject(sessionId, LecturerTest.testLecturer, students);

        for (Student elem : accept.getStudents()) {
            Assertions.assertThat(elem.getStatus()).isEqualTo(StudentStatus.REJECTED);
        }
    }

    @Test
    void findBySessionId() {
        Long sessionId = 2L;
        NsUser nsUser = NsUserTest.JAVAJIGI;
        Long amount = 200_000L;
        PaidSession paidSession = new PaidSession(sessionId, title, new ArrayList<>(List.of(image)), sessionDate, sessionCapacity, fee);
        paidSession.open();

        Student student = new Student(amount, nsUser.getId());
        Student student2 = new Student(amount, NsUserTest.SANJIGI.getId());

        when(sessionRepository.findById(anyLong())).thenReturn(Optional.of(paidSession));
        when(imageRepository.findAllBySessionId(anyLong())).thenReturn(new ArrayList<>(List.of(image)));
        when(studentRepository.findAllBySessionId(anyLong())).thenReturn(Arrays.asList(student, student2));
        when(lecturerRepository.findBySessionId(anyLong())).thenReturn(Optional.of(LecturerTest.testLecturer));

        Session foundSession = sessionService.findById(sessionId);

        assertThat(foundSession.getStudents()).hasSameElementsAs(Arrays.asList(student, student2));
        assertThat(foundSession.getId()).isEqualTo(sessionId);
        assertThat(foundSession.getImages()).hasSameElementsAs(List.of(image));
        assertThat(foundSession.getSessionType()).isEqualTo(SessionType.PAID);
    }
}
