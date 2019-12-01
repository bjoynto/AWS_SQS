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

import java.util.List;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.sqs.AmazonSQSAsyncClient;
import com.amazonaws.services.sqs.model.Message;
import com.amazonaws.services.sqs.model.ReceiveMessageRequest;
import com.amazonaws.services.sqs.model.SendMessageRequest;
import org.junit.Before;
import org.junit.Test;

public class SimpleQueueMessageLimitTest {

    private static AmazonSQSAsyncClient sqs;
    private String myQueueUrl;
    private List<Message> messages;

    @SuppressWarnings("deprecation")
    @Before
    public void setUp() {
        // Accessing SQS service using ElasticMQ on local host
        BasicAWSCredentials credentials = new BasicAWSCredentials("x", "x");
        sqs = new AmazonSQSAsyncClient(credentials).withEndpoint("http://localhost:9324");
        com.amazonaws.services.sqs.model.CreateQueueRequest createQueueRequest =
                new com.amazonaws.services.sqs.model.CreateQueueRequest("MyQueue");
        myQueueUrl = sqs.createQueue(createQueueRequest).getQueueUrl();
    }

    @Test
    public void testSendMessageLimit() {
        System.out.println("Sending 120001 messages to MyQueue.\n");
        for (int i = 0; i < 120001; i++) {
            sqs.sendMessage(new SendMessageRequest(myQueueUrl, "This is my message text."));
        }
        ReceiveMessageRequest receiveMessageRequest = new ReceiveMessageRequest(myQueueUrl);
        // Set WaitTimeSeconds to 10 seconds.
        receiveMessageRequest.setWaitTimeSeconds(10);
        messages = sqs.receiveMessage(receiveMessageRequest).getMessages();
        if (messages.size() > 120000) {
            System.out.println("Message limit of 120000 is not followed");
            System.out.println("Exiting...");
            System.exit(1);
        } else {
            System.out.println("Message limit of 120000 is followed");
            System.out.println("This is because 120001 messages caused new message size to be " + messages.size());
        }
    }
}
