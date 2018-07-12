package com.xplmc.example.esdatafeeder.feeder.repository;

import com.xplmc.example.esdatafeeder.feeder.entity.NetEasyFeedRecord;
import org.springframework.data.repository.PagingAndSortingRepository;

/**
 * NetEasy feed record table repository class
 *
 * @author luke
 */
public interface NetEasyFeedRecordRepository extends PagingAndSortingRepository<NetEasyFeedRecord, Long> {

    /**
     * find by file hash
     *
     * @param fileHash file hash using SHA1 checksum
     * @return NetEasyFeedRecord
     */
    Iterable<NetEasyFeedRecord> findByFileHash(String fileHash);

}
