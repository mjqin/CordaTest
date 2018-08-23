package com.template;

import co.paralleluniverse.fibers.Suspendable;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import net.corda.core.contracts.Command;
import net.corda.core.contracts.StateAndContract;
import net.corda.core.contracts.StateAndRef;
import net.corda.core.flows.*;
import net.corda.core.identity.Party;
import net.corda.core.transactions.SignedTransaction;
import net.corda.core.transactions.TransactionBuilder;
import net.corda.core.utilities.ProgressTracker;
import net.corda.core.node.services.Vault;
import net.corda.core.node.services.vault.QueryCriteria;
import net.corda.core.node.services.vault.QueryCriteria.VaultQueryCriteria;

import net.corda.core.contracts.ContractState;
import static net.corda.core.contracts.ContractsDSL.requireThat;

import java.security.PublicKey;
import java.util.List;

import org.slf4j.LoggerFactory;

/**
 * Define your flow here.
 */
@InitiatingFlow
@StartableByRPC
public class SecondTxFlow extends FlowLogic<Void>{
    private final Integer appleAmount;
    private final Integer orangeAmount;

    private final int priceOfApple = 5;
    private final int priceOfOrange = 10;

    private final Party otherParty;

    /**
     * The progress tracker
     */
    private final ProgressTracker progressTracker = new ProgressTracker();

    public SecondTxFlow(Integer appleAmount, Integer orangeAmount, Party otherParty){
        this.appleAmount = appleAmount;
        this.orangeAmount = orangeAmount;
        this.otherParty = otherParty;
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

        // get input state
        final StateAndRef<StateA> inputStateAndRef = getInputStateA();
        final StateA inputStateA = inputStateAndRef.getState().getData();

        int valueLeft = inputStateA.getValue() - priceOfApple * appleAmount - priceOfOrange * orangeAmount;
        if(valueLeft < 0) {
            throw new FlowException();
        }

        // We create the transaction components.
        StateB outputStateB = new StateB(valueLeft, appleAmount, getOurIdentity());
        StateC outputStateC = new StateC(valueLeft, orangeAmount, getOurIdentity());

        List<PublicKey> requiredSigners = ImmutableList.of(getOurIdentity().getOwningKey(), otherParty.getOwningKey());

        Command cmd1 = new Command<>(new ContractB.Create(), requiredSigners);
        Command cmd2 = new Command<>(new ContractC.Create(), requiredSigners);
        Command cmd3 = new Command<>(new ContractA.Commands.BuyStuff(), requiredSigners);

        // We add the items to the builder.
        txBuilder.addInputState(inputStateAndRef);
        txBuilder.addOutputState(outputStateB, ContractB.B_CONTRACT_ID);
        txBuilder.addOutputState(outputStateC, ContractC.C_CONTRACT_ID);
        txBuilder.addCommand(cmd1);
        txBuilder.addCommand(cmd2);
        txBuilder.addCommand(cmd3);

        // Verifying the transaction.
        txBuilder.verify(getServiceHub());

        // Signing the transaction.
        final SignedTransaction partSignedTx = getServiceHub().signInitialTransaction(txBuilder);

        FlowSession otherPartySession = initiateFlow(otherParty);
        final SignedTransaction fullySignedTx = subFlow(
                new CollectSignaturesFlow(partSignedTx, ImmutableSet.of(otherPartySession), CollectSignaturesFlow.Companion.tracker()));

        // Finalising the transaction.
        subFlow(new FinalityFlow(fullySignedTx));

        return null;
    }

    @InitiatedBy(SecondTxFlow.class)
    public static class Acceptor extends FlowLogic<SignedTransaction> {

        private final FlowSession otherPartyFlow;

        public Acceptor(FlowSession otherPartyFlow) {
            this.otherPartyFlow = otherPartyFlow;
        }

        @Suspendable
        @Override
        public SignedTransaction call() throws FlowException {

            class SignTxFlow extends SignTransactionFlow {
                private SignTxFlow(FlowSession otherPartyFlow, ProgressTracker progressTracker) {
                    super(otherPartyFlow, progressTracker);
                }

                @Override
                protected void checkTransaction(SignedTransaction stx) {
                    LoggerFactory.getLogger(Acceptor.class).info("Call responder to sign.");
                    requireThat(require -> {
                        ContractState output = stx.getTx().getOutputs().get(0).getData();
                        // do nothing right now
                        return null;
                    });
                }
            }

            return subFlow(new SignTxFlow(otherPartyFlow, SignTransactionFlow.Companion.tracker()));
        }
    }

    private StateAndRef<StateA> getInputStateA() throws FlowException {
        VaultQueryCriteria criteria = new VaultQueryCriteria(Vault.StateStatus.UNCONSUMED);
        List<StateAndRef<StateA>> states = getServiceHub().getVaultService().queryBy(StateA.class, criteria).getStates();
//        if (states.size() != 1) {
//            throw new FlowException();
//        }
        return states.get(0);
    }
}
