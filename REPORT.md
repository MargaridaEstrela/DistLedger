# DistLedger

Distributed Systems Project 2022/2023

### Resume
As a user, each operation is associated with a timestamp (TS), which is initialized at user creation. When an admin executes the gossip command with a specific TS, the server goes through a list of pending operations and tries to execute them one by one. The list is processed until it's impossible to complete any operation because the condition prevTS <= valueTS isn't met. We also use an extra atribute (stable) in the operation with the objective of allowing the server to send only the messages that have not been sent with gossip.

To allow adding an extra server while the others are still running, the naming server returns the number of servers for a specific service it knows. If that number is greater than 2, we're in the situation where we want to add a new server. In this case, the server (still in the main program) will request a full ledger from another server. It goes server by server until it gets a response. If it can't get a response, the server simply shuts down.

Adding new servers can cause issues for timestamps with a constant size. To avoid this, we decided to use a timestamp that adapts its size. If an operation has a TS greater than the current TS, we add 0's until they have the same size. Since the server didn't know about the existence of the other server, it hadn't received updates from it.
