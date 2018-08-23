package com.template;

import net.corda.core.contracts.CommandData;
import net.corda.core.contracts.*;
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
public class ContractA implements Contract {
    public static final String A_CONTRACT_ID = "com.template.ContractA";

    // Our Create command.
    public interface Commands extends CommandData {
        class Create extends TypeOnlyCommandData implements Commands {
        }

        class BuyStuff extends TypeOnlyCommandData implements Commands {
        }
    }

    @Override
    public void verify(LedgerTransaction tx) {
        LoggerFactory.getLogger(ContractA.class).info("Call ContractA.");
        final CommandWithParties<Commands> command = requireSingleCommand(tx.getCommands(), Commands.class);
        final Commands commandData = command.getValue();
        final List<PublicKey> setOfSigners = command.getSigners();

        if (commandData instanceof Commands.Create) {
            verifyCreate(tx, setOfSigners);
        } else if (commandData instanceof  Commands.BuyStuff) {
            verifyBuyStuff(tx, setOfSigners);
        }

    }

    private void verifyBuyStuff(LedgerTransaction tx, List<PublicKey> signers) {
        // do nothing
    }

    private void verifyCreate(LedgerTransaction tx, List<PublicKey> signers){
        requireThat(check -> {

            check.using("No inputs needed when create a new value.", tx.getInputs().isEmpty());
            check.using("There should be one output state of type StateA.", tx.getOutputs().size() == 1);

            // constraints
            final StateA out = tx.outputsOfType(StateA.class).get(0);
            final Party client = out.getClient();
            final Party bank = out.getBank();
            check.using("The state value must be non-negative.", out.getValue() > 0);
            check.using("The client and the bank cannot be the same entity.", client != bank);

            // Constraints on the signers.
            check.using("Only need bank signature.", signers.size() == 1);
            check.using("The borrower and lender must be signers.", signers.contains(bank.getOwningKey()));

            return null;
        });
    }


}