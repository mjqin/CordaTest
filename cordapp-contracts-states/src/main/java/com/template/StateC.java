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
public class StateC implements ContractState {
    private final int value;
    private final int amountOfOrange;
    private final Party owner;

    public StateC(int value, int amountOfOrange, Party owner) {
        this.value = value;
        this.amountOfOrange = amountOfOrange;
        this.owner = owner;
    }

    public int getValue() {
        return value;
    }

    public Party getOwner() {
        return owner;
    }

    public int getAmountOfOrange() { return amountOfOrange; }

    /** The public keys of the involved parties. */
    @Override public List<AbstractParty> getParticipants() {
        return ImmutableList.of(owner);
    }
}