package com.template;

import com.google.common.collect.ImmutableList;
import net.corda.core.identity.Party;

import net.corda.core.contracts.LinearState;
import net.corda.core.contracts.ContractState;
import net.corda.core.identity.AbstractParty;

import java.util.Collections;
import java.util.List;

/**
 * Define your state object here.
 */
public class StateA implements ContractState {
    private final int value;
    private final Party client;
    private final Party bank;

    public StateA(int value, Party client, Party bank) {
        this.value = value;
        this.client = client;
        this.bank = bank;
    }


    public int getValue() {
        return value;
    }

    public Party getClient() {
        return client;
    }

    public Party getBank() {
        return bank;
    }

    /** The public keys of the involved parties. */
    @Override public List<AbstractParty> getParticipants() {
        return ImmutableList.of(client, bank);
    }
}