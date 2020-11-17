package group.zeus.rpc.core;

import group.zeus.rpc.dto.RpcRequest;
import group.zeus.rpc.dto.RpcResponse;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.locks.AbstractQueuedSynchronizer;

/**
 * @Author: maodazhan
 * @Date: 2020/10/30 16:51
 */
public class RpcFuture implements Future<Object> {

    public Sync sync;
    private RpcRequest request;
    private RpcResponse response;

    public RpcFuture(RpcRequest request){
        this.sync = new Sync();
        this.request = request;
    }

    static class Sync extends AbstractQueuedSynchronizer{
        private static final long serialVersionUID = 9196975123891064904L;

        // future status 不可改变，可改变的是state (volatile)
        private final int done = 1;
        private final int pending = 0;

        // acquire(int arg) call
        @Override
        protected boolean tryAcquire(int arg) {
            // before tryRelease successfully, getState()!=done
            return getState() == done;
        }

        // release(int arg) call
        @Override
        protected boolean tryRelease(int arg) {
            if (getState() == pending){
                if (compareAndSetState(pending, done)){
                    return true;
                } else{
                    return false;
                }
            }else{
                return true;
            }
        }

        protected boolean isDone(){
            return getState() == done;
        }
    }

    @Override
    public boolean cancel(boolean mayInterruptIfRunning) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isCancelled() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isDone() {
        return sync.isDone();
    }

    @Override
    public Object get() throws InterruptedException, ExecutionException {
        sync.acquire(1);
        if (this.response!=null){
            return this.response.getResult();
        }
        return null;
    }

    public void done(RpcResponse response){
        this.response = response;
        sync.release(1);
    }

    @Override
    public Object get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
        throw new UnsupportedOperationException();
    }
}
