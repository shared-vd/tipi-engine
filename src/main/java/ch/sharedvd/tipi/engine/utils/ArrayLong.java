package ch.sharedvd.tipi.engine.utils;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

public class ArrayLong implements Serializable, Iterable<Long> {

    private static final long serialVersionUID = 6843092778934655429L;

    private long[] array;
    private int first;
    private int last;

    public ArrayLong() {
        this((long[]) null);
    }

    public ArrayLong(Collection<Long> list) {
        if (list != null && list.size() > 0) {
            this.array = list2array(list);
            this.first = 0;
            this.last = array.length - 1;
        } else {
            this.array = null;
        }
    }

    public ArrayLong(long... aArray) {
        if (aArray != null && aArray.length > 0) {
            this.array = aArray;
            this.first = 0;
            this.last = aArray.length - 1;
        } else {
            this.array = null;
        }
    }

    public ArrayLong(ArrayLong array, int first, int last) {
        this(array.array, first, last);
    }

    public ArrayLong(long[] array, int first, int last) {
        if (array != null) {
            Assert.isTrue(array.length > 0);
            Assert.isTrue(first >= 0);
            Assert.isTrue(last >= 0);
            Assert.isTrue(first <= last);
            Assert.isTrue(last < array.length);
            Assert.isTrue(array.length >= (last - first + 1));

            this.array = array;
            this.first = first;
            this.last = last;
        } else {
            this.array = null;
        }
    }

    public ArrayLong(int... aArray) {
        if (aArray != null && aArray.length > 0) {
            this.array = new long[aArray.length];
            for (int i = 0; i < aArray.length; i++) {
                this.array[i] = aArray[i];
            }
            this.first = 0;
            this.last = aArray.length - 1;
        } else {
            this.array = null;
        }
    }

    public int size() {
        if (array != null) {
            final int s = last - first + 1;
            return s;
        }
        return 0;
    }

    public boolean isEmpty() {
        return size() == 0;
    }

    public long get(int index) {
        Assert.isTrue(index >= 0 && index < size());
        return array[first + index];
    }

    public List<ArrayLong> splitByMax(int max) {
        List<ArrayLong> list = new ArrayList<ArrayLong>();

        int s = size();
        int index = 0;
        while (index < s) {
            int last = index + max - 1;
            if (last >= s) {
                last = s - 1;
            }
            final ArrayLong arr = new ArrayLong(array, index + first, last + first);
            list.add(arr);

            index += max;
        }
        return list;
    }

    public List<ArrayLong> splitInLists(int nbLists) {
        Assert.isTrue(nbLists > 0);
        final double max = size() / 1.0f / nbLists;
        int m = (int) Math.ceil(max);
        return splitByMax(m);
    }

    public List<Long> toList() {
        if (array != null) {
            final List<Long> list = new ArrayList<Long>();
            for (int i = 0; i < size(); i++) {
                list.add(get(i));
            }
            return list;
        } else {
            return null;
        }
    }

    public List<Integer> toIntegerList() {
        if (array != null) {
            final List<Integer> list = new ArrayList<Integer>();
            for (int i = 0; i < size(); i++) {
                list.add((int) get(i));
            }
            return list;
        } else {
            return null;
        }
    }

    public long[] toArray() {
        if (array != null) {
            final long[] arr = new long[size()];
            for (int i = 0; i < size(); i++) {
                arr[i] = get(i);
            }
            return arr;
        } else {
            return null;
        }
    }

    public Long[] toLongArray() {
        if (array != null) {
            final Long[] arr = new Long[size()];
            for (int i = 0; i < size(); i++) {
                arr[i] = get(i);
            }
            return arr;
        } else {
            return null;
        }
    }

    private static long[] list2array(Collection<Long> list) {
        Assert.notNull(list);
        Assert.notEmpty(list);
        long[] array = new long[list.size()];
        int index = 0;
        for (Long v : list) {
            array[index] = v;
            index++;
        }
        return array;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("[");
        boolean first = true;
        for (Iterator<Long> i = iterator(); i.hasNext(); ) {
            if (!first) {
                sb.append(" ");
            }
            sb.append(i.next());
            first = false;
        }
        sb.append("]");
        return sb.toString();
    }

    @Override
    public Iterator<Long> iterator() {
        return new ArrayLongIterator();
    }

    public class ArrayLongIterator implements Iterator<Long> {

        public int index = 0;

        @Override
        public boolean hasNext() {
            return index < size();
        }

        @Override
        public Long next() {
            return get(index++);
        }

        @Override
        public void remove() {
            Assert.fail("Not supported");
        }
    }

    // ArrayLong serialization
    // Permet de ne sérialiser que la partie utile de array.
    private void writeObject(ObjectOutputStream out) throws IOException {
        long[] altow = array;
        if (null != array) {
            int len = last - first + 1;
            if (len < array.length) {
                altow = new long[len];
                System.arraycopy(array, first, altow, 0, len);
            }
        }
        out.writeObject(altow);
    }

    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        array = (long[]) in.readObject();
        if (null != array) {
            first = 0;
            last = array.length - 1;
        }
    }
}
