package com.wire.bots.holdem;

import com.wire.bots.sdk.server.model.User;

import java.util.*;
import java.util.stream.Collectors;

class Table {
    private static final int INITIAL_SMALL_BLIND = 1;
    private static final int INITIAL_RAISE = 5;
    private static final int BLIND_INCREASE = 2;
    private static final int RAISE_INCREASE = 5;
    private final ArrayList<Player> players = new ArrayList<>();
    private final ArrayList<Card> board = new ArrayList<>();
    private Deck deck;
    private int pot;
    private int roundNumber;
    private int raise = INITIAL_RAISE;
    private int smallBlind = INITIAL_SMALL_BLIND;

    Table(Deck deck) {
        this.deck = deck;
    }

    Player addPlayer(User user, boolean bot) {
        return addPlayer(user.id, user.name, bot);
    }

    private Player addPlayer(String userId, String name, boolean bot) {
        Player player = new Player(userId, name, board);
        player.setBot(bot);
        players.add(player);

        if (players.size() == 1)
            player.setRole("SB");
        if (players.size() == 2)
            player.setRole("BB");

        return player;
    }

    void flopCard() {
        board.add(deck.drawFromDeck());
    }

    Player getWinner() {
        return players
                .stream()
                .filter(player -> !player.isFolded())
                .max(Comparator.naturalOrder())
                .orElse(null);
    }

    ArrayList<Card> getBoard() {
        return board;
    }

    Collection<Player> getPlayers() {
        return players;
    }

    Collection<Player> getActivePlayers() {
        return players.stream().filter(player -> !player.isFolded()).collect(Collectors.toList());
    }

    boolean isAllFolded() {
        return getActivePlayers().size() <= 1;
    }

    boolean fold(String userId) {
        Player player = getPlayer(userId);
        if (player != null && !player.isCalled()) {
            player.fold();
            return true;
        }
        return false;
    }

    void removePlayer(String userId) {
        players.removeIf(player -> player.getId().equals(userId));
    }

    Card dealCard(Player player) {
        Card card = deck.drawFromDeck();
        player.addCard(card);
        return card;
    }

    void shuffle() {
        roundNumber++;
        if (roundNumber % 3 == 0)
            smallBlind += BLIND_INCREASE;
        if (roundNumber % 5 == 0)
            raise += RAISE_INCREASE;
        deck = new Deck();
        board.clear();
        players.forEach(Player::reset);
    }

    void shiftRoles() {
        if (players.size() <= 1)
            return;

        if (players.size() == 2) {
            String tmp = players.get(0).getRole();
            players.get(0).setRole(players.get(1).getRole());
            players.get(1).setRole(tmp);
            return;
        }

        ListIterator<Player> iterator = players.listIterator();
        while (iterator.hasNext()) {
            Player player = iterator.next();
            if (player.getRole().equals("SB")) {
                player.setRole("");

                if (iterator.hasNext()) {
                    Player next = iterator.next();
                    next.setRole("SB");
                    if (iterator.hasNext()) {
                        next = iterator.next();
                        next.setRole("BB");
                    } else {
                        players.get(0).setRole("BB");
                    }
                } else {
                    players.get(0).setRole("SB");
                    players.get(1).setRole("BB");
                }

                return;
            }
        }
    }

    // Pay to the Player and flush the pot
    int flushPot(Player player) {
        int ret = pot;
        player.put(pot);
        pot = 0;
        return ret;
    }

    void blind(String userId) {
        Player player = getPlayer(userId);
        switch (player.getRole()) {
            case "SB":
                player.setCall(smallBlind);
                pot += player.take();
                player.setCall(smallBlind);
                break;
            case "BB":
                player.setCall(2 * smallBlind);
                pot += player.take();
                break;
            default:
                player.setCall(2 * smallBlind);
                break;
        }
    }

    int raise(String userId) {
        Player player = getPlayer(userId);
        if (player != null && !player.isCalled()) {
            int call = player.getCall();
            raiseCallers(raise);
            call(userId);
            return raise + call;
        }
        return -1;
    }

    int call(String userId) {
        Player player = getPlayer(userId);
        if (player != null && !player.isCalled()) {
            int take = player.take();
            pot += take;
            player.setCalled(true);
            return take;
        }
        return -1;
    }

    private void raiseCallers(int raise) {
        players.forEach(player -> {
            player.raiseCall(raise);
            player.setCalled(false);
        });
    }

    void newBet() {
        players.forEach(player -> {
            player.setCall(0);
            player.setCalled(false);
        });
    }

    boolean isAllCalled() {
        return players.stream().allMatch(Player::isCalled);
    }

    boolean isShowdown() {
        return board.size() == 5;
    }

    boolean isFlopped() {
        return !board.isEmpty();
    }

    int getRoundNumber() {
        return roundNumber;
    }

    int getPot() {
        return pot;
    }

    int getSmallBlind() {
        return smallBlind;
    }

    int getRaise() {
        return raise;
    }

    Player getPlayer(String userId) {
        return players.stream()
                .filter(player -> player.getId().equals(userId))
                .findAny()
                .orElse(null);
    }

    Collection<Player> collectPlayers() {
        List<Player> losers = players.stream().filter(player -> player.getChips() <= 0).collect(Collectors.toList());
        players.removeAll(losers);
        return losers;
    }

    String printPlayers() {
        StringBuilder sb = new StringBuilder();
        getPlayers().forEach(player -> sb.append(player.getNameWithRole()).append(" | "));
        return sb.toString();
    }
}
