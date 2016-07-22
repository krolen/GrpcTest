package my.tests.grpc.server;

import com.google.common.base.Function;
import io.grpc.stub.StreamObserver;

import java.util.Collection;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Created by kkulagin on 7/19/2016.
 */
public class SynchronizedObserverDelegate<T> {
    private final StreamObserver<T> wrapped;
    private final ReentrantLock observerLock = new ReentrantLock(true);

    public SynchronizedObserverDelegate(StreamObserver<T> wrapped) {
        this.wrapped = wrapped;
    }

    public void onNext(T data) {
        try {
            observerLock.lock();
            wrapped.onNext(data);
        } finally {
            observerLock.unlock();
        }
    }

    public <S> void onNext(Collection<S> data, Function<S, T> f) {
        try {
            observerLock.lock();
            for (S s : data) {
                wrapped.onNext(f.apply(s));
            }
        } finally {
            observerLock.unlock();
        }
    }
}
