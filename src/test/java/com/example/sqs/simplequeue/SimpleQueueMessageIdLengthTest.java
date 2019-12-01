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
import com.amazonaws.services.sqs.model.*;
import org.junit.Before;
import org.junit.Test;

import java.util.List;


public class SimpleQueueMessageIdLengthTest {

    private static AmazonSQSAsyncClient sqs;
    private String myQueueUrl;
    private List<Message> messages;

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
    public void testSendMessage() {
        System.out.println("Sending a message to MyQueue.\n");
        // Using DelaySeconds to send message in queue after 5 sec
        sqs.sendMessage(new SendMessageRequest(myQueueUrl, "This is my message text.").withDelaySeconds(5));
    }

    @Test
    public void testMessageIdLengthLimit() {
        ReceiveMessageRequest receiveMessageRequest = new ReceiveMessageRequest(myQueueUrl);
        // Set WaitTimeSeconds to 10 seconds for polling for message
        receiveMessageRequest.setWaitTimeSeconds(10);

        messages = sqs.receiveMessage(receiveMessageRequest).getMessages();
        for (Message message : messages) {
            // Set VisibilityTimeout parameter using ChangeMessageVisibility
            // i.e time until the message will be in queue to be consumed
            sqs.changeMessageVisibility(myQueueUrl, message.getReceiptHandle(), 60);
            if (message.getMessageId().length() > 100) {
                System.out.println("Message ID length has exceeded 100 characters");
                System.out.println("Exiting...");
                System.exit(1);
            }
        }
        System.out.println("Message ID length has not exceeded 100 characters for any of the messages");

        System.out.println("Deleting message in MyQueue\n");
        try {
            sqs.deleteMessage(new DeleteMessageRequest(myQueueUrl, messages.get(0).getReceiptHandle()));
        } catch (NullPointerException e) {
            System.out.println("No more message in MyQueue");
        }

        System.out.println("Purging MyQueue.\n");
        sqs.purgeQueue(new PurgeQueueRequest(myQueueUrl));

        System.out.println("Deleting MyQueue\n");
        sqs.deleteQueue(new DeleteQueueRequest(myQueueUrl));
    }
}
