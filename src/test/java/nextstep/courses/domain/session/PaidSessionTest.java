package nextstep.courses.domain.session;

import nextstep.courses.domain.image.Image;
import nextstep.courses.domain.image.ImagePixel;
import nextstep.courses.domain.image.ImageSize;
import nextstep.courses.domain.image.ImageType;
import nextstep.courses.domain.lecturer.LecturerTest;
import nextstep.courses.domain.student.Student;
import nextstep.courses.domain.student.StudentStatus;
import nextstep.payments.domain.Payment;
import nextstep.users.domain.NsUserTest;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class PaidSessionTest {
    private Image image;
    private SessionDate sessionDate;
    private Long id;
    private String title;

    @BeforeEach
    void init() {
        this.image = new Image(1L, new ImageSize(1024), ImageType.GIF, new ImagePixel(300, 200));

        LocalDateTime start = LocalDateTime.of(2024, 10, 10, 10, 10);
        LocalDateTime end = LocalDateTime.of(2024, 10, 10, 10, 11);

        this.sessionDate = new SessionDate(start, end);
        this.id = 1L;
        this.title = "TDD";
    }

    @Test
    @DisplayName("PaidSession 생성")
    void createPaidSessionTest() {
        SessionCapacity sessionCapacity = new SessionCapacity(10);
        Money fee = new Money(200_000L);

        PaidSession paidSession = new PaidSession(id, title, new ArrayList<>(List.of(image)), sessionDate, sessionCapacity, fee);

        Assertions.assertThat(paidSession).isNotNull();
        Assertions.assertThat(paidSession.getId()).isEqualTo(id);
    }

    @Test
    @DisplayName("PaidSession 생성 - 강사 추가")
    void createPaidSessionTest_WithLecturer() {
        SessionCapacity sessionCapacity = new SessionCapacity(10);
        Money fee = new Money(200_000L);

        PaidSession paidSession = new PaidSession(id, title, new ArrayList<>(List.of(image)), sessionDate, sessionCapacity, fee);
        paidSession.assignLecturer(LecturerTest.testLecturer);

        Assertions.assertThat(paidSession).isNotNull();
        Assertions.assertThat(paidSession.getId()).isEqualTo(id);
        Assertions.assertThat(paidSession.getLecturer()).isEqualTo(LecturerTest.testLecturer);
    }

    @Test
    @DisplayName("PaidSession 수강 신청")
    void registerPaidSessionTest() {
        SessionCapacity sessionCapacity = new SessionCapacity(1);
        Money fee = new Money(200_000L);

        PaidSession paidSession = new PaidSession(id, title, new ArrayList<>(List.of(image)), sessionDate, sessionCapacity, fee);
        paidSession.assignLecturer(LecturerTest.testLecturer);

        paidSession.open();

        paidSession.register(RegistrationTest.REGISTRATION);

        Assertions.assertThat(paidSession.getStudents()).hasSize(1);
        Assertions.assertThat(paidSession.getStudents()).hasSameElementsAs(List.of(Student.of(RegistrationTest.REGISTRATION)));

    }

    @Test
    @DisplayName("PaidSession 학생 선발")
    void acceptStudentsPaidSessionTest() {
        SessionCapacity sessionCapacity = new SessionCapacity(2);
        Money fee = new Money(200_000L);
        List<Student> students = new ArrayList<>(Student.of(List.of(RegistrationTest.REGISTRATION, RegistrationTest.REGISTRATION2)));
        PaidSession paidSession = new PaidSession(id, title, new ArrayList<>(List.of(image)), sessionDate, sessionCapacity, fee);

        paidSession.assignLecturer(LecturerTest.testLecturer);

        paidSession.open();

        paidSession.register(RegistrationTest.REGISTRATION);
        paidSession.register(RegistrationTest.REGISTRATION2);

        paidSession.acceptStudents(LecturerTest.testLecturer, List.of(students.get(0)));
        paidSession.rejectStudents(LecturerTest.testLecturer, List.of(students.get(1)));

        Assertions.assertThat(paidSession.getStudents()).hasSize(2);

        for (Student student : paidSession.getStudents()) {
            int index = students.indexOf(student);
            Assertions.assertThat(student.getNsUserId()).isEqualTo(students.get(index).getNsUserId());
            if (index == 0) {
                Assertions.assertThat(student.getStatus()).isEqualTo(StudentStatus.ACCEPTED);
                continue;
            }
            Assertions.assertThat(student.getStatus()).isEqualTo(StudentStatus.REJECTED);
        }
    }

    @Test
    @DisplayName("PaidSession 수강 신청 - 강의가 모집중이 아닐때 수간신청 체크")
    void checkRegisterNotOpenPaidSessionTest() {
        SessionCapacity sessionCapacity = new SessionCapacity(1);
        Money fee = new Money(200_000L);

        PaidSession paidSession = new PaidSession(id, title, new ArrayList<>(List.of(image)), sessionDate, sessionCapacity, fee);
        paidSession.assignLecturer(LecturerTest.testLecturer);

        Assertions.assertThatThrownBy(() -> paidSession.register(RegistrationTest.REGISTRATION))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("PaidSession 수강 신청 - 결제 데이터 null 체크")
    void checkRegisterPaidSessionTest_WithNullPayment() {
        SessionCapacity sessionCapacity = new SessionCapacity(1);
        Money fee = new Money(200_000L);

        PaidSession paidSession = new PaidSession(id, title, new ArrayList<>(List.of(image)), sessionDate, sessionCapacity, fee);
        paidSession.assignLecturer(LecturerTest.testLecturer);

        paidSession.open();

        Assertions.assertThatThrownBy(() -> paidSession.register(null))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("PaidSession 수강 신청 - 강의 가격보다 낮은 금액 결제 체크")
    void checkRegisterPaidSessionTest_WithLowerPayment() {
        SessionCapacity sessionCapacity = new SessionCapacity(1);
        Money fee = new Money(200_000L);

        PaidSession paidSession = new PaidSession(id, title, new ArrayList<>(List.of(image)), sessionDate, sessionCapacity, fee);
        paidSession.assignLecturer(LecturerTest.testLecturer);

        paidSession.open();

        Payment payment = new Payment("1", id, NsUserTest.JAVAJIGI.getId(), fee.getPrice() - 1);
        Registration registration = new Registration(1L, NsUserTest.JAVAJIGI, payment);

        Assertions.assertThatThrownBy(() -> paidSession.register(registration))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("PaidSession 수강 신청 - 강의 가격보다 높은 금액 결제 체크")
    void checkRegisterPaidSessionTest_WithAbovePayment() {
        SessionCapacity sessionCapacity = new SessionCapacity(1);
        Money fee = new Money(200_000L);

        PaidSession paidSession = new PaidSession(id, title, new ArrayList<>(List.of(image)), sessionDate, sessionCapacity, fee);
        paidSession.assignLecturer(LecturerTest.testLecturer);
        paidSession.open();
        Payment payment = new Payment("1", id, NsUserTest.JAVAJIGI.getId(), fee.getPrice() + 1);
        Registration registration = new Registration(1L, NsUserTest.JAVAJIGI, payment);

        Assertions.assertThatThrownBy(() -> paidSession.register(registration))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("강의 모집중")
    void openPaidSessionTest() {
        SessionCapacity sessionCapacity = new SessionCapacity(1);
        Money fee = new Money(200_000L);

        PaidSession paidSession = new PaidSession(id, title, new ArrayList<>(List.of(image)), sessionDate, sessionCapacity, fee);
        paidSession.assignLecturer(LecturerTest.testLecturer);

        paidSession.open();

        Assertions.assertThat(paidSession.getSessionStatus()).isEqualTo(new SessionStatus(SessionProgressStatus.ON_GOING, SessionRecruitStatus.RECRUITMENT));
    }

    @Test
    @DisplayName("강의 종료")
    void closePaidSessionTest() {
        SessionCapacity sessionCapacity = new SessionCapacity(1);
        Money fee = new Money(200_000L);

        PaidSession paidSession = new PaidSession(id, title, new ArrayList<>(List.of(image)), sessionDate, sessionCapacity, fee);
        paidSession.assignLecturer(LecturerTest.testLecturer);

        paidSession.close();

        Assertions.assertThat(paidSession.getSessionStatus()).isEqualTo(new SessionStatus(SessionProgressStatus.END, SessionRecruitStatus.NON_RECRUITMENT));
    }
}
