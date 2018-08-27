package com.template;

import co.paralleluniverse.fibers.Suspendable;
import net.corda.core.contracts.*;
import net.corda.core.flows.*;
import net.corda.core.identity.Party;
import net.corda.core.flows.IdentitySyncFlow;
import net.corda.core.identity.PartyAndCertificate;
import net.corda.core.serialization.CordaSerializable;
import net.corda.core.transactions.SignedTransaction;
import net.corda.core.transactions.TransactionBuilder;
import net.corda.core.utilities.ProgressTracker;
import net.corda.core.utilities.ProgressTracker.Step;
import com.google.common.collect.ImmutableList;
import net.corda.confidential.IdentitySyncFlow

import java.security.PublicKey;
import java.util.*;


public class TwoPartyTradeFlow {

    @CordaSerializable
    public static class SellerTradeInfo {
        public final Integer price;
        public final Party payToIdentity;

        public SellerTradeInfo(Integer price, Party payToIdentity) {
            this.price = price;
            this.payToIdentity = payToIdentity;
        }
    }

    @InitiatingFlow
    @StartableByRPC
    public static class Seller extends FlowLogic<SignedTransaction> {
        private final FlowSession otherSession;
        private final StateAndRef<OwnableState> assetToSell;
        private final Integer price;
        private final Party myParty;

        private final Step AWAITING_PROPOSAL = new Step("Awaiting transaction proposal.");
        private final Step VERIFYING_AND_SIGNING = new Step("Verifying and signing transaction proposal.") {
            @Override
            public ProgressTracker childProgressTracker() { return SignTransactionFlow.Companion.tracker(); }
        };

        private final ProgressTracker progressTracker = new ProgressTracker(
                AWAITING_PROPOSAL,
                VERIFYING_AND_SIGNING
        );

        @Override
        public ProgressTracker getProgressTracker() {
            return progressTracker;
        }

        public Seller(FlowSession otherSession,
                      StateAndRef<OwnableState> assetToSell,
                      Integer price
                      ) {
            this.otherSession = otherSession;
            this.assetToSell = assetToSell;
            this.price = price;
            this.myParty = getOurIdentity();
        }

        @Suspendable
        @Override
        public SignedTransaction call() throws FlowException {
            progressTracker.setCurrentStep(AWAITING_PROPOSAL);

            final SellerTradeInfo hello = new SellerTradeInfo(price, myParty);

            subFlow(SendStateAndRefFlow(otherSession, ImmutableList(assetToSell)));

            progressTracker.setCurrentStep(VERIFYING_AND_SIGNING);

            subFlow(IdentitySyncFlow.Receive(otherSession));

            SignTransactionFlow signTransactionFlow = new SignTransactionFlow(otherSession, VERIFYING_AND_SIGNING.childProgressTracker()) {
                @Override
                protected void checkTransaction(SignedTransaction stx) {
                    // do nothing
                }
            };

            return waitForLedgerCommit(subFlow(signTransactionFlow).id);
        }
    }


}