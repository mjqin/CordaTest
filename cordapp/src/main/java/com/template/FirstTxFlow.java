package com.template;

import co.paralleluniverse.fibers.Suspendable;
import com.google.common.collect.ImmutableList;
import net.corda.core.contracts.Command;
import net.corda.core.contracts.StateAndContract;
import net.corda.core.flows.*;
import net.corda.core.identity.Party;
import net.corda.core.transactions.SignedTransaction;
import net.corda.core.transactions.TransactionBuilder;
import net.corda.core.utilities.ProgressTracker;

import java.security.PublicKey;
import java.util.List;

/**
 * Define your flow here.
 */
@InitiatingFlow
@StartableByRPC
public class FirstTxFlow extends FlowLogic<Void>{
    private final Integer value;
    private final Party client;

    /**
     * The progress tracker
     */
    private final ProgressTracker progressTracker = new ProgressTracker();

    public FirstTxFlow(Integer value, Party client){
        this.value = value;
        this.client = client;
    }

    @Override
    public ProgressTracker getProgressTracker() {
        return progressTracker;
    }

    /**
     * The flow logic is encapsulated within the call() method
     */
    @Suspendable
    @Override
    public Void call() throws FlowException {
        final Party notary = getServiceHub().getNetworkMapCache().getNotaryIdentities().get(0);

        // We create a transaction builder.
        final TransactionBuilder txBuilder = new TransactionBuilder();
        txBuilder.setNotary(notary);

        // We create the transaction components.
        StateA outputState = new StateA(value, client, getOurIdentity());
        StateAndContract outputContractAndState = new StateAndContract(outputState, ContractA.A_CONTRACT_ID);
        List<PublicKey> requiredSigners = ImmutableList.of(getOurIdentity().getOwningKey());
        Command cmd = new Command<>(new ContractA.Commands.Create(), requiredSigners);

        // We add the items to the builder.
        txBuilder.withItems(outputContractAndState, cmd);

        // Verifying the transaction.
        txBuilder.verify(getServiceHub());

        // Signing the transaction.
        final SignedTransaction signedTx = getServiceHub().signInitialTransaction(txBuilder);

        // Finalising the transaction.
        subFlow(new FinalityFlow(signedTx));

        return null;
    }
}
