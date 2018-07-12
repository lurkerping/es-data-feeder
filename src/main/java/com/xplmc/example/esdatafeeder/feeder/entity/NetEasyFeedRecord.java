package com.xplmc.example.esdatafeeder.feeder.entity;

import com.xplmc.example.esdatafeeder.common.entity.BaseEntity;
import lombok.*;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import java.io.Serializable;

/**
 * NetEasy feed record table
 *
 * @author luke
 */
@Entity
@NoArgsConstructor
@Data
@RequiredArgsConstructor(staticName = "of")
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = false)
public class NetEasyFeedRecord extends BaseEntity implements Serializable {

    private static final long serialVersionUID = 1154286229272794616L;

    /**
     * primary key, auto generated
     */
    @Id
    @GeneratedValue
    private Long recordId;

    /**
     * NetEasy feed file hash(SHA1)
     */
    @NonNull
    private String fileHash;

    /**
     * NetEasy feed file name
     */
    @NonNull
    private String fileName;

    /**
     * NetEasy feed file record count
     */
    @NonNull
    private Long recordCount;

    /**
     * NetEasy feeder code version
     */
    private String codeVersion;

    /**
     * NetEasy feed file cost
     */
    private Long costTime;

}
