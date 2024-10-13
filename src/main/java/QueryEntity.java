import jakarta.persistence.*;
import lombok.*;

import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.time.LocalDateTime;

@Entity
@Table(name = "query_history")
@Getter
@Setter
@ToString
@EqualsAndHashCode
@AllArgsConstructor
@NoArgsConstructor
public class QueryEntity implements Serializable {

    @Id
    @NotNull
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(name = "user_name", columnDefinition = "varchar(255)")
    private String userName;

    @Column(columnDefinition = "varchar(255)")
    private String town;

    @Column(name = "query_date", columnDefinition = "datetime")
    private LocalDateTime dateAndTime;

    @Column(columnDefinition = "text")
    private String response;
}
