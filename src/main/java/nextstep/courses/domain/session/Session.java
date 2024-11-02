package nextstep.courses.domain.session;

import nextstep.courses.domain.image.Image;
import nextstep.courses.domain.student.Student;

import java.util.List;

public abstract class Session {
    private Long id;
    private String title;
    private SessionDate sessionDate;
    private Image image;
    private SessionType sessionType;
    private SessionStatus sessionStatus;
    private List<Student> students;

    public Session(Image image,
                   SessionDate sessionDate,
                   Long id,
                   String title,
                   SessionStatus sessionStatus,
                   SessionType sessionType,
                   List<Student> students
    ) {
        this.image = image;
        this.sessionDate = sessionDate;
        this.id = id;
        this.title = title;
        this.sessionStatus = sessionStatus;
        this.sessionType = sessionType;
        this.students = students;
    }

    public Long getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public SessionDate getSessionDate() {
        return sessionDate;
    }

    public Image getImage() {
        return image;
    }

    public SessionType getSessionType() {
        return sessionType;
    }

    public List<Student> getStudents() {
        return students;
    }

    public void addStudent(Student student) {
        this.students.add(student);
    }

    public abstract void register(Registration registration);

    public void open() {
        updateStatus(SessionStatus.RECRUITING);
    }

    public void close() {
        updateStatus(SessionStatus.CLOSE);
    }

    protected boolean isAvailableForRegistration() {
        return this.sessionStatus.isOpen();
    }

    protected void updateStatus(SessionStatus sessionStatus) {
        this.sessionStatus = sessionStatus;
    }

    public SessionStatus getSessionStatus() {
        return sessionStatus;
    }

    @Override
    public final boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Session)) {
            return false;
        }

        Session session = (Session) o;
        return id.equals(session.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }
}
