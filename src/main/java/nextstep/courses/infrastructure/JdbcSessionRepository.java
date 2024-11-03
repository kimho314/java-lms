package nextstep.courses.infrastructure;

import nextstep.courses.domain.session.*;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcOperations;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Repository
public class JdbcSessionRepository implements SessionRepository {
    private static final RowMapper<Session> COURSE_ROW_MAPPER = (rs, rowNum) -> {
        long sessionId = rs.getLong(1);
        String title = rs.getString(2);
        Timestamp startAt = rs.getTimestamp(3);
        Timestamp endAt = rs.getTimestamp(4);
        SessionDate date = new SessionDate(startAt.toLocalDateTime(), endAt.toLocalDateTime());
        SessionType type = SessionType.of(rs.getString(5));
        SessionStatus status = SessionStatus.valueOf(rs.getString(6));
        if (type.equals(SessionType.FREE)) {
            return new FreeSession(null, date, sessionId, title, status, type, new ArrayList<>());
        }

        SessionCapacity capacity = new SessionCapacity(rs.getInt(7));
        Money money = new Money(rs.getLong(8));
        return new PaidSession(null, date, sessionId, title, status, type, capacity, money, new ArrayList<>());
    };

    private NamedParameterJdbcOperations namedParameterJdbcTemplate;

    public JdbcSessionRepository(NamedParameterJdbcOperations namedParameterJdbcTemplate) {
        this.namedParameterJdbcTemplate = namedParameterJdbcTemplate;
    }

    @Override
    public long save(Session session, Long courseId) {
        String sql = "insert into session (course_id, title, start_at, end_at, session_type, session_status, capacity, price,  created_at) " +
                "values(:courseId, :title, :startAt, :endAt, :sessionType, :sessionStatus, :capacity, :price, :createdAt)";

        MapSqlParameterSource param = getParam(courseId, session);
        GeneratedKeyHolder keyHolder = new GeneratedKeyHolder();

        namedParameterJdbcTemplate.update(sql, param, keyHolder);

        return Objects.requireNonNull(keyHolder.getKey()).longValue();
    }

    private MapSqlParameterSource getParam(Long courseId, Session session) {
        MapSqlParameterSource param = new MapSqlParameterSource();
        param.addValue("courseId", courseId);
        param.addValue("title", session.getTitle());
        param.addValue("startAt", session.getSessionDate().getStart());
        param.addValue("endAt", session.getSessionDate().getEnd());
        param.addValue("sessionType", session.getSessionType().name());
        param.addValue("sessionStatus", session.getSessionStatus().name());
        param.addValue("createdAt", LocalDateTime.now());

        if (session instanceof FreeSession) {
            param.addValue("capacity", null);
            param.addValue("price", null);
            return param;
        }

        PaidSession paidSession = (PaidSession) session;
        param.addValue("capacity", paidSession.getCapacity().getCapacity());
        param.addValue("price", paidSession.getFee().getPrice());
        return param;
    }

    @Override
    public Optional<Session> findById(Long id) {
        String sql = "select id, title, start_at, end_at, session_type, session_status, capacity, price from session where id = :id";
        MapSqlParameterSource param = new MapSqlParameterSource();
        param.addValue("id", id);
        return Optional.ofNullable(namedParameterJdbcTemplate.queryForObject(sql, param, COURSE_ROW_MAPPER));
    }

    @Override
    public List<Session> findAllByCourseId(Long courseId) {
        String sql = "select id, title, start_at, end_at, session_type, session_status, capacity, price from session where course_id = :courseId";
        MapSqlParameterSource param = new MapSqlParameterSource();
        param.addValue("courseId", courseId);
        return namedParameterJdbcTemplate.query(sql, param, COURSE_ROW_MAPPER);
    }
}