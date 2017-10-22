# uf-type-messenger
Messenger client/server solution for communication within UF TYPE students.

To Run
Server: mvn exec:java -Dexec.mainClass="com.uftype.messenger.server.Server"
Client: mvn exec:java -Dexec.mainClass="com.uftype.messenger.client.Client"

To Assemble
Need to have dependencies in the final jar:
mvn assembly:assembly -DdescriptorId=jar-with-dependencies