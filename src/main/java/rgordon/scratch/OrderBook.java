package rgordon.scratch;

import java.util.*;
import java.util.function.Consumer;

import static rgordon.scratch.Side.BID;
import static rgordon.scratch.Side.OFFER;

/**
 * Simple implementation of an Order Book. This is not Thread Safe.
 *
 * To be more performant.
 *  - Primitive collections could be used to avoid autoboxing.
 *  - If we have full control over Order we could use it internally and change size avoiding copying to
 *    an internal structure. This would avoid increased garbage collection but would make the code harder to
 *    reason about.
 *  - Read/Write locks could be used per side to allow concurrent access.
 *  - Level read operations are slow because of iteration. If these operations were more frequent than modifications
 *    then various memorisation options could be used.
 */
public class OrderBook {

    private int timeOrder;

    private final Map<Long, OrderInternal> ordersById = new HashMap<>();

    private final OrdersBySide bids;

    private final OrdersBySide offers ;

    public OrderBook() {

        bids = new OrdersBySide((l, r) -> l.compareTo(r) * -1);
        offers = new OrdersBySide(Comparator.naturalOrder());
    }

    public void addOrder(Order order) {

        OrdersBySide ordersBySide = getSide(order.getSide());
        ordersBySide.addOrder(order);
    }

    public boolean removeOrder(long id) {

        OrderInternal orderInternal = ordersById.remove(id);
        if (orderInternal == null) {
            return false;
        }

        orderInternal.level.remove(orderInternal.timeOrder);
        return true;
    }

    public void modifySize(long id, long newSize) {
        if (newSize < 1) {
            throw new IllegalArgumentException("Invalid order size " + newSize);
        }

        Objects.requireNonNull(ordersById.get(id),
                "No order " + id).size = newSize;
    }

    public double getPrice(char side, int level) {
        return getSide(side).getPrice(level);
    }

    public long getSize(char side, int level) {
        return getSide(side).getSize(level);
    }

    public List<Order> getOrders(char side) {
        List<Order> orders = new ArrayList<>(128);
        dumpOrders(side, orders::add);
        return orders;
    }

    public void dumpOrders(char side, Consumer<Order> consumer) {

        getSide(side).dump(consumer);
    }

    class OrderInternal {

        private final long id;

        private final double price;

        private final char side;

        private long size;

        private final Level level;

        private final int timeOrder;

        OrderInternal(Order order, Level level, int timeOrder) {
            this.id = order.getId();
            this.price = order.getPrice();
            this.side = order.getSide();
            this.size = order.getSize();
            this.level = level;
            this.timeOrder = timeOrder;
        }
    }

    class OrdersBySide {

        private final Map<Double, Level> levels;

        OrdersBySide(Comparator<Double> comparator) {
            levels = new TreeMap<>(comparator);
        }

        void addOrder(Order order) {
            levels.computeIfAbsent(order.getPrice(),
                            price -> new Level(() -> levels.remove(price)))
                    .addOrder(order);
        }

        public void dump(Consumer<? super Order> consumer) {
            levels.values().forEach(level -> level.dump(consumer));
        }

        public double getPrice(int level) {
            if (level <= 0) {
                throw new IllegalArgumentException("Bad level " + level);
            }

            for (Double price : levels.keySet()) {
                if (--level == 0) {
                    return price;
                }
            }
            return Double.NaN;
        }

        public long getSize(int level) {
            if (level <= 0) {
                throw new IllegalArgumentException("Bad level " + level);
            }

            for (Level l : levels.values()) {
                if (--level == 0) {
                    return l.getTotalSize();
                }
            }

            return 0L;
        }
    }

    class Level {

        private final Runnable removeCallback;

        private final Map<Integer, OrderInternal> orders = new TreeMap<>();

        Level(Runnable removeCallback) {
            this.removeCallback = removeCallback;
        }

        void addOrder(Order order) {
            int seq = ++timeOrder;
            OrderInternal internal = new OrderInternal(order, this, seq);
            if (ordersById.put(order.getId(), internal) != null) {
                throw new IllegalArgumentException("Duplicate order Id " + order.getId());
            }
            orders.put(seq, internal);
        }

        public void dump(Consumer<? super Order> consumer) {
            orders.values().forEach(internal -> consumer.accept(
                    new Order(internal.id, internal.price, internal.side, internal.size)));
        }

        public void remove(int timeOrder) {
            orders.remove(timeOrder);
            if (orders.isEmpty()) {
                removeCallback.run();
            }
        }

        public long getTotalSize() {
            long total = 0;
            for (OrderInternal order : orders.values()) {
                total += order.size;
            }
            return total;
        }
    }

    protected OrdersBySide getSide(char c) {
        switch (c) {
            case BID:
                return bids;
            case OFFER:
                return offers;
            default:
                throw new IllegalArgumentException("Unknown side " + c);
        }
    }
}
