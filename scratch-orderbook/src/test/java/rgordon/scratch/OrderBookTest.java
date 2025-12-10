package rgordon.scratch;

import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.is;
import static rgordon.scratch.Side.BID;
import static rgordon.scratch.Side.OFFER;

class OrderBookTest {

    // Given an Order, add it to the OrderBook (order additions are expected to occur extremely frequently)

    @Test
    void givenOrder_whenAdded_thenOrderBookCorrect() {

        Order bid1 = new Order(123, 95.5, BID, 5);
        Order bid2 = new Order(124, 95.7, BID, 3);
        Order bid3 = new Order(125, 95.7, BID, 2);

        Order offer1 = new Order(127, 96.5, OFFER, 4);
        Order offer2 = new Order(128, 96.7, OFFER, 2);
        Order offer3 = new Order(129, 96.3, OFFER, 2);

        OrderBook test = new OrderBook();
        test.addOrder(bid1);
        test.addOrder(bid2);
        test.addOrder(bid3);
        test.addOrder(offer1);
        test.addOrder(offer2);
        test.addOrder(offer3);

        assertThat(test.getOrders(BID), contains(bid2, bid3, bid1));
        assertThat(test.getOrders(OFFER), contains(offer3, offer1, offer2));
    }

    @Test
    void givenOrderWithExistingId_whenAdded_thenRejectedAndOrderBookCorrect() {

        Order bid1 = new Order(123, 95.5, BID, 5);
        Order bid2 = new Order(123, 95.7, BID, 3);

        OrderBook test = new OrderBook();
        test.addOrder(bid1);

        try {
            test.addOrder(bid2);
            assertThat("Add should fail", false);
        }
        catch (IllegalArgumentException e) {
            // expected
        }

        assertThat(test.getOrders(BID), contains(bid1));
    }

    // Given an order id, remove an Order from the OrderBook (order deletions are expected to occur at
    // approximately 60% of the rate of order additions)

    @Test
    void givenOrderId_whenRemoved_thenOrderBookCorrect() {

        Order bid1 = new Order(123L, 95.5, BID, 5L);
        Order bid2 = new Order(124L, 95.7, BID, 3L);
        Order bid3 = new Order(125L, 95.7, BID, 2L);

        Order offer1 = new Order(127L, 96.5, OFFER, 4L);
        Order offer2 = new Order(128L, 96.7, OFFER, 2L);

        OrderBook test = new OrderBook();
        test.addOrder(bid1);
        test.addOrder(bid2);
        test.addOrder(bid3);
        test.addOrder(offer1);
        test.addOrder(offer2);

        assertThat(test.removeOrder(125L), is(true));
        assertThat(test.removeOrder(127L), is(true));
        assertThat(test.removeOrder(128L), is(true));

        assertThat(test.getOrders(BID), contains(bid2, bid1));
        assertThat(test.getOrders(OFFER), Matchers.empty());
    }

    // Given an order id and a new size, modify an existing order in the book to use the new size (size modications do
    // not affect time priority)

    @Test
    void givenOrderId_whenSizeChanged_thenOrderBookCorrect() {

        Order bid1 = new Order(123L, 95.5, BID, 5L);
        Order bid2 = new Order(124L, 95.7, BID, 3L);
        Order bid3 = new Order(125L, 95.7, BID, 2L);

        Order offer1 = new Order(127L, 96.0, OFFER, 4L);
        Order offer2 = new Order(128L, 96.0, OFFER, 2L);
        Order offer3 = new Order(129L, 96.0, OFFER, 3L);

        OrderBook test = new OrderBook();
        test.addOrder(bid1);
        test.addOrder(bid2);
        test.addOrder(bid3);
        test.addOrder(offer1);
        test.addOrder(offer2);
        test.addOrder(offer3);

        test.modifySize(124L, 5L);
        test.modifySize(128L, 7L);

        Order newBid2 = new Order(124L, 95.7, BID, 5L);
        Order newOffer2 = new Order(128L, 96.0, OFFER, 7L);

        assertThat(test.getOrders(BID), contains(newBid2, bid3, bid1));
        assertThat(test.getOrders(OFFER), contains(offer1, newOffer2, offer3));
    }

    // Given a side and a level (an integer value >0) return the price for that level (where level 1 represents the
    // best price for a given side). For example, given side=B and level=2 return the second best bid price

    @Test
    void givenSideAndLevel_thenReturnPrice() {

        Order bid1 = new Order(123L, 95.5, BID, 5L);
        Order bid2 = new Order(124L, 95.7, BID, 3L);
        Order bid3 = new Order(125L, 95.7, BID, 2L);

        Order offer1 = new Order(127L, 96.0, OFFER, 4L);
        Order offer2 = new Order(128L, 96.0, OFFER, 2L);
        Order offer3 = new Order(129L, 96.0, OFFER, 3L);

        OrderBook test = new OrderBook();
        test.addOrder(bid1);
        test.addOrder(bid2);
        test.addOrder(bid3);
        test.addOrder(offer1);
        test.addOrder(offer2);
        test.addOrder(offer3);

        assertThat(test.getPrice(BID, 1 ), is(95.7));
        assertThat(test.getPrice(BID, 2 ), is(95.5));
        assertThat(test.getPrice(OFFER, 1 ), is(96.0));
        assertThat(Double.isNaN(test.getPrice(OFFER, 2 )), is(true));
    }

    // Given a side and a level return the total size available for that level

    @Test
    void givenSideAndLevel_thenReturnSize() {

        Order bid1 = new Order(123L, 95.5, BID, 5L);
        Order bid2 = new Order(124L, 95.7, BID, 3L);
        Order bid3 = new Order(125L, 95.7, BID, 1L);

        Order offer1 = new Order(127L, 96.0, OFFER, 4L);
        Order offer2 = new Order(128L, 96.0, OFFER, 2L);
        Order offer3 = new Order(129L, 96.0, OFFER, 3L);

        OrderBook test = new OrderBook();
        test.addOrder(bid1);
        test.addOrder(bid2);
        test.addOrder(bid3);
        test.addOrder(offer1);
        test.addOrder(offer2);
        test.addOrder(offer3);

        assertThat(test.getSize(BID, 1), is(4L));
        assertThat(test.getSize(BID, 2), is(5L));
        assertThat(test.getSize(OFFER, 1), is(9L));
        assertThat(test.getSize(OFFER, 2), is(0L));
    }

    // Given a side return all the orders from that side of the book, in level- and time-order

    @Test
    void givenSide_thenReturnOrdersCorrectly() {

        Order bid1 = new Order(123, 95.5, BID, 5);
        Order bid2 = new Order(124, 95.7, BID, 3);
        Order bid3 = new Order(125, 95.7, BID, 2);

        Order offer1 = new Order(127, 96.5, OFFER, 4);
        Order offer2 = new Order(128, 96.7, OFFER, 2);

        OrderBook test = new OrderBook();
        test.addOrder(bid1);
        test.addOrder(bid2);
        test.addOrder(bid3);
        test.addOrder(offer1);
        test.addOrder(offer2);

        assertThat(test.getOrders(BID), contains(bid2, bid3, bid1));
        assertThat(test.getOrders(OFFER), contains(offer1, offer2));
    }
}