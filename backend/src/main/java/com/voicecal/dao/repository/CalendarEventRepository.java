package com.voicecal.dao.repository;

import com.voicecal.common.enums.dao.EventStatus;
import com.voicecal.common.enums.dao.EventCategory;
import com.voicecal.dao.entity.CalendarEvent;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 * 日程事件数据访问接口，提供日程基础查询能力。
 */
public interface CalendarEventRepository extends JpaRepository<CalendarEvent, Long> {

    /**
     * 按状态查询日程，并按开始时间升序返回。
     *
     * @param status 日程状态
     * @return 指定状态的日程列表
     */
    List<CalendarEvent> findByStatusOrderByStartTimeAsc(EventStatus status);

    /**
     * 按状态和分类查询日程，并按开始时间升序返回。
     *
     * @param status 日程状态
     * @param category 日程分类
     * @return 指定状态和分类的日程列表
     */
    List<CalendarEvent> findByStatusAndCategoryOrderByStartTimeAsc(EventStatus status, EventCategory category);

    /**
     * 查询与指定时间范围有交集的日程。
     *
     * @param rangeStart 查询范围开始时间
     * @param rangeEnd 查询范围结束时间
     * @param status 日程状态
     * @return 与时间范围有交集的日程列表
     */
    @Query("""
            select event
            from CalendarEvent event
            where event.status = :status
              and event.startTime < :rangeEnd
              and event.endTime > :rangeStart
            order by event.startTime asc
            """)
    List<CalendarEvent> findOverlappingEvents(
            @Param("rangeStart") LocalDateTime rangeStart,
            @Param("rangeEnd") LocalDateTime rangeEnd,
            @Param("status") EventStatus status
    );

    /**
     * 查询与指定时间范围有交集的日程，并可排除指定日程 ID。
     *
     * @param rangeStart 查询范围开始时间
     * @param rangeEnd 查询范围结束时间
     * @param excludeEventId 需要排除的日程 ID，可为空
     * @param status 日程状态
     * @return 与时间范围有交集的日程列表
     */
    @Query("""
            select event
            from CalendarEvent event
            where event.status = :status
              and event.startTime < :rangeEnd
              and event.endTime > :rangeStart
              and (:excludeEventId is null or event.id <> :excludeEventId)
            order by event.startTime asc
            """)
    List<CalendarEvent> findOverlappingEventsExcludingEvent(
            @Param("rangeStart") LocalDateTime rangeStart,
            @Param("rangeEnd") LocalDateTime rangeEnd,
            @Param("excludeEventId") Long excludeEventId,
            @Param("status") EventStatus status
    );

    /**
     * 查询与指定时间点冲突的日程。
     *
     * @param pointTime 时间点
     * @param excludeEventId 需要排除的日程 ID，可为空
     * @param status 日程状态
     * @return 与时间点冲突的日程列表
     */
    @Query("""
            select event
            from CalendarEvent event
            where event.status = :status
              and (:excludeEventId is null or event.id <> :excludeEventId)
              and (
                    (event.startTime = :pointTime and event.endTime = :pointTime)
                    or (event.startTime <= :pointTime and event.endTime > :pointTime)
              )
            order by event.startTime asc
            """)
    List<CalendarEvent> findConflictsAtPoint(
            @Param("pointTime") LocalDateTime pointTime,
            @Param("excludeEventId") Long excludeEventId,
            @Param("status") EventStatus status
    );

    /**
     * 按关键词查询标题或描述匹配的日程。
     *
     * @param keyword 关键词
     * @param status 日程状态
     * @return 标题或描述包含关键词的日程列表
     */
    @Query("""
            select event
            from CalendarEvent event
            where event.status = :status
              and (
                    lower(event.title) like lower(concat('%', :keyword, '%'))
                    or lower(coalesce(event.description, '')) like lower(concat('%', :keyword, '%'))
              )
            order by event.startTime asc
            """)
    List<CalendarEvent> searchByKeyword(
            @Param("keyword") String keyword,
            @Param("status") EventStatus status
    );

    /**
     * 按 ID 和状态查询单个日程。
     *
     * @param id 日程 ID
     * @param status 日程状态
     * @return 符合条件的日程
     */
    Optional<CalendarEvent> findByIdAndStatus(Long id, EventStatus status);

    /**
     * 查询尚未触发提醒且未来开始的有效日程候选。
     *
     * @param status 日程状态
     * @param now 当前时间
     * @return 待判断提醒是否到期的日程候选列表
     */
    List<CalendarEvent> findByStatusAndReminderMinutesIsNotNullAndReminderTriggeredFalseAndStartTimeAfterOrderByStartTimeAsc(
            EventStatus status,
            LocalDateTime now
    );

    /**
     * 查询最近已经触发提醒的日程。
     *
     * @param status 日程状态
     * @param pageable 分页限制
     * @return 最近已提醒日程列表
     */
    List<CalendarEvent> findByStatusAndReminderTriggeredTrueAndRemindedAtIsNotNullOrderByRemindedAtDesc(
            EventStatus status,
            Pageable pageable
    );
}
