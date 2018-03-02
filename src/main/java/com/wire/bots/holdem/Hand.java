package com.wire.bots.holdem;

import java.util.ArrayList;
import java.util.Comparator;

public class Hand implements Comparable<Hand> {
    private ArrayList<Card> cards;

    Hand(ArrayList<Card> cards) {
        this.cards = new ArrayList<>(cards);
        this.cards.sort(Comparator.reverseOrder());
    }

    @Override
    public String toString() {
        if (straightFlush() != -1)
            return String.format("straight flush %s high", Card.rankAsString(straightFlush()));
        if (fourKind() != -1)
            return String.format("four of kind %s", Card.rankAsString(fourKind()));
        if (fullHouse() != -1)
            return String.format("full house of %s", Card.rankAsString(fullHouse()));
        if (flush() != -1)
            return String.format("flush %s high", Card.rankAsString(flush()));
        if (straight() != -1)
            return String.format("straight %s high", Card.rankAsString(straight()));
        if (threeKind() != -1)
            return String.format("three of kind %s", Card.rankAsString(threeKind()));
        if (twoPair() != -1)
            return String.format("two pair %s high", Card.rankAsString(twoPair()));
        if (onePair() != -1)
            return String.format("one pair %s high", Card.rankAsString(onePair()));

        return String.format("high card of %s", Card.rankAsString(highCard().getRank()));
    }

    @Override
    public int compareTo(Hand that) {
        if (straightFlush() > that.straightFlush())
            return 1;
        if (straightFlush() < that.straightFlush())
            return -1;

        if (fourKind() > that.fourKind())
            return 1;
        if (fourKind() < that.fourKind())
            return -1;

        if (fullHouse() > that.fullHouse())
            return 1;
        if (fullHouse() < that.fullHouse())
            return -1;

        if (flush() > that.flush())
            return 1;
        if (flush() < that.flush())
            return -1;

        if (straight() > that.straight())
            return 1;
        if (straight() < that.straight())
            return -1;

        if (threeKind() > that.threeKind())
            return 1;
        if (threeKind() < that.threeKind())
            return -1;

        if (twoPair() > that.twoPair())
            return 1;
        if (twoPair() < that.twoPair())
            return -1;

        if (secondPair() > that.secondPair())
            return 1;
        if (secondPair() < that.secondPair())
            return -1;

        if (onePair() > that.onePair())
            return 1;
        if (onePair() < that.onePair())
            return -1;

        return higherCard(that);
    }

    private int higherCard(Hand that) {
        if (cards.get(0).getRank() > that.cards.get(0).getRank())
            return 1;
        if (cards.get(0).getRank() < that.cards.get(0).getRank())
            return -1;

        if (cards.get(1).getRank() > that.cards.get(1).getRank())
            return 1;
        if (cards.get(1).getRank() < that.cards.get(1).getRank())
            return -1;

        if (cards.get(2).getRank() > that.cards.get(2).getRank())
            return 1;
        if (cards.get(2).getRank() < that.cards.get(2).getRank())
            return -1;

        if (cards.get(3).getRank() > that.cards.get(3).getRank())
            return 1;
        if (cards.get(3).getRank() < that.cards.get(3).getRank())
            return -1;

        if (cards.get(4).getRank() > that.cards.get(4).getRank())
            return 1;
        if (cards.get(4).getRank() < that.cards.get(4).getRank())
            return -1;
        return 0;
    }

    @Override
    public int hashCode() {
        return String.format("%s%s%s%s%s",
                cards.get(0),
                cards.get(1),
                cards.get(2),
                cards.get(3),
                cards.get(4))
                .hashCode();
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof Hand && hashCode() == o.hashCode();
    }

    ArrayList<Card> getCards() {
        return cards;
    }

    int straightFlush() {
        return straight() != -1 && flush() != -1 ? highCard().getRank() : -1;
    }

    int flush() {
        boolean flush = cards.stream().allMatch(card -> card.getSuit() == highCard().getSuit());
        return flush ? highCard().getRank() : -1;
    }

    int straight() {
        for (int i = 1; i < 4; i++) {
            int prev = cards.get(i - 1).getRank();
            int mid = cards.get(i).getRank();
            int next = cards.get(i + 1).getRank();

            if (i == 1 && cards.get(0).getRank() == 12 && cards.get(4).getRank() == 0)
                continue;// Ace and Two at the beginning

            if ((prev + next) % 2 != 0)
                return -1;
            if (mid != (prev + next) / 2)
                return -1;
            if (mid == next || mid == prev)
                return -1;
        }
        return cards.get(1).getRank() == 3 ? cards.get(1).getRank() : highCard().getRank();
    }


    int fourKind() {
        for (int i = 0; i < 2; i++) {
            int c1 = cards.get(i).getRank();
            int c2 = cards.get(i + 1).getRank();
            int c3 = cards.get(i + 2).getRank();
            int c4 = cards.get(i + 3).getRank();
            if (c1 == c2 && c2 == c3 && c3 == c4)
                return c4;
        }
        return -1;
    }

    int fullHouse() {
        int ret = threeKind();
        if (ret == -1)
            return -1;

        for (int i = 0; i < 4; i++) {
            int c1 = cards.get(i).getRank();
            int c2 = cards.get(i + 1).getRank();
            if (c1 != ret && c1 == c2) {
                return ret;
            }
        }
        return -1;
    }

    int threeKind() {
        for (int i = 0; i < 3; i++) {
            int c1 = cards.get(i).getRank();
            int c2 = cards.get(i + 1).getRank();
            int c3 = cards.get(i + 2).getRank();
            if (c1 == c2 && c2 == c3)
                return c3;
        }
        return -1;
    }

    int twoPair() {
        ArrayList<Integer> pairs = new ArrayList<>();
        for (int i = 0; i < 4; i++) {
            int c1 = cards.get(i).getRank();
            int c2 = cards.get(i + 1).getRank();
            if (c1 == c2) {
                i++; //to avoid three of kind
                pairs.add(c1);
            }
        }
        return pairs.size() == 2 ? pairs.get(0) : -1;
    }

    int onePair() {
        int dup = 0;
        int ret = -1;
        for (int i = 0; i < 4; i++) {
            int c1 = cards.get(i).getRank();
            int c2 = cards.get(i + 1).getRank();
            if (c1 == c2) {
                i++;
                ret = c1;
                dup++;
            }
        }
        return dup == 1 ? ret : -1;
    }

    private int secondPair() {
        int dup = 0;
        int ret = -1;
        for (int i = 0; i < 4; i++) {
            int c1 = cards.get(i).getRank();
            int c2 = cards.get(i + 1).getRank();
            if (c1 == c2) {
                i++;
                ret = c1;
                dup++;
            }
        }
        return dup == 2 ? ret : -1;
    }

    private Card highCard() {
        return cards.get(0);
    }
}


