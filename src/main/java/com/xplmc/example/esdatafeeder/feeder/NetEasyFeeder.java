package com.xplmc.example.esdatafeeder.feeder;

import com.xplmc.example.esdatafeeder.common.util.SimpleHashCalcUtils;
import com.xplmc.example.esdatafeeder.feeder.entity.NetEasyFeedRecord;
import com.xplmc.example.esdatafeeder.feeder.repository.NetEasyFeedRecordRepository;
import org.elasticsearch.action.bulk.BulkRequestBuilder;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.client.transport.TransportClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.*;

/**
 * feed NetEasy user and pwd file into Elasticsearch
 *
 * @author luke
 */
@Component
public class NetEasyFeeder {

    private static final Logger logger = LoggerFactory.getLogger(NetEasyFeeder.class);
    private static final String TXT_SUFFIX_LOWER_CASE = ".txt";
    private static final String TXT_SUFFIX_UPPER_CASE = ".TXT";
    private static final String USER_PWD_DELIMITER = "----";
    private static final String INDEX_NAME = "net_easy";
    private static final String TYPE_NAME = "user_pwd";

    /**
     * bulk commit size
     */
    private static final int BULK_SIZE = 5000;

    /**
     * NetEasy feeder code version
     */
    private static final String CODE_VERSION = "20180712 v1, bulk size: " + BULK_SIZE;

    @Value("${feeder.root-file-path}")
    private String rootFilePath = null;

    private TransportClient client = null;

    private NetEasyFeedRecordRepository netEasyFeedRecordRepository = null;

    public NetEasyFeeder(TransportClient client, NetEasyFeedRecordRepository netEasyFeedRecordRepository) {
        this.client = client;
        this.netEasyFeedRecordRepository = netEasyFeedRecordRepository;
    }


    /**
     * feed NetEasy user and pwd file into Elasticsearch
     */
    public void feed() throws IOException {

        Path path = Paths.get(rootFilePath);
        //walk the directory tree, feed any txt/TXT file
        Files.walkFileTree(path, new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                if (file.toString().endsWith(TXT_SUFFIX_LOWER_CASE) || file.toString().endsWith(TXT_SUFFIX_UPPER_CASE)) {
                    logger.info("txt file, begin feed, file name: {}", file.getFileName());
                    //feed a single file
                    doFeed(file);
                } else {
                    logger.info("not a txt file, ignore, file name: {}", file.getFileName());
                }
                return super.visitFile(file, attrs);
            }
        });
    }

    /**
     * fee a single file into Elasticsearch
     * it the file is already fed, ignore
     * inserting a NetEasyFeedRecord when finished
     *
     * @param file file path
     */
    private void doFeed(Path file) {
        //calculate the file's SHA1 checksum
        String sha1sum = SimpleHashCalcUtils.sha1sum(file.toFile());
        if (sha1sum == null) {
            logger.warn("error when calculating file's hash, ignore, fileName: " + file);
            return;
        }
        //check if the file is already fed
        Iterable<NetEasyFeedRecord> netEasyFeedRecordIterable = netEasyFeedRecordRepository.findByFileHash(sha1sum);
        if (netEasyFeedRecordIterable.iterator().hasNext()) {
            logger.info("file is already fed，ignore, fileName: " + file);
            return;
        }

        //start time
        long start = System.currentTimeMillis();
        //begin feeding
        Long count = doRealFeed(file);

        if (count == null) {
            logger.info("fed failed, fileName: " + file);
        } else {
            //fed finished, insert a NetEasyFeedRecord
            NetEasyFeedRecord netEasyFeedRecord = new NetEasyFeedRecord();
            netEasyFeedRecord.setFileHash(sha1sum);
            netEasyFeedRecord.setFileName(file.toString());
            netEasyFeedRecord.setRecordCount(count);
            netEasyFeedRecord.setCodeVersion(CODE_VERSION);
            netEasyFeedRecord.setCostTime(System.currentTimeMillis() - start);
            netEasyFeedRecordRepository.save(netEasyFeedRecord);
        }

    }

    /**
     * do the real feeding job
     *
     * @param file file path
     * @return record count that's successfully fed，null when something bad happened
     */
    private Long doRealFeed(Path file) {
        //keep count
        long count = 0;

        //temp array list for bulk operation
        List<Map<String, String>> tempList = new ArrayList<>();

        try (BufferedReader br = new BufferedReader(new FileReader(file.toFile()))) {

            String line;
            while ((line = br.readLine()) != null) {
                try {
                    //split by ----
                    String[] userPwdArray = line.split(USER_PWD_DELIMITER);

                    //check line format
                    if (userPwdArray.length != 2) {
                        logger.info("wrong format, line: {}, content: {}, will ignore", count, line);
                    } else {
                        //index operation
                        Map<String, String> userPwdMap = new HashMap<>(BULK_SIZE * 2);
                        userPwdMap.put("username", userPwdArray[0]);
                        userPwdMap.put("password", userPwdArray[1]);
                        tempList.add(userPwdMap);
                        count++;
                    }

                    //commit
                    if (tempList.size() >= BULK_SIZE) {
                        BulkRequestBuilder bulkRequestBuilder = client.prepareBulk();

                        //add bulk request
                        Iterator<Map<String, String>> userPwdIterator = tempList.iterator();
                        while (userPwdIterator.hasNext()) {
                            bulkRequestBuilder.add(client.prepareIndex(INDEX_NAME, TYPE_NAME).setSource(userPwdIterator.next()));
                            userPwdIterator.remove();
                        }

                        //print bulk result
                        BulkResponse bulkResponse = bulkRequestBuilder.get();
                        if (bulkResponse.hasFailures()) {
                            logger.error("bulk has failures, ignore");
                        } else {
                            logger.info("bulk success, size: {}, current count: {}", BULK_SIZE, count);
                        }
                    }
                } catch (Exception ie) {
                    logger.info("error when parsing line: {}, content: {}, will ignore", count, line, ie);
                }

            }
            return count;
        } catch (FileNotFoundException e) {
            logger.error("file not found: " + file);
        } catch (IOException e) {
            logger.error("io error when reading file", e);
        }
        return null;
    }

}
