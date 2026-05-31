package com.voicecal.dao.entity;

import com.voicecal.common.enums.dao.EventCategory;
import com.voicecal.common.enums.dao.EventStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;

/**
 * 日程事件实体，映射用户在日历中创建的单个日程记录。
 */
@Entity
@Table(
        name = "calendar_event",
        indexes = {
                @Index(name = "idx_calendar_event_start_time", columnList = "start_time"),
                @Index(name = "idx_calendar_event_end_time", columnList = "end_time"),
                @Index(name = "idx_calendar_event_status", columnList = "status"),
                @Index(name = "idx_calendar_event_category", columnList = "category"),
                @Index(name = "idx_calendar_event_created_at", columnList = "created_at")
        }
)
public class CalendarEvent {

    public static final String DEFAULT_TIMEZONE = "Asia/Shanghai";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 日程标题，用于列表展示、搜索和语音确认。
     */
    @NotBlank(message = "日程标题不能为空")
    @Size(max = 100, message = "日程标题不能超过 100 个字符")
    @Column(name = "title", nullable = false, length = 100)
    private String title;

    /**
     * 日程描述，保存用户补充的详细说明。
     */
    @Size(max = 1000, message = "日程描述不能超过 1000 个字符")
    @Column(name = "description", length = 1000)
    private String description;

    /**
     * 日程开始时间，后续 Service 层负责校验其早于结束时间。
     */
    @NotNull(message = "开始时间不能为空")
    @Column(name = "start_time", nullable = false)
    private LocalDateTime startTime;

    /**
     * 日程结束时间，后续 Service 层负责校验其不早于开始时间。
     */
    @NotNull(message = "结束时间不能为空")
    @Column(name = "end_time", nullable = false)
    private LocalDateTime endTime;

    /**
     * 日程所属时区，默认使用 Asia/Shanghai。
     */
    @Size(max = 50, message = "时区不能超过 50 个字符")
    @Column(name = "timezone", nullable = false, length = 50)
    private String timezone;

    /**
     * 提前提醒分钟数，空值表示暂不提醒。
     */
    @Column(name = "reminder_minutes")
    private Integer reminderMinutes;

    /**
     * 提醒是否已经触发，避免同一个日程重复触发提醒。
     */
    @Column(name = "reminder_triggered", nullable = false)
    private Boolean reminderTriggered;

    /**
     * 提醒实际触发时间，未触发时为空。
     */
    @Column(name = "reminded_at")
    private LocalDateTime remindedAt;

    /**
     * 日程地点，可用于后续地图、会议室或出行提醒扩展。
     */
    @Size(max = 255, message = "地点不能超过 255 个字符")
    @Column(name = "location", length = 255)
    private String location;

    /**
     * 日程分类，默认 OTHER。
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "category", nullable = false, length = 30)
    private EventCategory category;

    /**
     * 日程状态，用于区分有效、取消或已删除的日程。
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    private EventStatus status;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    public CalendarEvent() {
    }

    /**
     * 持久化前初始化默认字段和审计时间。
     */
    @PrePersist
    public void prePersist() {
        LocalDateTime now = now();
        createdAt = now;
        updatedAt = now;
        applyDefaults();
    }

    /**
     * 更新前刷新更新时间并补齐默认字段。
     */
    @PreUpdate
    public void preUpdate() {
        updatedAt = now();
        applyDefaults();
    }

    private static LocalDateTime now() {
        return LocalDateTime.now();
    }

    private void applyDefaults() {
        if (timezone == null || timezone.isBlank()) {
            timezone = DEFAULT_TIMEZONE;
        }
        if (category == null) {
            category = EventCategory.OTHER;
        }
        if (status == null) {
            status = EventStatus.ACTIVE;
        }
        if (reminderTriggered == null) {
            reminderTriggered = false;
        }
    }

    public Long getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }

    public LocalDateTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalDateTime endTime) {
        this.endTime = endTime;
    }

    public String getTimezone() {
        return timezone;
    }

    public void setTimezone(String timezone) {
        this.timezone = timezone;
    }

    public Integer getReminderMinutes() {
        return reminderMinutes;
    }

    public void setReminderMinutes(Integer reminderMinutes) {
        this.reminderMinutes = reminderMinutes;
    }

    public Boolean getReminderTriggered() {
        return reminderTriggered;
    }

    public void setReminderTriggered(Boolean reminderTriggered) {
        this.reminderTriggered = reminderTriggered;
    }

    public LocalDateTime getRemindedAt() {
        return remindedAt;
    }

    public void setRemindedAt(LocalDateTime remindedAt) {
        this.remindedAt = remindedAt;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public EventCategory getCategory() {
        return category;
    }

    public void setCategory(EventCategory category) {
        this.category = category;
    }

    public EventStatus getStatus() {
        return status;
    }

    public void setStatus(EventStatus status) {
        this.status = status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
}
