package com.template;

import net.corda.core.contracts.CommandData;
import net.corda.core.contracts.Contract;
import net.corda.core.transactions.LedgerTransaction;

import com.google.common.collect.ImmutableList;
import net.corda.core.contracts.CommandWithParties;
import net.corda.core.identity.Party;

import java.security.PublicKey;
import java.util.List;

import static net.corda.core.contracts.ContractsDSL.requireSingleCommand;
import static net.corda.core.contracts.ContractsDSL.requireThat;

import org.slf4j.LoggerFactory;

/**
 * Define your contract here.
 */
public class ContractC implements Contract {
    public static final String C_CONTRACT_ID = "com.template.ContractC";

    public static final int priceOfApple = 5;
    public static final int priceOfOrange = 10;

    // Our Create command.
    public static class Create implements CommandData {
    }

    @Override
    public void verify(LedgerTransaction tx) {
        LoggerFactory.getLogger(ContractC.class).info("Call ContractC.");

        final CommandWithParties<ContractC.Create> command = requireSingleCommand(tx.getCommands(), ContractC.Create.class);

        requireThat(check -> {

            // get inputs
            List<StateA> stateAInputs = tx.inputsOfType(StateA.class);
            check.using("There must be one input of tpye StateA", stateAInputs.size() == 1);

            StateA inputStateA = stateAInputs.get(0);

            // constraints
            final StateB outB = tx.outputsOfType(StateB.class).get(0);
            final StateC outC = tx.outputsOfType(StateC.class).get(0);

            int totalAmout = outB.getAmountOfApple() * priceOfApple + outC.getAmountOfOrange() * priceOfOrange;

            check.using("The value must not be less than price of apple plus price of orange",
                    inputStateA.getValue() >= totalAmout);

            LoggerFactory.getLogger(ContractC.class).info("outC.Owner:" + outC.getOwner() + " inputStateA.Client:" + inputStateA.getClient());
        //    check.using("The owner must be myself.", outC.getOwner() == inputStateA.getClient());

            // Constraints on the signers.
            final List<PublicKey> signers = command.getSigners();
        //    check.using("Only need client signature.", signers.size() == 1);
        //    check.using("The borrower and lender must be signers.", signers.contains(outC.getOwner().getOwningKey()));

            return null;
        });
    }
}