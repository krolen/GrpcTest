package my.tests.grpc.queue;

import com.google.common.util.concurrent.Uninterruptibles;
import net.openhft.chronicle.queue.ChronicleQueue;
import net.openhft.chronicle.queue.ExcerptAppender;
import net.openhft.chronicle.queue.ExcerptTailer;
import net.openhft.chronicle.queue.TailerState;
import net.openhft.chronicle.queue.impl.single.SingleChronicleQueueBuilder;

import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

/**
 * Created by kkulagin on 7/28/2016.
 */
public class QueuePublisher {
    public static void main(String[] args) {
        try (ChronicleQueue queue = SingleChronicleQueueBuilder.binary("/trades").build()) {
            ExcerptAppender appender = queue.acquireAppender();
            IntStream.range(0, 1_000_000).forEach(i -> appender.writeText("aaaaaabbbbbbbbbbbbbbbbbbbbbbbrrrrrrrrrrrrrrrrrdsdddddd" + Integer.toString(i)));
        }
    }

}
