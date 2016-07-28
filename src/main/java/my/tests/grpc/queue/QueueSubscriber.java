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
public class QueueSubscriber {


    public static void main(String[] args) {
        try (ChronicleQueue queue = SingleChronicleQueueBuilder.binary("/trades").build()) {
            ExcerptTailer tailer = queue.createTailer();
            int count = 0;
            while (count < 10) {
                TailerState state = tailer.state();
                System.out.println(state);
                String text = tailer.readText();
                if(text != null) {
                    count++;
                }
                System.out.println(text);
                Uninterruptibles.sleepUninterruptibly(1, TimeUnit.SECONDS);
            }
//            System.out.println();
        }
    }
}
