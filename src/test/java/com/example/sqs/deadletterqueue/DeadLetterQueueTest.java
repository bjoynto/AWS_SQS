/*
 * Copyright 2011-2019 Amazon.com, Inc. or its affiliates. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at:
 *
 *    http://aws.amazon.com/apache2.0
 *
 * This file is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES
 * OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and
 * limitations under the License.
 *
 * NOTE: Before running the test case, please run a local instance of elasticMQ
 *       download a execute it with java -jar elasticmq-server-0.15.2.jar.
 */

package com.example.sqs.deadletterqueue;

import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.sqs.AmazonSQSAsyncClient;
import org.junit.Before;
import org.junit.Test;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.sqs.SqsClient;

import java.util.List;

public class DeadLetterQueueTest
{
    private static AmazonSQSAsyncClient sqs;
    private String myQueueUrl;

    @SuppressWarnings("deprecation")
    @Before
    public void setUp() {
        // Accessing SQS service using ElasticMQ on local host
        BasicAWSCredentials credentials = new BasicAWSCredentials("x", "x");
        sqs = new AmazonSQSAsyncClient(credentials).withEndpoint("http://localhost:9324");
        com.amazonaws.services.sqs.model.CreateQueueRequest createQueueRequest = new com.amazonaws.services.sqs.model.CreateQueueRequest("MyQueue");
        myQueueUrl = sqs.createQueue(createQueueRequest).getQueueUrl();
    }

    @Test
    public void testDeadLetterServiceName() {
        if (!sqs.getServiceName().equals("sqs")) {
            System.out.println("SQS deadletter service name verified as not same as sqs");
            System.out.println("Exiting...");
            System.exit(1);
        }
    }

    @Test
    public void testDeadLetterQueuesValid() {
        String src_queue_name = "a";
        String dl_queue_name = "b";
        testQueues(src_queue_name, dl_queue_name);
    }

    @Test
    public void testQueueUrlsContainsDeadLetterQueueUrl() {
        // This test is to check whether SQS queue URL is either of the two expected URLs.
        for (String queueUrl : sqs.listQueues("My").getQueueUrls()) {
            if (!queueUrl.equals("http://localhost:9324/queue/MyQueue") &&
                    !queueUrl.equals("http://localhost:9324/queue/MyDeadLetterQueue")) {
                String s1 = "http://localhost:9324/queue/MyQueue";
                String s2 = "http://localhost:9324/queue/MyDeadLetterQueue";
                System.out.println("QueueUrl does not correspond to either of " + s1 + " or " + s2);
                System.out.println("QueueUrl is " + queueUrl + " instead");
                System.out.println("Exiting...");
                System.exit(1);
            }
        }
    }

    public void testQueues(String sQueue, String dQueue) {

        SqsClient sqs = SqsClient.builder().region(Region.US_WEST_2).build();

        // Test to check and print error if source queue name is longer than 80 characters.
        if (sQueue.length() > 80) {
            System.out.println("The length of source queue name should be less than 80 characters.");
            System.out.println("Exiting...");
            System.exit(1);
        }

        // Test to check and print error if source queue name is longer than 80 characters.
        if (dQueue.length() > 80) {
            System.out.println("The length of destination queue name should be less than 80 characters.");
            System.out.println("Exiting...");
            System.exit(1);
        }

        // Test to check and print error if source queue name contains character other than alphanumberic, underscore or hyphen.
        if (!sQueue.matches("1|2|3|4|5|6|7|8|9|0|a|b|c|d|e|f|g|h|i|j|k|l|m|n|o|p|q|r|s|t|u|v|w|x|y|z|A|B|C|D|E|F|G|H|I|J|K|L|M|N|O|P|Q|R|S|T|U|V|W|X|Y|Z|-|_")) {
            System.out.println("Source queue name contains character other than alphanumeric, underscore or hyphen.");
            System.out.println("Exiting...");
            System.exit(1);
        }

        // Test to check and print error if destination queue name contains character other than alphanumberic, underscore or hyphen.
        if (!dQueue.matches("1|2|3|4|5|6|7|8|9|0|a|b|c|d|e|f|g|h|i|j|k|l|m|n|o|p|q|r|s|t|u|v|w|x|y|z|A|B|C|D|E|F|G|H|I|J|K|L|M|N|O|P|Q|R|S|T|U|V|W|X|Y|Z|-|_")) {
            System.out.println("Destination queue name contains character other than alphanumeric, underscore or hyphen.");
            System.out.println("Exiting...");
            System.exit(1);
        }
    }
}
