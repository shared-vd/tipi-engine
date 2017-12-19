package ch.sharedvd.tipi.engine.utils;

import java.util.Iterator;
import java.util.List;

public class ResultListWithCount<T> implements Iterable<T> {

    private List<T> result;
    private long count;

    public ResultListWithCount(List<T> result, long count) {
        Assert.notNull(result);
        this.result = result;
        this.count = count;
    }

    public List<T> getResult() {
        return result;
    }

    public void setResult(List<T> result) {
        this.result = result;
    }

    public long getCount() {
        return count;
    }

    public void setCount(long count) {
        this.count = count;
    }

    @Override
    public Iterator<T> iterator() {
        return result.iterator();
    }

}
