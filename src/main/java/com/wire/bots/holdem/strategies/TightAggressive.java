package com.wire.bots.holdem.strategies;

import com.wire.bots.holdem.Action;
import com.wire.bots.holdem.game.Player;

public class TightAggressive extends BaseStrategy implements Strategy {
    public TightAggressive(Player bot) {
        super(bot);
    }

    @Override
    public Action action(Action cmd) {
        if (!flop()) {
            return Action.CALL;
        }

        // it was a raise
        if (call > 0) {
            if (getChance() > 80f && able())
                return Action.RAISE;
            else
                return Action.CALL;
        }

        // it was a call

        if (getChance() > 80f && able())
            return Action.RAISE;
        else
            return Action.CALL;
    }
}
