package com.template;

import com.google.common.collect.ImmutableList;
import net.corda.core.identity.Party;

import net.corda.core.contracts.ContractState;
import net.corda.core.identity.AbstractParty;

import java.util.Collections;
import java.util.List;

/**
 * Define your state object here.
 */
public class StateB implements ContractState {
    private final int value;
    private final int amountOfApple;
    private final Party owner;

    public StateB(int value, int amountOfApple, Party owner) {
        this.value = value;
        this.amountOfApple = amountOfApple;
        this.owner = owner;
    }

    public int getValue() {
        return value;
    }

    public Party getOwner() {
        return owner;
    }

    public int getAmountOfApple() { return amountOfApple; }

    /** The public keys of the involved parties. */
    @Override public List<AbstractParty> getParticipants() {
        return ImmutableList.of(owner);
    }
}