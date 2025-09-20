package me.herohd.rubyisland.utils;

import java.util.*;
import java.util.stream.Collectors;

public class PercentElementList<E> implements Iterable<E> {
    private static final KRandom RANDOM = new KRandom();
    private final List<RandomElement<E>> internalList;
    private final Map<RandomElement<E>, Double> internalMap;

    public PercentElementList() {
        this.internalList = new ArrayList<>();
        this.internalMap = new HashMap<>();
    }

    public PercentElementList(List<RandomElement<E>> internalList) {
        this.internalList = new ArrayList<>();
        this.internalMap = new HashMap<>();
        internalList.forEach(a -> internalMap.put(a, a.percentage));
    }

    public E next() {
        return getRandomElement(internalMap).element;
    }

    public void add(E element, double percentage) {
        RandomElement<E> el = new RandomElement<>(element, percentage);
        internalMap.put(el, percentage);
        internalList.add(el);
    }

    public int size() {
        return internalMap.size();
    }

    public <T> T getRandomElement(Map<T, Double> map) {
        List<T> list = getRandomElement(new HashMap<>(map), 1);
        return list.isEmpty() ? null : list.get(0);
    }

    public <T> List<T> getRandomElement(Map<T, Double> map, int amount) {
        map.values().removeIf(chance -> chance <= 0D);
        if (map.isEmpty()) return Collections.emptyList();

        List<T> list = new ArrayList<>();
        double total = map.values().stream().mapToDouble(d -> d).sum();

        for (int count = 0; count < amount; count++) {
            double index = getDouble(0D, total);// Math.random() * total;
            double countWeight = 0D;

            for (Map.Entry<T, Double> en : map.entrySet()) {
                countWeight += en.getValue();
                if (countWeight >= index) {
                    list.add(en.getKey());
                    break;
                }
            }
        }
        return list;
    }

    public static double getDouble(double min, double max) {
        return min + (max - min) * RANDOM.nextDouble();
    }

    /**
     * @return la lista interna
     */
    public List<RandomElement<E>> getInternalList() {
        return internalList;
    }

    /**
     * @return The internal object map with percentage
     */
    public Map<RandomElement<E>, Double> getInternalMap(){
        return internalMap;
    }

    @Override
    public Iterator<E> iterator() {
        return getInternalList().stream()
                .map(eRandomRandomElement -> eRandomRandomElement.element)
                .collect(Collectors.toList())
                .iterator();
    }

    public static class RandomElement<E> {
        final E element;
        final double percentage;

        public RandomElement(E element, double percentage) {
            this.element = element;
            this.percentage = percentage;
        }

        public E getElement() {
            return element;
        }

        public double getPercentage() {
            return percentage;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof RandomElement)) return false;
            RandomElement<?> that = (RandomElement<?>) o;
            return Double.compare(that.percentage, percentage) == 0 && Objects.equals(element, that.element);
        }

        @Override
        public int hashCode() {
            return Objects.hash(getElement(), getPercentage());
        }

        @Override
        public String toString() {
            return "RandomElement{" +
                    "element=" + element +
                    ", percentage=" + percentage +
                    '}';
        }
    }

    @Override
    public String toString() {
        return "PercentElementList_N{" +
                "internalMap=" + internalMap +
                '}';
    }

    public static class KRandom extends Random {
        private static final int UPPER_MASK = Integer.MIN_VALUE;
        private static final int LOWER_MASK = Integer.MAX_VALUE;
        private static final int N = 624;
        private static final int M = 397;
        private static final int[] MAGIC;
        private static final int MAGIC_FACTOR1 = 1812433253;
        private static final int MAGIC_FACTOR2 = 1664525;
        private static final int MAGIC_FACTOR3 = 1566083941;
        // private static final int MAGIC_MASK1 = -1658038656;
        // private static final int MAGIC_MASK2 = -272236544;
        private static final int MAGIC_SEED = 19650218;
        private static final long DEFAULT_SEED = 5489L;

        static {
            MAGIC = new int[]{0, -1727483681};
        }

        private transient int[] mt;
        private transient int mti;
        private transient boolean compat;
        private transient int[] ibuf;

        public KRandom() {
            this(false);
        }

        public KRandom(boolean compatible) {
            super(0L);
            this.compat = false;
            this.compat = compatible;
            this.setSeed(this.compat ? DEFAULT_SEED : System.currentTimeMillis());
        }

        public KRandom(long seed) {
            super(seed);
            this.compat = false;
        }

        public KRandom(byte[] buf) {
            super(0L);
            this.compat = false;
            this.setSeed(buf);
        }

        public KRandom(int[] buf) {
            super(0L);
            this.compat = false;
            this.setSeed(buf);
        }

        public static int[] pack(byte[] buf) {
            int blen = buf.length;
            int ilen = buf.length + 3 >>> 2;
            int[] ibuf = new int[ilen];
            for (int n = 0; n < ilen; ++n) {
                int m = n + 1 << 2;
                if (m > blen) {
                    m = blen;
                }
                int k;
                for (k = (buf[--m] & 0xFF); (m & 0x3) != 0x0; k = (k << 8 | (buf[--m] & 0xFF))) {

                }
                ibuf[n] = k;
            }
            return ibuf;
        }

        private void setSeed(int seed) {
            if (this.mt == null) {
                this.mt = new int[N];
            }
            this.mt[0] = seed;
            this.mti = 1;
            while (this.mti < N) {
                this.mt[this.mti] = MAGIC_FACTOR1 * (this.mt[this.mti - 1] ^ this.mt[this.mti - 1] >>> 30) + this.mti;
                ++this.mti;
            }
        }

        @Override
        public synchronized void setSeed(long seed) {
            if (this.compat) {
                this.setSeed((int) seed);
            } else {
                if (this.ibuf == null) {
                    this.ibuf = new int[2];
                }
                this.ibuf[0] = (int) seed;
                this.ibuf[1] = (int) (seed >>> 32);
                this.setSeed(this.ibuf);
            }
        }

        public void setSeed(byte[] buf) {
            this.setSeed(pack(buf));
        }

        public synchronized void setSeed(int[] buf) {
            int length = buf.length;
            if (length == 0) {
                throw new IllegalArgumentException("Seed buffer may not be empty");
            }
            int i = 1;
            int j = 0;
            int k = Math.max(N, length);
            this.setSeed(MAGIC_SEED);
            while (k > 0) {
                this.mt[i] = (this.mt[i] ^ (this.mt[i - 1] ^ this.mt[i - 1] >>> 30) * MAGIC_FACTOR2) + buf[j] + j;
                ++i;
                ++j;
                if (i >= N) {
                    this.mt[0] = this.mt[623];
                    i = 1;
                }
                if (j >= length) {
                    j = 0;
                }
                --k;
            }
            for (k = 623; k > 0; --k) {
                this.mt[i] = (this.mt[i] ^ (this.mt[i - 1] ^ this.mt[i - 1] >>> 30) * MAGIC_FACTOR3) - i;
                if (++i >= N) {
                    this.mt[0] = this.mt[623];
                    i = 1;
                }
            }
            this.mt[0] = UPPER_MASK;
        }

        @Override
        protected synchronized int next(int bits) {
            if (this.mti >= N) {
                int kk;
                for (kk = 0; kk < 227; ++kk) {
                    int y = (this.mt[kk] & UPPER_MASK) | (this.mt[kk + 1] & LOWER_MASK);
                    this.mt[kk] = (this.mt[kk + M] ^ y >>> 1 ^ KRandom.MAGIC[y & 0x1]);
                }
                while (kk < 623) {
                    int y = (this.mt[kk] & UPPER_MASK) | (this.mt[kk + 1] & LOWER_MASK);
                    this.mt[kk] = (this.mt[kk - 227] ^ y >>> 1 ^ KRandom.MAGIC[y & 0x1]);
                    ++kk;
                }
                int y = (this.mt[623] & UPPER_MASK) | (this.mt[0] & LOWER_MASK);
                this.mt[623] = (this.mt[396] ^ y >>> 1 ^ KRandom.MAGIC[y & 0x1]);
                this.mti = 0;
            }
            int y = this.mt[this.mti++];
            y ^= y >>> 11;
            y ^= (y << 7 & 0x9D2C5680);
            y ^= (y << 15 & 0xEFC60000);
            y ^= y >>> 18;
            return y >>> 32 - bits;
        }
    }
}