# ♕ BYU CS 240 Chess

This project demonstrates mastery of proper software design, client/server architecture, networking using HTTP and WebSocket, database persistence, unit testing, serialization, and security.

## 10k Architecture Overview

The application implements a multiplayer chess server and a command line chess client.

here is the URL for the sequence diagram:
https://sequencediagram.org/index.html?presentationMode=readOnly#initialData=C4S2BsFMAIFkFdygA7gIYE9ICdoGEALSAZ2IEIAoCtAY2AHtcA5SAd2gFVidq7HoAQpDTFgsEcBwA2ACwVkabKBogFAO2ABzbPXjJoAYnAhNBLdkzQAPDXrhGALgOsCYSAD4rAelv3s7imwFJRAVdWBoAGUcADceIMVlVTQNKNjQyApINQATeUTQ5I1tXX0jEzMAI3B4GBs7R2dXSU8fBv8KHLRgNEqRGAARbt7+rNyKYgUaTIoAXlmojFFIAFtoAHlkHG6QejViaHny02Bq2sNtSGyAbgBma67iIjyKFnYuHGgAWnc07DjsA5oAAlSCaEDLXAUIQScSQ2TfX7Rf44IEAGXo4LU0C80DREIiAHE0CtMsiAYi-jEMkDgbpJCDIABHWqiCjkjKUoY9PrcIGESA0ADWnG42HZ6WmXOGvMg-OwwgZH1wAAoQAAzaBqNgASgl-05P2g3JGfPwCu6MAAKvQhdl9dSpUaTbLacIcjjoAB1bBuDbAIi4E0OjJWL5fF39Byg4DwbDY+Bi6AgNTq+jQFIehh2-Yhp1G8mo0U4NQkmAAamgNpzxAdn3DvzexcB5sVMGVUXgNGmpDruAbgmEojhkmwsnRmJTne7JFrcwWAtI0GJpMOCzuDxEzyoTY7BdiRbwFoZK7JB-7SMlcugADUcBqMFXbdloJWAII5D0ABXQWHFHKdX5IzNG80GMLoGUrI822XMtXjYZtrHDKki0iLse2IdVEHAR9oMtD1T2hIcxAkaQZEpQsWwAKXoKdCIoex6H0YQaAIaBY3jKhoG46BdyTfcURbcQ7Tgeg4goHiUIvKkaVvMCQAgmBYDEzJJIAmBnRlKNOGQRTBHoRQ8jUq9vgEgEgQ4XT8P0wyJJ4yjTN+GFh1IscZAsqyGQEAzsBeSTnJI+FyLMothKUlS7O4hyzNk0DwMtUTxOMg1AONLSzUsvTvNs5LHQ0kKW0y6zst8yKpMc3iEOVDysp8l5shePj60vQS3WIEw1D7CiryBABRXJYNJPMNKA9LryKk8yyiHpY1raKjQCkcyKBU8NgBIA
## Modules

The application has three modules.

- **Client**: The command line program used to play a game of chess over the network.
- **Server**: The command line program that listens for network requests from the client and manages users and games.
- **Shared**: Code that is used by both the client and the server. This includes the rules of chess and tracking the state of a game.

## Starter Code

As you create your chess application you will move through specific phases of development. This starts with implementing the moves of chess and finishes with sending game moves over the network between your client and server. You will start each phase by copying course provided [starter-code](starter-code/) for that phase into the source code of the project. Do not copy a phases' starter code before you are ready to begin work on that phase.

## IntelliJ Support

Open the project directory in IntelliJ in order to develop, run, and debug your code using an IDE.

## Maven Support

You can use the following commands to build, test, package, and run your code.

| Command                    | Description                                     |
| -------------------------- | ----------------------------------------------- |
| `mvn compile`              | Builds the code                                 |
| `mvn package`              | Run the tests and build an Uber jar file        |
| `mvn package -DskipTests`  | Build an Uber jar file                          |
| `mvn install`              | Installs the packages into the local repository |
| `mvn test`                 | Run all the tests                               |
| `mvn -pl shared test`      | Run all the shared tests                        |
| `mvn -pl client exec:java` | Build and run the client `Main`                 |
| `mvn -pl server exec:java` | Build and run the server `Main`                 |

These commands are configured by the `pom.xml` (Project Object Model) files. There is a POM file in the root of the project, and one in each of the modules. The root POM defines any global dependencies and references the module POM files.

## Running the program using Java

Once you have compiled your project into an uber jar, you can execute it with the following command.

```sh
java -jar client/target/client-jar-with-dependencies.jar

♕ 240 Chess Client: chess.ChessPiece@7852e922
```
