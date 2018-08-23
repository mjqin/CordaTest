# CordaTest

This is a simple modification from (Corda template)[https://github.com/corda/cordapp-template-java].

## Description

Basiclly, there are three nodes:

* Notary: provide notary services for bank and client
* Bank: start the first tx flow to issue some value to client through StateA
* Client: start the second tx flow to crate StateB and StateC

From the second tx flow, we can test almost all the funciton related to `Command`, `Contract` and `State` that corda provide.
