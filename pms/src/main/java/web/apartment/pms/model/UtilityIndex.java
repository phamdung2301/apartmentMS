package web.apartment.pms.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "utility_indices")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UtilityIndex {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "room_id", nullable = false)
    private Room room;

    @Column(name = "`month`", nullable = false)
    private Integer month;

    @Column(name = "`year`", nullable = false)
    private Integer year;

    @Column(name = "old_elec", nullable = false)
    @Builder.Default
    private Integer oldElec = 0;

    @Column(name = "new_elec", nullable = false)
    @Builder.Default
    private Integer newElec = 0;

    @Column(name = "old_water", nullable = false)
    @Builder.Default
    private Integer oldWater = 0;

    @Column(name = "new_water", nullable = false)
    @Builder.Default
    private Integer newWater = 0;

    @Column(name = "created_at", nullable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();
}
