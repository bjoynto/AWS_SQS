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

package com.example.sqs.simplequeue;

import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.sqs.AmazonSQSAsyncClient;
import com.amazonaws.services.sqs.model.CreateQueueRequest;
import com.amazonaws.services.sqs.model.Message;
import org.junit.Before;
import org.junit.Test;

import java.util.List;


public class SimpleQueueUrlTest {

    private static AmazonSQSAsyncClient sqs;
    private String myQueueUrl;

    @Before
    public void setUp() {
        // Accessing SQS service using ElasticMQ on local host
        BasicAWSCredentials credentials = new BasicAWSCredentials("x", "x");
        sqs = new AmazonSQSAsyncClient(credentials).withEndpoint("http://localhost:9324");
        CreateQueueRequest createQueueRequest =
                new CreateQueueRequest("MyQueue");
        myQueueUrl = sqs.createQueue(createQueueRequest).getQueueUrl();
    }

    @Test
    public void testQueueUrlsContainsSimpleQueueUrl() {
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
}
