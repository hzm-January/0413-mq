# RabbitMQ 学习笔记
## 1 hello world
### 1.1 生产者
### 1.2 消费者
## 2 work queues
### 2.1 多消费者消费任务
官方说明：
>By default, RabbitMQ will send each message to the next consumer, in sequence. 
On average every consumer will get the same number of messages.
This way of distributing messages is called round-robin.

默认情况下，RabbitMQ按照顺序将消息发送给每个消费者，
每个消费者会获取到相同数目的消息，
这种分发消息的方式被称为round-robin循环调度
### 2.2 Consumer Acknowledgements（消费者确认）
#### 2.2.1 为什么需要Consumer Acknowledgements
官方说明：
>Systems that use a messaging broker such as RabbitMQ are by definition distributed. 
Since protocol methods (messages) sent are not guaranteed to reach the peer or be successfully processed by it, 
both publishers and consumers need a mechanism for delivery and processing confirmation. 
Several messaging protocols supported by RabbitMQ provide such features. 
This guide covers the features in AMQP 0-9-1 
but the idea is largely the same in other protocols (STOMP, MQTT, et cetera).

诸如RabbitMQ这种分布式消息队列，由于发送的方法或者消息不能确保接受者接收到并且已处理完成，
所以发布者和消费者都需要一个交付和处理确认机制

#### 2.2.2 什么是Consumer Acknowledgements
> Doing a task can take a few seconds.
You may wonder what happens if one of the consumers starts a long task and dies with it only partly done. 
With our current code, once RabbitMQ delivers a message to the customer it immediately marks it for deletion. 
In this case, if you kill a worker we will lose the message it was just processing.
We'll also lose all the messages that were dispatched to this particular worker but were not yet handled.

如果将autoAck设置为true（意为：fire-and-forget即发既忘，自动消息确认），RabbitMQ将消息发送给消费者之后立即标记为删除，
执行某个任务可能需要耗费一定的时间，如果该任务只执行了一部分，worker宕机，将丢失这个正在执行的任务，
并且将丢失RabbitMQ已经分配给consumer的所有任务（尽管这些任务还没来得急执行，还在该consumer排队待执行）
> But we don't want to lose any tasks. If a worker dies, we'd like the task to be delivered to another worker.

但是实际开发中并不想丢失这些任务，如果一个worker宕机，我们希望任务被分配给其他worker

> In order to make sure a message is never lost, RabbitMQ supports message acknowledgments. 
An ack(nowledgement) is sent back by the consumer to tell RabbitMQ that a particular message has been received, 
processed and that RabbitMQ is free to delete it.

为了确保消息不丢失，RabbitMQ支持消息确认。当consumer收到消息并且已执行完成，consumer发回ack(nowledgement)通知RabbitMQ可以自由的删除该消息

> If a consumer dies (its channel is closed, connection is closed, or TCP connection is lost) without sending an ack, 
RabbitMQ will understand that a message wasn't processed fully and will re-queue it. 
If there are other consumers online at the same time, it will then quickly redeliver it to another consumer.
That way you can be sure that no message is lost, even if the workers occasionally die.

如果消费者宕机（可能情况：channel关闭，连接关闭，TCP连接丢失）没有发送ack回执，RabbitMQ了解到该消息没有被完全执行，将会重新排队。
如果此时还有其他consumer在线，将会快速的将该消息发送给其他consumer。这种方式将会确保消息不会丢失。

#### 2.2.3 如何使用Consumer Acknowledgements（消费者确认）

##### 2.2.3.1 自动交付确认
autoAck=true

官方风险说明：
>In automatic acknowledgement mode, 
a message is considered to be successfully delivered immediately after it is sent. 
This mode trades off higher throughput (as long as the consumers can keep up) 
for reduced safety of delivery and consumer processing. 
This mode is often referred to as "fire-and-forget". Unlike with manual acknowledgement model, 
if consumers's TCP connection or channel is closed before successful delivery, 
the message sent by the server will be lost. Therefore,
automatic message acknowledgement should be considered unsafe and not suitable for all workloads.

在自动确认模式中，消息被认为在发送后立即成功传送。该模式折衷了更高的吞吐量（只要消费者可以跟上），
以降低交付和消费者处理的安全性。这种模式通常被称为“即发即忘”。
与手动确认模型不同，如果消费者的TCP连接或通道在成功交付之前关闭，则服务器发送的消息将丢失。
因此，自动消息确认应被视为不安全 ，并不适用于所有工作负载。

>Another things that's important to consider 
when using automatic acknowledgement mode is that of consumer overload.
Manual acknowledgement mode is typically used 
with a bounded channel prefetch which limits the number of outstanding ("in progress") 
deliveries on a channel. With automatic acknowledgements, however, 
there is no such limit by definition. Consumers therefore can be overwhelmed by the rate of deliveries, 
potentially accumulating a backlog in memory and running out of heap or getting their process terminated by the OS. 
Some client libraries will apply TCP back pressure 
(stop reading from the socket until the backlog of unprocessed deliveries drops beyond a certain limit). 
Automatic acknolwedgement mode is therefore only recommended for consumers that can process deliveries efficiently and at a steady rate.

自动确认需要考虑的另外一种情况是消息过载。
手动确认模式通常与有界信道预取一起使用，该预取(prefetchCount)限制了信道上未完成（“进行中”）交付的数量。
但是，通过自动确认，根据定义没有这种限制。因此，消费者可能会被交付速度所淹没，
可能会积累内存中的积压并耗尽堆或使操作系统终止其进程。
某些客户端库将应用TCP反压（停止从套接字读取，直到未处理的交付积压超过某个限制）。
因此，仅建议能够以稳定的速度有效处理交付的消费者使用自动交钥匙模式。

##### 2.2.3.2 Positively Acknowledging Deliveries 主动积极的交付确认

autoAck设置为false，并主动调用支付确认Channel#basicAck和Channel#basicNack

>官方说明

>API methods used for delivery acknowledgement are usually exposed as operations on a channel in client libraries. 
Java client users will use Channel#basicAck and Channel#basicNack to perform a basic.ack and basic.nack, 
respectively. Here's a Java client examples that demonstrates a positive acknowledgement:

Java开发者可以使用Channel#basicAck和Channel#basicNack分别执行basic.ack和basic.nack