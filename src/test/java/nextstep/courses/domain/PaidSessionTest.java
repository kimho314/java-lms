package nextstep.courses.domain;

import nextstep.payments.domain.Payment;
import nextstep.users.domain.NsUserTest;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

public class PaidSessionTest {
    private Image image;
    private SessionDate sessionDate;
    private SessionId sessionId;

    @BeforeEach
    void init() {
        this.image = new Image(1L, new ImageSize(1024), ImageType.GIF, new ImagePixel(300L, 200L));

        LocalDateTime start = LocalDateTime.of(2024, 10, 10, 10, 10);
        LocalDateTime end = LocalDateTime.of(2024, 10, 10, 10, 11);

        this.sessionDate = new SessionDate(start, end);
        this.sessionId = SessionId.of(1L, "TDD");
    }

    @Test
    @DisplayName("PaidSession 생성")
    void createPaidSessionTest() {
        SessionCapacity sessionCapacity = new SessionCapacity(10);
        Money fee = new Money(200_000L);

        PaidSession paidSession = new PaidSession(image, sessionDate, sessionId, sessionCapacity, fee);

        Assertions.assertThat(paidSession).isNotNull();
        Assertions.assertThat(paidSession.getSessionId()).isEqualTo(sessionId);
    }

    @Test
    @DisplayName("PaidSession 수강 신청")
    void registerPaidSessionTest() {
        SessionCapacity sessionCapacity = new SessionCapacity(1);
        Money fee = new Money(200_000L);

        PaidSession paidSession = new PaidSession(image, sessionDate, sessionId, sessionCapacity, fee);

        paidSession.open();

        paidSession.register(RegistrationTest.REGISTRATION);

        Assertions.assertThat(paidSession.getSessionStatus()).isEqualTo(SessionStatus.RECRUITING);
        Assertions.assertThat(paidSession.getCapacity().getCurrentCount()).isEqualTo(1);
        Assertions.assertThat(paidSession.getRegistrations()).hasSize(1);
        Assertions.assertThat(paidSession.getRegistrations()).hasSameElementsAs(List.of(RegistrationTest.REGISTRATION));

    }

    @Test
    @DisplayName("PaidSession 수강 신청 - 걍의가 모집중이 아닐때 수간신청 체크")
    void checkRegisterNotOpenPaidSessionTest() {
        SessionCapacity sessionCapacity = new SessionCapacity(1);
        Money fee = new Money(200_000L);

        PaidSession paidSession = new PaidSession(image, sessionDate, sessionId, sessionCapacity, fee);

        Payment payment = new Payment("1", sessionId.getId(), NsUserTest.JAVAJIGI.getId(), fee.getPrice());

        Assertions.assertThatThrownBy(() -> paidSession.register(RegistrationTest.REGISTRATION))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("PaidSession 수강 신청 - 결제 데이터 null 체크")
    void checkRegisterPaidSessionTest_WithNullPayment() {
        SessionCapacity sessionCapacity = new SessionCapacity(1);
        Money fee = new Money(200_000L);

        PaidSession paidSession = new PaidSession(image, sessionDate, sessionId, sessionCapacity, fee);
        paidSession.open();

        Assertions.assertThatThrownBy(() -> paidSession.register(null))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("PaidSession 수강 신청 - 강의 가격보다 낮은 금액 결제 체크")
    void checkRegisterPaidSessionTest_WithLowerPayment() {
        SessionCapacity sessionCapacity = new SessionCapacity(1);
        Money fee = new Money(200_000L);

        PaidSession paidSession = new PaidSession(image, sessionDate, sessionId, sessionCapacity, fee);
        paidSession.open();
        Payment payment = new Payment("1", sessionId.getId(), NsUserTest.JAVAJIGI.getId(), fee.getPrice() - 1);
        Registration registration = new Registration(1L, NsUserTest.JAVAJIGI, payment);

        Assertions.assertThatThrownBy(() -> paidSession.register(registration))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("PaidSession 수강 신청 - 강의 가격보다 높은 금액 결제 체크")
    void checkRegisterPaidSessionTest_WithAbovePayment() {
        SessionCapacity sessionCapacity = new SessionCapacity(1);
        Money fee = new Money(200_000L);

        PaidSession paidSession = new PaidSession(image, sessionDate, sessionId, sessionCapacity, fee);
        paidSession.open();
        Payment payment = new Payment("1", sessionId.getId(), NsUserTest.JAVAJIGI.getId(), fee.getPrice() + 1);
        Registration registration = new Registration(1L, NsUserTest.JAVAJIGI, payment);

        Assertions.assertThatThrownBy(() -> paidSession.register(registration))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("강의 모집중")
    void openPaidSessionTest() {
        SessionCapacity sessionCapacity = new SessionCapacity(1);
        Money fee = new Money(200_000L);

        PaidSession paidSession = new PaidSession(image, sessionDate, sessionId, sessionCapacity, fee);

        paidSession.open();

        Assertions.assertThat(paidSession.getSessionStatus()).isEqualTo(SessionStatus.RECRUITING);
    }

    @Test
    @DisplayName("강의 종료")
    void closePaidSessionTest() {
        SessionCapacity sessionCapacity = new SessionCapacity(1);
        Money fee = new Money(200_000L);

        PaidSession paidSession = new PaidSession(image, sessionDate, sessionId, sessionCapacity, fee);

        paidSession.close();

        Assertions.assertThat(paidSession.getSessionStatus()).isEqualTo(SessionStatus.CLOSE);
    }
}
