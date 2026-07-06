package com.smarthotel.housekeeping_service.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "cleaning_tasks")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CleaningTask {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "room_id", nullable = false)
    private UUID roomId;

    @Column(name = "staff_id")
    private UUID staffId; // Nhân viên buồng phòng nhận việc (có thể NULL khi mới tạo)

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 30, nullable = false)
    private CleaningTaskStatus status;

    @Column(name = "assigned_at")
    private LocalDateTime assignedAt;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @Version
    private Long version;
}