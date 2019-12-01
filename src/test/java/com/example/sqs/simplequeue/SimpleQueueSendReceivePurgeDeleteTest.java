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
import com.amazonaws.services.sqs.model.DeleteMessageRequest;
import com.amazonaws.services.sqs.model.DeleteQueueRequest;
import com.amazonaws.services.sqs.model.Message;
import com.amazonaws.services.sqs.model.PurgeQueueRequest;
import com.amazonaws.services.sqs.model.ReceiveMessageRequest;
import com.amazonaws.services.sqs.model.SendMessageRequest;
import org.junit.Before;
import org.junit.Test;

public class SimpleQueueSendReceivePurgeDeleteTest {

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
    public void testSimpleServiceName() {
        // This test is to check whether SQS simple service name is "sqs".
        if (!sqs.getServiceName().equals("sqs")) {
            System.out.println("SQS simple service name verified as not same as sqs");
            System.out.println("Exiting...");
            System.exit(1);
        }
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

    @Test
    public void testSendMessage() {
        System.out.println("Sending a message to MyQueue.\n");
        // Using DelaySeconds to send message in queue after 5 sec
        sqs.sendMessage(new SendMessageRequest(myQueueUrl, "This is my message text.").withDelaySeconds(5));
    }

    @Test
    public void testReceiveMessage() {
        System.out.println("Receiving messages from MyQueue.\n");

        ReceiveMessageRequest receiveMessageRequest = new ReceiveMessageRequest(myQueueUrl);
        // Set WaitTimeSeconds to 10 seconds for polling for message
        receiveMessageRequest.setWaitTimeSeconds(10);

        messages = sqs.receiveMessage(receiveMessageRequest).getMessages();
        for (Message message : messages) {
            // Set VisibilityTimeout parameter using ChangeMessageVisibility
            // i.e time until the message will be in queue to be consumed
            sqs.changeMessageVisibility(myQueueUrl, message.getReceiptHandle(), 60);
            System.out.println("  Message");
            System.out.println("    MessageId:     " + message.getMessageId());
            System.out.println("    ReceiptHandle: " + message.getReceiptHandle());
            System.out.println("    MD5OfBody:     " + message.getMD5OfBody());
            System.out.println("    Body:          " + message.getBody());
        }
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
    }

    @Test
    public void testReceiptHandleLengthLimit() {
        ReceiveMessageRequest receiveMessageRequest = new ReceiveMessageRequest(myQueueUrl);
        // Set WaitTimeSeconds to 10 seconds for polling for message
        receiveMessageRequest.setWaitTimeSeconds(10);

        messages = sqs.receiveMessage(receiveMessageRequest).getMessages();
        for (Message message : messages) {
            // Set VisibilityTimeout parameter using ChangeMessageVisibility
            // i.e time until the message will be in queue to be consumed
            sqs.changeMessageVisibility(myQueueUrl, message.getReceiptHandle(), 60);
            if (message.getReceiptHandle().length() > 1024) {
                System.out.println("Receipt handle length has exceeded 1024 characters");
                System.out.println("Exiting...");
                System.exit(1);
            }
        }
        System.out.println("Receipt handle length has not exceeded 1024 characters for any of the receipt handles");
    }

    @Test
    public void testDeleteMessage() {
        System.out.println("Deleting message in MyQueue\n");
        try {
            sqs.deleteMessage(new DeleteMessageRequest(myQueueUrl, messages.get(0).getReceiptHandle()));
        } catch (NullPointerException e) {
            System.out.println("No more message in MyQueue");
        }
    }

    @Test
    public void testPurgeQueue() {
        System.out.println("Purging MyQueue.\n");
        sqs.purgeQueue(new PurgeQueueRequest(myQueueUrl));
    }

    @Test
    public void testDeleteQueue() {
        System.out.println("Deleting MyQueue\n");
        sqs.deleteQueue(new DeleteQueueRequest(myQueueUrl));
    }
}
