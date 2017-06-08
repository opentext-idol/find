package com.hp.autonomy.frontend.find.core.savedsearches;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Type;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import java.util.UUID;

/**
 * Entity for the Users table prior to FIND-969
 *
 * @deprecated Only used for migrations. Use UserEntity instead
 */
@Entity
@Table(name = OldUserEntity.Table.NAME)
@Data
@Builder(toBuilder = true)
@EqualsAndHashCode(exclude = "userId")
@NoArgsConstructor
@AllArgsConstructor
@Deprecated
public class OldUserEntity {
    @Id
    @Column(name = OldUserEntity.Table.Column.USER_ID)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long userId;

    @Column(name = OldUserEntity.Table.Column.USER_STORE)
    private String userStore;

    private String domain;
    private Long uid;
    private String username;

    // Need to specify char-style UUID for Maria DB, otherwise hibernate tries to send binary
    @Type(type = "uuid-char")
    private UUID uuid;

    public interface Table {
        String NAME = "users";

        @SuppressWarnings("InnerClassTooDeeplyNested")
        interface Column {
            String USER_ID = "user_id";
            String USER_STORE = "user_store";
        }
    }
}
